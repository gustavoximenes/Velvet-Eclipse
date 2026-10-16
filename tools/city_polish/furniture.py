"""Append local door states without renumbering any installed architecture tile.

Seats and door anchors are measured from the approved authors' furniture functions.
Computers, counters and architecture outside the leaves are retained verbatim.
"""
from pathlib import Path
import gzip,json,math
from collections import Counter
import numpy as np
import export as e

DATA=e.DATA; AS=e.AS
def read(p):return json.loads(gzip.decompress(p.read_bytes()))
plan=read(DATA/'plan.json.gz');defs=read(DATA/'architecture.json.gz')
base_count=max(c[4] for c in plan['cells'] if c[3]==40)+1
defs=defs[:base_count];end=defs[-1]['offset']+defs[-1]['length']
binary=np.frombuffer(gzip.decompress((AS/'city/architecture.bin.gz').read_bytes()),dtype='>f4').astype('<f4')[:end]
chunks=[binary];cursor=len(binary)
cells={tuple(c[:3]):c[4] for c in plan['cells'] if c[3]==40}
doors=[];seats=[]
def seat(id,x,y,z,yaw,bounds):
    anchor=(math.floor(x),math.floor(y),math.floor(z));assert anchor in cells,(id,anchor)
    seats.append(dict(id=id,x=x,y=y,z=z,yaw=yaw,bounds=bounds,anchor=list(anchor),tile=cells[anchor]))
def dorm_seat(id,x,y,z,yaw=180,w=1,d=1,top=.97):
    seat(id,x-100+w/2,y+65+top-.55,z+66+d/2,yaw,[x-100,y+65,z+66,x-100+w,y+67.15,z+66+d])
for lev in [1,2,3]:
    for i,x in enumerate([-8.7,4] if lev<3 else [-8,4]):dorm_seat(f'bedroom_{lev}_{i}',-.5 if x<0 else 11.5,lev*5,8.78)
    if lev<3:
        for x in [-7,6]:dorm_seat(f'reading_{lev}_{x}',x,lev*5,-5.5)
for x in [9,11]:dorm_seat(f'dining_{x}',x,0,-.3)
dorm_seat('library',-5,0,-5.5)
for x in [2.89,5.64,8.39,11.14]:dorm_seat(f'operator_{x}',x,15,-6.65,w=1.08,d=1.02,top=.87)
for x in [-7,-5,-3]:
    dorm_seat(f'meeting_n_{x}',x,15,-8.7,0,w=1.08,d=1.02,top=.87)
    dorm_seat(f'meeting_s_{x}',x,15,-4.85,w=1.08,d=1.02,top=.87)
for x in [5.55,6.7,7.85,9.0]:dorm_seat(f'sofa_s_{x}',x,0,8.12,w=.85,d=1.05,top=1.06)
for z in [3.85,5,6.15,7.2]:dorm_seat(f'sofa_e_{z}',12.12,0,z,90,w=1.05,d=.85,top=1.06)
for x in [8.9,10.1,11.3,12.5]:dorm_seat(f'banquette_{x}',x,0,-2.45,0,w=.9,d=.55,top=1.05)
def mall_point(angle,r,x,y,z):
    a=math.radians(angle);c,s=math.cos(a),math.sin(a)
    return [93+s*r+c*x-s*z,64+y,65-c*r+s*x+c*z]
def mall_seat(id,a,r,x,y,z,w,d,yaw):
    p=mall_point(a,r,x,y-.55,z);corners=np.array([mall_point(a,r,xx,yy,zz) for xx in [x-w/2,x+w/2] for yy in [0,y+.65] for zz in [z-d/2,z+d/2]])
    seat(id,*p,yaw-a,np.r_[corners.min(0),corners.max(0)].tolist())
for x in [-2.35,2.35]:mall_seat(f'chagall_{x}',-67.5,19.5,x,.97,-1,.64,.64,180)
for x in [-2.3,-1.2,1.2,2.3]:mall_seat(f'escapade_{x}',-90,19.5,x,.97,-3.05,.64,.64,180)
for a in range(0,360,90):
    for x in [-.62,.62]:mall_seat(f'bench_{a}_{x}',a,4.25,x,.65,.04,.9,.55,0)
for x in [-2.3,2.3]:mall_seat(f'blue_{x}',-45,19.5,x,.70,-1.8,.9,1.2,0)

def transform(raw,boxes,hinge,angle,offset=(0,0,0)):
    a=math.radians(angle);c,s=math.cos(a),math.sin(a);m=np.array([[c,0,-s],[0,1,0],[s,0,c]])
    out=e.lock_uv(raw);out[:,:,:3]=(out[:,:,:3]-hinge)@m.T+hinge+offset;out[:,:,3:6]=out[:,:,3:6]@m.T
    bs=[]
    for b in boxes:
        pts=np.array([[x,y,z] for x in [b[0],b[3]] for y in [b[1],b[4]] for z in [b[2],b[5]]]);pts=(pts-hinge)@m.T+hinge+offset
        bs.append(np.r_[pts.min(0),pts.max(0)].tolist())
    return out,bs
def bake(raw,boxes):return e.bake_geometry('dorm',{'solids':boxes},raw.copy())[1:3]
def keytri(a):return np.round(a,5).astype('<f4').tobytes()
def keybox(b):return tuple(np.round(b,5))
def addtile(g,b):
    global cursor
    g=np.array(g,dtype='<f4').flatten();idx=len(defs);defs.append(dict(offset=cursor,length=len(g),boxes=b,light=max([0]+[min(15,round(9+v*5)) for v in g[10::13] if v>=.2])));chunks.append(g);cursor+=len(g);return idx
def door(id,closed,opened,old=None,offset=(0,0,0)):
    cv,cb=bake(*closed);ov,ob=bake(*opened);rv,rb=({}, {}) if old is None else bake(*old)
    positions=set(cv)|set(cb)|set(ov)|set(ob)|set(rv)|set(rb);rows=[]
    for key in sorted(positions):
        world=tuple(key[i]+offset[i] for i in range(3));base=cells.get(world,-1)
        if base>=0:
            df=defs[base];g=binary[df['offset']:df['offset']+df['length']].reshape(-1,39);boxes=df['boxes']
        else:g=np.empty((0,39));boxes=[]
        take=np.ones(len(g),bool)
        for tri in np.array(rv.get(key,[])).reshape(-1,39):
            delta=np.max(abs(g-tri),axis=1);delta[~take]=np.inf;i=int(delta.argmin())
            assert delta[i]<2e-5,(id,key,'unmatched authored triangle',delta[i])
            take[i]=False
        kept=g[take].flatten().tolist()
        remove=Counter(keybox(b) for b in rb.get(key,[]));keptboxes=[]
        for b in boxes:
            k=keybox(b)
            if remove[k]:remove[k]-=1
            else:keptboxes.append(b)
        assert not any(remove.values()),(id,key,'unmatched door collision')
        ci=addtile(kept+cv.get(key,[]),keptboxes+cb.get(key,[]));oi=addtile(kept+ov.get(key,[]),keptboxes+ob.get(key,[]))
        rows.append([*world,base,oi,ci])
    pts=np.array([r[:3] for r in rows]);doors.append(dict(id=id,cells=rows,bounds=np.r_[pts.min(0),pts.max(0)+1].tolist()))

raw=np.frombuffer(gzip.decompress((e.HERE/'approved/dorm.bin.gz').read_bytes()),dtype='<f4').reshape(-1,3,11).astype(float)
meta=read(e.HERE/'approved/dorm.json.gz')
# Rotate complete authored components, including every face of the brass arches.
owners=e.front_leaf_owners(raw,meta['solids']);mask=owners>=0;front=raw[mask];fb=[b for b in meta['solids'] if len(b)==6 and e.front_leaf(b[:3],b[3:])]
oldop=front.copy();op=e.lock_uv(front)
for i,t in enumerate(op):
    op[i]=e.open_leaf(t,left=owners[mask][i]==0)
    oldop[i]=e.open_leaf(oldop[i],left=owners[mask][i]==0)
ob=[]
for b in fb:
    ps=np.array([[x,y,z] for x in [b[0],b[3]] for y in [b[1],b[4]] for z in [b[2],b[5]]]);ps=e.open_leaf(ps);ob.append(np.r_[ps.min(0),ps.max(0)].tolist())
door('dorm_entrance',(front,fb),(op,ob),(oldop,ob),(-100,65,66))
# Existing roof metal leaf and handle, excluding its masonry jamb.
vols=[[-10.40,20.01,.85,-10.30,22.49,2.65],[-10.47,21.17,1.20,-10.39,21.42,1.32]]
mask=np.zeros(len(raw),bool)
for b in vols:mask|=np.all((raw[:,:,:3]>=np.array(b[:3])-1e-5)&(raw[:,:,:3]<=np.array(b[3:])+1e-5),axis=(1,2))
roof=(raw[mask],vols);closed=transform(*roof,[-10.35,20,2.65],-90)
door('roof_door',closed,roof,roof,(-100,65,66))
def leaf(x,y,z,width,height,metal=False):
    boxes=[[x,y+.03,z,x+width,y+height,z+.10],[x+width-.18,y+1.2,z+.10,x+width-.10,y+1.46,z+.16]]
    chunks=[e.cuboid(boxes[0][:3],boxes[0][3:],0 if metal else 11,(.28,.37,.35) if metal else (.24,.15,.115)),e.cuboid(boxes[1][:3],boxes[1][3:],3,(.63,.49,.27))]
    return np.concatenate(chunks),boxes
for lev in [1,2,3]:
    for x in [-6.75,6.4]:
        for z in [2,-2.25]:
            cl=leaf(x+.12,lev*5,z+.08,1.62,2.77);op=transform(*cl,[x+.12,lev*5,z+.08],90 if z>0 else -90)
            door(f'room_{lev}_{x}_{z}',cl,op,offset=(-100,65,66))
cl=leaf(1.32,0,-2.92,1.62,2.77);door('kitchen_door',cl,transform(*cl,[1.32,0,-2.92],-90),offset=(-100,65,66))
e.dump(DATA/'architecture.json.gz',defs)
(AS/'city/architecture.bin.gz').write_bytes(gzip.compress(np.concatenate(chunks).astype('>f4').tobytes(),mtime=0))
e.dump(DATA/'furniture.json.gz',dict(seats=seats,doors=doors))
e.dump(e.HERE/'furniture-report.json',dict(seats=len(seats),doors=len(doors),baseTiles=base_count,tiles=len(defs),unchangedComputers=True))
print('Furniture:',len(seats),'seats;',len(doors),'doors;',len(defs)-base_count,'local tile variants')
