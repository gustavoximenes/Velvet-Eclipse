package com.gx.velveteclipse.client.presentation;

import com.gx.velveteclipse.equipment.EquipmentCatalog.Family;
import net.minecraft.world.phys.Vec3;
import org.joml.*;
import java.lang.Math;
import static com.gx.velveteclipse.client.presentation.ApprovedWeaponPose.*;

/** The approved relaxed stance, separate from attacks, with bounded correction for wall-safe views. */
public final class BattleCommandPose {
 public static float yaw(Vec3 player,Vec3 enemies,float fallback){return yaw(player,enemies,null,fallback);}
 public static float yaw(Vec3 player,Vec3 enemies,Vec3 camera,float fallback){
  var delta=enemies.m_82546_(player);
  if(delta.f_82479_*delta.f_82479_+delta.f_82481_*delta.f_82481_<.0001)return fallback;
  double facing=Math.toDegrees(Math.atan2(-delta.f_82479_,delta.f_82481_))+80;
  if(camera!=null){
   double towardsCamera=Math.toDegrees(Math.atan2(player.f_82479_-camera.f_82479_,camera.f_82481_-player.f_82481_));
   // Preserve the approved side view, with only a small correction for walls.
   // The rejected 35-degree camera pose turned the avatar towards the viewer.
   double reference=Math.toDegrees(Math.atan2(3.8,-4.8))-80;
   double correction=(towardsCamera-facing-reference+180)%360;if(correction<0)correction+=360;correction-=180;
   facing+=Math.max(-25,Math.min(25,correction));
  }
  return (float)facing;
 }
 public static Frame sample(Family family,boolean slim){
  var base=ApprovedSwordPose.sample(-1,slim);
  if(family==Family.NONE||family==Family.SWORD)return base;
  var left=base.left();var right=base.right();
  // Carry the bow low with one hand, but keep its lower limb above the floor.
  if(family==Family.BOW)left=arm(true,slim,-30,0,-10,-15);
  var bodyRotation=base.body().getNormalizedRotation(new Quaternionf());
  var grip=base.body().transformPosition(new Vector3f((family==Family.BOW?left:right).hand()));
  var weapon=new Quaternionf(base.weapon());float scale=family==Family.SPEAR?.79f:.78f;
  if(family==Family.SPEAR)weapon=new Quaternionf().rotationTo(v(0,1,0),v(-.46,.14,-.877).normalize());
  if(family==Family.AXE)weapon=new Quaternionf().rotationTo(v(0,1,0),v(-.45,.25,-.857).normalize());
  if(family==Family.BOW)weapon=new Quaternionf(bodyRotation).rotateY(rad(90));
  if(family==Family.GUN)weapon=new Quaternionf(bodyRotation).rotateX(rad(45));
  if(family==Family.FISTS)weapon=new Quaternionf().rotationTo(v(0,1,0),base.body().transformDirection(new Vector3f(right.forward())));
  var matrix=new Matrix4f().translation(grip).rotate(weapon).scale(scale);
  double reach=switch(family){case GREATSWORD->33/16.;case AXE->28/16.;case SPEAR->39/16.;case RAPIER->29/16.;case KNIFE->1;case FISTS->.20;default->0;};
  var tip=matrix.transformPosition(family==Family.GUN?v(0,.13,-.875):v(0,reach,0));
  var top=matrix.transformPosition(v(4.55/16.,-17/16.,0));var bottom=matrix.transformPosition(v(4.55/16.,17/16.,0));var nock=new Vector3f(top).lerp(bottom,.5f);
  return new Frame(base.body(),base.head(),right,left,base.rightLeg(),base.leftLeg(),base.rightKnee(),base.leftKnee(),grip,weapon,scale,tip,top,bottom,nock,0,base.feet());
 }
 private static Arm arm(boolean left,boolean slim,double x,double y,double z,double bend){
  var rotation=new Quaternionf().rotationZYX(rad(z),rad(y),rad(x));float hinge=rad(bend);
  var hand=shoulder(left,slim).add(rotation.transform(v((left?1:-1)*(slim?.03125:.0625),.25,0))).add(new Quaternionf(rotation).rotateX(hinge).transform(v(0,.375,0)));
  return new Arm(rotation,hinge,hand,new Quaternionf(rotation).rotateX(hinge).transform(v(0,1,0)));
 }
 private BattleCommandPose(){}
}
