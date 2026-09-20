"""Code-authored pixel Skill Cards and creative block item models."""
from pathlib import Path
import json,re,struct,zlib
p=Path(__file__).resolve().parents[1];a=p/'src/main/resources/assets/velvet_eclipse'
def write(path,obj):path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(obj,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
def png(path,pixels):
    def chunk(k,d):return struct.pack('!I',len(d))+k+d+struct.pack('!I',zlib.crc32(k+d)&0xffffffff)
    raw=b''.join(b'\0'+bytes(v for pixel in row for v in pixel) for row in pixels);path.parent.mkdir(parents=True,exist_ok=True)
    path.write_bytes(b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',struct.pack('!2I5B',32,32,8,6,0,0,0))+chunk(b'IDAT',zlib.compress(raw))+chunk(b'IEND',b''))
source=(p/'src/main/java/com/gx/velveteclipse/core/persona/PersonaRegistry.java').read_text(encoding='utf8')
skills=re.findall(r'new SkillDefinition\("([^"]+)",\s*"([^"]+)",\s*Element\.([A-Z_]+)',source)
colors={'FIRE':(255,81,68),'ICE':(79,208,254),'ELECTRIC':(255,225,55),'WIND':(74,241,139),'DARK':(183,103,243),'LIGHT':(240,255,196),'HEALING':(66,241,201),'SUPPORT':(92,149,255),'PHYSICAL':(255,132,63)}
for i,(skill,name,element) in enumerate(skills):
    color=colors.get(element,(236,111,201));pixels=[]
    for y in range(32):
        row=[]
        for x in range(32):
            c=(0,0,0,0)
            if 5<=x<=26 and 1<=y<=30:
                c=(*color,255) if x in (5,6,25,26) or y in (1,2,29,30) else (194,225,241,255) if x in (7,24) or y in (3,28) else (19+((x+y)%4)*4,36,73,255)
                # Stylized sword/star seal, with a distinct register along the base.
                if abs(x-16)+abs(y-13)<=7:c=(*color,255)
                if x in (15,16) and 6<=y<=20 or y==17 and 12<=x<=19:c=(246,255,255,255)
                if y in (24,25) and 9<=x<23 and ((i+1)>>(x-9)//2)&1:c=(*color,255)
            row.append(c)
        pixels.append(row)
    png(a/'textures/item'/f'skill_card_{skill}.png',pixels)
    write(a/'models/item'/f'skill_card_{skill}.json',{'parent':'minecraft:item/generated','textures':{'layer0':f'velvet_eclipse:item/skill_card_{skill}'}})
blocks=sorted(path.stem for path in (a/'blockstates').glob('tartarus_*.json'))
for block in blocks:write(a/'models/item'/f'{block}.json',{'parent':f'velvet_eclipse:block/{block}'})
names={'stone':'Pedra','trim':'Moldura','light':'Luz','stairs':'Escada','portal':'Portal de entrada','back':'Portal de retorno','forward':'Portal para o próximo andar','exit':'Portal de saída','summit':'Portal do último andar','blue_stairs':'Escadaria azul','ivory':'Mármore marfim','tile':'Piso azul','tile_dark':'Piso escuro','wall':'Parede','column':'Coluna','gold':'Ornamento dourado','carpet':'Tapete azul','clock':'Mostrador do relógio','hand':'Ponteiro do relógio','border':'Borda do piso','seal':'Selo','window':'Vitral verde','azure':'Vitral azul','violet':'Vitral violeta'}
for lang in ['pt_br','en_us']:
    file=a/'lang'/f'{lang}.json';data=json.loads(file.read_text(encoding='utf8'))
    data['key.velvet_eclipse.shuffle']='Shuffle Time'
    for block in blocks:data['block.velvet_eclipse.'+block]='Tartarus • '+(names.get(block[9:],block) if lang=='pt_br' else block[9:].replace('_',' ').title())
    for skill,name,_ in skills:data['item.velvet_eclipse.skill_card_'+skill]='Skill Card • '+name
    write(file,data)
print(f'{len(skills)} Skill Cards, {len(blocks)} creative block models and translations generated.')
