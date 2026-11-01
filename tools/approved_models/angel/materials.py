"""Authored pixel palette; native Box UV. No borrowed character texture."""
from PIL import Image,ImageDraw
PALETTE={
 'skin':['#b6aeb8','#d1cbd0','#eee9e3','#f9f2e7'],
 'skin_soft':['#aaa8b4','#c9c3ca','#ded7d7','#eee7e4'],
 'lip':['#604c66','#877086','#a18c9e','#b7a2b0'],
 'mouth':['#302739']*4,
 'hair':['#5b573c','#8f8355','#c3b778','#e2d398'],
 'hair_shadow':['#413e2d','#625c40','#8c8052','#b3a369'],
 'strap':['#101321','#1b1f33','#2b3048','#414963'],
 'blindfold':['#10142c','#1b2242','#273258','#45537b'],
 'feather':['#9c9fb5','#c3c6d3','#e0e2e9','#f4f2ec'],
 'feather_base':['#8b91a9','#adb5ca','#cdd4e2','#e4e9ee'],
 'silver':['#464b66','#767f99','#aab7cb','#d7e1e9'],
}
def paint(w,h,material,face,pattern):
 p=PALETTE[material];tone={'north':2,'south':1,'west':1,'east':2,'up':3,'down':0}[face]
 if material in ('skin','skin_soft','lip'):tone=2
 im=Image.new('RGBA',(w,h),p[tone]);d=ImageDraw.Draw(im)
 if pattern=='hair' and w>3 and h>5 and face in ('north','south','east','west'):
  for f in [.25,.70]:
   x=int(w*f);d.line([(x,1),(max(1,x-1),h//2),(max(1,x-2),h-2)],fill=p[1])
  d.line([(max(1,w//2),2),(max(1,w//2),h//2)],fill=p[3])
 if pattern=='feather' and w>2 and h>5 and face in ('north','south'):
  x=w//2;d.line((x,1,x,h-2),fill=p[3])
  for y in [h//3,2*h//3]:
   d.line((1,y+2,x,y),fill=p[1]);d.line((x,y,w-2,y+2),fill=p[1])
 if pattern=='band' and face in ('north','south') and h>3:
  d.line((1,1,w-2,1),fill=p[3]);d.line((1,h-2,w-2,h-2),fill=p[0])
 if pattern=='embroidery' and face=='north' and w>5:
  # Broken silver-blue scrolls, not eyes painted onto the opaque blindfold.
  for x in range(2,w-4,7):d.line([(x,h//2),(x+2,h//2-2),(x+4,h//2),(x+2,h//2+2)],fill=p[3])
 return im
