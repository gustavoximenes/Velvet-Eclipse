"""Pose authoring for the three independent previews; no game integration."""
import json,math
import numpy as np
import build_study as model

OUT=model.OUT
bones=model.bones
lookup={b['name']:b for b in bones}

def rotation(v):
    x,y,z=np.radians(v);cx,sx=np.cos(x),np.sin(x);cy,sy=np.cos(y),np.sin(y);cz,sz=np.cos(z),np.sin(z)
    return np.array([[cz,-sz,0],[sz,cz,0],[0,0,1]])@np.array([[cy,0,sy],[0,1,0],[-sy,0,cy]])@np.array([[1,0,0],[0,cx,-sx],[0,sx,cx]])

def euler(m):
    y=math.asin(float(np.clip(-m[2,0],-1,1)))
    if abs(math.cos(y))>.00001:x=math.atan2(m[2,1],m[2,2]);z=math.atan2(m[1,0],m[0,0])
    else:x=0;z=math.atan2(-m[0,1],m[1,1])
    return np.degrees([x,y,z])

def fk(pose):
    result={}
    for b in bones:
        r=rotation(pose.get(b['name'],b.get('rotation',[0,0,0])))
        p=np.array(b['pivot'])/model.DENSITY
        m=np.eye(4);m[:3,:3]=r;m[:3,3]=p-r@p
        result[b['name']]=result.get(b.get('parent'),np.eye(4))@m
    return result

def wrist(pose,angle):
    world=fk(pose)
    desired=world['body'][:3,:3]@rotation([0,0,angle])
    return euler(world['right_forearm'][:3,:3].T@desired@rotation(lookup['weapon']['rotation']).T)

# Orient the idle shaft independently of the bent elbow; it stays in the fist.
lookup['right_hand']['rotation']=wrist({},-68).tolist()
base={b['name']:np.array(b.get('rotation',[0,0,0]),float) for b in bones}

def direction_frame(d):
    y=-d/np.linalg.norm(d);z=np.array([0.,0.,1.]);z-=y*np.dot(z,y)
    if np.linalg.norm(z)<.01:z=np.array([1.,0.,0.]);z-=y*np.dot(z,y)
    z/=np.linalg.norm(z);x=np.cross(y,z)
    return np.column_stack((x,y,z))

def support_grip(pose):
    """Two-bone IK: the free palm reaches the rear part of the same shaft."""
    world=fk(pose);body=world['body'];inv=np.linalg.inv(body)
    target=(inv@world['weapon']@np.array([-9.,35.,-.5,1.]))[:3]
    shoulder=np.array([9.,49.,0.]);d=target-shoulder;dist=np.linalg.norm(d)
    reach=float(np.clip(dist,.2,21.85));axis=d/max(dist,1e-8)
    pole=np.array([0.,0.,-1.]);pole-=axis*np.dot(pole,axis);pole/=max(np.linalg.norm(pole),1e-8)
    a=(10**2-12**2+reach**2)/(2*reach)
    elbow=shoulder+axis*a+pole*math.sqrt(max(0,10**2-a*a))
    upper=direction_frame(elbow-shoulder)
    forearm=direction_frame(shoulder+axis*reach-elbow)
    return {'left_upper_arm':euler(upper),'left_forearm':euler(upper.T@forearm),'left_hand':np.zeros(3)},max(0,dist-reach)

def blend_angles(a,b,f):
    return a+((b-a+180)%360-180)*f

def interpolate(frames,t):
    for (ta,pa),(tb,pb) in zip(frames,frames[1:]):
        if ta<=t<=tb:
            f=(t-ta)/(tb-ta);f=f*f*(3-2*f)
            return {n:np.array(pa.get(n,base.get(n,[0,0,0])))+(np.array(pb.get(n,base.get(n,[0,0,0])))-np.array(pa.get(n,base.get(n,[0,0,0]))))*f for n in set(pa)|set(pb)}
    return frames[-1][1].copy()

# Two supplied magic poses are treated as gathering -> chest/arms opening.
magic=[(0,{}),(.3,{
    'root_position':[0,-1,0],'body':[-12,0,0],'head':[-12,-6,0],
    'left_upper_arm':[-5,0,30],'right_upper_arm':[-12,0,-28],
    'left_hand':[35,-10,0],'right_forearm':[-15,0,0]}),
(.8,{'root_position':[0,2,0],'body':[-8,-10,0],'head':[-8,0,0],
    'left_upper_arm':[4,0,48],'right_upper_arm':[-8,0,-48],
    'left_forearm':[-12,0,0],'right_forearm':[-12,0,0],
    'left_hand':[80,0,0],'right_hand':[15,0,12],
    'coat_left_lower':[-18,0,32],'coat_right_lower':[-18,0,-32],
    'coat_back_lower':[-27,0,0],
    'right_thigh':[40,0,-20],'left_thigh':[8,0,30],
    'right_ribbon':[-30,-15,-45],'left_ribbon':[-30,15,45]}),
(1.25,{'root_position':[0,4,0],'body':[22,12,0],'head':[10,0,0],
    'left_upper_arm':[8,0,72],'right_upper_arm':[8,0,-68],
    'left_forearm':[-8,0,0],'right_forearm':[-5,0,0],
    'left_hand':[75,0,-8],'right_hand':[10,0,12],
    'coat_left_lower':[-30,0,53],'coat_right_lower':[-30,0,-53],
    'coat_back_lower':[-40,0,0],
    'right_thigh':[38,0,-25],'left_thigh':[-8,0,30],
    'right_ribbon':[-45,-20,-65],'left_ribbon':[-45,20,65],
    'right_ribbon_end':[20,0,-25],'left_ribbon_end':[20,0,25]}),
(1.65,{'root_position':[0,3,0],'body':[14,8,0],'head':[6,0,0],
    'left_upper_arm':[5,0,65],'right_upper_arm':[5,0,-61],
    'left_forearm':[-8,0,0],'right_forearm':[-8,0,0],
    'left_hand':[65,0,0],'right_hand':[12,0,12],
    'coat_left_lower':[-24,0,44],'coat_right_lower':[-24,0,-44],
    'coat_back_lower':[-32,0,0],
    'right_ribbon':[-32,-18,-45],'left_ribbon':[-32,18,45]}),
(2.5,{}),(2.8,{})]

physical=[(0,{}),(.3,{'root_position':[0,-1.5,1.5],'body':[-14,28,-7],
    'right_upper_arm':[30,0,70],'right_forearm':[10,0,0]}),
(.55,{'root_position':[0,-3,3],'body':[-26,55,-10],'head':[-5,-25,0],
    'right_upper_arm':[35,-10,70],'right_forearm':[15,0,0],
    'right_thigh':[65,0,-20],'right_shin':[-100,0,0],
    'left_thigh':[-25,0,30],'left_shin':[-10,0,0],
    'coat_left_lower':[-15,10,25],'coat_right_lower':[-18,10,-25],
    'right_ribbon':[-25,-35,-35],'left_ribbon':[-25,-10,35]}),
(.83,{'root_position':[0,-1.5,-10],'body':[-30,-95,-15],'head':[-10,20,0],
    'right_upper_arm':[30,0,58],'right_forearm':[8,0,0],
    'right_thigh':[55,0,-15],'right_shin':[-85,0,0],
    'left_thigh':[-38,0,15],'left_shin':[-8,0,0],
    'coat_left_lower':[-25,-20,40],'coat_right_lower':[-28,-20,-40],
    'coat_back_lower':[-38,0,0],
    'right_ribbon':[-55,35,-65],'left_ribbon':[-55,25,65],
    'right_ribbon_end':[8,15,-30],'left_ribbon_end':[8,15,30]}),
(1.1,{'root_position':[0,-1,-12],'body':[-18,-125,-10],'head':[-6,20,0],
    'right_upper_arm':[25,0,50],'right_forearm':[12,0,0],
    'right_thigh':[45,0,-12],'right_shin':[-72,0,0],
    'left_thigh':[-20,0,22],
    'coat_left_lower':[-20,-10,32],'coat_right_lower':[-20,-10,-32],
    'right_ribbon':[-40,30,-50],'left_ribbon':[-40,25,50]}),
(1.85,{}),(2.2,{})]

animations=model.animation['animations']
grip_errors=[]
def bake(name,frames):
    duration=frames[-1][0];out={};last={}
    allnames=set().union(*(set(p) for _,p in frames));allnames.discard('root_position')
    allnames|={'left_upper_arm','left_forearm','left_hand','right_hand'}
    allnames|={f'left_finger_{i}' for i in range(4)}
    for time in sorted(set([round(i*.025,4) for i in range(round(duration/.025)+1)]+[t for t,_ in frames])):
        pose=interpolate(frames,time)
        if name=='attack.physical':
            w=min(1,time/.3,max(0,(1.85-time)/.55));w=w*w*(3-2*w)
            pose['right_hand']=blend_angles(base['right_hand'],wrist(pose,-90),w)
            support,error=support_grip(pose)
            if w>.999:grip_errors.append(error)
            for n,v in support.items():pose[n]=blend_angles(base[n],v,w)
            for i in range(4):pose[f'left_finger_{i}']=blend_angles(base[f'left_finger_{i}'],np.array([78,0,0]),w)
        else:
            w=min(1,time/.8,max(0,(2.5-time)/.8))
            for i in range(4):pose[f'left_finger_{i}']=blend_angles(base[f'left_finger_{i}'],np.array([2,0,(i-1.5)*5]),w)
        for n in allnames:
            value=np.array(pose.get(n,base[n]))-base[n]
            if n in last:value=last[n]+((value-last[n]+180)%360-180)
            last[n]=value
            out.setdefault(n,{}).setdefault('rotation',{})[str(time)]=np.round(value,5).tolist()
        out.setdefault('root',{}).setdefault('position',{})[str(time)]=(np.array(pose.get('root_position',[0,0,0]))*model.DENSITY).round(5).tolist()
    animations[name]={'loop':False,'animation_length':duration,'bones':out}

bake('attack.magic',magic)
bake('attack.physical',physical)
(OUT/'izanagi.animation.json').write_text(json.dumps({'format_version':'1.8.0','animations':animations},indent=2),encoding='utf-8')
(OUT/'izanagi.geo.json').write_text(json.dumps(model.geometry,indent=2),encoding='utf-8')
(OUT/'animation-review.json').write_text(json.dumps({
 'status':'preview_only','magic':{'gather':.8,'release':1.25,'return':2.5},
 'physical':{'windup':.55,'impact':.83,'follow_through':1.1,'return':1.85},
 'support_hand_max_unreachable_distance':max(grip_errors,default=0)},indent=2),encoding='utf-8')
print('Animation clips prepared:',', '.join(animations),'; support reach error:',round(max(grip_errors,default=0),3))
