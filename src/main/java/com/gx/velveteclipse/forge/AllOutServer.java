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
  final Object leader;final UUID battle;final List<Object> viewers;final boolean victory;int tick;
  Sequence(Object leader,BattleSession session,boolean victory){this.leader=leader;this.battle=session.id();this.viewers=BattleService.members(leader);this.victory=victory;}
 }
 public static boolean active(Object p){var s=BattleService.current(p);return s!=null&&sequences.containsKey(s.id());}
 public static boolean start(Object p){
  var s=BattleService.current(p);if(!(p instanceof Entity leader)||s==null||BattleService.actionBusy(p)||!s.canAllOut(RuntimeBridge.playerUuid(p)))return false;
  var fighters=s.allOutFighters();var reserved=s.prepare(RuntimeBridge.playerUuid(p),BattleAction.allOut());if(reserved==null)return false;
  boolean victory=reserved.hits().entrySet().stream().allMatch(e->s.find(e.getKey()).map(t->(t.guarding()?Math.max(1,e.getValue().damage()/2):e.getValue().damage())>=t.hp()).orElse(false));
  var seq=new Sequence(p,s,victory);sequences.put(s.id(),seq);BattleService.beginAction(p,BattleAction.allOut());
  var ids=fighters.stream().map(c->BattleService.targetActor(p,c.id())).filter(Objects::nonNull).map(Entity::m_19879_).toList();
  var cue=new AllOutCueS2C(true,leader.m_19879_(),ids,victory);for(Object member:seq.viewers)ModNetwork.sendPresentation(member,cue);return true;
 }
 public static void tick(){for(var seq:new ArrayList<>(sequences.values())){
  var s=BattleService.current(seq.leader);if(s==null||!s.id().equals(seq.battle)){end(seq);continue;}
  if(++seq.tick==AllOutTimeline.IMPACT)BattleService.act(seq.leader,BattleAction.allOut());
  if(seq.tick>=AllOutTimeline.duration(seq.victory))end(seq);
 }}
 public static void cancel(Object player){var s=BattleService.current(player);if(s!=null){var seq=sequences.get(s.id());if(seq!=null){s.cancelPrepared();end(seq);}}}
 private static void end(Sequence seq){sequences.remove(seq.battle);for(Object member:seq.viewers)ModNetwork.sendPresentation(member,new AllOutCueS2C(false,-1,List.of(),false));}
}
