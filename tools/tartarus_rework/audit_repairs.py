"""Regressions from the in-game portal, hall and lateral-stair screenshots."""
from pathlib import Path
import gzip,json
import numpy as np
from PIL import Image
ROOT=Path(__file__).resolve().parents[2]
A=ROOT/'src/main/resources/assets/velvet_eclipse'
checks=0
def check(ok,why):
 global checks
 checks+=1
 if not ok:raise AssertionError(why)
def mesh(name):return np.frombuffer(gzip.decompress((A/'tartarus'/f'{name}.mesh.gz').read_bytes()),dtype='<f4',offset=8).reshape(-1,3,11)
def coverage(raw,axis,plane_low,plane_high,samples,label):
 # Project only the requested opaque surface band. The sky or a distant wall
 # cannot accidentally make a missing portal reveal pass this test.
 axes=[i for i in range(3) if i!=axis]
 selected=raw[(raw[:,:,axis].min(1)>=plane_low)&(raw[:,:,axis].max(1)<=plane_high)&(abs(raw[:,0,axis+3])>.8)]
 p=selected[:,:,axes];a=p[:,0];b=p[:,1]-a;c=p[:,2]-a;det=b[:,0]*c[:,1]-b[:,1]*c[:,0]
 valid=abs(det)>1e-8;a=a[valid];b=b[valid];c=c[valid];det=det[valid]
 for sample in samples:
  q=np.array(sample)-a;u=(q[:,0]*c[:,1]-q[:,1]*c[:,0])/det;v=(b[:,0]*q[:,1]-b[:,1]*q[:,0])/det
  check(np.any((u>=-1e-5)&(v>=-1e-5)&(u+v<=1.00001)),f'{label}: exposed hole at {sample}')
hall=mesh('hall')
coverage(hall,2,30,31.1,[(x,y) for x in np.linspace(-27.5,28.5,29) for y in np.linspace(1.2,33.8,18)],'hall entrance wall')
coverage(hall,1,21.99,22.36,[(x,z) for x in np.linspace(-2.25,2.25,13) for z in np.linspace(-21.95,-17.41,17)],'clock tunnel ceiling')
coverage(hall,1,16.84,17.05,[(x,z) for x in np.linspace(-2.25,2.25,13) for z in np.linspace(-21.95,-16.96,17)],'clock threshold has no slit')
coverage(hall,2,-22.01,-21.9,[(x,y) for x in np.linspace(-2.28,2.28,13) for y in np.linspace(17.1,22.04,17)],'clock portal plane')
for name in ['kit_back','kit_return']:
 coverage(mesh(name),2,-1.71,1.71,[(x,y) for x in np.linspace(-2.9,3.9,25) for y in np.linspace(.02,8.45 if name=='kit_back' else 5.05,22)],name+' full reveal')

def clip(poly,axis,value,greater):
 out=[]
 for a,b in zip(poly,poly[1:]+poly[:1]):
  ia=a[axis]>=value if greater else a[axis]<=value;ib=b[axis]>=value if greater else b[axis]<=value
  if ia:out.append(a)
  if ia!=ib:out.append(a+(b-a)*((value-a[axis])/(b[axis]-a[axis])))
 return out
for radius in [9,10]:
 for mask in [5,10]:
  tris=mesh(f'room_{radius}_platforms_{mask}')[:,:,:3]
  for direction in range(4):
   if not mask&(1<<direction):continue
   axis=2 if direction%2==0 else 0;across=2-axis;sign=-1 if direction in [0,3] else 1
   low,high=sorted([sign*(radius-3),sign*(radius+.95)])
   bounds={axis:(low+.001,high-.001),across:(-1.99,2.99),1:(1.10,4.50)}
   candidates=tris
   for a,(lo,hi) in bounds.items():candidates=candidates[(candidates[:,:,a].max(1)>lo)&(candidates[:,:,a].min(1)<hi)]
   for tri in candidates:
    poly=list(tri)
    for a,(lo,hi) in bounds.items():poly=clip(clip(poly,a,lo,True),a,hi,False)
    check(len(poly)<3 or sum(np.linalg.norm(np.cross(poly[i]-poly[0],poly[i+1]-poly[0])) for i in range(1,len(poly)-1))<1e-7,f'platform blocks doorway: r={radius} mask={mask} direction={direction}')
   checks+=1
 # Rotation must preserve the original platforms exactly, not shrink them.
 original=mesh(f'room_{radius}_platforms_5');rotated=mesh(f'room_{radius}_platforms_10')
 check(len(original)==len(rotated),'rotated twin stairs keep all geometry')
 expected=original.copy();expected[:,:,0]=1-original[:,:,2];expected[:,:,2]=original[:,:,0];expected[:,:,3]=-original[:,:,5];expected[:,:,5]=original[:,:,3]
 check(np.allclose(expected,rotated,atol=1e-6),'twin stairs are the original room turned 90 degrees')
for material in ['5','6','window','light']:
 diffuse=Image.open(A/f'textures/block/tartarus_native_{material}.png')
 emission=Image.open(A/f'textures/block/tartarus_native_{material}_e.png')
 pbr=Image.open(A/f'textures/block/tartarus_native_{material}_s.png')
 check(diffuse.size==emission.size==pbr.size,'emissive maps match diffuse dimensions')
 check(pbr.getpixel((0,0))[3] in range(1,255),'labPBR emission alpha is active')
for material in [7,8,9]:
 pixels=np.array(Image.open(A/f'textures/block/tartarus_native_{material}.png'))
 check(np.array_equal(pixels[:,0],pixels[:,-1]),'animated portal texture has no repeat seam')
aliases=(A/'shaders/block.properties').read_text()
check('block.10028=velvet_eclipse:tartarus_architecture_emissive ' in aliases,'Complementary receives true modded emission for green surfaces')
check('velvet_eclipse:tartarus_architecture ' not in aliases,'stone is not classified as an emitter')
check((A.parent/'minecraft/optifine/emissive.properties').read_text().strip()=='suffix.emissive=_e','OptiFine default namespace emissive discovery')
report=dict(result='PASS',checks=checks,architectureVersion=2,inGame=False)
out=ROOT/'build/reports/tartarus-rework';out.mkdir(parents=True,exist_ok=True)
(out/'repairs.json').write_text(json.dumps(report,indent=2))
print(json.dumps(report))
