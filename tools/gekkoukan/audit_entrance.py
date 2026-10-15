"""Regression for floating brass arches left behind by the open dorm doors."""
from pathlib import Path
import gzip,json,sys,zipfile
import numpy as np
R=Path(__file__).resolve().parents[2]
sys.path.insert(0,str(R/'tools/city_polish'))
import export as exporter
jar=zipfile.ZipFile(sys.argv[1]) if len(sys.argv)>1 else None
D='data/velvet_eclipse/city/'
def read(n):return gzip.decompress(jar.read(n) if jar else (R/'src/main/resources'/n).read_bytes())
plan=json.loads(read(D+'plan.json.gz'));defs=json.loads(read(D+'architecture.json.gz'))
furniture=json.loads(read(D+'furniture.json.gz'))
raw=np.frombuffer(read('assets/velvet_eclipse/city/architecture.bin.gz'),'>f4').reshape(-1,13)
door=next(d for d in furniture['doors'] if d['id']=='dorm_entrance')
lo=np.array([-101,65.2,77.55]);hi=np.array([-99,68.75,78.25]);counts={}
for mode,slot in [('open',4),('closed',5)]:
 cells={tuple(c[:3]):c[4] for c in plan['cells'] if c[3]==40}
 for c in door['cells']:cells[tuple(c[:3])]=c[slot]
 count=0
 for pos,tile in cells.items():
  if np.any(np.array(pos)+1<=lo) or np.any(np.array(pos)>=hi):continue
  d=defs[tile];tris=raw[d['offset']//13:(d['offset']+d['length'])//13,:3].reshape(-1,3,3).astype(float)+pos
  for tri in tris:
   if np.any(tri.max(0)<=lo) or np.any(tri.min(0)>=hi):continue
   poly=list(tri)
   for axis in range(3):poly=exporter.clip(exporter.clip(poly,axis,lo[axis],True),axis,hi[axis],False)
   if len(poly)>2 and sum(np.linalg.norm(np.cross(poly[k]-poly[0],poly[k+1]-poly[0])) for k in range(1,len(poly)-1))>1e-8:count+=1
 counts[mode]=count
assert counts['closed']>0,counts
out=R/'build/reports/gekkoukan';out.mkdir(parents=True,exist_ok=True)
report=dict(apertureTriangles=counts,inGame=False)
(out/('entrance-baseline.json' if jar else 'entrance-validation.json')).write_text(json.dumps(report,indent=2))
if jar:
 assert counts['open']>0,counts
 print('CONFIRMED baseline floating entrance fragments:',counts)
else:
 assert counts['open']==0,counts
 print('PASS entrance: no fragments remain in open doorway;',counts)
