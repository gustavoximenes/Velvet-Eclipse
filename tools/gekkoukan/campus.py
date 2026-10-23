"""Assemble the approved room studies into one walkable three-storey campus.

All positions are Minecraft blocks, +Z faces the city. Production origin (0,64,-110).
Meshes and collisions share the same authoring primitives. Trees remain native blocks.
"""
from pathlib import Path
import gzip,json,math
import numpy as np
from scene import Scene
HERE=Path(__file__).parent
STONE=(.78,.81,.73);TRIM=(.92,.91,.83);FLOOR=(.65,.73,.68);METAL=(.25,.34,.34);WOOD=(.58,.40,.26)

def transform(v,angle,offset):
 a=math.radians(angle);c,s=math.cos(a),math.sin(a)
 m=np.array([[c,0,-s],[0,1,0],[s,0,c]])
 return np.asarray(v)@m.T+offset

def build():
 s=Scene('school');rooms=[];seats=[];doors=[];b=s.box
 def box(x,y,z,w,h,d,col=STONE,mat=31,emit=0):b(x,y,z,w,h,d,col,mat,emit)
 def pane(x,y,z,w,h,d):box(x,y,z,w,h,d,(.52,.73,.75),18)
 def window_wall(start,end,y,fixed,axis,opening=None):
  # A continuous envelope: pier, sill, glass and lintel fill every wall bay.
  # Door bays omit only the lower 3.5 m, never the whole wall above a doorway.
  cuts=sorted(set([start,end,*np.arange(start,end,4).tolist(),*(opening or [])]))
  cuts=[v for v in cuts if start<=v<=end]
  def part(a,b,yy,h,glass=False):
   if b<=a:return
   if axis=='x':args=(a,yy,fixed,b-a,h,.24)
   else:args=(fixed,yy,a,.24,h,b-a)
   (pane if glass else box)(*args)
   if glass:
    if axis=='x':
     box(a,yy,fixed-.025,b-a,.055,.29,METAL,34);box(a,yy+h-.055,fixed-.025,b-a,.055,.29,METAL,34)
     box(b-.055,yy,fixed-.025,.055,h,.29,METAL,34)
    else:
     box(fixed-.025,yy,a,.29,.055,b-a,METAL,34);box(fixed-.025,yy+h-.055,a,.29,.055,b-a,METAL,34)
     box(fixed-.025,yy,b-.055,.29,h,.055,METAL,34)
  for a,c in zip(cuts,cuts[1:]):
   if opening and a>=opening[0] and c<=opening[1]:part(a,c,y+3.5,2.25);continue
   pier=min(.25,(c-a)*.25);part(a,a+pier,y,5.75)
   part(a+pier,c,y,1);part(a+pier,c,y+4.8,.95)
   part(a+pier,c,y+1,3.8,True)
 def rail(x,y,z,w,d):
  box(x,y,z,w,.12,d,METAL,34)
  along=w>d
  for a in np.arange(0,w if along else d,1.4):box(x+a if along else x,y-1,z if along else z+a,.08,1,.08,METAL,34)
 def room(name,x,y,z,angle=0):
  meta=json.loads(gzip.decompress((HERE/'approved'/f'{name}.json.gz').read_bytes()))
  raw=np.frombuffer(gzip.decompress((HERE/'approved'/f'{name}.bin.gz').read_bytes()),'<f4').reshape(-1,11).copy()
  raw[:36,1]+=.018
  meta['solids'][0]['lo'][1]+=.018;meta['solids'][0]['hi'][1]+=.018
  glass=(raw[:,9]==0)&(np.max(abs(raw[:,6:9]-[.49,.68,.70]),axis=1)<.005)
  # Wood in the study used material 18; production reserves 18 for actual glass.
  for old,new in [(18,35),(4,33),(2,35),(0,32)]:raw[raw[:,9]==old,9]=new
  raw[glass,9]=18
  raw[:,:3]=transform(raw[:,:3],angle,[x,y,z]);raw[:,3:6]=transform(raw[:,3:6],angle,[0,0,0]);s.verts.extend(raw.tolist())
  for item in meta['solids']:
   lo,hi=item['lo'],item['hi'];points=transform([[xx,yy,zz] for xx in [lo[0],hi[0]] for yy in [lo[1],hi[1]] for zz in [lo[2],hi[2]]],angle,[x,y,z])
   s.solids.append(dict(lo=points.min(0).tolist(),hi=points.max(0).tolist()))
  d=meta['door'];entry=transform([(d[0]+d[3])/2,0,d[2]-.8],angle,[x,y,z]).tolist()
  rooms.append(dict(id=name,origin=[x,y,z],angle=angle,entry=entry,size=meta['roomSize']))
  doors.append(dict(id='school_'+name,lo=d[:3],hi=d[3:],angle=angle,offset=[x,y,z]))
  for i,p in enumerate(meta['props']):
   if p['kind'] not in ('assento','banco'):continue
   q=p['bounds'];center=transform([(q[0]+q[3])/2,p['seatTop']-.40,(q[2]+q[5])/2],angle,[x,y,z])
   points=transform([[xx,yy,zz] for xx in [q[0],q[3]] for yy in [0,q[4]] for zz in [q[2],q[5]]],angle,[x,y,z])
   seats.append(dict(id=f'school_{name}_{i}',point=center.tolist(),yaw=p['seatYaw']+angle,bounds=[*points.min(0),*points.max(0)]))
  return meta
 def classroom(id,x,y,z):
  # Same school furniture language, real doorway and four clear aisles.
  w,d=14,15
  box(x-7,y-.182,z-7.5,14,.2,15,FLOOR,33)
  box(x-7,y,z-7.5,14,4.5,.2,STONE,32);box(x-7,y,z-7.5,.2,4.5,15,STONE,32);box(x+6.8,y,z-7.5,.2,4.5,15,STONE,32)
  for xx,ww in [(x-7,10.5),(x+5.5,1.5)]:box(xx,y,z+7.3,ww,4.5,.2,STONE,32)
  box(x+3.5,y+2.8,z+7.3,2,1.7,.2,STONE,32);box(x-7,y+4.5,z-7.5,14,.18,15,TRIM,32)
  box(x-5,y+1.15,z-7.24,8.8,2,.12,(.16,.32,.28),32)
  for xx in [-4.8,-1.9,1,3.9]:
   for zz in [-3,-.4,2.2]:
    box(x+xx,y+1,z+zz,1.2,.1,.8,WOOD,35)
    for dx in [.06,1.08]:
     for dz in [.06,.68]:box(x+xx+dx,y,z+zz+dz,.06,1,.06,METAL,34)
    cx=x+xx+.6;cz=z+zz+1.45
    box(cx-.31,y+.53,cz-.3,.62,.1,.6,WOOD,35);box(cx-.31,y+.65,cz+.24,.62,.62,.08,WOOD,35)
    for dx in [-.26,.20]:
     for dz in [-.25,.19]:box(cx+dx,y,cz+dz,.06,.53,.06,METAL,34)
    for dx in [-.26,.20]:box(cx+dx,y+.53,cz+.24,.06,.23,.08,METAL,34)
    seats.append(dict(id=f'school_{id}_{xx}_{zz}',point=[cx,y+.23,cz],yaw=180,bounds=[cx-.32,y,cz-.34,cx+.32,y+1.3,cz+.34]))
  for zz in [-4,1,5]:box(x-4,y+4.42,z+zz,8,.08,.22,(.97,.97,.87),32,.4)
  doors.append(dict(id='school_'+id,lo=[3.5,0,7.3],hi=[5.5,2.8,7.5],angle=0,offset=[x,y,z]))
  rooms.append(dict(id=id,origin=[x,y,z],angle=0,entry=[x+4.5,y,z+6.5],size=[14,4.5,15]))

 # Main footprint, court and approach join the existing city avenue without teleportation.
 box(-50,-1,-80,100,.85,144,(.40,.51,.39),32)
 box(50,-1,-80,40,.85,50,(.40,.51,.39),32)
 box(-47,-.12,0,94,.12,58,(.69,.70,.64),33)
 box(-8,0,0,16,.025,74,FLOOR,33)
 for level in range(3):
  y=level*6
  for x in [-43,11]:
   box(x,y-.3,-22,32,.3,22,FLOOR,33)
   # Front corridor windows and ivory pilasters preserve the approved facade rhythm.
   for xx in range(x,x+32,4):
    box(xx,y,-.25,.48,5.75,.25);box(xx+.48,y,-.25,3.52,1,.25);box(xx+.48,y+4.8,-.25,3.52,.95,.25)
    pane(xx+.48,y+1,-.18,3.52,3.8,.08);box(xx+.48,y+2.8,-.1,3.52,.06,.08,METAL,34)
   box(x-.15,y+5.75,-22.15,32.3,.23,22.3,TRIM)
  # Hall balconies are on its perimeter; no slab blocks the grand central volume.
  if level:
   box(-11,y-.25,-6,22,.25,6,FLOOR,33)
   box(-11,y-.25,-22,2,.25,16,FLOOR,33)
  box(-21,y-.25,-25,42,.25,3,FLOOR,33)
  if level:
   rail(-9,y+1.15,-22,.09,16);rail(-4.05,y+1.15,-6,15.05,.09)
   rail(-27,y+1.15,-25,54,.09)
  for xx in [-35,-19,19,35]:box(xx-1.3,y+5.64,-3,2.6,.11,.35,(.97,.97,.87),32,.65)
  for xx in [-24,24]:
   for zz in [-33,-49,-65]:box(xx-.2,y+5.64,zz-1.2,.4,.11,2.4,(.97,.97,.87),32,.65)
  # Rear wings contain rooms on the outer side, connected galleries on the court side.
  for x in [-43,21]:
   box(x,y-.25,-73,22,.25,51,FLOOR,33)
   box(x-.15,y+5.75,-73.15,22.3,.23,51.3,TRIM)
   window_wall(-76,-.25,y,-43.24 if x<0 else 43,'z',(-6,-1) if x>0 and not level else None)
   window_wall(-73,-25,y,-21.24 if x<0 else 21,'z',(-44,-40) if not level else None)
  box(-43,y-.25,-76,86,.25,3,FLOOR,33)
  window_wall(-43.24,43.24,y,-76.24,'x')
  window_wall(-21,21,y,-73.24,'x',(-2.5,2.5) if not level else None)
  window_wall(-21,21,y,-25,'x',(-2.5,2.5) if not level else None)
  for x in [-43,11]:window_wall(x,x+32,y,-22.24,'x')
 # Continuous roof over the links; the courtyard alone remains open to the sky.
 box(-43.24,17.75,-76.24,86.48,.25,3.24,TRIM)
 box(-21.24,17.75,-25,42.48,.25,3,TRIM)
 # Tall glazed entrance and open rear portal. Front glass stops above the entry.
 box(-11,0,-22,.4,22,16);box(10.6,0,-22,.4,22,16)
 box(-11,0,-22,8.5,22,.3);box(2.5,0,-22,8.5,22,.3);box(-2.5,3.5,-22,5,18.5,.3)
 for x in [-10.64,-5,5,10.64]:box(x-.36,0,-.6,.72,21.6,1.2,TRIM)
 box(-5,3.5,-.6,10,.5,1.2,TRIM)
 box(-.16,4,-.38,.32,17.6,.4,METAL,34)
 for a,c in [(-10.28,-5.36),(-4.64,-.16),(.16,4.64),(5.36,10.28)]:
  for low,high in [(4,5.75),(6,11.75),(12,17.75),(18,21.6)]:
   pane(a,low,-.28,c-a,high-low,.10)
 for a,c in [(-10.28,-5.36),(5.36,10.28)]:
  box(a,0,-.3,c-a,1,.3);pane(a,1,-.28,c-a,2.5,.10)
 # Outer edges overlap the wing walls, while each floor retains its full walkway.
 for x in [-11,10.6]:
  for y in [0,6,12]:box(x,y+3.5,-6,.4,2.5,6)
  box(x,18,-6,.4,3.6,6)
 for y in [5.75,11.75,17.75]:box(-10.28,y,-.38,20.56,.25,.4,METAL,34)
 box(-11.5,21.6,-22.5,23,1,23,TRIM);box(-10.5,22.6,-21,21,.5,21)
 for i in range(7):box(-1+i*.1,23.1+i*.4,-3,2-i*.2,.45,.6,TRIM)
 s.ring((0,24.25,-2.38),.49,.08,(.48,.57,.49),mat=34)
 # Open entry, no giant solid slab across the hall.
 box(-11,-.25,-22,22,.25,22,FLOOR,33)
 s.ring((0,.015,-11),4.4,.09,METAL,'xz',34)
 s.ring((0,.018,-11),3.9,.045,METAL,'xz',34)
 for level in range(3):
  y=level*6
  # Two twelve-step flights. Slender undersides leave safe headroom between levels.
  for i in range(12):
   top=y+(i+1)*.25;box(-8.8,top-.25,-6-(i+1)*.4,2.1,.25,.4,TRIM)
   for xx in [-8.75,-6.98]:box(xx,max(y,top-.65),-6-(i+1)*.4,.23,top-.15-max(y,top-.65),.4,STONE)
   top=y+3+(i+1)*.25;box(-6.4,top-.25,-10.8+i*.4,2.1,.25,.4,TRIM)
   for xx in [-6.35,-4.58]:box(xx,top-.65,-10.8+i*.4,.23,.5,.4,STONE)
  box(-8.8,y+2.75,-12.2,4.5,.25,1.4,TRIM)
  box(-8.8,y+2.45,-12.2,4.5,.3,.4,STONE)
  for xx in [-8.88,-4.30]:
   reverse=xx>-5
   for i in range(12):
    z=-10.6+i*.4 if reverse else -6.2-i*.4
    top=y+(3 if reverse else 0)+(i+1)*.25
    box(xx,top,z,.07,1.05,.07,METAL,34)
   s.beam((xx+.035,y+(4.30 if reverse else 1.30),-10.6 if reverse else -6.2),(xx+.035,y+(7.05 if reverse else 4.05),-6.2 if reverse else -10.6),.045,METAL,34,4)
  # Posts and stringers remain below or beside each flight, outside the walking strip.
  rail(-8.92,y+4.1,-12.2,.08,1.4)
 for xx in [-8.75,-4.68]:box(xx,0,-12.15,.33,14.75,.33,STONE)
 # High hall lights and their brackets connect to the real side walls.
 for yy in [4.5,10.5,16.5]:
  for xx in [-10.6,10.35]:
   for zz in [-8,-18]:box(xx,yy,zz,.25,.8,.28,(.97,.95,.79),32,.8)
 # Roof access exits above the same stairs; fenced roof has a real opening.
 box(-11,17.75,-6,22,.25,6,FLOOR,33);box(-11,17.75,-22,2,.25,16,FLOOR,33)
 for x in [-43,11]:
  box(x,18,-22,32,.25,22,TRIM)
  for zz in [-22,-.25]:rail(x,19.45,zz,32,.10)
  for xx in [x,x+31.9]:rail(xx,19.45,-22,.10,16 if abs(xx)<12 else 22)
 for x in [-37,-18,15,31]:
  box(x,18.75,-10,3,.16,.7,WOOD,35)
  for dx in [.2,2.7]:box(x+dx,18.25,-10,.1,.5,.7,METAL,34)
  for dx in [.5,1.5,2.5]:seats.append(dict(id=f'school_roof_bench_{x}_{dx}',point=[x+dx,18.51,-9.65],yaw=0,bounds=[x,18.25,-10,x+3,19.25,-9.3]))
 for x in [-39,34]:box(x,18.25,-18,2.3,1.7,2.3,(.53,.62,.61),34)
 # Approved room interiors. All entrances face a circulation gallery.
 main=[['secretaria','professores','enfermaria','biblioteca'],['classroom_2f','conselho','laboratorio','ciencias'],['musica','artes','costura','classroom_3a']]
 for lev,names in enumerate(main):
  for name,x in zip(names,[-35,-19,19,35]):
   if name.startswith('classroom'):classroom(name,x,lev*6,-13.5)
   else:
    meta=json.loads(gzip.decompress((HERE/'approved'/f'{name}.json.gz').read_bytes()))
    room(name,x,lev*6,-6-meta['roomSize'][2]/2)
 rear=[('sanitarios',-35,-33,-90),('deposito',-35,-50,-90),('vestiarios',-35,-66,-90),('orientacao',35,-33,90),('impressao',35,-50,90),('transmissao',35,-66,90)]
 for name,x,z,a in rear:room(name,x,0,z,a)
 # Enclosed upper study galleries look into the courtyard through window bays.
 for lev in [1,2]:
  for x in [-38,30]:
   for z in [-32,-47,-63]:
    box(x,lev*6+1,z,6,.15,1.3,WOOD,35)
    for dx in [.2,5.5]:box(x+dx,lev*6,z,.15,1,1.3,METAL,34)
 # Store and lockers are a hall module, not a separate disconnected room shell.
 for x in [3.2,5.4,7.6]:
  box(x,0,-20,1.9,2.3,.7,(.57,.65,.62),34)
  for yy in [.55,1.1,1.65]:box(x,yy,-19.29,1.9,.018,.015,METAL,34)
 box(3,1.15,-15,5.6,.15,1.2,WOOD,35);box(3,0,-15,5.6,1.15,1,(.49,.41,.31),35)
 rooms.append(dict(id='loja-armarios',entry=[5,0,-12],origin=[0,0,0],angle=0,size=[8,4.5,10]))
 rooms.extend([dict(id='corredor-escada',entry=[-7.7,0,-5],origin=[0,0,0],angle=0,size=[5,18,7]),dict(id='cobertura',entry=[-7.5,18,-4],origin=[0,18,0],angle=0,size=[86,4.5,22])])
 # Gym is reached through the eastern covered gallery, including changing facilities on the same ground floor.
 room('ginasio',73,0,-54)
 box(43,-.1,-6,12,.1,6,FLOOR,33);box(52,-.1,-35,4,.1,35,FLOOR,33);box(52,-.1,-38,35,.1,4,FLOOR,33)
 box(52,4.3,-38,35,.2,4,TRIM)
 box(43,4.3,-6,9,.2,6,TRIM);box(52,4.3,-34,4,.2,34,TRIM)
 for z in [-30,-20,-10,-1]:
  for x in [52,55.8]:box(x,0,z,.2,4.3,.2)
 for x in range(53,88,5):box(x,0,-37.9,.2,4.3,.2);box(x,0,-34.3,.2,4.3,.2)
 # Courtyard paths connect each gallery without invisible cutout walls.
 box(-21,0,-72,42,.03,47,(.30,.46,.32),32)
 for z in [-71,-29]:box(-21,.03,z,42,.035,3,FLOOR,33)
 for x in [-21,18]:box(x,.03,-72,3,.035,46,FLOOR,33)
 box(-1.5,.03,-45,3,.035,23,FLOOR,33)
 for x,z in [(5,-41),(-8,-31)]:
  box(x,.6,z,3,.16,.72,WOOD,35);box(x,.95,z-.1,3,.7,.12,WOOD,35)
  for dx in [.25,2.65]:
   box(x+dx,.03,z,.1,.57,.66,METAL,34);box(x+dx,.6,z-.1,.1,.6,.12,METAL,34)
  for dx in [.5,1.5,2.5]:seats.append(dict(id=f'school_court_bench_{x}_{dx}',point=[x+dx,.36,z+.36],yaw=0,bounds=[x,0,z-.1,x+3,1.65,z+.72]))
 for x in [-10,9]:box(x,0,56,1,4.6,1)
 for x in [-10.5,10.5]:
  for z in [6,22,39,54]:
   box(x,0,z,.14,4,.14,METAL,34);box(x-.2,4,z-.2,.54,.55,.54,(.93,.88,.67),32,.7)
 return s,rooms,seats,doors

if __name__=='__main__':
 s,r,se,d=build();print('campus:',len(s.verts)//3,'triangles;',len(r),'rooms;',len(se),'seats;',len(d),'doors')
