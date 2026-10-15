"""Offline rasterizer of the new model. Geometry/UV/camera only; no generated images."""
from pathlib import Path
import json,itertools,math
import numpy as np
from PIL import Image,ImageDraw,ImageFont
from build_model import face_uv
ROOT=Path(__file__).resolve().parent
OUT=ROOT/'draft'
data=json.loads((OUT/'nekomata.geo.json').read_text(encoding='utf-8'))['minecraft:geometry'][0]
texture=np.array(Image.open(OUT/'nekomata.png').convert('RGBA'))/255
animations=json.loads((OUT/'nekomata.animation.json').read_text(encoding='utf-8'))['animations']
KEYS={}
for clip,a in animations.items():
    for bone,tracks in a['bones'].items():
        for channel,keys in tracks.items():
            pairs=sorted((float(k),v) for k,v in keys.items())
            KEYS[(clip,bone,channel)]=(np.array([x[0] for x in pairs]),np.array([x[1] for x in pairs]))
head_bones={'head'}
for bone in data['bones']:
    if bone.get('parent') in head_bones:head_bones.add(bone['name'])
def T(v):
    m=np.eye(4);m[:3,3]=v;return m
def R(v):
    x,y,z=np.radians(v);cx,sx=np.cos(x),np.sin(x);cy,sy=np.cos(y),np.sin(y);cz,sz=np.cos(z),np.sin(z)
    m=np.eye(4)
    m[:3,:3]=np.array([[cz,-sz,0],[sz,cz,0],[0,0,1]])@np.array([[cy,0,sy],[0,1,0],[-sy,0,cy]])@np.array([[1,0,0],[0,cx,-sx],[0,sx,cx]])
    return m
FACE={'north':[0,2,6,4],'south':[1,5,7,3],'west':[0,1,3,2],
      'east':[4,6,7,5],'up':[2,3,7,6],'down':[0,4,5,1]}

def sample(name,channel,time,clip='animation.nekomata.ground_idle'):
    animation=animations[clip]
    keys=KEYS.get((clip,name,channel))
    if keys is None:return np.zeros(3)
    times,values=keys
    time=time%animation['animation_length'] if animation['loop'] else np.clip(time,0,animation['animation_length'])
    index=int(np.searchsorted(times,time,side='right'))
    if index==0:return values[0]
    if index>=len(times):return values[-1]
    a,b=times[index-1:index+1];av,bv=values[index-1:index+1]
    return av+(bv-av)*(time-a)/(b-a)

def surfaces(yaw,pitch,head=False,time=0,clip='animation.nekomata.ground_idle',bare=False):
    mats={};quads=[];camera=R((pitch,yaw,0))
    for b in data['bones']:
        p=np.array(b['pivot'])
        world=mats.get(b.get('parent'),np.eye(4))@T(sample(b['name'],'position',time,clip))@T(p)@R(np.array(b.get('rotation',[0,0,0]))+sample(b['name'],'rotation',time,clip))@T(-p)
        mats[b['name']]=world
        if head and b['name'] not in head_bones:continue
        if bare and (b['name']=='hat' or b['name'].startswith('hair_')):continue
        for c in b.get('cubes',[]):
            p=np.array(c.get('pivot',[0,0,0]));cm=camera@world@T(p)@R(c.get('rotation',[0,0,0]))@T(-p)
            verts=np.array([[*(np.array(c['origin'])+np.array(c['size'])*c.get('comparison_scale',1)*v),1] for v in itertools.product([0,1],repeat=3)])
            points=(cm@verts.T).T[:,:3]
            for face,ids in FACE.items():
                q=points[ids];normal=np.cross(q[1]-q[0],q[2]-q[0]);length=np.linalg.norm(normal)
                if length<1e-8:continue
                normal/=length
                if normal[2]>=0:continue
                u,v,w,h=face_uv(*c['uv'],*c['size'])[face]
                if w<=0 or h<=0:continue
                if face=='up':
                    uv=np.array([[u,v],[u,v+h],[u+w,v+h],[u+w,v]])
                elif face=='down':
                    uv=np.array([[u,v],[u+w,v],[u+w,v+h],[u,v+h]])
                elif face in ('north','east'):
                    uv=np.array([[u,v+h],[u,v],[u+w,v],[u+w,v+h]])
                else:
                    uv=np.array([[u,v+h],[u+w,v+h],[u+w,v],[u,v]])
                quads.append((q,uv,normal,b['name']))
    return quads

_bounds_source=None
_bounds_local={}

def bounds_vertices(yaw,pitch,time=0,clip='animation.nekomata.ground_idle'):
    """All cube vertices, including back faces, for conservative frame fitting."""
    global _bounds_source,_bounds_local
    if _bounds_source is not data:
        _bounds_source=data;_bounds_local={}
        for bone in data['bones']:
            chunks=[]
            for c in bone.get('cubes',[]):
                p=np.array(c.get('pivot',[0,0,0]));cm=T(p)@R(c.get('rotation',[0,0,0]))@T(-p)
                verts=np.array([[*(np.array(c['origin'])+np.array(c['size'])*c.get('comparison_scale',1)*v),1] for v in itertools.product([0,1],repeat=3)])
                chunks.append(verts@cm.T)
            if chunks:_bounds_local[bone['name']]=np.concatenate(chunks)
    mats={};points=[];camera=R((pitch,yaw,0))
    for b in data['bones']:
        p=np.array(b['pivot'])
        world=mats.get(b.get('parent'),np.eye(4))@T(sample(b['name'],'position',time,clip))@T(p)@R(np.array(b.get('rotation',[0,0,0]))+sample(b['name'],'rotation',time,clip))@T(-p)
        mats[b['name']]=world
        if b['name'] in _bounds_local:points.append((_bounds_local[b['name']]@(camera@world).T)[:,:3])
    return np.concatenate(points)

def render(yaw,width,height,head=False,pitch=4,time=0,bounds=None,clip='animation.nekomata.ground_idle',clay=False,bare=False):
    quads=surfaces(yaw,pitch,head,time,clip,bare)
    vertices=np.concatenate([q[0] for q in quads]);lo=vertices.min(axis=0);hi=vertices.max(axis=0)
    if bounds is not None:lo,hi=bounds
    scale=min((width-50)/(hi[0]-lo[0]),(height-44)/(hi[1]-lo[1]))
    center=(lo+hi)/2
    bg=np.array([.88,.91,.92])
    rgb=np.empty((height,width,3));rgb[:]=bg;depth=np.full((height,width),np.inf)
    light=np.array([-.42,.55,-.72]);light/=np.linalg.norm(light)
    opaque=[q for q in quads if not q[3].startswith(('veil_','skirt_'))]
    translucent=sorted([q for q in quads if q[3].startswith(('veil_','skirt_'))],key=lambda q:-float(q[0][:,2].mean()))
    for q,uv,normal,bone in opaque+translucent:
        screen=q.copy();screen[:,0]=width/2+(q[:,0]-center[0])*scale
        screen[:,1]=height/2-(q[:,1]-center[1])*scale
        shade=(.40+.6*max(0,float(normal@light))) if clay else (.76+.24*max(0,float(normal@light)))
        for ids in ([0,1,2],[0,2,3]):
            p=screen[ids];tex=uv[ids]
            x0=max(0,int(p[:,0].min()));x1=min(width,int(p[:,0].max())+1)
            y0=max(0,int(p[:,1].min()));y1=min(height,int(p[:,1].max())+1)
            if x0>=x1 or y0>=y1:continue
            a,b,c=p;den=(b[1]-c[1])*(a[0]-c[0])+(c[0]-b[0])*(a[1]-c[1])
            if abs(den)<1e-7:continue
            ys,xs=np.mgrid[y0:y1,x0:x1];xs=xs+.5;ys=ys+.5
            aa=((b[1]-c[1])*(xs-c[0])+(c[0]-b[0])*(ys-c[1]))/den
            bb=((c[1]-a[1])*(xs-c[0])+(a[0]-c[0])*(ys-c[1]))/den;cc=1-aa-bb
            z=aa*a[2]+bb*b[2]+cc*c[2]
            tx=aa*tex[0,0]+bb*tex[1,0]+cc*tex[2,0];ty=aa*tex[0,1]+bb*tex[1,1]+cc*tex[2,1]
            col=texture[np.clip(ty.astype(int),0,texture.shape[0]-1),np.clip(tx.astype(int),0,texture.shape[1]-1)]
            if clay:col=np.broadcast_to(np.array([.70,.73,.76,1]), col.shape)
            mask=(aa>=0)&(bb>=0)&(cc>=0)&(z<depth[y0:y1,x0:x1])&(col[:,:,3]>.01)
            if not mask.any():continue
            target=rgb[y0:y1,x0:x1];alpha=col[:,:,3:4]
            target[mask]=(col[:,:,:3]*shade*alpha+target*(1-alpha))[mask]
            if not bone.startswith(('veil_','skirt_')) or clay:depth[y0:y1,x0:x1][mask]=z[mask]
    return Image.fromarray(np.clip(rgb*255,0,255).astype('uint8'))
