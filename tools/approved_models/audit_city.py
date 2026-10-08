from pathlib import Path
import gzip,json,collections,math
ROOT=Path(__file__).resolve().parents[2];RES=ROOT/'src/main/resources';plan=json.loads(gzip.decompress((RES/'data/velvet_eclipse/city/plan.json.gz').read_bytes()));cells={tuple(c[:3]):c for c in plan['cells']};places={p['id']:p for p in plan['places']}
# Foot-height occupancy audit uses the same 1/16 surface height as CitySurfaceBlock.
def top(c):return c[1]+(c[5]/16 if len(c)>5 else 1)
def overlaps(x,z,y,h=1.8):
 for by in range(math.floor(y),math.ceil(y+h)):
  c=cells.get((x,by,z))
  if c and top(c)>y+.001:return True
 return False
floors=collections.defaultdict(list)
for c in plan['cells']:
 y=top(c)
 if 62<=y<=68 and not overlaps(c[0],c[2],y):floors[c[0],c[2]].append(y)
def nearby(x,z):return [(x+dx,z+dz) for dx in range(-2,3) for dz in range(-2,3) if (x+dx,z+dz) in floors]
spawn=places['bedroom'];start=(spawn['x'],spawn['z'],min(floors[spawn['x'],spawn['z']]))
todo=collections.deque([start]);visited={start};door_edges=collections.defaultdict(list)
for d in plan['doors']:
 if d['target'] not in places:continue
 p=places[d['target']]
 for x,z in nearby(d['x'],d['z']):door_edges[x,z].append((p['x'],p['z'],min(floors[p['x'],p['z']])))
while todo:
 x,z,y=todo.popleft();nodes=list(door_edges[x,z])
 for dx,dz in [(1,0),(-1,0),(0,1),(0,-1)]:
  for yy in floors[x+dx,z+dz]:
   if abs(y-yy)<=1.001:nodes.append((x+dx,z+dz,yy))
 for node in nodes:
  if node not in visited:visited.add(node);todo.append(node)
unreachable=[p['id'] for p in plan['places'] if not any((p['x'],p['z'],y) in visited for y in floors[p['x'],p['z']])]
bad_arrivals=[p['id'] for p in plan['places'] if not floors[p['x'],p['z']]]
shops=[c for c in plan['cells'] if 20<=c[3]<=28]
unreachable_shops=[c[3] for c in shops if not any(any((x,z,y) in visited for y in floors[x,z]) for x,z in nearby(c[0],c[2]))]
out=ROOT/'build/reports/city';out.mkdir(parents=True,exist_ok=True)
report={'cells':len(cells),'sections':len(plan['meshes']),'destinations':len(places),'unreachable_destinations':unreachable,'unsafe_arrivals':bad_arrivals,'unreachable_shops':unreachable_shops,'reachable_walk_nodes':len(visited),'scope':'Offline grid/support connectivity, not live Minecraft collision or visual validation'}
(out/'connectivity.json').write_text(json.dumps(report,indent=2));print(json.dumps(report,indent=2));assert not unreachable and not bad_arrivals and not unreachable_shops
