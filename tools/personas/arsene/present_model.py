"""Render approval material from the delivered geometry, PNG and animation JSON."""
import json, math
import numpy as np
from PIL import Image, ImageDraw, ImageFont
from render_model import OUT,render,surfaces,animations

BG='#e0e6e3'
INK='#0b1423'
RED='#d12942'
def font(size,bold=False):
 return ImageFont.truetype('C:/Windows/Fonts/segoeuib.ttf' if bold else 'C:/Windows/Fonts/segoeui.ttf',size)

def bounds_for(clip,yaw,pitch=4):
 vertices=[]
 for t in np.linspace(0,animations[clip]['animation_length'],41):
  vertices.extend(q[0] for q in surfaces(yaw,pitch,time=float(t),clip=clip))
 v=np.concatenate(vertices);return v.min(axis=0),v.max(axis=0)

def contact(clip,name,times,labels):
 cols=len(times);cellw=610;cellh=510
 im=Image.new('RGB',(cols*cellw,cellh+120),BG);d=ImageDraw.Draw(im)
 d.rectangle((0,0,im.width,72),fill=INK)
 d.text((25,13),name,font=font(33,True),fill='white')
 bound=bounds_for(clip,-14)
 for i,(t,label) in enumerate(zip(times,labels)):
  im.paste(render(-14,cellw,cellh,time=t,clip=clip,bounds=bound),(i*cellw,72))
  d.text((i*cellw+24,cellh+81),f'{t:.2f}s  /  {label}',font=font(21,True),fill=INK)
 return im

def main():
 front=render(0,1024,1120);front.save(OUT/'arsene-front.png')
 angle=render(-28,1024,1120);angle.save(OUT/'arsene-three-quarter.png')
 back=render(180,800,880);back.save(OUT/'arsene-back.png')
 side=render(80,800,1024);side.save(OUT/'arsene-side.png')
 detail=render(-16,760,900,head=True,pitch=0);detail.save(OUT/'arsene-details.png')
 sheet=Image.new('RGB',(1800,1400),INK);d=ImageDraw.Draw(sheet)
 d.text((42,23),'ARSÈNE',font=font(61,True),fill='white')
 d.text((46,103),'ESTUDO 3D  /  PERSONA  /  VERSÃO 02',font=font(23,True),fill='#ef6175')
 sheet.paste(angle.resize((1000,1094)),(24,172))
 sheet.paste(detail.resize((440,521)),(1038,172))
 sheet.paste(render(168,714,572),(1038,710))
 d=ImageDraw.Draw(sheet)
 d.text((1510,220),'CARTOLA',font=font(24,True),fill='white')
 d.text((1510,263),'Máscara negra',font=font(20),fill='#b6c4d3')
 d.text((1510,298),'Cravat branco',font=font(20),fill='#b6c4d3')
 d.text((1510,333),'Casaca rubra',font=font(20),fill='#b6c4d3')
 d.text((1510,368),'Correntes douradas',font=font(20),fill='#b6c4d3')
 d.text((1510,418),'ASAS',font=font(24,True),fill='white')
 d.text((1510,461),'Penas em camadas',font=font(20),fill='#b6c4d3')
 d.text((1510,496),'Pontas articuladas',font=font(20),fill='#b6c4d3')
 d.text((46,1306),'IDLE   •   ATAQUE MÁGICO   •   CHUTE DIAGONAL',font=font(29,True),fill='white')
 d.text((46,1354),'Renderizações dos arquivos entregues. Modelo separado, aguardando aprovação para integração.',font=font(21),fill='#b6c4d3')
 sheet.save(OUT/'arsene-approval.png')
 contact('animation.arsene.magic_attack','MAGIA  /  Elevação → varredura da mão para baixo → retorno',[.0,.8,1.6,2.8],['Idle','Preparação','Liberação • olhar à frente','Retorno']).save(OUT/'arsene-magic-poses.png')
 contact('animation.arsene.physical_attack','FÍSICO  /  Chute diagonal de cima para baixo',[.0,.65,.95,1.25,2.6],['Idle','Elevação','Ponto alto','Corte descendente','Retorno']).save(OUT/'arsene-physical-poses.png')
 print('Stills and pose sheets saved.',flush=True)
 for short,clip,yaw,size in [('idle','ground_idle',-22,(660,720)),('magic','magic_attack',-16,(900,700)),('physical','physical_attack',-14,(900,700))]:
  name='animation.arsene.'+clip;duration=animations[name]['animation_length'];bound=bounds_for(name,yaw)
  frames=[];fps=20;count=round(duration*fps)
  for i in range(count):
   t=i/fps;frame=Image.new('RGB',size,BG)
   frame.paste(render(yaw,size[0],size[1]-64,time=t,clip=name,bounds=bound),(0,50))
   d=ImageDraw.Draw(frame);d.rectangle((0,0,size[0],47),fill=INK)
   title={'idle':'ARSÈNE  /  IDLE','magic':'ARSÈNE  /  ATAQUE MÁGICO','physical':'ARSÈNE  /  CHUTE DIAGONAL'}[short]
   d.text((18,8),title,font=font(21,True),fill='white')
   d.line((0,size[1]-3,int(size[0]*i/count),size[1]-3),fill=RED,width=3)
   frames.append(frame)
  # RGB frames share a palette so texture colors do not flash from frame to frame.
  atlas=Image.new('RGB',(size[0],size[1]*4))
  for j in range(4):atlas.paste(frames[j*count//4],(0,j*size[1]))
  palette=atlas.quantize(colors=128,method=Image.Quantize.MEDIANCUT)
  indexed=[f.quantize(palette=palette,dither=Image.Dither.NONE) for f in frames]
  indexed[0].save(OUT/f'arsene-{short}.gif',save_all=True,append_images=indexed[1:],duration=50,loop=0,disposal=2,optimize=False)
  print(f'{short}: {count} frames rendered from exported tracks.',flush=True)

if __name__=='__main__':main()
