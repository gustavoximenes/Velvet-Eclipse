"""Inspect modeled anatomy from fixed directions, without concealing back surfaces."""
from PIL import Image,ImageDraw,ImageFont
import render_model as r

im=Image.new('RGB',(1800,1100),'#10182b')
font=ImageFont.truetype('C:/Windows/Fonts/segoeui.ttf',19)
for row,(bare,clay) in enumerate([(False,False),(True,True)]):
    for col,yaw in enumerate((0,-35,-90,180)):
        panel=r.render(yaw,444,505,head=True,pitch=0,bare=bare,clay=clay)
        im.paste(panel,(col*450+3,row*550+39))
        ImageDraw.Draw(im).text((col*450+15,row*550+9),f'{yaw}° / '+('geometria' if bare else 'textura'),font=font,fill='#d3e9e7')
im.save(r.OUT/'contour-review.png')
