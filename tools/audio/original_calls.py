"""Build a review-only audition from user-supplied, original recordings.

Never writes production resources, sounds.json, or a JAR. Source masters are
copied byte-for-byte. Audition WAVs use one fixed gain, without synthesis.
"""
from pathlib import Path
import hashlib
import html
import json
import shutil
import wave
import zipfile
import numpy as np

ROOT = Path(__file__).resolve().parents[2]
WORK = ROOT.parent / "work/audio-original-02"
OUT = ROOT / "modeling/audio/audit-02"
DELIVERY = ROOT.parent / "outputs/auditoria-voz-original-02"
SELECTION = [
    ("orpheus", "Orpheus", 136),
    ("pixie", "Pixie", 228),
    ("apsaras", "Apsaras", 115),
    ("jack_frost", "Jack Frost", 180),
    ("forneus", "Forneus", 233),
    ("chimera", "Chimera", 148),
    ("nekomata", "Nekomata", 216),
    ("thanatos", "Thanatos", 200),
]
RATE = 48000
GAIN_DB = -3.0
GAIN = 10 ** (GAIN_DB / 20)


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def save(path, samples):
    with wave.open(str(path), "wb") as f:
        f.setnchannels(1)
        f.setsampwidth(2)
        f.setframerate(RATE)
        f.writeframes(samples.astype("<i2").tobytes())


def db(value):
    return round(20 * np.log10(max(float(value), 1e-12) / 32768), 3)


def build():
    for folder in (OUT, OUT/"originals", OUT/"voices", DELIVERY):
        folder.mkdir(parents=True, exist_ok=True)
    catalogue = json.loads((WORK/"catalogue.json").read_text(encoding="utf-8"))
    by_id = {r["id"]:r for r in catalogue["files"]}
    entries = []
    reel = [np.zeros(int(.35*RATE), np.int16)]
    position = len(reel[0])
    for key, label, number in SELECTION:
        info = by_id[number]
        source = WORK/info["file"]
        assert digest(source) == info["sha256"]
        master = OUT/"originals"/f"{key}.wav"
        shutil.copyfile(source, master)
        assert digest(master) == info["sha256"]
        with wave.open(str(source), "rb") as f:
            assert (f.getframerate(),f.getnchannels(),f.getsampwidth()) == (RATE,1,2)
            raw = np.frombuffer(f.readframes(f.getnframes()), "<i2").copy()
        # Only a uniform gain reduction: do not trim phonemes or alter voice.
        audition = np.rint(raw.astype(np.float64)*GAIN).astype("<i2")
        assert len(raw) == len(audition)
        assert np.max(np.abs(audition.astype(np.int32))) < 32767
        target = OUT/"voices"/f"{key}.wav"
        save(target, audition)
        entry = dict(
            id=key, name=label, sourceIndex=number,
            sourceMember=info["archive_member"],
            original=f"originals/{key}.wav", audition=f"voices/{key}.wav",
            sourceSha256=info["sha256"], originalSha256=digest(master),
            auditionSha256=digest(target), originalFrames=len(raw),
            auditionFrames=len(audition), seconds=round(len(raw)/RATE,6),
            sourcePeakDbFS=db(np.max(np.abs(raw.astype(np.int32)))),
            sourceRmsDbFS=db(np.sqrt(np.mean(raw.astype(np.float64)**2))),
            sourceFullScaleSamples=int(np.count_nonzero((raw==32767)|(raw==-32768))),
            auditionPeakDbFS=db(np.max(np.abs(audition.astype(np.int32)))),
            gainDb=GAIN_DB, trimFrames=0, synthetic=False,
            reelStart=round(position/RATE,6),
            reelEnd=round((position+len(audition))/RATE,6),
        )
        entries.append(entry)
        reel.extend([audition,np.zeros(int(.8*RATE),np.int16)])
        position += len(audition)+int(.8*RATE)
    sequence=np.concatenate(reel)
    save(OUT/"chamadas-em-sequencia.wav",sequence)
    manifest=dict(
        date="2026-10-20",status="Original recordings — audition pending user approval",
        integrated=False,jarBuilt=False,language="en",game="Persona 3 Reload",
        character="Protagonist / Makoto Yuki",
        sourceArchive=catalogue["archive"],sourceArchiveSha256=catalogue["archive_sha256"],
        sourceUrl=catalogue["source_url"],sourceFilesInventoried=len(catalogue["files"]),
        provenance="User-supplied original game recordings; no synthesized voice",
        format=dict(codec="PCM signed 16-bit",sampleRate=RATE,channels=1),
        processing="Originals copied byte-for-byte. Auditions: fixed -3 dB gain only; no trimming, pitch, speed, EQ, denoise or added effects.",
        identification="Batch transcription followed by two unprompted isolated ASR passes and contextual review. Proper-name spellings require correction; listening approval remains with user.",
        calls=entries,
        sequence=dict(file="chamadas-em-sequencia.wav",seconds=round(len(sequence)/RATE,6),
                      sha256=digest(OUT/"chamadas-em-sequencia.wav"),gapSeconds=.8),
    )
    (OUT/"manifest.json").write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+"\n",encoding="utf-8")
    shutil.copyfile(WORK/"verification-names.json",OUT/"identification-evidence.json")
    validation=dict(
        sourceMastersByteIdentical=all(e["sourceSha256"]==e["originalSha256"] for e in entries),
        allFrameCountsPreserved=all(e["originalFrames"]==e["auditionFrames"] for e in entries),
        noNewFullScaleSamples=True,sourceFullScaleSamples=sum(e["sourceFullScaleSamples"] for e in entries),
        fixedGainDb=GAIN_DB,synthesizedCalls=0,selectedCalls=len(entries),
        namesAuditionStatus="Awaiting user listening review; no claim of in-game validation",
    )
    (OUT/"validation.json").write_text(json.dumps(validation,indent=2)+"\n",encoding="utf-8")
    rows=[]
    cards=[]
    for index,e in enumerate(entries,1):
        rows.append(f'| {e["name"]} | `{e["sourceMember"]}` | {e["seconds"]:.3f} s |')
        cards.append(f'''<article><header><span>{index:02}</span><h2>{html.escape(e["name"])}</h2></header>
<audio controls preload="none" src="{e["audition"]}" aria-label="{html.escape(e["name"])}"></audio>
<p class="meta">{e["seconds"]:.2f} s · gravação original · volume −3 dB</p>
<details><summary>Comparar com o arquivo recebido</summary>
<audio controls preload="none" src="{e["original"]}" aria-label="Original {html.escape(e["name"])}"></audio>
<p class="meta">Arquivo {e["sourceIndex"]} · volume original, sem processamento</p></details></article>''')
    readme=f"""# Auditoria sonora 02 — voz original do protagonista

20/10/2026 · Inglês · Aguardando avaliação · Sem integração ao mod.

As oito guias sintéticas da auditoria 01 foram substituídas nesta prévia por
gravações do ZIP enviado pelo usuário. O pacote recebido contém 301 WAVs
individuais, PCM de 16 bits, mono, 48 kHz, totalizando 242,56 segundos.

## Ouvir

Abra `index.html` ou `chamadas-em-sequencia.wav`. A ordem é Orpheus, Pixie,
Apsaras, Jack Frost, Forneus, Chimera, Nekomata e Thanatos. Há 0,8 s de silêncio
entre chamadas. Cada card permite comparar a prévia e o arquivo original.

| Persona | Arquivo no ZIP | Duração |
|---|---|---|
{chr(10).join(rows)}

## Preservação e identificação

`originals/` contém cópias idênticas aos arquivos recebidos, com hashes conferidos.
`voices/` e a sequência receberam somente redução uniforme de 3 dB, preservando
as diferenças naturais de intensidade. Sem síntese, alteração de voz, afinação,
mudança de velocidade, cortes, filtros ou efeitos adicionados. A redução de ganho
não pretende restaurar eventuais amostras já saturadas na gravação recebida.

A identificação cruzou uma transcrição do conjunto, duas transcrições isoladas
sem sugestão de nomes e o contexto das chamadas vizinhas no banco. Nomes próprios
geram grafias automáticas incorretas, como “Fornius/Fortius” para Forneus;
`identification-evidence.json` preserva os resultados sem transformá-los em
alegação de revisão auditiva humana. A aprovação da escuta ainda está pendente.
Os originais não foram recortados: cada arquivo já contém uma chamada completa.

Fonte: [The Sounds Resource — Protagonist, Battle Voices (English)]({catalogue["source_url"]}).
O usuário forneceu diretamente o ZIP após o bloqueio do download automático.
SHA-256 do ZIP: `{catalogue["archive_sha256"]}`.
`manifest.json` registra nomes, origem, duração, ganho, posições na sequência e hashes.

## Integração posterior

Esta revisão valida a voz isolada. Os efeitos da auditoria 01 continuam sem
aprovação e não foram misturados às gravações. Na futura integração, tocar uma
chamada por ação somente quando a fraqueza causada pela própria Persona concede
One More, com causalidade por acerto e isolamento por sessão multiplayer.
Não usar crítico isolado, item, ataque básico ou nome da Persona selecionada
depois da ação como gatilho. O áudio não altera dano ou concessão de turnos.

Nenhum recurso de produção, registro de som ou JAR foi alterado nesta etapa.
"""
    (OUT/"README.md").write_text(readme,encoding="utf-8")
    page='''<!doctype html><html lang="pt-BR"><head><meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>Velvet Eclipse · Voz original</title><style>
:root{color-scheme:dark}*{box-sizing:border-box}body{margin:0;background:#071222;color:#edf7ff;font:16px/1.6 system-ui,sans-serif}
main{max-width:1120px;margin:auto;padding:44px 22px 60px}.eyebrow{color:#82edfa;font-size:13px;letter-spacing:.15em}
h1{font-size:clamp(30px,5vw,48px);line-height:1.12;margin:14px 0 18px}p{color:#baccde}.intro{max-width:760px}
.tag{display:inline-block;background:#163144;color:#a2f3d6;padding:5px 12px;border-radius:99px;font-size:13px;margin:6px 6px 12px 0}
.sequence{margin:26px 0;background:#12314a;border-left:4px solid #7df2ff;padding:22px}.sequence h2{margin:0 0 10px}
.grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(290px,1fr));gap:16px}article{background:#112338;border:1px solid #274057;border-radius:8px;padding:20px}
header{display:flex;align-items:center;gap:14px;margin-bottom:16px}header span{color:#73dcea;font-size:13px}h2{font-size:21px;margin:0}
audio{width:100%;display:block}.meta{font-size:13px;color:#9cb5ca;margin:9px 0 0}details{margin-top:14px}summary{cursor:pointer;color:#8fd9ed;font-size:13px}
details audio{margin-top:14px}a{color:#8eefff}footer{margin-top:32px;font-size:14px}.order{font-size:14px;line-height:1.8}
</style></head><body><main><div class="eyebrow">VELVET ECLIPSE / AUDITORIA SONORA 02</div>
<h1>As chamadas, com a voz original.</h1><p class="intro">Gravações do protagonista de Persona 3 Reload, retiradas do arquivo que você enviou. Ouça as oito chamadas para avaliar a voz e a intensidade antes da integração.</p>
<span class="tag">Inglês</span><span class="tag">Sem voz sintética</span><span class="tag">Prévia para avaliação</span>
<section class="sequence"><h2>Ouvir todas em sequência</h2><p class="order">Orpheus → Pixie → Apsaras → Jack Frost → Forneus → Chimera → Nekomata → Thanatos</p>
<audio controls preload="metadata" src="chamadas-em-sequencia.wav" aria-label="Todas as chamadas em sequência"></audio>
<p class="meta">Apenas as falas, separadas por pequenas pausas. Mesma redução de volume em todas.</p></section>
<div class="grid">'''+''.join(cards)+'''</div><footer><p>Os arquivos originais estão preservados. A prévia aplica somente −3 dB de volume, sem alterar o timbre, a velocidade ou a duração das falas.</p>
<p><a href="README.md">Documentação</a> · <a href="manifest.json">Origem dos arquivos</a></p><p>Áudios ainda não integrados ao mod. Os efeitos anteriores continuam em revisão.</p></footer></main>
<script>document.querySelectorAll('audio').forEach(a=>a.addEventListener('play',()=>{document.querySelectorAll('audio').forEach(b=>{if(a!==b)b.pause()})}))</script></body></html>'''
    (OUT/"index.html").write_text(page,encoding="utf-8")
    for source in OUT.rglob("*"):
        if source.is_file():
            dest=DELIVERY/source.relative_to(OUT)
            dest.parent.mkdir(parents=True,exist_ok=True)
            shutil.copyfile(source,dest)
    package=ROOT.parent/"outputs/velvet-eclipse-vozes-originais-auditoria-02.zip"
    with zipfile.ZipFile(package,"w",zipfile.ZIP_DEFLATED) as z:
        for p in sorted(DELIVERY.rglob("*")):
            if p.is_file():
                z.write(p,Path(DELIVERY.name)/p.relative_to(DELIVERY))
    report=dict(validation=validation,sequenceSeconds=manifest["sequence"]["seconds"],
                delivery=str(DELIVERY),zip=str(package),zipSha256=digest(package))
    (WORK/"delivery.json").write_text(json.dumps(report,indent=2)+"\n",encoding="utf-8")
    print(json.dumps(report,ensure_ascii=False,indent=2))


if __name__=="__main__":
    build()
