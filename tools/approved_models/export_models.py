"""Export approved Pixie 03 / Elizabeth 05 once at the GeckoLib boundary.

Keep native integer dimensions, Box UV, textures and poses. World scale belongs
to the renderer; only action time is mapped to the existing combat impact.
"""
from pathlib import Path
import copy, hashlib, json, shutil

ROOT = Path(__file__).resolve().parent
ASSETS = ROOT.parents[1] / 'src/main/resources/assets/velvet_eclipse'
SCALES = {'pixie': .085, 'elizabeth': .11}
IMPACTS = {'magic': 1.18, 'physical': .66}

def convert_vector(value, channel):
    return [-value[0], -value[1], value[2]] if channel == 'rotation' else [-value[0], value[1], value[2]]

def retime(time, impact, end):
    return round(time / impact * 1.4 if time <= impact else 1.4 + (time-impact)/(end-impact)*1.1, 6)

def export(name):
    source = ROOT / name / 'draft'
    geo = json.loads((source / (name+'.geo.json')).read_text(encoding='utf-8'))
    for bone in geo['minecraft:geometry'][0]['bones']:
        bone['pivot'][0] *= -1
        if 'rotation' in bone: bone['rotation'] = convert_vector(bone['rotation'], 'rotation')
        for cube in bone.get('cubes', []):
            assert all(v > 0 and v == int(v) for v in cube['size'])
            cube['origin'][0] = -cube['origin'][0]-cube['size'][0]
            if 'pivot' in cube: cube['pivot'][0] *= -1
            if 'rotation' in cube: cube['rotation'] = convert_vector(cube['rotation'], 'rotation')
    authored = json.loads((source / (name+'.animation.json')).read_text())
    clips = {}
    for key, original in authored['animations'].items():
        clip = copy.deepcopy(original)
        kind = 'idle' if key.endswith('_idle') else 'magic' if key.endswith('magic_attack') else 'physical'
        for tracks in clip['bones'].values():
            for channel, keys in list(tracks.items()):
                assert channel in ('rotation', 'position')
                tracks[channel] = {
                    time if kind == 'idle' else str(retime(float(time), IMPACTS[kind], original['animation_length'])):
                    convert_vector(value, channel) for time, value in keys.items()
                }
        if kind != 'idle': clip['animation_length'] = 2.5
        clips['misc.idle' if kind == 'idle' else 'action.'+kind] = clip
    for folder, suffix, data in [('geo', '.geo.json', geo), ('animations', '.animation.json', {'format_version': authored['format_version'], 'animations': clips})]:
        (ASSETS/folder/'entity'/(name+suffix)).write_text(json.dumps(data, indent=2)+'\n', encoding='utf-8')
    shutil.copy2(source/(name+'.png'), ASSETS/'textures/entity'/(name+'.png'))
    return {'revision': '03' if name == 'pixie' else '05', 'scale': SCALES[name],
            'authoring_density': 8, 'clips': list(clips),
            'source_sha256': {p.name: hashlib.sha256(p.read_bytes()).hexdigest() for p in source.iterdir() if p.name in (name+'.geo.json', name+'.animation.json', name+'.png')}}

if __name__ == '__main__':
    report = {name: export(name) for name in SCALES}
    (ROOT/'integration.json').write_text(json.dumps(report, indent=2)+'\n')
    print('Exported approved Pixie 03 and Elizabeth 05; native dimensions and textures preserved.')
