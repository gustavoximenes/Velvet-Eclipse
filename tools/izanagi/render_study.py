"""Flat-shaded, nearest-texel preview of the draft's actual Bedrock cuboids."""
from pathlib import Path
import json,itertools,math
import numpy as np
from PIL import Image,ImageDraw,ImageFont
import animate_study
from build_study import face_uv

ROOT=Path(__file__).resolve().parent
OUT=ROOT/'draft'
data=json.loads((OUT/'izanagi.geo.json').read_text(encoding='utf-8'))['minecraft:geometry'][0]
texture=np.array(Image.open(OUT/'izanagi.png').convert('RGBA'))/255
animations=json.loads((OUT/'izanagi.animation.json').read_text(encoding='utf-8'))['animations']

def T(v):
    m=np.eye(4);m[:3,3]=v;return m
def R(v):
    x,y,z=np.radians(v);cx,sx=np.cos(x),np.sin(x);cy,sy=np.cos(y),np.sin(y);cz,sz=np.cos(z),np.sin(z)
    m=np.eye(4)
    m[:3,:3]=np.array([[cz,-sz,0],[sz,cz,0],[0,0,1]])@np.array([[cy,0,sy],[0,1,0],[-sy,0,cy]])@np.array([[1,0,0],[0,cx,-sx],[0,sx,cx]])
    return m
FACE={'north':[0,2,6,4],'south':[1,5,7,3],'west':[0,1,3,2],
      'east':[4,6,7,5],'up':[2,3,7,6],'down':[0,4,5,1]}

def sample(name,channel,time,clip='misc.idle'):
    animation=animations[clip]
    keys=animation['bones'].get(name,{}).get(channel)
    if not keys:return np.zeros(3)
    pairs=sorted((float(k),np.array(v)) for k,v in keys.items())
    time=time%animation['animation_length'] if animation['loop'] else np.clip(time,0,animation['animation_length'])
    for (a,av),(b,bv) in zip(pairs,pairs[1:]):
        if a<=time<=b:return av+(bv-av)*(time-a)/(b-a)
    return pairs[0][1]

def surfaces(yaw,pitch,head=False,time=0,clip='misc.idle'):
    mats={};quads=[];camera=R((pitch,yaw,0))
    for b in data['bones']:
        p=np.array(b['pivot'])
        world=mats.get(b.get('parent'),np.eye(4))@T(sample(b['name'],'position',time,clip))@T(p)@R(np.array(b.get('rotation',[0,0,0]))+sample(b['name'],'rotation',time,clip))@T(-p)
        mats[b['name']]=world
        if head and b['name']!='head':continue
        for c in b.get('cubes',[]):
            p=np.array(c.get('pivot',[0,0,0]));cm=camera@world@T(p)@R(c.get('rotation',[0,0,0]))@T(-p)
            verts=np.array([[*(np.array(c['origin'])+np.array(c['size'])*v),1] for v in itertools.product([0,1],repeat=3)])
            points=(cm@verts.T).T[:,:3]
            for face,ids in FACE.items():
                q=points[ids];normal=np.cross(q[1]-q[0],q[2]-q[0]);length=np.linalg.norm(normal)
                if length<1e-8:continue
                normal/=length
                if normal[2]>=0:continue
                u,v,w,h=face_uv(*c['uv'],*c['size'])[face]
                if w<=0 or h<=0:continue
                if face=='up':
                    uv=np.array([[u,v],[u,v+h],[u+w,v+h],[u+w,v]])
                elif face=='down':
                    uv=np.array([[u,v],[u+w,v],[u+w,v+h],[u,v+h]])
                elif face in ('north','east'):
                    uv=np.array([[u,v+h],[u,v],[u+w,v],[u+w,v+h]])
                else:
                    uv=np.array([[u,v+h],[u+w,v+h],[u+w,v],[u,v]])
                quads.append((q,uv,normal,b['name']))
    return quads

def render(yaw,width,height,head=False,pitch=4,time=0,bounds=None,clip='misc.idle'):
    quads=surfaces(yaw,pitch,head,time,clip)
    vertices=np.concatenate([q[0] for q in quads]);lo=vertices.min(axis=0);hi=vertices.max(axis=0)
    if bounds is not None:lo,hi=bounds
    scale=min((width-50)/(hi[0]-lo[0]),(height-44)/(hi[1]-lo[1]))
    center=(lo+hi)/2
    bg=np.array([.88,.90,.89])
    rgb=np.empty((height,width,3));rgb[:]=bg;depth=np.full((height,width),np.inf)
    light=np.array([-.42,.55,-.72]);light/=np.linalg.norm(light)
    for q,uv,normal,bone in quads:
        screen=q.copy();screen[:,0]=width/2+(q[:,0]-center[0])*scale
        screen[:,1]=height/2-(q[:,1]-center[1])*scale
        shade=.65+.35*max(0,float(normal@light))
        for ids in ([0,1,2],[0,2,3]):
            p=screen[ids];tex=uv[ids]
            x0=max(0,int(p[:,0].min()));x1=min(width,int(p[:,0].max())+1)
            y0=max(0,int(p[:,1].min()));y1=min(height,int(p[:,1].max())+1)
            if x0>=x1 or y0>=y1:continue
            a,b,c=p;den=(b[1]-c[1])*(a[0]-c[0])+(c[0]-b[0])*(a[1]-c[1])
            if abs(den)<1e-7:continue
            ys,xs=np.mgrid[y0:y1,x0:x1];xs=xs+.5;ys=ys+.5
            aa=((b[1]-c[1])*(xs-c[0])+(c[0]-b[0])*(ys-c[1]))/den
            bb=((c[1]-a[1])*(xs-c[0])+(a[0]-c[0])*(ys-c[1]))/den;cc=1-aa-bb
            z=aa*a[2]+bb*b[2]+cc*c[2]
            tx=aa*tex[0,0]+bb*tex[1,0]+cc*tex[2,0];ty=aa*tex[0,1]+bb*tex[1,1]+cc*tex[2,1]
            col=texture[np.clip(ty.astype(int),0,texture.shape[0]-1),np.clip(tx.astype(int),0,texture.shape[1]-1)]
            mask=(aa>=0)&(bb>=0)&(cc>=0)&(z<depth[y0:y1,x0:x1])&(col[:,:,3]>.5)
            if not mask.any():continue
            rgb[y0:y1,x0:x1][mask]=(col[:,:,:3]*shade)[mask];depth[y0:y1,x0:x1][mask]=z[mask]
    return Image.fromarray(np.clip(rgb*255,0,255).astype('uint8'))

def previous_main():
    f=lambda n,bold=False:ImageFont.truetype('C:/Windows/Fonts/segoeuib.ttf' if bold else 'C:/Windows/Fonts/segoeui.ttf',n)
    canvas=Image.new('RGB',(1800,1160),'#101c2d');d=ImageDraw.Draw(canvas)
    d.rectangle((35,38,42,102),fill='#55dfdf')
    d.text((61,28),'IZANAGI',font=f(43,True),fill='#f3f3e9')
    d.text((63,84),'ESTUDO VOXEL 04  /  POSTURA E DETALHES DA REFERÊNCIA',font=f(17),fill='#7cdee2')
    canvas.paste(render(-27,1120,930),(32,138))
    canvas.paste(render(-12,584,540,True,0),(1184,138))
    d=ImageDraw.Draw(canvas)
    d.text((55,154),'POSE BASE DO IDLE',font=f(16,True),fill='#4d606a')
    d.text((1200,154),'MÁSCARA',font=f(16,True),fill='#4d606a')
    d.text((1198,704),'Ajustes desta prévia',font=f(21,True),fill='#eef2ec')
    for y,a,b in [(748,'Olhos mais expostos','Pupilas maiores, voltadas ao centro.'),
                  (826,'Mãos e pés','Dedos separados e pontas metálicas.'),
                  (904,'Espada e postura','Lâmina reta e pose flutuante assimétrica.')]:
        d.text((1200,y),a,font=f(18,True),fill='#71dbdf')
        d.text((1200,y+28),b,font=f(16),fill='#b5c4ce')
    d.line((1198,982,1740,982),fill='#3b5066',width=1)
    d.text((1200,1005),'Protótipo e idle para avaliação.',font=f(18,True),fill='#f5ead1')
    d.text((1200,1036),'Ainda sem integração ou JAR.',font=f(16),fill='#b5c4ce')
    d.text((36,1100),'Render do modelo 3D provisório. Não é captura do Minecraft.',font=f(19),fill='#c0cdd6')
    path=ROOT/'izanagi-voxel-preview-04.png';canvas.save(path);print(path)
    detail=Image.new('RGB',(1500,850),'#101c2d');hd=ImageDraw.Draw(detail)
    hd.text((32,24),'IZANAGI  /  ESTUDO DA MÁSCARA',font=f(28,True),fill='#edf0e6')
    detail.paste(render(0,714,680,True,0),(28,90))
    detail.paste(render(-38,714,680,True,0),(758,90))
    hd=ImageDraw.Draw(detail)
    hd.text((47,104),'FRENTE',font=f(16,True),fill='#4d606a')
    hd.text((777,104),'TRÊS QUARTOS',font=f(16,True),fill='#4d606a')
    hd.text((32,797),'Faixa branca sobre os olhos, íris amarela, nariz em ponta e fechos nas laterais.',font=f(20),fill='#bfd0d6')
    detail.save(ROOT/'izanagi-head-preview-04.png')

def clip_settings():
    return [('misc.idle','IDLE',-27,0,'idle'),('attack.magic','MAGIA',-25,1.25,'magic'),('attack.physical','FÍSICO',-60,.83,'physical')]

def main():
    font=ImageFont.truetype('C:/Windows/Fonts/segoeuib.ttf',28)
    small=ImageFont.truetype('C:/Windows/Fonts/segoeui.ttf',18)
    canvas=Image.new('RGB',(1740,1060),'#101c2d');d=ImageDraw.Draw(canvas)
    d.text((30,22),'IZANAGI / REVISÃO 05 / POSES DAS ANIMAÇÕES',font=font,fill='#eef4ed')
    for i,(clip,label,yaw,time,slug) in enumerate(clip_settings()):
        x=24+i*576
        canvas.paste(render(yaw,552,870,time=time,clip=clip),(x,105))
        d=ImageDraw.Draw(canvas);d.text((x+18,118),label,font=small,fill='#354955')
    d.text((30,1003),'Prévia do modelo e das animações. Ainda fora do mod; sem efeitos para facilitar a avaliação das poses.',font=small,fill='#bfd0d6')
    path=ROOT/'izanagi-animation-poses-05.png';canvas.save(path);print(path,flush=True)

def animate(clip,label,yaw,time,slug):
    duration=animations[clip]['animation_length']
    # Include the whole motion in one fixed camera, rather than recentering it.
    points=np.concatenate([q[0] for t in np.arange(0,duration+.01,.1) for q in surfaces(yaw,4,time=t,clip=clip)])
    lo=points.min(axis=0)-[1,1,0];hi=points.max(axis=0)+[1,1,0]
    frames=[]
    font=ImageFont.truetype('C:/Windows/Fonts/segoeui.ttf',17)
    for i in range(round(duration/.05)):
        frame=render(yaw,760,760,time=i*.05,bounds=(lo,hi),clip=clip)
        d=ImageDraw.Draw(frame);d.text((25,22),'IZANAGI / '+label+' / PRÉVIA FORA DO MOD',font=font,fill='#354955')
        frames.append(frame)
    if not animations[clip]['loop']:frames.extend([frames[-1]]*12)
    palette=frames[0].quantize(colors=128)
    indexed=[frame.quantize(palette=palette,dither=Image.Dither.NONE) for frame in frames]
    path=ROOT/f'izanagi-{slug}-preview-05.gif'
    indexed[0].save(path,save_all=True,append_images=indexed[1:],duration=50,loop=0,optimize=False,disposal=1)
    print(path,flush=True)

if __name__=='__main__':
    import sys
    main()
    if '--animate' in sys.argv:
        for setting in clip_settings():animate(*setting)
