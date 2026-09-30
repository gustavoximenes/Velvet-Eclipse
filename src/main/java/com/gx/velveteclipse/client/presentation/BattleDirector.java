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
 private static int lastTick=-1;private static float fov=36;
 private static BattleFraming.View commandFrame;private static long frameTick;private static int frameKey;
 public static void accept(BattleDirectionS2C p){
  var world=Minecraft.m_91087_().f_91073_;if(world==null)return;
  if(p.kind().equals("cancel")){if(cue!=null&&cue.actor().equals(p.actor()))reset();return;}
  cue=p;level=world;started=world.m_46467_();lastTick=-1;
 }
 public static void reset(){cue=null;level=null;lastTick=-1;fov=36;commandFrame=null;}
 public static boolean active(){return cue!=null&&age(0)<cue.duration();}
 public static double age(float partial){var w=Minecraft.m_91087_().f_91073_;return cue==null||w==null||w!=level?1000:w.m_46467_()-started+partial;}
 public static String skill(){return cue==null?"":cue.skill();}
 public static boolean shifting(){return active()&&cue.kind().equals("shift");}
 public static boolean shiftActor(UUID id){return shifting()&&(cue.actor().equals(id)||shiftRecipient(id));}
 public static boolean shiftRecipient(UUID id){var w=Minecraft.m_91087_().f_91073_;var p=w==null||cue==null?null:w.m_6815_(cue.targetEntity());return shifting()&&p!=null&&p.m_20148_().equals(id);}
 public static void shiftHud(HudDesign.Canvas c,int w,int h,float partial){if(!shifting())return;var world=Minecraft.m_91087_().f_91073_;var a=actor();var b=world==null?null:world.m_6815_(cue.targetEntity());ShiftPresentation.paint(c,w,h,age(partial),a==null?"Aliado":a.m_7755_().getString(),b==null?"Aliado":b.m_7755_().getString());}
 public static boolean summoning(UUID id){return active()&&cue.kind().equals("skill")&&cue.actor().equals(id);}
 public static float castingYaw(UUID id,float fallback){if(!summoning(id))return fallback;double x=cue.tx()-cue.x(),z=cue.tz()-cue.z();return x*x+z*z<.001?fallback:(float)Math.toDegrees(Math.atan2(-x,z));}
 public static boolean attacking(UUID id){return active()&&cue.kind().equals("attack")&&cue.actor().equals(id);}
 public static double hitAge(float partial){return age(partial)-(cue!=null&&cue.kind().equals("skill")?86:cue!=null&&cue.kind().equals("enemy")?10:19);}
 public static boolean hitPlayer(UUID id){var w=Minecraft.m_91087_().f_91073_;if(!active()||w==null||cue.targetEntity()<0)return false;var target=w.m_6815_(cue.targetEntity());return target!=null&&target.m_20148_().equals(id)&&hitAge(0)>=0&&hitAge(0)<12;}
 public static Family weapon(){try{return cue==null?Family.SWORD:Family.valueOf(cue.weapon());}catch(IllegalArgumentException e){return Family.SWORD;}}
 public static float personaAlpha(String owner,float partial){
  if(!active()||!cue.kind().equals("skill")||!cue.actor().toString().equals(owner))return 1;
  double t=age(partial),a=Math.max(0,Math.min(1,(t-46)/12)),b=Math.max(0,Math.min(1,(108-t)/7));return (float)(a*a*(3-2*a)*b*b*(3-2*b));
 }
 public static float fov(){return fov;}
 public static boolean commandSubject(UUID id){
  var mc=Minecraft.m_91087_();if(mc.f_91074_==null||!ClientBattleState.snapshot().active())return false;
  if(active()&&BattleFollowCamera.direct(cue.actor().equals(mc.f_91074_.m_20148_()),shifting()))return false;
  var member=BattleTargetHud.roster().stream().filter(t->t.ally()&&t.turn()).findFirst().orElse(null);
  return id.equals(BattleFollowCamera.enabled()&&member!=null?member.id():mc.f_91074_.m_20148_());
 }
 public static void tick(){
  var mc=Minecraft.m_91087_();if(mc.f_91073_==null||level!=null&&mc.f_91073_!=level||!ClientBattleState.snapshot().active()){reset();return;}if(level==null)level=mc.f_91073_;if(!active())return;
  int t=(int)age(0);if(lastTick==t)return;
  if(cue.kind().equals("skill")&&lastTick<38&&t>=38){var a=actor();if(a!=null)PhotonBattleEffects.trigger(a.m_20185_(),a.m_20186_()+1.55,a.m_20189_());}
  if(cue.kind().equals("skill")&&lastTick<46&&t>=46){var a=actor();if(a!=null){double yaw=Math.toRadians(castingYaw(cue.actor(),a.m_146908_()));PhotonBattleEffects.summon(a.m_20185_()+Math.sin(yaw)*1.4,a.m_20186_(),a.m_20189_()-Math.cos(yaw)*1.4);}}
  if(shifting()&&lastTick<ShiftPresentation.TRANSFER&&t>=ShiftPresentation.TRANSFER){var a=actor();var b=targetPosition(1);if(a!=null)PhotonBattleEffects.shift(a.m_20182_(),b);}
  if(cue.kind().equals("attack")&&lastTick<19&&t>=19){var at=targetPosition(1);PhotonBattleEffects.impact(weapon()==Family.BOW||weapon()==Family.GUN||weapon()==Family.SPEAR?Element.PIERCE:weapon()==Family.FISTS?Element.PHYSICAL:Element.SLASH,1,at.f_82479_,at.f_82480_,at.f_82481_,cue.actor().getLeastSignificantBits());}
  if(cue.kind().equals("attack")&&weapon()==Family.BOW&&lastTick<16&&t>=16){var a=actor();var at=targetPosition(1);if(a instanceof net.minecraft.client.player.AbstractClientPlayer p){var from=ApprovedAttackRenderer.projectileOrigin(p,Family.BOW);com.gx.velveteclipse.client.exploration.ArrowFlight.accept(new com.gx.velveteclipse.exploration.ExplorationNetwork.Shot(from.f_82479_,from.f_82480_,from.f_82481_,at.f_82479_,at.f_82480_+.8,at.f_82481_,3));}}
  int enemyHit=cue.kind().equals("enemy_melee")?19:10;
  if(cue.kind().startsWith("enemy")&&cue.skill().isEmpty()&&lastTick<enemyHit&&t>=enemyHit){var at=targetPosition(1);PhotonBattleEffects.impact(Element.PHYSICAL,1,at.f_82479_,at.f_82480_,at.f_82481_,cue.actor().getLeastSignificantBits());}
  lastTick=t;
 }
 private static Entity actor(){var w=Minecraft.m_91087_().f_91073_;return cue==null||w==null?null:w.m_6815_(cue.actorEntity());}
 private static Vec3 targetPosition(float partial){var w=Minecraft.m_91087_().f_91073_;var e=w==null?null:w.m_6815_(cue.targetEntity());return e==null?new Vec3(cue.tx(),cue.ty(),cue.tz()):e.m_20318_(partial);}
 public static boolean camera(ViewportEvent.ComputeCameraAngles event){
  var mc=Minecraft.m_91087_();if(!ClientBattleState.snapshot().active()||mc.f_91073_==null||mc.f_91074_==null)return false;
  float partial=(float)event.getPartialTick();Vec3 a,b;String kind="command";double time=0;
  boolean directed=active()&&BattleFollowCamera.direct(cue.actor().equals(mc.f_91074_.m_20148_()),shifting());
  if(directed){
   var entity=actor();a=entity==null?new Vec3(cue.x(),cue.y(),cue.z()):entity.m_20318_(partial);
   var target=mc.f_91073_.m_6815_(cue.targetEntity());b=target==null?new Vec3(cue.tx(),cue.ty(),cue.tz()):target.m_20318_(partial);kind=cue.kind();time=age(partial);
  }else{
   var member=BattleTargetHud.roster().stream().filter(t->t.ally()&&t.turn()).findFirst().orElse(null);
   var entity=!BattleFollowCamera.enabled()||member==null?mc.f_91074_:mc.f_91073_.m_6815_(member.entityId());if(entity==null)entity=mc.f_91074_;a=entity.m_20318_(partial);
   // Neutral command camera uses the same enemy centroid for every following client.
   b=Vec3.f_82478_;int count=0;for(var enemy:BattleTargetHud.roster())if(!enemy.ally()&&enemy.hp()>0){var e=mc.f_91073_.m_6815_(enemy.entityId());if(e!=null){b=b.m_82549_(e.m_20318_(partial));count++;}}
   b=count==0?a.m_82520_(0,0,4):b.m_82490_(1./count);
  }
  Vec3 forward=new Vec3(b.f_82479_-a.f_82479_,0,b.f_82481_-a.f_82481_);
  if(kind.equals("shift")){var enemy=BattleTargetHud.roster().stream().filter(t->!t.ally()&&t.hp()>0).findFirst().orElse(null);var e=enemy==null?null:mc.f_91073_.m_6815_(enemy.entityId());if(e!=null)forward=e.m_20318_(partial).m_82546_(a.m_165921_(b,.5));forward=new Vec3(forward.f_82479_,0,forward.f_82481_);}
  if(forward.m_82556_()<.001)forward=new Vec3(0,0,1);else forward=forward.m_82541_();Vec3 side=new Vec3(forward.f_82481_,0,-forward.f_82479_);
  var shot=BattleShot.sample(kind,time);Vec3 focus=a.m_165921_(b,shot.focus()).m_82520_(0,shot.focusHeight(),0);
  Vec3 desired=focus.m_82549_(side.m_82490_(shot.side())).m_82549_(forward.m_82490_(shot.depth())).m_82520_(0,shot.height()-shot.focusHeight(),0);
  double hit=hitAge(partial);if(directed&&!shifting()&&hit>=0&&hit<5)desired=desired.m_82520_(Math.sin(hit*5)*.035*(1-hit/5),Math.cos(hit*6)*.025*(1-hit/5),0);
  Vec3 position;
  if(kind.equals("command")){
   double aspect=mc.m_91268_().m_85445_()/(double)Math.max(1,mc.m_91268_().m_85446_());
   int key=java.util.Objects.hash(BattleFollowCamera.enabled(),a,aspect,BattleTargetHud.roster().size());long tick=mc.f_91073_.m_46467_();
   if(commandFrame==null||tick-frameTick>=5||key!=frameKey){
    var points=new java.util.ArrayList<Vec3>();for(var member:BattleTargetHud.roster())if(!member.ally()&&member.hp()>0){var entity=mc.f_91073_.m_6815_(member.entityId());if(entity!=null){var foot=entity.m_20318_(partial);double radius=member.ally()?.35:.85,height=entity.m_7755_().getString().startsWith("Guardião")?4:2.5;for(double x:new double[]{-radius,radius})for(double z:new double[]{-radius,radius})for(double y:new double[]{.05,height})points.add(foot.m_82520_(x,y,z));}}
    commandFrame=BattleFraming.command(a,points,shot.fov(),aspect,ExplorationCamera::collide);frameKey=key;frameTick=tick;
   }
   focus=commandFrame.focus();position=commandFrame.eye();
  }else position=BattleCameraSafety.choose(focus,desired,ExplorationCamera::collide);
  ExplorationCamera.place(event,position,focus,shot.roll());fov=shot.fov();return true;
 }
}
