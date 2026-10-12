"""Append functional campus doors and seats to the existing dorm/mall catalogue."""
from pathlib import Path
import json,gzip,sys,math,importlib.util
import numpy as np
from collections import Counter
HERE=Path(__file__).parent
spec=importlib.util.spec_from_file_location('city_export',HERE.parent/'city_polish/export.py');e=importlib.util.module_from_spec(spec);spec.loader.exec_module(e)
def read(p):return json.loads(gzip.decompress(p.read_bytes()))
plan=read(e.DATA/'plan.json.gz');defs=read(e.DATA/'architecture.json.gz');catalog=read(e.DATA/'furniture.json.gz')
binary=np.frombuffer(gzip.decompress((e.AS/'city/architecture.bin.gz').read_bytes()),'>f4').astype('<f4')
chunks=[binary];cursor=len(binary);cells={tuple(c[:3]):c[4] for c in plan['cells'] if c[3]==40};doors=catalog['doors'];seats=catalog['seats']
# Reuse the same authored-leaf clipping and merge code, without executing the dorm script.
source=(HERE.parent/'city_polish/furniture.py').read_text(encoding='utf8')
exec(source[source.index('def transform('):source.index('\nraw=np.frombuffer')])
assembly=json.loads((HERE/'assembly.json').read_text())
for d in assembly['doors']:
 lo=np.array(d['lo']);hi=np.array(d['hi']);x,y,z=lo
 bs=[[x+.025,y+.01,z+.06,hi[0]-.025,hi[1]-.02,z+.14],[hi[0]-.21,y+1.15,z+.14,hi[0]-.12,y+1.4,z+.20]]
 cl=(np.concatenate([e.cuboid(b[:3],b[3:],34 if i else 35,(.28,.37,.35) if i else (.57,.43,.30)) for i,b in enumerate(bs)]),bs)
 op=transform(*cl,[x+.025,y,z+.10],90)
 cl=transform(*cl,[0,0,0],d['angle'],d['offset']);op=transform(*op,[0,0,0],d['angle'],d['offset'])
 door(d['id'],cl,op,offset=(0,64,-110))
for s in assembly['seats']:
 p=np.array(s['point'])+[0,64,-110];anchor=np.floor(p).astype(int).tolist();assert tuple(anchor) in cells,(s['id'],anchor)
 bounds=(np.array(s['bounds'])+[0,64,-110,0,64,-110]).tolist()
 seats.append(dict(id=s['id'],x=p[0],y=p[1],z=p[2],yaw=s['yaw'],bounds=bounds,anchor=anchor,tile=cells[tuple(anchor)]))
e.dump(e.DATA/'architecture.json.gz',defs)
(e.AS/'city/architecture.bin.gz').write_bytes(gzip.compress(np.concatenate(chunks).astype('>f4').tobytes(),mtime=0))
e.dump(e.DATA/'furniture.json.gz',dict(seats=seats,doors=doors))
print('Functional city furniture:',len(seats),'seats;',len(doors),'doors;',len(defs),'tiles')
