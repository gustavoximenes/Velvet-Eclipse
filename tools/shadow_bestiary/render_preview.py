"""Render the authored cuboids/UVs with Igor's offline rasterizer; also bake creative icons."""
from pathlib import Path
import json,sys
import numpy as np
from PIL import Image,ImageDraw,ImageFont
ROOT=Path(__file__).resolve().parent;OUT=ROOT/'draft'
sys.path.insert(0,str(ROOT.parent/'igor'))
import render_model as renderer
assets=ROOT.parents[1]/'src/main/resources/assets/velvet_eclipse'
entries=json.loads((OUT/'catalog.json').read_text(encoding='utf-8'))
font=ImageFont.truetype('C:/Windows/Fonts/segoeuib.ttf',18)
sheet=Image.new('RGB',(1500,5*335),'#0b172d');d=ImageDraw.Draw(sheet)
for i,e in enumerate(entries):
 stem='enemy_'+e['id'];renderer.data=json.loads((OUT/(stem+'.geo.json')).read_text(encoding='utf-8'))['minecraft:geometry'][0]
 renderer.animations=json.loads((OUT/(stem+'.animation.json')).read_text(encoding='utf-8'))['animations'];renderer.texture=np.array(Image.open(OUT/(stem+'.png')).convert('RGBA'))/255
 im=renderer.render(-22,365,290,pitch=9,time=0)
 sheet.paste(im,((i%4)*375+5,(i//4)*335+34));d.text(((i%4)*375+12,(i//4)*335+8),e['name'],font=font,fill='#71f4e0')
 icon=im.resize((32,32),Image.Resampling.NEAREST).convert('RGBA');bg=icon.getpixel((0,0))
 for y in range(32):
  for x in range(32):
   if icon.getpixel((x,y))==bg:icon.putpixel((x,y),(0,0,0,0))
 icon.save(assets/'textures/item'/(stem+'_spawn.png'))
 if e['id'] in ('cowardly_maya','muttering_tiara','grave_beetle','heat_balance'):
  renderer.render(-22,500,480,pitch=9,time=.5,clip='action.physical').save(OUT/(stem+'-attack-preview.png'))
sheet.save(OUT/'bestiary-preview.png');print(OUT/'bestiary-preview.png')
