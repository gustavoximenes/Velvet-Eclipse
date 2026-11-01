"""Package the reviewable study only. Production resources are never written."""
from pathlib import Path
import json,hashlib,shutil,zipfile,re
from PIL import Image,ImageChops
ROOT=Path(__file__).resolve().parent;O=ROOT/'draft';BASE=ROOT.parents[1];DEST=BASE/'outputs/angel-auditoria-03'
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def main():
 for name in ['validation.json','native-validation.json','framing-validation.json','menu-validation.json','connectivity-validation.json']:
  assert json.loads((O/name).read_text())['status']=='passed',name
 for report in ['framing-validation.json','menu-validation.json','connectivity-validation.json']:
  for name,h in json.loads((O/report).read_text())['source_hashes'].items():assert sha(O/name)==h,name
 media={}
 for kind,length in [('idle',4800),('physical',2600),('magic',3000),('expressions',9600),('turntable',3000),('menu',4800),('joints',10400)]:
  p=O/f'angel-{kind}-03.gif';duration=0;different=False
  with Image.open(p) as im:
   first=im.convert('RGB');frames=im.n_frames
   for i in range(frames):
    im.seek(i);im.load();duration+=im.info.get('duration',0)
    if i and ImageChops.difference(first,im.convert('RGB')).getbbox():different=True
   assert frames>1 and different and duration==length,(kind,frames,duration)
   media[kind]={'frames':frames,'duration_ms':duration,'dimensions':[im.width,im.height],'sha256':sha(p)}
  assert p.with_suffix('.mp4').stat().st_size>1000
 html=(ROOT/'index.html').read_text(encoding='utf-8')
 for link in re.findall(r'(?:src|href)="([^"#]+)"',html):
  if not link.startswith(('http:','https:','data:')):assert (ROOT/link).is_file(),link
 report={'status':'passed','media':media,'offline':True,'integration':False,'new_jar_generated':False}
 (O/'media-validation.json').write_text(json.dumps(report,indent=2))
 files=list(ROOT.glob('*.py'))+list(ROOT.glob('*.java'))+[ROOT/'README.md',ROOT/'index.html']
 files+=list(O.iterdir())
 files+=[ROOT/'references'/name for name in ['reload-compendium.jpg','reload-portrait.jpg','MODELING.md','sources.json']]
 files=[p for p in files if p.is_file() and p.name!='manifest.json']
 manifest={p.relative_to(ROOT).as_posix():sha(p) for p in files}
 (O/'manifest.json').write_text(json.dumps(manifest,indent=2));files.append(O/'manifest.json')
 for p in files:
  rel=p.relative_to(ROOT)
  if rel.parts[0] in ('draft','references') or rel.name in ('README.md','index.html'):
   target=DEST/rel;target.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(p,target)
 zpath=BASE/'outputs/angel-modelo-03.zip'
 with zipfile.ZipFile(zpath,'w',zipfile.ZIP_DEFLATED) as z:
  for p in files:z.write(p,'angel-study-03/'+p.relative_to(ROOT).as_posix())
 with zipfile.ZipFile(zpath) as z:
  assert z.testzip() is None
  for rel,h in manifest.items():assert hashlib.sha256(z.read('angel-study-03/'+rel)).hexdigest()==h,rel
 result={'status':'passed','gallery':str(DEST/'index.html'),'package':str(zpath),'files':len(files),'bytes':zpath.stat().st_size,'sha256':sha(zpath)}
 (ROOT/'work/delivery.json').write_text(json.dumps(result,indent=2));print(json.dumps(result,indent=2))
if __name__=='__main__':main()
