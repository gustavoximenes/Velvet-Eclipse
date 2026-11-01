"""Isolated leg inspection; actual complete rig, only upper-body surfaces hidden."""
import json
import numpy as np
from PIL import Image
import render_model as r
from present_model import text,BG,CYAN,save_media
from animate_model import LENGTHS

def main():
 for bone in r.data['bones']:
  if bone['name']!='hips' and not any(x in bone['name'] for x in ['_thigh','_shin','_foot','_knee_bridge','_ankle_bridge']):bone['cubes']=[]
 r._bounds_source=None
 kinds=[('ground_idle','REPOUSO'),('physical_attack','FÍSICO / PROPOSTA'),('magic_attack','MAGIA / ADAPTAÇÃO')]
 allbounds={}
 for yaw in [0,80]:
  pts=np.concatenate([r.bounds_vertices(yaw,3,float(t),'animation.angel.'+kind) for kind,_ in kinds for t in np.linspace(0,LENGTHS[kind],37)])
  allbounds[yaw]=(pts.min(0)-4,pts.max(0)+4)
 frames=[]
 for kind,label in kinds:
  for t in np.arange(0,LENGTHS[kind],.1):
   im=Image.new('RGB',(960,720),BG);text(im,(22,12),'ANGEL / JUNTAS / '+label,23,b=True)
   for col,yaw in enumerate([0,80]):
    im.paste(r.render(yaw,464,610,pitch=3,time=float(t),bounds=allbounds[yaw],clip='animation.angel.'+kind),(8+col*480,60))
   text(im,(22,681),f'{t:.1f} s · Mesma pose em duas câmeras · quadril, joelhos e tornozelos',17,CYAN)
   frames.append(im)
 save_media('joints',frames,100)
 frames[12].save(r.OUT/'angel-joints-03.png')
 print('PASS: 104 joint-inspection frames from the final rig.',flush=True)
if __name__=='__main__':main()
