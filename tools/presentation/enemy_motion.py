"""Adds looping locomotion to the approved Shadow rigs without changing their meshes."""
from pathlib import Path
import json

root = Path(__file__).resolve().parents[2]
for path in (root / 'src/main/resources/assets/velvet_eclipse/animations/entity').glob('enemy_*.animation.json'):
    data = json.loads(path.read_text(encoding='utf-8'))
    bones = data['animations']['misc.idle']['bones']
    walk = {'body': {'position': {'0': [0,0,0], '.15': [0,1.1,0], '.3': [0,0,0], '.45': [0,1.1,0], '.6': [0,0,0]}, 'rotation': [5,0,0]}}
    for i, bone in enumerate(bones):
        sign = -1 if i % 2 else 1
        if bone.startswith('wheel'):
            walk[bone] = {'rotation': {'0': [0,0,0], '.6': [360,0,0]}}
        elif bone.startswith(('leg', 'arm', 'wing', 'tail', 'ribbon')):
            amplitude = 28 if bone.startswith(('leg', 'arm')) else 18 if bone.startswith('wing') else 10
            axis = 2 if bone.startswith(('wing', 'ribbon')) else 0
            a, b = [0,0,0], [0,0,0]
            a[axis], b[axis] = sign*amplitude, -sign*amplitude
            walk[bone] = {'rotation': {'0': a, '.3': b, '.6': a}}
    data['animations']['misc.walk'] = {'loop': True, 'animation_length': .6, 'bones': walk}
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2)+'\n', encoding='utf-8')
print('Updated 19 Shadow locomotion clips.')
