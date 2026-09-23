"""Create the review ZIP from an explicit asset list, with checksums."""
from pathlib import Path
import hashlib,json,zipfile

ROOT=Path(__file__).resolve().parent
DEST=ROOT.parents[1]/'outputs'
FILES=['README.md','build_model.py','render_model.py','present_model.py','verify_model.py','package_model.py']
FILES+=['draft/'+n for n in ['arsene.bbmodel','arsene.geo.json','arsene.animation.json','arsene.png','arsene-approval.png','arsene-front.png','arsene-three-quarter.png','arsene-back.png','arsene-side.png','arsene-details.png','arsene-idle.gif','arsene-magic.gif','arsene-physical.gif','arsene-magic-poses.png','arsene-physical-poses.png','validation.json','model-info.json','parts.json']]

def main():
 assert json.loads((ROOT/'draft/validation.json').read_text(encoding='utf-8'))['status']=='passed'
 assert all((ROOT/f).is_file() for f in FILES)
 manifest={f:hashlib.sha256((ROOT/f).read_bytes()).hexdigest() for f in FILES}
 (ROOT/'SHA256.json').write_text(json.dumps(manifest,indent=2)+'\n',encoding='utf-8')
 DEST.mkdir(exist_ok=True);target=DEST/'arsene-modelo-02.zip'
 with zipfile.ZipFile(target,'w',zipfile.ZIP_DEFLATED,compresslevel=9) as z:
  for f in FILES+['SHA256.json']:z.write(ROOT/f,'arsene/'+f)
 with zipfile.ZipFile(target) as z:
  assert z.testzip() is None
  for f,digest in manifest.items():assert hashlib.sha256(z.read('arsene/'+f)).hexdigest()==digest
 checksum=hashlib.sha256(target.read_bytes()).hexdigest()
 (DEST/'arsene-modelo-02.zip.sha256').write_text(checksum+'  '+target.name+'\n',encoding='utf-8')
 print(json.dumps({'zip':str(target),'files':len(FILES)+1,'bytes':target.stat().st_size,'sha256':checksum}))

if __name__=='__main__':main()
