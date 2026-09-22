"""Deterministic native pixel assets for the two progression item families."""
from pathlib import Path
import json
from PIL import Image, ImageDraw
ROOT=Path(__file__).resolve().parents[2]/'src/main/resources/assets/velvet_eclipse'
langs={code:json.loads((ROOT/'lang'/f'{code}.json').read_text(encoding='utf-8')) for code in ['pt_br','en_us']}
categories={'general':('Progressão e ferramentas','Progression & tools'),'skill_cards':('Skill Cards','Skill Cards'),'enemies':('Modelos de inimigos','Enemy models'),'personas':('Modelos de Personas','Persona models'),'npcs':('Personagens','Characters'),'blocks':('Blocos','Blocks'),'props':('Objetos do Tártaro','Tartarus objects'),'weapons':('Armas','Weapons'),'armor':('Armaduras','Armor'),'footwear':('Calçados','Footwear'),'accessories':('Acessórios','Accessories'),'supplies':('Consumíveis','Consumables')}
for key,(pt,en) in categories.items():
 langs['pt_br']['itemGroup.velvet_eclipse.'+key]='Velvet Eclipse • '+pt
 langs['en_us']['itemGroup.velvet_eclipse.'+key]='Velvet Eclipse • '+en
for floor in [0]+list(range(25,251,25)):
 name='igor_contract' if floor==0 else f'purchase_permit_{floor}'
 im=Image.new('RGBA',(32,32));d=ImageDraw.Draw(im)
 if floor==0:
  d.polygon([(5,2),(23,2),(27,6),(27,28),(6,28),(4,25)],fill='#14234f',outline='#87dbec')
  d.rectangle((7,4,23,24),fill='#d4e8ed');d.line((7,5,23,5),fill='#809cae');d.polygon([(23,2),(23,7),(27,7)],fill='#537fbb')
  d.ellipse((10,6,20,12),fill='#143d9b',outline='#658eec');d.line((15,7,15,11),fill='#edf8ff',width=2)
  for y,w in [(15,11),(18,10),(21,7)]:d.line((9,y,9+w,y),fill='#42698b')
  d.polygon([(20,24),(27,24),(27,31),(23,29),(20,31)],fill='#194bd6')
  d.ellipse((19,21,28,28),fill='#bd914b',outline='#fff2b1');d.line((22,23,25,26),fill='#fff2b1',width=2)
 else:
  d.polygon([(3,3),(26,3),(29,6),(29,28),(3,28)],fill='#07142f',outline='#dda857')
  d.rectangle((5,5,26,26),fill='#153b94',outline='#4ba6d9');d.polygon([(5,5),(26,5),(24,12),(5,14)],fill='#2edcda')
  d.line((8,8,12,8),fill='#08193e',width=2);d.line((10,6,10,11),fill='#08193e')
  d.rectangle((8,15,23,16),fill='#d6eeee');d.line((8,19,19,19),fill='#8cbadd')
  # Ten small holes are a legible tier code at Minecraft's native item resolution.
  for i in range(floor//25):
   x=7+(i%5)*4;y=22+(i//5)*3;d.rectangle((x,y,x+1,y+1),fill='#ffe6a2')
 im.save(ROOT/'textures/item'/f'{name}.png')
 (ROOT/'models/item'/f'{name}.json').write_text(json.dumps({'parent':'minecraft:item/generated','textures':{'layer0':f'velvet_eclipse:item/{name}'}},indent=2)+'\n',encoding='utf-8')
 langs['pt_br']['item.velvet_eclipse.'+name]='Contrato de Igor' if floor==0 else f'Permissão de Compra • {floor}F'
 langs['en_us']['item.velvet_eclipse.'+name]="Igor's Contract" if floor==0 else f'Purchase Permit • {floor}F'
for code,content in langs.items():(ROOT/'lang'/f'{code}.json').write_text(json.dumps(content,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print('11 item models/textures and creative category names generated.')
