"""Small depth-tested native cuboid rasterizer for offline card assets."""
import math
import numpy as np
from PIL import Image
W,H=256,384
class Camera:
 def __init__(self,eye,target,fov=44,roll=0):
  self.eye=np.array(eye,dtype=float);front=np.array(target)-self.eye;front/=np.linalg.norm(front)
  right=np.cross(front,[0,1,0]);right/=np.linalg.norm(right);up=np.cross(right,front)
  self.axes=np.stack([right,up,front]);self.f=H/(2*math.tan(math.radians(fov)/2));self.roll=math.radians(roll)
 def project(self,points):
  p=(np.array(points)-self.eye)@self.axes.T
  z=p[...,2];x=p[...,0]*self.f/np.maximum(.05,z);y=-p[...,1]*self.f/np.maximum(.05,z)
  c,s=math.cos(self.roll),math.sin(self.roll)
  return np.stack([W/2+c*x-s*y,H/2+s*x+c*y+getattr(self,"offset_y",0),z],axis=-1)

def raster(bg,faces,cam,accent=(55,190,235),brightness=1):
 rgb=np.array(bg.convert('RGB'),dtype=np.float32);depth=np.full((H,W),np.inf,dtype=np.float64)
 light=np.array([-.35,.7,-.6]);light/=np.linalg.norm(light)
 for q,uv,tex,color in faces:
  normal=np.cross(q[1]-q[0],q[2]-q[0]);length=np.linalg.norm(normal)
  if length<1e-9:continue
  normal/=length
  if np.dot(normal,cam.eye-q.mean(axis=0))<=0:continue
  p=cam.project(q)
  if p[:,2].min()<=.1:continue
  if p[:,0].max()<0 or p[:,0].min()>=W or p[:,1].max()<0 or p[:,1].min()>=H:continue
  shade=(.61+.39*max(0,float(normal@light)))*brightness
  rim=(1-abs(np.dot(normal,(cam.eye-q.mean(axis=0))/np.linalg.norm(cam.eye-q.mean(axis=0)))))**3*.12
  tint=np.array(accent)*rim
  for ids in ([0,1,2],[0,2,3]):
   tri=p[ids];a,b,c=tri
   x0=max(0,int(tri[:,0].min()));x1=min(W,int(tri[:,0].max())+1)
   y0=max(0,int(tri[:,1].min()));y1=min(H,int(tri[:,1].max())+1)
   if x0>=x1 or y0>=y1:continue
   den=(b[1]-c[1])*(a[0]-c[0])+(c[0]-b[0])*(a[1]-c[1])
   if abs(den)<1e-9:continue
   ys,xs=np.mgrid[y0:y1,x0:x1];xs=xs+.5;ys=ys+.5
   aa=((b[1]-c[1])*(xs-c[0])+(c[0]-b[0])*(ys-c[1]))/den
   bb=((c[1]-a[1])*(xs-c[0])+(a[0]-c[0])*(ys-c[1]))/den;cc=1-aa-bb
   orthographic=getattr(cam,'orthographic',False)
   inv=aa/a[2]+bb/b[2]+cc/c[2];z=aa*a[2]+bb*b[2]+cc*c[2] if orthographic else 1/np.maximum(inv,1e-9)
   mask=(aa>=0)&(bb>=0)&(cc>=0)&(z<depth[y0:y1,x0:x1])
   if not mask.any():continue
   if tex is not None:
    u=uv[ids];tx=aa*u[0,0]+bb*u[1,0]+cc*u[2,0] if orthographic else (aa*u[0,0]/a[2]+bb*u[1,0]/b[2]+cc*u[2,0]/c[2])*z
    ty=aa*u[0,1]+bb*u[1,1]+cc*u[2,1] if orthographic else (aa*u[0,1]/a[2]+bb*u[1,1]/b[2]+cc*u[2,1]/c[2])*z
    col=tex[np.clip(ty.astype(int),0,tex.shape[0]-1),np.clip(tx.astype(int),0,tex.shape[1]-1)]
    mask&=col[:,:,3]>90
    rgb[y0:y1,x0:x1][mask]=(col[:,:,:3]*shade+tint)[mask]
   else:rgb[y0:y1,x0:x1][mask]=color*shade+tint
   depth[y0:y1,x0:x1][mask]=z[mask]
 return Image.fromarray(np.uint8(np.clip(rgb,0,255)))
