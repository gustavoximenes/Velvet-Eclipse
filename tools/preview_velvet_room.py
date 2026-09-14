"""Offline inspection of the actual room plan and Java block/item JSON models."""
from pathlib import Path
import json, math, sys
import numpy as np
from PIL import Image, ImageDraw

project=Path(__file__).resolve().parents[1]
assets=project/'src/main/resources/assets/velvet_eclipse'
out=project/'build/reports/room/previews';out.mkdir(parents=True,exist_ok=True)
ns={'__file__':str(project/'tools/render_battle_previews.py')}
exec((project/'tools/render_battle_previews.py').read_text(encoding='utf-8').split('for n in (2,7):')[0],ns)
triangle=ns['triangle'];textures={}
faces={'north':[0,2,3,1],'south':[5,7,6,4],'west':[4,6,2,0],'east':[1,3,7,5],'up':[2,6,7,3],'down':[4,0,1,5]}
def rotation(axis,angle):
 a=math.radians(angle);c,s=math.cos(a),math.sin(a)
 return np.array({'x':[[1,0,0],[0,c,-s],[0,s,c]],'y':[[c,0,-s],[0,1,0],[s,0,c]],'z':[[c,-s,0],[s,c,0],[0,0,1]]}[axis])
def load(name):
 model=json.loads((assets/f'models/{name}.json').read_text(encoding='utf-8'))
 if model.get('parent')=='minecraft:block/cube_all':
  model['elements']=[{'from':[0,0,0],'to':[16,16,16],'faces':{f:{'texture':'#all','uv':[0,0,16,16]} for f in faces}}]
 elif 'parent' in model:return load(model['parent'].split(':')[1])
 return model
def geometry(name,at=(0,0,0),yaw=0,hidden=()):
 model=load(name);result=[]
 for e in model['elements']:
  a,b=e['from'],e['to'];points=np.array([[x,y,z] for z in (a[2],b[2]) for y in (a[1],b[1]) for x in (a[0],b[0])],float)
  if 'rotation' in e:
   r=e['rotation'];origin=np.array(r['origin']);points=(points-origin)@rotation(r['axis'],r['angle']).T+origin
  points=((points-8)@rotation('y',yaw).T+8)/16+at
  for side,face in e['faces'].items():
   if side in hidden:continue
   tex=face['texture']
   while tex.startswith('#'):tex=model['textures'][tex[1:]]
   if tex not in textures:textures[tex]=Image.open(assets/('textures/'+tex.split(':')[1]+'.png')).convert('RGBA')
   u,v,U,V=np.array(face.get('uv',[0,0,16,16]))/16
   result.append((points[faces[side]],np.array([[U,V],[U,v],[u,v],[u,V]]),textures[tex]))
 return result
def render(quads,path,eye,target,width=1000,height=650,fov=65):
 eye=np.array(eye);forward=np.array(target)-eye;forward/=np.linalg.norm(forward)
 right=np.cross(forward,[0,1,0]);right/=np.linalg.norm(right);up=np.cross(right,forward)
 f=height/(2*math.tan(math.radians(fov)/2));items=[]
 for p,uv,tex in quads:
  rel=p-eye;z=rel@forward
  if z.min()<.08:continue
  xy=np.column_stack([width/2+rel@right*f/z,height/2-rel@up*f/z])
  if xy[:,0].max()<0 or xy[:,0].min()>width or xy[:,1].max()<0 or xy[:,1].min()>height:continue
  items.append((z.mean(),xy,uv,tex))
 im=Image.new('RGBA',(width,height),'#101944')
 for z,xy,uv,tex in sorted(items,key=lambda x:-x[0]):
  triangle(im,xy[:3],uv[:3],tex);triangle(im,xy[[0,2,3]],uv[[0,2,3]],tex)
 ImageDraw.Draw(im).text((14,height-20),'PREVIA OFFLINE | modelos e planta reais | sem captura do Minecraft',fill='#8fd6f2')
 im.convert('RGB').save(path)

cells=json.loads((project/'build/reports/room/layout.json').read_text(encoding='utf-8'))
occupied={(c['x'],c['y'],c['z']) for c in cells if c['material']!='grille'}
quads=[];steps={'west':(-1,0,0),'east':(1,0,0),'up':(0,1,0),'down':(0,-1,0),'north':(0,0,-1),'south':(0,0,1)}
for c in cells:
 at=(c['x'],c['y'],c['z']);material=c['material'];yaw=0;hidden=[]
 if material=='grille':yaw=90 if c['x']==-7 else 270 if c['x']==7 else 180
 else:hidden=[face for face,delta in steps.items() if tuple(a+b for a,b in zip(at,delta)) in occupied]
 quads+=geometry('block/velvet_'+material,at,yaw,hidden)
quads+=geometry('block/velvet_table',(0,64,-2))
for x,z in [(0,6),(-3,5),(3,5),(-5,4),(5,4)]:quads+=geometry('block/velvet_chair',(x,64,z))
quads+=geometry('block/velvet_chair',(0,64,-5),180)
for half,y in [('lower',64),('upper',65)]:quads+=geometry('block/velvet_door_'+half+'_left_false',(0,y,10))
render(quads,out/'velvet-room.png',(3.1,65.75,9),(-.3,65,2.5))
render(geometry('item/velvet_key'),out/'velvet-key.png',(1,1.05,-1.3),(.5,.5,.5),500,700,48)
door=[]
for half,y in [('lower',0),('upper',1)]:door+=geometry('block/velvet_door_'+half+'_left_false',(0,y,0))
render(door,out/'velvet-door.png',(1.6,1.45,-3),(.5,1,0),500,700,48)
print('Room, key and door previews: '+str(out))
