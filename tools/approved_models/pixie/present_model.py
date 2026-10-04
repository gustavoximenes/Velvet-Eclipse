"""Render the delivered native model and all three clips, with fixed cameras."""
import math
import numpy as np
from PIL import Image,ImageDraw,ImageFont
import render_model as r

NAVY='#10182b';CYAN='#b9e5df';WHITE='#edf5f6';OUT=r.OUT
def font(size,bold=False):return ImageFont.truetype('C:/Windows/Fonts/segoeuib.ttf' if bold else 'C:/Windows/Fonts/segoeui.ttf',size)
def text(im,xy,value,size=18,color=WHITE,bold=False):ImageDraw.Draw(im).text(xy,value,font=font(size,bold),fill=color)
def clip(kind):return 'animation.pixie.'+{'idle':'air_idle','magic':'magic_attack','physical':'physical_attack'}[kind]
def bounds(yaw,kinds=('idle','magic','physical')):
    vertices=[]
    for kind in kinds:
        key=clip(kind);length=r.animations[key]['animation_length']
        for t in np.linspace(0,length,17):vertices.extend(q for q,*_ in r.surfaces(yaw,2,time=t,clip=key))
    a=np.concatenate(vertices);lo=a.min(0);hi=a.max(0)
    lo-=8;hi+=8
    return lo,hi
def savegif(path,frames,ms):
    palette_sheet=Image.new('RGB',(frames[0].width*4,frames[0].height*2))
    for i,n in enumerate(np.linspace(0,len(frames)-1,8,dtype=int)):
        palette_sheet.paste(frames[n],(i%4*frames[0].width,i//4*frames[0].height))
    palette=palette_sheet.quantize(256,method=Image.Quantize.MEDIANCUT)
    indexed=[f.quantize(palette=palette,dither=Image.Dither.NONE) for f in frames]
    indexed[0].save(path,save_all=True,append_images=indexed[1:],duration=ms,loop=0,disposal=2,optimize=False)

def main():
    camera=-35;framing=bounds(camera)
    im=Image.new('RGB',(1500,1100),NAVY)
    text(im,(30,17),'PIXIE',42,bold=True)
    text(im,(33,72),'REVISÃO 03  /  MODELO E TRÊS ANIMAÇÕES PARA AVALIAÇÃO',17,CYAN)
    for i,(kind,title,t) in enumerate([('idle','REPOUSO FLUTUANDO',0),('magic','ATAQUE MÁGICO',1.18),('physical','TAPINHA COM A MÃO DIREITA',.66)]):
        im.paste(r.render(camera,484,764,pitch=2,time=t,clip=clip(kind),bounds=framing),(8+i*500,111))
        text(im,(25+i*500,122),title,16,NAVY,True)
    text(im,(28,910),f"{sum(len(b.get('cubes',[])) for b in r.data['bones'])} cuboides • {len(r.data['bones'])} ossos • Box UV • mãos e asas articuladas",19,CYAN)
    text(im,(28,951),'Repouso: 4 s  |  Magia: 2,8 s  |  Físico: 1,8 s',20,WHITE,True)
    text(im,(28,995),'Prévia da geometria e animações entregues, sem efeitos adicionados e sem alterar o mod.',19,CYAN)
    im.save(OUT/'pixie-preview-03.png')
    im=Image.new('RGB',(1600,950),NAVY);text(im,(25,13),'PIXIE  /  VISTAS DO MODELO',30,bold=True)
    for i,(yaw,title) in enumerate([(0,'FRENTE'),(-35,'TRÊS QUARTOS'),(90,'PERFIL'),(180,'COSTAS')]):
        im.paste(r.render(yaw,386,806,pitch=0),(10+i*399,72))
        text(im,(27+i*399,86),title,16,NAVY,True)
    text(im,(25,906),'Asas com recorte em pixels e pintura nos dois lados; cuboides com dimensões inteiras.',18,CYAN)
    im.save(OUT/'pixie-views-03.png')
    im=Image.new('RGB',(1200,660),NAVY);text(im,(25,12),'PIXIE  /  ROSTO E ASAS',28,bold=True)
    im.paste(r.render(-25,584,550,head=True,pitch=0),(10,63))
    im.paste(r.render(180,584,550,pitch=0),(606,63))
    text(im,(25,628),'Feições em volume; orelhas pontudas; cabelo castanho-avermelhado; asas de folha com pontas curvas.',16,CYAN)
    im.save(OUT/'pixie-details-03.png')
    for kind,times in [('magic',[0,.35,.78,1.18,2.05,2.8]),('physical',[0,.26,.49,.66,1.05,1.8])]:
        im=Image.new('RGB',(1500,630),NAVY)
        text(im,(24,12),'PIXIE  /  '+('GESTO MÁGICO' if kind=='magic' else 'TAPINHA — UM ÚNICO GOLPE'),26,bold=True)
        for i,t in enumerate(times):
            im.paste(r.render(camera,246,517,pitch=2,time=t,clip=clip(kind),bounds=framing),(i*250+2,61))
            text(im,(i*250+15,588),f'{t:.2f} s',16,CYAN)
        im.save(OUT/f'pixie-{kind}-poses-03.png')
    print('Stills and pose sequences saved',flush=True)
    for kind,n,ms,label in [('idle',80,50,'REPOUSO FLUTUANDO'),('magic',70,40,'ATAQUE MÁGICO'),('physical',45,40,'TAPINHA — MÃO DIREITA')]:
        frames=[];length=r.animations[clip(kind)]['animation_length']
        for i in range(n):
            im=Image.new('RGB',(640,800),NAVY)
            im.paste(r.render(camera,640,696,pitch=2,time=i*length/n,clip=clip(kind),bounds=framing),(0,50))
            text(im,(20,12),'PIXIE  /  '+label,20,bold=True)
            text(im,(20,765),'Geometria real • revisão 03 • prévia offline',17,CYAN)
            frames.append(im)
        savegif(OUT/f'pixie-{kind}-03.gif',frames,ms)
        print(kind+' GIF saved',flush=True)
    # One navigation-free comparison sheet shows all clips in parallel.
    frames=[]
    for i in range(100):
        t=i*.04;im=Image.new('RGB',(1200,650),NAVY)
        for col,(kind,label,start) in enumerate([('idle','REPOUSO',0),('magic','MAGIA',.35),('physical','TAPINHA',.65)]):
            time=t if kind=='idle' else float(np.clip(t-start,0,r.animations[clip(kind)]['animation_length']))
            im.paste(r.render(camera,392,547,pitch=2,time=time,clip=clip(kind),bounds=framing),(col*400+4,46))
            text(im,(col*400+20,11),label,22,bold=True)
        text(im,(20,614),'PIXIE • revisão 03 • mesmos arquivos do modelo editável • sem integração ao mod',18,CYAN)
        frames.append(im)
    savegif(OUT/'pixie-animations-03.gif',frames,40)
    print('Combined animation GIF saved',flush=True)

if __name__=='__main__':main()
