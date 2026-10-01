"""Export approved geometry and rigid bone tracks (not rendered video frames).
Run with Python + numpy/Pillow. This also exports the room's world decoration mesh.
"""
from pathlib import Path
import gzip,struct,math,json,sys
import numpy as np
from PIL import Image,ImageDraw
import choreography as c
OUT=c.ASSET/'room';OUT.mkdir(parents=True,exist_ok=True)
T,R,S=c.T,c.R,c.S
meshes={};current={};colors={};FPS=30

def color_uv(color):
 color=tuple(int(np.clip(v,0,255)) for v in color[:3])
 if color not in colors:colors[color]=len(colors)
 n=colors[color];assert n<4096
 return np.array([(n%64+.5)/64,(n//64+.5)/64])
def mesh(key,tex,vertices,matrix,variant=0,slot=-1):
 if key not in meshes:meshes[key]=(tex,np.asarray(vertices,dtype='>f4'),variant,slot)
 current[key]=np.asarray(matrix,dtype='>f4')
def emit_faces(key,faces,mat=None,tex='velvet_room_palette',variant=0,slot=-1):
 if key in meshes:
  current[key]=np.eye(4) if mat is None else mat
  return
 verts=[]
 for p,uv,image,col in faces:
  coords=np.repeat(color_uv(col)[None,:],4,axis=0) if uv is None else uv/np.array([image.shape[1],image.shape[0]])
  verts.extend(np.c_[p,coords])
  if uv is None:verts.extend(np.c_[p[::-1],coords[::-1]])
 if verts:mesh(key,tex,verts,np.eye(4) if mat is None else mat,variant,slot)
def line_faces(points,color,width=.012):
 out=[]
 for a,z in zip(points,points[1:]):
  a=np.array(a);z=np.array(z);d=z-a
  if np.linalg.norm(d)<1e-8:continue
  side=np.cross(d,[0,0,1])
  if np.linalg.norm(side)<1e-6:side=np.cross(d,[0,1,0])
  side=side/np.linalg.norm(side)*width/2
  out+=c.face([a-side,z-side,z+side,a+side],color)
 return out

def room_geometry(furniture=True):
 out=[]
 def poly(draw,cam,points,fill):
  if len(points)==4:out.extend(c.face(points,fill[:3]))
 def line(draw,cam,points,fill,width=1):out.extend(line_faces(points,fill,width*.006))
 oldp,oldl=c.poly3,c.line3;c.poly3=poly;c.line3=line
 try:c.room_background(c.b.Camera((0,2,-4),(0,1,2)))
 finally:c.poly3, c.line3=oldp,oldl
 return out+(c.furniture() if furniture else [])
ROOM=room_geometry()
REVEAL=room_geometry(False)
for x in [-1.55,1.55]:REVEAL+=line_faces([(x,0,3.87),(x,3.6,3.87),(x*.76,3.91,3.87)],(159,171,231),.024)
for x in np.linspace(-1.5,1.5,17):REVEAL+=line_faces([(x,0,3.9),(x,3.62,3.9)],(85,104,179),.012)
for y in [.08,.17,3.51,3.64]:REVEAL+=line_faces([(-1.55,y,3.87),(1.55,y,3.87)],(118,136,213),.012)
center=np.array([0,3.83,4.05])
for radius,color in [(.63,(108,124,198)),(.56,(76,85,159)),(.43,(63,74,143))]:
 for a,z in zip(np.linspace(0,math.tau,90)[:-1],np.linspace(0,math.tau,90)[1:]):
  REVEAL+=c.face([center,center+[math.cos(a)*radius,math.sin(a)*radius,0],center+[math.cos(z)*radius,math.sin(z)*radius,0],center],color)
 center[2]-=.002
for i in range(12):
 a=i*math.tau/12;REVEAL+=line_faces([center+[math.sin(a)*.45,math.cos(a)*.45,-.01],center+[math.sin(a)*.53,math.cos(a)*.53,-.01]],(174,184,215),.012)
REVEAL+=line_faces([center+[-.19,.09,-.02],center+[0,0,-.02],center+[0,.31,-.02]],(184,201,241),.018)

def native_actor(name,t,root,pose=None):
 m=c.model(name);mats={}
 for bone in m.bones:
  key=bone['name'];pivot=np.array(bone['pivot'])
  mat=pose[key] if pose else mats.get(bone.get('parent'),root)@T(m.sample('misc.idle',key,'position',t))@T(pivot)@R(np.array(bone.get('rotation',[0,0,0]))+m.sample('misc.idle',key,'rotation',t))@T(-pivot)
  mats[key]=mat
  verts=[]
  for vs,uvs in m.cubes[key]:
   for ids,uv in uvs:verts.extend(np.c_[vs[ids,:3],uv/np.array([m.tex.shape[1],m.tex.shape[0]])])
  if verts:mesh(name+'/'+key,name,verts,mat)

def skin_cube(key,center,pixels,uv,mat,variant,slot,inflate=0,row_start=0,row_end=None):
 w,h,d=pixels;row_end=h if row_end is None else row_end;size=np.array([w,row_end-row_start,d])/16+inflate*2
 v=np.array(center)+(c.b.CORNERS-.5)*size;verts=[]
 for face,ids in c.b.FACES.items():
  u0,v0,uw,vh=c.b.uv_faces(*uv,w,h,d)[face]
  if face not in ('up','down'):v0+=row_start;vh=row_end-row_start
  verts.extend(np.c_[v[ids],c.b.uv_quad(face,u0,v0,uw,vh)/64])
 mesh(f'player/{variant}/{key}','player',verts,mat,variant,slot)

def limb(key,a,z,uv,overlay,mat,variant,slot,start,end):
 a=np.array(a);z=np.array(z);up=(a-z)/np.linalg.norm(a-z);right=np.cross(up,[0,0,1]);right/=np.linalg.norm(right);front=np.cross(right,up)
 basis=np.eye(4);basis[:3,:3]=np.stack([right,up,front],axis=1);basis[:3,3]=(a+z)/2;basis=mat@basis
 for layer in range(2):skin_cube(key+str(layer),(0,0,0),(4,12,4),uv if layer==0 else overlay,basis,variant,-1 if layer==0 else slot,0 if layer==0 else .009,start,end)

def player(t,base):
 for variant,arm in [(1,4),(2,3)]:
  for layer in range(2):
   skin_cube('body'+str(layer),(0,1.025,0),(8,12,4),(16,16) if not layer else (16,32),base,variant,-1 if not layer else 5,layer*.009)
   skin_cube('head'+str(layer),(0,1.66,0),(8,8,8),(0,0) if not layer else (32,0),base@c.head_motion(t)@c.HEAD_REST,variant,-1 if not layer else 7,layer*.015625)
  for sign,uv,over,slot in [(-1,(0,16),(0,32),1),(1,(16,48),(0,48),3)]:
   knee=(sign*.125,.539,-.3582);foot=(sign*.125,.165,-.3855)
   limb('thigh'+str(sign),(sign*.125,.65,0),knee,uv,over,base,variant,slot,0,6)
   limb('shin'+str(sign),knee,foot,uv,over,base,variant,slot,6,12)
   auv=(40,16) if sign<0 else (32,48);ov=(40,32) if sign<0 else (48,48);aslot=11 if sign<0 else 9
   a=base@T((sign*(.25+arm/32),1.36,0))@R((-27,0,sign*4))
   for layer in range(2):skin_cube('arm'+str(sign)+str(layer),(0,-.375,0),(arm,12,4),auv if not layer else ov,a,variant,-1 if not layer else aslot,layer*.009)

def chair(base):
 f=c.b.box((0,.58,.06),(.86,.17,.72),(48,49,155),base)
 for x in [-.42,.42]:
  f+=c.b.box((x,.78,.4),(.095,1.53,.09),(62,65,171),base);f+=c.b.box((x,.29,-.28),(.09,.57,.09),(30,31,92),base)
 for x in [-.25,0,.25]:f+=c.b.box((x,1.07,.42),(.08,.94,.07),(39,66,182),base)
 return f+c.b.box((0,1.56,.4),(.92,.12,.12),(68,63,183),base)

def card_texture():
 im=Image.new('RGB',(256,384),'#0a154e');d=ImageDraw.Draw(im)
 for pad,color,width in [(3,'#d4bc70',5),(13,'#a0b9e1',2)]:d.rectangle((pad,pad,255-pad,383-pad),outline=color,width=width)
 for rad in [82,66,48]:d.ellipse((128-rad,192-rad,128+rad,192+rad),outline='#80aee9',width=2)
 d.polygon([(128,94),(188,192),(128,290),(68,192)],outline='#dcefff',width=4);d.ellipse((116,180,140,204),fill='#8aeafa')
 im.save(c.ASSET/'textures/entity/velvet_fusion_card.png')

def cards(t):
 if t>=5:return
 for i in range(6):
  pos,rotation,width=c.card_state(t,i%2)
  if i>=2:
   pos=pos.copy();pos[0]+=(1 if i%2 else -1)*(i//2)*.23*(1-c.ease(t,4,5))
  h=width*1.5;p=np.array([[-width/2,-h/2,0],[-width/2,h/2,0],[width/2,h/2,0],[width/2,-h/2,0]])
  # Unit vertices; all size changes are uniform in the card plane.
  uv=np.array([[1,1],[1,0],[0,0],[0,1]]);mat=T(pos)@R(rotation)@S(width/.48)
  if f'card/{i}' not in meshes:p*=.48/width
  mesh('card/'+str(i),'velvet_fusion_card',np.concatenate([np.c_[p,uv],np.c_[p[::-1],uv[::-1]]]),mat,0,100+i)

def effect_segment(key,a,z,width,color,cam,opacity=1):
 a=np.asarray(a);z=np.asarray(z);along=z-a;side=np.cross(along,cam.eye-a);norm=np.linalg.norm(side)
 if norm<1e-7:return
 side=side/norm*width;mat=np.eye(4);mat[:3,0]=side;mat[:3,1]=along;mat[:3,3]=a;mat[3,3]=opacity
 uv=color_uv(color);verts=[[-1,0,0,*uv],[1,0,0,*uv],[1,1,0,*uv],[-1,1,0,*uv]]
 mesh(key,'velvet_room_glow',verts,mat)

def effects(t,cam):
 if t<5:return
 for j,line in enumerate(c.ribbons(t)):
  for i in range(len(line)-1):effect_segment(f'ribbon/{j}/{i}',line[i],line[i+1],.008,[79,179,247],cam)
 for i in range(52):
  phase=(i*.754877666+(t-5)*.43)%1;angle=i*2.399+(t-5)*.65;spread=phase*.47*c.effect_strength(t)
  p=c.nape(t)+c.BACK*(phase*1.65*c.effect_strength(t))+c.SIDE*(math.cos(angle)*spread)+c.UP*(math.sin(angle)*spread*.8)
  effect_segment(f'spark/{i}',p,p+np.array([.01,.028+.035*(1-phase),0]),.01,[164,222,255],cam)

def entrance(t):
 travel=c.smooth((t-.85)/3.75);cam=c.b.Camera((.2+1.5*c.smooth((t-3.25)/1.25),1.25,-3+travel*17),(.1,1.15,22),68,-9+3*travel)
 if t<3.65:
  f=[]
  for z in range(-4,32,2):
   for x in range(-14,16,2):f+=c.face([(x,0,z),(x+2,0,z),(x+2,0,z+2),(x,0,z+2)],(17,53,241) if ((x+z)//2)%2==0 else (3,7,34))
  for z in [28,22,16,10,4]:
   for side in [-1,1]:
    x=side*(2.4 if z%4==0 else 3.3);f+=c.face([(x,0,z),(x,2.8,z),(x+1,2.8,z),(x+1,0,z)],(238,246,255));f+=c.face([(x,.005,z),(x+1,.005,z),(x+3.5,.005,z-8),(x+1.5,.005,z-8)],(18,102,248))
  emit_faces('corridor',f)
 else:
  cam=c.b.Camera((-.25,1.4,-5.3+.55*c.ease(t,3.65,5.1)),(0,1.15,0),46,-7)
  native_actor('elizabeth',t,T((.75,0,0))@R((0,8,0))@S(.017))
  emit_faces('door',c.b.box((-.85,1.60,.15),(1.45,3.2,.04),(229,235,255)))
 return cam

def scene(name,t):
 if name=='entrance':return entrance(t)
 if name=='reveal':
  emit_faces('reveal',REVEAL)
  return c.b.Camera((.35-.12*c.ease(t,0,3),2.48,-2.3),(0,1.98,2.75),48)
 if name!='portrait':emit_faces('room',ROOM)
 if name!='portrait':native_actor('elizabeth',t,T((-2.45,0,3.03))@R((0,5,0))@S(.016))
 if name=='fusion':
  native_actor('igor',t,np.eye(4),c.igor_pose(t+.4)[1]);player(t,c.PLAYER_MATRIX);emit_faces('chair',chair(c.PLAYER_MATRIX));cards(t);cam=c.camera(t);effects(t,cam);return cam
 if name!='portrait':native_actor('igor',0,T((0,.12,3.13))@S(.016))
 base=T((-2.05,0,-.95))@R((0,148,0));player(0,base);emit_faces('chair',chair(base))
 if name=='portrait':return c.b.Camera((-4.1,2.1,3),(-2.05,.99,-.95),36,-5)
 return c.b.Camera((2.65,3,-5.7),(-1,1.05,1.25),49,-5)

def integer(f,n):f.write(struct.pack('>i',n))
def floats(f,v):f.write(np.asarray(v,dtype='>f4').tobytes())
def export(name,end):
 global meshes,current
 meshes={};frames=[];count=math.ceil(end*FPS)+1
 for index in range(count):
  current={};t=min(end,index/FPS);cam=scene(name,t);camera=[*cam.eye,*cam.axes.flatten(),cam.f*540/720,cam.roll];frames.append((camera,current))
 keys=list(meshes);path=OUT/(name+'.bin.gz')
 with path.open('wb') as raw,gzip.GzipFile(filename='',fileobj=raw,mode='wb',mtime=0,compresslevel=9) as f:
  integer(f,0x56454157);integer(f,1);integer(f,len(keys))
  for key in keys:
   tex,verts,variant,slot=meshes[key];b=tex.encode();integer(f,len(b));f.write(b);integer(f,variant);integer(f,len(verts));floats(f,verts);integer(f,slot)
  floats(f,[0,FPS]);integer(f,count)
  for camera,poses in frames:
   floats(f,camera)
   for key in keys:floats(f,poses.get(key,np.zeros((4,4))))
 print(name,len(keys),count,path.stat().st_size,flush=True)
 return dict(name=name,meshes=len(keys),frames=count,seconds=end)
if __name__=='__main__':
 card_texture();manifest=[export('fusion',7.6),export('menu',0),export('portrait',0),export('entrance',5.1),export('reveal',5.1)]
 # Shared architecture for the world renderer; textures are palette texels, not screenshots.
 meshes={};current={};emit_faces('room',ROOM)
 with gzip.open(OUT/'architecture.bin.gz','wb') as f:floats(f,meshes['room'][1])
 palette=Image.new('RGBA',(64,64));px=palette.load()
 for color,index in colors.items():px[index%64,index//64]=(*color,255)
 for name in ['velvet_room_palette','velvet_room_glow']:palette.save(c.ASSET/'textures/entity'/f'{name}.png')
 (OUT/'manifest.json').write_text(json.dumps(manifest,indent=2),encoding='utf8')
