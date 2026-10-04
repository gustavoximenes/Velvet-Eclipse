"""Inspect the face contour with real geometry, at a larger scale."""
import numpy as np
from PIL import Image,ImageDraw,ImageFont
import render_model as r

out=Image.new('RGB',(1600,850),'#111b31')
f=ImageFont.truetype('C:/Windows/Fonts/segoeui.ttf',19)
bounds=(np.array([-26,292,-32]),np.array([26,345,32]))
for i,(yaw,label) in enumerate([(0,'FRENTE'),(-35,'TRÊS QUARTOS'),(60,'PERFIL 60°'),(90,'PERFIL 90°')]):
    out.paste(r.render(yaw,400,780,head=True,pitch=0,bounds=bounds,bare=True),(i*400,40))
    ImageDraw.Draw(out).text((i*400+15,9),label,font=f,fill='white')
out.save(r.OUT/'contour-review.png')
