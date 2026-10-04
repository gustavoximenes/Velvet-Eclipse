"""Elizabeth revision 05: restored oval contour with the polished face and shoulders.

Bedrock, North = -Z, uniform 8x authoring grid, integer cuboids and Box UV.
All visible anatomy (including the facial relief) is solid cuboid geometry.
"""
from pathlib import Path
import base64, json, math, uuid
import numpy as np
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parent
OUT = ROOT / 'draft'
DENSITY = 8
SIZE = 1024
PALETTE = {
    'skin': ['#d1aaa1', '#e6c9b8', '#f8dfc9', '#ffecd6'],
    'skin_soft': ['#d1a7a0', '#e6c5b5', '#f7d8c3', '#ffead4'],
    'lip': ['#be9290', '#d6aaa2', '#e6bdb0', '#f0d1be'],
    'mouth': ['#9c7477', '#b08688', '#bc9191', '#cea5a0'],
    'hair': ['#b1b8c6', '#d0d3d9', '#ebeae5', '#faf5e8'],
    'hair_shadow': ['#8792a7', '#adb8c7', '#cdd4da', '#e4e7e7'],
    'sclera': ['#c5bdb7', '#ddd4c6', '#f7f0db', '#fff9e9'],
    'iris': ['#826122', '#c7a329', '#e5ca45', '#fae681'],
    'pupil': ['#282537', '#302b38', '#3b303b', '#4c3d48'],
    'lash': ['#544954', '#63505a', '#75606a', '#87747b'],
    'blue': ['#101e50', '#1a327a', '#264ba4', '#4168bd'],
    'panel': ['#122b64', '#1d4495', '#305aba', '#517bce'],
    'navy': ['#070e24', '#111b37', '#19284a', '#2c3c62'],
    'silver': ['#526985', '#829cae', '#b0c8d0', '#d6e2df'],
    'gold': ['#5c4b34', '#928060', '#c8b78b', '#ead5a4'],
    'book': ['#262135', '#42334c', '#625066', '#847183'],
    'paper': ['#a69b87', '#c3b8a1', '#ded4be', '#f5e9d0'],
    'stocking': ['#161f36', '#27324a', '#3a465c', '#52627b'],
}

def face_uv(u, v, w, h, d):
    return {'north': (u+d, v+d, w, h), 'south': (u+2*d+w, v+d, w, h),
            'west': (u+d+w, v+d, d, h), 'east': (u, v+d, d, h),
            'up': (u+d, v, w, d), 'down': (u+d+w, v, w, d)}

def paint(w, h, material, face, pattern):
    palette = PALETTE[material]
    tone = {'north': 2, 'south': 1, 'west': 1, 'east': 2, 'up': 3, 'down': 0}[face]
    # Skin receives form shading from the actual surface normal, not dark borders
    # painted onto each adjoining cuboid. This does not change the skin palette.
    if material in ('skin', 'skin_soft'):tone=2
    image = Image.new('RGBA', (w, h), palette[tone])
    d = ImageDraw.Draw(image)
    if material in ('blue', 'panel', 'navy', 'book', 'silver', 'gold') and min(w, h) > 4:
        d.line((1, 0, w-2, 0), fill=palette[min(3, tone+1)])
        d.line((w-1, 1, w-1, h-1), fill=palette[max(0, tone-1)])
    if pattern == 'cloth' and face in ('north', 'south') and w > 9:
        d.line((2, 2, 2, h-3), fill=palette[min(tone+1, 3)])
        d.line((w-3, 3, w-3, h-2), fill=palette[max(tone-1, 0)])
    if pattern == 'hair' and face in ('north', 'south', 'east', 'west') and w > 6:
        # Broad, designed streaks, never noise or baked facial features.
        for x in range(5, w-2, 15):
            d.line((x, 1, max(1, x-2), h-2), fill=palette[max(0, tone-1)])
            d.line((x+1, 2, max(2, x-1), max(2, h-5)), fill=palette[min(3, tone+1)])
    if pattern == 'pages' and face in ('east', 'west', 'up', 'down'):
        for y in range(2, h-1, 3):
            d.line((1, y, w-2, y), fill=palette[max(0, tone-1)])
    if pattern == 'seam' and face in ('north', 'south'):
        d.line((w//2, 1, w//2, h-2), fill=palette[max(tone-1, 0)])
    return image

class Model:
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
                atlas.paste(paint(fw, fh, part['material'], face, part['pattern']), (u, v))
            x += aw+2; row=max(row, ah)
        self.atlas_height = y+row
        return atlas

def ring(m, bone, name, center, radius, thickness=.125, material='silver', feature='clasp'):
    x, y, z = center
    for i in range(8):
        a = (i+.5)*math.tau/8
        length = math.ceil((2*radius*math.tan(math.pi/8)+.04)*DENSITY)/DENSITY
        m.box(bone, f'{name}_facet_{i}', (x+radius*math.cos(a), y+radius*math.sin(a), z),
              (length, thickness, .25), material, (0, 0, math.degrees(a)+90), feature=feature)

def build():
    m = Model(); B=m.bone; C=m.box; L=m.segment
    B('elizabeth'); B('hips', 'elizabeth', (0, 24, 0)); B('body', 'hips', (0, 27, 0), (0, -3, 0))
    # Elongated silhouette: tailored long vest, bare upper arms, knee-high boots.
    C('body', 'waist', (0, 27.1, .05), (4.5, 4.25, 3), 'blue', pattern='cloth')
    C('body', 'ribcage', (0, 31.1, .05), (5.5, 4.25, 3.5), 'blue', pattern='cloth')
    C('body', 'upper_chest', (0, 33.2, .05), (5.25, 1.5, 3.25), 'blue')
    for s, side in [(-1, 'right'), (1, 'left')]:
        C('body', side+'_waist_facet', (s*2.3, 28.45, .05), (.75, 4.5, 3), 'blue', (0, 0, -s*8))
        C('body', side+'_chest_facet', (s*1.75, 31.1, -1.25), (2, 3.5, 1), 'blue', (0, -s*14, -s*4))
        L('body', side+'_armhole_piping', (s*2.68, 30.65, -1.49), (s*2.35, 34, -1.3), .125, .25, 'silver')
        L('body', side+'_back_armhole', (s*2.68, 30.65, 1.61), (s*2.35, 34, 1.36), .125, .25, 'silver')
    C('body', 'front_tailored_panel', (0, 29.4, -1.88), (2.75, 7.25, .25), 'panel', pattern='cloth')
    C('body', 'back_center_seam', (0, 30.1, 1.82), (.125, 6.75, .125), 'navy')
    # High collar is a hollow, four-sided garment around the neck.
    B('neck', 'body', (0, 34.5, 0))
    C('neck', 'neck_anatomy', (0, 35.7, .12), (1.5, 3, 1.5), 'skin')
    B('collar', 'body', (0, 34, 0))
    C('collar', 'collar_back', (0, 35.12, 1.05), (2.25, 2.25, .375), 'blue', (-8, 0, 0))
    for s, side in [(-1, 'right'), (1, 'left')]:
        C('collar', side+'_collar_side', (s*1.05, 35.1, 0), (.375, 2.25, 2), 'blue', (0, 0, -s*9))
        C('collar', side+'_collar_front', (s*.54, 35, -1.03), (1, 2.25, .375), 'panel', (7, 0, -s*9))
        L('collar', side+'_collar_rim', (s*.16, 36.2, -1.11), (s*1.2, 36.1, -.99), .125, .125, 'silver')
    # Continuous fitted skirt. Broad rotated facets replace staircase curves.
    B('skirt', 'hips', (0, 27, 0))
    C('skirt', 'skirt_core', (0, 20.4, .15), (4.5, 12.5, 2.75), 'navy')
    B('skirt_front', 'skirt', (0, 27, -1.5))
    C('skirt_front', 'front_center', (0, 20.5, -1.62), (3.75, 13, .5), 'panel', pattern='cloth')
    C('skirt', 'back_center', (0, 20.5, 1.56), (3.75, 13, .5), 'blue', pattern='seam')
    for s, side in [(-1, 'right'), (1, 'left')]:
        B('skirt_'+side, 'skirt', (s*2.05, 27, 0))
        rot = (0, 0, s*4.6); pivot=(s*2.05, 27, 0)
        C('skirt_'+side, side+'_front_panel', (s*1.98, 20.5, -1.59), (1.5, 13, .5), 'blue', rot, pivot, 'cloth')
        C('skirt_'+side, side+'_back_panel', (s*1.98, 20.5, 1.53), (1.5, 13, .5), 'blue', rot, pivot, 'cloth')
        C('skirt_'+side, side+'_side_panel', (s*2.67, 20.5, -.02), (.375, 13, 3.5), 'blue', rot, pivot, 'cloth')
        C('skirt_'+side, side+'_front_hem', (s*1.98, 14.13, -1.64), (1.5, .25, .5), 'navy', rot, pivot)
        C('skirt_'+side, side+'_back_hem', (s*1.98, 14.13, 1.58), (1.5, .25, .5), 'navy', rot, pivot)
        C('skirt_'+side, side+'_side_hem', (s*2.68, 14.13, -.02), (.375, .25, 3.5), 'navy', rot, pivot)
    C('skirt_front', 'front_hem_piping', (0, 14.14, -1.9), (3.75, .25, .125), 'navy')
    C('skirt', 'back_hem_piping', (0, 14.14, 1.83), (3.75, .25, .125), 'navy')
    # The five dress fastenings have physical rings, dark recessed seats and gold studs.
    for i, y in enumerate([32, 28.1, 24.05, 20.15, 16.4]):
        parent='body' if i<2 else 'skirt_front'; z=-2.12 if i<2 else -1.99
        C(parent, f'clasp_{i}_seat', (0, y, z), (1.375, 1.375, .25), 'navy', (0, 0, 45), feature='clasp')
        ring(m, parent, f'clasp_{i}', (0, y, z-.19), .81)
        C(parent, f'clasp_{i}_pin', (0, y, z-.39), (.375, .375, .25), 'gold', (0, 0, 45), feature='clasp')
    # Paired articulated legs and knee-high footwear, heels share y=0.
    for s, side in [(-1, 'right'), (1, 'left')]:
        x=s*1.48
        B(side+'_thigh', 'hips', (x, 24, .1))
        C(side+'_thigh', side+'_upper_stocking', (x, 18.1, .1), (1.75, 11.5, 1.875), 'stocking')
        B(side+'_shin', side+'_thigh', (x, 12.3, .1))
        C(side+'_shin', side+'_boot_calf', (x, 8.7, .1), (1.875, 6.75, 2), 'blue', pattern='cloth')
        C(side+'_shin', side+'_boot_ankle', (x, 3.93, -.05), (1.5, 3.5, 1.625), 'blue', pattern='cloth')
        C(side+'_shin', side+'_boot_shin', (x, 7.1, -.83), (1.375, 8.75, .375), 'panel', (-1.8, 0, 0), pattern='cloth')
        C(side+'_shin', side+'_boot_outer_facet', (x+s*.83, 8.45, .13), (.375, 6.5, 1.625), 'blue', (0, 0, s*2))
        C(side+'_shin', side+'_boot_top_cuff', (x, 12.0, .1), (2, .5, 2.125), 'navy')
        C(side+'_shin', side+'_boot_top_piping', (x, 12.28, -.94), (2, .125, .125), 'silver')
        C(side+'_shin', side+'_rear_seam', (x, 7, 1.13), (.125, 9.75, .125), 'navy')
        B(side+'_foot', side+'_shin', (x, 2, 0))
        C(side+'_foot', side+'_heel', (x, .75, .5), (.875, 1.5, 1), 'navy')
        C(side+'_foot', side+'_shoe_quarter', (x, 1.86, -.36), (1.625, 1.625, 2.125), 'blue')
        C(side+'_foot', side+'_instep', (x, 1.38, -1.53), (1.625, 1, 2), 'panel', (-13, 0, 0))
        C(side+'_foot', side+'_pointed_toe', (x, .62, -2.63), (1.25, .75, 1.125), 'blue')
        C(side+'_foot', side+'_front_sole', (x, .125, -2.06), (1.625, .25, 2.5), 'navy')
    # Arms in Elizabeth's reference pose: book supported near her left chest,
    # right forearm crosses beneath it. Geometry is authored in this calm rest pose.
    joints = {
        'right': [(-2.65, 33.15, 0), (-3.6, 28.6, -.25), (1.4, 28.1, -3.85)],
        'left': [(2.65, 33.15, 0), (3.55, 28.5, .0), (4.8, 30.8, -3.4)],
    }
    for side, (shoulder, elbow, wrist) in joints.items():
        s=-1 if side=='right' else 1
        B(side+'_upper_arm', 'body', shoulder)
        L(side+'_upper_arm', side+'_bare_upper_arm', shoulder, elbow, 1.5, 1.625, 'skin')
        C(side+'_upper_arm', side+'_deltoid', shoulder, (1.625, 1.5, 1.75), 'skin', (0, 0, -s*7), feature='shoulder')
        C('body', side+'_shoulder_bridge', (s*2.1, 33.55, 0), (1.375, 1.125, 1.75), 'skin', feature='shoulder')
        B(side+'_forearm', side+'_upper_arm', elbow)
        L(side+'_forearm', side+'_long_glove', elbow, wrist, 1.25, 1.375, 'blue', 'cloth')
        e=np.array(elbow);w=np.array(wrist); delta=(w-e)/np.linalg.norm(w-e)
        L(side+'_forearm', side+'_glove_cuff', e-delta*.25, e+delta*.65, 1.625, 1.75, 'panel')
        L(side+'_forearm', side+'_cuff_piping', e-delta*.30, e-delta*.175, 1.75, 1.875, 'silver')
        L(side+'_forearm', side+'_wrist_cuff', w-delta*.55, w+delta*.1, 1.375, 1.5, 'panel')
        B(side+'_hand', side+'_forearm', wrist)
    # Left palm and book share a rigid pivot. Four finger joints wrap the cover edge.
    C('left_hand', 'left_palm', (4.74, 31.1, -3.72), (1.25, 1.5, .625), 'blue', (-10, 0, -12))
    B('book', 'left_hand', (4.74, 31.1, -3.72), (-7, -14, -18))
    C('book', 'page_block', (3.67, 32.4, -3.35), (3.75, 5.625, 1), 'paper', pattern='pages', feature='book')
    C('book', 'front_cover', (3.67, 32.4, -3.99), (4.125, 6, .25), 'book', feature='book')
    C('book', 'back_cover', (3.67, 32.4, -2.7), (4.125, 6, .25), 'book', feature='book')
    C('book', 'bound_spine', (1.66, 32.4, -3.35), (.375, 6, 1.25), 'book', feature='book')
    for y in [29.71, 30.06, 34.74, 35.09]:
        C('book', f'spine_band_{str(y).replace(".", "_")}', (1.44, y, -3.35), (.125, .125, 1.5), 'gold', feature='book')
    for z in [-4.15, -2.54]:
        suf='front' if z< -3 else 'back'
        for s in [-1, 1]:
            C('book', f'{suf}_border_vertical_{s}', (3.67+s*1.72, 32.4, z), (.125, 5.25, .125), 'gold', feature='book')
            C('book', f'{suf}_border_horizontal_{s}', (3.67, 32.4+s*2.55, z), (3.5, .125, .125), 'gold', feature='book')
            C('book', f'{suf}_inner_vertical_{s}', (3.67+s*1.38, 32.4, z), (.125, 4.5, .125), 'gold', feature='book')
            C('book', f'{suf}_inner_horizontal_{s}', (3.67, 32.4+s*2.19, z), (2.75, .125, .125), 'gold', feature='book')
        ring(m, 'book', suf+'_sigil', (3.67, 32.4, z-.01 if z< -3 else z+.01), .91, .125, 'gold', 'book')
        C('book', suf+'_sigil_vertical', (3.67, 32.4, z), (.125, 2.875, .125), 'gold', feature='book')
        C('book', suf+'_sigil_horizontal', (3.67, 32.4, z), (2.375, .125, .125), 'gold', feature='book')
    C('book', 'book_clasp', (5.75, 32.4, -3.96), (.5, .625, .5), 'gold', feature='book')
    for i in range(4):
        B(f'left_finger_{i}', 'book', (5.29, 30.18+i*.35, -3.42))
        C(f'left_finger_{i}', f'left_finger_{i}_base', (5.56, 30.18+i*.35, -3.82), (.5, .25, .875), 'blue', (0, -15, 0), feature='finger')
        C(f'left_finger_{i}', f'left_finger_{i}_tip', (5.31, 30.18+i*.35, -4.28), (.75, .25, .25), 'panel', feature='finger')
    C('book', 'left_thumb', (4.74, 30.02, -2.38), (.875, .375, .5), 'blue', (0, 0, 20), feature='finger')
    C('right_hand', 'right_palm', (1.69, 28.33, -3.95), (1.5, .75, 1), 'blue', (0, -8, 18))
    for i in range(4):
        B(f'right_finger_{i}', 'right_hand', (2.1, 28.13+i*.18, -4.29+i*.25))
        C(f'right_finger_{i}', f'right_finger_{i}_proximal', (2.58, 28.38+i*.17, -4.29+i*.25), (.875, .25, .25), 'panel', (0, -6, 15), feature='finger')
        C(f'right_finger_{i}', f'right_finger_{i}_tip', (3.0, 28.64+i*.17, -4.29+i*.25), (.375, .25, .25), 'blue', (0, 0, 38), feature='finger')
    C('right_hand', 'right_thumb', (1.81, 28.88, -3.61), (.75, .375, .375), 'blue', (0, 0, 35), feature='finger')
    # Joined head volumes. The broad skin surfaces differ by hundredths in depth;
    # their intersections are buried, preserving a solid gently tapered silhouette.
    B('head', 'neck', (0, 36.7, .1), (0, 3, -2))
    B('face_base', 'head', (0, 40.6, -2.222), (-10, 0, 0))
    C('neck', 'neck_upper_joint', (0, 37.15, -.35), (1.125, .625, 1.25), 'skin', feature='neck')
    C('head', 'cranial_core', (0, 41.5, .05), (4.25, 4, 3.5), 'skin', feature='cranium')
    C('head', 'forehead_plane', (0, 42.6, -1.71), (3.875, 1.75, 1), 'skin', feature='forehead')
    C('face_base', 'midface', (0, 40.5, -1.722), (3.5, 3.25, 1), 'skin', feature='midface')
    C('face_base', 'lower_face', (0, 39.03, -1.346), (2.5, 1.375, 1.75), 'skin', feature='jaw')
    C('face_base', 'chin_plane', (0, 38.4875, -1.595), (.75, 1.25, 1.25), 'skin', feature='chin')
    # Four deliberate facets define an oval contour: fuller cheeks, curved lower
    # jaw and short beveled chin, instead of a long straight V ending in a shelf.
    # Each solid extends inward; its joins sit inside the adjacent face volumes.
    contour=[(2.20,41.1),(2.13,39.8),(1.70,38.85),(.94,38.04),(.35,37.86)]
    labels=['cheekbone','mandible','chin_facet','chin_bevel']
    for s,side in [(-1,'right'),(1,'left')]:
        for i,(upper,lower) in enumerate(zip(contour,contour[1:])):
            a=np.array(upper);b=np.array(lower);direction=(a-b)/np.linalg.norm(a-b)
            outward=np.array([direction[1],-direction[0]])
            width=2.5
            length=math.ceil(np.linalg.norm(a-b)*DENSITY)/DENSITY
            center=(a+b)/2-outward*width/2
            angle=-s*math.degrees(math.atan2(direction[0],direction[1]))
            depth=[2.0,1.875,1.5,1.25][i]
            C('face_base',side+'_'+labels[i],(s*center[0],center[1],-2.222+depth/2+i*.004),
              (width,length,depth),'skin',(0,0,angle),feature='cheek' if i==0 else 'jaw' if i==1 else 'chin')
    for s, side in [(-1, 'right'), (1, 'left')]:
        C('head', side+'_temple', (s*1.98, 41.4, .05), (.875, 3.25, 2), 'skin', (0, -s*20, 0), feature='temple')
        B('ear_'+side, 'head', (s*2.6, 40.02, -.02))
        C('ear_'+side, side+'_ear_outer', (s*2.62, 40.02, -.02), (.5, 1.25, .75), 'skin', (0, 0, -s*8), feature='ear')
        C('ear_'+side, side+'_ear_inner', (s*2.89, 40.05, -.12), (.125, .75, .375), 'skin_soft', feature='ear')
        C('ear_'+side, side+'_ear_lobe', (s*2.66, 39.51, -.09), (.375, .375, .5), 'skin', feature='ear')
        # Eye surfaces are actual solids nested below modeled lids.
        B('eye_'+side, 'face_base', (s*1.13, 40.56, -2.22))
        C('eye_'+side, side+'_orbital_ridge', (s*1.2, 41.01, -2.08), (1.625, .25, .375), 'skin', feature='orbit')
        C('eye_'+side, side+'_eye_white', (s*1.12, 40.56, -2.16), (1.25, .625, .375), 'sclera', (0, 0, -s*3), feature='eye')
        C('eye_'+side, side+'_outer_sclera', (s*1.76, 40.64, -2.18), (.25, .375, .25), 'sclera', (0, -s*10, s*22), feature='eye')
        C('eye_'+side, side+'_iris', (s*1.06, 40.55, -2.31), (.375, .625, .125), 'iris', feature='eye')
        for edge in [-1,1]:
            C('eye_'+side, side+f'_iris_edge_{edge}', (s*1.06+edge*.21, 40.55, -2.29), (.125, .375, .125), 'iris', feature='eye')
        C('eye_'+side, side+'_pupil', (s*1.06, 40.54, -2.33), (.125, .5, .125), 'pupil', feature='eye')
        C('eye_'+side, side+'_eye_glint', (s*1.06-.125, 40.72, -2.35), (.125, .125, .125), 'sclera', feature='eye')
        C('eye_'+side, side+'_lower_eyelid', (s*1.13, 40.21, -2.24), (1.375, .125, .25), 'skin', (0, 0, s*2), feature='eyelid')
        C('eye_'+side, side+'_upper_eyelid', (s*1.15, 40.96, -2.23), (1.5, .125, .25), 'skin', (0, 0, -s*3), feature='eyelid')
        C('eye_'+side, side+'_lash_line', (s*1.15, 40.91, -2.318), (1.5, .125, .125), 'lash', (0, 0, -s*3), feature='eyelid')
        C('eye_'+side, side+'_outer_lash', (s*1.87, 40.85, -2.25), (.375, .125, .25), 'lash', (0, -s*10, s*28), feature='eyelid')
        C('eye_'+side, side+'_inner_eye_corner', (s*.45, 40.49, -2.23), (.25, .25, .125), 'skin', (0, 0, s*25), feature='eyelid')
        B('brow_'+side, 'face_base', (s*1.19, 41.65, -2.22))
        C('brow_'+side, side+'_eyebrow', (s*1.18, 41.6, -2.205), (1.375, .125, .125), 'hair_shadow', (0, 0, -s*5), feature='brow')
        C('brow_'+side, side+'_brow_tip', (s*1.87, 41.51, -2.135), (.375, .125, .125), 'hair_shadow', (0, -s*18, -s*14), feature='brow')
    # One continuous sloping bridge, rooted beneath the facial surface. Its lower
    # end projects forward; the small tip meets it without a shelf or stacked peg.
    C('face_base', 'nose_bridge', (0, 40.15, -2.245), (.25, 1.125, .375), 'skin', (20, 0, 0), feature='nose')
    C('face_base', 'nose_tip', (0, 39.65, -2.39), (.375, .25, .375), 'skin', (20, 0, 0), feature='nose')
    B('mouth', 'face_base', (0, 38.94, -2.08))
    C('mouth', 'mouth_recess', (0, 38.94, -2.175), (.875, .125, .125), 'mouth', feature='mouth')
    C('mouth', 'lower_lip', (0, 38.86, -2.19), (.625, .125, .125), 'lip', feature='mouth')
    for s, side in [(-1, 'right'), (1, 'left')]:
        C('mouth', side+'_upper_lip', (s*.23, 39.018, -2.19), (.5, .125, .125), 'lip', (0, 0, s*9), feature='mouth')
    # Short silver bob: solid crown, oblique cheek locks, separate swept fringe.
    B('hair_crown', 'head', (0, 43.05, .2))
    C('hair_crown', 'crown_volume', (0, 43.05, .16), (4.75, 1.875, 4.75), 'hair', pattern='hair', feature='hair')
    C('hair_crown', 'crown_top', (-.25, 43.95, .38), (3.875, .875, 3.875), 'hair', (0, 0, -4), pattern='hair', feature='hair')
    B('hair_back', 'head', (0, 43, 1.91))
    C('hair_back', 'bob_occipital', (0, 41.2, 2.19), (4.75, 4.75, 1.25), 'hair_shadow', (-7, 0, 0), pattern='hair', feature='hair')
    C('hair_back', 'bob_nape', (0, 38.91, 1.95), (3.625, 1.125, 1.25), 'hair', (13, 0, 0), pattern='hair', feature='hair')
    for s, side in [(-1, 'right'), (1, 'left')]:
        B('hair_'+side, 'head', (s*2.1, 42.9, .2))
        C('hair_'+side, side+'_crown_facet', (s*2.3, 42.69, .28), (1.625, 2.625, 4.125), 'hair', (0, 0, s*22), pattern='hair', feature='hair')
        C('hair_'+side, side+'_side_bob', (s*2.81, 40.98, .38), (1.125, 3.5, 3.5), 'hair', (0, 0, s*3), pattern='hair', feature='hair')
        C('hair_'+side, side+'_bob_tip', (s*2.66, 39.15, .4), (1.125, 1.375, 3), 'hair', (0, 0, -s*21), pattern='hair', feature='hair')
        C('hair_'+side, side+'_face_lock', (s*2.43, 40.31, -1.72), (.875, 3, .875), 'hair', (0, s*15, -s*10), pattern='hair', feature='hair')
        C('hair_'+side, side+'_face_tip', (s*2.23, 38.99, -1.71), (.5, 1.125, .625), 'hair', (0, s*8, -s*25), pattern='hair', feature='hair')
        C('hair_back', side+'_rear_facet', (s*2.11, 40.9, 1.95), (1.625, 4, 1.625), 'hair', (-5, s*30, -s*4), pattern='hair', feature='hair')
        C('hair_back', side+'_nape_lock', (s*1.2, 38.75, 2.23), (.875, 1.125, .75), 'hair', (10, 0, -s*9), pattern='hair', feature='hair')
    B('hair_fringe', 'head', (0, 43.25, -1.88))
    for name, center, size, rot in [
        ('swept_bang_outer', (-1.82, 42.18, -2.26), (1.375, 2.875, .75), (0, -11, -26)),
        ('swept_bang_middle', (-.88, 42.76, -2.31), (1.375, 2.375, .75), (0, -3, -31)),
        ('parting_lock', (.05, 43.09, -2.12), (1, 1.75, .875), (0, 0, -35)),
        ('short_fringe_0', (.73, 42.55, -2.3), (.5, 1.25, .625), (0, 0, 14)),
        ('short_fringe_1', (1.18, 42.48, -2.2), (.5, 1.125, .625), (0, 8, 12)),
        ('short_fringe_2', (1.62, 42.42, -2.05), (.5, 1, .625), (0, 16, 10)),
        ('short_fringe_3', (2.04, 42.39, -1.87), (.5, 1, .625), (0, 22, 6)),
    ]:
        C('hair_fringe', name, center, size, 'hair', rot, pattern='hair', feature='hair')
    # The cap has a small, faceted pillbox silhouette and a separate lip/rim.
    B('hat', 'head', (.42, 43.7, .25), (-7, -6, -12))
    C('hat', 'hat_core', (.42, 44.67, .3), (3.5, 1.75, 3.25), 'blue', pattern='cloth', feature='hat')
    C('hat', 'hat_rim_core', (.42, 43.84, .3), (3.75, .375, 3.5), 'navy', feature='hat')
    C('hat', 'hat_crown_top', (.42, 45.52, .3), (3.5, .25, 3.125), 'panel', feature='hat')
    for s in [-1, 1]:
        C('hat', f'hat_side_{s}', (.42+s*1.8, 44.6, .3), (.75, 1.625, 2), 'blue', feature='hat')
        C('hat', f'hat_side_rim_{s}', (.42+s*1.82, 43.84, .3), (.875, .375, 2.125), 'navy', feature='hat')
        for z in [-1, 1]:
            C('hat', f'hat_corner_{s}_{z}', (.42+s*1.62, 44.62, .3+z*1.26), (.875, 1.625, 1), 'blue', (0, s*z*38, 0), feature='hat')
    C('hat', 'cap_front_seam', (.42, 44.66, -1.39), (.125, 1.375, .125), 'panel', feature='hat')
    # No large painted insignia: a subtle geometric blue/silver tab.
    C('hat', 'cap_tab', (.42, 44.4, -1.48), (.375, .5, .125), 'silver', feature='hat')
    # Shorten the exposed neck while retaining every local head/hat proportion.
    heads={'head'}
    for bone in m.bones:
        if bone.get('parent') in heads:heads.add(bone['name'])
        if bone['name'] not in heads:continue
        bone['pivot'][1]-=7
        for cube in bone.get('cubes',[]):
            cube['origin'][1]-=7
            if 'pivot' in cube:cube['pivot'][1]-=7
    write(m)
    return m

def animations():
    duration=4.8; tracks={}
    for name, axis, amp in [('body', 0, .35), ('head', 1, 1.7), ('head', 2, .45),
                             ('hair_left', 2, -.55), ('hair_right', 2, .55), ('hair_fringe', 0, .35),
                             ('skirt_left', 2, -.18), ('skirt_right', 2, .18)]:
        rotation=tracks.setdefault(name, {}).setdefault('rotation', {})
        for i in range(25):
            t=round(duration*i/24, 3); key=f'{t:g}'
            v=rotation.setdefault(key, [0, 0, 0]);v[axis]=round(math.sin(t/duration*math.tau)*amp, 6)
    return {'format_version': '1.8.0', 'animations': {'animation.elizabeth.ground_idle': {'loop': True, 'animation_length': duration, 'bones': tracks}}}

def write(m):
    OUT.mkdir(parents=True, exist_ok=True)
    atlas=m.atlas(); atlas.save(OUT/'elizabeth.png')
    geo={'format_version': '1.12.0', 'minecraft:geometry': [{'description': {'identifier': 'geometry.elizabeth_rework',
        'texture_width': SIZE, 'texture_height': SIZE, 'visible_bounds_width': 20, 'visible_bounds_height': 28,
        'visible_bounds_offset': [0, 12, 0]}, 'bones': m.bones}]}
    anim=animations()
    for name, data in [('elizabeth.geo.json', geo), ('elizabeth.animation.json', anim), ('parts.json', m.parts)]:
        (OUT/name).write_text(json.dumps(data, indent=2)+'\n', encoding='utf-8')
    bb(m, anim)
    print(f'Built fresh: {len(m.parts)} cuboids / {len(m.bones)} bones / atlas {SIZE}px, occupied height {m.atlas_height}px', flush=True)

def bb(m, anim):
    def ident(name): return str(uuid.uuid5(uuid.NAMESPACE_URL, 'velvet-eclipse/elizabeth-rework-05/'+name))
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
        clips.append({'uuid':ident(name),'name':name,'loop':'loop','length':a['animation_length'],'animators':animators})
    project={'meta':{'format_version':'4.10','model_format':'bedrock','box_uv':True},'name':'Elizabeth — contorno oval 05',
        'model_identifier':'elizabeth_rework','visible_box':[20,28,12],'resolution':{'width':SIZE,'height':SIZE},
        'elements':elements,'outliner':[group(b) for b in m.bones if 'parent' not in b],
        'textures':[{'name':'elizabeth.png','id':'0','uuid':ident('texture'),'path':'elizabeth.png',
        'source':'data:image/png;base64,'+base64.b64encode((OUT/'elizabeth.png').read_bytes()).decode(),'mode':'bitmap','saved':True}], 'animations':clips}
    (OUT/'elizabeth.bbmodel').write_text(json.dumps(project,indent=2)+'\n',encoding='utf-8')

if __name__=='__main__': build()
