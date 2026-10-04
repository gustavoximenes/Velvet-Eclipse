"""Pixie 03: structural head reconstruction with a sloped jaw and fitted hair."""
import math
import numpy as np

def build_head(m):
    B=m.bone;C=m.box
    B('head','body',(0,28.3,0))
    B('face_base','head',(0,31.4,-1.70),(-8,0,0))
    B('jaw_base','face_base',(0,30.5,-1.70))
    from pathlib import Path
    import json
    fitted=json.loads((Path(__file__).parent/'skin_boxes.json').read_text())
    for box in fitted['boxes']:
        C('head',box['name'],box['center'],box['size'],'skin',box['rotation'],feature=box['feature'])
    C('head','chin_tip',(0,29.12,-1.20),(.625,.375,.375),'skin',(-8,0,0),feature='chin')
    C('head','neck_connector',(0,29.15,0),(1.0,1.5,1.0),'skin',feature='neck')
    # Continuous front surfaces over the solid cranium. All share the same
    # front plane, so buried joins do not make horizontal ledges on the face.
    C('face_base','face_surface',(0,30.75,-1.6375),(2.375,2.375,.125),'skin',feature='midface')
    C('face_base','jaw_surface',(0,29.70,-1.6375),(1.375,1.125,.125),'skin',feature='jaw')
    C('face_base','chin_surface',(0,29.43,-1.6375),(.5,.875,.125),'skin',feature='chin')
    contour=[(1.48,31.98),(1.63,31.4),(1.58,30.5),(1.25,29.8),(.75,29.2),(.26,28.98)]
    for s,side in [(-1,'right'),(1,'left')]:
        for i,(upper,lower) in enumerate(zip(contour,contour[1:])):
            a=np.array(upper);b=np.array(lower);direction=(a-b)/np.linalg.norm(a-b)
            outward=np.array([direction[1],-direction[0]])
            width=2.;center=(a+b)/2-outward*width/2
            length=math.ceil(np.linalg.norm(a-b)*8)/8
            C('face_base',side+'_face_surface_'+str(i),(s*center[0],center[1],-1.6375),
              (width,length,.125),'skin',(0,0,-s*math.degrees(math.atan2(direction[0],direction[1]))),feature='cheek' if i<2 else 'jaw')
    for s,side in [(-1,'right'),(1,'left')]:
        B('ear_'+side,'head',(s*1.47,31.0,.0))
        C('ear_'+side,side+'_ear_base',(s*1.73,31.15,.04),(.5,.75,.375),'skin',(0,0,-s*31),feature='ear')
        C('ear_'+side,side+'_ear_tip',(s*2.01,31.48,.04),(.25,.75,.25),'skin',(0,0,-s*45),feature='ear')
        C('ear_'+side,side+'_ear_inner',(s*1.78,31.25,-.17),(.25,.5,.125),'skin_soft',(0,0,-s*36),feature='ear')
        B('eye_'+side,'face_base',(s*.78,31.18,-1.65))
        B('eye_color_'+side,'eye_'+side,(s*.78,31.18,-1.65))
        # Pixel iris on a solid eye, avoiding protruding iris stacks.
        C('eye_color_'+side,side+'_eye',(s*.78,31.18,-1.70),(1.0,.625,.125),'eye',pattern='eye_'+side,feature='eye')
        C('eye_'+side,side+'_upper_lid',(s*.78,31.52,-1.69),(1.125,.125,.125),'skin',(0,0,-s*4),feature='orbit')
        C('eye_'+side,side+'_lash',(s*.79,31.485,-1.743),(1.125,.125,.125),'lash',(0,0,-s*4),feature='eyelid')
        C('eye_'+side,side+'_lower_lid',(s*.78,30.84,-1.682),(1.0,.125,.125),'skin',feature='eyelid')
        C('face_base',side+'_brow',(s*.78,31.81,-1.685),(.875,.125,.125),'hair',(0,0,-s*5),feature='brow')
    C('face_base','nose_bridge',(0,30.94,-1.71),(.25,.625,.25),'skin',(15,0,0),feature='nose')
    C('face_base','nose_tip',(0,30.65,-1.796),(.25,.25,.25),'skin',(15,0,0),feature='nose')
    C('jaw_base','mouth',(0,30.03,-1.712),(.625,.125,.125),'mouth',feature='mouth')
    C('jaw_base','lower_lip',(0,29.96,-1.705),(.5,.125,.125),'lip',feature='mouth')
    for s,side in [(-1,'right'),(1,'left')]:
        C('jaw_base',side+'_upper_lip',(s*.155,30.09,-1.72),(.25,.125,.125),'lip',(0,0,s*8),feature='mouth')
    # Fitted cap, single front wave and cropped nape, without stacked planks.
    B('hair_crown','head',(0,32.6,0))
    C('hair_crown','crown',(0,32.72,.13),(3.0,.875,2.875),'hair',pattern='cap',feature='hair')
    C('hair_crown','crest',(0,33.15,.13),(2.125,.625,2.625),'hair',pattern='crest',feature='hair')
    C('hair_crown','rear_hair',(0,31.83,1.16),(2.5,2.0,.5),'hair',(-9,0,0),pattern='back',feature='hair')
    C('hair_crown','nape',(0,30.82,1.0),(2.0,.625,.5),'hair',(-20,0,0),pattern='back',feature='hair')
    for s,side in [(-1,'right'),(1,'left')]:
        C('hair_crown',side+'_crown_bevel',(s*1.10,32.77,.05),(1.0,1.0,2.375),'hair',(0,0,-s*30),pattern='cap',feature='hair')
        C('hair_crown',side+'_side_hair',(s*1.43,32.02,.10),(.5,1.5,1.75),'hair',(0,-s*10,-s*5),pattern='side',feature='hair')
        C('hair_crown',side+'_back_bevel',(s*1.05,31.72,1.0),(1.0,1.75,.75),'hair',(0,-s*25,0),pattern='back',feature='hair')
    B('hair_fringe','head',(-.45,32.95,-1.52))
    C('hair_fringe','hairline',(0,32.85,-1.08),(2.625,1.0,1.25),'hair',(-10,0,20),pattern='cap',feature='hair')
    C('hair_fringe','front_wave',(-.30,32.62,-1.40),(2.375,1.0,.875),'hair',(-10,-5,34),pattern='wave',feature='hair')
    C('hair_fringe','wave_root',(-.85,32.55,-1.3),(1.25,1.0,.75),'hair',(-10,0,20),pattern='cap',feature='hair')
    C('hair_fringe','sweep_end',(-1.28,32.05,-1.43),(.5,.875,.625),'hair',(-12,-8,24),pattern='wave',feature='hair')
