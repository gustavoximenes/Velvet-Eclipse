"""Rotating a textured leaf must preserve its local UVs across world-cell clipping."""
from pathlib import Path
import importlib.util
import numpy as np
path=Path(__file__).resolve().parents[1]/'city_polish/export.py'
spec=importlib.util.spec_from_file_location('export_uv',path);e=importlib.util.module_from_spec(spec);spec.loader.exec_module(e)
original=e.lock_uv(e.cuboid([.23,.03,.11],[1.85,2.77,.21],11))
checks=0
for angle in [-90,17,90,180]:
 a=np.radians(angle);c,s=np.cos(a),np.sin(a);m=np.array([[c,0,-s],[0,1,0],[s,0,c]])
 transformed=original.copy();transformed[:,:,:3]=original[:,:,:3]@m.T;transformed[:,:,3:6]=original[:,:,3:6]@m.T
 _,tiles,_,_=e.bake_geometry('dorm',dict(solids=[]),transformed)
 for key,vertices in tiles.items():
  v=np.array(vertices).reshape(-1,13);points=(v[:,:3]+key)@m;normals=v[:,3:6]@m
  for p,n,uv in zip(points,normals,v[:,11:]):
   axis=int(np.argmax(abs(n)));u=2 if axis==0 else 0;w=2 if axis==1 else 1
   delta=np.array([p[u],p[w]])-uv
   assert np.max(abs(delta-np.round(delta)))<1e-5,(angle,key,p,uv)
   assert np.min(uv)>-1e-5 and np.max(uv)<1.00001
   checks+=1
print('PASS rotating door UV:',checks,'vertices; four rotations including an oblique angle')
