"""Original native-cuboid props with packed Box UVs and editable Blockbench sources."""
from pathlib import Path
import json,uuid,base64,math,shutil
from PIL import Image,ImageDraw
ROOT=Path(__file__).resolve().parents[2]; A=ROOT/'src/main/resources/assets/velvet_eclipse';D=Path(__file__).resolve().parent/'weapons';D.mkdir(exist_ok=True)
palette={'steel':(162,193,211),'edge':(225,243,249),'dark':(30,48,74),'blue':(22,57,145),'gold':(198,157,65),'leather':(49,38,38),'red':(140,41,53),'string':(217,222,194),'wood':(75,50,38)}
models={}
def model(name):models[name]=[];return models[name]
def box(m,name,x,y,z,w,h,d,material):m.append(dict(name=name,origin=[x,y,z],size=[w,h,d],material=material))
for name in ['sword','greatsword','rapier','knife']:
 m=model(name);wide,length={'sword':(2,21),'greatsword':(4,28),'rapier':(1,24),'knife':(2,11)}[name]
 box(m,'wrapped_grip',-1,-4,-1,2,7,2,'blue');box(m,'pommel',-1.5,-5,-1.5,3,2,3,'gold')
 box(m,'crossguard',-wide-2,2,-1.5,wide*2+4,1,3,'gold')
 box(m,'blade',-wide/2,3,-.5,wide,length,1,'steel');box(m,'bright_edge',wide/2-.25,3,-.7,.5,length,.5,'edge')
 box(m,'ridge',-.25,3,-.75,.5,length-2,.5,'edge');box(m,'point',-.5,length+3,-.5,1,2,1,'edge')
 for y in range(-3,2,2):box(m,'grip_wrap',-1.1,y,-1.1,2.2,.5,2.2,'dark')
 if name=='rapier':
  for x in [-3,3]:box(m,'basket_guard',x,0,-2,.5,5,4,'gold')
  box(m,'basket_bottom',-3,0,-2,6,.5,4,'gold')
 if name=='greatsword':box(m,'blade_emblem',-1,4,-1,2,4,1,'blue')
m=model('spear');box(m,'shaft',-.5,-13,-.5,1,41,1,'wood');box(m,'grip',-1,-5,-1,2,9,2,'blue');box(m,'ferrule',-1,-14,-1,2,2,2,'gold');box(m,'collar',-2,25,-1,4,2,2,'gold');box(m,'spearhead',-1,27,-.5,2,9,1,'steel');box(m,'spearpoint',-.5,36,-.5,1,3,1,'edge');box(m,'blade_left',-2,28,-.5,1,6,1,'edge');box(m,'blade_right',1,28,-.5,1,6,1,'edge')
m=model('bow');box(m,'grip',-.75,-3,-1,1.5,6,2,'leather')
for sign in [-1,1]:
 for i in range(6):
  y=sign*(3+i*2.5);x=(i/5)**1.5*5
  box(m,'recurve_limb',x-.7,y-1.5,-.7,1.4,3,1.4,'blue' if i<4 else 'gold')
 box(m,'limb_tip',3.5,sign*17-1,-.6,2,2,1.2,'gold')
box(m,'taut_string',4.4,-17,-.15,.3,34,.3,'string');box(m,'arrow_shaft',-7,-.3,-.3,17,.6,.6,'wood');box(m,'arrow_head',-10,-.6,-.6,3,1.2,1.2,'steel');box(m,'fletching',8,-1,-.2,2,2,.4,'edge')
m=model('fists');box(m,'padded_glove',-2.5,-3,-2,5,3.5,4,'blue');box(m,'wrist_guard',-2.75,-4,-2.25,5.5,2,4.5,'gold');box(m,'knuckle_plate',-2.5,.5,-2.5,5,2,5,'steel');box(m,'thumb',2,-1,-1,2,3,2,'leather')
for x in [-1.75,-.5,.75,2]:box(m,'knuckle',x-.4,2,-2.2,.8,1,1.2,'gold')
m=model('gun');box(m,'pistol_grip',-1,-2,-1,2,5,2,'blue');box(m,'receiver',-1.5,1,-5,3,3,8,'steel');box(m,'barrel',-.75,1.5,-13,1.5,1.5,8,'dark');box(m,'muzzle',-1,1.25,-14,2,2,1,'gold');box(m,'sight',-.5,0,-5,1,1,2,'gold')
m=model('axe');box(m,'shaft',-.75,-7,-.75,1.5,32,1.5,'wood');box(m,'grip',-1,-6,-1,2,9,2,'blue');box(m,'head',-1.5,20,-1.5,3,7,3,'gold');box(m,'blade',1.5,18,-.7,7,11,1.4,'steel');box(m,'cutting_edge',8,17,-.5,1,13,1,'edge')
def uvfaces(u,v,w,h,d):return {'north':(u+d,v+d,w,h),'south':(u+2*d+w,v+d,w,h),'west':(u+d+w,v+d,d,h),'east':(u,v+d,d,h),'up':(u+d,v,w,d),'down':(u+d+w,v,w,d)}
for name,cubes in models.items():
 atlas=Image.new('RGBA',(128,128),(0,0,0,0));draw=ImageDraw.Draw(atlas);x=y=row=1
 for c in cubes:
  w,h,d=c['size'];uw=math.ceil(2*(w+d));uh=math.ceil(h+d)
  if x+uw+1>128:x=1;y+=row+1;row=1
  assert y+uh<128,(name,c['name']);c['uv']=[x,y];color=palette[c['material']]
  draw.rectangle((x,y,x+uw,y+uh),fill=color+(255,));draw.line((x,y+uh,x+uw,y+uh),fill=tuple(int(v*.65) for v in color)+(255,));draw.line((x,y,x+uw,y),fill=tuple(min(255,int(v*1.1)) for v in color)+(255,))
  x+=uw+2;row=max(row,uh)
 texture=A/'textures/entity'/('weapon_'+name+'.png');atlas.save(texture);shutil.copy2(texture,D/(name+'.png'))
 runtime=A/'weapons'/f'{name}.json';runtime.parent.mkdir(exist_ok=True);runtime.write_text(json.dumps({'cubes':cubes},indent=2),encoding='utf-8')
 geo={'format_version':'1.12.0','minecraft:geometry':[{'description':{'identifier':'geometry.weapon_'+name,'texture_width':128,'texture_height':128,'visible_bounds_width':6,'visible_bounds_height':6,'visible_bounds_offset':[0,1,0]},'bones':[{'name':'grip','pivot':[0,0,0],'cubes':[{k:c[k] for k in ['origin','size','uv']} for c in cubes]}]}]}
 (D/(name+'.geo.json')).write_text(json.dumps(geo,indent=2),encoding='utf-8')
 elements=[]
 for c in cubes:
  faces={k:{'uv':[u,v,u+w,v+h],'texture':0} for k,(u,v,w,h) in uvfaces(*c['uv'],*c['size']).items()}
  elements.append({'name':c['name'],'type':'cube','uuid':str(uuid.uuid4()),'from':c['origin'],'to':[a+b for a,b in zip(c['origin'],c['size'])],'origin':[0,0,0],'rotation':[0,0,0],'box_uv':True,'uv_offset':c['uv'],'faces':faces})
 bb={'meta':{'format_version':'4.10','model_format':'bedrock','box_uv':True},'name':'Velvet '+name,'resolution':{'width':128,'height':128},'elements':elements,'outliner':[{'name':'grip','uuid':str(uuid.uuid4()),'origin':[0,0,0],'children':[e['uuid'] for e in elements]}],'textures':[{'name':name+'.png','id':'0','uuid':str(uuid.uuid4()),'width':128,'height':128,'source':'data:image/png;base64,'+base64.b64encode(texture.read_bytes()).decode()}]}
 (D/(name+'.bbmodel')).write_text(json.dumps(bb,indent=2),encoding='utf-8')
 # Creative item models use the same geometry scaled into Minecraft item-model bounds.
 item_elements=[]
 for c in cubes:
  origin=[8+v*.45 for v in c['origin']];end=[8+(a+b)*.45 for a,b in zip(c['origin'],c['size'])]
  faces={k:{'uv':[u/8,v/8,(u+w)/8,(v+h)/8],'texture':'#0'} for k,(u,v,w,h) in uvfaces(*c['uv'],*c['size']).items()}
  item_elements.append({'from':origin,'to':end,'faces':faces})
 (A/'models/item'/('equipment_'+name+'.json')).write_text(json.dumps({'textures':{'0':'velvet_eclipse:entity/weapon_'+name},'elements':item_elements,'display':{'gui':{'rotation':[20,-35,-20],'translation':[0,-1,0],'scale':[.7,.7,.7]},'thirdperson_righthand':{'rotation':[0,90,0],'translation':[0,1,0],'scale':[.8,.8,.8]},'firstperson_righthand':{'rotation':[0,90,0],'translation':[0,2,0],'scale':[.8,.8,.8]}}}),encoding='utf-8')
# Small original icons for non-visual equipment and recovery items.
for name in ['armor','footwear','accessory','supply','elizabeth']:
 im=Image.new('RGBA',(32,32));d=ImageDraw.Draw(im)
 if name=='armor':d.polygon([(5,4),(12,2),(20,2),(27,4),(30,13),(24,16),(24,29),(8,29),(8,16),(2,13)],fill='#2c56b9',outline='#a9c8e9');d.line((16,5,16,27),fill='#d3ba68',width=2)
 elif name=='footwear':
  for x in [3,18]:d.polygon([(x,4),(x+7,4),(x+7,20),(x+11,23),(x+11,28),(x,28)],fill='#2c56b9',outline='#a9c8e9');d.line((x,26,x+10,26),fill='#d3ba68',width=2)
 elif name=='accessory':d.ellipse((4,4,27,27),outline='#d3ba68',width=4);d.polygon([(16,2),(22,9),(16,16),(10,9)],fill='#74dce8',outline='#f4f6dd')
 elif name=='supply':d.rounded_rectangle((8,8,24,29),3,fill='#b0dce7',outline='#3661a3',width=2);d.rectangle((10,3,22,8),fill='#c6a861');d.rectangle((10,13,22,23),fill='#2253b6');d.line((16,15,16,21),fill='white',width=2);d.line((13,18,19,18),fill='white',width=2)
 else:d.rectangle((5,5,25,29),fill='#173eaa',outline='#d3ba68',width=2);d.ellipse((10,10,21,21),outline='#68def9',width=2)
 im.save(A/'textures/item'/('equipment_'+name+'.png'))
 (A/'models/item'/('equipment_'+name+'.json')).write_text(json.dumps({'parent':'minecraft:item/generated','textures':{'layer0':'velvet_eclipse:item/equipment_'+name}}),encoding='utf-8')
gear=json.loads((ROOT/'src/main/resources/data/velvet_eclipse/reload_equipment.json').read_text(encoding='utf-8'))
labels={}
for g in gear:
 ident='gear_'+g['id'];category=g['family'].lower() if g['slot']=='WEAPON' else g['slot'].lower()
 (A/'models/item'/(ident+'.json')).write_text(json.dumps({'parent':'velvet_eclipse:item/equipment_'+category}),encoding='utf-8');labels['item.velvet_eclipse.'+ident]=g['name']
supplies={'medicine':'Medicine','life_stone':'Life Stone','bead':'Bead','snuff_soul':'Snuff Soul','chewing_soul':'Chewing Soul','precious_egg':'Precious Egg','dis_poison':'Dis-Poison','dis_charm':'Dis-Charm','dis_rage':'Dis-Rage','dis_patra':'Dis-Patra','amrita_soda':'Amrita Soda','elizabeth_model':'Elizabeth'}
for ident,name in supplies.items():
 (A/'models/item'/(ident+'.json')).write_text(json.dumps({'parent':'velvet_eclipse:item/equipment_'+('elizabeth' if ident=='elizabeth_model' else 'supply')}),encoding='utf-8');labels['item.velvet_eclipse.'+ident]=name
labels['entity.velvet_eclipse.elizabeth']='Elizabeth'
for lang in ['pt_br','en_us']:
 p=A/'lang'/(lang+'.json');data=json.loads(p.read_text(encoding='utf-8'));data.update(labels);p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print('9 weapon families, 635 equipment models, 11 recovery items and Elizabeth creative item generated.')
