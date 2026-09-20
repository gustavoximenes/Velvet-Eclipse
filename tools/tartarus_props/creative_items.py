"""Inventory icons rendered from the same native authored cuboids and pixel atlas."""
from pathlib import Path
import json,sys
import numpy as np
from PIL import Image
ROOT=Path(__file__).resolve().parent
ASSETS=ROOT.parents[1]/'src/main/resources/assets/velvet_eclipse'
sys.path.insert(0,str(ROOT.parent/'igor'))
import render_model as render
for name in ['terminal','treasure','monad']:
 stem='tartarus_'+name;d=ROOT/'draft'
 render.data=json.loads((d/(stem+'.geo.json')).read_text(encoding='utf-8'))['minecraft:geometry'][0]
 render.texture=np.array(Image.open(d/(stem+'.png')).convert('RGBA'))/255
 render.animations=json.loads((d/(stem+'.animation.json')).read_text(encoding='utf-8'))['animations']
 pixels=np.array(render.render(-20,192,192,pitch=8).convert('RGBA'));bg=np.array([224,229,226])
 pixels[:,:,3][np.all(pixels[:,:,:3]==bg,axis=2)]=0
 Image.fromarray(pixels).resize((64,64),Image.Resampling.NEAREST).save(ASSETS/'textures/item'/(stem+'.png'))
 (ASSETS/'models/item'/(stem+'.json')).write_text(json.dumps({'parent':'minecraft:item/generated','textures':{'layer0':'velvet_eclipse:item/'+stem}}),encoding='utf-8')
for locale in ['pt_br','en_us']:
 path=ASSETS/'lang'/(locale+'.json');data=json.loads(path.read_text(encoding='utf-8'))
 for name in ['terminal','treasure','monad']:data['item.velvet_eclipse.tartarus_'+name]=data['entity.velvet_eclipse.tartarus_'+name]
 for name in ['cinder_wisp','nocturne_knight','sylphid','lumen_hare','iron_oni','umbra_slime']:data['item.velvet_eclipse.model_'+name]=data['entity.velvet_eclipse.persona_'+name]+(' • Modelo' if locale=='pt_br' else ' • Model')
 path.write_text(json.dumps(data,indent=2,ensure_ascii=False)+'\n',encoding='utf-8')
print('3 placement icons/models and complete creative item names generated')
