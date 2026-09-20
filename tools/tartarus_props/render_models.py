"""Render the authored models with their real pixel atlas, no synthesized artwork."""
from pathlib import Path
import sys,json
import numpy as np
from PIL import Image,ImageDraw,ImageFont
ROOT=Path(__file__).resolve().parent
sys.path.insert(0,str(ROOT.parent/'igor'))
import render_model as render
out=ROOT/'draft';sheet=Image.new('RGB',(1500,980),'#101c2d');d=ImageDraw.Draw(sheet)
font=lambda n:ImageFont.truetype('C:/Windows/Fonts/segoeuib.ttf',n)
for i,(name,label) in enumerate([('terminal','TERMINAL'),('treasure','TESOURO'),('monad','MONAD')]):
 stem='tartarus_'+name
 render.data=json.loads((out/(stem+'.geo.json')).read_text(encoding='utf-8'))['minecraft:geometry'][0]
 render.texture=np.array(Image.open(out/(stem+'.png')).convert('RGBA'))/255
 render.animations=json.loads((out/(stem+'.animation.json')).read_text(encoding='utf-8'))['animations']
 sheet.paste(render.render(-24,480,580,pitch=9),(10+i*500,75));sheet.paste(render.render(0,280,245,pitch=0),(110+i*500,675))
 d.text((28+i*500,20),label,font=font(28),fill='#abffcf')
d.text((30,943),'Geometria real • Box UV • Frente e três quartos • Prévia offline',font=font(20),fill='#cfdded')
sheet.save(out/'props-preview.png');print(out/'props-preview.png')
