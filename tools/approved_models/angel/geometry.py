"""Angel: original integer cuboids, adult proportions, North-facing authoring rig."""
import math
import numpy as np

def limb_profile(C,bone,name,x,profile,depth,core_width,core_depth):
 # A few broad rotated cuboids describe the outside contour, as on the oval
 # face. Their inner edges overlap the central core; no stacked rings or mesh.
 top=profile[0][1];bottom=profile[-1][1]
 C(bone,name,(x,(top+bottom)/2,0),(core_width,math.ceil((top-bottom)*8)/8,core_depth),'skin',feature='leg')
 for s in [-1,1]:
  for i,(a,b) in enumerate(zip(profile,profile[1:])):
   a=np.array(a);b=np.array(b);axis=(a-b)/np.linalg.norm(a-b);outward=np.array([axis[1],-axis[0]])
   width=1.5 if name.endswith('upper_thigh') else 1.125
   center=(a+b)/2-outward*width/2
   length=math.ceil((np.linalg.norm(a-b)+.20)*8)/8
   C(bone,name+f'_contour_{s}_{i}',(x+s*center[0],center[1],0),(width,length,depth),'skin',(0,0,-s*math.degrees(math.atan2(axis[0],axis[1]))),feature='leg')

def geometry(Model):
 m=Model();B=m.bone;C=m.box;L=m.segment
 B('angel');B('hips','angel',(0,21.5,0));B('body','hips',(0,23.5,0))
 C('hips','pelvis',(0,21.7,0),(4.5,2.5,3),'skin',feature='pelvis')
 C('body','waist',(0,25.1,0),(3.125,4.75,2.5),'skin',feature='body')
 C('body','ribcage',(0,29.5,.04),(4.25,4.75,2.875),'skin',feature='body')
 C('body','upper_chest',(0,32.15,.05),(4.75,1.375,2.75),'skin',feature='body')
 C('body','neck',(0,33.9,.1),(1.5,2.75,1.625),'skin',feature='neck')
 C('body','collar',(0,34.3,.10),(1.875,.75,1.875),'strap',pattern='band',feature='clothing')
 C('body','chain_collar_mount',(.8,34.25,-1.10),(.5,.5,.875),'silver',feature='chain_mount')
 C('body','belt',(0,25.1,0),(3.625,1.375,2.875),'strap',pattern='band',feature='clothing')
 C('body','belt_buckle',(.75,25.15,-1.53),(.875,1.0,.25),'silver',pattern='band',feature='buckle')
 C('body','belt_buckle_center',(.75,25.15,-1.69),(.5,.625,.125),'strap',feature='buckle')
 C('body','vertical_front_strap',(.35,29,-1.54),(.5,7.5,.25),'strap',(0,0,-4),pattern='band',feature='clothing')
 C('body','upper_back_band',(0,31.15,1.59),(4.375,1.375,.25),'strap',pattern='band',feature='clothing')
 C('hips','hip_band',(0,22.3,0),(4.875,1.25,3.25),'strap',pattern='band',feature='clothing')
 C('hips','lower_garment',(0,20.77,-.10),(2.75,1.5,3.0),'strap',feature='clothing')
 for s,side in [(-1,'right'),(1,'left')]:
  C('hips',side+'_hip_facet',(s*1.65,21.6,0),(1.25,2.5,2.875),'skin',(0,0,s*12),feature='pelvis')
  C('body',side+'_rib_facet',(s*1.6,28.5,.05),(1.25,5,2.5),'skin',(0,0,-s*9),feature='body')
  C('body',side+'_chest_band',(s*1.12,31.15,-1.25),(2.5,1.625,1.125),'strap',(-6,-s*12,-s*13),pattern='band',feature='clothing')
  C('body',side+'_shoulder',(s*2.45,32.5,.1),(1.75,1.75,2.125),'skin',(0,0,-s*12),feature='shoulder')
  B(side+'_upper_arm','body',(s*2.80,32.5,.1))
  C(side+'_upper_arm',side+'_upper_arm',(s*2.8,29.35,.1),(1.625,6.875,1.75),'skin',feature='arm')
  B(side+'_elbow_bridge',side+'_upper_arm',(s*2.8,26.25,.1))
  C(side+'_elbow_bridge',side+'_elbow',(s*2.8,26.25,.1),(1.5,1.5,1.625),'skin',feature='arm')
  B(side+'_forearm',side+'_upper_arm',(s*2.8,26.25,.1))
  C(side+'_forearm',side+'_forearm',(s*2.8,23.55,.1),(1.25,5.875,1.375),'skin',feature='arm')
  C(side+'_forearm',side+'_wrist',(s*2.8,20.90,.1),(1.0,.875,1.125),'skin',feature='wrist')
  C(side+'_forearm',side+'_wrist_band',(s*2.8,21.45,.1),(1.5,.625,1.625),'strap',pattern='band',feature='clothing')
  B(side+'_hand',side+'_forearm',(s*2.8,20.75,.1))
  C(side+'_hand',side+'_palm',(s*2.8,20.05,.1),(1.25,1.625,.625),'skin',feature='hand')
  for j in range(4):
   x=s*2.8+(j-1.5)*.29;length=1.125 if j in (1,2) else .875
   B(side+'_finger_'+str(j),side+'_hand',(x,19.38,.1))
   C(side+'_finger_'+str(j),side+'_finger_'+str(j),(x,19.38-length/2,.06),(.25,length,.375),'skin',(7,0,0),feature='finger')
   C(side+'_finger_'+str(j),side+'_fingertip_'+str(j),(x,19.38-length-.13,-.02),(.25,.375,.25),'skin',(23,0,0),feature='finger')
  B(side+'_thumb',side+'_hand',(s*2.20,20.27,.1))
  C(side+'_thumb',side+'_thumb',(s*2.08,19.97,.02),(.375,.875,.5),'skin',(8,0,-s*26),feature='finger')
  B(side+'_thigh','hips',(s*1.35,21.5,0))
  limb_profile(C,side+'_thigh',side+'_upper_thigh',s*1.35,[(1.125,21.875),(1.3125,19.0),(1.125,15.8),(.80,10.70)],1.75,1.125,1.875)
  C(side+'_thigh',side+'_lower_thigh',(s*1.35,13.95,0),(1.25,5.5,1.25),'skin',feature='leg')
  B(side+'_knee_bridge',side+'_thigh',(s*1.35,11.25,0))
  C(side+'_knee_bridge',side+'_knee',(s*1.35,11.25,0),(1.5,1.5,1.625),'skin',feature='knee')
  B(side+'_shin',side+'_thigh',(s*1.35,11.25,0))
  limb_profile(C,side+'_shin',side+'_calf',s*1.35,[(.8,11.70),(.9375,9.0),(.80,6.8),(.5625,1.15)],1.375,.75,1.50)
  C(side+'_shin',side+'_shin',(s*1.35,3.9,0),(1.0,3.5,1.0),'skin',feature='leg')
  B(side+'_ankle_bridge',side+'_shin',(s*1.35,1.5,0))
  C(side+'_ankle_bridge',side+'_ankle',(s*1.35,1.5,0),(.875,1.5,1.125),'skin',feature='foot')
  B(side+'_foot',side+'_shin',(s*1.35,1.5,0))
  C(side+'_foot',side+'_instep',(s*1.35,.90,-.7),(1.125,1.0,2.25),'skin',(-8,0,0),feature='foot')
  C(side+'_foot',side+'_heel',(s*1.35,.80,.12),(1.0,.75,.875),'skin',feature='foot')
  for j in range(4):C(side+'_foot',side+'_toe_'+str(j),(s*1.35+(j-1.5)*.27,.60,-1.85),(.25,.5,.75 if j<2 else .625),'skin',feature='toe')
 build_head(m)
 build_wings(m)
 build_chain(m)
 return m

def build_head(m):
 B=m.bone;C=m.box
 B('head','body',(0,35.7,.1));B('face_base','head',(0,39.6,-2.222),(-8,0,0))
 B('jaw','face_base',(0,38.7,-.8))
 C('head','neck_upper_joint',(0,36.8,0),(1.375,2.5,1.5),'skin',feature='neck')
 C('head','cranial_core',(0,40.5,.05),(4.25,4,3.5),'skin',feature='cranium')
 C('head','forehead_plane',(0,41.6,-1.71),(3.875,1.75,1),'skin',feature='forehead')
 C('face_base','midface',(0,39.5,-1.722),(3.5,3.25,1),'skin',feature='midface')
 C('jaw','lower_face',(0,38.03,-1.346),(2.5,1.375,1.75),'skin',feature='jaw')
 C('jaw','chin_plane',(0,37.4875,-1.595),(.75,1.25,1.25),'skin',feature='chin')
 contour=[(2.20,40.1),(2.13,38.8),(1.70,37.85),(.94,37.04),(.35,36.86)]
 for s,side in [(-1,'right'),(1,'left')]:
  for i,(upper,lower) in enumerate(zip(contour,contour[1:])):
   a=np.array(upper);b=np.array(lower);axis=(a-b)/np.linalg.norm(a-b);outward=np.array([axis[1],-axis[0]])
   width=2.5;length=math.ceil(np.linalg.norm(a-b)*8)/8;center=(a+b)/2-outward*width/2;depth=[2,1.875,1.5,1.25][i]
   C('jaw' if i>=2 else 'face_base',side+'_face_facet_'+str(i),(s*center[0],center[1],-2.222+depth/2+i*.004),(width,length,depth),'skin',(0,0,-s*math.degrees(math.atan2(axis[0],axis[1]))),feature='face_contour')
  C('head',side+'_temple',(s*1.98,40.4,.05),(.875,3.25,2),'skin',(0,-s*20,0),feature='temple')
  C('head',side+'_ear',(s*2.5,39.05,.02),(.5,1.25,.75),'skin',(0,0,-s*8),feature='ear')
  # Opaque cloth follows the face; no visible eye geometry is added beneath it.
  C('face_base',side+'_blindfold',(s*1.03,39.70,-2.29),(2.125,1.125,.25),'blindfold',(0,-s*5,-s*8),pattern='embroidery',feature='blindfold')
  C('head',side+'_blindfold_side',(s*2.18,39.70,-.85),(.375,.75,2.875),'blindfold',(0,-s*11,0),pattern='band',feature='blindfold')
 C('head','blindfold_back',(0,39.65,1.83),(4.375,.75,.25),'blindfold',feature='blindfold')
 C('face_base','blindfold_bridge',(0,39.66,-2.41),(.625,.625,.25),'blindfold',feature='blindfold')
 C('face_base','nose_bridge',(0,39.15,-2.245),(.25,1.125,.375),'skin',(20,0,0),feature='nose')
 C('face_base','nose_tip',(0,38.65,-2.39),(.375,.25,.375),'skin',(20,0,0),feature='nose')
 B('mouth','face_base',(0,37.94,-2.20))
 C('mouth','mouth_recess',(0,37.94,-2.27),(.625,.25,.125),'mouth',feature='mouth')
 B('lip_lower','jaw',(0,37.86,-2.31))
 C('lip_lower','lower_lip',(0,37.86,-2.34),(.5,.125,.125),'lip',feature='expression')
 C('lip_lower','lower_lip_cover',(0,37.68,-2.28),(.875,.25,.25),'skin',feature='expression')
 for s,side in [(-1,'right'),(1,'left')]:
  B('lip_'+side,'mouth',(s*.16,38.025,-2.32))
  C('lip_'+side,side+'_upper_lip',(s*.16,38.025,-2.34),(.375,.125,.125),'lip',(0,0,s*9),feature='expression')
  B('mouth_corner_'+side,'mouth',(s*.34,37.95,-2.29))
  C('mouth_corner_'+side,side+'_lip_corner',(s*.34,37.95,-2.28),(.125,.125,.125),'lip',feature='expression')
 B('hair_crown','head',(0,42,.15));B('hair_back','head',(0,41.5,1.6))
 C('hair_back','hair_occipital',(0,40.1,1.85),(4.375,4.75,1.125),'hair_shadow',(-7,0,0),pattern='hair',feature='hair')
 C('hair_crown','hair_cap',(0,42.1,.15),(4.5,1.375,4.125),'hair',pattern='hair',feature='hair')
 C('hair_crown','hair_crown',(-.2,42.70,.2),(3.375,.875,3.375),'hair',(-4,0,-7),pattern='hair',feature='hair')
 C('hair_crown','front_swept_lock',(-.55,42.15,-1.70),(2.125,1.5,.875),'hair',(15,0,-27),pattern='hair',feature='hair')
 C('hair_crown','front_swept_tip',(-1.42,41.15,-1.94),(.875,1.875,.75),'hair',(8,-10,-18),pattern='hair',feature='hair')
 C('hair_crown','fringe_sweep',(-.40,41.85,-2.03),(2.125,2.625,.625),'hair',(7,0,-36),pattern='hair',feature='hair')
 C('hair_crown','fringe_outer',(-1.62,40.75,-2.03),(.875,2.375,.625),'hair',(4,-8,-16),pattern='hair',feature='hair')
 C('hair_crown','fringe_right_edge',(1.64,41.9,-1.85),(.75,2.0,.625),'hair',(4,8,18),pattern='hair',feature='hair')
 for s,side in [(-1,'right'),(1,'left')]:
  C('hair_crown',side+'_crown_bevel',(s*1.92,42,.1),(1.25,1.625,3.5),'hair',(0,0,s*28),pattern='hair',feature='hair')
  B('hair_'+side,'head',(s*2.15,41.6,.2))
  C('hair_'+side,side+'_side_bob',(s*2.35,40.0,.2),(.875,3.625,2.875),'hair',(0,0,s*5),pattern='hair',feature='hair')
  C('hair_'+side,side+'_jaw_lock',(s*2.25,38.15,-.66),(.75,1.5,1.25),'hair',(-7,-s*15,-s*17),pattern='hair',feature='hair')
 B('ribbon_right','head',(-1,39.7,2.15));B('ribbon_left','head',(1,39.7,2.15))
 C('ribbon_right','ribbon_right',(-1.2,34.8,2.5),(.625,10,.125),'blindfold',(-6,0,-7),feature='ribbon')
 C('ribbon_left','ribbon_left',(1.1,35.4,2.7),(.5,8.875,.125),'blindfold',(-10,0,6),feature='ribbon')
 # Short visible neck, matching the compendium silhouette. Uniform translation
 # of the assembly keeps facial pivots and attached hair in the same space.
 descendants={'head'}
 for b in m.bones:
  if b.get('parent') in descendants:descendants.add(b['name'])
  if b['name'] not in descendants:continue
  b['pivot'][1]-=12
  for c in b.get('cubes',[]):
   c['origin'][1]-=12
   if 'pivot' in c:c['pivot'][1]-=12

def build_wings(m):
 B=m.bone;C=m.box;L=m.segment
 for s,side in [(-1,'right'),(1,'left')]:
  B('wing_'+side,'body',(s*2.5,32.5,1.5))
  L('wing_'+side,side+'_wing_root',(s*2.4,32.5,1.7),(s*7.0,37.0,2.0),1.375,1.25,'feather_base',feature='wing')
  L('wing_'+side,side+'_wing_leading',(s*6.7,36.8,2),(s*18,42.5,2),1.5,.875,'feather_base',feature='wing')
  B('wing_'+side+'_outer','wing_'+side,(s*11.5,39.1,2))
  for i in range(13):
   u=i/12;start=np.array([s*(4.6+u*11.8),33.5+u*7.7,2.1])
   end=np.array([s*(7.0+u*20.0),27.6+u*17.4,2.6])
   d=end-start;d/=np.linalg.norm(d);bone='wing_'+side+('_outer' if i>=7 else '')
   L(bone,side+f'_primary_{i}',start,end,1.375,.375,'feather',pattern='feather',feature='feather')
   L(bone,side+f'_primary_tip_{i}',end-d*.25,end+d*.80,.75,.25,'feather',pattern='feather',feature='feather')
  for i in range(10):
   u=i/9;start=(s*(3.9+u*10.7),33.7+u*6.6,1.55);end=(s*(6.0+u*13.0),30.3+u*10.4,1.30)
   L('wing_'+side,side+f'_secondary_{i}',start,end,1.5,.5,'feather_base',pattern='feather',feature='feather')
   L('wing_'+side,side+f'_secondary_tip_{i}',np.array(end)-np.array([0,.3,0]),np.array(end)+np.array([s*.35,-.35,0]),.875,.375,'feather',feature='feather')

def build_chain(m):
 B=m.bone;C=m.box
 knots=np.array([[.9,34.2,-1.45],[2.9,31.1,-2.0],[3.2,27.0,-2.0],[1.6,23.1,-2.0],[-1.5,20.0,-2.1],[-4.0,17.4,-2.1]])
 points=[]
 for a,b in zip(knots,knots[1:]):
  n=max(1,round(np.linalg.norm(b-a)/.75))
  for i in range(n):points.append(a+(b-a)*i/n)
 points.append(knots[-1])
 B('chain','body',knots[0])
 for i,p in enumerate(points):
  nxt=points[min(len(points)-1,i+1)];prev=points[max(0,i-1)];delta=nxt-prev
  angle=math.degrees(math.atan2(-delta[0],delta[1]));bone='chain_link_'+str(i)
  B(bone,'chain',p,(0,65 if i%2 else 0,angle))
  for side in [-1,1]:
   C(bone,f'chain_{i}_side_{side}',p+[side*.25,0,0],(.125,.875,.125),'silver',feature='chain')
   C(bone,f'chain_{i}_end_{side}',p+[0,side*.4375,0],(.625,.125,.125),'silver',feature='chain')
