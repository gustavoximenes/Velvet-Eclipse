"""Original Pixie cuboid geometry. No game resources are written or reused."""
import math
import numpy as np

def geometry(Model):
    m=Model();B=m.bone;C=m.box;L=m.segment
    B('pixie');B('hips','pixie',(0,18,0));B('body','hips',(0,20,0))
    C('hips','pelvis',(0,19.3,0),(3.25,1.25,2.5),'blue',pattern='cloth')
    C('hips','suit_lower_center',(0,18.4,0),(1.375,2.125,2.375),'blue',pattern='cloth')
    C('body','waist',(0,21.1,0),(3.125,3.0,2.25),'blue',pattern='cloth')
    C('body','ribcage',(0,24.2,.025),(3.75,3.5,2.625),'blue',pattern='cloth')
    C('body','upper_chest',(0,26.1,0),(3.75,1.25,2.375),'blue')
    C('body','front_panel',(0,22.6,-1.28),(1.875,5.75,.25),'blue',pattern='cloth')
    C('body','neck',(0,27.6,0),(1.125,2.0,1.125),'skin',feature='neck')
    C('body','high_collar',(0,27.1,0),(1.5,.875,1.5),'blue',pattern='cloth')
    for s,side in [(-1,'right'),(1,'left')]:
        C('body',side+'_rib_bevel',(s*1.68,23.7,0),(.75,3.75,2.625),'blue',(0,0,-s*10),pattern='cloth')
        C('body',side+'_chest_panel',(s*.95,25.15,-1.1),(1.75,1.875,.875),'blue',(0,-s*12,-s*6),pattern='cloth')
        # High-cut leotard: skin at the hip belongs to the leg, suit edges are inset.
        C('hips',side+'_legline',(s*1.0,18.65,-1.18),(1.0,1.875,.375),'blue',(0,0,-s*38))
        C('body',side+'_deltoid',(s*2.05,26.1,0),(1.125,1.125,1.375),'skin',feature='shoulder')
        B(side+'_upper_arm','body',(s*2.25,26.05,0))
        C(side+'_upper_arm',side+'_upper_arm',(s*2.25,23.975,0),(1.125,4.375,1.25),'skin',feature='arm')
        B(side+'_forearm',side+'_upper_arm',(s*2.25,21.8,0))
        C(side+'_forearm',side+'_long_glove',(s*2.25,19.8,0),(1.0,4.375,1.125),'blue',pattern='glove',feature='arm')
        C(side+'_forearm',side+'_cuff',(s*2.25,21.65,0),(1.25,.5,1.375),'blue_edge',feature='arm')
        B(side+'_hand',side+'_forearm',(s*2.25,17.675,0))
        C(side+'_hand',side+'_palm',(s*2.25,17.3,0),(1.0,.875,.625),'blue',feature='hand')
        for i in range(4):
            x=s*2.25+(i-1.5)*.24
            B(side+f'_finger_{i}',side+'_hand',(x,16.94,0))
            C(side+f'_finger_{i}',side+f'_finger_{i}',(x,16.62,0),(.25,.75 if i in [1,2] else .625,.375),'blue_edge',feature='finger')
        C(side+'_hand',side+'_thumb',(s*2.86,17.19,-.05),(.375,.75,.5),'blue',(0,0,s*30),feature='finger')
        B(side+'_thigh','hips',(s*1.15,18.25,0))
        C(side+'_thigh',side+'_thigh',(s*1.15,15.35,0),(1.875,6.0,2.125),'skin',feature='leg')
        C(side+'_thigh',side+'_upper_boot',(s*1.15,13.65,0),(2.0,3.375,2.25),'blue',pattern='boot',feature='leg')
        C(side+'_thigh',side+'_boot_top',(s*1.15,15.3,0),(2.125,.375,2.375),'blue_edge',feature='leg')
        B(side+'_shin',side+'_thigh',(s*1.15,12.25,0))
        C(side+'_shin',side+'_boot_calf',(s*1.15,10.5,0),(1.75,3.75,1.875),'blue',pattern='boot',feature='leg')
        C(side+'_shin',side+'_boot_lower',(s*1.15,7.4,0),(1.375,3.0,1.625),'blue',pattern='boot',feature='leg')
        C(side+'_shin',side+'_ankle',(s*1.15,6.1,0),(1.25,.875,1.5),'blue',feature='leg')
        B(side+'_foot',side+'_shin',(s*1.15,5.75,0))
        C(side+'_foot',side+'_instep',(s*1.15,5.3,-.65),(1.375,1.125,2.5),'blue',(-10,0,0),pattern='boot')
        C(side+'_foot',side+'_toe',(s*1.15,4.94,-1.9),(1.0,.625,1.25),'blue')
        C(side+'_foot',side+'_heel',(s*1.15,4.7,.55),(.625,1.375,.625),'blue')
    from head_geometry import build_head
    build_head(m)
    # Four thin native cubes, painted on both sides with pixel-cut leaf outlines.
    # Their Box UV mapping and texel density match the rest of the model.
    for s,side in [(-1,'right'),(1,'left')]:
        root=(s*.78,25.5,1.4)
        B('wing_'+side,'body',root)
        B('wing_'+side+'_upper','wing_'+side,root,(0,0,-s*24))
        C('wing_'+side+'_upper',side+'_upper_wing',(s*2.3,33.0,1.65),(5.25,15.25,.125),'wing',pattern='leaf_'+side,feature='wing')
        B('wing_'+side+'_lower','wing_'+side,root,(0,0,s*20))
        C('wing_'+side+'_lower',side+'_lower_wing',(s*2.3,25.15,1.82),(5.0,6.5,.125),'wing',pattern='lower_'+side,feature='wing')
        C('wing_'+side,side+'_wing_socket',(s*.78,25.5,1.55),(.5,.875,.5),'wing_edge',feature='wing')
    descendants={'head'}
    for bone in m.bones:
        if bone.get('parent') in descendants:descendants.add(bone['name'])
        if bone['name'] not in descendants:continue
        bone['pivot'][1]-=4
        for cube in bone.get('cubes',[]):
            cube['origin'][1]-=4
            if 'pivot' in cube:cube['pivot'][1]-=4
    return m
