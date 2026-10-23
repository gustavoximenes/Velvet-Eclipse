"""Collect the remaining audition material, without touching game/mod resources."""
from pathlib import Path
import importlib.util, json, shutil, struct, subprocess

ROOT=Path(__file__).resolve().parents[2]
OLD=ROOT.parent/'work/p3r-audio-04'
WORK=ROOT.parent/'work/audio-placement-05'
OUT=WORK/'extras'
def read(p): return json.loads(p.read_text(encoding='utf-8-sig'))
def save(p,v): p.write_text(json.dumps(v,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')

def main():
    spec=importlib.util.spec_from_file_location('decode04',Path(__file__).with_name('reload_sfx_audit.py'))
    helper=importlib.util.module_from_spec(spec);spec.loader.exec_module(helper)
    for p in [WORK/'banks',OUT/'masters',OUT/'audio']:p.mkdir(parents=True,exist_ok=True)
    names=read(OLD/'skill-names-en.json'); banks=[]; waves=[]
    for e in read(OLD/'acb-index.json'):
        if '/Battle/Theurgia/' not in e['asset'] or Path(e['asset']).stem not in {f'{n:04}' for n in range(260,267)}:continue
        source=OLD/'cues'/e['asset'];data=source.read_bytes();start=e['offset'];end=start+e['bytes']
        assert data[start:start+4]==b'@UTF' and struct.unpack_from('>I',data,start+4)[0]+8==e['bytes']
        name='theurgy_'+source.stem+'.acb'; dest=WORK/'banks'/name;dest.write_bytes(data[start:end])
        provenance=dict(e,bank=name,sourceAssetSha256=helper.sha(source),bankSha256=helper.sha(dest));banks.append(provenance)
        result=subprocess.run([str(helper.VGM),'-m','-I','-s','1','-S','0',str(dest)],capture_output=True,check=True)
        for line in result.stdout.decode('utf-8').splitlines():
            if line.startswith('{'):waves.append(dict(json.loads(line),bank=name,source=provenance))
    system=next(e for e in read(ROOT/'modeling/audio/audit-04/bank-provenance.json') if e['bank']=='system.acb')
    for suffix in ['.acb','.awb']:
        shutil.copyfile(OLD/'banks'/('system'+suffix),WORK/'banks'/('system'+suffix))
    selected={121:('bow_release','Arco — disparo'),125:('bow_draw','Arco — tensionar'),143:('gun_shot','Arma de fogo — disparo'),144:('gun_handling','Arma de fogo — manuseio')}
    for line in (OLD/'system-cues.jsonl').read_text(encoding='utf-8-sig').splitlines():
        w=json.loads(line)
        if w['streamInfo']['index'] in selected:waves.append(dict(w,bank='system.acb',source=system))
    helper.WORK=WORK;helper.OUT=OUT;entries=[]
    for w in waves:
        idx=w['streamInfo']['index'];sid=None
        if w['bank'].startswith('theurgy'):
            sid=int(w['bank'][8:12]);key=Path(w['bank']).stem+f'_{idx:02}';title=names[sid];cat='Teurgias'
            note='Onda original de teurgia; montagem de camadas e sincronização ainda não reproduzidas.'
        else:
            key,title=selected[idx];cat='Físicos';note='Proposta para armas do mod; disparo original do rifle de Aigis não é exclusivo de cada arma.' if key=='gun_shot' else ''
        e=dict(id=key,name=title,category=cat,note=note,bank=w['bank'],subsong=idx,cueNames=w['streamInfo']['name'],
            rate=w['sampleRate'],channels=w['channels'],frames=w['numberOfSamples'],seconds=w['numberOfSamples']/w['sampleRate'],encoding=w['encoding'],loop=w['loopingInfo'],sourceSkillId=sid,source=w['source'])
        entries.append(helper.decode(e))
    save(OUT/'manifest.json',{'entries':entries});save(OUT/'bank-provenance.json',banks);save(OUT/'wave-metadata.json',waves)
    print(json.dumps({'newSamples':len(entries),'entries':[{k:e[k] for k in ['id','name','seconds','silent','cueNames']} for e in entries]},ensure_ascii=True,indent=2))

if __name__=='__main__':main()
