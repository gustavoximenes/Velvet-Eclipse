"""Authored Bedrock cuboids, Box UV, integer texels. No downloaded game assets."""
from pathlib import Path
import json,math,uuid,base64,copy,sys
from PIL import Image,ImageDraw
ROOT=Path(__file__).resolve().parent
OUT=ROOT/'draft';OUT.mkdir(parents=True,exist_ok=True)
ASSETS=ROOT.parents[1]/'src/main/resources/assets/velvet_eclipse'
sys.path.insert(0,str(ROOT.parent/'igor'))
from build_model import face_uv
S=512
PAL={'metal':['#07171f','#18333e','#365963','#688787'], 'silver':['#354857','#69838e','#b5c8c8','#eff5dd'],
 'cyan':['#155967','#219ca9','#52e5ea','#ccfff6'], 'gold':['#5e451d','#967333','#d4b855','#fff6b0'],
 'black':['#080c18','#15172c','#303047','#514859'], 'red':['#561331','#a62f57','#f45275','#ffb4a3'],
 'purple':['#241434','#542555','#9c4e9e','#f6a7ff']}

class Model:
 def __init__(self,name):self.name=name;self.bones=[];self.parts=[];self.atlas=Image.new('RGBA',(S,S));self.glow=Image.new('RGBA',(S,S));self.x=self.y=1;self.row=0;self.bone('root')
 def bone(self,name,parent='root',pivot=(0,0,0)):
  b={'name':name,'pivot':list(pivot)}
  if name!='root':b['parent']=parent
  self.bones.append(b);return name
 def cube(self,bone,origin,size,mat='metal',rotation=None,pivot=None,pattern=''):
  w,h,d=size;assert all(isinstance(v,int) and v>0 for v in size)
  bw=2*(w+d);bh=h+d
  if self.x+bw>=S:self.x=1;self.y+=self.row+2;self.row=0
  assert self.y+bh<S
  uv=[self.x,self.y];self.x+=bw+2;self.row=max(self.row,bh)
  c={'origin':list(origin),'size':list(size),'uv':uv}
  if rotation:c.update(rotation=list(rotation),pivot=list(pivot or origin))
  next(b for b in self.bones if b['name']==bone).setdefault('cubes',[]).append(c)
  self.parts.append({'bone':bone,'name':bone+'_'+str(len(self.parts)),'cube':c})
  for face,(u,v,fw,fh) in face_uv(*uv,w,h,d).items():
   p=PAL[mat];shade={'north':2,'south':1,'east':1,'west':1,'up':3,'down':0}[face]
   im=Image.new('RGBA',(fw,fh),p[shade]);dr=ImageDraw.Draw(im)
   if fw>2 and fh>2:dr.line((0,0,fw-1,0),fill=p[3]);dr.line((0,0,0,fh-1),fill=p[0]);dr.line((fw-1,1,fw-1,fh-1),fill=p[0])
   if pattern=='ribs':
    for x in range(2,fw-1,3):dr.line((x,1,x,fh-2),fill=p[0]);dr.line((x+1,1,x+1,fh-2),fill=p[2])
   if pattern=='core' and face=='north':
    dr.rectangle((2,2,fw-3,fh-3),outline=p[3]);dr.line((fw//2,2,fw//2,fh-3),fill=p[1])
   if pattern=='sigil' and face in ('north','south'):
    # Three coils and symmetrical wing strokes, painted on the door leaf.
    for cy in [12,28,44]:
     pts=[]
     for k in range(95):
      a=k/94*math.pi*4;r=1+k/94*6;pts.append((int(fw/2+math.cos(a)*r),int(cy+math.sin(a)*r)))
     dr.line(pts,fill='#ffe7ab',width=1)
    for side in [-1,1]:
     for k in range(8):dr.line((fw//2+side*4,38+k,fw//2+side*(7+k),31+k),fill='#ffbfce',width=1)
   if pattern=='taper':
    im=Image.new('RGBA',(fw,fh));dr=ImageDraw.Draw(im)
    if face in ('north','south'):dr.polygon([(fw//2,0),(fw-1,fh-1),(0,fh-1)],fill=p[shade])
   self.atlas.paste(im,(u,v))
   if mat in ('cyan','red','purple'):
    glow=im.copy()
    if mat!='cyan' and pattern!='sigil':glow.putalpha(100)
    self.glow.paste(glow,(u,v))
  return c
 def export(self,tracks):
  desc={'identifier':'geometry.tartarus_'+self.name,'texture_width':S,'texture_height':S,'visible_bounds_width':6,'visible_bounds_height':8,'visible_bounds_offset':[0,3,0]}
  geo={'format_version':'1.12.0','minecraft:geometry':[{'description':desc,'bones':self.bones}]}
  animation={'format_version':'1.8.0','animations':{'misc.idle':{'loop':True,'animation_length':4,'bones':tracks}}}
  stem='tartarus_'+self.name
  def write(path,data):path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(data,indent=2)+'\n',encoding='utf-8')
  write(OUT/(stem+'.geo.json'),geo);write(OUT/(stem+'.animation.json'),animation);self.atlas.save(OUT/(stem+'.png'))
  # Identical conversion used for the approved Igor/Izanagi coordinates.
  native=copy.deepcopy(geo)
  for b in native['minecraft:geometry'][0]['bones']:
   b['pivot'][0]*=-1
   for c in b.get('cubes',[]):
    c['origin'][0]=-c['origin'][0]-c['size'][0]
    if 'pivot' in c:c['pivot'][0]*=-1
    if 'rotation' in c:c['rotation']=[-c['rotation'][0],-c['rotation'][1],c['rotation'][2]]
  write(ASSETS/'geo/entity'/(stem+'.geo.json'),native);write(ASSETS/'animations/entity'/(stem+'.animation.json'),animation)
  self.atlas.save(ASSETS/'textures/entity'/(stem+'.png'));self.glow.save(ASSETS/'textures/entity'/(stem+'_glowmask.png'))
  ident=lambda s:str(uuid.uuid5(uuid.NAMESPACE_URL,stem+'/'+s))
  elements=[]
  for p in self.parts:
   c=p['cube'];a=c['origin'];elements.append({'name':p['name'],'type':'cube','uuid':ident(p['name']),'from':a,'to':[v+s for v,s in zip(a,c['size'])],'origin':c.get('pivot',a),'rotation':c.get('rotation',[0,0,0]),'box_uv':True,'uv_offset':c['uv'],'faces':{f:{'uv':[u,v,u+w,v+h],'texture':0} for f,(u,v,w,h) in face_uv(*c['uv'],*c['size']).items()}})
  def group(b):return {'name':b['name'],'uuid':ident('bone/'+b['name']),'origin':b['pivot'],'children':[ident(p['name']) for p in self.parts if p['bone']==b['name']]+[group(k) for k in self.bones if k.get('parent')==b['name']]}
  project={'meta':{'format_version':'4.10','model_format':'bedrock','box_uv':True},'name':stem,'resolution':{'width':S,'height':S},'elements':elements,'outliner':[group(self.bones[0])],'textures':[{'id':'0','name':stem+'.png','uuid':ident('texture'),'source':'data:image/png;base64,'+base64.b64encode((OUT/(stem+'.png')).read_bytes()).decode()}]}
  write(OUT/(stem+'.bbmodel'),project);print(stem,len(self.parts),'cuboids')

def terminal():
 m=Model('terminal');m.bone('platform');m.bone('arch');m.bone('coils');m.bone('core',pivot=(0,27,6));m.bone('beam')
 # Octagonal base made of five intentional intersecting structural slabs.
 for x,z,w,d in [(-14,-20,28,40),(-20,-14,40,28)]:m.cube('platform',(x,0,z),(w,3,d),'metal')
 m.cube('platform',(-13,3,-17),(26,2,34),'cyan');m.cube('platform',(-17,3,-13),(34,2,26),'cyan');m.cube('platform',(-7,5,-7),(14,1,14),'black')
 for side in [-1,1]:
  x=side*18
  m.cube('arch',(x-3,3,5),(6,36,7),'metal',pattern='ribs')
  m.cube('arch',(x-1,6,4),(2,32,1),'cyan')
  m.cube('arch',(x-3,36,5),(6,12,7),'metal',(0,0,side*18),(x,36,8),pattern='ribs')
  m.cube('arch',(side*15-3,46,4),(6,12,8),'metal',(0,0,side*49),(side*15,46,8),pattern='ribs')
  m.cube('coils',(side*22-1,4,-4),(2,41,2),'silver')
  for y in [31,35,39]:m.cube('coils',(side*22-4,y,-7),(8,2,8),'silver')
  m.cube('coils',(side*22-2,43,-5),(4,3,4),'cyan')
  m.cube('platform',(side*22-4,2,-1),(8,15,6),'metal',(side*8,0,side*16),(side*22,3,0))
  for y in [12,23]:m.cube('arch',(side*14,y,8),(6,1,2),'black',(0,0,side*12),(side*17,y,9))
 m.cube('arch',(-7,52,3),(14,6,9),'metal',pattern='ribs')
 m.cube('core',(-8,24,6),(16,16,4),'cyan',pattern='core');m.cube('core',(-6,26,5),(12,12,1),'metal',pattern='core')
 for x in [-8,6]:m.cube('beam',(x,6,6),(2,17,2),'cyan')
 m.export({'core':{'position':{'0':[0,0,0],'2':[0,2,0],'4':[0,0,0]}}})

def treasure():
 m=Model('treasure');m.bone('body');m.bone('lid');m.bone('spark',pivot=(0,20,0))
 for x in [-10,7]:
  for z in [-7,4]:m.cube('body',(x,0,z),(3,5,3),'metal')
 m.cube('body',(-11,4,-8),(22,11,16),'black');m.cube('body',(-12,4,-9),(24,2,18),'silver')
 m.cube('lid',(-12,15,-9),(24,3,18),'silver');m.cube('lid',(-10,18,-7),(20,2,14),'metal')
 for x in [-9,7]:m.cube('body',(x,6,-10),(2,12,1),'gold');m.cube('lid',(x,20,-7),(2,1,14),'gold')
 m.cube('body',(-2,10,-10),(4,6,2),'gold');m.cube('body',(-1,12,-11),(2,2,1),'cyan')
 m.cube('spark',(-1,23,-1),(2,4,2),'purple',(0,0,45),(0,25,0));m.export({'spark':{'position':{'0':[0,0,0],'2':[0,3,0],'4':[0,0,0]}}})

def monad():
 m=Model('monad');m.bone('frame');m.bone('leaf');m.bone('crest')
 m.cube('leaf',(-16,4,-2),(32,56,4),'red',pattern='sigil');m.cube('leaf',(-16,60,-2),(32,18,4),'red',pattern='taper');m.cube('frame',(-22,0,-5),(44,4,10),'black')
 for side in [-1,1]:
  m.cube('frame',(side*19-3,3,-4),(6,55,8),'black',pattern='ribs')
  m.cube('frame',(side*16-1,4,-5),(2,55,1),'purple')
  m.cube('frame',(side*18-3,54,-4),(6,29,8),'black',(0,0,side*38),(side*18,55,0))
  m.cube('frame',(side*18-1,54,-5),(2,27,1),'red',(0,0,side*38),(side*18,55,0))
  m.cube('crest',(side*24-2,34,-4),(4,4,3),'purple',(0,0,45),(side*24,36,-2))
 m.cube('crest',(-3,73,-5),(6,6,6),'purple',(0,0,45),(0,76,-2));m.export({})

terminal();treasure();monad()
for name,color in [('amber',(208,156,34)),('rose',(206,49,107))]:
 stem='tartarus_'+name;im=Image.new('RGB',(16,16));pix=im.load()
 for y in range(16):
  for x in range(16):pix[x,y]=tuple(max(0,min(255,v+(-100 if x%7<2 or y%7<2 else 20))) for v in color)
 im.save(ASSETS/'textures/block'/(stem+'.png'))
 for folder,obj in [('models/block',{'parent':'minecraft:block/cube_all','textures':{'all':'velvet_eclipse:block/'+stem}}),('models/item',{'parent':'velvet_eclipse:block/'+stem}),('blockstates',{'variants':{'':{'model':'velvet_eclipse:block/'+stem}}})]:
  (ASSETS/folder/(stem+'.json')).write_text(json.dumps(obj),encoding='utf-8')
for locale in ['pt_br','en_us']:
 path=ASSETS/'lang'/(locale+'.json');obj=json.loads(path.read_text(encoding='utf-8'))
 for key,pt,en in [('terminal','Terminal do Tártaro','Tartarus Terminal'),('treasure','Tesouro do Tártaro','Tartarus Treasure'),('monad','Porta de Monad','Monad Door')]:obj['entity.velvet_eclipse.tartarus_'+key]=pt if locale=='pt_br' else en
 for key,pt,en in [('amber','Vitral âmbar','Amber Window'),('rose','Vitral carmesim','Crimson Window')]:obj['block.velvet_eclipse.tartarus_'+key]=pt if locale=='pt_br' else en
 path.write_text(json.dumps(obj,indent=2,ensure_ascii=False)+'\n',encoding='utf-8')
