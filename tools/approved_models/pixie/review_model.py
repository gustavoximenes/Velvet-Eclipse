from PIL import Image,ImageDraw,ImageFont
import render_model as r

sheet=Image.new('RGB',(1800,1220),'#10182b')
font=ImageFont.truetype('C:/Windows/Fonts/segoeui.ttf',19)
views=[('IDLE / FRENTE',0,'air_idle',0,False),('IDLE / 3/4',-35,'air_idle',0,False),
       ('COSTAS',180,'air_idle',0,False),('ROSTO',-20,'air_idle',0,True),
       ('MAGIA / FRENTE',0,'magic_attack',1.18,False),('MAGIA / 3/4',-45,'magic_attack',1.18,False),
       ('TAPINHA / 3/4',-45,'physical_attack',.66,False),('TAPINHA / PERFIL',85,'physical_attack',.66,False)]
for i,(label,yaw,clip,time,head) in enumerate(views):
    x=i%4*450;y=i//4*610
    im=r.render(yaw,450,565,pitch=0,time=time,head=head,clip='animation.pixie.'+clip)
    sheet.paste(im,(x,y+35));ImageDraw.Draw(sheet).text((x+12,y+6),label,font=font,fill='#c1e6ed')
sheet.save(r.OUT/'review.png')
print('Review saved')
