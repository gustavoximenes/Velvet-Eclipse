"""Render fusion card fronts from the mod's actual Persona models/textures.

Offline asset build only, numpy/Pillow; no Minecraft, network or screenshot inputs.
Run again after changing native Persona models. The existing arcana back stays separate.
"""
from pathlib import Path
import json,math,re
import numpy as np
from PIL import Image,ImageDraw,ImageFont
import choreography as c
from portrait_raster import Camera,raster

ROOT=c.ROOT.parents[1]
OUT=c.ASSET/'textures/entity/fusion_cards'

def definitions():
    rows=json.loads((ROOT/'src/main/resources/data/velvet_eclipse/catalogue/personas.json').read_text(encoding='utf8'))
    result={p['id']:p['name'] for p in rows}
    source=(ROOT/'src/main/java/com/gx/velveteclipse/core/persona/PersonaRegistry.java').read_text(encoding='utf8')
    result.update(re.findall(r'persona\(\s*"([a-z0-9_]+)"\s*,\s*"([^"]+)"',source))
    return result

def portrait(id):
    model=c.model({'cinder_wisp':'thanatos','umbra_slime':'wealth_hand'}.get(id,id));faces=model.faces(0)
    # Frame both width and height, including wings and weapons; no cropped heads.
    points=np.concatenate([p for p,_,_,_ in faces]);lo=points.min(0);hi=points.max(0)
    center=(lo+hi)/2
    cam=Camera((0,0,-1),(0,0,0),36)
    radius=np.linalg.norm(hi-lo)/2
    distance=max(radius*1.5,1)
    for _ in range(100):
        cam=Camera((0,0,-distance),(0,0,0),36)
        p=cam.project(points-center)
        if p[:,2].min()>.1 and p[:,0].min()>25 and p[:,0].max()<231 and p[:,1].min()>30 and p[:,1].max()<292:break
        distance*=1.08
    # Offset the portrait up to leave a permanent label strip.
    cam.offset_y=-24
    rendered=raster(Image.new('RGB',(256,384),'#0a154e'),model.faces(0,'misc.idle',c.T(-center)),cam,brightness=1.14)
    return rendered

def card(id,name):
    im=portrait(id);d=ImageDraw.Draw(im)
    d.rectangle((3,3,252,380),outline='#d4bc70',width=5)
    d.rectangle((13,13,242,370),outline='#a0b9e1',width=2)
    for x in [24,232]:
        for y in [24,360]:d.polygon([(x,y-6),(x+6,y),(x,y+6),(x-6,y)],fill='#ccbc81')
    font=ImageFont.truetype(str(c.ASSET/'font/display.ttf'),24)
    label=name.upper()
    while font.getlength(label)>210:font=ImageFont.truetype(str(c.ASSET/'font/display.ttf'),font.size-1)
    d.rectangle((19,317,236,351),fill='#0a154e')
    d.text(((256-font.getlength(label))/2,321),label,font=font,fill='#e1eeff')
    return im

if __name__=='__main__':
    OUT.mkdir(parents=True,exist_ok=True)
    rows=definitions()
    for i,(id,name) in enumerate(sorted(rows.items())):
        card(id,name).save(OUT/f'{id}.png')
        if i%20==0:print(f'{i+1}/{len(rows)} {id}',flush=True)
    print(f'Generated {len(rows)} native Persona fronts',flush=True)
