"""Regression probes of the installed furniture, wall attachments and stair supports."""
from pathlib import Path
import gzip,json
import numpy as np
R=Path(__file__).resolve().parents[2];D=R/'src/main/resources/data/velvet_eclipse/city'
def read(p):return json.loads(gzip.decompress(p.read_bytes()))
plan=read(D/'plan.json.gz');defs=read(D/'architecture.json.gz');furniture=read(D/'furniture.json.gz')
cells={tuple(c[:3]):c[4] for c in plan['cells'] if c[3]==40}
count=0
def solid(point,label):
 global count
 count+=1;p=np.asarray(point);key=tuple(np.floor(p).astype(int));local=p-key
 assert key in cells,(label,'missing block',point)
 boxes=np.array(defs[cells[key]]['boxes'])
 assert len(boxes) and np.any(np.all((local>=boxes[:,:3]-1e-5)&(local<=boxes[:,3:]+1e-5),axis=1)),(label,'unsupported',point)
def dorm(p,label):solid(np.asarray(p)+[-100,65,66],label)
for lev in [1,2,3]:
 y=5*lev
 for x in ([-8.7,4] if lev<3 else [-8,4]):
  window=0 if x<0 else 12
  for yy in [.1,1,2.4]:dorm([-9.71 if x<0 else 3.44,y+yy,6.2],'wardrobe against side wall')
  dorm([x+7.6,y+2,2.29],'mirror on bedroom wall')
  dorm([x+4.2,y+3.5,2.28],'sconce mounted on bedroom wall')
  for dx in [-1.5,1.5]:dorm([window+dx,y+2,11.14],'curtain aligned to window')
  dorm([window,y+3.84,11.18],'curtain rod attached to reveal')
  dorm([window,y+1.4,10.55],'desk centered under window')
  seat=next(s for s in furniture['seats'] if s['id']==f'bedroom_{lev}_{0 if x<0 else 1}')
  assert abs(seat['x']-(window-100))<1e-5 and abs(seat['z']-75.28)<1e-5,'seat follows moved chair'
for lev in range(4):
 for z in [-9,-4,5]:
  dorm([-15.22 if lev==0 and z==5 else -10.04,lev*5+3.2,z],'stair sconce touches wall')
# Probe both continuous stringers, every tread and both full-height landing columns.
for lev in range(3):
 y=lev*6
 for i in range(12):
  top=y+(i+1)*.25
  for x in [-8.65,-6.88]:solid([x,64+max(y,top-.65)+.05,-110-6-(i+.5)*.4],'first flight structural stringer')
  top=y+3+(i+1)*.25
  for x in [-6.25,-4.48]:solid([x,64+top-.5,-110-10.8+(i+.5)*.4],'return flight structural stringer')
 for xx in np.arange(-8.65,-4.35,.2):solid([xx,64+y+2.6,-122.05],'landing crossbeam')
for x in [-8.6,-4.53]:
 for y in np.arange(.1,14.74,.2):solid([x,64+y,-122],'continuous landing pillar')
out=R/'build/reports/gekkoukan/fixtures-validation.json'
out.write_text(json.dumps(dict(checks=count,bedrooms=6,wallSconces=18,stairFlights=6,landingPillars=2,inGame=False),indent=2))
print('PASS furniture and stair attachments:',count,'installed-geometry probes')
