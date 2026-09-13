"""Build and export the approved voxel model to GeckoLib. Python: Pillow + NumPy."""
from pathlib import Path
import json, shutil
import animate_study as study
from build_study import face_uv

assets=Path(__file__).resolve().parents[2]/'src/main/resources/assets/velvet_eclipse'
for folder,name in [('geo','izanagi.geo.json'),('textures','izanagi.png'),('textures','izanagi_glowmask.png')]:
    target=assets/folder/'entity'/name
    target.parent.mkdir(parents=True,exist_ok=True)
    shutil.copy2(study.OUT/name,target)

# The preview authors Cartesian rotations. GeckoLib negates X coordinates and
# X/Y angles while baking Bedrock JSON. Compensate once at the export boundary.
geometry=json.loads((study.OUT/'izanagi.geo.json').read_text(encoding='utf-8'))
for bone in geometry['minecraft:geometry'][0]['bones']:
    bone['pivot'][0]*=-1
    if 'rotation' in bone:bone['rotation']=[-bone['rotation'][0],-bone['rotation'][1],bone['rotation'][2]]
    for cube in bone.get('cubes',[]):
        cube['origin'][0]=-cube['origin'][0]-cube['size'][0]
        if 'pivot' in cube:cube['pivot'][0]*=-1
        if 'rotation' in cube:cube['rotation']=[-cube['rotation'][0],-cube['rotation'][1],cube['rotation'][2]]
        if 0 in cube['size']:
            # Opposite faces of a zero-thickness cube fought at the same depth,
            # especially in the emissive eye pass. Preserve original UV rectangles.
            faces=face_uv(*cube['uv'],*cube['size'])
            cube['uv']={name:{'uv':[u,v],'uv_size':[w,h]} for name,(u,v,w,h) in faces.items() if w>0 and h>0}
            cube['size']=[.08 if n==0 else n for n in cube['size']]
(assets/'geo/entity/izanagi.geo.json').write_text(json.dumps(geometry,indent=2),encoding='utf-8')

# Three transition ticks + 28 clip ticks put the contact pose at server tick 86.
# Preserve the approved poses; retime recovery to finish before the actor hides.
maps={'attack.magic':[(0,0),(.8,.85),(1.25,1.4),(1.65,1.7),(2.5,2.35),(2.8,2.5)],
      'attack.physical':[(0,0),(.55,.8),(.83,1.4),(1.1,1.65),(1.85,2.35),(2.2,2.5)]}
data=json.loads((study.OUT/'izanagi.animation.json').read_text(encoding='utf-8'))
for clip in data['animations'].values():
    for tracks in clip['bones'].values():
        for channel,keys in tracks.items():
            for t,v in keys.items():
                if channel=='rotation':keys[t]=[-v[0],-v[1],v[2]]
                elif channel=='position':keys[t]=[-v[0],v[1],v[2]]
for name,points in maps.items():
    clip=data['animations'].pop(name)
    def remap(t):
        for (a,x),(b,y) in zip(points,points[1:]):
            if t<=b+1e-7:return x+(y-x)*(t-a)/(b-a)
        return points[-1][1]
    for tracks in clip['bones'].values():
        for channel,keys in tracks.items():
            tracks[channel]={str(round(remap(float(t)),6)):v for t,v in keys.items()}
    clip['animation_length']=2.5
    data['animations'][name.replace('attack.','action.')]=clip
(assets/'animations/entity/izanagi.animation.json').write_text(json.dumps(data,indent=2),encoding='utf-8')
print('Exported Izanagi: native geometry, 512px atlas, idle + two synchronized attacks.')
