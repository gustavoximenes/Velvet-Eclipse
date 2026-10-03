"""Focused production resources and creative catalogue verification."""
from pathlib import Path
import json,gzip,struct,hashlib
from PIL import Image
ROOT=Path(__file__).resolve().parents[2];A=ROOT/'src/main/resources/assets/velvet_eclipse';CAT=json.loads((A/'tartarus/catalog.json').read_text());checks=0
def check(value,msg):
 global checks;checks+=1
 if not value:raise AssertionError(msg)
for name,meta in CAT.items():
 raw=gzip.decompress((A/'tartarus'/(name+'.mesh.gz')).read_bytes());magic,count=struct.unpack('<II',raw[:8]);check(magic==0x56454d31 and len(raw)==8+44*count and count==meta['vertices'],name+' geometry');check(hashlib.sha256(raw).hexdigest()==meta['sha256'],name+' provenance')
def model(path,visited=None):
 visited=set() if visited is None else visited
 if path in visited:return
 visited.add(path);p=A/'models'/(path+'.json');check(p.is_file(),'missing model '+path);data=json.loads(p.read_text())
 parent=data.get('parent','')
 if parent.startswith('velvet_eclipse:'):model(parent.split(':')[1],visited)
 if data.get('loader')=='velvet_eclipse:tartarus_mesh':check(data['mesh'] in ['empty','emissive'] or data['mesh'] in CAT,'native mesh exists');check(all('m'+str(i) in data['textures'] for i in range(10)),'all native materials resolve')
 for key,texture in data.get('textures',{}).items():
  if texture.startswith('velvet_eclipse:'):
   image=A/'textures'/(texture.split(':')[1]+'.png');check(image.is_file(),str(image));im=Image.open(image);check(im.width>0 and im.height>0,'valid sprite')
 for element in data.get('elements',[]):check(all(a<=b for a,b in zip(element['from'],element['to'])),'valid block cuboid')
kit=['column','ivory_column','lantern','rail','cornice','seal','arch','forward','back','stair'];solid=['tile','wall','window','light','stairs'];blocks=['tartarus_kit_'+k for k in kit]+['tartarus_reworked_'+k for k in solid]
pt=json.loads((A/'lang/pt_br.json').read_text(encoding='utf8'));en=json.loads((A/'lang/en_us.json').read_text(encoding='utf8'))
for block in blocks+['velvet_door','tartarus_portal','tartarus_forward','tartarus_back','tartarus_exit','tartarus_summit']:
 check('block.velvet_eclipse.'+block in pt and 'block.velvet_eclipse.'+block in en,'creative block translated '+block);model('item/'+block)
 state=json.loads((A/'blockstates'/(block+'.json')).read_text())
 for variant in state['variants'].values():
  for val in variant if isinstance(variant,list) else [variant]:model(val['model'].split(':')[1])
for prop in ['terminal','treasure','monad']:model('item/tartarus_'+prop)
for internal in ['tartarus_architecture','tartarus_architecture_emissive']:model('block/'+internal)
for mat in [7,8,9]:
 image=Image.open(A/f'textures/block/tartarus_native_{mat}.png');metadata=json.loads((A/f'textures/block/tartarus_native_{mat}.png.mcmeta').read_text());check(image.height==image.width*24 and metadata['animation']['frametime']==2,'24-frame native animated portal');check(image.crop((0,0,128,128)).tobytes()!=image.crop((0,128,128,256)).tobytes(),'portal actually moves')
registry=(ROOT/'src/main/java/com/gx/velveteclipse/darkhour/DarkHourRegistry.java').read_text();creative=(ROOT/'src/main/java/com/gx/velveteclipse/room/VelvetRoomRegistry.java').read_text()
check('DarkHourRegistry.creativeBlocks()' in creative and 'new BlockItem' in creative,'registered blocks automatically have creative items');check('"tartarus_architecture"' in registry,'internal geometry anchor separate');check('"terminal","treasure","monad"' in (ROOT/'src/main/java/com/gx/velveteclipse/darkhour/TartarusPropRegistry.java').read_text(),'all props have placement items')
report=dict(result='PASS',checks=checks,prefabs=len(CAT),newCreativeBlocks=blocks,protocol=2,inGame=False,shadersInGame=False)
out=ROOT/'build/reports/tartarus-rework';out.mkdir(parents=True,exist_ok=True);(out/'assets.json').write_text(json.dumps(report,indent=2),encoding='utf8');print(json.dumps(report,indent=2))
