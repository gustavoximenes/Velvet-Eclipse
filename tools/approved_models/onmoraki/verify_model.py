"""Resource, articulation, loop and source-preservation checks for the draft."""
from pathlib import Path
import base64,hashlib,json,re
import numpy as np
import render_model as r
from build_model import SIZE,DENSITY
from animate_model import IMPACTS,NAMES
from joint_check import worlds,overlap
ROOT=r.ROOT;OUT=r.OUT;count=0
def check(value,label):
    global count
    count+=1
    assert value,label

def main():
    parts=json.loads((OUT/'parts.json').read_text());named={p['name']:p for p in parts}
    bb=json.loads((OUT/'onmoraki.bbmodel').read_text(encoding='utf-8'));seen=set();rects=[]
    check(r.data['bones'][0]['name']=='onmoraki' and r.data['bones'][0]['pivot']==[0,0,0],'root at origin')
    for b in r.data['bones']:
        check(bool(re.fullmatch('[a-z][a-z0-9_]*',b['name'])),'lowercase bone')
        check(b['name'] not in seen and (not b.get('parent') or b['parent'] in seen),'acyclic hierarchy')
        seen.add(b['name'])
        for c in b.get('cubes',[]):
            check(all(isinstance(v,int) and v>0 for v in c['size']),'positive integer dimensions')
            check(isinstance(c['uv'],list) and len(c['uv'])==2,'Box UV')
            u,v=c['uv'];w,h,d=c['size'];rect=(u,v,u+2*(w+d),v+h+d)
            check(min(rect)>=0 and max(rect)<=SIZE,'texture bounds')
            check(all(rect[2]<=a or rect[0]>=x or rect[3]<=b or rect[1]>=y for a,b,x,y in rects),'UV packing')
            rects.append(rect);check(np.isfinite(c['origin']+c.get('rotation',[])+c.get('pivot',[])).all(),'finite geometry')
    check(len(parts)==len(named)==len(bb['elements'])==len(rects),'BB and geometry correspondence')
    check(bb['meta']['model_format']=='bedrock' and bb['meta']['box_uv'],'native editable format')
    check(base64.b64decode(bb['textures'][0]['source'].split(',')[1])==(OUT/'onmoraki.png').read_bytes(),'embedded atlas')
    check(len(bb['animations'])==3,'three editable clips')
    for f in ('head','body','neck','wing','feather','leg','claw','jaw','mouth','crest'):
        check(any(p['feature']==f for p in parts),'anatomy '+f)
    pairs=[('chest','neck_joint'),('neck_joint','skull_front_core'),
           ('skull_front_core','upper_skull_front_core'),('upper_skull_front_core','crown_front_core'),
           ('crown_front_core','stalk_socket_front_core'),('stalk_socket_front_core','crest_0'),
           ('oral_hinge','lower_jaw_front_core')]
    pairs += [(f'crest_{i}',f'crest_{i+1}') for i in range(4)]
    for side in ('right','left'):
        pairs += [('chest',side+'_shoulder'),(side+'_shoulder',side+'_wing_arm'),(side+'_wing_arm',side+'_wing_forearm'),
                  ('rump_side_core',side+'_thigh'),(side+'_thigh',side+'_shin'),(side+'_shin',side+'_foot')]
    check(all(a in named and b in named for a,b in pairs),'structural contact names')
    baseline=worlds(0,'animation.onmoraki.air_idle');bad=[];min_overlap=999.;samples=0;maximum_step=0
    for clip,a in r.animations.items():
        check(a['loop']==clip.endswith('air_idle'),'only idle loops')
        check(set(a['bones'])<=seen,'animation references existing bones')
        for bone,tracks in a['bones'].items():
            check(set(tracks)<={'position','rotation'},'no scale animation')
            for keys in tracks.values():
                times=sorted(float(k) for k in keys)
                check(times[0]==0 and abs(times[-1]-a['animation_length'])<1e-6,'full timeline')
                check(np.isfinite(list(keys.values())).all(),'finite keys')
        # Read the explicit endpoint; the idle sampler otherwise wraps to 0.
        for bone,tracks in a['bones'].items():
            for ch,keys in tracks.items():
                check(np.allclose(keys[min(keys,key=float)],keys[max(keys,key=float)],atol=1e-5),'clip boundary '+clip+'/'+bone+'/'+ch)
        check(all(np.allclose(worlds(a['animation_length'],clip)[b],baseline[b],atol=1e-4) for b in seen),'return to rest '+clip)
        last=None
        for t in np.linspace(0,a['animation_length'],97):
            mats=worlds(float(t),clip);samples+=1
            for left,right in pairs:
                depth=overlap(named[left],named[right],mats);min_overlap=min(min_overlap,depth)
                if depth<-.02:bad.append((clip,round(float(t),3),left,right,round(depth,5)))
            if last:
                for b in seen:
                    step=np.degrees(np.arccos(np.clip((np.trace(last[b][:3,:3].T@mats[b][:3,:3])-1)/2,-1,1)))
                    maximum_step=max(maximum_step,float(step))
            last=mats
    check(not bad,'disconnected joints '+str(bad[:15]))
    (OUT/'connections.json').write_text(json.dumps(pairs,indent=2)+'\n')
    check(maximum_step<12,'no sudden pose flips')
    physical='animation.onmoraki.'+NAMES['physical'];magic='animation.onmoraki.'+NAMES['magic']
    check(r.sample('onmoraki','position',IMPACTS['physical'],physical)[2]<-20,'forward physical strike')
    check(r.sample('jaw','rotation',IMPACTS['magic'],magic)[0]<-25,'spell mouth opens')
    check(r.sample('left_wing','rotation',IMPACTS['magic'],magic)[2]>25,'spell wing extension')
    snapshot=json.loads((ROOT/'source-before.json').read_text())
    for path,digest in snapshot.items():check(hashlib.sha256(Path(path).read_bytes()).hexdigest()==digest,'production unchanged '+path)
    result={'status':'passed','checks':count,'cuboids':len(parts),'bones':len(seen),'atlas':[SIZE,SIZE],'authoring_density':DENSITY,
            'sampled_poses':samples,'structural_contacts':len(pairs),'minimum_joint_overlap_native_units':min_overlap,
            'max_rotation_step_degrees':maximum_step,'clip_durations':{k:v['animation_length'] for k,v in r.animations.items()},
            'proposed_impact_seconds':IMPACTS,'source_and_installed_jar_preserved':len(snapshot),'integrated':False,'minecraft_run':False}
    (OUT/'validation.json').write_text(json.dumps(result,indent=2)+'\n');print(json.dumps(result,indent=2),flush=True)
if __name__=='__main__':main()
