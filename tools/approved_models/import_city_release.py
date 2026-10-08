"""Import approved standalone revisions; keep Box UV and authoring files."""
from pathlib import Path
import json,shutil,copy
import export_models as common
ROOT=Path(__file__).resolve().parents[3]
models={'apsaras':('apsaras-study-04',.0625,1.50,.68),'jack_frost':('jack-frost-study-01',.125,1.8,1.8),'forneus':('forneus-study-01',.125,1.75,1.75),'chimera':('chimera-study-01',.125,1.15,1.0)}
for name,(folder,scale,magic,physical) in models.items():
 src=ROOT/'modeling'/folder/'draft';dst=common.ROOT/name/'draft';dst.mkdir(parents=True,exist_ok=True)
 if src.is_dir():
  for ext in ['.geo.json','.animation.json','.png','.bbmodel']:shutil.copy2(src/(name+ext),dst/(name+ext))
 assert all((dst/(name+ext)).is_file() for ext in ['.geo.json','.animation.json','.png','.bbmodel']), 'Missing approved authoring source: '+name
 common.SCALES[name]=scale;common.IMPACTS.update(magic=magic,physical=physical)
 report=common.export(name)
 file=common.ASSETS/'animations/entity'/f'{name}.animation.json';a=json.loads(file.read_text());clips=a['animations']
 if 'action.physical' not in clips:clips['action.physical']=copy.deepcopy(clips['action.magic'])
 file.write_text(json.dumps(a,indent=2)+'\n')
 print(name,scale,list(clips))
