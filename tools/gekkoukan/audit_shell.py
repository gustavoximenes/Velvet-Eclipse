"""Probe the exported mesh at photographed envelope gaps (not collision alone)."""
from pathlib import Path
import gzip,json,sys,zipfile
import numpy as np
R=Path(__file__).resolve().parents[2];D='data/velvet_eclipse/city/'
jar=zipfile.ZipFile(sys.argv[1]) if len(sys.argv)>1 else None
def read(n):return gzip.decompress(jar.read(n) if jar else (R/'src/main/resources'/n).read_bytes())
plan=json.loads(read(D+'plan.json.gz'));defs=json.loads(read(D+'architecture.json.gz'));raw=np.frombuffer(read('assets/velvet_eclipse/city/architecture.bin.gz'),'>f4').reshape(-1,13)
cells={tuple(c[:3]):c[4] for c in plan['cells'] if c[3]==40};cache={};count=0;fail=[]
def covered(p,axis):
 base=np.floor(p).astype(int);uv=[i for i in range(3) if i!=axis]
 for d in [-1,0,1]:
  pos=base.copy();pos[axis]+=d;pos=tuple(pos)
  if pos not in cells:continue
  if pos not in cache:
   m=defs[cells[pos]];cache[pos]=raw[m['offset']//13:(m['offset']+m['length'])//13,:3].reshape(-1,3,3).astype(float)+pos
  t=cache[pos];selected=(np.abs(t[:,:,axis].mean(1)-p[axis])<.5)&(np.ptp(t[:,:,axis],axis=1)<1e-5);t=t[selected][:,:,uv]
  if not len(t):continue
  a=t[:,0];b=t[:,1]-a;c=t[:,2]-a;q=np.array(p)[uv]-a
  det=b[:,0]*c[:,1]-b[:,1]*c[:,0];good=np.abs(det)>1e-10;tiny=np.where(good,det,1)
  v=(q[:,0]*c[:,1]-q[:,1]*c[:,0])/tiny;w=(b[:,0]*q[:,1]-b[:,1]*q[:,0])/tiny
  if np.any(good&(v>=-1e-5)&(w>=-1e-5)&(v+w<=1+1e-5)):return True
 return False
def probe(name,axis,points):
 global count
 for p in points:
  count+=1
  if not covered(p,axis):fail.append(dict(area=name,point=p))
probe('hall_behind_stairs',2,[[x,y,-132] for x in [-10.7,-10.2,-9.7,-9.2] for y in np.arange(64.3,85.5,.5)])
probe('rear_facade',2,[[x,y,-186.24] for x in np.arange(-42.7,43,.7) for y in np.arange(64.3,81.5,.7)])
for x in [-21.12,21.12]:probe('court_upper_windows',0,[[x,y,z] for z in np.arange(-182.7,-135.2,.7) for y in np.r_[np.arange(70.3,75.5,.7),np.arange(76.3,81.5,.7)]])
probe('rear_link_roof',1,[[x,82,-184.5] for x in np.arange(-42.7,43,.7)])
probe('front_link_roof',1,[[x,82,-133.5] for x in np.arange(-20.7,21,.7)])
report=dict(probes=count,failures=fail[:30],failedCount=len(fail),mesh=True,inGame=False)
out=R/'build/reports/gekkoukan';out.mkdir(parents=True,exist_ok=True);(out/('shell-baseline.json' if jar else 'shell-validation.json')).write_text(json.dumps(report,indent=2))
if jar:assert fail,'Expected old screenshot gaps to be detected';print('CONFIRMED baseline gaps:',len(fail),'/',count)
else:assert not fail,fail[:10];print('PASS city envelope:',count,'mesh probes at the photographed gaps')
