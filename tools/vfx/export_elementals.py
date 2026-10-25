"""Reproduce approved elemental geometry/recipes at the Photon 1.1.17 boundary.

The exported PNG flipbooks are committed in src/main/resources. No original game install is
needed to build the mod. Original-reference capture scripts are historical audit tools.
"""
from pathlib import Path
import json
ROOT=Path(__file__).resolve().parents[2]; STUDY=Path(__file__).resolve().parent/'approved02'; R=ROOT/'src/main/resources'
def main():
 recipes=json.loads((STUDY/'recipes.json').read_text(encoding='utf8')); bindings={}
 for r in recipes:
  first=min(p['start'] for p in r['parts']);r['sourceStartOffset']=first
  for p in r['parts']:p['start']-=first
 for line in (STUDY/'COBERTURA.md').read_text(encoding='utf8').splitlines():
  if line.startswith('| ') and line.split('|')[2].strip().isdigit():
   c=[s.strip() for s in line.split('|')];bindings[c[1]]=c[3]
 assert len(bindings)==38
 (R/'data/velvet_eclipse/presentation/elemental-effects.json').write_text(json.dumps(dict(revision=2,recipes=recipes,bindings=bindings),indent=2,ensure_ascii=False)+'\n',encoding='utf8')
 out=R/'assets/velvet_eclipse/models/vfx/audit02';out.mkdir(parents=True,exist_ok=True)
 for name,mesh in json.loads((STUDY/'meshes.json').read_text(encoding='utf8')).items():
  lines=['mtllib white.mtl','usemtl white','vt 0.5 0.5']
  # TileParticle Model mode subtracts (.5,.5,.5) before scale/rotation; compensate once in OBJ.
  lines+=['v '+' '.join(format(float(x)+.5,'.9g') for x in v) for v in mesh['vertices']]
  lines+=['f '+' '.join(f'{i+1}/1' for i in face) for face in mesh['faces']]
  (out/(name+'.obj')).write_text('\n'.join(lines)+'\n',encoding='utf8')
  data={'loader':'forge:obj','model':'velvet_eclipse:models/vfx/audit02/'+name+'.obj','flip_v':True,'automatic_culling':False,'shade_quads':False,'textures':{'white':'velvet_eclipse:vfx/audit02/white','particle':'velvet_eclipse:vfx/audit02/white'}}
  (out/(name+'.json')).write_text(json.dumps(data,indent=2)+'\n',encoding='utf8')
 (out/'white.mtl').write_text('newmtl white\nKd 1 1 1\nmap_Kd #white\n',encoding='utf8')
 print('Exported 22 recipes, 38 bindings and centered Photon OBJ meshes; summoning untouched.')
if __name__=='__main__':main()
