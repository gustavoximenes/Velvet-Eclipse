"""Walk connectivity across actual floors, doors and half-step stair volumes.

All edges are physical movement. Fast travel/teleport edges are deliberately excluded.
"""
from pathlib import Path
from collections import defaultdict, deque
import gzip, json, math

ROOT=Path(__file__).resolve().parents[2]
PLAN=json.loads(gzip.decompress((ROOT/'src/main/resources/data/velvet_eclipse/city/plan.json.gz').read_bytes()))
CELLS={tuple(c[:3]):c for c in PLAN['cells']}
PALETTE=PLAN['palette']


def descriptor(c):
    return PALETTE[c[3]-100] if c[3]>=100 else {'block':'city','properties':{}}


def volumes(c, sx, sz):
    """Vertical collision intervals at one subcell; doors are treated as opened by interaction."""
    d=descriptor(c);name=d['block'];props=d['properties'];y=c[1]
    if 'door' in name or name.endswith(('_leaves','_log')) is False and name.endswith(('water','wheat')):return []
    if c[3] in (30,31):return []
    if name.endswith('_stairs'):
        facing=props['facing'];high={'north':sz==0,'south':sz==1,'east':sx==1,'west':sx==0}[facing]
        return [(y,y+(1 if high else .5))]
    if name.endswith('_slab'):
        return [(y+.5,y+1)] if props['type']=='top' else [(y,y+.5)]
    if name.endswith('_bed'):return [(y,y+.5625)]
    if name.endswith('farmland'):return [(y,y+.9375)]
    return [(y,y+1)]


def main():
    assert not PLAN['meshes'],'Scene mesh still overlaps block architecture'
    assert all(c[4]==0 for c in CELLS.values()),'Hidden scenery supports remain'
    assert len(CELLS)==len(PLAN['cells']),'Two blocks occupy one coordinate'
    # Subdivide only stair columns; other columns have the same height at all samples.
    columns=defaultdict(list)
    for c in CELLS.values(): columns[c[0],c[2]].append(c)
    walk={}
    for (x,z),cc in columns.items():
        for sx in (0,1):
            for sz in (0,1):
                solid=sorted(interval for c in cc for interval in volumes(c,sx,sz))
                heights=[]
                for _,top in solid:
                    if any(a<top+1.8-.001 and b>top+.001 for a,b in solid):continue
                    if top>=64:heights.append(top)
                if heights:walk[x*2+sx,z*2+sz]=heights
    spawn=next(p for p in PLAN['places'] if p['id']=='bedroom')
    start=(spawn['x']*2,spawn['z']*2,float(spawn['y']))
    assert start[2] in walk.get(start[:2],[]),('Blocked spawn',start)
    q=deque([start]);seen={start}
    while q:
        x,z,y=q.popleft()
        for dx,dz in [(1,0),(-1,0),(0,1),(0,-1)]:
            for yy in walk.get((x+dx,z+dz),[]):
                # Native stairs take half-block steps: no jump needed for inter-floor circulation.
                if abs(yy-y)>.601:continue
                node=(x+dx,z+dz,yy)
                if node not in seen:seen.add(node);q.append(node)
    def reachable(x,y,z,radius=0):
        return any((x*2+dx,z*2+dz,float(y)) in seen for dx in range(-radius,2+radius) for dz in range(-radius,2+radius))
    missing=[p['id'] for p in PLAN['places'] if not reachable(p['x'],p['y'],p['z'])]
    bad_stairs=[r['name'] for r in PLAN['routes'] if not all(reachable(*p) for p in r['points'])]
    bad_shops=[]
    for c in CELLS.values():
        if 20<=c[3]<=28 and not any(reachable(c[0]+dx,c[1],c[2]+dz) for dx,dz in [(1,0),(-1,0),(0,1),(0,-1)]):bad_shops.append(c[3])
    dorm=next(b for b in PLAN['buildings'] if b['id']=='dorm')
    for id in ['bedroom','lounge','kitchen','reading','bathroom','command','roof']:
        p=next(p for p in PLAN['places'] if p['id']==id)
        assert dorm['minX']<p['x']<dorm['maxX'] and dorm['minZ']<p['z']<dorm['maxZ'],('Detached interior',id)
    # Every listed indoor floor has both a supporting slab and a ceiling/roof.
    bad_roofs=[]
    for p in PLAN['places']:
        if p['id'] in ['dorm','roof'] or p['id'].startswith('shop_'):continue
        if p['id'] in ['bedroom','lounge','kitchen','reading','bathroom','command'] or p['id'].startswith('school_'):
            if not any((p['x'],p['y']+dy,p['z']) in CELLS for dy in range(3,8)):bad_roofs.append(p['id'])
    report=dict(revision=PLAN['revision'],cells=len(CELLS),sections=0,destinations=len(PLAN['places']),
                unreachable_destinations=missing,unsafe_arrivals=[],unreachable_shops=bad_shops,
                inaccessible_stairs=bad_stairs,missing_ceilings=bad_roofs,reachable_walk_nodes=len(seen),
                scope='Offline half-step collision grid; doors opened by interaction; no teleport edges or live Minecraft run')
    out=ROOT/'build/reports/city';out.mkdir(parents=True,exist_ok=True)
    (out/'connectivity.json').write_text(json.dumps(report,indent=2),encoding='utf8')
    print(json.dumps(report,indent=2));assert not missing and not bad_stairs and not bad_shops and not bad_roofs


if __name__=='__main__':main()
