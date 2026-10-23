"""Build a local listening audit from the user's extracted P3R cue sheets.

Never writes to the installation or production mod resources. Prerequisites:
retoc to-legacy with the CriData/CueSheet and DatSkill filters; vgmstream r2117.
The extraction inventory and source assets remain in work/p3r-audio-04.
"""
from pathlib import Path
from concurrent.futures import ThreadPoolExecutor
import hashlib, html, json, math, re, shutil, struct, subprocess, wave, zipfile
import numpy as np

ROOT = Path(__file__).resolve().parents[2]
WORK = ROOT.parent / 'work/p3r-audio-04'
OUT = ROOT / 'modeling/audio/audit-04'
DELIVERY = ROOT.parent / 'outputs/auditoria-efeitos-originais-04'
VGM = WORK / 'tools/vgmstream-win64/vgmstream-cli.exe'
GAME = Path('C:/Program Files (x86)/Steam/steamapps/common/P3R')

def sha(p): return hashlib.sha256(p.read_bytes()).hexdigest()
def save(p, data): p.write_text(json.dumps(data, ensure_ascii=False, indent=2)+'\n', encoding='utf-8')
def read(p): return json.loads(p.read_text(encoding='utf-8-sig'))

# Original cue names identify function; these are proposals for the mod, not
# declarations that the engine's complete cue mixer has been reproduced.
SYSTEM = {
  37:('summon','Manifestação da Persona','Invocação',''),
  38:('evoker','Evoker — gatilho 00','Invocação',''),
  39:('evoker_alt','Evoker — gatilho 01','Invocação','Alternativa original; revisar a escolha.'),
  31:('persona_aura','Aura da Persona','Invocação',''),
  32:('persona_cutin','Aura do cut-in','Invocação',''),
  4:('one_more','One More — jogador','Combate e menus',''),
  3:('one_more_enemy','One More — inimigo','Combate e menus',''),
  10:('critical','Crítico','Combate e menus',''),
  11:('down','Queda do alvo','Combate e menus','Não é uma confirmação exclusiva de fraqueza.'),
  17:('battle_confirm','Confirmar ação','Combate e menus',''),
  22:('battle_target','Selecionar alvo','Combate e menus',''),
  186:('cursor','Cursor de menu','Combate e menus',''),
  187:('error','Ação inválida','Combate e menus',''),
  216:('cancel','Cancelar','Combate e menus',''),
  16:('item_use','Usar item','Itens','Som de uso original; recuperação HP/SP ainda requer escolha por função.'),
  82:('hit_enemy_strike','Impacto contundente no inimigo','Físicos',''),
  83:('hit_enemy_slash','Impacto de corte no inimigo','Físicos',''),
  84:('hit_enemy_stab','Impacto de perfuração no inimigo','Físicos',''),
  97:('strike_heavy','Golpe contundente — variação forte','Físicos',''),
  98:('strike_light','Golpe contundente — variação curta','Físicos','Candidato para o tapinha, com volume a revisar; não é um som exclusivo da Pixie.'),
  100:('slash_heavy','Espada — variação forte','Físicos',''),
  101:('slash_medium','Espada — variação média','Físicos',''),
  102:('slash_light','Espada — variação curta','Físicos',''),
  103:('pierce_heavy','Perfuração — variação forte','Físicos',''),
  105:('pierce_light','Perfuração — variação curta','Físicos',''),
  153:('swish_heavy','Movimento de ataque pesado','Físicos',''),
  165:('swish_medium','Movimento de ataque médio','Físicos',''),
  169:('swish_small','Movimento de ataque leve','Físicos','Candidato para a extensão da mão no tapinha.'),
  128:('cloth_soft','Movimento suave de roupa','Objetos','Proposta adaptada para sentar/levantar; não é um ruído identificado de cadeira.'),
  173:('sword_draw','Sacar espada','Físicos',''),
  175:('sword_sheath','Guardar espada','Físicos',''),
  33:('absorb','Absorção','Combate e menus',''),
  35:('reflect','Reflexão','Combate e menus',''),
}
DAILY = {
  22:('wood_open','Porta de madeira — abrir'),20:('wood_close','Porta de madeira — fechar'),
  30:('room_open','Porta do quarto — abrir'),28:('room_close','Porta do quarto — fechar'),
  32:('slide_open','Porta de correr — abrir'),31:('slide_close','Porta de correr — fechar'),
  7:('auto_open','Porta automática — abrir'),6:('auto_close','Porta automática — fechar'),
  40:('velvet_open','Velvet Room — abrir porta'),41:('velvet_open_alt','Velvet Room — alternativa'),
}
FEATURED_SKILLS = {10,20,30,40,50,56,64,70,78,91,110,125,139,155,165,170,180,190,200,201}

def source_banks():
    inventory = read(WORK/'acb-index.json'); selected=[]
    for e in inventory:
        p=e['asset']
        if '/Xrd777/' not in p: continue
        if Path(p).stem not in {'system','daily_se','dungeon_se'} and '/Battle/Skill/' not in p: continue
        asset=WORK/'cues'/p; b=asset.read_bytes(); offset=e['offset']; size=e['bytes']
        assert b[offset:offset+4]==b'@UTF'
        assert struct.unpack_from('>I',b,offset+4)[0]+8==size and offset+size<=len(b)
        name=('skill_' if '/Battle/Skill/' in p else '')+asset.stem+'.acb'
        target=WORK/'banks'/name; target.parent.mkdir(parents=True,exist_ok=True)
        target.write_bytes(b[offset:offset+size])
        selected.append(dict(e,bank=name,sourceAssetSha256=sha(asset),bankSha256=sha(target)))
    return selected

def decode(e):
    bank=WORK/'banks'/e['bank']; dest=OUT/'masters'/f"{e['id']}.wav"
    run=subprocess.run([str(VGM),'-i','-s',str(e['subsong']),'-W','4','-o',str(dest),str(bank)],capture_output=True)
    assert run.returncode==0,(e['id'],run.stderr.decode(errors='replace'))
    # Read float WAV without a heavyweight audio dependency; preserve native
    # rate/channels/frames and all source samples in the decoded master.
    b=dest.read_bytes(); assert b[:4]==b'RIFF' and b[8:12]==b'WAVE'
    pos=12; fmt=None; raw=None
    while pos+8<=len(b):
        tag=b[pos:pos+4]; n=struct.unpack_from('<I',b,pos+4)[0]; chunk=b[pos+8:pos+8+n]
        if tag==b'fmt ': fmt=chunk
        if tag==b'data': raw=chunk
        pos+=8+n+(n%2)
    assert fmt is not None and raw is not None
    codec,channels,rate,_,_,bits=struct.unpack_from('<HHIIHH',fmt)
    if codec==65534:codec=struct.unpack_from('<H',fmt,24)[0]
    assert codec==3 and bits==32,(codec,bits)
    x=np.frombuffer(raw,'<f4').reshape(-1,channels); assert np.isfinite(x).all()
    assert len(x)==e['frames'] and rate==e['rate'] and channels==e['channels']
    peak=float(np.abs(x).max()); rms=float(np.sqrt(np.mean(x.astype(np.float64)**2)))
    # Keep a fixed audition gain; if the source would clip, reduce further.
    gain=min(10**(-6/20),.98/max(peak,1e-12)); y=np.rint(x*gain*32767).astype('<i2')
    audition=OUT/'audio'/f"{e['id']}.wav"
    with wave.open(str(audition),'wb') as w:
        w.setnchannels(channels);w.setsampwidth(2);w.setframerate(rate);w.writeframes(y.tobytes())
    e.update(file='audio/'+audition.name,master='masters/'+dest.name,sha256=sha(audition),masterSha256=sha(dest),
        gainDb=20*math.log10(gain),peakDbFS=20*math.log10(max(peak*gain,1e-12)),
        sourcePeak=peak,sourceRms=rms,sourceOverFullScaleSamples=int(np.sum(np.abs(x)>1)),
        silent=peak<1e-5,trimmed=False,synthetic=False,mixedWithVideo=False)
    return e

def main():
    for p in [OUT,OUT/'audio',OUT/'masters',DELIVERY]:p.mkdir(parents=True,exist_ok=True)
    snapshot=[{'file':p.name,'size':p.stat().st_size,'mtimeNs':p.stat().st_mtime_ns} for p in sorted((GAME/'P3R/Content/Paks').iterdir()) if p.is_file()]
    banks=source_banks(); bankmap={e['bank']:e for e in banks}
    waves=read(WORK/'wave-catalogue.json'); names=read(WORK/'skill-names-en.json')
    modskills=read(ROOT/'src/main/resources/data/velvet_eclipse/catalogue/skills.json')
    by_name={s['name'].casefold():s for s in modskills}; entries=[]
    for w in waves:
        bank=w['bank']; idx=w['streamInfo']['index']; cue=w['streamInfo']['name']; note=''; featured=True; skill_id=None; mod_id=None
        if bank=='system.acb' and idx in SYSTEM:key,title,cat,note=SYSTEM[idx]
        elif bank=='daily_se.acb' and idx in DAILY:key,title=DAILY[idx];cat='Objetos'
        elif bank.startswith('skill_'):
            skill_id=int(bank[6:10]); original_name=names[skill_id]
            key=Path(bank).stem+f'_{idx:02}';title=original_name;cat='Habilidades'
            mod=by_name.get(original_name.casefold());mod_id=mod['id'] if mod else None
            featured=skill_id in FEATURED_SKILLS and idx==1
            if w['streamInfo']['total']>1:note=f"Variação/camada {idx} de {w['streamInfo']['total']}; o mixer do jogo não foi reproduzido."
            if not mod_id:note+=' Sem associação automática a uma habilidade do mod.'
        else:continue
        entries.append({'id':key,'name':title,'category':cat,'note':note.strip(),'featured':featured,'bank':bank,
          'subsong':idx,'cueNames':cue,'rate':w['sampleRate'],'channels':w['channels'],'frames':w['numberOfSamples'],
          'seconds':w['numberOfSamples']/w['sampleRate'],'encoding':w['encoding'],'loop':w['loopingInfo'],
          'sourceSkillId':skill_id,'modSkillId':mod_id,'source':bankmap[bank],
          'identification':'Original ACB cue names; skills cross-checked with the local English DatSkillNameDataAsset',
          'status':'Listening audit; no runtime binding or approval'})
    assert len({e['id'] for e in entries})==len(entries)
    with ThreadPoolExecutor(max_workers=4) as pool:entries=list(pool.map(decode,entries))
    # Do not offer placeholder silence as an effect. Retain it in the manifest.
    for e in entries:
        if e['silent']:e['featured']=False;e['note']='Silêncio no banco original; excluído das propostas.'
    names_asset=WORK/'skill-tables/P3R/Content/L10N/en/Xrd777/Blueprints/common/Names/DatSkillNameDataAsset.uexp'
    mismatches=[{'id':s['id'],'catalogueName':s['name'],'sourceId':s['source'],'installedName':names[s['source']]} for s in modskills if isinstance(s.get('source'),int) and 0<=s['source']<len(names) and names[s['source']].casefold()!=s['name'].casefold()]
    summary={'extractedCueAssets':4116,'indexedAcbBanks':len(read(WORK/'acb-index.json')),'inspectedSfxBanks':len(banks),
      'indexedWaves':len(waves),'auditionFiles':len(entries),'featured':sum(e['featured'] for e in entries),'silentPlaceholders':sum(e['silent'] for e in entries),
      'skillBanks':len({e['bank'] for e in entries if e['category']=='Habilidades'}),'voiceAuditPreserved':True,'integrated':False,'jarBuilt':False}
    manifest={'date':'2026-10-21','game':'Persona 3 Reload','installation':str(GAME),'summary':summary,
      'nameTableSha256':sha(names_asset),'nameTableAsset':names_asset.relative_to(WORK).as_posix(),
      'skillCatalogueDiscrepancies':mismatches,'entries':entries,
      'processing':'Float PCM masters at native rate/channels; audition -6 dB, extra headroom only if necessary; no edits, trim, pitch, resynthesis, or video separation.'}
    save(OUT/'manifest.json',manifest);save(OUT/'bank-provenance.json',banks)
    # Coverage is intentionally explicit about adaptations and missing cues.
    coverage={
      'Invocação':{'audition':['evoker','evoker_alt','summon','persona_aura','persona_cutin'],'pending':'Sincronizar gatilho, aura e manifestação; estilhaços não isolados como evento separado.'},
      'Físicos':{'audition':['swish_small','swish_medium','swish_heavy','hit_enemy_slash','hit_enemy_strike','hit_enemy_stab','strike_light'],'pending':'Tapinha é proposta de movimento leve + impacto curto; bloqueio/esquiva específicos ainda não identificados.'},
      'Magias':{'audition':[e['id'] for e in entries if e['category']=='Habilidades' and e['featured']],'pending':'Psíquico/nuclear do mod não foram associados a efeitos de outro elemento. Necessitam proposta adaptada; não encontrados como famílias próprias de Reload.'},
      'Itens':{'audition':['item_use','skill_0155_01','skill_0165_01','skill_0170_01','skill_0180_01'],'pending':'Uso original + candidatos Dia/Recarm/Patra/Tarukaja para o resultado. Recuperação de SP ainda sem efeito próprio confirmado; esses vínculos de item são adaptações.'},
      'Objetos':{'audition':[v[0] for v in DAILY.values()]+['cloth_soft'],'pending':'Cadeiras: roupa suave é uma proposta adaptada; rangido/trinco dedicados ainda não confirmados.'},
      'Combate e menus':{'audition':['one_more','one_more_enemy','critical','down','battle_confirm','battle_target','cursor','cancel','error'],'pending':'Não substituir fraqueza por som de crítico. One More precisa da condição causal confirmada por acerto.'}}
    save(OUT/'coverage.json',coverage)
    # Audition sequence, not a claimed recreation of original game timing.
    by_id={e['id']:e for e in entries}; pieces=[]
    for key in ['evoker','summon']:
        with wave.open(str(OUT/by_id[key]['file'])) as w:
            x=np.frombuffer(w.readframes(w.getnframes()),'<i2').reshape(-1,w.getnchannels())
            assert w.getframerate()==48000
            if x.shape[1]==1:x=np.repeat(x,2,axis=1)
            pieces.extend([x,np.zeros((int(.4*48000),2),dtype=np.int16)])
    with wave.open(str(OUT/'invocacao-sequencia.wav'),'wb') as w:
        w.setnchannels(2);w.setsampwidth(2);w.setframerate(48000);w.writeframes(np.concatenate(pieces).astype('<i2').tobytes())
    save(OUT/'validation.json',{'allDecoded':True,'allFramesPreserved':True,'allFinite':True,'noAuditionClipping':all(e['peakDbFS']<0 for e in entries),
       'noSyntheticAudio':True,'noVideoBackground':True,'inputMetadataUnchanged':snapshot==[{'file':p.name,'size':p.stat().st_size,'mtimeNs':p.stat().st_mtime_ns} for p in sorted((GAME/'P3R/Content/Paks').iterdir()) if p.is_file()],
       'inGameTest':False,'listeningApproval':False})
    render_page(entries,summary)
    (OUT/'README.md').write_text(f'''# Efeitos originais — auditoria 04

21/10/2026. Origem: instalação local de Persona 3 Reload informada pelo usuário.
Somente leitura dos arquivos do jogo; saída em pasta de trabalho. Nenhuma integração ao mod, novo JAR, commit ou publicação.

## Entrega

{len(entries)} arquivos de audição, {summary['featured']} destacados para a primeira revisão. {summary['skillBanks']} bancos de habilidades identificados pelo código e pela tabela inglesa da própria instalação. Todas as amostras preservam duração, canais e taxa de amostragem. Masters float sem ganho; cópias PCM16 com −6 dB (ou margem extra se necessária). Nenhuma voz sintetizada, música de vídeo, remoção artificial de fundo ou corte das caudas.

Abra `index.html`: filtros, busca, áudio individual, comparação com o master e revisão local por amostra. “Invocação — sequência” junta gatilho e manifestação com intervalo de 0,4 s para escuta; não representa a sincronização final da animação. Chamadas originais de todas as Personas permanecem na auditoria 03.

## Identificação e limites

`manifest.json` registra cue original, banco, índice de subsom, asset de origem, hashes e medidas. Nomes das habilidades conferidos com `DatSkillNameDataAsset` em inglês. Detectadas diferenças do catálogo do mod: código 70 está nomeado Eiga no mod, mas é Eiha nesta instalação; 73 está nomeado Maeiga, mas é Maeiha. A auditoria usa os nomes da instalação e associa por nome exato, sem mudar o catálogo do mod.

O decoder exporta as ondas de um cue, não seu grafo completo de reprodução, ganho, aleatoriedade ou camadas. Variações/camadas são mostradas separadamente. A calibração de volumes e marcadores de animação permanece para a integração após aprovação. Silêncios originais são registrados e excluídos das propostas. A aprovação auditiva pertence ao usuário; não houve execução do Minecraft.

`coverage.json` separa material original identificado, propostas adaptadas e lacunas. Não afirmar que efeitos de cadeira, trinco, SP, bloqueio/esquiva, psíquico e nuclear foram confirmados. Roupa suave para sentar/levantar, impacto curto para tapinha e magias de cura para itens são propostas para revisão. Não chamar a combinação de fraqueza/One More antes da confirmação causal por alvo.

## Ferramentas e procedência

- [repak](https://github.com/trumank/repak), v0.2.3: cópia de bancos AWB selecionados.
- [retoc](https://github.com/trumank/retoc), v0.1.5: leitura dos containers e conversão dos assets selecionados.
- [vgmstream](https://github.com/vgmstream/vgmstream), r2117: leitura dos cues e decodificação de HCA/ADX.
- [Documentação de modding](https://animatedswine37.github.io/persona-modding-docs/docs/getting-started/extracting-files/): formato UE4.27 da instalação.

Os bancos completos extraídos ficam somente em `work/p3r-audio-04/`. O pacote da auditoria contém apenas as amostras selecionadas, os masters correspondentes e documentação; não é uma cópia da instalação.
''',encoding='utf-8')
    for f in OUT.rglob('*'):
        if f.is_file():
            target=DELIVERY/f.relative_to(OUT);target.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(f,target)
    archive=ROOT.parent/'outputs/velvet-eclipse-efeitos-originais-auditoria-04.zip'
    with zipfile.ZipFile(archive,'w',zipfile.ZIP_DEFLATED) as z:
        for f in sorted(DELIVERY.rglob('*')):
            if f.is_file():z.write(f,Path(DELIVERY.name)/f.relative_to(DELIVERY))
    report={'summary':summary,'output':str(DELIVERY),'zip':str(archive),'zipSha256':sha(archive)}
    save(WORK/'delivery.json',report);print(json.dumps(report,ensure_ascii=False,indent=2))

def render_page(entries, summary):
    # No external services or calls; review labels stay in this browser only.
    data=json.dumps(entries,ensure_ascii=False).replace('</','<\\/')
    page='''<!doctype html><html lang="pt-BR"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Velvet Eclipse · Efeitos originais</title><style>
:root{color-scheme:dark}*{box-sizing:border-box}body{margin:0;background:#071323;color:#edf8ff;font:16px/1.5 system-ui}main{max-width:1220px;margin:auto;padding:38px 24px 70px}h1{font-size:clamp(30px,5vw,48px);line-height:1.1;max-width:850px}h2{margin-top:36px}p{color:#b4cbdc}.eyebrow{color:#79e9ed;letter-spacing:.15em;font-size:12px}.note{border-left:3px solid #7ce5ec;padding:14px 18px;background:#14283e}.stats,.filters,.actions{display:flex;gap:12px;flex-wrap:wrap;align-items:center}.stats span{background:#15354b;padding:10px 14px;border-radius:5px}.stats b{color:#8bf4d5}input,select,button{font:inherit;color:inherit;background:#173047;border:1px solid #42617c;padding:10px;border-radius:5px}button{cursor:pointer}button:hover{background:#285474}button:disabled{opacity:.4;cursor:default}input{flex:1;min-width:180px}.filters{margin:18px 0}.grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(285px,1fr));gap:14px}article{background:#10243a;padding:18px;border:1px solid #28465f;border-radius:6px}h3{margin:0 0 8px;font-size:18px}audio{width:100%;margin:10px 0}.meta{font-size:12px;color:#9ab7c9;overflow-wrap:anywhere}.badge{font-size:12px;color:#9debd5}.pages{display:flex;gap:16px;align-items:center;margin:20px 0}.review{margin-top:12px;width:100%;font-size:13px}a{color:#88e3f5}#now{color:#9aeddb;min-height:28px}details{font-size:13px;margin-top:10px}footer{font-size:13px;margin-top:36px}
</style></head><body><main><div class="eyebrow">VELVET ECLIPSE / AUDITORIA SONORA 04</div><h1>Efeitos direto dos bancos de Reload.</h1><p>Invocação, combate, habilidades, itens e portas. Amostras isoladas para escolhermos o som de cada ação.</p><div class="stats"><span><b>__COUNT__</b> amostras</span><span><b>__FEATURED__</b> destaques</span><span><b>__BANKS__</b> bancos de habilidades</span></div><p class="note">Revisão de áudio: ainda sem integração ao mod. Os efeitos abaixo vêm dos arquivos do jogo instalado; as chamadas de voz continuam preservadas na auditoria 03.</p>
<h2>Invocação — sequência de audição</h2><p>Gatilho do Evoker → manifestação da Persona. O intervalo entre os sons serve para esta comparação; a sincronização da animação será ajustada depois.</p><audio controls preload="none" src="invocacao-sequencia.wav"></audio>
<h2>Ouvir e revisar</h2><div class="filters"><input id="search" type="search" placeholder="Buscar efeito, habilidade ou cue…" aria-label="Buscar efeito"><select id="category" aria-label="Categoria"><option value="all">Todas as categorias</option><option>Invocação</option><option>Físicos</option><option>Habilidades</option><option>Itens</option><option>Objetos</option><option>Combate e menus</option></select><select id="scope" aria-label="Seleção"><option value="featured">Destaques para revisão</option><option value="all">Todas as amostras</option></select></div><div class="actions"><button id="play">Ouvir seleção</button><button id="stop">Parar</button><button id="export">Exportar minhas marcações</button><span id="count"></span></div><p class="meta">As marcações ficam neste navegador. Exporte o JSON e envie no chat para compartilhar sua revisão.</p><div id="now" aria-live="polite"></div><div id="cards" class="grid"></div><div class="pages"><button id="prev">Anterior</button><span id="page"></span><button id="next">Próxima</button></div>
<h2>O que ainda precisa de decisão</h2><p>O tapinha pode combinar movimento leve com impacto curto. Para sentar e levantar, há uma proposta de roupa suave. Os efeitos exclusivos de cadeira, trinco, recuperação de SP, bloqueio/esquiva, psíquico e nuclear ainda não estão confirmados.</p><p>As habilidades com mais de uma onda mostram variações ou camadas separadas. Não reproduzimos aqui o mixer completo do jogo. Os masters permitem comparar a versão sem redução de volume.</p><footer><a href="README.md">Documentação</a> · <a href="manifest.json">Origem de cada amostra</a> · <a href="coverage.json">Cobertura e pendências</a> · <a href="validation.json">Verificações offline</a></footer></main>
<script id="data" type="application/json">__DATA__</script><script>
const data=JSON.parse(document.getElementById('data').textContent()),$=id=>document.getElementById(id),PAGE=18,KEY='velvet-audio-audit04';let reviews={};try{reviews=JSON.parse(localStorage.getItem(KEY)||'{}')}catch{}let page=0,filtered=[],queue=[],index=0,timer=null,run=0;const player=new Audio();
const esc=s=>String(s).replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));const norm=s=>String(s).normalize('NFD').replace(/[\u0300-\u036f]/g,'').toLowerCase();
function halt(){run++;clearTimeout(timer);timer=null;player.pause();queue=[];$('now').textContent=''}
function pauseOther(active){document.querySelectorAll('audio').forEach(a=>{if(a!==active)a.pause()});if(active!==player)halt()}
function bind(){document.querySelectorAll('audio').forEach(a=>a.onplay=()=>pauseOther(a));document.querySelectorAll('[data-review]').forEach(s=>s.onchange=()=>{reviews[s.dataset.review]=s.value;try{localStorage.setItem(KEY,JSON.stringify(reviews))}catch{}})}
function card(e){let opts=['Pendente','Aprovado para proposta','Revisar','Não usar'].map(x=>`<option ${reviews[e.id]===x?'selected':''}>${x}</option>`).join('');return `<article><span class="badge">${esc(e.category)}${e.silent?' · silêncio original':''}</span><h3>${esc(e.name)}</h3><audio controls preload="none" src="${e.file}" aria-label="${esc(e.name)}"></audio><p class="meta">${e.seconds.toFixed(2)} s · ${e.rate/1000} kHz · ${e.channels===1?'mono':'estéreo'}<br>${esc(e.cueNames)} · onda ${e.subsong}</p>${e.note?`<p class="meta">${esc(e.note)}</p>`:''}<details><summary>Comparar master sem ganho</summary><audio controls preload="none" src="${e.master}"></audio></details><select class="review" data-review="${e.id}" aria-label="Revisão de ${esc(e.name)}">${opts}</select></article>`}
function render(){filtered=data.filter(e=>($('scope').value==='all'||e.featured)&&($('category').value==='all'||e.category===$('category').value)&&norm(e.name+' '+e.cueNames+' '+e.id).includes(norm($('search').value)));let pages=Math.max(1,Math.ceil(filtered.length/PAGE));page=Math.min(page,pages-1);$('cards').innerHTML=filtered.slice(page*PAGE,(page+1)*PAGE).map(card).join('')||'<p>Nenhuma amostra encontrada.</p>';$('count').textContent=filtered.length+' amostras';$('page').textContent=(page+1)+' / '+pages;$('prev').disabled=page===0;$('next').disabled=page>=pages-1;$('play').disabled=!filtered.length;bind()}
function nextAudio(){if(index>=queue.length){halt();return}let e=queue[index++],token=run;pauseOther(player);$('now').textContent='Ouvindo '+e.name+' · '+index+'/'+queue.length;player.src=e.file;player.play().catch(()=>{if(token===run){halt();$('now').textContent='Use o controle individual para iniciar o áudio.'}})}
player.onended=()=>{timer=setTimeout(()=>{timer=null;if(queue.length)nextAudio()},600)};$('play').onclick=()=>{halt();queue=filtered.filter(e=>!e.silent);index=0;nextAudio()};$('stop').onclick=()=>{halt();pauseOther(null)};
['search','category','scope'].forEach(id=>$(id).addEventListener(id==='search'?'input':'change',()=>{halt();page=0;render()}));$('prev').onclick=()=>{page--;render()};$('next').onclick=()=>{page++;render()};$('export').onclick=()=>{let url=URL.createObjectURL(new Blob([JSON.stringify({audit:4,reviews},null,2)],{type:'application/json'})),a=document.createElement('a');a.href=url;a.download='minha-revisao-audio-04.json';a.click();setTimeout(()=>URL.revokeObjectURL(url),1000)};render();
</script></body></html>'''
    page=page.replace('__COUNT__',str(summary['auditionFiles'])).replace('__FEATURED__',str(summary['featured'])).replace('__BANKS__',str(summary['skillBanks'])).replace('__DATA__',data)
    (OUT/'index.html').write_text(page,encoding='utf-8')

if __name__=='__main__':main()
