"""Approved audits -> local, independently breakable Minecraft architecture blocks.

Every triangle and collision box is clipped to its owning 1x1x1 block.
The source geometry is retained, not rasterized; only rotated collision surfaces
use conservative 1/8-block strips. No invisible full-block collision shell.
"""
from pathlib import Path
from collections import defaultdict
import gzip, json, math, struct, hashlib
import numpy as np
from PIL import Image, ImageDraw

ROOT=Path(__file__).resolve().parents[2]
HERE=Path(__file__).resolve().parent
RES=ROOT/'src/main/resources'
AS=RES/'assets/velvet_eclipse'
DATA=RES/'data/velvet_eclipse/city'
def dump(path,obj):
    path.parent.mkdir(parents=True,exist_ok=True)
    raw=json.dumps(obj,separators=(',',':'),ensure_ascii=False).encode()
    path.write_bytes(gzip.compress(raw,mtime=0) if path.suffix=='.gz' else raw)

def clip(poly,axis,bound,positive):
    if not poly:return []
    out=[];prev=poly[-1];pd=(prev[axis]-bound)*(1 if positive else -1)
    for p in poly:
        d=(p[axis]-bound)*(1 if positive else -1)
        if (d>=-1e-7)!=(pd>=-1e-7):
            t=pd/(pd-d);out.append(prev+(p-prev)*t)
        if d>=-1e-7:out.append(p)
        prev=p;pd=d
    return out

def indices(lo,hi):
    a=math.floor(lo+1e-6);b=math.ceil(hi-1e-6)
    return range(a,max(a+1,b))

def front_leaf(lo,hi):
    # Whole authored components, including the brass arches projecting beyond
    # the wooden leaf. This predicate must never be applied to individual faces.
    return lo[0]>=-1.51 and hi[0]<=1.51 and lo[1]>=-.001 and hi[1]<=3.81 and lo[2]>=11.63 and hi[2]<=12.25

def front_leaf_owners(raw,solids):
    owners=np.full(len(raw),-1,dtype=np.int8)
    points=raw[:,:,:3]
    for row in solids:
        if len(row)!=6 or not front_leaf(row[:3],row[3:]):continue
        lo=np.array(row[:3]);hi=np.array(row[3:])
        mask=np.all((abs(points-lo)<2e-5)|(abs(points-hi)<2e-5),axis=(1,2))
        assert mask.sum()==12,('incomplete entrance component',row,int(mask.sum()))
        assert np.all(owners[mask]==-1),('ambiguous entrance ownership',row)
        owners[mask]=0 if (lo[0]+hi[0])*.5 < -1e-6 else 1
    return owners

def cuboid(lo,hi,mat=11,color=(.24,.15,.115)):
    corners=np.array([[x,y,z] for x in [lo[0],hi[0]] for y in [lo[1],hi[1]] for z in [lo[2],hi[2]]])
    faces=[([0,1,3,2],[-1,0,0]),([4,6,7,5],[1,0,0]),([0,4,5,1],[0,-1,0]),([2,3,7,6],[0,1,0]),([0,2,6,4],[0,0,-1]),([1,5,7,3],[0,0,1])]
    return np.array([[np.r_[corners[face[k]],normal,color,mat,0] for k in tri] for face,normal in faces for tri in [(0,1,2),(0,2,3)]])

def open_leaf(points,left=None):
    """Keep the authored double entrance doors open, with their real thickness."""
    q=points.copy()
    if left is None:left=q[:,0].mean() < -1e-6
    hinge=-1.35 if left else 1.35
    a=math.pi/2 if left else -math.pi/2
    x=q[:,0]-hinge;z=q[:,2]-11.64
    q[:,0]=hinge+math.cos(a)*x-math.sin(a)*z
    q[:,2]=11.64+math.sin(a)*x+math.cos(a)*z
    if q.shape[1]>3:
        x=q[:,3].copy();z=q[:,5].copy();q[:,3]=math.cos(a)*x-math.sin(a)*z;q[:,5]=math.sin(a)*x+math.cos(a)*z
    return q

def bake_scene(name,offset):
    meta=json.loads(gzip.decompress((HERE/'approved'/f'{name}.json.gz').read_bytes()))
    raw=np.frombuffer(gzip.decompress((HERE/'approved'/f'{name}.bin.gz').read_bytes()),dtype='<f4').reshape(-1,3,11).astype(float)
    if name=='dorm':
        def replace_box(row,replacements):
            nonlocal raw
            lo=np.array(row[:3]);hi=np.array(row[3:6]);v=raw[:,:,:3]
            match=np.all((abs(v-lo)<2e-5)|(abs(v-hi)<2e-5),axis=(1,2))
            indices=np.flatnonzero(match);chosen=None
            for start in indices:
                if start+12>len(raw) or not match[start:start+12].all():continue
                chunk=raw[start:start+12];normals,counts=np.unique(chunk[:,0,3:6].round(3),axis=0,return_counts=True)
                if len(normals)==6 and (counts==2).all() and np.max(abs(chunk[:,:,:3].min((0,1))-lo))<2e-5 and np.max(abs(chunk[:,:,:3].max((0,1))-hi))<2e-5:chosen=start;break
            assert chosen is not None,('authored cuboid',row,int(match.sum()))
            match[:]=False;match[chosen:chosen+12]=True
            color=raw[match][0,0,6:9];mat=round(raw[match][0,0,9]);raw=raw[~match]
            meta['solids'].remove(row)
            for lower,upper in replacements:raw=np.concatenate((raw,cuboid(lower,upper,mat,color)));meta['solids'].append(lower+upper)
        for row in list(meta['solids']):
            lo=row[:3];hi=row[3:6]
            # Upper flights need stepped undersides rather than solid prisms down to the storey.
            if abs(lo[0]+14.7)<1e-5 and abs(hi[0]+11.4)<1e-5 and lo[1]>=5 and abs(hi[2]-lo[2]-1)<1e-5 and hi[1]-lo[1]>.501:
                replace_box(row,[[[lo[0],hi[1]-.5,lo[2]],hi]])
            # Decorative cross beams must stop at the staircase opening.
            elif abs(lo[0]+15.25)<1e-5 and abs(hi[0]-15.25)<1e-5 and abs(hi[1]-lo[1]-.3)<1e-5 and lo[2]<2:
                replace_box(row,[[lo,[-14.95,hi[1],hi[2]]],[[-10,lo[1],lo[2]],hi]])
        # A continuous return landing lets players reach the foot of the next flight.
        for y in [5,10,15]:
            lo=[-11.1,y-.25,-9];hi=[-10,y,2]
            raw=np.concatenate((raw,cuboid(lo,hi)));meta['solids'].append(lo+hi)
    return bake_geometry(name,meta,raw,True)

def lock_uv(raw):
    """Retain the leaf's authored UV axes when geometry and normals rotate."""
    if raw.shape[-1]==13:return raw.copy()
    out=np.zeros((*raw.shape[:-1],13),dtype=float);out[:,:,:11]=raw
    for i,t in enumerate(raw):
        axis=int(np.argmax(abs(t[0,3:6])));u=2 if axis==0 else 0;v=2 if axis==1 else 1
        mat=round(t[0,9]);span=1 if mat==11 or mat>=31 else 4 if mat==14 else 2 if 10<=mat<=17 else 8
        out[i,:,11]=t[:,u]/span;out[i,:,12]=t[:,v]/span
    return out

def uv_patches(poly):
    """Split rotated faces at UV repeat boundaries before mapping to the block atlas."""
    points=np.array(poly);out=[]
    for u in indices(points[:,11].min(),points[:,11].max()):
        p=clip(clip(poly,11,u,True),11,u+1,False)
        if len(p)<3:continue
        for v in indices(points[:,12].min(),points[:,12].max()):
            q=clip(clip(p,12,v,True),12,v+1,False)
            if len(q)>=3:out.append((q,np.array([u,v])))
    return out

def bake_geometry(name,meta,raw,open_front=False,surface_only=False):
    verts=defaultdict(list);boxes=defaultdict(list);lights=defaultdict(int)
    owners=front_leaf_owners(raw,meta['solids']) if open_front and name=='dorm' else None
    def addbox(lo,hi):
        lo=np.array(lo);hi=np.array(hi)
        for x in indices(lo[0],hi[0]):
            for y in indices(lo[1],hi[1]):
                for z in indices(lo[2],hi[2]):
                    key=(x,y,z)
                    if surface_only and key not in verts:continue
                    a=np.maximum(lo,key)-key;b=np.minimum(hi,np.array(key)+1)-key
                    if np.min(b-a)>1e-5:boxes[key].append(np.round(np.r_[a,b],5).tolist())
    for i,tri in enumerate(raw):
        if owners is not None and owners[i]>=0:tri=open_leaf(tri,left=owners[i]==0)
        normal=int(np.argmax(np.abs(tri[0,3:6])));u=2 if normal==0 else 0;v=2 if normal==1 else 1
        mat=round(tri[0,9]);span=1 if mat==11 or mat>=31 else 4 if mat==14 else 2 if 10<=mat<=17 else 8
        transparent=mat==18 or (name=='dorm' and mat==0 and np.max(abs(tri[0,6:9]-[.24,.35,.36]))<.005)
        if transparent:tri[:,9]=18
        elif name=='dorm' and 10<=mat<=17:tri[:,9]=mat+10
        if np.dot(np.cross(tri[1,:3]-tri[0,:3],tri[2,:3]-tri[0,:3]),tri[0,3:6])<0:tri=tri[[0,2,1]]
        lo=tri[:,:3].min(0);hi=tri[:,:3].max(0)
        ranges=[list(indices(lo[a],hi[a])) for a in range(3)]
        # A positive face on an integer boundary belongs to the block behind it.
        for a in range(3):
            if hi[a]-lo[a]<1e-6 and abs(lo[a]-round(lo[a]))<1e-6 and tri[0,3+a]>0:ranges[a]=[round(lo[a])-1]
        for x in ranges[0]:
            px=clip(clip(list(tri),0,x,True),0,x+1,False)
            if len(px)<3:continue
            for y in ranges[1]:
                py=clip(clip(px,1,y,True),1,y+1,False)
                if len(py)<3:continue
                for z in ranges[2]:
                    poly=clip(clip(py,2,z,True),2,z+1,False)
                    if len(poly)<3:continue
                    key=(x,y,z);start=np.array(key)
                    for patch,uv_origin in uv_patches(poly) if raw.shape[-1]==13 else [(poly,None)]:
                        for k in range(1,len(patch)-1):
                            tr=np.array([patch[0],patch[k],patch[k+1]])
                            if np.linalg.norm(np.cross(tr[1,:3]-tr[0,:3],tr[2,:3]-tr[0,:3]))<1e-9:continue
                            uv=tr[:,11:13]-uv_origin if uv_origin is not None else np.column_stack((tr[:,u]/span-math.floor((start[u]+.001)/span),tr[:,v]/span-math.floor((start[v]+.001)/span)))
                            tr[:,:3]=np.clip(tr[:,:3]-start,0,1)
                            verts[key].extend(np.column_stack((tr[:,:11],uv)).astype('<f4').flatten().tolist())
                            if tr[0,10]>=.2:lights[key]=max(lights[key],min(15,round(9+tr[0,10]*5)))
        if i%25000==0:print(name,'triangles',i,'/',len(raw),flush=True)
    for row in meta['solids']:
        lo=np.array(row[:3]);hi=np.array(row[3:6]);a=0 if len(row)==6 else row[6]
        origin=np.zeros(3) if len(row)==6 else np.array(row[7:10])
        if open_front and name=='dorm' and front_leaf(lo,hi):
            corners=np.array([[x,y,z] for x in [lo[0],hi[0]] for y in [lo[1],hi[1]] for z in [lo[2],hi[2]]]);corners=open_leaf(corners);lo=corners.min(0);hi=corners.max(0)
        if abs(a)<1e-7:addbox(lo+origin,hi+origin);continue
        c,s=math.cos(a),math.sin(a)
        poly=[np.array([origin[0]+c*x-s*z,origin[2]+s*x+c*z]) for x,z in [(lo[0],lo[2]),(hi[0],lo[2]),(hi[0],hi[2]),(lo[0],hi[2])]]
        xmin=min(p[0] for p in poly);xmax=max(p[0] for p in poly)
        for ix in range(math.floor(xmin*8),math.ceil(xmax*8)):
            sl=clip(clip(poly,0,ix/8,True),0,(ix+1)/8,False)
            if len(sl)<3:continue
            addbox([max(xmin,ix/8),lo[1]+origin[1],min(p[1] for p in sl)],[min(xmax,(ix+1)/8),hi[1]+origin[1],max(p[1] for p in sl)])
    if name=='mall':
        for ix in range(-29,29):
            for iz in range(-29,29):
                if math.hypot((ix+.5)/8,(iz+.5)/8)<3.6:addbox([ix/8,0,iz/8],[(ix+1)/8,1.27,(iz+1)/8])
    return meta,verts,boxes,lights

def main():
    oldpath=DATA/'legacy-plan-v2.json.gz'
    if not oldpath.exists():oldpath.write_bytes((DATA/'plan.json.gz').read_bytes())
    plan=json.loads(gzip.decompress(oldpath.read_bytes()))
    # Only the two approved districts change. All other district cells are byte-for-byte retained.
    def changed(c):
        x,y,z=c[:3]
        return (-130<=x<=-70 and 44<=z<=94 and y>=64) or (53<=x<=133 and 26<=z<=105 and y>=64)
    cells={tuple(c[:3]):c for c in plan['cells'] if not changed(c)}
    # Existing ground surface is replaced where an exact, fractional floor occupies the cell.
    # Creative placement defaults to a complete brick block, never an invisible empty tile.
    sample=cuboid([0,0,0],[1,1,1],20,(1,1,1)).reshape(-1,11)
    uv=[]
    for v in sample:
        normal=int(np.argmax(abs(v[3:6])));uv.append([v[2] if normal==0 else v[0],v[2] if normal==1 else v[1]])
    sample=np.column_stack((sample,np.array(uv))).astype('<f4').flatten()
    definitions=[{'offset':0,'length':len(sample),'boxes':[[0,0,0,1,1,1]],'light':0}];binary=[sample];dedup={};scene_data={};offsets={'dorm':(-100,65,66),'mall':(93,64,65)}
    for name,offset in offsets.items():
        meta,verts,boxes,lights=bake_scene(name,offset);scene_data[name]=meta
        for key in sorted(set(verts)|set(boxes)):
            geometry=np.asarray(verts.get(key,[]),dtype='<f4');collision=boxes.get(key,[])
            # A containing box subsumes all the small decorative boxes in that cell.
            if any(max(abs(np.array(b)-[0,0,0,1,1,1]))<1e-5 for b in collision):collision=[[0,0,0,1,1,1]]
            else:collision=[list(t) for t in sorted(set(tuple(b) for b in collision))]
            light=lights.get(key,0);digest=hashlib.sha256(geometry.tobytes()+json.dumps([collision,light]).encode()).hexdigest()
            if digest not in dedup:
                dedup[digest]=len(definitions)
                definitions.append({'offset':sum(len(b) for b in binary),'length':len(geometry),'boxes':collision,'light':light})
                binary.append(geometry)
            ident=dedup[digest];world=tuple(key[i]+offset[i] for i in range(3))
            cells[world]=[*world,40,ident]
        print(name,'blocks',len(set(verts)|set(boxes)),'unique',len(definitions),flush=True)
        atlas=Image.open(HERE/'approved'/f'{name}-atlas.png')
        print(name,'atlas',atlas.size,flush=True)
    # Actual terminals are architecture tiles, not cubes superimposed on the approved counters.
    terminals=[]
    for shop in scene_data['mall']['shops']:
        if shop['id']=='mandragora':x,y,z=2.4,4,-30.1
        else:
            a=math.radians(shop['angle']);x=math.sin(a)*23.6;y=0;z=-math.cos(a)*23.6
        target=(math.floor(x+93),math.floor(y+64),math.floor(z+65))
        nearby=[p for p in cells if cells[p][3]==40 and sum((p[i]-target[i])**2 for i in range(3))<=6]
        assert nearby,shop['id'];target=min(nearby,key=lambda p:sum((p[i]-target[i])**2 for i in range(3)))
        terminals.append({'x':target[0],'y':target[1],'z':target[2],'target':'shop:'+shop['id']})
    terminals.append({'x':94,'y':64,'z':35,'target':'velvet'})
    plan['doors']=[d for d in plan['doors'] if d['target']=='tartarus']+terminals
    places={p['id']:p for p in plan['places']}
    move={'dorm':(-100,64,82),'lounge':(-105,65,72),'kitchen':(-91,65,58),'reading':(-92,65,69),'bedroom':(-104,70,71),'bathroom':(-91,70,62),'command':(-106,80,62),'roof':(-108,85,70),'paulownia':(93,64,80),'velvet_exit':(94,64,36)}
    for shop in scene_data['mall']['shops']:
        pos=shop['counter'];move['shop_'+shop['id']]=(math.floor(pos[0]+93),round(pos[1]+64),math.floor(pos[2]+65))
    for ident,xyz in move.items():
        if ident in places:places[ident].update(zip(('x','y','z'),xyz))
        elif ident=='velvet_exit':places[ident]={'id':ident,'name':'Velvet Room · saída','x':xyz[0],'y':xyz[1],'z':xyz[2]}
    plan.update(revision=3,cells=sorted(cells.values(),key=lambda c:(c[1],c[0],c[2])),places=list(places.values()),meshes=[])
    plan['buildings']=[b for b in plan['buildings'] if b['id']!='dorm']+[{'id':'dorm','minX':-119,'maxX':-81,'minZ':51,'maxZ':84,'floors':[65,70,75,80,85]},{'id':'mall','minX':62,'maxX':124,'minZ':31,'maxZ':97,'floors':[64,68]}]
    plan['routes']=[r for r in plan.get('routes',[]) if not any(k in r['name'].lower() for k in ['dorm','mall','paulownia'])]
    dump(DATA/'plan.json.gz',plan);dump(DATA/'architecture.json.gz',definitions)
    out=np.concatenate(binary).astype('>f4');(AS/'city').mkdir(exist_ok=True);(AS/'city/architecture.bin.gz').write_bytes(gzip.compress(out.tobytes(),mtime=0))
    maps=json.loads((AS/'city-map.json').read_text());maps=[m for m in maps if not m['id'].startswith(('dorm','mall'))]
    for b in plan['buildings']:
        if b['id'] not in ('dorm','mall'):continue
        for i,y in enumerate(b['floors']):
            layer={'id':b['id']+'_'+str(i),'minX':b['minX']-2,'maxX':b['maxX']+2,'minZ':b['minZ']-2,'maxZ':b['maxZ']+2,'floor':y};maps.append(layer)
    for layer in maps:
        image=Image.new('RGB',(512,512),'#08192e');draw=ImageDraw.Draw(image);x0,z0=layer['minX'],layer['minZ'];sx=512/(layer['maxX']-x0+1);sz=512/(layer['maxZ']-z0+1)
        for c in plan['cells']:
            x,y,z,mat=c[:4]
            if not x0<=x<=layer['maxX'] or not z0<=z<=layer['maxZ']:continue
            floor=layer['floor'];color=None
            if floor<0:
                if 63<=y<=65:color='#1c4260' if y==63 else '#56858c'
            elif y==floor-1:color='#284e68'
            elif floor<=y<floor+2:
                solid=mat!=40 or bool(definitions[c[4]]['boxes'])
                if solid:color='#85b7bb'
            if color:draw.rectangle((int((x-x0)*sx),int((z-z0)*sz),int((x+1-x0)*sx),int((z+1-z0)*sz)),fill=color)
        image.save(AS/'textures/gui/city'/f"{layer['id']}.png")
    dump(AS/'city-map.json',maps)
    # Each district uses its own approved eight-tile atlas.
    for name in offsets:
        atlas=Image.open(HERE/'approved'/f'{name}-atlas.png').convert('RGBA')
        size=atlas.height
        for i in range(8):atlas.crop((i*size,0,(i+1)*size,size)).save(AS/'textures/block'/f'city_{name}_{i}.png')
    dump(HERE/'export-report.json',{'revision':3,'tiles':len(definitions),'blocks':sum(c[3]==40 for c in plan['cells']),'floats':len(out),'districts':list(offsets),'triangles':len(out)//39,'collision':'local boxes; rotated surfaces conservatively sliced at 1/8 block','inGame':False})
    print('exported',len(definitions),'tiles',len(out)//39,'triangles',flush=True)
if __name__=='__main__':main()
