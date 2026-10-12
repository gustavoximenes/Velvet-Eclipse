"""Deterministic approval model; never writes to mod sources, packs or worlds."""
from pathlib import Path
import json,math,hashlib
import numpy as np
PI=math.pi
IVORY=(.71,.78,.77);BLUE=(.024,.13,.38);DARK=(.027,.045,.075);GREEN=(.035,.105,.088);GOLD=(.55,.37,.105);PALE=(.40,.53,.48);STONE=(.09,.14,.23);GLASS=(.16,.74,.27);CYAN=(.20,.80,.96)
class Scene:
 def __init__(self,name):self.name=name;self.verts=[];self.lights=[];self.cutPortal=False;self.solids=[]
 def quad(self,p,col,mat=0,emit=0,normal=None):
  p=np.asarray(p,float);n=np.cross(p[1]-p[0],p[2]-p[0]) if normal is None else np.array(normal,float);n/=max(1e-8,np.linalg.norm(n))
  if self.cutPortal:
   # Clip clock ornament triangles around the real opening instead of covering it with a decal.
   def clip(poly,axis,bound,greater):
    out=[]
    for a,b in zip(poly,poly[1:]+poly[:1]):
     ina=(a[axis]>=bound) if greater else (a[axis]<=bound);inb=(b[axis]>=bound) if greater else (b[axis]<=bound)
     if ina:out.append(a)
     if ina!=inb:out.append(a+(b-a)*((bound-a[axis])/(b[axis]-a[axis])))
    return out
   for inds in [(0,1,2),(0,2,3)]:
    tri=[p[i] for i in inds]
    pieces=[clip(tri,0,-2.5,False),clip(tri,0,2.5,True)]
    middle=clip(clip(tri,0,-2.5,True),0,2.5,False);pieces += [clip(middle,1,22.1,True),clip(middle,1,16.95,False)]
    for poly in pieces:
     for j in range(1,len(poly)-1):
      for v in [poly[0],poly[j],poly[j+1]]:self.verts.append([*v,*n,*col,mat,emit])
  else:
   for i in [0,1,2,0,2,3]:self.verts.append([*p[i],*n,*col,mat,emit])
 def box(self,x,y,z,w,h,d,col,mat=0,emit=0):
  self.solids.append(dict(start=len(self.verts),lo=[x,y,z],hi=[x+w,y+h,z+d],angle=0,origin=[0,0,0]))
  a,b,c=x+w,y+h,z+d
  for p,n in [([(x,y,z),(a,y,z),(a,b,z),(x,b,z)],(0,0,-1)), ([(x,y,c),(x,b,c),(a,b,c),(a,y,c)],(0,0,1)), ([(x,b,z),(a,b,z),(a,b,c),(x,b,c)],(0,1,0)), ([(x,y,z),(x,y,c),(a,y,c),(a,y,z)],(0,-1,0)), ([(x,y,z),(x,b,z),(x,b,c),(x,y,c)],(-1,0,0)), ([(a,y,z),(a,y,c),(a,b,c),(a,b,z)],(1,0,0))]:self.quad(p,col,mat,emit,n)
 def beam(self,a,b,r,col,mat=0,segments=6):
  a=np.array(a,float);b=np.array(b,float);n=b-a;n/=np.linalg.norm(n);u=np.cross(n,(0,1,0) if abs(n[1])<.9 else (1,0,0));u/=np.linalg.norm(u);v=np.cross(n,u)
  for i in range(segments):
   aa=2*PI*i/segments;ab=2*PI*(i+1)/segments;q=(math.cos(aa)*u+math.sin(aa)*v)*r;t=(math.cos(ab)*u+math.sin(ab)*v)*r
   self.quad([a+q,b+q,b+t,a+t],col,mat,normal=q+t)
 def ring(self,center,r,width,col,plane='xy',mat=3,emit=0,segments=64,start=0,end=2*PI):
  x,y,z=center
  def p(a,rr):return (x+math.cos(a)*rr,y+math.sin(a)*rr,z) if plane=='xy' else (x+math.cos(a)*rr,y,z+math.sin(a)*rr)
  for i in range(segments):
   a=start+(end-start)*i/segments;b=start+(end-start)*(i+1)/segments
   self.quad([p(a,r),p(b,r),p(b,r-width),p(a,r-width)],col,mat,emit,(0,0,1) if plane=='xy' else (0,1,0))
 def disk(self,c,r,col,plane='xy',mat=0,emit=0,n=48):self.ring(c,r,r,col,plane,mat,emit,n)
 def pillar(self,x,z,height=24,r=1.25,base=1,white=True):
  color=IVORY if white else (.15,.21,.29)
  self.box(x-r-.4,base,z-r-.4,2*r+.8,.4,2*r+.8,color,1)
  self.box(x-r-.22,base+.4,z-r-.22,2*r+.44,.5,2*r+.44,color,1)
  self.beam((x,base+.9,z),(x,height+base-1,z),r,color,1,16)
  for j in range(16):
   a=j*PI/8;xx=x+math.cos(a)*(r+.015);zz=z+math.sin(a)*(r+.015)
   self.beam((xx,base+1.2,zz),(xx,base+height-1.25,zz),.035,(.35,.47,.54),0,4)
  self.box(x-r-.3,base+height-1,z-r-.3,2*r+.6,.45,2*r+.6,color,1)
  self.box(x-r-.5,base+height-.55,z-r-.5,2*r+1,.4,2*r+1,GOLD if white else PALE,3)
 def lamp(self,x,y,z):
  self.beam((x,y+2,z),(x,y+.55,z),.045,GOLD,3)
  self.beam((x,y-.7,z),(x,y+.5,z),.34,CYAN,6,6)
  for i in range(6):
   a=i*PI/3;self.beam((x+math.cos(a)*.36,y-.75,z+math.sin(a)*.36),(x+math.cos(a)*.36,y+.55,z+math.sin(a)*.36),.038,GOLD,3,4)
  self.lights.append([x,y,z,.33,.74,1.0,11])
