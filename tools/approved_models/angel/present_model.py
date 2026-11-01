"""Render the shipped editable geometry, atlas and clips; no image substitution."""
from pathlib import Path
import json,hashlib,subprocess,sys
import numpy as np
from PIL import Image,ImageDraw,ImageFont
import render_model as r
from animate_model import LENGTHS
O=r.OUT;BG='#101a2c';CYAN='#a1d7e8';WHITE='#f4f3e9';YAW=24;PITCH=3
FF=Path(__file__).resolve().parents[2]/'work/awakening-preview/deps/imageio_ffmpeg/binaries/ffmpeg-win-x86_64-v7.1.exe'
def font(n,b=False):return ImageFont.truetype('C:/Windows/Fonts/segoeuib.ttf' if b else 'C:/Windows/Fonts/segoeui.ttf',n)
def text(im,xy,s,n=19,color=WHITE,b=False):ImageDraw.Draw(im).text(xy,s,font=font(n,b),fill=color)
def clip(kind):return 'animation.angel.'+kind
def fit(kind,yaws=(YAW,),head=False):
 points=[]
 for yaw in yaws:
  for t in np.linspace(0,LENGTHS[kind],37):
   points.append(np.concatenate([q[0] for q in r.surfaces(yaw,PITCH,True,t,clip(kind))]) if head else r.bounds_vertices(yaw,PITCH,float(t),clip(kind)))
 a=np.concatenate(points);return a.min(0)-5,a.max(0)+5
def head_bounds():
 a=[fit('expression_'+kind,(0,),True) for kind in ['serene','focus','exertion','hurt']]
 return np.min([x[0] for x in a],0),np.max([x[1] for x in a],0)
def save_media(name,frames,duration=50):
 sheet=Image.new('RGB',(frames[0].width*4,frames[0].height*2))
 for i,j in enumerate(np.linspace(0,len(frames)-1,8,dtype=int)):sheet.paste(frames[j],(i%4*frames[0].width,i//4*frames[0].height))
 pal=sheet.quantize(256,method=Image.Quantize.MEDIANCUT)
 frames=[f.quantize(palette=pal,dither=Image.Dither.NONE) for f in frames]
 p=O/f'angel-{name}-03.gif';frames[0].save(p,save_all=True,append_images=frames[1:],duration=duration,loop=0,disposal=2,optimize=False)
 subprocess.run([str(FF),'-y','-v','error','-i',str(p),'-an','-c:v','libx264','-crf','19','-pix_fmt','yuv420p','-movflags','+faststart',str(p.with_suffix('.mp4'))],check=True)
def stills():
 im=Image.new('RGB',(1440,1000),BG);text(im,(28,18),'ANGEL / ESTUDO DE MODELAGEM 03',36,b=True);text(im,(30,66),'JUSTICE · ROSTO OVAL, EXPRESSÕES E ASAS ARTICULADAS',19,CYAN)
 im.paste(r.render(YAW,904,820,pitch=PITCH),(18,120))
 im.paste(r.render(0,480,430,head=True,pitch=PITCH),(940,120));text(im,(955,126),'ROSTO / REPOUSO',16,BG,True)
 im.paste(r.render(64,480,366,head=True,pitch=PITCH),(940,574));text(im,(955,580),'PERFIL / VOLUME REAL',16,BG,True)
 text(im,(27,962),'Prévia offline dos arquivos editáveis · sem integração no mod',20,CYAN);im.save(O/'angel-preview-03.png')
 im=Image.new('RGB',(1440,1240),BG);text(im,(25,18),'ANGEL / QUATRO VISTAS',32,b=True)
 for i,(yaw,label) in enumerate([(0,'FRENTE'),(30,'TRÊS QUARTOS'),(90,'PERFIL'),(180,'COSTAS')]):
  x=10+i%2*720;y=76+i//2*568;im.paste(r.render(yaw,700,550,pitch=PITCH),(x,y));text(im,(x+15,y+10),label,16,BG,True)
 im.save(O/'angel-views-03.png')
 hb=head_bounds();im=Image.new('RGB',(1600,560),BG);text(im,(24,16),'ANGEL / EXPRESSÕES FACIAIS',30,b=True)
 for i,(kind,label) in enumerate([('serene','REPOUSO'),('focus','CONCENTRAÇÃO'),('exertion','ESFORÇO'),('hurt','REAÇÃO A DANO')]):
  im.paste(r.render(0,388,430,head=True,pitch=PITCH,time=1.2,clip=clip('expression_'+kind),bounds=hb),(i*400+6,75));text(im,(i*400+22,515),label,19,CYAN,True)
 im.save(O/'angel-expressions-03.png')
 im=Image.new('RGB',(1440,600),BG);text(im,(25,16),'ANGEL / CONSTRUÇÃO DO ROSTO',29,b=True)
 for i,(yaw,clay,bare,label) in enumerate([(0,False,False,'FRENTE'),(32,False,False,'TRÊS QUARTOS'),(70,True,True,'CONTORNO SEM CABELO')]):
  im.paste(r.render(yaw,468,486,head=True,pitch=PITCH,clay=clay,bare=bare),(6+i*480,78));text(im,(20+i*480,82),label,16,BG,True)
 im.save(O/'angel-face-03.png')
 print('Stills ready',flush=True)
 if '--stills' in sys.argv:return
def main():
 stills()
 if '--stills' in sys.argv:return
 framing={};allframes=[]
 for kind,name,label in [('ground_idle','idle','REPOUSO / FLUTUAÇÃO'),('physical_attack','physical','FÍSICO / PROPOSTA PARA REVISÃO'),('magic_attack','magic','MAGIA / ADAPTAÇÃO DO GESTO DO COMPÊNDIO')]:
  bounds=fit(kind);length=LENGTHS[kind];frames=[];overflow=-1e9
  for i,t in enumerate(np.arange(0,length,.05)):
   verts=r.bounds_vertices(YAW,PITCH,t,clip(kind));overflow=max(overflow,float(np.max(bounds[0]-verts)),float(np.max(verts-bounds[1])))
   im=Image.new('RGB',(860,790),BG);text(im,(24,14),'ANGEL',30,b=True);text(im,(24,57),label,17,CYAN)
   im.paste(r.render(YAW,840,640,pitch=PITCH,time=float(t),clip=clip(kind),bounds=bounds),(10,93));text(im,(24,752),f'{t:.2f} s · Modelo real / render offline / estudo 03',17,CYAN)
   frames.append(im)
  assert overflow<=0,(kind,overflow)
  save_media(name,frames);framing[kind]={'frames':len(frames),'overflow':overflow};print('Rendered',name,flush=True)
 hb=head_bounds();frames=[]
 for kind,label in [('serene','REPOUSO'),('focus','CONCENTRAÇÃO'),('exertion','ESFORÇO'),('hurt','REAÇÃO A DANO')]:
  for t in np.arange(0,2.4,.1):
   im=Image.new('RGB',(640,690),BG);text(im,(22,15),'ANGEL / '+label,24,b=True)
   im.paste(r.render(0,620,580,head=True,pitch=PITCH,time=float(t),clip=clip('expression_'+kind),bounds=hb),(10,65));text(im,(22,654),'Boca, mandíbula e cabeça · venda preservada',17,CYAN);frames.append(im)
 save_media('expressions',frames,100);print('Rendered expressions',flush=True)
 bounds=fit('ground_idle',tuple(range(0,360,20)));frames=[]
 for yaw in range(0,360,6):
  im=Image.new('RGB',(860,790),BG);text(im,(24,14),'ANGEL / INSPEÇÃO 360°',29,b=True)
  im.paste(r.render(yaw,840,660,pitch=PITCH,bounds=bounds),(10,70));text(im,(24,752),'Giro de inspeção da câmera · pose de repouso fixa',17,CYAN);frames.append(im)
 save_media('turntable',frames);print('Rendered turntable',flush=True)
 report={'status':'passed','offline':True,'fixed_camera_actions':True,'clips':framing,'source_hashes':{p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in [O/'angel.geo.json',O/'angel.animation.json',O/'angel.png']}}
 (O/'framing-validation.json').write_text(json.dumps(report,indent=2))
if __name__=='__main__':main()
