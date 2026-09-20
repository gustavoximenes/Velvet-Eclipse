"""Solve a forward reaching key pose without stretching the arm segments."""
import numpy as np

def align(a,b):
    a=np.array(a,dtype=float);b=np.array(b,dtype=float)
    a/=np.linalg.norm(a);b/=np.linalg.norm(b)
    v=np.cross(a,b);c=float(a@b)
    k=np.array([[0,-v[2],v[1]],[v[2],0,-v[0]],[-v[1],v[0],0]])
    return np.eye(3)+k+k@k/(1+c)

def arm_rotations(sign,euler):
    shoulder=np.array((sign*4.05,21,0))
    elbow=np.array((sign*5.8,12.6,.4))
    wrist=np.array((sign*7.25,4.85,-1))
    target_elbow=np.array((sign*6.5,16,-5))
    target_wrist=np.array((sign*1.5,21 if sign<0 else 18,-11))
    upper=align(elbow-shoulder,target_elbow-shoulder)
    actual_elbow=shoulder+upper@(elbow-shoulder)
    lower=align(wrist-elbow,upper.T@(target_wrist-actual_elbow))
    angle=np.radians(50 if sign<0 else 70)
    hand_world=np.array([[1,0,0],[0,np.cos(angle),-np.sin(angle)],[0,np.sin(angle),np.cos(angle)]])
    hand=(upper@lower).T@hand_world
    return euler(upper),euler(lower),euler(hand)
