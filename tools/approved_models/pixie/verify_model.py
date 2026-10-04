"""Structural, joint, motion and source-preservation checks for the standalone model."""
import base64,hashlib,itertools,json,re
from pathlib import Path
import numpy as np
from PIL import Image
import render_model as r
from build_model import SIZE,DENSITY,face_uv
from animate_model import IMPACTS

ROOT=Path(__file__).resolve().parent;OUT=ROOT/'draft';BASE=ROOT.parents[1]
count=0
def check(condition,label):
    global count
    count+=1
    assert condition,label

def worlds(t,clip):
    out={}
    for b in r.data['bones']:
        pivot=np.array(b['pivot'])
        out[b['name']]=out.get(b.get('parent'),np.eye(4))@r.T(r.sample(b['name'],'position',t,clip))@r.T(pivot)@r.R(np.array(b.get('rotation',[0,0,0]))+r.sample(b['name'],'rotation',t,clip))@r.T(-pivot)
    return out

def box(part,pose):
    c=part['cube'];pivot=np.array(c.get('pivot',[0,0,0]))
    transform=pose[part['bone']]@r.T(pivot)@r.R(c.get('rotation',[0,0,0]))@r.T(-pivot)
    return (transform@np.r_[np.array(c['origin'])+np.array(c['size'])/2,1])[:3],transform[:3,:3],np.array(c['size'])/2

def overlap(a,b,pose):
    ac,ar,ah=box(a,pose);bc,br,bh=box(b,pose)
    directions=[*ar.T,*br.T]+[np.cross(x,y) for x in ar.T for y in br.T]
    depths=[]
    for v in directions:
        norm=np.linalg.norm(v)
        if norm<1e-8:continue
        v=v/norm
        depths.append(float(np.abs(ar.T@v)@ah+np.abs(br.T@v)@bh-abs(v@(bc-ac))))
    return min(depths)

def verify():
    parts=json.loads((OUT/'parts.json').read_text());named={p['name']:p for p in parts}
    bb=json.loads((OUT/'pixie.bbmodel').read_text(encoding='utf-8'))
    seen=set();uvs=[]
    check(r.data['bones'][0]['name']=='pixie' and r.data['bones'][0]['pivot']==[0,0,0],'root pixie at origin')
    for b in r.data['bones']:
        check(bool(re.fullmatch('[a-z][a-z0-9_]*',b['name'])),'lowercase bone')
        check(b['name'] not in seen and (not b.get('parent') or b['parent'] in seen),'ordered acyclic hierarchy')
        seen.add(b['name'])
        for c in b.get('cubes',[]):
            check(all(isinstance(v,int) and v>0 for v in c['size']),'positive integer dimensions')
            check(isinstance(c['uv'],list) and len(c['uv'])==2,'Box UV only')
            u,v=c['uv'];w,h,d=c['size'];rect=(u,v,u+2*(w+d),v+h+d)
            check(min(rect)>=0 and max(rect)<=SIZE,'atlas bounds')
            check(all(rect[2]<=a or rect[0]>=x or rect[3]<=b or rect[1]>=y for a,b,x,y in uvs),'non-overlapping UVs')
            uvs.append(rect)
            check(np.isfinite(c['origin']+c.get('rotation',[])+c.get('pivot',[])).all(),'finite geometry')
    check('head' in seen,'head bone')
    check(len(parts)==len(named)==len(bb['elements'])==len(uvs),'native and editable cube counts')
    check(all(re.fullmatch('[a-z][a-z0-9_]*',p['name']) for p in parts),'lowercase part names')
    check(bb['meta']['model_format']=='bedrock' and bb['meta']['box_uv'],'Bedrock editability')
    check(base64.b64decode(bb['textures'][0]['source'].split(',')[1])==(OUT/'pixie.png').read_bytes(),'embedded atlas matches')
    previous=ROOT.parent/'pixie'/'draft'
    preserved=0
    if previous.exists():
        old_parts={p['name']:p for p in json.loads((previous/'parts.json').read_text())}
        old_texture=np.array(Image.open(previous/'pixie.png').convert('RGBA'))/255
        old_animation=json.loads((previous/'pixie.animation.json').read_text())['animations']
        check(old_animation==r.animations,'all three animation clips preserved exactly')
        old_bones={b['name']:b for b in json.loads((previous/'pixie.geo.json').read_text())['minecraft:geometry'][0]['bones']}
        for b in r.data['bones']:
            if b['name'] not in r.head_bones:
                check({k:v for k,v in b.items() if k!='cubes'}=={k:v for k,v in old_bones[b['name']].items() if k!='cubes'},'body and wing rig preserved: '+b['name'])
        for p in parts:
            if p['bone'] in r.head_bones:continue
            old=old_parts[p['name']];c=p['cube'];oc=old['cube']
            check({k:v for k,v in c.items() if k!='uv'}=={k:v for k,v in oc.items() if k!='uv'},'body and wing geometry preserved: '+p['name'])
            for face,(u,v,w,h) in face_uv(*c['uv'],*c['size']).items():
                ou,ov,ow,oh=face_uv(*oc['uv'],*oc['size'])[face]
                check(np.array_equal(r.texture[v:v+h,u:u+w],old_texture[ov:ov+oh,ou:ou+ow]),'body and wing texels preserved: '+p['name']+'/'+face)
            preserved+=1
    for feature in ('cranium','forehead','midface','cheek','jaw','chin','orbit','eye','eyelid','brow','nose','mouth','ear'):
        matching=[p for p in parts if p['feature']==feature]
        check(bool(matching),'modeled facial anatomy: '+feature)
        for p in matching:
            for u,v,w,h in face_uv(*p['cube']['uv'],*p['cube']['size']).values():
                check(np.all(r.texture[v:v+h,u:u+w,3]==1),'solid opaque facial anatomy: '+p['name'])
    for p in parts:
        if p['material']!='wing':continue
        check(p['cube']['size'][2]==1,'native one-texel wing thickness')
        for face in ('north','south'):
            u,v,w,h=face_uv(*p['cube']['uv'],*p['cube']['size'])[face]
            alpha=r.texture[v:v+h,u:u+w,3]
            check(np.any(alpha==0) and np.any(alpha==1),'double-sided wing cutout')
    # The structural head volumes must stay inside the authored cheek and jaw
    # planes, including rotated corners. Decorative facial parts are separate.
    fitted=json.loads((ROOT/'skin_boxes.json').read_text())
    A=np.array([q['normal'] for q in fitted['planes']]);limits=np.array([q['offset'] for q in fitted['planes']])
    maximum_protrusion=-float('inf')
    for cube in fitted['boxes']:
        points=np.array(list(itertools.product((-1,1),repeat=3)))*np.array(cube['size'])/2
        points=points@r.R(cube['rotation'])[:3,:3].T+cube['center']
        excess=float((points@A.T-limits).max());maximum_protrusion=max(maximum_protrusion,excess)
        check(excess<1e-5,'facial volume inside intended contour: '+cube['name'])
    idle='animation.pixie.air_idle' ;reference=worlds(0,idle)
    minimum=float('inf');neck_minimum=float('inf');samples=0;max_motion=0;bad=[]
    for clip,a in r.animations.items():
        check(a['loop']==clip.endswith('air_idle'),'only idle loops in native file')
        for bone,tracks in a['bones'].items():
            check(bone in seen,'known animated bone')
            check(set(tracks)<={'position','rotation'},'no animated scale')
            for channel,keys in tracks.items():
                pairs=sorted((float(k),np.array(v)) for k,v in keys.items())
                check(pairs[0][0]==0 and abs(pairs[-1][0]-a['animation_length'])<1e-6,'complete timeline')
                check(np.isfinite([v for _,v in pairs]).all(),'finite animation')
        end=worlds(a['animation_length'],clip)
        check(all(np.allclose(end[b],reference[b],atol=1e-4) for b in seen),'animation returns to reference pose: '+clip)
        last=None
        for t in np.linspace(0,a['animation_length'],81):
            pose=worlds(t,clip);samples+=1
            neck_contact=min(overlap(named['neck'],named['neck_connector'],pose),max(overlap(named['neck_connector'],p,pose) for p in parts if p['feature'] in ('cranium','jaw')))
            neck_minimum=min(neck_minimum,neck_contact)
            if last:
                # Compare orientation matrices, not wrapped Euler angle numbers.
                for b in seen:
                    relative=last[b][:3,:3].T@pose[b][:3,:3]
                    angle=np.degrees(np.arccos(np.clip((np.trace(relative)-1)/2,-1,1)))
                    max_motion=max(max_motion,float(angle))
            last=pose
            for side in ('left','right'):
                pairs=[(side+'_deltoid',side+'_upper_arm'),(side+'_upper_arm',side+'_long_glove'),
                       (side+'_long_glove',side+'_palm'),(side+'_upper_boot',side+'_boot_calf'),
                       (side+'_boot_calf',side+'_boot_lower'),(side+'_ankle',side+'_instep')]
                for first,second in pairs:
                    depth=overlap(named[first],named[second],pose);minimum=min(minimum,depth)
                    if depth<-.02:bad.append((clip,round(float(t),3),first,second,round(depth,3)))
    check(not bad,'joint gaps: '+str(bad[:8]))
    check(neck_minimum>=-.02,'head joins neck across all three animations')
    check(max_motion<45,'no abrupt single-frame joint flip')
    # The slap is one-hand contact with an open palm facing the target.
    physical=worlds(IMPACTS['physical'],'animation.pixie.physical_attack')
    forward=(physical['body']@np.array([0,0,-1,0]))[:3]
    normal=(physical['right_hand']@np.array([0,0,-1,0]))[:3]
    check(normal@forward>.8,'slap palm faces forward')
    for clip in ('magic_attack','physical_attack'):
        key=clip.split('_')[0];pose=worlds(IMPACTS[key],'animation.pixie.'+clip)
        for side in (('left','right') if key=='magic' else ('right',)):
            point=np.array(next(b for b in r.data['bones'] if b['name']==side+'_hand')['pivot'])
            wrist=(np.linalg.inv(pose['body'])@pose[side+'_hand']@np.r_[point,1])[:3]/DENSITY
            check(wrist[2]<-7,'hand extends toward target: '+clip+'/'+side)
    baseline=ROOT/'source-before.json';before=json.loads(baseline.read_text()) if baseline.exists() and (BASE/'velvet-eclipse/src').exists() else {}
    for path,digest in before.items():check(hashlib.sha256((BASE/'velvet-eclipse'/path).read_bytes()).hexdigest()==digest,'mod unchanged: '+path)
    report={'status':'passed','checks':count,'cuboids':len(parts),'bones':len(seen),'atlas':[SIZE,SIZE],
        'uniform_density':DENSITY,'integer_dimensions':True,'box_uv':True,'wing_cutout_both_sides':True,
        'animation_clips':{k:v['animation_length'] for k,v in r.animations.items()},'impact_times':IMPACTS,
        'sampled_poses':samples,'minimum_joint_overlap':minimum,'minimum_neck_overlap':neck_minimum,'maximum_sampled_rotation_degrees':max_motion,
        'body_and_wing_cuboids_preserved':preserved,'original_three_clips_preserved':bool(previous.exists()),
        'maximum_structural_head_protrusion':maximum_protrusion,'mod_files_unchanged':len(before),'integrated':False,'game_validation':False}
    (OUT/'validation.json').write_text(json.dumps(report,indent=2)+'\n')
    print(json.dumps(report,indent=2))

if __name__=='__main__':verify()
