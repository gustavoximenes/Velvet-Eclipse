"""Reference idle, two-hand spell gesture and a light one-hand tap."""
import math
import numpy as np
from rig_math import matrix,euler,ik

DENSITY=8
LENGTHS={'idle':4.0,'magic':2.8,'physical':1.8}
IMPACTS={'magic':1.18,'physical':.66}

def curve(t,times,values):
    # Smooth transitions between authored poses, baked to ordinary linear keys.
    for a,b,va,vb in zip(times,times[1:],values,values[1:]):
        if a<=t<=b:
            u=(t-a)/(b-a);u=u*u*(3-2*u)
            return np.array(va)*(1-u)+np.array(vb)*u
    return np.array(values[-1] if t>times[-1] else values[0],float)

def pose(kind,t):
    p={};pos={'pixie':np.zeros(3)}
    hover=math.sin(t*math.tau/4)
    flutter=math.sin(t*math.tau*2)
    if kind!='idle':
        envelope=math.sin(math.pi*t/LENGTHS[kind])**2
        hover*=envelope;flutter*=envelope
    body=np.array([-5.0,0,-6.0])+[hover*.8,hover*.5,hover*.6]
    pos['pixie']=np.array([0,hover*.30,0])
    wrists={'right':np.array([-1.3,28.0,-3.15]),'left':np.array([1.3,27.8,-3.2])}
    hands={'right':np.array([-162.,-5,-18]),'left':np.array([-158.,5,18])}
    ankle={'right':np.array([-1.25,7.2,3.1]),'left':np.array([1.55,10.6,6.1])}
    openness=0;close_eyes=0
    if kind=='magic':
        times=[0,.35,.78,1.18,1.60,2.05,2.8]
        openness=float(curve(t,times,[0,.15,.85,1,1,.65,0]))
        for side,s in [('right',-1),('left',1)]:
            wrists[side]=curve(t,times,[wrists[side],[s*1.6,27.2,-3.8],[s*2.4,27.0,-6.2],
                    [s*1.9,28.15,-7.95],[s*1.9,28.15,-7.95],[s*2.1,27.8,-5.0],wrists[side]])
            # 198° is equivalent to -162°, avoiding a wrist flip during extension.
            rest=np.array(hands[side]);rest[0]+=360
            hands[side]=rest*(1-openness)+np.array([92,0,-s*8])*openness
        body+=np.array([-3,0,5])*openness
        pos['pixie']+=np.array([0,.75,-.35])*openness
        ankle['right']+=np.array([-.25,4.0,2.9])*openness
        ankle['left']+=np.array([.25,1.0,.8])*openness
        close_eyes=float(curve(t,[0,.5,.95,1.65,2.15,2.8],[0,0,1,1,0,0]))
    elif kind=='physical':
        times=[0,.26,.49,.66,.78,1.05,1.8]
        factor=float(curve(t,times,[0,.25,.8,1,.9,.5,0]))
        rest=wrists['right'].copy()
        wrists['right']=curve(t,times,[rest,[-3.0,27.0,-2.0],[-2.6,26.7,-5.7],
             [-1.25,26.8,-7.8],[-.7,26.5,-7.3],[-2,27.5,-4.6],rest])
        # Palm faces the target; fingers remain open. No fist, weapon or second hit.
        hands['right']=np.array([-162.,-5,-18])*(1-factor)+np.array([-180.,170,-8])*factor
        openness=factor
        body+=np.array([-2,-6,4])*factor
        pos['pixie']+=np.array([0,.15,-.65])*factor
    p['body']=body
    p['head']=[3-body[0]*.35,hover*.6,3-body[2]*.25]
    p['hips']=[hover*.4,0,-10+hover*.6]
    p['hair_fringe']=[hover*.6,0,flutter*.3]
    for side,s in [('right',-1),('left',1)]:
        pos['eye_color_'+side]=np.array([0,0,.6*close_eyes])
        p['wing_'+side]=[-6,s*(12+flutter*13),0]
        p['wing_'+side+'_upper']=[flutter*2,0,-s*flutter*2]
        p['wing_'+side+'_lower']=[-flutter*4,s*flutter*4,0]
        a,b,arm,_=ik((s*2.25,26.05,0),wrists[side],(s*5.2,22.5,-.25),4.25,4.125,hinge=True)
        p[side+'_upper_arm']=a;p[side+'_forearm']=b
        p[side+'_hand']=euler(arm.T@matrix(hands[side]))
        for i in range(4):p[side+f'_finger_{i}']=[-8*(1-openness),0,(i-1.5)*4*openness]
        a,b,leg,_=ik((s*1.15,18.25,0),ankle[side],(s*1.5,11,-4),6.,6.5)
        p[side+'_thigh']=a;p[side+'_shin']=b
        p[side+'_foot']=euler(leg.T@matrix([-36,0,s*5]))
    return p,pos

def animate(m):
    rest,restpos=pose('idle',0)
    authored={b['name']:np.array(b.get('rotation',[0,0,0]),float) for b in m.bones}
    for b in m.bones:
        if b['name'] in rest:b['rotation']=list(np.round(authored[b['name']]+rest[b['name']],6))
    clips={}
    for kind,length in LENGTHS.items():
        times=sorted(set([round(i*length/(160 if kind=='idle' else 140),6) for i in range((160 if kind=='idle' else 140)+1)]+([IMPACTS[kind]] if kind in IMPACTS else [])))
        tracks={};previous={}
        for t in times:
            rot,ps=pose(kind,t)
            if t==length:rot={k:np.array(v) for k,v in rest.items()};ps=restpos
            for bone,v in rot.items():
                v=np.array(v,float)
                if bone in previous:
                    candidates=[v,np.array([v[0]+180,180-v[1],v[2]+180])]
                    candidates=[a+360*np.round((previous[bone]-a)/360) for a in candidates]
                    v=min(candidates,key=lambda a:np.linalg.norm(a-previous[bone]))
                previous[bone]=v
                delta=v-rest.get(bone,np.zeros(3))
                tracks.setdefault(bone,{}).setdefault('rotation',{})[f'{t:g}']=list(np.round(delta,6))
            for bone,v in ps.items():tracks.setdefault(bone,{}).setdefault('position',{})[f'{t:g}']=list(np.round(v*DENSITY,6))
        clip={'loop':kind=='idle','animation_length':length,'bones':tracks}
        clips['animation.pixie.'+{'idle':'air_idle','magic':'magic_attack','physical':'physical_attack'}[kind]]=clip
    return {'format_version':'1.8.0','animations':clips}
