"""Native block city. A single occupancy grid drives appearance, collision and maps.

Interior rooms occupy their actual building, with physical stairs and doors.
No duplicate scene meshes, hidden support walls or off-site interior sets.
"""
from pathlib import Path
import gzip, json, math
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[2]
RES = ROOT / 'src/main/resources'
ASSETS = RES / 'assets/velvet_eclipse'
DATA = RES / 'data/velvet_eclipse/city'
CELLS, PALETTE, LOOKUP, PLACES, BUILDINGS, ROUTES = {}, [], {}, [], [], []
BOUNDS = (-150, -155, 150, 120)


def state(block, **properties):
    if ':' not in block: block = 'minecraft:' + block
    key = (block, tuple(sorted(properties.items())))
    if key not in LOOKUP:
        LOOKUP[key] = 100 + len(PALETTE)
        PALETTE.append(dict(block=block, properties=properties))
    return LOOKUP[key]


def put(x, y, z, material):
    key = (int(x), int(y), int(z))
    if material is None: CELLS.pop(key, None)
    else: CELLS[key] = [*key, material, 0]


def fill(x0, y0, z0, x1, y1, z1, material):
    for x in range(x0, x1 + 1):
        for y in range(y0, y1 + 1):
            for z in range(z0, z1 + 1): put(x, y, z, material)


PAVING, BRICK, PLASTER, WOOD, _, SCHOOL, LEAVES, ROOF, LIGHT = range(9)
GRASS = state('grass_block', snowy='false')
STONE, TRIM, IRON = state('stone_bricks'), state('smooth_quartz'), state('polished_deepslate')
GLASS, WATER = state('light_blue_stained_glass'), state('water', level='0')
RED, BLUE, GOLD, GREEN = [state(c + '_terracotta') for c in ['red', 'blue', 'yellow', 'green']]
BOOKS = state('bookshelf')


def place(id, name, x, y, z): PLACES.append(dict(id=id, name=name, x=x, y=y, z=z))


def shell(x0, z0, x1, z1, levels, wall=PLASTER, floor=WOOD):
    for y in levels:
        fill(x0, y - 1, z0, x1, y - 1, z1, floor)
        for z in [z0, z1]: fill(x0, y, z, x1, y + 4, z, wall)
        for x in [x0, x1]: fill(x, y, z0, x, y + 4, z1, wall)
        fill(x0, y + 5, z0, x1, y + 5, z1, floor)


def door(x, y, z, facing='south', double=False):
    for i in range(2 if double else 1):
        xx, zz = (x + i, z) if facing in ('north', 'south') else (x, z + i)
        for dy, half in enumerate(['lower', 'upper']):
            put(xx, y + dy, zz, state('oak_door', facing=facing, half=half,
                hinge='left' if i == 0 else 'right', open='false', powered='false'))


def window(x, y, z, w, h, side='south'):
    for i in range(w):
        for j in range(h):
            xx, zz = (x + i, z) if side in ('north', 'south') else (x, z + i)
            put(xx, y + j, zz, GLASS)


def light(x, y, z): put(x, y, z, state('ochre_froglight', axis='y'))


def bench(x, y, z, length=4, facing='north'):
    for dx in range(length):
        put(x + dx, y, z, state('spruce_stairs', facing=facing, half='bottom', shape='straight', waterlogged='false'))


def table(x, y, z, w=4, d=2):
    fill(x, y, z, x + w - 1, y, z + d - 1, state('spruce_slab', type='top', waterlogged='false'))


def bed(x, y, z):
    put(x, y, z, state('blue_bed', facing='north', part='foot', occupied='false'))
    put(x, y, z - 1, state('blue_bed', facing='north', part='head', occupied='false'))
    put(x - 1, y, z - 1, WOOD); light(x - 1, y + 1, z - 1)


def stairs(name, x, z, y, height, width=3, facing='north'):
    dx, dz = {'north': (0, -1), 'south': (0, 1), 'east': (1, 0), 'west': (-1, 0)}[facing]
    mat = state('smooth_quartz_stairs', facing=facing, half='bottom', shape='straight', waterlogged='false')
    for step in range(height):
        for w in range(width):
            xx, zz = x + dx * step + (w if dz else 0), z + dz * step + (w if dx else 0)
            fill(xx, y + step, zz, xx, y + step + 3, zz, None)
            put(xx, y + step, zz, mat)
    ROUTES.append(dict(name=name, points=[[x-dx, y, z-dz], [x+dx*height, y+height, z+dz*height]]))


def lamp(x, z, y=64):
    fill(x, y, z, x, y+3, z, state('stone_brick_wall', up='true', north='none', south='none', east='none', west='none', waterlogged='false'))
    light(x, y+4, z)
    put(x, y+5, z, state('stone_brick_slab', type='bottom', waterlogged='false'))


def tree(x, z, cherry=False):
    fill(x, 64, z, x, 68, z, state('cherry_log' if cherry else 'oak_log', axis='y'))
    for dx in range(-3, 4):
        for dz in range(-3, 4):
            for dy in range(3):
                if dx*dx + dz*dz + dy*dy*2 <= 12:
                    put(x+dx, 68+dy, z+dz, state('cherry_leaves' if cherry else 'oak_leaves', distance='1', persistent='true', waterlogged='false'))


def dorm():
    x0, x1, z0, z1, levels = -128, -72, 45, 87, [64, 70, 76, 82]
    BUILDINGS.append(dict(id='dorm', minX=x0, maxX=x1, minZ=z0, maxZ=z1, floors=levels+[88]))
    shell(x0, z0, x1, z1, levels, BRICK)
    for y in levels:
        for z in [z0-1, z1+1]: fill(x0-1, y+5, z, x1+1, y+5, z, TRIM)
        for x in range(x0+3, x1-2, 7):
            window(x, y+1, z0, 3, 3); window(x, y+1, z1, 3, 3)
        for z in range(z0+3, z1-2, 7):
            window(x0, y+1, z, 3, 3, 'east'); window(x1, y+1, z, 3, 3, 'west')
        for x in [x0, -106, -95, x1]: fill(x, y, z1, x, y+4, z1, TRIM)
        for x in [-106, -95]: fill(x, y, z0+1, x, y+4, z1-1, PLASTER)
        fill(-95, y, 49, -95, y+3, 55, None)
        fill(-95, y, 63, -95, y+3, 66, None)
        for z in [51, 64, 79]: light(-101, y+4, z)
        fill(-90, y, 46, -90, y+4, 64, PLASTER)
        if y in (70, 76):
            for zz in [64, 77]:
                fill(x0+1, y, zz, -107, y+4, zz, PLASTER)
                fill(-89, y, zz, x1-1, y+4, zz, PLASTER)
            for zz in [58, 70, 81]:
                door(-106, y, zz, 'east'); door(-95, y, zz, 'west')
                bed(-123, y, zz); table(-119, y, zz-4, 4, 1); bench(-118, y, zz-2, 1)
                fill(-109, y, zz-4, -108, y+2, zz-2, WOOD); light(-117, y+4, zz-1)
                if zz > 64:
                    bed(-77, y, zz); table(-87, y, zz-4, 4, 1); light(-83, y+4, zz-1)
        elif y == 82:
            door(-106, y, 67, 'east', True)
            fill(-126, y+1, 47, -108, y+3, 47, BLUE)
            for x in [-123, -118, -113]: fill(x, y+2, 48, x+2, y+3, 48, state('cyan_stained_glass'))
            table(-122, y, 58, 12, 4)
            for z in [56, 63]: bench(-120, y, z, 8)
            door(-95, y, 75, 'west')
            for x in [-85, -78]: fill(x, y, 71, x+1, y+2, 73, TRIM)
            light(-115, y+4, 60); light(-82, y+4, 75)
    door(-101, 64, z1, double=True); fill(-104, 63, 88, -96, 63, 96, PAVING)
    fill(-107, 64, 67, -106, 67, 73, None)
    for x, z in [(-123, 58), (-123, 66), (-115, 58)]: bench(x, 64, z, 5)
    table(-121, 64, 62, 9, 2)
    fill(-124, 64, 48, -111, 64, 49, WOOD); fill(-120, 65, 48, -115, 67, 48, IRON)
    light(-117, 68, 62)
    fill(-94, 64, 67, -73, 68, 67, PLASTER)
    door(-95, 64, 72, 'west', True); door(-90, 64, 64, 'north', True)
    fill(-86, 64, 47, -74, 64, 48, WOOD)
    fill(-86, 65, 47, -74, 65, 48, state('smooth_quartz_slab', type='bottom', waterlogged='false'))
    put(-78, 65, 48, state('water_cauldron', level='3'))
    fill(-87, 64, 50, -86, 66, 51, state('iron_block'))
    table(-85, 64, 58, 8, 2); bench(-84, 64, 61, 6)
    fill(-87, 64, 69, -75, 67, 69, BOOKS); table(-85, 64, 77, 7, 2); bench(-84, 64, 80, 5)
    light(-82, 68, 58); light(-82, 68, 77)
    fill(-89, 70, 46, -73, 74, 46, PLASTER)
    for x in [-87, -83, -79]:
        fill(x, 70, 48, x+2, 72, 49, TRIM); put(x+1, 70, 52, state('water_cauldron', level='3'))
    light(-81, 74, 57)
    door(-90, 70, 54, 'east', True)
    fill(x0, 87, z0, x1, 87, z1, ROOF)
    for z in [z0, z1]: fill(x0, 88, z, x1, 88, z, STONE)
    for x in [x0, x1]: fill(x, 88, z0, x, 88, z1, STONE)
    for x in [-122, -115, -108]:
        fill(x, 88, 70, x+3, 88, 80, WOOD)
        fill(x+1, 88, 71, x+2, 88, 79, state('farmland', moisture='7'))
        for z in [72, 75, 78]: put(x+1, 89, z, state('wheat', age='7'))
    for y in levels: stairs('Dormitório: escada '+str(y), -94, 62, y, 6, 3)
    fill(-96, 88, 48, -89, 91, 48, PLASTER)
    for x in [-96, -89]: fill(x, 88, 48, x, 91, 64, PLASTER)
    fill(-96, 92, 48, -89, 92, 64, ROOF)
    for args in [('dorm','entrada',-100,64,92),('lounge','lounge / térreo',-111,64,70),('kitchen','cozinha / térreo',-81,64,63),('reading','leitura / térreo',-81,64,83),('bedroom','seu quarto / 2º piso',-111,70,70),('bathroom','banheiros / 2º piso',-83,70,59),('command','comando / 4º piso',-111,82,69),('roof','horta / cobertura',-100,88,70)]:
        id, label, x, y, z = args; place(id, 'Dormitório • '+label, x, y, z)


def school():
    BUILDINGS.append(dict(id='school', minX=-50, maxX=50, minZ=-135, maxZ=-82, floors=[64,71,78,85]))
    for y in [64,71,78]:
        shell(-50,-135,50,-82,[y],PLASTER,SCHOOL)
        fill(-50,y+6,-135,50,y+6,-82,SCHOOL)
        fill(-49,y,-95,-14,y+5,-95,PLASTER); fill(14,y,-95,49,y+5,-95,PLASTER)
        for x in [-32,32]: fill(x,y,-134,x,y+5,-96,PLASTER)
        for x in [-41,-23,22,40]: door(x,y,-95,double=True)
        for x in range(-47,49,6):
            window(x,y+1,-82,3,4); window(x,y+1,-135,3,4)
        fill(-50,y+5,-81,50,y+5,-81,TRIM)
        for x in [-50,-13,13,50]: fill(x,y,-81,x,y+5,-81,TRIM)
        for x in range(-44,45,11): light(x,y+5,-88)
        for x0 in [-48,-30,15,34]:
            if x0<0:
                for x in [x0+3,x0+8]:
                    for z in [-125,-118,-111]: table(x,y,z,2,1); bench(x,y,z+2,1)
                fill(x0+2,y+1,-132,x0+11,y+3,-132,GREEN)
            else:
                for x in range(x0+1,min(x0+12,47),5): fill(x,y,-132,x+2,y+3,-131,BOOKS if y==64 else WOOD)
                table(x0+3,y,-121,7,3); bench(x0+3,y,-117,6)
            light(x0+7,y+5,-121)
        for x in [-13,13]: fill(x,y,-134,x,y+5,-96,PLASTER)
    for y in [64,71,78]:
        stairs('Gekkoukan: oeste '+str(y),-10,-97,y,7,3)
        stairs('Gekkoukan: leste '+str(y),7,-97,y,7,3)
    for x in [-8,-3,3,8]: fill(x,64,-81,x,84,-81,TRIM)
    window(-7,67,-82,15,17)
    for y in [70,77,84]: fill(-8,y,-82,8,y,-82,TRIM)
    door(-1,64,-82,double=True)
    # Match the door opening through the projecting entrance columns.
    fill(-1,64,-81,0,66,-81,None)
    fill(-4,63,-81,4,63,-33,PAVING)
    for x in [-19,19]:
        for z in [-72,-57,-42]:
            fill(x-3,63,z-3,x+3,63,z+3,GRASS); tree(x,z,True)
        for z in [-64,-49,-34]: lamp(int(x*.55),z)
    for x in [-8,8]: fill(x,64,-32,x,68,-32,TRIM)
    for z in [-135,-82]: fill(-50,85,z,50,85,z,STONE)
    for x in [-50,50]: fill(x,85,-135,x,85,-82,STONE)
    place('gekkoukan','Gekkoukan • entrada',0,64,-36)
    place('school_hallway','Gekkoukan • corredor central',0,64,-88)
    place('school_classroom','Gekkoukan • sala 2-F / 2º piso',-23,71,-99)
    place('school_library','Gekkoukan • biblioteca',22,64,-99)
    place('school_faculty','Gekkoukan • sala dos professores',-41,64,-99)
    place('school_council','Gekkoukan • conselho / 2º piso',40,71,-99)
    put(6,64,-37,29)


SHOPS=['aohige','police','mayoido','blue','chagall','mandragora','arcade','escapade','kenneth']
TITLES=['Aohige Pharmacy','Tatsumi East Police','Mayoido Antiques','Be Blue V','Chagall Café','Mandragora','Game Parade','Club Escapade','Kenneth Arnold']


def shop(id,x0,z0,x1,z1,facing,floor=64):
    shell(x0,z0,x1,z1,[floor],BRICK,SCHOOL)
    cx,cz=(x0+x1)//2,(z0+z1)//2
    if facing in ('south','north'):
        zz=z1 if facing=='south' else z0
        fill(cx-2,floor,zz,cx+2,floor+3,zz,None)
        counter=(cx+2,floor,cz); arrival=(cx,floor,zz-(2 if facing=='south' else -2))
        for x in range(x0+2,x1-1,3):
            if abs(x-cx)>3: window(x,floor+1,zz,2,2)
        fill(cx-4,floor+4,zz,cx+4,floor+4,zz,[BLUE,RED,GREEN][SHOPS.index(id)%3])
    else:
        xx=x1 if facing=='east' else x0
        fill(xx,floor,cz-2,xx,floor+3,cz+2,None)
        counter=(cx,floor,cz+2); arrival=(xx-(2 if facing=='east' else -2),floor,cz)
        window(xx,floor+1,z0+2,2,2,facing); window(xx,floor+1,z1-3,2,2,facing)
    for dx in [-1,0,1]: put(counter[0]+dx,floor,counter[2],WOOD)
    put(*counter,20+SHOPS.index(id))
    if id in ('aohige','mayoido','police'):
        for x in range(x0+2,x1-1,3): fill(x,floor,z0+1,x+1,floor+2,z0+1,TRIM if id=='aohige' else BOOKS if id=='mayoido' else IRON)
    elif id=='chagall':
        for z in [z0+3,z1-3]: table(x0+5,floor,z,3,1); bench(x0+5,floor,z+2,2)
    elif id=='arcade':
        for z in range(z0+2,z1-1,3): fill(x1-2,floor,z,x1-1,floor+1,z+1,BLUE); light(x1-2,floor+2,z)
    elif id in ('escapade','mandragora'):
        fill(x0+1,floor-1,z0+1,x1-1,floor-1,z1-1,BLUE if id=='mandragora' else RED)
        bench(x0+2,floor,z1-2,min(6,x1-x0-3))
    elif id=='blue':
        for z in [z0+3,z1-3]: bench(x0+2,floor,z,4)
    light(cx,floor+4,cz)
    place('shop_'+id,TITLES[SHOPS.index(id)],*arrival)


def mall():
    fill(53,63,26,133,63,104,PAVING); cx,cz=93,65
    for x in range(69,117):
        for z in range(41,87):
            if any(abs(math.hypot(x-cx,z-cz)-v)<.6 for v in [8,17,22]): put(x,63,z,WOOD)
    for y in range(70,82):
        for z in [26,104]: fill(53,y,z,133,y,z,BRICK)
        for x in [53,133]: fill(x,y,26,x,y,104,BRICK)
    for x in range(57,132,8):
        for z in [26,104]: window(x,73,z,5,6); fill(x-1,64,z,x-1,82,z,TRIM)
    for z in range(30,102,8):
        for x in [53,133]: window(x,73,z,5,6,'east'); fill(x,64,z-1,x,82,z-1,TRIM)
    # Tall glazed rotunda over the central plaza, with radial ribs and arched window bands.
    for x in range(cx-28,cx+29):
        for z in range(cz-28,cz+29):
            r=math.hypot(x-cx,z-cz)
            if r>28: continue
            a=(math.atan2(z-cz,x-cx)+math.tau)%math.tau
            sector=a*12/math.tau;rib=min(sector%1,1-sector%1)<.035
            y=82+round(8*math.sqrt(max(0,1-(r/28)**2)))
            put(x,y,z,TRIM if rib or 18<r<19 else GLASS)
            if 27<=r<=28:
                for yy in range(70,83):
                    arch=79+3*math.sin((sector%1)*math.pi)
                    put(x,yy,z,TRIM if rib else GLASS if 73<=yy<=arch else BRICK)
    shop('police',55,28,78,42,'south'); shop('aohige',79,28,102,42,'south'); shop('mayoido',103,28,131,42,'south')
    shop('blue',54,44,68,63,'east'); shop('kenneth',54,65,68,84,'east')
    shop('arcade',118,44,132,63,'west'); shop('chagall',118,65,132,84,'west')
    shop('escapade',55,87,79,102,'north'); shop('mandragora',104,87,131,102,'north',67)
    stairs('Paulownia: Mandragora',114,83,64,3,4,'south')
    fill(114,66,86,119,66,89,SCHOOL); fill(114,67,86,119,70,89,None)
    fill(86,64,103,100,71,104,None); fill(86,63,103,100,63,110,PAVING)
    for x in [85,101]: fill(x,64,103,x,74,104,TRIM)
    fill(85,74,103,101,74,104,TRIM)
    for x in range(cx-6,cx+7):
        for z in range(cz-6,cz+7):
            r=math.hypot(x-cx,z-cz)
            if r<=6: put(x,63,z,TRIM); put(x,64,z,TRIM if r>=4.7 else WATER)
    fill(cx,64,cz,cx,68,cz,TRIM); fill(cx-1,67,cz-1,cx+1,67,cz+1,TRIM)
    for x,z in [(75,48),(111,48),(75,81),(111,81)]: lamp(x,z)
    bench(78,64,67,5); bench(104,64,67,5)
    fill(105,64,86,108,68,86,PLASTER); put(106,64,86,30); put(106,65,86,31)
    place('paulownia','Paulownia Mall • praça',93,64,80)


def iwatodai():
    fill(-137,63,-57,-62,63,-9,PAVING)
    for y in [64,70,76]:
        shell(-127,-54,-73,-30,[y],BRICK,SCHOOL); fill(-128,y-1,-29,-72,y-1,-24,SCHOOL)
        for x in [-122,-109,-96,-83]:
            window(x,y+1,-30,7,3); door(x+2,y,-30,double=True)
            fill(x-1,y+4,-29,x+8,y+4,-29,[RED,GOLD,BLUE,GREEN][(x+122)//13])
            table(x,y,-37,5,2); bench(x,y,-34,4); light(x+3,y+4,-27)
        for side in [-134,-69]:
            fill(side,y-1,-43,side+3,y-1,-23,SCHOOL)
            fill(min(side,-127),y-1,-43,max(side+3,-73),y-1,-42,SCHOOL)
    for y in [64,70]:
        stairs('Iwatodai: oeste '+str(y),-134,-27,y,6,3)
        stairs('Iwatodai: leste '+str(y),-69,-27,y,6,3)
    fill(-102,63,-24,-98,63,-4,GOLD)
    for x in [-125,-78]: bench(x,64,-15,6); lamp(x,-11)
    place('iwatodai','Iwatodai • rua comercial',-100,64,-17)


def station():
    shell(66,-107,136,-47,[64],PLASTER,SCHOOL); fill(67,69,-106,135,69,-48,None)
    for x in range(67,136):
        for z in range(-107,-46): put(x,75,z,ROOF if z%8==0 else GLASS)
    fill(91,64,-47,106,68,-47,None)
    for x in range(71,132,10):
        window(x,65,-47,5,3); fill(x,64,-70,x,74,-70,TRIM); light(x,73,-55)
    fill(70,67,-105,132,67,-86,STONE)
    fill(70,68,-95,132,68,-94,GOLD); fill(70,68,-101,132,68,-100,IRON)
    stairs('Estação: plataforma',98,-82,64,4,5)
    for x in [76,114]: bench(x,64,-58,6); fill(x,64,-50,x+2,65,-50,BLUE); light(x+1,66,-50)
    fill(91,63,-46,106,63,-38,PAVING)
    place('station','Port Island Station • saguão',100,64,-54)


def shrine():
    fill(-133,63,-152,-66,63,-86,GRASS); fill(-104,63,-115,-96,63,-85,PAVING)
    fill(-124,63,-149,-76,63,-123,STONE); fill(-124,70,-149,-76,70,-123,STONE)
    stairs('Naganaki: escadaria',-104,-116,64,7,9)
    shell(-115,-145,-85,-129,[71],WOOD,WOOD); fill(-103,71,-129,-97,74,-129,None)
    for step in range(5): fill(-118+step,77+step,-148+step,-82-step,77+step,-126-step,GREEN)
    for x in [-108,-92]: fill(x,64,-94,x,71,-94,RED)
    fill(-112,70,-94,-88,70,-94,RED); fill(-113,72,-94,-87,72,-94,IRON)
    for x,z in [(-127,-94),(-74,-97),(-128,-120),(-70,-123)]: tree(x,z)
    lamp(-109,-113); lamp(-91,-113)
    place('naganaki','Santuário Naganaki • escadaria',-100,64,-108)


def maps():
    layers=[dict(id='map',minX=BOUNDS[0],minZ=BOUNDS[1],maxX=BOUNDS[2],maxZ=BOUNDS[3],floor=-1)]
    for b in BUILDINGS:
        for i,y in enumerate(b['floors']): layers.append(dict(id=b['id']+'_'+str(i),minX=b['minX']-2,maxX=b['maxX']+2,minZ=b['minZ']-2,maxZ=b['maxZ']+2,floor=y))
    target=ASSETS/'textures/gui/city'; target.mkdir(parents=True,exist_ok=True)
    ordered=sorted(CELLS.items(),key=lambda v:v[0][1])
    for layer in layers:
        im=Image.new('RGB',(512,512),'#06172a'); d=ImageDraw.Draw(im)
        x0,z0,x1,z1,y=(layer[k] for k in ['minX','minZ','maxX','maxZ','floor'])
        for (x,yy,z),c in ordered:
            if not(x0<=x<=x1 and z0<=z<=z1) or yy<63 or (y>=0 and not y-1<=yy<=y+2): continue
            mat=c[3]; color='#234456' if yy==(63 if y<0 else y-1) else '#6dabb5'
            if mat in (PAVING,SCHOOL,WOOD): color='#304f69'
            if 20<=mat<=30: color='#e6bc64'
            px,pz=(x-x0)/(x1-x0+1)*512,(z-z0)/(z1-z0+1)*512
            d.rectangle((px,pz,px+512/(x1-x0+1),pz+512/(z1-z0+1)),fill=color)
        im.save(target/(layer['id']+'.png'))
    (ASSETS/'city-map.json').write_text(json.dumps(layers,indent=2),encoding='utf8')


def build():
    fill(-150,62,-155,150,62,120,STONE); fill(-150,63,-155,150,63,120,GRASS)
    fill(-5,63,-155,5,63,116,PAVING); fill(-143,63,-7,143,63,7,PAVING)
    fill(-143,63,94,143,63,110,PAVING); fill(-106,63,-89,-94,63,95,PAVING)
    fill(94,63,-44,106,63,27,PAVING)
    for x in [-42,42]:
        for z in [19,52,83]: tree(x,z)
    for z in range(-15,93,18): lamp(-8,z); lamp(8,z)
    dorm(); school(); mall(); iwatodai(); station(); shrine()
    for p in PLACES:
        assert (p['x'],p['y']-1,p['z']) in CELLS,('arrival lacks floor',p)
        assert all((p['x'],p['y']+dy,p['z']) not in CELLS for dy in [0,1]),('occupied arrival',p)
    plan=dict(revision=2,cells=list(CELLS.values()),palette=PALETTE,meshes=[],places=PLACES,
              doors=[dict(x=6,y=64,z=-37,target='tartarus')],buildings=BUILDINGS,routes=ROUTES)
    DATA.mkdir(parents=True,exist_ok=True)
    (DATA/'plan.json.gz').write_bytes(gzip.compress(json.dumps(plan,ensure_ascii=False,separators=(',',':')).encode(),mtime=0))
    maps()
    print(f'City revision 2: {len(CELLS)} native blocks, {len(PALETTE)} states, {len(PLACES)} destinations; no architecture meshes')


if __name__=='__main__': build()
