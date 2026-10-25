"""Onmoraki 01: authored native cuboids. Uniform 8x grid; front -Z."""
import math
import numpy as np

def octagon(m,bone,name,center,size,corner,material,feature):
    """Six overlapping solids form a closed, eight-sided prism, without mesh faces."""
    x,y,z=center;w,h,d=size
    diagonal=round(corner*math.sqrt(2)*8)/8
    b=diagonal/math.sqrt(2)
    round_up=lambda v:math.ceil(v*8)/8
    m.box(bone,name+'_front_core',(x,y,z),(round_up(w-2*b),h,d),material,feature=feature)
    m.box(bone,name+'_side_core',(x,y,z),(w,h,round_up(d-2*b)),material,feature=feature)
    for sx in (-1,1):
        for sz in (-1,1):
            m.box(bone,name+f'_corner_{sx}_{sz}'.replace('-','m'),(x+sx*(w/2-b),y,z+sz*(d/2-b)),
                  (diagonal,h,diagonal),material,(0,45,0),feature=feature)

def geometry(Model):
    m=Model(); B=m.bone; C=m.box; S=m.segment
    B('onmoraki');B('body','onmoraki',(0,9.4,.6))
    # The silhouette is top-heavy: bulbous skull, compact bird trunk, thin limbs.
    octagon(m,'body','trunk',(0,9.3,.65),(3.75,4.75,3.5),.65,'brown','body')
    octagon(m,'body','rump',(0,7.1,.65),(3.125,1.25,3.0),.5,'brown','body')
    C('body','chest',(0,11.15,.2),(3,2.25,2.75),'brown',(-9,0,0),feature='body')
    B('neck','body',(0,11.9,1.5))
    C('neck','neck_joint',(0,12.75,1.5),(1.75,2.75,1.75),'mouth',feature='neck')
    B('head','neck',(0,13.25,1.3))
    octagon(m,'head','skull',(0,16.,-1.15),(9.75,4.75,6.75),1.375,'ivory','head')
    octagon(m,'head','upper_skull',(0,18.65,-1.15),(8.25,1.5,5.75),1.125,'ivory','head')
    octagon(m,'head','crown',(0,19.7,-.85),(5.75,1.125,4.375),.75,'ivory','head')
    octagon(m,'head','stalk_socket',(0,20.2,-.3),(3.25,1.,2.625),.5,'ivory','head')
    # Small uneven ivory lobes sit above the dark seam; no generic angry brow.
    for i in range(7):
        x=(i-3)*1.14
        C('head',f'upper_lip_lobe_{i}',(x,13.59+.06*abs(i-3),-3.8+.07*abs(i-3)),(1.125,.375,.875),'ivory',(0,0,(i-3)*-7),feature='head')
    C('head','oral_cavity',(0,13.42,-1.45),(7.375,.5,4.625),'mouth',feature='mouth')
    C('head','oral_hinge',(0,13.4,.8),(5.75,.875,1.375),'mouth',feature='mouth')
    B('jaw','head',(0,13.45,1.2))
    octagon(m,'jaw','lower_jaw',(0,13.15,-1.15),(8.0,.625,5.5),.75,'jaw','jaw')
    octagon(m,'jaw','inner_lower_mouth',(0,13.49,-1.35),(7.375,.125,4.875),.75,'mouth','mouth')
    # Small upper teeth become visible during the proposed spell gesture.
    for i in range(6):
        C('head',f'upper_tooth_{i}',((i-2.5)*.95,13.25,-3.35),(.375,.375,.375),'ivory',feature='tooth')
    # Crest curves backwards in profile, rather than standing up as a straight horn.
    points=[(0,20.25,-.1),(0,21.6,-.55),(0,23.1,-.35),(0,24.3,.50),(0,25.05,2.1),(0,26.05,3.7)]
    widths=[2.375,1.875,1.25,.625,.25]; depths=[1.875,1.375,1.0,.5,.25]
    for i,(a,b) in enumerate(zip(points,points[1:])):
        name=f'crest_{i}';B(name,'head' if i==0 else f'crest_{i-1}',a)
        S(name,name,a,b,widths[i],depths[i],'green',pattern='crest',feature='crest')
    # Wings: elbowed short arms, a shallow feather web and three separate tips.
    for s,side in [(-1,'right'),(1,'left')]:
        a=(s*1.4,11.25,.65);b=(s*4.0,10.8,.7);c=(s*6.2,12.,.65)
        B(side+'_wing','body',a)
        S(side+'_wing',side+'_wing_arm',a,b,1.,1.125,'brown',feature='wing')
        C(side+'_wing',side+'_shoulder',(s*1.55,11.15,.6),(1.25,1.5,1.5),'brown',feature='wing')
        B(side+'_wing_tip',side+'_wing',b)
        S(side+'_wing_tip',side+'_wing_forearm',b,c,.625,.875,'tan',feature='wing')
        S(side+'_wing_tip',side+'_wing_web',(s*4.1,10.82,.78),(s*5.5,11.4,1.0),.625,1.125,'brown',feature='wing')
        for j in range(3):
            start=(s*(5.0+j*.42),11.2+j*.28,.8)
            end=(s*(5.75+j*.40),10.85+j*.40,1.05)
            S(side+'_wing_tip',side+f'_feather_{j}',start,end,.25,.375,'tan',feature='feather')
        # Avian legs: pronounced hock, tapered toes, no humanoid boots.
        hip=(s*1.25,7.65,.75);knee=(s*1.6,5.35,-.15);ankle=(s*1.8,3.3,1.2)
        B(side+'_thigh','body',hip)
        S(side+'_thigh',side+'_thigh',hip,knee,1.375,1.625,'brown',feature='leg')
        C(side+'_thigh',side+'_thigh_front',(s*1.5,6.9,-.12),(1.125,2.5,.75),'tan',(0,0,s*5),feature='leg')
        B(side+'_shin',side+'_thigh',knee)
        S(side+'_shin',side+'_shin',knee,ankle,.625,.75,'brown',feature='leg')
        B(side+'_foot',side+'_shin',ankle)
        S(side+'_foot',side+'_foot',ankle,(s*2.0,2.25,-.3),.5,.5,'brown',feature='leg')
        for j in range(2):
            S(side+'_foot',side+f'_toe_{j}',(s*2,2.4,-.1),(s*(1.82+j*.36),1.95,-1.0-j*.1),.25,.25,'claw',feature='claw')
    # Deliberately omit unconfirmed tails and long flight feathers.
    m.connections=[]
    return m
