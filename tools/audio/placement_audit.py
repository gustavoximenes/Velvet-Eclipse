"""Make an audition and placement proposal. No production writes or integration."""
from pathlib import Path
from collections import defaultdict, Counter
import gzip, hashlib, json, re, shutil, wave, zipfile

ROOT=Path(__file__).resolve().parents[2]
BASE=ROOT.parent
WORK=BASE/'work/audio-placement-05'
OUT=ROOT/'modeling/audio/audit-05'
DELIVERY=BASE/'outputs/auditoria-audio-mapa-05'
A03=ROOT/'modeling/audio/audit-03'
A04=ROOT/'modeling/audio/audit-04'
EXTRA=WORK/'extras'
def read(p):return json.loads(p.read_text(encoding='utf-8-sig'))
def save(p,x):p.write_text(json.dumps(x,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def copy(source,dest):dest.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(source,dest)

BATTLE='Participantes da mesma sessão de combate; posição do ator/alvo, sem alcançar outra batalha.'
UI='Som local apenas para quem usa o menu.'
WORLD='Jogadores próximos na mesma dimensão; posição do objeto. Não transmitir globalmente.'
routes=[]
def route(id,name,category,clips,where,when,note='',kind='Original',audience=BATTLE,hook='',subject=None):
    row=dict(id=id,name=name,category=category,clips=clips,where=where,when=when,note=note,kind=kind,audience=audience,hook=hook,subject=subject,status='Aguardando revisão')
    routes.append(row);return row

def collect():
    entries=[]
    for source in [A04,EXTRA]:
        for raw in read(source/'manifest.json')['entries']:
            if raw.get('silent'):continue
            e=dict(raw);e['kind']='effect';e['auditOrigin']='04' if source==A04 else '05'
            for field,sub in [('file','audio/effects'),('master','masters/effects')]:
                dest=f"{sub}/{e['id']}.wav";copy(source/raw[field],OUT/dest);e[field]=dest
            entries.append(e)
    for raw in read(A03/'manifest.json')['calls']:
        e=dict(raw);e['sourceId']=raw['id'];e['id']='voice_'+raw['id'];e['kind']='voice';e['category']='Vozes';e['auditOrigin']='03'
        e['rate']=48000;e['channels']=1;e['masterSha256']=e['originalSha256']
        e['file']='audio/voices/'+e['sourceId']+'.wav';e['master']='masters/voices/'+e['sourceId']+'.wav'
        copy(A03/raw['file'],OUT/e['file']);copy(A03/raw['original'],OUT/e['master']);entries.append(e)
    return entries

def core_routes():
    for key,name,when in [
        ('evoker','Evoker: disparo','No marcador de disparo da animação aceita; uma vez por invocação.'),
        ('summon','Persona: manifestação','Quando a Persona aparece, no começo da subida (tick 46 do fluxo atual).'),
        ('persona_aura','Persona: aura','Ao iniciar a aura visível da invocação; um disparo, sem loop automático.'),
        ('persona_cutin','Persona: cut-in','Somente se a cena exibir o cut-in; não tocar em toda magia.')]:
        route('summon_'+key,name,'Invocação',[key],'Invocação de qualquer Persona, com ou sem modelo próprio.',when,
            'Tempos de audição não são a sincronização final. Cancelamento encerra eventos agendados.',hook='forge/PersonaCinematicServer.java + marcador visual do Evoker')
    for key,name,when,note in [
        ('one_more','One More do jogador','Após a resolução que concede a ação extra ao jogador.','Um efeito por ação, mesmo em magia de área.'),
        ('one_more_enemy','One More do inimigo','Após a resolução que concede ação extra ao inimigo.','Sem voz do protagonista para o inimigo.'),
        ('critical','Acerto crítico','Quando um resultado confirma crítico.','Não usar para um acerto que é apenas fraqueza.'),
        ('down','Alvo derrubado','Na transição real de em pé para caído.','Não repetir em alvo já caído.'),
        ('absorb','Alvo absorve o ataque','Após afinidade DRAIN confirmada.','Substitui o impacto comum; sem voz de fraqueza.'),
        ('reflect','Alvo reflete o ataque','Após afinidade REPEL confirmada.','Substitui o impacto comum no refletor; refletido não chama a Persona do atacante.')]:
        route('battle_'+key,name,'Resultados de combate',[key],'Combates solo e multiplayer, incluindo raids.',when,note,hook='core/battle/BattleSession.java → forge/BattleService.java')
    for key,name,when in [
        ('cursor','Mover seleção','Quando o foco muda de opção; não a cada quadro nem ao passar sobre a mesma opção.'),
        ('battle_target','Selecionar alvo','Quando muda o alvo válido destacado no combate.'),
        ('battle_confirm','Confirmar','Na confirmação válida de uma ação, abertura de loja ou compra confirmada pelo servidor.'),
        ('cancel','Voltar ou cancelar','Ao voltar um nível ou cancelar a seleção.'),
        ('error','Ação recusada','Na recusa de ação, compra sem recursos ou interação inválida; uma vez por tentativa.')]:
        route('ui_'+key,name,'Menus',[key],'Menus do mod: combate, TAB/System, lojas do Paulownia, cidade/viagem e Velvet Room.',when,
            'Aplicar áudio mantendo o visual e o comportamento dos menus. Evitar duplicar o clique vanilla.',audience=UI,hook='Telas client/*, CityShopScreen e resposta da transação')
    physical=[
        ('slash','Ataque básico de corte',['swish_medium','slash_medium'],'Movimento da lâmina; impacto somente se o golpe atingir.','Espadas e ataques classificados como SLASH.'),
        ('strike','Ataque básico contundente',['swish_medium','strike_heavy'],'Movimento do ataque; impacto somente se atingir.','Armas contundentes e golpes PHYSICAL.'),
        ('pierce','Ataque básico de perfuração',['swish_small','pierce_light'],'Avanço da arma; impacto somente se atingir.','Armas de estocada e golpes PIERCE.'),
        ('tap','Tapinha: Pixie e Apsaras',['swish_small','strike_light'],'Movimento ao estender a mão e impacto curto no contato confirmado.','Ataque físico simples de Pixie e Apsaras.'),
        ('bow','Arco',['bow_draw','bow_release','hit_enemy_stab'],'Tensionar, soltar a flecha e acertar o alvo; são três eventos separados.','Ataque básico de arco.'),
        ('gun','Arma de fogo',['gun_handling','gun_shot','hit_enemy_stab'],'Manuseio se houver pose, disparo e contato confirmado; não sobrepor tudo no início.','Ataque básico com arma de fogo.'),
        ('draw','Sacar espada',['sword_draw'],'Na transição visual real para arma desembainhada.','Entrada no combate com espada.'),
        ('sheath','Guardar espada',['sword_sheath'],'Ao terminar a animação de guardar a espada.','Retorno à exploração com espada.'),
    ]
    for id,name,clips,when,where in physical:
        route('physical_'+id,name,'Ataques físicos',clips,where,when,
            'Escolha de uso adaptada ao mod. Não somar impacto básico com o áudio completo de uma habilidade. Esquiva mantém movimento e remove contato.'+(' O disparo de rifle de Aigis é proposta para as armas de fogo do mod.' if id=='gun' else ''),
            kind='Adaptação',hook='forge/MeleeBattleServer.java; MeleeTimeline.IMPACT=19, após BattleService.act')
    for key,label in [('hit_enemy_slash','corte'),('hit_enemy_strike','contundente')]:
        route('physical_npc_'+key,'Contato de inimigo — '+label,'Ataques físicos',[key],'Ataque básico do inimigo, quando sua apresentação não contém impacto próprio.',
            'Somente após contato confirmado no alvo.','Alternativa ao impacto da arma; não reproduzir ambos.',hook='forge/BattleService.java / resolução de ação inimiga')

def skill_routes(entries):
    skills=read(ROOT/'src/main/resources/data/velvet_eclipse/catalogue/skills.json');by_source=defaultdict(list)
    for e in entries:
        if e['kind']=='effect' and e.get('sourceSkillId') is not None:by_source[e['sourceSkillId']].append(e)
    area={13:10,14:11,15:12,23:20,24:21,25:22,33:30,34:31,35:32,43:40,44:41,45:42,52:50,53:51,59:56,60:57,61:58,66:64,67:65,73:71,75:72,86:85,88:87,90:89,92:91,94:93,102:101,160:155,161:156,162:157,171:170,173:172,185:180,186:181,187:182,195:190,196:191,197:192}
    nuclear={'frei':78,'freila':79,'freidyne':80,'mafrei':78,'mafreila':79,'mafreidyne':80,'atomic_flare':80,'cosmic_flare':80}
    mappings=[]
    for s in skills:
        if s.get('passive'):continue
        sid=s['source'];kind='Original';note='Banco original identificado pelo nome inglês da instalação. Onda principal proposta; outras camadas ficam em reserva.'
        if sid==-1:
            chosen=nuclear[s['id']] if s['element']=='NUCLEAR' else 91;kind='Adaptação'
            note='Reload não tem esta família como elemento próprio. Proposta: '+('Megido/Megidola/Megidolaon por potência.' if s['element']=='NUCLEAR' else 'Pulinpa para a família psíquica, sem alterar o timbre ou fingir ser áudio original de Psi.')
        elif s['id']=='eiga':
            chosen=71;kind='Compatibilidade';note='O catálogo usa código 70 mas nome Eiga. Proposta pelo nome: Eiga do banco 71; não alterar o nome nem o balanceamento do mod nesta etapa.'
        elif sid in area:
            chosen=area[sid];kind='Compartilhado';note='Proposta: variante em área compartilha o banco da habilidade individual; tocar uma vez na área, sem multiplicar volume pelo número de alvos.'
            if sid==73:kind='Compatibilidade';note+=' Maeiga do catálogo usa Eiga (71) por nome; o código 73 original é Maeiha. Essa equivalência precisa de aprovação.'
        else:chosen=sid
        candidates=sorted(by_source[chosen],key=lambda e:e['id'])
        assert candidates,(s['id'],chosen)
        # Theurgia MixRaid waves are presented as a candidate, never claimed to
        # reproduce the full original cue graph. Other pieces remain available.
        primary=next((e for e in candidates if 'SE_Btl_MixRaid' in e.get('cueNames','')),candidates[0])
        when='Na aplicação visual da habilidade, associada à ação aceita. O fluxo atual resolve a magia no tick 86; alinhar o início audível com o VFX durante a integração.'
        if 260<=sid<=266:
            when='No marcador principal da respectiva teurgia; não herdar automaticamente o tick da invocação normal.'
            note+=' Candidato MixRaid; os cortes separados estão preservados para montagem posterior, sem mistura automática.'
        row=route('skill_'+s['id'],s['name'],'Teurgias' if 260<=sid<=266 else 'Habilidades',[primary['id']],
            f"{s['element']} · {s['target']} · qualquer Persona ou inimigo que use {s['name']}.",when,note,kind=kind,
            hook='forge/PersonaCinematicServer.java, CinematicEffects.java e resolução de ação inimiga',subject={'skillId':s['id'],'catalogueSource':sid,'audioSource':chosen,'element':s['element']})
        mappings.append(row)
    return skills,mappings

def item_routes():
    base=ROOT/'src/main/java/com/gx/velveteclipse/equipment'
    supplies=re.findall(r'new Supply\("([^"]+)","([^"]+)",\d+,(-?\d+),(-?\d+),"([^"]*)"',(base/'Supplies.java').read_text(encoding='utf-8'))
    specs=[]
    for id,name,hp,sp,cures in supplies:
        clip='skill_0157_01' if int(hp)==-100 else 'skill_0155_01' if int(hp)!=0 else 'skill_0177_01' if cures=='ALL' else 'skill_0172_01' if id=='dis_poison' else 'skill_0170_01' if cures else None
        specs.append((id,name,clip,'Recuperação de HP ou remoção de estado no alvo.' if not int(sp) else 'Recuperação de SP: usar apenas o som de item; efeito dedicado de SP não confirmado.'))
    specs.append(('pulse_tonic','Pulse Tonic','skill_0155_01','Recupera 30% de HP no código atual.'))
    item_source={'agilao_gem':11,'maragi_gem':10,'bufula_gem':21,'mabufu_gem':20,'zionga_gem':41,'mazio_gem':40,'garula_gem':31,'magaru_gem':30,'kouga_gem':57,'makouha_gem':56,'eiga_gem':71,'maeiha_gem':70,'megido_gem':78,'megidolaon_gem':80,'flame_magatama':12,'frigid_magatama':22,'shock_magatama':42,'gale_magatama':32,'holy_magatama':58,'malice_magatama':72}
    damaging=re.findall(r'new Item\("([^"]+)","([^"]+)"',(base/'DamageItems.java').read_text(encoding='utf-8'))
    for id,name in damaging:specs.append((id,name,f'skill_{item_source[id]:04}_01','Gema/magatama: proposta de efeito da magia equivalente; não invocar Persona nem tocar a voz de uma Persona.'))
    for id,name,clip,note in specs:
        route('item_'+id,name,'Itens',['item_use']+([clip] if clip else []),'Uso de '+name+' em combate; fora dele apenas se já houver uma ação de uso válida.',
            'Som de uso após consumo confirmado; resultado no alvo quando HP/SP/estado/dano for aplicado. Não tocar os dois no mesmo marcador se houver animação de uso.',
            note+' Adaptação de uso de áudio original. Não há item de ressurreição no catálogo atual.',kind='Adaptação',hook='equipment/Supplies.java, DamageItems.java e forge/BattleService.java',subject={'itemId':id})
    return specs

def furniture_routes():
    f=json.loads(gzip.decompress((ROOT/'src/main/resources/data/velvet_eclipse/city/furniture.json.gz').read_bytes()))
    mapping=[]
    for d in f['doors']:
        id=d['id'];room=id.startswith('room_');school=id.startswith('school_')
        pair=['room_open','room_close'] if room or school else ['wood_open','wood_close']
        where=('Gekkoukan · '+id.removeprefix('school_')) if school else ('Dormitório · '+id)
        note='Som de porta de cômodo, proposto para a geometria atual; não pressupõe mecanismo deslizante.' if school else 'Escolha por tipo e localização da porta.'
        r=route('door_'+id,where,'Portas e móveis',pair,where,'Abrir: '+pair[0]+'. Fechar: '+pair[1]+'. Somente após troca de estado bem-sucedida.',
            note+' Uma vez por porta completa, não por bloco; porta obstruída e restauração automática são silenciosas.',kind='Adaptação',audience=WORLD,
            hook='city/CityFurniture.java: interact/change',subject={'doorId':id,'bounds':d['bounds']})
        mapping.append(r)
    route('velvet_door','Entrada da Velvet Room','Portas e móveis',['velvet_open'],'Porta da Velvet Room no Paulownia e demais acessos existentes.',
        'Na abertura/passagem confirmada; deduplicar clique e teleporte da mesma travessia.',
        'Candidato original específico da Velvet Room. Não reaproveitar como som de fechar: fechamento dedicado ainda não identificado. Evitar reprodução junto do som vanilla da porta.',audience=WORLD,hook='room/VelvetDoorBlock.java / VelvetDoorPassage')
    route('seat_sit','Sentar','Portas e móveis',['cloth_soft'],'Cadeiras e bancos registrados: dormitório, Gekkoukan, Paulownia; cadeiras da Velvet Room.',
        'Após montar com sucesso no assento.', 'Roupa suave original aplicada à interação. Não é rangido de cadeira. Computadores continuam sem interação nova.',kind='Adaptação',audience=WORLD,hook='city/CityFurniture.java e room/VelvetSeatEntity.java')
    route('seat_stand','Levantar','Portas e móveis',['cloth_soft'],'Os mesmos assentos.',
        'Após desmontar voluntariamente e chegar à posição segura.', 'Não tocar em morte, desconexão, limpeza de entidade, teleporte ou restauração do mundo.',kind='Adaptação',audience=WORLD,hook='room/VelvetSeatEntity.java / evento de desmontagem voluntária')
    return f,mapping

def voice_routes():
    manifest=read(A03/'manifest.json');calls={e['id']:e for e in manifest['calls']};bindings=read(A03/'bindings.json')['bindings']
    for persona,b in bindings.items():
        call=calls[b['voiceId']]
        route('voice_for_'+persona,persona.replace('_',' ').title(),'Vozes',['voice_'+call['id']],
            'Persona '+persona+' · funciona também sem modelo próprio.',
            'Depois de um acerto desta Persona em fraqueza que conceda One More. Uma fala por ação, não por alvo ou pacote de sincronização.',
            ('Nome original em inglês: '+call['name']+'. '+(call.get('spokenNameNote') or '')) if b['kind']=='named' else 'Persona autoral sem chamada nominal no jogo: usar a fala genérica original “Persona!”. Não herdar o nome do modelo substituto.',
            kind='Original' if b['kind']=='named' else 'Adaptação',hook='BattleSession: resultado causal por ação/alvo → BattleService: voz aos participantes',subject={'personaId':persona,'voiceId':call['id'],'requiresModel':False})
    return bindings

def main():
    OUT.mkdir(parents=True,exist_ok=True);DELIVERY.mkdir(parents=True,exist_ok=True)
    entries=collect();core_routes();skills,skillmap=skill_routes(entries);items=item_routes();furniture,doors=furniture_routes();bindings=voice_routes()
    by_id={e['id']:e for e in entries};assert len(by_id)==len(entries)
    use=defaultdict(list)
    for r in routes:
        for key in r['clips']:
            assert key in by_id,(r['id'],key)
            use[key].append(r['id'])
    reserve_notes={'evoker_alt':'Alternativa original do gatilho; comparar com evoker antes de substituir.',
      'velvet_open_alt':'Alternativa original da abertura Velvet; sem evento ativo adicional.',
      'auto_open':'Abertura automática para futura porta que realmente possua essa interação; vitrines e indicadores de loja não devem disparar portas fictícias.',
      'auto_close':'Fechamento automático; aguarda porta funcional correspondente.',
      'slide_open':'Portas deslizantes: conservar até confirmar esse mecanismo nos objetos do mod.',
      'slide_close':'Fechamento deslizante: conservar até existir estado de fechar correspondente.',
      'slash_heavy':'Alternativa de corte pesado; não misturar com slash_medium na mesma ação.',
      'slash_light':'Alternativa de corte leve; futura diferenciação por arma.',
      'pierce_heavy':'Alternativa de perfuração pesada; futura diferenciação por arma.',
      'swish_heavy':'Movimento pesado alternativo; manter até definir o marcador de arma pesada.'}
    for e in entries:
        e['routeIds']=use[e['id']];e['selection']='Proposto' if e['routeIds'] else 'Reserva'
        e['category']={'Físicos':'Ataques físicos','Objetos':'Portas e móveis','Combate e menus':'Resultados de combate'}.get(e['category'],e['category'])
        if e['routeIds']:
            e['category']=next(r['category'] for r in routes if r['id']==e['routeIds'][0])
        if not e['routeIds']:
            if e['kind']=='voice':reason='Chamada DLC fora do catálogo atual; sem disparo no mod.' if e.get('scope')=='extra' else 'Variante genérica original; não adicionar fala a toda invocação sem revisão.'
            else:reason=reserve_notes.get(e['id'],'Onda adicional ou habilidade sem uso proposto no catálogo atual. Preservada para comparação e futura montagem; não randomizar/sobrepor automaticamente.')
            e['reserveReason']=reason
        else:e['reserveReason']=None
        e['plannedResource']=('assets/velvet_eclipse/sounds/'+('voices/en/'+e['sourceId'] if e['kind']=='voice' else 'effects/reload/'+e['id'])+'.ogg') if e['routeIds'] else None
        e['plannedSoundId']=('velvet_eclipse:'+('voice.persona.'+e['sourceId'] if e['kind']=='voice' else 'sfx.'+e['id'])) if e['routeIds'] else None
    # Preserve the current project's audio in place, with an inventory. Large
    # existing awakening tracks are not redundantly bundled into this new audit.
    soundroot=ROOT/'src/main/resources/assets/velvet_eclipse'
    existing={'registry':read(soundroot/'sounds.json'),'files':[{'path':p.relative_to(ROOT).as_posix(),'sha256':sha(p),'bytes':p.stat().st_size} for p in sorted((soundroot/'sounds').rglob('*')) if p.is_file()],
      'policy':'Permanecem nos caminhos atuais do projeto; não substituídos nem duplicados neste pacote.'}
    summary={'preservedClips':len(entries),'voices':sum(e['kind']=='voice' for e in entries),'effects':sum(e['kind']=='effect' for e in entries),
      'proposedClips':sum(bool(e['routeIds']) for e in entries),'reserveClips':sum(not e['routeIds'] for e in entries),'personaBindings':len(bindings),
      'activeSkills':len(skillmap),'passiveSkillsWithoutDedicatedCue':sum(bool(s.get('passive')) for s in skills),'items':len(items),'cityDoors':len(doors),'citySeats':len(furniture['seats']),
      'routes':len(routes),'integrated':False,'approved':False,'jarBuilt':False,'date':'2026-10-22'}
    decisions=[
      {'id':'voice-causal','title':'Falas apenas no One More por fraqueza','text':'175 nomes originais; seis Personas autorais usam “Persona!”. Não falar em ataque básico, item, crítico isolado ou todo cast. Idioma da voz permanece inglês, independente do idioma do menu.'},
      {'id':'shared-area','title':'Magias de área','text':'Reutilizar o áudio da versão individual nos casos listados como Compartilhado. Uma reprodução no centro da área, sem N cópias em volume total.'},
      {'id':'psy-nuclear','title':'Psíquico e nuclear são adaptações','text':'Pulinpa para a família psíquica; Megido/Megidola/Megidolaon para nuclear. Aprovação dessa proposta não implica que existam gravações de Psi/Frei em Reload.'},
      {'id':'item-seat','title':'Itens e assentos','text':'Uso de item original + cura/limpeza correspondente; SP só usa o som de item. Sentar/levantar usam roupa suave. Sem inventar rangido, trinco ou efeito dedicado de SP.'},
      {'id':'mismatch','title':'Eiga e Maeiga','text':'Proposta de usar o banco Eiga 71 pelo nome. Os códigos 70/73 do catálogo do mod divergem dos nomes Eiha/Maeiha da instalação. Esta etapa não altera habilidades nem balanceamento.'},
      {'id':'layers','title':'Camadas e alternativas','text':'Um candidato principal por habilidade. Todas as outras ondas ficam em reserva; montagem do cue original e marcadores finais ainda exigem revisão de integração.'},
    ]
    omissions=[
      'Bloqueio/esquiva: não foi confirmado um efeito dedicado. Guard mantém confirmação de menu; esquiva mantém movimento e remove contato, sem acrescentar som errado.',
      'Fraqueza sem One More: não substituir pelo som de crítico. Não foi selecionado um cue exclusivo de fraqueza.',
      'Fechar Velvet Room: não foi identificado cue dedicado de fechamento; não inverter nem reutilizar abertura como se fosse fechamento.',
      'Computadores, armários decorativos, vitrines e objetos sem interação continuam sem novos sons de uso.',
      'Amostra silenciosa skill_0390_04, falas sintéticas rejeitadas e recortes de vídeo com música não entram na seleção.',
      'Músicas, diálogos de vendedores e novos sons ambiente da cidade não fazem parte desta leva de efeitos de ação.'
    ]
    manifest={'date':summary['date'],'status':'AUDITORIA — AGUARDANDO APROVAÇÃO DO USUÁRIO','summary':summary,'entries':entries,'routes':routes,'decisions':decisions,'omissions':omissions,
      'processing':'Masters originais de voz byte a byte; efeitos decodificados float32 nativos. Audição voz −3 dB e efeitos −6 dB ou margem extra. Sem cortes, mudança de pitch/velocidade ou síntese. OGG e registro futuro só após aprovação.',
      'sourceManifests':{'03':sha(A03/'manifest.json'),'04':sha(A04/'manifest.json'),'extras05':sha(EXTRA/'manifest.json')}}
    save(OUT/'manifest.json',manifest);save(OUT/'mapa-de-uso.json',routes);save(OUT/'existing-audio-preserved.json',existing)
    for label,source in [('voices-03',A03),('effects-04',A04),('extras-05',EXTRA)]:
        copy(source/'manifest.json',OUT/'provenance'/(label+'.json'))
    save(OUT/'door-map.json',doors);save(OUT/'persona-map.json',bindings);save(OUT/'skill-map.json',skillmap)
    save(OUT/'approval-template.json',{'audit':'05','approved':False,'decisions':{d['id']:'pendente' for d in decisions},'notes':''})
    write_docs(manifest,existing)
    template=Path(__file__).with_name('placement_panel.html').read_text(encoding='utf-8')
    payload=json.dumps(manifest,ensure_ascii=False).replace('</','<\\/')
    (OUT/'index.html').write_text(template.replace('__AUDIT_DATA__',payload),encoding='utf-8')
    validate(manifest)
    for p in OUT.rglob('*'):
        if p.is_file():copy(p,DELIVERY/p.relative_to(OUT))
    archive=BASE/'outputs/velvet-eclipse-audio-preservacao-e-mapa-05.zip'
    with zipfile.ZipFile(archive,'w',zipfile.ZIP_DEFLATED,compresslevel=6) as z:
        for p in sorted(DELIVERY.rglob('*')):
            if p.is_file():z.write(p,Path(DELIVERY.name)/p.relative_to(DELIVERY))
    with zipfile.ZipFile(archive) as z:assert z.testzip() is None;count=len(z.infolist())
    report={'summary':summary,'archive':str(archive),'sha256':sha(archive),'archiveFiles':count,'archiveBytes':archive.stat().st_size,'panel':str(DELIVERY/'index.html')}
    save(WORK/'delivery.json',report);print(json.dumps(report,ensure_ascii=True,indent=2))

def write_docs(m,existing):
    s=m['summary'];lines=['# Áudios preservados e mapa de uso — auditoria 05','',
      '22/10/2026. Proposta para aprovação antes da implementação, conforme solicitado. Não é uma versão do mod.', '',
      f"{s['preservedClips']} clipes preservados: {s['voices']} vozes e {s['effects']} efeitos. {s['proposedClips']} têm uso proposto e {s['reserveClips']} ficam em reserva, identificados individualmente. Cópias de audição e masters não contam como dois clipes.", '',
      f"Cobertura: {s['personaBindings']} Personas registradas, {s['activeSkills']} habilidades ativas, {s['items']} itens, {s['cityDoors']} portas da cidade e acesso Velvet. {s['citySeats']} assentos da cidade mais cadeiras Velvet usam a proposta de sentar/levantar. {s['passiveSkillsWithoutDedicatedCue']} passivas não disparam áudio próprio.", '',
      'Abra index.html para buscar por Persona, habilidade, item, cômodo ou som e ouvir cada amostra. A página tem decisões locais por grupo e exportação de notas; essas marcações não implementam nem autorizam automaticamente alterações. Use o chat para confirmar a aprovação.', '',
      '## Organização dos arquivos','',
      '- `audio/voices` e `audio/effects`: versões de audição; `masters`: fontes preservadas para conversão posterior.',
      '- `mapa-de-uso.json`: local, condição, audiência e ponto de integração de cada uso.',
      '- `manifest.json`: cada clipe, hash, fonte, reserva ou uso e caminho OGG futuro. Os caminhos futuros ainda não existem no mod.',
      '- `MAPA-COMPLETO.md`: todas as colocações propostas e lista de reservas.',
      '- `existing-audio-preserved.json`: inventário dos áudios já existentes no projeto, preservados nos próprios caminhos; não duplicados neste ZIP.',
      '', '## Proposta resumida', '',
      '| Grupo | Uso proposto |','|---|---|',
      '| Invocação | Evoker no disparo, manifestação ao aparecer, aura/cut-in somente quando visíveis. |',
      '| Vozes | Nome inglês da Persona depois do acerto em fraqueza que gera One More. |',
      '| Físicos | Movimento no golpe, impacto no contato; arco/arma de fogo com disparo; tapinha leve em Pixie/Apsaras. |',
      '| Magias e teurgias | Arquivo associado a cada habilidade do catálogo; alternativas e camadas em reserva. |',
      '| Itens | Uso confirmado e efeito correspondente no alvo; SP usa apenas item_use. |',
      '| Portas e móveis | Abrir/fechar após sucesso; sentar/levantar com roupa suave; nenhuma reprodução por bloco ou atualização. |',
      '| Menus | Cursor, alvo, confirmar, cancelar, recusa; só para o jogador que usa o menu. |',
      '| Resultados | One More, crítico, queda, absorção e reflexão apenas quando o resultado correspondente é confirmado. |',
      '', '## Decisões para aprovação','']
    for d in m['decisions']:lines.extend(['### '+d['title'],'',d['text'],''])
    lines.extend(['## Limites e lacunas','']+['- '+x for x in m['omissions']]+['','## Preservação e origem','',m['processing'],'',
      'Vozes: ZIP enviado pelo usuário, identificação contextual e ASR registrada na auditoria 03; nomes próprios ainda dependem da escuta de aprovação. Efeitos: instalação local informada pelo usuário, nomes dos cues e tabela inglesa DatSkillNameDataAsset. As 20 amostras adicionais cobrem teurgias, arco e arma de fogo. Nenhum download de vídeo compõe a seleção.', '',
      'Ouvir uma onda exportada não equivale a reproduzir a sequência/mistura completa de Reload. As propostas de compartilhamento e adaptação estão explícitas. O som deve seguir o ID da habilidade/Persona, independente de haver modelo próprio.', '',
      '## Contrato para a futura integração','',
      'Cada ação recebe identificador de sessão e ação. Disparar uma vez por marcador; descartar eventos de sessões encerradas e limpar fila ao cancelar. Filtrar destinatários por sessão: a batalha de outro grupo não deve ouvir essas falas. Sons de objeto são posicionais e limitados à dimensão/proximidade; menus são locais.', '',
      'One More nominal exige resultado causal do acerto: tipo de ação Persona + identidade da Persona usada + alvo fraco + concessão nova de One More por essa fraqueza. Não basta ler flags agregadas weaknessHit/oneMore quando alvos diferentes podem ter gerado eventos diferentes. Não disparar em sincronização de HUD, crítico isolado, item, reflexão ou a cada alvo de área.', '',
      'Voz tem prioridade sobre efeitos: propor atenuar efeitos durante a chamada e limitar sobreposição. Volume final e deslocamento de cada onda até o marcador serão calibrados após aprovação. Não truncar caudas na conversão; não repetir uma onda completa de magia em cada alvo.', '',
      'Habilidade com áudio próprio substitui o impacto genérico. Portas restauradas por tick ou reconstrução não tocam. Falha ao sentar/abrir não produz som de sucesso. Evitar clique vanilla duplicado.', '',
      'Validação desta entrega é offline: integridade, cobertura, formatos, arquivos e sintaxe da página. Sem teste no Minecraft ou afirmação de escuta humana. Não foi gerado JAR, commit ou instalação.',''])
    (OUT/'README.md').write_text('\n'.join(lines),encoding='utf-8')
    lines=['# Mapa completo de colocação proposta','','Todos os usos abaixo aguardam aprovação.','']
    for category in dict.fromkeys(r['category'] for r in routes):
        lines+=['## '+category,'','| Ação/local | Áudio | Momento | Observação |','|---|---|---|---|']
        for r in routes:
            if r['category']!=category:continue
            esc=lambda x:x.replace('|',' / ').replace('\n',' ')
            lines.append('| '+' | '.join(esc(x) for x in [r['name']+' — '+r['where'],', '.join(r['clips']),r['when'],r['kind']+': '+r['note']])+' |')
        lines.append('')
    lines+=['## Reservas: preservar sem disparo no mod','','| Arquivo | Finalidade da preservação |','|---|---|']
    lines += ['| '+e['id']+' | '+e['reserveReason']+' |' for e in m['entries'] if not e['routeIds']]
    (OUT/'MAPA-COMPLETO.md').write_text('\n'.join(lines)+'\n',encoding='utf-8')

def validate(m):
    assert m['summary']['personaBindings']==181 and m['summary']['activeSkills']==171
    assert len({r['id'] for r in routes})==len(routes)
    for e in m['entries']:
        assert sha(OUT/e['file'])==e['sha256'] and sha(OUT/e['master'])==e['masterSha256'],e['id']
        with wave.open(str(OUT/e['file']),'rb') as w:
            assert w.getnframes()==e['frames'] and w.getframerate()==e['rate'] and w.getnchannels()==e['channels']
        assert e['routeIds'] or e['reserveReason']
        assert not e.get('synthetic') and not e.get('silent')
    current={str(p.relative_to(ROOT/'src')):sha(p) for p in (ROOT/'src').rglob('*') if p.is_file()}
    assert current==read(WORK/'production-before.json'),'Production changed during audit'
    save(OUT/'validation.json',{'hashesAndFormats':True,'completeMapping':True,'productionUnchanged':True,'voices':181,'activeSkills':171,'inGameTest':False,'userListeningApproval':False,'jarBuilt':False})

if __name__=='__main__':main()
