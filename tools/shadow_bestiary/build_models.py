"""Hand-authored Reload Shadow interpretations. Cuboids, articulated bones and Box UV.
Same authored Cartesian space and Bedrock export compensation as Igor/Izanagi.
No imported game meshes or textures. Run with Python + Pillow.
"""
from pathlib import Path
import sys,json,math,copy,uuid,base64,re
from PIL import Image,ImageDraw
ROOT=Path(__file__).resolve().parent
OUT=ROOT/'draft';OUT.mkdir(parents=True,exist_ok=True)
ASSETS=ROOT.parents[1]/'src/main/resources/assets/velvet_eclipse'
sys.path.insert(0,str(ROOT.parent/'igor'))
from build_model import face_uv
S=512
PAL={
 'ink':['#070b14','#131825','#253044','#354357'],
 'blue':['#12274d','#284c85','#538bd1','#9dd6e9'],
 'purple':['#2d153e','#562767','#874696','#ba77c8'],
 'gold':['#604321','#a17938','#d2b459','#f5e4a2'],
 'silver':['#343944','#677886','#a7bdc8','#e1eff0'],
 'white':['#655f61','#a29c91','#dcd4bb','#fff4d3'],
 'red':['#4b1225','#8d2735','#ce4850','#ff9a76'],
 'cyan':['#163e51','#277587','#55bdc9','#b6ffff'],
 'robe':['#211717','#402b25','#674937','#8c7452'],
 'eye':['#b56d23','#e6ae48','#ffe393','#fff8d3']}
def write(path,data):
 path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
class Model:
 def __init__(self,name):
  self.name=name;self.bones=[];self.parts=[];self.x=self.y=1;self.row=0;self.tex=Image.new('RGBA',(S,S));self.bone('root');self.bone('body',pivot=(0,12,0))
 def bone(self,name,parent='root',pivot=(0,0,0),rotation=None):
  b={'name':name,'pivot':list(pivot)}
  if name!='root':b['parent']=parent
  if rotation:b['rotation']=list(rotation)
  self.bones.append(b);return name
 def cube(self,bone,o,s,mat,rotation=None,pivot=None):
  w,h,d=s;assert all(isinstance(v,int) and v>0 for v in s)
  bw,bh=2*(w+d),h+d
  if self.x+bw>=S:self.x=1;self.y+=self.row+2;self.row=0
  assert self.y+bh<S,(self.name,self.y)
  uv=[self.x,self.y];self.x+=bw+2;self.row=max(self.row,bh)
  c={'origin':list(o),'size':list(s),'uv':uv}
  if rotation:c.update(rotation=list(rotation),pivot=list(pivot or o))
  next(b for b in self.bones if b['name']==bone).setdefault('cubes',[]).append(c);self.parts.append((bone,c))
  for f,(u,v,fw,fh) in face_uv(*uv,w,h,d).items():
   p=PAL[mat];shade={'north':2,'south':1,'east':1,'west':1,'up':3,'down':0}[f]
   im=Image.new('RGBA',(fw,fh),p[shade]);dr=ImageDraw.Draw(im)
   if fw>3 and fh>3:
    dr.line((1,1,fw-2,1),fill=p[min(3,shade+1)]);dr.line((fw-1,1,fw-1,fh-1),fill=p[max(0,shade-1)])
    if mat in ('ink','robe'):
     for y in range(4,fh,6):dr.line((1,y,fw-2,y-1),fill=p[max(0,shade-1)])
   self.tex.paste(im,(u,v))
  return c
 def mask(self,bone,x,y,z,mat='blue',mood=0):
  # Stepped oval mask, projecting nose, inset eye wells and painted Arcana strokes.
  self.cube(bone,(x-5,y+2,z),(10,10,2),mat);self.cube(bone,(x-4,y,z),(8,3,2),mat)
  self.cube(bone,(x-3,y-2,z),(6,3,2),mat);self.cube(bone,(x-4,y+12,z),(8,2,2),mat)
  for side in (-1,1):
   self.cube(bone,(x+side*3-1,y+6,z-1),(3,2,1),'ink',(0,0,side*(12 if mood==0 else -14)),(x+side*3,y+7,z))
   self.cube(bone,(x+side*3,y+6,z-2),(1,1,1),'eye')
  self.cube(bone,(x-1,y+3,z-2),(2,4,2),mat)
  self.cube(bone,(x-2,y+1,z-1),(4,1,1),'ink')
  self.cube(bone,(x-2,y+10,z-1),(1,3,1),'ink');self.cube(bone,(x+1,y+10,z-1),(1,3,1),'ink')
 def limb(self,name,parent,o,length,mat,rotation=(0,0,0),width=3):
  self.bone(name,parent,o,rotation);self.cube(name,(o[0]-width/2,o[1]-length,o[2]-width/2),(width,length,width),mat)
 def export(self):
  name=self.name;geo={'format_version':'1.12.0','minecraft:geometry':[{'description':{'identifier':'geometry.'+name,'texture_width':S,'texture_height':S,'visible_bounds_width':6,'visible_bounds_height':6,'visible_bounds_offset':[0,1.5,0]},'bones':self.bones}]}
  idle={'body':{'position':{'0':[0,0,0],'1.2':[0,.8,0],'2.4':[0,0,0],'3.6':[0,-.5,0],'4.8':[0,0,0]},'rotation':{'0':[0,0,0],'2.4':[0,3,0],'4.8':[0,0,0]}}}
  hit={'body':{'rotation':{'0':[0,0,0],'.2':[-12,0,0],'.5':[20,0,0],'.7':[10,0,0],'1':[0,0,0]},'position':{'0':[0,0,0],'.2':[0,1,2],'.5':[0,0,-6],'1':[0,0,0]}}}
  magic={'body':{'position':{'0':[0,0,0],'.3':[0,3,0],'.6':[0,4,0],'1':[0,0,0]},'rotation':{'0':[0,0,0],'.3':[-8,0,0],'.6':[6,0,0],'1':[0,0,0]}}}
  for i,b in enumerate(self.bones):
   n=b['name'];amp=5 if any(x in n for x in ('finger','ribbon','tail','pan','chain','leg')) else 11 if 'wing' in n else 2
   if n in ('root','body'):continue
   sign=1 if i%2 else -1
   idle[n]={'rotation':{'0':[0,0,0],'1.2':[amp*sign,0,amp/2],'2.4':[0,0,0],'3.6':[-amp*sign,0,-amp/2],'4.8':[0,0,0]}}
   if any(x in n for x in ('wing','arm','finger','horn','head','beam','cutlery','wheel')):
    hit[n]={'rotation':{'0':[0,0,0],'.2':[-15,0,sign*12],'.5':[28,0,-sign*15],'.7':[12,0,0],'1':[0,0,0]}}
    magic[n]={'rotation':{'0':[0,0,0],'.3':[-18,0,sign*14],'.6':[-8,0,sign*6],'1':[0,0,0]}}
  anim={'format_version':'1.8.0','animations':{'misc.idle':{'loop':True,'animation_length':4.8,'bones':idle},'action.physical':{'loop':False,'animation_length':1,'bones':hit},'action.magic':{'loop':False,'animation_length':1,'bones':magic}}}
  write(OUT/(name+'.geo.json'),geo);write(OUT/(name+'.animation.json'),anim);self.tex.save(OUT/(name+'.png'))
  native=copy.deepcopy(geo)
  for b in native['minecraft:geometry'][0]['bones']:
   b['pivot'][0]*=-1
   if 'rotation' in b:b['rotation']=[-b['rotation'][0],-b['rotation'][1],b['rotation'][2]]
   for c in b.get('cubes',[]):
    c['origin'][0]=-c['origin'][0]-c['size'][0]
    if 'pivot' in c:c['pivot'][0]*=-1
    if 'rotation' in c:c['rotation']=[-c['rotation'][0],-c['rotation'][1],c['rotation'][2]]
  runtime=copy.deepcopy(anim)
  for clip in runtime['animations'].values():
   for tracks in clip['bones'].values():
    for channel,keys in tracks.items():
     for t,v in keys.items():keys[t]=[-v[0],-v[1],v[2]] if channel=='rotation' else [-v[0],v[1],v[2]]
  write(ASSETS/'geo/entity'/(name+'.geo.json'),native);write(ASSETS/'animations/entity'/(name+'.animation.json'),runtime);self.tex.save(ASSETS/'textures/entity'/(name+'.png'))
  ident=lambda s:str(uuid.uuid5(uuid.NAMESPACE_URL,name+'/'+s))
  elems=[]
  for i,(bone,c) in enumerate(self.parts):
   a=c['origin'];elems.append({'name':bone+'_'+str(i),'type':'cube','uuid':ident(str(i)),'from':a,'to':[v+s for v,s in zip(a,c['size'])],'origin':c.get('pivot',a),'rotation':c.get('rotation',[0,0,0]),'box_uv':True,'uv_offset':c['uv'],'faces':{f:{'uv':[u,v,u+w,v+h],'texture':0} for f,(u,v,w,h) in face_uv(*c['uv'],*c['size']).items()}})
  def group(b):return {'name':b['name'],'uuid':ident('bone/'+b['name']),'origin':b['pivot'],'rotation':b.get('rotation',[0,0,0]),'children':[ident(str(i)) for i,(bn,c) in enumerate(self.parts) if bn==b['name']]+[group(k) for k in self.bones if k.get('parent')==b['name']]}
  clips=[]
  for n,clip in anim['animations'].items():
   animators={ident('bone/'+bn):{'name':bn,'type':'bone','keyframes':[{'channel':ch,'data_points':[{'x':v[0],'y':v[1],'z':v[2]}],'time':float(t),'interpolation':'linear','uuid':ident(n+bn+ch+t)} for ch,keys in tracks.items() for t,v in keys.items()]} for bn,tracks in clip['bones'].items()}
   clips.append({'uuid':ident(n),'name':n,'loop':'loop' if clip['loop'] else 'once','length':clip['animation_length'],'animators':animators})
  write(OUT/(name+'.bbmodel'),{'meta':{'format_version':'4.10','model_format':'bedrock','box_uv':True},'name':name,'resolution':{'width':S,'height':S},'elements':elems,'outliner':[group(self.bones[0])],'animations':clips,'textures':[{'id':'0','name':name+'.png','uuid':ident('texture'),'source':'data:image/png;base64,'+base64.b64encode((OUT/(name+'.png')).read_bytes()).decode()}]})
  write(ASSETS/'models/item'/(name+'_spawn.json'),{'parent':'minecraft:item/generated','textures':{'layer0':'velvet_eclipse:item/'+name+'_spawn'}})
  print(name,len(self.parts),'cuboids',len(self.bones),'bones')

def maya(m,variant):
 for o,s in [((-18,0,-12),(36,2,25)),((-14,2,-10),(28,5,21)),((-10,6,-7),(20,7,15)),((-6,12,-3),(12,9,10))]:m.cube('body',o,s,'ink')
 for i,(x,z,rot) in enumerate([(-16,-3,-26),(13,0,30),(-9,9,18),(7,10,-20)]):
  n=m.bone('tail'+str(i),'body',(x,3,z),(0,i*30,rot));m.cube(n,(x-2,2,z-2),(5,9,4),'ink')
 m.bone('head','body',(0,16,-4),(8,0,-7 if variant==0 else 7));m.mask('head',0,15,-7,'blue' if variant!=1 else 'purple',variant)
 # Distinct painted forehead motifs and expressions among the Maya family.
 for x in range(-variant,variant+1):m.cube('head',(x*2,28,-8),(1,2,1),'white')

def tiara(m,grief):
 m.bone('head','body',(0,21,0));m.cube('head',(-7,17,-5),(14,12,10),'ink');m.mask('head',0,17,-7,'purple')
 for i in range(9):
  a=i*math.tau/9;x=round(math.cos(a)*7);z=round(math.sin(a)*5);n=m.bone('ribbon'+str(i),'head',(x,18,z),(10 if z>0 else -8,0,(i%3-1)*9));m.cube(n,(x-1,3+i%3,z-1),(3,16-i%3,3),'ink')
 # Open crown arches use discrete cuboids, never a closed helmet silhouette.
 for side in (-1,1):
  for j,(dx,y,ang) in enumerate([(6,29,side*-20),(10,33,side*10),(10,39,side*36),(6,44,side*58)]):m.cube('head',(side*dx-1,y,-2),(3,7,4),'purple' if grief else 'gold',(0,0,ang),(side*dx,y,-1))
 m.cube('head',(-8,28,-6),(16,3,12),'gold');m.cube('head',(-2,34,-5),(4,12,5),'gold');m.cube('head',(-3,35,-7),(6,6,2),'purple' if grief else 'cyan',(0,0,45),(0,38,-6))

def hand(m,kind):
 mat='white' if kind==0 else 'gold';m.cube('body',(-7,1,-5),(14,4,10),mat);m.cube('body',(-6,5,-4),(12,16,8),mat)
 m.bone('head','body',(0,13,-4));m.mask('head',0,7,-6,'purple' if kind==0 else 'white')
 for i,length in enumerate([12,16,18,14]):
  x=-6+i*4;n=m.bone('finger'+str(i),'body',(x,20,0),(0,0,(1.5-i)*4));m.cube(n,(x-1,20,-3),(3,length,6),mat)
  m.cube(n,(x-1,20+length,-2),(3,3,4),mat);m.cube(n,(x-1,20+length-2,-4),(3,4,1),'white' if kind==0 else 'gold')
 n=m.bone('finger_thumb','body',(-7,13,0),(0,0,42));m.cube(n,(-10,13,-3),(5,11,6),mat)
 for side in (-1,1):m.cube('body',(side*5-3,2,-6),(5,4,2),'red',(0,0,side*18),(side*5,4,-5))
 m.cube('body',(-2,3,-7),(4,3,2),'purple')
 if kind>=2:
  # Rare/greedy ranks retain the iconic golden hand, with articulated cuffs and gems.
  m.cube('body',(-8,0,-6),(16,3,12),'gold')
  for x in (-6,0,6):m.cube('body',(x-1,1,-8.5),(2,2,1),'cyan' if kind==2 else 'red')
 if kind==3:
  for i in range(5):m.cube('body',(-8+i*4,3,-7.3),(2,5,2),'gold')

def twins(m):
 for side in (-1,1):
  x=side*10;n=m.bone('twin'+str(side),'body',(x,21,0),(0,0,side*-7))
  m.cube(n,(x-5,13,-4),(10,17,8),'robe');m.cube(n,(x-7,10,-5),(14,6,10),'robe')
  m.cube(n,(x-6,28,-5),(12,12,10),'robe');m.mask(n,x,27,-7,'blue');m.cube(n,(x-6,26,-5),(12,3,10),'ink')
  for j in (-1,1):
   m.limb('arm'+str(side)+'_'+str(j),n,(x+j*6,25,0),14,'robe',(0,0,j*18),4)
   m.cube(n,(x+j*7-3,9,-3),(6,3,6),'ink');m.cube(n,(x+j*7-2,5,-2),(4,4,4),'white')
   m.limb('leg'+str(side)+'_'+str(j),n,(x+j*3,12,0),10,'robe',(0,0,j*6),4)
   m.cube(n,(x+j*3-3,1,-3),(6,3,6),'ink')
 for y in (4,11,27):
  m.cube('body',(-25,y,-1),(50,2,2),'silver')
  for side in (-1,1):m.cube('body',(side*26-2,y-1,-2),(4,4,4),'silver',(0,0,45),(side*26,y,0))

def wings(m,parent,x,y,z,mat='ink',span=19):
 for side in (-1,1):
  n=m.bone('wing'+str(side),parent,(x+side*4,y,z),(0,side*-8,side*12))
  m.cube(n,(x+min(side*4,side*span),y-2,z),(span-3,5,4),mat)
  for i in range(5):
   px=x+side*(7+i*3);m.cube(n,(px-1,y-10+i,z+1),(3,12-i,2),mat,(0,0,side*12),(px,y,z))

def cupid(m):
 m.cube('body',(-4,16,-3),(8,11,6),'white');m.cube('body',(-5,13,-3),(10,5,7),'purple')
 m.bone('head','body',(0,28,0));m.cube('head',(-7,26,-6),(14,12,12),'purple');m.mask('head',0,26,-7,'blue');m.cube('head',(-8,37,-5),(16,2,12),'gold')
 wings(m,'body',0,26,4,'white',18)
 for side in (-1,1):
  m.limb('leg'+str(side),'body',(side*3,15,0),10,'white',(side*17,0,side*10),3)
  m.limb('arm'+str(side),'body',(side*5,25,0),9,'white',(-35,0,side*35),3)
 m.bone('bow','body',(-12,20,-7));m.cube('bow',(-13,9,-7),(2,23,2),'gold');m.cube('bow',(-17,12,-7),(1,17,1),'white')
 for y,r in [(10,-40),(28,40)]:m.cube('bow',(-15,y,-7),(2,6,2),'gold',(0,0,r),(-14,y,-7))
 m.cube('bow',(-17,21,-9),(29,1,1),'gold')
 for x,y in [(12,21),(14,21),(13,19)]:m.cube('bow',(x,y,-10),(2,3,2),'red',(0,0,45),(x+1,y+1,-9))

def beetle(m):
 m.cube('body',(-11,6,-9),(22,10,24),'ink');m.cube('body',(-9,15,-6),(18,5,18),'purple');m.cube('body',(-1,15,-7),(2,6,20),'ink')
 for side in (-1,1):
  for i,z in enumerate([-6,3,11]):
   n=m.bone('leg'+str(side)+'_'+str(i),'body',(side*10,12,z),(0,0,side*-35));m.cube(n,(side*10-2,3,z-1),(4,10,3),'purple');m.cube(n,(side*15-2,0,z-2),(4,8,4),'ink')
 m.bone('head','body',(0,12,-10));m.cube('head',(-7,6,-17),(14,11,10),'purple');m.mask('head',0,7,-19,'blue')
 m.bone('horn','head',(0,17,-13),(-22,0,0));m.cube('horn',(-2,16,-15),(4,16,4),'purple');m.cube('horn',(-5,27,-16),(10,3,3),'gold')

def balance(m):
 m.cube('body',(-2,4,-2),(4,39,4),'ink');m.bone('beam','body',(0,29,0));m.cube('beam',(-22,28,-2),(44,4,4),'ink');m.mask('beam',0,24,-4,'silver')
 for side in (-1,1):
  mat='red' if side<0 else 'cyan'
  for i in range(5):
   a=math.radians(-65+i*32);x=side*round(math.cos(a)*12);y=29+round(math.sin(a)*12);m.cube('beam',(x-2,y-2,1),(4,6,3),mat,(0,0,-side*(i*32-65)),(x,y,2))
  n=m.bone('pan'+str(side),'beam',(side*20,28,0))
  for j in (-1,1):
   for k in range(8):m.cube(n,(side*20+j*k*.7-1,25-k*2,-1),(2,3,2),mat,(0,0,j*18),(side*20,28,0))
  m.cube(n,(side*20-7,10,-5),(14,2,10),mat);m.cube(n,(side*20-5,8,-3),(10,2,6),mat)
  if side>0:m.cube(n,(side*20-3,12,-3),(6,5,6),'cyan',(0,0,30),(side*20,12,0))

def table(m):
 m.cube('body',(-14,12,-8),(28,3,18),'ink')
 for x in (-11,9):
  for z in (-6,6):m.limb('leg'+str(x)+'_'+str(z),'body',(x,12,z),11,'ink',(0,0,0),3)
 m.cube('body',(-15,15,-9),(30,1,20),'white')
 for x in range(-14,14,4):m.cube('body',(x,9+(x%3),-10),(4,7-x%3,1),'white')
 m.bone('head','body',(0,12,-10));m.mask('head',0,4,-12,'blue')
 for i,(x,y,z) in enumerate([(-12,31,0),(-5,29,5),(7,30,0),(14,29,5)]):
  n=m.bone('cutlery'+str(i),'body',(x,y,z),(0,0,i*9-12))
  if i==0:m.cube(n,(x-1,y-6,z),(2,15,1),'silver');m.cube(n,(x-2,y-6,z),(4,2,2),'gold')
  elif i==1:m.cube(n,(x-1,y-8,z),(2,15,2),'silver');m.cube(n,(x-3,y+4,z-2),(6,3,5),'silver')
  elif i==2:
   m.cube(n,(x-4,y-6,z-3),(8,2,6),'silver');m.cube(n,(x-1,y-5,z-1),(2,6,2),'silver');m.cube(n,(x-4,y+1,z-3),(8,5,6),'silver');m.cube(n,(x-2,y+5,z-1),(4,1,2),'ink')
  else:m.cube(n,(x-5,y-1,z-5),(10,2,10),'silver',(50,0,0),(x,y,z))

def raven(m,ice):
 mat='cyan' if ice else 'ink';m.cube('body',(-5,16,-4),(10,13,12),mat);m.cube('body',(-4,13,5),(8,4,14),mat,(-18,0,0),(0,17,6))
 wings(m,'body',0,26,2,mat,23)
 m.bone('head','body',(0,31,-2));m.cube('head',(-5,27,-7),(10,10,10),mat);m.cube('head',(-2,28,-15),(4,3,9),'gold');m.cube('head',(-1,27,-16),(2,2,8),'gold')
 for side in (-1,1):m.cube('head',(side*4-1,32,-8),(2,2,1),'eye')
 m.cube('head',(-8,37,-9),(16,2,14),'ink');m.cube('head',(-5,39,-6),(10,9,9),'ink');m.cube('head',(-5,40,-7),(10,2,1),'blue' if ice else 'red')
 for side in (-1,1):
  m.limb('leg'+str(side),'body',(side*3,17,0),9,'gold',(-12,0,0),2)
  for j in (-1,1):m.cube('leg'+str(side),(side*3+j-1,6,-4),(2,2,5),'gold')
 n=m.bone('lantern','body',(6,13,0));m.cube(n,(6,7,-1),(1,6,1),'gold');m.cube(n,(3,0,-4),(7,2,7),'ink');m.cube(n,(4,2,-3),(5,4,5),'cyan' if ice else 'red');m.cube(n,(3,6,-4),(7,2,7),'ink')
 for x in (3,9):m.cube(n,(x,2,-4),(1,4,1),'gold')

def wheel(m,axle):
 for side in (-1,1) if axle else (0,):
  x=side*13;n=m.bone('wheel'+str(side),'body',(x,14,0))
  for i in range(12):
   a=i*30;y=14+round(math.sin(math.radians(a))*12);z=round(math.cos(math.radians(a))*12)
   m.cube(n,(x-3,y-3,z-3),(6,6,6),'silver' if axle else 'robe',(a,0,0),(x,y,z))
  for a in (0,45,90,135):m.cube(n,(x-1,2,-1),(2,24,2),'gold',(a,0,0),(x,14,0))
 if axle:
  m.cube('body',(-12,15,-12),(24,12,24),'ink');m.cube('body',(-9,27,-8),(18,7,17),'blue');m.cube('body',(-13,26,-13),(26,3,27),'silver');m.bone('head','body',(0,22,-13));m.mask('head',0,18,-15,'blue')
  for side in (-1,1):
   m.cube('body',(side*17-2,14,-16),(4,2,28),'silver');m.cube('body',(side*17-4,14,-19),(8,1,12),'gold')
 else:
  m.bone('head','body',(0,21,-6));m.cube('head',(-10,20,-9),(20,14,13),'robe');m.cube('head',(-7,21,-12),(14,9,6),'gold');m.cube('head',(-5,21,-15),(10,5,4),'robe')
  for side in (-1,1):
   m.cube('head',(side*7-2,32,-7),(4,5,4),'gold');m.cube('head',(side*4-1,27,-13),(3,2,1),'eye');m.cube('head',(side*4-1,21,-16),(2,4,2),'white')
  for i in range(7):m.cube('head',(-12+i*4,18,-4),(3,12,5),'robe',(0,0,(i-3)*8),(0,26,0))

def build():
 source=(ROOT.parents[1]/'src/main/java/com/gx/velveteclipse/shadow/EnemyCatalog.java').read_text(encoding='utf-8')
 entries=re.findall(r'add\("([a-z_]+)","([^"]+)","([a-z]+)"',source)
 for id,title,family in entries:
  m=Model('enemy_'+id)
  if family=='maya':maya(m,['cowardly_maya','merciless_maya','frivolous_maya'].index(id))
  elif family=='tiara':tiara(m,id=='grieving_tiara')
  elif family in ('hand','checkmate'):hand(m,0 if id=='magic_hand' else 1 if id=='wealth_hand' else 2 if id=='treasure_hand' else 3)
  elif family=='twins':twins(m)
  elif family=='cupid':cupid(m)
  elif family=='beetle':beetle(m)
  elif family=='balance':balance(m)
  elif family=='table':table(m)
  elif family=='raven':raven(m,id=='ruthless_ice_raven')
  elif family in ('wheel','axle'):wheel(m,family=='axle')
  else:raise ValueError(family)
  m.export()
 for locale in ('pt_br','en_us'):
  p=ASSETS/'lang'/(locale+'.json');data=json.loads(p.read_text(encoding='utf-8'))
  for id,title,family in entries:data.update({'entity.velvet_eclipse.enemy_'+id:title,'item.velvet_eclipse.enemy_'+id+'_spawn':('Modelo • ' if locale=='pt_br' else 'Model • ')+title})
  write(p,data)
 write(OUT/'catalog.json',[{'id':id,'name':title,'family':family} for id,title,family in entries])
if __name__=='__main__':build()
