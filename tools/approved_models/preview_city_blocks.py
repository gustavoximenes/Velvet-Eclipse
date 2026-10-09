"""Export actual plan blocks and Minecraft JSON models to a hidden-context visual audit.
Minecraft textures are read from the locally installed client, never redistributed.
Lighting in this audit is illustrative; it is not an in-game shader capture.
"""
from pathlib import Path
import gzip,json,math,io,zipfile,functools
import numpy as np
from PIL import Image
ROOT=Path(__file__).resolve().parents[2];RES=ROOT/'src/main/resources';OUT=ROOT/'build/reports/city/blocks';OUT.mkdir(parents=True,exist_ok=True)
CLIENT=zipfile.ZipFile(Path.home()/'AppData/Roaming/.minecraft/versions/1.20.1/1.20.1.jar')
PLAN=json.loads(gzip.decompress((RES/'data/velvet_eclipse/city/plan.json.gz').read_bytes()))
CELLS={tuple(c[:3]):c[3] for c in PLAN['cells']}
NAMES=['paving','brick','plaster','wood','glass','school_floor','leaves','roof','light']
SHOPS=['aohige','police','mayoido','blue','chagall','mandragora','arcade','escapade','kenneth']
TILES={};IMAGES=[]
DIRS={'up':(0,1,0),'down':(0,-1,0),'east':(1,0,0),'west':(-1,0,0),'south':(0,0,1),'north':(0,0,-1)}


def resource(name,kind,ext='json'):
    if ':' not in name:name='minecraft:'+name
    ns,path=name.split(':');file=f'assets/{ns}/{kind}/{path}.{ext}'
    return (RES/file).read_bytes() if ns!='minecraft' else CLIENT.read(file)


@functools.lru_cache(None)
def model(name):
    raw=json.loads(resource(name,'models'));parent=raw.get('parent','')
    inherited=model(parent) if parent and not parent.startswith('builtin') else {}
    return {**inherited,**raw,'textures':{**inherited.get('textures',{}),**raw.get('textures',{})}}


def texture(name):
    if name not in TILES:
        TILES[name]=len(IMAGES)
        im=Image.open(io.BytesIO(resource(name,'textures','png'))).convert('RGBA')
        if im.height>im.width:im=im.crop((0,0,im.width,im.width))
        IMAGES.append(im.resize((64,64),Image.Resampling.NEAREST))
    return TILES[name]


def rotation(axis,degrees):
    a=math.radians(degrees);c,s=math.cos(a),math.sin(a)
    return np.array([[1,0,0],[0,c,-s],[0,s,c]]) if axis=='x' else np.array([[c,0,s],[0,1,0],[-s,0,c]]) if axis=='y' else np.array([[c,-s,0],[s,c,0],[0,0,1]])


def matches(where,props):
    if 'OR' in where:return any(matches(x,props) for x in where['OR'])
    if 'AND' in where:return all(matches(x,props) for x in where['AND'])
    return all(props.get(k,'') in v.split('|') for k,v in where.items())


def descriptor(mat):
    if mat>=100:return PLAN['palette'][mat-100]
    name=NAMES[mat] if mat<9 else 'shop_'+SHOPS[mat-20] if mat<29 else 'tartarus_access' if mat==29 else 'plaster'
    return dict(block='velvet_eclipse:city_'+name,properties={})


@functools.lru_cache(None)
def blocks(mat):
    d=descriptor(mat);block=d['block'];props=d['properties']
    special=block.endswith(('_bed','water'))
    if special:
        tex='minecraft:block/blue_wool' if block.endswith('_bed') else 'minecraft:block/water_still'
        raw=dict(textures={'all':tex},elements=[{'from':[0,0,0],'to':[16,9 if block.endswith('_bed') else 14,16],
            'faces':{face:dict(texture='#all',uv=[0,0,16,16]) for face in DIRS}}]);variants=[({},raw)]
    else:
        bs=json.loads(resource(block,'blockstates'));variants=[]
        for key,value in bs.get('variants',{}).items():
            where=dict(p.split('=') for p in key.split(',') if p)
            if matches(where,props):
                v=value[0] if isinstance(value,list) else value;variants.append((v,model(v['model'])))
        for part in bs.get('multipart',[]):
            if matches(part.get('when',{}),props):
                v=part['apply'];v=v[0] if isinstance(v,list) else v;variants.append((v,model(v['model'])))
        assert variants,('No vanilla model matches native state',d)
    faces=[]
    for v,raw in variants:
        transform=rotation('y',-v.get('y',0))@rotation('x',-v.get('x',0))
        for e in raw.get('elements',[]):
            a=np.array(e['from'])/16;b=np.array(e['to'])/16;x,y,z=a;u,w,t=b
            points={'up':[(x,w,z),(x,w,t),(u,w,t),(u,w,z)],'down':[(x,y,t),(x,y,z),(u,y,z),(u,y,t)],
             'north':[(u,y,z),(x,y,z),(x,w,z),(u,w,z)],'south':[(x,y,t),(u,y,t),(u,w,t),(x,w,t)],
             'west':[(x,y,z),(x,y,t),(x,w,t),(x,w,z)],'east':[(u,y,t),(u,y,z),(u,w,z),(u,w,t)]}
            for direction,f in e['faces'].items():
                p=np.array(points[direction]);normal=np.array(DIRS[direction],dtype=float)
                if 'rotation' in e:
                    r=e['rotation'];rot=rotation(r['axis'],r['angle']);origin=np.array(r['origin'])/16;p=(p-origin)@rot.T+origin;normal=normal@rot.T
                p=(p-.5)@transform.T+.5;normal=normal@transform.T
                tex=f['texture']
                for _ in range(12):
                    if not tex.startswith('#'):break
                    tex=raw['textures'][tex[1:]]
                tile=texture(tex);uv=f.get('uv',[0,0,16,16]);uv=np.array([[uv[0],uv[3]],[uv[2],uv[3]],[uv[2],uv[1]],[uv[0],uv[1]]])/16
                uv=np.roll(uv,f.get('rotation',0)//90,axis=0)
                tint=np.array([.55,.78,.42]) if 'tintindex' in f else np.ones(3)
                emission=1 if block.endswith('froglight') or mat==8 else 0
                cull=np.rint(np.array(DIRS[f['cullface']])@transform.T).astype(int) if 'cullface' in f else None
                faces.append((p,normal,uv,tile,tint,emission,cull))
    return faces


def opaque(mat):
    if mat is None:return False
    name=descriptor(mat)['block']
    return not any(t in name for t in ['glass','stairs','slab','door','bed','water','wall','wheat','leaves','cauldron'])


def main():
    for mat in set(CELLS.values()):blocks(mat)
    floats=[]
    for (x,y,z),mat in CELLS.items():
        for points,normal,uv,tile,tint,emission,cull in blocks(mat):
            if cull is not None and opaque(CELLS.get((x+int(cull[0]),y+int(cull[1]),z+int(cull[2])))):continue
            p=points+[x,y,z]
            for k in [0,1,2,0,2,3]:floats.extend([*p[k],*normal,*uv[k],tile,*tint,emission])
    np.array(floats,dtype='<f4').tofile(OUT/'city.bin')
    atlas=Image.new('RGBA',(64*len(IMAGES),64));
    for i,im in enumerate(IMAGES):atlas.paste(im,(i*64,0))
    atlas.save(OUT/'atlas.png')
    lights=[[c[0]+.5,c[1]+.5,c[2]+.5] for c in PLAN['cells'] if descriptor(c[3])['block'].endswith('froglight')]
    views=[
      dict(id='dormitorio-exterior',eye=[-150,86,114],target=[-100,77,65],fov=65),
      dict(id='dormitorio-lounge',eye=[-109,66,73],target=[-121,66,57],fov=75),
      dict(id='dormitorio-quarto',eye=[-109,72,75],target=[-122,72,69],fov=75),
      dict(id='dormitorio-escada',eye=[-100,72,65],target=[-93,74,57],fov=75),
      dict(id='dormitorio-cobertura',eye=[-100,91,84],target=[-113,89,68],fov=75),
      dict(id='gekkoukan',eye=[-34,72,-29],target=[0,78,-100],fov=73),
      dict(id='gekkoukan-sala',eye=[-17,73,-100],target=[-27,73,-124],fov=75),
      dict(id='paulownia-dia',eye=[85,67,82],target=[94,72,49],fov=82),
      dict(id='paulownia-dark-hour',eye=[85,67,82],target=[94,72,49],fov=82,dark=True),
      dict(id='iwatodai',eye=[-107,68,-7],target=[-101,72,-42],fov=80),
      dict(id='station',eye=[86,66,-51],target=[112,68,-85],fov=80),
      dict(id='naganaki',eye=[-110,67,-89],target=[-101,74,-138],fov=73),
      dict(id='city',eye=[-180,220,220],target=[0,66,-15],fov=65)]
    (OUT/'scene.json').write_text(json.dumps(dict(vertices=len(floats)//13,tiles=len(IMAGES),lights=lights,views=views)),encoding='utf8')
    print('Exported native plan preview:',len(floats)//13,'vertices;',len(IMAGES),'textures; all native block state variants resolved')


if __name__=='__main__':main()
