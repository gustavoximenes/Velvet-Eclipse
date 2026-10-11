"""Approved exterior source. Export a preview; run export.py to bake native blocks."""
from pathlib import Path
import json,math,shutil,sys
import numpy as np
from scene import Scene
HERE=Path(__file__).resolve().parent
OUT=Path(sys.argv[1]).resolve() if len(sys.argv)>1 else HERE/'preview'
OUT.mkdir(parents=True,exist_ok=True)
S=Scene('tower');stone=(.23,.32,.27);dark=(.06,.115,.10);edge=(.44,.51,.43);gold=(.40,.38,.20);jade=(.23,.76,.34);ivory=(.68,.76,.48)
groups=[]
def box(x,y,z,w,h,d,c=stone,mat=2,emit=0):S.box(x,y,z,w,h,d,c,mat,emit)
def rotate(start,angle,center=(0,0,0)):
    a=math.radians(angle);c,s=math.cos(a),math.sin(a);r=np.array([[c,0,-s],[0,1,0],[s,0,c]])
    ar=np.array(S.verts[start:]);ar[:,:3]=ar[:,:3]@r.T+center;ar[:,3:6]=ar[:,3:6]@r.T;S.verts[start:]=ar.tolist()
    for solid in S.solids:
        if solid['start']>=start:
            solid['origin']=(np.array(solid['origin'])@r.T+center).tolist();solid['angle']+=a
def clock(x,y,z,r,a=0,large=False):
    start=len(S.verts)
    # Recessed face, layered physical rings, radial teeth and raised hands.
    S.disk((0,0,.05),r,dark,n=64)
    for rr,ww,col,depth,em in [(r+.45,.50,edge,.16,0),(r-.05,.28,gold,.25,0),(r-.52,.10,jade,.29,.55),(r*.72,.08,ivory,.20,.18)]:S.ring((0,0,depth),rr,ww,col,mat=3,emit=em,segments=64)
    for k in range(60):
        q=k*math.tau/60;ln=.47 if k%5==0 else .20
        S.beam((math.sin(q)*(r-.70),math.cos(q)*(r-.70),.33),(math.sin(q)*(r-.70-ln),math.cos(q)*(r-.70-ln),.33),.08 if k%5==0 else .034,ivory,3,4)
    S.beam((0,0,.45),(0,r*.61,.45),.13,gold,3,4);S.beam((0,0,.49),(-r*.43,-r*.2,.49),.16,gold,3,4);S.disk((0,0,.56),.28,jade,emit=.5,n=16)
    for k in range(24):
        q=k*math.tau/24
        box(math.sin(q)*(r+.45)-.14,math.cos(q)*(r+.45)-.14,-.02,.28,.28,.30,gold,3)
    rotate(start,a,(x,y,z));groups.append(dict(type='clock',center=[x,y,z],radius=r))
def volume(x,y,z,w,h,d,a=0,windows=True):
    start=len(S.verts)
    box(-w/2,0,-d/2,w,h,d,stone)
    for yy in [0,h-.9,h]:box(-w/2-.7,yy,-d/2-.7,w+1.4,.55,d+1.4,edge,3)
    # Uneven side faces carry buttresses and individually framed tall windows.
    for side in range(4):
        t=len(S.verts);span=w if side%2==0 else d;depth=d if side%2==0 else w
        for xx in np.arange(-span/2+1,span/2,.0+3.8):
            box(xx-.16,.6,depth/2,.32,h-.8,.4,edge,3)
        if windows:
            for yy in np.arange(2.1,h-2,5.2):
                for xx in np.arange(-span/2+2,span/2-1.5,3.8):
                    box(xx-.58,yy,depth/2+.015,1.16,2.8,.06,dark,0)
                    box(xx-.36,yy+.17,depth/2+.09,.72,2.30,.05,jade,0,.38)
                    box(xx-.055,yy+.16,depth/2+.16,.11,2.36,.1,edge,3)
                    box(xx-.42,yy+1.22,depth/2+.16,.84,.08,.1,edge,3)
                    box(xx-.68,yy+2.82,depth/2,.0+1.36,.15,.33,edge,3)
        rotate(t,side*90)
    rotate(start,a,(x,y,z));groups.append(dict(type='volume',position=[x,y,z],size=[w,h,d],rotation=a))

# Foundation and an unobstructed approach aligned to the existing city entrance.
box(-27,-1,-27,54,1,56,dark);box(-18,0,18,36,.18,11,edge,1)
box(-3,0,29,6,.12,13,(.13,.18,.19),2)
for k in range(12):
    for side in [-1,1]:box(side*6.8-.15,0,27+k,.3,2.2,.13,edge,3)
for x in [-18,-10,10,18]:
    box(x-1,0,21,2,15,2,stone)
    for y in [0,13.8]:box(x-1.5,y,20.5,3,.65,3,edge,3)
    for xx in [x-.65,x,x+.65]:box(xx-.08,.8,22.04,.16,12.7,.18,edge,3)
# Central entrance is a real open passage; the dark plane indicates the future portal.
box(-9,0,17,5.4,16,3,stone);box(3.6,0,17,5.4,16,3,stone);box(-9,9,17,18,7,3,stone)
for x in [-3.6,3.3]:box(x,0,20,.3,8.5,.4,edge,3)
box(-3.3,8.3,20,6.6,.3,.4,edge,3);box(-3.25,0,17.1,6.5,8.3,.08,(.012,.036,.025),0)
clock(0,16.5,22.9,5.5,large=True)
for x in [-22,-15,15,22]:
    box(x-.20,0,27,.40,5,.40,edge,3);S.beam((x,5,27),(x,5.6,27),.18,gold,3,4)
for side in [-1,1]:
    for x in np.arange(4.5,23,1.1):box(side*x-.055,0,27,.11,3.8,.11,dark,3)
    box(side*13.5-9.5,3.1,27,19,.13,.13,edge,3)

# A displaced cluster, not concentric copies of the same square storey.
specs=[(0,0,-2,37,25,33,0),(-3,24,-1,31,27,29,-7),(3,50,1,28,25,25,13),(-3,74,-2,24,29,24,-11),(3,102,1,24,26,22,17),(-1,127,-1,20,24,19,-4),(1,150,0,16,27,18,12),(-1,176,0,14,23,13,-15),(0,198,0,10,24,10,8)]
for i,spec in enumerate(specs):
    volume(*spec)
    x,y,z,w,h,d,a=spec
    for side in [-1,1]:
        bx=x+side*(w/2+1.4);by=y+4+(i%3)*2;bz=z+(-5 if side<0 else 3)
        volume(bx,by,bz,4.2,12+i%4*2,5.2,a+side*9,windows=i%2==0)
        for j in range(7):box(bx-2+j*.24,by+12+i%4*2+j*.65,bz-2.5+j*.24,4-j*.48,.68,5-j*.48,edge,3)
    if i%2==0:clock(x,y+12,z+d/2+1,6.2 if i<3 else 4.1,a)
    if i in [1,3,5,7]:clock(x-w/2-1,y+10,z,3.5,-90)

# Broad, rising external bands with treads and relief edges intertwine with the masses.
for offset,turns in [(0,2.35),(math.pi,1.72)]:
    for k in range(240):
        t=k/239;y=16+t*184;a=offset+t*math.tau*turns;r=24-t*15
        if y<30 and math.cos(a)*r>13:continue
        start=len(S.verts);box(-2.4,y, r-.7,4.8,.42,1.4,edge,3)
        box(-2.4,y+.42,r+.60,4.8,.32,.18,jade,0,.24)
        if k%5==0:box(-.10,y-1.5,r+.52,.20,3,.24,gold,3)
        rotate(start,math.degrees(a))
for y,side in [(43,1),(92,-1),(146,1)]:
    r=21-y*.06
    for k in range(14):box(side*r-.7-k*.12,y-k*.45,1+k*.36,1.4+k*.24,.50,1.4,edge,3)

# Finial and split spires, with the upper tip kept under the current world build ceiling.
for k in range(20):
    r=max(.30,3.3-k*.155);box(-r,220+k*.85,-r,2*r,.9,2*r,edge if k%4 else jade,3,.12 if k%4==0 else 0)
for side in [-1,1]:S.beam((side*4,211,0),(side*1.3,233,0),.42,edge,3,6)

views=[dict(id='torre',eye=[88,123,282],target=[0,116,0],fov=51),dict(id='entrada',eye=[34,15,62],target=[0,15,13],fov=57),dict(id='relogios',eye=[60,113,82],target=[0,98,2],fov=48)]
shots=[dict(id='orbita',frames=48,eye=[130,125,260],eyeEnd=[-95,125,280],target=[0,116,0],targetEnd=[0,116,0],fov=51),dict(id='entrada',frames=24,eye=[25,12,57],eyeEnd=[-17,12,58],target=[0,15,15],targetEnd=[0,15,15],fov=55)]
raw=np.array(S.verts,dtype='<f4');raw.tofile(OUT/'tower.bin');shutil.copy2(HERE/'atlas.png',OUT/'atlas.png')
meta=dict(vertexCount=len(raw),solids=[s['lo']+s['hi']+[s['angle']]+s['origin'] for s in S.solids],lights=[],key=[100,300,170],center=[0,116,0],views=views,shots=shots,groups=groups,status='Exterior aprovado; fonte da integração nativa',height=237,scale='1 unidade = 1 bloco; altura adaptada ao limite atual da cidade')
(OUT/'tower.json').write_text(json.dumps(meta,ensure_ascii=False,indent=2),encoding='utf8')
print('Tower study:',len(raw)//3,'triangles;',len(groups),'architectural groups; no live tower changes')
