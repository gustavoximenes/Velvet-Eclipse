"""Onmoraki standalone approval model: native integer cuboids and Box UV."""
from pathlib import Path
import base64,json,math,uuid
import numpy as np
from PIL import Image
from materials import paint,PALETTE,surface_pattern
from geometry import geometry
from animate_model import animate
ROOT=Path(__file__).resolve().parent
OUT=ROOT/'draft'
DENSITY=8
SIZE=1024

def face_uv(u, v, w, h, d):
    return {'north': (u+d, v+d, w, h), 'south': (u+2*d+w, v+d, w, h),
            'west': (u+d+w, v+d, d, h), 'east': (u, v+d, d, h),
            'up': (u+d, v, w, d), 'down': (u+d+w, v, w, d)}

class Model:
    density = DENSITY
    def __init__(self):
        self.bones = []
        self.parts = []

    def bone(self, name, parent=None, pivot=(0, 0, 0), rotation=None):
        b = {'name': name, 'pivot': [round(float(v)*DENSITY, 5) for v in pivot]}
        if parent: b['parent'] = parent
        if rotation: b['rotation'] = list(rotation)
        self.bones.append(b)
        return name

    def box(self, bone, name, center, size, material='blue', rotation=None, pivot=None, pattern='', feature=''):
        dims = [round(v*DENSITY) for v in size]
        assert all(v > 0 for v in dims) and all(abs(v*DENSITY-i) < 1e-5 for v, i in zip(size, dims)), (name, size)
        cube = {'origin': [round((a-b/2)*DENSITY, 5) for a, b in zip(center, size)], 'size': dims}
        if rotation:
            cube.update(rotation=list(rotation), pivot=[round(float(v)*DENSITY, 5) for v in (pivot or center)])
        next(b for b in self.bones if b['name'] == bone).setdefault('cubes', []).append(cube)
        self.parts.append({'name': name, 'bone': bone, 'material': material, 'pattern': pattern, 'feature': feature, 'cube': cube})

    def segment(self, bone, name, start, end, width, depth, material='blue', pattern='', feature=''):
        a, b = np.array(start), np.array(end)
        dx, dy, dz = b-a
        length = np.linalg.norm(b-a)
        # Rz @ Rx transforms local positive Y onto this segment.
        rx = math.degrees(math.atan2(dz, math.hypot(dx, dy)))
        rz = math.degrees(math.atan2(-dx, dy))
        size = (width, math.ceil(length*DENSITY)/DENSITY, depth)
        self.box(bone, name, (a+b)/2, size, material, (rx, 0, rz), pattern=pattern, feature=feature)

    def atlas(self):
        atlas = Image.new('RGBA', (SIZE, SIZE))
        # Height sorted shelves give a compact, non-overlapping native Box UV atlas.
        ordered = sorted(self.parts, key=lambda p: -(p['cube']['size'][1]+p['cube']['size'][2]))
        x = y = 2
        row = 0
        for part in ordered:
            c = part['cube']; w, h, d = c['size']; aw, ah = 2*(w+d), h+d
            if x+aw+2 > SIZE: x=2; y+=row+2; row=0
            assert y+ah+2 <= SIZE, ('atlas full', part['name'])
            c['uv'] = [x, y]
            for face, (u, v, fw, fh) in face_uv(x, y, w, h, d).items():
                pixels=paint(fw, fh, part['material'], face, part['pattern'])
                pixels=surface_pattern(pixels,c,face,DENSITY,part)
                atlas.paste(pixels, (u, v))
            x += aw+2; row=max(row, ah)
        self.atlas_height = y+row
        return atlas

def write(m):
    OUT.mkdir(parents=True, exist_ok=True)
    atlas=m.atlas(); atlas.save(OUT/'onmoraki.png')
    geo={'format_version': '1.12.0', 'minecraft:geometry': [{'description': {'identifier': 'geometry.onmoraki_rework',
        'texture_width': SIZE, 'texture_height': SIZE, 'visible_bounds_width': 32, 'visible_bounds_height': 32,
        'visible_bounds_offset': [0, 8, 0]}, 'bones': m.bones}]}
    anim=animate(m)
    for name, data in [('onmoraki.geo.json', geo), ('onmoraki.animation.json', anim), ('parts.json', m.parts), ('connections.json',m.connections)]:
        (OUT/name).write_text(json.dumps(data, indent=2)+'\n', encoding='utf-8')
    bb(m, anim)
    print(f'Built fresh: {len(m.parts)} cuboids / {len(m.bones)} bones / atlas {SIZE}px, occupied height {m.atlas_height}px', flush=True)

def bb(m, anim):
    def ident(name): return str(uuid.uuid5(uuid.NAMESPACE_URL, 'velvet-eclipse/onmoraki-study-01/'+name))
    elements=[]
    for part in m.parts:
        c=part['cube'];a=c['origin'];b=[x+y for x,y in zip(a,c['size'])]
        elements.append({'name':part['name'],'type':'cube','uuid':ident(part['name']),'from':a,'to':b,
            'origin':c.get('pivot',a),'rotation':c.get('rotation',[0,0,0]),'box_uv':True,'uv_offset':c['uv'],
            'autouv':0,'color':0,'faces':{f:{'uv':[u,v,u+w,v+h],'texture':0} for f,(u,v,w,h) in face_uv(*c['uv'],*c['size']).items()}})
    def group(b):
        return {'name':b['name'],'origin':b['pivot'],'rotation':b.get('rotation',[0,0,0]),'uuid':ident('bone/'+b['name']),
            'export':True,'isOpen':True,'children':[ident(p['name']) for p in m.parts if p['bone']==b['name']]+[group(child) for child in m.bones if child.get('parent')==b['name']]}
    clips=[]
    for name,a in anim['animations'].items():
        animators={}
        for bone,tracks in a['bones'].items():
            animators[ident('bone/'+bone)]={'name':bone,'type':'bone','keyframes':[{'channel':ch,'data_points':[dict(zip('xyz',v))],
                'time':float(t),'color':-1,'interpolation':'linear','uuid':ident(name+bone+ch+t)} for ch,keys in tracks.items() for t,v in keys.items()]}
        clips.append({'uuid':ident(name),'name':name,'loop':'loop' if a['loop'] else 'once','length':a['animation_length'],'animators':animators})
    project={'meta':{'format_version':'4.10','model_format':'bedrock','box_uv':True},'name':'Onmoraki — estudo de modelagem 01',
        'model_identifier':'onmoraki_rework','visible_box':[24,24,8],'resolution':{'width':SIZE,'height':SIZE},
        'elements':elements,'outliner':[group(b) for b in m.bones if 'parent' not in b],
        'textures':[{'name':'onmoraki.png','id':'0','uuid':ident('texture'),'path':'onmoraki.png',
        'source':'data:image/png;base64,'+base64.b64encode((OUT/'onmoraki.png').read_bytes()).decode(),'mode':'bitmap','saved':True}], 'animations':clips}
    (OUT/'onmoraki.bbmodel').write_text(json.dumps(project,indent=2)+'\n',encoding='utf-8')


if __name__=='__main__':write(geometry(Model))
