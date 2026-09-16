"""Igor cuboid study. Writes only to tools/igor/draft, never to runtime resources.

Bedrock entity, Box UV, one uniform quarter-unit authoring grid. The large
authoring scale allows crisp facial pixels; runtime scale is a later decision.
"""
from pathlib import Path
import json, math, uuid, base64
from PIL import Image, ImageDraw

ROOT=Path(__file__).resolve().parent
OUT=ROOT/'draft'
DENSITY=4
S=512
P={
 'suit':['#0c101c','#1a2030','#303545','#454c5b'],
 'lapel':['#111622','#272d3b','#424755','#5c626c'],
 'shirt':['#7e8b9c','#b0bac6','#d9dfe3','#f3f1e6'],
 'glove':['#788897','#a9b7c1','#dce3e3','#f8f7ee'],
 'skin':['#aa8180','#c8a69c','#e6c6b5','#f6ddcc'],
 'nose':['#a77a72','#c59a88','#e0bba2','#f1d4ba'],
 'ear':['#875d65','#ae7a7f','#d4a397','#e9c4b0'],
 'hair':['#7e899a','#aab6c7','#dbe3e7','#f4f3eb'],
 'brow':['#080d19','#101725','#1b2433','#2a3341'],
 'shoe':['#080d17','#141c2b','#263043','#3d4960'],
 'silver':['#535f73','#8795a8','#c0c9d2','#e2e6e9'],
}

def face_uv(u,v,w,h,d):
 return {'north':(u+d,v+d,w,h),'south':(u+2*d+w,v+d,w,h),
         'west':(u+d+w,v+d,d,h),'east':(u,v+d,d,h),
         'up':(u+d,v,w,d),'down':(u+d+w,v,w,d)}

def paint(w,h,mat,face,pattern):
 p=P[mat];level={'north':2,'up':3,'south':1,'down':0,'east':1,'west':1}[face]
 im=Image.new('RGBA',(max(1,w),max(1,h)),p[level]);d=ImageDraw.Draw(im)
 if not w or not h:return im
 if mat in ('suit','lapel') and w>=4 and h>=6:
  d.line((0,0,0,h-1),fill=p[min(level+1,3)])
  d.line((w-1,0,w-1,h-1),fill=p[0])
  if h>15:
   d.line((w//3,4,w//3,h//3),fill=p[1])
   d.line((w//3+1,h//3,w//3+1,h-4),fill=p[1])
 if mat in ('glove','shirt'):
  d.line((0,0,w-1,0),fill=p[3]);d.line((w-1,0,w-1,h-1),fill=p[0])
  if w>6 and h>6:d.line((1,2,1,h-3),fill=p[3])
 if mat=='hair' and w>2 and h>3:
  for x in range(1,w-1,3):d.line((x,1,x,h-1),fill=p[(x//3)%2+1])
  d.line((0,0,0,h-2),fill=p[3])
 if mat=='nose' and face in ('north','west','east') and w>=2:
  d.line((0,0,0,h-1),fill=p[3]);d.line((w-1,0,w-1,h-1),fill=p[0])
 if pattern=='forehead' and face=='north':
  for y,width in [(3,w//2),(6,w//3)]:
   if y<h:d.line((w//2-width//2,y,w//2+width//2,y),fill=P['skin'][1])
  d.line((w//2-2,h-5,w//2-1,h-2),fill=P['skin'][0]);d.line((w//2+2,h-5,w//2+1,h-2),fill=P['skin'][0])
 if pattern=='cheek' and face=='north':
  for y in [2,5,8]:
   if y<h-1:d.line((1,y,max(1,w-3),y+1),fill=P['skin'][0])
  d.line((w-2,1,w-2,h-2),fill=P['skin'][1])
 if pattern=='mouth' and face=='north':
  # A continuous curved row of teeth, separated by fine dark seams.
  for x in range(2,w-2):
   k=(x-(w-1)/2)/((w-5)/2);top=2+round(2*max(0,1-k*k))
   d.line((x,top-1,x,min(h-2,top+3)),fill='#332936')
   d.line((x,top,x,min(h-3,top+2)),fill='#eee9df')
   if x%3==0:d.line((x,top+1,x,min(h-3,top+2)),fill='#817984')
  d.line((3,h-1,w-4,h-1),fill=P['skin'][1])
 if pattern.startswith('eye') and face in ('north','south'):
  im=Image.new('RGBA',(w,h),(0,0,0,0));d=ImageDraw.Draw(im)
  border=[(3,0),(w-4,0),(w-1,3),(w-1,h-4),(w-4,h-1),(3,h-1),(0,h-4),(0,3)]
  d.polygon(border,fill='#434453')
  d.polygon([(3,1),(w-4,1),(w-2,3),(w-2,h-4),(w-4,h-2),(3,h-2),(1,h-4),(1,3)],fill='#eff2ec')
  d.line((3,h-3,w-4,h-3),fill='#b3c1d1')
  cx=w//2+(1 if pattern.endswith('right') else -1);cy=h//2
  d.rectangle((cx-2,cy-2,cx+2,cy+2),fill='#9daecc')
  d.rectangle((cx-1,cy-1,cx,cy+1),fill='#15182a');d.point((cx-1,cy-2),fill='#ffffff')
 if pattern.startswith('eye') and face not in ('north','south'):
  im=Image.new('RGBA',(w,h),(0,0,0,0))
 if pattern=='glove_back' and face in ('up','north') and w>=7:
  for x in [w//3,w//2,2*w//3]:d.line((x,max(1,h//3),x,max(1,2*h//3)),fill=P['glove'][1])
 if pattern=='cuff' and face in ('north','east','west'):
  d.line((0,h-2,w-1,h-2),fill=P['glove'][0])
 if pattern=='shoe' and face=='up':
  d.line((2,2,w-3,2),fill=P['shoe'][3])
  for y in range(h//2,max(h//2+1,h-2),3):d.line((w//3,y,2*w//3,y),fill=P['shoe'][0])
 if pattern=='jacket' and face=='north':
  for y in [h//2,h-5]:
   d.rectangle((w//2-1,y,w//2+1,y+2),fill=P['silver'][0]);d.point((w//2-1,y),fill=P['silver'][2])
 return im

def build():
 OUT.mkdir(parents=True,exist_ok=True)
 atlas=Image.new('RGBA',(S,S));bones=[];parts=[];bx=by=1;row=0
 def bone(name,parent=None,pivot=(0,0,0)):
  b={'name':name,'pivot':[v*DENSITY for v in pivot]}
  if parent:b['parent']=parent
  bones.append(b);return name
 def cube(group,name,origin,size,mat='suit',pattern='',rotation=None,pivot=None):
  nonlocal bx,by,row
  w,h,d=[round(v*DENSITY) for v in size]
  assert all(abs(v*DENSITY-n)<1e-5 for v,n in zip(size,[w,h,d])),name
  aw,ah=2*(w+d),h+d
  if bx+aw+1>S:bx=1;by+=row+2;row=0
  assert by+ah+1<=S,'Atlas is full'
  uv=[bx,by];bx+=aw+2;row=max(row,ah)
  for f,(u,v,fw,fh) in face_uv(*uv,w,h,d).items():
   if fw and fh:atlas.paste(paint(fw,fh,mat,f,pattern),(u,v))
  c={'origin':[round(n*DENSITY,6) for n in origin],'size':[w,h,d],'uv':uv}
  if rotation:c.update(rotation=list(rotation),pivot=[n*DENSITY for n in (pivot or origin)])
  next(b for b in bones if b['name']==group).setdefault('cubes',[]).append(c)
  parts.append({'name':name,'bone':group,'material':mat,'pattern':pattern,'cube':c})
  return c
 def segment(group,name,a,b,width,depth,mat='suit',pattern=''):
  dx,dy,dz=[v-u for u,v in zip(a,b)];length=math.sqrt(dx*dx+dy*dy+dz*dz)
  rx=math.degrees(math.atan2(dz,math.hypot(dx,dy)));rz=math.degrees(math.atan2(-dx,dy))
  return cube(group,name,(a[0]-width/2,a[1],a[2]-depth/2),(width,math.ceil(length*4)/4,depth),mat,pattern,(rx,0,rz),a)

 bone('igor');bone('pelvis','igor',(0,9,1))
 cube('pelvis','seated_hips',(-4,9,-1.25),(8,3,5),'suit')
 # Bent legs, with hips on the chair and both shoes on the ground.
 for s,label in [(-1,'right'),(1,'left')]:
  x=s*2.4;bone(label+'_thigh','pelvis',(x,10,1))
  segment(label+'_thigh',label+'_trousers_thigh',(x,10.5,.75),(x+s*.4,9.25,-5.5),3.25,3.5)
  bone(label+'_calf',label+'_thigh',(x+s*.4,9.25,-5.5))
  segment(label+'_calf',label+'_trousers_calf',(x+s*.4,9.25,-5.5),(x+s*.7,1.5,-5.75),2.5,2.75)
  bone(label+'_foot',label+'_calf',(x+s*.7,1.5,-5.75))
  cube(label+'_foot',label+'_polished_shoe',(x+s*.7-1.5,0,-8.5),(3,1.75,5.5),'shoe','shoe')
  cube(label+'_foot',label+'_shoe_toe',(x+s*.7-1.25,.25,-9),(2.5,1.25,.75),'shoe')
  cube('pelvis',label+'_jacket_tail',(s*2.5-2,10,-1),(4,4.5,1.25),'suit',rotation=(22,0,s*8),pivot=(s*2.5,12,0))
 bone('body','pelvis',(0,11,.5))
 cube('body','jacket_waist',(-3.75,11,-1),(7.5,5.5,4.5),'suit','jacket',rotation=(-9,0,0),pivot=(0,11,.5))
 cube('body','hunched_jacket',(-4.5,15.5,-2.75),(9,6,4.5),'suit',rotation=(-16,0,0),pivot=(0,16,0))
 cube('body','white_shirt',(-1.75,16.5,-4.25),(3.5,5,.5),'shirt',rotation=(-12,0,0),pivot=(0,19,-3))
 segment('body','right_notched_lapel',(-3.5,21,-4.1),(-.8,16.25,-4.35),1.5,.5,'lapel')
 segment('body','left_notched_lapel',(3.5,21,-4.1),(.8,16.25,-4.35),1.5,.5,'lapel')
 for s in [-1,1]:
  cube('body',('right' if s<0 else 'left')+'_collar_tip',(s*1.5-.75,20,-4.8),(1.5,2.25,.5),'shirt',rotation=(0,0,s*27),pivot=(s*1.5,22,-4.5))
 cube('body','black_tie_knot',(-.6,20,-5),(1.25,1.25,.5),'brow',rotation=(0,0,45),pivot=(0,20.6,-4.75))
 cube('body','narrow_black_tie',(-.5,17,-4.75),(1,3.5,.5),'brow',rotation=(-8,0,0),pivot=(0,20,-4.75))
 cube('body','breast_pocket',(2.4,18.5,-3.8),(1.75,.5,.5),'lapel')
 for x,y,angle in [(2.6,19,12),(3.15,19.3,-6),(3.7,19.05,-22)]:
  cube('body','folded_pocket_square_'+str(x).replace('.','_'),(x-.375,y,-4.1),(.75,1.25,.25),'shirt',rotation=(0,0,angle),pivot=(x,y,-4))
 bone('neck','body',(0,21,-2))
 cube('neck','old_neck',(-1.5,20.5,-4),(3,3.25,2.75),'skin')
 bone('head','neck',(0,23,-3))
 cube('head','skull',(-4.5,24,-6.75),(9,6,6.5),'skin')
 cube('head','bald_forehead',(-4,28,-7),(8,2.75,6.25),'skin','forehead')
 cube('head','bald_crown',(-3.25,30,-6.5),(6.5,1.25,5.5),'skin')
 cube('head','jaw',(-3.5,21.75,-6.7),(7,3.5,5.5),'skin')
 cube('head','chin',(-2.5,21.25,-6.25),(5,1.25,4),'skin')
 for s,label in [(-1,'right'),(1,'left')]:
  cube('head',label+'_creased_cheek',(s*3.5-.875,23.25,-7.2),(1.75,2.5,.75),'skin','cheek',rotation=(0,0,-s*9),pivot=(s*3.5,25,-7))
 cube('head','wide_crooked_grin',(-3.5,22.1,-7.5),(7,2.25,.5),'skin','mouth')
 # White hair surrounds a bare crown, with only a few deliberate angled locks.
 bone('hair_back','head',(0,27,-1))
 cube('hair_back','grey_hair_at_back',(-4.5,23,-.75),(9,5.5,1.5),'hair')
 for s,label in [(-1,'right'),(1,'left')]:
  bone('hair_'+label,'head',(s*4,28,-3))
  cube('hair_'+label,label+'_temple',(s*4.25-.625,25,-5.75),(1.25,3.75,4.75),'hair')
  for i,(start,end,width) in enumerate([
   ((4.1,29,-5.7),(6.4,29.7,-2.8),1.25),
   ((4.4,28.4,-4),(6.6,27.6,-.7),1.5),
   ((4.4,27,-2.8),(6.1,24.5,.1),1.25),
   ((4.1,25.5,-1.8),(5.4,22,.4),1.0),
   ((3.5,24.3,-.5),(4.1,21.8,.9),.75)]):
   a=(s*start[0],start[1],start[2]);b=(s*end[0],end[1],end[2]);m=tuple(x+(y-x)*.72 for x,y in zip(a,b));segment('hair_'+label,label+'_hair_lock_'+str(i),a,m,width,.5,'hair');segment('hair_'+label,label+'_hair_tip_'+str(i),m,b,.5,.25,'hair')
  bone('face_'+label+'_ear','head',(s*4.4,26,-3.8))
  segment('face_'+label+'_ear',label+'_pointed_ear',(s*4.1,25,-4),(s*6.1,28,-4.75),1.5,.75,'skin')
  segment('face_'+label+'_ear',label+'_ear_tip',(s*5.6,27,-4.55),(s*6.8,28.5,-4.9),.75,.5,'skin')
  segment('face_'+label+'_ear',label+'_inner_ear',(s*4.7,25.9,-4.6),(s*5.8,27.5,-4.85),.5,.25,'ear')
  bone('face_'+label+'_eye','head',(s*2.25,26.9,-7.4))
  # Raised white centres supply volume; a Box UV plate carries the stepped rim and pupil.
  cube('face_'+label+'_eye',label+'_eyeball_core',(s*2.25-.75,26.15,-7.65),(1.5,1.5,.75),'shirt')
  cube('face_'+label+'_eye',label+'_staring_eye',(s*2.25-1.375,25.525,-7.9),(2.75,2.75,.25),'shirt','eye_'+label,rotation=(0,0,s*7),pivot=(s*2.25,26.9,-7.7))
  bone('face_'+label+'_brow','head',(s*.75,28,-7.75))
  for i,(a,b,w) in enumerate([((.6,27.9,-8.1),(2.25,29.7,-8.1),.75),((2.25,29.7,-8.1),(4.1,29.2,-7.9),.75),((4.1,29.2,-7.9),(5.3,28.5,-7.4),.5)]):
   segment('face_'+label+'_brow',label+'_arched_brow_'+str(i),(s*a[0],a[1],a[2]),(s*b[0],b[1],b[2]),w,.5,'brow')
 bone('face_nose','head',(0,27.6,-7.4))
 # Four native Box UV planes form a closed angular pyramid. Their pixel-cut
 # triangular faces taper continuously; this is not a smoothed mesh or cylinder.
 import numpy as np
 tip=np.array((0,20.35,-13.6));base=np.array((0,27.6,-7.4));axis=(tip-base);axis/=np.linalg.norm(axis)
 horizontal=np.array((1,0,0));depth_axis=np.cross(horizontal,axis);depth_axis/=np.linalg.norm(depth_axis)
 corners=[base+horizontal*x*1.125+depth_axis*z*.875 for x,z in [(-1,-1),(1,-1),(1,1),(-1,1)]]
 for i in range(4):
  a=corners[i];b=corners[(i+1)%4];u=(b-a);width=np.linalg.norm(u);u/=width
  v=(a+b)/2-tip;height=np.linalg.norm(v);v/=height;n=np.cross(u,v);matrix=np.column_stack((u,v,n))
  ry=math.asin(max(-1,min(1,-matrix[2,0])));rx=math.atan2(matrix[2,1],matrix[2,2]);rz=math.atan2(matrix[1,0],matrix[0,0]);rotation=tuple(math.degrees(q) for q in (rx,ry,rz))
  grid_height=math.ceil(height*4)/4
  c=cube('face_nose','tapered_nose_plane_'+str(i),(tip[0]-width/2,tip[1],tip[2]),(width,grid_height,0),'nose','',rotation,tuple(tip))
  w,h,_=c['size'];u0,v0=c['uv']
  for face,(tx,ty,fw,fh) in face_uv(u0,v0,w,h,0).items():
   if fw and fh:
    patch=Image.new('RGBA',(fw,fh));d=ImageDraw.Draw(patch)
    d.polygon([(0,0),(fw-1,0),((fw-1)/2,fh-1)],fill=P['nose'][3 if i==2 else 2 if i in [1,3] else 1])
    d.line((0,0,(fw-1)/2,fh-1),fill=P['nose'][3 if i in [1,2] else 2])
    atlas.paste(patch,(tx,ty))
 # Leaning elbows and raised forearms place the gloved hands together below the nose.
 for s,label in [(-1,'right'),(1,'left')]:
  shoulder=(s*5.1,20.5,-2.8);elbow=(s*6,14.5,-5.7)
  wrist=(-2,18.5,-9.6) if s<0 else (2,19.1,-8.4)
  bone(label+'_upper_arm','body',shoulder)
  segment(label+'_upper_arm',label+'_sleeve_upper',shoulder,elbow,2.5,3,'suit')
  bone(label+'_forearm',label+'_upper_arm',elbow)
  segment(label+'_forearm',label+'_sleeve_forearm',elbow,wrist,2,2.25,'suit')
  cuff=tuple(w+(e-w)*.13 for w,e in zip(wrist,elbow))
  segment(label+'_forearm',label+'_white_cuff',cuff,wrist,2.25,2.5,'shirt','cuff')
  bone(label+'_hand',label+'_forearm',wrist)
  if s<0:
   segment(label+'_hand','right_glove_palm',wrist,(.4,19.3,-9.4),1,2,'glove','glove_back')
   for i in range(4):
    z=-10.1+i*.5;a=(.15,19.4,z);b=(2.3-(abs(i-1.5)*.2),19.3,z-.05);c=(2.65-(abs(i-1.5)*.2),18.45,z-.2)
    bone('right_finger_'+str(i),'right_hand',a);segment('right_finger_'+str(i),'right_finger_'+str(i)+'_first',a,b,.5,.5,'glove');segment('right_finger_'+str(i),'right_finger_'+str(i)+'_tip',b,c,.5,.5,'glove')
   segment('right_hand','right_thumb',(-1.35,19,-8.8),(-.8,20,-8.9),.75,.75,'glove')
  else:
   segment(label+'_hand','left_glove_palm',wrist,(-.1,19.7,-8.5),1,2,'glove','glove_back')
   for i in range(4):
    z=-9.25+i*.5;a=(.2,19.65,z);b=(-1.85+(abs(i-1.5)*.2),19.7,z-.05);c=(-2.3+(abs(i-1.5)*.2),19,z-.15)
    bone('left_finger_'+str(i),'left_hand',a);segment('left_finger_'+str(i),'left_finger_'+str(i)+'_first',a,b,.5,.5,'glove');segment('left_finger_'+str(i),'left_finger_'+str(i)+'_tip',b,c,.5,.5,'glove')
   segment('left_hand','left_thumb',(1.25,19.7,-9.25),(.2,20.25,-9.5),.75,.75,'glove')

 description={'identifier':'geometry.igor_voxel_study','texture_width':S,'texture_height':S,'visible_bounds_width':8,'visible_bounds_height':10,'visible_bounds_offset':[0,4,0]}
 geo={'format_version':'1.12.0','minecraft:geometry':[{'description':description,'bones':bones}]}
 duration=4.8;times=[round(i*duration/16,3) for i in range(17)]
 wave=lambda t:math.sin(t*2*math.pi/duration)
 tracks={}
 for group,amount in [('body',.20),('head',.35)]:
  keys={str(t):[round(amount*wave(t),5),0,0] for t in times};keys[str(duration)]=keys['0.0'].copy();tracks[group]={'rotation':keys}
 animation={'format_version':'1.8.0','animations':{'misc.idle':{'loop':True,'animation_length':duration,'bones':tracks}}}
 for filename,obj in [('igor.geo.json',geo),('igor.animation.json',animation),('parts.json',parts)]:
  (OUT/filename).write_text(json.dumps(obj,indent=2)+'\n',encoding='utf-8')
 atlas.save(OUT/'igor.png')
 export_blockbench(bones,parts,animation)
 print(f'Igor study: {len(parts)} cuboids; {len(bones)} bones; {S}px Box UV atlas, height used {by+row}.')

def export_blockbench(bones,parts,animation):
 def ident(name):return str(uuid.uuid5(uuid.NAMESPACE_URL,'velvet-eclipse/igor/'+name))
 elements=[]
 for part in parts:
  c=part['cube'];a=c['origin'];b=[x+y for x,y in zip(a,c['size'])]
  elements.append({'name':part['name'],'type':'cube','uuid':ident(part['name']), 'from':a,'to':b,'origin':c.get('pivot',a),'rotation':c.get('rotation',[0,0,0]),'box_uv':True,'uv_offset':c['uv'],'autouv':0,'color':0,'faces':{f:{'uv':[u,v,u+w,v+h],'texture':0} for f,(u,v,w,h) in face_uv(*c['uv'],*c['size']).items()}})
 def group(b):return {'name':b['name'],'origin':b['pivot'],'rotation':b.get('rotation',[0,0,0]),'uuid':ident('bone/'+b['name']),'export':True,'isOpen':True,'children':[ident(p['name']) for p in parts if p['bone']==b['name']]+[group(child) for child in bones if child.get('parent')==b['name']]}
 project={'meta':{'format_version':'4.10','model_format':'bedrock','box_uv':True},'name':'igor_voxel_study','model_identifier':'igor_voxel_study','visible_box':[8,10,4],'resolution':{'width':S,'height':S},'elements':elements,'outliner':[group(b) for b in bones if 'parent' not in b], 'textures':[{'name':'igor.png','id':'0','uuid':ident('texture'),'path':'igor.png','source':'data:image/png;base64,'+base64.b64encode((OUT/'igor.png').read_bytes()).decode(),'mode':'bitmap','saved':True}]}
 animators={}
 for name,tracks in animation['animations']['misc.idle']['bones'].items():
  animators[ident('bone/'+name)]={'name':name,'type':'bone','keyframes':[{'channel':channel,'data_points':[{'x':v[0],'y':v[1],'z':v[2]}],'time':float(t),'color':-1,'interpolation':'linear','uuid':ident(name+channel+t)} for channel,keys in tracks.items() for t,v in keys.items()]}
 project['animations']=[{'uuid':ident('animation/idle'),'name':'misc.idle','loop':'loop','length':4.8,'animators':animators}]
 (OUT/'igor.bbmodel').write_text(json.dumps(project,indent=2)+'\n',encoding='utf-8')

if __name__=='__main__':build()
