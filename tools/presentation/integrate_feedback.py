"""Install approved audit resources into source assets; never modifies the game profile."""
from pathlib import Path
import json, shutil, subprocess, hashlib, concurrent.futures
ROOT=Path(__file__).resolve().parents[3];SRC=ROOT/'velvet-eclipse/src';WORK=ROOT/'work/feedback-0.6.55';WORK.mkdir(exist_ok=True)
RES=SRC/'main/resources';ASSET=RES/'assets/velvet_eclipse';DATA=RES/'data/velvet_eclipse/presentation';DATA.mkdir(parents=True,exist_ok=True)
FF=ROOT/'work/awakening-preview/deps/imageio_ffmpeg/binaries/ffmpeg-win-x86_64-v7.1.exe'
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def dump(p,x):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(x,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')

# Record existing summon code/resources before the first implementation write.
before=WORK/'preserved-before.json'
if not before.exists():
    paths=[p for p in SRC.rglob('*') if p.is_file() and (any(s in p.name.lower() for s in ['summon','aura','evoker','spellcore','photonbattle','personacinematic']) or 'sounds/awakening' in p.as_posix())]
    dump(before,{str(p.relative_to(SRC)):sha(p) for p in paths})
    shutil.copy2(SRC/'main/java/com/gx/velveteclipse/client/vfx/PhotonBattleEffects.java',WORK/'PhotonBattleEffects.before.java')

audio=json.loads((ROOT/'outputs/auditoria-audio-mapa-05/manifest.json').read_text(encoding='utf-8'))
routes={r['id']:r['clips'] for r in audio['routes']}
entries={e['id']:e for e in audio['entries']};used=set(c for cs in routes.values() for c in cs)
assert all(entries[c]['selection']=='Proposto' or entries[c]['selection'].startswith('Propost') for c in used),set(entries[c]['selection'] for c in used)
sounds_path=ASSET/'sounds.json';sounds=json.loads(sounds_path.read_text(encoding='utf-8')) if sounds_path.exists() else {}
out=ASSET/'sounds/reload';out.mkdir(parents=True,exist_ok=True)
def convert(id):
    src=ROOT/'outputs/auditoria-audio-mapa-05'/entries[id]['file'];dst=out/(id+'.ogg')
    subprocess.run([str(FF),'-hide_banner','-loglevel','error','-y','-i',str(src),'-ac','1','-c:a','libvorbis','-q:a','5',str(dst)],check=True)
    return dict(id=id,source=entries[id].get('source',entries[id].get('original',entries[id]['file'])),sourcePreviewSha256=sha(src),oggSha256=sha(dst),bytes=dst.stat().st_size)
with concurrent.futures.ThreadPoolExecutor(max_workers=4) as pool:converted=list(pool.map(convert,sorted(used)))
for id in sorted(used):sounds['reload.'+id]={'sounds':[{'name':'velvet_eclipse:reload/'+id,'stream':False}]}
dump(sounds_path,sounds);dump(DATA/'audio-routes.json',routes)
dump(DATA/'audio-provenance.json',{'source':'Approved original recording audit 05','conversion':'Audition gain retained; mono Vorbis quality 5; no timing/pitch changes; original masters retained in audit output','clips':converted})

vfx=json.loads((ROOT/'outputs/auditoria-vfx-photon-01/manifest.json').read_text(encoding='utf-8'))
profiles={p['id']:p for p in vfx['profiles'] if p['id']!='summon'}
fallback={'ghastly_wail':'mudo','foul_breath':'ailment','stagnant_air':'ailment','life_drain':'eiga','spirit_drain':'psy','virus_breath':'ailment','cadenza':'dia','jack_brothers':'ailment','king_and_i':'bufu','best_friends':'concentrate','scarlet_havoc':'slash','trickster':'megido','armageddon':'megido'}
bindings={r['id']:r['profile'] or fallback[r['id']] for r in vfx['routes']}
# Composite status operations use every attribute, rather than recoloring one ring.
for id,base in [('heat_riser','kaja'),('debilitate','nda')]:
    p=json.loads(json.dumps(profiles['taru'+base]));p['id']=id;p['layers']=[]
    for i,prefix in enumerate(['taru','raku','suku']):
        for l in profiles[prefix+base]['layers']:
            if l['type']=='beam':continue
            layer=json.loads(json.dumps(l));layer['start']+=i*3
            if layer['type']=='sprite':layer['count']=max(1,layer['count']//2)
            p['layers'].append(layer)
    profiles[id]=p;bindings[id]=id
# Each elemental Break retains the corresponding palette.
for id,col in [('fire_break','#ffb150'),('ice_break','#8cddff'),('wind_break','#a5ff71'),('elec_break','#d5a5ff')]:
    p=json.loads(json.dumps(profiles['break']));p['id']=id
    for l in p['layers']:l['color']=col
    profiles[id]=p;bindings[id]=id
# Do not share the exact same symbol/tint for distinct ailments.
for id,col in [('poisma','#ba78ff'),('poison_mist','#ba78ff'),('marin_karin','#ff90c5'),('sexy_dance','#ff90c5'),('evil_touch','#7748b0'),('evil_smile','#7748b0'),('provoke','#ff5c3d'),('infuriate','#ff5c3d'),('bewilder','#d9a268'),('eerie_sound','#d9a268')]:
    p=json.loads(json.dumps(profiles['ailment']));p['id']=id
    for l in p['layers']:l['color']=col
    profiles[id]=p;bindings[id]=id
for p in profiles.values():
    for l in p['layers']:l['start']=max(0,l['start']-6) # Already-confirmed impact, no extra 300ms lag.
dump(DATA/'photon-effects.json',list(profiles.values()));dump(DATA/'skill-vfx.json',bindings)
texture_src=ROOT/'outputs/auditoria-vfx-photon-01/draft/resources/assets/velvet_eclipse/textures/vfx/audit01'
shutil.copytree(texture_src,ASSET/'textures/vfx/reload',dirs_exist_ok=True)
dump(WORK/'resources.json',{'audioClips':len(used),'audioRoutes':len(routes),'skills':len(bindings),'profiles':len(profiles),'summonReplaced':False,'adaptedSpecials':fallback})
print('Prepared',len(used),'original clips;',len(bindings),'skill bindings;',len(profiles),'Photon recipes',flush=True)
