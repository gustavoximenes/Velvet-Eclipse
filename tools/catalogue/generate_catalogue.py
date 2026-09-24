"""Reproducible factual catalogue, pinned upstream revision; never imports upstream code."""
import json,re,math,unicodedata
from pathlib import Path
ROOT=Path(__file__).resolve().parents[2]
UP=Path(__file__).parent/'upstream'
OUT=ROOT/'src/main/resources/data/velvet_eclipse/catalogue'
OUT.mkdir(parents=True,exist_ok=True)
def read(name):return json.loads((UP/name).read_text(encoding='utf-8-sig'))
def ident(name):return re.sub('[^a-z0-9]+','_',unicodedata.normalize('NFKD',name).encode('ascii','ignore').decode().lower()).strip('_')
def skillid(name):return {'Eiha':'eiga','Maeiha':'maeiga','Eiga':'eiga','Maeiga':'maeiga'}.get(name,ident(name))
def write(name,data):(OUT/name).write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
demons=read('demon-data.json'); unlocks=read('demon-unlocks.json'); rawskills=read('skill-data.json')
dlc=set(next(x['conditions'] for x in unlocks if x['category']=='Downloadable Content'))
personas={k:v for k,v in demons.items() if k not in dlc}
used={k for v in personas.values() for k in v['skills']}
selected={k:v for k,v in rawskills.items() if v['a'][0] != 'Mahama B' and (v['a'][0] in used or (v['b'][0]>0 and int(k)<900) or v['c'][2].startswith('Sword'))}
elems=dict(sla='SLASH',str='PHYSICAL',pie='PIERCE',fir='FIRE',ice='ICE',ele='ELECTRIC',win='WIND',lig='LIGHT',dar='DARK',alm='ALMIGHTY',ail='AILMENT',rec='HEALING',sup='SUPPORT',pas='SUPPORT',spe='SUPPORT')
targets={'1 foe':'ONE_ENEMY','All foes':'ALL_ENEMIES','1 ally':'ONE_ALLY','All allies':'ALL_ALLIES','Universal':'ALL_ENEMIES','Party':'ALL_ALLIES','-':'SELF'}
skills={}
for number,row in selected.items():
 name,kind,target=row['a']; rank,cost,power,lo,hi,accuracy,critical,value=row['b']; detail,fmt,card=row['c']; sid=skillid(name)
 # Eiga is the user's name for the initial dark spell; do not duplicate Eiha.
 if sid in skills:continue
 passive=kind=='pas'; cardrank=0
 if card.startswith('Sword '):
  r=card.split()[-1];cardrank={'J':11,'Q':12,'K':13}.get(r,0) or int(r)
 nativelevels=[int(l if l>=1 else d['lvl']) for d in personas.values() for s,l in d['skills'].items() if skillid(s)==sid]
 minlevel=min(nativelevels,default=max(1,int(rank)*9))
 scaled=round(44*math.sqrt(power/40)) if power>0 and kind not in ('pas','sup','rec') else int(power)
 if kind=='rec':scaled={'Dia':60,'Diarama':130,'Media':45,'Mediarama':100}.get(name,0)
 skills[sid]=dict(id=sid,name='Eiga' if name=='Eiha' else 'Maeiga' if name=='Maeiha' else name,element=elems[kind],power=scaled,accuracy=max(1,int(accuracy)),sp=int(cost-1000) if cost>=1000 else 0,hp=int(cost) if cost<1000 else 0,critical=critical>0,target='SELF' if passive or name in ('Charge','Concentrate','Bloody Charge') else targets[target],passive=passive,rank=cardrank,minLevel=minlevel,effect=fmt,detail=detail,value=value,minHits=max(1,int(lo)),maxHits=max(1,int(hi)),critRate=int(critical),universal=target=='Universal',source=int(number))
 # Theurgies remain powerful skills but require resources instead of a missing gauge.
 if name in ('Armageddon','Scarlet Havoc','King and I','Cadenza','Jack Brothers','Trickster','Best Friends'):
  skills[sid]['sp']={'Armageddon':99,'Cadenza':30,'Jack Brothers':35,'Best Friends':40}.get(name,60);skills[sid]['minLevel']=20 if name=='Cadenza' else 70
  if name=='Armageddon':skills[sid]['power']=9999
for element,families in [('NUCLEAR',['Frei','Freila','Freidyne','Mafrei','Mafreila','Mafreidyne','Atomic Flare','Cosmic Flare']),('PSY',['Psi','Psio','Psiodyne','Mapsi','Mapsio','Mapsiodyne','Psycho Force','Psycho Blast'])]:
 for i,name in enumerate(families):
  tier=i%3 if i<6 else 3;area=i in (3,4,5,7)
  skills[ident(name)]=dict(id=ident(name),name=name,element=element,power=[44,75,115,170][tier],accuracy=95 if area else 99,sp=([10,16,24,54] if area else [4,8,12,48])[tier],hp=0,critical=False,target='ALL_ENEMIES' if area else 'ONE_ENEMY',passive=False,rank=([5,8,11,0] if area else [2,6,10,0])[tier],minLevel=[4,20,45,70][tier],effect='-',detail='-',value=0,minHits=1,maxHits=1,critRate=0,universal=False,source=-1)
resist={'w':'WEAK','s':'RESIST','n':'NULL','r':'REPEL','d':'DRAIN','-':'NORMAL'}
order=['SLASH','PHYSICAL','PIERCE','FIRE','ICE','ELECTRIC','WIND','LIGHT','DARK','ALMIGHTY']
definitions=[]
for name,d in personas.items():
 learn={}
 for s,l in d['skills'].items():learn.setdefault(str(max(d['lvl'],int(l))),[]).append(skillid(s))
 definitions.append(dict(id=ident(name),name=name,arcana=d['race'].upper(),level=d['lvl'],stats=d['stats'],affinities={e:resist[c] for e,c in zip(order,d['resists'])},learnset={l:sorted(set(s)) for l,s in learn.items()},provisional=name!='Orpheus'))
special={ident(k):[ident(n) for n in v] for k,v in read('special-recipes.json').items() if k in personas or k=='Arsene'}
write('skills.json',list(skills.values()));write('personas.json',definitions);write('special-fusions.json',special);write('fusion-chart.json',read('fusion-chart.json'))
assert all(s in skills for p in definitions for v in p['learnset'].values() for s in v)
print(f'Generated {len(definitions)} Reload Personas, {len(skills)} skills, {len(special)} special recipes.')
