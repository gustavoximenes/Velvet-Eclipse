"""Offline Photon 1.20.1 VFX study. Never writes src/ or a game installation."""
from pathlib import Path
import json, math, random, re, hashlib, shutil, subprocess, sys
import numpy as np
from PIL import Image, ImageDraw, ImageFont, ImageFilter, ImageChops

ROOT = Path(__file__).resolve().parents[3]
OUT = ROOT/'outputs/auditoria-vfx-photon-01'
DOC = ROOT/'velvet-eclipse/modeling/vfx/audit-01'
WORK = ROOT/'work/vfx-audit-01'
ORIGINAL = WORK/'original-assets/P3R/Content/Xrd777/Effects/Niagara/Skill'
FFMPEG = ROOT/'work/awakening-preview/deps/imageio_ffmpeg/binaries/ffmpeg-win-x86_64-v7.1.exe'
VIDEO = ROOT/'modeling/nekomata-study-02/work/reload-skills.mp4'
FPS, FRAMES, W, H = 20, 64, 512, 384
TAU = math.tau
def dump(path, data):
    path.parent.mkdir(parents=True,exist_ok=True)
    path.write_text(json.dumps(data,ensure_ascii=False,indent=2),encoding='utf-8')
def sha(p): return hashlib.sha256(p.read_bytes()).hexdigest()
def rgb(c): return tuple(int(c[i:i+2],16) for i in (1,3,5))
def font(n,bold=False): return ImageFont.truetype('C:/Windows/Fonts/'+('arialbd.ttf' if bold else 'arial.ttf'),n)
def sprite(tex, at, size, color='#ffffff', start=4, life=20, count=1, radius=0, speed=0, blend='add'):
    return dict(type='sprite',texture=tex,at=at,size=size,color=color,start=start,life=life,count=count,radius=radius,speed=speed,blend=blend)
def trail(points,color,width=.08,start=4,life=20,tail=6):
    return dict(type='trail',points=points,color=color,width=width,start=start,life=life,tail=tail)
def beam(a,b,color,width=.06,start=6,life=5):
    return dict(type='beam',a=a,b=b,color=color,width=width,start=start,life=life)
def helix(radius,height,turns=2,phase=0,n=65):
    return [[radius*math.cos(phase+TAU*turns*t),.12+height*t,radius*math.sin(phase+TAU*turns*t)] for t in np.linspace(0,1,n)]
def circle(radius,y=0,vertical=False,phase=0,n=49):
    return [[radius*math.cos(a),y+(radius*math.sin(a) if vertical else 0),0 if vertical else radius*math.sin(a)] for a in np.linspace(phase,phase+TAU,n)]
def spark(color,at=(0,.2,0),start=6,count=14,life=16,radius=.65,speed=.045):
    return sprite('spark',list(at),[.12,.18],color,start,life,count,radius,speed)

def recipes():
    rows=[]
    def add(id,name,group,source,desc,layers,reference=None,confidence='Estudo de forma; composição a refinar'):
        rows.append(dict(id=id,name=name,group=group,source=source,description=desc,layers=layers,referenceSeconds=reference,confidence=confidence,
                         anchor='alvo: pés, com altura relativa de 1,8 m',impactTick=6,seed=91,
                         durationTicks=max(x['start']+x['life']+x.get('tail',0) for x in layers),
                         photon='1.20.1 / 1.1.17',status='Prévia offline, não integrada'))
    observed='Forma e cor conferidas em trecho Reload; curvas e tempos propostos'
    add('agi','Agi','Elementais',10,'Explosão curta no alvo: núcleo escuro, línguas laranja, clarão e brasas. Sem gaiola de linhas.',[
        sprite('fire',[0,.65,0],[1.55,1.65],'#ffe5bc',6,16,3,.5,.01),
        sprite('smoke',[0,.63,0],[1.05,1.05],'#3f1018',7,14,2,.12,0,'alpha'),
        sprite('glow',[0,.15,0],[2.2,.45],'#ffad32',6,7),spark('#ffab39')],335.75,observed)
    z=[]; rng=random.Random(80); points=[[0,4.1,0]]+[[rng.uniform(-.24,.24),4.1-i*.39,rng.uniform(-.08,.08)] for i in range(1,11)]+[[0,.12,0]]
    for a,b in zip(points,points[1:]): z.append(beam(a,b,'#ce8dff',.085,6,5))
    z += [sprite('glow',[0,.15,0],[2.1,.45],'#dab3ff',6,7),spark('#dfb7ff',start=6,count=16,life=13)]
    add('zio','Zio','Elementais',40,'Raio violeta descendente, recortado e breve; impacto branco e faíscas baixas.',z,546.75,observed)
    ice=[sprite('ice',[math.cos(a)*.7,.3+i*.23,math.sin(a)*.7],[.32,.4],'#bdefff',6+i,16) for i,a in enumerate(np.linspace(0,TAU,6,endpoint=False))]
    add('bufu','Bufu','Elementais',20,'Pequenos cristais facetados ao redor do alvo e névoa fria. Grandes massas de gelo ficam para tiers próprios. Esta prévia usa sprites; volume final pede malhas.',ice+[sprite('smoke',[0,.2,0],[1.8,.7],'#9ac9de',6,24,2,.3,0,'alpha'),spark('#dcffff',start=17,count=16)],409.65,observed)
    add('garu','Garu','Elementais',30,'Turbilhão verde com três fitas largas e poeira; evitar anéis de laser separados.',[trail(helix(.9,2.6,2.3,a),'#b5ff63',.16,4,21,8) for a in [0,2.1,4.2]]+[spark('#d3ef94',count=16)])
    add('kouha','Kouha','Elementais',56,'Luz dourada concentrada e lâminas radiais; distinta dos talismãs de Hama.',[sprite('glow',[0,1,0],[1.3,2.6],'#ffe3a3',6,15),spark('#ffe7ac',count=20)]+[beam([math.cos(a)*.7,.1,math.sin(a)*.7],[0,2.2,0],'#fff5bf',.06,6,10) for a in np.linspace(0,TAU,6,endpoint=False)])
    add('eiga','Eiga / Eiha','Elementais',70,'Garras e cortes rubros convergem ao alvo com fumaça escura. Nome/código divergente no catálogo: revisão explícita.',[sprite('smoke',[0,1,0],[2.1,2.2],'#260819',4,24,2,.2,0,'alpha')]+[trail([[math.cos(a)*(1.5-t),.3+2*t,math.sin(a)*(1.5-t)] for t in np.linspace(0,1,36)],'#ee275b',.16,5+i,13,6) for i,a in enumerate([0,2,4])])
    add('megido','Megido','Elementais',78,'Núcleo violeta compacto, expansão branca e onda de pressão; tiers fortes precisam de coreografia própria.',[sprite('glow',[0,1,0],[2.6,2.6],'#bf7fff',4,23),sprite('star',[0,1,0],[2,2],'#ffffff',6,8),spark('#d4a2ff',count=24,radius=.8,speed=.09)])
    add('dia','Dia','Cura',155,'Luz de cura e partículas ascendentes; sem aparência de eletricidade ou golpe.',[sprite('glow',[0,.8,0],[1.5,2.4],'#aaffcf',5,28),spark('#bcffe1',start=5,count=24,life=28,radius=.5,speed=.075)])
    for stem,col,src,ref in [('taru','#ffbb47',180,1108.25),('raku','#ff55ee',181,1116.25),('suku','#abff45',182,1124.25)]:
        add(stem+'kaja',stem.capitalize()+'kaja','Buffs',src,'Fitas e coluna ascendentes, clarão na base e partículas. Cor comunica o atributo.',
            [trail(helix(.77,2.8,2.4,a),col,.11,4,21,7) for a in [0,math.pi]]+
            [beam([math.cos(a)*.6,.1,math.sin(a)*.6],[math.cos(a)*.6,2.8,math.sin(a)*.6],col,.022,8,15) for a in [0,1.6,3.2,4.8]]+
            [trail(circle(.95,.12),col,.09,4,9,18),sprite('glow',[0,.1,0],[2.3,.5],col,5,12),spark(col,count=24,life=23,radius=.65,speed=.1)],ref,observed)
    for stem,col,src,ref in [('taru','#ff9f39',190,1133.25),('raku','#ec60ff',191,1143.25),('suku','#abff45',192,1153.75)]:
        paths=[]
        for phase in [0,math.pi]:
            p=[]
            for t in np.linspace(0,1,90):
                r=.95*(1-.72*t);a=phase+TAU*2.4*t;p.append([r*math.cos(a),1+r*math.sin(a),.12*math.sin(a*2)])
            paths.append(trail(p,col,.09,5,25,7))
        add(stem+'nda',stem.capitalize()+'nda','Debuffs',src,'Arcos verticais convergem num núcleo sobre o corpo e se apagam. Não reutiliza a coluna ascendente dos buffs.',paths+[sprite('glow',[0,1,0],[.65,.65],col,8,24),spark(col,at=(0,1,0),start=21,count=9,life=10,radius=.25)],ref,observed)
    add('dekaja','Dekaja','Limpeza e proteção',200,'Desfazer bônus: estilhaços luminosos, fitas brancas/rosadas e colapso violeta.',[trail(helix(1.05,1.8,1.5,a),'#f6afff',.12,4,23,7) for a in [0,math.pi]]+[sprite('star',[0,1,0],[1.6,1.6],'#ec98ff',15,14,4,.7),spark('#ffe4ff',start=18,count=20)],1233.75,observed)
    add('dekunda','Dekunda','Limpeza e proteção',201,'Remover reduções: faixa dourada de purificação e fragmentos iridescentes. Não aplica aumento de status.',[trail(circle(.95,.7), '#ffe6a0',.2,5,18,18)]+[spark(c,start=11+i,count=7,life=21,radius=.8,speed=.06) for i,c in enumerate(['#ffc478','#acffec','#e0acff'])],1252.4,observed)
    for id,name,src,col in [('charge','Charge',205,'#ffab5c'),('concentrate','Concentrate',206,'#8ca5ff')]:
        add(id,name,'Buffs',src,'Energia recolhida para o próximo golpe: linhas ascendentes e núcleo pulsante. Paleta e detalhes ainda em revisão.',[trail([[math.cos(a)*(.7-.45*t),t*2.1,math.sin(a)*(.7-.45*t)] for t in np.linspace(0,1,32)],col,.07,4,22,6) for a in np.linspace(0,TAU,8,endpoint=False)]+[sprite('glow',[0,1,0],[.7,.7],col,13,16)])
    for id,name,src,col in [('tetrakarn','Tetrakarn',215,'#ffe8a6'),('makarakarn','Makarakarn',216,'#dca1ff')]:
        ls=[trail(circle(1.05,1.05,True),col,.055,4,18,19),trail(circle(1.05,1.05),col,.05,4,18,19),sprite('shield',[0,1.05,0],[2.15,2.15],col,5,30)]
        add(id,name,'Limpeza e proteção',src,'Película protetora com trama e borda luminosa, que some após a aplicação. Persistência visível não é estado de gameplay.',ls)
    add('break','Break elemental','Limpeza e proteção',220,'Barreira da resistência se fragmenta para fora; cor será escolhida pelo elemento.',[sprite('shield',[0,1,0],[1.7,1.7],'#ffbc70',4,12),spark('#ffcd94',at=(0,1,0),start=14,count=22,life=17,radius=.8,speed=.09)])
    add('ailment','Aflições','Estados',91,'Estudo de confusão: símbolos e névoa perto da cabeça. Veneno, charme, medo e fúria terão receitas separadas; não homologados por este protótipo.',[sprite('smoke',[0,1.65,0],[1.6,.7],'#9058bf',5,27,2,.4,0,'alpha'),trail(circle(.65,1.85),'#e59dff',.06,6,25,9),sprite('star',[0,1.9,0],[.25,.25],'#ffc1ff',6,25,5,.6)])
    add('hama','Hama','Morte instantânea',50,'Talismãs claros orbitam o alvo antes do selo. Não compartilhar o mesmo feixe de Kouha.',[sprite('talisman',[0,1,0],[.35,.7],'#fff3b7',4,26,7,.85,.018),trail(circle(1,.15),'#ffe9a0',.07,5,22,20)])
    add('mudo','Mudo','Morte instantânea',64,'Selo sombrio e fragmentos escuros; morte/fracasso depende do resultado, não do efeito de partículas.',[sprite('smoke',[0,.8,0],[1.9,2],'#26052e',4,28,2,.2,0,'alpha'),trail(circle(.95,.1),'#b74be6',.11,5,23,22),spark('#c779ed',start=7,count=16,life=23)])
    add('slash','Corte','Físicos',110,'Um arco rápido no contato e fagulhas; não manter um aro inteiro em torno do alvo.',[trail([[1.15*math.cos(a),1+.9*math.sin(a),0] for a in np.linspace(-.6,2.6,32)],'#ffe2b2',.16,5,5,4),spark('#ffc983',at=(0,1,0),start=7,count=12,life=8,radius=.2,speed=.08)])
    add('strike','Impacto','Físicos',125,'Clarão seco e detritos curtos no contato. Tapinha usa versão reduzida, sem grande explosão.',[sprite('star',[0,1,0],[1.2,1.2],'#fff1d7',6,7),spark('#ffe0a0',at=(0,1,0),start=6,count=12,life=10,radius=.2,speed=.09)])
    add('pierce','Perfuração','Físicos',139,'Risco direcional e pequeno contato; rajadas devem seguir a quantidade real de acertos.',[beam([-.6,1,.7],[.4,1,-.5],'#fff5cd',.04,5,4),sprite('star',[0,1,0],[.6,.6],'#ffe7b0',6,7),spark('#ffd2a5',at=(0,1,0),start=6,count=8,life=8,radius=.1,speed=.08)])
    add('psy','PSY — adaptação','Adaptações',-1,'Família ausente do Reload: distorção rosada em planos inclinados, proposta autoral declarada.',[trail([[.9*math.cos(a),1+.7*math.sin(a),.4*math.sin(a)] for a in np.linspace(0,TAU*2,75)],'#ff8dcc',.09,5,25,12),sprite('glow',[0,1,0],[.6,.6],'#ffb6eb',6,22)])
    add('nuclear','Nuclear — adaptação','Adaptações',-1,'Família ausente do Reload: núcleo ciano, choque radial e partículas, sem afirmar fonte original.',[sprite('glow',[0,1,0],[1.9,1.9],'#79efff',6,20),trail(circle(1.2,1),'#a7ffff',.13,5,15,15),spark('#c7ffff',start=8,count=24,speed=.09)])
    add('summon','Invocação','Invocação',-1,'Estudo da aura azul de manifestação e cacos brancos. O disparo do Evoker e o One More terão marcadores separados.',[sprite('fire',[0,.55,0],[1.3,1.7],'#70afff',4,28,4,.6),spark('#ecffff',start=4,count=24,life=30,radius=.8,speed=.08)])
    return rows

def texture(kind,t,size=96):
    yy,xx=np.mgrid[:size,:size];x=(xx-size/2)/(size/2);y=(yy-size/2)/(size/2)
    r=np.hypot(x,y); a=np.zeros_like(x);col=np.ones((*x.shape,3))
    if kind=='fire':
        # Authored animated noise silhouette; no extracted game texture is used.
        v=(1-y)*.5; taper=.12+.48*(1-v); drift=.1*np.sin(v*13-t*14)+.06*np.sin(v*28+t*17)
        noise=.11*np.sin(x*24+v*22-t*18)+.06*np.cos(x*39-v*17-t*12)
        a=np.clip((taper+noise-abs(x-drift))*9,0,1)*np.clip((v-.02)*10,0,1)*np.clip((.98-v)*8,0,1)
        hot=np.clip(1-abs(x-drift)/(taper+.01),0,1);col[:,:,0]=1;col[:,:,1]=.2+.75*hot;col[:,:,2]=.02+.36*hot**4
    elif kind=='smoke': a=np.exp(-r*r*3.6)*(.62+.18*np.sin(x*12+y*9+t*7));col[:]=1
    elif kind=='glow': a=np.exp(-r*r*9)*np.clip(1-r,0,1)
    elif kind=='spark': a=np.exp(-(x*x*80+y*y*7))
    elif kind=='shield': a=np.exp(-((r-.88)*24)**2)*.6+np.maximum(0,np.cos(x*22)+np.cos(y*22)-1.6)*.13*(r<.87)
    else:
        im=Image.new('RGBA',(size,size));d=ImageDraw.Draw(im)
        if kind=='star':
            pts=[]
            for i in range(8):
                ang=i*TAU/8;rr=.95 if i%2==0 else .14;pts.append((size/2+math.cos(ang)*size/2*rr,size/2+math.sin(ang)*size/2*rr))
            d.polygon(pts,fill='white')
        if kind=='ice':
            d.polygon([(size*.45,2),(size*.8,size*.4),(size*.62,size*.96),(size*.14,size*.85),(size*.22,size*.35)],fill=(94,181,214,245))
            d.polygon([(size*.45,2),(size*.5,size*.85),(size*.14,size*.85),(size*.22,size*.35)],fill=(217,250,255,255))
            d.polygon([(size*.45,2),(size*.8,size*.4),(size*.62,size*.96),(size*.5,size*.85)],fill=(120,205,242,245))
        if kind=='talisman':
            d.rectangle((size*.32,size*.06,size*.68,size*.94),fill=(255,250,219,255),outline=(211,154,70,255),width=2)
            for py in range(23,80,12):d.line((size*.43,py,size*.57,py+5),fill=(107,74,31,255),width=2)
        return im
    a=np.clip(a,0,1);arr=np.concatenate([col*255,a[:,:,None]*255],axis=2).clip(0,255).astype('uint8');return Image.fromarray(arr,'RGBA')

TEXTURES={}
def prepare_textures():
    folder=OUT/'draft/resources/assets/velvet_eclipse/textures/vfx/audit01';folder.mkdir(parents=True,exist_ok=True)
    for kind in ['fire','smoke','glow','spark','shield','star','ice','talisman']:
        frames=32 if kind in ['fire','smoke'] else 1
        ts=[texture(kind,j/max(1,frames-1)) for j in range(frames)];TEXTURES[kind]=ts
        sheet=Image.new('RGBA',(96*(8 if frames>1 else 1),96*(4 if frames>1 else 1)))
        for j,im in enumerate(ts):sheet.paste(im,((j%8)*96,(j//8)*96))
        sheet.save(folder/(kind+'.png'))

def evidence():
    data=[]
    for p in sorted((WORK/'original-assets').rglob('*.uasset')):
        strings=[s.decode('ascii') for s in re.findall(rb'[ -~]{5,}',p.read_bytes())]
        m=re.search(r'S(\d{4})_',p.name)
        paths=sorted(set(s for s in strings if s.startswith('/Game/') and len(s)<240))
        emitters=sorted(set(s.split('.')[0] for s in strings if '.Color.ColorCurve' in s and len(s)<160))
        uexp=p.with_suffix('.uexp')
        data.append(dict(asset=str(p.relative_to(WORK/'original-assets')).replace('\\','/'),source=int(m[1]) if m else None,sha256=sha(p),exportSha256=sha(uexp) if uexp.exists() else None,imports=paths,emitterNames=emitters))
    return data

def route(sk):
    id,src,elem=sk['id'],sk['source'],sk['element'];special=''
    if src>=260:return None,'Teurgia: sequência exclusiva; storyboard e malhas ainda pendentes'
    if id in ['tarukaja','rakukaja','sukukaja','tarunda','rakunda','sukunda','dekaja','dekunda','charge','concentrate','tetrakarn','makarakarn']:return id,''
    if src in [185,186,187,195,196,197]:return {185:'tarukaja',186:'rakukaja',187:'sukukaja',195:'tarunda',196:'rakunda',197:'sukunda'}[src],'Área: uma instância por alvo confirmado'
    if src in [188,198]:return ('tarukaja' if src==188 else 'tarunda'),'Composição tripla de atributos: não aprovar como simples troca de cor'
    if src in [207,210,211]:return 'charge','Variação especial: Bloody Charge/vermelho e crítico exigem coreografia própria'
    if 220<=src<=223:return 'break','Cor do elemento; paleta e quebra a refinar por habilidade'
    if 50<=src<=54:return 'hama','Instant kill; separar sucesso, imunidade e falha'
    if 64<=src<=68:return 'mudo','Instant kill; separar sucesso, imunidade e falha'
    if src in [100,101,102,103,104,105]:return None,'Ghastly Wail, vulnerabilidade e drenos: efeito específico pendente; não usar Megido genérico'
    if elem=='PHYSICAL':return 'strike','Ajustar contato à animação; multi-hit segue os acertos reais'
    if elem=='HEALING':
        if src in [165,166,167]:return 'dia','Reviver: núcleo ascendente específico e alvo caído; não validado por Dia'
        if src in [170,171,172,173,176,177]:return 'dekunda','Limpeza de aflições: receita própria pendente; Dekunda é somente estudo de purificação'
        if src==163:return 'dia','Salvation: cura e limpeza composta pendente'
        return 'dia','Cura em níveis: densidade e intensidade propostas, não multiplicar altura do alvo'
    if elem=='AILMENT':return 'ailment','Receita individual de '+sk['detail']+' pendente; não tratar todas como confusão'
    profile={'FIRE':'agi','ELECTRIC':'zio','ICE':'bufu','WIND':'garu','LIGHT':'kouha','DARK':'eiga','ALMIGHTY':'megido','PSY':'psy','NUCLEAR':'nuclear','SLASH':'slash','STRIKE':'strike','PIERCE':'pierce'}.get(elem)
    if src<0:return profile,'Adaptação autoral: essa família não existe em Reload'
    if id in ['eiga','maeiga']:special='Nome do catálogo difere do código original; áudio por nome 71, VFX 70/73 não homologado'
    elif id not in ['agi','zio','bufu','garu','kouha','megido'] and elem not in ['SLASH','STRIKE','PIERCE']:special='Variação/tier: referência específica a refinar; não herdar apenas escala'
    elif elem in ['SLASH','STRIKE','PIERCE']:special='Ajustar trajetória à animação, arma e marcadores de acerto; multiplo hit sem repetir pelo render'
    return profile,special

def build_manifest(rs,ev):
    skills=json.loads((ROOT/'velvet-eclipse/src/main/resources/data/velvet_eclipse/catalogue/skills.json').read_text(encoding='utf-8'))
    au=json.loads((ROOT/'outputs/auditoria-audio-mapa-05/manifest.json').read_text(encoding='utf-8'))
    aud={r.get('subject',{}).get('skillId'):r for r in au['routes'] if isinstance(r.get('subject'),dict) and 'skillId' in r['subject']}
    ents={e['id']:e for e in au['entries']};routes=[]
    for sk in skills:
        if sk['passive']:continue
        p,n=route(sk);ar=aud.get(sk['id'],{});assets=[e['asset'] for e in ev if e['source']==sk['source'] and '/Xrd777/' in e['asset']] if sk['source']>=0 else []
        routes.append(dict(id=sk['id'],name=sk['name'],element=sk['element'],target=sk['target'],source=sk['source'],profile=p,note=n,
                           minHits=sk['minHits'],maxHits=sk['maxHits'],assets=assets,audioRoute=ar.get('id'),audioClips=ar.get('clips',[]),
                           status='Proposta; aguarda revisão',evidence='Assets locais identificados' if assets else 'Sem asset de skill associado'))
    audio=OUT/'audio';audio.mkdir(exist_ok=True)
    for r in rs:
        sr=next((x for x in routes if x['id']==r['id']),None)
        if sr and sr['audioClips']:
            en=ents[sr['audioClips'][0]];p=ROOT/'outputs/auditoria-audio-mapa-05'/en['file'];dst=audio/(en['id']+'.wav');shutil.copy2(p,dst)
            r['audio']='audio/'+dst.name;r['audioNote']='Candidato original da auditoria 05; onda isolada, não mixer completo. Reprodução separada; sincronismo final pendente.'
    return dict(date='2026-10-22',status='Auditoria visual e técnica, sem implementação',photon='Forge 1.20.1 / 1.1.17',
                originalAssets=len(ev),baseGameAssets=sum('/Xrd777/' in e['asset'] for e in ev),activeSkills=len(routes),passives=sum(x['passive'] for x in skills),profiles=rs,routes=routes,
                sourceVideo='https://www.youtube.com/watch?v=TZap-dV5Oyo',sourceVideoNote='ReVersed JRPGs; comparação Portable/Reload. Só frames Reload selecionados para referências.',
                limitations=['Imports e nomes de emitters lidos de 697 assets; propriedades/curvas Niagara não decodificadas.','Prévias renderizadas em software com receitas autorais; não são capturas Photon/Minecraft.','Sprites de cristais/escudos são estudos: volume final pede revisão de malhas e transparência.','Mapeamento completo não equivale a 171 efeitos terminados; variações especiais e teurgias estão explicitamente pendentes.'])

def project(p):
    x,y,z=p;return (256+(x*.92+z*.38)*82,304-y*82+(z*.92-x*.38)*20)
def base():
    im=Image.new('RGB',(W,H),'#091321');d=ImageDraw.Draw(im)
    for k in range(-4,5):
        d.line([project([k,0,-4]),project([k,0,4])],fill='#1a2c3b');d.line([project([-4,0,k]),project([4,0,k])],fill='#1a2c3b')
    d.ellipse((208,289,304,317),fill='#071019')
    def box(x,y,w,h,z=0):
        sx,sy=project([x,y,z]);d.rectangle((sx-w*41,sy-h*82,sx+w*41,sy),fill='#495a6d',outline='#708598',width=1)
        d.polygon([(sx+w*41,sy),(sx+w*41+7,sy-4),(sx+w*41+7,sy-h*82-4),(sx+w*41,sy-h*82)],fill='#2a3a4c')
    box(-.15,0,.24,.77);box(.15,0,.24,.77);box(0,.77,.56,.64);box(-.43,.71,.2,.68);box(.43,.71,.2,.68);box(0,1.43,.4,.39)
    d.text((18,346),'ALVO 1,8 m  /  CÂMERA FIXA',font=font(11),fill='#8198ad')
    return im
BASE=base()
def mix_sprite(dst,tex,x,y,w,h,tint,alpha,add=True):
    if w<1 or h<1 or alpha<=0:return dst
    im=tex.resize((max(1,int(w)),max(1,int(h))),Image.Resampling.BILINEAR)
    arr=np.array(im,dtype=np.float32);arr[:,:,:3]*=np.array(tint)/255;arr[:,:,3]*=min(1,alpha);im=Image.fromarray(arr.clip(0,255).astype('uint8'))
    layer=Image.new('RGBA',dst.size);layer.paste(im,(int(x-w/2),int(y-h/2)))
    if add:
        black=Image.new('RGB',dst.size);black.paste(layer,mask=layer.getchannel('A'));return ImageChops.add(dst,black)
    return Image.alpha_composite(dst.convert('RGBA'),layer).convert('RGB')

def frame(recipe,tick,labels=True):
    im=BASE.copy();back=Image.new('RGB',(W,H));front=Image.new('RGB',(W,H));draws=[ImageDraw.Draw(back),ImageDraw.Draw(front)]
    for li,l in enumerate(recipe['layers']):
        age=tick-l['start'];u=age/l['life']
        if age<0:continue
        if l['type']=='sprite':
            if u>=1:continue
            rng=random.Random(recipe['seed']+li*31);fade=min(1,age/2)*min(1,(1-u)*4)
            for j in range(l['count']):
                angle=rng.random()*TAU;r=l['radius']*math.sqrt(rng.random());p=list(l['at']);p[0]+=r*math.cos(angle)+math.cos(angle)*l['speed']*age*.35;p[2]+=r*math.sin(angle)+math.sin(angle)*l['speed']*age*.35;p[1]+=l['speed']*age
                size=l['size'];tex=TEXTURES[l['texture']][min(len(TEXTURES[l['texture']])-1,int(u*len(TEXTURES[l['texture']])))];x,y=project(p)
                im=mix_sprite(im,tex,x,y,size[0]*82,size[1]*82,rgb(l['color']),fade,l['blend']=='add')
        else:
            if l['type']=='beam':
                if u>=1:continue
                points=[l['a'],l['b']];fade=min(1,(1-u)*3)
            else:
                if age>l['life']+l['tail']:continue
                pts=l['points'];end=min(1,u);begin=max(0,(age-l['tail'])/l['life']);a=int(begin*(len(pts)-1));b=max(a+1,int(end*(len(pts)-1)));points=pts[a:b+1];fade=min(1,max(0,(l['life']+l['tail']-age)/l['tail']))
            col=tuple(int(x*fade) for x in rgb(l['color']));w=max(1,int(l['width']*82));xy=[project(p) for p in points]
            if len(xy)>=2:
                z=sum(p[2] for p in points)/len(points);d=draws[0 if z<-.1 else 1];d.line(xy,fill=col,width=w);d.line(xy,fill=tuple(min(255,int(x*.7+80))*int(fade>0) for x in col),width=max(1,w//3))
    # Subdued bloom approximates the study view; production uses depth-tested materials, bloom off.
    glow=ImageChops.add(back,front);im=ImageChops.add(im,glow.filter(ImageFilter.GaussianBlur(5)).point(lambda x:int(x*.45)));im=ImageChops.add(im,glow)
    if labels:
        d=ImageDraw.Draw(im);d.rectangle((0,0,W,54),fill='#0d1e30');d.text((18,12),recipe['name'].upper(),font=font(22,True),fill='#d5f8ff');d.text((18,368),'ESTUDO OFFLINE • NÃO É CAPTURA DO MINECRAFT',font=font(10),fill='#839bad');d.text((390,19),f'{tick/20:.2f} s',font=font(13),fill='#83cce5')
    return im

def render(rs):
    prev=OUT/'previews';prev.mkdir(exist_ok=True);refs=OUT/'references';refs.mkdir(exist_ok=True)
    peaks=[]
    for r in rs:
        ims=[frame(r,i) for i in range(FRAMES)]
        ims[0].save(prev/(r['id']+'.gif'),save_all=True,append_images=ims[1:],duration=50,loop=0,optimize=False)
        # Pick meaningful mid-effect composition, not an empty warm-up frame.
        peak=12 if r['id']=='zio' else (9 if r['id'] in ['slash','strike','pierce'] else 19)
        if r['id']=='zio':peak=8
        ims[peak].save(prev/(r['id']+'.png'));r['preview']='previews/'+r['id']+'.gif';r['poster']='previews/'+r['id']+'.png'
        if r['referenceSeconds'] is not None:
            dest=refs/(r['id']+'.jpg');subprocess.run([str(FFMPEG),'-hide_banner','-loglevel','error','-ss',str(r['referenceSeconds']),'-i',str(VIDEO),'-frames:v','1','-q:v','3','-y',str(dest)],check=True)
            r['reference']='references/'+dest.name
        print('preview',r['id'],flush=True)
    featured=['agi','zio','tarukaja','tarunda','dekaja','dekunda'];selected=[next(r for r in rs if r['id']==id) for id in featured]
    montages=[]
    for i in range(FRAMES):
        c=Image.new('RGB',(W*3,H*2))
        for j,r in enumerate(selected):c.paste(frame(r,i),(j%3*W,j//3*H))
        montages.append(c.resize((1152,576),Image.Resampling.LANCZOS))
    montages[0].save(OUT/'comparativo-animado.gif',save_all=True,append_images=montages[1:],duration=50,loop=0,optimize=False)
    sheet=Image.new('RGB',(W*3,H*2))
    for j,r in enumerate(selected):sheet.paste(Image.open(prev/(r['id']+'.png')),(j%3*W,j//3*H))
    sheet.save(OUT/'painel-vfx.png')

def main():
    OUT.mkdir(parents=True,exist_ok=True);DOC.mkdir(parents=True,exist_ok=True)
    rs=recipes();ev=evidence();man=build_manifest(rs,ev)
    prepare_textures()
    if '--no-render' not in sys.argv:render(rs)
    else:
        old=json.loads((OUT/'manifest.json').read_text(encoding='utf-8'))
        for r in rs:
            prior=next(p for p in old['profiles'] if p['id']==r['id'])
            for k in ['preview','poster','reference']:
                if k in prior:r[k]=prior[k]
    for r in rs:
        r['peakEmitterCount']=max(sum(l['start']<=t<l['start']+l['life']+l.get('tail',0) for l in r['layers']) for t in range(64))
        r['particleBurstCount']=sum(l.get('count',0) for l in r['layers'])
    dump(OUT/'manifest.json',man);dump(OUT/'evidence.json',ev);dump(OUT/'draft/recipes.json',rs)
    dump(DOC/'manifest.json',man);dump(DOC/'evidence.json',ev)
    print(json.dumps({'profiles':len(rs),'activeSkills':len(man['routes']),'passives':man['passives'],'assets':len(ev)},ensure_ascii=False),flush=True)
if __name__=='__main__': main()
