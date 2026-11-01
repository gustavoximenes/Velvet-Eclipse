"""Direct geometry comparison, identical camera and framing for both revisions."""
from pathlib import Path
import importlib.util,copy
import numpy as np
from PIL import Image
import render_model as after
from present_model import text,BG,CYAN
ROOT=Path(__file__).resolve().parent
spec=importlib.util.spec_from_file_location('angel_before',ROOT.parent/'angel-study-02/render_model.py')
before=importlib.util.module_from_spec(spec);spec.loader.exec_module(before)
out=Image.new('RGB',(1440,1260),BG)
text(out,(26,15),'ANGEL / POLIMENTO DE PERNAS E MÃOS',31,b=True)
text(out,(26,61),'Mesma câmera e escala em cada par · geometria real das duas revisões',18,CYAN)
for row,region in enumerate(['hands','legs']):
 sources=[];points=[]
 for mod in [before,after]:
  sources.append(copy.deepcopy(mod.data))
  for b in mod.data['bones']:
   keep=any(v in b['name'] for v in ['_thigh','_shin','_foot','_knee_bridge','_ankle_bridge']) if region=='legs' else ((b['name'] in mod.head_bones and not b['name'].startswith('ribbon_')) or any(v in b['name'] for v in ['_upper_arm','_forearm','_hand','_finger','_thumb','_elbow_bridge']))
   if not keep:b['cubes']=[]
  points.append(np.concatenate([q[0] for q in mod.surfaces(18,3)]))
 points=np.concatenate(points);bounds=(points.min(0)-4,points.max(0)+4)
 for col,mod in enumerate([before,after]):
  x=col*720+10;y=140+row*555
  out.paste(mod.render(18,700,510,pitch=3,bounds=bounds),(x,y))
  text(out,(x+16,y-33),('02 / ANTES' if col==0 else '03 / CORRIGIDO')+' · '+('BRAÇOS E MÃOS' if row==0 else 'PERNAS E PÉS'),20,CYAN,True)
  mod.data=sources[col]
out.save(ROOT/'draft/angel-polish-comparison-03.png')
print('PASS: old/new geometry, matched camera and bounds.')
