package com.gx.velveteclipse.forge;

import com.gx.velveteclipse.core.battle.*;
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
        final Object level;
        final double[] origin,center;
        final float yaw;
        final int enemyCount;
        int delay=28;
        boolean wasBusy;
        String ending="";
        Encounter(Object p,BattleSession s,int count){session=s;enemyCount=count;origin=RuntimeBridge.playerPosition(p);yaw=RuntimeBridge.playerYaw(p);center=RuntimeBridge.localOffset(origin,yaw,0,.05,5.25);level=p instanceof Entity e?e.m_9236_():null;}
    }
    private static Encounter group(Object p){return p==null?null:MEMBERS.get(RuntimeBridge.playerUuid(p));}
    public static BattleSession current(Object p){var g=group(p);return g==null?null:g.session;}
    public static boolean hasActiveBattle(Object p){var s=current(p);return s!=null&&s.state()==BattleState.ACTIVE;}
    public static List<Object> members(Object p){var g=group(p);return g==null?List.of():List.copyOf(g.players.values());}
    private static boolean busy(Encounter g){return g.players.values().stream().anyMatch(p->MeleeBattleServer.active(p)||PersonaCinematicServer.suppressLegacySettle(p)||AllOutServer.active(p));}
    public static boolean actionBusy(Object p){var g=group(p);return g!=null&&(g.session.prepared()!=null||busy(g));}
    private static BattleCombatant combatant(Object p){var d=PlayerPersonaData.load(p);return new BattleCombatant(RuntimeBridge.playerUuid(p),RuntimeBridge.playerName(p),BattleSide.PLAYERS,d.activeInstance(),d.maxHp(),d.hp(),d.maxSp(),d.sp());}
    private static double distance(double[] a,double[] b){double x=a[0]-b[0],y=a[1]-b[1],z=a[2]-b[2];return x*x+y*y+z*z;}

    public static BattleSession startDemo(Object p){return startDemo(p,1);}
    public static BattleSession startDemo(Object p,int count){
        BattleFormation.validateCount(count);
        if(com.gx.velveteclipse.exploration.ExplorationService.blocksBattleStart(p)||current(p)!=null)return current(p);
        var participants=new ArrayList<BattleCombatant>();participants.add(combatant(p));
        for(int i=0;i<count;i++)participants.add(new BattleCombatant(UUID.randomUUID(),count==1?"Umbra Slime":"Umbra Slime "+(i+1),BattleSide.ENEMIES,new PersonaInstance(PersonaRegistry.persona("umbra_slime").orElseThrow()),85,85,30,30));
        var s=new BattleSession(participants);var g=new Encounter(p,s,count);BATTLES.put(s.id(),g);attach(g,p,g.origin);
        VisualBattleActors.spawnBattle(p,PlayerPersonaData.load(p).activePersona(),"umbra_slime");
        syncAll(g,count+" Shadow"+(count==1?"":"s")+" bloqueiam o caminho.");return s;
    }
    private static void attach(Encounter g,Object p,double[] anchor){
        UUID id=RuntimeBridge.playerUuid(p);g.players.put(id,p);g.anchors.put(id,anchor);MEMBERS.put(id,g);
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
    private static boolean canJoin(ServerPlayer p,Encounter g){return g.session.state()==BattleState.ACTIVE&&g.players.values().stream().noneMatch(AllOutServer::active)&&p.m_6084_()&&p.m_9236_()==g.level&&distance(RuntimeBridge.playerPosition(p),g.center)<=81&&!com.gx.velveteclipse.exploration.ExplorationService.blocksBattleStart(p)&&PlayerPersonaData.load(p).hp()>0;}

    /** Leaving/disconnecting removes only that participant; an interrupted reservation costs nothing. */
    public static void stop(Object p){
        var g=group(p);if(g==null){sync(p,"Batalha encerrada.");return;}
        AllOutServer.cancel(p);persist(p,g.session);UUID id=RuntimeBridge.playerUuid(p);var reserved=g.session.prepared();
        if(reserved!=null&&(reserved.actor().equals(id)||id.equals(reserved.action().targetId()))){
            Object acting=g.players.get(reserved.actor());if(acting!=null){MeleeBattleServer.clear(acting);PersonaCinematicServer.clear(acting);ModNetwork.sendPresentation(acting,new BattleCueS2C("cancel","",false,0,0,0,0,0,0));}g.session.cancelPrepared();
        }
        detach(g,p);g.session.leave(id);sync(p,"Você saiu da batalha.");
        if(g.players.isEmpty()){VisualBattleActors.clearEnemies(g.session.id());BATTLES.remove(g.session.id());}
        else if(g.session.state()!=BattleState.ACTIVE)finish(g,"Batalha encerrada.");else syncAll(g,RuntimeBridge.playerName(p)+" saiu da batalha.");
    }
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
        if(busy(g)&&(s.prepared()==null||!s.prepared().actor().equals(id)||!s.prepared().action().equals(action)))return false;
        if(s.currentActor()==null||!s.currentActor().id().equals(id)){sync(p,"Aguarde seu turno.");return false;}
        if(s.prepared()!=null&&!s.prepared().action().equals(action))return false;
        if(action.type()==BattleActionType.ESCAPE){stop(p);return true;}
        if(action.type()==BattleActionType.ITEM){var data=PlayerPersonaData.load(p);if(!data.consumePulseTonic()){sync(p,"Sem Pulse Tonic.");return false;}data.save(p);}
        var reserved=s.prepared();var result=s.perform(id,action);
        if(reserved!=null&&s.prepared()==null)reserved.hits().forEach((target,hit)->s.find(target).ifPresent(t->BattleKnowledge.learn(p,t.persona().definitionId(),hit)));
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
        if(type.equals("join")){try{if(!join(p,UUID.fromString(value)))RuntimeBridge.sendMessage(p,"§eEste combate não está disponível para entrada agora.");}catch(IllegalArgumentException ignored){}return;}
        if(actionBusy(p))return;
        switch(type){
            case "start_demo"->{if(current(p)==null)startDemo(p);}
            case "attack"->{var s=current(p);if(s!=null){UUID enemy=target==null?s.participants().stream().filter(c->c.side()==BattleSide.ENEMIES&&c.alive()).map(BattleCombatant::id).findFirst().orElse(null):target;if(enemy!=null)MeleeBattleServer.start(p,BattleAction.attack(enemy));}}
            case "skill"->PersonaDelayedActions.queueSkill(p,value,target);
            case "persona"->switchPersona(p,value);
            case "baton_pass"->{var g=group(p);if(g!=null&&g.session.batonPass(RuntimeBridge.playerUuid(p),target)){syncAll(g,"SHIFT! Turno passado com bônus de dano de 50%.");}else sync(p,"Baton Pass indisponível para este alvo.");}
            case "all_out"->{if(!AllOutServer.start(p))sync(p,"All-Out Attack indisponível.");}
            case "guard"->act(p,BattleAction.guard());case "item"->act(p,BattleAction.item("pulse_tonic"));case "escape"->act(p,BattleAction.escape());
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
            boolean acting=busy(g);
            if(!acting&&g.session.state()!=BattleState.ACTIVE){finish(g,g.ending);continue;}
            if(!acting){VisualBattleActors.prune(g.session);if(g.wasBusy){g.wasBusy=false;syncAll(g,"Escolha uma ação.");}var actor=g.session.currentActor();if(actor!=null&&actor.side()==BattleSide.ENEMIES&&--g.delay<=0)enemyTurn(g);}
        }
        if(++ticks%10==0)syncObservers();
    }
    private static void enemyTurn(Encounter g){
        var s=g.session;var actor=s.currentActor();var targets=s.enemiesOf(actor);if(targets.isEmpty())return;
        var target=targets.get(Math.floorMod(ticks+actor.id().hashCode(),targets.size()));
        var skill=actor.persona().learnedSkills().stream().map(id->PersonaRegistry.skill(id).orElse(null)).filter(Objects::nonNull).filter(d->d.isDamageSkill()&&d.spCost()<=actor.sp()).findFirst().orElse(null);
        var result=s.perform(actor.id(),skill==null?BattleAction.attack(target.id()):BattleAction.skill(skill.id(),target.id()));g.delay=28;
        for(Object p:g.players.values())persist(p,s);
        if(s.state()!=BattleState.ACTIVE)finish(g,String.join(" • ",result.log()));else syncAll(g,String.join(" • ",result.log()));
    }
    private static void syncAll(Encounter g,String message){for(Object p:g.players.values())sync(p,message);}
    public static void sync(Object p,String message){
        if(p==null)return;var data=PlayerPersonaData.load(p);var g=group(p);var actor=g==null?null:g.session.find(RuntimeBridge.playerUuid(p)).orElse(null);
        var persona=actor==null?data.activeInstance():actor.persona();var targets=new ArrayList<BattleRosterS2C.Target>();
        if(g!=null)for(var c:g.session.participants()){var visual=targetActor(p,c.id());boolean ally=c.side()==BattleSide.PLAYERS;targets.add(new BattleRosterS2C.Target(c.id(),visual==null?-1:visual.m_19879_(),c.name(),ally,c.hp(),c.maxHp(),c.persona().definitionId(),ally?"":BattleKnowledge.load(p).forEnemy(c.persona().definitionId()),g.session.currentActor()!=null&&g.session.currentActor().id().equals(c.id())));}
        ModNetwork.sendPresentation(p,new BattleRosterS2C(targets));
        if(g==null)ModNetwork.sendPresentation(p,BattleTacticsS2C.empty());
        else {
            var turnActor=g.session.currentActor();UUID turnId=turnActor==null?null:turnActor.id();
            boolean own=RuntimeBridge.playerUuid(p).equals(turnId);
            var downed=g.session.participants().stream().filter(c->c.alive()&&c.downed()).map(c->new BattleTacticsS2C.Down(targetActor(p,c.id())==null?-1:targetActor(p,c.id()).m_19879_(),c.skipsNextTurn())).toList();
            ModNetwork.sendPresentation(p,new BattleTacticsS2C(turnId,own&&g.session.oneMore(),g.session.tacticalEvent(),g.session.tacticalKind(),own&&g.session.canAllOut(turnId),own?g.session.batonTargets().stream().map(BattleCombatant::id).toList():List.of(),downed));
        }
        var enemy=targets.stream().filter(t->!t.ally()&&t.hp()>0).findFirst().orElse(null);
        boolean turn=actor!=null&&g.session.currentActor()==actor&&!busy(g)&&g.session.prepared()==null;
        ModNetwork.sendToPlayer(p,new BattleSnapshotS2C(g!=null,turn,persona.definitionId(),PersonaRegistry.persona(persona.definitionId()).orElseThrow().displayName(),actor==null?data.hp():actor.hp(),actor==null?data.maxHp():actor.maxHp(),actor==null?data.sp():actor.sp(),actor==null?data.maxSp():actor.maxSp(),enemy==null?"":enemy.name(),enemy==null?0:enemy.hp(),enemy==null?0:enemy.maxHp(),data.pulseTonics(),new ArrayList<>(persona.learnedSkills()),new ArrayList<>(data.rosterIds()),message==null?"":message));
    }
    private static void persist(Object p,BattleSession s){s.find(RuntimeBridge.playerUuid(p)).ifPresent(c->{var d=PlayerPersonaData.load(p);d.setResources(c.hp(),c.sp());d.updatePersona(c.persona());d.equipPersona(c.persona().definitionId());d.save(p);});}
    private static void finish(Encounter g,String message){
        if(!BATTLES.containsKey(g.session.id()))return;
        for(Object p:new ArrayList<>(g.players.values())){persist(p,g.session);var data=PlayerPersonaData.load(p);String result="BATALHA ENCERRADA";
            if(g.session.state()==BattleState.PLAYERS_WON){var persona=data.activeInstance();int xp=80*g.enemyCount;persona.addExperience(PersonaRegistry.persona(persona.definitionId()).orElseThrow(),xp);data.updatePersona(persona);data.givePersona("umbra_slime");result="VICTORY • +"+xp+" EXP";}else if(g.session.state()==BattleState.ENEMIES_WON)result="DEFEAT";
            data.save(p);detach(g,p);sync(p,(message.isBlank()?"":message+" • ")+result);
        }
        VisualBattleActors.clearEnemies(g.session.id());BATTLES.remove(g.session.id());
    }
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
