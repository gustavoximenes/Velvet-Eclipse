import numpy as np
import math

def matrix(v):
 x,y,z=np.radians(v);cx,sx=np.cos(x),np.sin(x);cy,sy=np.cos(y),np.sin(y);cz,sz=np.cos(z),np.sin(z)
 return np.array([[cz,-sz,0],[sz,cz,0],[0,0,1]])@np.array([[cy,0,sy],[0,1,0],[-sy,0,cy]])@np.array([[1,0,0],[0,cx,-sx],[0,sx,cx]])

def euler(r):
 y=math.asin(np.clip(-r[2,0],-1,1))
 if abs(math.cos(y))>1e-6:x=math.atan2(r[2,1],r[2,2]);z=math.atan2(r[1,0],r[0,0])
 else:x=0;z=math.atan2(-r[0,1],r[1,1])
 return np.degrees([x,y,z])

def aim_down(v):
 v=np.array(v,dtype=float);v/=np.linalg.norm(v)
 start=np.array([0.,-1.,0.]);axis=np.cross(start,v);s=np.linalg.norm(axis);c=float(start@v)
 if s<1e-8:return np.eye(3) if c>0 else matrix([0,0,180])
 x,y,z=axis;k=np.array([[0,-z,y],[z,0,-x],[-y,x,0]])
 return np.eye(3)+k+k@k*((1-c)/(s*s))

def ik(start,end,pole,l1,l2,hinge=False):
 start=np.array(start,float);end=np.array(end,float);pole=np.array(pole,float)
 v=end-start;dist=np.linalg.norm(v);n=v/dist;dist=min(l1+l2-.005,max(abs(l1-l2)+.005,dist));end=start+n*dist
 a=(l1*l1-l2*l2+dist*dist)/(2*dist);h=math.sqrt(max(0,l1*l1-a*a))
 side=pole-start;side-=n*(side@n);side/=np.linalg.norm(side)
 elbow=start+n*a+side*h;r1=aim_down(elbow-start);r2=aim_down(end-elbow)
 if hinge:
  # Both arm bones share the elbow hinge axis. This prevents artificial
  # forearm roll while the hands track the rigid lyre.
  normal=np.cross(n,side);normal*=(-1 if start[0]<0 else 1);normal/=np.linalg.norm(normal)
  def frame(direction):
   y=-direction/np.linalg.norm(direction);x=np.cross(y,normal);x/=np.linalg.norm(x)
   return np.column_stack([x,y,normal])
  r1=frame(elbow-start);r2=frame(end-elbow)
 return euler(r1),euler(r1.T@r2),r2,end
