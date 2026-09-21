"""Original pixel glyphs for the elemental consumables; no downloaded game art."""
from pathlib import Path
import json
from PIL import Image,ImageDraw
root=Path(__file__).resolve().parents[2]/'src/main/resources/assets/velvet_eclipse'
colors={'fire':(255,76,92),'ice':(98,229,255),'electric':(255,230,82),'wind':(79,255,157),'light':(252,254,221),'dark':(165,96,241),'almighty':(232,189,254)}
items={'agilao_gem':('Agilao Gem','fire'),'maragi_gem':('Maragi Gem','fire'),'bufula_gem':('Bufula Gem','ice'),'mabufu_gem':('Mabufu Gem','ice'),'zionga_gem':('Zionga Gem','electric'),'mazio_gem':('Mazio Gem','electric'),'garula_gem':('Garula Gem','wind'),'magaru_gem':('Magaru Gem','wind'),'kouga_gem':('Kouga Gem','light'),'makouha_gem':('Makouha Gem','light'),'eiga_gem':('Eiga Gem','dark'),'maeiha_gem':('Maeiha Gem','dark'),'megido_gem':('Megido Gem','almighty'),'megidolaon_gem':('Megidolaon Gem','almighty'),'flame_magatama':('Flame Magatama','fire'),'frigid_magatama':('Frigid Magatama','ice'),'shock_magatama':('Shock Magatama','electric'),'gale_magatama':('Gale Magatama','wind'),'holy_magatama':('Holy Magatama','light'),'malice_magatama':('Malice Magatama','dark')}
for ident,(name,element) in items.items():
 im=Image.new('RGBA',(32,32));d=ImageDraw.Draw(im);color=colors[element]
 if ident.endswith('gem'):
  d.polygon([(10,4),(23,4),(29,14),(16,29),(3,14)],fill=(12,23,62),outline=(186,219,255),width=1)
  d.polygon([(10,6),(22,6),(26,14),(16,26),(6,14)],fill=color)
  d.polygon([(10,6),(16,11),(6,14)],fill=(235,254,255));d.polygon([(16,11),(26,14),(16,26)],fill=tuple(int(c*.67) for c in color));d.line([(6,14),(26,14)],fill=(240,255,255))
 else:
  d.ellipse((4,3,26,25),fill=(12,23,62),outline=(197,224,255));d.ellipse((6,5,24,23),fill=color);d.polygon([(24,14),(28,27),(14,29),(8,23),(18,21)],fill=color);d.ellipse((10,8,15,13),fill=(7,18,45));d.line([(9,16),(11,20),(15,22),(21,23)],fill=(243,255,249),width=2)
 im.save(root/'textures/item'/f'{ident}.png')
 (root/'models/item'/f'{ident}.json').write_text(json.dumps({'parent':'minecraft:item/generated','textures':{'layer0':f'velvet_eclipse:item/{ident}'}})+'\n',encoding='utf-8')
for lang in ['pt_br','en_us']:
 p=root/'lang'/f'{lang}.json';data=json.loads(p.read_text(encoding='utf-8'))
 for ident,(name,_) in items.items():data['item.velvet_eclipse.'+ident]=name
 p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print('20 elemental consumables: creative models, textures and names generated.')
