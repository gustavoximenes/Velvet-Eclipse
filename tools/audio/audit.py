"""Authorial audition pack. Does not register assets in the mod or write a JAR."""
from pathlib import Path
import numpy as np
import wave,json,hashlib,html,zipfile
ROOT=Path(__file__).resolve().parents[2];OUT=ROOT/'modeling/audio/audit-01';OUT.mkdir(parents=True,exist_ok=True)
RATE=48000;rng=np.random.default_rng(1969);entries=[];samples={}
def timeline(seconds):return np.arange(round(seconds*RATE))/RATE
def tone(t,hz):return np.sin(2*np.pi*np.cumsum(np.broadcast_to(hz,t.shape))/RATE)
def noise(t,lo=200,hi=6000):
 a=rng.normal(size=len(t));f=np.fft.rfftfreq(len(t),1/RATE);band=np.clip((f-lo)/max(60,lo*.5),0,1)*np.clip((hi-f)/max(100,hi*.3),0,1)
 a=np.fft.irfft(np.fft.rfft(a)*band,n=len(t));return a/max(.01,np.std(a))
def env(t,attack=.012,decay=.18):return (1-np.exp(-t/attack))*np.exp(-t/decay)
def echo(a,seconds=.075,gain=.22):
 b=a.copy();n=int(seconds*RATE)
 for i in range(1,4):
  d=n*i
  if d<len(a):b[d:]+=a[:-d]*gain**i
 return b
def save(id,name,group,a,when,peak=-6,variant=False):
 a=np.asarray(a,float);assert np.isfinite(a).all();a-=a.mean()
 fade=min(480,len(a)//4);a[:fade]*=np.linspace(0,1,fade);a[-fade:]*=np.linspace(1,0,fade)
 a*=10**(peak/20)/max(np.max(abs(a)),1e-8)
 folder=OUT/('demos' if group=='Demonstração' else 'sfx');folder.mkdir(exist_ok=True);p=folder/(id+'.wav')
 with wave.open(str(p),'wb') as w:w.setnchannels(1);w.setsampwidth(2);w.setframerate(RATE);w.writeframes(np.int16(a*32767).tobytes())
 samples[id]=a
 entries.append(dict(id=id,name=name,group=group,file=p.relative_to(OUT).as_posix(),seconds=round(len(a)/RATE,3),peakDbFS=peak,rmsDbFS=round(20*np.log10(max(1e-9,np.sqrt(np.mean(a*a)))),2),trigger=when,provenance='Authorial synthesis, seed 1969',sha256=hashlib.sha256(p.read_bytes()).hexdigest()))
def impact(t,base=105):return .7*tone(t,base+180*np.exp(-t*35))*env(t,.002,.07)+noise(t,500,9000)*env(t,.001,.024)*.3
def wind(t):return noise(t,180,4300)*env(t,.10,.24)+.14*tone(t,420-260*t)*env(t,.05,.3)
for i in range(3):
 t=timeline(.48);save('physical_slash_'+str(i+1),'Corte · variação '+str(i+1),'Ataques físicos',wind(t)+np.roll(impact(t,110+i*12),int(.14*RATE))*.6,'Vento na antecipação; impacto apenas ao acertar. As variações não se acumulam.')
t=timeline(.42);save('physical_bash','Impacto contundente','Ataques físicos',impact(t,72)+.18*noise(t,40,500)*env(t,.008,.13),'Contato confirmado, uma vez por alvo.')
t=timeline(.28);save('physical_pierce','Perfuração','Ataques físicos',.7*noise(t,1700,12000)*env(t,.002,.018)+.5*tone(t,780-350*t)*env(t,.002,.055),'Contato de ataque perfurante.')
t=timeline(.24);save('physical_tap','Tapinha · Pixie / Apsaras','Ataques físicos',.35*noise(t,800,6500)*env(t,.001,.021)+tone(t,180)*env(t,.001,.028),'Contato da palma, leve e curto.',peak=-10)
t=timeline(.62);save('door_open','Porta · abrir','Objetos',.32*noise(t,400,2200)*env(t,.04,.16)+.17*tone(t,260+80*np.sin(t*18))*env(t,.01,.13)+np.roll(impact(t,160),int(.04*RATE))*.20,'Somente depois de a porta realmente mudar de estado.',peak=-12)
t=timeline(.38);save('door_close','Porta · fechar','Objetos',impact(t,84)*.5+noise(t,1300,7000)*env(t,.001,.014)*.2,'Fechamento concluído; tentativa bloqueada não toca.',peak=-10)
t=timeline(.47);save('chair_sit','Cadeira · sentar / levantar','Objetos',noise(t,400,3300)*env(t,.045,.12)+.19*tone(t,170+60*t)*env(t,.01,.12),'Montagem ou saída confirmada, com intervalo mínimo.',peak=-16)
t=timeline(.38);save('object_latch','Objeto · trinco / abrir compartimento','Objetos',.45*impact(t,210)+np.roll(impact(t,330),int(.11*RATE))*.25,'Interação válida com objeto que tenha compartimento.',peak=-14)
for id,name,notes,group in [('item_hp','Item · recuperação HP',[523.25,659.25,783.99],'Itens'),('item_sp','Item · recuperação SP',[440,659.25,880],'Itens'),('item_buff','Item · reforço',[392,587.33,783.99],'Itens')]:
 t=timeline(1.05);a=np.zeros_like(t)
 for i,f in enumerate(notes):
  tt=np.maximum(0,t-i*.105);a+=(tone(t,f)+.15*tone(t,2*f))*env(tt,.008,.20)*(t>=i*.105)
 save(id,name,group,echo(a),'Após consumo confirmado e aplicação do efeito; sem som se cancelado.',peak=-12)
t=timeline(.8);save('persona_fire','Magia · fogo','Personas',noise(t,70,3200)*env(t,.045,.19)+.4*impact(t,68),'Início da liberação; impacto acompanha o resultado.')
t=timeline(1.05);a=noise(t,1800,11000)*env(t,.008,.09)*.22
for f in [1320,1760,2349,3520]:a+=tone(t,f)*env(t,.003,.24)*.19
save('persona_ice','Magia · gelo','Personas',echo(a,.095,.32),'Liberação de Bufu e derivados; brilho curto após o contato.',peak=-9)
t=timeline(.77);save('persona_electric','Magia · eletricidade','Personas',noise(t,600,9000)*env(t,.003,.16)*(.28+.65*np.sin(t*2*np.pi*57)**8)+tone(t,95)*env(t,.003,.13)*.23,'Descarga no frame de efeito, sem repetição por tick.',peak=-9)
t=timeline(.87);save('persona_wind','Magia · vento','Personas',echo(wind(t),.13,.2),'Movimento de ar na liberação de Garu.',peak=-9)
t=timeline(1.2);a=sum(tone(t,f)*env(t,.025,.28) for f in [659.25,987.77,1318.51]);save('persona_light','Magia · luz','Personas',echo(a,.13,.28),'Liberação luminosa; cauda limitada para não encobrir a voz.',peak=-11)
t=timeline(1.1);save('persona_dark','Magia · trevas','Personas',echo(tone(t,95-35*t)*env(t,.06,.25)+noise(t,100,1500)*env(t,.03,.3)*.25,.12,.3),'Liberação de magia de trevas.',peak=-10)
t=timeline(.95);save('persona_ailment','Persona · alteração de estado','Personas',echo((tone(t,510+180*np.sin(t*11))+.4*tone(t,747))*env(t,.025,.20),.09,.25),'Aplicação de status; falha usa apenas a antecipação.',peak=-12)
t=timeline(.92);save('persona_psy','Magia · psíquico','Personas',echo((tone(t,620+220*np.sin(19*t))+.32*tone(t,930+130*np.sin(11*t)))*env(t,.025,.22),.09,.25),'Pulso psíquico no marcador de liberação.',peak=-10)
t=timeline(.98);save('persona_nuclear','Magia · nuclear','Personas',impact(t,52)+.32*noise(t,200,4000)*env(t,.08,.22)+.13*tone(t,1100-700*t)*env(t,.08,.22),'Expansão grave na liberação; queda curta após o impacto.',peak=-9)
t=timeline(1.1);save('persona_almighty','Magia · onipotente','Personas',echo(impact(t,65)+sum(.13*tone(t,f)*env(t,.055,.25) for f in [293.66,440,587.33])+noise(t,2000,8000)*env(t,.035,.18)*.14,.105,.22),'Liberação ampla, com cauda limitada para a próxima fala.',peak=-8)
t=timeline(1.2);save('persona_healing','Persona · cura','Personas',echo(sum(tone(t,f)*env(t,.065,.28) for f in [523.25,783.99,1046.5]),.13,.30),'Aplicação da cura, separada do pequeno sinal de consumo de item.',peak=-12)
t=timeline(.88);save('persona_support','Persona · suporte','Personas',echo((tone(t,392)+.65*tone(t,587.33))*env(t,.025,.19),.10,.28),'Reforço aplicado; sem voz de One More.',peak=-12)
t=timeline(.40);save('weakness_confirm','Fraqueza · confirmação','One More',sum(tone(t,f)*env(t,.003,.09) for f in [880,1320,1760]),'Fraqueza confirmada; a voz depende também de One More concedido.',peak=-11)
t=timeline(.54);a=tone(t,740)*env(t,.003,.1)+np.where(t>.09,tone(t,1174)*env(np.maximum(0,t-.09),.003,.15),0)
save('one_more','One More · sinal curto','One More',echo(a,.07,.15),'Uma vez por ação que realmente concede One More.',peak=-12)
# SAPI voices are conspicuously labelled as guides. They are never added to sounds.json.
guides=[]
for p in sorted((OUT/'voices-guide').glob('*.wav')):
 with wave.open(str(p),'rb') as w:
  assert w.getframerate()==RATE and w.getnchannels()==1 and w.getsampwidth()==2
  a=np.frombuffer(w.readframes(w.getnframes()),'<i2').astype(float)/32768
 nz=np.flatnonzero(abs(a)>.006)
 if len(nz):a=a[max(0,nz[0]-1200):min(len(a),nz[-1]+4000)]
 a*=.50/max(np.max(abs(a)),1e-8)
 with wave.open(str(p),'wb') as w:w.setnchannels(1);w.setsampwidth(2);w.setframerate(RATE);w.writeframes(np.int16(a*32767).tobytes())
 guides.append(dict(id=p.stem,file=p.relative_to(OUT).as_posix(),seconds=round(len(a)/RATE,3),voice='Microsoft Zira Desktop, en-US',status='Synthetic timing guide; not original actor or final performance'))
 if p.stem=='jack_frost':
  demo=np.zeros(int(2.7*RATE))
  for at,key,gain in [(0,'persona_ice',.7),(.36,'weakness_confirm',.7),(.48,'one_more',.45)]:
   n=int(at*RATE);v=samples[key];demo[n:n+len(v)]+=v*gain
  n=int(.62*RATE);demo[n:n+len(a)]+=a
  save('demo_jack_frost_one_more','Gelo → fraqueza → One More → Jack Frost','Demonstração',demo,'Demonstração de montagem. Voz sintética de guia, não final.',peak=-4)
  entries[-1]['provenance']='Authorial SFX + Microsoft Zira Desktop synthetic guide'
manifest=dict(status='AUDIT ONLY — not integrated',sampleRate=RATE,channels=1,bits=16,sfx=entries,guideVoices=guides,originalVoiceSource='https://sounds.spriters-resource.com/pc_computer/persona3reload/asset/449330/',sourceAccess='Download returned HTTP 403 on 2026-10-19. No original clips obtained or included.')
(OUT/'manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2),encoding='utf8')
groups={}
for e in entries:groups.setdefault(e['group'],[]).append(e)
sections=''
for group,rows in groups.items():
 sections+=f'<section><h2>{html.escape(group)}</h2><div class="grid">'
 for e in rows:sections+=f'<article><h3>{html.escape(e["name"])}</h3><audio controls preload="none" src="{e["file"]}"></audio><p>{html.escape(e["trigger"])}</p><small>{e["seconds"]} s · pico {e["peakDbFS"]} dBFS</small></article>'
 sections+='</div></section>'
sections+='<section><h2>Chamadas em inglês · GUIAS SINTÉTICAS</h2><p>Estas vozes servem apenas para avaliar o momento da chamada. Não são a voz do protagonista de Persona 3 Reload nem uma imitação dela. A atuação e a pronúncia final ainda precisam de revisão.</p><div class="grid">'
for e in guides:sections+=f'<article><h3>{e["id"].replace("_"," ").title()}</h3><audio controls preload="none" src="{e["file"]}"></audio></article>'
sections+='</div></section>'
(OUT/'index.html').write_text('''<!doctype html><html lang="pt-BR"><meta charset="utf-8"><meta name="viewport" content="width=device-width"><title>Velvet Eclipse · Auditoria sonora 01</title><style>body{margin:0;background:#071224;color:#effaff;font:16px system-ui}main{max-width:1150px;margin:auto;padding:36px 24px}h1{font-size:38px;max-width:800px}h2{color:#7ef0f8;margin-top:40px}.grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(310px,1fr));gap:14px}article{padding:18px;background:#11233c;border-top:2px solid #47627e;border-radius:4px}h3{margin-top:0;font-size:18px}audio{width:100%}p{line-height:1.6;color:#c0d3df}small{color:#93b2c5}.badge{color:#8df4d1;letter-spacing:.1em}.note{padding:18px;background:#192a43;border-left:3px solid #7ef0f8}a{color:#7ef0f8}</style><main><div class="badge">VELVET ECLIPSE / AUDITORIA 01 / 48 KHZ MONO</div><h1>Efeitos sonoros para revisar antes da implementação</h1><p class="note">Efeitos autorais e chamadas em inglês com voz sintética de guia. Nenhum destes áudios foi instalado no mod. O pacote de referência do protagonista retornou HTTP 403; não há vozes originais neste conjunto.</p><p>Ouça uma amostra de cada vez. Comece pela demonstração de One More, depois compare os ataques, objetos e itens.</p><p><a href="README.md">Documentação e regras de disparo</a> · <a href="manifest.json">Manifesto técnico</a></p>'''+sections+'''<script>document.querySelectorAll('audio').forEach(a=>a.addEventListener('play',()=>document.querySelectorAll('audio').forEach(b=>{if(a!==b)b.pause()})))</script></main></html>''',encoding='utf8')
print('Created',len(entries),'SFX/demo files and',len(guides),'synthetic guide calls at',OUT)
