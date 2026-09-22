"""Import factual Police Station unlock rows from a locally downloaded source HTML.
Usage: python tools/progression/import_shop.py path/to/overworld.html
Source: https://aqiu384.github.io/megaten-database/p3r/overworld.html
Recruitment-only equipment is assigned to the nearest campaign period explicitly below.
"""
import html, json, re, sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
catalog = json.loads((ROOT / 'src/main/resources/data/velvet_eclipse/reload_equipment.json').read_text(encoding='utf-8'))
normalize = lambda s: re.sub('[^a-z0-9]', '', s.lower())
by_name = {normalize(g['name']): g for g in catalog}
periods = ['-', '5/10', '6/14', '7/18', '8/8', '9/10', '10/6', '11/6', '12/10', '1/1']
recruits = {}
for tier, names in {
    1: ['Bladefist', 'Sonic Fist'],
    2: ['Quarter Pike'],
    3: ['Six-Shot', 'Grenade Launcher', 'Aramid Full Shell', 'Aramid Vest', 'Fullerene Legs', 'Alnico Legs'],
    4: ['Blitz Kunai', 'Lucky Knife', 'Hard Dog Suit', 'Leather Dog Suit'],
    5: ['Glaive', 'Sexy Lance', 'Ogre Hammer', 'Night Stalker'],
}.items():
    recruits.update({normalize(n): tier for n in names})
source = Path(sys.argv[1]).read_text(encoding='utf-8').split('<h3 id="police-station">', 1)[1].split('</table>', 1)[0]
rows = []
for row in re.findall(r'<tr>(.*?)</tr>', source, re.S):
    cells = [html.unescape(re.sub('<[^>]+>', '', c)).strip() for c in re.findall(r'<td>(.*?)</td>', row, re.S)]
    if not cells:
        continue
    date, name, price = cells
    gear = by_name[normalize(name)]
    tier = recruits.get(normalize(name), 0) if date == '-' else periods.index(date)
    assert gear['originalPrice'] == int(price), (name, gear['originalPrice'], price)
    rows.append(dict(id=gear['id'], tier=tier, date=date))
assert len(rows) == 250 and len({r['id'] for r in rows}) == 250
assert sum(r['tier'] == 0 for r in rows) == 17
out = ROOT / 'src/main/resources/data/velvet_eclipse/reload_shop.json'
out.write_text(json.dumps(rows, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print('Shop rows:', len(rows), 'per period:', [sum(r['tier'] == n for r in rows) for n in range(10)])
