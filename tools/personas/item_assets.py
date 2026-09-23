"""Native pixel item art following the existing Skill Card palette and layout."""
from pathlib import Path
import json
from PIL import Image,ImageDraw
P=Path(__file__).resolve().parents[2]
A=P/'src/main/resources/assets/velvet_eclipse'
SKILLS={'agi':('Agi','#ff5144'),'eiga':('Eiga','#cf304f'),'arsene_cleave':('Cleave','#ff843f'),
 'sukunda':('Sukunda','#5c95ff'),'dream_needle':('Dream Needle','#ffb12d'),
 'adverse_resolve':('Adverse Resolve','#5c95ff'),'bash':('Bash','#ff843f'),'tarunda':('Tarunda','#5c95ff')}
def write(path,obj):path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(obj,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def model(name):write(A/'models/item'/(name+'.json'),{'parent':'minecraft:item/generated','textures':{'layer0':'velvet_eclipse:item/'+name}})
for index,(id,(name,color)) in enumerate(SKILLS.items()):
 im=Image.new('RGBA',(32,32));d=ImageDraw.Draw(im)
 d.rectangle((5,1,26,30),fill=color);d.rectangle((7,3,24,28),fill='#c2e1f1');d.rectangle((8,4,23,27),fill='#132449')
 d.polygon([(16,6),(23,13),(16,20),(9,13)],fill=color);d.rectangle((15,6,16,20),fill='#f6ffff');d.rectangle((12,17,19,18),fill='#f6ffff')
 for bit in range(7):
  if (index+1)>>bit&1:d.rectangle((9+bit*2,24,10+bit*2,25),fill=color)
 im.save(A/'textures/item'/('skill_card_'+id+'.png'));model('skill_card_'+id)
for id in ['arsene','orpheus']:
 im=Image.new('RGBA',(32,32));d=ImageDraw.Draw(im)
 d.polygon([(16,1),(29,15),(16,30),(3,15)],fill='#132449',outline='#55dcf1')
 if id=='arsene':
  d.polygon([(5,12),(11,14),(10,4),(20,4),(19,14),(26,11),(22,17),(9,17)],fill='#152c3e',outline='#9ab3bc')
  d.line((10,15,21,15),fill='#ff3b4c',width=2);d.polygon([(11,17),(21,17),(17,25),(13,22)],fill='#711a34');d.line((12,19,15,20),fill='#ff5948');d.line((18,20,20,19),fill='#ff5948')
 else:
  d.line((8,8,11,24,22,24,25,8),fill='#d4e2dc',width=3);d.line((9,8,24,8),fill='#f3f3dc',width=3)
  for x in range(12,23,2):d.line((x,10,x,22),fill='#91c7d8')
  d.polygon([(12,5),(16,2),(21,5),(19,8),(14,8)],fill='#d4e2dc');d.rectangle((12,24,21,26),fill='#70b9bf')
 im.save(A/'textures/item'/(id+'_model.png'));model(id+'_model')
for lang in ['pt_br','en_us']:
 path=A/'lang'/(lang+'.json');data=json.loads(path.read_text(encoding='utf-8'))
 for id,(name,_) in SKILLS.items():data['item.velvet_eclipse.skill_card_'+id]='Skill Card • '+name
 data['item.velvet_eclipse.skill_card_ember']='Skill Card • Agi'
 for id,name in [('arsene','Arsène'),('orpheus','Orpheus')]:
  data['item.velvet_eclipse.'+id+'_model']=('Modelo • ' if lang=='pt_br' else 'Model • ')+name
  data['entity.velvet_eclipse.persona_'+id]=name
 write(path,data)
print('8 Skill Cards and 2 model items with names, textures and creative categories.')
