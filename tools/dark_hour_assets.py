"""Deterministic native voxel materials; run with Python 3 (no third-party libraries)."""
from pathlib import Path
import json,struct,zlib
root=Path(__file__).resolve().parents[1]/'src/main/resources/assets/velvet_eclipse'
def write(path,data):
    path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(data,indent=2)+'\n',encoding='utf8')
def png(path,pixels):
    def chunk(kind,data):return struct.pack('!I',len(data))+kind+data+struct.pack('!I',zlib.crc32(kind+data)&0xffffffff)
    path.parent.mkdir(parents=True,exist_ok=True)
    raw=b''.join(b'\0'+bytes(v for pixel in row for v in pixel) for row in pixels)
    path.write_bytes(b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',struct.pack('!2I5B',16,16,8,2,0,0,0))+chunk(b'IDAT',zlib.compress(raw))+chunk(b'IEND',b''))
for name,base in [('tartarus_stone',(22,48,41)),('tartarus_trim',(57,100,77)),('tartarus_light',(72,211,154))]:
    pixels=[]
    for y in range(16):
        row=[]
        for x in range(16):
            n=(x*13+y*7+x*y*3)%11-5
            if name=='tartarus_stone':n-=12 if y%8==0 or (x+(8 if y>=8 else 0))%16==0 else 0
            elif name=='tartarus_trim':n+=14 if x in (1,14) or y in (1,14) else -8 if x in (0,15) else 0
            else:n+=24 if x==y or x+y==15 else -30 if abs(x-8)+abs(y-8)>9 else 0
            row.append(tuple(max(0,min(255,c+n)) for c in base))
        pixels.append(row)
    png(root/'textures/block'/f'{name}.png',pixels)
    write(root/'models/block'/f'{name}.json',{'parent':'minecraft:block/cube_all','textures':{'all':f'velvet_eclipse:block/{name}'}})
    write(root/'blockstates'/f'{name}.json',{'variants':{'':{'model':f'velvet_eclipse:block/{name}'}}})
for shape,parent in [('','stairs'),('_inner','inner_stairs'),('_outer','outer_stairs')]:
    write(root/'models/block'/f'tartarus_stairs{shape}.json',{'parent':f'minecraft:block/{parent}','textures':{p:'velvet_eclipse:block/tartarus_stone' for p in ['bottom','top','side']}})
variants={}
for facing,angle in [('east',0),('south',90),('west',180),('north',270)]:
    for half in ['bottom','top']:
        for shape in ['straight','inner_left','inner_right','outer_left','outer_right']:
            rot=angle+(270 if shape.endswith('left') else 0)+(90 if half=='top' and shape!='straight' else 0)
            model='tartarus_stairs'+('_inner' if shape.startswith('inner') else '_outer' if shape.startswith('outer') else '')
            variants[f'facing={facing},half={half},shape={shape}']={'model':'velvet_eclipse:block/'+model,'y':rot%360,'x':180 if half=='top' else 0,'uvlock':True}
write(root/'blockstates/tartarus_stairs.json',{'variants':variants})
print('Dark Hour: 3 materials, 6 models and 4 blockstates generated.')
