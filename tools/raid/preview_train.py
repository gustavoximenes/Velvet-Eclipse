"""Preview actual Java-emitted train blocks and runtime textures, not a game screenshot."""
from pathlib import Path
import json
import numpy as np
from PIL import Image, ImageDraw, ImageFont
root=Path(__file__).resolve().parents[2]
out=root/'build/reports/raid'
a=root/'src/main/resources/assets/velvet_eclipse'
cells=json.loads((out/'train-layout.json').read_text())
names=['air','floor','wall','window','seat','metal','lamp','rail'];grid=np.zeros((19,9,126),dtype=np.int8)
for c in cells:grid[c['x']+9,c['y'],c['z']]=names.index(c['material'])
w,h=900,500
y,x=np.mgrid[0:h,0:w];rays=np.stack(((x+.5-w/2)/540,-(y+.5-h/2)/540-.045,np.ones_like(x)),axis=-1).reshape(-1,3)
origin=np.array([.5,2.62,5.5]);pos=np.tile(np.floor(origin).astype(int),(w*h,1));step=np.where(rays>0,1,-1);delta=1/np.maximum(np.abs(rays),1e-8)
side=np.where(step>0,pos+1-origin,origin-pos)*delta
active=np.ones(w*h,dtype=bool);rgb=np.tile([7,21,20],(w*h,1)).astype(float)
textures={i:np.array(Image.open(a/'textures/block'/('train_'+name+'.png')).convert('RGB')) for i,name in enumerate(names) if i}
for _ in range(190):
 ids=np.flatnonzero(active)
 if not len(ids):break
 axes=side[ids].argmin(axis=1);d=side[ids,axes].copy();side[ids,axes]+=delta[ids,axes];pos[ids,axes]+=step[ids,axes]
 v=pos[ids];valid=(v[:,0]>=-9)&(v[:,0]<=9)&(v[:,1]>=0)&(v[:,1]<=8)&(v[:,2]>=0)&(v[:,2]<=125)
 active[ids[~valid]]=False;ids=ids[valid];v=v[valid];d=d[valid];axes=axes[valid]
 materials=grid[v[:,0]+9,v[:,1],v[:,2]]
 # The rail's actual model is a narrow cuboid, intersect that box instead of a full voxel.
 rail=np.flatnonzero(materials==7)
 if len(rail):
  ri=ids[rail];lo=v[rail]+[.375,0,.375];hi=v[rail]+[.625,1,.625]
  t1=(lo-origin)/rays[ri];t2=(hi-origin)/rays[ri];entry=np.minimum(t1,t2).max(axis=1);leave=np.maximum(t1,t2).min(axis=1)
  hit=leave>=entry
  materials[rail[~hit]]=0;d[rail[hit]]=entry[hit];axes[rail[hit]]=np.minimum(t1,t2)[hit].argmax(axis=1)
 keep=materials>0;ids=ids[keep];d=d[keep];axes=axes[keep];materials=materials[keep]
 point=origin+rays[ids]*d[:,None];fract=point-np.floor(point)
 uv=np.empty((len(ids),2));uv[:,0]=np.where(axes==0,fract[:,2],fract[:,0]);uv[:,1]=np.where(axes==1,fract[:,2],1-fract[:,1]);uv=np.minimum(31,(uv*32).astype(int))
 for m in np.unique(materials):
  mask=materials==m;cols=textures[int(m)][uv[mask,1],uv[mask,0]].astype(float)
  shade=np.where(axes[mask]==1,.92,.77)*(0.75+0.25*np.exp(-d[mask]/25))
  if m==6:shade[:]=1
  rgb[ids[mask]]=cols*shade[:,None]*[.86,1.10,.9]
 active[ids]=False
im=Image.fromarray(np.clip(rgb.reshape(h,w,3),0,255).astype('uint8'))
d=ImageDraw.Draw(im);font=ImageFont.truetype(str(a/'font/body.ttf'),14)
d.rectangle((0,0,w,27),fill='#061727');d.text((12,4),'MONORAIL / PRÉVIA OFFLINE DA GEOMETRIA / iluminação aproximada',font=font,fill='#a5f4ed')
im.save(out/'train.png')
dialog=Image.open(out/'dialogue.png').convert('RGBA');composite=im.convert('RGBA');composite.alpha_composite(dialog);composite.save(out/'train-dialogue.png')
print(out/'train.png')
