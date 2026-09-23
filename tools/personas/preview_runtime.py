"""Offline rendering of the actual GeckoLib bake and Java spell geometry from starterPersonasTest."""
from pathlib import Path
import sys,json,importlib.util
import numpy as np
from PIL import Image,ImageDraw,ImageFont
P=Path(__file__).resolve().parents[2];REPORT=P/'build/reports/starter-personas'
FONT=ImageFont.truetype('C:/Windows/Fonts/arial.ttf',20)
sheet=Image.new('RGB',(1500,1110),'#101c2d')
for row,name in enumerate(['arsene','orpheus']):
 source=P/'tools/personas'/name;sys.path.insert(0,str(source));sys.modules.pop('build_model',None)
 spec=importlib.util.spec_from_file_location('render_'+name,source/'render_model.py');render=importlib.util.module_from_spec(spec);spec.loader.exec_module(render)
 for col,clip in enumerate(['misc.idle','action.magic','action.physical']):
  raw=json.loads((REPORT/(name+'-'+clip+'.json')).read_text())
  def surfaces(yaw,pitch,head=False,time=0,clip=None):
   camera=render.R((pitch,yaw,0));out=[]
   for face in raw:
    v=np.array(face['vertices']);points=(camera[:3,:3]@v[:,:3].T).T;n=camera[:3,:3]@np.array(face['normal'])
    if n[2]>=0:continue
    out.append((points,v[:,3:5],n,face['bone']))
   return out
  render.surfaces=surfaces
  im=render.render(-22 if col!=2 else -43,480,500)
  sheet.paste(im,(10+col*500,40+row*550));ImageDraw.Draw(sheet).text((18+col*500,12+row*550),name.upper()+' / '+clip,font=FONT,fill='white')
 sys.path.pop(0)
ImageDraw.Draw(sheet).text((15,1080),'Prévia offline: geometria carregada pelo GeckoLib. Ataques no quadro de impacto.',font=FONT,fill='#8addfa')
sheet.save(REPORT/'models-runtime.png')

# Render the same native spell strokes, with a simple additive beam/halo approximation.
fx=Image.new('RGB',(1100,610),'#081024')
for col,name in enumerate(['eiga','agi']):
 w=h=550;buf=np.zeros((h,w,3));buf[:]=[.024,.045,.080]
 camera=np.array([[.866,0,-.5],[-.17,.94,-.296],[.47,.34,.814]])
 def project(v):
  q=camera@np.array([v['x'],v['y'],v['z']]);return np.array([w/2+q[0]*140,410-q[1]*140])
 raw=json.loads((REPORT/(name+'-effect.json')).read_text())['4']
 for s in raw:
  a=project(s['from']);b=project(s['to']);width=max(.4,s['width']*140)
  x0=max(0,int(min(a[0],b[0])-width*2));x1=min(w,int(max(a[0],b[0])+width*2)+1)
  y0=max(0,int(min(a[1],b[1])-width*2));y1=min(h,int(max(a[1],b[1])+width*2)+1)
  if x1<=x0 or y1<=y0:continue
  ys,xs=np.mgrid[y0:y1,x0:x1];d=b-a;length=float(d@d)
  t=np.clip(((xs-a[0])*d[0]+(ys-a[1])*d[1])/max(.001,length),0,1)
  distance=np.sqrt((xs-a[0]-t*d[0])**2+(ys-a[1]-t*d[1])**2)
  alpha=np.maximum(0,1-distance/width)**1.65*s['alpha'];core=(1-np.clip(distance/(width*.32),0,1))*.72
  color=np.array([(s['color']>>shift&255)/255 for shift in [16,8,0]])
  buf[y0:y1,x0:x1]+=((1-core[:,:,None])*color+core[:,:,None])*alpha[:,:,None]
 im=Image.fromarray(np.clip(buf*255,0,255).astype('uint8'));fx.paste(im,(col*550,40))
 ImageDraw.Draw(fx).text((col*550+24,18),name.upper()+' / impacto',font=FONT,fill='white')
ImageDraw.Draw(fx).text((18,580),'Prévia offline da geometria dos efeitos; iluminação final depende do jogo.',font=FONT,fill='#8addfa')
fx.save(REPORT/'spell-effects.png')
print('Rendered native GeckoLib models and Java effect geometry.')
