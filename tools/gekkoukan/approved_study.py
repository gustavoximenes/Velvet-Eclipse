"""Separate, offline interior studies. No Minecraft assets or worlds are written."""
from pathlib import Path
import sys,json,math,shutil,gzip
import numpy as np
from PIL import Image,ImageDraw,ImageFont
HERE=Path(__file__).parent;ROOT=HERE.parents[1];PREV=HERE
sys.path.insert(0,str(PREV))
from scene import Scene
OUT=HERE/'approved'
WALL=(.81,.84,.77);CREAM=(.92,.91,.83);GREEN=(.29,.46,.40);METAL=(.24,.32,.32);WOOD=(.55,.37,.23);PALE=(.67,.73,.69);INK=(.075,.10,.12);WHITE=(.93,.94,.87);BLUE=(.32,.53,.59)
SCENES=[]

def text(s,t,x,y,z,width,col=INK):
    f=ImageFont.truetype('C:/Windows/Fonts/arialbd.ttf',24);bb=f.getbbox(t);im=Image.new('L',(bb[2]+2,29));ImageDraw.Draw(im).text((1,-bb[1]),t,font=f,fill=255);a=np.array(im)>110;k=width/im.width
    for row in range(im.height):
        start=None
        for i in range(im.width+1):
            on=i<im.width and a[row,i]
            if on and start is None:start=i
            if not on and start is not None:
                s.quad([(x+(start-im.width/2)*k,y+(im.height-row)*k,z),(x+(i-im.width/2)*k,y+(im.height-row)*k,z),(x+(i-im.width/2)*k,y+(im.height-row-1)*k,z),(x+(start-im.width/2)*k,y+(im.height-row-1)*k,z)],col);start=None

def room(id,w=12,d=12,h=4.5,floor='tile',windows=True):
    s=Scene(id);s.w=w;s.d=d;s.h=h;s.props=[];b=s.box
    b(-w/2,-.2,-d/2,w,.2,d,(.61,.45,.29) if floor=='wood' else (.63,.72,.66),18 if floor=='wood' else 4)
    if floor!='wood':
        for x in np.arange(-w/2,w/2,1):b(x,.001,-d/2,.013,.004,d,(.51,.59,.54))
        for z in np.arange(-d/2,d/2,1):b(-w/2,.001,z,w,.004,.013,(.51,.59,.54))
    b(-w/2,0,-d/2,w,h,.24,WALL);b(w/2-.24,0,-d/2,.24,h,d,WALL)
    if windows:
        b(-w/2,0,-d/2,.24,1.1,d,WALL);b(-w/2,3.5,-d/2,.24,h-3.5,d,WALL)
        b(-w/2+.08,1.1,-d/2,.08,2.4,d,(.49,.68,.70))
        for z in np.arange(-d/2,d/2+.01,2):b(-w/2+.13,1.1,z,.16,2.4,.07,METAL)
        b(-w/2+.18,2.3,-d/2,.08,.06,d,METAL)
    else:b(-w/2,0,-d/2,.24,h,d,WALL)
    # A real 2-block aperture remains in the front wall. Door leaf is omitted for this study.
    doorX=w/2-3.5;front=d/2-.24
    b(-w/2,0,front,doorX+w/2-.08,h,.24,WALL);b(doorX+2.08,0,front,w/2-doorX-2.08,h,.24,WALL);b(doorX-.08,2.88,front,2.16,h-2.88,.24,WALL)
    for x in [doorX-.08,doorX+2]:b(x,0,front-.08,.08,2.88,.4,METAL)
    b(doorX-.08,2.8,front-.08,2.16,.08,.4,METAL)
    s.door=[doorX,0,front,doorX+2,2.8,d/2]
    # Reserve an entry strip in front of the aperture; audited independently of furniture.
    s.entry=[doorX,0,front-1.5,doorX+2,2.8,front]
    b(-w/2,h,-d/2,w,.18,d,CREAM)
    b(-w/2+.25,0,-d/2+.25,w-.5,1.0,.10,(.66,.72,.63));b(-w/2+.25,1.,-d/2+.25,w-.5,.08,.14,PALE)
    for z in [-d*.25,d*.2]:
        for x in [-w*.22,w*.22]:b(x-1.2,h-.08,z,2.4,.08,.3,WHITE,0,.2)
    s.lights=[[0,h-.25,0,.98,.97,.84,6],[-w/2+1,3,0,.80,.94,1,5]]
    return s

def prop(s,kind,x,z,w,d,h):s.props.append({'kind':kind,'bounds':[x,0,z,x+w,h,z+d]})
def table(s,x,z,w=2,d=1.2,h=1,col=CREAM):
    b=s.box;b(x,h,z,w,.1,d,col,2 if col==WOOD else 0)
    for xx in [x+.1,x+w-.16]:
        for zz in [z+.1,z+d-.16]:b(xx,0,zz,.06,h,.06,METAL)
    prop(s,'mesa',x,z,w,d,h+.1)
def chair(s,x,z,back=1,col=GREEN):
    b=s.box;b(x-.31,.53,z-.30,.62,.10,.60,col);b(x-.31,.65,z+back*.29-.04,.62,.6,.08,col)
    for xx in [-.26,.20]:
        for zz in [-.25,.19]:b(x+xx,0,z+zz,.06,.54,.06,METAL)
    for dx in [-.26,.20]:b(x+dx,.53,z+back*.29-.04,.06,.23,.08,METAL)
    prop(s,'assento',x-.32,z-.34,.64,.68,1.25)
def stool(s,x,z):
    s.box(x-.25,.65,z-.25,.5,.08,.5,WOOD,2)
    for dx in [-.20,.16]:
        for dz in [-.20,.16]:s.box(x+dx,0,z+dz,.04,.65,.04,METAL)
    prop(s,'banco',x-.25,z-.25,.5,.5,.73)
def cabinet(s,x,z,w=2,h=2.7,col=PALE,books=False):
    if 0<=z-(-s.d/2+.24)<.65:z=-s.d/2+.25
    b=s.box;d=.60;b(x,0,z,w,h,d,col,2 if books else 0)
    for yy in np.arange(.15,h-.3,.55):
        if books:
            b(x+.06,yy,z+.61,w-.12,.07,.11,WOOD)
            for j,xx in enumerate(np.arange(x+.10,x+w-.1,.15)):
                colors=[(.37,.48,.55),(.61,.38,.26),(.64,.60,.43),(.35,.46,.34),(.63,.62,.65)]
                hh=.29+(j%3)*.045;b(xx,yy+.08,z+.605,.11,hh,.08,colors[j%5])
        else:
            b(x+.02,yy,z+.601,w-.04,.015,.015,METAL)
            for xx in np.arange(x+.25,x+w,.7):b(xx,yy+.12,z+.63,.08,.025,.04,METAL)
    prop(s,'estante' if books else 'armario',x,z,w,.72,h)
def papers(s,x,y,z):
    s.box(x,y,z,.55,.045,.4,WHITE)
    for dz in [.08,.15,.22,.29]:s.box(x+.07,y+.048,z+dz,.34,.005,.008,(.45,.48,.43))
def board(s,x,z,w=4,y=1.5,h=1.5,col=GREEN,label=''):
    z=-s.d/2+.28
    s.box(x-.08,y-.08,z-.04,w+.16,h+.16,.12,WOOD);s.box(x,y,z+.079,w,h,.02,col)
    if label:text(s,label,x+w/2,y+h-.42,z+.101,w*.70,CREAM if col==GREEN else INK)
def monitor(s,x,y,z):
    b=s.box;b(x-.38,y+.16,z,.76,.52,.08,INK);b(x-.34,y+.2,z+.081,.68,.44,.008,(.21,.39,.42));b(x-.04,y,z,.08,.18,.08,INK);b(x-.22,y,z-.12,.44,.04,.36,INK)
def tap(s,x,y,z):
    s.box(x,y,z,.05,.32,.05,METAL);s.box(x,y+.30,z,.05,.05,.24,METAL);s.box(x,y+.2,z+.20,.05,.13,.05,METAL)
def sink(s,x,z):
    b=s.box;b(x,0,z,1,1,.72,PALE);b(x-.04,1,z-.04,1.08,.10,.80,WHITE);b(x+.13,1.10,z+.16,.65,.008,.45,(.32,.44,.45));tap(s,x+.45,1.10,z+.05)
    prop(s,'pia',x-.04,z-.04,1.08,.8,1.5)
def bottle(s,x,y,z,color=BLUE):
    s.box(x-.10,y,z-.10,.20,.30,.20,color);s.box(x-.05,y+.30,z-.05,.10,.12,.10,color);s.box(x-.06,y+.42,z-.06,.12,.04,.12,WHITE)
def finish(s,eye=None,target=None,fov=84):
    defaultEye=[s.w/2-.9,2.65,s.d/2-.85];defaultTarget=[-s.w*.10,1.9,-s.d*.20]
    (OUT/(s.name+'.bin.gz')).write_bytes(gzip.compress(np.array(s.verts,dtype='<f4').tobytes(),mtime=0))
    meta={'vertexCount':len(s.verts),'key':[-30,35,22],'center':[0,s.h/2,0],'lights':s.lights,'views':[{'id':s.name,'eye':eye or defaultEye,'target':target or defaultTarget,'fov':fov}],'shots':[],'roomSize':[s.w,s.h,s.d],'door':s.door,'entryReserve':s.entry,'props':s.props,'solids':[{'lo':v['lo'],'hi':v['hi']} for v in s.solids],'status':'offline visual study, layout proposed'}
    (OUT/(s.name+'.json.gz')).write_bytes(gzip.compress(json.dumps(meta,ensure_ascii=False,separators=(',',':'),default=float).encode(),mtime=0));SCENES.append(s.name)
    print(s.name,len(s.verts)//3,'triangles')

def library():
    s=room('biblioteca',16,15,floor='wood')
    for x in [-7,-4.2,-1.4,1.4,4.2]:cabinet(s,x,-7,2.55,3.25,WOOD,True)
    for x in [-6.6,-2.1]:
        for z in [-3,1]:
            table(s,x,z,3.0,1.5,col=CREAM)
            for xx in [x+.7,x+2.3]:chair(s,xx,z-.55,-1);chair(s,xx,z+2.05,1)
            papers(s,x+.35,1.11,z+.3);papers(s,x+1.9,1.11,z+.6)
    for z in [-4.9,-1.5,1.9]:cabinet(s,4.0,z,2.5,2.3,WOOD,True)
    text(s,'LIBRARY',0,3.58,-7.24,2.6)
    finish(s,eye=[5.6,2.7,6.2],target=[-.8,1.8,-3])
def faculty():
    s=room('professores',14,13)
    for x in [-6,-3.5,3.6]:cabinet(s,x,-5.9,2.15,2.6)
    board(s,-.6,-6.13,3.5,col=WHITE,label='FACULTY')
    for x in [-5.1,.2]:
        for z in [-3.4,.2,3.4]:
            table(s,x,z,3,1.25);chair(s,x+1.4,z+1.8);papers(s,x+.2,1.11,z+.3);monitor(s,x+2.3,1.1,z+.15)
            s.box(x+.25,1.1,z+.8,.6,.28,.25,BLUE)
    finish(s,eye=[5.7,2.6,5.55],target=[-1.4,1.8,-2.6],fov=90)
def nurse():
    s=room('enfermaria',12,12)
    for x in [-5,1.3]:
        s.box(x,.48,-4.8,2,.32,3.4,WHITE);s.box(x+.08,.80,-4.7,1.84,.12,3.2,(.76,.87,.84));s.box(x+.27,.94,-4.5,1.45,.16,.65,WHITE)
        for dx in [.1,1.8]:
            for dz in [-4.7,-1.6]:s.box(x+dx,0,dz,.1,.5,.1,METAL)
        s.box(x,1.05,-4.92,2,.65,.07,PALE)
        for dx in [.1,1.8]:s.box(x+dx,.48,-4.92,.1,.62,.19,METAL)
        prop(s,'maca',x,-4.92,2,3.6,1.7)
    s.box(-2.55,3.5,-5.2,.055,.06,4.5,PALE)
    for z in [-5.15,-.8]:s.box(-2.55,3.55,z,.055,.95,.055,PALE)
    for i in range(9):s.box(-2.56+(i%2)*.09,.3,-5.15+i*.11,.05,3.17,.13,(.67,.82,.80))
    cabinet(s,3.65,-5.6,1.9,2.85);board(s,-1.8,-5.62,2.65,col=WHITE,label='SCHEDULE')
    table(s,-4.6,1.2,3.3,1.25);chair(s,-3,3);papers(s,-4.2,1.11,1.5)
    for x in [-2.0,-1.6]:bottle(s,x,1.11,1.5)
    sink(s,3.7,-1.2);table(s,.2,-.5,1.3,.7,.85)
    for x in [.4,.8,1.2]:bottle(s,x,.96,-.15)
    finish(s)
def council():
    s=room('conselho',12,12,floor='wood')
    cabinet(s,-5.4,-5.65,3,2.7,WOOD,True);board(s,-1.9,-5.64,5.8,label='STUDENT COUNCIL')
    # Trophy shelf behind the meeting table, below a clear display wall.
    s.box(-5.4,2.9,-5.65,3,.12,.75,WOOD)
    for x in [-5,-4.1,-3.2]:
        s.box(x,3.02,-5.3,.35,.09,.35,INK);s.box(x+.13,3.11,-5.17,.09,.22,.09,(.69,.52,.21));s.box(x+.02,3.32,-5.25,.3,.24,.24,(.69,.52,.21))
    for x in [-3.8,-.6]:
        table(s,x,-1.9,2.9,2.8)
        for xx in [x+.65,x+2.15]:chair(s,xx,-2.5,-1);chair(s,xx,1.5);papers(s,xx-.2,1.11,-.7)
    cabinet(s,3.65,-1.8,1.85,1.7)
    finish(s)
def art():
    s=room('artes',14,14,floor='wood')
    cabinet(s,3.5,-6.7,2.7,2.7,WOOD,True);board(s,-4.9,-6.62,6.5,col=(.66,.65,.51),label='ART CLUB')
    for x in [-4.8,-.8,3.2]:
        for z in [-2.7,1.5]:
            for xx in [-.5,.45]:s.box(x+xx,0,z,.06,2.4,.1,WOOD)
            s.box(x-.52,1.9,z,1.1,.10,.15,WOOD);s.box(x-.46,.9,z-.02,1,1.05,.09,CREAM)
            s.box(x-.16,1.10,z+.08,.4,.5,.015,(.59,.61,.57));s.box(x-.57,.83,z-.05,1.2,.08,.3,WOOD);stool(s,x,z+1)
            prop(s,'cavalete',x-.6,z-.07,1.2,.4,2.4)
    for x in [-3.5,-.5,2.5]:
        table(s,x,-5.4,1.1,.85,.7);s.box(x+.22,.81,-5.2,.62,.32,.48,WHITE);s.box(x+.39,1.12,-5.1,.3,.24,.3,WHITE);s.box(x+.28,1.33,-5.17,.52,.52,.45,WHITE)
    finish(s,eye=[5.4,2.9,6],target=[0,1.8,-3.5])
def homeec():
    s=room('costura',14,13,floor='wood')
    cabinet(s,3,-5.9,3.2,2.8,WOOD,True);board(s,-5.5,-6.1,5.4,col=(.72,.65,.55),label='HOME ECONOMICS')
    for x in [-5,-.5]:
        for z in [-3,1.1]:
            table(s,x,z,3.2,1.45,col=WOOD)
            for xx in [x+.4,x+2.0]:
                b=s.box;b(xx,1.12,z+.3,.85,.08,.42,WHITE);b(xx+.55,1.2,z+.32,.18,.43,.30,WHITE);b(xx+.10,1.55,z+.32,.55,.13,.30,WHITE);b(xx+.08,1.30,z+.44,.045,.25,.04,METAL);b(xx+.6,1.68,z+.4,.06,.13,.06,(.63,.31,.27))
                chair(s,xx+.3,z+1.95);b(xx,1.11,z+.84,.83,.012,.48,(.55,.61,.70))
    for x in [-5.7,-3.8]:
        s.box(x,0,-5.35,.6,.07,.6,METAL);s.box(x+.27,.07,-5.08,.06,1.2,.06,METAL);s.box(x+.03,1.2,-5.3,.55,.4,.42,(.76,.65,.54));s.box(x+.10,1.6,-5.28,.4,.35,.38,(.76,.65,.54));s.box(x-.07,1.95,-5.3,.72,.28,.42,(.76,.65,.54));s.box(x+.22,2.23,-5.16,.15,.17,.15,(.76,.65,.54))
    finish(s)
def music():
    s=room('musica',14,14,floor='wood');b=s.box
    board(s,-5.5,-6.6,6.8,label='MUSIC')
    for yy in [1.8,2.25,2.7]:
        for n in range(5):b(-5.25,yy+n*.055,-6.47,6.25,.014,.018,CREAM)
    cabinet(s,3.6,-6.6,2.6,2.3,WOOD,True)
    # Stepped grand piano silhouette, keyboard, raised lid and three legs.
    for x,z,w,d in [(-5.3,-3.9,3.4,1.2),(-5.3,-2.7,3.0,1.0),(-5.3,-1.7,2.3,.8)]:b(x,.92,z,w,.45,d,INK)
    for x,z in [(-5.1,-3.7),(-2.2,-3.7),(-4.2,-1.2)]:b(x,0,z,.18,1,.18,INK)
    b(-5.3,1.12,-.94,2.3,.1,.42,CREAM)
    for i in range(22):
        xx=-5.3+i*.104;b(xx,1.221,-.91,.006,.01,.37,INK)
        if i%7 in [0,1,3,4,5]:b(xx+.068,1.225,-.92,.045,.045,.22,INK)
    b(-5.2,1.72,-3.8,2.9,.07,2.2,INK);b(-4.1,1.37,-3,.07,.35,.07,INK);table(s,-4.8,.1,1.6,.55,.55,col=INK)
    for x in [-.5,2.4,5.0]:
        for z in [-3.3,.1,3.5]:
            chair(s,x,z);b(x-.04,0,z-.65,.08,1.2,.08,METAL);b(x-.43,1.2,z-.68,.86,.46,.06,INK);papers(s,x-.3,1.665,z-.68)
    finish(s,eye=[5.7,2.65,6.0],target=[-1.4,1.6,-1.7])
def lab(science=False):
    s=room('ciencias' if science else 'laboratorio',14,14)
    for x in [-5.8,3.8]:cabinet(s,x,-6.65,2.1,2.8)
    board(s,-3.1,-6.64,6.0,label='SCIENCE' if science else 'LABORATORY')
    for x in [-5.2,.8]:
        for z in [-3.8,.7]:
            table(s,x,z,3.8,1.7,1.02,CREAM if science else INK)
            for xx in [x+.65,x+2.95]:stool(s,xx,z+2.4)
            if science:
                papers(s,x+.3,1.13,z+.6)
                # Teaching models: orbit rings, specimen tray, microscope silhouette.
                s.box(x+2.5,1.13,z+.65,.6,.10,.45,INK);s.box(x+2.88,1.23,z+.83,.13,.45,.13,PALE);s.box(x+2.65,1.66,z+.83,.35,.10,.13,PALE)
            else:
                s.box(x+.4,1.13,z+.6,.7,.012,.65,(.32,.48,.47));tap(s,x+.66,1.14,z+.38)
                for xx,col in [(x+1.7,BLUE),(x+2.3,(.61,.48,.57)),(x+2.9,(.65,.71,.37))]:bottle(s,xx,1.13,z+.7,col)
    finish(s)
def broadcast():
    s=room('transmissao',10,10,windows=False,floor='wood');b=s.box
    for x in np.arange(-4.5,4.6,1.2):b(x,1.3,-4.65,.85,2.2,.16,(.38,.45,.45))
    table(s,-4,-2.3,5.6,1.3,col=WOOD);chair(s,-2.8,-.3);chair(s,-.2,-.3)
    b(-3.7,1.11,-2.13,2.4,.10,.92,INK)
    for i in range(10):
        x=-3.56+i*.21;b(x,1.215,-1.95,.015,.008,.52,PALE);b(x-.03,1.23,-1.72,.08,.04,.06,WHITE)
    for x in [-2.7,.3]:
        b(x,1.11,-2.0,.08,.4,.08,INK);s.beam((x+.04,1.5,-1.96),(x+.35,1.7,-1.55),.028,INK,0,4);b(x+.24,1.65,-1.59,.20,.12,.2,INK)
    monitor(s,.4,1.11,-2);cabinet(s,2.65,-4.5,1.5,3,INK)
    for y in np.arange(.35,2.7,.4):b(2.78,y,-3.86,1.20,.12,.025,PALE)
    text(s,'PA ROOM',-.7,3.82,-4.69,2.4);finish(s)
def printing():
    s=room('impressao',10,10,windows=False);b=s.box
    for x in [-4,-1.5,1]:cabinet(s,x,-4.6,2.1,2.7)
    for x in [-3.5,0]:
        b(x,0,-1.6,2,1.4,1.3,PALE);b(x-.05,1.4,-1.65,2.1,.15,1.4,WHITE);b(x+.2,1.56,-1.4,1.5,.05,.85,INK);b(x+.15,1.62,-1.44,1.6,.08,.95,PALE);b(x+1.4,1.58,-.35,.40,.12,.30,BLUE)
        prop(s,'copiadora',x-.05,-1.65,2.1,1.4,1.75)
    table(s,-4.3,2.0,3.2,1.1)
    for x in [-4,-3,-2]:papers(s,x,1.11,2.4)
    text(s,'PRINTING',0,3.5,-4.65,2.7);finish(s)
def administration():
    s=room('secretaria',12,11);b=s.box
    cabinet(s,-5,-5.1,4,2.8);cabinet(s,2.2,-5.1,2.9,2.8)
    table(s,-4,-1.5,6.6,1.35);monitor(s,-2,1.1,-1.2);papers(s,0,1.11,-1.0)
    chair(s,-2.1,-2.2,-1);chair(s,.7,-2.2,-1)
    for x in [-4,-2.7,-1.4]:chair(s,x,2.2)
    board(s,0,-5.1,1.8,col=WHITE,label='OFFICE');finish(s)
def counselling():
    s=room('orientacao',10,10,floor='wood')
    cabinet(s,-4.5,-4.5,3.0,2.65,WOOD,True);board(s,0,-4.64,3.6,col=WHITE,label='GUIDANCE')
    table(s,-2,-1.8,3.4,1.3,col=WOOD);chair(s,-.3,-2.4,-1);chair(s,-.9,.2);chair(s,.4,.2)
    papers(s,-1.7,1.11,-1.4)
    s.box(2.3,0,-3.8,1.6,1.2,.6,PALE);finish(s)
def washroom():
    s=room('sanitarios',12,11,windows=False);b=s.box
    for x in [-5,-3.6,-2.2]:
        sink(s,x,-4.5);b(x+.03,1.6,-4.53,.94,1.2,.04,(.65,.79,.81))
    for x in [-.2,1.8,3.8]:
        b(x,0,-5.25,.09,2.9,4.0,PALE);b(x+.09,0,-1.32,1.65,2.8,.08,(.48,.63,.58));b(x+1.32,1.25,-1.22,.05,.18,.07,INK)
        b(x+.65,0,-4.1,.65,.45,.65,WHITE);b(x+.61,.45,-4.15,.73,.13,.83,WHITE);b(x+.60,.5,-4.55,.75,.8,.25,WHITE)
    b(5.7,0,-5.25,.09,2.9,4,PALE)
    text(s,'WC',-2.9,3.3,-5.1,1.0);finish(s,eye=[4.4,2.6,4.5],target=[-1.4,1.4,-3.0])
def shop():
    s=room('loja-armarios',14,12,windows=False);b=s.box
    for x in [-5.9,-3.7,-1.5]:
        cabinet(s,x,-5.65,2,2.3)
        for xx in np.arange(x+.49,x+2,.5):b(xx,0,-5.02,.02,2.3,.02,METAL)
    cabinet(s,2.2,-5.5,3.5,2.7,WOOD,True)
    table(s,1.6,-2.7,4.5,1.1,1.12,col=WOOD);b(1.6,.2,-2.7,4.5,.9,1.1,(.49,.41,.31))
    for x in [2.0,2.8,3.6,4.4]:papers(s,x,1.23,-2.4)
    text(s,'SCHOOL STORE',3.8,3.3,-5.23,3.2)
    for x in [-4,-1]:table(s,x,.8,2,.6,.5,col=WOOD)
    finish(s)
def storage():
    s=room('deposito',10,10,windows=False);b=s.box
    for x in [-4.3,-1.6,1.1]:
        cabinet(s,x,-4.6,2.25,3,PALE,True)
        for z in [-1.3,1.5]:
            b(x,0,z,1.0,.85,.75,(.62,.53,.37),2);b(x,.85,z+.04,.95,.09,.66,WOOD)
    text(s,'SUPPLIES',0,3.55,-4.68,2.9);finish(s)
def changing():
    s=room('vestiarios',14,12,windows=False);b=s.box
    for x in np.arange(-6.4,5.4,1.3):
        cabinet(s,x,-5.6,1.16,2.65,(.46,.58,.61))
        for yy in [2.1,2.2,2.3]:b(x+.18,yy,-4.98,.8,.025,.018,INK)
    for x in [-5.5,.0]:table(s,x,-1.7,4,.65,.5,col=WOOD)
    for x in [-5.7,-4.1]:sink(s,x,2.2)
    b(4.9,0,-3.3,.10,2.85,4.2,PALE)
    text(s,'CHANGING',0,3.4,-5.7,3);finish(s)
def gym():
    s=room('ginasio',26,32,10.5,floor='wood',windows=False);b=s.box
    # Marked court, stage and raised windows: proportions are a connected-campus adaptation.
    for x in [-10,10]:b(x,.014,-12,.045,.006,24,WHITE)
    for z in [-12,0,12]:b(-10,.014,z,20,.006,.045,WHITE)
    s.ring((0,.025,0),2.9,.055,WHITE,'xz')
    for z,sgn in [(-12,1),(12,-1)]:
        for x in [-2.6,2.6]:b(x,.014,z if sgn==1 else z-4.6,.045,.006,4.6,WHITE)
        b(-2.6,.014,z+sgn*4.6,5.2,.006,.045,WHITE)
        b(-1.35,4.8,z-sgn*.8,2.7,1.6,.10,WHITE);s.ring((0,4.5,z+sgn*.35),.52,.06,(.68,.28,.17),'xz')
    b(-10,0,-15.6,20,.70,2.3,WOOD,2)
    for x in [-11,10.5]:
        b(x,.7,-15.7,.5,6.3,2.5,(.33,.30,.40))
    for z in np.arange(-13,14,4):
        for x in [-12.72,12.6]:b(x,6,z,.10,2.7,2.8,(.43,.61,.64))
        b(-12.5,9.9,z,25,.22,.16,PALE)
    text(s,'GEKKOUKAN',0,7.4,-15.6,8.0)
    finish(s,eye=[10.8,3.0,14.4],target=[-1.0,3,-5.5],fov=82)
def corridor():
    s=room('corredor-escada',12,22,10.5,windows=False);b=s.box
    # Independent circulation module: two flights rise to a real opening in an upper slab.
    # Low walls are omitted where classroom doors will connect; this is a section study.
    for x in [-5.5,-2.8,.1]:
        cabinet(s,x,-10.55,2.4,2.6,PALE)
    for i in range(12):
        y=(i+1)*.25;z=4.2-i*.38;b(-5.2,0,z,2.6,y,.38,PALE)
        if i%2==0:b(-5.35,y,z,.07,1.1,.07,METAL)
    b(-5.2,2.8,-2.0,5.5,.20,1.9,PALE)
    for i in range(12):
        y=3+(i+1)*.25;z=-.1+i*.38;b(-2.35,3,z,2.6,y-3,.38,PALE)
        if i%2==0:b(.3,y,z,.07,1.1,.07,METAL)
    s.beam((-5.32,1.35,4.25),(-5.32,4.1,-.04),.045,METAL,0,4);s.beam((.33,4.35,-.1),(.33,7.1,4.4),.045,METAL,0,4)
    b(.4,5.80,-10.7,5.2,.20,21.4,PALE)
    for z in [-6,-1,5]:
        b(5.70,1.3,z,.02,1.65,2.5,(.36,.51,.59))
        for j in range(5):b(5.67,1.5+j*.22,z+.2,.03,.05,1.7,CREAM)
    text(s,'2F  /  CLASSROOMS',0,3.7,-10.70,5.4)
    finish(s,eye=[4.8,2.4,9.5],target=[-1.8,2.8,0],fov=82)
def roof():
    s=room('cobertura',24,24,4.5,windows=False);s.verts=[];s.solids=[];s.lights=[];b=s.box
    b(-12,-.3,-12,24,.3,24,(.60,.67,.65),4)
    for z in [-11.6,11.6]:
        b(-11.6,0,z,23.2,.65,.25,PALE)
        for x in np.arange(-11.4,11.6,.8):b(x,.65,z+.08,.04,1.2,.04,METAL)
        b(-11.6,1.85,z+.08,23.2,.04,.04,METAL)
    for x in [-11.6,11.6]:
        b(x,0,-11.6,.25,.65,23.2,PALE)
        for z in np.arange(-11.4,11.6,.8):b(x+.08,.65,z,.04,1.2,.04,METAL)
        b(x+.08,1.85,-11.6,.04,.04,23.2,METAL)
    # Stair headhouse with 2 x 2.8 aperture; rear block is clear of the door.
    b(-8,0,-10,6,4,.25,WALL);b(-8,0,-10,.25,4,5,WALL);b(-2.25,0,-10,.25,4,5,WALL)
    b(-8,0,-5,2,4,.25,WALL);b(-4,0,-5,2,4,.25,WALL);b(-6,2.8,-5,2,1.2,.25,WALL);b(-8.2,4,-10.2,6.4,.25,5.4,CREAM)
    s.door=[-6,0,-5,-4,2.8,-4.75];s.entry=[-6,0,-4.75,-4,2.8,-3.25]
    for x in [-8,2]:
        for z in [1,7]:table(s,x,z,3,.7,.5,col=WOOD)
    for x in [4,7]:
        b(x,0,-9,2.2,1.8,2.2,PALE)
        for zz in np.arange(-8.8,-7,.15):b(x+.1,1.82,zz,2,.04,.05,METAL)
    text(s,'ROOF ACCESS',-5,3.15,-4.73,3)
    finish(s,eye=[9,4,10],target=[-2,1.5,-2],fov=85)

if __name__=='__main__':
    for fn in [library,faculty,nurse,council,art,homeec,music,lab,lambda:lab(True),broadcast,printing,administration,counselling,washroom,shop,storage,changing,gym,corridor,roof]:fn()
