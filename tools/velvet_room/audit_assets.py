"""Structural/kinematic checks of exported production assets; no GL/client claims."""
from pathlib import Path
import gzip,struct,json
import numpy as np
import choreography as c

out=c.ROOT.parents[1]/'build/reports/velvet-rework'
out.mkdir(parents=True,exist_ok=True)
report={}
for scene in json.loads((c.ASSET/'room/manifest.json').read_text()):
 name=scene['name'];meshes=[]
 with gzip.open(c.ASSET/'room'/f'{name}.bin.gz','rb') as f:
  def integer():return struct.unpack('>i',f.read(4))[0]
  assert integer()==0x56454157 and integer()==1
  count=integer();assert count==scene['meshes']
  for _ in range(count):
   texture=f.read(integer()).decode();variant=integer();n=integer()
   assert 4<=n<=100000 and n%4==0
   vertices=np.frombuffer(f.read(n*20),dtype='>f4').reshape(n,5)
   assert np.isfinite(vertices).all()
   slot=integer();meshes.append((texture,variant,slot))
   assert texture in ('player','velvet_fusion_front') or (c.ASSET/'textures/entity'/f'{texture}.png').exists()
   if texture=='velvet_fusion_front':assert slot>=100 and (vertices[:,2]<0).all()
   if texture=='velvet_fusion_card':assert slot>=100 and (vertices[:,2]>0).all()
  start,fps=struct.unpack('>ff',f.read(8));frames=integer()
  data=np.frombuffer(f.read(),dtype='>f4').reshape(frames,14+16*count)
  assert fps==30 and start==0 and frames==scene['frames'] and np.isfinite(data).all()
 matrices=data[:,14:].reshape(frames,count,4,4)
 for i,(texture,variant,slot) in enumerate(meshes):
  if texture not in ('player','igor','elizabeth'):continue
  visible=matrices[:,i,3,3]>.5
  singular=np.linalg.svd(matrices[visible,i,:3,:3],compute_uv=False)
  assert np.allclose(singular,singular[0],atol=1e-6),f'{name}: animated body scale'
  assert np.allclose(singular,singular[:,:1],atol=1e-6),f'{name}: stretched bone'
 if name=='fusion':
  assert sum(t=='velvet_fusion_front' for t,_,_ in meshes)==6
  assert sum(t=='velvet_fusion_card' for t,_,_ in meshes)==6
  assert (data[:,0]<5.1).all(),'camera outside the grille'
  for i,(texture,variant,slot) in enumerate(meshes):
   if slot<100:continue
   visible=matrices[:,i,3,3]>.5
   assert not visible[int(5*fps):].any(),'cards survive forehead contact'
   for frame in range(120,150):
    normal=matrices[frame,i,:3,2];normal=normal/np.linalg.norm(normal)
    assert abs(normal[1])>.9999,'card flight plane is not horizontal'
  assert c.recoil(5.2)>23.99 and abs(c.recoil(6.9))<1e-8
  assert np.linalg.norm(c.nape(5.2)-c.nape(5))>.04,'nape must follow head recoil'
 report[name]={'meshes':count,'frames':frames,'duration':scene['seconds'],'rigid_bones':True,'resources_present':True}
# The room-only tone curve lifts shadows monotonically, preserving highlights.
y=np.linspace(0,1,10001);x=np.clip((y-.06)/(.35-.06),0,1);shadow=1-x*x*(3-2*x)
target=y+(np.maximum(y,np.sqrt(y)*.6)-y)*shadow
gain=np.minimum(2.5,target/np.maximum(y,.0001));result=y*gain
assert np.min(np.diff(result))>=-1e-8 and np.all(result>=y-1e-8)
assert result[0]==0 and np.allclose(result[y>=.35],y[y>=.35])
report['shader_curve']={'monotonic':True,'black_preserved':True,'highlights_preserved':True,'in_game_shaderpack_tested':False}
(out/'assets-audit.json').write_text(json.dumps(report,indent=2),encoding='utf8')
print('PASS: production scene resources, rigid bones, horizontal flight, animated nape, shader tone curve. Offline only.')
