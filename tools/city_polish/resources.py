from pathlib import Path
import json,gzip
from PIL import Image
R=Path(__file__).resolve().parents[2];A=R/'src/main/resources/assets/velvet_eclipse'
def put(path,data):
    p=A/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
Image.new('RGBA',(16,16),(255,255,255,255)).save(A/'textures/block/city_neutral.png')
tex={f'm{i}':'velvet_eclipse:block/city_neutral' for i in range(28)}
for i in range(8):tex[f'm{i+10}']=f'velvet_eclipse:block/city_mall_{i}';tex[f'm{i+20}']=f'velvet_eclipse:block/city_dorm_{i}'
tex['particle']='velvet_eclipse:block/city_dorm_0'
put(Path('models/block/city_architecture.json'),{'loader':'velvet_eclipse:city_architecture','ambientocclusion':False,'textures':tex})
put(Path('blockstates/city_architecture.json'),{'variants':{'':{'model':'velvet_eclipse:block/city_architecture'}}})
put(Path('models/item/city_architecture.json'),{'parent':'velvet_eclipse:block/city_dorm_0'})
names={'dorm':['Tijolo do dormitório','Madeira do dormitório','Carpete do dormitório','Papel de parede do dormitório','Telhado do dormitório','Azulejo do dormitório','Cantaria do dormitório','Estofado do dormitório'],'mall':['Tijolo do Paulownia','Persiana do Paulownia','Pavimento do Paulownia','Revestimento do Paulownia','Cobertura do Paulownia','Madeira do Paulownia','Pedra do Paulownia','Azulejo do Paulownia']}
for district,values in names.items():
    for i,name in enumerate(values):
        block=f'city_{district}_{i}'
        put(Path(f'models/block/{block}.json'),{'parent':'minecraft:block/cube_all','textures':{'all':f'velvet_eclipse:block/{block}'}})
        put(Path(f'models/item/{block}.json'),{'parent':f'velvet_eclipse:block/{block}'})
        put(Path(f'blockstates/{block}.json'),{'variants':{'':{'model':f'velvet_eclipse:block/{block}'}}})
for locale in ['pt_br','en_us']:
    p=A/'lang'/f'{locale}.json';data=json.loads(p.read_text(encoding='utf8'));data['block.velvet_eclipse.city_architecture']='Arquitetura detalhada da cidade' if locale=='pt_br' else 'Detailed city architecture'
    for district,values in names.items():
        for i,name in enumerate(values):data[f'block.velvet_eclipse.city_{district}_{i}']=name if locale=='pt_br' else f'{district.title()} material {i+1}'
    put(Path(f'lang/{locale}.json'),data)
