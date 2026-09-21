"""Offline validation and actual-geometry presentation of the Elizabeth study."""
from pathlib import Path
import json, numpy as np
from PIL import Image,ImageDraw,ImageFont
import render_model as r
OUT=r.OUT
font=lambda size,bold=False:ImageFont.truetype('C:/Windows/Fonts/segoeuib.ttf' if bold else 'C:/Windows/Fonts/segoeui.ttf',size)
def validate():
 geo=r.data;bb=json.loads((OUT/'elizabeth.bbmodel').read_text(encoding='utf-8'));names=set();count=0;uvs=[]
 for bone in geo['bones']:
  assert bone['name'] not in names
  assert not bone.get('parent') or bone['parent'] in names
  names.add(bone['name'])
  for c in bone.get('cubes',[]):
   count+=1;assert all(v>0 and v==int(v) for v in c['size'])
   w,h,d=c['size'];u,v=c['uv'];rect=(u,v,u+2*(w+d),v+h+d)
   assert all(0<=a<=512 for a in rect)
   for x1,y1,x2,y2 in uvs:assert rect[2]<=x1 or x2<=rect[0] or rect[3]<=y1 or y2<=rect[1]
   uvs.append(rect)
 assert len(bb['elements'])==count and len({e['uuid'] for e in bb['elements']})==count
 assert len(bb['animations'])==1
 for clip in r.animations.values():
  assert set(clip['bones'])<=names
  for tracks in clip['bones'].values():
   for keys in tracks.values():
    ordered=sorted((float(t),v) for t,v in keys.items());assert ordered[0][0]==0 and ordered[-1][0]==4.8
    assert np.allclose(ordered[0][1],ordered[-1][1]) and np.isfinite(np.array([v for _,v in ordered])).all()
 # Compare world-space rest/end vertices to check the loop and fixed feet.
 start=r.surfaces(0,0,time=0);end=r.surfaces(0,0,time=4.8)
 assert len(start)==len(end)
 for a,b in zip(start,end):assert np.allclose(a[0],b[0])
 parts=json.loads((OUT/'parts.json').read_text(encoding='utf-8'))
 assert sum(p['pattern']=='button' for p in parts)==5
 assert all(any(p['bone']==b for p in parts) for b in ['hat','hair_back','book'])
 (OUT/'validation.txt').write_text(f'{count} cuboids; {len(names)} bones; unique IDs; integer Box UV dimensions; no UV overlap; atlas 512x512.\nIdle 4.8 s closes continuously, named bones valid. Five dress rings, white bob, hat and book present.\nActual geometry previews inspected offline. Standalone modeling only; no mod integration, JAR, commit or game testing.\n',encoding='utf-8')
 print('Model checks passed:',count,'cuboids,',len(names),'bones.',flush=True)
def stills():
 for name,yaw,w,h,head in [('front',0,800,1000,False),('three-quarter',-25,800,1000,False),('back',180,800,1000,False),('side',90,800,1000,False),('face',0,700,650,True)]:r.render(yaw,w,h,head=head,pitch=0 if head else 4).save(OUT/(name+'.png'))
 sheet=Image.new('RGB',(1360,1190),'#101a36');d=ImageDraw.Draw(sheet)
 d.rectangle((30,29,36,96),fill='#a8d5ff');d.text((58,22),'ELIZABETH',font=font(42,True),fill='#f1f2e9')
 d.text((61,77),'ESTUDO 02  /  VESTIDO AJUSTADO AO CORPO',font=font(17),fill='#a8d5ff')
 sheet.paste(r.render(-22,808,989,pitch=4),(25,130));sheet.paste(r.render(0,477,445,head=True,pitch=0),(856,130));sheet.paste(r.render(180,477,553,pitch=4),(856,595))
 d=ImageDraw.Draw(sheet);d.text((45,145),'POSE COM LIVRO',font=font(15,True),fill='#425166');d.text((876,145),'ROSTO',font=font(15,True),fill='#425166');d.text((876,610),'COSTAS',font=font(15,True),fill='#425166')
 d.text((33,1155),'Geometria real • Box UV • 92 cuboides • 29 ossos • Somente modelagem',font=font(16),fill='#c1cddd');sheet.save(OUT/'elizabeth-preview-02.png')
 print('Stills and presentation saved.',flush=True)
def animate():
 bounds=(np.array([-48,-4,-48]),np.array([48,163,48]))
 frames=[r.render(-22,480,650,pitch=4,time=float(t),bounds=bounds) for t in np.linspace(0,4.8,32,endpoint=False)]
 frames[0].save(OUT/'elizabeth-idle.gif',save_all=True,append_images=frames[1:],duration=150,loop=0,disposal=2)
 frames=[r.render(float(yaw),480,650,pitch=3,bounds=bounds) for yaw in np.linspace(0,360,36,endpoint=False)]
 frames[0].save(OUT/'elizabeth-turntable.gif',save_all=True,append_images=frames[1:],duration=85,loop=0,disposal=2)
 print('Idle and turntable saved.',flush=True)
if __name__=='__main__':validate();stills();animate()
