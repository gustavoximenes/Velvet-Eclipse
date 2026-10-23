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
school_cells={tuple(c[:3]):c for c in plan['cells']}
for door in read(DATA/'furniture.json.gz')['doors']:
 if door['id'].startswith('school_'):
  for c in door['cells']:school_cells[tuple(c[:3])]=[*c[:3],40,c[4]]
for c in school_cells.values():
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
views=[dict(id='gekkoukan-fachada',eye=[34,77,-28],target=[0,76,-115],fov=62),dict(id='gekkoukan-campus',eye=[119,161,-8],target=[12,72,-132],fov=66),dict(id='gekkoukan-hall',eye=[0,66.4,-111.5],target=[0,70,-128],fov=82),dict(id='gekkoukan-biblioteca',eye=[40.6,66.7,-117.1],target=[34.2,65.8,-126.5],fov=84),dict(id='gekkoukan-2f',eye=[-29.5,72.3,-117.3],target=[-36,71.8,-128],fov=82),dict(id='gekkoukan-patio',eye=[-16,66.5,-139],target=[14,70,-167],fov=80),dict(id='gekkoukan-escada',eye=[2,66.2,-114],target=[-7.6,70,-129],fov=83),dict(id='gekkoukan-cobertura-patio',eye=[2,104,-138],target=[0,71,-169],fov=86)]
(OUT/'school.json').write_text(json.dumps(dict(vertexCount=len(v),key=[-80,180,-10],center=[0,72,-132],lights=[],views=views,shots=[])))
code=(HERE.parent/'tartarus_rework/RenderNative.java').read_text(encoding='utf8').replace('RenderNative','RenderGekkoukan').replace('List.of("floor-0","floor-7","floor-8","floor-10","floor-20","floor-25")','List.of("school","dorm_open","dorm_closed")').replace('128,128,10,','128,128,'+str(len(images))+',').replace('i<10','i<'+str(len(images))).replace('Path.of(args[1],"textures/block/tartarus_native_"+i+".png")','base.resolve("material-"+i+".png")').replace('.put((byte)255);','.put((byte)(argb>>24));').replace('GL_LINEAR','GL_NEAREST').replace('.12f,150)', '.12f,600)').replace('glClearColor(.022f,.033f,.056f,1)','glClearColor(.57f,.68f,.74f,1)').replace('ortho(-45,45,-45,45,.2f,120)','ortho(-160,160,-160,160,.2f,600)')
code=code.replace('if(mat>=7){emission=.6;}','').replace('if(mat==6){emission=1.5;}','')
code=code.replace('glTexParameteri(GL_TEXTURE_2D_ARRAY,GL_TEXTURE_MIN_FILTER,GL_NEAREST);','glGenerateMipmap(GL_TEXTURE_2D_ARRAY);glTexParameteri(GL_TEXTURE_2D_ARRAY,GL_TEXTURE_MIN_FILTER,GL_NEAREST_MIPMAP_LINEAR);')
code=code.replace('void main(){\n  vec3 normal=', 'void main(){\n  if(texture(Materials,vec3(uv,surface.x)).a<.5)discard;\n  vec3 normal=')
(OUT/'RenderGekkoukan.java').write_text(code,encoding='utf8')

# Render both functional variants in the same camera, including the roof exit.
for mode,slot in [('open',4),('closed',5)]:
 tiles={tuple(c[:3]):c[4] for c in plan['cells'] if c[3]==40 and -130<=c[0]<=-70 and 44<=c[2]<=94}
 for door in read(DATA/'furniture.json.gz')['doors']:
  if door['id'].startswith('school_'):continue
  for c in door['cells']:tiles[tuple(c[:3])]=c[slot]
 chunks=[]
 for point,i in tiles.items():
  if not(-130<=point[0]<=-70):continue
  d=defs[i];q=raw[d['offset']//13:(d['offset']+d['length'])//13].copy();q[:,:3]+=point;chunks.append(q)
 v=np.concatenate(chunks);v.astype('<f4').tofile(OUT/f'dorm_{mode}-render.bin')
 views=[dict(id=f'dorm-quarto-{mode}',eye=[-102,82,66],target=[-105.9,81.5,68.4],fov=76),dict(id=f'dorm-interior-{mode}',eye=[-97.7,72.0,72.4],target=[-105.5,71.6,73.2],fov=78),dict(id=f'dorm-parede-{mode}',eye=[-106.3,71.8,74.8],target=[-102.5,71.9,68.5],fov=77),dict(id=f'dorm-estudo-{mode}',eye=[-103.2,71.8,70.7],target=[-100,71.7,76.6],fov=77),dict(id=f'dorm-telhado-{mode}',eye=[-112.2,86.7,66.5],target=[-111.2,86.4,69.3],fov=73),dict(id=f'dorm-entrada-{mode}',eye=[-100,66.8,80.5],target=[-100,66.8,77],fov=74)]
 (OUT/f'dorm_{mode}.json').write_text(json.dumps(dict(vertexCount=len(v),key=[-80,180,120],center=[-100,76,66],lights=[],views=views,shots=[])))

print('Native preview:',len(v)//3,'triangles;',len(images),'materials;',OUT)

# A close view of an actual school leaf, not an empty doorway in the base plan.
for mode,slot in [('open',4),('closed',5)]:
 tiles={tuple(c[:3]):c[4] for c in plan['cells'] if c[3]==40 and -51<=c[0]<=90 and -190<=c[2]<=-35 and c[1]>=63}
 for door in read(DATA/'furniture.json.gz')['doors']:
  if door['id']=='school_enfermaria':
   for c in door['cells']:tiles[tuple(c[:3])]=c[slot]
 chunks=[]
 for pos,i in tiles.items():
  if i<0:continue
  d=defs[i];q=raw[d['offset']//13:(d['offset']+d['length'])//13].copy();q[:,:3]+=pos;chunks.append(q)
 v=np.concatenate(chunks);v.astype('<f4').tofile(OUT/f'school_door_{mode}-render.bin')
 (OUT/f'school_door_{mode}.json').write_text(json.dumps(dict(vertexCount=len(v),key=[-80,180,-10],center=[19,66,-120],lights=[],views=[dict(id=f'gekkoukan-porta-{mode}',eye=[19.8,65.8,-113.5],target=[22.5,65.7,-116.1],fov=66)],shots=[])))
p=OUT/'RenderGekkoukan.java';p.write_text(p.read_text(encoding='utf8').replace('List.of("school","dorm_open","dorm_closed")','List.of("school","dorm_open","dorm_closed","school_door_open","school_door_closed")'),encoding='utf8')

# Review the actual alpha pass instead of representing glass as solid blue panels.
from preview_glass import apply
p.write_text(apply(p.read_text(encoding='utf8')),encoding='utf8')
meta=json.loads((OUT/'school.json').read_text())
meta['views'] += [dict(id='gekkoukan-janela-corredor',eye=[-39,66,-114.5],target=[-24,66.7,-110.1],fov=76),dict(id='gekkoukan-juncao-hall',eye=[-19,77,-92],target=[-8,75,-113],fov=66),dict(id='gekkoukan-pilares-entrada',eye=[-3,66,-102],target=[-7,68,-110],fov=76)]
(OUT/'school.json').write_text(json.dumps(meta))
