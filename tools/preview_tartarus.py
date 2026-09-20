"""Offline perspective preview of production blocks, with front wall/ceiling cut away."""
from pathlib import Path
import json, math
import numpy as np
from PIL import Image, ImageDraw
out=Path(__file__).resolve().parents[1]/'build/reports/tartarus'
colors={'ivory':(202,213,212),'tile':(67,90,125),'tile_dark':(42,59,82),'wall':(22,34,55),'column':(94,111,131),'gold':(166,140,68),'carpet':(29,168,218),'clock':(181,218,164),'hand':(29,93,54),'border':(149,199,172),'seal':(13,32,49),'window':(30,190,73),'azure':(27,152,205),'violet':(118,67,204),'trim':(57,100,77),'blue_stairs':(29,168,218),'stairs':(57,100,77),'velvet_door':(25,66,245),'forward':(0,0,0),'back':(0,0,0),'exit':(0,0,0),'summit':(0,0,0)}
faces=[((1,0,0),[(1,0,0),(1,0,1),(1,1,1),(1,1,0)],.8),((-1,0,0),[(0,0,1),(0,0,0),(0,1,0),(0,1,1)],.65),((0,1,0),[(0,1,0),(1,1,0),(1,1,1),(0,1,1)],1.05),((0,0,1),[(1,0,1),(0,0,1),(0,1,1),(1,1,1)],.94),((0,0,-1),[(0,0,0),(1,0,0),(1,1,0),(0,1,0)],.7)]
def hall():
    cells=[c for c in json.loads((out/'hall.json').read_text()) if c['y']<34 and not(c['z']>=29 and c['y']>0)]
    occupied={(c['x'],c['y'],c['z']) for c in cells};camera=np.array((18,19,65.));target=np.array((0,13,-2.))
    f=target-camera;f/=np.linalg.norm(f);right=np.cross(f,(0,1,0));right/=np.linalg.norm(right);up=np.cross(right,f)
    def project(p):
        q=p-camera;z=np.dot(q,f);return 700+np.dot(q,right)*950/z,430-np.dot(q,up)*950/z,z
    polygons=[]
    for c in cells:
        at=np.array((c['x'],c['y'],c['z']));base=colors[c['material']]
        for n,v,light in faces:
            if tuple(at+n) in occupied or np.dot(camera-at,n)<=0:continue
            verts=[project(at+p) for p in v]
            if min(p[2] for p in verts)<=1:continue
            polygons.append((sum(p[2] for p in verts),[(p[0],p[1]) for p in verts],tuple(min(255,int(x*light)) for x in base)))
    im=Image.new('RGB',(1400,900),'#070e18');draw=ImageDraw.Draw(im)
    for _,verts,color in sorted(polygons,reverse=True):draw.polygon(verts,fill=color)
    draw.text((22,22),'TARTARUS HALL | production blocks | offline cutaway',fill='white');im.save(out/'hall-preview.png')
def floor():
    im=Image.new('RGB',(1100,1100),'#050b12');d=ImageDraw.Draw(im)
    for c in sorted(json.loads((out/'floor.json').read_text()),key=lambda c:c['y']):
        if c['y']==13:continue
        x=550+c['x']*10;z=550+c['z']*10;d.rectangle((x,z,x+9,z+9),fill=colors[c['material']])
    d.text((22,22),'FLOOR 7 | production blocks | ceiling removed',fill='white');im.save(out/'floor-preview.png')
hall();floor();print(out)
