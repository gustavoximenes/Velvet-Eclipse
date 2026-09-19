"""Offline silhouette review of the exported production Tartarus block layout."""
from pathlib import Path
import json,math
import numpy as np
from PIL import Image,ImageDraw
root=Path(__file__).resolve().parents[1]
out=root/'build/reports/dark-hour'
cells=json.loads((out/'tower.json').read_text())
palette=[(27,62,49),(58,108,80),(91,244,177),(27,62,49)]
faces=[((-1,0,0),[(0,0,0),(0,0,1),(0,1,1),(0,1,0)],.67),((1,0,0),[(1,0,1),(1,0,0),(1,1,0),(1,1,1)],.88),((0,1,0),[(0,1,0),(0,1,1),(1,1,1),(1,1,0)],1.12),((0,0,1),[(0,0,1),(1,0,1),(1,1,1),(0,1,1)],1)]
def render(path,entrance=False):
    w,h=(1050,760) if entrance else (760,1160)
    chosen=[c for c in cells if not entrance or c['y']<21]
    occupied={(c['x'],c['y'],c['z']) for c in chosen}
    angle=math.radians(25 if entrance else 15);ca,sa=math.cos(angle),math.sin(angle)
    scale=22 if entrance else 3.7
    def project(p):
        x,y,z=p;u=x*ca-z*sa;depth=x*sa+z*ca
        return w*.5+u*scale,h*(.72 if entrance else .965)-y*scale-depth*scale*(.3 if entrance else .16),depth+y*.05
    polygons=[]
    for c in chosen:
        at=np.array([c['x'],c['y'],c['z']])
        for normal,verts,light in faces:
            if tuple(at+normal) in occupied:continue
            q=[project(at+v) for v in verts]
            color=tuple(min(255,int(v*light)) for v in palette[c['material']])
            polygons.append((sum(v[2] for v in q)/4,[(v[0],v[1]) for v in q],color))
    im=Image.new('RGB',(w,h),'#05150f');d=ImageDraw.Draw(im)
    for depth,q,color in sorted(polygons):d.polygon(q,fill=color)
    d.text((20,20),'TARTARUS | production block layout | offline preview',fill='#a5ead0')
    im.save(out/path)
render('tower-preview.png');render('entrance-preview.png',True)
print(out)
