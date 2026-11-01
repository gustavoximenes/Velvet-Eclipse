"""Author original ground-space entry labels with the mod's existing display typeface."""
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont
R=Path(__file__).resolve().parents[2]/'src/main/resources/assets/velvet_eclipse'
O=R/'textures/gui/entry';O.mkdir(parents=True,exist_ok=True)
font=ImageFont.truetype(str(R/'font/display.ttf'),112)
for name,first,color in [('sees','S.E.E.S.','#1643ff'),('shadow','SHADOW','#ba234d')]:
 for lang in ('en','pt'):
  words=(first,'ADVANTAGE') if lang=='en' else ('VANTAGEM', 'DA EQUIPE' if name=='sees' else 'INIMIGA')
  im=Image.new('RGBA',(1024,256));d=ImageDraw.Draw(im);d.polygon([(30,12),(1023,12),(994,242),(0,242)],fill=color)
  for text,y in zip(words,(8,132)):
   layer=Image.new('RGBA',(1300,150));pen=ImageDraw.Draw(layer);box=pen.textbbox((0,0),text,font=font);pen.text((0,-box[1]),text,font=font,fill='white')
   layer=layer.crop((0,0,box[2],box[3]-box[1])).resize((885,100));im.alpha_composite(layer,(55,y))
  im.save(O/f'{name}_{lang}.png')
print('Built four original localized entry decals')
