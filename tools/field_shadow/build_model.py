"""Original Shadow cuboids/Box UV, authored in the same coordinates as Igor and Izanagi.
No game mesh, image or texture is copied. Run with Python + Pillow.
"""
from pathlib import Path
import json,math,uuid,base64,copy,sys
from PIL import Image,ImageDraw
ROOT=Path(__file__).resolve().parent
OUT=ROOT/'draft';OUT.mkdir(parents=True,exist_ok=True)
ASSETS=ROOT.parents[1]/'src/main/resources/assets/velvet_eclipse'
sys.path.insert(0,str(ROOT.parent/'igor'))
from build_model import face_uv
S=512
PAL={'body':['#080a17','#111326','#23243a','#333247'],
 'bone':['#23302f','#405148','#7f9b83','#c9d5ad'],
 'joint':['#0c2923','#134e3d','#2e8660','#77bd77'],
 'band':['#401624','#771a36','#ba2943','#f15450'],
 'eye':['#bd5118','#ee9b25','#ffe063','#ffffc5']}
RED={'body':['#170710','#360d20','#64172c','#a32a38'],
 'bone':['#211721','#47333d','#957077','#ddaf9c'],
 'joint':['#36101d','#77162a','#c33242','#ff7865']}

def write(path,data):
 path.parent.mkdir(parents=True,exist_ok=True)
 path.write_text(json.dumps(data,indent=2,ensure_ascii=False)+'\n',encoding='utf-8')

class Model:
 def __init__(self):
  self.bones=[];self.parts=[];self.x=self.y=1;self.row=0
  self.textures=[Image.new('RGBA',(S,S)) for _ in range(2)]
  self.glows=[Image.new('RGBA',(S,S)) for _ in range(2)]
  self.bone('root')
 def bone(self,name,parent='root',pivot=(0,0,0),rotation=None):
  b={'name':name,'pivot':list(pivot)}
  if name!='root':b['parent']=parent
  if rotation:b['rotation']=list(rotation)
  self.bones.append(b);return name
 def cube(self,bone,origin,size,mat,rotation=None,pivot=None):
  w,h,d=size;assert all(isinstance(v,int) and v>0 for v in size)
  bw,bh=2*(w+d),h+d
  if self.x+bw>=S:self.x=1;self.y+=self.row+2;self.row=0
  assert self.y+bh<S
  uv=[self.x,self.y];self.x+=bw+2;self.row=max(self.row,bh)
  c={'origin':list(origin),'size':list(size),'uv':uv}
  if rotation:c.update(rotation=list(rotation),pivot=list(pivot or origin))
  next(b for b in self.bones if b['name']==bone).setdefault('cubes',[]).append(c)
  self.parts.append((bone,c))
  for variant in range(2):
   palette=RED.get(mat,PAL[mat]) if variant else PAL[mat]
   for face,(u,v,fw,fh) in face_uv(*uv,w,h,d).items():
    shade={'north':2,'south':1,'east':1,'west':1,'up':3,'down':0}[face]
    im=Image.new('RGBA',(fw,fh),palette[shade]);dr=ImageDraw.Draw(im)
    if mat=='body':
     # Deliberate layered ink ripples, not noise or tiled stone edges.
     for y in range(2,fh,5):dr.line([(0,y),(fw//3,y-1),(fw*2//3,y),(fw-1,y-1)],fill=palette[max(0,shade-1)])
    elif mat=='bone' and fw>=3 and fh>=4:
     dr.line((1,1,1,fh-2),fill=palette[3]);dr.line((fw-1,1,fw-1,fh-1),fill=palette[0]);dr.point((fw//2,fh//2),fill=palette[1])
    elif mat=='eye':dr.line((0,0,fw-1,0),fill=palette[3])
    elif fw>2 and fh>2:dr.line((0,0,fw-1,0),fill=palette[3])
    self.textures[variant].paste(im,(u,v))
    if mat in ('eye','band','joint'):
     glow=im.copy()
     if mat!='eye':glow.putalpha(110 if mat=='joint' else 150)
     self.glows[variant].paste(glow,(u,v))

m=Model();m.bone('body',pivot=(0,4,0))
# A low ink pool with an irregular, rising core: front is -Z.
for o,s,r in [((-23,0,-15),(46,2,30),None),((-19,1,-20),(38,3,38),None),((-20,3,-14),(40,6,29),(0,0,4)),((-15,7,-12),(30,7,24),(0,0,-5)),((-10,12,-6),(20,8,19),(9,0,0)),((-6,18,0),(12,8,15),(14,0,-9))]:m.cube('body',o,s,'body',r,(0,3,0))
# Two staring eyes emerging from the dark mass, distinct even in low light.
m.bone('eyes','body',pivot=(0,12,-13))
for side in (-1,1):
 m.cube('eyes',(side*6-3,10,-15),(6,4,2),'band',(0,0,-side*15),(side*6,12,-14))
 m.cube('eyes',(side*6-2,11,-16),(4,2,1),'eye',(0,0,-side*15),(side*6,12,-14))

arms=[];fingers=[]
def hand(name,x,y,z,tilt,turn,spread):
 arm=m.bone(name,'body',(x,y,z),(tilt,turn,spread));arms.append(name)
 m.cube(arm,(x-2,y,z-2),(4,10,4),'joint')
 for dx in (-2,1):m.cube(arm,(x+dx,y+2,z-2),(1,9,4),'bone')
 m.cube(arm,(x-3,y+9,z-3),(6,3,6),'band')
 palm=m.bone(name+'_palm',arm,(x,y+12,z))
 m.cube(palm,(x-4,y+12,z-2),(8,8,4),'bone')
 for dx in (-3,0,3):m.cube(palm,(x+dx,y+13,z-3),(1,6,1),'joint')
 for i in range(4):
  fx=x-3+i*2;length=[5,7,7,5][i]
  finger=m.bone(name+'_finger'+str(i),palm,(fx,y+20,z),(0,0,(1.5-i)*7));fingers.append(finger)
  m.cube(finger,(fx-1,y+20,z-2),(2,length,3),'bone')
  m.cube(finger,(fx-1,y+20+length,z-2),(2,2,3),'band')
  tip=m.bone(finger+'_tip',finger,(fx,y+22+length,z),(-24,0,0))
  m.cube(tip,(fx-1,y+22+length,z-2),(2,4,3),'bone')
 thumb=m.bone(name+'_thumb',palm,(x-4,y+15,z),(-10,0,43));fingers.append(thumb)
 m.cube(thumb,(x-6,y+14,z-2),(3,6,3),'bone')
 m.cube(thumb,(x-6,y+20,z-3),(3,3,3),'joint',(-35,0,0),(x-5,y+20,z))

# Crawling front hands; rising outer hands; tall rear arm framing the silhouette.
hand('front_left',-17,3,-8,-59,12,26)
hand('front_right',17,3,-8,-62,-12,-26)
hand('rear_left',-13,8,7,-8,-24,32)
hand('rear_right',14,8,8,-12,24,-33)
hand('crown',-3,14,8,7,-9,5)

idle={'body':{'position':{'0':[0,0,0],'1.2':[0,.65,0],'2.4':[0,0,0],'3.6':[0,.45,0],'4.8':[0,0,0]}}}
attack={'body':{'rotation':{'0':[0,0,0],'.2':[-8,0,0],'.55':[17,0,0],'.75':[12,0,0],'1':[0,0,0]},'position':{'0':[0,0,0],'.2':[0,1,2],'.55':[0,0,-7],'1':[0,0,0]}}}
for i,name in enumerate(arms):
 sign=-1 if i%2 else 1
 idle[name]={'rotation':{'0':[0,0,0],'1.2':[3,sign*3,sign*4],'2.4':[-2,sign*-2,sign*-3],'3.6':[2,0,sign*2],'4.8':[0,0,0]}}
 attack[name]={'rotation':{'0':[0,0,0],'.2':[22,0,sign*-9],'.55':[-42 if i>1 else -16,0,sign*9],'.75':[-35 if i>1 else -9,0,0],'1':[0,0,0]}}
for i,name in enumerate(fingers):
 idle[name]={'rotation':{'0':[0,0,0],'1.2':[6+i%4,0,0],'2.4':[0,0,0],'3.6':[-4,0,0],'4.8':[0,0,0]}}
 attack[name]={'rotation':{'0':[0,0,0],'.2':[20,0,0],'.55':[-35,0,0],'.75':[-48,0,0],'1':[0,0,0]}}
animations={'format_version':'1.8.0','animations':{'misc.idle':{'loop':True,'animation_length':4.8,'bones':idle},'action.attack':{'loop':False,'animation_length':1,'bones':attack}}}
geometry={'format_version':'1.12.0','minecraft:geometry':[{'description':{'identifier':'geometry.field_shadow','texture_width':S,'texture_height':S,'visible_bounds_width':5,'visible_bounds_height':4,'visible_bounds_offset':[0,1,0]},'bones':m.bones}]}
write(OUT/'field_shadow.geo.json',geometry);write(OUT/'field_shadow.animation.json',animations)
native=copy.deepcopy(geometry)
for b in native['minecraft:geometry'][0]['bones']:
 b['pivot'][0]*=-1
 if 'rotation' in b:b['rotation']=[-b['rotation'][0],-b['rotation'][1],b['rotation'][2]]
 for c in b.get('cubes',[]):
  c['origin'][0]=-c['origin'][0]-c['size'][0]
  if 'pivot' in c:c['pivot'][0]*=-1
  if 'rotation' in c:c['rotation']=[-c['rotation'][0],-c['rotation'][1],c['rotation'][2]]
runtime=copy.deepcopy(animations)
# Apply the same Cartesian-to-Bedrock compensation as Igor/Izanagi once on export.
for clip in runtime['animations'].values():
 for tracks in clip['bones'].values():
  for channel,keys in tracks.items():
   for t,v in keys.items():
    if channel=='rotation':keys[t]=[-v[0],-v[1],v[2]]
    elif channel=='position':keys[t]=[-v[0],v[1],v[2]]
write(ASSETS/'geo/entity/field_shadow.geo.json',native);write(ASSETS/'animations/entity/field_shadow.animation.json',runtime)
for i,stem in enumerate(['field_shadow','red_shadow']):
 m.textures[i].save(OUT/(stem+'.png'));m.textures[i].save(ASSETS/'textures/entity'/(stem+'.png'));m.glows[i].save(ASSETS/'textures/entity'/(stem+'_glowmask.png'))
 icon=Image.new('RGBA',(32,32));d=ImageDraw.Draw(icon)
 d.polygon([(3,27),(5,17),(9,19),(11,13),(20,13),(23,21),(28,18),(30,27)],fill='#8e1b38' if i else '#24243d')
 for x,y in [(6,15),(25,13),(16,6)]:
  d.rectangle((x-1,y,x+1,y+9),fill='#9bba9b');d.rectangle((x-3,y-3,x+3,y),fill='#577965')
  for dx in [-3,0,3]:d.line((x+dx,y-3,x+dx,y-6),fill='#cbd5a6')
 d.rectangle((11,20,13,21),fill='#fff66b');d.rectangle((18,20,20,21),fill='#fff66b')
 icon.save(ASSETS/'textures/item'/(stem+'_spawn.png'))
 write(ASSETS/'models/item'/(stem+'_spawn.json'),{'parent':'minecraft:item/generated','textures':{'layer0':'velvet_eclipse:item/'+stem+'_spawn'}})
ident=lambda s:str(uuid.uuid5(uuid.NAMESPACE_URL,'field_shadow/'+s))
elements=[]
for i,(bone,c) in enumerate(m.parts):
 a=c['origin'];elements.append({'name':bone+'_'+str(i),'type':'cube','uuid':ident(str(i)),'from':a,'to':[v+s for v,s in zip(a,c['size'])],'origin':c.get('pivot',a),'rotation':c.get('rotation',[0,0,0]),'box_uv':True,'uv_offset':c['uv'],'faces':{f:{'uv':[u,v,u+w,v+h],'texture':0} for f,(u,v,w,h) in face_uv(*c['uv'],*c['size']).items()}})
def group(b):return {'name':b['name'],'uuid':ident('bone/'+b['name']),'origin':b['pivot'],'rotation':b.get('rotation',[0,0,0]),'children':[ident(str(i)) for i,(name,c) in enumerate(m.parts) if name==b['name']]+[group(k) for k in m.bones if k.get('parent')==b['name']]}
editor_animations=[]
for name,clip in animations['animations'].items():
 animators={}
 for bone,tracks in clip['bones'].items():
  animators[ident('bone/'+bone)]={'name':bone,'type':'bone','keyframes':[{'channel':channel,'data_points':[{'x':v[0],'y':v[1],'z':v[2]}],'time':float(t),'color':-1,'interpolation':'linear','uuid':ident(name+bone+channel+t)} for channel,keys in tracks.items() for t,v in keys.items()]}
 editor_animations.append({'uuid':ident(name),'name':name,'loop':'loop' if clip['loop'] else 'once','length':clip['animation_length'],'animators':animators})
write(OUT/'field_shadow.bbmodel',{'meta':{'format_version':'4.10','model_format':'bedrock','box_uv':True},'name':'field_shadow','resolution':{'width':S,'height':S},'elements':elements,'outliner':[group(m.bones[0])],'animations':editor_animations,'textures':[{'id':'0','name':'field_shadow.png','uuid':ident('texture'),'source':'data:image/png;base64,'+base64.b64encode((OUT/'field_shadow.png').read_bytes()).decode()}]})
for locale in ['pt_br','en_us']:
 path=ASSETS/'lang'/(locale+'.json');obj=json.loads(path.read_text(encoding='utf-8'))
 obj.update({'entity.velvet_eclipse.field_shadow':'Shadow','item.velvet_eclipse.field_shadow_spawn':'Invocar Shadow' if locale=='pt_br' else 'Spawn Shadow','item.velvet_eclipse.red_shadow_spawn':'Invocar Shadow Vermelha' if locale=='pt_br' else 'Spawn Red Shadow'})
 write(path,obj)
print(len(m.parts),'cuboids,',len(m.bones),'bones; atlas last row',m.y+m.row)
