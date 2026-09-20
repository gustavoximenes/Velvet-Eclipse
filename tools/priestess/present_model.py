"""Actual geometry previews and concise validation for the standalone study."""
import json
from pathlib import Path
import numpy as np
from PIL import Image, ImageDraw, ImageFont
import render_model as r

OUT=r.OUT
FONT=Path('C:/Windows/Fonts')
def font(n,bold=False):
    return ImageFont.truetype(str(FONT/('segoeuib.ttf' if bold else 'segoeui.ttf')),n)

def bounds_for(clip,yaw,pitch,head=False):
    vertices=np.concatenate([q[0] for t in np.linspace(0,r.animations[clip]['animation_length'],17)
                             for q in r.surfaces(yaw,pitch,head,float(t),clip)])
    return vertices.min(axis=0)-2,vertices.max(axis=0)+2

def validate():
    geo=json.loads((OUT/'priestess.geo.json').read_text(encoding='utf-8'))['minecraft:geometry'][0]
    bb=json.loads((OUT/'priestess.bbmodel').read_text(encoding='utf-8'))
    bones=geo['bones'];names=[b['name'] for b in bones];known=set()
    assert len(set(names))==len(names)
    cubes=[]
    for b in bones:
        assert not b.get('parent') or b['parent'] in known
        known.add(b['name'])
        for c in b.get('cubes',[]):
            assert all(v==int(v) and v>=0 for v in c['size'])
            assert sum(v==0 for v in c['size'])<=1
            for u,v,w,h in r.face_uv(*c['uv'],*c['size']).values():
                assert 0<=u<=u+w<=1024 and 0<=v<=v+h<=1024
            cubes.append(c)
    assert len(cubes)==len(bb['elements'])
    assert len({e['uuid'] for e in bb['elements']})==len(cubes)
    assert len(bb['animations'])==2
    for clip,a in r.animations.items():
        assert set(a['bones'])<=known
        for tracks in a['bones'].values():
            for keys in tracks.values():
                pairs=sorted((float(k),np.array(v)) for k,v in keys.items())
                assert pairs[0][0]==0 and pairs[-1][0]==a['animation_length']
                assert all(np.isfinite(v).all() for _,v in pairs)
                assert np.allclose(pairs[0][1],pairs[-1][1]),clip
    message=(f'OK: {len(cubes)} native elements; {len(names)} named bones; integer Box UV sizes; '
             'all UVs in atlas; valid hierarchy; two clips returning to rest.\n'
             'Visual previews use the exported geometry, atlas and animation tracks.\n'
             'No mod integration or in-game validation was performed.\n')
    (OUT/'validation.txt').write_text(message,encoding='utf-8')
    print(message,flush=True)

def stills():
    sheet=Image.new('RGB',(1780,1260),'#12232e');d=ImageDraw.Draw(sheet)
    d.rectangle((30,29,36,101),fill='#9fe6d4')
    d.text((57,23),'SACERDOTISA',font=font(42,True),fill='#f1f0df')
    d.text((59,79),'ESTUDO 02  /  CABEÇA REMODELADA E TRANÇAS ARTICULADAS',font=font(17),fill='#9fe6d4')
    sheet.paste(r.render(-16,1090,730,pitch=5),(24,132))
    sheet.paste(r.render(-20,614,730,head=True,pitch=1),(1142,132))
    d=ImageDraw.Draw(sheet)
    d.text((47,151),'POSE PADRÃO',font=font(17,True),fill='#344955')
    d.text((1165,151),'ROSTO E TRANÇAS',font=font(17,True),fill='#344955')
    attack_bounds=(np.array([-40,49,-85]),np.array([40,134,20]))
    sheet.paste(r.render(-10,470,324,pitch=4,time=1.2,clip='action.attack',bounds=attack_bounds),(24,886))
    d=ImageDraw.Draw(sheet)
    d.text((47,901),'ATAQUE / GESTO FRONTAL',font=font(15,True),fill='#344955')
    d.text((533,904),'Modelo editável no Blockbench',font=font(28,True),fill='#f1f0df')
    d.text((535,953),'Rosto afunilado, nariz pequeno e máscara mais estreita.',font=font(22),fill='#c3d2d4')
    d.text((535,994),'Tranças com mechas cruzadas em relevo e pontas articuladas.',font=font(22),fill='#c3d2d4')
    d.text((535,1035),'Idle: 4,8 s  ·  Ataque: 2,8 s  ·  Textura em pixels / Box UV',font=font(22),fill='#9fe6d4')
    d.text((535,1114),'SOMENTE MODELAGEM',font=font(20,True),fill='#f1dba2')
    d.text((535,1150),'Prévia da geometria real. O mod não foi alterado.',font=font(20),fill='#c3d2d4')
    d.text((26,1222),'Ataque interpretado a partir das imagens fornecidas; não é uma captura de movimento do jogo.',font=font(16),fill='#acbfc4')
    sheet.save(OUT/'priestess-preview-02.png')
    r.render(-25,800,900,head=True,pitch=2).save(OUT/'face-three-quarter.png')
    # Sequence makes the anticipation, forward reach and return easy to inspect.
    strip=Image.new('RGB',(1800,550),'#12232e');d=ImageDraw.Draw(strip)
    d.text((24,15),'ATAQUE / ANTECIPAÇÃO, AVANÇO, IMPACTO E RETORNO',font=font(21,True),fill='#f1f0df')
    for i,(t,label) in enumerate([(.28,'ANTECIPAÇÃO'),(.7,'AVANÇO'),(1.2,'GESTO FRONTAL'),(2.25,'RETORNO')]):
        strip.paste(r.render(-15,435,440,pitch=5,time=t,clip='action.attack',bounds=attack_bounds),(15+i*448,62))
        ImageDraw.Draw(strip).text((32+i*448,513),label,font=font(15,True),fill='#9fe6d4')
    strip.save(OUT/'attack-sequence.png')
    print('Static preview and attack sequence ready.',flush=True)

def animations():
    for clip,count,ms,filename in [('misc.idle',48,100,'priestess-idle.gif'),('action.attack',35,80,'priestess-attack.gif')]:
        frames=[];bbox=bounds_for(clip,-16,5);hb=bounds_for(clip,-14,0,True)
        for i,t in enumerate(np.linspace(0,r.animations[clip]['animation_length'],count,endpoint=False)):
            image=Image.new('RGB',(1100,700),'#12232e')
            image.paste(r.render(-16,780,610,pitch=5,time=float(t),bounds=bbox,clip=clip),(10,62))
            image.paste(r.render(-14,292,610,head=True,pitch=0,time=float(t),bounds=hb,clip=clip),(798,62))
            d=ImageDraw.Draw(image)
            d.text((23,16),'SACERDOTISA / '+('IDLE' if clip=='misc.idle' else 'ATAQUE'),font=font(23,True),fill='#f1f0df')
            d.text((806,31),'ROSTO / TRANÇAS',font=font(14,True),fill='#9fe6d4')
            d.text((23,680),'Prévia do modelo real · Estudo separado do mod',font=font(13),fill='#b8cdd1')
            frames.append(image)
        # One shared palette avoids colour flickering between animation frames.
        palette=frames[len(frames)//2].quantize(colors=256,method=Image.Quantize.MEDIANCUT)
        frames=[f.quantize(palette=palette,dither=Image.Dither.NONE) for f in frames]
        frames[0].save(OUT/filename,save_all=True,append_images=frames[1:],duration=ms,loop=0,disposal=2,optimize=False)
        print(filename+' ready.',flush=True)

if __name__=='__main__':
    validate();stills();animations()
