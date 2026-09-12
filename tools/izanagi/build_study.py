"""Independent cuboid study. Writes only beside this file, never into the mod.

All surfaces are native boxes/planes with integer sizes and Box UV. No meshes,
ellipsoids, cylinders, smooth normals, downloaded assets or image synthesis.
"""
from pathlib import Path
import json, math, uuid, base64
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parent
OUT = ROOT / 'draft'; OUT.mkdir(exist_ok=True)
S = 512
# One uniform half-unit grid for the whole model. It gives the face enough
# texels for iris/pupil and buckles while retaining native cuboids and Box UV.
DENSITY = 2
atlas = Image.new('RGBA', (S,S), (0,0,0,0))
glow = Image.new('RGBA', (S,S), (0,0,0,0))
P = {
 'cloth': ['#111822','#1d2834','#2c3a47','#40505b'],
 'black': ['#0b1019','#121b27','#1f2b39','#303e4b'],
 'metal': ['#647781','#8d9fa5','#bdcacc','#e2e7df'],
 'white': ['#819396','#b3c2bc','#dae3d6','#f4f4db'],
 'red': ['#32101b','#5c1d2c','#853040','#ac4752'],
 'gold': ['#655025','#9e813a','#d8b953','#f7df7a'],
}
bones=[]; parts=[]; bx=by=1; row=0

def bone(name, parent=None, pivot=(0,0,0), rotation=(0,0,0)):
    b={'name':name,'pivot':[n*DENSITY for n in pivot]}
    if parent: b['parent']=parent
    if any(rotation): b['rotation']=list(rotation)
    bones.append(b); return name

def face_uv(u,v,w,h,d):
    return {'north':(u+d,v+d,w,h), 'south':(u+2*d+w,v+d,w,h),
            'west':(u+d+w,v+d,d,h), 'east':(u,v+d,d,h),
            'up':(u+d,v,w,d), 'down':(u+d+w,v,w,d)}

def paint_face(w,h,mat,face,pattern):
    p=P[mat]; level={'north':2,'up':3,'south':1,'down':0,'west':1,'east':1}[face]
    im=Image.new('RGBA',(max(1,w),max(1,h)),p[level]); d=ImageDraw.Draw(im)
    if w==0 or h==0:return im
    # Hand-selected pixel clusters: seams, broad fold, bevel. No noise or blur.
    if mat in ('cloth','black') and w>=3 and h>=5:
        d.line([(w-1,2),(w-1,h-1)],fill=p[0])
        d.line([(0,0),(0,max(1,h//2))],fill=p[min(3,level+1)])
        if w>=5:
            d.line([(w//2,1),(w//2,min(h-1,3))],fill=p[1])
            d.line([(w//2+1,4),(w//2+1,h-2)],fill=p[1])
    if mat in ('metal','white') and w>=2:
        d.line([(0,0),(w-1,0)],fill=p[3])
        if h>=3:d.line([(w-1,1),(w-1,h-1)],fill=p[0])
    if pattern=='brow' and face=='north':
        d.rectangle((0,0,w-1,h-1),fill='#e1e8df')
        d.line([(0,0),(w-1,0)],fill='#f8f6e9')
        d.line([(0,h-1),(w-1,h-1)],fill='#b5c3bf')
    if pattern=='eye_panel' and face=='north':
        d.rectangle((0,0,w-1,h-1),fill=P['white'][2])
        eye=Image.new('RGBA',(8,6),(0,0,0,0));ed=ImageDraw.Draw(eye)
        ed.polygon([(0,0),(7,1),(6,4),(2,4),(0,3)],fill='#121b25')
        ed.rectangle((2,1,5,2),fill='#f1e479')
        ed.line([(2,3),(4,3)],fill='#d6bd38')
        ed.rectangle((4,1,5,2),fill='#080e19')
        ed.point((2,1),fill='#f7f3c1')
        im.alpha_composite(eye,(0,0))
        im.alpha_composite(eye.transpose(Image.Transpose.FLIP_LEFT_RIGHT),(w-8,0))
        d=ImageDraw.Draw(im)
        d.line([(w//2-1,1),(w//2-1,h-1)],fill='#f0f0de')
        d.line([(w//2,1),(w//2,h-1)],fill='#a4b5b3')
    if pattern=='crown' and face in ('north','south'):
        d.rectangle((0,0,w-1,h-1),fill=P['black'][1])
        d.polygon([(0,h-1),(0,2),(3,1),(7,3),(10,0),(w-1,2),(w-1,h-1)],fill=P['metal'][2])
        d.polygon([(2,h-1),(3,2),(7,4),(9,1),(10,h-1)],fill=P['white'][3])
        d.line([(10,0),(11,h-1)],fill=P['metal'][0])
    if pattern.startswith('cheek_') and face=='north':
        # The lower mask's dark chevron is a seam, not a separate mouth bar.
        d.rectangle((0,0,w-1,h-1),fill=P['white'][2])
        d.polygon([(0,0),(1,0),(2,h-1),(0,h-1)],fill=P['metal'][1])
        d.line([(0,3),(1,3),(w-2,5),(w-1,5)],fill=P['black'][1],width=1)
        d.line([(0,4),(1,4),(w-2,6),(w-1,6)],fill=P['metal'][0],width=1)
        if pattern.endswith('left'):im=im.transpose(Image.Transpose.FLIP_LEFT_RIGHT)
    if pattern in ('nose','chin') and face in ('north','south'):
        im=Image.new('RGBA',(w,h),(0,0,0,0));d=ImageDraw.Draw(im)
        if pattern=='nose':
            contour=[(0,0),(w-1,0),(w//2,h-1)]
        else:contour=[(0,0),(w-1,0),(w-2,h-3),(w//2,h-1),(1,h-3)]
        d.polygon(contour,fill=P['white'][2])
        if pattern=='nose':
            d.polygon([(0,0),(w//2-1,0),(w//2,h-1)],fill=P['white'][3])
            d.line([(w-1,0),(w//2,h-1)],fill=P['metal'][0])
        else:
            d.polygon([(0,0),(w//2-1,0),(w//2,h-1),(0,h-4)],fill=P['white'][3])
            d.line([(w-1,0),(w-1,h-4),(w//2,h-1)],fill=P['metal'][0])
    if pattern=='buckle' and face in ('north','west','east'):
        d.rectangle((0,0,w-1,h-1),fill=P['metal'][0])
        d.line([(0,h-1),(0,0),(w-1,0)],fill=P['metal'][3])
        if w>2 and h>2:d.rectangle((1,1,w-2,h-2),fill=P['red'][0])
    if pattern=='lapel' and face=='north':
        d.line([(0,0),(0,h-1)],fill=P['white'][3])
    if pattern=='collar' and face=='north':
        d.rectangle((0,0,w-1,h-1),fill=P['red'][2])
        d.line([(0,0),(w-1,0)],fill=P['black'][1])
    if pattern=='chest' and face=='north':
        d.rectangle((0,0,w-1,h-1),fill=P['cloth'][2])
        d.rectangle((0,0,1,h-1),fill=P['white'][1])
        d.line([(w-1,1),(w-1,h-1)],fill=P['white'][2])
        for x in (3,5,7,9):
            d.line([(x,1),(x,h-1)],fill=P['metal'][1])
        for y in (7,18):
            if y<h:d.rectangle((4,y,7,y+1),fill=P['metal'][2])
    if pattern=='medallion' and face in ('north','west','east'):
        d.rectangle((0,0,w-1,h-1),fill=P['black'][1])
        cx,cy=(w-1)//2,(h-1)//2
        for dx,dy in ((0,-4),(3,-3),(4,0),(3,3),(0,4),(-3,3),(-4,0),(-3,-3)):
            d.line([(cx+dx,cy+dy),(cx+dx//2,cy+dy//2)],fill=P['gold'][2],width=1)
        d.rectangle((cx-1,cy-1,cx+1,cy+1),fill=P['gold'][1])
        d.point((cx,cy),fill=P['gold'][3])
    if pattern=='glove' and face in ('north','south'):
        for x in (0,w-1):
            d.line([(x,h-3),(x,h-1)],fill=P['gold'][1])
        if h>3:d.line([(0,0),(w-1,0)],fill=P['metal'][2])
    if pattern=='armor_strip' and face=='north':
        d.rectangle((0,0,w-1,h-1),fill=P['metal'][1])
        d.line([(0,0),(0,h-1)],fill=P['metal'][3])
        d.line([(w-1,0),(w-1,h-1)],fill=P['black'][1])
        d.line([(w//2,1),(w//2,h-2)],fill=P['metal'][0])
    if pattern=='pommel' and face in ('north','south'):
        d.rectangle((0,0,w-1,h-1),fill=P['black'][0])
        for y in (2,h-4):
            d.rectangle((w//2-1,y,w//2,y+1),fill=P['metal'][2])
            d.point((w//2-1,y),fill=P['metal'][3])
    if pattern=='toe_blade' and face in ('up','down'):
        im=Image.new('RGBA',(w,h),(0,0,0,0));d=ImageDraw.Draw(im)
        # North-facing, angular toe: full width at the ankle, a central point.
        d.polygon([(w//2,0),(w-1,h-3),(w-1,h-1),(0,h-1),(0,h-3)],fill=P['metal'][1])
        d.polygon([(w//2,0),(w//2,h-1),(0,h-1),(0,h-3)],fill=P['metal'][2])
        d.line([(w//2,0),(w-1,h-3),(w-1,h-1)],fill=P['metal'][3])
    if pattern.startswith('coat_front_') and face=='north':
        d.rectangle((0,0,w-1,h-1),fill=P['black'][2])
        d.line([(w-2,2),(w-2,h-1)],fill=P['black'][1],width=2)
        d.line([(0,0),(0,h-1)],fill=P['red'][2],width=1)
        for y in range(14,h-3,9):
            d.rectangle((1,y,6,y+1),fill=P['metal'][2])
            d.line([(1,y),(5,y)],fill=P['metal'][3])
            d.point((6,y+2),fill=P['metal'][0])
        if pattern.endswith('right'):im=im.transpose(Image.Transpose.FLIP_LEFT_RIGHT)
    if pattern=='coat_back' and face=='north':
        d.rectangle((0,0,w-1,h-1),fill=P['red'][0])
        d.line([(w//2,0),(w//2,h-1)],fill=P['red'][1],width=2)
    if pattern=='lining' and face=='north':
        d.rectangle((0,0,w-1,h-1),fill=P['red'][1])
        d.line([(0,0),(0,h-1)],fill=P['red'][3])
    if pattern=='ribbon' and w>=2:
        d.line([(0,0),(0,h-1)],fill=P['white'][3])
    if pattern=='wrap':
        for y in range(1,h,3):
            d.line([(0,y),(w-1,y)],fill=P['metal'][0])
    if pattern=='blade' and face in ('north','south'):
        # Straight black blade, silver cutting edge and an actual alpha slot.
        im=Image.new('RGBA',(w,h),(0,0,0,0));d=ImageDraw.Draw(im)
        for y in range(h):
            left=0 if y<h-16 else int((y-(h-16))*(w-1)/15)
            for x in range(left,w):
                col=P['black'][0] if x<w-4 else P['metal'][1]
                if x==w-3:col=P['metal'][2]
                if x==w-2:col=P['metal'][3]
                if x==w-1:col=P['white'][3]
                if x in (2,3) and int(h*.63)<=y<int(h*.63)+8:continue
                d.point((x,y),fill=col)
    return im

def cube(group,name,origin,size,mat='cloth',pattern='',rotation=None,pivot=None):
    global bx,by,row
    authored_origin=origin
    origin=[n*DENSITY for n in origin]
    size=[n*DENSITY for n in size]
    assert all(int(n)==n and n>=0 for n in size)
    w,h,d=map(int,size); aw=2*(w+d);ah=h+d
    if bx+aw+1>S:bx=1;by+=row+2;row=0
    assert by+ah+1<=S, 'Atlas needs repacking'
    uv=[bx,by];bx+=aw+2;row=max(row,ah)
    for f,(u,v,fw,fh) in face_uv(*uv,w,h,d).items():
        if fw and fh:
            patch=paint_face(fw,fh,mat,f,pattern)
            atlas.paste(patch,(u,v))
            if pattern=='eye_panel' and f=='north':
                # Emit only the painted iris; dark pupils/lids remain dark.
                gd=ImageDraw.Draw(glow)
                for yy in range(fh):
                    for xx in range(fw):
                        color=patch.getpixel((xx,yy))
                        if color[0]>170 and color[1]>150 and color[2]<145:
                            gd.point((u+xx,v+yy),fill=color)
    c={'origin':list(origin),'size':list(size),'uv':uv}
    if rotation:
        c['rotation']=list(rotation);c['pivot']=[n*DENSITY for n in (pivot or authored_origin)]
    next(b for b in bones if b['name']==group).setdefault('cubes',[]).append(c)
    parts.append({'name':name,'bone':group,'material':mat,'pattern':pattern,'cube':c})
    return c

bone('root')
bone('body','root',(0,34,0),(-2,0,3))
cube('body','waist',(-6,32,-3.5),(12,10,7),'black')
cube('body','long_coat_chest',(-7,41,-3),(14,10,6),'black')
cube('body','chest_placket',(-3,38,-4),(6,13,1),'cloth','chest')
cube('body','hip',(-5,28,-3),(10,6,6),'black')
cube('body','belt',(-6.5,33,-4.8),(13,1.5,1),'black')
cube('body','left_white_lapel',(-5.5,38,-4),(2,14,1),'white','lapel',(0,0,12),(-4,51,-3))
cube('body','right_white_lapel',(3.5,38,-4),(2,14,1),'white','lapel',(0,0,-12),(4,51,-3))
cube('body','left_high_collar',(-5,50,-3),(2,6,4),'black','collar',(0,0,12),(-4,50,0))
cube('body','right_high_collar',(3,50,-3),(2,6,4),'black','collar',(0,0,-12),(4,50,0))
cube('body','back_high_collar',(-4,50,2),(8,5,1),'black')
cube('body','back_seam',(-1,35,3),(2,14,1),'cloth')
cube('body','coat_back_yoke',(-6,33,3.2),(12,9,1.5),'black')

# The supplied centre portrait is the face reference: white blindfold-like brow,
# visible recessed gold eyes, sharp central plate, lower chevrons, side buckles.
bone('neck','body',(0,50,0))
cube('neck','neck',(-2,50,-2),(4,4,4),'black')
bone('head','neck',(0,53,0),(2,-6,-3))
cube('head','helmet_core',(-3.75,54.5,-3),(7.5,8,6.5),'black')
cube('head','inner_face',(-3,53.5,-3.55),(6,6,6),'white')
cube('head','continuous_mask_foundation',(-3.5,54,-4.1),(7,5,.5),'white')
cube('head','crown_facets',(-4,60.9,-3.85),(8,2.5,0),'metal','crown')
cube('head','solid_white_brow',(-4.75,59.4,-4.8),(9.5,1.5,8.5),'white','brow')
cube('head','eyes_below_brow',(-4,56.4,-4.55),(8,3,0),'white','eye_panel')
cube('head','right_cheek',(-4,54.1,-4.3),(2.5,4,1),'white','cheek_right',rotation=(0,0,20),pivot=(-3.5,58.1,-3.8))
cube('head','left_cheek',(1.5,54.1,-4.3),(2.5,4,1),'white','cheek_left',rotation=(0,0,-20),pivot=(3.5,58.1,-3.8))
cube('head','nose_structure',(-.5,56,-4.65),(1,3,.5),'metal',rotation=(4,0,0),pivot=(0,59,-4.2))
cube('head','pointed_nose_plate',(-1.5,54.5,-4.95),(3,4.5,0),'white','nose',rotation=(4,0,0),pivot=(0,59,-4.2))
cube('head','jaw_core',(-1.5,52.5,-3.8),(3,2,4),'white')
cube('head','pointed_chin',(-2,51.5,-4.5),(4,3,0),'white','chin')
for s,label in ((-1,'right'),(1,'left')):
    cube('head',label+'_temple',(s*4-.5,55,-2),(1,5.5,4),'metal')
    cube('head',label+'_helmet_fin',(s*4-.75,60.5,-1),(1.5,5.5,2),'white',rotation=(0,0,-s*6),pivot=(s*4,60.5,0))
    cube('head',label+'_fin_tip',(s*4-.5,65.5,-.5),(1,1.5,1),'white',rotation=(0,0,-s*6),pivot=(s*4,60.5,0))
    cube('head',label+'_mask_strap',(s*4.25-.25,54,-3.5),(.5,7.5,1.5),'red')
    for index,y in enumerate((54.5,56.5,58.5)):
        cube('head',f'{label}_buckle_{index}',(s*4.65-.25,y,-3.85),(.5,1.5,2),'metal','buckle')
    cube('head',label+'_brow_rivet',(s*4-.25,60,-4.9),(.5,.5,.5),'red')

# The reference floats with an unequal leg bend and pointed armored feet.
for s,label in ((-1,'right'),(1,'left')):
    x=s*3.3
    bent=label=='right'
    bone(label+'_thigh','body',(x,31,0),(25,5,-5) if bent else (-3,-7,23))
    cube(label+'_thigh',label+'_trouser',(x-2,18,-2),(4,14,4),'black')
    bone(label+'_shin',label+'_thigh',(x,18,0),(-42,0,0) if bent else (3,0,0))
    cube(label+'_shin',label+'_shin_core',(x-2,5,-2),(4,13,4),'black')
    cube(label+'_shin',label+'_greave',(x-1.5,6,-2.7),(3,11,.5),'metal','armor_strip')
    bone(label+'_foot',label+'_shin',(x,5,0),(-45,0,0) if bent else (-65,0,3))
    cube(label+'_foot',label+'_ankle',(x-1.5,3,-1.5),(3,4,3),'black')
    cube(label+'_foot',label+'_ankle_guard',(x-2,3,-2),(4,1.5,4),'black',rotation=(0,0,-12),pivot=(x,4,0))
    cube(label+'_foot',label+'_instep',(x-1.25,1,-3.5),(2.5,2,4),'metal','armor_strip')
    cube(label+'_foot',label+'_pointed_toe_upper',(x-1.5,1.55,-8),(3,0,6),'metal','toe_blade')
    cube(label+'_foot',label+'_pointed_toe_lower',(x-1.5,1.05,-8),(3,0,6),'metal','toe_blade')

# A continuous wrap coat: front panels start near the centre, overlap the waist,
# and join full-depth side walls and a covered centre back. Only the front opens.
bone('coat_back','body',(0,39,3.5))
cube('coat_back','continuous_back',(-3,29,3.8),(6,11,1),'black','coat_back')
bone('coat_back_lower','coat_back',(0,31,3.5),(-7,0,0))
cube('coat_back_lower','continuous_lower_back',(-4,7,3.8),(8,24,1),'black','coat_back')
for s,label in ((-1,'right'),(1,'left')):
    bone('coat_'+label,'body',(s*.5,39,0),(0,0,s*3))
    x=-6.5 if s<0 else .5
    sx=-7 if s<0 else 6
    cube('coat_'+label,label+'_waist_front',(x,30.5,-4.3),(6,8.5,1),'black')
    cube('coat_'+label,label+'_waist_side',(sx,30.5,-3.3),(1,8.5,8),'black')
    cube('coat_'+label,label+'_waist_back',(x,30.5,4),(6,8.5,1),'black','coat_back')
    lower='coat_'+label+'_lower'
    bone(lower,'coat_'+label,(s*.5,31,0),(-5,0,s*(6 if s<0 else 10)))
    cube(lower,label+'_front_skirt',(x,7,-4.3),(6,24.5,1),'black','coat_front_'+label)
    cube(lower,label+'_inner_red',(x,7,-3.25),(6,24.5,.5),'red','lining')
    cube(lower,label+'_side_skirt',(sx,7,-3.3),(1,24.5,8),'black')
    cube(lower,label+'_back_skirt',(x,7,4),(6,24.5,1),'black','coat_back')

for s,label in ((-1,'right'),(1,'left')):
    x=s*9
    free=label=='left'
    bone(label+'_upper_arm','body',(x,49,0),(-12,0,19) if free else (-12,0,2))
    cube(label+'_upper_arm',label+'_upper_sleeve',(x-2,39,-2),(4,11,4),'black')
    cube(label+'_upper_arm',label+'_shoulder_cap',(x-2.5,46,-3),(5,6,6),'black','medallion')
    cube(label+'_upper_arm',label+'_shoulder_trim',(x-2.5,52.01,-3),(5,.5,6),'gold')
    cube(label+'_upper_arm',label+'_arm_plate',(x-1.25,40,-2.4),(2.5,6,.5),'metal','armor_strip')
    bone(label+'_forearm',label+'_upper_arm',(x,39,0),(-8,0,-3) if free else (-48,0,0))
    cube(label+'_forearm',label+'_forearm',(x-1.5,29,-2),(3,10,4),'black')
    cube(label+'_forearm',label+'_forearm_plate',(x-1,30,-2.4),(2,9,.5),'metal','armor_strip')
    cube(label+'_forearm',label+'_silver_cuff',(x-2,29,-2.5),(4,2,5),'metal')
    bone(label+'_hand',label+'_forearm',(x,29,0),(-12,-30,8) if free else (0,0,10))
    cube(label+'_hand',label+'_palm',(x-1.5,25.5,-1.5),(3,3.5,2),'black','glove')
    if free:
        for i,(offset,length,spread) in enumerate(((-1.125,3.5,-4),(-.375,4.5,-1),(.375,4,2),(1.125,3.5,5))):
            finger=f'{label}_finger_{i}'
            bone(finger,label+'_hand',(x+offset,25.5,-.5),(32+i*2,0,spread))
            cube(finger,finger+'_base',(x+offset-.25,23,-1),(.5,2.5,1),'black')
            cube(finger,finger+'_lower',(x+offset-.25,25.5-length,-1.5),(.5,length-2,.5),'black')
            cube(finger,finger+'_tip',(x+offset-.25,25.25-length,-1.5),(.5,.5,.5),'metal')
        bone('left_thumb','left_hand',(x-1.5,27,-.5),(10,0,-35))
        cube('left_thumb','left_thumb_base',(x-2,24.5,-1),(1,2.5,1),'black')
        cube('left_thumb','left_thumb_lower',(x-2,23.5,-1.5),(.5,1.5,.5),'black')
        cube('left_thumb','left_thumb_tip',(x-2,23.25,-1.5),(.5,.5,.5),'metal')
    else:
        for i in range(4):
            fx=x-1.125+i*.75
            cube(label+'_hand',f'grip_finger_{i}',(fx-.25,25,-1),(.5,2,2),'black')
        cube(label+'_hand','grip_thumb',(x-1.5,27,-2),(2,1,1),'black')

# Paired, slightly splayed ribbons in only two segments each.
for s,label in ((-1,'right'),(1,'left')):
    bone(label+'_ribbon','head',(s*4,58,2),(-24,s*14,s*18))
    cube(label+'_ribbon',label+'_ribbon_upper',(s*4-1,39,2),(2,19,0),'white','ribbon')
    bone(label+'_ribbon_end',label+'_ribbon',(s*4,39,2),(16,-s*10,-s*10))
    cube(label+'_ribbon_end',label+'_ribbon_lower',(s*4-1,20,2),(2,19,0),'white','ribbon')
    bone(label+'_ribbon_tail',label+'_ribbon_end',(s*4,20,2),(18,0,-s*6))
    cube(label+'_ribbon_tail',label+'_ribbon_tail',(s*4-.75,8,2),(1.5,12,0),'white','ribbon')

# A single long shaft traverses the gripping hand behind the coat. Its black
# guard and slotted straight blade follow the full-body reference, not a sabre.
bone('weapon','right_hand',(-9,27,-.5),(0,0,-84))
cube('weapon','long_black_shaft',(-9.75,13,-1.25),(1.5,34,1.5),'black')
cube('weapon','shaft_inlay',(-9.3,13,-1.3),(.5,21,.5),'metal')
cube('weapon','wrapped_rear_grip',(-10,35,-1.5),(2,10,2),'white','wrap')
cube('weapon','dark_pommel',(-10.5,45,-1.5),(3,4.5,2),'black','pommel')
cube('weapon','black_cross_guard',(-12.5,11.5,-1.75),(7,1.5,2.5),'black')
cube('weapon','guard_edge',(-12.5,11.5,-1.85),(.5,1.5,2.5),'metal')
cube('weapon','blade_spine',(-11.5,-9,-.7),(.5,21,.5),'black')
cube('weapon','straight_slotted_blade',(-11.75,-21,-1.1),(5.5,33,0),'metal','blade')

geometry={'format_version':'1.12.0','minecraft:geometry':[{
 'description':{'identifier':'geometry.izanagi_voxel_study','texture_width':S,'texture_height':S,
 'visible_bounds_width':20,'visible_bounds_height':18,'visible_bounds_offset':[0,4,0]},
 'bones':bones}]}
(OUT/'izanagi.geo.json').write_text(json.dumps(geometry,indent=2),encoding='utf-8')
(OUT/'parts.json').write_text(json.dumps(parts,indent=2),encoding='utf-8')
atlas.save(OUT/'izanagi.png');glow.save(OUT/'izanagi_glowmask.png')

# Pose is stored in geometry; these channels add a small seamless idle to it.
# Weapon follows the gripping hand through the bone hierarchy at every time.
duration=4.8
times=[round(i*duration/16,3) for i in range(17)]
tracks={}
def track(name,channel,fn):
    keys={str(t):[round(float(n),5) for n in fn(t)] for t in times}
    keys[str(duration)]=keys['0.0'].copy()
    tracks.setdefault(name,{})[channel]=keys
def wave(t,phase=0):
    return math.sin(t*2*math.pi/duration+phase)-math.sin(phase)
track('root','position',lambda t:(0,.45*DENSITY*(1-math.cos(t*2*math.pi/duration)),0))
track('body','rotation',lambda t:(.45*wave(t),0,.3*wave(t,.4)))
track('head','rotation',lambda t:(.3*wave(t,.6),.7*wave(t,.2),0))
track('left_forearm','rotation',lambda t:(.8*wave(t,.5),0,.35*wave(t,.8)))
track('right_forearm','rotation',lambda t:(.4*wave(t,.4),0,0))
for s,label in ((-1,'right'),(1,'left')):
    track('coat_'+label+'_lower','rotation',lambda t,s=s:(1.1*wave(t,.4*s),0,s*.65*wave(t,.8)))
    track(label+'_ribbon','rotation',lambda t,s=s:(1.7*wave(t,.5*s),s*1.4*wave(t,.8),s*.8*wave(t,.3)))
    track(label+'_ribbon_end','rotation',lambda t,s=s:(2.4*wave(t,.9*s),s*2.0*wave(t,1.1),0))
    track(label+'_ribbon_tail','rotation',lambda t,s=s:(3.0*wave(t,1.2*s),s*1.5*wave(t,1.4),0))
    track(label+'_foot','rotation',lambda t:(.55*wave(t,.7),0,0))
track('coat_back_lower','rotation',lambda t:(1.0*wave(t,.4),0,0))
for i in range(4):
    track(f'left_finger_{i}','rotation',lambda t,i=i:(1.2*wave(t,i*.2),0,0))
animation={'format_version':'1.8.0','animations':{'misc.idle':{
    'loop':True,'animation_length':duration,'bones':tracks}}}
(OUT/'izanagi.animation.json').write_text(json.dumps(animation,indent=2),encoding='utf-8')
print(f'Draft: {len(parts)} cuboids/planes, {len(bones)} groups, {S}x{S} atlas; used height {by+row}.')
