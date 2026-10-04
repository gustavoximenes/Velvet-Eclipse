"""Small deliberate pixel palettes, including double-sided cutout fairy wings."""
from PIL import Image,ImageDraw
import numpy as np

PALETTE={
 'skin':['#a87778','#d8ad9f','#f2d1bc','#ffe5d1'],
 'skin_soft':['#ad767c','#d7979a','#edb7ab','#f8d0bc'],
 'blue':['#101243','#182170','#263994','#4566c1'],
 'blue_edge':['#19255c','#2c4996','#5481c6','#92badd'],
 'hair':['#291d29','#492b38','#683d46','#986563'],
 'sclera':['#999aab','#d4cfcb','#f6e9d7','#fff9eb'],
 'iris':['#273348','#455f83','#7598af','#b4d7e2'],
 'pupil':['#142139','#172641','#1e2b49','#2b3851'],
 'lash':['#352638','#422b3f','#51364c','#6e4b5a'],
 'lip':['#986373','#b87685','#cf969c','#eeb4b0'],
 'mouth':['#805463','#a1707d','#b08389','#d6a5a3'],
 'wing':['#458f9e','#6cbfbd','#a2dfd6','#e4f4d7'],
 'wing_edge':['#438e99','#7fbbb5','#c6e8cc','#edf6dc'],
}

def bezier(a,b,c,d,n=20):
    return [(1-t)**3*np.array(a)+3*(1-t)**2*t*np.array(b)+3*(1-t)*t*t*np.array(c)+t**3*np.array(d) for t in np.linspace(0,1,n)]

def wing(w,h,face,pattern):
    im=Image.new('RGBA',(w,h),(0,0,0,0))
    if face not in ('north','south'):return im
    draw=ImageDraw.Draw(im)
    flip=pattern.endswith('right') ^ (face=='south')
    def coords(points):return [(round((1-x if flip else x)*(w-1)),round(y*(h-1))) for x,y in points]
    if pattern.startswith('leaf'):
        outline=(bezier((.12,.99),(.01,.66),(.04,.30),(.56,.075))+
                 bezier((.56,.075),(.79,-.03),(1.05,.025),(.91,.125))+
                 bezier((.91,.125),(.95,.035),(.72,.04),(.69,.17))+
                 bezier((.69,.17),(.64,.49),(.92,.69),(.12,.99)))
        draw.polygon(coords(outline),fill=PALETTE['wing'][2])
        draw.polygon(coords(bezier((.12,.99),(.07,.61),(.20,.37),(.59,.19))+bezier((.59,.19),(.41,.55),(.58,.71),(.12,.99))),fill=PALETTE['wing'][1])
        draw.line(coords(outline+[outline[0]]),fill=PALETTE['wing_edge'][3],width=1)
        spine=bezier((.12,.98),(.21,.67),(.45,.43),(.64,.17))
        draw.line(coords(spine),fill=PALETTE['wing_edge'][3],width=1)
        for end,a in [((.2,.43),(.26,.69)),((.66,.52),(.30,.66)),((.57,.73),(.20,.82))]:
            draw.line(coords(bezier(a,(a[0]-.08,a[1]-.15),(end[0],end[1]+.09),end)),fill=PALETTE['wing_edge'][2],width=1)
    else:
        outline=(bezier((.03,.31),(.26,.09),(.67,.12),(.81,.30))+
                 bezier((.81,.30),(1.02,.54),(.89,.89),(.62,.86))+
                 bezier((.62,.86),(.81,.77),(.80,.62),(.69,.53))+
                 bezier((.69,.53),(.72,.77),(.42,.92),(.32,.71))+
                 bezier((.32,.71),(.63,.87),(.62,.53),(.03,.31)))
        draw.polygon(coords(outline),fill=PALETTE['wing'][2])
        draw.line(coords(outline+[outline[0]]),fill=PALETTE['wing_edge'][3],width=1)
        draw.line(coords(bezier((.05,.32),(.40,.28),(.68,.32),(.68,.54))),fill=PALETTE['wing'][0],width=1)
    return im

def paint(w,h,material,face,pattern):
    if material=='wing':return wing(w,h,face,pattern)
    if material=='eye':
        im=Image.new('RGBA',(w,h),'#e8e5d8');d=ImageDraw.Draw(im)
        if face=='north':
            cx=w//2+(0 if pattern.endswith('left') else -1)
            d.rectangle((cx-1,0,cx+1,h-1),fill='#7892a5')
            d.line((cx,1,cx,h-2),fill='#2c364a')
            d.point((cx-1,0),fill='#fff8e9')
            d.line((0,h-1,w-1,h-1),fill='#ddd2c5')
        return im
    p=PALETTE[material]
    tone={'north':2,'south':1,'west':1,'east':2,'up':3,'down':1}[face]
    if material.startswith('skin'):tone=2
    if material=='hair' and pattern:tone=1 if face in ('south','down') else 2
    im=Image.new('RGBA',(w,h),p[tone]);d=ImageDraw.Draw(im)
    if material in ('blue','blue_edge') and face in ('north','east','west') and min(w,h)>5:
        d.polygon([(max(1,w//5),1),(max(2,w//3),1),(max(2,w//3),h//2),(max(1,w//5),h-3)],fill=p[3])
        d.line((w-2,2,w-2,h-2),fill=p[1])
    if material=='hair' and pattern and min(w,h)>4:
        if face=='north':
            # Broad grouped pixels following the swept roll, without noisy stripes.
            sweep=[(round(x*(w-1)),round(y*(h-1))) for x,y in bezier((.02,.85),(.26,.47),(.58,.28),(.96,.24),16)]
            d.line(sweep,fill='#8e575b',width=1)
        elif face in ('south','east','west'):
            for f in (.28,.70):
                x=round(w*f)
                d.line((x,1,max(1,x-1),h-2),fill='#744552' if face!='south' else '#613c48',width=1)
    return im
