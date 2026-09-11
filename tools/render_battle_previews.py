from pathlib import Path
import json,math
import numpy as np
from PIL import Image,ImageDraw,ImageFont
project=Path(__file__).resolve().parents[1];reports=project/'build/reports';out=reports/'previews';out.mkdir(exist_ok=True)
fontpath=str(project/'src/main/resources/assets/velvet_eclipse/font/display.ttf')
def rgba(value):
 value&=0xffffffff;return (value>>16&255,value>>8&255,value&255,value>>24)
def triangle(im,pts,uv,texture):
 pts=np.array(pts);uv=np.array(uv);low=np.floor(pts.min(axis=0)).astype(int);high=np.ceil(pts.max(axis=0)).astype(int)
 low=np.maximum(low,[0,0]);high=np.minimum(high,[im.width-1,im.height-1])
 if np.any(high<low):return
 xx,yy=np.meshgrid(np.arange(low[0],high[0]+1)+.5,np.arange(low[1],high[1]+1)+.5)
 a,b,c=pts;den=(b[1]-c[1])*(a[0]-c[0])+(c[0]-b[0])*(a[1]-c[1])
 if abs(den)<1e-7:return
 w0=((b[1]-c[1])*(xx-c[0])+(c[0]-b[0])*(yy-c[1]))/den
 w1=((c[1]-a[1])*(xx-c[0])+(a[0]-c[0])*(yy-c[1]))/den;w2=1-w0-w1
 mask=(w0>=0)&(w1>=0)&(w2>=0)
 xy=w0[...,None]*uv[0]+w1[...,None]*uv[1]+w2[...,None]*uv[2]
 tx=np.clip((xy[...,0]*texture.width).astype(int),0,texture.width-1);ty=np.clip((xy[...,1]*texture.height).astype(int),0,texture.height-1)
 arr=np.array(texture)[ty,tx];arr[...,3]=np.where(mask,arr[...,3],0)
 im.alpha_composite(Image.fromarray(arr,'RGBA'),tuple(low))
def skin_fixture():
 # Illustrative dark skin/clothing fixture; the runtime samples the actual player's texture.
 skin=Image.new('RGBA',(64,64),(55,38,48,255));d=ImageDraw.Draw(skin)
 d.rectangle((8,8,15,15),fill=(83,59,53));d.rectangle((8,8,15,9),fill=(18,16,21))
 d.rectangle((8,10,9,11),fill=(25,20,25));d.rectangle((14,10,15,11),fill=(24,18,22))
 d.rectangle((9,11,10,11),fill=(232,228,222));d.point((10,11),fill=(39,45,71))
 d.rectangle((13,11,14,11),fill=(232,228,222));d.point((13,11),fill=(39,45,71))
 d.rectangle((11,12,12,13),fill=(67,41,35));d.rectangle((10,14,13,14),fill=(46,24,30))
 d.rectangle((20,20,31,31),fill=(100,20,51));d.rectangle((21,20,21,31),fill=(125,40,65))
 d.rectangle((20,28,31,29),fill=(215,201,190));d.rectangle((20,30,31,31),fill=(65,66,76))
 d.rectangle((44,20,47,23),fill=(100,20,51));d.rectangle((44,24,47,24),fill=(215,201,190))
 d.rectangle((44,25,47,31),fill=(83,59,53));d.rectangle((36,52,39,63),fill=(83,59,53))
 arr=np.array(skin);lum=(arr[:,:,0].astype(float)*3+arr[:,:,1]*6.0+arr[:,:,2])/10
 threshold=max(40,min(195,int(lum.mean())+12));tone=np.minimum(1,lum/(threshold*1.7))
 for low,high,a,b in [(0,.45,(16,41,117),(41,139,213)),(.45,.75,(41,139,213),(123,211,243)),(.75,1,(123,211,243),(246,255,255))]:
  t=np.clip((tone-low)/(high-low),0,1);color=np.array(a)+(np.array(b)-a)*t[:,:,None]
  mask=(tone>=low)&(tone<=high);arr[:,:,:3][mask]=np.round(color[mask]).astype(np.uint8)
 return Image.fromarray(arr,'RGBA')

def portrait(im,o):
 layer=Image.new('RGBA',im.size);skin=skin_fixture();quads=json.loads((reports/'selector-geometry-wide.json').read_text())
 az=-.11;ay=-.20;rz=np.array([[math.cos(az),-math.sin(az),0],[math.sin(az),math.cos(az),0],[0,0,1]])
 ry=np.array([[math.cos(ay),0,math.sin(ay)],[0,1,0],[-math.sin(ay),0,math.cos(ay)]])
 size=o['h']*.54;points=[]
 for q in quads:
  raw=np.array(q['points']);p=raw[:,:3]@(rz@ry).T
  xy=[(o['x']+o['w']*.54+x*size,o['y']+o['h']*.34+y*size) for x,y,z in p]
  points.append((p[:,2].mean(),xy,raw[:,3:],q['part']))
 for _,xy,uv,part in sorted(points,key=lambda p:-p[0]):
  if part=='gun':ImageDraw.Draw(layer).polygon(xy,fill='#f6ffff')
  else:
   triangle(layer,xy[:3],uv[:3],skin);triangle(layer,[xy[0],xy[2],xy[3]],[uv[0],uv[2],uv[3]],skin)
 crop=Image.new('RGBA',im.size);x=max(0,o['x']);y=max(0,o['y']);right=min(im.width,o['x']+o['w']);bottom=min(im.height,o['y']+o['h'])
 if o.get('alpha',1)<1:layer.putalpha(layer.getchannel('A').point(lambda v:round(v*o['alpha'])))
 if right>x and bottom>y:crop.paste(layer.crop((x,y,right,bottom)),(x,y));im.alpha_composite(crop)
def render(name,size):
 im=Image.new('RGBA',size,'#0a2432');d=ImageDraw.Draw(im)
 for yy in range(size[1]):
  if yy>size[1]*.55:
   r=(yy-size[1]*.55)/(size[1]*.45);d.line((0,yy,size[0],yy),fill=(int(15+25*r),int(48+40*r),int(56+30*r),255))
 for o in json.loads((reports/(name+'.json')).read_text()):
  typ=o['type']
  if typ=='fill':
   if o['w']>0 and o['h']>0:
    color=rgba(o['color'])
    if color[3]==255:ImageDraw.Draw(im).rectangle((o['x'],o['y'],o['x']+o['w']-1,o['y']+o['h']-1),fill=color)
    elif color[3]>0:
     x=max(0,o['x']);y=max(0,o['y']);right=min(im.width,o['x']+o['w']);bottom=min(im.height,o['y']+o['h'])
     if right>x and bottom>y:im.alpha_composite(Image.new('RGBA',(right-x,bottom-y),color),(x,y))
   continue
  layer=Image.new('RGBA',size);d=ImageDraw.Draw(layer)
  if typ=='fill':
   if o['w']>0 and o['h']>0:d.rectangle((o['x'],o['y'],o['x']+o['w']-1,o['y']+o['h']-1),fill=rgba(o['color']))
  elif typ=='text':
   f=ImageFont.truetype(fontpath,max(5,round(11*o['scale'])))
   box=f.getbbox(o['text']);label=Image.new('RGBA',(max(1,box[2]+8),max(1,box[3]-box[1]+10)))
   ImageDraw.Draw(label).text((3,3-box[1]),o['text'],font=f,fill=rgba(o['color']))
   angle=o.get('angle',0);label=label.rotate(-angle,expand=True,resample=Image.Resampling.BICUBIC)
   layer.alpha_composite(label,(o['x']-3,o['y']-3))
  elif typ=='portrait':portrait(layer,o)
  elif typ=='fragment':
   eye=Image.new('RGBA',(8,4),(43,139,207,round(255*o['alpha'])));ed=ImageDraw.Draw(eye)
   ed.rectangle((0,1,7,2),fill=(214,255,255,round(255*o['alpha'])));ed.point((2,1),fill=(7,39,85,round(255*o['alpha'])));ed.point((5,1),fill=(7,39,85,round(255*o['alpha'])))
   xy=np.array(o['xy']).reshape(-1,2);uv=np.array(o['uv']).reshape(-1,2)
   triangle(layer,xy[:3],uv[:3],eye);triangle(layer,[xy[0],xy[2],xy[3]],[uv[0],uv[2],uv[3]],eye)
  im.alpha_composite(layer)
 footer=ImageDraw.Draw(im);footer.text((15,size[1]-18),'PREVIA OFFLINE â€” geometria real; skin ilustrativa; sem captura do Minecraft',font=ImageFont.truetype('C:/Windows/Fonts/arial.ttf',12),fill='#bdeefa')
 im.convert('RGB').save(out/(name+'.png'));return im
for n in (2,7):render('persona-selector-'+str(n),(1280,720))
render('target-markers',(1000,280))
frames=[render('shatter-'+str(t),(1280,720)).resize((640,360)) for t in (25,38,42,47)]
sheet=Image.new('RGB',(1280,720));d=ImageDraw.Draw(sheet)
for i,frame in enumerate(frames):sheet.paste(frame,(i%2*640,i//2*360));d.text((i%2*640+20,i//2*360+22),'Tick '+str((25,38,42,47)[i]),fill='white',font=ImageFont.truetype('C:/Windows/Fonts/arial.ttf',18))
sheet.save(out/'shatter-sequence.png')

opening=[render('selector-open-'+str(ms),(1280,720)).resize((640,360)) for ms in (0,60,140,240,340)]
opening[0].save(out/'selector-opening.gif',save_all=True,append_images=opening[1:],duration=[60,80,100,100,1000],loop=0)
