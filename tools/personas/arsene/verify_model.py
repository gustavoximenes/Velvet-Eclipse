"""Asset and animation checks for the standalone approval package."""
import base64, json, math
import numpy as np
from PIL import Image
from build_model import DENSITY,face_uv,matrix
from render_model import OUT,data,animations,sample,T,R

def worlds(clip,t):
 result={}
 for b in data['bones']:
  p=np.array(b['pivot'])
  result[b['name']]=result.get(b.get('parent'),np.eye(4))@T(sample(b['name'],'position',t,clip))@T(p)@R(np.array(b.get('rotation',[0,0,0]))+sample(b['name'],'rotation',t,clip))@T(-p)
 return result

def joint(clip,t,name):
 b=next(b for b in data['bones'] if b['name']==name)
 return (worlds(clip,t)[name]@np.array([*b['pivot'],1]))[:3]/DENSITY

def verify():
 bones=data['bones'];names=[b['name'] for b in bones];parts=json.loads((OUT/'parts.json').read_text(encoding='utf-8'))
 assert len(names)==len(set(names)) and all(n==n.lower() for n in names)
 assert bones[0]['name']=='arsene' and bones[0]['pivot']==[0,0,0] and 'parent' not in bones[0]
 assert 'head' in names
 seen=set();rectangles=[];count=0
 for b in bones:
  if 'parent' in b:assert b['parent'] in seen
  seen.add(b['name'])
  for c in b.get('cubes',[]):
   count+=1;w,h,d=c['size'];assert all(isinstance(v,int) and v>0 for v in c['size'])
   assert isinstance(c['uv'],list) and len(c['uv'])==2
   u,v=c['uv'];r=(u,v,u+2*(w+d),v+h+d)
   assert r[0]>=0 and r[1]>=0 and r[2]<=1024 and r[3]<=1024
   assert all(r[2]<=a or r[0]>=c2 or r[3]<=b2 or r[1]>=d2 for a,b2,c2,d2 in rectangles)
   rectangles.append(r)
 assert count==len(parts) and len({p['name'] for p in parts})==count
 project=json.loads((OUT/'arsene.bbmodel').read_text(encoding='utf-8'))
 assert project['meta']['model_format']=='bedrock' and project['meta']['box_uv'] is True
 assert len(project['elements'])==count and len(project['animations'])==3
 assert all(e['box_uv'] for e in project['elements'])
 assert base64.b64decode(project['textures'][0]['source'].split(',')[1])==(OUT/'arsene.png').read_bytes()
 assert Image.open(OUT/'arsene.png').size==(1024,1024)
 expected={'animation.arsene.ground_idle','animation.arsene.magic_attack','animation.arsene.physical_attack'}
 assert set(animations)==expected
 max_jump=0
 for name,clip in animations.items():
  assert clip['loop']==name.endswith('ground_idle')
  for bone,tracks in clip['bones'].items():
   assert bone in seen
   for channel,keys in tracks.items():
    pairs=sorted((float(t),np.array(v)) for t,v in keys.items())
    assert pairs[0][0]==0 and pairs[-1][0]==clip['animation_length']
    rest=np.array(next(b for b in bones if b['name']==bone).get('rotation',[0,0,0]))
    assert np.allclose(matrix(rest+pairs[0][1]),matrix(rest+pairs[-1][1]),atol=1e-5), (name,bone,'loop boundary')
    for (a,av),(b,bv) in zip(pairs,pairs[1:]):
     assert b>a and np.isfinite(bv).all()
     if channel=='rotation':max_jump=max(max_jump,float(abs(bv-av).max()))
 assert max_jump<55,('large Euler discontinuity',max_jump)
 magic='animation.arsene.magic_attack';physical='animation.arsene.physical_attack'
 hand_up=joint(magic,.8,'right_hand');hand_low=joint(magic,1.6,'right_hand')
 assert hand_up[1]-hand_low[1]>20 and hand_low[1]<27
 gaze_error=max(float(np.linalg.norm(worlds(magic,float(t))['head'][:3,:3]@np.array([0,0,-1])-np.array([0,0,-1]))) for t in np.linspace(0,2.8,80))
 assert gaze_error<.001,('magic gaze',gaze_error)
 high=joint(physical,.95,'right_foot');low=joint(physical,1.25,'right_foot')
 assert high[1]>40 and low[1]<15 and low[0]-high[0]>25,(high,low)
 report={'status':'passed','cuboids':count,'bones':len(bones),'all_sizes_integer':True,'box_uv_nonoverlapping':True,'embedded_texture_matches_png':True,'three_clips_return_to_idle':True,'max_keyframe_rotation_delta_degrees':round(max_jump,4),'magic_wrist_raised':hand_up.round(3).tolist(),'magic_wrist_lowered':hand_low.round(3).tolist(),'magic_forward_gaze_error':round(gaze_error,8),'kick_ankle_high':high.round(3).tolist(),'kick_ankle_low':low.round(3).tolist(),'render_source':'exported geometry, texture and animation JSON','in_game_test':False,'mod_integration':False}
 (OUT/'validation.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8');print(json.dumps(report,indent=2))

if __name__=='__main__':verify()
