"""Package approved scene triangles and an explicit walkable city layout."""
from pathlib import Path
import json,gzip,struct,shutil,math
import numpy as np
PROJECT=Path(__file__).resolve().parents[2];INPUT=Path(__file__).resolve().parent/'city';RES=PROJECT/'src/main/resources';A=RES/'assets/velvet_eclipse';D=RES/'data/velvet_eclipse/city';D.mkdir(parents=True,exist_ok=True)
cells={};meshes=[];places=[];doors=[];sections={}
def cell(x,y,z,m=0,hidden=0):cells[(int(x),int(y),int(z))]=[int(x),int(y),int(z),m,hidden]
def box(x,y,z,w,h,d,m=0,hidden=0):
 for xx in range(x,x+w):
  for yy in range(y,y+h):
   for zz in range(z,z+d):cell(xx,yy,zz,m,hidden)
def place(id,name,x,y,z):places.append(dict(id=id,name=name,x=x,y=y,z=z))
def scene(name,src,x,y,z):
 arr=np.fromfile(src,dtype='<f4').reshape(-1,11);model='city_'+name
 # Existing city studies use materials 10..17; emission becomes the shared luminous material.
 arr[arr[:,10]>.4,9]=6
 world=arr.copy();world[:,:3]+=[x,y,z]
 for tri in world.reshape(-1,3,11):
  low=np.floor(tri[:,:3].min(0)/16).astype(int)*16;hi=np.floor((tri[:,:3].max(0)-.00001)/16).astype(int)*16;hi=np.maximum(low,hi)
  for sx in range(low[0],hi[0]+1,16):
   for sy in range(low[1],hi[1]+1,16):
    for sz in range(low[2],hi[2]+1,16):sections.setdefault((sx,sy,sz),[]).append(tri)
 # Horizontal surface support. Faces are projected to block-center samples, not bounding-box fills.
 for tri in arr.reshape(-1,3,11):
  if tri[0,4]<.7:continue
  p=tri[:,:3];lo=np.floor(p.min(0)).astype(int);hi=np.ceil(p.max(0)).astype(int)
  if hi[0]-lo[0]>180 or hi[2]-lo[2]>180:continue
  a,b,c=p;v0=b[[0,2]]-a[[0,2]];v1=c[[0,2]]-a[[0,2]];det=v0[0]*v1[1]-v1[0]*v0[1]
  if abs(det)<1e-7:continue
  for xx in range(lo[0],hi[0]):
   for zz in range(lo[2],hi[2]):
    v=np.array([xx+.5,zz+.5])-a[[0,2]];u=(v[0]*v1[1]-v1[0]*v[1])/det;t=(v0[0]*v[1]-v[0]*v0[1])/det
    if u>=-.001 and t>=-.001 and u+t<=1.001:
     surface=float(a[1]+u*(b[1]-a[1])+t*(c[1]-a[1]));yy=math.ceil(surface-.001)-1
     cell(x+xx,y+yy,z+zz,2,1);cells[(x+xx,y+yy,z+zz)].append(max(1,min(16,math.ceil((surface-yy)*16))))
 # Vertical wall support uses triangle interiors rather than filling entire building bounds.
 for tri in arr.reshape(-1,3,11):
  n=tri[0,3:6];axis=int(np.argmax(np.abs(n)))
  if axis==1:continue
  axes=[j for j in range(3) if j!=axis];p=tri[:,:3];a,b,c=p;v0=b[axes]-a[axes];v1=c[axes]-a[axes];det=v0[0]*v1[1]-v1[0]*v0[1]
  if abs(det)<1e-7:continue
  low2=np.floor(p[:,axes].min(0)).astype(int);high2=np.ceil(p[:,axes].max(0)).astype(int)
  for u0 in range(low2[0],high2[0]):
   for v0i in range(low2[1],high2[1]):
    v=np.array([u0+.5,v0i+.5])-a[axes];u=(v[0]*v1[1]-v1[0]*v[1])/det;t=(v0[0]*v[1]-v[0]*v0[1])/det
    if u>=0 and t>=0 and u+t<=1:
     pos=np.floor(a+u*(b-a)+t*(c-a)).astype(int)+[x,y,z];cell(*pos,2,1)
 return arr
W=INPUT/'exteriors'
# A continuous island ground keeps the authored districts out of an exposed void.
box(-145,62,-270,295,1,500,6)
for name,x,z,label in [('dorm',-100,70,'Dormitório'),('paulownia',90,70,'Paulownia Mall'),('gekkoukan',0,-80,'Gekkoukan'),('iwatodai',-100,-30,'Iwatodai'),('station',100,-60,'Port Island Station'),('naganaki',-100,-130,'Santuário Naganaki')]:
 scene(name,W/(name+'.bin'),x,64,z);place(name,label,x,64,z+28)
 # District plazas and broad pedestrian paths join at an uninterrupted central street.
 box(x-30,63,z+20,60,1,15,0);box(min(0,x)-3,63,z+30,abs(x)+7,1,7,0)
box(-3,63,-150,7,1,310,0)
# Interconnected dorm rooms, preserving the approved furniture in each room.
rooms=[('quarto','bedroom','Seu quarto'),('lounge','lounge','Lounge'),('cozinha','kitchen','Cozinha'),('leitura','reading','Sala de leitura'),('comando','command','Sala de comando'),('banheiro','bathroom','Banheiros'),('cobertura','roof','Horta e terraço')]
for i,(src,id,label) in enumerate(rooms):
 x=-100+i*24;z=180;scene('dorm_'+src,INPUT/'interiors'/(src+'.bin'),x,64,z)
 box(x-10,63,z-8,20,1,27,3);box(x+10,64,z-8,1,6,20,2);box(x-10,70,z-8,21,1,20,2)
 # Front partition with a three-block opening into the common hall.
 box(x-10,64,z+12,8,5,1,2);box(x+2,64,z+12,9,5,1,2);box(x-2,67,z+12,4,2,1,2)
 place(id,label,x,64,z+10)
box(-110,63,192,24*len(rooms),1,7,3)
# Connected Gekkoukan interior rooms and a continuous common corridor.
for i,(src,label) in enumerate([('classroom','Sala 2-F'),('library','Biblioteca'),('hallway','Corredor escolar')]):
 x=-20+i*28;z=-220;scene('school_'+src,W/(src+'.bin'),x,64,z);box(x-12,63,z-12,27,1,33,5);place('school_'+src,label,x,64,z+15)
box(-32,63,-203,88,1,8,5)
# Doors intentionally link the facade to its furnished interior; all rooms within each wing are walk-connected.
for x,z,target in [(-100,76,'lounge'),(-3,-83,'school_classroom'),(-100,195,'dorm'),(-20,-200,'gekkoukan'),(5,-45,'tartarus')]:
 doors.append(dict(x=x,y=64,z=z,target=target))
for i,id in enumerate(['escapade','chagall','arcade','kenneth','aohige','mayoido','mandragora','police','blue'],1):
 a=(i+.5)*math.tau/10;x=90+round(math.sin(a)*17);z=70+round(math.cos(a)*17);cell(x,64,z,20+['aohige','police','mayoido','blue','chagall','mandragora','arcade','escapade','kenneth'].index(id));box(x-1,63,z-1,3,1,4,0)
 place('shop_'+id,{'escapade':'Club Escapade','chagall':'Chagall Café','arcade':'Game Parade','kenneth':'Kenneth Arnold','aohige':'Aohige Pharmacy','mayoido':'Mayoido Antiques','mandragora':'Mandragora','police':'Tatsumi East Police','blue':'Be Blue V'}[id],90+round(math.sin(a)*14),64,70+round(math.cos(a)*14))
# Clear safe arrival volumes after all furnishings/supports, preserve solid ground.
for p in places:
 x,y,z=p['x'],p['y'],p['z'];box(x-1,y-1,z-1,3,1,3,0)
 for xx in range(x-1,x+2):
  for yy in range(y,y+3):
     for zz in range(z-1,z+2):cells.pop((xx,yy,zz),None)
# Corridor mouths, access to facades, the mall gateway and every shop are protected walkways.
def passage(x0,z0,x1,z1,y=64,width=3):
 for t in np.linspace(0,1,max(abs(x1-x0),abs(z1-z0))+1):
  x,z=round(x0+(x1-x0)*t),round(z0+(z1-z0)*t)
  for dx in range(-(width//2),width//2+1):
   for dz in range(-(width//2),width//2+1):
    cell(x+dx,y-1,z+dz,0)
    for dy in range(3):cells.pop((x+dx,y+dy,z+dz),None)
passage(-100,98,-100,79);passage(0,-52,-3,-80);passage(0,-52,5,-47)
passage(30,-198,30,-190);passage(30,-190,36,-190);passage(36,-190,36,-193)
# Mall front is sector 0, facing south-east in the approved mesh.
passage(97,96,93,80);passage(93,80,90,78)
for i,id in enumerate(['escapade','chagall','arcade','kenneth','aohige','mayoido','mandragora','police','blue'],1):
 a=(i+.5)*math.tau/10;x=90+round(math.sin(a)*17);z=70+round(math.cos(a)*17)
 # Ring around the fountain keeps every approach walkable without cutting through it.
 prev=None
 for theta in np.linspace(0,math.tau,81):
  q=(90+round(math.sin(theta)*11),70+round(math.cos(theta)*11))
  if prev:passage(*prev,*q)
  prev=q
 passage(90+round(math.sin(a)*11),70+round(math.cos(a)*11),90+round(math.sin(a)*15),70+round(math.cos(a)*15),width=1)
# Preserve all counter/door cells that a nearby route might touch.
for d in doors:box(d['x'],d['y'],d['z'],1,3,1,3,1 if d['target'] in ('lounge','school_classroom') else 0)
for i,id in enumerate(['escapade','chagall','arcade','kenneth','aohige','mayoido','mandragora','police','blue'],1):
 a=(i+.5)*math.tau/10;cell(90+round(math.sin(a)*17),64,70+round(math.cos(a)*17),20+['aohige','police','mayoido','blue','chagall','mandragora','arcade','escapade','kenneth'].index(id))
# Match interactive doors to visible labels; material index 29 is reserved for the Tártaro entrance.
for d in doors:
 if d['target']=='tartarus':cell(d['x'],d['y'],d['z'],29)
# Functional Velvet Room door at the authored blue recess beside Mandragora.
passage(79,70,76,67,width=1);box(74,63,66,4,1,2,0)
for xx in range(74,78):
 for yy in range(64,67):
  for zz in range(66,68):cells.pop((xx,yy,zz),None)
cell(74,64,66,30);cell(74,65,66,31)
# Section files retain exact authored triangles and are clipped by the native baked model.
for idx,(origin,tris) in enumerate(sorted(sections.items())):
 arr=np.asarray(tris,dtype='<f4').reshape(-1,11);arr[:,:3]-=origin;model=f'city_section_{idx}'
 (A/'tartarus'/f'{model}.mesh.gz').write_bytes(gzip.compress(struct.pack('<II',0x56454d31,len(arr))+arr.tobytes(),mtime=0))
 x,y,z=origin;meshes.append(dict(name=model,x=x,y=y,z=z,minX=x,minY=y,minZ=z,maxX=x,maxY=y,maxZ=z))
(D/'plan.json.gz').write_bytes(gzip.compress(json.dumps(dict(cells=list(cells.values()),meshes=meshes,places=places,doors=doors),ensure_ascii=False,separators=(',',':')).encode('utf8'),mtime=0))
# Material resources and normal creative building blocks.
names=['paving','brick','plaster','wood','glass','school_floor','leaves','roof','light'];textures=['pedra_calcada','tijolo_comercial','reboco_pedra','madeira','vidro','piso_escolar','folhagem','telha']
from PIL import Image,ImageDraw,ImageFont
palette=[(.79,.73,.63),(.52,.31,.23),(.83,.80,.70),(.46,.29,.17),(.27,.40,.48),(.57,.47,.34),(.23,.40,.22),(.24,.27,.29),(.92,.84,.62)]
for i,name in enumerate(names):
 original=INPUT/'textures'/(textures[min(i,7)]+'.png');shutil.copy2(original,A/'textures/block'/f'city_native_{i}.png')
 pixels=np.array(Image.open(original).convert('RGB')).astype(float)*palette[i];Image.fromarray(np.clip(pixels,0,255).astype('uint8')).save(A/'textures/block'/f'city_{name}.png')
 (A/'blockstates'/f'city_{name}.json').write_text(json.dumps({'variants':{'':{'model':f'velvet_eclipse:block/city_{name}'}}}))
 (A/'models/block'/f'city_{name}.json').write_text(json.dumps({'parent':'minecraft:block/cube_all','textures':{'all':f'velvet_eclipse:block/city_{name}'}}))
 (A/'models/item'/f'city_{name}.json').write_text(json.dumps({'parent':f'velvet_eclipse:block/city_{name}'}))
for file in (A/'models/block').glob('*.json'):
 d=json.loads(file.read_text(encoding='utf8'))
 if d.get('loader')=='velvet_eclipse:tartarus_mesh':
  for i,name in enumerate(names[:8]):d['textures']['m'+str(i+10)]=f'velvet_eclipse:block/city_native_{i}'
  file.write_text(json.dumps(d,indent=2))
shops=['aohige','police','mayoido','blue','chagall','mandragora','arcade','escapade','kenneth']
for id in shops:
 name='city_shop_'+id
 (A/'blockstates'/f'{name}.json').write_text(json.dumps({'variants':{'':{'model':f'velvet_eclipse:block/{name}'}}}))
 (A/'models/block'/f'{name}.json').write_text(json.dumps({'parent':'minecraft:block/cube_all','textures':{'all':f'velvet_eclipse:block/city_shop_{id}'}}))
 (A/'models/item'/f'{name}.json').write_text(json.dumps({'parent':f'velvet_eclipse:block/{name}'}))
 from PIL import Image,ImageDraw,ImageFont
 im=Image.new('RGB',(128,128),'#071b38');draw=ImageDraw.Draw(im);draw.rectangle((4,4,123,123),outline='#62eeee',width=3);font=ImageFont.truetype(str(A/'font/body.ttf'),18);draw.text((9,45),id.upper(),font=font,fill='#f4ffff');im.save(A/'textures/block'/f'{name}.png')
# Distinct entrance plaque; it is not a shop counter.
im=Image.new('RGB',(128,128),'#06192d');draw=ImageDraw.Draw(im);draw.rectangle((5,5,122,122),outline='#68ffd3',width=4);font=ImageFont.truetype(str(A/'font/body.ttf'),21);draw.text((22,45),'TARTARUS',font=font,fill='#c9ffee');draw.polygon([(60,85),(70,85),(65,97)],fill='#66f2d0');im.save(A/'textures/block/city_tartarus_access.png')
# Real map image derived from the generated support/road grid; player and destinations overlay at runtime.
im=Image.new('RGB',(512,512),'#07172e');draw=ImageDraw.Draw(im)
for c in sorted(cells.values(),key=lambda c:c[1]):
 x,y,z,mat=c[:4]
 if y<63:continue
 px=round((x+150)/310*512);pz=round((z+275)/515*512)
 color='#4e7886' if c[4]==0 else '#183d5a'
 if mat>=20:color='#ffcc72'
 draw.rectangle((px,pz,px+2,pz+2),fill=color)
gui=A/'textures/gui/city';gui.mkdir(parents=True,exist_ok=True);im.save(gui/'map.png')
for file in ['city_tartarus_access']:
 (A/'blockstates'/f'{file}.json').write_text(json.dumps({'variants':{'':{'model':f'velvet_eclipse:block/{file}'}}}))
 (A/'models/block'/f'{file}.json').write_text(json.dumps({'parent':'minecraft:block/cube_all','textures':{'all':'velvet_eclipse:block/city_tartarus_access'}}))
 (A/'models/item'/f'{file}.json').write_text(json.dumps({'parent':f'velvet_eclipse:block/{file}'}))
for lang in ['pt_br','en_us']:
 file=A/'lang'/f'{lang}.json';d=json.loads(file.read_text(encoding='utf8'))
 labels=['Calçada de pedra','Tijolo comercial','Reboco de pedra','Madeira do dormitório','Vidro urbano','Piso escolar','Folhagem urbana','Telha urbana','Luminária urbana'] if lang=='pt_br' else ['Stone paving','Commercial brick','Stone plaster','Dormitory wood','City glass','School floor','City foliage','City roof','City light']
 titles=['Aohige Pharmacy','Tatsumi East Police','Mayoido Antiques','Be Blue V','Chagall Café','Mandragora','Game Parade','Club Escapade','Kenneth Arnold']
 d.update({f'block.velvet_eclipse.city_{name}':labels[i] for i,name in enumerate(names)});d.update({f'block.velvet_eclipse.city_shop_{id}':('Balcão • ' if lang=='pt_br' else 'Counter • ')+titles[i] for i,id in enumerate(shops)})
 d['block.velvet_eclipse.city_tartarus_access']='Acesso ao Tártaro' if lang=='pt_br' else 'Tartarus entrance'
 d['key.velvet_eclipse.tartarus_map']='Mapa e viagem rápida' if lang=='pt_br' else 'Map and fast travel'
 file.write_text(json.dumps(d,ensure_ascii=False,indent=2),encoding='utf8')
dim=json.loads((RES/'data/velvet_eclipse/dimension/velvet_room.json').read_text());dim['type']='velvet_eclipse:city';(RES/'data/velvet_eclipse/dimension/city.json').write_text(json.dumps(dim,indent=2))
typ=json.loads((RES/'data/velvet_eclipse/dimension_type/velvet_room.json').read_text());typ.update(natural=True,has_skylight=True,has_ceiling=False,ambient_light=.15,bed_works=True,effects='minecraft:overworld');typ.pop('fixed_time',None);(RES/'data/velvet_eclipse/dimension_type/city.json').write_text(json.dumps(typ,indent=2))
print('City:',len(cells),'cells',len(meshes),'sections from 16 approved scenes',len(places),'destinations')
