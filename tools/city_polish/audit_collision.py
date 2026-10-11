from pathlib import Path
import json,gzip,math
import numpy as np
R=Path(__file__).resolve().parents[2];D=R/'src/main/resources/data/velvet_eclipse/city'
plan=json.loads(gzip.decompress((D/'plan.json.gz').read_bytes()));defs=json.loads(gzip.decompress((D/'architecture.json.gz').read_bytes()))
cells={tuple(c[:3]):c for c in plan['cells']};local={}
for k,c in cells.items():
    if c[3]==40:local[k]=np.array(defs[c[4]]['boxes']).reshape(-1,6)+np.tile(k,2)
def around(p):
    found=[]
    for x in range(math.floor(p[0]-.3),math.floor(p[0]+.3)+1):
        for y in range(math.floor(p[1]-.61),math.floor(p[1]+2.4)+1):
            for z in range(math.floor(p[2]-.3),math.floor(p[2]+.3)+1):
                k=(x,y,z)
                if k in local:found.extend(local[k])
                elif k in cells and cells[k][3]<=8:found.append([x,y,z,x+1,y+1,z+1])
    return np.array(found).reshape(-1,6)
def walk(points):
    y=points[0][1];n=0
    for a,b in zip(points,points[1:]):
        a=np.array(a);b=np.array(b)
        for t in np.linspace(0,1,max(2,math.ceil(np.linalg.norm(b-a)/.09))):
            p=a+(b-a)*t;p[1]=y;boxes=around(p)
            footprint=(boxes[:,0]<p[0]+.3-1e-5)&(boxes[:,3]>p[0]-.3+1e-5)&(boxes[:,2]<p[2]+.3-1e-5)&(boxes[:,5]>p[2]-.3+1e-5)
            support=footprint&(boxes[:,4]<=y+.6)&(boxes[:,4]>=y-.6)
            if not support.any():return {'error':'no support','at':p.tolist()}
            y=float(boxes[support,4].max());p[1]=y
            hits=footprint&(boxes[:,4]>y+.01)&(boxes[:,1]<y+1.8)
            if hits.any():return {'error':'blocked','at':p.tolist(),'boxes':boxes[hits][:3].tolist()}
            n+=1
    return {'samples':n,'endY':y}
results={}
for name,points in json.loads((Path(__file__).resolve().parent/'approved/mall-routes.json').read_text()).items():
    pts=[[p[0]+93,p[1]+64,p[2]+65] for p in points];results['mall_'+name]=walk(pts)
routes={
 'dorm_entry':[[0,0,16],[0,0,12],[0,0,4],[0,0,0],[-5.8,0,0]],
 'computers':[[-5.85,15.07,0],[-5.85,15.07,-3.15],[7.5,15.07,-3.15],[7.5,15.07,-7.25],[8.9,15.07,-7.25]],
 'roof':[[-13,20.07,1.65],[-13,20.07,2.2],[-11.3,20.07,2.2],[-11.3,20.07,3.65],[-7.8,20.07,3.65],[0,20.07,3.65],[0,20.07,-4.4]],
}
for floor in range(4):routes['stairs_'+str(floor)]=[[-10.55,floor*5,0],[-10.55,floor*5,-10],[-13,floor*5,-10],[-13,floor*5,1.85],[-10.55,(floor+1)*5,1.85]]
routes['stairs_3']=routes['stairs_3'][:-1]
for name,pts in routes.items():results[name]=walk([[p[0]-100,p[1]+65,p[2]+66] for p in pts])
destinations={}
for dest in plan['places']:
    if not (dest['id'] in ['dorm','lounge','kitchen','reading','bedroom','bathroom','command','roof','velvet_exit','paulownia'] or dest['id'].startswith('shop_')):continue
    found=False
    for dy in range(25):
        p=np.array([dest['x']+.5,dest['y']+dy/8+.002,dest['z']+.5]);boxes=around(p)
        foot=(boxes[:,0]<p[0]+.3)&(boxes[:,3]>p[0]-.3)&(boxes[:,2]<p[2]+.3)&(boxes[:,5]>p[2]-.3)
        hits=foot&(boxes[:,4]>p[1]+.001)&(boxes[:,1]<p[1]+1.8)
        support=foot&(boxes[:,4]<=p[1]+.001)&(boxes[:,4]>=p[1]-.126)
        if not hits.any() and support.any():found=p.tolist();break
    destinations[dest['id']]=found
report={'routes':results,'destinations':destinations};(Path(__file__).resolve().parent/'collision-report.json').write_text(json.dumps(report,indent=2));print(json.dumps(report,indent=2))

assert all('error' not in result for result in results.values()),results
assert all(destinations.values()),destinations
print('PASS exported city circulation:',sum(row['samples'] for row in results.values()),'samples;',len(destinations),'destinations')
