"""Original voxel props and pixel textures. Run from any directory; no downloaded assets."""
from pathlib import Path
from PIL import Image, ImageDraw
import json, math, copy
root=Path(__file__).resolve().parents[1]/'src/main/resources';assets=root/'assets/velvet_eclipse';data=root/'data/velvet_eclipse'
def write(path,value):
 path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(value,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
colors={'navy':'#09132e','black':'#0b1021','steel':'#526b97','blue':'#174aab','cobalt':'#193dd3','light':'#69cbee','silver':'#c7e5f4','gold':'#bc9d52','cloth':'#192a97','shade':'#14215a'}
atlas=Image.new('RGB',(32,32));d=ImageDraw.Draw(atlas)
for i,(name,color) in enumerate(colors.items()):d.rectangle((i%4*8,i//4*8,i%4*8+7,i//4*8+7),fill=color)
(assets/'textures/block').mkdir(parents=True,exist_ok=True);(assets/'textures/item').mkdir(parents=True,exist_ok=True);atlas.save(assets/'textures/block/velvet_palette.png')
def uv(name):
 i=list(colors).index(name);return [i%4*4+.5,i//4*4+.5,i%4*4+3.5,i//4*4+3.5]
def cube(a,b,color):return {'from':list(a),'to':list(b),'faces':{f:{'texture':'#palette','uv':uv(color)} for f in ['north','south','east','west','up','down']}}
def model(elements,textures=None):return {'ambientocclusion':False,'textures':{'palette':'velvet_eclipse:block/velvet_palette','particle':'velvet_eclipse:block/velvet_floor',**(textures or {})},'elements':elements}
def block(name,elements):
 write(assets/f'models/block/{name}.json',model(elements));write(assets/f'blockstates/{name}.json',{'variants':{'':{'model':'velvet_eclipse:block/'+name}}});write(assets/f'models/item/{name}.json',{'parent':'velvet_eclipse:block/'+name})
for name,base in [('velvet_floor','#17217b'),('velvet_wall','#111945'),('velvet_trim','#283a89')]:
 im=Image.new('RGB',(32,32),base);d=ImageDraw.Draw(im)
 if name=='velvet_floor':
  for x in range(0,32,4):
   for y in range(0,32,4):d.line((x,y,x+2,y),fill='#1c2985');d.point((x+1,y+2),fill='#1d2881')
  d.line((0,0,31,0),fill='#202e87');d.line((0,0,0,31),fill='#202e87')
 elif name=='velvet_wall':
  d.rectangle((2,1,29,30),outline='#303e70');d.rectangle((4,3,27,28),outline='#1c2854');d.line((15,4,15,27),fill='#18214c')
 else:
  for y,c in [(0,'#7899c2'),(2,'#283866'),(7,'#42619e'),(15,'#41548a'),(24,'#7f9fd1'),(28,'#b3d7ec')]:d.line((0,y,31,y),fill=c,width=2)
 im.save(assets/f'textures/block/{name}.png');write(assets/f'models/block/{name}.json',{'parent':'minecraft:block/cube_all','textures':{'all':'velvet_eclipse:block/'+name}});write(assets/f'blockstates/{name}.json',{'variants':{'':{'model':'velvet_eclipse:block/'+name}}});write(assets/f'models/item/{name}.json',{'parent':'velvet_eclipse:block/'+name})
# Small, readable mask emblem, with stepped silhouette and alternating sun rays.
emblem=Image.new('RGBA',(32,32),'#163462');d=ImageDraw.Draw(emblem)
d.polygon([(10,1),(21,1),(28,8),(31,15),(28,24),(21,30),(10,30),(3,24),(0,15),(3,8)],fill='#91e4fb')
d.polygon([(10,3),(21,3),(26,9),(29,15),(26,23),(21,28),(10,28),(5,23),(2,15),(5,9)],fill='#112456')
for i in range(18):
 a=i*math.tau/18;d.polygon([(16+math.cos(a)*13,16+math.sin(a)*13),(16+math.cos(a+.11)*9,16+math.sin(a+.11)*9),(16+math.cos(a-.11)*9,16+math.sin(a-.11)*9)],fill='#eaf8ff')
d.polygon([(12,6),(19,6),(22,12),(21,22),(17,26),(11,22),(9,12)],fill='#e9f7fc');d.polygon([(16,6),(19,6),(22,12),(21,22),(17,26),(16,24)],fill='#142951')
d.rectangle((11,13,14,14),fill='#132349');d.rectangle((18,13,21,14),fill='#ecfbff');d.line((16,15,15,20),fill='#428ab1');d.line((13,22,18,22),fill='#47769b');emblem.save(assets/'textures/item/velvet_key_emblem.png')
key=[cube((7.5,.5,7.5),(8.5,10.4,8.5),'blue'),cube((7.5,.5,7.45),(7.75,10.4,7.55),'light')]
for y in [3.2,8.8,9.4]:key.append(cube((7.25,y,7.25),(8.75,y+.28,8.75),'steel'))
for a,b in [((4.5,11,7.2),(5.5,14,8.8)),((10.5,11,7.2),(11.5,14,8.8)),((5.5,10,7.2),(10.5,11,8.8)),((5.5,14,7.2),(10.5,15,8.8)),((5,10.5,7.2),(11,14.5,8.8))]:key.append(cube(a,b,'blue'))
plate=cube((5.25,10.5,7.15),(10.75,14.5,8.85),'navy')
for face in ['north','south']:plate['faces'][face]={'texture':'#emblem','uv':[0,0,16,16]}
key.append(plate);key.append(cube((7.6,15,7.6),(8.4,16,8.4),'light'))
# Connected stepped branch and diamond teeth, like the reference key.
key += [cube((8.3,1.9,7.5),(10.5,2.4,8.5),'blue'),cube((9.5,1.2,7.5),(10,3.5,8.5),'blue')]
for x,y in [(9.75,3.45),(10.5,2.15),(9.75,1.15)]:
 c=cube((x-.38,y-.38,7.5),(x+.38,y+.38,8.5),'light' if x<10 else 'blue');c['rotation']={'origin':[x,y,8],'axis':'z','angle':45,'rescale':False};key.append(c)
# Keep the medallion narrow relative to the long shaft.
for c in key[5:11]:
 for point in ['from','to']:c[point][0]=8+(c[point][0]-8)*.78
km=model(key,{'emblem':'velvet_eclipse:item/velvet_key_emblem'});km['display']={'gui':{'rotation':[0,0,-35],'translation':[0,0,0],'scale':[1.1,1.1,1.1]},'firstperson_righthand':{'rotation':[0,-90,20],'translation':[1,1,-1],'scale':[.75,.75,.75]},'thirdperson_righthand':{'rotation':[0,90,0],'translation':[0,2,0],'scale':[.65,.65,.65]},'ground':{'rotation':[0,0,0],'translation':[0,2,0],'scale':[.6,.6,.6]}};write(assets/'models/item/velvet_key.json',km)
# Door: static carved frame and six inset panels. Open models rotate only the leaf.
for half in ['lower','upper']:
 ybase=0 if half=='lower' else 16
 frame=[cube((0,0,0),(1,16,4),'light'),cube((15,0,0),(16,16,4),'light')]
 if half=='upper':frame +=[cube((0,14,0),(16,16,4),'steel'),cube((1,13,0),(15,14,3),'silver')]
 leaf=[cube((1,0,1),(15,16,3),'blue')]
 for x in [2.25,8.25]:
  for low,high in ([(1,6),(8,14)] if half=='lower' else [(1,7),(9,13)]):
   leaf +=[cube((x,low,.55),(x+5.5,high,1.05),'cobalt'),cube((x+.6,low+.6,.3),(x+4.9,high-.6,.55),'shade'),cube((x+.6,high-.9,.22),(x+4.9,high-.6,.3),'light')]
 if half=='lower':leaf +=[cube((11.4,12,.15),(12.5,14,.55),'gold'),cube((10.4,12.8,-.4),(12.4,13.2,.25),'gold'),cube((6.1,13.2,.2),(9.6,14.3,.65),'gold')]
 for hinge in ['left','right']:
  for opened in [False,True]:
   parts=copy.deepcopy(leaf)
   if opened:
    for c in parts:
     a,b=c['from'],c['to'];c['from']=[a[2],a[1],16-b[0]];c['to']=[b[2],b[1],16-a[0]]
     if hinge=='right':c['from'][0],c['to'][0]=16-c['to'][0],16-c['from'][0]
   write(assets/f'models/block/velvet_door_{half}_{hinge}_{str(opened).lower()}.json',model(frame+parts))
variants={}
for facing,y in [('north',0),('east',90),('south',180),('west',270)]:
 for half in ['lower','upper']:
  for hinge in ['left','right']:
   for opened in ['false','true']:variants[f'facing={facing},half={half},hinge={hinge},open={opened}']={'model':f'velvet_eclipse:block/velvet_door_{half}_{hinge}_{opened}','y':y}
write(assets/'blockstates/velvet_door.json',{'variants':variants});write(assets/'models/item/velvet_door.json',{'parent':'velvet_eclipse:block/velvet_door_lower_left_false'})
# Upholstered chair: stepped arch, metal ribs, curved silhouette represented by cuboids.
chair=[cube((2,8,2),(14,10,14),'black'),cube((3,10,3),(13,11,13),'cloth'),cube((2,10,12),(14,24,14),'navy'),cube((4,11,11.7),(12,23,12),'cloth')]
for x in [2,13]:
 for z in [3,12]:chair.append(cube((x,0,z),(x+1,8,z+1),'steel'))
 chair +=[cube((x,11,4),(x+1,15,5),'black'),cube((x,14,3),(x+1,15,13),'steel'),cube((x,12,13),(x+1,26,14),'steel')]
for x in [4,7,10]:chair.append(cube((x,12,13),(x+.6,27,13.8),'steel'))
chair +=[cube((3,25,12),(13,26,14),'steel'),cube((5,26,12),(11,28,14),'blue'),cube((7,27.5,12),(9,29,14),'gold')]
for x in [5,8,11]:
 for y in [13,17,21]:chair.append(cube((x,y,11.55),(x+.5,y+.5,11.75),'silver'))
block('velvet_chair',chair);write(assets/'blockstates/velvet_chair.json',{'variants':{'facing='+f:{'model':'velvet_eclipse:block/velvet_chair','y':y} for f,y in [('north',0),('east',90),('south',180),('west',270)]}})
table=[cube((-4,0,-4),(20,13,20),'cloth'),cube((-8,0,0),(24,13,16),'cloth'),cube((0,0,-8),(16,13,24),'cloth'),cube((-5,13,-5),(21,14,21),'cobalt'),cube((-8,13,0),(24,14,16),'cobalt'),cube((0,13,-8),(16,14,24),'cobalt')]
for x in range(-4,21,3):table.append(cube((x,0,-4.1),(x+.6,12,-3.9),'shade'))
block('velvet_table',table)
grille=[cube((0,0,13),(1,16,15),'steel'),cube((15,0,13),(16,16,15),'steel'),cube((7.5,0,13),(8.5,16,15),'steel'),cube((0,0,13),(16,.75,15),'steel'),cube((0,15,13),(16,16,15),'steel')]
for x,y in [(3,3),(11,3),(3,11),(11,11)]:
 c=cube((x-1,y-1,13),(x+1,y+1,14),'silver');c['rotation']={'origin':[x,y,13.5],'axis':'z','angle':45,'rescale':False};grille.append(c)
block('velvet_grille',grille)
write(assets/'blockstates/velvet_grille.json',{'variants':{'facing='+f:{'model':'velvet_eclipse:block/velvet_grille','y':y} for f,y in [('north',0),('east',90),('south',180),('west',270)]}})
for locale in ['en_us','pt_br']:
 path=assets/f'lang/{locale}.json';lang=json.loads(path.read_text(encoding='utf-8')) if path.exists() else {}
 lang.update({'itemGroup.velvet_eclipse':'Velvet Eclipse','item.velvet_eclipse.velvet_key':'Velvet Key','block.velvet_eclipse.velvet_door':'Velvet Door' if locale=='en_us' else 'Porta da Velvet Room','block.velvet_eclipse.velvet_chair':'Velvet Chair' if locale=='en_us' else 'Cadeira de veludo','block.velvet_eclipse.velvet_table':'Velvet Table' if locale=='en_us' else 'Mesa da Velvet Room','block.velvet_eclipse.velvet_floor':'Velvet Carpet','block.velvet_eclipse.velvet_wall':'Velvet Wall','block.velvet_eclipse.velvet_trim':'Velvet Cornice','block.velvet_eclipse.velvet_grille':'Velvet Grille'});write(path,lang)
write(data/'dimension_type/velvet_room.json',{'ultrawarm':False,'natural':False,'coordinate_scale':1.0,'has_skylight':False,'has_ceiling':True,'ambient_light':.6,'fixed_time':18000,'piglin_safe':True,'bed_works':False,'respawn_anchor_works':False,'has_raids':False,'logical_height':256,'min_y':0,'height':256,'infiniburn':'#minecraft:infiniburn_overworld','effects':'minecraft:the_end','monster_spawn_block_light_limit':0,'monster_spawn_light_level':0})
write(data/'dimension/velvet_room.json',{'type':'velvet_eclipse:velvet_room','generator':{'type':'minecraft:flat','settings':{'biome':'minecraft:the_void','features':False,'lakes':False,'layers':[{'block':'minecraft:air','height':1}],'structure_overrides':[]}}})
print('Velvet Room: voxel key, 8 door models, 4 furniture/material models, textures, languages and dimension data generated.')
