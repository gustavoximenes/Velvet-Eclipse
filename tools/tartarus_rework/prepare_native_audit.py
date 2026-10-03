"""Inspect production baked chunks with real materials; lighting remains an offline approximation."""
from pathlib import Path
import json,gzip,math
import numpy as np
ROOT=Path(__file__).resolve().parents[2];WORK=ROOT/'build/reports/tartarus-rework';OUT=WORK/'native';ASSETS=ROOT/'src/main/resources/assets/velvet_eclipse'
catalog=json.loads((ASSETS/'tartarus/catalog.json').read_text())
def readmesh(name):return np.frombuffer(gzip.decompress((ASSETS/'tartarus'/(name+'.mesh.gz')).read_bytes()),dtype='<f4',offset=8).reshape(-1,11).copy()
def prop(name,x,y,z,yaw=0):
 a=readmesh(name);c=math.cos(yaw);s=math.sin(yaw);px=a[:,0].copy();pz=a[:,2].copy();nx=a[:,3].copy();nz=a[:,5].copy()
 a[:,0]=c*px+s*pz+x;a[:,1]+=y;a[:,2]=-s*px+c*pz+z;a[:,3]=c*nx+s*nz;a[:,5]=-s*nx+c*nz
 for i in range(0,len(a),3):
  if np.dot(np.cross(a[i+1,:3]-a[i,:3],a[i+2,:3]-a[i,:3]),a[i,3:6])<0:a[[i+1,i+2]]=a[[i+2,i+1]]
 uv=np.zeros((len(a),2));axis=np.argmax(abs(a[:,3:6]),axis=1)
 for i in range(3):
  sel=axis==i;uv[sel,0]=(a[sel,2 if i==0 else 0]/8)%1;uv[sel,1]=(a[sel,2 if i==1 else 1]/8)%1
 mat=a[:,9]>=7;a[mat,6:9]=1
 return np.concatenate([a,uv],axis=1).astype('<f4')
for floor in [0,7,8,10,20,25]:
 p=json.loads((WORK/f'floor-{floor}.json').read_text());raw=np.fromfile(OUT/f'floor-{floor}.bin',dtype='<f4').reshape(-1,13);models=[raw];lights=[]
 for piece in p['architecture']:
  for light in catalog[piece['model']]['lights']:lights.append([light[i]+piece[['x','y','z'][i]] for i in range(3)]+light[3:])
 for cell in p['cells']:
  if cell['material']=='velvet_door':models.append(prop('velvet_door_false',cell['x']+.375,cell['y'],cell['z']+.375,-math.pi/2))
 if floor in [10,20]:models.append(prop('kit_gate',0,0,237))
 if floor==0:models.append(prop('terminal',18.5,0,18.5))
 elif floor in [10,20]:models.append(prop('terminal',6.5,0,291.5))
 elif floor==25:
  models.append(prop('terminal',15.5,0,268.5));models.append(prop('monad',21.5,0,256.5,-math.pi/2))
  for x in [-6,0,6]:models.append(prop('treasure',x+.5,0,264.5))
 data=np.concatenate(models);data.tofile(OUT/f'floor-{floor}-render.bin')
 def view(id,eye,target,fov=75):return dict(id=id,eye=eye,target=target,fov=fov)
 if floor==0:views=[view('hall',[0,5,27],[0,15,-17],68),view('clock',[2.5,19.2,-9],[0,19.6,-21],66),view('hall-door',[-15,3,8],[-23,3,0],70),view('hall-return',[6,4,18],[0,8,30],75),view('clock-close',[.5,18.8,-17],[0,20,-22],82)]
 elif floor in [10,20]:views=[view(f'{floor}-arena',[-9 if floor==10 else 10,3.7,267],[0,5,251],82),view(f'{floor}-stair',[5.5,4.2,228],[0,7.5,219],78)]
 elif floor==25:views=[view('25-services',[10,4.5,276],[-6,4,265],85),view('25-stair',[5,4,251],[0,7,239],78)]
 else:
  views=[]
  back=next(v for v in p['architecture'] if v['model']=='kit_back');views.append(view('floor-return',[back['x']+3,3.2,back['z']+8],[back['x'],3.3,back['z']],80))
  for kind in (['platforms'] if floor==8 else ['long','pillars','stairs']):
   hit=next((v for v in p['architecture'] if f'_{kind}_' in v['model']),None)
   if hit:views.append(view(kind,[hit['x']+4,3.3,hit['z']+5],[hit['x']-1,6,hit['z']-4],82))
   if hit and kind=='platforms':
    r=int(hit['model'].split('_')[1]);mask=int(hit['model'].split('_')[3])
    eye=[hit['x']-(r+2),2.7,hit['z']+.5] if mask==10 else [hit['x']+.5,2.7,hit['z']+r+2]
    views.append(view('platform-entrance',eye,[hit['x']+.5,3,hit['z']+.5],78))
 meta=dict(name=f'floor-{floor}',vertexCount=len(data),lights=lights[:32],views=views,shots=[],key=[2,30,-8 if floor==0 else 250 if floor!=7 else -24],center=[0,8,256 if floor in [10,20,25] else -12])
 (OUT/f'floor-{floor}.json').write_text(json.dumps(meta),encoding='utf8')
renderer=ROOT/'tools/tartarus_rework/RenderNative.java';s=renderer.read_text()
if 'class RenderPreview' in s:
 s=s.replace('class RenderPreview','class RenderNative').replace('Offline architectural prototype, hidden OpenGL context; never launches Minecraft.','Production chunk geometry and materials, hidden offline OpenGL lighting probe; never launches Minecraft.')
 s=s.replace('layout(location=3) in vec2 Surface;','layout(location=3) in vec2 Surface;layout(location=4) in vec2 TexCoord;')
 s=s.replace('out vec4 shadowPos;','out vec4 shadowPos;out vec2 uv;').replace('surface=Surface;','surface=Surface;uv=TexCoord;')
 s=s.replace('in vec4 shadowPos;','in vec4 shadowPos;in vec2 uv;uniform sampler2DArray Materials;')
 s=s.replace('base=pow(color,vec3(2.2))','base=pow(color*texture(Materials,vec3(uv,surface.x)).rgb,vec3(2.2))')
 lines=s.splitlines();lines=[line for line in lines if not any('if(mat=='+str(i)+')' in line for i in [1,2,4,7,8,9])]
 s='\n'.join(lines).replace('if(mat==6){emission=1.5;}','if(mat==6){emission=1.5;}if(mat>=7){emission=.6;}')
 s=s.replace('name+".bin"','name+"-render.bin"').replace('for(int i=0;i<4;i++){glEnableVertexAttribArray(i);glVertexAttribPointer(i,i==3?2:3,GL_FLOAT,false,44,i*12);}','for(int i=0;i<5;i++){glEnableVertexAttribArray(i);glVertexAttribPointer(i,i>=3?2:3,GL_FLOAT,false,52,i==4?44:i*12);}')
 s=s.replace('List.of("andar-10","andar-20","andar-25","escada-comum")','List.of("floor-0","floor-7","floor-10","floor-20","floor-25")').replace('glDisable(GL_CULL_FACE)','glEnable(GL_CULL_FACE)')
 upload='''
   glActiveTexture(GL_TEXTURE1);int materials=glGenTextures();glBindTexture(GL_TEXTURE_2D_ARRAY,materials);glTexImage3D(GL_TEXTURE_2D_ARRAY,0,GL_RGBA8,128,128,10,0,GL_RGBA,GL_UNSIGNED_BYTE,(ByteBuffer)null);
   for(int i=0;i<10;i++){var im=ImageIO.read(Path.of(args[1],"textures/block/tartarus_native_"+i+".png").toFile());var pix=BufferUtils.createByteBuffer(128*128*4);for(int y=0;y<128;y++)for(int x=0;x<128;x++){int argb=im.getRGB(x,y);pix.put((byte)(argb>>16)).put((byte)(argb>>8)).put((byte)argb).put((byte)255);}pix.flip();glTexSubImage3D(GL_TEXTURE_2D_ARRAY,0,0,0,i,128,128,1,GL_RGBA,GL_UNSIGNED_BYTE,pix);}
   glTexParameteri(GL_TEXTURE_2D_ARRAY,GL_TEXTURE_MIN_FILTER,GL_LINEAR);glTexParameteri(GL_TEXTURE_2D_ARRAY,GL_TEXTURE_MAG_FILTER,GL_LINEAR);glUseProgram(program);glUniform1i(glGetUniformLocation(program,"Materials"),1);
'''
 s=s.replace('   for(String scene:',upload+'\n   for(String scene:')
 renderer.write_text(s,encoding='utf8')
print('Production render inputs prepared; real baked chunks and textures, offline lighting only.')
