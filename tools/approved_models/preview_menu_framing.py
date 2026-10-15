"""Composite native GeckoLib vertices with the production menu layout and framing transform."""
from pathlib import Path
import json,sys
import numpy as np
from PIL import Image,ImageDraw,ImageFont
ROOT=Path(__file__).resolve().parents[2]
sys.path.insert(0,str(ROOT/'tools/velvet_room'))
import portrait_raster as raster
OUT=ROOT/'build/reports/city/menu'
ASSETS=ROOT/'src/main/resources/assets/velvet_eclipse'
transforms=json.loads((OUT/'transforms.json').read_text())
raster.W,raster.H=1260,700
rows=(len(transforms)+1)//2
sheet=Image.new('RGB',(1680,rows*467+36),'#081225')
font=ImageFont.truetype(str(ASSETS/'font/body.ttf'),21)
for index,(name,t) in enumerate(transforms.items()):
    matrix=np.array(t['matrix']).reshape(4,4).T
    class Camera:
        orthographic=True
        eye=(np.linalg.inv(matrix)@np.array([0,0,10000,1]))[:3]
        def project(self,points):
            q=np.c_[points,np.ones(len(points))]@matrix.T
            return np.stack([(t['x']+q[:,0]*t['scale'])*1.4,(t['y']+q[:,1]*t['scale'])*1.4,100-q[:,2]],axis=-1)
    texture=np.array(Image.open(ASSETS/f'textures/entity/{name}.png').convert('RGBA'))
    faces=[]
    geometry=OUT.parent/f'{name}-misc.idle.json'
    if not geometry.exists():geometry=ROOT/f'build/reports/model-combat/{name}-misc.idle.json'
    for face in json.loads(geometry.read_text()):
        v=np.array(face['vertices']);p=v[:,:3]*t['rendererScale']/16;uv=v[:,3:5]
        if np.dot(np.cross(p[1]-p[0],p[2]-p[0]),face['normal'])<0:p=p[::-1];uv=uv[::-1]
        faces.append((p,uv,texture,None))
    im=raster.raster(Image.open(OUT/f'{name}-background.png'),faces,Camera(),brightness=1.18)
    im=im.convert('RGBA');im.alpha_composite(Image.open(OUT/f'{name}-foreground.png'))
    im.convert('RGB').save(OUT/f'{name}-framing.png')
    sheet.paste(im.convert('RGB').resize((840,467)),((index%2)*840,(index//2)*467))
ImageDraw.Draw(sheet).text((18,rows*467+8),'Geometria e layout de produção • enquadramento offline • iluminação ilustrativa; sem captura do Minecraft',font=font,fill='#91d7ed')
sheet.save(OUT/'personas-enquadramento.png')
print(OUT/'personas-enquadramento.png')
