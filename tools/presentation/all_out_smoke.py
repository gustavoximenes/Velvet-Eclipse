"""Deterministic shaded smoke atlas authored for the All-Out stage. No downloaded imagery."""
from pathlib import Path
import numpy as np
from PIL import Image,ImageFilter
root=Path(__file__).resolve().parents[2]
size=128
atlas=Image.new('RGBA',(size*4,size*2))
for variant in range(8):
    rng=np.random.default_rng(1337+variant)
    y,x=np.mgrid[-1:1:complex(size),-1:1:complex(size)]
    noise=np.zeros((size,size))
    for frequency,amplitude in [(3,.32),(7,.14),(15,.07),(30,.025)]:
        small=Image.fromarray(np.uint8(rng.random((frequency,frequency))*255))
        noise+=(np.asarray(small.resize((size,size),Image.Resampling.BICUBIC))/255-.5)*amplitude
    angle=np.arctan2(y,x)
    radius=.70+.08*np.sin(angle*5+variant)+.07*np.sin(angle*8-variant*.7)
    field=radius-np.sqrt((x+noise*.17)**2+(y+noise*.13)**2)+noise*.2
    alpha=np.clip(field/.13,0,1)**1.35
    # Smooth density gives rolling billows; differentiating hard volume intersections
    # would produce the pinched, metallic folds that do not resemble smoke.
    depth=np.exp(-(x*x+y*y)*3)*.6
    for i in range(7):
        a=i*2.399+variant;r=.28+rng.random()*.25
        cx,cy=np.cos(a)*r,np.sin(a)*r
        depth+=np.exp(-((x-cx)**2+(y-cy)**2)*15)*.25
    height=Image.fromarray(np.uint8(np.clip(depth/1.5,0,1)*255)).filter(ImageFilter.GaussianBlur(3))
    dy,dx=np.gradient(np.array(height)/255.)
    shade=np.clip(.83-dx*5-dy*8+noise*.045,.66,.98)
    rgb=np.stack([shade*.97,shade*.99,shade],axis=-1)
    pixels=np.dstack([np.uint8(rgb*255),np.uint8(alpha*255)])
    atlas.paste(Image.fromarray(pixels,'RGBA'),(variant%4*size,variant//4*size))
target=root/'src/main/resources/assets/velvet_eclipse/textures/gui/all_out_smoke.png'
target.parent.mkdir(parents=True,exist_ok=True);atlas.save(target)
print(target)
