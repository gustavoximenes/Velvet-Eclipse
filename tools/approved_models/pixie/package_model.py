"""Package and verify only the standalone study and its renders."""
import hashlib,json,zipfile
from pathlib import Path
from PIL import Image,ImageDraw,ImageFont

ROOT=Path(__file__).resolve().parent;OUT=ROOT/'draft';BASE=ROOT.parents[1]
target=BASE/'outputs/pixie-modelo-03.zip'
expected={'idle':80,'magic':70,'physical':45,'animations':100};media={}
sheet=Image.new('RGB',(1440,1230),'#10182b');font=ImageFont.truetype('C:/Windows/Fonts/segoeui.ttf',16)
for row,kind in enumerate(('idle','magic','physical')):
    path=OUT/f'pixie-{kind}-03.gif'
    with Image.open(path) as gif:
        assert gif.n_frames==expected[kind]
        media[path.name]={'frames':gif.n_frames,'size':gif.size}
        for col,index in enumerate([0,gif.n_frames//4,gif.n_frames//2,3*gif.n_frames//4]):
            gif.seek(index);im=gif.convert('RGB');im.thumbnail((340,370))
            x=col*360;y=row*410
            sheet.paste(im,(x+(360-im.width)//2,y+30))
            ImageDraw.Draw(sheet).text((x+12,y+5),f'{kind} / quadro {index}',font=font,fill='#c1e6df')
sheet.save(OUT/'gif-review.png')
with Image.open(OUT/'pixie-animations-03.gif') as gif:
    assert gif.n_frames==expected['animations']
    media['pixie-animations-03.gif']={'frames':gif.n_frames,'size':gif.size}
head_sheet=Image.new('RGB',(1440,740),'#10182b')
for row,(name,count) in enumerate([('pixie-face-03.gif',64),('pixie-head-turntable-03.gif',72)]):
    with Image.open(OUT/name) as gif:
        assert gif.n_frames==count
        media[name]={'frames':gif.n_frames,'size':gif.size}
        for col,index in enumerate([0,count//4,count//2,3*count//4]):
            gif.seek(index);im=gif.convert('RGB');im.thumbnail((350,328))
            x=col*360;y=row*370
            head_sheet.paste(im,(x+(360-im.width)//2,y+31))
            ImageDraw.Draw(head_sheet).text((x+12,y+5),f'cabeça / quadro {index}',font=font,fill='#c1e6df')
head_sheet.save(OUT/'head-gif-review.png')
assert json.loads((OUT/'validation.json').read_text())['mod_files_unchanged']==4190
assert json.loads((OUT/'native-validation.json').read_text())['status']=='passed'
files=[p for p in ROOT.rglob('*') if p.is_file() and '__pycache__' not in p.parts and p.name!='source-before.json' and p.suffix not in ('.tmp','.pyc','.class')]
manifest={p.relative_to(ROOT).as_posix():hashlib.sha256(p.read_bytes()).hexdigest() for p in files}
with zipfile.ZipFile(target,'w',zipfile.ZIP_DEFLATED,compresslevel=7) as z:
    for p in files:z.write(p,'pixie-rework-03/'+p.relative_to(ROOT).as_posix())
    z.writestr('pixie-rework-03/SHA256.json',json.dumps(manifest,indent=2))
with zipfile.ZipFile(target) as z:
    assert z.testzip() is None
    for path,digest in manifest.items():assert hashlib.sha256(z.read('pixie-rework-03/'+path)).hexdigest()==digest
digest=hashlib.sha256(target.read_bytes()).hexdigest()
target.with_suffix('.zip.sha256').write_text(digest+'  '+target.name+'\n')
print(json.dumps({'zip':str(target),'bytes':target.stat().st_size,'files':len(files),'sha256':digest,'media':media},indent=2))
