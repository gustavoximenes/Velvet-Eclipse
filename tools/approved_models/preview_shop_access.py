"""Preview the actual baked shop resources emitted by cityShopAccessTest."""
from pathlib import Path
import json, sys
import numpy as np
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / 'tools/velvet_room'))
import portrait_raster as r

REPORT = ROOT / 'build/reports/shop-access'
ASSET = ROOT / 'src/main/resources/assets/velvet_eclipse'
points = {p['shop']: p for p in json.loads((ROOT / 'src/main/resources/data/velvet_eclipse/city/shop-access.json').read_text())}
r.W, r.H = 360, 480
sheet = Image.new('RGB', (1440, 612), '#0d1930')
draw = ImageDraw.Draw(sheet)
font = ImageFont.truetype(str(ASSET / 'font/body.ttf'), 20)
small = ImageFont.truetype(str(ASSET / 'font/body.ttf'), 16)
title = ImageFont.truetype(str(ASSET / 'font/display.ttf'), 32)
draw.text((20, 12), 'PAULOWNIA MALL | ACESSO ÀS LOJAS', font=title, fill='#e7f7fa')
for col, (shop, label) in enumerate([('police', 'Kurosawa · revisão 03'), ('aohige', 'Aohige · revisão 02'), ('mayoido', 'Mayoido · revisão 02'), ('blue', 'Indicador · seis lojas')]):
    point = points[shop]
    tex = np.array(Image.open(ASSET / f'textures/entity/{point["asset"]}.png').convert('RGBA'))
    faces = []
    for face in json.loads((REPORT / f'{shop}-native.json').read_text()):
        v = np.array(face['vertices'])
        p = v[:, :3] * point['scale'] / 16
        uv = v[:, 3:5] * np.array([tex.shape[1], tex.shape[0]]) / 512
        if np.dot(np.cross(p[1]-p[0], p[2]-p[0]), face['normal']) < 0:
            p, uv = p[::-1], uv[::-1]
        faces.append((p, uv, tex, None))
    camera = r.Camera([.45, 1.23, -4.1], [0, 1.03, 0], 34) if point['npc'] else r.Camera([.15, .49, -1.85], [0, .43, 0], 34)
    frame = r.raster(Image.new('RGB', (r.W, r.H), '#dce6e9'), faces, camera, brightness=1.08)
    sheet.paste(frame, (col*r.W, 88))
    draw.text((col*r.W+14, 58), label, font=font, fill='#9de8f2')
draw.text((18, 582), 'Recursos reais • render offline • NPCs na mesma escala • indicador ampliado • sem captura do Minecraft', font=small, fill='#9de8f2')
sheet.save(REPORT / 'shop-access-preview.png')
print(REPORT / 'shop-access-preview.png')
