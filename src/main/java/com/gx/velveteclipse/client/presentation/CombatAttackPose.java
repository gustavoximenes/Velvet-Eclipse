package com.gx.velveteclipse.client.presentation;
import com.gx.velveteclipse.client.exploration.WeaponPose;
import com.gx.velveteclipse.core.battle.MeleeTimeline;
import com.gx.velveteclipse.equipment.EquipmentCatalog.Family;
import dev.kosmx.playerAnim.core.util.Vec3f;
import static com.gx.velveteclipse.exploration.ExplorationRules.smooth;

/** Plant before the cut, then backstep into formation. Ranged attacks remain planted. */
public final class CombatAttackPose {
 public static double weight(double t){return smooth(t/4)*(1-smooth((t-39)/5));}
 public static double speed(Family family,double t){if(family==Family.BOW||family==Family.GUN)return 0;return .9*(smooth((t-2)/3)*(1-smooth((t-9)/3))+smooth((t-30)/3)*(1-smooth((t-40)/4)));}
 public static Vec3f sample(Family family,String part,double t,boolean bend){
  double speed=speed(family,t),phase=t<30?t*.95:-(t-30)*.85,attack=MeleeTimeline.slash(t);
  var pose=bend?WeaponPose.bend(family,part,phase,speed,attack):WeaponPose.rotation(family,part,phase,speed,attack);
  if(!bend&&part.equals("body")&&t>30)return new Vec3f((float)(.11*speed),pose.getY(),pose.getZ());
  return pose;
 }
 private CombatAttackPose(){}
}
