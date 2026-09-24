import json, collections
from pathlib import Path
p=Path(__file__).parent/'upstream'
read=lambda f:json.loads((p/f).read_text(encoding='utf-8-sig'))
demons=read('demon-data.json'); unlocks=read('demon-unlocks.json'); skills=read('skill-data.json')
dlc=set(next(x['conditions'] for x in unlocks if x['category']=='Downloadable Content'))
base={k:v for k,v in demons.items() if k not in dlc}
used={k for v in base.values() for k in v['skills']}
selected={k:v for k,v in skills.items() if v['a'][0] in used or v['b'][0]>0 or v['c'][2].startswith('Sword')}
print('BASE',len(base),'DLC',len(dlc),'SELECTED',len(selected))
print('Missing',used-{v['a'][0] for v in selected.values()})
for fmt in sorted(set(v['c'][1] for v in selected.values())):
 print(fmt, [(v['a'][0],v['c'][0],v['b'][7]) for v in selected.values() if v['c'][1]==fmt])
