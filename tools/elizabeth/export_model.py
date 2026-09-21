"""Export the approved Elizabeth draft without regenerating or changing the model."""
from pathlib import Path
import json,shutil
root=Path(__file__).resolve().parent
assets=root.parents[1]/'src/main/resources/assets/velvet_eclipse'
def face_uv(u,v,w,h,d):
 return {'north':(u+d,v+d,w,h),'south':(u+2*d+w,v+d,w,h),'west':(u+d+w,v+d,d,h),'east':(u,v+d,d,h),'up':(u+d,v,w,d),'down':(u+d+w,v,w,d)}
g=json.loads((root/'draft/elizabeth.geo.json').read_text(encoding='utf-8'))
for b in g['minecraft:geometry'][0]['bones']:
 b['pivot'][0]*=-1
 if 'rotation' in b:b['rotation']=[-b['rotation'][0],-b['rotation'][1],b['rotation'][2]]
 for c in b.get('cubes',[]):
  c['origin'][0]=-c['origin'][0]-c['size'][0]
  if 'pivot' in c:c['pivot'][0]*=-1
  if 'rotation' in c:c['rotation']=[-c['rotation'][0],-c['rotation'][1],c['rotation'][2]]
  if 0 in c['size']:
   c['uv']={f:{'uv':[u,v],'uv_size':[w,h]} for f,(u,v,w,h) in face_uv(*c['uv'],*c['size']).items() if w>0 and h>0}
   c['size']=[.08 if x==0 else x for x in c['size']]
a=json.loads((root/'draft/elizabeth.animation.json').read_text(encoding='utf-8'))
for clip in a['animations'].values():
 for tracks in clip['bones'].values():
  for channel,keys in tracks.items():
   for t,v in keys.items():
    if channel=='rotation':keys[t]=[-v[0],-v[1],v[2]]
    elif channel=='position':keys[t]=[-v[0],v[1],v[2]]
for folder,name,data in [('geo','elizabeth.geo.json',g),('animations','elizabeth.animation.json',a)]:
 dest=assets/folder/'entity'/name;dest.parent.mkdir(parents=True,exist_ok=True);dest.write_text(json.dumps(data,indent=2),encoding='utf-8')
shutil.copy2(root/'draft/elizabeth.png',assets/'textures/entity/elizabeth.png')
print('Elizabeth: approved model exported to GeckoLib.')
