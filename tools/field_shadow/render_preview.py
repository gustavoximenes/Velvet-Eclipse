"""Offline preview of the actual authored geometry and UV atlas, using Igor's rasterizer."""
from pathlib import Path
import sys,json
import numpy as np
from PIL import Image,ImageDraw,ImageFont
ROOT=Path(__file__).resolve().parent
sys.path.insert(0,str(ROOT.parent/'igor'))
import render_model as renderer
OUT=ROOT/'draft'
renderer.data=json.loads((OUT/'field_shadow.geo.json').read_text(encoding='utf-8'))['minecraft:geometry'][0]
renderer.animations=json.loads((OUT/'field_shadow.animation.json').read_text(encoding='utf-8'))['animations']
font=ImageFont.truetype('C:/Windows/Fonts/segoeuib.ttf',24)
sheet=Image.new('RGB',(1500,570),'#09182b');d=ImageDraw.Draw(sheet)
for i,(texture,clip,time,label) in enumerate([('field_shadow','misc.idle',0,'SHADOW / IDLE'),('field_shadow','action.attack',.55,'ATAQUE / IMPACTO'),('red_shadow','misc.idle',1.2,'VARIANTE VERMELHA / 5%')]):
 renderer.texture=np.array(Image.open(OUT/(texture+'.png')).convert('RGBA'))/255
 sheet.paste(renderer.render(-24,490,460,pitch=14,time=time,clip=clip),(i*500+5,66))
 d.text((i*500+16,18),label,fill='#65f5e4',font=font)
d.text((16,535),'Geometria real • 120 cuboides • 58 ossos • Box UV • prévia offline',fill='#bcceda',font=ImageFont.truetype('C:/Windows/Fonts/segoeui.ttf',19))
sheet.save(OUT/'field-shadow-preview.png')
print(OUT/'field-shadow-preview.png')
