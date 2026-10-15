"""Fit native cuboids inside an explicitly authored, faceted head envelope.

This creates no mesh or smoothing. The output is a collection of ordinary rotated
cuboids. The envelope prevents any internal volume from protruding past the
authored cheek, chin or side-profile planes. Dimensions remain native integers.
"""
import sys,json,math
from pathlib import Path
import numpy as np
ROOT=Path(__file__).resolve().parent
from scipy.optimize import minimize,linprog
from scipy.spatial import HalfspaceIntersection,ConvexHull
from rig_math import matrix

planes=[]
def plane(n,b):
    planes.append((np.array(n,float),b));return len(planes)-1

# Deliberately authored Nekomata envelope, independent of the previous Pixie face.
sides=[]
contour=[(.26,29.75),(.67,29.88),(1.18,30.4),(1.50,31.1),(1.57,32.0),(1.40,32.70),(1.0,33.15)]
labels=['chin_bevel','chin_facet','mandible','lower_cheek','cheek','temple']
for side,s in [('right',-1),('left',1)]:
    for j,((xa,ya),(xb,yb)) in enumerate(zip(contour,contour[1:])):
        k=-(xb-xa)/(yb-ya);bound=xa+k*ya
        feature='chin' if j<2 else 'jaw' if j==2 else 'cheek'
        i=plane((s,k,0),bound)
        sides.append((side+'_'+labels[j],(0,0,math.degrees(math.atan2(k*s,1))),i,feature))
bottom=plane((0,-1,0),-29.75)
top=plane((0,1,0),33.15)
front=plane((0,-math.tan(math.radians(7)),-1),1.45-math.tan(math.radians(7))*31.5)
lowerfront=front
back=plane((0,0,1),1.35)
under=plane((0,-1,.80),-30.10)
corners=[]
for side,s in [('right',-1),('left',1)]:
    corners.append((side+'_rear_corner',(0,-s*math.degrees(math.atan(.40)),0),plane((s,0,.40),1.91),'cranium'))
forehead=plane((0,.30,-1),11.50)
A=np.array([n for n,b in planes]);b=np.array([b for n,b in planes])

def fit(name,rot,face,feature,surface=False):
    R=matrix(rot);K=np.abs(A@R)
    weights=np.ones(3)
    if surface:weights[np.argmax(K[face])]=.04
    # Six variables: center and half dimensions. A box's support along each
    # normal gives an exact containment test for all eight corners.
    M=np.hstack([A,K])
    others=np.arange(len(b))!=face
    bounds=[(-2,2),(29.30,33.15),(-1.8,1.35)]+[(.0625,2.5)]*3
    if surface:bounds[3+np.argmax(K[face])]=(.0625,.0625)
    feasible=linprog(np.r_[np.zeros(3),-np.ones(3)],A_ub=M,b_ub=b,
                    A_eq=M[[face]],b_eq=b[[face]],bounds=bounds,method='highs')
    assert feasible.success,(name,feasible.message)
    result=minimize(lambda v:-(weights*np.log(v[3:])).sum(),feasible.x,
        jac=lambda v:np.r_[np.zeros(3),-weights/v[3:]],bounds=bounds,
        constraints=[{'type':'ineq','fun':lambda v:b[others]-M[others]@v,'jac':lambda v:-M[others]},
                     {'type':'eq','fun':lambda v:M[face]@v-b[face],'jac':lambda v:M[face]}],
        method='SLSQP',options={'ftol':1e-10,'maxiter':600})
    assert result.success,(name,result.message)
    dims=np.floor(result.x[3:]*16+1e-6)/8
    assert min(dims)>0,name
    padded=b.copy()
    padded[face]=b[face]
    limits=padded-K@(dims/2)
    seed=linprog(np.zeros(3),A_ub=A,b_ub=limits,A_eq=A[[face]],b_eq=limits[[face]],bounds=[(None,None)]*3,method='highs')
    assert seed.success,(name,seed.message)
    center=minimize(lambda c:np.sum((c-result.x[:3])**2),seed.x,
        jac=lambda c:2*(c-result.x[:3]),
        constraints=[{'type':'ineq','fun':lambda c:limits[others]-A[others]@c,'jac':lambda c:-A[others]},
                     {'type':'eq','fun':lambda c:A[face]@c-limits[face],'jac':lambda c:A[face]}],
        method='SLSQP',options={'ftol':1e-11,'maxiter':300})
    if not center.success:center=seed
    assert np.max(A@center.x+K@(dims/2)-padded)<1e-6,name
    return {'name':name,'center':center.x.tolist(),'size':dims.tolist(),'rotation':list(rot),'feature':feature}

def main():
    specs=[('cranium',(0,0,0),back,'cranium'),('midface',(-7,0,0),front,'midface'),
           ('forehead',(math.degrees(math.atan(.30)),0,0),forehead,'forehead'),
           ('lower_face',(-7,0,0),lowerfront,'jaw'),
           ('chin',(0,0,0),bottom,'chin'),
           ('jaw_underside',(-math.degrees(math.atan(.80)),0,0),under,'jaw')]+sides+corners
    boxes=[fit(*row) for row in specs]
    for name,rot,face,feature in sides:
        if not any(label in name for label in ('mandible','chin_facet','chin_bevel')):continue
        pitch=math.degrees(math.atan(math.cos(math.radians(rot[2]))/.80))
        boxes.append(fit(name+'_profile',(pitch,0,rot[2]),face,feature,True))
    # Fit corner junctions too: separate face-maximizing cuboids leave notches
    # where the front, jaw and underside meet. These are ordinary solid cuboids.
    vertices=HalfspaceIntersection(np.c_[A,-b],np.array([0.,31.10,0.])).intersections
    hull=ConvexHull(vertices)
    points=[*vertices]
    for triangle in hull.simplices:
        q=vertices[triangle]
        points.extend([(q[0]+q[1])/2,(q[1]+q[2])/2,(q[2]+q[0])/2,q.mean(0)])
    points=np.unique(np.round(points,6),axis=0)
    def covered(point):
        return any(np.max(np.abs(matrix(box['rotation']).T@(point-np.array(box['center'])))-np.array(box['size'])/2)<=.015 for box in boxes)
    rotations=[(0,0,0),(-math.degrees(math.atan(.16)),0,0),(-math.degrees(math.atan(.80)),0,0)]
    rotations.extend((0,0,s*math.degrees(math.atan(k))) for s in (-1,1) for k in (.32,.82,1.75))
    for surface_point in points:
        point=surface_point*.96+np.array([0.,31.10,0.])*.04
        if covered(point):continue
        candidates=[c for rot in rotations if (c:=fit_junction(point,rot)) is not None]
        assert candidates,point
        box=max(candidates,key=lambda row:np.prod(row['size']))
        box.update(name='contour_joint_'+str(len(boxes)),feature='chin' if point[1]<30 else 'cheek')
        boxes.append(box)
    # Finish each authored side plane with coplanar native solids. The inner
    # intersection boxes provide volume, but must not create visible stair steps.
    for name,rot,face,feature in sides+corners+[specs[1],specs[2],specs[3],specs[5]]:
        poly=vertices[np.abs(vertices@A[face]-b[face])<1e-5]
        center=poly.mean(0)
        points=[center,*[v*.93+center*.07 for v in poly]]
        points.extend((a+c)*.465+center*.07 for a in poly for c in poly)
        for pt in points:
            def flat_cover(box):
                rr=matrix(box['rotation']);kk=np.abs(A[face]@rr)
                if abs(kk.max()-np.linalg.norm(A[face]))>1e-5:return False
                if abs(A[face]@box['center']+kk@(np.array(box['size'])/2)-b[face])>1e-5:return False
                return np.max(np.abs(rr.T@(pt-np.array(box['center'])))-np.array(box['size'])/2)<1e-5
            if any(flat_cover(box) for box in boxes):continue
            patch=fit_patch(pt,rot,face)
            if patch:
                patch.update(name=name+'_surface_'+str(len(boxes)),feature=feature)
                boxes.append(patch)
    # Drop identical/internal pieces that add no shape to the assembled solid.
    retained=[]
    for box in sorted(boxes,key=lambda q:-np.prod(q['size'])):
        R=matrix(box['rotation']);center=np.array(box['center']);half=np.array(box['size'])/2
        import itertools
        box_corners=np.array(list(itertools.product((-1,1),repeat=3)))*half
        box_corners=box_corners@R.T+center
        if any(np.all(np.abs((box_corners-np.array(other['center']))@matrix(other['rotation']))<=np.array(other['size'])/2+1e-7) for other in retained):continue
        retained.append(box)
    boxes=retained
    (ROOT/'facial_volume.json').write_text(json.dumps({'planes':[{'normal':n.tolist(),'offset':bb} for n,bb in planes],
        'boxes':boxes},indent=2)+'\n')
    print(f'Fitted {len(boxes)} integer-dimension cuboids inside the authored facial envelope')

def fit_junction(point,rot):
    R=matrix(rot);K=np.abs(A@R);M=np.hstack([A,K])
    padded=b.copy()
    # Exact point containment, expressed in the candidate cube's local frame.
    P=np.vstack([np.c_[R.T,-np.eye(3)],np.c_[-R.T,-np.eye(3)]])
    pb=np.r_[R.T@point,-R.T@point]
    cons=np.vstack([M,P]);rhs=np.r_[padded,pb]
    bounds=[(-2,2),(29.48,33.8),(-1.9,1.4)]+[(.0625,2.5)]*3
    seed=linprog(np.r_[np.zeros(3),-np.ones(3)],A_ub=cons,b_ub=rhs,bounds=bounds,method='highs')
    if not seed.success:return None
    result=minimize(lambda v:-np.log(v[3:]).sum(),seed.x,jac=lambda v:np.r_[np.zeros(3),-1/v[3:]],
        bounds=bounds,constraints=[{'type':'ineq','fun':lambda v:rhs-cons@v,'jac':lambda v:-cons}],
        method='SLSQP',options={'ftol':1e-9,'maxiter':200})
    if not result.success:return None
    dims=np.maximum(.125,np.floor(result.x[3:]*16+1e-6)/8)
    ca=np.vstack([A,R.T,-R.T]);cb=np.r_[padded-K@(dims/2),R.T@point+dims/2,-R.T@point+dims/2]
    center=linprog(np.zeros(3),A_ub=ca,b_ub=cb,bounds=[(None,None)]*3,method='highs')
    if not center.success:return None
    return {'center':center.x.tolist(),'size':dims.tolist(),'rotation':list(rot)}

def fit_patch(point,rot,face):
    R=matrix(rot);K=np.abs(A@R);M=np.hstack([A,K]);axis=int(np.argmax(K[face]))
    P=np.vstack([np.c_[R.T,-np.eye(3)],np.c_[-R.T,-np.eye(3)]])
    pb=np.r_[R.T@point,-R.T@point]
    cons=np.vstack([M,P]);rhs=np.r_[b,pb]
    bounds=[(-2,2),(29.48,33.8),(-1.9,1.4)]+[(.0625,2.5)]*3
    bounds[3+axis]=(.0625,.0625)
    seed=linprog(np.r_[np.zeros(3),-np.ones(3)],A_ub=cons,b_ub=rhs,A_eq=M[[face]],b_eq=b[[face]],bounds=bounds,method='highs')
    if not seed.success:return None
    others=np.arange(len(cons))!=face
    result=minimize(lambda v:-np.log(v[3:]).sum(),seed.x,jac=lambda v:np.r_[np.zeros(3),-1/v[3:]],bounds=bounds,
        constraints=[{'type':'ineq','fun':lambda v:rhs[others]-cons[others]@v,'jac':lambda v:-cons[others]},
        {'type':'eq','fun':lambda v:M[face]@v-b[face],'jac':lambda v:M[face]}],
        method='SLSQP',options={'ftol':1e-9,'maxiter':200})
    if not result.success:return None
    dims=np.maximum(.125,np.floor(result.x[3:]*16+1e-6)/8)
    ca=np.vstack([A,R.T,-R.T]);cb=np.r_[b-K@(dims/2),R.T@point+dims/2,-R.T@point+dims/2]
    center=linprog(np.zeros(3),A_ub=ca,b_ub=cb,A_eq=A[[face]],b_eq=(b-K@(dims/2))[[face]],bounds=[(None,None)]*3,method='highs')
    if not center.success:return None
    return {'center':center.x.tolist(),'size':dims.tolist(),'rotation':list(rot)}

if __name__=='__main__':main()
