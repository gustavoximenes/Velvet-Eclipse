"""Approved awakening choreography. Geometry is exported as cuboids + sampled bone matrices.
Runtime resolves the current player's skin; no player texture is exported.
Requires Python 3, numpy and Pillow. Run export_scene.py to regenerate.
"""
from pathlib import Path
import json, math, itertools
from functools import lru_cache
import numpy as np
from PIL import Image, ImageFont
ROOT=Path(__file__).resolve().parent
ASSET=ROOT.parents[1]/'src/main/resources/assets/velvet_eclipse'
W,H=960,540
CLIPS={k:{'seconds':v} for k,v in [('orpheus',18),('izanagi',11),('arsene',30.1)]}
CALL_HOLD=3.2
CONTRACT_START=33.2
def duration(name):return math.ceil((42+3.2+CLIPS[name]['seconds']-1.2)*12.5)/12.5
def visual_time(t,name):
 if 24.6<=t<27.8:return 24.6
 if t>=27.8:t-=CALL_HOLD
 if t>=30:
  length=CLIPS[name]['seconds']+1
  if t<30+length:return 30+(t-30)/length*2.2
  t-=CLIPS[name]['seconds']-1.2
 return t
FACES={'north':[0,2,6,4],'south':[1,5,7,3],'west':[0,1,3,2], 'east':[4,6,7,5],'up':[2,3,7,6],'down':[0,4,5,1]}
CORNERS=np.array(list(itertools.product([0,1],repeat=3)),dtype=float)
THEMES={
 'orpheus':{'name':'ORPHEUS','title':'A VOZ QUE DESPERTA','tag':'FOGO  /  CURA','color':(50,216,255),'dark':(3,15,57),'mid':(15,70,230),'spell':'AGI','word':'Dê voz à sua determinação.'},
 'izanagi':{'name':'IZANAGI','title':'A FORÇA DA VERDADE','tag':'ELETRICIDADE  /  CORTE','color':(255,222,48),'dark':(28,24,7),'mid':(155,111,4),'spell':'ZIO','word':'Encare o que está diante de você.'},
 'arsene':{'name':'ARSÈNE','title':'A CHAMA DA REBELDIA','tag':'MALDIÇÃO  /  FÍSICO','color':(255,45,70),'dark':(14,3,13),'mid':(153,5,36),'spell':'EIGA','word':'Recuse o destino que impuseram a você.'},
}

def smooth(t):
 t=np.clip(t,0,1);return float(t*t*(3-2*t))
def mix(a,b,t):return np.array(a)+(np.array(b)-np.array(a))*t
def T(v):
 m=np.eye(4);m[:3,3]=v;return m
def S(v):
 m=np.eye(4);m[:3,:3]*=v;return m
def R(v):
 x,y,z=np.radians(v);cx,sx=np.cos(x),np.sin(x);cy,sy=np.cos(y),np.sin(y);cz,sz=np.cos(z),np.sin(z)
 m=np.eye(4);m[:3,:3]=np.array([[cz,-sz,0],[sz,cz,0],[0,0,1]])@np.array([[cy,0,sy],[0,1,0],[-sy,0,cy]])@np.array([[1,0,0],[0,cx,-sx],[0,sx,cx]])
 return m
def uv_faces(u,v,w,h,d):
 return {'north':(u+d,v+d,w,h),'south':(u+2*d+w,v+d,w,h),'west':(u+d+w,v+d,d,h),'east':(u,v+d,d,h),'up':(u+d,v,w,d),'down':(u+d+w,v,w,d)}
def uv_quad(face,u,v,w,h):
 if face=='up':return np.array([[u,v],[u,v+h],[u+w,v+h],[u+w,v]])
 if face=='down':return np.array([[u,v],[u+w,v],[u+w,v+h],[u,v+h]])
 if face in ('north','east'):return np.array([[u,v+h],[u,v],[u+w,v],[u+w,v+h]])
 return np.array([[u,v+h],[u+w,v+h],[u+w,v],[u,v]])

class Model:
 def __init__(self,name):
  self.name=name
  self.data=json.loads((ASSET/'geo/entity'/f'{name}.geo.json').read_text())['minecraft:geometry'][0]
  self.anim=json.loads((ASSET/'animations/entity'/f'{name}.animation.json').read_text())['animations']
  # GeckoLib reverses X coordinates and X/Y Euler signs while loading Bedrock.
  # Undo that export compensation before using this Cartesian renderer, exactly
  # as the actual model loader does. Retimed animation keys remain unchanged.
  if name in ('orpheus','arsene','izanagi','field_shadow'):
   for b in self.data['bones']:
    b['pivot'][0]*=-1
    if 'rotation' in b:b['rotation']=[-b['rotation'][0],-b['rotation'][1],b['rotation'][2]]
    for c in b.get('cubes',[]):
     c['origin'][0]=-c['origin'][0]-c['size'][0]
     if 'pivot' in c:c['pivot'][0]*=-1
     if 'rotation' in c:c['rotation']=[-c['rotation'][0],-c['rotation'][1],c['rotation'][2]]
   for clip in self.anim.values():
    for tracks in clip.get('bones',{}).values():
     for channel,keys in tracks.items():
      for ts,v in keys.items():
       if channel=='rotation':keys[ts]=[-v[0],-v[1],v[2]]
       elif channel=='position':keys[ts]=[-v[0],v[1],v[2]]
  self.tex=np.array(Image.open(ASSET/'textures/entity'/f'{name}.png').convert('RGBA'))
  self.bones=self.data['bones'];self.cubes={}
  for b in self.bones:
   cubes=[]
   for c in b.get('cubes',[]):
    vs=np.c_[np.array(c['origin'])+CORNERS*np.array(c['size']),np.ones(8)]
    p=c.get('pivot',[0,0,0]);vs=(T(p)@R(c.get('rotation',[0,0,0]))@T(-np.array(p))@vs.T).T
    uvs=[]
    uv=c.get('uv',[0,0])
    for face,ids in FACES.items():
     if isinstance(uv,dict):
      fuv=uv.get(face,{});u,v=fuv.get('uv',[0,0]);w,h=fuv.get('uv_size',[0,0])
     else:u,v,w,h=uv_faces(*uv,*c['size'])[face]
     if not w or not h:continue
     uvs.append((ids,uv_quad(face,u,v,w,h)))
    cubes.append((vs,uvs))
   self.cubes[b['name']]=cubes
  self.keys={}
  for clip,a in self.anim.items():
   for bn,ch in a.get('bones',{}).items():
    for channel,keys in ch.items():
     if isinstance(keys,dict):
      pairs=[]
      for ts,val in keys.items():
       if isinstance(val,dict):val=val.get('post',val.get('pre',[0,0,0]))
       pairs.append((float(ts),np.array(val,dtype=float)))
      self.keys[(clip,bn,channel)]=sorted(pairs,key=lambda p:p[0])
     elif isinstance(keys,list):self.keys[(clip,bn,channel)]=[(0,np.array(keys,dtype=float))]
 def sample(self,clip,bone,channel,t):
  pairs=self.keys.get((clip,bone,channel))
  if not pairs:return np.ones(3) if channel=='scale' else np.zeros(3)
  a=self.anim[clip];length=a.get('animation_length',1)
  t=t%length if a.get('loop',False) else np.clip(t,0,length)
  for (ta,va),(tb,vb) in zip(pairs,pairs[1:]):
   if ta<=t<=tb:return mix(va,vb,(t-ta)/(tb-ta))
  return pairs[0][1] if t<pairs[0][0] else pairs[-1][1]
 def faces(self,t=0,clip='misc.idle',transform=None):
  root=np.eye(4) if transform is None else transform
  mats={};faces=[]
  for b in self.bones:
   n=b['name'];p=np.array(b['pivot'])
   m=mats.get(b.get('parent'),root)@T(self.sample(clip,n,'position',t))@T(p)@R(np.array(b.get('rotation',[0,0,0]))+self.sample(clip,n,'rotation',t))@S(self.sample(clip,n,'scale',t))@T(-p)
   mats[n]=m
   for vs,uvs in self.cubes[n]:
    q=(m@vs.T).T[:,:3]
    for ids,uv in uvs:faces.append((q[ids],uv,self.tex,None))
  return faces

@lru_cache(None)
def model(name):return Model(name)
@lru_cache(None)
def font(sz,bold=False):
 return ImageFont.truetype(str(ASSET/'font'/('display.ttf' if bold else 'body.ttf')),sz)

def box(center,size,color,mat=None):
 v=np.array(center)+ (CORNERS-.5)*np.array(size)
 if mat is not None:v=(mat@np.c_[v,np.ones(8)].T).T[:,:3]
 return [(v[ids],None,None,np.array(color,dtype=float)) for ids in FACES.values()]

def limb(a,b,width,depth,color,mat=None):
 a=np.array(a);b=np.array(b);y=b-a;length=np.linalg.norm(y);y/=length
 x=np.cross(y,[0,0,1]);x/=np.linalg.norm(x);z=np.cross(x,y)
 basis=np.eye(4);basis[:3,:3]=np.stack([x,y,z],axis=1);basis[:3,3]=(a+b)*.5
 if mat is not None:basis=mat@basis
 return box((0,0,0),(width,length,depth),color,basis)

@lru_cache(None)
def player_skin():
 return np.zeros((64,64,4),dtype=np.uint8),True

def skin_cube(center,pixels,uv,mat,inflate=0,row_start=0,row_end=None):
 tex,_=player_skin();w,h,d=pixels
 if row_end is None:row_end=h
 size=np.array([w,row_end-row_start,d])/16+inflate*2
 v=np.array(center)+(CORNERS-.5)*size
 v=(mat@np.c_[v,np.ones(8)].T).T[:,:3]
 out=[]
 for face,ids in FACES.items():
  u0,v0,uw,vh=uv_faces(*uv,w,h,d)[face]
  if face not in ('up','down'):v0+=row_start;vh=row_end-row_start
  out.append((v[ids],uv_quad(face,u0,v0,uw,vh),tex,None))
 return out

def skin_limb(a,b,width,uv,overlay,mat,row_start=0,row_end=12):
 a=np.array(a);b=np.array(b);up=(a-b)/np.linalg.norm(a-b)
 right=np.cross(up,[0,0,1]);right/=np.linalg.norm(right)
 front=np.cross(right,up)
 basis=np.eye(4);basis[:3,:3]=np.stack([right,up,front],axis=1);basis[:3,3]=(a+b)*.5
 basis=mat@basis
 return skin_cube((0,0,0),(width,12,4),uv,basis,row_start=row_start,row_end=row_end)+skin_cube((0,0,0),(width,12,4),overlay,basis,.009,row_start,row_end)

def draw_pose(a,recoil=0):
 a=float(np.clip(a,0,1));_,slim=player_skin();sx=-(.25+(3 if slim else 4)/32)
 shoulder=np.array([sx,1.46,0.])
 # First draw away from the upper arm, with the muzzle down. Only rotate inward
 # after the wrist has cleared the shoulder. IK keeps both bone lengths fixed.
 if a<.52:hand=mix((-.46,.80,-.23),(-.90,1.13,-.24),smooth(a/.52))
 else:hand=mix((-.90,1.13,-.24),(-.690,1.610,-.015),smooth((a-.52)/.48))
 hand+=np.array([-recoil*.025,0,0])
 delta=hand-shoulder;distance=np.linalg.norm(delta);axis=delta/distance
 bend=np.array([-1.,-.65,.85]);bend-=axis*np.dot(bend,axis);bend/=np.linalg.norm(bend)
 elbow=(shoulder+hand)*.5+bend*math.sqrt(max(0,.375**2-(distance*.5)**2))
 # The muzzle sweeps forward in free space, then turns toward the temple.
 # A flat sideways wrist rotation would intersect the bent forearm.
 yaw=-180*smooth(a)
 angle=84*(1-smooth(a/.56))+recoil*4
 palm=hand+(hand-elbow)/.375*.105
 gun=T(palm)@R((0,yaw,angle))@S(.115)
 return shoulder,elbow,hand,gun

def player(t,raise_amount=0,recoil=0,yaw=0,pos=(-1.7,0,0),walk=0,hero=False):
 """Minecraft skin, classic/slim arms and second layers; no invented avatar."""
 tex,slim=player_skin();armw=3 if slim else 4
 base=T(pos)@R((0,yaw,0))@T((0,math.sin(t*2.5)*.009,0));out=[]
 for side,uv,overlay in [(-1,(0,16),(0,32)),(1,(16,48),(0,48))]:
  leg=base@T((side*.125,.75,0))@R((math.sin(t*7+side*math.pi*.5)*walk*16,0,0))@T((-side*.125,-.75,0))
  out+=skin_cube((side*.125,.375,0),(4,12,4),uv,leg)
  out+=skin_cube((side*.125,.375,0),(4,12,4),overlay,leg,.009)
 out+=skin_cube((0,1.125,0),(8,12,4),(16,16),base)
 out+=skin_cube((0,1.125,0),(8,12,4),(16,32),base,.009)
 hm=base@T((0,1.5,0))@R((-3*raise_amount+recoil*5,-12+12*raise_amount,0))@T((0,-1.5,0))
 out+=skin_cube((0,1.75,0),(8,8,8),(0,0),hm)
 out+=skin_cube((0,1.75,0),(8,8,8),(32,0),hm,.015625)
 sx=.25+armw/32
 out+=skin_limb((sx,1.5,0),(sx,.75,0),armw,(32,48),(48,48),base)
 shoulder,elbow,hand,gun=draw_pose(raise_amount,recoil)
 out+=skin_limb(shoulder,elbow,armw,(40,16),(40,32),base,0,6)
 out+=skin_limb(elbow,hand,armw,(40,16),(40,32),base,6,12)
 out+=model('evoker_fx').faces(t,'misc.idle',base@gun)
 return out


class Camera:
 def __init__(self,eye,target,fov=44,roll=0):
  self.eye=np.array(eye,dtype=float);front=np.array(target)-self.eye;front/=np.linalg.norm(front)
  right=np.cross(front,[0,1,0]);right/=np.linalg.norm(right);up=np.cross(right,front)
  self.axes=np.stack([right,up,front]);self.f=H/(2*math.tan(math.radians(fov)/2));self.roll=math.radians(roll)
 def project(self,points):
  p=(np.array(points)-self.eye)@self.axes.T
  z=p[...,2];x=p[...,0]*self.f/np.maximum(.05,z);y=-p[...,1]*self.f/np.maximum(.05,z)
  c,s=math.cos(self.roll),math.sin(self.roll)
  return np.stack([W/2+c*x-s*y,H/2+s*x+c*y,z],axis=-1)

def persona_faces(name,t=0,clip='misc.idle',pos=(-1.7,0,1.65),yaw=0,scale=1):
 # Match native renderer relative proportions; cinematic world scale is explicit.
 s={'orpheus':.16,'izanagi':.25,'arsene':.125}[name]/16*1.9*scale
 return model(name).faces(t,clip,T(pos)@R((0,yaw,0))@S(s))

def scene(t,name):
 real_t=t;t=visual_time(real_t,name)
 th=THEMES[name];color=th['color'];flash=0;faces=[];persona=[];caption_text=None;chapter=''
 if 16<=t<23:t=15.999
 # Deliberate shot list, shared world coordinates preserve spatial continuity.
 if t<2.4:
  cam=Camera((7.8,4.6,-11.8),(-.2,1.2,-1.8),47);chapter='dark hour'
 elif t<5.6:
  a=smooth((t-2.4)/3.2);cam=Camera(mix((6.7,2.4,-8),(5.3,2.2,-7),a),(-.45,1,-2.8),48);chapter='approach'
 elif t<8.4:
  a=smooth((t-5.6)/2.8);cam=Camera(mix((2.65,.76,-2.0),(2.2,.7,-2.5),a),(1.0,.8,-5.65),48,roll=-3);chapter='shadow'
 elif t<11.8:
  a=smooth((t-8.4)/3.4);cam=Camera(mix((-4.65,1.54,-4.5),(-3.25,1.63,-3.0),a),(-1.83,1.3,0),38);chapter='raise'
 elif t<16:
  a=smooth((t-11.8)/3.8);cam=Camera(mix((-3.25,1.88,-2.4),(-2.55,1.86,-1.85),a),(-1.85,1.68,-.01),33);chapter='decision'
 elif t<26.2:
  cam=Camera((-2.82,1.82,-2.15),(-1.88,1.64,0),36);chapter='call'
 elif t<30:
  a=smooth((t-26.2)/3.8);cam=Camera(mix((3.8,1.52,-7.3),(2.6,2.2,-8.2),a),(-1.6,1.65,1.0),43);chapter='summon'
 elif t<32.2:
  elapsed=real_t-CONTRACT_START;a=elapsed/(CLIPS[name]['seconds']+1)
  if elapsed<5.5:cam=Camera(mix((1,2.4,-5.8),(.25,2.6,-5.6),smooth(elapsed/5.5)),(-1.7,2.04,1.45),40)
  elif elapsed<13:cam=Camera(mix((-4.9,2.8,-4.6),(-4.2,2.6,-4.8),smooth((elapsed-5.5)/7.5)),(-1.7,2.35,1.4),39)
  elif elapsed<22:cam=Camera(mix((2.2,2.4,-6.7),(1.6,2.65,-6.6),smooth((elapsed-13)/9)),(-1.7,2.0,1.25),42)
  else:cam=Camera(mix((.7,2.7,-5.7),(-.4,2.7,-5.6),smooth((elapsed-22)/10)),(-1.7,2.1,1.4),40)
  chapter='contract'
 elif t<34.7:
  cam=Camera((3.5,2.4,-5.6),(-1.6,1.65,.5),47,-2);chapter='windup'
 elif t<37.2:
  a=smooth((t-34.7)/2.5);cam=Camera(mix((6.1,2.8,-8.9),(6.5,3.0,-9.3),a),(0,.95,-2.5),49,-2);chapter='impact'
 else:
  a=smooth((t-37.2)/4.8);cam=Camera(mix((3.7,2.5,-7.7),(2.6,2.8,-8.6),a),(-1.6,1.65,.9),44);chapter='resolve'
 im=None
 if t<16:
  raised=smooth((t-8.8)/4.0)
  faces+=player(real_t,raised)
  z=mix(-8.4,-5.2,smooth((t-2.4)/7.0))
  # Original crawling Shadow rig, facing the player rather than its camera.
  faces+=model('field_shadow').faces(t-7.3 if 7.3<t<8.4 else t,'action.attack' if 7.3<t<8.4 else 'misc.idle',T((1.0,0,float(z)))@R((0,157,0))@S(.48/16*1.22))
 elif t<26.2:
  a=1-smooth((t-25.2)/1.8);recoil=math.sin(np.clip((t-24.6)/.45,0,1)*math.pi)
  faces+=player(real_t,a,recoil,hero=t>24.5)
  if 23.35<t<24.7:caption_text='Persona!'
  flash=max(0,1-abs(t-24.88)/.23)*.9
 else:
  faces+=player(real_t,1-smooth((t-25.2)/2.0),hero=True)
  rise=smooth((t-26.2)/1.9)
  pos=(-1.7,0,1.65)
  clip='action.magic' if 32.2<=t<34.7 else 'misc.idle'
  at=t-32.2 if clip=='action.magic' else real_t-29.4
  persona=persona_faces(name,at,clip,pos=pos,yaw=-7)
  if t<37.2:
   # Hit recoil and sinking/dissolution; no enemy pop-off before impact.
   hit=smooth((t-35.5)/1.6)
   faces+=model('field_shadow').faces(t,'misc.idle',T((1.0,-hit*1.3,-5.2-hit*.2))@R((hit*12,157,0))@S(.48/16*1.22*(1-.35*hit)))

 return cam,faces+persona
