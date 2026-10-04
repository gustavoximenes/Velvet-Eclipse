"""Revision comparison and head inspection from the delivered model files."""
import json,math
import numpy as np
from PIL import Image
import render_model as r
from present_model import text,savegif,NAVY,CYAN

OUT=r.OUT

def load(path):
    r.data=json.loads((path/'pixie.geo.json').read_text())['minecraft:geometry'][0]
    r.texture=np.array(Image.open(path/'pixie.png').convert('RGBA'))/255
    r.animations=json.loads((path/'pixie.animation.json').read_text())['animations']
    r.head_bones={'head'}
    for b in r.data['bones']:
        if b.get('parent') in r.head_bones:r.head_bones.add(b['name'])

def framing(paths,yaws,bare=False):
    vertices=[]
    for path in paths:
        load(path)
        for yaw in yaws:vertices.extend(q for q,*_ in r.surfaces(yaw,0,head=True,bare=bare))
    a=np.concatenate(vertices);lo=a.min(0);hi=a.max(0)
    lo-=2;hi+=2
    load(OUT)
    return lo,hi

def main():
    previous=r.ROOT.parent/'pixie-rework-02'/'draft'
    camera_bounds=framing([previous,OUT],[0,-35])
    im=Image.new('RGB',(1600,790),NAVY)
    text(im,(28,17),'PIXIE  /  CABELO E ROSTO',35,bold=True)
    text(im,(30,67),'ANTES E DEPOIS • MESMA CÂMERA, POSE E ESCALA',18,CYAN)
    for col,(path,yaw) in enumerate([(previous,0),(OUT,0),(previous,-35),(OUT,-35)]):
        load(path)
        im.paste(r.render(yaw,386,602,head=True,pitch=0,bounds=camera_bounds),(7+400*col,111))
        text(im,(23+400*col,125),'ANTERIOR • 02' if path==previous else 'REVISÃO • 03',17,NAVY,True)
    load(OUT)
    text(im,(29,741),'Mandíbula com perfil inclinado; feições menos salientes; cabelo com volumes unidos.',19,CYAN)
    im.save(OUT/'pixie-face-comparison-03.png')
    im=Image.new('RGB',(1440,890),NAVY)
    text(im,(25,17),'PIXIE  /  REVISÃO 03',34,bold=True)
    text(im,(28,65),'CABELO CURTO CASTANHO-AVERMELHADO • FEIÇÕES EM VOLUME',17,CYAN)
    for col,(yaw,bare,clay,label) in enumerate([(0,False,False,'FRENTE'),(-35,False,False,'TRÊS QUARTOS'),(-90,False,False,'PERFIL'),(-35,True,True,'GEOMETRIA DO ROSTO')]):
        im.paste(r.render(yaw,350,690,head=True,pitch=0,bare=bare,clay=clay),(5+360*col,112))
        text(im,(18+360*col,125),label,16,NAVY,True)
    text(im,(27,833),'Prévia da geometria entregue • cuboides nativos e Box UV • sem integração ao mod',19,CYAN)
    im.save(OUT/'pixie-face-03.png')
    print('Head stills and comparison saved',flush=True)
    face_bounds=framing([OUT],np.linspace(-66,66,25))
    bare_bounds=framing([OUT],np.linspace(-66,66,25),bare=True)
    frames=[]
    for i in range(64):
        yaw=66*math.sin(i/64*math.tau)
        im=Image.new('RGB',(1040,630),NAVY)
        im.paste(r.render(yaw,510,528,head=True,pitch=0,bounds=face_bounds),(5,51))
        im.paste(r.render(yaw,510,528,head=True,pitch=0,bounds=bare_bounds,bare=True,clay=True),(525,51))
        text(im,(20,13),'CABELO E ROSTO',21,bold=True)
        text(im,(540,13),'GEOMETRIA SEM CABELO E TEXTURA',19,bold=True)
        text(im,(20,597),'PIXIE • revisão 03 • render dos arquivos editáveis • sem retoques',18,CYAN)
        frames.append(im)
    savegif(OUT/'pixie-face-03.gif',frames,80)
    print('Head geometry GIF saved',flush=True)
    all_bounds=framing([OUT],range(0,360,10))
    frames=[]
    for i in range(72):
        im=Image.new('RGB',(660,720),NAVY)
        im.paste(r.render(-i*5,650,611,head=True,pitch=0,bounds=all_bounds),(5,51))
        text(im,(20,13),'PIXIE  /  GIRO COMPLETO DA CABEÇA',21,bold=True)
        text(im,(20,684),'Revisão 03 • volume e encaixe do cabelo',18,CYAN)
        frames.append(im)
    savegif(OUT/'pixie-head-turntable-03.gif',frames,80)
    print('Head turntable GIF saved',flush=True)

if __name__=='__main__':main()
