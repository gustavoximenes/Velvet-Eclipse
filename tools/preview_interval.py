"""Render Java-emitted All-Out geometry and interval plan; no in-game screenshot."""
from pathlib import Path
import json, math
import numpy as np
from PIL import Image, ImageDraw

p = Path(__file__).resolve().parents[1]
out = p/'build/reports/interval'
ns = {'__file__': str(p/'tools/render_battle_previews.py')}
exec((p/'tools/render_battle_previews.py').read_text(encoding='utf8').split('for n in (2,7):')[0], ns)
texture = ns['skin_fixture']()

def actor(im, tick, x, y, size, yaw=-.17, slim=False):
    quads = json.loads((out/f'pose-{tick}{"-slim" if slim else ""}.json').read_text(encoding='utf8'))
    az = -.05
    rz = np.array([[math.cos(az),-math.sin(az),0],[math.sin(az),math.cos(az),0],[0,0,1]])
    ry = np.array([[math.cos(yaw),0,math.sin(yaw)],[0,1,0],[-math.sin(yaw),0,math.cos(yaw)]])
    shapes = []
    for q in quads:
        raw = np.array(q);v = (raw[:,:3] + [0,-.55,0]) @ (rz@ry).T
        xy = np.column_stack([x+v[:,0]*size,y+v[:,1]*size])
        shapes.append((v[:,2].mean(),xy,raw[:,3:]))
    for z,xy,uv in sorted(shapes,key=lambda a:-a[0]):
        ns['triangle'](im,xy[:3],uv[:3],texture)
        ns['triangle'](im,xy[[0,2,3]],uv[[0,2,3]],texture)

for base,tick,x,y,size in [('victory',140,.74,.59,.48),('results',140,.82,.51,.33),('arena',24,.68,.59,.49)]:
    im=Image.open(out/f'{base}.png').convert('RGBA')
    actor(im,tick,x*900,y*500,size*500)
    ImageDraw.Draw(im).text((8,8),'OFFLINE / skin ilustrativa / malha real',fill='white')
    im.save(out/f'{base}-pose.png')

colors={'tile':'#405574','tile_dark':'#26394f','wall':'#182938','column':'#7d9fb6','trim':'#3e685b','window':'#27eb65','seal':'#081626','border':'#b1dfc4','gold':'#ba9d4a','ivory':'#cadada','stairs':'#588575','back':'black','forward':'black','velvet_door':'#1744ff','boss_gate':'#90ff8b'}
im=Image.new('RGB',(650,1080),'#040c18');d=ImageDraw.Draw(im)
cells=json.loads((out/'floor.json').read_text(encoding='utf8'))
for c in sorted(cells,key=lambda c:c['y']):
    if c['y']==13:continue
    x=325+c['x']*10;y=70+(c['z']-208)*10
    d.rectangle((x,y,x+9,y+9),fill=colors.get(c['material'],'#9faabb'))
d.text((20,25),'ANDAR 3 / PLANTA OFFLINE / TETO REMOVIDO',fill='white')
for label,z in [('TRANSICAO / ESCADA',220),('GUARDIAO',256),('PREPARACAO / VELVET ROOM',292)]:
    d.text((20,70+(z-208)*10),label,fill='#88eaff')
d.ellipse((317,542,333,558),fill='#ffe259')
im.save(out/'floor-preview.png')
print(out)
