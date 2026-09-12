"""Build and export the approved voxel model to GeckoLib. Python: Pillow + NumPy."""
from pathlib import Path
import json, shutil
import animate_study as study

assets=Path(__file__).resolve().parents[2]/'src/main/resources/assets/velvet_eclipse'
for folder,name in [('geo','izanagi.geo.json'),('textures','izanagi.png'),('textures','izanagi_glowmask.png')]:
    target=assets/folder/'entity'/name
    target.parent.mkdir(parents=True,exist_ok=True)
    shutil.copy2(study.OUT/name,target)

# Three transition ticks + 28 clip ticks put the contact pose at server tick 86.
# Preserve the approved poses; retime recovery to finish before the actor hides.
maps={'attack.magic':[(0,0),(.8,.85),(1.25,1.4),(1.65,1.7),(2.5,2.35),(2.8,2.5)],
      'attack.physical':[(0,0),(.55,.8),(.83,1.4),(1.1,1.65),(1.85,2.35),(2.2,2.5)]}
data=json.loads((study.OUT/'izanagi.animation.json').read_text(encoding='utf-8'))
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
