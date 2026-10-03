"""Deterministic approval model; never writes to mod sources, packs or worlds."""
from pathlib import Path
import json,math,hashlib
import numpy as np
ROOT=Path(__file__).resolve().parents[2];WORK=Path(__file__).resolve().parent;OUT=ROOT/'outputs/tartarus-previa-v2'
PI=math.pi
IVORY=(.71,.78,.77);BLUE=(.024,.13,.38);DARK=(.027,.045,.075);GREEN=(.035,.105,.088);GOLD=(.55,.37,.105);PALE=(.40,.53,.48);STONE=(.09,.14,.23);GLASS=(.16,.74,.27);CYAN=(.20,.80,.96)
class Scene:
 def __init__(self,name):self.name=name;self.verts=[];self.lights=[];self.cutPortal=False
 def quad(self,p,col,mat=0,emit=0,normal=None):
  p=np.asarray(p,float);n=np.cross(p[1]-p[0],p[2]-p[0]) if normal is None else np.array(normal,float);n/=max(1e-8,np.linalg.norm(n))
  if self.cutPortal:
   # Clip clock ornament triangles around the real opening instead of covering it with a decal.
   def clip(poly,axis,bound,greater):
    out=[]
    for a,b in zip(poly,poly[1:]+poly[:1]):
     ina=(a[axis]>=bound) if greater else (a[axis]<=bound);inb=(b[axis]>=bound) if greater else (b[axis]<=bound)
     if ina:out.append(a)
     if ina!=inb:out.append(a+(b-a)*((bound-a[axis])/(b[axis]-a[axis])))
    return out
   for inds in [(0,1,2),(0,2,3)]:
    tri=[p[i] for i in inds]
    pieces=[clip(tri,0,-2.5,False),clip(tri,0,2.5,True)]
    middle=clip(clip(tri,0,-2.5,True),0,2.5,False);pieces += [clip(middle,1,22.1,True),clip(middle,1,16.95,False)]
    for poly in pieces:
     for j in range(1,len(poly)-1):
      for v in [poly[0],poly[j],poly[j+1]]:self.verts.append([*v,*n,*col,mat,emit])
  else:
   for i in [0,1,2,0,2,3]:self.verts.append([*p[i],*n,*col,mat,emit])
 def box(self,x,y,z,w,h,d,col,mat=0,emit=0):
  a,b,c=x+w,y+h,z+d
  for p,n in [([(x,y,z),(a,y,z),(a,b,z),(x,b,z)],(0,0,-1)), ([(x,y,c),(x,b,c),(a,b,c),(a,y,c)],(0,0,1)), ([(x,b,z),(a,b,z),(a,b,c),(x,b,c)],(0,1,0)), ([(x,y,z),(x,y,c),(a,y,c),(a,y,z)],(0,-1,0)), ([(x,y,z),(x,b,z),(x,b,c),(x,y,c)],(-1,0,0)), ([(a,y,z),(a,y,c),(a,b,c),(a,b,z)],(1,0,0))]:self.quad(p,col,mat,emit,n)
 def beam(self,a,b,r,col,mat=0,segments=6):
  a=np.array(a,float);b=np.array(b,float);n=b-a;n/=np.linalg.norm(n);u=np.cross(n,(0,1,0) if abs(n[1])<.9 else (1,0,0));u/=np.linalg.norm(u);v=np.cross(n,u)
  for i in range(segments):
   aa=2*PI*i/segments;ab=2*PI*(i+1)/segments;q=(math.cos(aa)*u+math.sin(aa)*v)*r;t=(math.cos(ab)*u+math.sin(ab)*v)*r
   self.quad([a+q,b+q,b+t,a+t],col,mat,normal=q+t)
 def ring(self,center,r,width,col,plane='xy',mat=3,emit=0,segments=64,start=0,end=2*PI):
  x,y,z=center
  def p(a,rr):return (x+math.cos(a)*rr,y+math.sin(a)*rr,z) if plane=='xy' else (x+math.cos(a)*rr,y,z+math.sin(a)*rr)
  for i in range(segments):
   a=start+(end-start)*i/segments;b=start+(end-start)*(i+1)/segments
   self.quad([p(a,r),p(b,r),p(b,r-width),p(a,r-width)],col,mat,emit,(0,0,1) if plane=='xy' else (0,1,0))
 def disk(self,c,r,col,plane='xy',mat=0,emit=0,n=48):self.ring(c,r,r,col,plane,mat,emit,n)
 def pillar(self,x,z,height=24,r=1.25,base=1,white=True):
  color=IVORY if white else (.15,.21,.29)
  self.box(x-r-.4,base,z-r-.4,2*r+.8,.4,2*r+.8,color,1)
  self.box(x-r-.22,base+.4,z-r-.22,2*r+.44,.5,2*r+.44,color,1)
  self.beam((x,base+.9,z),(x,height+base-1,z),r,color,1,16)
  for j in range(16):
   a=j*PI/8;xx=x+math.cos(a)*(r+.015);zz=z+math.sin(a)*(r+.015)
   self.beam((xx,base+1.2,zz),(xx,base+height-1.25,zz),.035,(.35,.47,.54),0,4)
  self.box(x-r-.3,base+height-1,z-r-.3,2*r+.6,.45,2*r+.6,color,1)
  self.box(x-r-.5,base+height-.55,z-r-.5,2*r+1,.4,2*r+1,GOLD if white else PALE,3)
 def lamp(self,x,y,z):
  self.beam((x,y+2,z),(x,y+.55,z),.045,GOLD,3)
  self.beam((x,y-.7,z),(x,y+.5,z),.34,CYAN,6,6)
  for i in range(6):
   a=i*PI/3;self.beam((x+math.cos(a)*.36,y-.75,z+math.sin(a)*.36),(x+math.cos(a)*.36,y+.55,z+math.sin(a)*.36),.038,GOLD,3,4)
  self.lights.append([x,y,z,.33,.74,1.0,11])
 def save(self,views,shots=None):
  np.array(self.verts,dtype='<f4').tofile(WORK/(self.name+'.bin'))
  data={'name':self.name,'vertexCount':len(self.verts),'lights':self.lights[:32],'views':views,'shots':shots or [],'key':[0,30,18] if self.name=='hall' else [2,14,5],'center':[0,7,-6] if self.name=='hall' else [0,2,0]}
  (WORK/(self.name+'.json')).write_text(json.dumps(data),encoding='utf8');print(self.name,len(self.verts),'vertices',len(self.lights),'lights',flush=True)

def hall():
 s=Scene('hall')
 # Shell uses the existing hall's 57x61 footprint and height. Surface finish is redesigned.
 s.box(-28,0,-30,57,1,61,DARK)
 for x in range(-28,29,2):
  for z in range(-30,31,2):
   w=min(1.975,29-x);d=min(1.975,31-z);m=IVORY if ((x//2+z//2)%2==0) else BLUE
   s.box(x+.012,1,z+.012,w,.028,d,m,1)
 s.ring((0,1.065,0),21.4,.55,GOLD,'xz');s.ring((0,1.067,0),20.7,.10,GOLD,'xz')
 s.ring((0,1.06,0),20.5,2.1,IVORY,'xz',1)
 s.box(-4.5,1.085,-10,9,.024,41,(.022,.31,.53),4)
 for x in [-4.66,4.5]:s.box(x,1.09,-10,.16,.035,41,GOLD,3)
 s.box(-28,1,-30,57,33,.4,GREEN,2);s.box(-28,1,-30,.4,33,61,GREEN,2);s.box(28.6,1,-30,.4,33,61,GREEN,2)
 # The exit portal sits in front of a closed wall, not an opening into the dimension sky.
 s.box(-28,1,30.6,57,33,.4,GREEN,2)
 for x in range(-26,28,6):
  s.box(x,3,30.32,4.2,26,.16,(.03,.11,.10),2)
  for xx in [x-.12,x+4.2]:s.box(xx,2,30.05,.16,28,.25,(.20,.28,.24),3)
  for y in [3,5,26,29]:s.box(x-.12,y,30.05,4.5,.14,.25,PALE,3)
 s.box(-28,34,-30,57,.3,61,(.09,.18,.23),2)
 # Tall architectural wall bays, deep emerald insets and separate framing.
 for x in range(-26,28,6):
  s.box(x,3,-29.52,4.2,26,.16,(.03,.11,.10),2)
  for xx in [x-.12,x+4.2]:s.box(xx,2,-29.3,.16,28,.25,(.20,.28,.24),3)
  for y in [3,5,26,29]:s.box(x-.12,y,-29.3,4.5,.14,.25,PALE,3)
  for y in [10,17,23]:
   for xx in [x+1,x+2.6]:s.quad([(xx,y,-29.29),(xx+.55,y+1,-29.29),(xx,y+2,-29.29),(xx-.55,y+1,-29.29)],(.05,.24,.12),5,.24,(0,0,1))
 for side in [-1,1]:
  xx=-27.5 if side<0 else 28.45
  for z in range(-26,28,6):
   s.box(xx,3,z,.12,25,4.2,(.025,.10,.08),2)
   for zz in [z-.12,z+4.2]:s.box(xx-.04,2,zz,.22,28,.16,(.12,.26,.20),3)
   for y in [3,5,26,29]:s.box(xx-.08,y,z,.3,.13,4.35,PALE,3)
   for y in [10,17,23]:s.quad([(xx-side*.03,y,z+2),(xx-side*.03,y+1,z+2.7),(xx-side*.03,y+2,z+2),(xx-side*.03,y+1,z+1.3)],(.04,.25,.12),5,.22,(-side,0,0))
 # Cornices/coffers stay inside the current ceiling envelope.
 for y,w in [(30,.30),(31,.42),(32.4,.26)]:
  s.box(-27.8,y,-29.15,56.2,w,.5,IVORY,1)
  s.box(-27.8,y,30.05,56.2,w,.5,IVORY,1)
  s.box(-27.8,y,-29.15,.5,w,59.8,IVORY,1);s.box(28,y,-29.15,.5,w,59.8,IVORY,1)
 for x in range(-24,28,8):s.box(x,33.5,-29,.15,.3,59,GOLD,3)
 for z in range(-26,30,8):s.box(-27,33.5,z,55,.3,.15,GOLD,3)
 for x in [-20,20]:
  for z in [-22,-7,9,23]:s.pillar(x,z,25,1.30)
 for i in range(16):
  z=5-i;y=1+i
  # Half-block risers match a stair silhouette while keeping the same landing anchor.
  s.box(-5.5,1,z,11,y-.5,1,IVORY,1);s.box(-5.5,y+.5,z,11,.5,.5,IVORY,1)
  for zz,yy in [(z,y+1),(z+.5,y+.5)]:
   s.box(-4.7,yy+.015,zz+.005,9.4,.025,.49,(.035,.39,.67),4)
   s.box(-4.7,yy-.5,zz+.496,9.4,.515,.018,(.025,.32,.58),4)
  for x in [-5.5,4.7]:s.box(x,y+1.02,z,.8,.04,1,IVORY,1)
  s.box(-4.7,y+.47,z+.995,9.4,.035,.024,(.12,.52,.69),0)
  for x in [-7,6.4]:s.box(x,1,z,.6,y+1,1,IVORY,1);s.box(x-.06,y+2.02,z,.72,.10,1,GOLD,3)
 s.box(-6,16,-17,12,1,7,IVORY,1);s.box(-4.7,17.02,-17,9.4,.02,7,(.035,.34,.57),4)
 # Relieved astrolabe clock in the same location as the original block dial.
 s.cutPortal=True
 for r,w,col,zz in [(8.45,.35,GOLD,-18.4),(8.05,.28,IVORY,-18.25),(7.72,.5,GOLD,-18.05),(7.12,.14,PALE,-17.9)]:s.ring((0,23.5,zz),r,w,col)
 # The decorated border is modeled in relief, including paired curls and small studs.
 for i in range(48):
  a=i*2*PI/48;rr=7.48;cx=math.cos(a)*rr;cy=23.5+math.sin(a)*rr
  s.disk((cx,cy,-17.82),.10,(.82,.65,.29),mat=3,n=8)
 for side in [-1,1]:
  for y in [18.2,20.2,22.2]:
   cx=side*7.8
   s.ring((cx,y,-17.48),.75,.10,GOLD,start=-PI*.2,end=PI*1.8,segments=24)
   s.ring((cx+side*.24,y+.20,-17.44),.35,.07,(.76,.55,.19),start=0,end=PI*1.65,segments=20)
 s.disk((0,23.5,-18.1),7.0,(.055,.18,.105),mat=5,emit=.15)
 s.ring((0,23.5,-17.88),6.72,1.10,IVORY);s.ring((0,23.5,-17.80),5.63,.15,GOLD)
 for i in range(60):
  a=i*2*PI/60;r=6.6;rr=5.77 if i%5==0 else 6.22
  s.beam((math.sin(a)*r,23.5+math.cos(a)*r,-17.69),(math.sin(a)*rr,23.5+math.cos(a)*rr,-17.69),.082 if i%5==0 else .034,DARK,0,4)
 for cx,cy in [(-2.7,26.2),(2.7,26.2),(-4.15,21.5)]:
  s.disk((cx,cy,-17.51),1.53,GOLD,mat=3);s.disk((cx,cy,-17.40),1.29,(.52,.63,.43),mat=1);s.ring((cx,cy,-17.33),1.12,.07,PALE)
  for i in range(12):
   a=i*PI/6;s.beam((cx+math.cos(a)*.94,cy+math.sin(a)*.94,-17.31),(cx+math.cos(a)*1.1,cy+math.sin(a)*1.1,-17.31),.022,GOLD,3,4)
  s.beam((cx,cy,-17.28),(cx+.62,cy+.58,-17.28),.055,DARK);s.beam((cx,cy,-17.28),(cx-.1,cy+.95,-17.28),.035,DARK)
 s.beam((0,23.5,-17.2),(0,28.5,-17.2),.12,IVORY,3);s.beam((0,23.5,-17.16),(3.7,21.7,-17.16),.16,GOLD,3);s.disk((0,23.5,-17.05),.47,GOLD)
 for side in [-1,1]:
  s.box(side*7.8-.5,17,-18.4,1,6,1,GOLD,3)
  for y in [17,18,19,20,21,22]:s.box(side*7.8-.65,y,-17.48,1.3,.17,.16,IVORY,1)
  s.beam((side*7.8,1,-18.2),(side*7.8,17,-18.2),.22,GOLD,3,8)
  for y in [2,4,14,16]:s.box(side*7.8-.32,y,-18.5,.64,.25,.64,GOLD,3)
 s.cutPortal=False
 # The route is at the original landing. A recessed opening replaces the occluded rectangle.
 s.box(-2.5,16.85,-23.0,5,.18,6.05,(.035,.32,.58),4)
 for side in [-1,1]:
  x=side*2.5
  s.box(x-.16,17,-20,.32,5.25,2.70,GOLD,3)
  s.box(x-.055,17,-17.23,.11,5.2,.07,(.23,.58,.44),5,.32)
 s.box(-2.66,22.05,-20,5.32,.3,2.7,GOLD,3)
 # Continuous reveals from the dial through to the portal, including the missing soffit.
 s.box(-2.60,16.99,-22.1,.31,5.31,4.7,DARK,2);s.box(2.29,16.99,-22.1,.31,5.31,4.7,DARK,2)
 s.box(-2.60,22.0,-22.1,5.2,.3,4.7,DARK,2)
 s.box(-2.3,17,-22,4.6,5.1,.08,(.012,.048,.029),7,.12)
 s.box(-2.15,17.04,-19.75,4.3,.025,2.0,(.025,.32,.45),4)
 for j in range(3):s.ring((0,17.075,-19.5),1.8-j*.25,.045,(.19,.62,.38),'xz',5,.20,48)
 s.lights.append([0,19.4,-18.6,.2,.95,.52,5])
 # Functional doors and terminals are rendered once by their own blocks/entities.
 for x in [-15,15]:
  for z in [-18,8,24]:s.lights.append([x,22,z,.65,.83,1.0,28])
 s.lights += [[0,29,-14,1,.89,.59,24],[-23,5,2,.12,.5,1,9]]
 s.save([
  {'id':'hall-frontal','eye':[0,5,27],'target':[0,15,-17],'fov':68},
  {'id':'hall-lateral','eye':[13,7,17],'target':[-1,15,-15],'fov':72},
  {'id':'hall-relogio','eye':[3,18,2],'target':[0,23,-18],'fov':59},
  {'id':'portal-relogio','eye':[2.5,19.2,-9],'target':[0,19.6,-21],'fov':66}],
  [{'id':'hall-geral','eye':[5,6,24],'eyeEnd':[3,8,19],'target':[0,16,-17],'targetEnd':[0,17,-18],'fov':68,'frames':48},
   {'id':'hall-aproximacao','eye':[3,18.9,-3],'eyeEnd':[0,18.9,-16.8],'target':[0,20.1,-22],'targetEnd':[0,19.2,-22],'fov':65,'frames':60},
   {'id':'hall-portal','eye':[0,18.85,-16.8],'eyeEnd':[0,18.85,-20.7],'target':[0,18.9,-23],'targetEnd':[0,18.9,-23],'fov':65,'frames':24}])

def dungeon():
 s=Scene('thebel');p=json.loads((ROOT/'work/tartarus-appearance-audit/floor-plan.json').read_text());tiles={(v['x'],v['z']) for v in p['walkable']};materials={(c['x'],c['y'],c['z']):c['material'] for c in p['cells']}
 # Construct the visible surface of the SAME production plan, not a new level layout.
 for x,z in sorted(tiles):
  mat=materials.get((x,0,z),'tile');c=(.095,.13,.17) if (x+z)%2 else (.17,.22,.28)
  if mat=='border':c=PALE
  if mat in ('window','seal'):c=(.08,.15,.16)
  s.quad([(x,1,z),(x,1,z+1),(x+1,1,z+1),(x+1,1,z)],c,1,normal=(0,1,0))
  # Dark ceiling with surface pattern; arches provide the visible silhouette below it.
  s.quad([(x,13,z),(x+1,13,z),(x+1,13,z+1),(x,13,z+1)],STONE,2,normal=(0,-1,0))
  for dx,dz in [(1,0),(-1,0),(0,1),(0,-1)]:
   if (x+dx,z+dz) in tiles:continue
   # A modular wall face along the original boundary, no outward change of rooms.
   if dx:origin=np.array((x+(.998 if dx>0 else .002),0,z));u=np.array((0,0,1));normal=(-dx,0,0)
   else:origin=np.array((x,0,z+(.998 if dz>0 else .002)));u=np.array((1,0,0));normal=(0,0,-dz)
   def q(a,b,c,d,col,mat=0,emit=0,offset=0):
    off=np.array(normal)*offset;ps=[origin+u*a+(0,b,0)+off,origin+u*c+(0,b,0)+off,origin+u*c+(0,d,0)+off,origin+u*a+(0,d,0)+off];s.quad(ps,col,mat,emit,normal)
   q(0,1,1,13,STONE,2)
   for y in [1.15,2.55,11.95]:q(0,y,1,y+.16,(.17,.24,.29),3,offset=.025)
   coord=z if dx else x
   # Three-block-wide framed windows separated by pilasters.
   if coord%6 in [1,2,3]:
    q(.06,4.5,.94,10.3,DARK,0,offset=.05)
    for row in range(8):
     for col in range(2):q(.12+col*.4,4.65+row*.69,.44+col*.4,5.20+row*.69,GLASS,5,.50,offset=.075)
   if coord%6==0:
    q(.29,1,.71,12.5,(.20,.27,.34),1,offset=.18)
    q(.18,2.5,.82,2.85,PALE,3,offset=.21);q(.18,10.6,.82,10.9,PALE,3,offset=.21)
 # Columns, patterned seal, vaulted ribs: each room uses existing centre/radius.
 centers=[(x*24-36,z*24-36) for z in range(4) for x in range(4)]
 for cx,cz in centers:
  # Recover existing corner column positions rather than invent different footprints.
  corner=[c for c in p['cells'] if c['y']==1 and c['material']=='column' and abs(c['x']-cx)<12 and abs(c['z']-cz)<12]
  for c in corner:s.pillar(c['x']+.5,c['z']+.5,11,.48,white=False)
  radius=max([abs(c['x']-cx)+1 for c in corner],default=9)
  if (cx,cz)!=(p['stairs']['x'],p['stairs']['z']):
   s.ring((cx+.5,1.035,cz+.5),3.9,.09,PALE,'xz',3);s.ring((cx+.5,1.04,cz+.5),3.6,.20,(.065,.48,.23),'xz',5,.20)
   for i in range(16):
    a=i*PI/8;r=3.12;xx=cx+.5+math.cos(a)*r;zz=cz+.5+math.sin(a)*r;s.quad([(xx-.20,1.04,zz),(xx,1.04,zz+.2),(xx+.2,1.04,zz),(xx,1.04,zz-.2)],PALE,3,normal=(0,1,0))
  # Actual curved ceiling surface below the old flat cap; no low geometry on the path.
  for k in range(24):
   t0=-1+k/12;t1=-1+(k+1)/12
   x0=cx+.5+t0*(radius-.55);x1=cx+.5+t1*(radius-.55)
   y0=8.98+3.5*(1-abs(t0)**1.45);y1=8.98+3.5*(1-abs(t1)**1.45)
   s.quad([(x0,y0,cz-radius+.55),(x1,y1,cz-radius+.55),(x1,y1,cz+radius+.45),(x0,y0,cz+radius+.45)],(.115,.155,.24),2,normal=(y1-y0,-(x1-x0),0))
  # Crossed pointed vault ribs, leaving the bottom eight blocks clear.
  for direction in [0,1]:
   for off in [-radius+1,0,radius-1]:
    points=[]
    for j in range(17):
     t=-1+j/8;xx=t*(radius-.6);yy=8.8+3.55*(1-abs(t)**1.45)
     points.append((cx+.5+xx,yy,cz+.5+off) if direction==0 else (cx+.5+off,yy,cz+.5+xx))
    for a,b in zip(points,points[1:]):s.beam(a,b,.115,(.26,.35,.43),1,4)
  if len(s.lights)<24:s.lamp(cx+.5,9.2,cz+.5)
 # Rebuild actual stair and portal anchor blocks with the new finish.
 for c in p['cells']:
  x,y,z=c['x'],c['y'],c['z'];m=c['material']
  if m in ('stairs','forward','back'):
   col=(.12,.23,.31) if m=='stairs' else (.006,.021,.018)
   s.box(x,y,z,1,1,1,col,0)
 # Corridor arch modules are placed only at existing narrow passages.
 for x,z in sorted(tiles):
  if x%6!=0 or z%6!=0:continue
  alongX=all((x,z+k) in tiles for k in [-2,-1,0,1,2]) and (x,z+3) not in tiles and (x,z-3) not in tiles
  alongZ=all((x+k,z) in tiles for k in [-2,-1,0,1,2]) and (x+3,z) not in tiles and (x-3,z) not in tiles
  if not (alongX or alongZ):continue
  points=[]
  for j in range(13):
   t=-1+j/6;side=t*2.47;y=8.5+3.3*(1-abs(t)**1.4)
   points.append((x+.5,y,z+.5+side) if alongX else (x+.5+side,y,z+.5))
  for a,b in zip(points,points[1:]):s.beam(a,b,.12,(.24,.33,.40),1,4)
  s.lamp(x+.5,9.1,z+.5)
 # Local window bounce lights for the sampled route (preview lighting, not shaderpack claims).
 s.lights=s.lights[:24]+[[-41,7,-40,.17,1,.36,8],[-29,7,-40,.17,1,.36,8],[-18,7,-41,.17,1,.36,8],[-8,7,-30,.17,1,.36,8]]
 s.save([
  {'id':'andar-sala','eye':[-40,3.8,-30],'target':[-33,5,-42],'fov':78},
  {'id':'andar-corredor','eye':[-30,3.2,-35.5],'target':[-11,6,-35.5],'fov':74},
  {'id':'andar-abobada','eye':[-14,4,-30],'target':[-10,8,-37],'fov':80}])
 # Proof: the prototype reads the generator export, and source resources stay untouched.
 (WORK/'plan-contract.json').write_text(json.dumps({'seed':781241,'floor':7,'walkable':len(tiles),'entry':p['entry'],'stairs':p['stairs'],'spawns':p['spawns'],'inputSha256':hashlib.sha256((ROOT/'work/tartarus-appearance-audit/floor-plan.json').read_bytes()).hexdigest()},indent=2))
def rect(x0,z0,x1,z1):return {(x,z) for x in range(x0,x1+1) for z in range(z0,z1+1)}

def gallery(name,tiles,views,shots,kind='room',pillars=(),stairs=False,ports=()):
 """A visual template with fixed connection anchors, separate from the live generator."""
 s=Scene(name);height=14 if kind=='room' else 12
 for x,z in sorted(tiles):
  near=any((x+dx,z+dz) not in tiles for dx,dz in [(1,0),(-1,0),(0,1),(0,-1)])
  c=PALE if near else ((.10,.14,.19) if (x+z)%2 else (.19,.25,.30))
  s.quad([(x,1,z),(x,1,z+1),(x+1,1,z+1),(x+1,1,z)],c,1,normal=(0,1,0))
  s.quad([(x,height,z),(x+1,height,z),(x+1,height,z+1),(x,height,z+1)],STONE,2,normal=(0,-1,0))
  # Border is a path motif, not a luminous wall-height strip.
  if near:
   s.quad([(x+.15,1.022,z+.5),(x+.5,1.022,z+.80),(x+.85,1.022,z+.5),(x+.5,1.022,z+.20)],STONE,1,normal=(0,1,0))
  for dx,dz in [(1,0),(-1,0),(0,1),(0,-1)]:
   if (x+dx,z+dz) in tiles:continue
   # End ports are open in the catalog, allowing the same cardinal graph links.
   if ('N' in ports and z==min(v[1] for v in tiles) and dz==-1 and abs(x)<=2) or ('S' in ports and z==max(v[1] for v in tiles) and dz==1 and abs(x)<=2) or ('E' in ports and x==max(v[0] for v in tiles) and dx==1 and abs(z)<=2) or ('W' in ports and x==min(v[0] for v in tiles) and dx==-1 and abs(z)<=2):
    low=8.4
   else:low=1
   if dx:origin=np.array((x+(.998 if dx>0 else .002),0,z));u=np.array((0,0,1));normal=(-dx,0,0)
   else:origin=np.array((x,0,z+(.998 if dz>0 else .002)));u=np.array((1,0,0));normal=(0,0,-dz)
   def q(a,b,c,d,color,mat=0,em=0,offset=0):
    off=np.array(normal)*offset
    ps=[origin+u*a+(0,b,0)+off,origin+u*c+(0,b,0)+off,origin+u*c+(0,d,0)+off,origin+u*a+(0,d,0)+off];s.quad(ps,color,mat,em,normal)
   q(0,low,1,height,STONE,2)
   for y in [1.25,3.0,height-1]:
    if y>=low:q(0,y,1,y+.12,(.16,.22,.28),3,offset=.04)
   coord=z if dx else x
   if coord%7 in [1,2,3] and low==1:
    q(.04,5.0,.96,11.2,DARK,0,offset=.045)
    for row in range(8):
     for col in range(2):q(.12+col*.4,5.12+row*.72,.44+col*.4,5.73+row*.72,GLASS,5,.43,offset=.065)
   if coord%7==0 and low==1:
    q(.15,1,.85,height-.4,(.18,.25,.34),1,offset=.18)
    for y in [2.8,height-2]:q(.02,y,.98,y+.3,(.27,.36,.42),1,offset=.22)
 # Large smooth pillars create an actual route around them as seen at 01:10.
 for x,z,r in pillars:
  s.beam((x,1.4,z),(x,height,z),r,(.17,.23,.32),2,32)
  for y,rr in [(1,r+.15),(1.5,r+.06),(height-1,r+.11)]:
   s.beam((x,y,z),(x,y+.22,z),rr,(.11,.17,.24),3,32)
 # Pointed barrel vault for the long room; corridor ribs follow each lane's direction.
 if name=='sala-longa':
  for k in range(24):
   a=-1+k/12;b=-1+(k+1)/12;xx0=.5+a*6.5;xx1=.5+b*6.5;y0=9.3+4.2*(1-abs(a)**1.5);y1=9.3+4.2*(1-abs(b)**1.5)
   s.quad([(xx0,y0,-11.95),(xx1,y1,-11.95),(xx1,y1,12.95),(xx0,y0,12.95)],(.115,.16,.25),2,normal=(y1-y0,xx0-xx1,0))
  for z in [-10,-4,2,8]:
   points=[(.5+t*6.5,9.12+4.2*(1-abs(t)**1.5),z) for t in np.linspace(-1,1,25)]
   for a,b in zip(points,points[1:]):s.beam(a,b,.10,(.30,.38,.47),1,4)
 elif kind=='corridor':
  for x,z in sorted(tiles):
   if (x,z) not in {(0,-9),(0,-3),(0,3),(0,9),(-9,0),(-3,0),(3,0),(9,0)}:continue
   alongZ=all((x+k,z) in tiles for k in [-2,-1,0,1,2]) and (x+3,z) not in tiles and (x-3,z) not in tiles
   alongX=all((x,z+k) in tiles for k in [-2,-1,0,1,2]) and (x,z+3) not in tiles and (x,z-3) not in tiles
   if not(alongZ or alongX):continue
   points=[(x+.5+t*2.47,8.0+3.55*(1-abs(t)**1.4),z+.5) if alongZ else (x+.5,8.0+3.55*(1-abs(t)**1.4),z+.5+t*2.47) for t in np.linspace(-1,1,17)]
   for a,b in zip(points,points[1:]):s.beam(a,b,.105,(.29,.38,.46),1,4)
 # Lateral platforms and short half-step stairways, leaving the centre route open.
 if stairs:
  for side in [-1,1]:
   x=side*7.2-1.7
   s.box(x,1,-7,3.4,3,10,STONE,2)
   s.box(x,4.02,-7,3.4,.05,10,(.18,.27,.33),1)
   for i in range(6):
    z=3+i*.6;top=4-i*.5
    s.box(x,1,z,3.4,top-1,.6,STONE,2);s.box(x,top+.02,z,3.4,.06,.6,(.24,.34,.39),1)
   for railX in [x+.15,x+3.25]:
    for z in np.arange(-6,3.2,1.3):s.beam((railX,4.1,z),(railX,5.2,z),.065,(.08,.32,.22),3,6)
    s.beam((railX,5.2,-6.5),(railX,5.2,3),.10,(.12,.33,.26),3)
    s.beam((railX,5.2,3),(railX,2.25,6.5),.10,(.12,.33,.26),3)
    for i in range(5):
     z=3.3+i*.6;y=5.2-(z-3)*.84;s.beam((railX,y-1,z),(railX,y,z),.065,(.08,.32,.22),3,6)
 # A seal is a local focal point, not stamped at every junction.
 if kind=='room':
  cx,cz,r=(0,0,1.4) if pillars else (.5,.5,2.8)
  s.ring((cx,1.035,cz),r,.13,(.1,.49,.23),'xz',5,.20);s.ring((cx,1.033,cz),r+.2,.07,PALE,'xz')
 lamps=[(-4,8.9,0),(4,8.9,0),(0,10,-7),(0,10,7)] if kind=='room' else [(x,8.5,z) for x,z in [(0,-7),(0,0),(7,0),(-7,0)] if (x,z) in tiles]
 for x,y,z in lamps:s.lamp(x,y,z)
 s.lights += [[-5,7,-7,.19,.9,.40,9],[5,7,7,.19,.9,.40,9]]
 s.save(views,shots)
 obstacles={(x,z) for x,z in tiles if any((x+.5-cx)**2+(z+.5-cz)**2<(r+.35)**2 for cx,cz,r in pillars)}
 return {'id':name,'tiles':[list(p) for p in sorted(tiles)],'blocked':[list(p) for p in sorted(obstacles)],'ports':list(ports),'reference':{'sala-longa':'01:20 / galeria com abóbada','sala-pilares':'01:10 / dois grandes pilares e percurso lateral','sala-patamares':'06:45–07:00 / escadas e plataformas laterais','corredor-l':'01:10–01:20 / curva visível no minimapa','corredor-t':'00:50–00:55 / ramificação no minimapa','cruzamento':'Módulo complementar proposto para quatro conexões; não cópia 1:1 do vídeo'}[name]}

def variants():
 specs=[
  ('sala-longa',rect(-6,-12,6,12),[{'id':'sala-longa','eye':[3.5,3.1,10],'target':[0,5,-8],'fov':80}],[{'id':'galeria','eye':[3.5,3.1,10],'eyeEnd':[3,3.1,6],'target':[0,5,-8],'targetEnd':[0,5,-8],'fov':80,'frames':42}], 'room',(),False,('N','S')),
  ('sala-pilares',rect(-10,-10,10,10),[{'id':'sala-pilares','eye':[7,3.4,8],'target':[-1,5,-2],'fov':83}],[{'id':'pilares','eye':[7,3.4,8],'eyeEnd':[7,3.4,1.5],'target':[-1,5,-2],'targetEnd':[-1,5,-5],'fov':83,'frames':48}], 'room',((0,-4,2.0),(0,4,2.0)),False,('N','S','E','W')),
  ('sala-patamares',rect(-10,-9,10,9)|rect(-2,-12,2,-10),[{'id':'sala-patamares','eye':[1,3.3,8],'target':[-5,4,-3],'fov':90}],[{'id':'patamares','eye':[1,3.3,8],'eyeEnd':[2,4,3],'target':[-5,4,-3],'targetEnd':[-4,4.2,-6],'fov':90,'frames':48}], 'room',(),True,('N','S')),
  ('corredor-l',rect(-2,-12,2,2)|rect(3,-2,12,2),[{'id':'corredor-l','eye':[0,3.0,-6.5],'target':[4,4,0],'fov':92}],[{'id':'curva','eye':[0,3,-6.5],'eyeEnd':[1,3,0],'target':[4,4,0],'targetEnd':[10,4,0],'fov':92,'frames':48}], 'corridor',(),False,('N','E')),
  ('corredor-t',rect(-12,-2,12,2)|rect(-2,3,2,12),[{'id':'corredor-t','eye':[0,3.1,7],'target':[0,4.5,0],'fov':94}],[{'id':'ramificacao','eye':[0,3.1,7],'eyeEnd':[0,3.1,3.7],'target':[0,4.5,0],'targetEnd':[-4,4,0],'fov':94,'frames':42}], 'corridor',(),False,('W','E','S')),
  ('cruzamento',rect(-12,-2,12,2)|rect(-2,-12,2,12),[{'id':'cruzamento','eye':[0,3.2,4.5],'target':[0,4,-3],'fov':96}],[{'id':'cruzamento','eye':[0,3.2,4.5],'eyeEnd':[0,3.2,1.5],'target':[0,4,-3],'targetEnd':[-4,4,-4],'fov':96,'frames':36}], 'corridor',(),False,('N','S','E','W'))]
 catalog=[]
 for name,tiles,views,shots,kind,pillars,stairs,ports in specs:catalog.append(gallery(name,tiles,views,shots,kind,pillars,stairs,ports))
 (WORK/'catalog.json').write_text(json.dumps(catalog,ensure_ascii=False),encoding='utf8')
 # Connectivity of the template ports is the contract required by the current room graph.
 for item in catalog:
  floor={tuple(p) for p in item['tiles']};walk=floor-{tuple(p) for p in item['blocked']};start=next(iter(walk));seen={start};queue=[start]
  for x,z in queue:
   for dx,dz in [(1,0),(-1,0),(0,1),(0,-1)]:
    q=(x+dx,z+dz)
    if q in walk and q not in seen:seen.add(q);queue.append(q)
  assert seen==walk,item['id']
 print('PASS six template footprints connected, including circulation around large pillars. Proposal only.')

if __name__=='__main__':hall();variants()
