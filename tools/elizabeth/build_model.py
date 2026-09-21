"""Elizabeth: standalone cuboid/Box UV study. Never writes runtime mod resources."""
from pathlib import Path
import json, math, uuid, base64
from PIL import Image, ImageDraw
ROOT=Path(__file__).resolve().parent
OUT=ROOT/'draft';DENSITY=4;S=512
PAL={
 'blue':['#091846','#123497','#2056dd','#487cf8'],
 'panel':['#0a1747','#1732a1','#254bea','#5584ff'],
 'trim':['#050b24','#101939','#23326c','#44579e'],
 'skin':['#bf9eac','#dcc5c5','#f3e2d8','#fff2e5'],
 'hair':['#9cabbf','#c8d2df','#e4eaf0','#fffaf0'],
 'shoe':['#09132e','#122563','#224baf','#4776d4'],
 'stocking':['#202a46','#354059','#566078','#778197'],
 'paper':['#a28e70','#c9bca1','#e6ddc8','#fff7de'],
 'cover':['#292d45','#4a4c68','#73758d','#999bae'],
 'gold':['#665336','#a1905d','#d1c18c','#f3e5b5'],
}
def face_uv(u,v,w,h,d):
 return {'north':(u+d,v+d,w,h),'south':(u+2*d+w,v+d,w,h),'west':(u+d+w,v+d,d,h),'east':(u,v+d,d,h),'up':(u+d,v,w,d),'down':(u+d+w,v,w,d)}
def paint(w,h,mat,face,pattern=''):
 p=PAL[mat];shade={'north':2,'south':1,'up':3,'down':0,'west':1,'east':1}[face]
 im=Image.new('RGBA',(w,h),p[shade]);d=ImageDraw.Draw(im)
 if mat in ('blue','panel','shoe','cover') and min(w,h)>3:
  d.line((0,0,w-1,0),fill=p[3]);d.line((w-1,1,w-1,h-1),fill=p[0]);d.line((1,h-1,w-2,h-1),fill=p[1])
 if mat=='hair' and w>2 and h>4:
  for x in range(2,w-1,5):
   d.line((x,1,max(0,x-1),h-2),fill=p[1]);d.line((x+1,1,x,h-3),fill=p[3])
 if pattern=='cloth' and face in ('north','south') and w>5:
  d.line((1,1,1,h-2),fill=p[3]);d.line((w-2,1,w-2,h-1),fill=p[0])
 if pattern=='seam' and face in ('north','south'):
  d.line((w//2,0,w//2,h-1),fill=PAL['trim'][2])
 if pattern=='hem' and face in ('north','south','east','west'):
  d.line((0,h-2,w-1,h-2),fill=PAL['trim'][2]);d.line((0,h-1,w-1,h-1),fill=PAL['trim'][0])
 if pattern=='pages' and face in ('east','west','up','down'):
  if face in ('east','west'):
   for x in range(1,w,2):d.line((x,1,x,h-2),fill=p[1])
  else:
   for y in range(1,h,2):d.line((1,y,w-2,y),fill=p[1])
 if pattern in ('button','eye_l','eye_r','mouth','brow','emblem'):
  im=Image.new('RGBA',(w,h));d=ImageDraw.Draw(im)
  if face!='north':return im
  if pattern=='button':
   cx,cy=(w-1)/2,(h-1)/2
   for y in range(h):
    for x in range(w):
     r=math.hypot((x-cx)/(w/2),(y-cy)/(h/2))
     color=None
     if r<.99:color='#080d30'
     if .63<r<.87:color='#4e7ac7'
     if .66<r<.80 and y<cy:color='#9abde7'
     if .16<r<.23:color='#46381d'
     if r<=.16:color='#e2cc77'
     if color:d.point((x,y),fill=color)
  elif pattern.startswith('eye'):
   # Warm gold eyes, deliberate upper lashes, one-pixel highlights.
   d.polygon([(0,1),(2,0),(w-2,0),(w-1,1),(w-2,h-1),(1,h-1)],fill='#514359')
   d.polygon([(1,1),(w-2,1),(w-2,h-2),(1,h-2)],fill='#fff3e7')
   cx=w//2;d.rectangle((cx-1,1,cx+1,h-2),fill='#b39a3d');d.line((cx,1,cx,h-2),fill='#483c40');d.point((cx-1,1),fill='#fffbe1')
   d.line((1,0,w-2,0),fill='#514359');d.point((0,0) if pattern=='eye_r' else (w-1,0),fill='#514359')
  elif pattern=='mouth':
   d.line((1,0,w-2,0),fill='#bc8f97');d.point((w//2,1),fill='#e1afb2')
  elif pattern=='brow':d.line((0,h-1,w-1,0),fill='#afb3bd')
  elif pattern=='emblem':
   d.polygon([(w//2,1),(w-2,h//2),(w//2,h-2),(1,h//2)],outline='#aac6ec');d.line((w//2,3,w//2,h-4),fill='#718fbf')
 if pattern=='book' and face in ('north','south'):
  d.rectangle((2,2,w-3,h-3),outline=PAL['gold'][1]);d.rectangle((4,4,w-5,h-5),outline=PAL['gold'][2])
  cx,cy=w//2,h//2;r=min(w//3,h//4)
  d.polygon([(cx,cy-r),(cx+r,cy),(cx,cy+r),(cx-r,cy)],outline=PAL['gold'][3])
  d.line((cx,cy-r+2,cx,cy+r-2),fill=PAL['gold'][2]);d.line((cx-r+2,cy,cx+r-2,cy),fill=PAL['gold'][2])
  for y in [7,h-8]:d.line((w//3,y,2*w//3,y),fill=PAL['gold'][2])
 if pattern=='spine' and face in ('north','south','east','west'):
  for y in [3,6,h-7,h-4]:d.line((0,y,w-1,y),fill=PAL['gold'][2])
 return im
class Model:
 def __init__(self):self.bones=[];self.parts=[];self.atlas=Image.new('RGBA',(S,S));self.x=self.y=2;self.row=0
 def bone(self,name,parent=None,pivot=(0,0,0),rotation=None):
  b={'name':name,'pivot':[round(v*DENSITY,5) for v in pivot]}
  if parent:b['parent']=parent
  if rotation:b['rotation']=list(rotation)
  self.bones.append(b);return name
 def cube(self,bone,name,o,size,mat='blue',pattern='',rotation=None,pivot=None):
  w,h,d=[round(v*DENSITY) for v in size];assert all(v>0 for v in (w,h,d)),name
  assert all(abs(v*DENSITY-i)<1e-5 for v,i in zip(size,[w,h,d])),name
  aw,ah=2*(w+d),h+d
  if self.x+aw+2>S:self.x=2;self.y+=self.row+2;self.row=0
  assert self.y+ah+2<S,('atlas full',name)
  uv=[self.x,self.y];self.x+=aw+2;self.row=max(self.row,ah)
  for f,(u,v,fw,fh) in face_uv(*uv,w,h,d).items():self.atlas.paste(paint(fw,fh,mat,f,pattern),(u,v))
  c={'origin':[round(v*DENSITY,5) for v in o],'size':[w,h,d],'uv':uv}
  if rotation:c.update(rotation=list(rotation),pivot=[round(v*DENSITY,5) for v in (pivot or o)])
  next(b for b in self.bones if b['name']==bone).setdefault('cubes',[]).append(c)
  self.parts.append({'name':name,'bone':bone,'material':mat,'pattern':pattern,'cube':c})
 def segment(self,bone,name,a,b,width,depth,mat='blue',pattern=''):
  dx,dy,dz=[v-u for u,v in zip(a,b)];length=math.sqrt(dx*dx+dy*dy+dz*dz)
  rx=math.degrees(math.atan2(dz,math.hypot(dx,dy)));rz=math.degrees(math.atan2(-dx,dy))
  self.cube(bone,name,(a[0]-width/2,a[1],a[2]-depth/2),(width,math.ceil(length*4)/4,depth),mat,pattern,(rx,0,rz),a)
def build():
 m=Model();B=m.bone;C=m.cube;L=m.segment
 B('elizabeth');B('hips','elizabeth',(0,21,0));B('body','hips',(0,22,0))
 # Deliberately feminine proportions: narrow waist, bare shoulders and fitted bodice.
 C('body','fitted_waist',(-2.75,21,-1.75),(5.5,3.5,3.5),'blue','cloth')
 C('body','bodice',(-3.25,24,-2),(6.5,4.25,4),'blue','cloth')
 C('body','front_princess_panel',(-1.75,22,-2.3),(3.5,6,0.25),'panel','cloth')
 for s,label in [(-1,'right'),(1,'left')]:
  C('body',label+'_bodice_side',(s*2.7-.625,22,-1.6),(1.25,5.5,3.25),'blue',rotation=(0,0,-s*8),pivot=(s*2.7,22,0))
  C('body',label+'_white_armhole_piping',(s*3-.125,25.5,-2.12),(.25,2.75,.25),'paper',rotation=(0,0,s*10),pivot=(s*3,27.5,-2))
 B('collar','body',(0,28,0))
 C('collar','high_collar',(-1.7,27.5,-1.75),(3.5,2.5,3.25),'blue','seam')
 C('collar','collar_front_opening',(-.125,28,-1.82),(.25,2,.25),'trim')
 C('collar','left_collar_fold',(.2,28.25,-2),(1.75,1.75,.25),'panel',rotation=(-8,0,-12),pivot=(.5,29.5,-1.8))
 C('collar','right_collar_fold',(-1.9,28.25,-1.95),(1.75,1.75,.25),'blue',rotation=(-8,0,12),pivot=(-.5,29.5,-1.8))
 C('body','top_ring',(-1.25,25.6,-2.45),(2.5,2.5,.25),'trim','button')
 C('body','waist_ring',(-1.25,22.35,-2.62),(2.5,2.5,.25),'trim','button')
 # Fitted dress: continuous waist width/depth, modest lower flare only.
 B('skirt','hips',(0,22.5,0))
 C('skirt','inner_dress',(-2.65,9.75,-1.6),(5.25,12.75,3.25),'trim')
 B('skirt_front','skirt',(0,22.5,-1.8))
 C('skirt_front','center_front_skirt',(-2.75,9.5,-2),(5.5,13,.5),'panel','hem')
 for i,y in enumerate([19.3,16.05,12.8]):C('skirt_front','skirt_ring_'+str(i),(-1.375,y-1.375,-2.28),(2.75,2.75,.25),'trim','button')
 B('skirt_back','skirt',(0,22.5,1.5))
 C('skirt_back','center_back_skirt',(-2.75,9.5,1.4),(5.5,13,.5),'blue','seam')
 for s,label in [(-1,'right'),(1,'left')]:
  B('skirt_'+label,'skirt',(s*2.5,22.5,0),(0,0,s*6.5))
  x=0 if s>0 else -2.75
  C('skirt_'+label,label+'_front_skirt_panel',(x,9.5,-1.9),(2.75,13,.5),'blue','hem')
  C('skirt_'+label,label+'_back_skirt_panel',(x,9.5,1.3),(2.75,13,.5),'blue','hem')
  C('skirt_'+label,label+'_side_skirt_panel',(2.52 if s>0 else -2.77,9.5,-1.9),(.25,13,3.5),'blue','hem')
 # Legs and elegant heels; feet grounded, not floating in the idle.
 for s,label in [(-1,'right'),(1,'left')]:
  x=s*1.9;B(label+'_leg','hips',(x,20,0))
  C(label+'_leg',label+'_stocking',(x-1,2.1,-.95),(2,17,2),'stocking')
  B(label+'_foot',label+'_leg',(x,1.75,-.1),(0,-s*7,0))
  C(label+'_foot',label+'_heel',(x-.65,0,.25),(1.25,1.75,1.25),'shoe')
  C(label+'_foot',label+'_shoe',(x-1.1,.75,-2.35),(2.25,1.25,3.75),'shoe')
  C(label+'_foot',label+'_toe',(x-1,.5,-3),(2,1,1.5),'shoe')
  C(label+'_foot',label+'_ankle_top',(x-.9,1.5,-.9),(1.75,1.25,1.75),'blue')
 # Arms: bare upper arms, long blue gloves, separate fingers.
 B('right_upper_arm','body',(-3.8,27.5,0))
 L('right_upper_arm','right_bare_arm',(-5.65,22.1,-.2),(-3.7,27.3,0),1.75,1.75,'skin')
 C('right_upper_arm','right_shoulder',(-4.6,26,-.95),(1.75,1.5,1.75),'skin')
 B('right_forearm','right_upper_arm',(-5.65,22.1,-.2))
 L('right_forearm','right_blue_glove',(-7.9,18.7,-1.15),(-5.65,22.2,-.2),1.5,1.5,'blue','cloth')
 L('right_forearm','right_flared_cuff',(-6.1,21.35,-.4),(-5.5,22.4,-.1),2,1.75,'panel')
 B('right_hand','right_forearm',(-7.9,18.7,-1.15),(12,0,15))
 C('right_hand','right_palm',(-8.8,17.25,-1.6),(1.75,1.75,.75),'blue')
 for i in range(3):
  x=-8.65+i*.55;C('right_hand','right_finger_'+str(i),(x,15.9+(i==2)*.25,-1.55),(.5,1.5,.5),'blue',rotation=(-18,0,(i-1)*5),pivot=(x,17.5,-1.3))
 C('right_hand','right_thumb',(-7.05,17.1,-1.65),(.5,1.25,.5),'panel',rotation=(-12,0,-24),pivot=(-7.05,18,-1.3))
 B('left_upper_arm','body',(3.8,27.5,0))
 L('left_upper_arm','left_bare_arm',(5.8,22,.1),(3.7,27.3,0),1.75,1.75,'skin')
 C('left_upper_arm','left_shoulder',(2.85,26,-.95),(1.75,1.5,1.75),'skin')
 B('left_forearm','left_upper_arm',(5.8,22,.1))
 L('left_forearm','left_blue_glove',(5.8,22,.1),(6.95,24.75,-3.05),1.5,1.5,'blue','cloth')
 L('left_forearm','left_cuff',(5.6,21.9,.2),(6.1,22.75,-.8),2,1.75,'panel')
 B('left_hand','left_forearm',(6.95,24.75,-3.05))
 C('left_hand','left_palm',(6.1,24.35,-3.7),(1.75,1.5,.75),'blue',rotation=(-20,0,-12),pivot=(6.95,24.75,-3.05))
 B('book','left_hand',(6.5,24.6,-2.1),(-9,16,-14))
 C('book','book_page_block',(4.4,24.3,-2.8),(4.5,7,1.25),'paper','pages')
 C('book','front_compendium_cover',(4.15,24.1,-3.12),(5,7.5,.25),'cover','book')
 C('book','back_compendium_cover',(4.15,24.1,-1.5),(5,7.5,.25),'cover','book')
 C('book','bound_spine',(4.1,24.1,-3),(0.5,7.5,1.5),'cover','spine')
 C('book','gold_clasp',(8.85,27.2,-3.25),(.5,1,.5),'gold')
 for i in range(3):C('left_hand','left_gripping_finger_'+str(i),(6.0+i*.5,24.8,-3.95),(.5,1.5,.5),'panel',rotation=(-24,0,0),pivot=(6.0+i*.5,24.9,-3.5))
 C('left_hand','left_thumb',(7.65,24.6,-3.05),(.5,1.25,.5),'blue',rotation=(0,0,30),pivot=(7.8,25.4,-3))
 # Entirely new face and silhouette; not a reused Igor/Izanagi head.
 B('neck','body',(0,29,0));C('neck','neck',(-.875,28.7,-.8),(1.75,2.25,1.75),'skin')
 B('head','neck',(0,30.6,0),(0,-3,0))
 C('head','head_main',(-2.5,32,-2.25),(5,3.75,4.25),'skin')
 C('head','jaw',(-2,30.75,-2.2),(4,1.5,3.75),'skin')
 C('head','chin',(-1.3,30.35,-2),(2.5,.75,3.25),'skin')
 for s,label in [(-1,'right'),(1,'left')]:
  C('head',label+'_cheek',(s*2.05-.45,31.5,-2.27),(1,1.75,1),'skin',rotation=(0,0,-s*10),pivot=(s*2.05,32.5,-1.8))
  C('head',label+'_ear',(s*2.5-.35,32.2,-.55),(.75,1.75,1),'skin')
  B('face_'+label,'head',(s*1.2,33.4,-2.25))
  C('face_'+label,label+'_eye',(s*1.15-.875,33.1,-2.52),(1.75,1,.25),'skin','eye_l' if s>0 else 'eye_r')
  C('face_'+label,label+'_brow',(s*1.15-.875,34.35,-2.53),(1.75,.5,.25),'hair','brow',rotation=(0,0,-s*5),pivot=(s*1.15,34.5,-2.3))
 C('head','small_nose',(-.25,32.35,-2.82),(.5,1,.5),'skin')
 C('head','soft_smile',(-.625,31.3,-2.46),(1.25,.5,.25),'skin','mouth')
 # A rounded bob expressed with a small number of stepped and rotated locks.
 B('hair_back','head',(0,35.2,1.4))
 C('hair_back','bob_back',(-2.85,32.2,1.5),(5.75,4.5,1.5),'hair')
 C('hair_back','bob_nape',(-2.2,31.4,1.45),(4.5,1.25,1.25),'hair')
 C('hair_back','hair_crown',(-2.7,35.5,-1.9),(5.5,1.5,4.5),'hair')
 C('hair_back','hair_upper_cap',(-2.25,36.8,-1.55),(4.5,.75,3.75),'hair')
 for s,label in [(-1,'right'),(1,'left')]:
  B('hair_'+label,'head',(s*2.5,35.7,0))
  C('hair_'+label,label+'_bob_volume',(s*2.7-.5,32,-1.5),(1.25,4,3.75),'hair',rotation=(0,0,s*5),pivot=(s*2.7,35.5,0))
  C('hair_'+label,label+'_bob_end',(s*2.65-.5,31.5,-1.25),(1,1.5,3),'hair',rotation=(0,0,-s*13),pivot=(s*2.65,32.8,0))
  C('hair_'+label,label+'_cheek_lock',(s*2.5-.5,31.9,-2.0),(1,3,.75),'hair',rotation=(-4,0,-s*10),pivot=(s*2.5,34.7,-1.7))
 B('hair_fringe','head',(.45,36,-1.6))
 C('hair_fringe','broad_swept_fringe',(-2.6,34.3,-2.7),(2.75,2.5,.75),'hair',rotation=(0,-5,-25),pivot=(.2,36.3,-2.15))
 C('hair_fringe','swept_tip',(-2.8,33.6,-2.75),(1,1.75,.5),'hair',rotation=(0,0,-17),pivot=(-1.9,35.2,-2.5))
 C('hair_fringe','short_front_fringe',(.15,34.55,-2.55),(2.25,1.75,.75),'hair',rotation=(0,0,8),pivot=(.45,36,-2.15))
 C('hair_fringe','part_lock',(-.15,35,-2.72),(.75,1.75,.75),'hair',rotation=(0,0,23),pivot=(.45,36.4,-2.25))
 # Small tilted blue pillbox hat: flat crown and dark rim, no rounded mesh.
 B('hat','head',(.4,36.7,.2),(-5,0,-9))
 C('hat','dark_hat_rim',(-2.1,36.9,-1.9),(5,0.5,4),'trim')
 C('hat','blue_hat_front',(-1.9,37.35,-1.65),(4.5,1.75,3.5),'blue')
 C('hat','hat_left_facet',(-2.35,37.25,-1.1),(.75,1.5,2.5),'blue')
 C('hat','hat_right_facet',(2.25,37.25,-1.1),(.75,1.5,2.5),'blue')
 C('hat','flat_blue_crown',(-1.6,39.0,-1.35),(4,0.25,3),'panel')
 C('hat','hat_emblem',(-.1,37.6,-1.94),(1,1,.25),'blue','emblem')
 # Original subtle idle: feet fixed, small breathing/head/book motion and soft hair sway.
 duration=4.8;times=[0,1.2,2.4,3.6,4.8]
 tracks={}
 for name,axis,amp in [('body',0,.35),('head',1,1.2),('hair_right',2,.7),('hair_left',2,-.7),('hair_fringe',0,.5),('skirt_right',2,.3),('skirt_left',2,-.3),('right_forearm',0,.8),('book',0,.5)]:
  keys={}
  for t in times:
   v=[0,0,0];v[axis]=round(math.sin(t/duration*math.tau)*amp,5);keys[str(t)]=v
  tracks[name]={'rotation':keys}
 animation={'format_version':'1.8.0','animations':{'misc.idle':{'loop':True,'animation_length':duration,'bones':tracks}}}
 geo={'format_version':'1.12.0','minecraft:geometry':[{'description':{'identifier':'geometry.elizabeth_study','texture_width':S,'texture_height':S,'visible_bounds_width':8,'visible_bounds_height':12,'visible_bounds_offset':[0,5,0]},'bones':m.bones}]}
 OUT.mkdir(exist_ok=True)
 for name,obj in [('elizabeth.geo.json',geo),('elizabeth.animation.json',animation),('parts.json',m.parts)]:(OUT/name).write_text(json.dumps(obj,indent=2)+'\n',encoding='utf-8')
 m.atlas.save(OUT/'elizabeth.png');export_bb(m,animation)
 print(f'Elizabeth: {len(m.parts)} cuboids, {len(m.bones)} bones, {S}x{S} Box UV; atlas used {m.y+m.row}px.')
def export_bb(m,animation):
 def ident(name):return str(uuid.uuid5(uuid.NAMESPACE_URL,'velvet-eclipse/elizabeth-study/'+name))
 elements=[]
 for part in m.parts:
  c=part['cube'];a=c['origin'];b=[x+y for x,y in zip(a,c['size'])]
  elements.append({'name':part['name'],'type':'cube','uuid':ident(part['name']),'from':a,'to':b,'origin':c.get('pivot',a),'rotation':c.get('rotation',[0,0,0]),'box_uv':True,'uv_offset':c['uv'],'autouv':0,'color':0,'faces':{f:{'uv':[u,v,u+w,v+h],'texture':0} for f,(u,v,w,h) in face_uv(*c['uv'],*c['size']).items()}})
 def group(b):return {'name':b['name'],'origin':b['pivot'],'rotation':b.get('rotation',[0,0,0]),'uuid':ident('bone/'+b['name']),'export':True,'isOpen':True,'children':[ident(p['name']) for p in m.parts if p['bone']==b['name']]+[group(child) for child in m.bones if child.get('parent')==b['name']]}
 animators={}
 for name,tracks in animation['animations']['misc.idle']['bones'].items():animators[ident('bone/'+name)]={'name':name,'type':'bone','keyframes':[{'channel':channel,'data_points':[{'x':v[0],'y':v[1],'z':v[2]}],'time':float(t),'color':-1,'interpolation':'linear','uuid':ident(name+channel+t)} for channel,keys in tracks.items() for t,v in keys.items()]}
 project={'meta':{'format_version':'4.10','model_format':'bedrock','box_uv':True},'name':'Elizabeth — estudo independente','model_identifier':'elizabeth_study','visible_box':[8,12,5],'resolution':{'width':S,'height':S},'elements':elements,'outliner':[group(b) for b in m.bones if 'parent' not in b],'textures':[{'name':'elizabeth.png','id':'0','uuid':ident('texture'),'path':'elizabeth.png','source':'data:image/png;base64,'+base64.b64encode((OUT/'elizabeth.png').read_bytes()).decode(),'mode':'bitmap','saved':True}],'animations':[{'uuid':ident('animation/idle'),'name':'misc.idle','loop':'loop','length':4.8,'animators':animators}]}
 (OUT/'elizabeth.bbmodel').write_text(json.dumps(project,indent=2)+'\n',encoding='utf-8')
if __name__=='__main__':build()
