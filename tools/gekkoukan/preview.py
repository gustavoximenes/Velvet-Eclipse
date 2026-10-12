"""Export a view of installed resource geometry for the hidden offline renderer."""
from pathlib import Path
import gzip,json,importlib.util,io,zipfile
import numpy as np
from PIL import Image
HERE=Path(__file__).parent;ROOT=HERE.parents[1];AS=ROOT/'src/main/resources/assets/velvet_eclipse';DATA=ROOT/'src/main/resources/data/velvet_eclipse/city';OUT=ROOT/'build/reports/gekkoukan/preview';OUT.mkdir(parents=True,exist_ok=True)
def read(p):return json.loads(gzip.decompress(p.read_bytes()))
plan=read(DATA/'plan.json.gz');defs=read(DATA/'architecture.json.gz');raw=np.frombuffer(gzip.decompress((AS/'city/architecture.bin.gz').read_bytes()),'>f4').reshape(-1,13)
spec=importlib.util.spec_from_file_location('native_blocks',HERE.parent/'approved_models/preview_city_blocks.py');m=importlib.util.module_from_spec(spec);spec.loader.exec_module(m)
model=json.loads((AS/'models/block/city_architecture.json').read_text());images=[]
for i in range(36):
 tex=model['textures'][f'm{i}'].split(':')[1];images.append(Image.open(AS/(tex.replace('block/','textures/block/')+'.png')).convert('RGBA').resize((128,128),Image.Resampling.NEAREST))
chunks=[]
for c in plan['cells']:
 x,y,z,mat=c[:4]
 if not(-51<=x<=90 and -190<=z<=-35 and y>=63):continue
 if mat==40:
  d=defs[c[4]];v=raw[d['offset']//13:(d['offset']+d['length'])//13].copy();v[:,:3]+=[x,y,z];chunks.append(v)
 elif mat>=100:
  v=[]
  for points,normal,uv,tile,tint,emission,cull in m.blocks(mat):
   # Cherry leaves inherit the leaf JSON tint index but have no biome tint provider.
   if plan['palette'][mat-100]['block']=='minecraft:cherry_leaves':tint=np.ones(3)
   for k in [0,1,2,0,2,3]:v.append([*(points[k]+[x,y,z]),*normal,*tint,36+tile,emission,*uv[k]])
  chunks.append(np.array(v,dtype='<f4').reshape(-1,13))
images.extend(im.resize((128,128),Image.Resampling.NEAREST) for im in m.IMAGES)
for i,im in enumerate(images):im.save(OUT/f'material-{i}.png')
v=np.concatenate(chunks);v.astype('<f4').tofile(OUT/'school-render.bin')
views=[dict(id='gekkoukan-fachada',eye=[34,77,-28],target=[0,76,-115],fov=62),dict(id='gekkoukan-campus',eye=[119,161,-8],target=[12,72,-132],fov=66),dict(id='gekkoukan-hall',eye=[0,66.4,-111.5],target=[0,70,-128],fov=82),dict(id='gekkoukan-biblioteca',eye=[40.6,66.7,-117.1],target=[34.2,65.8,-126.5],fov=84),dict(id='gekkoukan-2f',eye=[-29.5,72.3,-117.3],target=[-36,71.8,-128],fov=82),dict(id='gekkoukan-patio',eye=[-16,66.5,-139],target=[14,70,-167],fov=80)]
(OUT/'school.json').write_text(json.dumps(dict(vertexCount=len(v),key=[-80,180,-10],center=[0,72,-132],lights=[],views=views,shots=[])))
code=(HERE.parent/'tartarus_rework/RenderNative.java').read_text(encoding='utf8').replace('RenderNative','RenderGekkoukan').replace('List.of("floor-0","floor-7","floor-8","floor-10","floor-20","floor-25")','List.of("school")').replace('128,128,10,','128,128,'+str(len(images))+',').replace('i<10','i<'+str(len(images))).replace('Path.of(args[1],"textures/block/tartarus_native_"+i+".png")','base.resolve("material-"+i+".png")').replace('.put((byte)255);','.put((byte)(argb>>24));').replace('GL_LINEAR','GL_NEAREST').replace('.12f,150)', '.12f,600)').replace('glClearColor(.022f,.033f,.056f,1)','glClearColor(.57f,.68f,.74f,1)').replace('ortho(-45,45,-45,45,.2f,120)','ortho(-160,160,-160,160,.2f,600)')
code=code.replace('if(mat>=7){emission=.6;}','').replace('if(mat==6){emission=1.5;}','')
code=code.replace('glTexParameteri(GL_TEXTURE_2D_ARRAY,GL_TEXTURE_MIN_FILTER,GL_NEAREST);','glGenerateMipmap(GL_TEXTURE_2D_ARRAY);glTexParameteri(GL_TEXTURE_2D_ARRAY,GL_TEXTURE_MIN_FILTER,GL_NEAREST_MIPMAP_LINEAR);')
code=code.replace('void main(){\n  vec3 normal=', 'void main(){\n  if(texture(Materials,vec3(uv,surface.x)).a<.5)discard;\n  vec3 normal=')
(OUT/'RenderGekkoukan.java').write_text(code,encoding='utf8')
print('Native preview:',len(v)//3,'triangles;',len(images),'materials;',OUT)
