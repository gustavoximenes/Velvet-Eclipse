"""Native atlas materials, creative models and clean functional access models."""
from pathlib import Path
import json,math
from PIL import Image,ImageDraw
ROOT=Path(__file__).resolve().parents[2]/'src/main/resources/assets/velvet_eclipse'
def write(path,data):
 p=ROOT/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
def save(name,img):
 p=ROOT/'textures/block'/('tartarus_native_'+name+'.png');p.parent.mkdir(parents=True,exist_ok=True);img.save(p)
# Subtle material grain modulates the authored vertex colours, without repainting the room purple.
for mat in range(10):
 n=128;frames=24 if mat in [7,8,9] else 1;img=Image.new('RGBA',(n,n*frames))
 for f in range(frames):
  for y in range(n):
   for x in range(n):
    grain=round(244+5*math.sin(x*1.7+y*2.1)+3*math.sin(x*.23-y*.34))
    if mat in [7,8,9]:
     t=f*math.tau/frames;u=(x%(n-1))/(n-1);v=(y%(n-1))/(n-1)
     ribbon=max(0,math.sin(u*math.tau*3+math.sin(v*math.tau+t)*2-t))**14
     haze=(math.sin(v*math.tau*2-u*math.tau+t)+1)*.5
     if mat in [7,8]:color=(round(5+26*ribbon),round(45+135*ribbon+19*haze),round(30+83*ribbon+12*haze),255)
     else:color=(round(8+37*ribbon),round(24+56*ribbon),round(60+128*ribbon+13*haze),255)
    else:
     if mat in [1,2] and (x%16==0 or y%16==0):grain-=16 if mat==2 else 7
     color=(grain,grain,grain,255)
    img.putpixel((x,y+n*f),color)
  if mat==6:ImageDraw.Draw(img).rectangle((0,0,n,n),fill=(255,255,255,255))
 save(str(mat),img)
 if mat in [5,6]:
  # OptiFine emissive textures and labPBR/SEUS emission use the same authored mask.
  # No shader-pack files or user options need to be overwritten.
  save(str(mat)+'_e',img)
  save(str(mat)+'_s',Image.new('RGBA',img.size,(24,10,200,200)))
 if frames>1:write('textures/block/tartarus_native_'+str(mat)+'.png.mcmeta',{'animation':{'frametime':2,'interpolate':True}})
emissive=ROOT.parent/'minecraft/optifine/emissive.properties';emissive.parent.mkdir(parents=True,exist_ok=True)
emissive.write_text('suffix.emissive=_e\n',encoding='ascii')
old=ROOT/'optifine/emissive.properties'
if old.is_file():old.unlink()
textures={f'm{i}':f'velvet_eclipse:block/tartarus_native_{i}' for i in range(10)};textures['particle']=textures['m2']
def native(name,mesh,display=None):
 data={'loader':'velvet_eclipse:tartarus_mesh','mesh':mesh,'textures':textures,'ambientocclusion':False}
 if display:data['display']=display
 write('models/block/'+name+'.json',data)
def item(name,parent=None):write('models/item/'+name+'.json',{'parent':'velvet_eclipse:block/'+(parent or name)})
kit_names={'column':('Coluna canelada azul','Blue fluted column'),'ivory_column':('Coluna canelada marfim','Ivory fluted column'),'lantern':('Lanterna do Tártaro','Tartarus lantern'),'rail':('Corrimão do Tártaro','Tartarus railing'),'cornice':('Cornija do Tártaro','Tartarus cornice'),'seal':('Selo ornamental do Tártaro','Tartarus ornamental seal'),'arch':('Arco ogival do Tártaro','Tartarus pointed arch'),'forward':('Moldura de portal verde','Green portal frame'),'back':('Moldura de portal azul','Blue portal frame'),'stair':('Escadaria ornamental do Tártaro','Tartarus staircase display')}
catalog=json.loads((ROOT/'tartarus/catalog.json').read_text())
for kind in kit_names:
 name='tartarus_kit_'+kind;b=catalog['kit_'+kind]['bounds'];size=max(b[i+3]-b[i] for i in range(3));scale=min(.7,1.1/size)
 display={'gui':{'rotation':[25,225,0],'translation':[-((b[0]+b[3])/2-.5)*scale*16,-((b[1]+b[4])/2-.5)*scale*16,0],'scale':[scale]*3},'ground':{'translation':[0,2,0],'scale':[scale*.6]*3},'fixed':{'scale':[scale]*3},'firstperson_righthand':{'scale':[scale*.7]*3},'thirdperson_righthand':{'translation':[0,2,0],'scale':[scale*.5]*3}}
 native(name,'kit_'+kind,display);write('blockstates/'+name+'.json',{'variants':{'':{'model':'velvet_eclipse:block/'+name}}});item(name)
native('tartarus_architecture','empty');write('blockstates/tartarus_architecture.json',{'variants':{'':{'model':'velvet_eclipse:block/tartarus_architecture'}}})
native('tartarus_architecture_emissive','emissive');write('blockstates/tartarus_architecture_emissive.json',{'variants':{'':{'model':'velvet_eclipse:block/tartarus_architecture_emissive'}}})
# OptiFine loads each mod's own shaders/block.properties after the pack mapping.
# Complementary 5.x reserves 10028 for modded emitters, including HDR/bloom/SSR.
# Only luminous triangles use this anchor; the stone geometry keeps normal shading.
aliases=ROOT/'shaders/block.properties';aliases.parent.mkdir(parents=True,exist_ok=True)
aliases.write_text('# Complementary: modded light sources (terrain emission, bloom and reflections).\nblock.10028=velvet_eclipse:tartarus_architecture_emissive velvet_eclipse:tartarus_reworked_window velvet_eclipse:tartarus_reworked_light\n',encoding='ascii')
# Solid blocks are visible in a creative build; only generated collision states are hidden.
colors={'tile':(55,75,84),'wall':(34,46,66),'window':(27,111,73),'light':(169,237,225)}
for kind,color in colors.items():
 img=Image.new('RGBA',(64,64),color+(255,));d=ImageDraw.Draw(img)
 for y in range(0,64,16):
  d.line((0,y,63,y),fill=tuple(round(v*.65) for v in color)+(255,))
  for x in range(0,64,16):d.line((x,y,x,min(63,y+16)),fill=tuple(round(v*.8) for v in color)+(255,))
 save(kind,img);name='tartarus_reworked_'+kind
 if kind in ['window','light']:
  save(kind+'_e',img);save(kind+'_s',Image.new('RGBA',img.size,(24,10,200,200)))
 write('models/block/'+name+'.json',{'parent':'minecraft:block/cube_all','textures':{'all':'velvet_eclipse:block/tartarus_native_'+kind}})
 write('blockstates/'+name+'.json',{'variants':{'':{'model':'velvet_eclipse:block/'+name}}});item(name)
# Reuse the proven vanilla stairs state table, replacing all model references.
old=json.loads((ROOT/'blockstates/tartarus_stairs.json').read_text());text=json.dumps(old).replace('tartarus_stairs','tartarus_reworked_stairs')
write('blockstates/tartarus_reworked_stairs.json',json.loads(text))
for suffix,parent in [('', 'stairs'),('_inner','inner_stairs'),('_outer','outer_stairs')]:
 write('models/block/tartarus_reworked_stairs'+suffix+'.json',{'parent':'minecraft:block/'+parent,'textures':{k:'velvet_eclipse:block/tartarus_native_tile' for k in ['bottom','top','side']}})
item('tartarus_reworked_stairs')
# Existing door states retain hinge/route logic. Upper block avoids drawing the frame twice.
for half in ['lower','upper']:
 for hinge in ['left','right']:
  for opened in ['false','true']:native(f'velvet_door_{half}_{hinge}_{opened}',('velvet_door_'+opened) if half=='lower' else 'empty',{'gui':{'rotation':[15,200,0],'translation':[0,-6,0],'scale':[.27]*3},'firstperson_righthand':{'scale':[.2]*3},'thirdperson_righthand':{'scale':[.2]*3},'ground':{'translation':[0,2,0],'scale':[.12]*3}})
item('velvet_door','velvet_door_lower_left_false')
# The existing trigger blocks have a clean plane in creative builds; generated states use the complete framed model.
for route in ['portal','forward','back','exit','summit']:
 name='tartarus_'+route;mat=8 if route in ['portal','forward','summit'] else 9
 write('models/block/'+name+'.json',{'textures':{'all':f'velvet_eclipse:block/tartarus_native_{mat}','particle':f'velvet_eclipse:block/tartarus_native_{mat}'},'elements':[{'from':[0,0,7.9],'to':[16,16,8.1],'faces':{side:{'texture':'#all','uv':[0,0,16,16]} for side in ['north','south']}}]});item(name)
# Placement items render the same native geometry as their in-world entities.
for kind in ['terminal','treasure','monad']:
 mesh=catalog[kind];b=mesh['bounds'];scale=1/max(b[i+3]-b[i] for i in range(3))
 native('tartarus_prop_'+kind,kind,{'gui':{'rotation':[20,225,0],'translation':[0,-(b[1]+b[4]-1)*scale*8,0],'scale':[scale]*3}})
 item('tartarus_'+kind,'tartarus_prop_'+kind)
for locale in ['pt_br','en_us']:
 data=json.loads((ROOT/'lang'/f'{locale}.json').read_text(encoding='utf8'));n=0 if locale=='pt_br' else 1
 for kind,names in kit_names.items():data['block.velvet_eclipse.tartarus_kit_'+kind]=names[n]
 for kind,names in {'tile':('Ladrilho lapidado do Tártaro','Tartarus dressed tile'),'wall':('Alvenaria azul do Tártaro','Tartarus blue masonry'),'window':('Vitral verde do Tártaro','Tartarus green window'),'light':('Luz arquitetônica do Tártaro','Tartarus architectural light'),'stairs':('Escadas lapidadas do Tártaro','Tartarus dressed stairs')}.items():data['block.velvet_eclipse.tartarus_reworked_'+kind]=names[n]
 write('lang/'+locale+'.json',data)
print('Native materials, block/item models and translations generated.')
