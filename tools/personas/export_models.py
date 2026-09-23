"""Export approved v02 rigs without rebuilding/editing their poses. Python stdlib only.

The authoring geometry stays in Cartesian axes at uniform 4x density. Compensate
GeckoLib X coordinate / X-Y rotation signs once, preserving all Box UV dimensions.
The renderer sets the world scale. Retiming aligns each contact with server tick86.
"""
from pathlib import Path
import json, shutil

ROOT=Path(__file__).resolve().parent
ASSETS=ROOT.parents[1]/'src/main/resources/assets/velvet_eclipse'
MAPS={
 'arsene':{'magic':[(0,0),(.8,.75),(1.15,1.05),(1.55,1.4),(1.85,1.65),(2.35,2.2),(2.8,2.5)],
           'physical':[(0,0),(.65,.6),(.95,1.05),(1.25,1.4),(1.55,1.7),(2,2.15),(2.6,2.5)]},
 'orpheus':{'magic':[(0,0),(.4,.35),(.8,.7),(1.2,1.1),(1.6,1.4),(2.1,1.8),(2.6,2.15),(3.2,2.5)],
            'physical':[(0,0),(.7,.7),(1.05,1.05),(1.3,1.4),(1.65,1.7),(2.25,2.15),(2.8,2.5)]}
}
def write(path,data):path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def remap(t,points):
 for (a,x),(b,y) in zip(points,points[1:]):
  if t<=b+1e-7:return round(x+(y-x)*(t-a)/(b-a),6)
 return points[-1][1]
def export(name):
 source=ROOT/name/'draft'
 geo=json.loads((source/(name+'.geo.json')).read_text(encoding='utf-8'))
 for bone in geo['minecraft:geometry'][0]['bones']:
  bone['pivot'][0]*=-1
  if 'rotation' in bone:bone['rotation']=[-bone['rotation'][0],-bone['rotation'][1],bone['rotation'][2]]
  for cube in bone.get('cubes',[]):
   assert all(d>0 and d==int(d) for d in cube['size']), 'Native cuboids require positive integer dimensions'
   cube['origin'][0]=-cube['origin'][0]-cube['size'][0]
   if 'pivot' in cube:cube['pivot'][0]*=-1
   if 'rotation' in cube:cube['rotation']=[-cube['rotation'][0],-cube['rotation'][1],cube['rotation'][2]]
 write(ASSETS/'geo/entity'/(name+'.geo.json'),geo)
 authored=json.loads((source/(name+'.animation.json')).read_text(encoding='utf-8'))
 result={'format_version':authored['format_version'],'animations':{}}
 for key,clip in authored['animations'].items():
  kind='idle' if key.endswith('ground_idle') else 'magic' if key.endswith('magic_attack') else 'physical'
  for tracks in clip['bones'].values():
   for channel,keys in list(tracks.items()):
    converted={}
    for t,v in keys.items():
     v=[-v[0],-v[1],v[2]] if channel=='rotation' else [-v[0],v[1],v[2]] if channel=='position' else v
     converted[t if kind=='idle' else str(remap(float(t),MAPS[name][kind]))]=v
    tracks[channel]=converted
  if kind!='idle':clip['animation_length']=2.5
  result['animations']['misc.idle' if kind=='idle' else 'action.'+kind]=clip
 write(ASSETS/'animations/entity'/(name+'.animation.json'),result)
 shutil.copy2(source/(name+'.png'),ASSETS/'textures/entity'/(name+'.png'))
 print(name+': approved geometry/texture, idle and synchronized magic + physical clips exported.')

if __name__=='__main__':
 for name in MAPS:export(name)
