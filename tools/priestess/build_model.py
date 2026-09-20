"""Standalone Priestess study: native Box UV cuboids/planes, never runtime files."""
from pathlib import Path
import base64, json, math, uuid
import numpy as np
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parent
OUT = ROOT / 'draft'
DENSITY = 4
S = 1024
P = {
    'ivory': ['#677d81','#a4b7b6','#d5ded3','#edf1de'],
    'obsidian': ['#101922','#1e2c36','#34474d','#506267'],
    'cloth_light': ['#506975','#899fa5','#becfc9','#e0e5d3'],
    'cloth_dark': ['#0c1824','#1c303d','#2e4752','#49646a'],
    'paper_light': ['#7c8c80','#abb6a0','#d6dbc0','#eef0d0'],
    'paper_dark': ['#111c20','#26352f','#405046','#60705a'],
    'mask': ['#472534','#7f304b','#b74c65','#dc7a85'],
    'gold': ['#534431','#89733e','#bfa15c','#ead58b'],
}

def face_uv(u,v,w,h,d):
    return {'north':(u+d,v+d,w,h), 'south':(u+2*d+w,v+d,w,h),
            'west':(u+d+w,v+d,d,h),'east':(u,v+d,d,h),
            'up':(u+d,v,w,d),'down':(u+d+w,v,w,d)}

def paint(w,h,mat,face,pattern=''):
    col=P[mat]; im=Image.new('RGBA',(w,h),col[2 if face in ('north','up') else 1]); d=ImageDraw.Draw(im)
    if min(w,h)>2:
        d.line((0,0,w-1,0),fill=col[3]);d.line((0,1,0,h-1),fill=col[1])
        d.line((w-1,1,w-1,h-1),fill=col[0]);d.line((1,h-1,w-1,h-1),fill=col[1])
    if pattern.startswith('paper') and face in ('north','south'):
        dark=mat.endswith('dark'); ink='#a0b49a' if dark else '#788872'
        # Deliberately drawn invented script, running along the length of each ribbon.
        # No noise filter, photographed text or borrowed texture.
        for lane,x in enumerate(range(4,w-3,4)):
            for y in range(4+lane%2,h-3,6):
                k=(y//6+lane*3+int(pattern.split('_')[-1]))%5
                d.line((x,y,x,y+3),fill=ink)
                d.line((x,y+1,x+2,y+1),fill=ink)
                if k!=1:d.point((x+1,y+3),fill=ink)
                if k in (0,3):d.line((x-1,y+2,x-1,y+4),fill=ink)
        d.line((1,1,1,h-2),fill=col[1]);d.line((w-3,1,w-3,h-2),fill=col[3])
    if pattern=='braid' and face in ('north','south','east','west'):
        for y in range(0,h,6):
            d.polygon([(0,y),(w//2,y+3),(w-1,y),(w-1,y+2),(w//2,y+5),(0,y+2)],fill=col[1])
            d.line((1,y+1,w//2,y+3),fill=col[3])
    if pattern=='cloth' and face in ('north','south'):
        for x in [w//4,3*w//4]:
            d.line((x,1,max(1,x-w//7),h-2),fill=col[1],width=2)
            d.line((x+1,1,max(2,x-w//7+1),h-2),fill=col[3])
    if pattern=='cuff' and face in ('north','east','west','south'):
        for yy in [h//4,3*h//4]:
            d.line((0,yy,w//2,yy+2,w-1,yy),fill='#b43744',width=2)
        if w>5:d.polygon([(w//2,1),(w-2,h//2),(w//2,h-2),(1,h//2)],outline='#b43744')
    if pattern=='B' and face=='north':
        x=max(1,w//2-2);y=h//3
        d.line((x,y,x,y+9),fill=P['ivory'][2]);d.line((x,y,x+3,y,x+4,y+2,x+3,y+4,x,y+4),fill=P['ivory'][2])
        d.line((x,y+4,x+4,y+5,x+4,y+8,x+3,y+9,x,y+9),fill=P['ivory'][2])
    if pattern=='J' and face=='north':
        x=max(1,w//2-2);y=h//3;ink=P['obsidian'][1]
        d.line((x,y,x+5,y),fill=ink);d.line((x+4,y,x+4,y+8,x+3,y+9,x+1,y+9,x,y+7),fill=ink)
    if pattern=='clasp' and face in ('north','south'):
        im=Image.new('RGBA',(w,h));d=ImageDraw.Draw(im)
        d.polygon([(2,0),(w-3,0),(w-1,2),(w-1,h-3),(w-3,h-1),(2,h-1),(0,h-3),(0,2)],fill=col[1])
        d.rectangle((2,2,w-3,h-3),outline=col[3]);d.line((3,h//2,w-4,h//2),fill=col[0])
        d.rectangle((w//2-1,2,w//2+1,h-3),fill=col[2])
    return im

def matrix_euler(m):
    ry=math.asin(float(np.clip(-m[2,0],-1,1)))
    if abs(math.cos(ry))>1e-7:rx=math.atan2(m[2,1],m[2,2]);rz=math.atan2(m[1,0],m[0,0])
    else:rx=0;rz=math.atan2(-m[0,1],m[1,1])
    return [math.degrees(q) for q in (rx,ry,rz)]

def build():
    OUT.mkdir(parents=True,exist_ok=True)
    atlas=Image.new('RGBA',(S,S));bones=[];parts=[];bx=by=2;row=0
    def bone(name,parent=None,pivot=(0,0,0)):
        b={'name':name,'pivot':[round(v*DENSITY,6) for v in pivot]}
        if parent:b['parent']=parent
        bones.append(b);return name
    def cube(group,name,origin,size,mat,pattern='',rotation=None,pivot=None):
        nonlocal bx,by,row
        w,h,d=[round(v*DENSITY) for v in size]
        assert all(abs(v*DENSITY-n)<1e-5 for v,n in zip(size,[w,h,d])),name
        assert min(w,h,d)>=0 and sum(n==0 for n in (w,h,d))<=1,name
        aw,ah=2*(w+d),h+d
        if bx+aw+2>S:bx=2;by+=row+2;row=0
        assert by+ah+2<S,'Atlas full'
        uv=[bx,by];bx+=aw+2;row=max(row,ah)
        for f,(u,v,fw,fh) in face_uv(*uv,w,h,d).items():
            if fw and fh:atlas.paste(paint(fw,fh,mat,f,pattern),(u,v))
        c={'origin':[round(n*DENSITY,6) for n in origin],'size':[w,h,d],'uv':uv}
        if rotation:c.update(rotation=list(rotation),pivot=[round(n*DENSITY,6) for n in (pivot or origin)])
        next(b for b in bones if b['name']==group).setdefault('cubes',[]).append(c)
        parts.append({'name':name,'bone':group,'material':mat,'pattern':pattern,'cube':c})
        return c
    def segment(group,name,a,b,width,depth,mat,pattern=''):
        dx,dy,dz=[v-u for u,v in zip(a,b)];length=math.sqrt(dx*dx+dy*dy+dz*dz)
        rot=(math.degrees(math.atan2(dz,math.hypot(dx,dy))),0,math.degrees(math.atan2(-dx,dy)))
        return cube(group,name,(a[0]-width/2,a[1],a[2]-depth/2),(width,math.ceil(length*4)/4,depth),mat,pattern,rot,a)
    def triangle(group,name,aa,bb,cc,mat):
        a,b,c=map(lambda p:np.array(p,dtype=float),(aa,bb,cc))
        ux=b-a;wd=np.linalg.norm(ux);ux/=wd;vy=c-a;projection=float(vy@ux);vy-=ux*projection;ht=np.linalg.norm(vy);vy/=ht
        nz=np.cross(ux,vy);rot=matrix_euler(np.column_stack((ux,vy,nz)))
        mn=min(0,projection);mx=max(wd,projection);ww=math.ceil((mx-mn)*4)/4;hh=math.ceil(ht*4)/4
        co=cube(group,name,tuple(a+np.array((mn,0,0))),(ww,hh,0),mat,rotation=rot,pivot=tuple(a))
        points=[(-mn*4,hh*4),((wd-mn)*4,hh*4),((projection-mn)*4,(hh-ht)*4)]
        # Native rectangular planes with a pixel-cut triangular silhouette.
        for face,(u,v,w,h) in face_uv(*co['uv'],*co['size']).items():
            if not w or not h:continue
            patch=paint(w,h,mat,face);d=ImageDraw.Draw(patch)
            for amount in [.25,.55,.8]:
                end=(points[1][0]*(1-amount)+points[2][0]*amount,points[1][1]*(1-amount)+points[2][1]*amount)
                d.line((*points[0],*end),fill=P[mat][1],width=2)
                d.line((points[0][0]+1,points[0][1],end[0]+1,end[1]),fill=P[mat][3])
            mask=Image.new('L',(w,h));ImageDraw.Draw(mask).polygon(points,fill=255)
            patch.putalpha(mask)
            if face=='south':patch=patch.transpose(Image.Transpose.FLIP_LEFT_RIGHT)
            atlas.paste(patch,(u,v))

    bone('priestess');bone('pelvis','priestess',(0,5,0))
    for sign,label,mat in [(-1,'black','obsidian'),(1,'white','ivory')]:
        x0=-4.5 if sign<0 else 0
        cube('pelvis',label+'_hip',(x0,4,-2),(4.5,4,4.5),mat)
        hip=(sign*3,6,-.5);knee=(sign*13.5,10,-6);ankle=(sign*23,1.8,-13)
        bone(label+'_thigh','pelvis',hip)
        segment(label+'_thigh',label+'_upper_leg',hip,knee,3.5,3.75,mat)
        bone(label+'_calf',label+'_thigh',knee)
        segment(label+'_calf',label+'_lower_leg',knee,ankle,2.5,2.75,mat)
        bone(label+'_foot',label+'_calf',ankle)
        cube(label+'_foot',label+'_foot',(sign*23-1.5,0,-16),(3,1.75,5),mat,rotation=(0,-sign*23,0),pivot=(sign*23,1,-13))
        cube(label+'_calf',label+'_anklet',(sign*23-1.375,1.5,-14.4),(2.75,.5,2.75),'gold')
        cloth='cloth_dark' if sign<0 else 'cloth_light'
        bone(label+'_skirt','pelvis',(sign*2,7,-1))
        points=[(sign*.25,7.5,-2.75),(sign*13.5,10.7,-6.25),(sign*23.5,.5,-4),
                (sign*13,.35,4),(sign*.25,.5,1)]
        for i in range(1,len(points)-1):triangle(label+'_skirt',label+'_drape_'+str(i),points[0],points[i],points[i+1],cloth)
        # A separate front fold connects the belt to the raised knee.
        triangle(label+'_skirt',label+'_front_fold',(sign*.25,7.5,-3),(sign*13.5,10.7,-6.5),(sign*8,1,-5),cloth)
    cube('pelvis','waist_clasp',(-1.5,7,-3.4),(3,1.75,.5),'gold','clasp')
    bone('body','pelvis',(0,8,0))
    for sign,label,mat in [(-1,'black','obsidian'),(1,'white','ivory')]:
        for name,width,y,height,depth,z in [('waist',2.5,8,5,3.25,-1.6),('ribcage',3,13,5,3.75,-1.9),('chest',3.75,18,4,4,-2)]:
            cube('body',label+'_'+name,(-width if sign<0 else 0,y,z),(width,height,depth),mat,'B' if label=='black' and name=='chest' else 'J' if name=='chest' else '')
    bone('neck','body',(0,22,0))
    cube('neck','black_neck',(-1.25,21.5,-1.1),(1.25,3.75,2.25),'obsidian')
    cube('neck','white_neck',(0,21.5,-1.1),(1.25,3.75,2.25),'ivory')
    from head_model import build_head
    build_head(bone,cube,segment,atlas,face_uv,P,matrix_euler)
    # The ribbons are jointed cuboids: long deliberate folds, not a smoothed mesh.
    bone('fan','head',(0,28,1.8))
    for sign,label,mat in [(-1,'light','paper_light'),(1,'dark','paper_dark')]:
        specifications=[(-23,40,3.5),(-7,43,4.0),(9,42,4.5),(25,38,4.0),(45,31,3.75),(69,23,3)]
        for i,(angle,length,width) in enumerate(specifications):
            theta=math.radians(angle+(2 if sign==1 and i%2 else 0))
            start=np.array((sign*2.65,28.6,1.8+i*.18))
            direction=np.array((sign*math.cos(theta),math.sin(theta),.055))
            # Three planar sections describe a large folded strip.
            first=start+direction*length*.28
            second=start+direction*length*.66+np.array((0,(-1)**i*1.7,-.8))
            end=start+direction*length+np.array((0,(-1)**(i+1)*2.5,1.7))
            for j,(a,b) in enumerate(zip([start,first,second],[first,second,end])):
                name=f'paper_{label}_{i}_{j}'
                parent='fan' if j==0 else f'paper_{label}_{i}_{j-1}'
                bone(name,parent,tuple(a));segment(name,name+'_strip',tuple(a),tuple(b),width,.25,mat,'paper_'+str(i))
    # Independent shoulders, elbows, wrists and fingers allow the forward attack.
    for sign,label,mat in [(-1,'black','obsidian'),(1,'white','ivory')]:
        shoulder=(sign*4.05,21,0);elbow=(sign*5.8,12.6,.4);wrist=(sign*7.25,4.85,-1)
        bone(label+'_upper_arm','body',shoulder)
        segment(label+'_upper_arm',label+'_upper_arm_shape',shoulder,elbow,1.75,2,mat)
        bone(label+'_forearm',label+'_upper_arm',elbow)
        segment(label+'_forearm',label+'_forearm_shape',elbow,wrist,1.25,1.5,mat,'cuff' if sign<0 else '')
        bone(label+'_hand',label+'_forearm',wrist)
        cube(label+'_hand',label+'_palm',(wrist[0]-1,2.6,-1.5),(2,2.25,.75),mat)
        for i in range(4):
            x=wrist[0]-.825+i*.55;y=2.7;tip=.4+abs(i-1.5)*.35
            name=label+'_finger_'+str(i);bone(name,label+'_hand',(x,y,-1.1))
            middle=(x+(i-1.5)*.17,1.45,-1.35);end=(x+(i-1.5)*.25,tip,-1.7)
            segment(name,name+'_base',(x,y,-1.1),middle,.5,.5,mat)
            segment(name,name+'_tip',middle,end,.5,.5,mat)
        segment(label+'_hand',label+'_thumb',(wrist[0]-sign*.85,3.8,-1.1),(wrist[0]-sign*1.7,2.6,-1.65),.5,.75,mat)

    duration=4.8;times=[round(i*duration/24,3) for i in range(25)]
    tracks={}
    def wave_track(name,amplitude,phase=0,axis=0):
        keys={}
        for t in times:
            v=[0.,0.,0.];v[axis]=round(amplitude*(math.sin(t*2*math.pi/duration+phase)-math.sin(phase)),5);keys[str(t)]=v
        keys[str(duration)]=keys['0.0'].copy();tracks[name]={'rotation':keys}
    wave_track('body',.35);wave_track('head',.55,axis=1)
    for label in ['light','dark']:
        for i in range(6):
            for j in range(3):wave_track(f'paper_{label}_{i}_{j}',.65+j*.4,i*.42+j*.28,2)
    for label,phase in [('black',.3),('white',1.1)]:
        wave_track('hair_'+label,.7,phase)
        wave_track('hair_'+label+'_lower',1.2,phase+.5)
        wave_track('hair_'+label+'_tip',1.65,phase+.9)
    # Strike timing is authored from the two still references, not motion capture.
    stamps=[0,.28,.7,1.12,1.35,1.8,2.25,2.8]
    weights=[0,-.12,.38,1,1,.76,.24,0]
    attack={}
    def attack_track(name,peak):
        attack[name]={'rotation':{str(t):[round(v*a,5) for v in peak] for t,a in zip(stamps,weights)}}
    attack_track('body',[5,0,0]);attack_track('head',[-5,0,0])
    from attack_pose import arm_rotations
    for sign,label in [(-1,'black'),(1,'white')]:
        for joint,angles in zip(['upper_arm','forearm','hand'],arm_rotations(sign,matrix_euler)):
            attack_track(label+'_'+joint,angles)
    for label in ['black','white']:
        for i in range(4):attack_track(label+'_finger_'+str(i),[8+(i%2)*5,0,(i-1.5)*9])
        attack_track('hair_'+label,[3,0,1 if label=='black' else -1])
        attack_track('hair_'+label+'_lower',[4,0,1.5 if label=='black' else -1.5])
        attack_track('hair_'+label+'_tip',[5,0,2 if label=='black' else -2])
    for label in ['light','dark']:
        for i in range(6):
            for j in range(3):attack_track(f'paper_{label}_{i}_{j}',[(-1 if label=='light' else 1)*(1+j*.75),0,(-1 if label=='light' else 1)*(1+j*.7)])
    animation={'format_version':'1.8.0','animations':{
        'misc.idle':{'loop':True,'animation_length':duration,'bones':tracks},
        'action.attack':{'loop':False,'animation_length':2.8,'bones':attack}}}
    geo={'format_version':'1.12.0','minecraft:geometry':[{'description':{'identifier':'geometry.priestess_study','texture_width':S,'texture_height':S,'visible_bounds_width':28,'visible_bounds_height':18,'visible_bounds_offset':[0,7,0]},'bones':bones}]}
    for filename,obj in [('priestess.geo.json',geo),('priestess.animation.json',animation),('parts.json',parts)]:
        (OUT/filename).write_text(json.dumps(obj,indent=2)+'\n',encoding='utf-8')
    atlas.save(OUT/'priestess.png')
    export_blockbench(bones,parts,animation)
    stats={'elements':len(parts),'bones':len(bones),'texture_size':S,'atlas_height_used':by+row,'density':DENSITY,'clips':{k:v['animation_length'] for k,v in animation['animations'].items()}}
    (OUT/'model-info.json').write_text(json.dumps(stats,indent=2)+'\n',encoding='utf-8')
    print(json.dumps(stats))

def export_blockbench(bones,parts,animation):
    def ident(name):return str(uuid.uuid5(uuid.NAMESPACE_URL,'standalone-study/priestess/'+name))
    elements=[]
    for part in parts:
        c=part['cube'];a=c['origin'];b=[x+y for x,y in zip(a,c['size'])]
        elements.append({'name':part['name'],'type':'cube','uuid':ident(part['name']),'from':a,'to':b,'origin':c.get('pivot',a),'rotation':c.get('rotation',[0,0,0]),'box_uv':True,'uv_offset':c['uv'],'autouv':0,'color':0,'faces':{f:{'uv':[u,v,u+w,v+h],'texture':0} for f,(u,v,w,h) in face_uv(*c['uv'],*c['size']).items()}})
    def group(b):
        return {'name':b['name'],'origin':b['pivot'],'rotation':b.get('rotation',[0,0,0]),'uuid':ident('bone/'+b['name']),'export':True,'isOpen':False,'children':[ident(p['name']) for p in parts if p['bone']==b['name']]+[group(child) for child in bones if child.get('parent')==b['name']]}
    project={'meta':{'format_version':'4.10','model_format':'bedrock','box_uv':True},'name':'priestess_study','model_identifier':'priestess_study','visible_box':[28,18,7],'resolution':{'width':S,'height':S},'elements':elements,'outliner':[group(b) for b in bones if 'parent' not in b], 'textures':[{'name':'priestess.png','id':'0','uuid':ident('texture'),'path':'priestess.png','source':'data:image/png;base64,'+base64.b64encode((OUT/'priestess.png').read_bytes()).decode(),'mode':'bitmap','saved':True}], 'animations':[]}
    for clip,anim in animation['animations'].items():
        animators={}
        for name,tracks in anim['bones'].items():
            animators[ident('bone/'+name)]={'name':name,'type':'bone','keyframes':[{'channel':channel,'data_points':[{'x':v[0],'y':v[1],'z':v[2]}],'time':float(t),'color':-1,'interpolation':'linear','uuid':ident(clip+name+channel+t)} for channel,keys in tracks.items() for t,v in keys.items()]}
        project['animations'].append({'uuid':ident('animation/'+clip),'name':clip,'loop':'loop' if anim['loop'] else 'once','length':anim['animation_length'],'animators':animators})
    (OUT/'priestess.bbmodel').write_text(json.dumps(project,indent=2)+'\n',encoding='utf-8')

if __name__=='__main__':build()
