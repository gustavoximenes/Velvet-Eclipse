"""Reviewable original choreography, not asserted to be extracted P3R motion."""
import math
import numpy as np
DENSITY=8
LENGTHS={'idle':3.6,'physical':2.4,'magic':2.8}
NAMES={'idle':'air_idle','physical':'physical_attack','magic':'magic_attack'}
IMPACTS={'physical':.80,'magic':1.05}

def curve(t,times,values):
    for a,b,va,vb in zip(times,times[1:],values,values[1:]):
        if a<=t<=b:
            u=(t-a)/(b-a);u=u*u*(3-2*u)
            return np.array(va)*(1-u)+np.array(vb)*u
    return np.array(values[-1] if t>times[-1] else values[0],dtype=float)

def pose(kind,t):
    wave=math.sin(math.tau*t/LENGTHS['idle'])
    if kind!='idle':wave=0
    p={'body':np.array([-7.,0,0])+[wave*2.2,0,wave*1.8],
       'neck':np.array([3.,0,0]),'head':np.array([4.,0,0])+[-wave*1.3,wave*2,0],
       'jaw':np.zeros(3)}
    pos={'onmoraki':np.array([0,wave*.30,0]),'neck':np.zeros(3)}
    spread=0.;leg=0.;crest=0.
    for side,s in [('right',-1),('left',1)]:
        flap=math.sin(math.tau*t/1.2)*5 if kind=='idle' else 0
        p[side+'_wing']=np.array([0.,0,s*(7+flap)])
        p[side+'_wing_tip']=np.array([0.,s*5,s*(-5-flap*.45)])
        p[side+'_thigh']=np.array([s*5+wave*4,0,s*6])
        p[side+'_shin']=np.array([8-wave*3,0,0])
        p[side+'_foot']=np.array([-8+wave*3,0,s*4])
    if kind=='physical':
        # Squash back, one quick head/body lunge, rebound and settle.
        times=[0,.25,.50,.80,.94,1.20,1.65,2.4]
        pos['onmoraki']+=curve(t,times,[[0,0,0],[0,-.6,1.0],[0,.3,.7],[0,.3,-3.2],[0,.1,-3.3],[0,.6,-1.1],[0,.2,-.3],[0,0,0]])
        p['body']+=curve(t,times,[[0,0,0],[10,0,0],[15,0,0],[-28,0,0],[-24,0,0],[14,0,0],[6,0,0],[0,0,0]])
        p['head']+=curve(t,times,[[0,0,0],[5,0,0],[10,0,0],[-12,0,0],[-9,0,0],[8,0,0],[0,0,0],[0,0,0]])
        spread=float(curve(t,times,[0,-8,-18,-35,-24,20,7,0]))
        leg=float(curve(t,times,[0,10,18,-30,-26,12,3,0]))
        crest=float(curve(t,times,[0,-4,-6,8,10,-9,-3,0]))
    elif kind=='magic':
        # Open wings, lift the skull, open the mouth and project the spell forward.
        times=[0,.30,.65,1.05,1.35,1.75,2.25,2.8]
        # Lift the skull from the tucked neck before opening the jaw, keeping
        # the lower mouth clear of the chest throughout the emission pose.
        pos['neck'][1]=float(curve(t,times,[0,.1,.4,.75,.75,.45,.1,0]))
        pos['onmoraki']+=curve(t,times,[[0,0,0],[0,-.25,0],[0,.75,.15],[0,1.,-.25],[0,1.,-.25],[0,.6,0],[0,.15,0],[0,0,0]])
        p['body']+=curve(t,times,[[0,0,0],[-6,0,0],[-12,0,0],[5,0,0],[5,0,0],[-5,0,0],[-2,0,0],[0,0,0]])
        p['head']+=curve(t,times,[[0,0,0],[-6,0,0],[-13,0,0],[-8,0,0],[-8,0,0],[-5,0,0],[0,0,0],[0,0,0]])
        p['jaw'][0]=float(curve(t,times,[0,-2,-18,-32,-32,-19,-4,0]))
        spread=float(curve(t,times,[0,-5,20,34,34,20,4,0]))
        leg=float(curve(t,times,[0,2,10,17,17,8,2,0]))
        crest=float(curve(t,times,[0,1,4,6,5,2,0,0]))
    for side,s in [('right',-1),('left',1)]:
        p[side+'_wing']+=np.array([0,-s*spread*.25,s*spread])
        p[side+'_wing_tip']+=np.array([0,s*spread*.15,-s*spread*.30])
        p[side+'_thigh'][0]+=leg
        p[side+'_shin'][0]-=leg*.6
    for i in range(5):
        sway=math.sin(math.tau*t/3.6-i*.35)*.8 if kind=='idle' else 0
        p['crest_'+str(i)]=np.array([sway+crest*(.16+i*.06),0,wave*.3])
    return p,pos

def animate(m):
    # Identical rest pose at all clip boundaries, small idle secondary motion.
    rest,restpos=pose('idle',0)
    # At t=0 the delayed crest motion must be shared by attacks too.
    for b in m.bones:
        if b['name'] in rest:b['rotation']=np.round(rest[b['name']],6).tolist()
    clips={}
    for kind,length in LENGTHS.items():
        times=sorted(set([round(i*length/96,6) for i in range(97)]+([IMPACTS[kind]] if kind in IMPACTS else [])))
        tracks={}
        for t in times:
            rot,positions=pose(kind,t)
            if kind!='idle':
                for i in range(5):rot['crest_'+str(i)]+=rest['crest_'+str(i)]
            if t==length or t==0:rot=rest;positions=restpos
            for name,v in rot.items():
                tracks.setdefault(name,{}).setdefault('rotation',{})[str(t)]=np.round(v-rest.get(name,np.zeros(3)),6).tolist()
            for name,v in positions.items():
                tracks.setdefault(name,{}).setdefault('position',{})[str(t)]=np.round(np.array(v)*DENSITY,6).tolist()
        clips['animation.onmoraki.'+NAMES[kind]]={'loop':kind=='idle','animation_length':length,'bones':tracks}
    return {'format_version':'1.8.0','animations':clips}
