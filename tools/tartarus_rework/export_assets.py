"""Convert approved architectural geometry into native chunk-model prefab assets."""
from pathlib import Path
import json,gzip,struct,math,sys,hashlib
import numpy as np
from PIL import Image
import approved_v2 as v2
import approved_special as special
import scene_base as base
ROOT=Path(__file__).resolve().parents[2];OUT=ROOT/'src/main/resources/assets/velvet_eclipse/tartarus'
OUT.mkdir(parents=True,exist_ok=True)
ACCESS_ONLY='--access-only' in sys.argv
CAT=json.loads((OUT/'catalog.json').read_text()) if ACCESS_ONLY else {}
def export(s,name=None,tiles=None,blocked=(),collision=()):
 name=name or s.name;v=np.array(s.verts,dtype='<f4');assert len(v)%3==0 and np.isfinite(v).all()
 # Keep green ornament emission distinct from blue stone and silver/gold trim.
 green=(v[:,7]>v[:,6]*1.3)&(v[:,7]>v[:,8]*1.18)&np.isin(v[:,9],[3,5])
 v[green,9]=5;v[green,10]=.65;v[green,6:9]=np.minimum(.95,v[green,6:9]*1.5)
 glowing=[]
 for tri in v.reshape(-1,3,11):
  if tri[0,9]!=5:continue
  p=tri[:,:3].mean(0)+tri[0,3:6]*.35
  if tri[0,4]>.8:p[1]-=.65
  glowing.append(tuple(np.floor(p).astype(int).tolist()))
 # Original geometry is preserved; normals and vertex RGB survive into the block model.
 raw=struct.pack('<II',0x56454d31,len(v))+v.tobytes()
 (OUT/(name+'.mesh.gz')).write_bytes(gzip.compress(raw,compresslevel=9,mtime=0))
 low=v[:,:3].min(0).tolist();high=v[:,:3].max(0).tolist()
 info={'name':name,'vertices':len(v),'bounds':low+high,'lights':s.lights,'sha256':hashlib.sha256(raw).hexdigest()}
 if glowing:info['glowLights']=[list(p) for p in sorted(set(glowing))]
 if tiles is not None:info['tiles']=[list(t) for t in sorted(tiles)]
 if blocked:info['blocked']=[list(t) for t in sorted(blocked)]
 if collision:info['collision']=collision
 CAT[name]=info
def capture(self,*args,**kwargs):export(self)
v2.Scene.save=capture;base.Scene.save=capture
v2.hall()

# Architectural wall panel, floor, and full-size decorations are also creative kit blocks.
def kit(name,build):
 s=base.Scene(name);build(s)
 if name in ['kit_back','kit_return']:
  # Five block cells span [-2,3]; their centre is +0.5, not zero.
  for v in s.verts:v[0]+=.5
  for light in s.lights:light[0]+=.5
 export(s)
kit('kit_column',lambda s:s.pillar(.5,.5,3,.30,base=0,white=False))
kit('kit_ivory_column',lambda s:s.pillar(.5,.5,3,.30,base=0,white=True))
kit('kit_lantern',lambda s:s.lamp(.5,.75,.5))
kit('kit_rail',lambda s:(s.beam((.1,0,.5),(.1,1.1,.5),.04,base.PALE,3,6),s.beam((.9,0,.5),(.9,1.1,.5),.04,base.PALE,3,6),s.beam((0,1.1,.5),(1,1.1,.5),.07,base.PALE,3,6)))
kit('kit_cornice',lambda s:(s.box(0,0,0,1,.18,.4,base.IVORY,1),s.box(0,.18,.05,1,.12,.3,base.GOLD,3)))
kit('kit_seal',lambda s:special.seal(s,0,0,3,True))
kit('kit_arch',lambda s:special.vault(s,0,0,2.5,.15,5.3))
kit('kit_forward',lambda s:special.portal(s,0,0,0,4.8,3.65))
kit('kit_back',lambda s:special.portal(s,0,0,0,4.8,3.65,'back'))
kit('kit_return',lambda s:special.portal(s,0,0,0,4.8,3.65,'back',math.pi))
kit('kit_gate',lambda s:special.gate(s,0))
kit('kit_stair',lambda s:special.staircase(s,0,0,7))

# Door/terminal geometry used by their existing functional renderers, never duplicated in a room mesh.
kit('terminal',lambda s:special.terminal(s,0,0))
kit('treasure',lambda s:special.chest(s,0,0))
kit('monad',lambda s:special.portal(s,0,0,1,5.4,6.5,'monad'))

# Clean double door. The frame stays put when its two leaves open.
for opened in [False,True]:
 s=base.Scene('velvet_door_'+str(opened).lower())
 width,height=3.3,4.2
 for x in [-width/2-.22,width/2]:s.box(x,0,-.25,.22,height,.50,(.29,.40,.45),3)
 s.box(-width/2-.22,height,-.25,width+.44,.23,.50,base.GOLD,3)
 for sign in [-1,1]:
  leaf=base.Scene('leaf');left=0 if sign<0 else -width/2
  leaf.box(left,0,-.05,width/2,height,.10,(.055,.10,.27),0)
  leaf.ring((left+width/4,height*.58,.055),width*.17,.055,base.GOLD,'xy',3)
  leaf.box(left+(width/2-.25 if sign<0 else .16),height*.4,.06,.09,.40,.08,base.GOLD,3)
  back=len(leaf.verts);leaf.ring((left+width/4,height*.58,-.055),width*.17,.055,base.GOLD,'xy',3)
  for v in leaf.verts[back:]:v[5]=-1
  leaf.box(left+(width/2-.25 if sign<0 else .16),height*.4,-.14,.09,.40,.08,base.GOLD,3)
  special.transformed(s,leaf,sign*width/2,0,(-sign*math.pi/2 if opened else 0))
 # Minecraft door's north-facing plane, with its original centre preserved.
 for v in s.verts:v[0]+=.5;v[2]+=.125
 export(s)

if ACCESS_ONLY:
 (OUT/'catalog.json').write_text(json.dumps(CAT,separators=(',',':')),encoding='utf8')
 print('Updated hall/access prefabs; other templates preserved.',flush=True)
 sys.exit(0)

# Full fixed floors reuse the approved builder, omitting live props, doors, and the dynamic barrier.
special.WORK=Path(__file__).resolve().parent
special.plan=lambda f:json.loads((special.WORK/'plans'/f'floor-{f}.json').read_text())
special.save=lambda s,*args:export(s)
realPortal=special.portal
special.portal=lambda s,x,z,base=1,width=5,height=4,kind='forward',angle=0: realPortal(s,x,z,base,width,height,kind,angle) if kind=='forward' else None
special.terminal=lambda *a:None;special.chest=lambda *a:None;special.gate=lambda *a:None
special.guardian_floor(10);special.guardian_floor(20);special.transition_floor()
special.portal=realPortal

# Room shell variants follow every possible cardinal connection, with an independent visual choice.
for r in range(7,11):
 for kind in ['plain','long','pillars','platforms','stairs']:
  for mask in range(16):
   # The original twin stairs are kept intact. E/W straight rooms rotate the
   # complete N/S template, including the collision and lights, by 90 degrees.
   visual_mask=5 if kind=='platforms' and mask==10 else mask
   ports=tuple(side for i,side in enumerate(['N','E','S','W']) if visual_mask&(1<<i))
   rx=r if kind!='long' else max(5,r-2);rz=r
   tiles=v2.rect(-rx,-rz,rx,rz)
   # Short central arms preserve graph doors even when the long room narrows.
   if 'E' in ports:tiles |=v2.rect(rx+1,-2,r,2)
   if 'W' in ports:tiles |=v2.rect(-r,-2,-rx-1,2)
   name=f'room_{r}_{kind}_{mask}'
   s=base.Scene(name);special.shell(s,tiles,14,[(0,-r,'z',5,8.4)]*('N' in ports)+[(0,r,'z',5,8.4)]*('S' in ports)+[(r,0,'x',5,8.4)]*('E' in ports)+[(-r,0,'x',5,8.4)]*('W' in ports))
   special.border(s,.5,.5,min(rx,rz)-.8)
   for x in [-rx+1,rx-1]:
    for z in [-rz+1,rz-1]:s.pillar(x,z,11,.43,white=False)
   if kind!='stairs':special.vault(s,.5,.5,rx-.12,rz-.10,14,False)
   obstacles=set();collision=[]
   if kind=='pillars':
    # Two substantial pillars, outside the protected spawn and treasure anchors.
    # Keep the lateral doorway approach clear when this room has E/W connections.
    for x,z in [(-r+3,-r+3 if mask&10 else 0),(r-3,-r+3 if mask&10 else 0)]:
     radius=1.20 if r<9 else 1.55
     s.beam((x,1,z),(x,13.8,z),radius,(.17,.23,.32),2,32)
     for y in [1,1.4,12.7]:s.beam((x,y,z),(x,y+.2,z),radius+.10,(.13,.19,.27),3,32)
     obstacles|={(xx,zz) for xx,zz in tiles if (xx+.5-x)**2+(zz+.5-z)**2<(radius+.15)**2}
   if kind=='platforms':
    # Compact versions of the approved lateral landings, clear of centre/loot (+4,+4).
    for sign in [-1,1]:
     # Original full-size landing, stairs and railing. These templates are only
     # selected for opposite entrances, parallel to the two platforms.
     end=3
     x=sign*(r-1.75)-.95;s.box(x,1,-r+2,1.9,2,end+r-2,(.09,.14,.23),2)
     for i in range(4):s.box(x,1,end+i*.5,1.9,2-i*.5,.5,(.22,.32,.37),1)
     for zz in np.arange(-r+2,end,1.2):s.beam((x+.1,3,zz),(x+.1,4.1,zz),.05,base.PALE,3,6)
     if end>-r+2:s.beam((x+.1,4.1,-r+2),(x+.1,4.1,end),.07,base.PALE,3,6)
     for xx in range(math.ceil(x-.5),math.ceil(x+1.9-.5)):
      for zz in range(-r+2,end):
       for yy in [1,2]:collision.append([xx,yy,zz,'scenic_wall'])
      collision.extend([[xx,1,end,'scenic_wall'],[xx,2,end,'scenic_stairs'],[xx,1,end+1,'scenic_stairs']])
   if kind=='stairs':special.staircase(s,0,0,7)
   else:special.seal(s,0,0,1.8 if kind=='pillars' else min(3,rx-3),False)
   for x in [-rx+2,rx-2]:s.lamp(x,8.6,0)
   if kind=='platforms' and mask==10:
    for v in s.verts:v[0],v[2],v[3],v[5]=1-v[2],v[0],-v[5],v[3]
    for light in s.lights:light[0],light[2]=1-light[2],light[0]
    collision=[[-z,y,x,material] for x,y,z,material in collision]
   export(s,name,tiles,obstacles,collision)

# Five-wide corridor cell strips can be combined in L/T/+ without changing the graph.
for mask in range(16):
 for phase in range(7):
  s=base.Scene(f'passage_{mask}_{phase}')
  # One real floor tile and its boundary walls. The union of these tiles handles L/T/+ naturally.
  col=(.16,.22,.27) if phase%2 else (.10,.14,.19)
  s.quad([(0,1,0),(0,1,1),(1,1,1),(1,1,0)],col,1,normal=(0,1,0))
  s.quad([(0,14,0),(1,14,0),(1,14,1),(0,14,1)],base.STONE,2,normal=(0,-1,0))
  for direction in range(4):
   if mask&(1<<direction):continue
   if direction==0:origin=np.array((0,0,0));u=np.array((1,0,0));n=(0,0,1)
   elif direction==1:origin=np.array((1,0,0));u=np.array((0,0,1));n=(-1,0,0)
   elif direction==2:origin=np.array((0,0,1));u=np.array((1,0,0));n=(0,0,-1)
   else:origin=np.array((0,0,0));u=np.array((0,0,1));n=(1,0,0)
   def face(a,b,c,d,color,mat=0,em=0,offset=0):
    off=np.array(n)*offset;s.quad([origin+u*a+(0,b,0)+off,origin+u*c+(0,b,0)+off,origin+u*c+(0,d,0)+off,origin+u*a+(0,d,0)+off],color,mat,em,n)
   face(0,1,1,14,base.STONE,2)
   for y in [1.25,3,13]:face(0,y,1,y+.12,(.18,.26,.32),3,offset=.04)
   if phase in (1,2,3):
    face(.04,5,.96,11.2,base.DARK,offset=.045)
    for row in range(8):
     for column in range(2):face(.12+column*.4,5.12+row*.72,.44+column*.4,5.73+row*.72,base.GLASS,5,.35,offset=.065)
   if phase==0:face(.15,1,.85,13.6,(.18,.25,.34),1,offset=.18)
  export(s)

for mask in range(16):
 tiles=v2.rect(-2,-2,2,2)
 for i in range(4):
  if mask&(1<<i):tiles|=[v2.rect(-2,-5,2,-3),v2.rect(3,-2,5,2),v2.rect(-2,3,2,5),v2.rect(-5,-2,-3,2)][i]
 s=base.Scene(f'junction_{mask}');special.shell(s,tiles,14,[(0,-5,'z',5,14),(5,0,'x',5,14),(0,5,'z',5,14),(-5,0,'x',5,14)])
 s.lamp(0,8.5,0);export(s,tiles=tiles)

(OUT/'catalog.json').write_text(json.dumps(CAT,separators=(',',':')),encoding='utf8')
print(f'Exported {len(CAT)} approved native geometry prefabs.',flush=True)
