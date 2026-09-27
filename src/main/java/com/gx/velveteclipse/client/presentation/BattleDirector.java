package com.gx.velveteclipse.client.presentation;

import com.gx.velveteclipse.client.*;
import com.gx.velveteclipse.client.exploration.ExplorationCamera;
import com.gx.velveteclipse.client.vfx.PhotonBattleEffects;
import com.gx.velveteclipse.network.BattleDirectionS2C;
import com.gx.velveteclipse.core.persona.*;
import com.gx.velveteclipse.equipment.EquipmentCatalog.Family;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.ViewportEvent;
import java.util.UUID;

/** One shared action clock, with collision-safe cameras for every participant. */
public final class BattleDirector {
 private static BattleDirectionS2C cue;private static long started;private static Object level;
 private static int lastTick=-1;private static float fov=65;
 public static void accept(BattleDirectionS2C p){
  var world=Minecraft.m_91087_().f_91073_;if(world==null)return;
  if(p.kind().equals("cancel")){if(cue!=null&&cue.actor().equals(p.actor()))reset();return;}
  cue=p;level=world;started=world.m_46467_();lastTick=-1;
 }
 public static void reset(){cue=null;level=null;lastTick=-1;fov=65;}
 public static boolean active(){return cue!=null&&age(0)<cue.duration();}
 public static double age(float partial){var w=Minecraft.m_91087_().f_91073_;return cue==null||w==null||w!=level?1000:w.m_46467_()-started+partial;}
 public static String skill(){return cue==null?"":cue.skill();}
 public static boolean summoning(UUID id){return active()&&cue.kind().equals("skill")&&cue.actor().equals(id);}
 public static boolean attacking(UUID id){return active()&&cue.kind().equals("attack")&&cue.actor().equals(id);}
 public static double hitAge(float partial){return age(partial)-(cue!=null&&cue.kind().equals("skill")?86:cue!=null&&cue.kind().equals("enemy")?10:19);}
 public static boolean hitPlayer(UUID id){var w=Minecraft.m_91087_().f_91073_;if(!active()||w==null||cue.targetEntity()<0)return false;var target=w.m_6815_(cue.targetEntity());return target!=null&&target.m_20148_().equals(id)&&hitAge(0)>=0&&hitAge(0)<12;}
 public static Family weapon(){try{return cue==null?Family.SWORD:Family.valueOf(cue.weapon());}catch(IllegalArgumentException e){return Family.SWORD;}}
 public static float personaAlpha(String owner,float partial){
  if(!active()||!cue.kind().equals("skill")||!cue.actor().toString().equals(owner))return 1;
  double t=age(partial),a=Math.max(0,Math.min(1,(t-46)/8)),b=Math.max(0,Math.min(1,(108-t)/7));return (float)(a*a*(3-2*a)*b*b*(3-2*b));
 }
 public static float fov(){return fov;}
 public static void tick(){
  var mc=Minecraft.m_91087_();if(mc.f_91073_!=level||!ClientBattleState.snapshot().active()){reset();return;}if(!active())return;
  int t=(int)age(0);if(lastTick==t)return;
  if(cue.kind().equals("skill")&&lastTick<38&&t>=38){var a=actor();if(a!=null)PhotonBattleEffects.trigger(a.m_20185_(),a.m_20186_()+1.55,a.m_20189_());}
  if(cue.kind().equals("skill")&&lastTick<46&&t>=46){var a=actor();if(a!=null){double yaw=Math.toRadians(a.m_146908_());PhotonBattleEffects.summon(a.m_20185_()+Math.sin(yaw)*1.4,a.m_20186_(),a.m_20189_()-Math.cos(yaw)*1.4);}}
  if(cue.kind().equals("attack")&&lastTick<19&&t>=19)PhotonBattleEffects.impact(weapon()==Family.BOW||weapon()==Family.GUN||weapon()==Family.SPEAR?Element.PIERCE:weapon()==Family.FISTS?Element.PHYSICAL:Element.SLASH,1,cue.tx(),cue.ty(),cue.tz(),cue.actor().getLeastSignificantBits());
  if(cue.kind().equals("attack")&&weapon()==Family.BOW&&lastTick<12&&t>=12){var a=actor();if(a!=null)com.gx.velveteclipse.client.exploration.ArrowFlight.accept(new com.gx.velveteclipse.exploration.ExplorationNetwork.Shot(a.m_20185_(),a.m_20186_()+1.3,a.m_20189_(),cue.tx(),cue.ty()+.8,cue.tz(),7));}
  if(cue.kind().equals("enemy")&&cue.skill().isEmpty()&&lastTick<10&&t>=10)PhotonBattleEffects.impact(Element.PHYSICAL,1,cue.tx(),cue.ty(),cue.tz(),cue.actor().getLeastSignificantBits());
  lastTick=t;
 }
 private static Entity actor(){var w=Minecraft.m_91087_().f_91073_;return cue==null||w==null?null:w.m_6815_(cue.actorEntity());}
 public static boolean camera(ViewportEvent.ComputeCameraAngles event){
  var mc=Minecraft.m_91087_();if(!ClientBattleState.snapshot().active()||mc.f_91073_==null||mc.f_91074_==null)return false;
  float partial=(float)event.getPartialTick();Vec3 a,b;String kind="command";double time=0;
  if(active()){
   var entity=actor();a=entity==null?new Vec3(cue.x(),cue.y(),cue.z()):entity.m_20318_(partial);
   var target=mc.f_91073_.m_6815_(cue.targetEntity());b=target==null?new Vec3(cue.tx(),cue.ty(),cue.tz()):target.m_20318_(partial);kind=cue.kind();time=age(partial);
  }else{
   var member=BattleTargetHud.roster().stream().filter(t->t.ally()&&t.turn()).findFirst().orElse(null);
   var entity=member==null?mc.f_91074_:mc.f_91073_.m_6815_(member.entityId());if(entity==null)entity=mc.f_91074_;a=entity.m_20318_(partial);
   var target=mc.f_91073_.m_6815_(BattleTargetHud.entityId());if(target==null){var enemy=BattleTargetHud.roster().stream().filter(t->!t.ally()&&t.hp()>0).findFirst().orElse(null);if(enemy!=null)target=mc.f_91073_.m_6815_(enemy.entityId());}
   b=target==null?a.m_82520_(0,0,4):target.m_20318_(partial);
  }
  Vec3 forward=new Vec3(b.f_82479_-a.f_82479_,0,b.f_82481_-a.f_82481_);if(forward.m_82556_()<.001)forward=new Vec3(0,0,1);else forward=forward.m_82541_();Vec3 side=new Vec3(forward.f_82481_,0,-forward.f_82479_);
  var shot=BattleShot.sample(kind,time);Vec3 focus=a.m_165921_(b,shot.focus()).m_82520_(0,shot.focusHeight(),0);
  Vec3 desired=focus.m_82549_(side.m_82490_(shot.side())).m_82549_(forward.m_82490_(shot.depth())).m_82520_(0,shot.height()-shot.focusHeight(),0);
  double hit=hitAge(partial);if(active()&&hit>=0&&hit<5)desired=desired.m_82520_(Math.sin(hit*5)*.035*(1-hit/5),Math.cos(hit*6)*.025*(1-hit/5),0);
  ExplorationCamera.place(event,ExplorationCamera.collide(focus,desired),focus,shot.roll());fov=shot.fov();return true;
 }
}
