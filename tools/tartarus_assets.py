"""Original voxel materials and dimension data for the Tartarus hall and dungeon."""
from pathlib import Path
import json
from dark_hour_assets import root, png, write

palette={'ivory':(202,213,212),'tile':(67,90,125),'tile_dark':(42,59,82),'wall':(22,34,55),
         'column':(94,111,131),'gold':(166,140,68),'carpet':(29,168,218),'clock':(181,218,164),
         'hand':(29,93,54),'border':(149,199,172),'seal':(13,32,49),'window':(30,190,73),
         'azure':(27,152,205),'violet':(118,67,204)}
for name,base in palette.items():
    pixels=[]
    for y in range(16):
        row=[]
        for x in range(16):
            n=(x*7+y*11)%9-4
            if name in ('window','azure','violet'):
                n=-95 if x in (0,1,7,8,14,15) or y in (0,1,7,8,14,15) else 20
            elif name in ('tile','tile_dark','wall'):
                n=-18 if x in (0,15) or y in (0,15) else n
            elif name=='column':n=16 if x%4==1 else -14 if x%4==0 else 0
            elif name=='ivory':n=8 if (x+2*y)%13<2 else n
            elif name=='gold':n=25 if x in (1,14) or y in (1,14) else n
            elif name=='border':n=-75 if abs(x-8)<abs(y-8)-1 else n
            row.append(tuple(max(0,min(255,c+n)) for c in base))
        pixels.append(row)
    name='tartarus_'+name
    png(root/'textures/block'/f'{name}.png',pixels)
    write(root/'models/block'/f'{name}.json',{'parent':'minecraft:block/cube_all','textures':{'all':'velvet_eclipse:block/'+name}})
    write(root/'blockstates'/f'{name}.json',{'variants':{'':{'model':'velvet_eclipse:block/'+name}}})

# The entrances are deliberately pitch black, not the vanilla purple Nether effect.
png(root/'textures/block/tartarus_portal.png', [[(0,0,0)]*16 for _ in range(16)])
for name in ['portal','forward','back','exit','summit']:
    write(root/'models/block'/f'tartarus_{name}.json',{'parent':'minecraft:block/cube_all','textures':{'all':'velvet_eclipse:block/tartarus_portal'}})
    write(root/'blockstates'/f'tartarus_{name}.json',{'variants':{'':{'model':f'velvet_eclipse:block/tartarus_{name}'}}})
for suffix,parent in [('', 'stairs'),('_inner','inner_stairs'),('_outer','outer_stairs')]:
    write(root/'models/block'/f'tartarus_blue_stairs{suffix}.json',{'parent':f'minecraft:block/{parent}','textures':{s:'velvet_eclipse:block/tartarus_carpet' for s in ('bottom','top','side')}})
states=json.loads((root/'blockstates/tartarus_stairs.json').read_text())
for variant in states['variants'].values():variant['model']=variant['model'].replace('tartarus_stairs','tartarus_blue_stairs')
write(root/'blockstates/tartarus_blue_stairs.json',states)

data=root.parents[1]/'data/velvet_eclipse'
dimension=json.loads((data/'dimension/velvet_room.json').read_text());dimension['type']='velvet_eclipse:tartarus'
write(data/'dimension/tartarus.json',dimension)
kind=json.loads((data/'dimension_type/velvet_room.json').read_text());kind['ambient_light']=.3
write(data/'dimension_type/tartarus.json',kind)
print('Tartarus: hall/dungeon materials, portals, stairs and dimension generated.')
