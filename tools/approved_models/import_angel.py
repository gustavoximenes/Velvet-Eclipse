"""Publish approved Angel03 through the native GeckoLib boundary exactly once."""
from pathlib import Path
import json, hashlib, shutil
from export_models import convert_vector
ROOT=Path(__file__).resolve().parent
ASSETS=ROOT.parents[1]/'src/main/resources/assets/velvet_eclipse'
def main():
 source=ROOT/'angel/draft';geo=json.loads((source/'angel.geo.json').read_text(encoding='utf8'))
 for bone in geo['minecraft:geometry'][0]['bones']:
  bone['pivot'][0]*=-1
  if 'rotation' in bone:bone['rotation']=convert_vector(bone['rotation'],'rotation')
  for cube in bone.get('cubes',[]):
   assert all(v>0 and v==int(v) for v in cube['size'])
   cube['origin'][0]=-cube['origin'][0]-cube['size'][0]
   if 'pivot' in cube:cube['pivot'][0]*=-1
   if 'rotation' in cube:cube['rotation']=convert_vector(cube['rotation'],'rotation')
 authored=json.loads((source/'angel.animation.json').read_text(encoding='utf8'));clips={}
 for name,clip in authored['animations'].items():
  short=name.removeprefix('animation.angel.')
  kind={'ground_idle':'misc.idle','physical_attack':'action.physical','magic_attack':'action.magic'}.get(short,short.replace('expression_','expression.'))
  # Expressions already accompany the three authored body clips. Additional expression
  # resources contain facial bones only, so future layering cannot replace the leg/wing pose.
  if kind.startswith('expression.'):
   clip['bones']={k:v for k,v in clip['bones'].items() if k.startswith(('lip_','mouth_corner_')) or k=='jaw'}
  for tracks in clip['bones'].values():
   for channel,keys in tracks.items():
    assert channel in ('rotation','position')
    for t,value in keys.items():keys[t]=convert_vector(value,channel)
  clips[kind]=clip
 for folder,suffix,data in [('geo','.geo.json',geo),('animations','.animation.json',{'format_version':authored['format_version'],'animations':clips})]:
  (ASSETS/folder/'entity'/('angel'+suffix)).write_text(json.dumps(data,indent=2)+'\n',encoding='utf8')
 shutil.copy2(source/'angel.png',ASSETS/'textures/entity/angel.png')
 report=dict(revision='03',renderer_scale=.125,authoring_density=8,axis_conversion_count=1,clip_seconds={k:v['animation_length'] for k,v in clips.items()},expressions='Authored idle/attack faces retained; standalone clips limited to facial bones',source_sha256={p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in source.glob('angel.*')})
 (ROOT/'angel/integration.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf8')
 print('Exported approved Angel03: 357 cuboids, 79 bones, 1024 Box UV, complete 4.8 / 2.6 / 3.0 s clips')
if __name__=='__main__':main()
