package com.gx.velveteclipse.forge;
import com.gx.velveteclipse.core.battle.*;
import com.gx.velveteclipse.network.*;
import com.gx.velveteclipse.util.RuntimeBridge;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import java.util.*;
public final class MeleeBattleServer {
 private static final Map<UUID,Sequence> sequences=new HashMap<>();
 private static final class Sequence {final ServerPlayer p;final BattleAction action;final Vec3 start,end;final float yaw;int tick;
  Sequence(ServerPlayer p,BattleAction a,Vec3 end){this.p=p;action=a;start=p.m_20182_();this.end=end;yaw=p.m_146908_();}}
 public static boolean active(Object p){return p!=null&&sequences.containsKey(RuntimeBridge.playerUuid(p));}
 public static boolean start(Object object,BattleAction action){
  if(!(object instanceof ServerPlayer p)||active(p))return false;
  var session=BattleService.current(p);if(session==null||session.prepared()!=null)return false;
  if(!(VisualBattleActors.enemyActor(p) instanceof Entity enemy)){BattleService.sync(p,"O alvo ainda não está disponível para o ataque.");return false;}
  Vec3 start=p.m_20182_(),delta=enemy.m_20182_().m_82546_(start);double distance=Math.sqrt(delta.f_82479_*delta.f_82479_+delta.f_82481_*delta.f_82481_);
  double travel=Math.max(0,distance-1.35);
  Vec3 direction=distance<.001?Vec3.f_82478_:new Vec3(delta.f_82479_/distance,0,delta.f_82481_/distance);
  boolean supported=!p.m_9236_().m_45756_(p,p.m_20191_().m_82386_(0,-.65,0));
  // Terrain constrains presentation, never the validity of a turn-based attack.
  double reachable=MeleeApproach.reachable(travel,d->{
   var box=p.m_20191_().m_82383_(direction.m_82490_(d));
   return p.m_9236_().m_45756_(p,box)&&(!supported||!p.m_9236_().m_45756_(p,box.m_82386_(0,-.65,0)));
  });
  Vec3 offset=direction.m_82490_(reachable);
  var reserved=session.prepare(p.m_20148_(),action);if(reserved==null){BattleService.sync(p,"Ataque indisponível.");return false;}
  var s=new Sequence(p,action,start.m_82549_(offset));sequences.put(p.m_20148_(),s);
  ModNetwork.sendPresentation(p,new BattleCueS2C("attack","",false,start.f_82479_,start.f_82480_,start.f_82481_,s.end.f_82479_,s.end.f_82480_,s.end.f_82481_));return true;
 }
 public static void tick(){for(var s:new ArrayList<>(sequences.values())){
  var session=BattleService.current(s.p);if(session==null||!s.p.m_6084_()){clear(s.p);continue;}
  ++s.tick;Vec3 desired=s.start.m_165921_(s.end,MeleeTimeline.progress(s.tick));
  var offset=desired.m_82546_(s.p.m_20182_());
  // A block placed during the animation may stop movement, but cannot discard its reserved hit.
  if(s.p.m_9236_().m_45756_(s.p,s.p.m_20191_().m_82383_(offset)))
   RuntimeBridge.freezePlayer(s.p,new double[]{desired.f_82479_,desired.f_82480_,desired.f_82481_});
  else s.p.m_20256_(Vec3.f_82478_);
  RuntimeBridge.setEntityRotation(s.p,s.yaw,0);
  if(s.tick==MeleeTimeline.IMPACT){CinematicEffects.melee(s.p);BattleService.act(s.p,s.action);}
  if(s.tick>=MeleeTimeline.END)clear(s.p);
 }}
 public static void clear(Object p){var s=sequences.remove(RuntimeBridge.playerUuid(p));if(s!=null){RuntimeBridge.hardFreezePlayer(s.p,new double[]{s.start.f_82479_,s.start.f_82480_,s.start.f_82481_},s.yaw,0);var battle=BattleService.current(p);if(battle!=null)battle.cancelPrepared();}}
}
