"""Render the exact geometry dumped by approvedModelsTest; no game control."""
from pathlib import Path
import sys, json
import numpy as np
from PIL import Image, ImageDraw, ImageFont

ROOT=Path(__file__).resolve().parents[2]
sys.path.insert(0,str(ROOT/'tools/velvet_room'))
import portrait_raster as r

REPORT=ROOT/'build/reports/model-combat'
ASSET=ROOT/'src/main/resources/assets/velvet_eclipse'
r.W,r.H=440,570
sheet=Image.new('RGB',(1760,625),'#101c2d')
font=ImageFont.truetype(str(ASSET/'font/display.ttf'),25)
for col,(name,clip) in enumerate([('pixie','misc.idle'),('pixie','action.magic'),('pixie','action.physical'),('elizabeth','misc.idle')]):
    raw=json.loads((REPORT/f'{name}-{clip}.json').read_text())
    tex=np.array(Image.open(ASSET/f'textures/entity/{name}.png').convert('RGBA'))
    faces=[]
    for face in raw:
        v=np.array(face['vertices']);p=v[:,:3];uv=v[:,3:5]
        if np.dot(np.cross(p[1]-p[0],p[2]-p[0]),face['normal'])<0:p=p[::-1];uv=uv[::-1]
        faces.append((p,uv,tex,None))
    points=np.concatenate([f[0] for f in faces]);center=(points.min(0)+points.max(0))/2
    distance=np.linalg.norm(points.max(0)-points.min(0))*1.3
    for _ in range(60):
        camera=r.Camera(center+np.array([.18,.05,-1])*distance,center,36)
        q=camera.project(points)
        if q[:,0].min()>18 and q[:,0].max()<r.W-18 and q[:,1].min()>18 and q[:,1].max()<r.H-18:break
        distance*=1.06
    im=r.raster(Image.new('RGB',(r.W,r.H),'#dce6e9'),faces,camera,brightness=1.08)
    sheet.paste(im,(col*r.W,38))
    ImageDraw.Draw(sheet).text((col*r.W+15,8),name.upper()+' / '+clip,font=font,fill='#a4e8ff')
ImageDraw.Draw(sheet).text((15,606),'GeckoLib • recursos integrados • amostras offline',font=ImageFont.truetype(str(ASSET/'font/body.ttf'),15),fill='#a4e8ff')
sheet.save(REPORT/'models-runtime.png')
print(REPORT/'models-runtime.png')
