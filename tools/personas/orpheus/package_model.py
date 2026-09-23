"""Package the standalone Orpheus model, animations and rendered previews."""
from pathlib import Path
import hashlib,json,zipfile
ROOT=Path(__file__).resolve().parent
DEST=ROOT.parents[1]/'outputs'
FILES=['README.md','build_model.py','render_model.py','present_model.py','verify_model.py','package_model.py']
FILES+=['draft/'+n for n in ['orpheus.bbmodel','orpheus.geo.json','orpheus.animation.json','orpheus.png','orpheus-approval.png','orpheus-front.png','orpheus-three-quarter.png','orpheus-back.png','orpheus-side.png','orpheus-details.png','orpheus-idle.gif','orpheus-magic.gif','orpheus-physical.gif','orpheus-magic-pose.png','orpheus-physical-pose.png','orpheus-magic-poses.png','orpheus-physical-poses.png','validation.json','model-info.json','parts.json']]

def main():
 assert json.loads((ROOT/'draft/validation.json').read_text(encoding='utf-8'))['status']=='passed'
 manifest={f:hashlib.sha256((ROOT/f).read_bytes()).hexdigest() for f in FILES}
 (ROOT/'SHA256.json').write_text(json.dumps(manifest,indent=2)+'\n',encoding='utf-8')
 DEST.mkdir(exist_ok=True);target=DEST/'orpheus-modelo-02.zip'
 with zipfile.ZipFile(target,'w',zipfile.ZIP_DEFLATED,compresslevel=9) as z:
  for f in FILES+['SHA256.json']:z.write(ROOT/f,'orpheus/'+f)
 with zipfile.ZipFile(target) as z:
  assert z.testzip() is None
  for f,digest in manifest.items():assert hashlib.sha256(z.read('orpheus/'+f)).hexdigest()==digest
 checksum=hashlib.sha256(target.read_bytes()).hexdigest()
 (DEST/'orpheus-modelo-02.zip.sha256').write_text(checksum+'  '+target.name+'\n',encoding='utf-8')
 print(json.dumps({'zip':str(target),'files':len(FILES)+1,'bytes':target.stat().st_size,'sha256':checksum}))

if __name__=='__main__':main()
