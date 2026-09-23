"""Orpheus standalone Bedrock/Box UV approval model. Never writes mod resources."""
from pathlib import Path
import base64, json, math, uuid
import numpy as np
from PIL import Image, ImageDraw
ROOT=Path(__file__).resolve().parent
OUT=ROOT/'draft'
DENSITY=4
S=1024

PAL={
 'teal':['#1c4656','#397f8c','#70b9bf','#b9e6df'],
 'ivory':['#687d88','#a1b8bc','#d4e2dc','#f3f3dc'],
 'metal':['#2c414f','#617e8a','#9ab9bf','#d9e6db'],
 'dark':['#111e31','#25384c','#3b5069','#6d899b'],
 'skin':['#4c5d72','#758aa0','#9aafc1','#bbced6'],
 'hair':['#788d9e','#afc2cb','#d5e3e2','#f4f3dd'],
 'red':['#451d32','#832e43','#b74a56','#e0807c'],
 'eye':['#7b152e','#c92646','#ff5a62','#ffcbaf'],
 'black':['#101b29','#233547','#374e65','#69899a'],
}

def paint(w,h,mat,face,pattern=''):
 p=PAL[mat];shade={'north':2,'south':1,'up':3,'down':0,'east':1,'west':1}[face]
 im=Image.new('RGBA',(w,h),p[shade]);d=ImageDraw.Draw(im)
 if min(w,h)>2:
  d.line((0,0,w-1,0),fill=p[3]);d.line((w-1,0,w-1,h-1),fill=p[0])
  d.line((1,h-1,w-1,h-1),fill=p[0])
 if pattern in ('plate','boot','frame') and w>5 and h>5:
  d.polygon([(1,1),(w-3,1),(max(2,w-4),h//3),(1,h//2)],fill=p[3])
  d.line((1,h-2,w-2,h-2),fill=p[1])
 if pattern=='seam' and w>4 and h>4:
  d.line((w//2,1,w//2,h-2),fill=p[1]);d.line((w//2+1,1,w//2+1,h-2),fill=p[3])
 if pattern=='hair' and w>2 and h>4:
  for x in range(2,w-1,4):
   d.line((x,1,max(1,x-1),h-2),fill=p[1]);d.line((x+1,1,x,h-3),fill=p[3])
 if pattern=='scarf' and h>4:
  for y in range(3,h-1,4):d.line((1,y,w-2,max(1,y-2)),fill=p[1])
 if pattern=='vent' and face in ('north','south'):
  d.rectangle((0,0,w-1,h-1),fill=p[shade])
  for y in range(2,h-1,3):
   for x in range(2+(y//3%2),w-1,3):
    d.point((x,y),fill=PAL['dark'][0]);d.point((x,y+1),fill=p[1])
 if pattern=='grille' and face in ('north','south'):
  for x in range(3,w-2,4):
   d.line((x,3,x,h-4),fill=PAL['dark'][1],width=2);d.line((x+2,3,x+2,h-4),fill=p[3])
 if pattern=='eye':
  im=Image.new('RGBA',(w,h));d=ImageDraw.Draw(im)
  if face!='north':return im
  d.polygon([(0,h//2),(w//3,0),(w-1,1),(w-2,h-1),(1,h-1)],fill=PAL['dark'][0])
  d.polygon([(1,h//2),(w//3,1),(w-2,1),(w-2,h-2),(1,h-2)],fill=PAL['eye'][2]);d.point((w//2,1),fill=PAL['eye'][3])
 if pattern=='port':
  im=Image.new('RGBA',(w,h));d=ImageDraw.Draw(im)
  if face not in ('north','south'):return im
  d.polygon([(w//3,0),(2*w//3,0),(w-1,h//3),(w-1,2*h//3),(2*w//3,h-1),(w//3,h-1),(0,2*h//3),(0,h//3)],fill=PAL['dark'][0])
  d.polygon([(w//2,2),(w-3,h//2),(w//2,h-3),(2,h//2)],fill=PAL['dark'][1])
 return im

def face_uv(u,v,w,h,d):
 return {'north':(u+d,v+d,w,h),'south':(u+2*d+w,v+d,w,h),'west':(u+d+w,v+d,d,h),'east':(u,v+d,d,h),'up':(u+d,v,w,d),'down':(u+d+w,v,w,d)}

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

def ik(start,end,pole,l1,l2,hinge=False):
 start=np.array(start,float);end=np.array(end,float);pole=np.array(pole,float)
 v=end-start;dist=np.linalg.norm(v);n=v/dist;dist=min(l1+l2-.005,max(abs(l1-l2)+.005,dist));end=start+n*dist
 a=(l1*l1-l2*l2+dist*dist)/(2*dist);h=math.sqrt(max(0,l1*l1-a*a))
 side=pole-start;side-=n*(side@n);side/=np.linalg.norm(side)
 elbow=start+n*a+side*h;r1=aim_down(elbow-start);r2=aim_down(end-elbow)
 if hinge:
  # Both arm bones share the elbow hinge axis. This prevents artificial
  # forearm roll while the hands track the rigid lyre.
  normal=np.cross(n,side);normal*=(-1 if start[0]<0 else 1);normal/=np.linalg.norm(normal)
  def frame(direction):
   y=-direction/np.linalg.norm(direction);x=np.cross(y,normal);x/=np.linalg.norm(x)
   return np.column_stack([x,y,normal])
  r1=frame(elbow-start);r2=frame(end-elbow)
 return euler(r1),euler(r1.T@r2),r2,end

def export_blockbench(m,animation):
 def ident(name):return str(uuid.uuid5(uuid.NAMESPACE_URL,'standalone-study/orpheus/'+name))
 elements=[]
 for part in m.parts:
  c=part['cube'];a=c['origin'];b=[x+y for x,y in zip(a,c['size'])]
  elements.append({'name':part['name'],'type':'cube','uuid':ident(part['name']),'from':a,'to':b,'origin':c.get('pivot',a),'rotation':c.get('rotation',[0,0,0]),'box_uv':True,'uv_offset':c['uv'],'autouv':0,'color':0,'faces':{f:{'uv':[u,v,u+w,v+h],'texture':0} for f,(u,v,w,h) in face_uv(*c['uv'],*c['size']).items()}})
 def group(b):
  return {'name':b['name'],'origin':b['pivot'],'rotation':b.get('rotation',[0,0,0]),'uuid':ident('bone/'+b['name']),'export':True,'isOpen':False,'children':[ident(p['name']) for p in m.parts if p['bone']==b['name']]+[group(c) for c in m.bones if c.get('parent')==b['name']]}
 project={'meta':{'format_version':'4.10','model_format':'bedrock','box_uv':True},'name':'orpheus','model_identifier':'orpheus','visible_box':[28,20,8],'resolution':{'width':S,'height':S},'elements':elements,'outliner':[group(m.bones[0])],'textures':[{'name':'orpheus.png','id':'0','uuid':ident('texture'),'path':'orpheus.png','source':'data:image/png;base64,'+base64.b64encode((OUT/'orpheus.png').read_bytes()).decode(),'mode':'bitmap','saved':True}],'animations':[]}
 for clip,anim in animation['animations'].items():
  animators={}
  for name,tracks in anim['bones'].items():
   animators[ident('bone/'+name)]={'name':name,'type':'bone','keyframes':[{'channel':channel,'data_points':[{'x':v[0],'y':v[1],'z':v[2]}],'time':float(t),'color':-1,'interpolation':'linear','uuid':ident(clip+name+channel+t)} for channel,keys in tracks.items() for t,v in keys.items()]}
  project['animations'].append({'uuid':ident('animation/'+clip),'name':clip,'loop':'loop' if anim['loop'] else 'once','length':anim['animation_length'],'animators':animators})
 (OUT/'orpheus.bbmodel').write_text(json.dumps(project,indent=2)+'\n',encoding='utf-8')

def build_geometry():
 m=Model();B=m.bone;C=m.cube;L=m.segment
 B('orpheus');B('hips','orpheus',(0,22,0));B('body','hips',(0,24,0))
 # A turquoise hourglass torso with an exposed cross-shaped resonator.
 C('hips','pelvis_center',(-2.75,20.3,-1.75),(5.5,3.25,3.5),'teal','plate')
 C('hips','pelvis_upper',(-2.25,23,-1.5),(4.5,1.5,3),'teal','plate')
 C('hips','pelvis_lower',(-2,19.8,-1.5),(4,1,3),'teal')
 C('body','abdominal_shell',(-2.4,24,-1.75),(4.75,6.25,3.5),'teal','plate')
 C('body','ribcage',(-3.3,30,-1.98),(6.5,3.5,3.75),'teal','plate')
 C('body','breastplate',(-4,33,-2.12),(8,3,4),'teal','plate')
 for s,label in [(-1,'right'),(1,'left')]:
  C('body',label+'_chest_bevel',(s*3.3-.65,31,-1.8),(1.25,4.5,3.5),'teal','plate',(0,0,-s*16),(s*3.2,35,0))
  C('body',label+'_speaker_vent',(s*2.5-1,33.3,-2.18),(2,2.5,.25),'teal','vent',(0,0,s*12),(s*2.5,34.3,-2.18))
  L('body',label+'_ivory_armhole',(s*3.7,32.5,-1.8),(s*4.1,35.8,-1.8),.5,.5,'ivory')
 C('body','resonator_dark_port',(-2,26,-2.05),(4,4,.25),'dark','port')
 for i in range(8):
  a=2*math.pi*i/8;b=2*math.pi*(i+1)/8
  L('body',f'resonator_rim_{i}',(2*math.cos(a),28+2*math.sin(a),-2.22),(2*math.cos(b),28+2*math.sin(b),-2.22),.5,.5,'metal')
 C('body','resonator_vertical_spoke',(-.25,26.1,-2.55),(.5,3.75,.5),'ivory')
 C('body','resonator_horizontal_spoke',(-1.875,27.75,-2.55),(3.75,.5,.5),'ivory')
 C('body','resonator_hub',(-.45,27.5,-2.75),(1,1,.5),'metal')
 C('body','neck_mechanism',(-.9,35.5,-.85),(1.75,3,1.75),'metal','seam')
 # Short layered scarf with two trailing pieces; all clips retain this red.
 B('scarf','body',(0,37.5,0))
 C('scarf','scarf_collar_back',(-2.1,36.8,.25),(4.25,2.75,1.25),'red','scarf')
 C('scarf','scarf_front_upper',(-2,36.9,-1.9),(4,1.5,1),'red','scarf',(0,0,-7),(0,37.7,-1.4))
 C('scarf','scarf_front_lower',(-2.25,35.8,-1.85),(4.5,1.5,1),'red','scarf',(0,0,6),(0,36.5,-1.4))
 for s,label in [(-1,'right'),(1,'left')]:C('scarf',label+'_scarf_wrap',(s*1.85-.5,36.4,-1.2),(1,2.25,2),'red','scarf',(0,0,s*10),(s*1.8,37,0))
 B('scarf_tail','scarf',(-1.7,37.5,1.4))
 C('scarf_tail','scarf_tail_main',(-3.3,31.5,1.7),(1.75,6.25,.5),'red','scarf',(-13,0,-15),(-1.7,37.5,1.4))
 B('scarf_tip','scarf_tail',(-3,32,2.8))
 C('scarf_tip','scarf_tail_tip',(-3.8,29.5,2.75),(1.25,3,.5),'red','scarf',(-10,0,12),(-3,32,2.8))
 # Gray human face, a visible red eye and long asymmetric white bangs.
 B('head','body',(0,38.2,0))
 C('head','face_core',(-1.7,39,-1.35),(3.5,4.75,2.75),'skin')
 C('head','jaw',(-1.3,38.3,-1.15),(2.5,1.5,2.5),'skin')
 C('head','chin',(-.85,38,-1.03),(1.75,.75,1.5),'skin')
 for s,label in [(-1,'right'),(1,'left')]:
  C('head',label+'_face_bevel',(s*1.5-.4,39,-1),( .75,3.5,2),'skin',rotation=(0,s*15,-s*7),pivot=(s*1.4,41,0))
  C('head',label+'_ear',(s*1.9-.3,40,-.1),(.5,1.25,.75),'skin')
  C('head',label+'_red_eye',(s*.92-.5,41.15,-1.61),(1,.75,.25),'eye','eye')
 C('head','nose_bridge',(-.2,39.8,-1.6),(.5,1.25,.5),'skin')
 C('head','mouth',(-.5,39.25,-1.53),(1,.25,.25),'dark')
 B('hair','head',(0,43,0))
 C('hair','hair_crown',(-1.7,43.15,-1.2),(3.5,1.75,2.75),'hair','hair')
 C('hair','hair_top',(-1.2,44.5,-.9),(2.5,.75,2.5),'hair')
 C('hair','hair_back',(-1.85,39.4,1),(3.75,4.75,1.25),'hair','hair')
 for i,(x,y,length,ang) in enumerate([(-1.8,43.6,4.25,-14),(-1,44.1,4.5,-22),(-.2,44.4,4.75,-27),(.65,44.25,3.25,-31),(1.4,43.75,2,-34)]):
  C('hair',f'fringe_{i}',(x-.5,y-length,-1.95-i*.018),(1,length,.75),'hair','hair',(0,0,ang),(x,y,-1.65))
  C('hair',f'fringe_tip_{i}',(x-.3,y-length-.5,-1.98-i*.018),(.5,.75,.5),'hair','hair',(0,0,ang),(x,y,-1.65))
 for s,label in [(-1,'right'),(1,'left')]:
  for i in range(3):
   C('hair',f'{label}_side_lock_{i}',(s*(1.95+i*.14)-.375,38.7+i*.3,-.8+i*.65),(.75,4.75-i*.25,.75),'hair','hair',(-i*3,0,s*(9+i*7)),(s*1.7,43,-.3+i*.65))
 B('hair_tuft','hair',(0,44.4,.8))
 C('hair_tuft','swept_top_lock',(-1,44.4,.5),(2,1.5,1),'hair','hair',(-28,0,-20),(0,44.4,.8))
 # Mechanical ball joints are stepped cuboids, not smooth mesh spheres.
 def joint(bone,name,x,y,z,w=2):
  C(bone,name+'_middle',(x-w/2,y-.75,z-w/2),(w,1.5,w),'metal','plate')
  for s,label in [(-1,'lower'),(1,'upper')]:C(bone,name+'_'+label,(x-(w-.5)/2,y+s*.875-.25,z-(w-.5)/2),(w-.5,.5,w-.5),'metal')
 for s,label in [(-1,'right'),(1,'left')]:
  x=s*5;B(label+'_upper_arm','body',(x,35.5,0))
  joint(label+'_upper_arm',label+'_shoulder_joint',x,35,0,2)
  C(label+'_upper_arm',label+'_pauldron_top',(x-1.6,34.9,-1.5),(3.25,1.5,3),'ivory','plate',(0,0,s*13),(x,35.5,0))
  C(label+'_upper_arm',label+'_pauldron_middle',(x-1.5,33.4,-1.4),(3,1.5,2.75),'ivory','plate')
  C(label+'_upper_arm',label+'_pauldron_lower',(x-1.25,32.4,-1.25),(2.5,1,2.5),'ivory','plate')
  L(label+'_upper_arm',label+'_humerus',(x,28.5,0),(x,32.8,0),1,1,'metal','seam')
  for offset in [-.6,.6]:L(label+'_upper_arm',label+'_piston_'+str(offset),(x+offset,28.8,.25),(x+offset,32.7,.25),.25,.5,'ivory')
  B(label+'_forearm',label+'_upper_arm',(x,28.5,0));joint(label+'_forearm',label+'_elbow',x,28.5,0,1.75)
  C(label+'_forearm',label+'_forearm_upper',(x-1,24.9,-1),(2,2.75,2),'ivory','plate')
  C(label+'_forearm',label+'_forearm_lower',(x-.8,21.4,-.8),(1.5,3.75,1.5),'ivory','seam')
  C(label+'_forearm',label+'_wrist_band',(x-.9,21.2,-.9),(1.75,.5,1.75),'metal')
  B(label+'_hand',label+'_forearm',(x,21,0))
  C(label+'_hand',label+'_palm',(x-.9,19.4,-.45),(1.75,1.75,1),'ivory','plate')
  for i in range(4):
   fx=x-.7+i*.45;bone=f'{label}_finger_{i}';B(bone,label+'_hand',(fx,19.6,0))
   L(bone,bone+'_proximal',(fx,19.6,0),(fx+(i-1.5)*.08,18.1,-.12),.25,.5,'ivory')
   L(bone,bone+'_tip',(fx+(i-1.5)*.08,18.1,-.12),(fx+(i-1.5)*.05,17.75,-.5),.25,.25,'ivory')
  L(label+'_hand',label+'_thumb',(x-s*.7,20.5,0),(x-s*1.25,19.4,-.4),.5,.5,'ivory')
  x=s*3;B(label+'_thigh','hips',(x,22,0))
  joint(label+'_thigh',label+'_hip_joint',x,21.5,0,2.5)
  C(label+'_thigh',label+'_thigh_guard',(x-1.5,18.1,-1.35),(3,3.75,2.75),'ivory','plate',(0,0,-s*15),(x,21,0))
  C(label+'_thigh',label+'_outer_hip_flare',(x+s*.9-1,20.2,-1.45),(2,2,3),'ivory','plate',(0,0,s*28),(x,21.5,0))
  C(label+'_thigh',label+'_thigh_collar',(x-1.2,17.6,-1.2),(2.5,1.25,2.5),'metal','plate')
  L(label+'_thigh',label+'_femur',(x,14.5,0),(x,18,0),1,1,'metal')
  B(label+'_shin',label+'_thigh',(x,14.5,0));joint(label+'_shin',label+'_knee',x,14.5,0,2.25)
  L(label+'_shin',label+'_knee_piston',(x,11.8,0),(x,14.1,0),1,1,'metal')
  C(label+'_shin',label+'_boot_upper',(x-1,8,-.9),(2,4.5,2),'ivory','boot')
  C(label+'_shin',label+'_boot_lower',(x-1.35,3,-1.05),(2.75,5.25,2.5),'ivory','boot')
  for side in [-1,1]:C(label+'_shin',f'{label}_greave_edge_{side}',(x+side*.9-.25,3,-1.18),(.5,9.25,.5),'ivory','boot',(0,0,-side*3),(x+side*.9,12,0))
  B(label+'_foot',label+'_shin',(x,3,0))
  C(label+'_foot',label+'_foot',(x-1.35,1,-2.15),(2.75,2.25,3.5),'ivory','plate')
  C(label+'_foot',label+'_toe',(x-1.2,.5,-3.3),(2.5,1.25,2),'ivory','plate')
  C(label+'_foot',label+'_sole',(x-1.35,.25,-2.9),(2.75,.5,4),'metal')
 # Independent lyre moves as one rigid instrument from the back to the hands.
 B('lyre','orpheus',(0,32,4))
 C('lyre','lyre_soundbox',(-3.25,17,3),(6.5,4.25,2),'ivory','grille')
 C('lyre','lyre_bridge',(-3.75,21,3.5),(7.5,.75,1.5),'metal')
 C('lyre','lyre_base_rim',(-3.5,16.75,2.75),(7,.5,2.5),'metal')
 for s,label in [(-1,'right'),(1,'left')]:
  pts=[(s*3,20,4),(s*6.7,20.5,4),(s*8.2,23,4),(s*8.3,26,4),(s*6.8,34,4),(s*6.3,39.5,4),(s*7.5,42,4)]
  for j,(a,b) in enumerate(zip(pts,pts[1:])):L('lyre',f'{label}_lyre_frame_{j}',a,b,1.5,1.25,'ivory','frame')
  # Volutes are angular scrolls built from short boxes on the same integer grid.
  for center,tag,radius in [((s*7.5,42,4),'upper',1.55),((s*5.8,21,4),'lower',1.2)]:
   for j in range(7):
    a=j*math.pi/4;b=(j+1)*math.pi/4
    L('lyre',f'{label}_{tag}_scroll_{j}',(center[0]+radius*math.cos(a),center[1]+radius*math.sin(a),3.8),(center[0]+radius*math.cos(b),center[1]+radius*math.sin(b),3.8),.75,1,'ivory')
   C('lyre',f'{label}_{tag}_scroll_center',(center[0]-.7,center[1]-.7,3.15),(1.5,1.5,.5),'metal','plate')
  L('lyre',label+'_upper_arch',(s*7,42.8,4),(s*1.2,45,4),1,1,'ivory','frame')
 C('lyre','lyre_top_bar',(-4,44.3,3.6),(8,.75,1),'metal')
 B('lyre_strings','lyre',(0,32,3.7))
 for i in range(7):
  x=-2.7+i*.9;L('lyre_strings',f'lyre_string_{i}',(x,21.6,3.65),(x,44.5,3.65),.25,.25,'metal')
 # Fan-shaped scallop crest above the white hair.
 for i in range(7):
  ang=(i-3)*13;end=(math.sin(math.radians(ang))*(4.8-abs(i-3)*.3),46+math.cos(math.radians(ang))*(4.8-abs(i-3)*.3),4)
  end=(end[0],end[1],4+i*.035)
  L('lyre',f'crest_flute_{i}',(0,45.2,4+i*.035),end,1.25,.75,'ivory','seam')
  C('lyre',f'crest_cap_{i}',(end[0]-.4,end[1]-.15,3.55+i*.035),(.75,.75,.75),'ivory')
 return m

def interp(t,times,values):return np.array([np.interp(t,times,[v[j] for v in values]) for j in range(3)],float)

def pose(kind,t):
 p={};pos={'orpheus':[0,0,0],'lyre':[0,0,0]};body=np.zeros(3)
 hand={'right':np.array([-6.5,21.5,-.75]),'left':np.array([6.5,21.5,-.75])}
 grip=0;lyre_center=np.array([0.,32.,4.]);lyre_rot=np.zeros(3);rise=0
 if kind=='idle':
  a=math.sin(t*2*math.pi/4.8);b=math.sin(t*2*math.pi/4.8+.6)-math.sin(.6)
  body=np.array([a*.5,0,b*.35]);p['scarf_tail']=[b*1.2,0,a*.9];p['scarf_tip']=[b*1.8,0,a*1.1];p['hair_tuft']=[a*.8,0,0]
  lyre_rot=np.array([a*.35,0,b*.25])
 elif kind=='magic':
  times=[0,.4,.8,1.2,1.6,2.1,2.6,3.2]
  lyre_center=interp(t,times,[(0,32,4),(9,34,3),(10,31,-5),(4,29,-8.5),(4,29,-8.5),(4,29,-8.5),(9,34,3),(0,32,4)])
  lyre_rot=interp(t,times,[(0,0,0),(0,0,-45),(-8,-6,-40),(-12,-10,-47),(-12,-10,-47),(-12,-10,-47),(0,0,-45),(0,0,0)])
  grip=float(np.interp(t,times,[0,.7,1,1,1,1,.7,0]));body=np.array([-10*grip,6*grip,-10*grip])
  p['scarf_tail']=[-8*grip,4*grip,0];p['scarf_tip']=[-5*grip,0,4*grip];p['hair_tuft']=[-3*grip,0,0]
 elif kind=='physical':
  times=[0,.35,.7,1.05,1.3,1.65,2.25,2.8]
  lyre_center=interp(t,times,[(0,32,4),(9,37,3),(12,36,-4),(12,32,-6),(10,20,-6),(10,21,-6),(9,37,3),(0,32,4)])
  lyre_rot=interp(t,times,[(0,0,0),(0,0,-45),(5,-12,-82),(10,-18,-105),(18,-22,-142),(16,-20,-136),(0,0,-45),(0,0,0)])
  grip=float(np.interp(t,times,[0,.7,1,1,1,1,.7,0]));rise=float(np.interp(t,times,[0,0,2,4,3,1,0,0]));body=np.array([-16*grip,0,-6*grip])
  p['scarf_tail']=[-14*grip,0,7*grip];p['scarf_tip']=[-12*grip,0,4*grip];p['hair_tuft']=[-5*grip,0,0]
 p['body']=body;p['head']=[-body[0]*.6,0,-body[2]*.35]
 p['lyre']=lyre_rot;pos['lyre']=list(lyre_center-np.array([0,32,4]));pos['orpheus']=[0,rise,0]
 lr=matrix(lyre_rot);br=matrix(body);bodypivot=np.array([0,24,0])
 for label,s in [('right',-1),('left',1)]:
  hand_matrix=np.eye(3)
  if kind!='idle':
   local=np.array([s*2.,23.,4.])
   if kind=='magic' and label=='right':
    brush=math.sin((t-1.15)*math.pi*3)*.9 if 1.15<=t<=2.15 else 0
    local=np.array([-1.25+brush,31.,2.65])
   elif kind=='magic':
    # Support the side rail while the lyre is held below the face and chest.
    local=np.array([7.,34.,4.])
   target=lyre_center+lr@(local-np.array([0,32,4]))
   bodylocal=bodypivot+br.T@(target-bodypivot)
   hand[label]=hand[label]*(1-grip)+bodylocal*grip
   hand_matrix=matrix(euler(br.T@lr)*grip)
  a,b,r,_=ik((s*5,35.5,0),hand[label],(s*12,29,-1),7,7.5,hinge=True)
  p[label+'_upper_arm']=a;p[label+'_forearm']=b;p[label+'_hand']=euler(r.T@hand_matrix)
  for i in range(4):p[f'{label}_finger_{i}']=[45*grip if kind!='magic' or label=='left' else 12*grip,0,0]
  ankle=np.array([s*6,3.3,0.])
  if kind=='physical':ankle+=np.array([s*rise*.8,rise*.5,(-1 if label=='right' else 1)*rise*.6])
  a,b,r,_=ik((s*3,22,0),ankle,(s*5,14,-3),7.5,11.5)
  p[label+'_thigh']=a;p[label+'_shin']=b;p[label+'_foot']=euler(r.T@matrix([rise*3,0,0]))
 return p,pos

def animate(m):
 rest,restpos=pose('idle',0)
 for b in m.bones:
  if b['name'] in rest:b['rotation']=[round(float(v),6) for v in rest[b['name']]]
 clips={}
 for kind,length,loop in [('idle',4.8,True),('magic',3.2,False),('physical',2.8,False)]:
  extra=[.4,.8,1.2,1.6,2.1,2.6] if kind=='magic' else [.35,.7,1.05,1.3,1.65,2.25] if kind=='physical' else []
  count=48 if loop else 320;times=sorted(set([round(i*length/count,6) for i in range(count+1)]+extra));tracks={};previous={}
  for t in times:
   p,ps=pose(kind,t)
   if t==length:p={k:list(v) for k,v in rest.items()};ps=restpos
   for bone,rot in p.items():
    v=np.array(rot,float)
    if bone in previous:
     variants=[v,np.array([v[0]+180,180-v[1],v[2]+180])];variants=[a+360*np.round((previous[bone]-a)/360) for a in variants]
     v=min(variants,key=lambda a:np.linalg.norm(a-previous[bone]))
    previous[bone]=v;delta=v-np.array(rest.get(bone,[0,0,0]))
    tracks.setdefault(bone,{}).setdefault('rotation',{})[str(t)]=[round(float(x),6) for x in delta]
   for bone,v in ps.items():tracks.setdefault(bone,{}).setdefault('position',{})[str(t)]=[round(float(x)*DENSITY,6) for x in v]
  clips['animation.orpheus.'+{'idle':'ground_idle','magic':'magic_attack','physical':'physical_attack'}[kind]]={'loop':loop,'animation_length':length,'bones':tracks}
 return {'format_version':'1.8.0','animations':clips}

def build():
 OUT.mkdir(parents=True,exist_ok=True);m=build_geometry();animation=animate(m)
 geo={'format_version':'1.12.0','minecraft:geometry':[{'description':{'identifier':'geometry.orpheus','texture_width':S,'texture_height':S,'visible_bounds_width':22,'visible_bounds_height':20,'visible_bounds_offset':[0,7,0]},'bones':m.bones}]}
 for fn,obj in [('orpheus.geo.json',geo),('orpheus.animation.json',animation),('parts.json',m.parts)]:
  (OUT/fn).write_text(json.dumps(obj,indent=2)+'\n',encoding='utf-8')
 m.atlas.save(OUT/'orpheus.png');export_blockbench(m,animation)
 stats={'cuboids':len(m.parts),'bones':len(m.bones),'atlas':[S,S],'atlas_height_used':m.y+m.row,'uniform_density':DENSITY,'clips':{k:v['animation_length'] for k,v in animation['animations'].items()},'color_variants':1}
 (OUT/'model-info.json').write_text(json.dumps(stats,indent=2)+'\n',encoding='utf-8');print(json.dumps(stats))

if __name__=='__main__':build()
