package com.gx.velveteclipse.forge;

import com.gx.velveteclipse.core.battle.*;
import com.gx.velveteclipse.shadow.*;
import com.gx.velveteclipse.core.persona.*;
import com.gx.velveteclipse.network.*;
import com.gx.velveteclipse.util.RuntimeBridge;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import java.util.*;

/** A single authoritative session/timer per encounter. Membership indexes never tick combat. */
public final class BattleService {
    private static final Map<UUID,Encounter> BATTLES=new LinkedHashMap<>();
    private static final Map<UUID,Encounter> MEMBERS=new HashMap<>();
    private static int ticks;
    private static final class Encounter {
        final BattleSession session;
        final LinkedHashMap<UUID,Object> players=new LinkedHashMap<>();
        final Map<UUID,double[]> anchors=new HashMap<>();
        final Map<UUID,UUID> targets=new HashMap<>();
        final Map<UUID,AffinityKnowledge> raidKnowledge=new HashMap<>();
        final Set<UUID> recordedDefeats=new HashSet<>();
        final Object level;
        final double[] origin,center;
        final float yaw;
        final int enemyCount;
        int delay=28;final Map<UUID,Integer> escapeReady=new HashMap<>();
        boolean wasBusy;
        boolean shuffleVictory,boss,raid,rare;int enemyPoseUntil;Entity animatingEnemy;int raidStage,dungeonFloor;
        final BattleResultsGate results=new BattleResultsGate();
        String ending="";
        Encounter(Object p,BattleSession s,int count){session=s;enemyCount=count;origin=RuntimeBridge.playerPosition(p);yaw=RuntimeBridge.playerYaw(p);center=RuntimeBridge.localOffset(origin,yaw,0,.05,5.25);level=p instanceof Entity e?e.m_9236_():null;}
    }
    private static Encounter group(Object p){return p==null?null:MEMBERS.get(RuntimeBridge.playerUuid(p));}
    public static BattleSession current(Object p){var g=group(p);return g==null?null:g.session;}
    public static boolean encounterActive(UUID id){return BATTLES.containsKey(id);}
    public static boolean hasActiveBattle(Object p){var s=current(p);return s!=null&&s.state()==BattleState.ACTIVE;}
    public static List<Object> members(Object p){var g=group(p);return g==null?List.of():List.copyOf(g.players.values());}
    private static boolean busy(Encounter g){return g.raid&&com.gx.velveteclipse.raid.RaidService.animating(g.session.id())||g.players.values().stream().anyMatch(p->MeleeBattleServer.active(p)||PersonaCinematicServer.suppressLegacySettle(p)||AllOutServer.active(p));}
    public static boolean actionBusy(Object p){var g=group(p);return g!=null&&(g.results.started()||g.session.state()!=BattleState.ACTIVE||g.session.prepared()!=null||busy(g));}
    private static BattleCombatant combatant(Object p){var d=PlayerPersonaData.load(p);return new BattleCombatant(RuntimeBridge.playerUuid(p),RuntimeBridge.playerName(p),BattleSide.PLAYERS,d.activeInstance(),d.maxHp(),d.hp(),d.maxSp(),d.sp()).equipment(com.gx.velveteclipse.equipment.EquipmentData.load(p).bonuses());}
    private static double distance(double[] a,double[] b){double x=a[0]-b[0],y=a[1]-b[1],z=a[2]-b[2];return x*x+y*y+z*z;}

    public static BattleSession startDemo(Object p){return startDemo(p,1);}
    public static BattleSession startDemo(Object p,int count){return startEncounter(p,count,0);}
    public static BattleSession startEncounter(Object p,int count,int dungeonFloor){
        return startEncounter(p,count,dungeonFloor,false);
    }
    public static BattleSession startEncounter(Object p,int count,int dungeonFloor,boolean boss){
        if(com.gx.velveteclipse.raid.RaidService.inside(p))return current(p);
        BattleFormation.validateCount(count);
        if(com.gx.velveteclipse.room.VelvetRoomService.inside(p)||com.gx.velveteclipse.exploration.ExplorationService.blocksBattleStart(p)||current(p)!=null)return current(p);
        var participants=new ArrayList<BattleCombatant>();participants.add(combatant(p));
        int floor=Math.max(0,Math.min(250,dungeonFloor)),hp=com.gx.velveteclipse.darkhour.TartarusInteriorLayout.hp(floor);
        for(int i=0;i<(boss?1:count);i++){
            var def=boss?EnemyCatalog.guardian(floor):EnemyCatalog.pool(floor).get(i%EnemyCatalog.pool(floor).size());
            int level=boss?Math.max(def.level(),com.gx.velveteclipse.darkhour.TartarusInteriorLayout.level(floor)+5):def.level();
            participants.add(BattleCombatant.enemy(UUID.randomUUID(),(boss?"Guardião do Tártaro • ":"")+def.displayName(),def,level,boss?hp*4:def.hp(),boss?80+floor:def.sp(),def.skills()));
        }
        var s=new BattleSession(participants);var g=new Encounter(p,s,count);g.dungeonFloor=floor;g.boss=boss;BATTLES.put(s.id(),g);attach(g,p,g.origin);
        VisualBattleActors.spawnBattle(p,PlayerPersonaData.load(p).activePersona(),"umbra_slime");
        if(boss)for(var c:s.participants())if(c.side()==BattleSide.ENEMIES){var e=targetActor(p,c.id());if(e!=null)com.gx.velveteclipse.darkhour.TartarusDungeonService.markGuardian(e);}
        syncAll(g,count+" Shadow"+(count==1?"":"s")+" bloqueiam o caminho.");return s;
    }
    public static BattleSession startWorldEncounter(Object p,com.gx.velveteclipse.shadow.FieldShadowEntity field,EncounterOpening opening){
        if(com.gx.velveteclipse.raid.RaidService.inside(p)||com.gx.velveteclipse.room.VelvetRoomService.inside(p)||com.gx.velveteclipse.exploration.ExplorationService.blocksBattleStart(p)||current(p)!=null)return current(p);
        int floor=Math.max(0,com.gx.velveteclipse.darkhour.TartarusDungeonService.floor(p));var roster=com.gx.velveteclipse.shadow.ShadowEncounterRules.roll(field.encounterSeed(),floor,field.rare());
        var participants=new ArrayList<BattleCombatant>();participants.add(combatant(p));int index=0;
        for(var enemy:roster){var def=EnemyCatalog.get(enemy.persona());participants.add(BattleCombatant.enemy(UUID.randomUUID(),(field.rare()?"Red • ":"")+def.displayName()+" "+(++index),def,enemy.level(),enemy.hp(),enemy.sp(),enemy.skills()));}
        var s=new BattleSession(participants,new Random(),opening);var g=new Encounter(p,s,roster.size());g.dungeonFloor=floor;g.rare=field.rare();BATTLES.put(s.id(),g);attach(g,p,g.origin);
        VisualBattleActors.spawnBattle(p,PlayerPersonaData.load(p).activePersona(),"");
        String message=switch(opening){case ADVANTAGE->"VANTAGEM DA EQUIPE • Aliados começam!";case AMBUSH->"EMBOSCADA • Inimigos começam!";default->"ENCONTRO NEUTRO • Ordem inicial sorteada.";};
        if(g.rare)message+=" • SHADOW VERMELHA: inimigos poderosos, recompensas maiores.";
        syncAll(g,message);return s;
    }
    public static BattleSession startRaid(List<ServerPlayer> party,int stage,Entity enemy){
        if(party.isEmpty()||enemy==null||party.stream().anyMatch(p->current(p)!=null))return null;
        var participants=new ArrayList<BattleCombatant>();for(var p:party)participants.add(combatant(p));
        var def=EnemyCatalog.get(stage==0?"magic_hand":stage==1?"trance_twins":"grieving_tiara");
        int hp=stage==2?com.gx.velveteclipse.raid.PriestessRules.bossHp(party.size()):180+65*party.size();
        var foe=BattleCombatant.enemy(UUID.randomUUID(),stage==2?"Sacerdotisa":stage==0?"Sentinela do Trem":"Condutor Sombrio",def,stage==2?30:25,hp,9999,Set.of("raid_ice_storm","shade_bite"));
        if(stage==2)foe.setRaidBoss(Element.FIRE);participants.add(foe);
        var s=new BattleSession(participants);var g=new Encounter(party.get(0),s,1);g.raid=true;g.raidStage=stage;g.boss=true;g.dungeonFloor=25;
        BATTLES.put(s.id(),g);for(var p:party){attach(g,p,RuntimeBridge.playerPosition(p));VisualBattleActors.replacePersona(p,PlayerPersonaData.load(p).activePersona());}
        VisualBattleActors.bindEnemy(s.id(),foe.id(),enemy);syncAll(g,"O caminho está bloqueado. Preparem-se!");return s;
    }
    private static void attach(Encounter g,Object p,double[] anchor){
        if(p instanceof ServerPlayer sp)sp.m_6915_();
        UUID id=RuntimeBridge.playerUuid(p);g.players.put(id,p);g.anchors.put(id,anchor);MEMBERS.put(id,g);
        for(var enemy:g.session.participants())if(enemy.side()==BattleSide.ENEMIES&&enemy.alive())BestiaryService.seen(p,knowledgeKey(g,enemy),enemy.persona().level());
        RuntimeBridge.setEntityRotation(p,g.yaw,2);
        if(p instanceof ServerPlayer sp)sp.f_8906_.m_9774_(anchor[0],anchor[1],anchor[2],g.yaw,2);else RuntimeBridge.freezePlayer(p,anchor);
        ModNetwork.sendPresentation(p,new BattleSceneS2C(List.of()));
    }
    public static boolean join(Object object,UUID battleId){
        var g=BATTLES.get(battleId);
        if(!(object instanceof ServerPlayer p)||g==null||current(p)!=null||!canJoin(p,g))return false;
        double[] anchor=null;
        for(int i=1;i<14;i++){
            double[] candidate=RuntimeBridge.localOffset(g.origin,g.yaw,BattleFormation.allyX(i),0,0);
            if(g.anchors.values().stream().anyMatch(a->distance(a,candidate)<2.25))continue;
            for(double dy:new double[]{0,1,-1,2,-2}){
                double[] at={candidate[0],candidate[1]+dy,candidate[2]};var box=p.m_20191_().m_82386_(at[0]-p.m_20185_(),at[1]-p.m_20186_(),at[2]-p.m_20189_());
                if(p.m_9236_().m_45756_(p,box)&&!p.m_9236_().m_45756_(p,box.m_82386_(0,-.7,0))){anchor=at;break;}
            }
            if(anchor!=null)break;
        }
        if(anchor==null){RuntimeBridge.sendMessage(p,"§eNão há espaço livre ao lado do grupo.");return false;}
        if(!g.session.join(combatant(p)))return false;
        attach(g,p,anchor);VisualBattleActors.replacePersona(p,PlayerPersonaData.load(p).activePersona());syncAll(g,RuntimeBridge.playerName(p)+" entrou na batalha!");return true;
    }
    private static boolean canJoin(ServerPlayer p,Encounter g){return !g.raid&&g.session.state()==BattleState.ACTIVE&&g.players.values().stream().noneMatch(AllOutServer::active)&&p.m_6084_()&&p.m_9236_()==g.level&&distance(RuntimeBridge.playerPosition(p),g.center)<=81&&!com.gx.velveteclipse.exploration.ExplorationService.blocksBattleStart(p)&&PlayerPersonaData.load(p).hp()>0;}

    /** Leaving/disconnecting removes only that participant; an interrupted reservation costs nothing. */
    public static void stop(Object p){
        var g=group(p);if(g==null){sync(p,"Batalha encerrada.");return;}
        if(g.results.started()){acknowledge(p,g.session.id().toString());return;}
        AllOutServer.cancel(p);persist(p,g.session);UUID id=RuntimeBridge.playerUuid(p);var reserved=g.session.prepared();
        if(reserved!=null&&(reserved.actor().equals(id)||id.equals(reserved.action().targetId()))){
            Object acting=g.players.get(reserved.actor());if(acting!=null){MeleeBattleServer.clear(acting);PersonaCinematicServer.clear(acting);ModNetwork.sendPresentation(acting,new BattleCueS2C("cancel","",false,0,0,0,0,0,0));}g.session.cancelPrepared();
        }
        detach(g,p);g.session.leave(id);sync(p,"Você saiu da batalha.");
        if(g.players.isEmpty()){ShadowWorldService.release(g.session.id(),false);VisualBattleActors.clearEnemies(g.session.id());BATTLES.remove(g.session.id());}
        else if(g.session.state()!=BattleState.ACTIVE){if(!busy(g))finish(g,"Batalha encerrada.");}else syncAll(g,RuntimeBridge.playerName(p)+" saiu da batalha.");
    }
    public static void abortEncounter(Object p){var g=group(p);if(g==null)return;ShadowWorldService.release(g.session.id(),false);BATTLES.remove(g.session.id());g.session.cancelPrepared();for(Object member:new ArrayList<>(g.players.values())){AllOutServer.cancel(member);PersonaCinematicServer.clear(member);ModNetwork.sendPresentation(member,new BattleCueS2C("cancel","",false,0,0,0,0,0,0));persist(member,g.session);detach(g,member);sync(member,"A Dark Hour terminou.");}VisualBattleActors.clearEnemies(g.session.id());}
    private static void detach(Encounter g,Object p){UUID id=RuntimeBridge.playerUuid(p);MeleeBattleServer.clear(p);VisualBattleActors.clear(p);MEMBERS.remove(id);g.players.remove(id);g.anchors.remove(id);g.targets.remove(id);}
    public static Entity targetActor(Object p,UUID id){var g=group(p);if(g==null||id==null)return null;Object target=g.players.get(id);return target instanceof Entity e?e:VisualBattleActors.enemy(g.session.id(),id);}
    public static UUID targetId(Object p){var g=group(p);if(g==null)return null;UUID id=RuntimeBridge.playerUuid(p);var r=g.session.prepared();if(r!=null&&r.actor().equals(id))return r.action().targetId();if(g.targets.containsKey(id))return g.targets.get(id);return g.session.participants().stream().filter(c->c.side()==BattleSide.ENEMIES&&c.alive()).map(BattleCombatant::id).findFirst().orElse(null);}
    public static void beginAction(Object p,BattleAction action){var g=group(p);if(g!=null){g.targets.put(RuntimeBridge.playerUuid(p),action.targetId());g.wasBusy=true;syncAll(g,RuntimeBridge.playerName(p)+" está agindo.");}}
    public static List<Entity> effectTargets(Object p,String skillId){
        var g=group(p);if(g==null)return List.of();var skill=PersonaRegistry.skill(skillId).orElse(null);var actor=g.session.find(RuntimeBridge.playerUuid(p)).orElse(null);if(actor==null)return List.of();
        List<BattleCombatant> targets=skill!=null&&skill.target()==SkillTarget.ALL_ENEMIES?g.session.enemiesOf(actor):skill!=null&&skill.target()==SkillTarget.ALL_ALLIES?g.session.alliesOf(actor):g.session.find(targetId(p)).map(List::of).orElse(List.of());
        return targets.stream().map(c->targetActor(p,c.id())).filter(Objects::nonNull).toList();
    }
    public static boolean act(Object p,BattleAction action){
        var g=group(p);if(g==null)return false;var s=g.session;UUID id=RuntimeBridge.playerUuid(p);
        if(g.results.started()||s.state()!=BattleState.ACTIVE||g.raid&&!com.gx.velveteclipse.raid.RaidService.allowAction(s.id()))return false;
        if(busy(g)&&(s.prepared()==null||!s.prepared().actor().equals(id)||!s.prepared().action().equals(action)))return false;
        if(s.currentActor()==null||!s.currentActor().id().equals(id)){sync(p,"Aguarde seu turno.");return false;}
        if(s.prepared()!=null&&!s.prepared().action().equals(action))return false;
        if(action.type()==BattleActionType.ESCAPE){if(g.raid){sync(p,"Não é possível fugir dentro do trem.");return false;}if(ticks<g.escapeReady.getOrDefault(id,0)){sync(p,"Aguarde antes de tentar fugir novamente.");return false;}g.escapeReady.put(id,ticks+40);if(s.attemptEscape(id)){ShadowWorldService.escapeGrace(p);stop(p);}else{g.delay=28;syncAll(g,"A fuga de "+RuntimeBridge.playerName(p)+" falhou! Turno consumido.");}return true;}
        if(action.type()==BattleActionType.ITEM){
            if(action.skillId().equals("pulse_tonic")){if(action.targetId()!=null&&s.find(action.targetId()).filter(c->c.side()==BattleSide.PLAYERS&&c.alive()).isEmpty())return false;var data=PlayerPersonaData.load(p);if(!data.consumePulseTonic()){sync(p,"Sem Pulse Tonic.");return false;}data.save(p);}
            else {var supply=com.gx.velveteclipse.equipment.Supplies.get(action.skillId());var target=action.targetId()==null?s.currentActor():s.find(action.targetId()).orElse(null);if(target==null||(com.gx.velveteclipse.equipment.DamageItems.get(action.skillId())!=null?target.side()!=BattleSide.ENEMIES||!target.alive():target.side()!=BattleSide.PLAYERS||!com.gx.velveteclipse.equipment.Supplies.useful(supply,target))){sync(p,"Este item não tem efeito sobre o aliado selecionado.");return false;}var bag=com.gx.velveteclipse.equipment.EquipmentData.load(p);if(!bag.consume(action.skillId())){sync(p,"Você não possui este item.");return false;}com.gx.velveteclipse.equipment.EquipmentData.save(p,bag);}
        }
        var reserved=s.prepared();if(reserved==null)reserved=s.prepare(id,action);var result=s.perform(id,action);
        if(reserved!=null&&s.prepared()==null)for(var entry:reserved.hits().entrySet()){
            var target=s.find(entry.getKey()).orElse(null);var hit=entry.getValue();if(target==null||target.side()!=BattleSide.ENEMIES||hit.missed()||hit.element()==Element.NONE)continue;
            String key=knowledgeKey(g,target);var known=knowledge(g,p);boolean discovered=known.get(key,hit.element())==null;
            if(g.raid&&g.raidStage==2)known.learn(key,hit);
            BattleKnowledge.learn(p,key,hit);
            if(discovered&&hit.affinity()==Affinity.WEAK)RuntimeBridge.sendMessage(p,"§bFraqueza descoberta: "+target.name()+" • "+hit.element().name());
        }
        recordDefeats(g);
        if(g.raid)com.gx.velveteclipse.raid.RaidService.afterAction(s);
        if(action.type()==BattleActionType.ALL_OUT&&s.state()==BattleState.PLAYERS_WON)g.shuffleVictory=true;
        for(Object member:g.players.values())persist(member,s);
        String message=String.join("  •  ",result.log());g.delay=28;
        if(s.state()!=BattleState.ACTIVE){g.ending=message;if(!busy(g)){finish(g,message);return true;}}
        syncAll(g,message);return true;
    }
    public static boolean switchPersona(Object p,String persona){
        var g=group(p);UUID id=RuntimeBridge.playerUuid(p);
        if(g==null||actionBusy(p)||!g.session.canSwitchPersona(id)){sync(p,"Não é possível trocar de Persona durante 1 More. Aguarde seu turno normal.");return false;}
        var data=PlayerPersonaData.load(p);var instance=data.instance(persona);if(instance==null||!data.equipPersona(persona)){sync(p,"Persona não está na seleção.");return false;}
        g.session.find(id).orElseThrow().setPersona(instance);data.save(p);VisualBattleActors.replacePersona(p,persona);syncAll(g,"PERSONA → "+PersonaRegistry.persona(persona).orElseThrow().displayName());return true;
    }
    public static void handleNetworkAction(Object p,String type,String value){handleNetworkAction(p,type,value,null);}
    public static void handleNetworkAction(Object p,String type,String value,UUID target){
        if(p==null||type==null)return;
        if(type.equals("results_ack")){acknowledge(p,value);return;}
        if(type.equals("join")){try{if(!join(p,UUID.fromString(value)))RuntimeBridge.sendMessage(p,"§eEste combate não está disponível para entrada agora.");}catch(IllegalArgumentException ignored){}return;}
        if(actionBusy(p))return;
        switch(type){
            case "attack"->{var s=current(p);if(s!=null){UUID enemy=target==null?s.participants().stream().filter(c->c.side()==BattleSide.ENEMIES&&c.alive()).map(BattleCombatant::id).findFirst().orElse(null):target;if(enemy!=null)MeleeBattleServer.start(p,BattleAction.attack(enemy));}}
            case "skill"->PersonaDelayedActions.queueSkill(p,value,target);
            case "persona"->switchPersona(p,value);
            case "baton_pass"->{var g=group(p);if(g!=null&&g.session.batonPass(RuntimeBridge.playerUuid(p),target)){syncAll(g,"SHIFT! Turno passado com bônus de dano de 50%.");}else sync(p,"Baton Pass indisponível para este alvo.");}
            case "all_out"->{if(!AllOutServer.start(p))sync(p,"All-Out Attack indisponível.");}
            case "guard"->act(p,BattleAction.guard());case "item"->act(p,new BattleAction(BattleActionType.ITEM,value==null||value.isBlank()?"pulse_tonic":value,target));case "escape"->act(p,BattleAction.escape());
        }
    }
    public static boolean actFirstTarget(Object p,String ignored){if(actionBusy(p))return false;var s=current(p);if(s==null)return false;var enemy=s.participants().stream().filter(c->c.side()==BattleSide.ENEMIES&&c.alive()).findFirst().orElse(null);return enemy!=null&&MeleeBattleServer.start(p,BattleAction.attack(enemy.id()));}
    public static boolean skillFirstTarget(Object p,String skill){return !actionBusy(p)&&PersonaDelayedActions.queueSkill(p,skill);}

    public static void tickServer(){
        PersonaCinematicServer.tick();MeleeBattleServer.tick();AllOutServer.tick();
        for(var g:new ArrayList<>(BATTLES.values())){
            for(Object p:new ArrayList<>(g.players.values()))if(p instanceof Entity e&&(!e.m_6084_()||e.m_9236_()!=g.level))stop(p);
            if(!BATTLES.containsKey(g.session.id()))continue;
            for(var entry:g.players.entrySet())if(!MeleeBattleServer.active(entry.getValue()))RuntimeBridge.freezePlayer(entry.getValue(),g.anchors.get(entry.getKey()));
            if(g.animatingEnemy!=null&&ticks>=g.enemyPoseUntil){g.animatingEnemy.m_146926_(0);g.animatingEnemy=null;}
            boolean acting=busy(g);
            if(g.results.started())continue;
            if(!acting&&g.session.state()!=BattleState.ACTIVE){finish(g,g.ending);continue;}
            if(!acting){VisualBattleActors.prune(g.session);if(g.wasBusy){g.wasBusy=false;syncAll(g,"Escolha uma ação.");}var actor=g.session.currentActor();if(actor!=null&&actor.side()==BattleSide.ENEMIES&&--g.delay<=0)enemyTurn(g);}
        }
        if(++ticks%10==0)syncObservers();
    }
    private static void enemyTurn(Encounter g){
        var s=g.session;var actor=s.currentActor();var targets=s.enemiesOf(actor);if(targets.isEmpty())return;
        var target=targets.get(Math.floorMod(ticks+actor.id().hashCode(),targets.size()));
        var skill=com.gx.velveteclipse.shadow.EnemyTactics.choose(actor,java.util.concurrent.ThreadLocalRandom.current());
        if(g.raid&&g.raidStage==2){boolean ice=com.gx.velveteclipse.raid.RaidService.iceTurn(s.id());skill=ice?PersonaRegistry.skill("raid_ice_storm").orElseThrow():null;
            if(ice&&g.level instanceof net.minecraft.server.level.ServerLevel l)for(Object p:g.players.values())if(p instanceof Entity e)l.m_8767_(net.minecraft.core.particles.ParticleTypes.f_175821_,e.m_20185_(),e.m_20186_()+1,e.m_20189_(),45,.6,1,.6,.08);
        }
        if(g.raid&&!com.gx.velveteclipse.raid.RaidService.allowAction(s.id()))return;
        if(!g.raid){g.animatingEnemy=VisualBattleActors.enemy(s.id(),actor.id());if(g.animatingEnemy!=null){g.animatingEnemy.m_146926_(skill!=null&&skill.usesMagicStat()?-45:45);g.enemyPoseUntil=ticks+22;}}
        var result=s.perform(actor.id(),skill==null?BattleAction.attack(target.id()):BattleAction.skill(skill.id(),target.id()));g.delay=28;
        recordDefeats(g);
        if(g.raid)com.gx.velveteclipse.raid.RaidService.afterAction(s);
        if(g.raid&&g.raidStage==2){g.delay=60;g.wasBusy=true;}
        for(Object p:g.players.values())persist(p,s);
        if(s.state()!=BattleState.ACTIVE)finish(g,String.join(" • ",result.log()));else syncAll(g,String.join(" • ",result.log()));
    }
    private static void recordDefeats(Encounter g){
        for(var enemy:g.session.participants())if(enemy.side()==BattleSide.ENEMIES&&!enemy.alive()&&g.recordedDefeats.add(enemy.id()))
            for(Object p:g.players.values())BestiaryService.defeated(p,knowledgeKey(g,enemy),enemy.persona().level());
    }
    private static String knowledgeKey(Encounter g,BattleCombatant c){return g.raid?"raid_"+g.raidStage:c.persona().definitionId();}
    private static AffinityKnowledge knowledge(Encounter g,Object p){return g.raid&&g.raidStage==2?g.raidKnowledge.computeIfAbsent(RuntimeBridge.playerUuid(p),id->new AffinityKnowledge()):BattleKnowledge.load(p);}
    public static void resetRaidKnowledge(UUID battle){var g=BATTLES.get(battle);if(g!=null)g.raidKnowledge.clear();}
    public static String knownRaidWeakness(Object p){var g=group(p);if(g==null||!g.raid||g.raidStage!=2)return "UNKNOWN";var k=knowledge(g,p);return Arrays.stream(Element.values()).filter(e->k.get("raid_2",e)==Affinity.WEAK).map(Enum::name).findFirst().orElse("UNKNOWN");}
    private static void syncAll(Encounter g,String message){for(Object p:g.players.values())sync(p,message);}
    public static void sync(Object p,String message){
        if(p instanceof net.minecraft.server.level.ServerPlayer sp)com.gx.velveteclipse.equipment.EquipmentService.sync(sp,"sync","");
        if(p==null)return;var data=PlayerPersonaData.load(p);var g=group(p);var actor=g==null?null:g.session.find(RuntimeBridge.playerUuid(p)).orElse(null);
        var persona=actor==null?data.activeInstance():actor.persona();var targets=new ArrayList<BattleRosterS2C.Target>();
        if(g!=null)for(var c:g.session.participants()){var visual=targetActor(p,c.id());boolean ally=c.side()==BattleSide.PLAYERS;
            String affinities=ally?"":knowledge(g,p).forEnemy(knowledgeKey(g,c));
            targets.add(new BattleRosterS2C.Target(c.id(),visual==null?-1:visual.m_19879_(),c.name()+(c.ailment().isEmpty()?"":" ["+c.ailment()+"]"),ally,c.hp(),c.maxHp(),c.persona().definitionId(),affinities,g.session.currentActor()!=null&&g.session.currentActor().id().equals(c.id())));}
        ModNetwork.sendPresentation(p,new BattleRosterS2C(targets));
        if(g==null)ModNetwork.sendPresentation(p,BattleTacticsS2C.empty());
        else {
            var turnActor=g.session.currentActor();UUID turnId=turnActor==null?null:turnActor.id();
            boolean own=RuntimeBridge.playerUuid(p).equals(turnId);
            var downed=g.session.participants().stream().filter(c->c.alive()&&c.downed()).map(c->new BattleTacticsS2C.Down(targetActor(p,c.id())==null?-1:targetActor(p,c.id()).m_19879_(),c.skipsNextTurn())).toList();
            ModNetwork.sendPresentation(p,new BattleTacticsS2C(turnId,own&&g.session.oneMore(),g.session.tacticalEvent(),g.session.tacticalKind(),own&&g.session.canAllOut(turnId),own?g.session.batonTargets().stream().map(BattleCombatant::id).toList():List.of(),downed));
        }
        var enemy=targets.stream().filter(t->!t.ally()&&t.hp()>0).findFirst().orElse(null);
        boolean turn=actor!=null&&g.session.state()==BattleState.ACTIVE&&!g.results.started()&&g.session.currentActor()==actor&&!busy(g)&&g.session.prepared()==null;
        ModNetwork.sendToPlayer(p,new BattleSnapshotS2C(g!=null,turn,persona.definitionId(),PersonaRegistry.persona(persona.definitionId()).orElseThrow().displayName(),actor==null?data.hp():actor.hp(),actor==null?data.maxHp():actor.maxHp(),actor==null?data.sp():actor.sp(),actor==null?data.maxSp():actor.maxSp(),enemy==null?"":enemy.name(),enemy==null?0:enemy.hp(),enemy==null?0:enemy.maxHp(),data.pulseTonics(),new ArrayList<>(persona.learnedSkills()),new ArrayList<>(data.rosterIds()),message==null?"":message));
    }
    private static void persist(Object p,BattleSession s){s.find(RuntimeBridge.playerUuid(p)).ifPresent(c->{var d=PlayerPersonaData.load(p);d.setResources(c.hp(),c.sp());d.updatePersona(c.persona());d.equipPersona(c.persona().definitionId());d.save(p);});}
    private static void finish(Encounter g,String message){
        if(!BATTLES.containsKey(g.session.id())||!g.results.begin(g.players.keySet()))return;
        boolean won=g.session.state()==BattleState.PLAYERS_WON;
        if(g.raid)com.gx.velveteclipse.raid.RaidService.finished(g.session.id(),won);
        com.gx.velveteclipse.darkhour.TartarusDungeonService.encounterFinished(g.session.id(),won);
        if(won)ShadowWorldService.release(g.session.id(),true);
        VisualBattleActors.clearEnemies(g.session.id());
        // Keep encounter and membership until each player's Enter acknowledgement.
        for(Object p:new ArrayList<>(g.players.values())){
            persist(p,g.session);var data=PlayerPersonaData.load(p);int before=data.playerLevel(),xp=won?com.gx.velveteclipse.shadow.ShadowEncounterRules.xp(g.dungeonFloor,g.enemyCount,g.rare)*(g.boss?3:1):0;String items="NENHUM ITEM";xp=com.gx.velveteclipse.equipment.EquipmentData.load(p).bonuses().xp(xp);
            if(won){var persona=data.activeInstance();data.addPlayerExperience(xp);persona.addExperience(PersonaRegistry.persona(persona.definitionId()).orElseThrow(),xp);data.updatePersona(persona);if(g.raid){long yen=g.raidStage==2?2000:500;data.addYen(yen);items="YEN • ¥ "+yen;}else{long yen=com.gx.velveteclipse.shadow.ShadowEncounterRules.yen(g.dungeonFloor,g.enemyCount,g.rare)*(g.boss?3:1);data.addYen(yen);items="YEN • ¥ "+yen;}}
            data.save(p);if(won&&g.raid&&g.raidStage==2&&p instanceof ServerPlayer player&&com.gx.velveteclipse.progression.ProgressionRewards.boss(player))items+=" • Permissão 25F • Contrato de Igor";VisualBattleActors.clear(p);sync(p,"Resultados • pressione Enter para continuar.");
            if((g.shuffleVictory||g.rare)&&won)com.gx.velveteclipse.shuffle.ShuffleService.credit(p,Math.max(1,g.dungeonFloor),g.rare);
            var persona=data.activeInstance();ModNetwork.sendPresentation(p,new BattleResultsS2C(g.session.id().toString(),won,xp,before,data.playerLevel(),data.playerExperience(),data.playerNextExperience(),PersonaRegistry.persona(persona.definitionId()).orElseThrow().displayName(),persona.level(),items,(g.shuffleVictory||g.rare)&&won));
        }
    }
    public static void acknowledge(Object p,String battle){var g=group(p);if(g==null||!g.session.id().toString().equals(battle)||!g.results.release(RuntimeBridge.playerUuid(p)))return;detach(g,p);sync(p,"Resultados confirmados.");if(g.players.isEmpty()){ShadowWorldService.release(g.session.id(),g.session.state()==BattleState.PLAYERS_WON);BATTLES.remove(g.session.id());}}
    private static void syncObservers(){
        var server=net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();if(server==null)return;
        for(ServerPlayer p:server.m_6846_().m_11314_()){
            var scenes=new ArrayList<BattleSceneS2C.Scene>();
            for(var g:BATTLES.values()){
                if(g.players.containsKey(p.m_20148_())||p.m_9236_()!=g.level||distance(RuntimeBridge.playerPosition(p),g.center)>48*48)continue;
                var ids=g.session.participants().stream().filter(c->c.side()==BattleSide.ENEMIES).map(c->VisualBattleActors.enemy(g.session.id(),c.id())).filter(Objects::nonNull).map(Entity::m_19879_).toList();
                scenes.add(new BattleSceneS2C.Scene(g.session.id(),RuntimeBridge.playerName(g.players.values().iterator().next()),g.center[0],g.center[1],g.center[2],1.7f+(g.enemyCount-1)*.9f,current(p)==null&&canJoin(p,g),ids));
            }
            scenes.sort(Comparator.comparingDouble(s->distance(RuntimeBridge.playerPosition(p),new double[]{s.x(),s.y(),s.z()})));ModNetwork.sendPresentation(p,new BattleSceneS2C(scenes.stream().limit(8).toList()));
        }
    }
}
