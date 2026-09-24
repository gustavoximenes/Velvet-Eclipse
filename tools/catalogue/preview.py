"""Offline previews of actual GeckoLib meshes and spell strokes exported by reloadCatalogueTest."""
from pathlib import Path
import json,math
import numpy as np
from PIL import Image,ImageDraw,ImageFont
ROOT=Path(__file__).resolve().parents[2];REPORT=ROOT/'build/reports/reload-catalogue';A=ROOT/'src/main/resources/assets/velvet_eclipse'
font=ImageFont.truetype('C:/Windows/Fonts/arial.ttf',20)
small=ImageFont.truetype('C:/Windows/Fonts/arial.ttf',15)
camera=np.array([[.91,0,-.414],[-.085,.979,-.187],[.405,.205,.89]])
sheet=Image.new('RGB',(1440,510),'#09162b');draw=ImageDraw.Draw(sheet)
names=['pixie','jack_frost','angel','eligor','unicorn','orpheus_telos']
for col,name in enumerate(names):
 faces=json.loads((REPORT/(name+'.json')).read_text());texture=Image.open(A/'textures/entity'/f'{name}.png').convert('RGB');polys=[]
 for face in faces:
  vertices=np.array(face['vertices']);points=vertices[:,:3]@camera.T;normal=camera@face['normal']
  if normal[2]>=0:continue
  uv=vertices[:,3:5].mean(axis=0);rgb=texture.getpixel((int(uv[0])%texture.width,int(uv[1])%texture.height));light=.64+.36*max(0,normal@np.array([-.3,.65,-.7]));color=tuple(int(c*light) for c in rgb)
  polys.append((points[:,2].mean(),points,color))
 for _,points,color in sorted(polys,key=lambda x:-x[0]):draw.polygon([(col*240+120+x*11,420-y*11) for x,y,z in points],fill=color)
 draw.text((col*240+15,36),name.replace('_',' ').upper(),font=small,fill='#78e5ff')
draw.text((18,475),'Modelos provisórios — geometria carregada pelo GeckoLib. As Personas detalhadas foram preservadas.',font=font,fill='white')
sheet.save(REPORT/'provisional-models.png')

names=['agi','eiga','garu','bufu','kouha','frei','psi','megido'];sheet=Image.new('RGB',(1440,820),'#09162b')
for index,name in enumerate(names):
 strokes=json.loads((REPORT/(name+'-effect.json')).read_text());w,h=360,350;buf=np.zeros((h,w,3));buf[:]=[.025,.044,.080]
 def project(v):
  q=camera@np.array([v['x'],v['y'],v['z']]);return np.array([w/2+q[0]*55,310-q[1]*55])
 for s in strokes:
  a=project(s['from']);b=project(s['to']);width=max(.35,s['width']*55);lo=np.maximum(0,np.floor(np.minimum(a,b)-width*2)).astype(int);hi=np.minimum([w,h],np.ceil(np.maximum(a,b)+width*2)).astype(int)
  x0,y0=lo;x1,y1=hi
  if x1<=x0 or y1<=y0:continue
  yy,xx=np.mgrid[y0:y1,x0:x1];d=b-a;t=np.clip(((xx-a[0])*d[0]+(yy-a[1])*d[1])/max(.001,d@d),0,1);dist=np.sqrt((xx-a[0]-t*d[0])**2+(yy-a[1]-t*d[1])**2)
  alpha=np.maximum(0,1-dist/width)**1.65*s['alpha'];core=(1-np.clip(dist/(width*.32),0,1))*.72;color=np.array([(s['color']>>shift&255)/255 for shift in [16,8,0]])
  buf[y0:y1,x0:x1]+=((1-core[:,:,None])*color+core[:,:,None])*alpha[:,:,None]
 im=Image.fromarray(np.clip(buf*255,0,255).astype('uint8'));x=index%4*360;y=index//4*385;sheet.paste(im,(x,y+30));ImageDraw.Draw(sheet).text((x+18,y+15),name.upper(),font=font,fill='#78e5ff')
ImageDraw.Draw(sheet).text((18,785),'Prévia offline da geometria. A iluminação e a composição final dependem do Minecraft.',font=font,fill='white');sheet.save(REPORT/'spell-effects.png')
print('Rendered GeckoLib provisional meshes and eight VFX families.')
