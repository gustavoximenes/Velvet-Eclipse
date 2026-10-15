"""Revision 02: Elizabeth-style oval face planes adapted to Reload Nekomata."""
import math
import numpy as np

def build_head(m):
 B=m.bone;C=m.box;L=m.segment
 B('head','body',(0,28.4,.10))
 C('head','neck_connector',(0,29.15,.10),(1.25,2.0,1.25),'dark',feature='neck')
 # Elizabeth 05 supplies the fuller cheek/short beveled chin proportions and
 # shallow eye construction. The solids are fitted inside that authored oval
 # contour to keep their hidden ends from forming shelves in the profile.
 B('face_base','head',(0,31.5,-1.45))
 from pathlib import Path
 import json
 volume=json.loads((Path(__file__).parent/'facial_volume.json').read_text())
 for part in volume['boxes']:
  C('face_base',part['name'],part['center'],part['size'],'face_volume',part['rotation'],feature=part['feature'])
 for s,side in [(-1,'right'),(1,'left')]:
  B('eye_'+side,'face_base',(s*.75,31.61,-1.47),(-7,0,s*5))
  C('eye_'+side,side+'_eye',(s*.75,31.61,-1.52),(1.25,.625,.25),'sclera',feature='eye')
  C('eye_'+side,side+'_outer_sclera',(s*1.34,31.64,-1.52),(.25,.375,.25),'sclera',(0,0,s*22),feature='eye')
  C('eye_'+side,side+'_iris',(s*.72,31.61,-1.63),(.375,.5,.125),'iris',feature='eye')
  C('eye_'+side,side+'_pupil',(s*.72,31.59,-1.66),(.125,.375,.125),'pupil',feature='eye')
  C('eye_'+side,side+'_glint',(s*.72-.125,31.77,-1.68),(.125,.125,.125),'eye_glint',feature='eye')
  C('eye_'+side,side+'_upper_eyelid',(s*.75,31.98,-1.50),(1.25,.125,.25),'face',feature='eyelid')
  C('eye_'+side,side+'_lash',(s*.77,31.92,-1.63),(1.25,.125,.125),'mask',(0,0,s*3),feature='eyelid')
  C('eye_'+side,side+'_outer_lash',(s*1.39,31.90,-1.59),(.375,.125,.125),'mask',(0,0,s*22),feature='eyelid')
  C('eye_'+side,side+'_lower_orbit',(s*.77,31.28,-1.51),(1.125,.125,.125),'face',feature='orbit')
  B('ear_'+side,'head',(s*1.30,32.15,.1))
  C('ear_'+side,side+'_ear',(s*1.76,32.48,.04),(.875,1.875,.625),'mask',(0,-s*9,-s*49),feature='ear')
  C('ear_'+side,side+'_ear_tip',(s*2.31,32.91,.04),(.375,.875,.375),'mask',(0,0,-s*55),feature='ear')
  C('ear_'+side,side+'_ear_inner',(s*1.85,32.57,-.301),(.375,1.0,.125),'ear',(0,0,-s*49),feature='ear')
 C('face_base','nose_bridge',(0,31.32,-1.37),(.25,.875,.375),'face',(18,0,0),feature='nose')
 C('face_base','nose_tip',(0,30.98,-1.52),(.375,.25,.375),'nose',(18,0,0),pattern='nose',feature='nose')
 C('face_base','muzzle_center',(0,30.69,-1.38),(.75,.375,.125),'mask',(-8,0,0),feature='muzzle')
 C('face_base','mouth',(0,30.47,-1.42),(.625,.125,.125),'mouth',feature='mouth')
 C('face_base','lower_lip',(0,30.39,-1.40),(.5,.125,.125),'lip',feature='mouth')
 for s,side in [(-1,'right'),(1,'left')]:
  C('face_base',side+'_upper_lip',(s*.18,30.53,-1.42),(.375,.125,.125),'mask',(0,0,s*9),feature='mouth')
  C('face_base',side+'_fang',(s*.1875,30.47,-1.50),(.125,.125,.125),'claw',feature='mouth')
 # A continuous cap and broad swept locks form the hairstyle, with a central part.
 B('hair_crown','head',(0,32.8,.15))
 C('hair_crown','hair_back',(0,32.0,1.10),(2.75,2.625,1.125),'hair',pattern='back',feature='hair')
 C('hair_crown','hair_cap',(0,33.10,.35),(2.5,1.0,2.5),'hair',pattern='part',feature='hair')
 C('hair_crown','hair_top',(0,33.54,.35),(1.75,.5,1.875),'hair',pattern='part',feature='hair')
 C('hair_crown','central_part',(0,33.68,-.12),(.125,.25,1.75),'hair_root',(8,0,0),feature='hair')
 C('hair_crown','frontal_hair_root',(0,33.15,-1.13),(2.375,.625,.75),'hair',(25,0,0),feature='hair')
 for s,side in [(-1,'right'),(1,'left')]:
  C('hair_crown',side+'_swept_front',(s*.66,33.25,-1.10),(1.375,1.0,.625),'hair',(32,s*10,-s*33),pattern='sweep_'+side,feature='hair')
  C('hair_crown',side+'_swept_outer',(s*1.16,32.83,-.85),(.625,1.25,.625),'hair',(24,-s*15,-s*22),pattern='sweep_'+side,feature='hair')
  C('hair_crown',side+'_crown_bevel',(s*1.11,33.17,.22),(1.125,.875,2.5),'hair',(0,0,s*27),pattern='back',feature='hair')
  C('hair_crown',side+'_temple_hair',(s*1.34,31.95,.38),(.625,2.375,1.875),'hair',(0,-s*15,-s*8),pattern='back',feature='hair')
  B('hair_'+side,'head',(s*1.16,31.7,1.1))
  C('hair_'+side,side+'_long_hair',(s*1.28,28.96,1.30),(.875,5.75,1.0),'hair',(9,-s*12,-s*6),pattern='long',feature='hair')
  B('hair_'+side+'_tip','hair_'+side,(s*1.58,26.58,1.0))
  C('hair_'+side+'_tip',side+'_hair_end',(s*1.63,25.50,1.02),(.625,2.5,.75),'hair',(-12,0,s*14),pattern='long',feature='hair')
 # A long asymmetric lock over the chest, visible in the Reload portrait.
 B('hair_front','head',(-1.23,31.45,-.66))
 C('hair_front','front_lock_upper',(-1.72,29.43,-.90),(.5,4.25,.5),'hair',(-8,0,-7),pattern='long',feature='hair')
 B('hair_front_tip','hair_front',(-1.83,27.3,-1.15))
 C('hair_front_tip','front_lock_lower',(-1.98,26.2,-1.22),(.375,2.375,.375),'hair',(-9,0,10),pattern='long',feature='hair')

 # Translate the complete head assembly, including hair and all pivots, to shorten the neck.
 descendants={'head'}
 for b in m.bones:
  if b.get('parent') in descendants:descendants.add(b['name'])
  if b['name'] not in descendants:continue
  b['pivot'][1]-=1.75*8
  for c in b.get('cubes',[]):
   c['origin'][1]-=1.75*8
   if 'pivot' in c:c['pivot'][1]-=1.75*8
