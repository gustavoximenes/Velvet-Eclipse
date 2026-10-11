"""Reconstruct the delivered local fragments and compare their surface with approval geometry.

The emitted tower.bin can be viewed by RenderTower; it contains production geometry,
not the original uncut preview. Rendering remains offline, not a Minecraft screenshot.
"""
from pathlib import Path
import gzip,json,sys,shutil
import numpy as np
ROOT=Path(__file__).resolve().parents[2];RES=ROOT/'src/main/resources';source=Path(sys.argv[1]);out=Path(sys.argv[2]);out.mkdir(parents=True,exist_ok=True)
def read(p):return json.loads(gzip.decompress(p.read_bytes()))
plan=read(RES/'data/velvet_eclipse/tartarus/exterior.json.gz');defs=read(RES/'data/velvet_eclipse/city/architecture.json.gz')
binary=np.frombuffer(gzip.decompress((RES/'assets/velvet_eclipse/city/architecture.bin.gz').read_bytes()),dtype='>f4')
original=np.fromfile(source/'tower.bin',dtype='<f4').reshape(-1,3,11).astype(float)
def area(v):return np.linalg.norm(np.cross(v[:,1,:3]-v[:,0,:3],v[:,2,:3]-v[:,0,:3]),axis=1).sum()/2
source_area=area(original);native_area=0;lower=np.full(3,np.inf);upper=-lower;count=0
with (out/'tower.bin').open('wb') as f:
    for x,y,z,mat in plan['cells']:
        if mat<1000:continue
        d=defs[mat-1000];v=binary[d['offset']:d['offset']+d['length']].reshape(-1,3,13).astype(float)[:,:,:11].copy()
        if not v.size:continue
        v[:,:,:3]+=[x,y,z];native_area+=area(v);count+=len(v)
        lower=np.minimum(lower,v[:,:,:3].min((0,1)));upper=np.maximum(upper,v[:,:,:3].max((0,1)))
        v[:,:,9]=np.where(v[:,:,9]==28,2,np.where(v[:,:,9]==29,3,0))
        f.write(v.astype('<f4').tobytes())
error=abs(source_area-native_area)/source_area
assert error<1e-6,('Missing/duplicated triangle area',source_area,native_area,error)
assert np.max(abs(lower-original[:,:,:3].min((0,1))))<2e-5
assert np.max(abs(upper-original[:,:,:3].max((0,1))))<2e-5
meta=json.loads((source/'tower.json').read_text(encoding='utf8'));meta['vertexCount']=count*3;meta['status']='Geometria nativa exportada; verificação offline, sem Minecraft'
(out/'tower.json').write_text(json.dumps(meta,ensure_ascii=False),encoding='utf8');shutil.copy2(source/'atlas.png',out/'atlas.png')
report=dict(triangles=count,sourceArea=source_area,nativeArea=native_area,relativeSurfaceError=error,bounds=[lower.tolist(),upper.tolist()],inGame=False)
(out/'validation.json').write_text(json.dumps(report,indent=2),encoding='utf8');print(json.dumps(report),flush=True)
