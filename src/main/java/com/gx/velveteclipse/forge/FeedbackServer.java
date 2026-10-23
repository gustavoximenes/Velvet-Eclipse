package com.gx.velveteclipse.forge;
import com.gx.velveteclipse.core.battle.*;
import com.gx.velveteclipse.core.persona.*;
import com.gx.velveteclipse.network.*;
import com.gx.velveteclipse.presentation.FeedbackLibrary;
import com.gx.velveteclipse.util.RuntimeBridge;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
/** Feedback is emitted once, after a successful state transition, to the encounter members. */
public final class FeedbackServer {
 private static final AtomicLong ids=new AtomicLong();
 public static void membership(Object player,UUID session,boolean start){ModNetwork.sendPresentation(player,new BattleFeedbackS2C(session,ids.incrementAndGet(),start?"start":"stop","",List.of()));}
 private static BattleFeedbackS2C packet(UUID session,String kind,String key,List<BattleFeedbackS2C.Target> targets){return new BattleFeedbackS2C(session,ids.incrementAndGet(),kind,key,targets);}
 private static void send(List<Object> members,BattleFeedbackS2C packet){for(var p:members)ModNetwork.sendPresentation(p,packet);}
 public static void cue(Object p,String route){var s=BattleService.current(p);if(s==null)return;var pts=p instanceof Entity e?List.of(new BattleFeedbackS2C.Target(BattleLightningS2C.Strike.at(e),"normal")):List.<BattleFeedbackS2C.Target>of();send(BattleService.members(p),packet(s.id(),"audio",route,pts));}
 public static void swing(Object p){var s=BattleService.current(p);if(s==null)return;String family=com.gx.velveteclipse.equipment.EquipmentData.load(p).bonuses().family().name();cue(p,"swing_"+FeedbackLibrary.weapon(family));}
 public static void weaponPhase(Object p,boolean start){String family=FeedbackLibrary.weapon(com.gx.velveteclipse.equipment.EquipmentData.load(p).bonuses().family().name());if(family.equals("slash"))cue(p,start?"physical_draw":"physical_sheath");else if(start&&(family.equals("bow")||family.equals("gun")))cue(p,"prepare_"+family);}
 public static void cancel(Object p){var s=BattleService.current(p);if(s!=null)send(BattleService.members(p),packet(s.id(),"cancel","",List.of()));}
 public static Map<UUID,BattleLightningS2C.Strike> targets(BattleSession s,UUID actorId,BattleAction action,Function<UUID,Entity> entities){
  var actor=s.find(actorId).orElse(null);if(actor==null)return Map.of();var skill=action.skillId()==null?null:PersonaRegistry.skill(action.skillId()).orElse(null);
  var item=com.gx.velveteclipse.equipment.DamageItems.get(action.skillId());if(action.type()==BattleActionType.ITEM&&item!=null)skill=item.skill();
  List<BattleCombatant> list;
  var info=skill==null?null:ReloadCatalogue.info(skill.id());
  if(info!=null&&info.universal())list=s.participants().stream().filter(BattleCombatant::alive).toList();
  else if(action.type()==BattleActionType.ALL_OUT||skill!=null&&skill.target()==SkillTarget.ALL_ENEMIES)list=s.enemiesOf(actor);
  else if(skill!=null&&skill.target()==SkillTarget.ALL_ALLIES)list=s.alliesOf(actor);
  else if(action.type()==BattleActionType.GUARD||skill!=null&&skill.target()==SkillTarget.SELF||action.targetId()==null)list=List.of(actor);
  else list=s.find(action.targetId()).map(List::of).orElse(List.of());
  var result=new LinkedHashMap<UUID,BattleLightningS2C.Strike>();for(var t:list){var e=entities.apply(t.id());if(e!=null)result.put(t.id(),BattleLightningS2C.Strike.at(e));}return result;
 }
 public static void result(BattleSession s,List<Object> members,UUID actorId,String persona,String family,BattleAction action,BattleResolution result,Map<UUID,BattleLightningS2C.Strike> targets){
  if(!result.accepted())return;var points=new ArrayList<BattleFeedbackS2C.Target>();
  for(var e:targets.entrySet()){var hit=result.hits().get(e.getKey());String state=hit==null?"normal":hit.missed()?"miss":hit.reflected()?"reflect":hit.drained()?"absorb":hit.affinity()==Affinity.NULL?"null":hit.critical()?"critical":hit.affinity()==Affinity.WEAK?"weak":"normal";points.add(new BattleFeedbackS2C.Target(e.getValue(),state));}
  if(action.type()==BattleActionType.SKILL)send(members,packet(s.id(),"skill",action.skillId(),points));
  else if(action.type()==BattleActionType.ATTACK)send(members,packet(s.id(),"melee",FeedbackLibrary.weapon(family),points));
  else if(action.type()==BattleActionType.ITEM)send(members,packet(s.id(),"item",action.skillId(),points));
  else if(action.type()==BattleActionType.ALL_OUT)send(members,packet(s.id(),"melee","strike",points));
  if(result.weaknessHit()&&s.find(actorId).map(a->a.side()==BattleSide.PLAYERS).orElse(false))send(members,packet(s.id(),"audio","summon_persona_cutin",points.stream().limit(1).toList()));
  if(result.oneMore()){
   var actor=s.find(actorId).orElse(null);boolean player=actor!=null&&actor.side()==BattleSide.PLAYERS;
   send(members,packet(s.id(),"audio",player?"battle_one_more":"battle_one_more_enemy",points.stream().limit(1).toList()));
   if(player&&action.type()==BattleActionType.SKILL&&result.weaknessOneMore())send(members,packet(s.id(),"voice",persona,points.stream().limit(1).toList()));
  }
  if(result.hits().values().stream().anyMatch(h->h.newlyDowned()&&!h.missed()))send(members,packet(s.id(),"audio","battle_down",points.stream().limit(1).toList()));
 }
 /** One sound per successful furniture operation, never once per block or automatic repair. */
 public static void furniture(ServerPlayer source,String route,int index,double x,double y,double z){var clips=FeedbackLibrary.audio(route);if(index<0||index>=clips.size())return;var p=packet(BattleFeedbackS2C.WORLD,"world",clips.get(index),List.of(new BattleFeedbackS2C.Target(new BattleLightningS2C.Strike(x,y,z,0),"normal")));for(var player:source.m_284548_().m_6907_())if(player.m_20275_(x,y,z)<=256)ModNetwork.sendPresentation(player,p);}
 public static void worldRoute(ServerPlayer target,String route){for(int i=0;i<FeedbackLibrary.audio(route).size();i++)furniture(target,route,i,target.m_20185_(),target.m_20186_(),target.m_20189_());}
 private FeedbackServer(){}
}
