"""Verify that a revision-3 release can still decode every saved architecture ID."""
from pathlib import Path
import gzip,json,sys,zipfile,hashlib

ROOT=Path(__file__).resolve().parents[2]
RES=ROOT/'src/main/resources'
baseline=json.loads((Path(__file__).parent/'baseline.json').read_text())
previous=Path(sys.argv[1])
with zipfile.ZipFile(previous) as jar:
 old_defs=json.loads(gzip.decompress(jar.read('data/velvet_eclipse/city/architecture.json.gz')))
 new_defs=json.loads(gzip.decompress((RES/'data/velvet_eclipse/city/architecture.json.gz').read_bytes()))
 assert len(old_defs)==baseline['tiles']
 assert new_defs[:len(old_defs)]==old_defs,'Saved tile metadata changed'
 old_raw=gzip.decompress(jar.read('assets/velvet_eclipse/city/architecture.bin.gz'))
 new_raw=gzip.decompress((RES/'assets/velvet_eclipse/city/architecture.bin.gz').read_bytes())
 assert len(old_raw)==baseline['floats']*4
 assert new_raw[:len(old_raw)]==old_raw,'Saved tile vertices changed'
 for name in ['plan','furniture']:
  old=json.loads(gzip.decompress(jar.read(f'data/velvet_eclipse/city/{name}.json.gz')))
  kept=json.loads(gzip.decompress((RES/f'data/velvet_eclipse/city/legacy-{name}-v3.json.gz').read_bytes()))
  assert old==kept,f'Previous {name} not retained exactly'
report=dict(previous=previous.name,previousSha256=hashlib.sha256(previous.read_bytes()).hexdigest(),preservedTiles=len(old_defs),preservedVertexBytes=len(old_raw),legacyPlan=True,legacyFurniture=True)
out=ROOT/'build/reports/gekkoukan';out.mkdir(parents=True,exist_ok=True)
(out/'legacy-assets.json').write_text(json.dumps(report,indent=2))
print('PASS legacy assets:',len(old_defs),'stable tile IDs;',len(old_raw),'unchanged vertex bytes; exact previous plan and door variants')
