"""Package and verify review artifacts only; no production compilation or edits."""
from pathlib import Path
import json, re, shutil, hashlib, subprocess, zipfile, math
from PIL import Image
from audit import ROOT,OUT,DOC,WORK,sha,dump

def run():
    man=json.loads((OUT/'manifest.json').read_text(encoding='utf-8'))
    profiles={r['id']:r for r in man['profiles']}
    evidence=json.loads((OUT/'evidence.json').read_text(encoding='utf-8'))
    shutil.copy2(DOC/'AUDITORIA.md',OUT/'AUDITORIA.md')
    shutil.copy2(DOC/'PhotonAuditPrototype.java',OUT/'draft/PhotonAuditPrototype.java')
    template=(ROOT/'velvet-eclipse/tools/vfx/panel.html').read_text(encoding='utf-8')
    html=template.replace('__DATA__',json.dumps(man,ensure_ascii=False).replace('</','<\\/'))
    (OUT/'index.html').write_text(html,encoding='utf-8')
    md=['# Mapa de habilidades e efeitos — auditoria 01','',
        '171 habilidades ativas; 163 passivas sem efeito independente. Todas as linhas são propostas, não implementação. As prévias de família não homologam as variações.','',
        '| Habilidade | Alvo | Estudo inicial | Evidência local | Pendência / ajuste |','|---|---|---|---|---|']
    audio=['# Associação VFX / áudio — auditorias 01 e 05','',
           'Nenhum novo áudio integrado ou recortado. Candidatos preservados na auditoria sonora 05; o painel VFX copia apenas amostras de base para escuta separada. As ondas não equivalem ao mixer completo de cada cue.','',
           '| Habilidade | Rota sonora | Clipes propostos | Referência visual |','|---|---|---|---|']
    for r in man['routes']:
        label=profiles[r['profile']]['name'] if r['profile'] else 'Sequência própria pendente'
        md.append(f"| {r['name']} | {r['target']} | {label} | {len(r['assets'])} assets | {r['note'] or 'Revisar forma, tamanho, cor e timing da base'} |")
        audio.append(f"| {r['name']} | {r['audioRoute'] or 'Ver auditoria 05'} | {', '.join(r['audioClips'])} | {label} |")
    md+=['','## Passivas','', 'Não emitir VFX por possuir uma passiva. Eventos de ativação reais devem possuir contrato próprio, se necessário.']
    for name,lines in [('MAPA-HABILIDADES.md',md),('AUDIO-VINCULOS.md',audio)]:
        (OUT/name).write_text('\n'.join(lines)+'\n',encoding='utf-8');shutil.copy2(OUT/name,DOC/name)
    (OUT/'README.md').write_text('''# Auditoria VFX Photon 01

Abra `index.html` para comparar 30 estudos animados e revisar o mapa das 171 habilidades ativas. Os GIFs são prévias offline, não capturas do Photon ou do Minecraft.

- `AUDITORIA.md`: diagnóstico, referências, decisões de forma/cor, API, áudio e limites.
- `MAPA-HABILIDADES.md`: aplicação por habilidade e pendências específicas.
- `AUDIO-VINCULOS.md`: associação aos candidatos da auditoria sonora 05.
- `comparativo-animado.gif`: seis efeitos em movimento lado a lado.
- `draft/`: Java e recursos experimentais. Não é resource pack instalável nem JAR.
- `evidence.json`: hashes e imports de assets do jogo lidos localmente. Nenhum executável/asset Unreal foi incluído no pacote.
- `validation.json`: verificações offline e comparação das fontes de produção.

As notas de revisão são locais ao navegador. Use “Exportar minhas notas” para salvá-las. Aprovar um item no painel não integra o efeito; a confirmação permanece pelo chat.

A auditoria sonora completa continua na entrega separada `auditoria-audio-mapa-05`. Nenhum áudio novo foi implementado nesta etapa. Nenhuma aprovação de VFX foi presumida.
''',encoding='utf-8')

    # Bounded, task-specific validation: references, recipes, resource frames and code API.
    assert len(man['routes'])==171 and man['passives']==163
    assert len(set(r['id'] for r in man['routes']))==171
    assert len(profiles)==30 and len(evidence)==697 and man['baseGameAssets']==612
    assert all(r['audioRoute'] for r in man['routes'])
    unresolved=[r['id'] for r in man['routes'] if r['profile'] is None]
    assert len(unresolved)==13,unresolved
    checks=[]
    for r in man['profiles']:
        assert r['peakEmitterCount']<=16 and r['durationTicks']<64
        for key in ['preview','poster','reference','audio']:
            if key in r:
                p=OUT/r[key];assert p.is_file(),p;checks.append(dict(file=r[key],sha256=sha(p)))
        with Image.open(OUT/r['preview']) as im:
            assert im.size==(512,384) and im.n_frames>=40
        for l in r['layers']:
            assert l['start']>=0 and l['life']>0 and re.fullmatch(r'#[0-9a-f]{6}',l['color'])
            assert l['type'] in ['sprite','beam','trail']
            for field in ['at','a','b','size']:
                if field in l:assert all(math.isfinite(n) for n in l[field])
            if 'points' in l:assert len(l['points'])>=2 and all(len(p)==3 and all(math.isfinite(n) for n in p) for p in l['points'])
            if l['type']=='sprite':
                tex=OUT/f"draft/resources/assets/velvet_eclipse/textures/vfx/audit01/{l['texture']}.png"
                assert tex.is_file()
                with Image.open(tex) as im:assert im.mode=='RGBA' and im.size==((768,384) if l['texture'] in ['fire','smoke'] else (96,96))
    # Audio copies must remain byte-identical to the previously collected previews.
    am=json.loads((ROOT/'outputs/auditoria-audio-mapa-05/manifest.json').read_text(encoding='utf-8'))
    ae={e['id']:e for e in am['entries']}
    for p in (OUT/'audio').glob('*.wav'):
        original=ROOT/'outputs/auditoria-audio-mapa-05'/ae[p.stem]['file'];assert sha(p)==sha(original)
    script=re.findall(r'<script>([\s\S]*?)</script>',html)[0]
    js=WORK/'panel-check.js';js.write_text(script,encoding='utf-8')
    subprocess.run(['node','--check',str(js)],check=True,capture_output=True)
    # Check literal local links only; templated entries were checked above.
    for path in re.findall(r'href="([^"<>]+)"',html.split('<script id="data"')[0]):
        if not path.startswith(('http:','https:','#')) and path!='validation.json':assert (OUT/path).exists(),path
    javac='C:/Program Files/Java/jdk-17/bin/javac.exe'
    result=subprocess.run([javac,'--release','17','-proc:none','-encoding','UTF-8','-cp',str(ROOT/'velvet-eclipse/dependencies/*'),'-d',str(WORK/'compiled-prototype'),str(DOC/'PhotonAuditPrototype.java')],capture_output=True,text=True)
    assert result.returncode==0,result.stderr
    (WORK/'prototype-compile.log').write_text(result.stderr,encoding='utf-8')
    before=json.loads((ROOT/'work/audio-placement-05/production-before.json').read_text(encoding='utf-8'))
    src=ROOT/'velvet-eclipse/src';after={str(p.relative_to(src)):sha(p) for p in src.rglob('*') if p.is_file()}
    assert before==after,'Production snapshot changed'
    jar=Path('C:/Users/User/AppData/Roaming/.minecraft/versions/yuxio test/mods/velvet-eclipse-0.6.54-paulownia-shops.jar')
    jar_hash=sha(jar);assert jar_hash=='ffba2a8bb94d8748a6c69602c82c15b48064d6fdd802a765df732b18f4f255fd'
    validation=dict(status='passed',date='2026-10-22',scope='Offline audit artifacts only',profiles=30,activeSkills=171,passivesWithoutVfx=163,
                    extractedAssets=697,baseGameAssets=612,astreaReserveAssets=85,bespokeUnprototypedSkills=unresolved,
                    apiCompile='Java 17 / Photon Forge 1.20.1 1.1.17 / success',compileWarning='ResourceLocation constructor marked for removal in local dependency; API available and compilation succeeds',
                    productionFilesUnchanged=len(after),installedJarUnchangedSha256=jar_hash,
                    maxEmittersPerTarget=max(r['peakEmitterCount'] for r in man['profiles']),
                    gifDecode=True,rgbaAtlases=True,audioCopiesIdentical=True,localLinks=True,panelJsSyntax=True,
                    minecraftRun=False,photonRuntimeTest=False,performanceBenchmark=False,approval=False,files=checks)
    dump(OUT/'validation.json',validation);dump(DOC/'validation.json',validation)
    for srcname in ['audit.py','package_audit.py','panel.html']:
        dest=OUT/'draft/generator'/srcname;dest.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(ROOT/'velvet-eclipse/tools/vfx'/srcname,dest)
    package=ROOT/'outputs/velvet-eclipse-auditoria-vfx-photon-01.zip'
    with zipfile.ZipFile(package,'w',zipfile.ZIP_DEFLATED,compresslevel=6) as z:
        for p in sorted(OUT.rglob('*')):
            if p.is_file():z.write(p,str(Path(OUT.name)/p.relative_to(OUT)))
    with zipfile.ZipFile(package) as z:assert z.testzip() is None;count=len(z.namelist())
    report=dict(package=str(package),sha256=sha(package),bytes=package.stat().st_size,files=count,
                validation=validation['status'],sourceFilesUnchanged=len(after),noProductionJarGenerated=True)
    dump(DOC/'package.json',report)
    print(json.dumps(report,ensure_ascii=True,indent=2))

if __name__=='__main__':run()
