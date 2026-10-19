"""Export the three accepted shop studies; markers replace every other attendant.

The original Bedrock sources remain unchanged. Convert axes once for GeckoLib,
preserve integer cuboids/Box UV and all authored animation timing.
"""
from pathlib import Path
import copy,gzip,hashlib,json,math,shutil
from PIL import Image,ImageDraw

ROOT=Path(__file__).resolve().parent
MOD=ROOT.parents[1]
AS=MOD/'src/main/resources/assets/velvet_eclipse'
DATA=MOD/'src/main/resources/data/velvet_eclipse/city'
SOURCE=ROOT/'shop_npcs'
STUDIES={'police':('kurosawa','01-kurosawa-study-03'),'aohige':('aohige','02-aohige-study-02'),'mayoido':('mayoido','03-mayoido-study-02')}

def save(path,data):path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
def vector(v,ch):return [-v[0],-v[1],v[2]] if ch=='rotation' else [-v[0],v[1],v[2]]
def convert(geo,clips):
 geo=copy.deepcopy(geo);clips=copy.deepcopy(clips)
 for b in geo['minecraft:geometry'][0]['bones']:
  b['pivot'][0]*=-1
  if 'rotation' in b:b['rotation']=vector(b['rotation'],'rotation')
  for c in b.get('cubes',[]):
   assert all(isinstance(x,int) and x>0 for x in c['size'])
   c['origin'][0]=-c['origin'][0]-c['size'][0]
   if 'pivot' in c:c['pivot'][0]*=-1
   if 'rotation' in c:c['rotation']=vector(c['rotation'],'rotation')
 for clip in clips.values():
  for tracks in clip['bones'].values():
   for ch,keys in tracks.items():
    assert ch in ['rotation','position']
    for t,v in keys.items():keys[t]=vector(v,ch)
 return geo,{'format_version':'1.8.0','animations':clips}

def main():
 report={}
 for shop,(name,study) in STUDIES.items():
  source=SOURCE/name/'draft'
  # Bootstrap from accepted standalone files only when the archive is absent.
  if not source.exists():
   approved=MOD.parent/'modeling/paulownia-npcs'/study/'draft';source.mkdir(parents=True)
   for suffix in ['.geo.json','.animation.json','.png','.bbmodel']:shutil.copy2(approved/(name+suffix),source/(name+suffix))
  geo=json.loads((source/(name+'.geo.json')).read_text(encoding='utf8'))
  authored=json.loads((source/(name+'.animation.json')).read_text(encoding='utf8'))['animations']
  clips={('misc.idle' if key.endswith('_idle') else 'action.greeting'):clip for key,clip in authored.items()}
  geo,anim=convert(geo,clips);asset='city_'+name
  geo['minecraft:geometry'][0]['description']['identifier']='geometry.'+asset
  save(AS/'geo/entity'/(asset+'.geo.json'),geo);save(AS/'animations/entity'/(asset+'.animation.json'),anim)
  shutil.copy2(source/(name+'.png'),AS/'textures/entity'/(asset+'.png'))
  save(AS/'models/item'/('city_'+shop+'_model.json'),{'parent':'velvet_eclipse:block/city_shop_'+shop})
  report[shop]={'asset':asset,'study':study,'scale':.09,'sourceSha256':{p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in source.iterdir() if p.is_file()}}
 # An authored cuboid icon: cyan diamond with a white shopping bag on both sides.
 atlas=Image.new('RGBA',(128,64));draw=ImageDraw.Draw(atlas);cubes=[];u=2;v=2;row=0
 def cube(center,size,color,rotation=None):
  nonlocal u,v,row
  w,h,d=size;aw=2*(w+d);ah=h+d
  if u+aw+2>128:u=2;v+=row+2;row=0
  assert v+ah+2<=64
  draw.rectangle((u,v,u+aw-1,v+ah-1),fill=color)
  c={'origin':[a-b/2 for a,b in zip(center,size)],'size':list(size),'uv':[u,v]}
  if rotation:c.update(rotation=rotation,pivot=list(center))
  cubes.append(c);u+=aw+2;row=max(row,ah)
 cube((0,7,0),(8,8,2),'#66e9f5',[0,0,45]);cube((0,7,0),(6,6,3),'#102e63',[0,0,45])
 for side in [-1,1]:
  z=side*2
  for c,s in [((0,6.4,z),(4,3,1)),((-1,8.5,z),(1,2,1)),((1,8.5,z),(1,2,1)),((0,9,z),(2,1,1))]:cube(c,s,'#eefcfc')
 geo={'format_version':'1.12.0','minecraft:geometry':[{'description':{'identifier':'geometry.city_shop_marker','texture_width':128,'texture_height':64,'visible_bounds_width':2,'visible_bounds_height':2,'visible_bounds_offset':[0,.5,0]},'bones':[{'name':'marker','pivot':[0,0,0],'cubes':cubes}]}]}
 track={str(i/10):[0,round(math.sin(i/40*math.tau)*.6,6),0] for i in range(41)}
 geo,anim=convert(geo,{'misc.idle':{'loop':True,'animation_length':4,'bones':{'marker':{'position':track}}}})
 save(AS/'geo/entity/city_shop_marker.geo.json',geo);save(AS/'animations/entity/city_shop_marker.animation.json',anim);atlas.save(AS/'textures/entity/city_shop_marker.png')
 plan=json.loads(gzip.decompress((DATA/'plan.json.gz').read_bytes()))
 mall=json.loads(gzip.decompress((MOD/'tools/city_polish/approved/mall.json.gz').read_bytes()))
 points=[]
 for shop in mall['shops']:
  ident=shop['id'];npc=ident in STUDIES;a=math.radians(shop['angle']);radius=21.6 if npc else 22.;tangent=.9 if npc else 0
  x=93+math.sin(a)*radius+math.cos(a)*tangent;z=65-math.cos(a)*radius+math.sin(a)*tangent;y=64.002 if npc else 64.7
  if ident=='mandragora':x,y,z=95.4,68.7,35.6
  anchor=next(d for d in plan['doors'] if d['target']=='shop:'+ident)
  points.append({'shop':ident,'asset':report[ident]['asset'] if npc else 'city_shop_marker','npc':npc,'x':round(x,5),'y':y,'z':round(z,5),'yaw':shop['angle'],'scale':.09 if npc else 1.,'width':1.3 if ident=='mayoido' else .8,'height':(1.5 if ident=='mayoido' else 2.1) if npc else 1.,'anchor':[anchor[k] for k in ['x','y','z']]})
 save(DATA/'shop-access.json',points);save(SOURCE/'integration.json',report)
 # Both the Minecraft catalogue and the mod's independent Tab language choice.
 names={'police':('Kurosawa — display model','Kurosawa — modelo decorativo'),'aohige':('Aohige owner — display model','Dono da Aohige — modelo decorativo'),'mayoido':('Mayoido owner — display model','Dona da Mayoido — modelo decorativo')}
 messages={'Clique direito: acessar loja':'Right-click: open shop','Clique com o botão direito no vendedor para acessar esta loja.':'Right-click the attendant to open this shop.','Atendimento encerrado. Interaja novamente com o vendedor ou indicador.':'Service ended. Interact with the attendant or marker again.'}
 for lang in ['en_us','pt_br']:
  path=AS/'lang'/(lang+'.json');data=json.loads(path.read_text(encoding='utf8'))
  for shop,pair in names.items():data['item.velvet_eclipse.city_'+shop+'_model']=pair[lang=='pt_br']
  for point in points:data['entity.velvet_eclipse.city_shop_'+point['shop']+'_actor']=next(s['name'] for s in mall['shops'] if s['id']==point['shop'])
  save(path,data)
  path=AS/'localization'/(lang+'.json');data=json.loads(path.read_text(encoding='utf8'));data.update({k:k if lang=='pt_br' else v for k,v in messages.items()});save(path,data)
 print('Exported 3 approved NPCs, 6 shop markers, 3 creative display items. No JAR built.')
if __name__=='__main__':main()
