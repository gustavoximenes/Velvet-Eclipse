"""Native provisional cuboids + box UV, preserving all approved detailed geometry."""
import json,math,hashlib,re
from pathlib import Path
from PIL import Image,ImageDraw
ROOT=Path(__file__).resolve().parents[2];A=ROOT/'src/main/resources/assets/velvet_eclipse';D=ROOT/'src/main/resources/data/velvet_eclipse/catalogue'
personas=json.loads((D/'personas.json').read_text());skills=json.loads((D/'skills.json').read_text())
def write(p,v):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(v,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
def item(id):write(A/'models/item'/f'{id}.json',{'parent':'minecraft:item/generated','textures':{'layer0':'velvet_eclipse:item/'+id}})
colors={'SLASH':'#ffb12d','PHYSICAL':'#ffb12d','PIERCE':'#ffb12d','FIRE':'#ff555a','ICE':'#67e5ff','ELECTRIC':'#ffff32','WIND':'#57ffa6','PSY':'#ff80b9','NUCLEAR':'#53cff5','LIGHT':'#e7ffff','DARK':'#b470f8','ALMIGHTY':'#d9e2f4','HEALING':'#56ffd4','SUPPORT':'#82a2ff','AILMENT':'#b2e454'}
def passive(d,c):
 for cx,cy in [(13,11),(19,11),(13,17),(19,17)]:d.ellipse((cx-3,cy-3,cx+3,cy+3),outline=c,width=2)
 d.polygon([(16,9),(20,14),(16,19),(12,14)],fill=c);d.rectangle((15,13,16,15),fill='#132449')
for s in skills:
 sid=s['id'];color='#e9d96f' if s['passive'] else colors[s['element']];im=Image.new('RGBA',(32,32));draw=ImageDraw.Draw(im)
 draw.rectangle((5,1,26,30),fill=color);draw.rectangle((7,3,24,28),fill='#c2e1f1');draw.rectangle((8,4,23,27),fill='#132449')
 if s['passive']:passive(draw,color)
 else:
  draw.polygon([(16,6),(23,13),(16,20),(9,13)],fill=color);draw.line((16,7,16,18),fill='#f6ffff',width=2)
 for bit in range(7):
  if int(hashlib.sha256(sid.encode()).hexdigest()[:4],16)>>bit&1:draw.rectangle((9+bit*2,24,10+bit*2,25),fill=color)
 im.save(A/'textures/item'/f'skill_card_{sid}.png');item('skill_card_'+sid)
# The already existing Arsene passive also receives the same distinct emblem.
im=Image.open(A/'textures/item/skill_card_adverse_resolve.png');draw=ImageDraw.Draw(im);draw.rectangle((8,4,23,21),fill='#132449');passive(draw,'#e9d96f');im.save(A/'textures/item/skill_card_adverse_resolve.png')
palettes={'FOOL':['#526b87','#202938','#e9d3b6'],'MAGICIAN':['#4776b1','#dde5ed','#eecc60'],'PRIESTESS':['#74b7da','#e1e9dc','#9784b2'],'EMPEROR':['#942d32','#e9b54f','#404d6a'],'EMPRESS':['#9475ae','#dedfd3','#9eba87'],'LOVERS':['#cf7caa','#e7cab5','#b95776'],'DEATH':['#735781','#2b3543','#d3c3a8'],'DEVIL':['#71445c','#342938','#c29c5e'],'STAR':['#82b4c7','#dcebf4','#d4b554'],'SUN':['#e7be50','#a44339','#fff0ce']}
fairies=set('pixie high_pixie silky apsaras leanan_sidhe titan ia'.replace('titan ia','titania').split())
angels=set('angel archangel principality power virtue dominion throne raphael gabriel michael uriel metatron sandalphon'.split())
beasts=set('chimera inugami nekomata neko_shogun orth rus byakko barong garuda yatagarasu horus jatayu suzaku phoenix houou thunderbird'.replace('orth rus','orthrus').split())
snakes=set('naga naga_raja ananta vasuki seiryu yamata_no_orochi quetzalcoatl yurlungur'.split())
horned=set('oni rakshasa gurulu el igor berith abaddon incubus succubus lilim lilith arahabaki beelzebub satan lucifer mara baphomet surt'.replace('el igor','eligor').split())
def faceuv(slot):return {face:{'uv':[slot*8,0],'uv_size':[8,8]} for face in ['north','south','east','west','up','down']}
def cube(origin,size,slot=0):return {'origin':origin,'size':size,'uv':faceuv(slot)}
count=0
for p in personas:
 id=p['id']
 if id in ('orpheus','thanatos'):continue
 palette=palettes.get(p['arcana'],['#568177','#354758','#d2bd94']);palette=list(palette)
 if id in ('jack_frost','black_frost'):palette=['#6c66a8' if id=='black_frost' else '#cee7ea','#312f49' if id=='black_frost' else '#4075ad','#eee6cd']
 if id=='jack_o_lantern':palette=['#e18a36','#3d3659','#ffc65c']
 if id in ('slime','mothman','dec arabia'.replace(' ','')):palette=['#a786c7','#355555','#dfcd91']
 im=Image.new('RGBA',(64,8));draw=ImageDraw.Draw(im)
 for n,c in enumerate(palette+['#1d2732','#f5eee1','#ef515d','#aeced5','#dbc777']):
  draw.rectangle((n*8,0,n*8+7,7),fill=c);rgb=tuple(int(c[i:i+2],16) for i in (1,3,5));draw.line((n*8,0,n*8+7,0),fill=tuple(min(255,int(x*1.13)) for x in rgb))
 im.save(A/'textures/entity'/f'{id}.png')
 bones=[{'name':'root','pivot':[0,0,0]},{'name':'body','parent':'root','pivot':[0,12,0],'cubes':[cube([-3.5,9,-2],[7,9,4],0),cube([-4,16,-2.25],[8,2,4.5],1)]},
 {'name':'head','parent':'body','pivot':[0,18,0],'cubes':[cube([-3,18,-2.5],[6,6,5],2),cube([-2.2,20,-2.65],[1.3,.7,.2],3),cube([.9,20,-2.65],[1.3,.7,.2],3)]},
 {'name':'left_arm','parent':'body','pivot':[4,17,0],'cubes':[cube([3.5,9,-1.5],[2.5,8,3],0),cube([3.5,8,-1.5],[2.5,2,3],2)]},
 {'name':'right_arm','parent':'body','pivot':[-4,17,0],'cubes':[cube([-6,9,-1.5],[2.5,8,3],0),cube([-6,8,-1.5],[2.5,2,3],2)]},
 {'name':'left_leg','parent':'root','pivot':[2,10,0],'cubes':[cube([.5,0,-1.5],[2.8,10,3],1),cube([.5,0,-2.5],[2.8,2,4],3)]},
 {'name':'right_leg','parent':'root','pivot':[-2,10,0],'cubes':[cube([-3.3,0,-1.5],[2.8,10,3],1),cube([-3.3,0,-2.5],[2.8,2,4],3)]}]
 if id in angels or id in fairies:
  for sign in (-1,1):bones.append({'name':'wing_'+str(sign),'parent':'body','pivot':[sign*3,16,2],'rotation':[0,sign*15,sign*-15],'cubes':[cube([sign*4-(6 if sign<0 else 0),12,2],[6,7,1],4 if id in angels else 6)]})
 if id in horned:
  for sign in (-1,1):bones[2]['cubes'].append(dict(cube([sign*2-1,23,-1],[1.5,5,1.5],7),pivot=[sign*2,23,0],rotation=[0,0,sign*15]))
 if id in beasts:
  bones[2]['cubes'] += [cube([-2,18,-4],[4,3,2],0),cube([-3,23,-1],[1.5,3,2],0),cube([1.5,23,-1],[1.5,3,2],0)]
  bones.append({'name':'tail','parent':'body','pivot':[0,10,2],'rotation':[30,0,0],'cubes':[cube([-.7,8,2],[1.4,2,7],0)]})
 if id in snakes:bones.append({'name':'tail','parent':'root','pivot':[0,6,0],'rotation':[0,20,0],'cubes':[cube([-3,0,1],[6,5,9],0),cube([-2,1,9],[4,3,6],1)]})
 if id in ('jack_frost','black_frost','jack_o_lantern'):
  bones[2]['cubes'] += [cube([-3.5,23,-2.8],[7,2,5.6],1),dict(cube([-2,25,-1.5],[4,5,3],1),pivot=[0,25,0],rotation=[0,0,20])]
 if id in ('unicorn','bicorn'):bones[2]['cubes'].append(dict(cube([-.5,23,-2],[1,5,1],7),pivot=[0,23,-1],rotation=[-20,0,0]))
 write(A/'geo/entity'/f'{id}.geo.json',{'format_version':'1.12.0','minecraft:geometry':[{'description':{'identifier':'geometry.'+id,'texture_width':64,'texture_height':8,'visible_bounds_width':5,'visible_bounds_height':4,'visible_bounds_offset':[0,1.5,0]},'bones':bones}]})
 def frames(values):return {str(t):{'vector':v,'easing':'easeInOutSine'} for t,v in values}
 animations={'misc.idle':{'loop':True,'animation_length':3,'bones':{'body':{'position':frames([(0,[0,0,0]),(1.5,[0,.3,0]),(3,[0,0,0])])},'left_arm':{'rotation':frames([(0,[0,0,-4]),(1.5,[3,0,-7]),(3,[0,0,-4])])},'right_arm':{'rotation':frames([(0,[0,0,4]),(1.5,[-3,0,7]),(3,[0,0,4])])}}},
 'action.magic':{'loop':False,'animation_length':2.5,'bones':{'right_arm':{'rotation':frames([(0,[0,0,4]),(.6,[-110,0,25]),(1.4,[-65,0,0]),(2.5,[0,0,4])])},'body':{'rotation':frames([(0,[0,0,0]),(.6,[-5,-12,0]),(1.4,[8,8,0]),(2.5,[0,0,0])])}}},
 'action.physical':{'loop':False,'animation_length':2.5,'bones':{'right_arm':{'rotation':frames([(0,[0,0,4]),(.8,[-145,0,20]),(1.4,[-50,0,-20]),(2.5,[0,0,4])])},'body':{'rotation':frames([(0,[0,0,0]),(.8,[-8,-24,0]),(1.4,[12,20,0]),(2.5,[0,0,0])])}}}}
 write(A/'animations/entity'/f'{id}.animation.json',{'format_version':'1.8.0','animations':animations})
 icon=Image.new('RGBA',(32,32));di=ImageDraw.Draw(icon);di.polygon([(16,1),(29,15),(16,30),(3,15)],fill='#132449',outline='#55dcf1');di.rectangle((11,6,20,14),fill=palette[2]);di.rectangle((9,15,22,24),fill=palette[0]);di.rectangle((11,24,14,28),fill=palette[1]);di.rectangle((18,24,21,28),fill=palette[1]);icon.save(A/'textures/item'/f'{id}_model.png');item(id+'_model');count+=1
# The already detailed Thanatos also has a proper placement icon.
im=Image.new('RGBA',(32,32));d=ImageDraw.Draw(im);d.polygon([(16,1),(29,15),(16,30),(3,15)],fill='#132449',outline='#55dcf1');d.rectangle((11,6,20,14),fill='#dbd8cb');d.rectangle((9,15,22,25),fill='#414658');d.rectangle((12,9,14,10),fill='#090e1d');d.rectangle((17,9,19,10),fill='#090e1d');im.save(A/'textures/item/thanatos_model.png');item('thanatos_model')
for lang in ('pt_br','en_us'):
 path=A/'lang'/f'{lang}.json';data=json.loads(path.read_text(encoding='utf8'))
 data.pop('item.velvet_eclipse.skill_card_mahama_b',None)
 legacy=(ROOT/'src/main/java/com/gx/velveteclipse/core/persona/PersonaRegistry.java').read_text(encoding='utf8')
 for sid,name in re.findall(r'new SkillDefinition\("([a-z0-9_]+)",\s*"([^"]+)"',legacy):data['item.velvet_eclipse.skill_card_'+sid]='Skill Card • '+name
 for s in skills:data['item.velvet_eclipse.skill_card_'+s['id']]='Skill Card • '+s['name']
 for p in personas:
  data['entity.velvet_eclipse.persona_'+p['id']]=p['name'];data['item.velvet_eclipse.'+p['id']+'_model']=('Modelo • ' if lang=='pt_br' else 'Model • ')+p['name']
 write(path,data)
print(f'{count} provisional rigs with idle/physical/magic animations; {len(skills)} skill cards.')
