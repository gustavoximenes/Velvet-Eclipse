"""Monorail pixel materials and conversion of the approved Priestess study."""
from pathlib import Path
import json,shutil
from PIL import Image,ImageDraw
ROOT=Path(__file__).resolve().parents[2];A=ROOT/'src/main/resources/assets/velvet_eclipse';DATA=ROOT/'src/main/resources/data/velvet_eclipse'
def write(path,data):path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
colors={'floor':('#566862','#3c4b48','#8a9890'),'wall':('#a0a98e','#747f73','#cad0b0'),'metal':('#45554f','#283b36','#7d8d7d'),'window':('#123b30','#061e1a','#487b4f'),'seat':('#164c49','#0c302f','#337466'),'lamp':('#d6ebc2','#8daf84','#eefbd4'),'rail':('#83908d','#536761','#bfcabc')}
for name,(base,shadow,light) in colors.items():
 im=Image.new('RGB',(32,32),base);d=ImageDraw.Draw(im)
 d.rectangle((0,0,31,31),outline=shadow);d.line((1,1,30,1),fill=light)
 if name=='floor':
  for y in range(3,32,4):d.line((1,y,30,y),fill=shadow)
  d.line((2,29,29,29),fill=light)
 elif name=='window':
  d.rectangle((0,0,31,31),fill=base)
  d.polygon([(0,16),(16,0),(26,0),(0,26)],fill='#1b4436')
  d.line((0,15,15,0),fill='#2e5741');d.line((18,31,31,18),fill='#204a39')
 elif name=='seat':
  for x in (7,15,23):d.line((x,2,x,29),fill=shadow);d.line((x+1,2,x+1,29),fill=light)
  d.line((1,27,30,27),fill=light)
 elif name=='wall':
  d.rectangle((2,2,29,21),outline=shadow);d.rectangle((1,24,30,28),fill='#647865');d.rectangle((23,5,26,13),fill='#52685b')
 elif name=='lamp':
  for y in (5,10,15,20,25):d.line((2,y,29,y),fill=light)
 else:
  for x,y in [(3,3),(28,3),(3,28),(28,28)]:d.rectangle((x,y,x+1,y+1),fill=light)
 path=A/'textures/block'/('train_'+name+'.png');path.parent.mkdir(parents=True,exist_ok=True);im.save(path)
 block={'parent':'minecraft:block/cube_all','textures':{'all':'velvet_eclipse:block/train_'+name}}
 if name=='rail':block={'textures':{'all':'velvet_eclipse:block/train_rail','particle':'velvet_eclipse:block/train_rail'},'elements':[{'from':[6,0,6],'to':[10,16,10],'faces':{f:{'texture':'#all'} for f in ['north','south','east','west','up','down']}}]}
 write(A/'models/block'/('train_'+name+'.json'),block)
 write(A/'blockstates'/('train_'+name+'.json'),{'variants':{'':{'model':'velvet_eclipse:block/train_'+name}}})
 write(A/'models/item'/('train_'+name+'.json'),{'parent':'velvet_eclipse:block/train_'+name})

draft=ROOT/'tools/priestess/draft'
g=json.loads((draft/'priestess.geo.json').read_text(encoding='utf-8'))
def uv(u,v,w,h,d):return {'north':(u+d,v+d,w,h),'south':(u+2*d+w,v+d,w,h),'west':(u+d+w,v+d,d,h),'east':(u,v+d,d,h),'up':(u+d,v,w,d),'down':(u+d+w,v,w,d)}
for b in g['minecraft:geometry'][0]['bones']:
 b['pivot'][0]*=-1
 if 'rotation' in b:b['rotation']=[-b['rotation'][0],-b['rotation'][1],b['rotation'][2]]
 for c in b.get('cubes',[]):
  c['origin'][0]=-c['origin'][0]-c['size'][0]
  if 'pivot' in c:c['pivot'][0]*=-1
  if 'rotation' in c:c['rotation']=[-c['rotation'][0],-c['rotation'][1],c['rotation'][2]]
  if 0 in c['size']:
   c['uv']={f:{'uv':[u,v],'uv_size':[w,h]} for f,(u,v,w,h) in uv(*c['uv'],*c['size']).items() if w and h}
   c['size']=[.08 if x==0 else x for x in c['size']]
animation=json.loads((draft/'priestess.animation.json').read_text(encoding='utf-8'))
for clip in animation['animations'].values():
 for tracks in clip['bones'].values():
  for channel,keys in tracks.items():
   for t,v in keys.items():
    if channel=='rotation':keys[t]=[-v[0],-v[1],v[2]]
    elif channel=='position':keys[t]=[-v[0],v[1],v[2]]
write(A/'geo/entity/priestess.geo.json',g);write(A/'animations/entity/priestess.animation.json',animation)
shutil.copy2(draft/'priestess.png',A/'textures/entity/priestess.png')
# Inventory icon is a downsample of the authored head render, with its flat preview background removed.
icon=Image.open(draft/'face.png').convert('RGBA');bg=icon.getpixel((0,0));pix=icon.load()
for y in range(icon.height):
 for x in range(icon.width):
  if pix[x,y][:3]==bg[:3]:pix[x,y]=(0,0,0,0)
icon.thumbnail((48,64),Image.Resampling.NEAREST);canvas=Image.new('RGBA',(64,64));canvas.alpha_composite(icon,((64-icon.width)//2,0));canvas.save(A/'textures/item/priestess_figure.png')
write(A/'models/item/priestess_figure.json',{'parent':'minecraft:item/generated','textures':{'layer0':'velvet_eclipse:item/priestess_figure'}})
card=json.loads((A/'models/item/skill_card_frost_shard.json').read_text(encoding='utf-8'));write(A/'models/item/skill_card_raid_ice_storm.json',card)
names={'floor':'Piso do Monorail','wall':'Parede do Monorail','window':'Janela do Monorail','seat':'Banco do Monorail','metal':'Metal do Monorail','lamp':'Luminária do Monorail','rail':'Barra do Monorail'}
for lang in ['pt_br','en_us']:
 path=A/'lang'/f'{lang}.json';data=json.loads(path.read_text(encoding='utf-8-sig'))
 data.update({'block.velvet_eclipse.train_'+k:v if lang=='pt_br' else 'Monorail '+k.title() for k,v in names.items()})
 data['item.velvet_eclipse.priestess_figure']='Figura da Sacerdotisa' if lang=='pt_br' else 'Priestess Figure'
 data['entity.velvet_eclipse.priestess']='Sacerdotisa' if lang=='pt_br' else 'Priestess';write(path,data)
dim=json.loads((DATA/'dimension/tartarus.json').read_text(encoding='utf-8'));dim['type']='velvet_eclipse:monorail';write(DATA/'dimension/monorail.json',dim)
typ=json.loads((DATA/'dimension_type/tartarus.json').read_text(encoding='utf-8'));typ['ambient_light']=.25;write(DATA/'dimension_type/monorail.json',typ)
print('Monorail materials, creative items, dimension and approved Priestess exported.')
