"""Approval images and GIFs made directly from the exported Orpheus assets."""
import numpy as np
from PIL import Image,ImageDraw,ImageFont
from render_model import OUT,render,surfaces,animations

BG='#e0e6e3';INK='#0c182b';TEAL='#7fc9cd'
def font(size,bold=False):return ImageFont.truetype('C:/Windows/Fonts/segoeuib.ttf' if bold else 'C:/Windows/Fonts/segoeui.ttf',size)

def bounds_for(clip,yaw):
 vertices=[]
 for t in np.linspace(0,animations[clip]['animation_length'],51):vertices.extend(q[0] for q in surfaces(yaw,4,time=float(t),clip=clip))
 v=np.concatenate(vertices);return v.min(axis=0),v.max(axis=0)

def contact(clip,name,times,labels):
 w,h=550,710;bound=bounds_for(clip,-16)
 im=Image.new('RGB',(len(times)*w,h+126),BG);d=ImageDraw.Draw(im)
 d.rectangle((0,0,im.width,74),fill=INK);d.text((25,13),name,font=font(30,True),fill='white')
 for i,(t,label) in enumerate(zip(times,labels)):
  im.paste(render(-16,w,h,time=t,clip=clip,bounds=bound),(i*w,74))
  d.text((i*w+22,h+87),f'{t:.2f}s  /  {label}',font=font(20,True),fill=INK)
 return im

def main():
 idle='animation.orpheus.ground_idle';magic='animation.orpheus.magic_attack';physical='animation.orpheus.physical_attack'
 for name,yaw,w,h,detail in [('front',0,850,1100,False),('three-quarter',-27,900,1100,False),('back',178,850,1100,False),('side',83,800,1100,False),('details',-13,900,1024,True)]:
  render(yaw,w,h,head=detail).save(OUT/f'orpheus-{name}.png')
 render(-16,900,1100,time=1.6,clip=magic).save(OUT/'orpheus-magic-pose.png')
 render(-16,900,1100,time=1.3,clip=physical).save(OUT/'orpheus-physical-pose.png')
 sheet=Image.new('RGB',(1800,1420),INK);d=ImageDraw.Draw(sheet)
 d.text((40,22),'ORPHEUS',font=font(61,True),fill='white')
 d.text((43,105),'ESTUDO 3D  /  PERSONA  /  VERSÃO 02',font=font(23,True),fill=TEAL)
 panels=[(idle,0,'IDLE','Lira nas costas'),(magic,1.6,'MAGIA','Lira baixa à frente; rosto livre'),(physical,1.3,'FÍSICO','Lira baixa, ao lado do corpo')]
 for i,(clip,t,title,caption) in enumerate(panels):
  x=22+i*593
  sheet.paste(render(-16,572,1040,time=t,clip=clip),(x,175))
  d.text((x+20,1235),title,font=font(30,True),fill='white')
  d.text((x+20,1282),caption,font=font(22),fill=TEAL)
 d.text((42,1360),'Mesma paleta nos três clipes  •  Prévias da geometria real  •  Aguardando integração conjunta',font=font(23),fill='#b6c8d2')
 sheet.save(OUT/'orpheus-approval.png')
 contact(magic,'MAGIA  /  Retirada da lira → apoio baixo → execução → retorno',[0,.8,1.6,3.2],['Idle','Transição','Execução','Retorno']).save(OUT/'orpheus-magic-poses.png')
 contact(physical,'FÍSICO  /  Preparação → golpe diagonal com a lira → retorno',[0,.7,1.05,1.3,2.8],['Idle','Preparação','Impulso','Golpe lateral','Retorno']).save(OUT/'orpheus-physical-poses.png')
 print('Views and pose sheets rendered.',flush=True)
 for short,clip,yaw,size in [('idle',idle,-23,(620,800)),('magic',magic,-16,(820,900)),('physical',physical,-16,(820,900))]:
  length=animations[clip]['animation_length'];count=round(length*20);bound=bounds_for(clip,yaw);frames=[]
  for i in range(count):
   frame=Image.new('RGB',size,BG)
   frame.paste(render(yaw,size[0],size[1]-64,time=i/20,clip=clip,bounds=bound),(0,50))
   d=ImageDraw.Draw(frame);d.rectangle((0,0,size[0],47),fill=INK)
   d.text((17,8),'ORPHEUS  /  '+{'idle':'IDLE','magic':'ATAQUE MÁGICO','physical':'ATAQUE FÍSICO'}[short],font=font(22,True),fill='white')
   d.line((0,size[1]-3,int(size[0]*i/count),size[1]-3),fill='#448b96',width=3);frames.append(frame)
  paletteboard=Image.new('RGB',(size[0],size[1]*4))
  for j in range(4):paletteboard.paste(frames[j*count//4],(0,j*size[1]))
  palette=paletteboard.quantize(colors=128,method=Image.Quantize.MEDIANCUT)
  indexed=[f.quantize(palette=palette,dither=Image.Dither.NONE) for f in frames]
  indexed[0].save(OUT/f'orpheus-{short}.gif',save_all=True,append_images=indexed[1:],duration=50,loop=0,disposal=2,optimize=False)
  print(f'{short}: {count} actual geometry frames.',flush=True)

if __name__=='__main__':main()
