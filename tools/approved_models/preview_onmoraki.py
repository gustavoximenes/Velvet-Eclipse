"""Render integrated GeckoLib vertices and refresh Onmoraki's fusion card only."""
from pathlib import Path
import json, sys
import numpy as np
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / 'src/main/resources/assets/velvet_eclipse'
OUT = ROOT / 'build/reports/model-combat'
sys.path.insert(0, str(ROOT / 'tools/velvet_room'))
import portrait_raster as raster

def faces(clip, scale=1):
    texture = np.array(Image.open(ASSETS / 'textures/entity/onmoraki.png').convert('RGBA'))
    result = []
    for face in json.loads((OUT / f'onmoraki-{clip}.json').read_text()):
        v = np.array(face['vertices']); p = v[:, :3] * scale; uv = v[:, 3:5]
        if np.dot(np.cross(p[1]-p[0], p[2]-p[0]), face['normal']) < 0:
            p = p[::-1]; uv = uv[::-1]
        result.append((p, uv, texture, None))
    return result

def render(clip, width, height, margin=18):
    raster.W, raster.H = width, height
    mesh = faces(clip); points = np.concatenate([f[0] for f in mesh])
    center = (points.min(0)+points.max(0))/2
    distance = np.linalg.norm(points.max(0)-points.min(0))*1.3
    for _ in range(80):
        camera = raster.Camera(center+np.array([.18, .05, -1])*distance, center, 36)
        q = camera.project(points)
        if q[:,0].min()>margin and q[:,0].max()<width-margin and q[:,1].min()>margin and q[:,1].max()<height-margin:
            break
        distance *= 1.06
    return raster.raster(Image.new('RGB', (width,height), '#18243a'), mesh, camera, brightness=1.12)

font = ImageFont.truetype(str(ASSETS / 'font/body.ttf'), 21)
sheet = Image.new('RGB', (1320,640), '#0a1425'); draw = ImageDraw.Draw(sheet)
for col, (clip, title) in enumerate([('misc.idle','Repouso'), ('action.physical','Ataque físico / amostra'), ('action.magic','Ataque mágico / amostra')]):
    sheet.paste(render(clip, 430,560), (col*440+5,38))
    draw.text((col*440+15,8), title, font=font, fill='#ace8ff')
draw.text((15,610), 'Geometria integrada • GeckoLib • render offline, sem execução do Minecraft', font=font, fill='#ace8ff')
sheet.save(OUT / 'onmoraki-runtime.png')

card = Image.new('RGB',(256,384),'#0a154e')
card.paste(render('misc.idle',220,282,10),(18,23)); d=ImageDraw.Draw(card)
d.rectangle((3,3,252,380),outline='#d4bc70',width=5)
d.rectangle((13,13,242,370),outline='#a0b9e1',width=2)
f = ImageFont.truetype(str(ASSETS / 'font/display.ttf'),24)
d.text(((256-f.getlength('ONMORAKI'))/2,321),'ONMORAKI',font=f,fill='#e1eeff')
card.save(ASSETS / 'textures/entity/fusion_cards/onmoraki.png')

menu = ROOT / 'build/reports/city/menu'
t = json.loads((menu / 'transforms.json').read_text())['onmoraki']
matrix = np.array(t['matrix']).reshape(4,4).T
class MenuCamera:
    orthographic = True
    eye = (np.linalg.inv(matrix) @ np.array([0,0,10000,1]))[:3]
    def project(self, points):
        q = np.c_[points,np.ones(len(points))] @ matrix.T
        return np.stack([(t['x']+q[:,0]*t['scale'])*1.4, (t['y']+q[:,1]*t['scale'])*1.4,100-q[:,2]],axis=-1)
raster.W, raster.H = 1260,700
im = raster.raster(Image.open(menu / 'onmoraki-background.png'), faces('misc.idle',t['rendererScale']/16), MenuCamera(), brightness=1.18).convert('RGBA')
im.alpha_composite(Image.open(menu / 'onmoraki-foreground.png'))
im.convert('RGB').save(OUT / 'onmoraki-menu.png')
print('PASS Onmoraki native previews and fusion card:', OUT)

# Placement icon is rendered from the same native geometry, keeping the existing creative item ID.
icon = render('misc.idle',128,128,8).convert('RGBA')
pixels=np.array(icon); key=np.all(pixels[:,:,:3]==np.array([24,36,58]),axis=2);pixels[key,3]=0
Image.fromarray(pixels).resize((64,64),Image.Resampling.NEAREST).save(ASSETS / 'textures/item/onmoraki_model.png')
