"""Revision 7 export. Append IDs so night journals and old worlds remain readable."""
from pathlib import Path
import sys,gzip,json,hashlib,math,pickle,importlib.util
import numpy as np
from PIL import Image,ImageDraw
from campus import build,transform
HERE=Path(__file__).parent;ROOT=HERE.parents[1];DATA=ROOT/'src/main/resources/data/velvet_eclipse/city';AS=ROOT/'src/main/resources/assets/velvet_eclipse'
spec=importlib.util.spec_from_file_location('city_export',HERE.parent/'city_polish/export.py');e=importlib.util.module_from_spec(spec);spec.loader.exec_module(e)
def read(p):return json.loads(gzip.decompress(p.read_bytes()))
def dump(p,v):e.dump(p,v)
CACHE=ROOT/'build/gekkoukan';CACHE.mkdir(parents=True,exist_ok=True)

def fix_authored_dorm():
 base=e.HERE/'approved'
 for ext in ['json.gz','bin.gz']:
  old=base/f'dorm-v3.{ext}'
  if not old.exists():old.write_bytes((base/f'dorm.{ext}').read_bytes())
 meta=read(base/'dorm-v3.json.gz');raw=np.frombuffer(gzip.decompress((base/'dorm-v3.bin.gz').read_bytes()),'<f4').reshape(-1,3,11).astype(float)
 def replace(row,lo,hi):
  nonlocal raw
  oldlo=np.array(row[:3]);oldhi=np.array(row[3:]);v=raw[:,:,:3]
  candidates=np.all((abs(v-oldlo)<2e-5)|(abs(v-oldhi)<2e-5),axis=(1,2))
  inds=np.flatnonzero(candidates);assert len(inds)==12,(row,len(inds))
  mat=raw[inds[0],0,9];color=raw[inds[0],0,6:9];raw=np.concatenate([raw[~candidates],e.cuboid(lo,hi,mat,color)])
  meta['solids'].remove(row);meta['solids'].append(lo+hi)
 # The old surrounds occupied the same exposed planes as the plaster behind
 # them (inner jamb and lintel underside). Recess the wall, rather than hiding
 # the resulting z-fighting with a shader offset.
 for level,z,doorx in [(lev,z,x) for lev in [1,2,3] for z in [2,-2.25] for x in [-6.75,6.4]]+[(0,-3,1.2)]:
  y=level*5
  for row in list(meta['solids']):
   if len(row)!=6:continue
   lo=np.array(row[:3]);hi=np.array(row[3:]);a=lo.copy();b=hi.copy()
   if abs(lo[1]-y)<1e-5 and (abs(lo[2]-z)<1e-5 or abs(lo[2]-z-.14)<1e-5):
    if abs(hi[0]-doorx)<1e-5 and hi[0]-lo[0]>.2:b[0]-=.09
    elif abs(lo[0]-doorx-1.8)<1e-5 and hi[0]-lo[0]>.2:a[0]+=.09
   elif abs(lo[0]-doorx)<1e-5 and abs(lo[1]-y-2.8)<1e-5 and abs(lo[2]-z)<1e-5:
    a[0]-=.09;b[0]+=.09;a[1]+=.10
   if np.any(a!=lo) or np.any(b!=hi):replace(row,a.tolist(),b.tolist())
 roof_frame=[
  ([-15.3,20,2.75],[-15.3,20,2.75],[-12.26,23.05,3]),
  ([-10.25,20,2.75],[-10.20,20,2.75],[-9.7,23.05,3]),
  ([-12.2,22.5,2.75],[-12.26,22.58,2.75],[-10.20,23.05,3]),
  ([-12.26,20,2.69],[-12.26,20,2.69],[-12.19,22.58,3.05]),
  ([-10.27,20,2.69],[-10.27,20,2.69],[-10.20,22.58,3.05]),
  ([-12.2,22.5,2.68],[-12.19,22.5,2.68],[-10.27,22.58,3.05])]
 for old,lo,hi in roof_frame:
  row=next(r for r in meta['solids'] if len(r)==6 and np.max(abs(np.array(r[:3])-old))<1e-5)
  replace(row,lo,hi)
 for row in list(meta['solids']):
  if len(row)!=6:continue
  lo=np.array(row[:3]);hi=np.array(row[3:]);size=hi-lo
  if np.max(abs(size-[2,3.5,.9]))<1e-5:
   # Compact wardrobe kept against the room wall, away from its entry.
   replace(row,(lo+[.15,0,.12]).tolist(),(lo+[1.65,2.5,.77]).tolist())
  elif np.max(abs(lo-[-10.28,20.05,1.05]))<1e-5:
   replace(row,[-10.40,20.01,.85],[-10.30,22.49,2.65])
 # Roof handle follows the leaf inward, clear of the masonry jamb.
 for row in list(meta['solids']):
  if len(row)==6 and np.max(abs(np.array(row[:3])-[-10.35,21.17,1.20]))<1e-5:
   replace(row,[-10.47,21.17,1.20],[-10.39,21.42,1.32])
 # Handles follow the smaller wardrobe, rather than floating in front of it.
 for row in list(meta['solids']):
  if len(row)!=6:continue
  lo=np.array(row[:3]);hi=np.array(row[3:])
  if np.max(abs(hi-lo-[.08,.5,.10]))<1e-5 and any(abs(lo[1]-(lev*5+1.4))<1e-5 for lev in [1,2,3]):
   replace(row,(lo+[0,-.35,-.13]).tolist(),(hi+[0,-.35,-.13]).tolist())
 from dorm_fixtures import repair
 raw,report=repair(meta,raw,e.cuboid)
 dump(HERE/'dorm-fixtures.json',report)
 dump(base/'dorm.json.gz',meta);(base/'dorm.bin.gz').write_bytes(gzip.compress(raw.astype('<f4').tobytes(),mtime=0))

def main():
 fix_authored_dorm()
 plan=read(DATA/'legacy-plan-v3.json.gz');defs=read(DATA/'architecture.json.gz')
 raw=np.frombuffer(gzip.decompress((AS/'city/architecture.bin.gz').read_bytes()),'>f4').astype('<f4')
 state=HERE/'baseline.json'
 if state.exists():
  start=json.loads(state.read_text());defs=defs[:start['tiles']];raw=raw[:start['floats']]
 else:
  start=dict(tiles=len(defs),floats=len(raw));state.write_text(json.dumps(start))
 chunks=[raw];cursor=len(raw);dedup={}
 def append(geometry,boxes,light=0):
  nonlocal cursor
  geometry=np.asarray(geometry,dtype='<f4').flatten()
  if len(geometry):
   # Exact duplicate triangles in an authored tile create depth fighting.
   triangles=geometry.reshape(-1,39);keys=np.round(triangles.reshape(-1,3,13)[:,:,:6],5).astype('<f4').reshape(-1,18)
   _,idx=np.unique(keys,axis=0,return_index=True);geometry=triangles[np.sort(idx)].flatten()
  boxes=[list(b) for b in dict.fromkeys(tuple(b) for b in boxes)]
  digest=hashlib.sha256(geometry.tobytes()+json.dumps([boxes,light]).encode()).digest()
  if digest in dedup:return dedup[digest]
  ident=len(defs);defs.append(dict(offset=cursor,length=len(geometry),boxes=boxes,light=light));chunks.append(geometry);cursor+=len(geometry);dedup[digest]=ident;return ident
 cells={tuple(c[:3]):c for c in plan['cells']}
 # Clear only cells authored in the previous school, including the obsolete remote rooms.
 for p,c in list(cells.items()):
  if -52<=p[0]<=52 and -138<=p[2]<=-31 and (p[1]>=64 or p[2]<=-64 and p[1]>=63) and c[3]!=29:del cells[p]
 # Rebuild the dorm with corrected cabinets and roof leaf; stable old IDs stay in the buffer.
 for p,c in list(cells.items()):
  if -130<=p[0]<=-70 and 44<=p[2]<=94 and p[1]>=64 and c[3]==40:del cells[p]
 def bake_cached(name,key,fn):
  file=CACHE/(name+'-'+hashlib.sha256(key).hexdigest()[:14]+'.pickle')
  if file.exists():return pickle.loads(file.read_bytes())
  result=fn();file.write_bytes(pickle.dumps(result,protocol=5));return result
 def install(result,offset):
  meta,verts,boxes,lights=result
  for key in sorted(set(verts)|set(boxes)):
   geometry=verts.get(key,[]);collision=boxes.get(key,[])
   if not geometry and not collision:continue
   ident=append(geometry,collision,lights.get(key,0));p=tuple(key[i]+offset[i] for i in range(3))
   if cells.get(p,[0,0,0,0])[3]!=29:cells[p]=[*p,40,ident]
 dormkey=(e.HERE/'approved/dorm.bin.gz').read_bytes()+(e.HERE/'export.py').read_bytes()
 install(bake_cached('dorm',dormkey,lambda:e.bake_scene('dorm',(-100,65,66))),(-100,65,66))
 # Only the artificially filled fountain cylinder had collision-only cells exposed to air.
 # Keep its actual visible tiles and remove the unsupported cells around the basin.
 removed=[]
 for p,c in list(cells.items()):
  if c[3]==40 and 89<=p[0]<=96 and 61<=p[2]<=68 and 64<=p[1]<=65 and defs[c[4]]['length']==0:
   del cells[p];removed.append(p)
 school,rooms,seats,doors=build()
 v=np.asarray(school.verts,dtype='<f4').reshape(-1,3,11)
 meta=dict(solids=[b['lo']+b['hi'] for b in school.solids])
 key=v.tobytes()+json.dumps(meta).encode()+(e.HERE/'export.py').read_bytes()
 install(bake_cached('school',key,lambda:e.bake_geometry('school',meta,v.copy())),(0,64,-110))
 # The room metadata accompanies the actual production geometry for navigation and audits.
 for r in rooms:
  r['entry']=(np.asarray(r['entry'])+[0,64,-110]).tolist()
 dump(DATA/'school-rooms.json.gz',rooms)
 tree_data=json.loads((HERE/'approved/trees.json').read_text())['blocks']
 palette=plan['palette']
 for tree in tree_data:
  block=tree['block'];props=tree.get('properties',{})
  entry=dict(block=block,properties=props)
  if entry not in palette:palette.append(entry)
  x,y,z=tree['pos'];p=(x,y+64,z-110);cells[p]=[*p,100+palette.index(entry)]
 places=[p for p in plan['places'] if not p['id'].startswith('school_')]
 aliases={'classroom_2f':'classroom','professores':'faculty','biblioteca':'library','conselho':'council'}
 for r in rooms:
  p=r['entry'];places.append(dict(id='school_'+aliases.get(r['id'],r['id']),name='Gekkoukan · '+r['id'].replace('_',' '),x=math.floor(p[0]),y=round(p[1]),z=math.floor(p[2])))
 places.append(dict(id='school_hallway',name='Gekkoukan · Hall',x=0,y=64,z=-119))
 plan.update(revision=7,cells=sorted(cells.values(),key=lambda c:(c[1],c[0],c[2])),places=places,palette=palette)
 plan['buildings']=[b for b in plan['buildings'] if b['id']!='school']+[dict(id='school',minX=-44,maxX=44,minZ=-187,maxZ=-110,floors=[64,70,76,82]),dict(id='school_gym',minX=59,maxX=87,minZ=-181,maxZ=-145,floors=[64])]
 plan['routes']=[r for r in plan.get('routes',[]) if 'school' not in r['name'].lower() and 'gekkoukan' not in r['name'].lower()]
 dump(DATA/'plan.json.gz',plan);dump(DATA/'architecture.json.gz',defs)
 (AS/'city/architecture.bin.gz').write_bytes(gzip.compress(np.concatenate(chunks).astype('>f4').tobytes(),mtime=0))
 dump(HERE/'assembly.json',dict(rooms=rooms,seats=seats,doors=doors,removedInvisibleCells=removed,trees=len(tree_data)))
 print('revision 7:',len(cells),'cells;',len(defs),'tiles;',len(rooms),'connected rooms; removed',len(removed),'invisible cells',flush=True)

if __name__=='__main__':main()
