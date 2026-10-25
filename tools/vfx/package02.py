from pathlib import Path
import sys,json,shutil,subprocess,hashlib,concurrent.futures,zipfile
sys.path.insert(0,str(Path(__file__).parent))
import audit02 as a

def main():
 data=json.loads((a.OUT/'manifest.json').read_text(encoding='utf-8'))
 profiles=data['profiles']
 sources={id:src for _,ids,srcs,*_ in a.SPECS for id,src in zip(ids,srcs)}
 for p in profiles:p['source']=sources[p['id']]
 a.dump(a.OUT/'manifest.json',data);a.dump(a.OUT/'draft/recipes.json',profiles);a.dump(a.DOC/'recipes.json',profiles)
 evidence=json.loads((a.ROOT/'outputs/auditoria-vfx-photon-01/evidence.json').read_text(encoding='utf-8'))
 a.dump(a.OUT/'evidence.json',[e for e in evidence if e.get('source') in set(sources.values()) and '/Astrea/' not in e['asset']])
 bySource={p['source']:p for p in profiles}
 skills=json.loads((a.ROOT/'velvet-eclipse/src/main/resources/data/velvet_eclipse/catalogue/skills.json').read_text(encoding='utf-8'))
 if isinstance(skills,dict):skills=skills['skills']
 mapping={13:10,14:11,15:12,23:20,24:21,25:22,33:30,34:31,35:32,43:40,44:41,45:42,59:56,60:57,61:58,73:70,74:71,75:72}
 coverage=[]
 for s in skills:
  src=s.get('source');base=mapping.get(src,src)
  if base not in bySource:continue
  p=bySource[base];note='Forma individual comparada; variante em área ainda exige conferência de timing.' if src in mapping else 'Estudo individual comparado com trecho Reload.'
  if s['id'] in ['eiga','maeiga']:note+=' Nome do catálogo diverge da fonte original; preservar ID até decisão específica.'
  coverage.append(dict(id=s['id'],name=s['name'],source=src,profile=p['id'],target=s.get('target'),note=note))
 dump=a.dump;dump(a.OUT/'coverage.json',coverage)
 lines=['# Cobertura da auditoria elemental 02','',f'{len(coverage)} entradas atuais do catálogo relacionadas aos 22 estudos. Nenhum catálogo de produção foi alterado.','', '| ID atual | Fonte | Estudo | Alvos | Observação |','|---|---:|---|---|---|']
 for s in coverage:lines.append(f"| {s['id']} | {s['source']} | {s['profile']} | {s['target']} | {s['note']} |")
 lines+=['','O nível intermediário Eiga (fonte 71) está modelado para referência, mesmo sem uma entrada correspondente no catálogo atual. Os estudos em área disponíveis no painel usam o nível forte de cada família com três alvos. Hama/Mudo e suas variantes, Divine Judgment/Demonic Decree e habilidades não elementais estão fora do escopo.']
 lines+=['','Maralagidyne (fonte 18) é uma sequência especial ainda sem proposta própria nesta rodada. Não considerá-la coberta por Inferno ou Maragidyne. Seu efeito de produção permanece inalterado.']
 (a.OUT/'COBERTURA.md').write_text('\n'.join(lines),encoding='utf-8')
 html=(a.ROOT/'velvet-eclipse/tools/vfx/panel02.html').read_text(encoding='utf-8').replace('__DATA__',json.dumps(data,ensure_ascii=False).replace('</','<\\/'))
 (a.OUT/'index.html').write_text(html,encoding='utf-8')
 shutil.copy2(a.DOC/'AUDITORIA.md',a.OUT/'AUDITORIA.md');shutil.copy2(a.OUT/'COBERTURA.md',a.DOC/'COBERTURA.md')
 shutil.copy2(a.DOC/'PhotonElementalDraft.java',a.OUT/'draft/PhotonElementalDraft.java')
 shutil.copytree(a.OUT/'draft/resources',a.DOC/'resources',dirs_exist_ok=True)
 shutil.copy2(a.OUT/'draft/meshes.json',a.DOC/'meshes.json')
 def encode(p):
  target=p.with_suffix('.mp4')
  subprocess.run([str(a.FF),'-hide_banner','-loglevel','error','-i',str(p),'-an','-c:v','libx264','-crf','20','-pix_fmt','yuv420p','-movflags','+faststart','-y',str(target)],check=True)
 with concurrent.futures.ThreadPoolExecutor(max_workers=3) as pool:list(pool.map(encode,sorted((a.OUT/'previews').glob('*.gif'))))
 # Compact six-family animated contact sheet for sharing without opening the panel.
 from PIL import Image,ImageDraw
 picks=['agi','bufudyne','garudyne','zio','kougaon','eigaon'];frames=[]
 gifs=[Image.open(a.OUT/'previews'/(id+'.gif')) for id in picks]
 for tick in range(54):
  sheet=Image.new('RGB',(960,900),'#07111f')
  for j,g in enumerate(gifs):g.seek(tick);sheet.paste(g.convert('RGB').resize((480,300)),((j%2)*480,(j//2)*300))
  # Keep every full frame, including the ground impact and scale caption.
  d=ImageDraw.Draw(sheet)
  for j,id in enumerate(picks):d.rectangle(((j%2)*480,(j//2)*300,(j%2+1)*480,(j//2)*300+23),fill='#0b1b2e');d.text(((j%2)*480+12,(j//2)*300+2),id.title()+' · PROPOSTA 02',font=a.font(14,True),fill='#d1f5ff')
  frames.append(sheet)
 frames[10].save(a.OUT/'painel.png');frames[0].save(a.OUT/'elementais-02.gif',save_all=True,append_images=frames[1:],duration=50,loop=0,optimize=False)
 before=json.loads((a.WORK/'production-preserved.json').read_text(encoding='utf-8'))
 changed=[p for p,h in before.items() if not (a.ROOT/'velvet-eclipse'/p).exists() or a.sha(a.ROOT/'velvet-eclipse'/p)!=h]
 assert not changed,changed
 checks=0
 meshes=json.loads((a.OUT/'draft/meshes.json').read_text())
 for p in profiles:
  assert 0<p['referencePeak']-p['referenceStart']<p['referenceDuration'];checks+=1
  assert (a.OUT/'references'/(p['id']+'.mp4')).stat().st_size>1000;checks+=1
  assert (a.OUT/'previews'/(p['id']+'.mp4')).stat().st_size>1000;checks+=1
  assert p['durationTicks']<=80 and len(p['parts'])<=96;checks+=1
  for layer in p['parts']:
   assert layer['model'] in meshes or layer['model'] in ['fire_flipbook','ground_glow'];assert layer['life']>0 and layer['start']>=0;checks+=2
  if p['tier']==3:assert (a.OUT/'previews'/(p['id']+'-area.mp4')).exists();checks+=1
 for family,ids,*_ in a.SPECS:
  hashes=[hashlib.sha256(json.dumps(next(p['parts'] for p in profiles if p['id']==id)).encode()).hexdigest() for id in ids]
  assert len(set(hashes))==len(hashes);checks+=1
 expected={'maragi':'agi','maragion':'agilao','maragidyne':'agidyne','inferno':'inferno','mabufu':'bufu','mabufula':'bufula','mabufudyne':'bufudyne','diamond_dust':'diamond_dust','magaru':'garu','magarula':'garula','magarudyne':'garudyne','panta_rhei':'panta_rhei','mazio':'zio','mazionga':'zionga','maziodyne':'ziodyne','thunder_reign':'thunder_reign','eiga':'eiha','eigaon':'eigaon'}
 actual={s['id']:s['profile'] for s in coverage}
 for id,profile in expected.items():assert actual[id]==profile,(id,actual[id],profile);checks+=1
 compilelog=(a.WORK/'prototype-compile.log').read_text(encoding='utf-8-sig')
 assert 'error:' not in compilelog and (a.WORK/'compiled-prototype/com/gx/velveteclipse/audit/PhotonElementalDraft.class').exists();checks+=1
 report={'status':'passed','checks':checks,'individualStudies':len(profiles),'areaStudies':6,'mappedCatalogueEntries':len(coverage),'preservedProductionFiles':len(before),'productionResourcesChanged':changed,'photonDraftCompiled':True,'inGameValidation':False,'jarBuilt':False,'timingTests':{'feedback':1396,'evokerWeakness':4421}}
 dump(a.OUT/'validation.json',report);dump(a.DOC/'validation.json',report)
 print(json.dumps(report,indent=2),flush=True)
if __name__=='__main__':main()
