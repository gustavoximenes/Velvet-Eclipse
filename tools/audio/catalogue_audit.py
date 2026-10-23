"""All-Persona voice audition and original SFX research. No mod integration."""
from pathlib import Path
import csv,hashlib,html,json,re,shutil,wave,zipfile
import numpy as np

ROOT=Path(__file__).resolve().parents[2]
WORK=ROOT.parent/'work/audio-original-02'
OUT=ROOT/'modeling/audio/audit-03'
DELIVERY=ROOT.parent/'outputs/auditoria-audio-completo-03'
RATE=48000
GAIN=10**(-3/20)
APPROVED_MODELS={'orpheus','thanatos','arsene','izanagi','pixie','apsaras','jack_frost','forneus','chimera','nekomata'}
EXTRA_NAMES={'arsene':'Arsène','captain_kidd':'Captain Kidd','jack_o_lantern':"Jack-o'-Lantern",'cendrillon':'Cendrillon','loki_p5':'Loki (Persona 5)','magatsu_izanagi':'Magatsu-Izanagi','kamu_susano_o':'Kamu Susano-o'}

def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()

def write_wav(p,x):
    with wave.open(str(p),'wb') as w:
        w.setnchannels(1);w.setsampwidth(2);w.setframerate(RATE);w.writeframes(x.astype('<i2').tobytes())

def pcm(p):
    with wave.open(str(p),'rb') as w:
        assert (w.getframerate(),w.getnchannels(),w.getsampwidth())==(RATE,1,2)
        return np.frombuffer(w.readframes(w.getnframes()),'<i2').copy()

def main():
    for folder in (OUT,OUT/'originals',OUT/'voices',DELIVERY):folder.mkdir(parents=True,exist_ok=True)
    catalogue_path=ROOT/'src/main/resources/data/velvet_eclipse/catalogue/personas.json'
    game_catalogue=json.loads(catalogue_path.read_text(encoding='utf-8'))
    registered={r['id']:r['name'] for r in game_catalogue}
    registry=(ROOT/'src/main/java/com/gx/velveteclipse/core/persona/PersonaRegistry.java').read_text(encoding='utf-8')
    registered.update(dict(re.findall(r'persona\(\s*"([^"]+)"\s*,\s*"([^"]+)"',registry)))
    source=json.loads((WORK/'catalogue.json').read_text(encoding='utf-8'))
    files={r['id']:r for r in source['files']}
    rows=list(csv.DictReader((Path(__file__).parent/'persona_voice_map.tsv').open(encoding='utf-8'),delimiter='\t'))
    assert len({r['persona_id'] for r in rows})==len(rows)
    assert len({r['source_index'] for r in rows})==len(rows)
    mapped={r['persona_id'] for r in rows}
    missing={r['id'] for r in game_catalogue}-mapped
    assert not missing,missing
    legacy=sorted(set(registered)-mapped)
    assert set(legacy)=={'cinder_wisp','nocturne_knight','sylphid','lumen_hare','iron_oni','umbra_slime'}
    verification=json.loads((WORK/'verification-catalogue.json').read_text(encoding='utf-8'))
    batch=json.loads((WORK/'transcription-batch.json').read_text(encoding='utf-8'))
    context={}
    for b in batch:
        words=[w for s in b['segments'] for w in s['words']]
        for r in b['intervals']:
            context[r['id']]=''.join(w['word'] for w in words if r['start']<=(w['start']+w['end'])/2<=r['end']+.25).strip()
    generic=[('generic_persona','Persona!',33),('summon_persona_alt','Persona! — variação',34),
             ('summon_come_persona','Come, Persona!',36),('summon_persona_emphatic','Persona! — enfática',37)]
    rows += [{'source_index':str(n),'persona_id':k,'label':name,'generic':True} for k,name,n in generic]
    entries=[];samples={}
    for r in rows:
        key=r['persona_id'];n=int(r['source_index']);f=files[n];original=WORK/f['file']
        assert sha(original)==f['sha256']
        master=OUT/'originals'/f'{key}.wav';shutil.copyfile(original,master)
        raw=pcm(original);y=np.rint(raw.astype(np.float64)*GAIN).astype('<i2')
        assert np.abs(y.astype(np.int32)).max()<32767
        audition=OUT/'voices'/f'{key}.wav';write_wav(audition,y);samples[key]=y
        general=r.get('generic',False)
        evidence=verification.get(key,verification.get('general_'+str(n),{}))
        assert evidence.get('sourceIndex')==n,(key,n,evidence)
        entries.append({'id':key,'name':r.get('label',registered.get(key,EXTRA_NAMES.get(key,key.replace('_',' ').title()))),
            'sourceIndex':n,'sourceMember':f['archive_member'],'sourceSha256':f['sha256'],
            'original':'originals/'+master.name,'file':'voices/'+audition.name,
            'originalSha256':sha(master),'sha256':sha(audition),'seconds':len(raw)/RATE,'frames':len(raw),
            'peakDbFS':float(20*np.log10(max(1,np.abs(y.astype(np.int32)).max())/32768)),
            'scope':'summon' if general else ('mod' if key in registered else 'extra'),
            'model':'approved' if key in APPROVED_MODELS else 'pending',
            'asrIsolated':evidence.get('asrText',''),'asrContext':context.get(n,''),
            'identification':'Contextual phonetic mapping plus unprompted isolated ASR; user listening review pending',
            'spokenNameNote':'The original recording says Orpheus, without Telos.' if key=='orpheus_telos' else None,
            'gainDb':-3,'trimFrames':0,'synthetic':False})
    # Voice binding is based on Persona ID only. Never copy a renderer fallback
    # (e.g. cinder_wisp -> Thanatos) into a name call.
    bindings={key:{'voiceId':key if key in mapped else 'generic_persona',
                   'kind':'named' if key in mapped else 'generic',
                   'reason':None if key in mapped else 'Original mod character: no recorded proper name in Persona 3 Reload'} for key in sorted(registered)}
    binding_data={'status':'Audit proposal, not runtime registration','requiresModel':False,'unknownIdFallback':'generic_persona','bindings':bindings}
    (OUT/'bindings.json').write_text(json.dumps(binding_data,ensure_ascii=False,indent=2),encoding='utf-8')
    # Retain the existing eight source selections exactly while extending coverage.
    previous=json.loads((ROOT/'modeling/audio/audit-02/manifest.json').read_text(encoding='utf-8'))
    by_id={e['id']:e for e in entries}
    for e in previous['calls']:assert e['sourceSha256']==by_id[e['id']]['sourceSha256']
    assert np.array_equal(pcm(OUT/'originals/orpheus.wav'),pcm(OUT/'originals/orpheus_telos.wav'))
    showcase=['orpheus','pixie','apsaras','jack_frost','forneus','chimera','nekomata','thanatos','alice','black_frost','siegfried','beelzebub','lucifer','messiah','arsene','izanagi']
    reel=[np.zeros(int(.25*RATE),np.int16)]
    for key in showcase:reel += [samples[key],np.zeros(int(.6*RATE),np.int16)]
    write_wav(OUT/'amostra-catalogo.wav',np.concatenate(reel))
    summary={'cataloguePersonas':len(game_catalogue),'namedModPersonas':len(set(registered)&mapped),
             'legacyGenericBindings':len(legacy),'registeredPersonas':len(registered),'extraDlcCalls':len(mapped-set(registered)),
             'namedCalls':len(mapped),'genericSummonVariants':len(generic),'modelIndependent':True,
             'unassignedSourceIndices':[265],'integrated':False,'jarBuilt':False}
    manifest={'date':'2026-10-20','status':'AUDIT ONLY','language':'en','game':'Persona 3 Reload','summary':summary,
              'sourceArchive':source['archive'],'sourceArchiveSha256':source['archive_sha256'],'sourceUrl':source['source_url'],
              'catalogueSha256':sha(catalogue_path),'sampleRate':RATE,'channels':1,'bits':16,
              'processing':'Original masters byte-identical; audition fixed -3 dB; no trimming, synthesis, pitch or speed edits.',
              'calls':entries,'showcase':showcase}
    (OUT/'manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2),encoding='utf-8')
    report={'allCatalogueIdsCovered':not missing,'allRegisteredIdsBound':set(bindings)==set(registered),
            'all198MastersIdentical':all(e['originalSha256']==e['sourceSha256'] for e in entries),
            'allFramesPreserved':all(len(pcm(OUT/e['file']))==e['frames'] for e in entries),
            'noNewClipping':all(e['peakDbFS']<0 for e in entries),'legacyNeverUsesWrongNamedPersona':all(bindings[k]['kind']=='generic' for k in legacy),
            'previousEightSelectionsPreserved':True,'noModelDependency':True,'unclassified265Excluded':all(e['sourceIndex']!=265 for e in entries),
            'catalogueVoiceCount':len(mapped),'auditionStatus':'Requires listening review, not in-game verification'}
    assert all(v for v in report.values() if isinstance(v,bool))
    (OUT/'validation.json').write_text(json.dumps(report,indent=2),encoding='utf-8')
    sfx=json.loads((OUT/'sfx-manifest.json').read_text(encoding='utf-8'))
    slots={
      'Invocação':['Disparo do Evoker','Estilhaços','Materialização','Desaparecimento'],
      'Ataques físicos':['Preparação de corte','Impacto de corte','Impacto contundente','Perfuração','Tapinha','Erro/esquiva','Bloqueio'],
      'Magias':['Fogo','Gelo','Eletricidade','Vento','Luz','Trevas','Psíquico','Nuclear','Onipotente','Cura','Suporte','Alteração de estado'],
      'Itens':['Recuperar HP','Recuperar SP','Reanimar','Curar estado','Reforço'],
      'Objetos':['Abrir porta','Fechar porta','Trinco','Sentar','Levantar'],
      'Combate e menus':['Fraqueza','One More','Crítico','Selecionar','Confirmar','Cancelar','Ação inválida']}
    event_rows=[{'category':category,'event':event,'status':'Original cue bank and semantic identification pending'} for category,events in slots.items() for event in events]
    (OUT/'effect-coverage.json').write_text(json.dumps({'events':event_rows,'referenceClips':14,'finishedEventBindings':0},ensure_ascii=False,indent=2),encoding='utf-8')
    legacy_names=', '.join(registered[k] for k in legacy)
    (OUT/'README.md').write_text(f'''# Auditoria sonora 03 — catálogo completo e efeitos originais

20/10/2026. Prévia offline; nenhum som registrado no mod ou JAR gerado.

## Vozes

194 chamadas nominais do ZIP original: as 173 Personas do catálogo Reload,
Arsène e Izanagi, mais 19 chamadas DLC mantidas em reserva. As 175 Personas
nominais do mod estão cobertas, inclusive as sem modelo definitivo. O vínculo
usa o ID da Persona, sem depender da geometria, renderer ou modelo substituto.

Há quatro falas originais genéricas de invocação. Para as seis Personas autorais
legadas ({legacy_names}), `bindings.json` propõe a chamada genérica “Persona!”.
Esses nomes não existem no banco fornecido. Não há vozes fabricadas ou nomes de
outra Persona usados para encobrir a ausência. Total: 181 IDs registrados tratados.

Orpheus Telos usa o arquivo 137 do banco: seu PCM é idêntico ao 136 e a gravação
diz apenas Orpheus. Os dois Loki são mantidos separados (280 e 297). O arquivo
265 (“Universe!” na transcrição) ficou fora dos vínculos por não corresponder a
uma Persona do catálogo; não foi atribuído por aproximação.

Os originais em `originals/` são idênticos aos recebidos. Em `voices/`, apenas
ganho uniforme de −3 dB, sem cortes, sintetização, filtros, alteração de altura
ou de velocidade. Identificação por transcrição em contexto, passagem isolada
sem sugestão de nomes e conferência fonética/contextual. Grafias de nomes próprios
no ASR são imperfeitas e estão preservadas no manifesto; a aprovação de escuta
do usuário continua pendente. Não afirmar teste dentro do Minecraft.

Origem: [Protagonist — Battle Voices (English)]({source['source_url']}).
ZIP enviado pelo usuário, SHA-256 `{source['archive_sha256']}`.

## Efeitos: início da revisão

- Dois recortes da invocação do vídeo original enviado anteriormente pelo usuário:
  disparo/estilhaços (96,05–98,35 s) e manifestação (98,35–103,25 s).
  São referências da cena, com a mistura de fundo preservada; não são efeitos
  isolados prontos para produção.
- Doze trechos curtos da compilação de efeitos de sistema de Reload:
  https://www.youtube.com/watch?v=QjDwTf-i2TQ (revampedtelephone).
  A descrição informa 453 WAVs, mas o vídeo não fornece nomes nem limites dos
  arquivos. A análise detectou 289 regiões por silêncio, que **não** equivalem
  aos 453 efeitos originais. Doze regiões foram separadas para triagem, sem
  afirmar que sejam confirmar, curar, atacar ou outro evento específico.
- `sfx-manifest.json` registra intervalos, procedência, ganho −6 dB e fades de
  5 ms. A compressão do vídeo não foi apresentada como WAV original sem perdas.
- `effect-coverage.json` contém {len(event_rows)} eventos/famílias a obter e identificar:
  invocação, golpes, magias, itens, móveis, fraqueza/One More e menus. Nenhum foi
  preenchido automaticamente por um efeito desconhecido. Os sintéticos rejeitados
  da auditoria 01 não fazem parte desta revisão.

O usuário informou que ainda não tem Reload instalado e ofereceu instalá-lo.
Aguardar o caminho da instalação para verificar os bancos de efeitos acessíveis.
Não alterar o jogo ou prometer extração antes de verificar formatos disponíveis.
Alternativas PS2 pesquisadas são apenas referências, não foram misturadas como Reload:
https://sounds.spriters-resource.com/playstation_2/persona3/asset/399008/
https://sounds.spriters-resource.com/playstation_2/persona3/asset/448947/
https://sounds.spriters-resource.com/playstation_2/persona3/asset/448948/

## Uso após aprovação

Uma chamada por ação, somente quando a fraqueza da própria Persona concede
One More. Exigir causalidade por acerto, pois fraqueza e crítico podem ocorrer
em alvos distintos. Não tocar por item, crítico isolado, ataque básico ou ação
inválida. Capturar ID da Persona no início da ação e limitar áudio à sessão.
Efeitos físicos distinguem preparação e contato; itens e móveis só tocam após
a aplicação/alteração confirmada pelo servidor. Música e áudio não alteram dano,
dificuldade, turnos ou regras multiplayer.
''',encoding='utf-8')
    render_page(entries,summary,sfx,registered,legacy)
    for p in OUT.rglob('*'):
        if p.is_file():
            target=DELIVERY/p.relative_to(OUT);target.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(p,target)
    archive=ROOT.parent/'outputs/velvet-eclipse-audio-catalogo-completo-03.zip'
    with zipfile.ZipFile(archive,'w',zipfile.ZIP_DEFLATED) as z:
        for p in sorted(DELIVERY.rglob('*')):
            if p.is_file():z.write(p,Path(DELIVERY.name)/p.relative_to(DELIVERY))
    delivery={'summary':summary,'validation':report,'output':str(DELIVERY),'zip':str(archive),'zipSha256':sha(archive)}
    (WORK/'delivery-03.json').write_text(json.dumps(delivery,indent=2),encoding='utf-8')
    print(json.dumps(delivery,ensure_ascii=False,indent=2))

def render_page(entries,summary,sfx,registered,legacy):
    sfx_cards=''
    for e in sfx['entries']:
        note='Referência da cena; mantém áudio de fundo.' if e['kind']=='cinematic_reference' else 'Trecho de sistema; uso específico ainda em identificação.'
        sfx_cards+=f'<article><h3>{html.escape(e["name"])}</h3><audio controls preload="none" src="{e["file"]}"></audio><p class="meta">{note} {e["seconds"]:.2f} s</p></article>'
    data=json.dumps(entries,ensure_ascii=False).replace('</','<\\/')
    page='''<!doctype html><html lang="pt-BR"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Velvet Eclipse · Catálogo de vozes</title><style>
:root{color-scheme:dark}*{box-sizing:border-box}body{margin:0;background:#071222;color:#eef8ff;font:16px/1.55 system-ui}main{max-width:1200px;margin:auto;padding:35px 22px 60px}h1{font-size:clamp(30px,5vw,46px);line-height:1.1}h2{margin-top:32px}h3{margin:0 0 14px;font-size:18px}p{color:#bdd0df}.eyebrow{color:#83eaf3;font-size:13px;letter-spacing:.14em}.stats{display:flex;flex-wrap:wrap;gap:10px}.stat{background:#143249;padding:10px 16px;border-radius:5px}.stat b{color:#99f5e1}a{color:#8feafa}.note{padding:16px;background:#16283d;border-left:3px solid #7ce9fa}input,select,button{background:#182e45;color:#f3fbff;border:1px solid #3e637f;border-radius:5px;padding:10px;font:inherit}button{cursor:pointer}button:hover{background:#235574}button:disabled{opacity:.45;cursor:default}.filters{display:flex;gap:10px;flex-wrap:wrap;margin:20px 0 10px}.filters input{flex:1;min-width:210px}.grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(270px,1fr));gap:14px}article{padding:18px;background:#102338;border:1px solid #274259;border-radius:6px}.meta{font-size:12px;color:#9ab6cc}audio{width:100%;display:block}.pages{display:flex;align-items:center;gap:14px;margin:16px 0}.pill{font-size:11px;color:#9ceacc}.toplinks{display:flex;gap:20px;flex-wrap:wrap}.now{min-height:28px;color:#9df6dd}.detail{margin-top:12px;font-size:13px}details audio{margin-top:12px}footer{margin-top:34px;font-size:13px}
</style></head><body><main><div class="eyebrow">VELVET ECLIPSE / AUDITORIA SONORA 03</div><h1>Vozes para todo o catálogo.</h1><p>Chamadas originais em inglês, inclusive para as Personas que ainda aguardam modelo definitivo. Abaixo, o início da revisão dos efeitos de invocação e sistema.</p><div class="stats"><div class="stat"><b>175</b> Personas do mod com nome gravado</div><div class="stat"><b>19</b> chamadas DLC em reserva</div><div class="stat"><b>14</b> referências de efeitos</div></div><p class="note">Prévia para escuta. O áudio ainda não foi integrado ao mod.</p><nav class="toplinks"><a href="#vozes">Chamadas das Personas</a><a href="#invocacao">Vozes de invocação</a><a href="#efeitos">Efeitos originais</a><a href="README.md">Documentação</a></nav>
<h2>Amostra do catálogo ampliado</h2><p class="meta">Orpheus · Pixie · Apsaras · Jack Frost · Forneus · Chimera · Nekomata · Thanatos · Alice · Black Frost · Siegfried · Beelzebub · Lucifer · Messiah · Arsène · Izanagi</p><audio controls preload="none" src="amostra-catalogo.wav"></audio>
<h2 id="vozes">Chamadas das Personas</h2><div class="filters"><input id="search" type="search" placeholder="Buscar Persona…" aria-label="Buscar Persona"><select id="scope" aria-label="Catálogo"><option value="mod">Personas do mod</option><option value="all">Todas as chamadas</option><option value="extra">DLC em reserva</option></select><select id="model" aria-label="Modelo"><option value="all">Qualquer modelo</option><option value="pending">Modelo definitivo pendente</option><option value="approved">Modelo aprovado</option></select></div><div class="pages"><button id="play">Ouvir seleção</button><button id="stop">Parar</button><span id="count"></span></div><div id="now" class="now" aria-live="polite"></div><div id="cards" class="grid"></div><div class="pages"><button id="prev">Anterior</button><span id="page"></span><button id="next">Próxima</button></div>
<details class="note"><summary>Personas autorais antigas</summary><p>Os nomes de Cinder Wisp, Nocturne Knight, Sylphid, Lumen Hare, Iron Oni e Umbra Slime não existem neste banco. Para elas, a proposta é a chamada original genérica “Persona!”, sem falar o nome de outro personagem.</p><p>O áudio é vinculado ao ID da Persona, não ao modelo que a representa.</p></details>
<h2 id="invocacao">Vozes originais de invocação</h2><div id="generic" class="grid"></div>
<h2 id="efeitos">Primeiras referências de efeitos</h2><p>Os dois primeiros recortes vêm da cena original de invocação que você enviou. Os demais são trechos da compilação de sistema de Reload, ainda sem associação a uma função específica.</p><p class="note">Golpes, magias, itens e objetos aguardam os bancos separados do jogo. Estes recortes ajudam na revisão, mas ainda não são uma seleção final de efeitos para cada ação.</p><div class="grid">'''+sfx_cards+'''</div><footer><p>Vozes: originais preservados; prévias com redução uniforme de 3 dB. Referências de efeitos: ganho −6 dB e fades de 5 ms nas bordas.</p><p><a href="manifest.json">Manifesto de vozes</a> · <a href="bindings.json">Cobertura das Personas</a> · <a href="sfx-manifest.json">Origem dos efeitos</a> · <a href="effect-coverage.json">Eventos a completar</a></p></footer>
</main><script id="data" type="application/json">'''+data+'''</script><script>
const data=JSON.parse(document.getElementById('data').textContent()),$=id=>document.getElementById(id),PAGE=24;let page=0,filtered=[],queue=[],qi=0,nextTimer=null;const player=new Audio();
const norm=s=>s.normalize('NFD').replace(/[\u0300-\u036f]/g,'').toLowerCase();
const esc=s=>s.replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
function halt(){clearTimeout(nextTimer);nextTimer=null;player.pause();queue=[];$('now').textContent=''}
function pauseOthers(active){document.querySelectorAll('audio').forEach(a=>{if(a!==active)a.pause()});if(active!==player)halt()}
function bind(){document.querySelectorAll('audio').forEach(a=>{a.onplay=()=>pauseOthers(a)})}
function card(e){let label=e.scope==='extra'?'DLC em reserva':e.scope==='summon'?'Voz de invocação':e.model==='approved'?'Modelo aprovado':'Modelo definitivo pendente';return `<article><h3>${esc(e.name)}</h3><p class="pill">${label}</p><audio controls preload="none" src="${e.file}" aria-label="${esc(e.name)}"></audio><p class="meta">${e.seconds.toFixed(2)} s · original em inglês</p>${e.spokenNameNote?'<p class="meta">A gravação original diz apenas Orpheus.</p>':''}<details class="detail"><summary>Comparar original</summary><audio controls preload="none" src="${e.original}"></audio><p class="meta">Arquivo ${e.sourceIndex} do banco recebido.</p></details></article>`}
function render(){filtered=data.filter(e=>e.scope!=='summon'&&($('scope').value==='all'||e.scope===$('scope').value)&&($('model').value==='all'||e.model===$('model').value)&&norm(e.name+' '+e.id).includes(norm($('search').value))).sort((a,b)=>a.name.localeCompare(b.name));const pages=Math.max(1,Math.ceil(filtered.length/PAGE));page=Math.min(page,pages-1);$('cards').innerHTML=filtered.slice(page*PAGE,(page+1)*PAGE).map(card).join('')||'<p>Nenhuma Persona encontrada.</p>';$('count').textContent=filtered.length+' chamadas';$('page').textContent=(page+1)+' / '+pages;$('prev').disabled=page===0;$('next').disabled=page>=pages-1;$('play').disabled=!filtered.length;bind()}
function nextAudio(){if(qi>=queue.length){halt();return}const e=queue[qi++];pauseOthers(player);$('now').textContent='Ouvindo '+e.name+' · '+qi+'/'+queue.length;player.src=e.file;player.play().catch(()=>{$('now').textContent='Use os controles individuais para iniciar a reprodução.'})}
player.onended=()=>{nextTimer=setTimeout(()=>{nextTimer=null;if(queue.length)nextAudio()},600)};
$('play').onclick=()=>{halt();queue=filtered.slice();qi=0;nextAudio()};$('stop').onclick=()=>{halt();pauseOthers(null)};
['search','scope','model'].forEach(id=>$(id).addEventListener(id==='search'?'input':'change',()=>{halt();page=0;render()}));
$('prev').onclick=()=>{page--;render()};$('next').onclick=()=>{page++;render()};$('generic').innerHTML=data.filter(e=>e.scope==='summon').map(card).join('');render();
</script></body></html>'''
    (OUT/'index.html').write_text(page,encoding='utf-8')

if __name__=='__main__':main()
