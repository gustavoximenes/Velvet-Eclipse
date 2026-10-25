import numpy as np
import render_model as r
def worlds(t,clip):
    out={}
    for b in r.data['bones']:
        p=np.array(b['pivot'])
        out[b['name']]=out.get(b.get('parent'),np.eye(4))@r.T(r.sample(b['name'],'position',t,clip))@r.T(p)@r.R(np.array(b.get('rotation',[0,0,0]))+r.sample(b['name'],'rotation',t,clip))@r.T(-p)
    return out

def box(part,pose):
    c=part['cube'];p=np.array(c.get('pivot',[0,0,0]));m=pose[part['bone']]@r.T(p)@r.R(c.get('rotation',[0,0,0]))@r.T(-p)
    return (m@np.r_[np.array(c['origin'])+np.array(c['size'])/2,1])[:3],m[:3,:3],np.array(c['size'])/2

def overlap(a,b,pose):
    ac,ar,ah=box(a,pose);bc,br,bh=box(b,pose)
    axes=[*ar.T,*br.T]+[np.cross(x,y) for x in ar.T for y in br.T];depths=[]
    for v in axes:
        norm=np.linalg.norm(v)
        if norm<1e-8:continue
        v=v/norm;depths.append(float(np.abs(ar.T@v)@ah+np.abs(br.T@v)@bh-abs(v@(bc-ac))))
    return min(depths)
