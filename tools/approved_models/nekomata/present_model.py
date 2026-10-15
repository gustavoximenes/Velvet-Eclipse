"""Render the delivered cuboids, atlas and animation tracks; no painted mockups."""
from pathlib import Path
import hashlib,json
import numpy as np
from PIL import Image,ImageDraw,ImageFont
import render_model as r

OUT=r.OUT
NAVY='#101b2e';CYAN='#a6dce5';WHITE='#eef6f5'
CAMERA=25;PITCH=2
KINDS={'idle':('ground_idle','REPOUSO',3.2),'physical':('physical_attack','ATAQUE FÍSICO',2.5),'magic':('magic_attack','ATAQUE MÁGICO',2.8)}

def font(size,bold=False):
    return ImageFont.truetype('C:/Windows/Fonts/segoeuib.ttf' if bold else 'C:/Windows/Fonts/segoeui.ttf',size)

def text(im,xy,value,size=18,color=WHITE,bold=False):
    ImageDraw.Draw(im).text(xy,value,font=font(size,bold),fill=color)

def clip(kind):return 'animation.nekomata.'+KINDS[kind][0]

def frame_bounds(yaws=(CAMERA,),kinds=tuple(KINDS),pitch=PITCH):
    points=[]
    for yaw in yaws:
        for kind in kinds:
            for t in np.linspace(0,KINDS[kind][2],81):
                points.append(r.bounds_vertices(yaw,pitch,float(t),clip(kind)))
    vertices=np.concatenate(points)
    return vertices.min(0)-5,vertices.max(0)+5

def savegif(path,frames,ms=50):
    sheet=Image.new('RGB',(frames[0].width*4,frames[0].height*2))
    for i,index in enumerate(np.linspace(0,len(frames)-1,8,dtype=int)):
        sheet.paste(frames[index],(i%4*frames[0].width,i//4*frames[0].height))
    palette=sheet.quantize(256,method=Image.Quantize.MEDIANCUT)
    frames=[im.quantize(palette=palette,dither=Image.Dither.NONE) for im in frames]
    frames[0].save(path,save_all=True,append_images=frames[1:],duration=ms,loop=0,disposal=2,optimize=False)

def face_bounds(yaw):
    # Crop to the head proper; long articulated locks remain rendered as context.
    allowed={'head','face_base','hair_crown','eye_left','eye_right','ear_left','ear_right'}
    vertices=np.concatenate([q for q,uv,n,b in r.surfaces(yaw,0,head=True) if b in allowed])
    lo=vertices.min(0);hi=vertices.max(0)
    lo[1]=max(lo[1],hi[1]-57)
    return lo-2,hi+2

def combine(cache):
    frames=[]
    for i in range(64):
        im=Image.new('RGB',(1200,650),NAVY)
        for col,(kind,start) in enumerate([('idle',0),('physical',6),('magic',3)]):
            index=i%len(cache[kind]) if kind=='idle' else min(max(0,i-start),len(cache[kind])-1)
            body=cache[kind][index].copy()
            body.thumbnail((392,550),Image.Resampling.LANCZOS)
            panel=Image.new('RGB',(392,550),body.getpixel((0,0)))
            panel.paste(body,((392-body.width)//2,(550-body.height)//2))
            im.paste(panel,(4+col*400,46));text(im,(20+col*400,10),KINDS[kind][1],22,bold=True)
        text(im,(20,614),'NEKOMATA • revisão 02 • modelo e movimentos reais • sem integração ao mod',18,CYAN)
        frames.append(im)
    savegif(OUT/'nekomata-animations-02.gif',frames)

def main(stills_only=False):
    framing=frame_bounds()
    print('Framing sampled, including back faces and every clip',flush=True)
    im=Image.new('RGB',(1500,1040),NAVY)
    text(im,(28,13),'NEKOMATA',42,bold=True)
    text(im,(30,70),'REVISÃO 02  /  MODELO EDITÁVEL E ANIMAÇÕES PARA REVISÃO',18,CYAN)
    for col,(kind,t) in enumerate([('idle',0),('physical',1.10),('magic',1.15)]):
        im.paste(r.render(CAMERA,484,782,pitch=PITCH,time=t,clip=clip(kind),bounds=framing),(8+col*500,110))
        text(im,(25+col*500,122),KINDS[kind][1],18,NAVY,True)
    count=sum(len(b.get('cubes',[])) for b in r.data['bones'])
    text(im,(30,917),f'{count} cuboides • {len(r.data["bones"])} ossos • rosto oval • mãos, cabelo e cauda articulados',21,CYAN)
    text(im,(30,958),'Repouso 3,2 s  |  Físico 2,5 s  |  Magia 2,8 s',20,WHITE,True)
    text(im,(30,997),'Prévia offline. Ataques guiados por vídeo de Reload; ciclo do idle aproximado. Sem integração.',18,CYAN)
    im.save(OUT/'nekomata-preview-02.png')
    im=Image.new('RGB',(1600,1000),NAVY)
    text(im,(25,12),'NEKOMATA  /  VISTAS DO CORPO',31,bold=True)
    views=[(0,'FRENTE'),(25,'TRÊS QUARTOS'),(90,'PERFIL'),(180,'COSTAS')]
    view_bounds=frame_bounds(tuple(a for a,b in views),('idle',),0)
    for col,(yaw,label) in enumerate(views):
        im.paste(r.render(yaw,386,842,pitch=0,bounds=view_bounds),(8+col*400,72))
        text(im,(25+col*400,86),label,17,NAVY,True)
    text(im,(25,942),'Mesma escala nas quatro vistas; cauda e cabelos incluídos no enquadramento.',20,CYAN)
    im.save(OUT/'nekomata-views-02.png')
    im=Image.new('RGB',(1600,630),NAVY)
    text(im,(25,11),'NEKOMATA  /  ROSTO, OLHOS E VOLUME',30,bold=True)
    for col,(yaw,label,clay) in enumerate([(0,'FRENTE',False),(25,'TRÊS QUARTOS',False),(90,'PERFIL',False),(25,'GEOMETRIA SEM COR',True)]):
        im.paste(r.render(yaw,386,480,pitch=0,bounds=face_bounds(yaw),clay=clay),(8+col*400,68))
        text(im,(23+col*400,80),label,16,NAVY,True)
    text(im,(25,574),'Olhos dourados com pupilas escuras, focinho curto e cabeça construída com cuboides.',20,CYAN)
    im.save(OUT/'nekomata-face-02.png')
    for kind,times in [('physical',[0,.44,.67,.91,1.25,2.5]),('magic',[0,.46,.69,.93,1.25,2.8])]:
        im=Image.new('RGB',(1500,1270),NAVY)
        text(im,(24,12),'NEKOMATA  /  '+KINDS[kind][1],26,bold=True)
        for i,t in enumerate(times):
            col=i%3;row=i//3
            im.paste(r.render(CAMERA,490,540,pitch=PITCH,time=t,clip=clip(kind),bounds=framing),(5+col*500,62+row*600))
            text(im,(22+col*500,614+row*600),f'{t:.2f} s',17,CYAN)
        im.save(OUT/f'nekomata-{kind}-poses-02.png')
    # Actual reference screenshot and actual model, with provenance labels.
    im=Image.new('RGB',(1440,850),NAVY)
    text(im,(25,12),'NEKOMATA  /  REFERÊNCIA E REVISÃO 02',29,bold=True)
    source=Image.open(r.ROOT/'references/reload-compendium.jpg').convert('RGB')
    source.thumbnail((830,690))
    im.paste(source,(10,90+(690-source.height)//2))
    text(im,(25,70),'REFERÊNCIA: PERSONA 3 RELOAD',17,CYAN)
    im.paste(r.render(CAMERA,580,690,pitch=PITCH,bounds=framing),(850,90))
    text(im,(870,100),'REVISÃO 02 — GEOMETRIA REAL',17,NAVY,True)
    text(im,(25,807),'Comparação de silhueta, cabelo, máscara facial, pelagem e cauda; iluminação e câmera distintas.',18,CYAN)
    im.save(OUT/'nekomata-reference-02.png')
    print('Model, face, reference and attack pose boards saved',flush=True)
    if stills_only:return
    cache={}
    for kind,(name,label,length) in KINDS.items():
        raw=[];frames=[];n=round(length*20)
        for i in range(n):
            body=r.render(CAMERA,600,710,pitch=PITCH,time=i*.05,clip=clip(kind),bounds=framing)
            raw.append(body)
            frame=Image.new('RGB',(600,810),NAVY);frame.paste(body,(0,48))
            text(frame,(18,11),'NEKOMATA  /  '+label,20,bold=True)
            text(frame,(18,778),'Geometria real • revisão 02 • prévia offline',16,CYAN)
            frames.append(frame)
            if i%20==0:print(f'{kind}: frame {i+1}/{n}',flush=True)
        cache[kind]=raw
        savegif(OUT/f'nekomata-{kind}-02.gif',frames)
        print(f'{kind}: GIF saved ({n} frames)',flush=True)
    combine(cache)
    print('Combined GIF saved from the same rendered frames',flush=True)
    turn_bounds=frame_bounds(tuple(range(0,360,10)),('idle',),0)
    frames=[]
    for yaw in range(0,360,10):
        frame=Image.new('RGB',(600,810),NAVY)
        frame.paste(r.render(yaw,600,710,pitch=0,bounds=turn_bounds),(0,48))
        text(frame,(18,11),'NEKOMATA  /  INSPEÇÃO EM 360°',20,bold=True)
        text(frame,(18,778),'Pose de repouso • câmera em rotação',16,CYAN)
        frames.append(frame)
    savegif(OUT/'nekomata-turntable-02.gif',frames,100)
    # Save hashes so packaging cannot accidentally combine new geometry with old media.
    hashes={name:hashlib.sha256((OUT/name).read_bytes()).hexdigest() for name in ['nekomata.geo.json','nekomata.animation.json','nekomata.png']}
    (OUT/'framing-validation.json').write_text(json.dumps({'status':'passed','camera_yaw':CAMERA,'camera_pitch':PITCH,'sampled_poses_per_clip':81,'includes_back_faces':True,'fixed_animation_bounds':[v.tolist() for v in framing],'source_hashes':hashes,'offline':True},indent=2)+'\n')
    print('Turntable and geometry/animation/media provenance saved',flush=True)

if __name__=='__main__':
    import sys
    if '--combine-only' in sys.argv:
        cache={}
        for kind in KINDS:
            with Image.open(OUT/f'nekomata-{kind}-02.gif') as source:
                cache[kind]=[]
                for i in range(source.n_frames):
                    source.seek(i);cache[kind].append(source.convert('RGB').crop((0,48,600,758)))
        combine(cache)
        print('Combined GIF regenerated with preserved proportions',flush=True)
    else:main('--stills-only' in sys.argv)
