"""Validate authoring assets, closed clips, fixed palette and lyre grips."""
import base64,json,math
import numpy as np
from PIL import Image
from build_model import DENSITY,matrix,pose
from render_model import OUT,data,animations,sample,T,R

def worlds(clip,t):
 result={}
 for b in data['bones']:
  p=np.array(b['pivot'])
  result[b['name']]=result.get(b.get('parent'),np.eye(4))@T(sample(b['name'],'position',t,clip))@T(p)@R(np.array(b.get('rotation',[0,0,0]))+sample(b['name'],'rotation',t,clip))@T(-p)
 return result

def point(mats,bone,p):return (mats[bone]@np.array([*(np.array(p)*DENSITY),1]))[:3]/DENSITY

def verify():
 bones=data['bones'];names=[b['name'] for b in bones];parts=json.loads((OUT/'parts.json').read_text(encoding='utf-8'))
 assert len(names)==len(set(names)) and all(n==n.lower() for n in names)
 assert bones[0]['name']=='orpheus' and bones[0]['pivot']==[0,0,0]
 assert {'head','hair','lyre','lyre_strings'}.issubset(names)
 seen=set();rects=[];count=0
 for b in bones:
  if 'parent' in b:assert b['parent'] in seen
  seen.add(b['name'])
  for c in b.get('cubes',[]):
   count+=1;w,h,d=c['size'];assert all(isinstance(v,int) and v>0 for v in c['size'])
   assert isinstance(c['uv'],list) and len(c['uv'])==2
   u,v=c['uv'];r=(u,v,u+2*(w+d),v+h+d)
   assert min(r)>=0 and r[2]<=1024 and r[3]<=1024
   assert all(r[2]<=a or r[0]>=c2 or r[3]<=b2 or r[1]>=d2 for a,b2,c2,d2 in rects)
   rects.append(r)
 assert count==len(parts) and len({p['name'] for p in parts})==count
 bb=json.loads((OUT/'orpheus.bbmodel').read_text(encoding='utf-8'))
 assert bb['meta']['model_format']=='bedrock' and bb['meta']['box_uv']
 assert len(bb['elements'])==count and len(bb['textures'])==1
 assert base64.b64decode(bb['textures'][0]['source'].split(',')[1])==(OUT/'orpheus.png').read_bytes()
 assert Image.open(OUT/'orpheus.png').size==(1024,1024)
 groups=[];refs=[];todo=list(bb['outliner'])
 while todo:
  g=todo.pop();groups.append(g['uuid']);refs.extend(c for c in g['children'] if isinstance(c,str));todo.extend(c for c in g['children'] if isinstance(c,dict))
 assert sorted(refs)==sorted(e['uuid'] for e in bb['elements']) and len(groups)==len(set(groups))==len(bones)
 assert all(set(a['animators']).issubset(groups) for a in bb['animations'])
 assert set(animations)=={'animation.orpheus.'+n for n in ['ground_idle','magic_attack','physical_attack']}
 maxjump=0;max_angle=0
 for clip,a in animations.items():
  assert a['loop']==clip.endswith('ground_idle')
  for bone,tracks in a['bones'].items():
   assert bone in seen
   for channel,keys in tracks.items():
    assert channel in ('rotation','position') # No animation-time color or material substitutions.
    pairs=sorted((float(t),np.array(v)) for t,v in keys.items())
    assert pairs[0][0]==0 and pairs[-1][0]==a['animation_length']
    rest=np.array(next(b for b in bones if b['name']==bone).get('rotation',[0,0,0]))
    if channel=='rotation':assert np.allclose(matrix(rest+pairs[0][1]),matrix(rest+pairs[-1][1]),atol=1e-5)
    else:assert np.allclose(pairs[0][1],pairs[-1][1])
    for (ta,va),(tb,vb) in zip(pairs,pairs[1:]):
     assert tb>ta and np.isfinite(vb).all()
     if channel=='rotation':
      maxjump=max(maxjump,float(np.abs(vb-va).max()))
      delta=matrix(rest+va).T@matrix(rest+vb)
      max_angle=max(max_angle,math.degrees(math.acos(np.clip((np.trace(delta)-1)/2,-1,1))))
 # Near a gimbal orientation, Euler coordinates can change greatly while the
 # actual joint orientation stays continuous. Test the physical rotation.
 assert max_angle<15,('joint orientation discontinuity',max_angle)
 # Check the rigid instrument and both hands at the fully gripped release poses.
 errors={}
 for kind,stamps in [('magic',np.linspace(1.2,2.1,45)),('physical',np.linspace(.7,1.65,45))]:
  clip='animation.orpheus.'+kind+'_attack';worst=0
  for t in stamps:
   mats=worlds(clip,t)
   for label,s in [('right',-1),('left',1)]:
    local=np.array([s*2,23,4],float)
    if kind=='magic' and label=='right':local=np.array([-1.25+np.sin((t-1.15)*np.pi*3)*.9,31,2.65])
    elif kind=='magic':local=np.array([7.,34.,4.])
    actual=point(mats,label+'_hand',(s*5,21,0));target=point(mats,'lyre',local)
    worst=max(worst,float(np.linalg.norm(actual-target)))
  errors[kind]=round(worst,4)
 assert max(errors.values())<.12,('hand detached from lyre',errors)
 idle=worlds('animation.orpheus.ground_idle',0);magic=worlds('animation.orpheus.magic_attack',1.6)
 idlelyre=point(idle,'lyre',(0,32,4));magiclyre=point(magic,'lyre',(0,32,4))
 assert magiclyre[2]<-7 and magiclyre[1]<idlelyre[1]
 report={'status':'passed','cuboids':count,'bones':len(bones),'integer_cuboid_sizes':True,'box_uv_nonoverlapping':True,'blockbench_hierarchy_and_clips':True,'single_texture_all_clips':True,'embedded_png_matches':True,'closed_clip_boundaries':True,'max_rotation_coordinate_delta_degrees':round(maxjump,4),'max_actual_joint_rotation_delta_degrees':round(max_angle,4),'grip_wrist_error_logical_units':errors,'lyre_idle_center':idlelyre.round(3).tolist(),'lyre_magic_center':magiclyre.round(3).tolist(),'mod_integration':False,'render_source':'exported geometry, texture and animation JSON'}
 (OUT/'validation.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8');print(json.dumps(report,indent=2))

if __name__=='__main__':verify()
