"""Rebuild the transcribed floor tables; see SOURCES.md for provenance and normalization."""
import json
from pathlib import Path
root=Path(__file__).resolve().parents[2]
for name in ('shuffle-personas.json','shuffle-ranks.json'):
 data=json.loads((Path(__file__).parent/'facts'/name).read_text(encoding='utf8'))
 assert len(data)==32
 (root/'src/main/resources/data/velvet_eclipse/catalogue'/name).write_text(json.dumps(data,indent=2)+'\n',encoding='utf8')
