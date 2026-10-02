"""Approved Tab-menu skeletal geometry; samples bind live skins at runtime, never preview portraits."""
from pathlib import Path
import sys,math,gzip,struct
import numpy as np
sys.dont_write_bytecode=True
ROOT=Path(__file__).resolve().parents[2]
sys.path.insert(0,str(ROOT/'tools/velvet_room'))
import choreography as c
import portrait_raster as r
r.W,r.H=440,640
skin=np.full((64,64,4),255,dtype=np.uint8)
slim=True;armw=3
c.b.player_skin=lambda:(skin,slim)
POSES={'main': {'head': (-10, -24, -8), 'arms': {-1: (18, -12, -10), 1: (138, 5, -18)}, 'legs': {-1: (12, 0, -4), 1: (-10, 0, 5)}, 'eye': (-1.5, 1.4, -6), 'target': (0, 1.05, 0), 'fov': 24, 'root': (0, 0, 0), 'hand': {1: (0.45, 1.98, -0.33)}}, 'skill': {'root': (0, -20, 0), 'head': (2, -35, 2), 'arms': {-1: (-12, -8, -8), 1: (-8, 6, 5)}, 'legs': {-1: (4, 0, -3), 1: (-6, 0, 4)}, 'hand': {-1: (-0.23, 0.91, -0.33)}, 'eye': (1.1, 1.3, -6), 'target': (0, 1.1, 0), 'fov': 25}, 'equip': {'root': (0, 55, 0), 'head': (4, 20, -6), 'arms': {-1: (-16, -4, -9), 1: (-10, 3, 5)}, 'legs': {-1: (6, 0, -4), 1: (-9, 0, 3)}, 'hand': {1: (0.32, 0.9, -0.29)}, 'eye': (-1.4, 1.25, -6), 'target': (0, 1.1, 0), 'fov': 25}, 'roster': {'head': (9, -18, -7), 'arms': {-1: (136, -12, 29), 1: (-5, 5, 9)}, 'legs': {-1: (0, 0, -3), 1: (0, 0, 3)}, 'eye': (1.4, 1.8, -5), 'target': (0, 1.45, 0), 'fov': 22, 'root': (0, 0, 0), 'hand': {-1: (-0.1, 1.98, -0.33)}}, 'status': {'head': (2, 30, -5), 'arms': {-1: (6, 0, -5), 1: (-8, 0, 7)}, 'legs': {-1: (0, 0, -3), 1: (0, 0, 3)}, 'eye': (1.7, 1.65, -5), 'target': (0, 1.63, 0), 'fov': 18, 'root': (0, 0, 0)}, 'system': {'head': (0, 14, -6), 'arms': {-1: (142, 8, 31), 1: (18, -4, -16)}, 'legs': {-1: (-21, 6, -12), 1: (17, 0, 10)}, 'eye': (1.7, 1.5, -6), 'target': (0, 1.12, 0), 'fov': 27, 'root': (0, 0, 0)}, 'item': {'root': (0, -32, 0), 'head': (-17, -33, -5), 'arms': {-1: (-16, 0, -8), 1: (-10, 0, 6)}, 'legs': {-1: (0, 0, 0), 1: (0, 0, 0)}, 'eye': (1.3, 1.62, -5), 'target': (0, 1.48, 0), 'fov': 23}}
def mesh(spec,t,name):
 faces=[];bob=math.sin(t);palms={}
 for layer in range(2):
  faces+=c.b.skin_cube((0,1.125,0),(8,12,4),(16,16) if not layer else (16,32),np.eye(4),layer*.009)
  angles=np.array(spec['head'])+np.array([bob*1.3,math.cos(t)*1.2,0])
  faces+=c.b.skin_cube((0,1.75,0),(8,8,8),(0,0) if not layer else (32,0),c.T((0,1.5,0))@c.R(angles)@c.T((0,-1.5,0)),layer*.009)
 for side in [-1,1]:
  leg=c.T((side*.125,.75,0))@c.R(np.array(spec['legs'][side])+[bob*1.2,0,0])
  arm=c.T((side*(.25+armw/32),1.43,0))@c.R(np.array(spec['arms'][side])+[bob*1.8,0,math.cos(t)*.7])
  for layer in range(2):
   uv=((0,16) if side<0 else (16,48)) if not layer else ((0,32) if side<0 else (0,48))
   faces+=c.b.skin_cube((0,-.375,0),(4,12,4),uv,leg,layer*.009)
  uv=(40,16) if side<0 else (32,48);overlay=(40,32) if side<0 else (48,48)
  if side in spec.get('hand',{}):
   # Fixed 6-pixel upper arm / forearm. The hands rest at the waist.
   shoulder=np.array([side*(.25+armw/32),1.43,0.])
   hand=np.array(spec['hand'][side])
   hand+=np.array([0,bob*.007,0])
   delta=hand-shoulder;axis=delta/np.linalg.norm(delta)
   pole=np.array([side*1.,-.25,.05]);pole-=axis*np.dot(pole,axis);pole/=np.linalg.norm(pole)
   elbow=(shoulder+hand)/2+pole*math.sqrt(max(0,.375**2-np.linalg.norm(delta)**2/4))
   palms[side]=hand+(elbow-hand)*.22
   faces+=c.b.skin_limb(shoulder,elbow,armw,uv,overlay,np.eye(4),0,6)
   faces+=c.b.skin_limb(elbow,hand,armw,uv,overlay,np.eye(4),6,12)
  else:
   faces+=c.b.skin_cube((0,-.375,0),(armw,12,4),uv,arm)
   faces+=c.b.skin_cube((0,-.375,0),(armw,12,4),overlay,arm,.009)
 if name=='skill':
  # Attach the actual native Evoker at the existing bent wrist; do not alter the arm pose.
  # Center of the rotated native grip, anchored inside the palm rather than at the wrist tip.
  grip=np.array([-.271,.055,0.])
  gun=c.T(palms[-1]+[0,0,-.012])@c.R((0,180,-10))@c.S(.105)@c.T(-grip)
  faces+=c.b.model('evoker_fx').faces(t,'misc.idle',gun)
 root=c.R(spec['root'])
 return [((root@np.c_[vertices,np.ones(len(vertices))].T).T[:,:3],uv,tex,color) for vertices,uv,tex,color in faces]
