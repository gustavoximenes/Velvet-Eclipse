"""Approved Velvet Room geometry and fusion keyframes; runtime uses the live player skin.
Generated from the approved studies, then maintained as the portable source of the scene assets.
"""
from pathlib import Path
from functools import lru_cache
import importlib.util,sys,math,inspect
import numpy as np
from PIL import Image,ImageDraw,ImageFilter
ROOT=Path(__file__).resolve().parent
ASSET=ROOT.parents[1]/'src/main/resources/assets/velvet_eclipse'
sp=importlib.util.spec_from_file_location('velvet_base',ROOT.parent/'awakening/choreography.py')
b=importlib.util.module_from_spec(sp);sys.modules[sp.name]=b;sp.loader.exec_module(b)
b.W,b.H=1280,720
T,R,S=b.T,b.R,b.S
W,H=1280,720
scope=dict(b.__dict__)
exec(inspect.getsource(b.Model).replace("if name in ('orpheus','arsene','izanagi','field_shadow'):","if True:").replace('for ts,v in keys.items():','for ts,v in (keys.items() if isinstance(keys,dict) else []):\n       v=v.get("vector",v.get("post",[0,0,0])) if isinstance(v,dict) else v'),scope)
@lru_cache(None)
def model(name):return scope['Model'](name)
v=f=q=sys.modules[__name__]
def smooth(x):x=np.clip(x,0,1);return float(x*x*(3-2*x))

def mix(a,z,t):return np.asarray(a)+(np.asarray(z)-a)*t

def line3(draw,cam,points,fill,width=1):
    p=cam.project(points)
    if p[:,2].min()>.08:draw.line([tuple(v[:2]) for v in p],fill=fill,width=width,joint='curve')

def poly3(draw,cam,points,fill):
    p=cam.project(points)
    if p[:,2].min()>.08:draw.polygon([tuple(v[:2]) for v in p],fill=fill)

def face(points,color):return [(np.array(points),None,None,np.array(color,dtype=float))]

def cylinder(pos,r,h,col,n=48):
    out=[];x,y,z=pos
    for i in range(n):
        a=i*math.tau/n;c=(i+1)*math.tau/n
        p=(x+math.cos(a)*r,y,z+math.sin(a)*r);q=(x+math.cos(c)*r,y,z+math.sin(c)*r)
        shade=.8+.2*(i%3)/2
        out+=face([p,(p[0],y+h,p[2]),(q[0],y+h,q[2]),q],np.array(col)*shade)
        out+=face([(x,y+h,z),(q[0],y+h,q[2]),(p[0],y+h,p[2]),(x,y+h,z)],np.array(col)*1.12)
    return out

def grille(draw,cam,side=False,mirror=1,glow=False):
    # Tall ironwork, repeated diamonds and scrolls: elevator, not a dungeon cell.
    def point(x,y):return (mirror*5.1,y,x) if side else (x,y,5.2)
    color=(33,40,102) if not glow else (102,128,244)
    for x in np.arange(-5,5.1,.5):
        line3(draw,cam,[point(x,.05),point(x,6.8)],color,2)
        line3(draw,cam,[point(x+.035,.1),point(x+.035,6.8)],(18,26,76),2)
    for yy in [.22,.34,3.85,4,6.55,6.7]:line3(draw,cam,[point(-5,yy),point(5,yy)],color,2)
    for x in np.arange(-4.75,5,.5):
        for y in np.arange(.65,6.3,.7):
            line3(draw,cam,[point(x,y-.3),point(x+.18,y),point(x,y+.3),point(x-.18,y),point(x,y-.3)],(22,29,74),1)
    for x in [-3,-1,1,3]:
        for sy in [1.65,3.05,4.9,6.05]:
            for sign in [-1,1]:
                pts=[]
                for theta in np.linspace(0,math.tau*1.3,40):
                    r=.37*(1-theta/(math.tau*1.45));pts.append(point(x+sign*(.13+r*math.cos(theta)),sy+r*math.sin(theta)))
                line3(draw,cam,pts,(48,56,124),2)
        line3(draw,cam,[point(x,0),point(x,6.7)],(41,49,118),3)

def room_background(cam):
    im=Image.new('RGB',(W,H),'#030421');d=ImageDraw.Draw(im)
    for y in range(H):
        a=y/H;d.line((0,y,W,y),fill=(5+int(a*3),7+int(a*4),27+int(a*24)))
    poly3(d,cam,[(-5.3,0,-6),(-5.3,0,5.4),(5.3,0,5.4),(5.3,0,-6)],(20,23,88))
    # Fine indigo carpet squares, subtle enough not to become the corridor pattern.
    for x in range(-11,11):
        for z in range(-12,11):
            c=(19,22,89) if (x+z)%2==0 else (18,21,86)
            poly3(d,cam,[(x*.5,.002,z*.5),((x+1)*.5,.002,z*.5),((x+1)*.5,.002,(z+1)*.5),(x*.5,.002,(z+1)*.5)],c)
    for rr in [1.85,1.9,2.7,2.74]:
        line3(d,cam,[(math.cos(a)*rr,.006,1.05+math.sin(a)*rr) for a in np.linspace(0,math.tau,100)],(42,44,136),1)
    grille(d,cam);grille(d,cam,True,-1);grille(d,cam,True,1)
    for x in [-4.4,-2.5,2.5,4.4]:
        for dx in [-.11,0,.11]:line3(d,cam,[(x+dx,.2,5.08),(x+dx,6.5,5.08)],(91,101,185),2)
    for x in [-4.35,4.35]:
        line3(d,cam,[(x,.2,4.95),(x,5.9,4.95)],(167,188,255),4)
    # Projected contact shadows give the furnishings weight on the carpet.
    shadows=Image.new('RGBA',(W,H));sd=ImageDraw.Draw(shadows)
    for x,z,rx,rz,alpha in [(0,1.15,1.8,1.8,125),(0,3.25,1.55,.75,160),(-2.45,3.05,.42,.50,140)]:
        pts=[(x+math.cos(a)*rx,.01,z+math.sin(a)*rz) for a in np.linspace(0,math.tau,80)]
        poly3(sd,cam,pts,(0,0,19,alpha))
    shadows=shadows.filter(ImageFilter.GaussianBlur(13));im.paste(shadows,(0,0),shadows)
    return im

def furniture():
    f=[]
    # Tall tufted violet couch with rolled arms; fabricated from cuboids.
    f+=b.box((0,.58,3.18),(2.7,.32,.92),(28,27,123))
    f+=b.box((0,1.26,3.55),(2.63,1.65,.26),(31,27,110))
    for row in range(5):
        for col in range(9):
            x=(col-4)*.29;y=.58+row*.31
            if abs(x)>.99 and row==4:continue
            mat=T((x,y,3.365))@R((0,0,45))
            f+=b.box((0,0,0),(.24,.24,.10),(59+row*3,45+col%2*7,171),mat)
    for x in [-1.35,1.35]:
        f+=b.box((x,.83,3.08),(.31,.48,.95),(47,37,157))
        f+=b.box((x,1.03,3.04),(.36,.17,1.04),(61,45,188))
        f+=b.box((x,1.92,3.52),(.29,.23,.29),(70,59,196))
        f+=b.box((x,.2,3.2),(.16,.4,.45),(21,17,55))
    # Cloth folds are slim facets on a round tabletop, with darker hanging velvet.
    f+=cylinder((0,.10,1.07),1.61,.91,(52,43,185),64)
    f+=cylinder((0,1.01,1.07),1.65,.065,(71,61,223),64)
    for i in range(64):
        a=i*math.tau/64
        f+=b.box((math.cos(a)*1.62,.54,1.07+math.sin(a)*1.62),(.027,.86,.027),(43+(i%3)*8,35+(i%2)*5,158+(i%3)*15))
    # Low plinths and vertical chrome light bands.
    for x in [-4.7,4.7]:
        f+=b.box((x,3.1,4.92),(.36,6.2,.35),(36,38,100))
        f+=b.box((x-.14,3.1,4.7),(.035,6.1,.05),(135,151,231))
    return f

def actor(name,pos,yaw=0,scale=.016,time=0):
    return model(name).faces(time,'misc.idle',T(pos)@R((0,yaw,0))@S(scale))
def ease(t,a,z):return v.smooth((t-a)/(z-a))

def unit(x):
    x=np.asarray(x,dtype=float);return x/max(1e-10,np.linalg.norm(x))

def align(a,z):
    a=unit(a);z=unit(z);cross=np.cross(a,z);c=float(a@z)
    if c>.999999:return np.eye(4)
    if c<-.999999:
        axis=unit(np.cross(a,[1,0,0] if abs(a[0])<.8 else [0,1,0]));mat=2*np.outer(axis,axis)-np.eye(3)
    else:
        x,y,zz=cross;k=np.array([[0,-zz,y],[zz,0,-x],[-y,x,0]]);mat=np.eye(3)+k+k@k/(1+c)
    out=np.eye(4);out[:3,:3]=mat;return out

def igor_pose(t):
    m=v.model('igor');bones={q['name']:q for q in m.bones}
    open_amount=ease(t,1.8,3.2)*(1-.18*ease(t,5.5,6.6))
    close_amount=ease(t,6.7,7.6)
    open_amount*=1-close_amount
    overrides={}
    for side,weight in [('left',1),('right',.48)]:
        upper=side+'_upper_arm';fore=side+'_forearm';hand=side+'_hand'
        sh=np.array(bones[upper]['pivot']);el=np.array(bones[fore]['pivot']);wr=np.array(bones[hand]['pivot'])
        sign=1 if sh[0]>0 else -1
        # Move through shoulder/elbow rotations only, never translate or scale limbs.
        target_upper=v.mix(el-sh,[sign*25,-9,-8],open_amount*weight)
        target_fore=v.mix(wr-el,[sign*24,7,-13],open_amount*weight)
        u=align(el-sh,target_upper)
        f=align(wr-el,u[:3,:3].T@target_fore)
        overrides[upper]=u;overrides[fore]=f
        overrides[hand]=R((0,0,-sign*7*open_amount))
        for i in range(4):overrides[side+f'_finger_{i}']=R((0,(i-1.5)*2.5*open_amount,0))
    overrides['head']=R((-5*ease(t,2.4,3.7)+3*ease(t,5.7,7.0),0,0))
    overrides['body']=R((1.6*open_amount,0,0))
    mats={};faces=[];root=T((0,.12,3.13))@S(.016)
    for bone in m.bones:
        n=bone['name'];pivot=np.array(bone['pivot']);rot=overrides.get(n,np.eye(4))
        matrix=mats.get(bone.get('parent'),root)@T(pivot)@R(bone.get('rotation',[0,0,0]))@rot@T(-pivot)
        mats[n]=matrix
        for verts,uvs in m.cubes[n]:
            q=(matrix@verts.T).T[:,:3]
            for ids,uv in uvs:faces.append((q[ids],uv,m.tex,None))
    return faces,mats

def initial_card_state(t,i):
    sign=-1 if i==0 else 1
    if t<2.4:
        a=ease(t,.25,1.2);return np.array([sign*.56,1.20+.40*a,2.04]),sign*6,sign*(-6+9*a),.48
    rise=ease(t,2.4,3.65);orbit=ease(t,3.6,5.45);collapse=ease(t,5.2,6.4)
    angle=sign*math.pi*.5+(t-3.55)*2.1
    x=sign*.56*(1-rise)+math.cos(angle)*.83*rise
    y=1.6+1.68*rise+math.sin(angle*2)*.22*orbit
    z=2.04+math.sin(angle)*.34*orbit
    center=v.mix([x,y,z],[0,3.2,1.86],collapse)
    return center,math.sin(angle)*35*orbit,sign*(3+124*orbit),.48*(1-.35*rise)

def shot(t):
    if t<2.5:
        a=ease(t,0,2.5)
        return b.Camera(v.mix([-.12,2.13,-.25],[.12,2.08,-.05],a),(0,1.69,2.8),47)
    if t<6.5:
        a=ease(t,2.5,6.5)
        return b.Camera(v.mix([.18,2.8,-3.45],[.12,2.78,-3.1],a),(0,2.12,2.65),49)
    return b.Camera((4.8,2.65,-.2),(0,1.78,-2.1),48)
def bezier(points,t):
    p=np.asarray(points);return p[0]*(1-t)**3+3*p[1]*t*(1-t)**2+3*p[2]*t*t*(1-t)+p[3]*t**3

LIFT=1.8;DANCE=2.6;LEVEL=3.5;LAUNCH=4.;IMPACT=5.;END=7.6
NECK=np.array([0,1.4,0]);PLAYER_MATRIX=T((0,0,-2.15))@R((0,180,0))
HEAD_REST=T(NECK)@R((6,-10,0))@T(-NECK)
SIDE=np.array([1.,0,0]);UP=np.array([0.,1,0])
BACK=unit((PLAYER_MATRIX@np.array([0,.28,1,0]))[:3])
def recoil(t):
    if t<IMPACT:return 0.
    return 24*f.ease(t,IMPACT,IMPACT+.2)*(1-.45*f.ease(t,5.35,5.85))*(1-f.ease(t,6.1,6.9))

def head_motion(t):return T(NECK)@R((recoil(t),0,0))@T(-NECK)

def head_frame(t):return PLAYER_MATRIX@head_motion(t)@HEAD_REST

def head_point(point,t):return (head_frame(t)@np.r_[point,1])[:3]

def forehead(t=IMPACT):return head_point([0,1.69,-.251],t)

def nape(t):return head_point([0,1.50,.252],t)

def dance_position(t,i):
    sign=-1 if i==0 else 1;angle=math.tau*f.ease(t,DANCE,LEVEL)
    return np.array([sign*.68*math.cos(angle),2.56+sign*.21*math.sin(angle),1.48+sign*.26*math.sin(angle)])

def card_state(t,i):
    sign=-1 if i==0 else 1
    if t<=LIFT:
        pos,yaw,roll,width=initial_card_state(t,i)
        return pos,np.array([0,yaw,roll]),width
    launch=np.array([sign*.68,forehead()[1],1.2])
    if t<DANCE:
        u=f.ease(t,LIFT,DANCE)
        pos=q.bezier([[sign*.56,1.6,2.04],[sign*.74,1.78,1.94],[sign*.83,2.40,1.65],dance_position(DANCE,i)],u)
        return pos,np.array([0,sign*6*(1-u),sign*(3+9*u)]),.48-.06*u
    if t<LEVEL:
        u=f.ease(t,DANCE,LEVEL);angle=math.tau*u
        return dance_position(t,i),np.array([12*math.sin(angle),sign*30*math.sin(angle),sign*(12+58*math.sin(angle))]),.42
    if t<LAUNCH:
        u=f.ease(t,LEVEL,LAUNCH)
        return v.mix(dance_position(LEVEL,i),launch,u),np.array([90*u,0,sign*12*(1-u)]),.42
    # One second of accelerated forward travel, half the previous duration.
    u=float(np.clip((t-LAUNCH)/(IMPACT-LAUNCH),0,1))**1.5
    pos=q.bezier([launch,[sign*.78,forehead()[1],.35],[sign*.32,forehead()[1],-.98],forehead()],u)
    # Plane stays horizontal throughout actual flight, including the final contact.
    return pos,np.array([90,sign*11*math.sin(u*math.pi),0]),.42

def camera(t):
    if t<2.1:return f.shot(t)
    u=f.ease(t,3.8,5.4)
    # Keep the shot inside the x=5.1 elevator grille; its bars must not cross Igor/cards.
    eye=v.mix([4.65,3.05,-5.1],[3.4,2.25,-.5],u)
    target=v.mix([0,1.73,.35],[0,1.61,-1.95],u)
    return b.Camera(eye,target,53-10*u)

def effect_strength(t):return 0 if t<IMPACT else .25+.75*f.ease(t,IMPACT,IMPACT+.22)

def ribbons(t):
    strength=effect_strength(t)
    if strength==0:return []
    origin=nape(t);age=t-IMPACT;result=[]
    for j in range(5):
        points=[]
        for u in np.linspace(0,1,54):
            angle=age*3+j*1.3+u*3.5
            spread=u*.36*strength
            points.append(origin+BACK*(u*(1.2+j*.10)*strength)+SIDE*(math.cos(angle)*spread)+UP*(math.sin(angle)*spread*.75))
        result.append(points)
    return result
