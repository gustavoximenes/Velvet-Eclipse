"""Special-floor and staircase appearance proposal. Reads the real plans; no mod writes."""
from scene_base import *

CONTRACTS=[]
def plan(f):return json.loads((WORK/'plans'/f'floor-{f}.json').read_text())
def transformed(dest,source,x,z,angle=0):
 c=math.cos(angle);a=math.sin(angle)
 for vertex in source.verts:
  px,py,pz,nx,ny,nz,*rest=vertex
  dest.verts.append([x+c*px+a*pz,py,z-a*px+c*pz,c*nx+a*nz,ny,-a*nx+c*nz,*rest])
 for lx,ly,lz,r,g,b,power in source.lights:dest.lights.append([x+c*lx+a*lz,ly,z-a*lx+c*lz,r,g,b,power])

def portal(s,x,z,base=1,width=5,height=4,kind='forward',angle=0):
 t=Scene('portal-local');metal=GOLD if kind=='monad' else (.29,.40,.45)
 for xx in [-width/2-.22,width/2]:
  t.box(xx,base,-.3,.22,height,.55,metal,3)
  t.box(xx+.055,base+.08,.26,.10,height-.13,.03,(.06,.44,.30) if kind=='forward' else (.09,.23,.48),5,.12)
 t.box(-width/2-.22,base+height,width*.002,width+.44,.23,.6,metal,3)
 t.box(-width/2,base-.04,-.4,width,.08,.85,(.29,.39,.39),1)
 col=(.019,.16,.11) if kind=='forward' else ((.035,.08,.22) if kind in ('back','velvet') else (.08,.075,.16))
 t.quad([(-width/2,base,0),(width/2,base,0),(width/2,base+height,0),(-width/2,base+height,0)],col,8 if kind=='forward' else 9,.35,(0,0,1))
 if kind=='back':
  # The graph's block-wide opening can be wider/taller and one block behind the
  # frame. A closed masonry reveal joins both planes without exposing the void.
  top=8.5 if abs(angle)<.01 else 5.1
  for left,right in [(-3.5,-width/2),(width/2,3.5)]:
   t.box(left,base-.05,-1.7,right-left,top+.05,1.65,STONE,2)
  t.box(-3.5,base+height,-1.7,7,top-height,1.65,STONE,2)
  t.box(-3.5,base-.10,-1.7,7,.10,1.65,PALE,1)
 if kind in ('velvet','monad'):
  t.box(-.045,base,.025,.09,height,.05,metal,3)
  for xx in [-width*.26,width*.26]:
   t.ring((xx,base+height*.58,.06),width*.17,.055,metal,'xy',3)
   t.box(xx-.12,base+height*.4,.055,.09,.4,.07,GOLD,3)
 t.lights.append([0,base+height*.5,.8,.14,.68,.47,4])
 transformed(s,t,x,z,angle)

def shell(s,tiles,height=13,openings=(),white=False):
 # Same floor footprint as the production plan. Only its finish is replaced.
 for x,z in sorted(tiles):
  edge=any((x+dx,z+dz) not in tiles for dx,dz in [(1,0),(-1,0),(0,1),(0,-1)])
  if white:
   col=(.38,.47,.48) if (x//2+z//2)%2 else (.13,.21,.30)
  else:col=(.10,.14,.19) if (x+z)%2 else (.19,.25,.30)
  if edge:col=PALE
  s.quad([(x,1,z),(x,1,z+1),(x+1,1,z+1),(x+1,1,z)],col,1,normal=(0,1,0))
  s.quad([(x,height,z),(x+1,height,z),(x+1,height,z+1),(x,height,z+1)],STONE,2,normal=(0,-1,0))
  for dx,dz in [(1,0),(-1,0),(0,1),(0,-1)]:
   if (x+dx,z+dz) in tiles:continue
   low=1
   for ax,az,axis,width,top in openings:
    if (axis=='z' and dz and abs(z-az)<1.1 and abs(x-ax)<width/2) or (axis=='x' and dx and abs(x-ax)<1.1 and abs(z-az)<width/2):low=max(low,top)
   if dx:o=np.array((x+(1 if dx>0 else 0),0,z));u=np.array((0,0,1));normal=(-dx,0,0)
   else:o=np.array((x,0,z+(1 if dz>0 else 0)));u=np.array((1,0,0));normal=(0,0,-dz)
   def q(a,b,c,d,color,mat=0,em=0,offset=0):
    off=np.array(normal)*offset
    s.quad([o+u*a+(0,b,0)+off,o+u*c+(0,b,0)+off,o+u*c+(0,d,0)+off,o+u*a+(0,d,0)+off],color,mat,em,normal)
   q(0,low,1,height,STONE,2)
   for y in [1.25,3,height-1.0]:
    if y>=low:q(0,y,1,y+.12,PALE if white else (.18,.26,.32),3,offset=.035)
   coord=z if dx else x
   if coord%7 in [1,2,3] and low==1:
    top=height-2.0;bottom=5 if height<15 else 6.2
    q(.04,bottom,.96,top,DARK,0,offset=.045)
    rows=8 if height<15 else 11;step=(top-bottom-.2)/rows
    for row in range(rows):
     for column in range(2):q(.12+column*.4,bottom+.10+row*step,.44+column*.4,bottom+.10+row*step+step*.82,GLASS,5,.35,offset=.065)
   if coord%7==0 and low==1:
    q(.15,1,.85,height-.4,(.18,.25,.34),1,offset=.18)
    for y in [2.8,height-2]:q(.01,y,.99,y+.3,(.28,.36,.42),1,offset=.22)

def border(s,cx,cz,r,color=PALE):
 for side in [-1,1]:
  s.box(cx-r,1.019,cz+side*r-.10,2*r,.026,.2,color,1)
  s.box(cx+side*r-.10,1.019,cz-r,.2,.026,2*r,color,1)
 for x in [-r,r]:
  for z in [-r,r]:s.disk((cx+x,1.048,cz+z),.35,(.03,.14,.16),'xz',3)

def vault(s,cx,cz,r,depth,height=13,cross=False):
 # The highest point remains below the original ceiling.
 for k in range(24):
  a=-1+k/12;b=-1+(k+1)/12
  x0=cx+a*r;x1=cx+b*r;y0=height-4.2+3.95*(1-abs(a)**1.55);y1=height-4.2+3.95*(1-abs(b)**1.55)
  s.quad([(x0,y0,cz-depth),(x1,y1,cz-depth),(x1,y1,cz+depth),(x0,y0,cz+depth)],(.115,.16,.25),2,normal=(y1-y0,x0-x1,0))
 for offset in [-depth+.6,0,depth-.6]:
  points=[(cx+t*r,height-4.35+3.95*(1-abs(t)**1.55),cz+offset) for t in np.linspace(-1,1,25)]
  for a,b in zip(points,points[1:]):s.beam(a,b,.11,(.29,.38,.47),1,4)
 if cross:
  for sign in [-1,1]:
   points=[(cx+t*r,height-4.35+3.95*(1-abs(t)**1.55),cz+sign*t*depth) for t in np.linspace(-1,1,25)]
   for a,b in zip(points,points[1:]):s.beam(a,b,.14,(.30,.39,.44),1,4)

def seal(s,x,z,r=7.8,ornate=False):
 s.disk((x,1.021,z),r,(.04,.09,.13),'xz',0)
 s.ring((x,1.05,z),r,.12,PALE,'xz',3)
 s.ring((x,1.046,z),r-.32,.23,(.08,.44,.22),'xz',5,.15)
 s.ring((x,1.049,z),r*.52,.09,(.09,.38,.24),'xz',3)
 if ornate:
  for j in range(8):
   a=j*PI/4;points=[]
   for angle,rr in [(a,r*.83),(a+PI/8,r*.42),(a+PI/4,r*.83)]:points.append((x+math.sin(angle)*rr,1.052,z+math.cos(angle)*rr))
   for p,q in zip(points,points[1:]):s.beam(p,q,.045,(.28,.47,.36),3,4)
 for j in range(16):
  a=j*PI/8;xx=x+math.sin(a)*(r-.9);zz=z+math.cos(a)*(r-.9)
  s.quad([(xx-.13,1.055,zz),(xx,1.055,zz+.22),(xx+.13,1.055,zz),(xx,1.055,zz-.22)],PALE,3,normal=(0,1,0))

def staircase(s,cx,cz,width=7,portalZ=None):
 # 16 half-height risers span the same eight-block ascent and run.
 # Source coordinates: tread front cz+5, landing cz-3..cz-6, portal at cz-6.
 for i in range(16):
  front=cz+5-i*.5;top=1+(i+1)*.5
  s.box(cx-width/2,1,front-.5,width,top-1,.5,(.13,.19,.27),2)
  s.box(cx-width/2,top+.008,front-.5,width,.025,.48,(.27,.37,.40),1)
  s.box(cx-width/2+.16,top+.037,front-.035,width-.32,.018,.035,(.15,.39,.38),3)
 s.box(cx-width/2,1,cz-6,width,8,3,(.11,.18,.25),2)
 s.box(cx-width/2,9.01,cz-6,width,.03,3,(.25,.36,.40),1)
 for x in [cx-width/2-.23,cx+width/2+.23]:
  s.beam((x,2.2,cz+5),(x,10.2,cz-3),.09,(.18,.34,.35),3,6)
  s.beam((x,10.2,cz-3),(x,10.2,cz-6),.09,(.18,.34,.35),3,6)
  for i in range(0,17,2):
   z=cz+5-i*.5;y=1+i*.5
   s.beam((x,y,z),(x,y+1.2,z),.055,(.18,.30,.34),3,6)
  for z in [cz-4.5,cz-6]:s.beam((x,9,z),(x,10.2,z),.055,(.18,.30,.34),3,6)
  s.box(x-.17,1,cz+4.7,.34,1.4,.34,(.25,.34,.40),1)
  s.box(x-.11,2.4,cz+4.76,.22,.17,.22,CYAN,5,.24)
 portal(s,cx,cz-6 if portalZ is None else portalZ,9,4.8,3.65)
 s.lights.append([cx,10.3,cz-3,.25,.78,.69,6])
 return {'center':[cx,cz],'risers':16,'riserHeight':.5,'run':8,'rise':8,'baseY':1,'landingY':9,'landingZ':[cz-6,cz-3],'portalZ':cz-6 if portalZ is None else portalZ,'width':width}

def terminal(s,x,z):
 s.box(x-.7,1,z-.55,1.4,.2,1.1,(.2,.31,.34),3)
 s.box(x-.35,1.2,z-.28,.7,1.25,.56,(.12,.19,.27),3)
 s.box(x-.7,2.45,z-.24,1.4,.95,.16,(.06,.15,.19),3)
 s.box(x-.57,2.58,z-.055,1.14,.67,.025,(.10,.52,.55),5,.24)
 s.lights.append([x,3.2,z+1,.26,.73,.8,3])

def chest(s,x,z):
 s.box(x-.85,1,z-.55,1.7,1.0,1.1,(.05,.12,.22),1)
 s.box(x-.88,2,z-.58,1.76,.33,1.16,(.17,.26,.34),3)
 for xx in [x-.73,x+.6]:s.box(xx,1,z-.57,.13,1.34,1.14,GOLD,3)
 s.box(x-.16,1.48,z+.56,.32,.5,.045,GOLD,3)

def gate(s,z):
 # Same five-block opening and barrier plane as the actual guardian floor.
 for x in [-2.58,2.5]:s.box(x,1,z-.14,.08,11.8,.28,(.30,.42,.43),3)
 for x in np.arange(-2.2,2.3,.55):
  s.beam((x,1,z),(x,12.8,z),.04,(.12,.51,.28),3,4)
 for y in [1.5,4,7,10,12.6]:s.beam((-2.5,y,z),(2.5,y,z),.045,(.18,.57,.34),3,4)
 s.ring((0,4,z+.06),1.0,.09,(.13,.67,.30),'xy',5,.15)
 s.lights.append([0,5,z+1,.20,.68,.34,4])

def save(s,views,shots,center,key):
 s.save(views,shots)
 p=WORK/(s.name+'.json');meta=json.loads(p.read_text());meta.update(center=center,key=key)
 p.write_text(json.dumps(meta),encoding='utf8')

def guardian_floor(f):
 doc=plan(f);p=doc['plan'];shift=256;tiles={(v['x'],v['z']-shift) for v in p['walkable']}
 s=Scene(f'andar-{f}');shell(s,tiles,13,[(0,46,'z',5,5)])
 for cz,r in [(36,9),(0,14),(-36,10)]:
  border(s,0,cz,r-1)
  for x in [-r+1,r-1]:
   for z in [cz-r+1,cz+r-1]:s.pillar(x,z,11,.55,white=False)
  # Stair chamber keeps the original overhead clearance instead of placing a vault over its portal.
  if cz!=-36:vault(s,.5,cz+.5,r-.20,r-.10,13,cross=(f==20 and cz==0))
 for z in [-24,-19,-14,17,23,28]:
  for x in [-2.42,2.42]:s.beam((x,1,z),(x,8.3,z),.12,(.22,.30,.39),1,6)
  pts=[(.5+t*2.46,8.2+4.25*(1-abs(t)**1.5),z) for t in np.linspace(-1,1,19)]
  for a,b in zip(pts,pts[1:]):s.beam(a,b,.12,(.28,.39,.46),1,4)
 seal(s,0,0,8.1,f==20)
 if f==20:
  for x in [-13,13]:
   for z in [-7,0,7]:
    s.pillar(x,z,11.2,.56,white=False)
    s.beam((x,8.5,z),(x*.83,11.4,z),.14,(.26,.35,.42),1,4)
 for x,z in [(-9,-7),(9,-7),(-9,7),(9,7),(0,36),(0,-30),(0,20)]:s.lamp(x,8.6,z)
 s.lights += [[-12,7,0,.18,.7,.37,7],[12,7,0,.18,.7,.37,7]]
 gate(s,-19)
 stairs=staircase(s,p['stairs']['x'],p['stairs']['z']-shift,7)
 portal(s,0,46,1,5,4,'back',PI)
 portal(s,-8,36,1,3.3,4.2,'velvet',PI/2)
 terminal(s,6,35)
 views=[{'id':f'andar-{f}-arena','eye':[-9 if f==10 else 10,3.7,11],'target':[0,5,-5],'fov':82},
        {'id':f'andar-{f}-escada','eye':[5.5,4.2,-28],'target':[0,7.5,-37],'fov':78}]
 if f==10:views.append({'id':'andar-10-preparacao','eye':[4,3.2,43],'target':[-3,3.8,34],'fov':87})
 shots=[{'id':'arena','eye':views[0]['eye'],'eyeEnd':[-5 if f==10 else 5,3.7,8],'target':[0,5,-5],'targetEnd':[0,5,-7],'fov':82,'frames':48}]
 if f==10:shots.append({'id':'chegada','eye':[1,2.8,43.5],'eyeEnd':[1,2.8,40],'target':[-2,3.5,34],'targetEnd':[-2,3.5,31],'fov':82,'frames':36})
 save(s,views,shots,[0,2,0],[3,26,20])
 CONTRACTS.append({'floor':f,'kind':'guardian','sourceSha256':hashlib.sha256((WORK/'plans'/f'floor-{f}.json').read_bytes()).hexdigest(),'sourceEntry':p['entry'],'sourceStairs':p['stairs'],'guardian':[0,256],'gate':[0,237],'gateRule':'opens only after guardian defeat','renderZOffset':-256,'walkable':len(tiles),'stairs':stairs,'props':doc['props']})

def transition_floor():
 doc=plan(25);p=doc['plan'];shift=256;tiles={(v['x'],v['z']-shift) for v in p['walkable']}
 s=Scene('andar-25');shell(s,tiles,18,[(0,24,'z',5,5),(-22,12,'x',4,6),(22,0,'x',5,8)],True)
 for x in [-18,18]:
  for z in [-19,0,19]:s.pillar(x,z,15,1.0,white=False)
 # Broad quiet room: restrained gold borders, open centre, no enemy or guardian barrier.
 for x in [-20,20]:s.box(x,1.03,-22,.12,.025,44,GOLD,3)
 for z in [-22,22]:s.box(-20,1.031,z,40,.025,.12,GOLD,3)
 seal(s,0,0,6.2,True)
 for z in [-18,0,18]:
  pts=[(.5+t*20.5,13.3+4.35*(1-abs(t)**1.6),z) for t in np.linspace(-1,1,33)]
  for a,b in zip(pts,pts[1:]):s.beam(a,b,.14,(.32,.43,.46),1,4)
 for x in [-11,11]:
  for z in [-13,2,17]:s.lamp(x,12.0,z)
 stairs=staircase(s,0,-14,9,portalZ=-20)
 portal(s,0,24,1,5,4,'back',PI)
 portal(s,-21,12,1,3.5,4.8,'velvet',PI/2)
 portal(s,21,0,1,5.4,6.5,'monad',-PI/2)
 for x in [-6,0,6]:chest(s,x,8)
 terminal(s,15,12)
 s.lights += [[0,12,-14,.28,.70,.7,8],[-19,7,12,.2,.35,.75,5],[19,8,0,.36,.48,.55,6]]
 views=[{'id':'andar-25-geral','eye':[12,5.2,20],'target':[-1,6,-5],'fov':82},
        {'id':'andar-25-servicos','eye':[-10,4.0,14],'target':[12,4,4],'fov':92},
        {'id':'andar-25-escada','eye':[6,4.5,-6],'target':[0,8.2,-17],'fov':78}]
 shots=[{'id':'transicao','eye':[12,5.2,20],'eyeEnd':[7,5.0,17],'target':[-1,6,-5],'targetEnd':[-1,6,-7],'fov':82,'frames':48}]
 save(s,views,shots,[0,3,0],[2,30,18])
 CONTRACTS.append({'floor':25,'kind':'safe transition','sourceSha256':hashlib.sha256((WORK/'plans'/'floor-25.json').read_bytes()).hexdigest(),'sourceEntry':p['entry'],'sourceStairs':p['stairs'],'renderZOffset':-256,'walkable':len(tiles),'stairs':stairs,'props':doc['props'],'velvetDoor':[-21,268],'backPortal':[0,280],'monadDoor':[21,256],'forwardDestination':26})

def common_stair_room():
 doc=plan(7);p=doc['plan'];cx=p['stairs']['x'];cz=p['stairs']['z'];alltiles={(v['x']-cx,v['z']-cz) for v in p['walkable']}
 # Show the existing stair module at the origin; other rooms remain outside this detail view.
 tiles={(x,z) for x,z in alltiles if -10<=x<=10 and -10<=z<=10}
 s=Scene('escada-comum');shell(s,tiles,13,[(0,10,'z',5,8)])
 border(s,.5,.5,9)
 for x in [-9,9]:
  for z in [-9,9]:s.pillar(x,z,11,.6,white=False)
 stair=staircase(s,0,0,7)
 for x in [-7,7]:s.lamp(x,8.5,0)
 views=[{'id':'escada-comum','eye':[6.5,3.3,9],'target':[0,6.2,-1],'fov':78},
        {'id':'escada-patamar','eye':[2.4,10.5,-2.6],'target':[0,10.8,-6.4],'fov':75}]
 # Camera profile follows the actual half-step rise and ends before the portal plane.
 shots=[{'id':'subida','eye':[1,2.8,7.5],'eyeEnd':[1,10.7,-5.3],'target':[0,7,-5.9],'targetEnd':[0,11,-6.7],'fov':72,'frames':84,'stairCamera':True}]
 save(s,views,shots,[0,2,0],[2,19,10])
 CONTRACTS.append({'floor':7,'kind':'common stair detail','sourceStairs':p['stairs'],'displayTranslation':[-cx,-cz],'stairs':stair,'route':'forward = current floor + 1; back = current floor - 1','sourceSha256':hashlib.sha256((WORK/'plans'/'floor-7.json').read_bytes()).hexdigest()})

if __name__=='__main__':
 guardian_floor(10);guardian_floor(20);transition_floor();common_stair_room()
 (WORK/'contract.json').write_text(json.dumps(CONTRACTS,ensure_ascii=False,indent=2),encoding='utf8')
 print('PASS proposal built from unchanged production plans; special-floor anchors retained.')
