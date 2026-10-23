"""Check sitting alignment against the actual chair back and cushion geometry."""
from pathlib import Path
import gzip,json,math
import numpy as np
from campus import transform
H=Path(__file__).parent;R=H.parents[1];D=R/'src/main/resources/data/velvet_eclipse/city'
def read(p):return json.loads(gzip.decompress(p.read_bytes()))
f=read(D/'furniture.json.gz');seats={s['id']:s for s in f['seats']};assembly=json.loads((H/'assembly.json').read_text());checks=0;opposed=0
for room in assembly['rooms']:
 p=H/'approved'/f"{room['id']}.json.gz"
 if not p.exists():continue
 meta=read(p)
 for i,prop in enumerate(meta['props']):
  if prop['kind']!='assento':continue
  q=prop['bounds'];cx=(q[0]+q[3])/2;cz=(q[2]+q[5])/2
  backs=[]
  for b in meta['solids']:
   lo=np.array(b['lo']);hi=np.array(b['hi']);size=hi-lo;mid=(lo+hi)/2
   if np.max(abs(size-[.62,.6,.08]))<1e-5 and abs(mid[0]-cx)<.01 and abs(mid[2]-cz)<.36 and abs(lo[1]-.65)<1e-5:backs.append(mid)
  assert len(backs)==1,(room['id'],i,'unique physical backrest',backs)
  s=seats[f"school_{room['id']}_{i}"];a=math.radians(s['yaw']);forward=np.array([-math.sin(a),0,math.cos(a)])
  back=transform(backs[0]-[cx,0,cz],room['angle'],[0,0,0]);assert np.dot(forward,back)<-.25,(s['id'],'faces through backrest')
  # Player riding offset is -.35, model hip is .75 above the entity origin.
  hip=s['y']-.35+.75;top=64+room['origin'][1]+.63
  assert abs(hip-top)<1e-5,(s['id'],'hips do not reach cushion',hip,top)
  checks+=2;opposed+=s['yaw']%360==0
assert opposed>10,'Opposed chairs in computer lab/library were lost'
out=R/'build/reports/gekkoukan/seating-validation.json';out.write_text(json.dumps(dict(checks=checks,opposedChairs=opposed,realBackrestGeometry=True,hipHeightError=0,inGame=False),indent=2))
print('PASS seating:',checks,'checks against real backrest geometry and native player sitting offsets')
