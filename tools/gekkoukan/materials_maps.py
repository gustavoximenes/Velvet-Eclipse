"""Native school textures and maps generated from the final placed block plan."""
from pathlib import Path
import gzip,json,math
import numpy as np
from PIL import Image,ImageDraw
HERE=Path(__file__).parent;ROOT=HERE.parents[1];AS=ROOT/'src/main/resources/assets/velvet_eclipse';DATA=ROOT/'src/main/resources/data/velvet_eclipse/city'
rng=np.random.default_rng(650)
for i,name in enumerate(['stone','plaster','tiles','metal','wood']):
 a=np.full((32,32,3),246,dtype=float);a+=rng.integers(-4,5,(32,32,1))
 if name=='stone':a[15:17]*=.91;a[:16,15:17]*=.91;a[16:,0:2]*=.91
 if name=='tiles':a[0]*=.78;a[:,0]*=.78
 if name=='wood':
  a[:,0]*=.77
  for y in range(32):a[y]*=.93+.05*math.sin(y*1.6)
 if name=='metal':a[0]*=.88;a[:,0]*=.88
 Image.fromarray(np.uint8(a.clip(0,255))).save(AS/f'textures/block/city_school_{name}.png')
plan=json.loads(gzip.decompress((DATA/'plan.json.gz').read_bytes()));defs=json.loads(gzip.decompress((DATA/'architecture.json.gz').read_bytes()))
names={'secretaria':('School Office','Secretaria'),'faculty':('Faculty Office','Sala dos professores'),'enfermaria':('Nurse Office','Enfermaria'),'library':('Library','Biblioteca'),'classroom':('Classroom 2-F','Sala 2-F'),'classroom_3a':('Classroom 3-A','Sala 3-A'),'council':('Student Council','Conselho estudantil'),'laboratorio':('Laboratory','Laboratório'),'ciencias':('Science Room','Sala de ciências'),'musica':('Music Room','Sala de música'),'artes':('Art Club','Clube de artes'),'costura':('Home Economics','Economia doméstica'),'sanitarios':('Restrooms','Sanitários'),'deposito':('Supply Room','Depósito'),'vestiarios':('Changing Rooms','Vestiários'),'orientacao':('Counselling','Orientação'),'impressao':('Print Room','Sala de impressão'),'transmissao':('Broadcast Room','Sala de transmissão'),'loja-armarios':('School Store and Lockers','Loja e armários'),'corredor-escada':('Stair Gallery','Galeria das escadas'),'cobertura':('Rooftop','Cobertura'),'ginasio':('Gymnasium','Ginásio'),'hallway':('Entrance Hall','Hall de entrada')}
languages={lang:json.loads((AS/f'localization/{lang}.json').read_text(encoding='utf8')) for lang in ['en_us','pt_br']}
for p in plan['places']:
 if not p['id'].startswith('school_'):continue
 en,pt=names[p['id'][7:]];p['name']='Gekkoukan · '+en
 languages['en_us'][p['name']]=p['name'];languages['pt_br'][p['name']]='Gekkoukan · '+pt
for lang,d in languages.items():(AS/f'localization/{lang}.json').write_text(json.dumps(d,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
(DATA/'plan.json.gz').write_bytes(gzip.compress(json.dumps(plan,ensure_ascii=False,separators=(',',':')).encode(),mtime=0))
maps=json.loads((AS/'city-map.json').read_text());maps=[m for m in maps if not m['id'].startswith('school')]
maps[0]['minZ']=-200
for b in plan['buildings']:
 if not b['id'].startswith('school'):continue
 for i,y in enumerate(b['floors']):maps.append(dict(id=b['id']+'_'+str(i),minX=b['minX']-2,maxX=b['maxX']+2,minZ=b['minZ']-2,maxZ=b['maxZ']+2,floor=y))
for m in maps:
 image=Image.new('RGB',(512,512),'#08192e');d=ImageDraw.Draw(image);sx=512/(m['maxX']-m['minX']+1);sz=512/(m['maxZ']-m['minZ']+1)
 for c in plan['cells']:
  x,y,z,mat=c[:4];f=m['floor'];color=None
  if not(m['minX']<=x<=m['maxX'] and m['minZ']<=z<=m['maxZ']):continue
  if f<0 and 63<=y<=65:color='#274858' if y==63 else '#60888d'
  elif f>=0 and y==f-1:color='#284e68'
  elif f>=0 and f<=y<f+2:
   if mat!=40:color='#85b7bb'
   elif any(b[4]>.25 for b in defs[c[4]]['boxes']):color='#85b7bb'
  if color:d.rectangle([int((x-m['minX'])*sx),int((z-m['minZ'])*sz),math.ceil((x+1-m['minX'])*sx),math.ceil((z+1-m['minZ'])*sz)],fill=color)
 image.save(AS/f"textures/gui/city/{m['id']}.png")
(AS/'city-map.json').write_text(json.dumps(maps))
m=maps[0];sx=512/(m['maxX']-m['minX']+1);sz=512/(m['maxZ']-m['minZ']+1);night=Image.open(AS/'textures/gui/city/map.png');d=ImageDraw.Draw(night)
for x in range(-51,91):
 for z in range(-190,-53):
  if x<=52 or z<=-140 or x<=57 and z<=-105:d.rectangle([int((x-m['minX'])*sx),int((z-m['minZ'])*sz),math.ceil((x+1-m['minX'])*sx),math.ceil((z+1-m['minZ'])*sz)],fill='#0c3526')
tower=json.loads(gzip.decompress((DATA.parent/'tartarus/exterior.json.gz').read_bytes()))
for x,y,z,mat in tower['cells']:
 if 0<=y<10:
  px=int((x-m['minX'])*sx);pz=int((z-108-m['minZ'])*sz);d.rectangle([px,pz,px+1,pz+1],fill='#70efb8')
night.save(AS/'textures/gui/city/map_night.png')
print('Five school materials; maps rebuilt for',len(maps),'layers')
