"""Original Nekomata study, native cuboids, uniform 8x authoring grid, North facing."""
import numpy as np

TAIL=[(0,18.7,1.05),(0,18.65,2.6),(.2,17.7,4.5),(.7,15.8,6.5),(1.3,13.25,8.25),(2.5,10.5,9.9),(4.1,8.55,10.6),(6.0,8.0,10.6),(7.65,8.15,9.8),(8.8,8.7,8.9)]

def geometry(Model):
 m=Model();B=m.bone;C=m.box;L=m.segment
 B('nekomata');B('hips','nekomata',(0,18.6,0));B('body','hips',(0,20.5,0))
 C('hips','pelvis',(0,18.85,.08),(3.5,2.125,2.75),'dark',feature='pelvis')
 C('body','waist',(0,21.0,0),(2.625,3.0,2.25),'dark',pattern='satin',feature='body')
 C('body','ribcage',(0,24.0,.05),(3.25,3.5,2.375),'fur',pattern='chest',feature='body')
 C('body','upper_chest',(0,26.0,.10),(3.75,1.25,2.375),'fur',pattern='chest',feature='body')
 C('body','neck',(0,27.6,.1),(1.25,2.75,1.25),'dark',feature='neck')
 C('body','neck_fur',(0,27.9,.1),(1.5,1.375,1.5),'fur',pattern='tuft',feature='fur')
 C('body','collar_front',(0,27.0,-.73),(1.75,.75,.5),'fur',(-12,0,0),pattern='tuft',feature='fur')
 C('body','collar_back',(0,27.1,.75),(2.125,1.0,.875),'fur',pattern='tuft',feature='fur')
 for s,side in [(-1,'right'),(1,'left')]:
  C('hips',side+'_hip_bevel',(s*1.27,18.8,.05),(1.0,2.0,2.625),'dark',(0,0,s*12),feature='pelvis')
  C('body',side+'_rib_bevel',(s*1.40,24.0,.05),(.875,3.75,2.25),'fur',(0,0,-s*12),pattern='fur',feature='body')
  C('body',side+'_breast_fur',(s*.82,25,-1.05),(1.625,1.875,.75),'fur',(0,-s*12,-s*7),pattern='chest',feature='fur')
  C('body',side+'_waist_fur',(s*1.14,22.5,-.35),(.75,1.625,1.625),'fur',(0,0,-s*18),pattern='tuft',feature='fur')
  C('body',side+'_collar_tuft',(s*.76,26.95,-.35),(.875,1.25,1.5),'fur',(0,0,s*24),pattern='tuft',feature='fur')
  C('body',side+'_shoulder',(s*2.0,26.0,.1),(1.75,1.625,1.875),'fur',(0,0,-s*13),pattern='fur',feature='shoulder')
  B(side+'_upper_arm','body',(s*2.30,25.85,.1))
  C(side+'_upper_arm',side+'_upper_arm',(s*2.30,23.88,.1),(1.375,4.5,1.5),'fur',pattern='fur',feature='arm')
  C(side+'_upper_arm',side+'_shoulder_tuft',(s*3,25.8,.45),(.875,1.75,1.375),'fur',(0,0,s*27),pattern='tuft',feature='fur')
  B(side+'_forearm',side+'_upper_arm',(s*2.30,21.6,.1))
  C(side+'_forearm',side+'_forearm',(s*2.30,19.56,.1),(1.125,4.5,1.25),'fur',pattern='fur',feature='arm')
  C(side+'_forearm',side+'_cuff',(s*2.30,17.8,.1),(1.5,1.125,1.5),'fur',pattern='tuft',feature='fur')
  for j,dz in enumerate((-.38,.38)):
   C(side+'_forearm',side+'_cuff_tuft_'+str(j),(s*2.75,18.2,.1+dz),(.625,1.5,.625),'fur',(0,0,s*21),pattern='tuft',feature='fur')
  B(side+'_hand',side+'_forearm',(s*2.30,17.475,.1))
  C(side+'_hand',side+'_palm',(s*2.30,16.95,.1),(1.125,1.375,.625),'dark',pattern='satin',feature='hand')
  for j in range(4):
   x=s*2.30+(j-1.5)*.27
   B(side+'_finger_'+str(j),side+'_hand',(x,16.38,.1))
   length=.875 if j in (1,2) else .75
   C(side+'_finger_'+str(j),side+'_finger_'+str(j),(x,16.38-length/2,.1),(.25,length+.125,.375),'dark',feature='finger')
   C(side+'_finger_'+str(j),side+'_claw_'+str(j),(x,16.38-length+.03,-.02),(.125,.375,.125),'claw',(15,0,0),feature='claw')
  B(side+'_thumb',side+'_hand',(s*2.91,17.1,.1))
  C(side+'_thumb',side+'_thumb',(s*2.92,16.76,.1),(.375,.875,.5),'dark',(0,0,s*28),feature='finger')
  C(side+'_thumb',side+'_thumb_claw',(s*3.1,16.44,-.05),(.125,.375,.125),'claw',(10,0,s*28),feature='claw')
  B(side+'_thigh','hips',(s*1.05,18.50,.05))
  C(side+'_thigh',side+'_thigh_top',(s*1.05,16.83,.05),(1.875,3.875,2.0),'dark',pattern='satin',feature='leg')
  C(side+'_thigh',side+'_thigh',(s*1.05,13.20,.05),(1.625,4.625,1.75),'dark',pattern='satin',feature='leg')
  B(side+'_shin',side+'_thigh',(s*1.05,10.75,.05))
  C(side+'_shin',side+'_calf',(s*1.05,8.83,.1),(1.5,4.375,1.75),'dark',pattern='satin',feature='leg')
  C(side+'_shin',side+'_shin',(s*1.05,5.40,.05),(1.125,3.75,1.375),'dark',pattern='satin',feature='leg')
  C(side+'_shin',side+'_ankle',(s*1.05,3.48,.05),(1.0,.875,1.125),'dark',feature='leg')
  B(side+'_foot',side+'_shin',(s*1.05,3.0,.05))
  C(side+'_foot',side+'_instep',(s*1.05,2.65,-.55),(1.125,1.125,2.25),'dark',(-12,0,0),pattern='satin',feature='foot')
  C(side+'_foot',side+'_toe',(s*1.05,2.31,-1.78),(1.125,.625,1.0),'dark',feature='foot')
  for j in range(3):
   C(side+'_foot',side+'_toe_claw_'+str(j),(s*1.05+(j-1)*.33,2.32,-2.30),(.125,.25,.375),'claw',feature='claw')
  C(side+'_foot',side+'_heel',(s*1.05,2.25,.35),(.875,.875,.875),'dark',feature='foot')
 # Segmented pale tail; authored curvature, not smooth mesh subdivision.
 for i,(a,b) in enumerate(zip(TAIL,TAIL[1:])):
  name='tail_'+str(i);B(name,'hips' if i==0 else 'tail_'+str(i-1),a)
  width=[.875,.75,.625,.625,.5,.5,.375,.25,.125][i]
  direction=np.array(b)-a;end=np.array(b)+direction/np.linalg.norm(direction)*.125
  L(name,name+'_volume',a,end,width,width,'fur',pattern='tail',feature='tail')
 from head_geometry import build_head
 build_head(m)
 # Bind-pose heel defines zero; every pivot and cuboid uses the same translation.
 shift=1.8125*8
 for b in m.bones:
  if b['name']=='nekomata':continue
  b['pivot'][1]-=shift
  for c in b.get('cubes',[]):
   c['origin'][1]-=shift
   if 'pivot' in c:c['pivot'][1]-=shift
 return m
