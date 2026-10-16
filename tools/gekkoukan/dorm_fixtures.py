"""Repair complete furniture assemblies in the retained, approved dormitory layout."""
import numpy as np

def repair(meta,raw,cuboid):
    report=[]
    def move(row,offset=(0,0,0),angle=0,pivot=(0,0,0)):
        nonlocal raw
        lo=np.array(row[:3]);hi=np.array(row[3:]);p=raw[:,:,:3]
        mask=np.all((abs(p-lo)<2e-5)|(abs(p-hi)<2e-5),axis=(1,2))
        assert mask.sum()==12,('fixture component',row,int(mask.sum()))
        a=np.radians(angle);c,s=np.cos(a),np.sin(a);m=np.array([[c,0,-s],[0,1,0],[s,0,c]])
        raw[mask,:,:3]=(raw[mask,:,:3]-pivot)@m.T+np.array(pivot)+offset
        raw[mask,:,3:6]=raw[mask,:,3:6]@m.T
        points=raw[mask,:,:3];new=[*points.min((0,1)),*points.max((0,1))]
        meta['solids'][meta['solids'].index(row)]=new
    def select(lo,hi):
        return [r for r in meta['solids'] if len(r)==6 and np.all(np.array(r[:3])>=np.array(lo)-1e-5) and np.all(np.array(r[3:])<=np.array(hi)+1e-5)]
    def group(name,lo,hi,offset=(0,0,0),angle=0,pivot=(0,0,0),count=None):
        rows=select(lo,hi)
        assert rows and (count is None or len(rows)==count),(name,len(rows),count)
        for r in rows:move(r,offset,angle,pivot)
        report.append(dict(id=name,components=len(rows)))
    def add(lo,hi,mat=11,col=(.24,.15,.115)):
        nonlocal raw
        raw=np.concatenate([raw,cuboid(lo,hi,mat,col)]);meta['solids'].append(lo+hi)
    for lev in [1,2,3]:
        y=lev*5
        for x in ([-8.7,4] if lev<3 else [-8,4]):
            name=f'bedroom_{lev}_{x}'
            # Move cabinet AND brass handles to the side wall, facing the room.
            pivot=[x+1.4,y,3.895];target=[-9.40 if x<0 else 3.75,y,6.25]
            group(name+'_wardrobe',[x+.64,y,3.56],[x+2.16,y+2.51,4.46],np.array(target)-pivot,-90,pivot,3)
            # The mirror, basin, tap and refrigerator share the front wall datum.
            group(name+'_vanity',[x+5.59,y,3.46],[x+8.41,y+2.96,4.61],[0,0,-1.20],count=5)
            group(name+'_sconce',[x+4.01,y+3.14,3.37],[x+4.39,y+3.86,3.74],[0,0,-1.12],count=2)
            # Curtains and desk follow an actual facade window, not the room's old arbitrary offset.
            window=0 if x<0 else 12
            for i,dx in enumerate([0,2.8]):
                old=x+4.95+dx;target=window+(-1.75 if i==0 else 1.15)
                group(name+f'_curtain_{i}',[old,y+1,10.35],[old+.6,y+3.8,10.5],[target-old,0,.72],count=1)
            add([window-1.85,y+3.81,11.02],[window+1.85,y+3.87,11.24],3,(.63,.49,.27))
            # Keep desk, books, lamp and functional chair together near the window.
            group(name+'_desk',[x+4.99,y,7.89],[x+8.01,y+2.24,10.01],[window-x-6.5,0,.88],count=19)
            # Bed frame is above the carpet: four visible feet connect it to the floor.
            for dx in [.75,2.85]:
                for z in [5.45,9.45]:add([x+dx,y+.075,z],[x+dx+.18,y+.17,z+.18])
        if lev<3:
            for x in [-8,5]:group(f'reading_{lev}_{x}',[x,y,-10.8],[x+5,y+2.8,-10.1],[0,0,-.52],count=1)
    # These sconces were authored facing Z, despite being beside an X-facing wall.
    for lev in range(4):
        for z in [-9,-4,5]:
            y=lev*5+3.2;pivot=[-10.4,y,z]
            # Ground-floor lounge has no divider at z=5; attach that fixture to the west wall.
            target=[-15.24 if lev==0 and z==5 else -10.01,y,z]
            angle=-90 if lev==0 and z==5 else 90
            group(f'stair_sconce_{lev}_{z}',[-10.59,y-.36,z-.001],[-10.21,y+.36,z+.36],np.array(target)-pivot,angle,pivot,2)
    for x,z,w,d in [(5,8,5,1.55),(12,3.3,1.55,4.7)]:
        for dx in [.15,w-.33]:
            for dz in [.15,d-.33]:add([x+dx,.07,z+dz],[x+dx+.18,.22,z+dz+.18])
    add([8.4,.07,-2.46],[13.8,.46,-1.91])
    # All wooden chair backs start above their seat; bridge that gap with rear posts.
    for r in list(meta['solids']):
        if len(r)==6 and np.max(abs(np.array(r[3:])-r[:3]-[1,1,.18]))<1e-5:
            x,y,z=r[:3]
            for dx in [.06,.84]:add([x+dx,y-.25,z],[x+dx+.10,y+.15,z+.10])
    return raw,report
