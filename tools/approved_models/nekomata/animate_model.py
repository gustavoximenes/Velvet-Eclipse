"""Reload-observed attacks on the native cuboid rig.
Reference TZap-dV5Oyo: Power Slash 10.30-11.30; Agi 333.75-335.06.
Recovery after summon dissolution is adapted. Idle timing is approximate.
"""
import math
import numpy as np
from rig_math import matrix,euler,ik
DENSITY=8
LENGTHS={'idle':3.2,'physical':2.5,'magic':2.8}
IMPACTS={'physical':.91,'magic':1.10}
NAMES={'idle':'ground_idle','physical':'physical_attack','magic':'magic_attack'}

def curve(t,times,values,smooth=True):
 for a,b,va,vb in zip(times,times[1:],values,values[1:]):
  if a<=t<=b:
   u=(t-a)/(b-a)
   if smooth:u=u*u*(3-2*u)
   return np.array(va)*(1-u)+np.array(vb)*u
 return np.array(values[-1] if t>times[-1] else values[0],float)

def pose(kind,t):
 p={};wave=math.sin(t*math.tau/LENGTHS['idle']);sway=math.cos(t*math.tau/LENGTHS['idle'])-1
 fade=1 if kind=='idle' else math.sin(math.pi*t/LENGTHS[kind])**2*.2
 wave*=fade;sway*=fade
 p['nekomata']=np.zeros(3)
 p['hips']=np.array([0.,0.,-9.])+[wave*.5,wave*.5,wave*.8]
 p['body']=np.array([-25.,-10.,5.])+[wave*1.4,wave*.8,wave*.5]
 pos={'nekomata':np.array([0,wave*.12,0])}
 wrists={'right':np.array([-3.0,25.0,-3.7]),'left':np.array([2.0,26.6,-3.0])}
 hands={'right':np.array([68.,-12.,-9.]),'left':np.array([72.,12.,9.])}
 ankles={'right':np.array([-2.0,3.2,-2.4]),'left':np.array([2.0,8.3,5.6])}
 ankles['left']+=np.array([wave*.10,wave*.16,wave*.20])
 p['head']=np.array([20.,10.,-5.])+[-wave*.5,wave*.8,0]
 spread=0.;tail_action=0.
 if kind=='physical':
  # Coil low, rise, open both arms and turn through the claw sweep; finish low.
  times=[0,.28,.44,.58,.72,.91,1.07,1.52,1.83,2.5]
  p['nekomata'][1]=float(curve(t,[0,.38,.53,.69,.87,1.03,1.12,2.5],[0,0,80,180,275,360,360,360],False))
  p['body']+=curve(t,times,[[0,0,0],[-9,-12,-4],[19,-8,-5],[26,0,-8],[20,8,-4],[-5,15,-9],[-20,12,-8],[-20,12,-8],[-7,5,-3],[0,0,0]])
  p['hips']+=curve(t,times,[[0,0,0],[0,-5,0],[0,-4,4],[0,0,9],[0,5,8],[0,7,4],[0,7,1],[0,7,1],[0,2,0],[0,0,0]])
  wrists['right']=curve(t,times,[wrists['right'],[-5.0,25.8,-1.2],[-6.0,29.1,0],[-7.5,28.3,-.8],[-9.2,26.3,-1.0],[-2.6,23.2,-7.2],[-4.8,21.4,-4.9],[-4.8,21.4,-4.9],[-3.8,23.8,-4.0],wrists['right']])
  left_times=[0,.28,.44,.58,.66,.96,1.12,1.52,1.83,2.5]
  wrists['left']=curve(t,left_times,[wrists['left'],[3.5,24,-4.5],[6.2,23.0,-1.0],[7.3,23.9,-1.8],[8.7,25.4,-1.0],[4.0,23.5,-4.4],[4.8,22.8,-4.7],[4.8,22.8,-4.7],[2.8,25,-3.6],wrists['left']])
  hands['right']=curve(t,times,[hands['right'],[50,10,-45],[10,15,-100],[0,15,-100],[15,10,-85],[65,20,40],[35,0,-18],[35,0,-18],[60,-5,-8],hands['right']])
  hands['left']=curve(t,left_times,[hands['left'],[50,-10,40],[25,-10,65],[20,-10,80],[15,-10,85],[35,5,25],[40,0,18],[40,0,18],[65,8,12],hands['left']])
  ankles['right']+=curve(t,times,[[0,0,0],[0,.5,0],[0,0,.5],[0,0,.7],[0,.3,.5],[-.5,1.8,-1.1],[-.8,4.0,-1.6],[-.8,4.0,-1.6],[-.3,1.7,-.4],[0,0,0]])
  ankles['left']+=curve(t,times,[[0,0,0],[0,.2,0],[.3,.7,0],[.3,1.4,1],[.3,1.4,1],[.5,.8,.5],[.9,-.7,-.8],[.9,-.7,-.8],[.3,0,-.2],[0,0,0]])
  p['head']+=curve(t,times,[[0,0,0],[4,5,0],[-15,0,0],[-22,-5,0],[-16,-5,0],[5,-10,3],[14,-9,3],[14,-9,3],[4,-3,1],[0,0,0]])
  lift=float(curve(t,times,[0,-.4,.8,1.2,1.2,.2,-1.2,-1.2,-.5,0]))
  pos['nekomata']+=np.array([0,lift,float(curve(t,times,[0,0,0,0,-.2,-.8,-1,-1,-.5,0]))])
  spread=float(curve(t,times,[0,.3,1,1,1,1,.7,.7,.3,0]))
  tail_action=float(curve(t,times,[0,-.3,.5,1,1,.5,-.3,-.3,-.1,0]))
 elif kind=='magic':
  # Agi: lift, full turn, right arm spread and left palm beside the cheek.
  times=[0,.30,.46,.59,.72,.84,.97,1.10,1.52,2.03,2.8]
  p['nekomata'][1]=float(curve(t,[0,.46,.57,.69,.81,.93,1.04,2.8],[0,0,70,160,255,340,360,360],False))
  p['body']+=curve(t,times,[[0,0,0],[-6,-3,0],[7,0,-2],[26,6,-5],[30,8,-7],[24,8,-7],[8,5,-5],[-12,2,-9],[-12,2,-9],[-4,1,-3],[0,0,0]])
  p['hips']+=curve(t,times,[[0,0,0],[0,0,0],[0,0,4],[0,0,8],[0,0,10],[0,0,10],[0,0,7],[0,0,4],[0,0,4],[0,0,2],[0,0,0]])
  wrists['right']=curve(t,times,[wrists['right'],[-3.7,25.9,-3.1],[-3.9,29,-1.6],[-6.3,28.8,0],[-8.3,27.4,0],[-8.9,26.5,-.5],[-9.6,25.5,-1.0],[-9.7,24.9,-1.3],[-9.7,24.9,-1.3],[-5.2,25.3,-2.1],wrists['right']])
  wrists['left']=curve(t,times,[wrists['left'],[2.4,27,-3.6],[3,29,-1.0],[5.1,28.6,0],[7.3,27.1,-.2],[6.0,27.2,-1.3],[4.1,28.3,-2.8],[2.7,28.2,-3.4],[2.7,28.2,-3.4],[2.6,27.2,-3.4],wrists['left']])
  hands['right']=curve(t,times,[hands['right'],[55,0,-30],[15,15,-90],[0,10,-100],[0,5,-90],[0,0,-85],[10,10,-80],[20,15,-75],[20,15,-75],[45,0,-30],hands['right']])
  hands['left']=curve(t,times,[hands['left'],[65,10,20],[25,-15,100],[5,-10,100],[5,-10,95],[5,-5,115],[0,-15,165],[0,-15,180],[0,-15,180],[35,5,90],hands['left']])
  ankles['right']+=curve(t,times,[[0,0,0],[0,.5,0],[0,0,0],[0,0,.5],[0,0,.7],[0,.3,.5],[-.7,2,-1.1],[-1.2,3.6,-1.3],[-1.2,3.6,-1.3],[-.5,1.5,-.5],[0,0,0]])
  ankles['left']+=curve(t,times,[[0,0,0],[0,0,0],[0,.7,0],[0,1,1],[0,1,1],[0,.7,1],[.4,-.7,0],[1,-2,-1.2],[1,-2,-1.2],[.3,-.5,-.4],[0,0,0]])
  p['head']+=curve(t,times,[[0,0,0],[4,0,0],[-5,0,0],[-23,-5,0],[-26,-8,0],[-18,-8,0],[-2,-5,0],[10,-2,5],[10,-2,5],[4,0,2],[0,0,0]])
  lift=float(curve(t,times,[0,-.3,.4,1.4,1.8,1.4,.2,-.9,-.9,-.4,0]))
  pos['nekomata']+=np.array([0,lift,float(curve(t,times,[0,0,0,0,0,0,-.2,-.5,-.5,-.2,0]))])
  spread=float(curve(t,times,[0,.3,.8,1,1,1,1,1,1,.5,0]))
  tail_action=float(curve(t,times,[0,-.2,.2,1,1.3,1.1,.6,.2,.2,.1,0]))
 for side,s in [('right',-1),('left',1)]:
  p['ear_'+side]=[wave*.6,0,s*wave*1.2]
  p['hair_'+side]=[wave*1.2+tail_action*3,tail_action*s*4,s*wave*.7-tail_action*4]
  p['hair_'+side+'_tip']=[wave*2-tail_action*4,tail_action*s*3,s*wave*1.4-tail_action*5]
  a,b,arm,_=ik((s*2.3,25.85,.1),wrists[side],(s*5.5,22.4,-.5),4.25,4.125,hinge=True)
  p[side+'_upper_arm']=a;p[side+'_forearm']=b;p[side+'_hand']=euler(arm.T@matrix(hands[side]))
  for j in range(4):p[side+'_finger_'+str(j)]=[-38*(1-spread)-8*spread,0,(j-1.5)*(3+4*spread)]
  p[side+'_thumb']=[-12,0,s*(-12-8*spread)]
  a,b,leg,_=ik((s*1.05,18.5,.05),ankles[side],(s*1.5,10,-4.8),7.75,7.75)
  p[side+'_thigh']=a;p[side+'_shin']=b;p[side+'_foot']=euler(leg.T@matrix([-21 if s<0 else -43,0,s*4]))
 p['hair_front']=[wave*.8-tail_action*3,tail_action*5,wave*.5-tail_action*3]
 p['hair_front_tip']=[wave*1.5+tail_action*3,tail_action*6,wave*.7-tail_action*4]
 for i in range(9):
  phase=t*math.tau/LENGTHS['idle']-i*.42
  p['tail_'+str(i)]=[math.sin(phase)*(.5+i*.18)*fade-tail_action*.7,math.sin(phase+.8)*(.4+i*.22)*fade+tail_action*2.3,0]
 return p,pos

def animate(m):
 rest,restpos=pose('idle',0)
 for b in m.bones:
  if b['name'] in rest:b['rotation']=np.round(np.array(b.get('rotation',[0,0,0]),float)+rest[b['name']],6).tolist()
 clips={}
 for kind,length in LENGTHS.items():
  n=128 if kind=='idle' else 180
  times=sorted(set([round(i*length/n,6) for i in range(n+1)]+([IMPACTS[kind]] if kind in IMPACTS else [])))
  tracks={};previous={}
  for t in times:
   rot,ps=pose(kind,t)
   if t==length:
    rot={k:np.array(v) for k,v in rest.items()};ps=restpos
    if kind!='idle':rot['nekomata']=np.array([0.,360.,0.])
   if kind!='idle' and t!=length:
    envelope=math.sin(math.pi*t/length)**2*.2
    for i in range(9):
     name='tail_'+str(i);rot[name]=np.array(rot[name])+np.array(rest[name])*(1-envelope)
   for bone,v in rot.items():
    v=np.array(v,float)
    # Root stays unwrapped; 360 is equivalent to rest but must retain the turn.
    if bone!='nekomata' and bone in previous:
     candidates=[v,np.array([v[0]+180,180-v[1],v[2]+180])]
     candidates=[q+360*np.round((previous[bone]-q)/360) for q in candidates]
     v=min(candidates,key=lambda q:np.linalg.norm(q-previous[bone]))
    previous[bone]=v
    tracks.setdefault(bone,{}).setdefault('rotation',{})[f'{t:g}']=list(np.round(v-rest.get(bone,np.zeros(3)),6))
   for bone,v in ps.items():tracks.setdefault(bone,{}).setdefault('position',{})[f'{t:g}']=list(np.round(v*DENSITY,6))
  clips['animation.nekomata.'+NAMES[kind]]={'loop':kind=='idle','animation_length':length,'bones':tracks}
 return {'format_version':'1.8.0','animations':clips}
