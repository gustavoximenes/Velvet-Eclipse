"""Limited deliberate pixel clusters. No noise, gradients or photograph textures."""
from PIL import Image,ImageDraw
PALETTE={
 'dark':['#101722','#202b39','#303c4b','#586674'],
 'mask':['#111b24','#26303a','#333e48','#52616b'],
 'face':['#b4c2c2','#d4dcd5','#e9ecdc','#f3f3e3'],
 'fur':['#859aaf','#b4c6d0','#e0e8e5','#f4f5e7'],
 'hair':['#343538','#655d52','#998a76','#c5b597'],
 'hair_root':['#24292c','#303137','#393b3d','#52504a'],
 'lip':['#1a222b','#27343e','#52616b','#68747b'],
 'ear':['#3e3940','#746c65','#aaa292','#c0b9a3'],
 'nose':['#0d1821','#13212c','#253440','#587380'],
 'claw':['#8eaaa9','#b3cbc2','#d7e2cb','#e5ecd6'],
 'sclera':['#d4d9cc','#d4d9cc','#f0edcf','#f0edcf'],
 'iris':['#a39040','#a39040','#dbc769','#dbc769'],
 'pupil':['#25313a','#25313a','#25313a','#25313a'],
 'eye_glint':['#fff5b1','#fff5b1','#fff5b1','#fff5b1'],
 'mouth':['#111722','#111722','#111722','#111722']}

def paint(w,h,material,face,pattern):
 if material=='eye':
  im=Image.new('RGBA',(w,h),'#e9ecdc');d=ImageDraw.Draw(im)
  if face=='north':
   # Almond sclera with a separate iris and pupil; the modeled upper lid does
   # most of the dark contour work, avoiding the previous goggle-like ring.
   d.polygon([(0,h//2),(2,1),(w-3,0),(w-1,h//2-1),(w-2,h-2),(w//2,h-1),(1,h-2)],fill='#26303a')
   d.polygon([(1,h//2),(3,2),(w-3,1),(w-2,h//2),(w-3,h-2),(3,h-2)],fill='#f0edcf')
   cx=w//2
   d.rectangle((cx-1,1,cx+1,h-2),fill='#dbc769')
   d.line((cx,2,cx,h-2),fill='#25313a')
   d.point((cx-1,1),fill='#fff5b1');d.point((cx+1,h-2),fill='#a39040')
   if pattern=='eye_right':im=im.transpose(Image.Transpose.FLIP_LEFT_RIGHT)
  return im
 p=PALETTE[material];tone={'north':2,'south':1,'west':1,'east':2,'up':3,'down':1}[face]
 if material in ('face','mask'):tone=2
 im=Image.new('RGBA',(w,h),p[tone]);d=ImageDraw.Draw(im)
 if material=='fur' and min(w,h)>4:
  # Sparse, broad notches describe the direction of the fur without confetti.
  if face in ('north','south','east','west') and pattern!='tail':
   for k,f in enumerate((.18,.68)):
    x=int(w*f);y=max(0,h-(4 if k else 6))
    d.polygon([(x,y),(min(w-1,x+2),h-1),(max(0,x-2),h-1)],fill='#c3d1d5')
   if pattern=='tuft':
    d.polygon([(0,h-1),(0,h-4),(w//3,h-1),(w//2,h-3),(w-1,h-1)],fill=p[1])
 if material=='dark' and pattern=='satin' and w>5 and h>7 and face in ('north','east','west'):
  x=max(1,w//4);d.polygon([(x,1),(x+1,2),(x+1,h//2),(x,h-3),(max(0,x-1),h//2)],fill=p[3])
 if material=='hair' and min(w,h)>3:
  if pattern=='part' and face in ('north','up'):
   d.rectangle((w//2,0,w//2,h-1),fill=p[0])
  if face in ('north','south','east','west'):
   for j,f in enumerate((.23,.65)):
    x=int(w*f);bend=1 if pattern.endswith('left') else -1
    d.line([(x,0),(max(0,min(w-1,x+bend*2)),h//2),(max(0,min(w-1,x+bend)),h-1)],fill=p[2] if j==0 else p[1],width=1)
 if pattern=='nose' and face=='north':
  d.line((1,0,w-2,0),fill=p[3])
  d.point((0,h-1),fill=p[0]);d.point((w-1,h-1),fill=p[0])
 if pattern=='smile' and face=='north':
  d.line((0,0,w-1,0),fill='#465260')
  if w>=4:d.point((1,0),fill='#e8ecda');d.point((w-2,0),fill='#e8ecda')
 return im

def paint_volume(c,face,w,h):
 # Paint the shared 3D facial pattern onto every Box UV face, so fitted cuboids
 # do not expose seams or inconsistent color patches at their junctions.
 import numpy as np
 from rig_math import matrix
 faces={'north':[0,2,6,4],'south':[1,5,7,3],'west':[0,1,3,2],'east':[4,6,7,5],'up':[2,3,7,6],'down':[0,4,5,1]}
 import itertools
 points=np.array([np.array(c['origin'])+np.array(c['size'])*p for p in itertools.product([0,1],repeat=3)])/8
 pivot=np.array(c.get('pivot',[0,0,0]))/8
 points=(points-pivot)@matrix(c.get('rotation',[0,0,0])).T+pivot
 q=points[faces[face]]
 if face in ('north','east'):tl,tr,br,bl=q[[1,2,3,0]]
 elif face=='up':tl,tr,br,bl=q[[0,3,2,1]]
 elif face=='down':tl,tr,br,bl=q
 else:tl,tr,br,bl=q[[3,2,1,0]]
 ys,xs=np.mgrid[0:h,0:w];u=(xs+.5)/w;v=(ys+.5)/h
 pos=tl[None,None,:]+u[:,:,None]*(tr-tl)+v[:,:,None]*(bl-tl)
 x=pos[:,:,0];y=pos[:,:,1]+1.8125+1.75
 # Softly shaped cheek/muzzle division, with small pale cheek notches.
 boundary=31.03+.12*np.maximum(0,1-abs(x)/.5)+.16*np.maximum(0,(abs(x)-1.0)/.6)
 colors=np.empty((h,w,4),dtype='uint8');colors[:]=[233,236,220,255]
 colors[y<boundary]=[51,62,72,255]
 return Image.fromarray(colors)
