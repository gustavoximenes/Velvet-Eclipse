"""Revision 02: a new Priestess head and interwoven, articulated braids.

All surfaces remain native Box UV planes/cuboids. The faceted face is authored
from scratch here, with a narrow jaw and chin, rather than the prior box head.
"""
import math
import numpy as np
from PIL import Image, ImageDraw


def build_head(bone,cube,segment,atlas,face_uv,palette,matrix_euler):
    palette.update({
        'face_white':['#97a8a6','#becbc3','#dae3d4','#f0eedb'],
        'face_black':['#15232b','#273b42','#3b5054','#587076'],
        'hair_light':['#627c81','#91a6a6','#bccdc6','#e7eadd'],
        'hair_dark':['#10222b','#233b43','#39555b','#668084'],
    })

    def panel(group,name,points,material,detail=None):
        """Planar polygon cut out of a native integer-size Box UV rectangle."""
        pts=np.array(points,dtype=float);anchor=pts[0]
        u=pts[1]-anchor;u/=np.linalg.norm(u)
        v=pts[2]-anchor;v-=u*float(u@v);v/=np.linalg.norm(v)
        n=np.cross(u,v);m=np.column_stack((u,v,n))
        # A non-planar quad must be split; projecting its fourth corner would
        # open a crack between neighbouring pieces of the face or hair.
        if len(pts)>3 and max(abs((p-anchor)@n) for p in pts[3:])>1e-6:
            for i in range(1,len(pts)-1):
                panel(group,name+'_'+str(i),[pts[0],pts[i],pts[i+1]],material,detail)
            return
        xy=np.array([[(p-anchor)@u,(p-anchor)@v] for p in pts])
        lo=xy.min(axis=0);hi=xy.max(axis=0)
        width=math.ceil((hi[0]-lo[0])*4)/4;height=math.ceil((hi[1]-lo[1])*4)/4
        origin=anchor+np.array((lo[0],lo[1],0))
        co=cube(group,name,tuple(origin),(width,height,0),material,rotation=matrix_euler(m),pivot=tuple(anchor))
        polygon=[((x-lo[0])*4,(height-y+lo[1])*4) for x,y in xy]
        col=palette[material]
        for face,(x,y,w,h) in face_uv(*co['uv'],*co['size']).items():
            if not w or not h:continue
            patch=Image.new('RGBA',(w,h));d=ImageDraw.Draw(patch)
            # No dark cube borders on skin: the actual face planes define volume.
            d.polygon(polygon,fill=col[2])
            d.line(polygon+[polygon[0]],fill=col[2],width=2)
            if detail=='hair':
                for xx in range(3,w-1,5):
                    d.line((xx,1,xx-1,h-2),fill=col[1])
                mask=Image.new('L',(w,h));md=ImageDraw.Draw(mask)
                md.polygon(polygon,fill=255);md.line(polygon+[polygon[0]],fill=255,width=2)
                patch.putalpha(mask)
            if face=='south':patch=patch.transpose(Image.Transpose.FLIP_LEFT_RIGHT)
            atlas.paste(patch,(x,y))
        return co

    bone('head','neck',(0,24,0))
    # Each level changes the outline deliberately: slim chin, cheeks, temples,
    # then a lower, sloping crown. There are no square jaw or forehead blocks.
    levels=[(24.8,.8,.64,-.9),(25.4,1.45,1.16,-.75),
            (26.65,2.15,1.72,-.55),(28.45,2.55,2.04,-.3),
            (29.75,2.3,1.84,-.05),(30.8,1.3,1.04,.1)]
    # A five-edge half-profile keeps the face broad at the cheeks, while the
    # three-quarter silhouette tapers behind the temples.
    profile=[(0,-1),(.72,-1),(1,-.5),(.92,.6),(0,1)]
    for sign,label,mat in [(-1,'black','face_black'),(1,'white','face_white')]:
        def ring(level):
            y,w,d,cz=level
            return [(sign*x*w,y,cz+z*d) for x,z in profile]
        for i,(lower,upper) in enumerate(zip(levels,levels[1:])):
            low,high=ring(lower),ring(upper)
            for j in range(len(profile)-1):
                panel('head',f'{label}_face_plane_{i}_{j}',[low[j],low[j+1],high[j+1],high[j]],mat)
        low=ring(levels[0]);top=ring(levels[-1])
        panel('head',label+'_under_chin',[low[0],low[1],low[2],low[3],low[4]],mat)
        panel('head',label+'_top_of_head',[top[0],top[1],top[2],top[3],top[4]],mat)
        # Sleek parted hair follows the skull. No tall square antennae.
        hair='hair_light' if sign<0 else 'hair_dark'
        bone('hair_cap_'+label,'head',(0,29,0))
        lower=[(sign*x*2.75,29.5,-.05+z*2.2) for x,z in profile]
        upper=[(sign*x*1.55,31.05,.1+z*1.24) for x,z in profile]
        for i in range(4):
            panel('hair_cap_'+label,f'{label}_swept_crown_{i}',
                  [lower[i],lower[i+1],upper[i+1],upper[i]],hair,'hair')
        panel('hair_cap_'+label,label+'_crown_top',upper,hair)
        # A closed pointed crest on each side. The plane triangle cuts share
        # vertices so there is no doubled strip or see-through split at the tip.
        crest=[(sign*1.8,29.7,.45),(sign*2.7,29.8,.5),(sign*2.48,32.15,1.35)]
        back=[(x,y,z+.4) for x,y,z in crest]
        panel('hair_cap_'+label,label+'_swept_crest_front',crest,hair)
        panel('hair_cap_'+label,label+'_swept_crest_back',back,hair)
        for edge in range(3):
            nxt=(edge+1)%3
            panel('hair_cap_'+label,label+'_crest_edge_'+str(edge),
                  [crest[edge],crest[nxt],back[nxt],back[edge]],hair)

        # Two interwoven strands form each visible braid. Their alternating depth
        # places one strand over the other at each crossing, including in profile.
        def center(k):
            return np.array((sign*(2.38+.1*min(k,5)-.09*max(0,k-5)),26.65-k*.98,-1.95-.17*min(k,5)+.04*max(0,k-5)))
        root=tuple(center(0));bone('hair_'+label,'head',root)
        bone('hair_'+label+'_lower','hair_'+label,tuple(center(4)))
        bone('hair_'+label+'_tip','hair_'+label+'_lower',tuple(center(8)))
        segment('hair_'+label,label+'_temple_lock',(sign*2.25,28.2,-1.25),root,.75,.75,hair,'braid')
        for k in range(8):
            owner='hair_'+label+('_lower' if k>=4 else '')
            ca,cb=center(k),center(k+1)
            for strand in (-1,1):
                direction=strand*(-1 if k%2 else 1)
                a=ca+np.array((direction*.27,0,.09*strand))
                b=cb+np.array((-direction*.27,0,-.09*strand))
                middle=(a+b)/2+np.array((0,0,-.24 if strand==1 else .18))
                segment(owner,f'{label}_braid_{k}_{strand}_upper',tuple(a),tuple(middle),.5,.5,hair)
                segment(owner,f'{label}_braid_{k}_{strand}_lower',tuple(middle),tuple(b),.5,.5,hair)
        end=center(8)
        cube('hair_'+label+'_tip',label+'_braid_clasp',(end[0]-.5,end[1]-.25,end[2]-.4),(1,.5,.75),'gold')
        segment('hair_'+label+'_tip',label+'_tapered_tassel',tuple(end+np.array((0,-.3,0))),tuple(end+np.array((-sign*.1,-1.1,-.08))),.5,.5,hair,'braid')
        segment('hair_'+label+'_tip',label+'_fine_tassel',tuple(end+np.array((-sign*.1,-1.05,-.08))),tuple(end+np.array((-sign*.25,-1.8,-.15))),.25,.25,hair)

    bone('face_mask','head',(0,28,-2.75))
    for sign,label in [(-1,'black'),(1,'white')]:
        co=cube('face_mask',label+'_slender_mask',(-3.75 if sign<0 else 0,26.85,-2.8),
                (3.75,2.75,0),'mask',rotation=(0,-sign*11,0),pivot=(0,28,-2.8))
        for face,(x,y,w,h) in face_uv(*co['uv'],*co['size']).items():
            if not w or not h:continue
            patch=Image.new('RGBA',(w,h));d=ImageDraw.Draw(patch)
            d.polygon([(0,5),(4,3),(8,3),(14,0),(13,5),(10,8),(5,10),(0,9)],fill='#ac465f')
            d.line((0,5,4,3,8,3,14,0),fill='#de8390')
            d.line((0,9,5,10,10,8,13,5),fill='#672c48')
            # Small elongated eyes with a fine upper lash, rather than goggle eyes.
            d.polygon([(3,5),(7,4),(11,3),(9,6),(6,7),(3,6)],fill='#362535')
            d.line((4,5,8,5,9,4),fill='#ded7aa');d.point((6,5),fill='#523b50')
            d.point((10,3),fill='#311b30');d.line((1,7,3,8),fill='#d3697b')
            if sign<0:patch=patch.transpose(Image.Transpose.FLIP_LEFT_RIGHT)
            if face=='south':patch=patch.transpose(Image.Transpose.FLIP_LEFT_RIGHT)
            atlas.paste(patch,(x,y))
    # Narrow tapered mask bridge; the small nose uses two solid half-cuboids.
    panel('face_mask','tapered_mask_bridge',[(-.45,27.4,-2.95),(.45,27.4,-2.95),(.3,28.4,-2.87),(-.3,28.4,-2.87)],'mask')
    for sign,label,mat in [(-1,'black','face_black'),(1,'white','face_white')]:
        co=cube('head',label+'_small_nose',(-.25 if sign<0 else 0,26.4,-2.65),(.25,.75,.5),mat)
        for face,(x,y,w,h) in face_uv(*co['uv'],*co['size']).items():
            if w and h:atlas.paste(Image.new('RGBA',(w,h),palette[mat][2 if face=='north' else 1]),(x,y))
    # Cupid's bow and a single lower-lip highlight: smaller than the old mouth.
    co=cube('head','defined_small_lips',(-.75,25.5,-2.06),(1.5,.5,0),'face_white',rotation=(-12,0,0),pivot=(0,25.5,-2.06))
    for face,(x,y,w,h) in face_uv(*co['uv'],*co['size']).items():
        if not w or not h:continue
        patch=Image.new('RGBA',(w,h));d=ImageDraw.Draw(patch)
        d.line((1,0,4,0),fill='#533749')
        d.line((2,1,3,1),fill='#9d6477')
        if face=='south':patch=patch.transpose(Image.Transpose.FLIP_LEFT_RIGHT)
        atlas.paste(patch,(x,y))
