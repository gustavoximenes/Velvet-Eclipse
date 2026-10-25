"""Limited authored pixel palette, coherent markings across head facets."""
from PIL import Image
import numpy as np
from rig_math import matrix

PALETTE={
 'ivory':['#b4b7a0','#cccfb8','#e0e2c8','#eff0d9'],
 'jaw':['#747e80','#8b9490','#a3aaa0','#bec3b2'],
 'brown':['#35222c','#4c3039','#65404a','#81515a'],
 'tan':['#9b7551','#b48a60','#c6a176','#d6b78c'],
 'green':['#3d5145','#61734c','#8e9e60','#b4bd79'],
 'mouth':['#11131b','#191c24','#242832','#353a42'],
 'claw':['#4d5040','#747753','#969964','#bbba83']}

def paint(w,h,material,face,pattern):
    return Image.new('RGBA',(w,h),PALETTE[material][2])

def segment_distance(x,y,a,b):
    dx=b[0]-a[0];dy=b[1]-a[1]
    t=np.clip(((x-a[0])*dx+(y-a[1])*dy)/(dx*dx+dy*dy),0,1)
    return np.hypot(x-a[0]-t*dx,y-a[1]-t*dy)

def surface_pattern(im,cube,face,density,part):
    w,h=im.size;v,u=np.mgrid[:h,:w];u=(u+.5)/w;v=(v+.5)/h
    zero=np.zeros_like(u);one=np.ones_like(u)
    loc={'north':(u,1-v,zero),'south':(u,1-v,one),'west':(zero,1-v,u),
         'east':(one,1-v,u),'up':(u,one,v),'down':(u,zero,v)}[face]
    xyz=np.stack(loc,-1)*np.array(cube['size'])/density+np.array(cube['origin'])/density
    pivot=np.array(cube.get('pivot',[0,0,0]))/density
    xyz=(xyz-pivot)@matrix(cube.get('rotation',[0,0,0])).T+pivot
    x,y,z=xyz[:,:,0],xyz[:,:,1],xyz[:,:,2];a=np.array(im)
    feature=part['feature']
    if feature=='head':
        # Two closed three-stroke fans: the P3R reference has no visible iris.
        ex=(np.abs(x)-2.1)/1.35;ey=(y-15.35)/1.35
        eye=np.zeros_like(x,dtype=bool)
        strokes=[([(-.58,-.1),(-.57,.38),(-.32,.77),(.02,.92)],.13),
                 ([(-.58,-.1),(-.22,.18),(.15,.42),(.60,.48)],.115),
                 ([(-.58,-.1),(-.13,-.12),(.35,.07),(.64,.23)],.12)]
        for points,r in strokes:
            for p,q in zip(points,points[1:]):eye|=segment_distance(ex,ey,p,q)<r
        eye&=z < -3.4
        a[eye]=[65,72,84,255]
    elif feature in ('body','neck','leg','wing') and part['material']=='brown':
        # Large chest patch and ochre sides, no random noise or photo projection.
        front=(z<.2)
        bib=((x/1.22)**2+((y-10.15)/1.5)**2<1)&front
        shoulders=(y>10.85)&(np.abs(x)>.75)
        belly_sides=(np.abs(x)>1.3)&(y<9.7)&(y>7.8)&(z<1.2)
        if feature=='neck':bib=(z<.1)
        if feature=='wing':shoulders=(y>11.0)
        a[bib|shoulders|belly_sides]=[198,161,118,255]
        if feature=='body':
            for yy in (8.4,9.0,9.6):
                stripe=(np.abs(y-(yy-.35*(np.abs(x)-1.2)))<.07)&(np.abs(x)>1.1)&(z<.25)
                a[stripe]=[162,123,91,255]
    elif feature=='crest':
        # Long dark grooves painted within the native solids.
        stripe=(np.abs(x-.2*np.sin(y*.9))<.20)|(np.abs(np.abs(x)-.72)<.12)
        a[stripe]=[62,83,66,255]
    return Image.fromarray(a)
