"""Render the exact editable model, its texture and animation; no game access."""
import math, json
import numpy as np
from PIL import Image, ImageDraw, ImageFont
import render_model as r

OUT=r.OUT
NAVY='#111b31'; WHITE='#edf5fa'; CYAN='#91cce8'; BG='#e0e8eb'
def font(size,bold=False):return ImageFont.truetype('C:/Windows/Fonts/segoeuib.ttf' if bold else 'C:/Windows/Fonts/segoeui.ttf',size)
def text(im,xy,s,size=20,color=WHITE,bold=False):ImageDraw.Draw(im).text(xy,s,font=font(size,bold),fill=color)
def header(im,subtitle):
    ImageDraw.Draw(im).rectangle((24,26,30,81),fill=CYAN)
    text(im,(48,16),'ELIZABETH',38,bold=True)
    text(im,(50,64),subtitle,17,CYAN)

def stills():
    for name,yaw,head in [('front',0,False),('three-quarter',-25,False),('back',180,False),('side',90,False),
                          ('face-front',0,True),('face-three-quarter',-35,True),('face-profile',90,True)]:
        r.render(yaw,900,1120 if not head else 900,head=head,pitch=0 if head else 3).save(OUT/(name+'.png'))
    r.render(-30,1000,1000,head=True,pitch=0,clay=True,bare=True).save(OUT/'face-clay.png')
    im=Image.new('RGB',(1440,1120),NAVY);header(im,'REVISÃO 05  /  CONTORNO OVAL E QUEIXO SUAVIZADO')
    im.paste(r.render(-23,650,938,pitch=2),(25,110))
    im.paste(r.render(0,720,448,head=True,pitch=0),(695,110))
    im.paste(r.render(-32,355,455,head=True,pitch=0),(695,593))
    im.paste(r.render(-32,355,455,head=True,pitch=0,clay=True,bare=True),(1060,593))
    text(im,(45,124),'POSE COM LIVRO',16,'#435974',True)
    text(im,(713,122),'ROSTO • FRENTE',16,'#435974',True)
    text(im,(710,568),'TRÊS QUARTOS',16,CYAN,True)
    text(im,(1067,568),'GEOMETRIA SEM TEXTURA',16,CYAN,True)
    count=sum(len(b.get('cubes',[])) for b in r.data['bones'])
    text(im,(29,1073),f'Geometria real • {count} cuboides • 45 ossos • Box UV • Sem integração ao mod',18,CYAN)
    im.save(OUT/'elizabeth-preview-05.png')
    im=Image.new('RGB',(1600,1070),NAVY);header(im,'VISTAS DE INSPEÇÃO  /  MESMA GEOMETRIA, SEM RETOQUE')
    for i,(name,yaw) in enumerate([('FRENTE',0),('TRÊS QUARTOS',-35),('PERFIL',90),('COSTAS',180)]):
        im.paste(r.render(yaw,386,890,pitch=0),(14+i*398,121))
        text(im,(30+i*398,133),name,16,'#435974',True)
    text(im,(25,1030),'Cuboides nativos com dimensões inteiras; roupa, cabelo, livro e mãos articulados.',18,CYAN)
    im.save(OUT/'elizabeth-views-05.png')
    im=Image.new('RGB',(1440,650),NAVY);header(im,'OMBROS E BRAÇOS  /  ENCAIXE NO TRONCO')
    bounds=(np.array([-70,207,-70]),np.array([70,299,70]))
    for i,(label,yaw) in enumerate([('FRENTE',0),('TRÊS QUARTOS',-45),('COSTAS',180)]):
        im.paste(r.render(yaw,464,467,pitch=0,bounds=bounds),(12+i*477,116))
        text(im,(30+i*477,128),label,16,'#435974',True)
    text(im,(27,607),'Ombros aproximados do tronco; antebraços realinhados, mantendo as mãos e o livro.',18,CYAN)
    im.save(OUT/'elizabeth-arms-05.png')
    # The comparison uses the actual archived revision, with identical camera and scale.
    previous=r.ROOT.parent/'elizabeth-rework-03'/'draft'
    if previous.exists():
        im=Image.new('RGB',(1600,810),NAVY);header(im,'ANTES E DEPOIS  /  MESMA CÂMERA E ESCALA')
        bounds=(np.array([-36,289,-32]),np.array([36,373,32]))
        saved=(r.data,r.texture,r.animations)
        for col,(rev,yaw) in enumerate([(3,0),(5,0),(3,-35),(5,-35)]):
            if rev==3:
                r.data=json.loads((previous/'elizabeth.geo.json').read_text(encoding='utf-8'))['minecraft:geometry'][0]
                r.texture=np.array(Image.open(previous/'elizabeth.png').convert('RGBA'))/255
                r.animations=json.loads((previous/'elizabeth.animation.json').read_text(encoding='utf-8'))['animations']
            else:r.data,r.texture,r.animations=saved
            im.paste(r.render(yaw,388,618,head=True,pitch=0,bounds=bounds),(6+col*400,117))
            text(im,(24+col*400,127),('ANTERIOR • 03' if rev==3 else 'CONTORNO OVAL • 05'),16,'#435974',True)
        r.data,r.texture,r.animations=saved
        text(im,(26,763),'Contorno mais oval, mandíbula mais cheia e queixo sem base larga e reta; nariz corrigido mantido.',18,CYAN)
        im.save(OUT/'elizabeth-comparison-05.png')
    print('Stills saved',flush=True)

def gif(path,frames,ms):
    # A single palette prevents tonal shimmer between frames.
    sheet=Image.new('RGB',(frames[0].width*4,frames[0].height*2))
    for i,n in enumerate(np.linspace(0,len(frames)-1,8,dtype=int)):sheet.paste(frames[n],(i%4*frames[0].width,i//4*frames[0].height))
    pal=sheet.quantize(colors=256,method=Image.Quantize.MEDIANCUT)
    result=[f.quantize(palette=pal,dither=Image.Dither.NONE) for f in frames]
    result[0].save(path,save_all=True,append_images=result[1:],duration=ms,loop=0,disposal=2,optimize=False)

def animate():
    bounds=(np.array([-80,-5,-80]),np.array([80,379,80]))
    frames=[]
    for i in range(72):
        im=Image.new('RGB',(620,780),NAVY)
        im.paste(r.render(i*5,620,697,pitch=3,bounds=bounds),(0,43))
        text(im,(22,9),'ELIZABETH  /  GIRO DO MODELO',19,bold=True)
        text(im,(21,750),'Polimento 05 • geometria real • modelagem',16,CYAN)
        frames.append(im)
    gif(OUT/'elizabeth-turntable-05.gif',frames,80)
    print('Turntable saved',flush=True)
    frames=[]
    for i in range(48):
        im=Image.new('RGB',(620,780),NAVY)
        im.paste(r.render(-23,620,697,pitch=3,time=i*.1,bounds=bounds),(0,43))
        text(im,(22,9),'ELIZABETH  /  IDLE DISCRETO',19,bold=True)
        text(im,(21,750),'Respiração e cabeça • pés fixos • livro apoiado',16,CYAN)
        frames.append(im)
    gif(OUT/'elizabeth-idle-05.gif',frames,100)
    print('Idle saved',flush=True)
    frames=[]
    facebounds=(np.array([-36,289,-32]),np.array([36,373,32]))
    claybounds=(np.array([-30,289,-32]),np.array([30,347,32]))
    for i in range(64):
        yaw=64*math.sin(i/64*math.tau)
        im=Image.new('RGB',(1000,550),NAVY)
        im.paste(r.render(yaw,490,461,head=True,pitch=0,bounds=facebounds),(5,48))
        im.paste(r.render(yaw,490,461,head=True,pitch=0,bounds=claybounds,clay=True,bare=True),(505,48))
        text(im,(18,12),'ROSTO COM TEXTURA',20,bold=True)
        text(im,(518,12),'GEOMETRIA • SEM CABELO E TEXTURA',19,bold=True)
        text(im,(20,519),'Revisão 05 • contorno oval e queixo suavizado • feições modeladas em volume.',16,CYAN)
        frames.append(im)
    gif(OUT/'elizabeth-face-05.gif',frames,80)
    print('Face geometry inspection GIF saved',flush=True)

if __name__=='__main__':stills();animate()
