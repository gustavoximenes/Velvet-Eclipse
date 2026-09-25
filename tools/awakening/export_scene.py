"""Deterministic, portable bone-track exporter; not a prerecorded video.
Binary layout is read by AwakeningScene.java. The current skin is bound in game.
"""
from pathlib import Path
import gzip, struct, math, json
import numpy as np
import choreography as c
OUT=c.ASSET/'awakening';OUT.mkdir(parents=True,exist_ok=True)
FPS=20
meshes={};current={};variant=0;skin_slot=0
def mesh(key,texture,vertices,matrix,kind=0):
 if key not in meshes:meshes[key]=(texture,np.asarray(vertices,dtype='>f4'),kind)
 current[key]=np.asarray(matrix,dtype='>f4')
 return []
def faces(self,t=0,clip='misc.idle',transform=None):
 root=np.eye(4) if transform is None else transform;mats={}
 for b in self.bones:
  n=b['name'];p=np.array(b['pivot'])
  m=mats.get(b.get('parent'),root)@c.T(self.sample(clip,n,'position',t))@c.T(p)@c.R(np.array(b.get('rotation',[0,0,0]))+self.sample(clip,n,'rotation',t))@c.S(self.sample(clip,n,'scale',t))@c.T(-p);mats[n]=m
  if not self.cubes[n]:continue
  vertices=[]
  for vs,uvs in self.cubes[n]:
   for ids,uv in uvs:vertices.extend(np.c_[vs[ids,:3],uv/np.array([self.tex.shape[1],self.tex.shape[0]])])
  mesh(f'{self.name}/{n}/{variant}',self.name,vertices,m,variant)
 return []
c.Model.faces=faces
def skin_cube(center,pixels,uv,mat,inflate=0,row_start=0,row_end=None):
 global skin_slot
 w,h,d=pixels
 if row_end is None:row_end=h
 size=np.array([w,row_end-row_start,d])/16+inflate*2
 v=np.array(center)+(c.CORNERS-.5)*size;vertices=[]
 for face,ids in c.FACES.items():
  u0,v0,uw,vh=c.uv_faces(*uv,w,h,d)[face]
  if face not in ('up','down'):v0+=row_start;vh=row_end-row_start
  vertices.extend(np.c_[v[ids],c.uv_quad(face,u0,v0,uw,vh)/64])
 key=f'player/{variant}/{skin_slot}';skin_slot+=1
 return mesh(key,'player',vertices,mat,variant)
c.skin_cube=skin_cube
original_player=c.player
def player(*a,**kw):
 global variant,skin_slot
 for slim in [False,True]:
  variant=2 if slim else 1;skin_slot=0
  c.player_skin=lambda:(None,slim)
  original_player(*a,**kw)
 variant=0
 return []
c.player=player
def i(f,n):f.write(struct.pack('>i',n))
def floats(f,v):f.write(np.asarray(v,dtype='>f4').tobytes())
def export(name,begin,end,label):
 global current,meshes
 meshes={};frames=[]
 count=math.ceil((end-begin)*FPS)+1
 for index in range(count):
  t=min(end,begin+index/FPS);current={};cam,_=c.scene(t,name)
  camera=[*cam.eye,*cam.axes.flatten(),cam.f,cam.roll]
  frames.append((camera,current))
 keys=list(meshes)
 path=OUT/(label+'.bin.gz')
 with path.open('wb') as raw,gzip.GzipFile(filename='',fileobj=raw,mode='wb',mtime=0,compresslevel=9) as f:
  i(f,0x56454157);i(f,1);i(f,len(keys))
  for k in keys:
   texture,verts,kind=meshes[k];b=texture.encode();i(f,len(b));f.write(b);i(f,kind);i(f,len(verts));floats(f,verts)
   # Skin overlay slot, -1 for other meshes. Respects Minecraft skin options.
   i(f,int(k.rsplit('/',1)[1]) if k.startswith('player/') else -1)
  floats(f,[begin,FPS]);i(f,count)
  for camera,poses in frames:
   floats(f,camera)
   for k in keys:
    matrix=poses.get(k);floats(f,np.zeros(16) if matrix is None else matrix.flatten())
 print(label,'meshes',len(keys),'frames',count,'compressed bytes',path.stat().st_size,flush=True)
 return {'file':path.name,'meshes':len(keys),'frames':count,'seconds':end-begin,'bytes':path.stat().st_size}
if __name__=='__main__':
 result=[export('orpheus',0,16,'intro')]
 for name in ['orpheus','izanagi','arsene']:result.append(export(name,23,c.duration(name),name))
 (OUT/'manifest.json').write_text(json.dumps(result,indent=2),encoding='utf-8')
