"""Bundle the standalone model only; no mod build, install, commit or push."""
from pathlib import Path
import hashlib, json, zipfile
from PIL import Image, ImageDraw, ImageFont

ROOT=Path(__file__).resolve().parent
BASE=ROOT.parents[1]
OUT=ROOT/'draft'
target=BASE/'outputs/elizabeth-modelo-05.zip'
font=ImageFont.truetype('C:/Windows/Fonts/segoeui.ttf',15)
sheet=Image.new('RGB',(1500,890),'#111b31')
media={}
for row,filename in enumerate(['elizabeth-turntable-05.gif','elizabeth-face-05.gif']):
    gif=Image.open(OUT/filename)
    expected=72 if row==0 else 64
    assert gif.n_frames==expected,(filename,gif.n_frames)
    media[filename]={'frames':gif.n_frames,'size':gif.size}
    for i,frame in enumerate([0,gif.n_frames//4,gif.n_frames//2,3*gif.n_frames//4]):
        gif.seek(frame);im=gif.convert('RGB');im.thumbnail((367,400))
        sheet.paste(im,(i*375+(367-im.width)//2,row*445+26))
        ImageDraw.Draw(sheet).text((i*375+12,row*445+5),f'{filename} / {frame}',font=font,fill='#cce3f3')
sheet.save(OUT/'gif-review.png')
with Image.open(OUT/'elizabeth-idle-05.gif') as idle:
    assert idle.n_frames==48
    media['elizabeth-idle-05.gif']={'frames':idle.n_frames,'size':idle.size}
assert json.loads((OUT/'validation.json').read_text())['mod_files_unchanged']==4190
assert json.loads((OUT/'native-validation.json').read_text())['status']=='passed'
manifest={}
excluded={'source-before.json','prepare_reference.py'}
files=[p for p in ROOT.rglob('*') if p.is_file() and '__pycache__' not in p.parts and p.name not in excluded and p.suffix not in ('.class','.pyc')]
for p in files:manifest[str(p.relative_to(ROOT)).replace('\\','/')]=hashlib.sha256(p.read_bytes()).hexdigest()
with zipfile.ZipFile(target,'w',zipfile.ZIP_DEFLATED,compresslevel=7) as z:
    for p in files:z.write(p,'elizabeth-rework-05/'+str(p.relative_to(ROOT)))
    z.writestr('elizabeth-rework-05/SHA256.json',json.dumps(manifest,indent=2))
with zipfile.ZipFile(target) as z:
    assert z.testzip() is None
    for name,digest in manifest.items():assert hashlib.sha256(z.read('elizabeth-rework-05/'+name)).hexdigest()==digest
digest=hashlib.sha256(target.read_bytes()).hexdigest()
target.with_suffix('.zip.sha256').write_text(digest+'  '+target.name+'\n',encoding='ascii')
print(json.dumps({'zip':str(target),'bytes':target.stat().st_size,'sha256':digest,'files':len(files),'media':media},indent=2))
