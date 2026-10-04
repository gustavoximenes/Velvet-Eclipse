from pathlib import Path
from PIL import Image, ImageDraw, ImageFont
import render_model as r

font=ImageFont.truetype('C:/Windows/Fonts/segoeui.ttf',20)
sheet=Image.new('RGB',(1680,1000),'#131e35')
views=[('Corpo • frente',0,False,False,False),('Rosto • frente',0,True,False,False),
       ('Rosto • 3/4',-35,True,False,False),('Perfil',90,True,False,False),
       ('Corpo • 3/4',-25,False,False,False),('Rosto sem cabelo',-25,True,False,True),
       ('Geometria sem textura',-30,True,True,True),('Costas',180,False,False,False)]
for i,(label,yaw,head,clay,bare) in enumerate(views):
    im=r.render(yaw,420,458,head=head,pitch=0,clay=clay,bare=bare)
    x,y=i%4*420,i//4*500
    sheet.paste(im,(x,y+35))
    ImageDraw.Draw(sheet).text((x+12,y+5),label,font=font,fill='#d6e9f3')
sheet.save(r.OUT/'review.png')
print('Saved review.png',flush=True)
