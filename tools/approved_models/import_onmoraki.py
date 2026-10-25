"""Export Onmoraki 01 once at the GeckoLib boundary, preserving full authored clip durations."""
from pathlib import Path
import json, hashlib, shutil
from export_models import convert_vector
ROOT=Path(__file__).resolve().parent
ASSETS=ROOT.parents[1]/'src/main/resources/assets/velvet_eclipse'
def main():
 source=ROOT/'onmoraki/draft'; geo=json.loads((source/'onmoraki.geo.json').read_text(encoding='utf8'))
 for bone in geo['minecraft:geometry'][0]['bones']:
  bone['pivot'][0]*=-1
  if 'rotation' in bone:bone['rotation']=convert_vector(bone['rotation'],'rotation')
  for cube in bone.get('cubes',[]):
   assert all(v>0 and v==int(v) for v in cube['size'])
   cube['origin'][0]=-cube['origin'][0]-cube['size'][0]
   if 'pivot' in cube:cube['pivot'][0]*=-1
   if 'rotation' in cube:cube['rotation']=convert_vector(cube['rotation'],'rotation')
 authored=json.loads((source/'onmoraki.animation.json').read_text(encoding='utf8')); clips={}
 for name,clip in authored['animations'].items():
  kind='misc.idle' if name.endswith('_idle') else 'action.magic' if name.endswith('magic_attack') else 'action.physical'
  for tracks in clip['bones'].values():
   for channel,keys in tracks.items():
    assert channel in ('rotation','position')
    for t,value in keys.items():keys[t]=convert_vector(value,channel)
  clips[kind]=clip
 for folder,suffix,data in [('geo','.geo.json',geo),('animations','.animation.json',{'format_version':authored['format_version'],'animations':clips})]:
  (ASSETS/folder/'entity'/('onmoraki'+suffix)).write_text(json.dumps(data,indent=2)+'\n',encoding='utf8')
 shutil.copy2(source/'onmoraki.png',ASSETS/'textures/entity/onmoraki.png')
 report=dict(revision='01',renderer_scale=.125,authoring_density=8,axis_conversion_count=1,clip_seconds={k:v['animation_length'] for k,v in clips.items()},damage='after the complete clip; PersonaActionTiming',source_sha256={p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in source.glob('onmoraki.*')})
 (ROOT/'onmoraki/integration.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf8')
 print('Exported Onmoraki01: 96 cuboids, native Box UV, complete 3.6 / 2.4 / 2.8 s clips.')
if __name__=='__main__':main()
