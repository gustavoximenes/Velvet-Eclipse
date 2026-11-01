"""Validate actual boxes, atlas, editable source, keyframes and animated contacts."""
import json,base64,re
from pathlib import Path
import numpy as np
import render_model as r
from joint_check import worlds,overlap
from animate_model import LENGTHS
O=r.OUT;count=0
def check(ok,label):
 global count;count+=1
 if not ok:raise AssertionError(label)
def main():
 parts=json.loads((O/'parts.json').read_text());named={p['name']:p for p in parts};bb=json.loads((O/'angel.bbmodel').read_text());rects=[];seen=set()
 check(r.data['bones'][0]['name']=='angel' and r.data['bones'][0]['pivot']==[0,0,0],'root origin')
 for b in r.data['bones']:
  check(bool(re.fullmatch('[a-z][a-z0-9_]*',b['name'])),'bone naming')
  check(b['name'] not in seen and (not b.get('parent') or b['parent'] in seen),'bone tree');seen.add(b['name'])
  for c in b.get('cubes',[]):
   check(all(isinstance(x,int) and x>0 for x in c['size']),'integer dimensions')
   check(isinstance(c['uv'],list) and len(c['uv'])==2,'Box UV')
   u,v=c['uv'];w,h,d=c['size'];rect=(u,v,u+2*(w+d),v+h+d)
   check(min(rect)>=0 and max(rect)<=1024,'atlas bounds')
   check(all(rect[2]<=a or rect[0]>=x or rect[3]<=b or rect[1]>=y for a,b,x,y in rects),'nonoverlapping UV');rects.append(rect)
 check(len(bb['elements'])==len(named)==len(rects),'editable model count')
 check(base64.b64decode(bb['textures'][0]['source'].split(',')[1])==(O/'angel.png').read_bytes(),'embedded atlas')
 check(len(bb['animations'])==len(LENGTHS)==len(r.animations),'editable clips')
 check(not any(p['feature']=='eye' for p in parts),'opaque blindfold respected')
 pairs=[('neck','neck_upper_joint'),('neck_upper_joint','lower_face'),('pelvis','waist'),('waist','ribcage'),('ribcage','upper_chest'),('upper_chest','neck'),('nose_bridge','nose_tip')]
 for side in ['right','left']:
  pairs += [('upper_chest',side+'_shoulder'),(side+'_shoulder',side+'_upper_arm'),(side+'_upper_arm',side+'_elbow'),(side+'_forearm',side+'_wrist'),(side+'_wrist',side+'_palm'),('pelvis',side+'_upper_thigh'),(side+'_upper_thigh',side+'_lower_thigh'),(side+'_lower_thigh',side+'_knee'),(side+'_knee',side+'_calf'),(side+'_calf',side+'_shin'),(side+'_shin',side+'_ankle'),('upper_chest',side+'_wing_root'),(side+'_wing_root',side+'_wing_leading')]
 minimum=999.;samples=0;clearance=999.
 for clip,a in r.animations.items():
  check(set(a['bones'])<=seen,'animation bones')
  for name,channels in a['bones'].items():
   check(set(channels)<={'position','rotation'},'no animated scale')
   for channel,keys in channels.items():
    vals=np.array(list(keys.values()));check(np.isfinite(vals).all(),'finite keys')
    check(float(min(keys,key=float))==0 and abs(float(max(keys,key=float))-a['animation_length'])<1e-5,'full timeline')
    if a['loop']:check(np.allclose(vals[0],vals[-1],atol=1e-4),'closed loop')
    if channel=='rotation':check(np.max(np.abs(np.diff(vals,axis=0)))<70,'no Euler flips')
  for t in np.linspace(0,a['animation_length'],49):
   pose=worlds(float(t),clip);samples+=1
   check(all(np.isfinite(x).all() for x in pose.values()),'finite pose')
   for one,two in pairs:
    depth=overlap(named[one],named[two],pose);minimum=min(minimum,depth)
    check(depth>=-.05,f'contact {clip} {t:.2f} {one}/{two}: {depth}')
   for one,two in [('right_palm','left_palm'),('right_palm','hair_crown'),('left_palm','hair_crown'),('right_knee','left_knee'),('right_calf','left_calf'),('right_ankle','left_shin')]:
    gap=-overlap(named[one],named[two],pose);clearance=min(clearance,gap)
    check(gap>=-.05,f'clearance {clip} {t:.2f} {one}/{two}: {gap}')
 report={'status':'passed','checks':count,'bones':len(seen),'cuboids':len(parts),'atlas':1024,'sampled_poses':samples,'minimum_contact_native_units':minimum,'minimum_clearance_native_units':clearance,'facial_clips':4,'animation_clips':7,'integration':False,'minecraft_executed':False}
 (O/'validation.json').write_text(json.dumps(report,indent=2));print(json.dumps(report,indent=2))
if __name__=='__main__':main()
