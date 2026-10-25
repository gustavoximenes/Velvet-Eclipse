"""Elemental audit 02: shared mesh/keyframe recipes, offline previews, Photon draft assets.
All writes stay in modeling/, outputs/ and work/. Production resources are read only.
"""
from pathlib import Path
import math,json,random,hashlib,subprocess,shutil,sys
import numpy as np
from PIL import Image,ImageDraw,ImageFont,ImageFilter

ROOT=Path(__file__).resolve().parents[3]
OUT=ROOT/'outputs/auditoria-vfx-photon-02'
DOC=ROOT/'velvet-eclipse/modeling/vfx/audit-02'
WORK=ROOT/'work/vfx-audit-02'
FF=ROOT/'work/awakening-preview/deps/imageio_ffmpeg/binaries/ffmpeg-win-x86_64-v7.1.exe'
VIDEO=ROOT/'modeling/nekomata-study-02/work/reload-skills.mp4'
FPS=20;W=640;H=400;TAU=math.tau
def dump(p,d):
 p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(d,ensure_ascii=False,indent=2),encoding='utf-8')
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def font(n,b=False):return ImageFont.truetype('C:/Windows/Fonts/'+('arialbd.ttf' if b else 'arial.ttf'),n)
def rgb(h):return tuple(int(h[i:i+2],16) for i in (1,3,5))
def mix(a,b,t):return np.array(a)*(1-t)+np.array(b)*t
MESH={}
def mesh(name,v,f):MESH[name]={'vertices':np.array(v).round(5).tolist(),'faces':f};return name
def crystal(name,n=5,shoulder=.25):
 v=[[0,1,0],[0,-.6,0]]+[[math.cos(a)*.5,shoulder,math.sin(a)*.5] for a in np.linspace(0,TAU,n,endpoint=False)]
 return mesh(name,v,[[0,2+i,2+(i+1)%n] for i in range(n)]+[[1,2+(i+1)%n,2+i] for i in range(n)])
def sphere(name,n=18,m=10):
 v=[];f=[]
 for j in range(m+1):
  p=math.pi*j/m
  for i in range(n):
   a=TAU*i/n;v.append([math.sin(p)*math.cos(a),math.cos(p),math.sin(p)*math.sin(a)])
 for j in range(m):
  for i in range(n):
   a=j*n+i;b=j*n+(i+1)%n;f.extend([[a,b,a+n],[b,b+n,a+n]])
 return mesh(name,v,f)
def ribbon(name,arc=4.6,height=.8,width=.3,phase=0):
 v=[];f=[]
 for i,t in enumerate(np.linspace(0,1,28)):
  a=phase+arc*t;w=width*(math.sin(math.pi*t)**.7+.01);y=height*(t-.5)
  v.extend([[math.cos(a),y-w,math.sin(a)],[math.cos(a),y+w,math.sin(a)]])
  if i:f.extend([[2*i-2,2*i-1,2*i],[2*i-1,2*i+1,2*i]])
 return mesh(name,v,f)
def stroke(name,points,width):
 v=[];f=[]
 for i,p in enumerate(points):
  v.extend([[p[0]-width,p[1],p[2]],[p[0]+width,p[1],p[2]]])
  if i:f.extend([[2*i-2,2*i-1,2*i],[2*i-1,2*i+1,2*i]])
 return mesh(name,v,f)
def geometry():
 crystal('ice_chunk',7,.15);crystal('ice_spear',5,-.2);crystal('dark_blade',3,-.55)
 sphere('core');ribbon('wind_sweep',4.6,.8,.34);ribbon('wind_crescent',5.4,0,.18)
 ribbon('light_ring',TAU,0,.025);ribbon('fire_curl',4.2,.6,.25)
 stroke('beam',[[0,0,0],[0,1,0]],.5)
 rng=random.Random(41)
 for k in range(8):
  points=[[0,0,0]]+[[rng.uniform(-.15,.15),i/12,rng.uniform(-.09,.09)] for i in range(1,12)]+[[0,1,0]]
  stroke('bolt'+str(k),points,.008)
 mesh('quad',[[-.5,0,-.5],[.5,0,-.5],[.5,0,.5],[-.5,0,.5]],[[0,1,2],[0,2,3]])

def part(parts,model,color,start,life,pos=(0,1,0),size=(1,1,1),rotation=(0,0,0),spin=(0,0,0),end=None,grow=(.1,1,.1),alpha=1,add=True):
 parts.append(dict(model=model,color=color,start=start,life=life,position=pos,endPosition=end or pos,scale=size,rotation=rotation,spin=spin,growth=grow,alpha=alpha,additive=add))
def bits(parts,col,start,ice=False,count=13):
 rng=random.Random(77)
 for i in range(count):
  a=rng.random()*TAU;r=rng.uniform(.4,1.8);y=rng.uniform(.25,2.4);s=rng.uniform(.025,.075)
  part(parts,'ice_chunk' if ice else 'dark_blade',col,start+i%4,9+i%6,(0,.6,0),(s,s*2,s),spin=(0,300,180),end=(math.cos(a)*r,y,math.sin(a)*r),grow=(.2,1,0),alpha=.8)
def ring(parts,color,start,life,r=1,y=.03,width=1,spin=0):
 part(parts,'light_ring',color,start,life,(0,y,0),(r,width,r),spin=(0,spin,0),grow=(.1,1,1.3),alpha=.85)
def family_parts(f,t):
 p=[];rng=random.Random(29+t);end=32 if t<3 else 42
 p.append(dict(model='ground_glow',color={'fire':'#ff8c16','ice':'#8fe3ff','wind':'#b0ffb0','electric':'#edc5ff','light':'#ffed9c','dark':'#ff1546'}[f],start=4,life=15+t*2,position=(0,.025,0),endPosition=(0,.025,0),scale=(2.5+t*.3,2.5+t*.3,1),rotation=(0,0,0),spin=(0,0,0),growth=(.2,1,1.2),alpha=.65,additive=True))
 if f=='fire':
  # Detached curling sheets around a dark volume; tiers add different ring/eruption structures.
  n=[3,5,7,10][t]
  p.append(dict(model='fire_flipbook',color='#ffffff',start=3,life=20+t*3,position=(0,1.05,0),endPosition=(0,1.2,0),scale=(2.35+t*.25,2.35+t*.35,1),rotation=(0,0,0),spin=(0,0,0),growth=(.55,1,1.1),alpha=1,additive=False))
  if t>=2:part(p,'core','#241017',5,13+t*2,(0,.8,0),(.48+t*.15,.55+t*.2,.48+t*.15),grow=(.05,1,.2),alpha=.35,add=False)
  for i in range(n):
   a=TAU*i/n
   part(p,'fire_curl',['#ff520b','#ff8e18','#ffd340'][i%3],3+i%3,12+t*3,(0,.85+t*.12,0),(.63+t*.19,.5+t*.2,.63+t*.19),(i*31,i*63,i*39),spin=(100,260,60),grow=(.02,1,1.1),alpha=.35)
  ring(p,'#ff9b25',4,10,1+t*.22,.035,.7)
  if t>=1:
   for i in range(3+t):part(p,'fire_curl','#ffd045',5+i,13,(0,.6+i*.2,0),(1.2,.24,1.2),(20*i,70*i,40),spin=(10,360,0),grow=(.2,1,.7))
  if t>=2:
   for i in range(3):part(p,'wind_crescent','#ff2b25',4+i*2,12,(0,.8,0),(1.5,.7,1.5),(35*i,50*i,60),spin=(0,-400,0),grow=(.1,1,1.4),alpha=.8)
  if t==3:
   for i in range(5):part(p,'fire_curl','#ffbe1f',2+i*3,20,(0,.3+i*.5,0),(1.4,.9,1.4),(i*45,0,10),spin=(0,600,0),grow=(.02,1,.1))
  bits(p,'#ffb844',7,count=12+t*4)
 elif f=='ice':
  n=[7,8,10,13][t]
  for i in range(n):
   a=i*TAU/n;r=.65 if t==0 else .8
   pos=(math.cos(a)*r,.65+rng.random()*.8,math.sin(a)*r)
   size=(.23,.25,.23) if t==0 else ((.3,.7,.3) if t==1 else (.45,1.4,.45))
   if t>=2:pos=(math.cos(a)*.5,.8,math.sin(a)*.5)
   part(p,'ice_chunk' if t==0 else 'ice_spear',['#9cddff','#c2f6ff','#50ace3'][i%3],3+i%3,16 if t==0 else 18,pos,size,(40*math.cos(a),a*180/math.pi,55*math.sin(a)),spin=(0,60,0),end=pos,grow=(.01,1,.05),alpha=.9,add=False)
  if t==3:
   for i in range(3):part(p,'ice_spear','#bdefff',4+i,24,(0,.8+i*.45,0),(.65,1.7,.65),(0,i*110,65),spin=(0,80,0),grow=(.01,1,.02),alpha=.85,add=False)
  ring(p,'#90f6ff',3,19,1.1+t*.2,.025,.5);bits(p,'#beefff',18,True,16+t*3)
 elif f=='wind':
  if t<2:
   for i in range(3+t):part(p,'wind_sweep',['#b6ffd8','#e5ffe3','#78dba6'][i%3],3+i*2,10+t*2,(0,.6+i*.34,0),(1.15+t*.2,.85,1.15+t*.2),(15*i,i*115,15),spin=(0,470,0),grow=(.1,1,1.4),alpha=.85)
  else:
   for i in range(7 if t==2 else 11):part(p,'wind_crescent',['#d6ffae','#85ff4b','#f2ffe3'][i%3],3+i%4,18 if t==2 else 28,(0,1.1,0),(1.35 if t==2 else 1.9,.9,1.35 if t==2 else 1.9),(i*31,i*47,i*71),spin=(70,350,100),grow=(.1,1,1.05),alpha=.78)
   ring(p,'#aaff73',11,13,1.5+t*.1,.03,3)
  bits(p,'#a6e8ac',9,count=10+t*2)
 elif f=='electric':
  if t<2:
   for i in range(1+t*3):
    x=(i-1.5)*.25 if t else 0
    part(p,'bolt'+str(i),'#ffe3a0',4+i%2,4,(x,.05,0),(1,3.5,1),grow=(1,1,1),alpha=1)
    part(p,'bolt'+str(i),'#fffef0',4+i%2,3,(x,.05,.015),(.45,3.5,1),grow=(1,1,1),alpha=1)
   ring(p,'#edc4ff',4,8,1.1,.04,2)
  else:
   # Ziodyne is a charged body-scale sphere and arcs, not a longer Zio beam.
   for i in range(5):part(p,'wind_crescent',['#c580ff','#9bd8ff','#fff8c8'][i%3],2+i%3,15,(0,1.1,0),(1.15,1.1,1.15),(i*48,i*39,i*65),spin=(80,320,130),grow=(.05,1,.1),alpha=.9)
   for i in range(7):part(p,'bolt'+str(i),'#fff0a5',7+i%3,6,(0,.9,0),(2.8,2.6,2.8),(i*51,i*47,75),grow=(.3,1,1.2))
   if t==3:
    part(p,'core','#362875',1,15,(0,2.5,0),(.5,.5,.5),grow=(.01,1,.1),add=False)
    for i in range(4):part(p,'bolt'+str(i),'#edc8ff',14+i,5,(0,0,0),(1.8,4,1.8),rotation=(0,i*80,0),grow=(1,1,1))
    ring(p,'#d1d6ff',14,13,2,.04,4)
  bits(p,'#fff0a5',7 if t<2 else 14,count=14)
 elif f=='light':
  for i in range(2+t*2):part(p,'wind_crescent','#ffe6a2',2+i,12,(0,1.3+i*.18,0),(1,.2,1),(i*12,i*68,0),spin=(0,320,0),grow=(.1,1,.4),alpha=.9)
  part(p,'beam','#fff4b4',10,5+t*3,(0,.02,0),(.07+t*.07,2.3+t*.5,.07),grow=(.01,1,.1))
  ring(p,'#ffd567',10,12,1.15+t*.2,.03,2)
  if t>=2:
   for i in range(7):
    a=TAU*i/7;part(p,'beam','#ffe79c',10+i%2,12,(math.cos(a)*.7,.02,math.sin(a)*.7),(.025,2.7,.025),grow=(.1,1,.1))
  bits(p,'#ffe7a1',12,count=15)
 elif f=='dark':
  for i in range(3+t*3):
   a=i*TAU/(3+t*3);r=.6+t*.1;pos=(math.cos(a)*r,.02,math.sin(a)*r)
   part(p,'dark_blade',['#e5004c','#ff3094','#9f1437'][i%3],4+i%3,10+t*3,pos,(.15+t*.07,1.1+t*.5,.15),(0,i*53,15*math.sin(a)),spin=(0,100,0),end=(pos[0],1.1+t*.3,pos[2]),grow=(.01,1,.01),alpha=.9)
  for i in range(2+t):part(p,'fire_curl','#d80749',3+i,15,(0,.35+i*.25,0),(.8,.4,.8),(i*30,i*70,0),spin=(0,-320,0),grow=(.1,1,.3),alpha=.8)
  ring(p,'#fd183e',4,12,1,.03,.5);bits(p,'#df194e',11,count=12)
 return p

# Times identify the Reload segments only. Curves and sizes below are authoring proposals.
SPECS=[
 ('fire',['agi','agilao','agidyne','inferno'],[10,11,12,16],[333,341,349,359],[335.35,343.35,352.8,362.3],
  ['Explosão arredondada, fogo laranja e recortes escuros.','Chamas que se abrem e aro de fogo, com centro mais luminoso.','Núcleo escuro maior, cortes circulares e explosão mais densa.','Eruptura e massas de fogo em sequência; estudo severo separado.']),
 ('ice',['bufu','bufula','bufudyne','diamond_dust'],[20,21,22,26],[407,416,425,435],[409.45,418.65,428.35,438.6],
  ['Pequenos volumes de gelo em torno do alvo, seguidos de quebra.','Fragmentos maiores e queda/choque antes da dispersão.','Explosão de pontas de gelo e fragmentação radial.','Massa facetada maior e estilhaçamento prolongado.']),
 ('wind',['garu','garula','garudyne','panta_rhei'],[30,31,32,36],[476,485,494,505],[478.8,487.3,496.7,508.1],
  ['Rajadas curvas claras com verde discreto; não uma hélice permanente.','Lâminas de vento mais largas e lançamento ascendente.','Casca esférica de vento verde com expansão na base.','Turbilhão amplo, folhas e ruptura da esfera.']),
 ('electric',['zio','zionga','ziodyne','thunder_reign'],[40,41,42,46],[544,552,560,570],[546.35,553.7,562.5,574],
  ['Descarga fina amarela/branca e contato curto no chão.','Descarga ramificada com arcos próximos do alvo.','Esfera azul/violeta com descargas amarelas laterais.','Carga elevada, raio e grande onda de impacto.']),
 ('light',['kouha','kouga','kougaon'],[56,57,58],[863,867,872],[865.8,870.55,875.9],
  ['Faixas douradas convergentes e clarão breve.','Aros de luz e feixe vertical que alcança o alvo.','Pilar dourado, feixes secundários e impacto na base.']),
 ('dark',['eiha','eiga','eigaon'],[70,71,72],[907,911,915],[909.8,913.75,918.9],
  ['Erupção rubra estreita com partículas escuras.','Correntes vermelhas ascendentes e espirais.','Pilar de lâminas magenta, ruptura e brilho vermelho no chão.'])]

def rotation(deg):
 x,y,z=np.radians(deg);cx,sx,cy,sy,cz,sz=math.cos(x),math.sin(x),math.cos(y),math.sin(y),math.cos(z),math.sin(z)
 return np.array([[cz,-sz,0],[sz,cz,0],[0,0,1]])@np.array([[cy,0,sy],[0,1,0],[-sy,0,cy]])@np.array([[1,0,0],[0,cx,-sx],[0,sx,cx]])
CAM=rotation((18,-28,0)); SCALE=80
def project(v):
 q=np.array(v)@CAM.T
 return np.column_stack((W*.5+q[:,0]*SCALE,H*.86-q[:,1]*SCALE,q[:,2]))
def cube(center,size):
 v=np.array([[x,y,z] for x in [-.5,.5] for y in [-.5,.5] for z in [-.5,.5]])*size+center
 return v,[[0,1,3,2],[4,6,7,5],[0,4,5,1],[2,3,7,6],[1,5,7,3],[0,2,6,4]]
def backdrop():
 im=Image.new('RGB',(W,H),'#071120');d=ImageDraw.Draw(im)
 for x in range(-5,6):
  pts=project([[x,0,-5],[x,0,5]]);d.line([tuple(p[:2]) for p in pts],fill='#192d40')
 for z in range(-5,6):
  pts=project([[-5,0,z],[5,0,z]]);d.line([tuple(p[:2]) for p in pts],fill='#192d40')
 return im
BACK=backdrop()
FIRE=[];GLOW=None
def fire_textures():
 global GLOW
 rng=np.random.default_rng(91);n=192;yy,xx=np.mgrid[:n,:n];x=(xx-n/2)/(n/2);y=(yy-n/2)/(n/2)
 def noise(x,y):
  value=np.zeros_like(x)
  for f,amp in [(3,.45),(7,.24),(15,.14),(31,.08),(58,.045)]:
   value+=amp*np.sin(x*f+np.sin(y*f*.76+f))*np.cos(y*f*.9+np.sin(x*f*.8))
  return value
 for i in range(32):
  t=i/31;r=np.hypot(x,y+.1);a=np.arctan2(y+.1,x);v=noise(x+.15*np.sin(t*7+y*3),y+t*.85)
  edge=.73+.11*np.sin(a*6+t*5)+v*.16
  alpha=np.clip((edge-r)*26,0,1)*np.clip((1-t)*5,0,1)
  hot=np.clip(.45+v*.9+np.sin((r+t*.15)*9-a*2)*.22+(r-.2)*.24,0,1)
  dark=np.clip(1-(r/.44)**2,0,1)*(.7+.3*np.cos(t*6))
  hot=np.clip(hot-dark*.65,0,1)
  colors=np.empty((n,n,3));colors[:,:,0]=np.clip(hot*2.8,.055,1);colors[:,:,1]=np.clip((hot-.28)*2,0,1);colors[:,:,2]=np.clip((hot-.68)*2.7,0,.85)
  colors=colors*255;colors[:,:,2]+=5
  FIRE.append(Image.fromarray(np.concatenate([colors,alpha[:,:,None]*255],axis=2).clip(0,255).astype('uint8'),'RGBA'))
 sheet=Image.new('RGBA',(n*8,n*4))
 for i,im in enumerate(FIRE):sheet.paste(im,(i%8*n,i//8*n))
 folder=OUT/'draft/resources/assets/velvet_eclipse/textures/vfx/audit02';folder.mkdir(parents=True,exist_ok=True);sheet.save(folder/'fire.png')
 alpha=(np.exp(-(x*x+y*y)*5)*np.clip(1-np.hypot(x,y),0,1)*255).astype('uint8')
 GLOW=Image.new('RGBA',(n,n),'white');GLOW.putalpha(Image.fromarray(alpha));GLOW.save(folder/'glow.png')
def frame(profile,tick,area=False):
 image=BACK.copy();draw=ImageDraw.Draw(image,'RGBA');faces=[]
 centers=[(-1.5,0,0),(0,0,.3),(1.5,0,0)] if area else [(0,0,0)]
 globalScale=.63 if area else 1
 for c in centers:
  c=np.array(c)
  for pos,size in [((0,1.58,0),(.36,.38,.35)),((0,1.05,0),(.45,.6,.27)),((-.32,1.06,0),(.15,.61,.18)),((.32,1.06,0),(.15,.61,.18)),((-.13,.36,0),(.18,.72,.22)),((.13,.36,0),(.18,.72,.22))]:
   v,f=cube(np.array(pos)*globalScale+c,np.array(size)*globalScale);q=project(v)
   for j,face in enumerate(f):faces.append((float(q[face,2].mean()),[tuple(p[:2]) for p in q[face]],(50+j*4,69+j*4,81+j*4),255,False))
  for layer in profile['parts']:
   u=(tick-layer['start'])/layer['life']
   if not 0<=u<1:continue
   g=layer['growth'];growth=(g[0]+(g[1]-g[0])*min(1,u/.27)) if u<.27 else g[1]+(g[2]-g[1])*((u-.27)/.73)
   alpha=layer['alpha']*min(1,u*12+.2)*min(1,(1-u)*4)
   if layer['model'] in ['fire_flipbook','ground_glow']:
    at=mix(layer['position'],layer['endPosition'],u)*globalScale+c;q=project([at])[0]
    sz=np.array(layer['scale'][:2])*growth*SCALE*globalScale
    if layer['model']=='ground_glow':sz[1]*=.32
    faces.append((float(q[2])+(5 if layer['model']=='ground_glow' else 0),(q,sz,int(u*31),layer['model'],layer['color']),None,int(alpha*255),'sprite'));continue
   model=MESH[layer['model']];v=np.array(model['vertices'])*np.array(layer['scale'])*growth
   angles=np.array(layer['rotation'])+np.array(layer['spin'])*u
   v=v@rotation(angles).T+mix(layer['position'],layer['endPosition'],u);v=v*globalScale+c;q=project(v);color=rgb(layer['color'])
   for f in model['faces']:
    normal=np.cross(v[f[1]]-v[f[0]],v[f[2]]-v[f[0]]);normal/=max(1e-6,np.linalg.norm(normal))
    shade=1 if layer['additive'] else .62+.38*abs(float(normal@np.array([.4,.8,.3])))
    faces.append((float(q[f,2].mean()),[tuple(x[:2]) for x in q[f]],tuple(min(255,int(k*shade)) for k in color),int(255*alpha),layer['additive']))
 # Painter ordering is an offline approximation of depth, not a Minecraft/Photon capture.
 glow=Image.new('RGBA',(W,H));gd=ImageDraw.Draw(glow)
 for depth,poly,color,alpha,add in sorted(faces,key=lambda f:-f[0]):
  if add=='sprite':
   at,sz,idx,kind,col=poly;source=FIRE[idx] if kind=='fire_flipbook' else GLOW
   tile=source.resize((max(1,int(sz[0])),max(1,int(sz[1]))),Image.Resampling.LANCZOS)
   if kind=='ground_glow':
    tint=Image.new('RGBA',tile.size,col);tint.putalpha(tile.getchannel('A'));tile=tint
   tile.putalpha(tile.getchannel('A').point(lambda a:int(a*alpha/255)))
   image.paste(tile,(int(at[0]-sz[0]/2),int(at[1]-sz[1]/2)),tile);draw=ImageDraw.Draw(image,'RGBA');continue
  draw.polygon(poly,fill=(*color,alpha))
  if add:gd.polygon(poly,fill=(*color,int(alpha*.32)))
 bloom=np.asarray(glow.filter(ImageFilter.GaussianBlur(4))).astype(float);base=np.asarray(image).astype(float)
 image=Image.fromarray(np.clip(base+bloom[:,:,:3]*(bloom[:,:,3:4]/255)*1.2,0,255).astype('uint8'));draw=ImageDraw.Draw(image)
 draw.text((20,16),profile['name']+(' · ÁREA' if area else ''),font=font(24,True),fill='#effaff')
 draw.text((20,47),'PROPOSTA 02 · ESTUDO DE VOLUME E MOVIMENTO',font=font(12),fill='#81cee3')
 draw.text((20,H-34),'Prévia offline • Photon: malhas + emissores • alvo 1,8 m',font=font(12),fill='#91a4b8')
 draw.line((20,H-10,20+int((W-40)*tick/54),H-10),fill='#67e6ee',width=3)
 return image

def export_models():
 res=OUT/'draft/resources/assets/velvet_eclipse'
 tex=res/'textures/vfx/audit02';tex.mkdir(parents=True,exist_ok=True);Image.new('RGBA',(4,4),'white').save(tex/'white.png')
 for name,m in MESH.items():
  folder=res/'models/vfx/audit02';folder.mkdir(parents=True,exist_ok=True)
  lines=['mtllib white.mtl','usemtl white','vt 0.5 0.5']+['v '+' '.join(str(x) for x in v) for v in m['vertices']]
  lines+=['f '+' '.join(f'{i+1}/1' for i in f) for f in m['faces']]
  (folder/(name+'.obj')).write_text('\n'.join(lines),encoding='utf-8')
  dump(folder/(name+'.json'),{'loader':'forge:obj','model':'velvet_eclipse:models/vfx/audit02/'+name+'.obj','flip_v':True,'automatic_culling':False,'shade_quads':False,'textures':{'white':'velvet_eclipse:vfx/audit02/white','particle':'velvet_eclipse:vfx/audit02/white'}})
 (res/'models/vfx/audit02/white.mtl').write_text('newmtl white\nKd 1 1 1\nmap_Kd #white\n',encoding='utf-8')

def extract(p):
 ref=OUT/'references';ref.mkdir(parents=True,exist_ok=True)
 video=ref/(p['id']+'.mp4')
 if not video.exists():subprocess.run([str(FF),'-hide_banner','-loglevel','error','-ss',str(p['referenceStart']),'-i',str(VIDEO),'-t',str(p['referenceDuration']),'-an','-vf','scale=640:-2','-r','30','-c:v','libx264','-crf','22','-pix_fmt','yuv420p','-movflags','+faststart','-y',str(video)],check=True)
 subprocess.run([str(FF),'-hide_banner','-loglevel','error','-ss',str(p['referencePeak']),'-i',str(VIDEO),'-frames:v','1','-vf','scale=640:-2','-y',str(ref/(p['id']+'.png'))],check=True)

def build():
 for d in [OUT,DOC,WORK,OUT/'previews']:d.mkdir(parents=True,exist_ok=True)
 geometry();export_models();fire_textures();profiles=[]
 for family,ids,sources,starts,peaks,notes in SPECS:
  for tier,(id,source,start,peak,note) in enumerate(zip(ids,sources,starts,peaks,notes)):
   p=dict(id=id,name=id.replace('_',' ').title(),family=family,tier=tier+1,source=source,referenceStart=start,referenceDuration=peak-start+.8,referencePeak=peak,note=note,parts=family_parts(family,tier),status='Estudo para aprovação; não integrado')
   p['durationTicks']=max(l['start']+l['life'] for l in p['parts']);p['previewTick']=(5 if tier<2 else 12) if family=='electric' else 14 if family=='light' else 10;profiles.append(p)
 dump(OUT/'draft/recipes.json',profiles);dump(OUT/'draft/meshes.json',MESH)
 evidence=json.loads((ROOT/'outputs/auditoria-vfx-photon-01/evidence.json').read_text(encoding='utf-8'))
 dump(OUT/'evidence.json',[e for e in evidence if e.get('source') in {p['source'] for p in profiles} and '/Astrea/' not in e['asset']])
 for p in profiles:
  extract(p)
  frames=[frame(p,t) for t in range(54)]
  frames[p['previewTick']].save(OUT/'previews'/(p['id']+'.png'))
  frames[0].save(OUT/'previews'/(p['id']+'.gif'),save_all=True,append_images=frames[1:],duration=50,loop=0,optimize=False)
  if p['tier']==3:
   frames=[frame(p,t,True) for t in range(54)];frames[0].save(OUT/'previews'/(p['id']+'-area.gif'),save_all=True,append_images=frames[1:],duration=50,loop=0,optimize=False)
  print('rendered',p['id'],flush=True)
 # Six-row reference comparison, chosen peak vs representative proposal shape (not synchronized).
 sheet=Image.new('RGB',(1280,6*430),'#071120');d=ImageDraw.Draw(sheet)
 picks=['agidyne','bufudyne','garudyne','ziodyne','kougaon','eigaon']
 for row,id in enumerate(picks):
  p=next(p for p in profiles if p['id']==id);y=row*430
  d.text((20,y+7),p['name']+' · RELOAD / PROPOSTA 02',font=font(20,True),fill='#e2f5ff')
  ref=Image.open(OUT/'references'/(id+'.png'));sheet.paste(ref,(0,y+46));sheet.paste(frame(p,p['previewTick']),(640,y+30))
 sheet.save(OUT/'comparativo.png')
 dump(OUT/'manifest.json',{'audit':'02','sourceVideo':'https://www.youtube.com/watch?v=TZap-dV5Oyo','scope':'Dano elemental: fogo, gelo, vento, eletricidade, luz e trevas','profiles':profiles,'summonChanged':False,'productionVfxChanged':False,'engineCapture':False})
 shutil.copy2(OUT/'draft/recipes.json',DOC/'recipes.json')
 print('done',len(profiles),flush=True)
if __name__=='__main__':build()
