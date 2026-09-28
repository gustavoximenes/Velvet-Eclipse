package com.gx.velveteclipse.forge;
import com.gx.velveteclipse.core.battle.*;
import com.gx.velveteclipse.network.*;
import com.gx.velveteclipse.util.RuntimeBridge;
import net.minecraft.world.entity.Entity;
import java.util.*;

/** Presentation runs on every participant; damage is reserved and applied once by the server. */
public final class AllOutServer {
 private static final Map<UUID,Sequence> sequences=new HashMap<>();
 private static final class Sequence {
  final Object leader;final UUID battle;final List<Object> viewers;boolean victory;int tick;AllOutCueS2C cue;
  Sequence(Object leader,BattleSession session){this.leader=leader;this.battle=session.id();this.viewers=BattleService.members(leader);}
 }
 public static boolean active(Object p){var s=BattleService.current(p);return s!=null&&sequences.containsKey(s.id());}
 public static boolean start(Object p){
  var s=BattleService.current(p);if(!(p instanceof Entity leader)||s==null||BattleService.actionBusy(p)||!s.canAllOut(RuntimeBridge.playerUuid(p)))return false;
  var fighters=s.allOutFighters();var reserved=s.prepare(RuntimeBridge.playerUuid(p),BattleAction.allOut());if(reserved==null)return false;
  var seq=new Sequence(p,s);sequences.put(s.id(),seq);BattleService.beginAction(p,BattleAction.allOut());
  var ids=fighters.stream().map(c->BattleService.targetActor(p,c.id())).filter(Objects::nonNull).map(Entity::m_19879_).toList();
  var weapons=new HashMap<Integer,String>();for(Object member:seq.viewers)if(member instanceof Entity e)weapons.put(e.m_19879_(),com.gx.velveteclipse.equipment.EquipmentData.load(member).bonuses().family().name());
  seq.cue=new AllOutCueS2C(true,leader.m_19879_(),ids,false,AllOutPreferences.get(p).id(),weapons);for(Object member:seq.viewers)ModNetwork.sendPresentation(member,seq.cue);return true;
 }
 public static void tick(){for(var seq:new ArrayList<>(sequences.values())){
  var s=BattleService.current(seq.leader);if(s==null||!s.id().equals(seq.battle)){end(seq);continue;}
  if(++seq.tick==AllOutTimeline.IMPACT){
   boolean applied=BattleService.act(seq.leader,BattleAction.allOut());
   seq.victory=applied&&s.state()==BattleState.PLAYERS_WON;
   var p=seq.cue;var result=new AllOutCueS2C(true,p.leader(),p.fighters(),seq.victory,p.style(),p.weapons(),AllOutTimeline.IMPACT);
   for(Object member:seq.viewers)ModNetwork.sendPresentation(member,result);
  }
  if(seq.tick>=AllOutTimeline.duration(seq.victory))end(seq);
 }}
 public static void cancel(Object player){var s=BattleService.current(player);if(s!=null){var seq=sequences.get(s.id());if(seq!=null){s.cancelPrepared();end(seq);}}}
 private static void end(Sequence seq){sequences.remove(seq.battle);for(Object member:seq.viewers)ModNetwork.sendPresentation(member,new AllOutCueS2C(false,-1,List.of(),false));}
}
