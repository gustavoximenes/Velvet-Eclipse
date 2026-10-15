"""Player-sized reachability on the actual exported collision, not just room bounding boxes."""
from pathlib import Path
import gzip,json,math
import numpy as np
from collections import deque
def label(mask):
 out=np.zeros(mask.shape,np.int32);n=0
 for x,z in zip(*np.nonzero(mask)):
  if out[x,z]:continue
  n+=1;out[x,z]=n;q=deque([(x,z)])
  while q:
   i,j=q.popleft()
   for a,b in [(i-1,j),(i+1,j),(i,j-1),(i,j+1)]:
    if 0<=a<mask.shape[0] and 0<=b<mask.shape[1] and mask[a,b] and not out[a,b]:out[a,b]=n;q.append((a,b))
 return out,n
HERE=Path(__file__).parent;ROOT=HERE.parents[1];DATA=ROOT/'src/main/resources/data/velvet_eclipse/city'
def read(name):return json.loads(gzip.decompress((DATA/name).read_bytes()))
plan=read('plan.json.gz');defs=read('architecture.json.gz');rooms=read('school-rooms.json.gz')
cells={tuple(c[:3]):c for c in plan['cells']};catalog=read('furniture.json.gz')
for d in catalog['doors']:
 for c in d['cells']:cells[tuple(c[:3])]=[*c[:3],40,c[4]]
boxes=[]
for p,c in cells.items():
 if -51<=p[0]<=90 and -190<=p[2]<=-35 and 62<=p[1]<=89:
  if c[3]==40:boxes.extend(np.array(b)+[*p,*p] for b in defs[c[4]]['boxes'])
  elif c[3]>=100:
   name=plan['palette'][c[3]-100]['block']
   if 'leaves' not in name:boxes.append(np.array([*p,p[0]+1,p[1]+1,p[2]+1]))
boxes=np.asarray(boxes);XS=np.arange(-49.75,89.76,.5);ZS=np.arange(-189.75,-34.74,.5)
def ranges(lo,hi,arr):return max(0,math.ceil((lo-arr[0])/.5)),min(len(arr),math.ceil((hi-arr[0])/.5))
reports=[];fail=[]
for floor in [64,70,76,82]:
 fy=floor+(.28 if floor==82 else .08 if floor==64 else .035);blocked=np.zeros((len(XS),len(ZS)),bool);support=blocked.copy()
 for b in boxes[(boxes[:,4]>fy+.001)&(boxes[:,1]<fy+1.8)]:
  x0,x1=ranges(b[0]-.3,b[3]+.3,XS);z0,z1=ranges(b[2]-.3,b[5]+.3,ZS);blocked[x0:x1,z0:z1]=True
 for b in boxes[(boxes[:,4]<=fy+.001)&(boxes[:,4]>=fy-.36)]:
  x0,x1=ranges(b[0]-.05,b[3]+.05,XS);z0,z1=ranges(b[2]-.05,b[5]+.05,ZS);support[x0:x1,z0:z1]=True
 groups,n=label(~blocked&support)
 start=(0,-106) if floor==64 else (-5.3,-114.5)
 ix=int(round((start[0]-XS[0])/.5));iz=int(round((start[1]-ZS[0])/.5));group=groups[ix,iz]
 assert group,(floor,'corridor starting point is blocked')
 for r in rooms:
  if round(r['entry'][1])!=floor:continue
  x,_,z=r['entry'];ix=int(round((x-XS[0])/.5));iz=int(round((z-ZS[0])/.5));ok=groups[ix,iz]==group
  reports.append(dict(room=r['id'],floor=floor,reachable=bool(ok)))
  if not ok:fail.append((r['id'],r['entry'],int(groups[ix,iz]),int(group)))
 np.save(ROOT/f'build/gekkoukan/paths-{floor}.npy',groups==group)
def walk(points):
 y=points[0][1];samples=0
 for a,b in zip(points,points[1:]):
  a=np.array(a);b=np.array(b)
  for t in np.linspace(0,1,max(2,math.ceil(np.linalg.norm(b-a)/.08))):
   p=a+(b-a)*t;p[1]=y
   foot=(boxes[:,0]<p[0]+.3-1e-5)&(boxes[:,3]>p[0]-.3+1e-5)&(boxes[:,2]<p[2]+.3-1e-5)&(boxes[:,5]>p[2]-.3+1e-5)
   nearby=boxes[foot];support=(nearby[:,4]<=y+.6)&(nearby[:,4]>=y-.6)
   if not support.any():return dict(error='no support',at=p.tolist())
   y=float(nearby[support,4].max());p[1]=y
   hits=(nearby[:,4]>y+.01)&(nearby[:,1]<y+1.8)
   if hits.any():return dict(error='blocked',at=p.tolist(),boxes=nearby[hits][:3].tolist())
   samples+=1
 return dict(samples=samples,endY=y)
stairs={}
for level in range(3):
 y=64+level*6
 route=[[-7.7,y,-115.3],[-7.7,y,-121.5],[-5.3,y+3,-121.5],[-5.3,y+3,-114.5]]
 for direction,points,target in [('up',route,y+6),('down',list(reversed(route)),y)]:
  if direction=='down':points[0]=[points[0][0],y+6,points[0][2]]
  result=walk(points);stairs[f'{level}_{direction}']=result
  if 'error' in result or abs(result['endY']-target)>.04:fail.append((f'stairs_{level}_{direction}',result))
out=ROOT/'build/reports/gekkoukan';out.mkdir(parents=True,exist_ok=True);(out/'paths.json').write_text(json.dumps(dict(playerWidth=.6,playerHeight=1.8,step=.5,rooms=reports,stairs=stairs,failures=fail),indent=2))
print(json.dumps(dict(rooms=len(reports),stairs=stairs,failures=fail),indent=2));assert not fail,fail
