"""Offline renders of the actual Java skin meshes and target-local VFX geometry; not Minecraft captures."""
from pathlib import Path
import json, math
import numpy as np
from PIL import Image, ImageDraw, ImageFilter

root = Path(__file__).resolve().parents[2]
out = root / 'build/reports/choreography'
ns = {'__file__': str(root/'tools/render_battle_previews.py')}
exec((root/'tools/render_battle_previews.py').read_text(encoding='utf8').split('for n in (2,7):')[0], ns)
texture = ns['skin_fixture']()
styles = ['makoto', 'yukari', 'junpei', 'akihiko', 'mitsuru']

def actor(im, style, tick=145, slim=False):
    quads = json.loads((out/f'pose-{style}-{tick}{"-slim" if slim else ""}.json').read_text())
    az, yaw = -.05, -.22
    rz = np.array([[math.cos(az),-math.sin(az),0],[math.sin(az),math.cos(az),0],[0,0,1]])
    ry = np.array([[math.cos(yaw),0,math.sin(yaw)],[0,1,0],[-math.sin(yaw),0,math.cos(yaw)]])
    shapes = []
    for q in quads:
        raw = np.array(q); v = (raw[:,:3] + [0,-.55,0]) @ (rz@ry).T
        xy = np.column_stack([900*.73+v[:,0]*500*.57,500*.64+v[:,1]*500*.57])
        shapes.append((v[:,2].mean(),xy,raw[:,3:]))
    for z,xy,uv in sorted(shapes,key=lambda a:-a[0]):
        ns['triangle'](im,xy[:3],uv[:3],texture)
        ns['triangle'](im,xy[[0,2,3]],uv[[0,2,3]],texture)

sheet = Image.new('RGB',(1800,1500),'#08132c')
for i, style in enumerate(styles):
    im = Image.open(out/f'{style}-back.png').convert('RGBA')
    actor(im, style, slim=i%2==1)
    im.alpha_composite(Image.open(out/f'{style}-text.png').convert('RGBA'))
    d=ImageDraw.Draw(im);d.rectangle((0,474,900,500),fill='#030d36')
    d.text((10,480),f'{style.upper()} | OFFLINE - SKIN ILUSTRATIVA, MALHA REAL',fill='white')
    im.save(out/f'{style}-preview.png');sheet.paste(im,(i%2*900,i//2*500))
sheet.save(out/'all-out-styles.jpg',quality=90)

effects=json.loads((out/'effects.json').read_text())
sheet=Image.new('RGB',(1500,600),'#061022')
for i,(skill,strokes) in enumerate(effects.items()):
    im=Image.new('RGBA',(300,300),'#061022');draw=ImageDraw.Draw(im)
    def point(v): return (150+(v['x']*.87-v['z']*.5)*38,235+(-v['y']+(v['x']*.5+v['z']*.87)*.22)*38)
    for n in range(-3,4):
        draw.line([point(dict(x=n,y=0,z=-3)),point(dict(x=n,y=0,z=3))],fill='#18334b')
        draw.line([point(dict(x=-3,y=0,z=n)),point(dict(x=3,y=0,z=n))],fill='#18334b')
    draw.ellipse((144,230,156,240),fill='#ffe080')
    light=Image.new('RGBA',im.size);d=ImageDraw.Draw(light)
    for s in strokes:
        rgb=s['color']&0xffffff;color=((rgb>>16)&255,(rgb>>8)&255,rgb&255,int(s['alpha']*255))
        d.line([point(s['from']),point(s['to'])],fill=color,width=max(1,round(s['width']*76)))
    im.alpha_composite(light.filter(ImageFilter.GaussianBlur(4)));im.alpha_composite(light)
    ImageDraw.Draw(im).text((12,12),skill.upper()+' / ALVO NO CENTRO',fill='white')
    sheet.paste(im,(i%5*300,i//5*300))
sheet.save(out/'spell-bodies.jpg',quality=94)
print(out)
