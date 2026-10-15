"""Compare the preserved 01 and new 02 geometry at the same pixel scale."""
from pathlib import Path
import subprocess,sys
import numpy as np
from PIL import Image,ImageDraw,ImageFont
ROOT=Path(__file__).resolve().parent;OUT=ROOT/'draft'

def face_worker(folder,path):
 sys.path.insert(0,str(folder))
 import render_model as r
 allowed={'head','face_base','hair_crown','eye_left','eye_right','ear_left','ear_right'}
 im=Image.new('RGB',(1000,560),'#dfe7e9')
 for i,yaw in enumerate([0,25]):
  pts=np.concatenate([q for q,uv,n,b in r.surfaces(yaw,0,head=True) if b in allowed])
  center=(pts.min(0)+pts.max(0))/2
  # Fixed world-unit dimensions make the shorter chin and neck measurable.
  bounds=(center-np.array([30,34,30]),center+np.array([30,34,30]))
  im.paste(r.render(yaw,500,560,pitch=0,bounds=bounds),(i*500,0))
 im.save(path)

def main():
 for version in ('01','02'):
  subprocess.run([sys.executable,str(Path(__file__).resolve()),'--worker',str(ROOT.parent/('nekomata-study-'+version)),str(OUT/f'work-compare-{version}.png')],check=True)
 im=Image.new('RGB',(1600,600),'#101b2e');d=ImageDraw.Draw(im)
 font=lambda n:ImageFont.truetype('C:/Windows/Fonts/segoeui.ttf',n)
 d.text((24,12),'NEKOMATA  /  ROSTO ANTES E DEPOIS',font=font(30),fill='#eef6f5')
 col=0
 for angle in range(2):
  for version,label in [('01','ANTERIOR · 01'),('02','REVISADO · 02')]:
   source=Image.open(OUT/f'work-compare-{version}.png').crop((angle*500,0,(angle+1)*500,560))
   source.thumbnail((390,470),Image.Resampling.LANCZOS)
   im.paste(source,(5+col*400,68));d.text((22+col*400,80),label,font=font(18),fill='#101b2e');col+=1
 d.text((24,548),'Mesma câmera e escala. Queixo mais curto, olhos em volume e pescoço reduzido; poses atualizadas.',font=font(20),fill='#a6dce5')
 im.save(OUT/'nekomata-face-comparison-02.png')
 # These are small reference excerpts for analysis, not redistributed videos.
 im=Image.new('RGB',(1440,850),'#101b2e');d=ImageDraw.Draw(im)
 d.text((24,12),'REFERÊNCIA OBSERVADA  /  PERSONA 3 RELOAD',font=font(29),fill='#eef6f5')
 rows=[('POWER SLASH',[(5,10.488),(10,10.696),(16,10.946),(21,11.154)],'physical-detail', (170,180,440,480)),
       ('AGI',[(5,333.938),(13,334.271),(20,334.562),(30,334.979)],'magic-detail',(130,0,650,480))]
 for row,(label,frames,prefix,crop) in enumerate(rows):
  y=75+row*345;d.text((24,y),label,font=font(23),fill='#a6dce5')
  for col,(index,t) in enumerate(frames):
   source=Image.open(ROOT/f'work/{prefix}-{index:03}.jpg').crop(crop)
   source.thumbnail((335,270),Image.Resampling.LANCZOS)
   im.paste(source,(20+col*360,y+37));d.text((22+col*360,y+310),f'{t:.3f} s',font=font(17),fill='#eef6f5')
 d.text((24,790),'ReVersed JRPGs · Persona 3 ALL Skills Showcase & Comparison · youtube.com/watch?v=TZap-dV5Oyo',font=font(18),fill='#a6dce5')
 im.save(ROOT/'references/reload-attacks-observed.jpg',quality=92)
 print('Face comparison and verified reference excerpts saved.')

if __name__=='__main__':
 if '--worker' in sys.argv:face_worker(Path(sys.argv[2]),Path(sys.argv[3]))
 else:main()
