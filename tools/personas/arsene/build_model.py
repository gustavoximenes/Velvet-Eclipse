"""Arsene approval study. Cuboids, Box UV and an articulated Bedrock rig.

Writes only beside this script; never installs or edits mod resources.
Logical quarter-units become integer cuboids on a uniformly enlarged grid.
"""
from pathlib import Path
import base64, json, math, uuid
import numpy as np
from PIL import Image, ImageDraw

ROOT=Path(__file__).resolve().parent
OUT=ROOT/'draft'
DENSITY=4
S=1024
PAL={
 'red':['#490c24','#871127','#c92135','#f34a48'],
 'ruby':['#390e24','#70132e','#a5203d','#db4052'],
 'black':['#070d18','#121c29','#22303d','#425561'],
 'feather':['#0b101f','#182037','#28334e','#414d71'],
 'violet':['#0c1022','#1a203b','#30395b','#4c577c'],
 'silver':['#354653','#718895','#acbcc2','#d7e2dd'],
 'ivory':['#747e86','#b9c4bf','#e4e8d8','#fffce3'],
 'gold':['#513627','#987235','#d0ad59','#f3d88a'],
 'eye':['#7d1231','#dc2945','#ff7348','#ffe482'],
}

def face_uv(u,v,w,h,d):
 return {'north':(u+d,v+d,w,h),'south':(u+2*d+w,v+d,w,h),'west':(u+d+w,v+d,d,h),'east':(u,v+d,d,h),'up':(u+d,v,w,d),'down':(u+d+w,v,w,d)}

def paint(w,h,mat,face,pattern=''):
 p=PAL[mat];shade={'north':2,'south':1,'up':3,'down':0,'west':1,'east':1}[face]
 im=Image.new('RGBA',(w,h),p[shade]);d=ImageDraw.Draw(im)
 if min(w,h)>2:
  d.line((w-1,0,w-1,h-1),fill=p[0]);d.line((0,h-1,w-1,h-1),fill=p[0])
  d.line((0,0,0,h-2),fill=p[3])
 if pattern in ('cloth','boot') and min(w,h)>5 and face in ('north','south','east','west'):
  d.line((w//4,1,w//3,h-2),fill=p[1])
  d.polygon([(w//2,1),(w-2,1),(w-3,max(2,h//2)),(w//2,h//3)],fill=p[2])
 if pattern=='hat' and min(w,h)>5 and face in ('north','south','east','west'):
  d.rectangle((1,1,w-2,h-2),fill=p[1])
  d.polygon([(1,1),(w-3,1),(max(1,w-4),h//3),(1,2*h//3)],fill=p[2])
  d.line((1,1,1,h-2),fill=p[3])
 if pattern=='feather' and face in ('north','south'):
  # Deliberate dark pixel clusters, shaft and a few barbs, never random noise.
  d.polygon([(1,1),(w//2,2),(max(1,w-2),h-2),(max(1,w//2),h-1)],fill=p[1])
  d.line((max(1,w//2),1,max(1,w//2-1),h-3),fill=p[3],width=1)
  for y in range(5,h-3,11):
   d.line((max(1,w//2),y,max(1,w-2),y-3),fill=p[2])
   d.line((max(1,w//2),y+2,1,y),fill=p[0])
 if pattern=='mask' and face=='north':
  d.polygon([(0,0),(w//3,1),(w//2,h-1),(0,h//2)],fill=PAL['black'][0])
  d.line((w//2,1,w//2,h-2),fill=PAL['silver'][1])
  d.line((w//2+1,h//2,w-2,2),fill=PAL['red'][1])
 if pattern=='lacing' and face=='north':
  for y in range(3,h-4,9):
   d.line((2,y,w-3,y+6),fill=PAL['red'][2],width=1)
   d.line((w-3,y,2,y+6),fill=PAL['red'][1],width=1)
   d.rectangle((1,y,2,y+1),fill=PAL['gold'][2]);d.rectangle((w-3,y,w-2,y+1),fill=PAL['gold'][3])
 return im

class Model:
 def __init__(self):
  self.bones=[];self.parts=[];self.atlas=Image.new('RGBA',(S,S));self.x=self.y=2;self.row=0
 def bone(self,name,parent=None,pivot=(0,0,0),rotation=None):
  b={'name':name,'pivot':[round(v*DENSITY,6) for v in pivot]}
  if parent:b['parent']=parent
  if rotation is not None:b['rotation']=list(rotation)
  self.bones.append(b);return name
 def cube(self,bone,name,o,size,mat='black',pattern='',rotation=None,pivot=None):
  dims=[round(v*DENSITY) for v in size];w,h,d=dims
  assert all(v>0 for v in dims) and all(abs(v*DENSITY-i)<1e-5 for v,i in zip(size,dims)),name
  aw,ah=2*(w+d),h+d
  if self.x+aw+2>S:self.x=2;self.y+=self.row+2;self.row=0
  assert self.y+ah+2<S,('atlas full',name,self.y)
  uv=[self.x,self.y];self.x+=aw+2;self.row=max(self.row,ah)
  for f,(u,v,fw,fh) in face_uv(*uv,w,h,d).items():self.atlas.paste(paint(fw,fh,mat,f,pattern),(u,v))
  c={'origin':[round(v*DENSITY,6) for v in o],'size':dims,'uv':uv}
  if rotation is not None:c.update(rotation=list(rotation),pivot=[round(v*DENSITY,6) for v in (pivot if pivot is not None else o)])
  next(b for b in self.bones if b['name']==bone).setdefault('cubes',[]).append(c)
  self.parts.append({'name':name,'bone':bone,'material':mat,'cube':c})
 def segment(self,bone,name,a,b,width,depth,mat='black',pattern=''):
  dx,dy,dz=np.array(b)-a;length=math.sqrt(dx*dx+dy*dy+dz*dz)
  rx=math.degrees(math.atan2(dz,math.hypot(dx,dy)));rz=math.degrees(math.atan2(-dx,dy))
  self.cube(bone,name,(a[0]-width/2,a[1],a[2]-depth/2),(width,math.ceil(length*4)/4,depth),mat,pattern,(rx,0,rz),a)

def build_geometry():
 m=Model();B=m.bone;C=m.cube;L=m.segment
 B('arsene');B('hips','arsene',(0,28,0));B('body','hips',(0,29,0))
 # Narrow black waistcoat framed by a fitted scarlet tailcoat.
 C('hips','pelvis',(-2.5,26,-1.6),(5,3.5,3.25),'black')
 C('body','waist',(-2,28.5,-1.5),(4,4,3),'black','cloth')
 C('body','vest_lower',(-2.4,32,-1.7),(4.75,3.75,3.5),'black','cloth')
 C('body','vest_ribcage',(-3,35.5,-1.85),(6,3.5,3.75),'black','cloth')
 C('body','shoulders',(-3.7,39,-1.8),(7.5,2.5,3.5),'red','cloth')
 C('body','corset_lacing',(-1.4,29.5,-1.94),(2.75,8.25,.25),'black','lacing')
 for s,label in [(-1,'right'),(1,'left')]:
  C('body',label+'_coat_ribs',(s*2.8-.875,34,-1.9),(1.75,6,3.75),'red','cloth',(0,0,-s*9),(s*3,40,0))
  L('body',label+'_lapel',(s*1.4,35,-2.2),(s*2.9,40.8,-2.2),1.25,.5,'red','cloth')
  C('body',label+'_vest_point',(s*.85-.7,27,-1.9),(1.5,4,.5),'black','cloth',(0,0,-s*18),(s*.85,31,-1.6))
  for j,y in enumerate([35.5,38,40]):
   C('body',label+'_brass_button_'+str(j),(s*(2.2+.12*j)-.22,y,-2.48),(.5,.5,.25),'gold',rotation=(0,0,45),pivot=(s*(2.2+.12*j),y+.25,-2.35))
  B(label+'_coat_tail','body',(s*2.5,32,1))
  C(label+'_coat_tail',label+'_tail_top',(s*2.9-1.4,22.5,.4),(2.75,10,1),'red','cloth',(0,0,s*8),(s*2.5,32,1))
  C(label+'_coat_tail',label+'_tail_taper',(s*3.25-.9,19,.5),(1.75,5,.75),'ruby','cloth',(0,0,-s*8),(s*3.25,24,1))
  C(label+'_coat_tail',label+'_side_front_tail',(s*3.15-1,22,-1.8),(2,9,.5),'red','cloth',(0,0,s*7),(s*2.5,31,-1.8))
  for j in range(3):C(label+'_coat_tail',label+'_tail_rivet_'+str(j),(s*3.4-.125,28.5-j*1.3,-2.05),(.25,.5,.25),'gold')
  # Gold pocket chains follow two scallops across the dark vest.
  for row in range(2):
   points=[(s*.2,33.5+row*2,-2.12),(s*1.2,33+row*2,-2.15),(s*2.2,34+row*2,-2.18)]
   for j in range(2):L('body',f'{label}_chain_{row}_{j}',points[j],points[j+1],.25,.25,'gold')
 # White cravat is folded cuboids, not a texture pasted across the whole torso.
 B('cravat','body',(0,40.8,-1.75))
 C('cravat','cravat_knot',(-.9,40,-2.9),(1.75,1.5,1),'ivory',rotation=(0,0,15),pivot=(0,40.8,-2.4))
 for s,label in [(-1,'r'),(1,'l')]:
  C('cravat',label+'_cravat_fold',(s*.8-.85,37.8,-2.5),(1.75,3.25,.5),'ivory',rotation=(-12,s*12,s*18),pivot=(0,40.6,-2.3))
  C('cravat',label+'_cravat_tip',(s*.55-.4,36.8,-2.75),(.75,2,.25),'ivory',rotation=(0,0,-s*12),pivot=(s*.55,38,-2.5))
 C('body','neck',(-.9,41,-.8),(1.75,2.75,1.75),'black')
 for s,label in [(-1,'right'),(1,'left')]:
  C('body',label+'_high_scarlet_collar',(s*1.7-.625,40.8,-.6),(1.25,5,1.75),'red','cloth',(0,0,-s*17),(s*1.7,41,0))
  L('body',label+'_collar_tip',(s*2.7,44,.3),(s*3.8,47,.3),.5,.5,'red')
 # Arsene's own elongated devil mask and swept brim. No reused Izanagi head.
 B('head','body',(0,43,0))
 C('head','mask_center',(-1.5,43.25,-1.6),(3,4.5,2.75),'black','mask')
 for s,label in [(-1,'right'),(1,'left')]:
  C('head',label+'_mask_cheek',(s*1.45-.5,43.4,-1.1),(1,3.75,2),'black','mask',(0,s*27,-s*15),(s*1.1,45,-.3))
  L('head',label+'_temple_red_crest',(s*.9,46.25,-1.85),(s*2.5,48.1,-.95),.5,.5,'red')
  L('head',label+'_narrow_ember_eye',(s*.35,45.75,-1.92),(s*1.35,46.05,-1.8),.25,.25,'eye')
  L('head',label+'_cheek_filagree',(s*.5,43.8,-1.83),(s*1.4,45.1,-1.9),.25,.25,'red')
  for j in range(2):L('head',f'{label}_jaw_mark_{j}',(s*.25,43.4+j*.65,-1.82),(s*.85,43.65+j*.65,-1.83),.25,.25,'gold' if j==0 else 'red')
  for j in range(3):
   L('head',f'{label}_mask_spike_{j}',(s*1.4,44.2+j*.85,.4),(s*(3.1+j*.4),44+j*1.2,1),.5,.5,'black')
 C('head','pointed_chin',(-.65,42.7,-1.65),(1.25,1.5,1.5),'black',rotation=(0,0,45),pivot=(0,43.5,-.7))
 C('head','nose_ridge',(-.25,44.4,-1.95),(.5,1.25,.5),'black')
 B('hat','head',(0,48,0))
 C('hat','hat_base',(-2.15,47.5,-1.9),(4.25,1,3.75),'black','hat')
 C('hat','hat_band',(-1.8,48,-1.65),(3.5,1,3.25),'red','cloth')
 C('hat','crown_middle',(-1.9,48.8,-1.6),(3.75,15,3.25),'black','hat')
 C('hat','crown_upper',(-2.15,61,-1.75),(4.25,4.5,3.5),'black','hat')
 C('hat','crown_front_facet',(-1.25,49,-1.8),(2.5,16.25,.25),'black','hat',(0,0,-1.8),(0,49,-1.7))
 C('hat','top_silver_rim',(-2.2,65.25,-1.85),(4.5,.25,3.75),'silver')
 C('hat','top_black_inset',(-1.9,65.47,-1.55),(3.75,.25,3),'black')
 for s,label in [(-1,'right'),(1,'left')]:
  pts=[(s*.6,47.9,-1.8),(s*3.1,49,-.9),(s*7.5,49.1,-.3),(s*8.9,50.2,.2),(s*9.1,52,.4)]
  for j,(a,b) in enumerate(zip(pts,pts[1:])):
   L('hat',f'{label}_swept_brim_{j}',a,b,.75 if j<2 else .5,.75,'black')
   if j<3:L('hat',f'{label}_brim_scarlet_inlay_{j}',np.array(a)+(0,.15,-.43),np.array(b)+(0,.15,-.43),.25,.25,'red')
 # Fully articulated slender arms with a five-finger silhouette.
 for s,label in [(-1,'right'),(1,'left')]:
  x=s*4.5;B(label+'_upper_arm','body',(x,40.2,0))
  C(label+'_upper_arm',label+'_shoulder_cap',(x-1.15,38.5,-1.25),(2.25,2.5,2.5),'red','cloth')
  C(label+'_upper_arm',label+'_sleeve',(x-.85,32.2,-.85),(1.75,7.5,1.75),'red','cloth')
  B(label+'_forearm',label+'_upper_arm',(x,32.2,0))
  C(label+'_forearm',label+'_glove_cuff',(x-1,30.9,-1),(2,1.5,2),'red')
  C(label+'_forearm',label+'_glove',(x-.7,24.2,-.7),(1.5,7,1.5),'red','cloth')
  B(label+'_hand',label+'_forearm',(x,24.2,0))
  C(label+'_hand',label+'_palm',(x-1,21.8,-.45),(2,2.5,1),'red')
  for i in range(4):
   fx=x-.8+i*.5;B(f'{label}_finger_{i}',label+'_hand',(fx,22,0))
   length=2.5-abs(i-1.5)*.35
   L(f'{label}_finger_{i}',f'{label}_finger_base_{i}',(fx,22,0),(fx+(i-1.5)*.2,22-length,-.35),.25,.5,'black')
   L(f'{label}_finger_{i}',f'{label}_talon_{i}',(fx+(i-1.5)*.2,22-length,-.35),(fx+(i-1.5)*.23,21.3-length,-.85),.25,.25,'black')
  L(label+'_hand',label+'_thumb',(x-s*.8,23.7,0),(x-s*1.65,22.8,-.2),.5,.5,'red')
  L(label+'_hand',label+'_thumb_talon',(x-s*1.65,22.8,-.2),(x-s*1.7,21.6,-.55),.25,.5,'black')
 # Long scarlet boots, pointed toes and dagger heels.
 for s,label in [(-1,'right'),(1,'left')]:
  x=s*2;B(label+'_thigh','hips',(x,28,0))
  C(label+'_thigh',label+'_upper_thigh',(x-1.25,21,-1.25),(2.5,7,2.5),'ruby','boot')
  C(label+'_thigh',label+'_lower_thigh',(x-.95,16,-1),(2,6,2),'ruby','boot')
  B(label+'_shin',label+'_thigh',(x,16,0))
  C(label+'_shin',label+'_knee_cap',(x-1.1,14.5,-1.3),(2.25,2,1.75),'red','boot')
  C(label+'_shin',label+'_boot_calf',(x-.95,8,-.9),(2,7,1.75),'ruby','boot')
  C(label+'_shin',label+'_boot_ankle',(x-.65,3.5,-.65),(1.25,5,1.5),'red','boot')
  B(label+'_foot',label+'_shin',(x,3.5,0))
  C(label+'_foot',label+'_boot_foot',(x-.8,2,-2.6),(1.5,2,3.5),'red','boot')
  C(label+'_foot',label+'_pointed_toe',(x-.5,1.7,-3.9),(1,1,2.25),'ruby',rotation=(-12,0,0),pivot=(x,2,-2))
  L(label+'_foot',label+'_steel_toe',(x,1.55,-4.8),(x,1.5,-2.8),.5,.5,'silver')
  L(label+'_foot',label+'_dagger_heel',(x,2.8,.8),(x,-.2,1.6),.5,.75,'silver')
 # Two jointed wings. Outer primaries taper as separate solid cuboids.
 B('wings','body',(0,39.5,1.7))
 for s,label in [(-1,'right'),(1,'left')]:
  B(label+'_wing','wings',(s*3,40,1.8))
  L(label+'_wing',label+'_wing_shoulder',(s*3,40,2),(s*12,46,2.5),2,2,'black')
  L(label+'_wing',label+'_leading_edge',(s*3.5,40.6,1.2),(s*12,46.5,1.6),.75,.75,'feather')
  L(label+'_wing',label+'_shoulder_highlight',(s*10.3,45.6,1.25),(s*12.3,46.5,1.6),.5,.5,'silver')
  for i in range(6):
   a=(s*(4+i*1.45),40.4+i*.86,2.7)
   b=(s*(5+i*1.25),34.5-i*.65,2.8)
   L(label+'_wing',f'{label}_inner_feather_{i}',b,a,2,.5,'feather','feather')
  B(label+'_wing_outer',label+'_wing',(s*12,46,2.5))
  L(label+'_wing_outer',label+'_wing_spar',(s*12,46,2.5),(s*22,24,3),1.75,1.5,'black')
  L(label+'_wing_outer',label+'_wing_ridge',(s*12,46.5,1.7),(s*22,24,2.2),.75,.75,'feather')
  tips=[(12,32),(12.8,30),(13,27),(14,23),(15,19),(17,15),(18.5,11),(20,8),(23,6),(26,4),(28,2),(30,1),(31,3)]
  for i,(ex,ey) in enumerate(tips):
   a=np.array((s*(12.2+i*.79),45-i*1.65,2.8+i*.04))
   end=np.array((s*ex,ey,2.9))
   bone=f'{label}_primary_{i}';B(bone,label+'_wing_outer',tuple(a))
   v=end-a;middle=a+v*.58+np.array((s*.8,0,-.2));narrow=a+v*.84
   L(bone,bone+'_vane',tuple(middle),tuple(a),2.5,.5,'feather' if i%3 else 'violet','feather')
   L(bone,bone+'_taper',tuple(narrow),tuple(middle+v*.02),1.75,.5,'feather','feather')
   L(bone,bone+'_tip',tuple(end),tuple(narrow+v*.02),.75,.25,'feather','feather')
   # Short coverts overlap the primary roots to make a layered feather mantle.
   a2=a+np.array((-s*.35,.8,-.7));end2=a2+v*.38+np.array((-s*.6,0,-.25))
   L(label+'_wing_outer',f'{label}_covert_{i}',tuple(end2),tuple(a2),2,.5,'black' if i%2 else 'feather','feather')
 return m

def matrix(v):
 x,y,z=np.radians(v);cx,sx=np.cos(x),np.sin(x);cy,sy=np.cos(y),np.sin(y);cz,sz=np.cos(z),np.sin(z)
 return np.array([[cz,-sz,0],[sz,cz,0],[0,0,1]])@np.array([[cy,0,sy],[0,1,0],[-sy,0,cy]])@np.array([[1,0,0],[0,cx,-sx],[0,sx,cx]])

def euler(r):
 y=math.asin(np.clip(-r[2,0],-1,1))
 if abs(math.cos(y))>1e-6:x=math.atan2(r[2,1],r[2,2]);z=math.atan2(r[1,0],r[0,0])
 else:x=0;z=math.atan2(-r[0,1],r[1,1])
 return np.degrees([x,y,z])

def aim_down(v):
 v=np.array(v,dtype=float);v/=np.linalg.norm(v)
 start=np.array([0.,-1.,0.]);axis=np.cross(start,v);s=np.linalg.norm(axis);c=float(start@v)
 if s<1e-8:return np.eye(3) if c>0 else matrix([0,0,180])
 x,y,z=axis;k=np.array([[0,-z,y],[z,0,-x],[-y,x,0]])
 return np.eye(3)+k+k@k*((1-c)/(s*s))

def ik(start,end,pole,l1,l2):
 start=np.array(start,float);end=np.array(end,float);pole=np.array(pole,float)
 v=end-start;dist=np.linalg.norm(v);n=v/dist;dist=min(l1+l2-.005,max(abs(l1-l2)+.005,dist));end=start+n*dist
 a=(l1*l1-l2*l2+dist*dist)/(2*dist);h=math.sqrt(max(0,l1*l1-a*a))
 side=pole-start;side-=n*(side@n);side/=np.linalg.norm(side)
 elbow=start+n*a+side*h;r1=aim_down(elbow-start);r2=aim_down(end-elbow)
 return euler(r1),euler(r1.T@r2),r2,end

def pose(kind,t):
 # Each key is a full pose, so no attack depends on an external idle controller.
 p={};pos={};body=[0,0,0]
 hand={'right':(-3,30,-3),'left':(-4,43.5,-3.7)}
 poles={'right':(-10,34,0),'left':(10,37,-1)}
 feet={'right':(-7,3.8,0),'left':(7,3.8,0)}
 wings=0;spread=0;tails=0;finger=0
 if kind=='idle':
  a=math.sin(t*2*math.pi/4.8);b=math.sin(t*2*math.pi/4.8+.7)-math.sin(.7)
  body=[a*.45,0,0];wings=a*1.2;spread=b*.9;tails=b*.6
  hand['left']=(-4,43.5+a*.12,-3.7)
 elif kind=='magic':
  stamps=[0,.35,.8,1.15,1.55,1.85,2.35,2.8]
  w=float(np.interp(t,stamps,[0,.12,.45,.65,1,1,.35,0]))
  body=[-22*w,0,0];wings=7*w;spread=58*w;tails=12*w;finger=14*w
  ys=np.interp(t,stamps,[30,37,54,51,25,25,28,30]);xs=np.interp(t,stamps,[-3,-5,-7,-7,-5,-5,-4,-3])
  zs=np.interp(t,stamps,[-3,-4,0,-2,-5,-5,-4,-3]);hand['right']=(xs,ys,zs)
  poles['right']=(-10-3*w,34+8*w,-w);hand['left']=(-4+10*w,43.5-11.5*w,-3.7-1.3*w)
 elif kind=='physical':
  stamps=[0,.3,.65,.95,1.25,1.55,2,2.6]
  w=float(np.interp(t,stamps,[0,.2,.7,1,.75,.4,.2,0]))
  body=[-6*w,-14*w,-9*w];spread=48*w;wings=5*w;tails=19*w
  hand['right']=(-3-9*w,30+7*w,-3);hand['left']=(-4+16.4*w,43.5-6*w,-3.7)
 p['body']=body
 # Compensate the entire torso transform: the mask faces forward during magic.
 p['head']=list(euler(matrix(body).T)) if kind=='magic' else [0,-body[1]*.35,0]
 for label,s in [('right',-1),('left',1)]:
  a,b,r,end=ik((s*4.5,40.2,0),hand[label],poles[label],8,8)
  p[label+'_upper_arm']=a;p[label+'_forearm']=b
  # Orient idle fingers beside the cheek; cast fingers point down at the release.
  turn=-145 if label=='left' else 0
  if kind=='magic':
   turn=-145*(1-w) if label=='left' else float(np.interp(t,[0,.35,.8,1.15,1.55,1.85,2.35,2.8],[0,-90,-165,-130,0,0,0,0]))
  elif kind=='physical':turn=-145+120*w if label=='left' else -20*w
  target=[0,0,turn]
  p[label+'_hand']=euler(r.T@matrix(target))
  a,b,r,end=ik((s*2,28,0),feet[label],(s*7,17,-7),12,12.5)
  p[label+'_thigh']=a;p[label+'_shin']=b
  footworld=matrix([0,0,0])
  if kind=='physical' and label=='right':footworld=matrix([-20*w,0,-18*w])
  p[label+'_foot']=euler(r.T@footworld)
  if kind=='physical' and label=='right':
   # A continuous hip arc passes in front of the body. Linear ankle IK would
   # pull the knee through the pelvis while crossing the strike diagonal.
   def keyed(vectors):return [float(np.interp(t,stamps,[v[j] for v in vectors])) for j in range(3)]
   thigh0=np.array(a);shin0=np.array(b);foot0=euler(r.T)
   p[label+'_thigh']=keyed([thigh0,[30,0,0],[100,0,35],[138,0,45],[25,0,45],[2,0,28],thigh0,thigh0])
   p[label+'_shin']=keyed([shin0,[-50,0,0],[-30,0,0],[-3,0,0],[0,0,0],[-8,0,0],shin0,shin0])
   p[label+'_foot']=keyed([foot0,[10,0,0],[15,0,0],[20,0,0],[-15,0,0],[-5,0,0],foot0,foot0])
  p[label+'_wing']=[0,-s*wings,-s*wings*.35]
  p[label+'_wing_outer']=[0,-s*spread*.12,s*spread]
  p[label+'_coat_tail']=[tails,0,s*tails*.15]
  for i in range(13):
   lag=(math.sin(t*2*math.pi/4.8+i*.35)-math.sin(i*.35)) if kind=='idle' else math.sin(t*math.pi/2.8)*(i/10)
   p[f'{label}_primary_{i}']=[lag*.7,0,s*lag*1.6]
  for i in range(4):p[f'{label}_finger_{i}']=[finger*(.5+i*.12),0,(i-1.5)*finger*.18]
 return p,pos

def animate(m):
 rest,_=pose('idle',0)
 for b in m.bones:
  if b['name'] in rest:b['rotation']=[round(float(v),6) for v in rest[b['name']]]
 clips={}
 for kind,length,loop in [('idle',4.8,True),('magic',2.8,False),('physical',2.6,False)]:
  times=sorted(set([round(i*length/(48 if loop else 156),6) for i in range((48 if loop else 156)+1)]+([.35,.8,1.15,1.55,1.85,2.35] if kind=='magic' else [.3,.65,.95,1.25,1.55,2] if kind=='physical' else [])))
  tracks={}
  previous={}
  for t in times:
   p,ps=pose(kind,t)
   if t==length:p={k:list(v) for k,v in rest.items()};ps={}
   for bone,rot in p.items():
    v=np.array(rot,dtype=float)
    # Avoid Euler +/-180 interpolation flips on forearms and knees.
    if bone in previous:
     candidates=[v,np.array([v[0]+180,180-v[1],v[2]+180])]
     candidates=[a+360*np.round((previous[bone]-a)/360) for a in candidates]
     v=min(candidates,key=lambda a:np.linalg.norm(a-previous[bone]))
    previous[bone]=v
    delta=v-np.array(rest.get(bone,[0,0,0]))
    tracks.setdefault(bone,{}).setdefault('rotation',{})[str(t)]=[round(float(x),6) for x in delta]
   for bone,v in ps.items():tracks.setdefault(bone,{}).setdefault('position',{})[str(t)]=[float(x)*DENSITY for x in v]
  clips['animation.arsene.'+{'idle':'ground_idle','magic':'magic_attack','physical':'physical_attack'}[kind]]={'loop':loop,'animation_length':length,'bones':tracks}
 return {'format_version':'1.8.0','animations':clips}

def export_blockbench(m,animation):
 def ident(name):return str(uuid.uuid5(uuid.NAMESPACE_URL,'standalone-study/arsene/'+name))
 elements=[]
 for part in m.parts:
  c=part['cube'];a=c['origin'];b=[x+y for x,y in zip(a,c['size'])]
  elements.append({'name':part['name'],'type':'cube','uuid':ident(part['name']),'from':a,'to':b,'origin':c.get('pivot',a),'rotation':c.get('rotation',[0,0,0]),'box_uv':True,'uv_offset':c['uv'],'autouv':0,'color':0,'faces':{f:{'uv':[u,v,u+w,v+h],'texture':0} for f,(u,v,w,h) in face_uv(*c['uv'],*c['size']).items()}})
 def group(b):
  return {'name':b['name'],'origin':b['pivot'],'rotation':b.get('rotation',[0,0,0]),'uuid':ident('bone/'+b['name']),'export':True,'isOpen':False,'children':[ident(p['name']) for p in m.parts if p['bone']==b['name']]+[group(c) for c in m.bones if c.get('parent')==b['name']]}
 project={'meta':{'format_version':'4.10','model_format':'bedrock','box_uv':True},'name':'arsene','model_identifier':'arsene','visible_box':[28,20,8],'resolution':{'width':S,'height':S},'elements':elements,'outliner':[group(m.bones[0])],'textures':[{'name':'arsene.png','id':'0','uuid':ident('texture'),'path':'arsene.png','source':'data:image/png;base64,'+base64.b64encode((OUT/'arsene.png').read_bytes()).decode(),'mode':'bitmap','saved':True}],'animations':[]}
 for clip,anim in animation['animations'].items():
  animators={}
  for name,tracks in anim['bones'].items():
   animators[ident('bone/'+name)]={'name':name,'type':'bone','keyframes':[{'channel':channel,'data_points':[{'x':v[0],'y':v[1],'z':v[2]}],'time':float(t),'color':-1,'interpolation':'linear','uuid':ident(clip+name+channel+t)} for channel,keys in tracks.items() for t,v in keys.items()]}
  project['animations'].append({'uuid':ident('animation/'+clip),'name':clip,'loop':'loop' if anim['loop'] else 'once','length':anim['animation_length'],'animators':animators})
 (OUT/'arsene.bbmodel').write_text(json.dumps(project,indent=2)+'\n',encoding='utf-8')

def build():
 OUT.mkdir(parents=True,exist_ok=True);m=build_geometry();animation=animate(m)
 geo={'format_version':'1.12.0','minecraft:geometry':[{'description':{'identifier':'geometry.arsene','texture_width':S,'texture_height':S,'visible_bounds_width':28,'visible_bounds_height':20,'visible_bounds_offset':[0,8,0]},'bones':m.bones}]}
 for fn,obj in [('arsene.geo.json',geo),('arsene.animation.json',animation),('parts.json',m.parts)]:
  (OUT/fn).write_text(json.dumps(obj,indent=2)+'\n',encoding='utf-8')
 m.atlas.save(OUT/'arsene.png');export_blockbench(m,animation)
 stats={'cuboids':len(m.parts),'bones':len(m.bones),'atlas':[S,S],'atlas_height_used':m.y+m.row,'uniform_density':DENSITY,'clips':{k:v['animation_length'] for k,v in animation['animations'].items()}}
 (OUT/'model-info.json').write_text(json.dumps(stats,indent=2)+'\n',encoding='utf-8');print(json.dumps(stats))

if __name__=='__main__':build()
