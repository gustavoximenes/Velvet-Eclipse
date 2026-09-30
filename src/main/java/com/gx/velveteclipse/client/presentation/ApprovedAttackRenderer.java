package com.gx.velveteclipse.client.presentation;

import com.gx.velveteclipse.client.*;
import com.gx.velveteclipse.client.exploration.*;
import com.gx.velveteclipse.core.battle.WeaponAttackTiming;
import com.gx.velveteclipse.equipment.EquipmentCatalog.Family;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderPlayerEvent;
import org.joml.*;
import java.lang.Math;
import java.util.*;
import static com.gx.velveteclipse.client.presentation.ApprovedWeaponPose.*;

/** Native standard/slim mesh with the actual participant's skin; shared by field and battle. */
public final class ApprovedAttackRenderer {
 private record Rig(ModelPart root,RigidSkinLimb[] limbs,RigidSkinLimb[] clothing){}
 private static final Map<Boolean,Rig> rigs=new HashMap<>();
 public static final float PLAYER_SCALE=.9375f;
 private static RigidSkinLimb arm(boolean slim,boolean left,boolean sleeve){return new RigidSkinLimb(left?(sleeve?48:32):40,left?48:sleeve?32:16,left?-1:slim?-2:-3,-2,-2,slim?3:4,12,4,sleeve?.25f:0,4);}
 private static RigidSkinLimb leg(boolean left,boolean pants){return new RigidSkinLimb(left?(pants?0:16):0,left?48:pants?32:16,-2,0,-2,4,12,4,pants?.25f:0,6);}
 private static Rig rig(boolean slim){return rigs.computeIfAbsent(slim,s->new Rig(LayerDefinition.m_171565_(PlayerModel.m_170825_(CubeDeformation.f_171458_,s),64,64).m_171564_(),new RigidSkinLimb[]{arm(s,false,false),arm(s,true,false),leg(false,false),leg(true,false)},new RigidSkinLimb[]{arm(s,false,true),arm(s,true,true),leg(false,true),leg(true,true)}));}
 public static void body(PoseStack p,VertexConsumer out,Frame frame,boolean slim,Set<PlayerModelPart> visible,int light){
  var rig=rig(slim);p.m_85836_();p.m_252931_(frame.body());
  for(String name:List.of("body","head","jacket","hat")){
   if(name.equals("hat")&&!visible.contains(PlayerModelPart.HAT)||name.equals("jacket")&&!visible.contains(PlayerModelPart.JACKET))continue;
   var part=rig.root.m_171324_(name);part.m_233569_();p.m_85836_();if(name.equals("head")||name.equals("hat"))p.m_252781_(frame.head());part.m_104301_(p,out,light,655360);p.m_85849_();
  }
  String[] names={"right_arm","left_arm","right_leg","left_leg"};PlayerModelPart[] layers={PlayerModelPart.RIGHT_SLEEVE,PlayerModelPart.LEFT_SLEEVE,PlayerModelPart.RIGHT_PANTS_LEG,PlayerModelPart.LEFT_PANTS_LEG};
  for(int i=0;i<4;i++){
   var part=rig.root.m_171324_(names[i]);part.m_233569_();p.m_85836_();part.m_104299_(p);float bend;
   if(i<2){var a=i==0?frame.right():frame.left();p.m_252781_(a.rotation());bend=a.bend();}
   else{var a=i==2?frame.rightLeg():frame.leftLeg();p.m_252781_(new Quaternionf().rotationZYX(rad(a.z),rad(a.y),rad(a.x)));bend=i==2?frame.rightKnee():frame.leftKnee();}
   rig.limbs[i].render(p,out,bend,light);
   if(visible.contains(layers[i])){rig.clothing[i].render(p,out,bend,light);}p.m_85849_();
  }p.m_85849_();
 }
 public static void weapon(PoseStack p,MultiBufferSource out,int light,Family family,Frame f,double t){
  p.m_85836_();p.m_85837_(f.grip().x,f.grip().y,f.grip().z);p.m_252781_(f.weapon());p.m_85841_(f.scale(),f.scale(),f.scale());
  if(family==Family.BOW)WeaponModels.bowWithoutString(p,out,light);else WeaponModels.draw(family,p,out,light);p.m_85849_();
  if(family==Family.FISTS){var hand=f.body().transformPosition(new Vector3f(f.left().hand()));var direction=f.body().transformDirection(new Vector3f(f.left().forward()));p.m_85836_();p.m_85837_(hand.x,hand.y,hand.z);p.m_252781_(new Quaternionf().rotationTo(v(0,1,0),direction));p.m_85841_(-f.scale(),f.scale(),f.scale());WeaponModels.draw(family,p,out,light);p.m_85849_();}
  if(family==Family.BOW){
   line(p,out,f.stringTop(),f.nock(),.009f,222,240,255,230);line(p,out,f.nock(),f.stringBottom(),.009f,222,240,255,230);
   if(t>=.16&&t<.52){var aim=f.weapon().transform(v(-1,0,0));var center=new Vector3f(f.nock()).add(aim.mul(.625f*f.scale()));p.m_85836_();p.m_85837_(center.x,center.y,center.z);p.m_252781_(f.weapon());p.m_85841_(f.scale(),f.scale(),f.scale());WeaponModels.arrow(p,out,light);p.m_85849_();}
  }
 }
 private static void vertex(VertexConsumer out,PoseStack p,Vector3f v,int r,int g,int b,int a){out.m_252986_(p.m_85850_().m_252922_(),v.x,v.y,v.z).m_6122_(r,g,b,a).m_5752_();}
 private static void quad(PoseStack p,MultiBufferSource buffers,Vector3f a,Vector3f b,Vector3f c,Vector3f d,int r,int g,int blue,int alpha){var out=buffers.m_6299_(RenderType.m_110502_());for(var v:new Vector3f[]{a,b,c,d})vertex(out,p,v,r,g,blue,alpha);}
 private static void line(PoseStack p,MultiBufferSource out,Vector3f a,Vector3f b,float width,int r,int g,int blue,int alpha){var side=new Vector3f(width,0,0);quad(p,out,new Vector3f(a).add(side),new Vector3f(b).add(side),new Vector3f(b).sub(side),new Vector3f(a).sub(side),r,g,blue,alpha);side.set(0,0,width);quad(p,out,new Vector3f(a).add(side),new Vector3f(b).add(side),new Vector3f(b).sub(side),new Vector3f(a).sub(side),r,g,blue,alpha);}
 private static void effects(PoseStack p,MultiBufferSource out,Family family,Frame current,double t,boolean slim){
  if(t<0||family==Family.BOW)return;
  if(family==Family.GUN){if(t>=.48&&t<.59){var at=current.tip();line(p,out,new Vector3f(at).add(-.11f,0,0),new Vector3f(at).add(.11f,0,0),.025f,255,218,91,245);line(p,out,new Vector3f(at).add(0,-.11f,0),new Vector3f(at).add(0,.11f,0),.02f,255,245,216,250);}return;}
  boolean two=family==Family.FISTS||family==Family.KNIFE;double begin=two?(t<.45?.22:.49):family==Family.SWORD||family==Family.NONE?.27:.34,end=two?(t<.45?.38:.66):.68;
  if(t<=begin||t>end+.07)return;double fade=1-smooth((t-end)/.07);Vector3f previous=null,previousInner=null;
  for(int i=0;i<7;i++){double old=Math.max(begin,t-.10)+(Math.min(t,end)-Math.max(begin,t-.10))*i/6;var f=sample(family,old,slim);Vector3f tip=family==Family.FISTS?f.body().transformPosition(new Vector3f((old<.45?f.left():f.right()).hand())):new Vector3f(f.tip());tip.y+=current.feet()+current.height()-f.feet()-f.height();var inner=new Vector3f(tip).lerp(current.grip(),family==Family.SPEAR||family==Family.RAPIER?.08f:.20f);
   if(previous!=null){quad(p,out,previous,tip,inner,previousInner,62,189,255,(int)(150*fade*i/6));line(p,out,previous,tip,.009f,207,254,255,(int)(220*fade*i/6));}previous=tip;previousInner=inner;
  }
 }
 public static Vec3 projectileOrigin(AbstractClientPlayer player,Family family){
  boolean slim="slim".equals(player.m_108564_());var f=sample(family,family==Family.BOW?.50:.48,slim);var at=new Vector3f(family==Family.BOW?f.nock():f.tip());at.set(at.x,(float)(f.feet()+f.height()-at.y),-at.z).mul(PLAYER_SCALE);new Quaternionf().rotationY(rad(-player.m_146908_())).transform(at);return player.m_20182_().m_82520_(at.x,at.y,at.z);
 }
 public static boolean render(RenderPlayerEvent.Pre e){
  if(!(e.getEntity() instanceof AbstractClientPlayer player))return false;var mc=Minecraft.m_91087_();if(player.m_5833_()||AllOutPresentation.active()||EntryPlayerRenderer.drawing())return false;
  boolean battle=ClientBattleState.snapshot().active(),attack=BattleDirector.attacking(player.m_20148_());
  boolean field=player==mc.f_91074_&&ExplorationClient.attacking()&&!battle;
  boolean command=battle&&!attack&&BattleTargetHud.roster().stream().anyMatch(a->a.ally()&&a.hp()>0&&a.id().equals(player.m_20148_()));
  boolean exploration=player==mc.f_91074_&&ExplorationClient.swordVisible()&&!battle&&!ExplorationClient.entry();
  var remote=player==mc.f_91074_?null:RemoteWeaponSwing.get(player);if(!attack&&!field&&!command&&!exploration&&remote==null)return false;
  if(BattleDirector.summoning(player.m_20148_())||BattleDirector.shiftActor(player.m_20148_())||BattleDirector.hitPlayer(player.m_20148_()))return false;
  float partial=e.getPartialTick();Family family=attack?BattleDirector.weapon():field?ExplorationClient.attackFamily():remote!=null?remote.family():player==mc.f_91074_?com.gx.velveteclipse.client.equipment.EquipmentScreen.family():BattleTargetHud.roster().stream().filter(a->a.id().equals(player.m_20148_())).map(a->RemoteWeaponSwing.family(a.weapon())).findFirst().orElse(Family.SWORD);
  double elapsed=attack?WeaponAttackTiming.battleElapsed(family,BattleDirector.age(partial)):field?ExplorationClient.ticks()+partial:remote!=null?remote.elapsed(partial):-1;
  double t=WeaponAttackTiming.authored(family,elapsed);boolean slim="slim".equals(player.m_108564_());Frame frame=sample(family,t,slim);
  if(attack&&elapsed<0&&!WeaponAttackTiming.ranged(family))frame=locomotion(frame,BattleDirector.age(partial),CombatAttackPose.speed(family,BattleDirector.age(partial)));
  if(exploration&&!field)frame=travel(frame,ExplorationAnimation.phase(partial),ExplorationAnimation.speed(partial));
  float yaw=command?commandYaw(player):remote!=null?remote.yaw():field?ExplorationClient.yaw():exploration?ExplorationAnimation.facing(partial):player.m_146908_();
  var p=e.getPoseStack();p.m_85836_();try{
   p.m_252781_(new Quaternionf().rotationY(rad(-yaw)));p.m_85841_(PLAYER_SCALE,PLAYER_SCALE,PLAYER_SCALE);p.m_85837_(0,frame.feet()+frame.height(),0);p.m_85841_(1,-1,-1);
   Set<PlayerModelPart> visible=EnumSet.noneOf(PlayerModelPart.class);for(var layer:PlayerModelPart.values())if(player.m_36170_(layer))visible.add(layer);
   if(command&&BattleDirector.commandSubject(player.m_20148_()))CommandOutline.draw(p,e.getMultiBufferSource(),frame,slim,visible);
   body(p,e.getMultiBufferSource().m_6299_(RenderType.m_110452_(player.m_108560_())),frame,slim,visible,e.getPackedLight());weapon(p,e.getMultiBufferSource(),e.getPackedLight(),family,frame,t);effects(p,e.getMultiBufferSource(),family,frame,t,slim);
  }finally{p.m_85849_();}e.setCanceled(true);return true;
 }
 private static float commandYaw(AbstractClientPlayer p){var world=Minecraft.m_91087_().f_91073_;Vec3 center=Vec3.f_82478_;int n=0;for(var t:BattleTargetHud.roster())if(!t.ally()&&t.hp()>0){var e=world.m_6815_(t.entityId());if(e!=null){center=center.m_82549_(e.m_20182_());n++;}}if(n==0)return p.m_146908_();center=center.m_82490_(1./n).m_82546_(p.m_20182_());return (float)Math.toDegrees(Math.atan2(-center.f_82479_,center.f_82481_))+80;}
 private static Frame locomotion(Frame f,double tick,double speed){return travel(f,(tick<30?tick:-(tick-30))*.95,speed);}
 public static Frame travelPose(Family family,boolean slim,double phase,double speed){return travel(sample(family,-1,slim),phase,speed);}
 private static Frame travel(Frame f,double phase,double speed){var r=v(Math.sin(phase)*36*speed,0,3);var l=v(-Math.sin(phase)*36*speed,0,-3);float rk=rad(Math.max(0,Math.sin(phase))*48*speed),lk=rad(Math.max(0,-Math.sin(phase))*48*speed);return new Frame(f.body(),f.head(),f.right(),f.left(),r,l,rk,lk,f.grip(),f.weapon(),f.scale(),f.tip(),f.stringTop(),f.stringBottom(),f.nock(),0,feet(f.body(),r,l,rk,lk));}
 private ApprovedAttackRenderer(){}
}
