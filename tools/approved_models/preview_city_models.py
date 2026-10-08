"""Offline previews and fusion cards from actual GeckoLib audit output."""
from pathlib import Path
import sys,json
import numpy as np
from PIL import Image,ImageDraw,ImageFont
ROOT=Path(__file__).resolve().parents[2];A=ROOT/'src/main/resources/assets/velvet_eclipse';REPORT=ROOT/'build/reports/city'
sys.path.insert(0,str(ROOT/'tools/velvet_room'))
import portrait_raster as r
def render(id,clip,w,h,margin=20):
 r.W,r.H=w,h;raw=json.loads((REPORT/f'{id}-{clip}.json').read_text());tex=np.array(Image.open(A/f'textures/entity/{id}.png').convert('RGBA'));faces=[]
 for face in raw:
  v=np.array(face['vertices']);p=v[:,:3];uv=v[:,3:5]
  if np.dot(np.cross(p[1]-p[0],p[2]-p[0]),face['normal'])<0:p=p[::-1];uv=uv[::-1]
  faces.append((p,uv,tex,None))
 points=np.concatenate([f[0] for f in faces]);center=(points.min(0)+points.max(0))/2;distance=np.linalg.norm(points.max(0)-points.min(0))*1.3
 for _ in range(80):
  camera=r.Camera(center+np.array([.18,.05,-1])*distance,center,36);q=camera.project(points)
  if q[:,0].min()>margin and q[:,0].max()<w-margin and q[:,1].min()>margin and q[:,1].max()<h-margin:break
  distance*=1.06
 return r.raster(Image.new('RGB',(w,h),'#17223b'),faces,camera,brightness=1.12)
names={'apsaras':'Apsaras','jack_frost':'Jack Frost','forneus':'Forneus','chimera':'Chimera'}
sheet=Image.new('RGB',(1440,1030),'#0a1228');font=ImageFont.truetype(str(A/'font/display.ttf'),27);body=ImageFont.truetype(str(A/'font/body.ttf'),16)
for col,(id,name) in enumerate(names.items()):
 for row,clip in enumerate(['misc.idle','action.magic']):
  im=render(id,clip,350,450);sheet.paste(im,(col*360+5,row*490+39));ImageDraw.Draw(sheet).text((col*360+15,row*490+7),name+' / '+('idle' if row==0 else 'magia'),font=font,fill='#a9e7ff')
 card=Image.new('RGB',(256,384),'#0a154e');portrait=render(id,'misc.idle',220,282,10);card.paste(portrait,(18,23));d=ImageDraw.Draw(card);d.rectangle((3,3,252,380),outline='#d4bc70',width=5);d.rectangle((13,13,242,370),outline='#a0b9e1',width=2);label=name.upper();f=ImageFont.truetype(str(A/'font/display.ttf'),24);d.text(((256-f.getlength(label))/2,321),label,font=f,fill='#e1eeff');card.save(A/f'textures/entity/fusion_cards/{id}.png')
ImageDraw.Draw(sheet).text((15,996),'Recursos finais carregados pelo GeckoLib • amostras offline, sem execução no Minecraft',font=body,fill='#a9e7ff');sheet.save(REPORT/'models-runtime.png')
print(REPORT/'models-runtime.png')
