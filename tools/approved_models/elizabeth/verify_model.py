"""Authoring validation only. Checks the untouched mod against its baseline."""
import base64, collections, hashlib, itertools, json, re
from pathlib import Path
import numpy as np
from PIL import Image
from build_model import SIZE, DENSITY, face_uv
import render_model as r

ROOT=Path(__file__).resolve().parent
OUT=ROOT/'draft'
BASE=ROOT.parents[1]
checks=0
def check(value, description):
    global checks
    checks+=1
    assert value,description

def worlds(time):
    mats={}
    for b in r.data['bones']:
        p=np.array(b['pivot'])
        mats[b['name']]=mats.get(b.get('parent'),np.eye(4))@r.T(r.sample(b['name'],'position',time))@r.T(p)@r.R(np.array(b.get('rotation',[0,0,0]))+r.sample(b['name'],'rotation',time))@r.T(-p)
    return mats

def oriented_box(part,pose):
    c=part['cube'];pivot=np.array(c.get('pivot',[0,0,0]))
    transform=pose[part['bone']]@r.T(pivot)@r.R(c.get('rotation',[0,0,0]))@r.T(-pivot)
    center=(transform@np.r_[np.array(c['origin'])+np.array(c['size'])/2,1])[:3]
    return center,transform[:3,:3],np.array(c['size'])/2

def overlap_depth(first,second,pose):
    # Separating-axis theorem, including edge cross products: this catches gaps
    # between tilted solids that an axis-aligned bounding box would miss.
    ac,axes_a,ah=oriented_box(first,pose);bc,axes_b,bh=oriented_box(second,pose)
    axes=[*axes_a.T,*axes_b.T]+[np.cross(a,b) for a in axes_a.T for b in axes_b.T]
    overlaps=[]
    for axis in axes:
        length=np.linalg.norm(axis)
        if length<1e-8:continue
        axis=axis/length
        overlaps.append(float(np.abs(axes_a.T@axis)@ah+np.abs(axes_b.T@axis)@bh-abs((bc-ac)@axis)))
    return min(overlaps)

def verify():
    bones=r.data['bones']; parts=json.loads((OUT/'parts.json').read_text(encoding='utf-8'))
    bb=json.loads((OUT/'elizabeth.bbmodel').read_text(encoding='utf-8'))
    seen=set(); rects=[]; count=0
    check(bones[0]['name']=='elizabeth' and bones[0]['pivot']==[0,0,0],'root at origin')
    for b in bones:
        check(bool(re.fullmatch('[a-z][a-z0-9_]*',b['name'])),'lowercase bone')
        check(b['name'] not in seen and (not b.get('parent') or b['parent'] in seen),'valid acyclic hierarchy')
        seen.add(b['name'])
        for c in b.get('cubes',[]):
            count+=1
            check(all(isinstance(x,int) and x>0 for x in c['size']),'all three dimensions are positive integers')
            check(isinstance(c['uv'],list) and len(c['uv'])==2,'Box UV only')
            w,h,d=c['size'];u,v=c['uv'];box=(u,v,u+2*(w+d),v+h+d)
            check(min(box)>=0 and max(box)<=SIZE,'UV inside atlas')
            check(all(box[2]<=a or box[0]>=x or box[3]<=b2 or box[1]>=y for a,b2,x,y in rects),'no UV rectangle overlap')
            rects.append(box)
            check(np.isfinite(c['origin']+c.get('rotation',[0,0,0])+c.get('pivot',[0,0,0])).all(),'finite transforms')
    check('head' in seen,'head named for future look controller')
    check(count==len(parts)==len(bb['elements']),'export element counts')
    check(len({p['name'] for p in parts})==count and all(re.fullmatch('[a-z][a-z0-9_-]*',p['name']) for p in parts),'unique lowercase part names')
    check(bb['meta']['model_format']=='bedrock' and bb['meta']['box_uv'],'native Bedrock editability')
    check(base64.b64decode(bb['textures'][0]['source'].split(',')[1])==(OUT/'elizabeth.png').read_bytes(),'embedded texture matches external texture')
    check(Image.open(OUT/'elizabeth.png').size==(SIZE,SIZE),'single-density atlas size')
    groups=[];references=[];pending=list(bb['outliner'])
    while pending:
        group=pending.pop();groups.append(group['uuid'])
        references.extend(c for c in group['children'] if isinstance(c,str))
        pending.extend(c for c in group['children'] if isinstance(c,dict))
    check(sorted(references)==sorted(e['uuid'] for e in bb['elements']),'all elements parented once')
    check(len(set(groups))==len(bones),'all bones exported once')
    for clip in bb['animations']:check(set(clip['animators'])<=set(groups),'Blockbench tracks use existing bones')
    feature_counts=dict(collections.Counter(p['feature'] or 'clothing' for p in parts))
    for feature in ('cranium','forehead','midface','cheek','jaw','chin','orbit','eye','eyelid','brow','nose','mouth','ear'):
        matching=[p for p in parts if p['feature']==feature]
        check(bool(matching),'modeled anatomy: '+feature)
        for p in matching:
            check(min(p['cube']['size'])>0,'solid face feature: '+p['name'])
            for face,(u,v,w,h) in face_uv(*p['cube']['uv'],*p['cube']['size']).items():
                check(np.all(r.texture[v:v+h,u:u+w,3]==1),'opaque face geometry, no face photograph/decal')
    check(all(len([p for p in parts if p['name'].startswith(f'clasp_{i}_facet_')])==8 for i in range(5)),'five geometric dress rings')
    for animation in r.animations.values():
        check(animation['loop'] and animation['animation_length']==4.8,'idle loop duration')
        for name,tracks in animation['bones'].items():
            check(name in seen,'known animated bone')
            for channel,keys in tracks.items():
                check(channel in ('position','rotation'),'no scale animation')
                ordered=sorted((float(t),v) for t,v in keys.items())
                check(ordered[0][0]==0 and ordered[-1][0]==4.8,'closed timeline')
                check(np.allclose(ordered[0][1],ordered[-1][1]),'continuous loop endpoints')
                check(np.isfinite(np.array([v for _,v in ordered])).all(),'finite keyframes')
    rest=worlds(0);foot_error=0;book_error=0
    for t in np.linspace(0,4.8,49):
        pose=worlds(t)
        for side in ('left','right'):
            foot_error=max(foot_error,float(np.max(np.abs(pose[side+'_foot']-rest[side+'_foot']))))
        book_error=max(book_error,float(np.max(np.abs(np.linalg.inv(pose['left_hand'])@pose['book']-np.linalg.inv(rest['left_hand'])@rest['book']))))
    check(foot_error<1e-9,'feet fixed throughout idle')
    check(book_error<1e-9,'book attached to hand throughout idle')
    named={p['name']:p for p in parts};minimum_attachment=float('inf');minimum_neck_attachment=float('inf')
    for t in np.linspace(0,4.8,49):
        pose=worlds(t)
        for first,second in [('neck_anatomy','neck_upper_joint'),('neck_upper_joint','chin_plane')]:
            depth=overlap_depth(named[first],named[second],pose)
            minimum_neck_attachment=min(minimum_neck_attachment,depth)
            check(depth>.01,f'head remains connected to neck at {t}: {first} / {second}')
        for side in ('left','right'):
            chain=['upper_chest',side+'_shoulder_bridge',side+'_deltoid',side+'_bare_upper_arm',
                   side+'_glove_cuff',side+'_long_glove',side+'_wrist_cuff',side+'_palm']
            for first,second in zip(chain,chain[1:]):
                depth=overlap_depth(named[first],named[second],pose)
                minimum_attachment=min(minimum_attachment,depth)
                check(depth>.01,f'connected arm solids at {t}: {first} / {second}')
    for first,second in [('midface','nose_bridge'),('nose_bridge','nose_tip')]:
        check(overlap_depth(named[first],named[second],rest)>.01,'continuous nose volume: '+first+' / '+second)
    def local_corners(part):
        c=part['cube'];pivot=np.array(c.get('pivot',[0,0,0]))
        transform=r.T(pivot)@r.R(c.get('rotation',[0,0,0]))@r.T(-pivot)
        return np.array([(transform@np.r_[np.array(c['origin'])+np.array(c['size'])*v,1])[:3] for v in itertools.product([0,1],repeat=3)])
    nose_projection=float(local_corners(named['midface'])[:,2].min()-min(local_corners(p)[:,2].min() for p in parts if p['feature']=='nose'))
    check(0<nose_projection<.5*DENSITY,'nose projects less than half a logical unit')
    previous=ROOT.parent/'elizabeth-rework-03'/'draft'
    preserved=0
    if previous.exists():
        older=json.loads((previous/'parts.json').read_text(encoding='utf-8'))
        changed_arm_suffixes=('_bare_upper_arm','_deltoid','_long_glove','_glove_cuff','_cuff_piping','_wrist_cuff')
        anatomy={'cranium','forehead','midface','cheek','jaw','chin','temple','orbit','eye','eyelid','brow','nose','mouth'}
        for part in older:
            if part['feature'] in anatomy or part['name'].endswith(changed_arm_suffixes):continue
            check(part['name'] in named,'retained part: '+part['name'])
            current=named[part['name']]
            # UV origins move when the atlas is repacked; shape and material do not.
            def geometry(p):return {k:v for k,v in p['cube'].items() if k!='uv'}
            check(geometry(part)==geometry(current) and part['bone']==current['bone'] and part['material']==current['material'],
                  'preserved outfit/hair/book/hand geometry: '+part['name'])
            preserved+=1
    before=json.loads((ROOT/'source-before.json').read_text(encoding='utf-8')) if (ROOT/'source-before.json').exists() and (BASE/'velvet-eclipse/src').is_dir() else {}
    for name,digest in before.items():
        check(hashlib.sha256((BASE/'velvet-eclipse'/name).read_bytes()).hexdigest()==digest,'mod was not changed: '+name)
    report={'status':'passed','checks':checks,'cuboids':count,'bones':len(bones),'texture':[SIZE,SIZE],
        'box_uv':True,'integer_dimensions':True,'uv_overlap':False,'root_origin':[0,0,0], 'facing':'north (-z)',
        'authoring_density':DENSITY,'feature_counts':feature_counts,'facial_features_are_opaque_solids':True,
        'idle_foot_transform_error':foot_error,'idle_book_attachment_error':book_error,
        'arm_attachment_minimum_overlap_native_units':minimum_attachment,'arm_attachment_samples':49,
        'head_neck_minimum_overlap_native_units':minimum_neck_attachment,
        'nose_projection_logical_units':nose_projection/DENSITY,'unchanged_parts_from_revision_03':preserved,
        'mod_files_unchanged':len(before),'mod_baseline_available':bool(before),'mod_integration':False,'game_validation':False}
    (OUT/'validation.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8')
    print(json.dumps(report,indent=2))

if __name__=='__main__':verify()
