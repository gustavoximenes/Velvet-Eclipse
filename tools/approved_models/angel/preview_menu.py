"""New editable Angel in the actual PersonaSheetDesign layout. Offline only."""
from pathlib import Path
import json,hashlib,subprocess,sys
import numpy as np
from PIL import Image,ImageDraw,ImageFont
import render_model as r
from present_model import save_media,FF

O=r.OUT;YAW=18;PITCH=3.4377
def bounds():
 points=np.concatenate([r.bounds_vertices(YAW,PITCH,float(t)) for t in np.linspace(0,4.8,49)])
 return points.min(0)-5,points.max(0)+5

def compose(t,bb,plain=False):
 # The actual screen's viewport is x=396,y=8,w=495,h=444 on a 900x500 canvas.
 # Reserve the full width for this winged model, with constant idle bounds.
 bg=Image.open(O/'menu-background.png').convert('RGBA')
 sprite=r.render(YAW,990,888,pitch=PITCH,time=t,bounds=bb,transparent=True)
 bg.alpha_composite(sprite,(792,16))
 if not plain:bg.alpha_composite(Image.open(O/'menu-foreground.png').convert('RGBA'))
 return bg.convert('RGB')

def main():
 bb=bounds();im=compose(0,bb);im.save(O/'angel-menu-03.png')
 compose(0,bb,True).save(O/'angel-menu-model-only-03.png')
 if '--still' in sys.argv:return
 frames=[];occupied=[]
 for i in range(72):
  t=i/15;frames.append(compose(t,bb).resize((1260,700),Image.Resampling.LANCZOS))
  q=r.bounds_vertices(YAW,PITCH,t);s=min(940/(bb[1][0]-bb[0][0]),844/(bb[1][1]-bb[0][1]));c=(bb[0]+bb[1])/2
  pixels=np.c_[1287+(q[:,0]-c[0])*s,460-(q[:,1]-c[1])*s]
  assert pixels[:,0].min()>=792 and pixels[:,0].max()<=1782 and pixels[:,1].min()>=16 and pixels[:,1].max()<=904
  occupied.append([*pixels.min(0),*pixels.max(0)])
 # GIF centiseconds: 70+70+60 ms repeats for an exact 4.8 s loop at mean 15 fps.
 palette=Image.fromarray(np.concatenate([np.array(f.resize((630,350))) for f in frames[::12]],axis=0)).quantize(256,method=Image.Quantize.MEDIANCUT)
 quantized=[f.quantize(palette=palette,dither=Image.Dither.NONE) for f in frames]
 path=O/'angel-menu-03.gif';quantized[0].save(path,save_all=True,append_images=quantized[1:],duration=[70,70,60]*24,loop=0,optimize=False,disposal=2)
 subprocess.run([str(FF),'-y','-v','error','-i',str(path),'-an','-c:v','libx264','-crf','18','-pix_fmt','yuv420p','-movflags','+faststart',str(path.with_suffix('.mp4'))],check=True)
 src=Path(__file__).resolve().parents[2]/'velvet-eclipse/src/main/java/com/gx/velveteclipse/client/menu/PersonaSheetDesign.java'
 report={'status':'passed','offline':True,'integrated':False,'layout_source':str(src),'layout_sha256':hashlib.sha256(src.read_bytes()).hexdigest(),'canvas':[1800,1000],'viewport':[792,16,990,888],'yaw':YAW,'pitch':PITCH,'idle_frames':72,'constant_scale':True,'proposed_framing':True,'occupied_bounds':occupied,'stats_overlay':'Production foreground drawn after model, as in actual status sheet.','source_hashes':{p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in [O/'angel.geo.json',O/'angel.animation.json',O/'angel.png']}}
 (O/'menu-validation.json').write_text(json.dumps(report,indent=2))
 print('PASS: production menu, 72-frame idle, fixed framing, bounds checked.',flush=True)
if __name__=='__main__':main()
