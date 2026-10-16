"""Detect coplanar duplicate coverage on the visible dorm jambs and lintels."""
from pathlib import Path
import gzip,json,sys,zipfile
import numpy as np
R=Path(__file__).resolve().parents[2];jar=zipfile.ZipFile(sys.argv[1]) if len(sys.argv)>1 else None
D='data/velvet_eclipse/city/'
def read(n):return gzip.decompress(jar.read(n) if jar else (R/'src/main/resources'/n).read_bytes())
plan=json.loads(read(D+'plan.json.gz'));defs=json.loads(read(D+'architecture.json.gz'));raw=np.frombuffer(read('assets/velvet_eclipse/city/architecture.bin.gz'),'>f4').reshape(-1,13)
cells={tuple(c[:3]):c[4] for c in plan['cells'] if c[3]==40};cache={};fail=[];count=0
# Only static surroundings; door leaves never occupy the selected exposed frame planes.
def overlap(p,axis):
 base=np.floor(p).astype(int);uv=[i for i in range(3) if i!=axis];hits=0
 for delta in [-1,0,1]:
  pos=base.copy();pos[axis]+=delta;pos=tuple(pos)
  if pos not in cells:continue
  if pos not in cache:
   m=defs[cells[pos]];cache[pos]=raw[m['offset']//13:(m['offset']+m['length'])//13,:3].reshape(-1,3,3).astype(float)+pos
  t=cache[pos];t=t[(np.max(abs(t[:,:,axis]-p[axis]),axis=1)<2e-5)][:,:,uv]
  if not len(t):continue
  a=t[:,0];b=t[:,1]-a;c=t[:,2]-a;q=np.array(p)[uv]-a;det=b[:,0]*c[:,1]-b[:,1]*c[:,0];valid=abs(det)>1e-10
  den=np.where(valid,det,1);v=(q[:,0]*c[:,1]-q[:,1]*c[:,0])/den;w=(b[:,0]*q[:,1]-b[:,1]*q[:,0])/den
  inside=valid&(v>=-1e-6)&(w>=-1e-6)&(v+w<=1+1e-6)
  edge=(abs(v)<1e-6)|(abs(w)<1e-6)|(abs(v+w-1)<1e-6)
  hits+=float(np.sum(np.where(edge,.5,1)[inside]))
 return hits
points=[]
for lev,z,x in [(lev,z,x) for lev in [1,2,3] for z in [2,-2.25] for x in [-6.75,6.4]]+[(0,-3,1.2)]:
 for side in [x,x+1.8]:
  for y in np.arange(lev*5+.173,lev*5+2.75,.31):
   for zz in np.arange(z-.051,z+.319,.041):points.append((0,[side-100,y+65,zz+66]))
 for xx in np.arange(x+.173,x+1.79,.21):
  for zz in np.arange(z-.051,z+.319,.041):points.append((1,[xx-100,lev*5+67.8,zz+66]))
for xx in np.arange(-12.16,-10.29,.13):
 for zz in np.arange(2.701,3.04,.033):points.append((1,[xx-100,87.5,zz+66]))
if not jar:
 # School jambs used the same coplanar wall/frame construction as the old dorm.
 assembly=json.loads((R/'tools/gekkoukan/assembly.json').read_text())
 for door in assembly['doors']:
  lo,hi=np.array(door['lo']),np.array(door['hi']);a=np.radians(door['angle']);c,s=np.cos(a),np.sin(a)
  rotation=np.array([[c,0,-s],[0,1,0],[s,0,c]])
  origin=np.array(door['offset'])+[0,64,-110]
  for side in [lo[0],hi[0]]:
   for y in np.arange(.173,2.75,.31):
    for z in np.arange(lo[2]+.031,hi[2]-.02,.041):
     points.append((2 if abs(s)>.5 else 0,(rotation@np.array([side,y,z])+origin).tolist()))
for axis,p in points:
 count+=1;n=overlap(p,axis)
 if n!=1:fail.append(dict(axis=axis,point=p,surfaces=n))
report=dict(probes=count,failedCount=len(fail),failures=fail[:15],inGame=False)
out=R/'build/reports/gekkoukan';out.mkdir(parents=True,exist_ok=True);(out/('jamb-baseline.json' if jar else 'jamb-validation.json')).write_text(json.dumps(report,indent=2))
if jar:assert fail;print('CONFIRMED baseline jamb overlaps:',len(fail),'/',count)
else:assert not fail,fail[:10];print('PASS city jambs:',count,'visible frame samples, exactly one surface each')
