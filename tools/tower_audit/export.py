"""Bake approved tower triangles into ordinary, local chunk geometry and collisions.

Run build.py OUTPUT then this script OUTPUT, after city_polish/furniture.py.
All installed city/furniture IDs and bytes are retained; reruns replace only this tower.
"""
from pathlib import Path
import gzip,json,sys,hashlib,importlib.util,math
import numpy as np
from PIL import Image
HERE=Path(__file__).resolve().parent;ROOT=HERE.parents[1];RES=ROOT/'src/main/resources'
spec=importlib.util.spec_from_file_location('city_export',ROOT/'tools/city_polish/export.py');e=importlib.util.module_from_spec(spec);spec.loader.exec_module(e)
OUT=Path(sys.argv[1]).resolve();DATA=RES/'data/velvet_eclipse/city';AS=RES/'assets/velvet_eclipse'
def read(p):return json.loads(gzip.decompress(p.read_bytes()))
definitions=read(DATA/'architecture.json.gz');furniture=read(DATA/'furniture.json.gz')
base=max(c[i] for d in furniture['doors'] for c in d['cells'] for i in (4,5))+1
definitions=definitions[:base];end=definitions[-1]['offset']+definitions[-1]['length']
original=gzip.decompress((AS/'city/architecture.bin.gz').read_bytes())[:end*4]
raw=np.fromfile(OUT/'tower.bin',dtype='<f4').reshape(-1,3,11).astype(float)
green=(raw[:,:,7]>raw[:,:,6]*2)&(raw[:,:,7]>raw[:,:,8]*1.8)
raw[:,:,10]=np.where(green,np.maximum(raw[:,:,10],.30),raw[:,:,10])
meta=json.loads((OUT/'tower.json').read_text(encoding='utf8'))
_,verts,boxes,lights=e.bake_geometry('tower',meta,raw,surface_only=True)

def compact(rows):
    if not rows:return []
    if any(max(abs(np.array(b)-[0,0,0,1,1,1]))<1e-5 for b in rows):return [[0,0,0,1,1,1]]
    rows=list(set(tuple(b) for b in rows))
    # Merge adjoining strips without enlarging rotated surfaces into full cubes.
    for axis in (0,2,1):
        rest=[k for k in range(6) if k not in (axis,axis+3)];groups={}
        for b in rows:groups.setdefault(tuple(b[k] for k in rest),[]).append(b)
        rows=[]
        for bs in groups.values():
            bs.sort(key=lambda b:b[axis]);current=list(bs[0])
            for b in bs[1:]:
                if b[axis]<=current[axis+3]+1e-5:current[axis+3]=max(current[axis+3],b[axis+3])
                else:rows.append(current);current=list(b)
            rows.append(current)
    return rows

chunks=[];cursor=end;cells=[];dedup={}
# Solid creative sample uses the tower masonry material, not a hidden/empty tile.
sample=e.cuboid([0,0,0],[1,1,1],2,(.23,.32,.27)).reshape(-1,11)
sample=np.column_stack((sample,np.array([[v[2] if abs(v[3])>.5 else v[0],v[2] if abs(v[4])>.5 else v[1]] for v in sample])))
sample[:,9]=28;sample=sample.astype('>f4').flatten();sample_id=base
definitions.append(dict(offset=cursor,length=len(sample),boxes=[[0,0,0,1,1,1]],light=0));chunks.append(sample.tobytes());cursor+=len(sample)
for key in sorted(verts,key=lambda p:(p[1],p[0],p[2])):
    geometry=np.array(verts[key],dtype='<f4').reshape(-1,13)
    # Keep authored color/material/emission; only the texture slots change.
    geometry[:,9]=np.where(geometry[:,10]>=.2,30,np.where(geometry[:,9]==2,28,np.where(geometry[:,9]==3,29,0)))
    collision=compact(boxes.get(key,[]));light=lights.get(key,0);buf=geometry.astype('>f4').tobytes()
    digest=hashlib.sha256(buf+json.dumps([collision,light]).encode()).hexdigest()
    if digest not in dedup:
        dedup[digest]=len(definitions);definitions.append(dict(offset=cursor,length=geometry.size,boxes=collision,light=light));chunks.append(buf);cursor+=geometry.size
    cells.append([*key,dedup[digest]+1000])
# The server portal lives just ahead of the authored opaque portal surface.
# No collision or model fragments occupy these cells; the complete arch remains intact.
occupied={tuple(c[:3]) for c in cells}
for x in range(-3,3):
    for y in range(1,8):
        p=(x,y,18)
        assert p not in occupied,('portal intersects architecture',p)
        cells.append([*p,4])
cells.sort(key=lambda c:(c[1],c[0],c[2]))
e.dump(DATA/'architecture.json.gz',definitions)
(AS/'city/architecture.bin.gz').write_bytes(gzip.compress(original+b''.join(chunks),mtime=0))
e.dump(RES/'data/velvet_eclipse/tartarus/exterior.json.gz',dict(revision=1,height=238,sample=sample_id,cells=cells,sourceHash=hashlib.sha256((OUT/'tower.bin').read_bytes()).hexdigest()))
# Texture data equivalent to the study's masonry, with LabPBR emission for green surfaces.
tex=AS/'textures/block';tex.mkdir(parents=True,exist_ok=True)
for kind in ['stone','trim','glow']:
    pixels=[];specular=[]
    for y in range(128):
        for x in range(128):
            u=(x+.5)/128*8;v=(y+.5)/128*8
            seam=((u+(math.floor(v*1.5)%2)*.5)*.65)%1>.974 or (v*1.5)%1>.963
            value=225 if kind=='stone' and seam else 255
            pixels.append((value,value,value,255));specular.append((140 if kind=='trim' else 40,32,0,220 if kind=='glow' else 0))
    for suffix,data in [('',pixels),('_s',specular)]:
        img=Image.new('RGBA',(128,128));img.putdata(data);img.save(tex/f'tower_{kind}{suffix}.png')
model=AS/'models/block/city_architecture.json';obj=json.loads(model.read_text(encoding='utf8'))
for i,kind in enumerate(['stone','trim','glow'],28):obj['textures']['m'+str(i)]='velvet_eclipse:block/tower_'+kind
e.dump(model,obj)
e.dump(AS/'models/block/tartarus_exterior.json',obj)
e.dump(AS/'blockstates/tartarus_exterior.json',{'variants':{'':{'model':'velvet_eclipse:block/tartarus_exterior'}}})
# Inventory is a readable masonry cube; placed samples use the exact local geometry.
e.dump(AS/'models/item/tartarus_exterior.json',{'parent':'minecraft:block/cube_all','textures':{'all':'velvet_eclipse:block/tower_stone'}})
report=dict(sourceTriangles=len(raw),nativeTriangles=(cursor-end-len(sample))//39,blocks=len(cells),uniqueTiles=len(definitions)-base,baseTiles=base,preservedBytes=len(original),height=238,portalBlocks=42,litBlocks=sum(1 for c in cells if c[3]>=1000 and definitions[c[3]-1000]['light']>0),sourceHash=hashlib.sha256((OUT/'tower.bin').read_bytes()).hexdigest())
e.dump(HERE/'export-report.json',report);print(json.dumps(report),flush=True)
