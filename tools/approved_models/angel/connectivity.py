"""Inspect whole-model surface connections, including small decorative pieces."""
import json,sys,hashlib
import numpy as np
import render_model as r
from joint_check import worlds,box,overlap

def components(parts,pose,tolerance=.08):
 boxes=[box(p,pose) for p in parts]
 centers=np.array([x[0] for x in boxes]);axes=np.array([x[1] for x in boxes]);half=np.array([x[2] for x in boxes]);ext=np.einsum('nij,nj->ni',np.abs(axes),half)
 low=centers-ext;high=centers+ext
 candidate=np.all(low[:,None,:]<=high[None,:,:]+tolerance,axis=2)&np.all(low[None,:,:]<=high[:,None,:]+tolerance,axis=2)
 adj=[set() for _ in parts]
 for i,j in zip(*np.where(np.tril(candidate,-1))):
  basis=np.concatenate([axes[i].T,axes[j].T,np.cross(axes[i].T[:,None,:],axes[j].T[None,:,:]).reshape(-1,3)])
  length=np.linalg.norm(basis,axis=1);basis=basis[length>1e-8];basis/=np.linalg.norm(basis,axis=1)[:,None]
  depth=np.abs(basis@axes[i])@half[i]+np.abs(basis@axes[j])@half[j]-np.abs(basis@(centers[j]-centers[i]))
  if depth.min()>=-tolerance:adj[i].add(j);adj[j].add(i)
 seen=set();groups=[]
 for i in range(len(parts)):
  if i in seen:continue
  group=set();todo=[i]
  while todo:
   j=todo.pop()
   if j in group:continue
   group.add(j);todo.extend(adj[j]-group)
  seen|=group;groups.append([parts[j]['name'] for j in sorted(group)])
 return sorted(groups,key=len,reverse=True)

def linked(a,b,pose):
 # Metal links interlock through their openings; they need not intersect as
 # solid boxes. Count signed crossings of one closed loop through the other.
 bone_a=next(x for x in r.data['bones'] if x['name']==a);bone_b=next(x for x in r.data['bones'] if x['name']==b)
 pa=np.array(bone_a['pivot']);pb=np.array(bone_b['pivot'])
 corners=np.array([[-2,-3.5,0],[2,-3.5,0],[2,3.5,0],[-2,3.5,0]])
 world=np.c_[corners+pb,np.ones(4)]@pose[b].T
 local=(world@np.linalg.inv(pose[a]).T)[:,:3]-pa
 count=0
 for p,q in zip(local,np.roll(local,-1,axis=0)):
  if (p[2]<=0<q[2]) or (q[2]<=0<p[2]):
   t=-p[2]/(q[2]-p[2]);hit=p+t*(q-p)
   if abs(hit[0])<2 and abs(hit[1])<3.5:count+=1 if q[2]>p[2] else -1
 return abs(count)==1

def main():
 parts=json.loads((r.OUT/'parts.json').read_text());body=[p for p in parts if p['feature']!='chain'];results=[]
 chain=sorted([b['name'] for b in r.data['bones'] if b['name'].startswith('chain_link_')],key=lambda n:int(n.rsplit('_',1)[1]))
 mount=next(p for p in parts if p['name']=='chain_collar_mount');first=[p for p in parts if p['bone']==chain[0]]
 for kind,a in r.animations.items():
  for t in np.linspace(0,a['animation_length'],49):
   pose=worlds(t,kind);groups=components(body,pose)
   unlinked=[(x,y) for x,y in zip(chain,chain[1:]) if not linked(x,y,pose)]
   attached=max(overlap(mount,p,pose) for p in first)>=-.08
   row={'clip':kind,'time':float(t),'body_components':[len(x) for x in groups],'detached':groups[1:],'unlinked_chain_pairs':unlinked,'collar_attached':bool(attached)};results.append(row)
   if len(groups)>1 or unlinked or not attached:print(kind,round(t,2),'detached',[g[:3] for g in groups[1:]],'chain',unlinked,'mount',attached,flush=True)
 report={'status':'passed' if all(len(x['body_components'])==1 and not x['unlinked_chain_pairs'] and x['collar_attached'] for x in results) else 'failed','poses':len(results),'chain_links':len(chain),'method':'Solid component connectivity for body, clothes, hair, wings and mount; topological interlocking for chain loops.','results':results}
 report['source_hashes']={n:hashlib.sha256((r.OUT/n).read_bytes()).hexdigest() for n in ['angel.geo.json','angel.animation.json','angel.png']}
 (r.OUT/'connectivity-validation.json').write_text(json.dumps(report,indent=2))
 print(report['status'],len(results),'whole-model poses',flush=True)
 if report['status']!='passed':sys.exit(1)
if __name__=='__main__':main()
