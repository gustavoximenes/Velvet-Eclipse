"""Export the approved projected cuboids with real UVs, lighting and fixed bone lengths."""
from pathlib import Path
import gzip,struct,math,json
import numpy as np
import poses as p

OUT=p.ROOT/'src/main/resources/assets/velvet_eclipse/menu'
OUT.mkdir(parents=True,exist_ok=True)
light=np.array([-.35,.7,-.6]);light/=np.linalg.norm(light)
counts={}
for name,spec in p.POSES.items():
 for slim in (False,True):
  p.slim=slim;p.armw=3 if slim else 4
  cam=p.r.Camera(spec['eye'],spec['target'],spec['fov'])
  frames=[]
  for frame in range(3):
   faces=p.mesh(spec,frame*2*math.pi/3,name);vertices=[];bindings=[]
   for q,uv,tex,color in faces:
    normal=np.cross(q[1]-q[0],q[2]-q[0]);normal/=np.linalg.norm(normal)
    shade=(.61+.39*max(0,float(normal@light)))*1.22
    ray=cam.eye-q.mean(axis=0);ray/=np.linalg.norm(ray)
    rim=(1-abs(normal@ray))**3*.12
    projected=cam.project(q)
    texture=0 if tex is p.skin else 1
    assert texture==0 or name=='skill'
    bindings.append(texture)
    for xyz,coord in zip(projected,uv):
     vertices.extend([*xyz,coord[0]/tex.shape[1],coord[1]/tex.shape[0],shade,rim])
   frames.append(vertices)
  assert all(len(f)==len(frames[0]) for f in frames)
  with gzip.open(OUT/f'{name}_{"slim" if slim else "wide"}.bin.gz','wb') as out:
   out.write(struct.pack('>4i',0x56454d50,1,len(bindings),3))
   out.write(bytes(bindings))
   out.write(np.asarray(frames,dtype='>f4').tobytes())
  counts[name+'_'+('slim' if slim else 'wide')]=len(bindings)
(OUT/'manifest.json').write_text(json.dumps({'format':1,'samples':3,'dimensions':[440,640],'faces':counts,'textureBindings':['live_player_skin','evoker_fx'],'main':'Approved reference reflected horizontally at identical scale'},indent=2),encoding='utf8')
print(counts)
