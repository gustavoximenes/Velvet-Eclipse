from pathlib import Path
from PIL import Image,ImageDraw,ImageFont
import sys,json,zipfile
import numpy as np
work=Path(sys.argv[1]);out=Path(sys.argv[2]);out.mkdir(parents=True,exist_ok=True)
integrated='--integrated' in sys.argv
def font(size,bold=False):return ImageFont.truetype('C:/Windows/Fonts/'+('arialbd.ttf' if bold else 'arial.ttf'),size)
bg='#0b1720';ink='#e1eee5';dim='#9abbb3';accent='#72df9c'
sheet=Image.new('RGB',(1700,1200),bg);d=ImageDraw.Draw(sheet)
d.text((30,22),'TÁRTARO / EXTERIOR',font=font(34,True),fill=ink)
d.text((32,69),'VERSÃO 0.6.49 • geometria aprovada integrada ao mod' if integrated else 'AUDITORIA 01 • primeira proposta em geometria 3D • para revisão',font=font(21),fill=accent)
whole=Image.open(work/'frames/torre.png');sheet.paste(whole.resize((873,960)),(18,128))
for i,(name,title) in enumerate([('entrada','01 / Relógio e acesso ao hall'),('relogios','02 / Volumes, relógios e faixas externas')]):
    shot=Image.open(work/'frames'/f'{name}.png');shot=shot.crop((0,250,1000,930)).resize((744,506))
    x,y=925,134+i*507;sheet.paste(shot,(x,y));d.rectangle((x,y,x+744,y+34),fill=bg);d.text((x+8,y+5),title,font=font(20,True),fill=ink)
d.text((32,1126),'Torre de 237 blocos • entrada alinhada ao campus • emissão separada da cor dos materiais',font=font(20),fill=dim)
d.text((32,1165),'Render offline da geometria exportada. Não é captura do Minecraft; iluminação ilustrativa.' if integrated else 'Render offline. Não é captura do Minecraft; exterior ainda não integrado. Iluminação e escala são adaptações.',font=font(18),fill=dim)
sheet.save(out/'tartaro-exterior-01.png')
for name in ['torre','entrada','relogios']:
    im=Image.open(work/'frames'/f'{name}.png');im.save(out/f'{name}.png')
frames=[]
for shot,label in [('orbita','Silhueta / volumes deslocados'),('entrada','Entrada / relógio e portal')]:
    for path in sorted((work/'frames').glob(f'tower-{shot}-*.png')):
        im=Image.open(path).convert('RGB').resize((540,660));dr=ImageDraw.Draw(im)
        dr.rectangle((0,0,540,42),fill=bg);dr.text((14,11),label,font=font(19,True),fill=ink)
        dr.rectangle((0,630,540,660),fill=bg);dr.text((13,636),'Geometria integrada • render offline • 0.6.49' if integrated else 'Prévia 3D offline • exterior ainda não integrado',font=font(15),fill=dim);frames.append(im)
palette=Image.new('RGB',(540,660*8))
for i in range(8):palette.paste(frames[round(i*(len(frames)-1)/7)],(0,i*660))
palette=palette.quantize(colors=256)
movie=[f.quantize(palette=palette,dither=Image.Dither.NONE) for f in frames]
movie[0].save(out/'tartaro-exterior-01.gif',save_all=True,append_images=movie[1:],duration=95,loop=0,optimize=False)
if integrated:
    (out/'validacao-geometria.json').write_bytes((work/'validation.json').read_bytes())
    print('Production geometry previews:',len(frames),'frames');sys.exit(0)
# Portable geometry: vertex-colour OBJ, plus original binary and reproducible authoring sources.
raw=np.fromfile(work/'tower.bin',dtype='<f4').reshape(-1,11)
with (out/'tartaro-exterior-01.obj').open('w',encoding='utf8') as f:
    f.write('# Exterior study 01; 1 unit = 1 block; vertex colours\n')
    for p in raw:f.write('v '+' '.join(f'{v:.5f}' for v in [*p[:3],*p[6:9]])+'\n')
    for i in range(0,len(raw),3):f.write(f'f {i+1} {i+2} {i+3}\n')
report={'status':'prévia para revisão, não integrada','triangles':len(raw)//3,'frames':len(frames),'heightBlocks':237,'references':[{'url':'https://p3re.jp/en/','use':'entrada oficial de Reload; relógio acima do portão e atmosfera verde'},{'url':'https://megatenwiki.com/wiki/Tartarus','use':'silhueta clássica da torre; fonte comparativa, não prova de cada detalhe de Reload'}],'adaptations':['Altura limitada ao espaço disponível na cidade','Partes altas e lados sem levantamento completo são proposta autoral','Relógios e faixas remontados em geometria para futura divisão em blocos locais'],'minecraftValidated':False,'shaderPackValidated':False}
(out/'auditoria.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
with zipfile.ZipFile(out/'tartaro-exterior-01-modelagem.zip','w',zipfile.ZIP_DEFLATED) as z:
    for p in Path(__file__).parent.iterdir():
        if p.is_file() and p.suffix in ['.py','.java','.png']:z.write(p,'fontes/'+p.name)
    for name in ['tower.bin','tower.json']:z.write(work/name,'geometria/'+name)
    for name in ['auditoria.json','tartaro-exterior-01.obj']:z.write(out/name,name)
print('Tower preview packaged:',len(raw)//3,'triangles,',len(frames),'frames')
