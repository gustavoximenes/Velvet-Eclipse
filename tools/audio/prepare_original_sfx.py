"""Original-source SFX research, kept separate from production resources."""
from pathlib import Path
import json,subprocess,hashlib,wave
import numpy as np

ROOT=Path(__file__).resolve().parents[2]
WORK=ROOT.parent/'work/audio-original-02/sfx-research'
OUT=ROOT/'modeling/audio/audit-03/sfx'
FF=ROOT.parent/'work/awakening-preview/deps/imageio_ffmpeg/binaries/ffmpeg-win-x86_64-v7.1.exe'
RATE=48000

def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()

def cut(source,id,name,start,end,kind,source_url=None):
    # Decode without source EQ, source separation, voice synthesis or time stretch.
    raw=subprocess.check_output([str(FF),'-hide_banner','-loglevel','error','-ss',str(start),'-i',str(source),'-t',str(end-start),'-vn','-ac','2','-ar',str(RATE),'-f','s16le','-'])
    x=np.frombuffer(raw,'<i2').reshape(-1,2).astype(np.float64)
    x*=10**(-6/20)
    fade=min(240,len(x)//10)
    x[:fade]*=np.linspace(0,1,fade)[:,None]
    x[-fade:]*=np.linspace(1,0,fade)[:,None]
    y=np.rint(x).astype('<i2')
    p=OUT/(id+'.wav')
    with wave.open(str(p),'wb') as f:
        f.setnchannels(2);f.setsampwidth(2);f.setframerate(RATE);f.writeframes(y.tobytes())
    return {'id':id,'name':name,'file':'sfx/'+p.name,'seconds':len(y)/RATE,'sourceFile':source.name,
            'sourceSha256':sha(source),'sourceStart':start,'sourceEnd':end,'sourceUrl':source_url,
            'kind':kind,'gainDb':-6,'edgeFadeMs':5,'sourceMixRetained':kind=='cinematic_reference',
            'sha256':sha(p),'peakDbFS':float(20*np.log10(max(1,np.abs(y.astype(np.int32)).max())/32768)),
            'status':'Reference audition; event mapping pending; not integrated'}

def main():
    OUT.mkdir(parents=True,exist_ok=True)
    source=Path('C:/Users/User/Downloads/videoplayback (2).mp4')
    entries=[cut(source,'evoker_cinematic','Evoker e estilhaços — referência',96.05,98.35,'cinematic_reference'),
             cut(source,'manifestation_cinematic','Manifestação — referência',98.35,103.25,'cinematic_reference')]
    intervals=json.loads((WORK/'system-segments.json').read_text())
    # Select complete short regions for first audition. A region may contain more
    # than one source effect; do not label these as independently extracted cues.
    selected=[(i,a,b) for i,(a,b) in enumerate(intervals,1) if .12<=b-a<=1.6][:12]
    for index,a,b in selected:
        entries.append(cut(WORK/'reload-system.webm',f'system_region_{index:03}',f'Sistema — trecho {index:03}',max(0,a-.025),b+.025,'system_candidate','https://www.youtube.com/watch?v=QjDwTf-i2TQ'))
    data={'entries':entries,'sourceClaimedWavFiles':453,'detectedAudioRegions':len(intervals),
          'note':'Automatic silence regions are not equivalent to the original cue boundaries. No semantic event classification is asserted for the system samples.',
          'source':'https://www.youtube.com/watch?v=QjDwTf-i2TQ','uploader':'revampedtelephone',
          'originalBankStillRequired':True,'integrated':False}
    (OUT.parent/'sfx-manifest.json').write_text(json.dumps(data,ensure_ascii=False,indent=2),encoding='utf-8')
    print(json.dumps({'referenceClips':2,'systemCandidates':len(selected),'total':len(entries),'integrated':False}))

if __name__=='__main__':main()
