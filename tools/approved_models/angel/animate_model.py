"""Authored audit motion. Reference pose verified; attack choreography is provisional."""
import math
import numpy as np
from rig_math import ik,matrix,euler
DENSITY=8
LENGTHS={'ground_idle':4.8,'physical_attack':2.6,'magic_attack':3.0,
         'expression_serene':2.4,'expression_focus':2.4,'expression_exertion':2.4,'expression_hurt':2.4}

def joint_midpoint(rotation):
 r=matrix(rotation);angle=math.acos(np.clip((np.trace(r)-1)/2,-1,1))
 if angle<1e-8:return np.zeros(3)
 axis=np.array([r[2,1]-r[1,2],r[0,2]-r[2,0],r[1,0]-r[0,1]])/(2*math.sin(angle))
 x,y,z=axis;k=np.array([[0,-z,y],[z,0,-x],[-y,x,0]])
 return euler(np.eye(3)+math.sin(angle/2)*k+(1-math.cos(angle/2))*(k@k))

def curve(t,times,values):
 for a,b,va,vb in zip(times,times[1:],values,values[1:]):
  if a<=t<=b:
   u=(t-a)/(b-a);u=u*u*(3-2*u);return np.array(va)*(1-u)+np.array(vb)*u
 return np.array(values[-1] if t>times[-1] else values[0],float)

def pose(kind,t):
 wave=math.sin(t*math.tau/4.8);flap=math.sin(t*math.tau/2.4)
 if kind!='ground_idle':wave=0;flap=0
 p={'hips':np.array([0,-5,-5.]),'body':np.array([-3.,5,4]),'head':np.array([2.,-5,-2])}
 pos={'angel':np.array([0,3.8+.25*wave,0.])}
 p['body']+=np.array([wave*.7,0,wave*.4]);p['head']+=np.array([wave*.5,wave*.5,0])
 # One open hand rises above the crown; the other follows its upper rear contour.
 # Unequal wrist positions avoid the former parallel, ear-like hand silhouette.
 wrists={'right':np.array([-.90,42.65,.65]),'left':np.array([1.45,42.50,2.30])}
 handrot={'right':np.array([154.,-18,40]),'left':np.array([22.,-12,-103])}
 # A clear bent leg crosses in front of a long relaxed leg. Pointed toes continue
 # the shin line, rather than forming a horizontal standing foot while airborne.
 ankles={'right':np.array([.1,6.55,-1.8]),'left':np.array([2.25,1.65,.60])}
 expr='serene';effort=0.;wings=0
 if kind=='physical_attack':
  times=[0,.45,.85,1.12,1.5,2.05,2.6]
  effort=float(curve(t,times,[0,.2,.5,1,.5,.1,0]));wings=float(curve(t,times,[0,-3,13,19,12,3,0]))
  p['body']+=curve(t,times,[[0,0,0],[-5,10,0],[7,-16,-3],[17,-22,-5],[12,-10,-3],[3,0,0],[0,0,0]])
  # Preserve her raised-arm silhouette during the proposed kick. The former
  # outward hand sweep crossed the elbow pole and flipped the forearm frame.
  wrists['right']+=curve(t,times,[[0,0,0],[0,-.2,0],[-.2,-.3,-.2],[-.3,-.5,-.2],[-.2,-.3,0],[0,-.1,0],[0,0,0]])
  wrists['left']+=curve(t,times,[[0,0,0],[0,-.1,0],[.2,-.2,0],[.3,-.4,-.2],[.1,-.2,0],[0,-.1,0],[0,0,0]])
  ankles['right']+=curve(t,times,[[0,0,0],[-1,1.8,0],[.1,1,-4],[1,4,-7],[0,2,-4],[0,.8,-1],[0,0,0]])
  pos['angel']+=curve(t,times,[[0,0,0],[0,-.5,0],[0,.5,-.6],[0,.4,-2],[0,.1,-1.4],[0,0,-.3],[0,0,0]])
  expr='exertion'
 elif kind=='magic_attack':
  # s-Ya33_6Wfs at 230.5–232.9 s: compendium action, arms overhead,
  # a full body turn and folding/reopening wings. Skill category unconfirmed.
  times=[0,.35,.70,1.02,1.35,1.65,2.15,3]
  effort=float(curve(t,times,[0,.2,.5,.8,.8,.6,.15,0]))
  p['angel']=np.array([0,float(curve(t,times,[0,0,95,190,285,360,360,360])),0])
  wings=float(curve(t,times,[0,2,18,30,-10,8,0,0]))
  wrists['right']+=curve(t,times,[[0,0,0],[0,0,-.3],[-.5,-.7,-.4],[-.3,-.3,0],[.1,0,.1],[.3,.2,0],[0,0,0],[0,0,0]])
  p['body']+=curve(t,times,[[0,0,0],[0,0,-2],[3,0,-5],[4,0,-4],[-3,0,3],[-5,0,4],[-2,0,1],[0,0,0]])
  pos['angel'][1]+=float(curve(t,times,[0,-.15,.5,.7,.2,.4,.15,0]))
  ankles['right']+=curve(t,times,[[0,0,0],[0,0,0],[0,1,0],[0,2.2,0],[0,-1.2,-1],[0,0,0],[0,0,0],[0,0,0]])
  expr='focus'
 elif kind.startswith('expression_'):
  expr=kind.removeprefix('expression_');effort=math.sin(math.pi*t/2.4)**2
 for side,s in [('right',-1),('left',1)]:
  wrist=wrists[side]+np.array([wave*.05, wave*.08+effort*.75,0])
  a,b,r2,actual=ik((s*2.8,32.5,.1),wrist,(s*8,38,-1),6.25,5.5,hinge=True)
  p[side+'_upper_arm']=a;p[side+'_forearm']=b;p[side+'_hand']=euler(r2.T@matrix(handrot[side]))
  p[side+'_elbow_bridge']=joint_midpoint(b)
  for j in range(4):p[side+'_finger_'+str(j)]=np.array([8+j*3+effort*6,0,s*(j-1.5)*1.2])
  p[side+'_thumb']=np.array([18,0,s*11])
  pole=(6.0,14,-5-(7*effort if kind=='physical_attack' else 0)) if side=='right' else (2.5,11,-5)
  a,b,r2,actual=ik((s*1.35,21.5,0),ankles[side],pole,10.25,9.75,hinge=True)
  p[side+'_thigh']=a;p[side+'_shin']=b
  p[side+'_foot']=np.array([-60 if side=='right' else -68,0,0.])
  p[side+'_knee_bridge']=joint_midpoint(b)
  p[side+'_ankle_bridge']=joint_midpoint(p[side+'_foot'])
  p['wing_'+side]=np.array([-8+s*1.5,-s*(12+flap*3+wings*.35),-s*(4+flap*4+wings*.30)])
  p['wing_'+side+'_outer']=np.array([0,-s*(flap*3+wings*.25),s*(flap*2)])
  p['hair_'+side]=np.array([wave*1.2,0,s*wave*.5])
  p['ribbon_'+side]=np.array([wave*3+effort*3,s*wave*2, s*wave*.6])
  p['mouth_corner_'+side]=np.array([0,0,s*4])
  pos['mouth_corner_'+side]=np.array([0,.035,0])
 if expr=='focus':
  p['head']+=np.array([6*effort,0,0]);pos['lip_lower']=np.array([0,.035*effort,0])
  for side,s in [('right',-1),('left',1)]:p['lip_'+side]=np.array([0,0,-s*7*effort]);pos['mouth_corner_'+side]=np.array([0,-.06*effort,0])
 elif expr=='exertion':
  pos['lip_lower']=np.array([0,-.14*effort,0]);p['head']+=np.array([-5*effort,0,0])
  pos['jaw']=np.array([0,-.04*effort,0]);p['jaw']=np.array([effort*1.5,0,0])
  for side,s in [('right',-1),('left',1)]:pos['lip_'+side]=np.array([0,.055*effort,0]);p['mouth_corner_'+side]=np.array([0,0,-s*12*effort])
 elif expr=='hurt':
  pos['lip_lower']=np.array([0,-.11*effort,0]);p['head']+=np.array([7*effort,6*effort,9*effort])
  p['jaw']=np.array([effort,0,0])
  for side,s in [('right',-1),('left',1)]:pos['mouth_corner_'+side]=np.array([0,-.10*effort,0]);p['lip_'+side]=np.array([0,0,-s*10*effort])
 else:
  for side,s in [('right',-1),('left',1)]:pos['mouth_corner_'+side]=np.array([0,.035+.045*effort,0])
 p['chain']=np.array([wave*.8,0,wave*.7])
 return p,pos

def animate(m):
 names={b['name'] for b in m.bones};clips={}
 for kind,length in LENGTHS.items():
  tracks={};steps=round(length*20)
  for i in range(steps+1):
   t=length*i/steps;p,pos=pose(kind,t);key=f'{t:.5f}'
   for channel,values in [('rotation',p),('position',pos)]:
    for name,v in values.items():
     assert name in names,name
     tracks.setdefault(name,{}).setdefault(channel,{})[key]=[round(float(x)*(8 if channel=='position' else 1),5) for x in v]
  for channels in tracks.values():
   if 'rotation' in channels:
    keys=channels['rotation'];values=np.degrees(np.unwrap(np.radians(list(keys.values())),axis=0))
    channels['rotation']={key:[round(float(x),5) for x in v] for key,v in zip(keys,values)}
  clips['animation.angel.'+kind]={'loop':kind=='ground_idle' or kind.startswith('expression_'),'animation_length':length,'bones':tracks}
 return {'format_version':'1.8.0','animations':clips}
