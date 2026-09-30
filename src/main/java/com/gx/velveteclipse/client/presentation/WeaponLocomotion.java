package com.gx.velveteclipse.client.presentation;
import com.gx.velveteclipse.equipment.EquipmentCatalog.Family;
import org.joml.*;
import java.lang.Math;
import static com.gx.velveteclipse.client.presentation.ApprovedWeaponPose.*;

/** Walk/carry pose uses the same native axes and elbow convention as the normal sword. */
public final class WeaponLocomotion {
 public static Frame sample(Family family,boolean slim,double phase,double speed){
  var idle=ApprovedWeaponPose.sample(family,-1,slim);double w=Math.max(0,Math.min(1,speed));
  double stride=Math.sin(phase),swing=Math.cos(phase);
  var r=v(stride*28*w,0,3);var l=v(-stride*28*w,0,-3);
  float rk=rad((4+Math.max(0,-swing)*32)*w),lk=rad((4+Math.max(0,swing)*32)*w);
  var body=new Matrix4f(idle.body()).rotateAround(new Quaternionf().rotationZYX(rad(stride*.8*w),rad(stride*1.8*w),rad(5*w)),0,.75f,0);
  var right=idle.right();var left=idle.left();
  boolean free=family==Family.SWORD||family==Family.NONE||family==Family.RAPIER||family==Family.KNIFE;
  if(free){right=swing(right,-stride*12*w,0,-4*w,false,slim);left=swing(left,stride*25*w,0,2*w,true,slim);}
  if(family==Family.BOW){right=swing(right,-stride*23*w,0,2*w,false,slim);left=swing(left,stride*10*w,0,-3*w,true,slim);}
  boolean bow=family==Family.BOW;
  var primary=bow?left:right;var oldPrimary=bow?idle.left():idle.right();
  var rotation=body.getNormalizedRotation(new Quaternionf());var oldRotation=idle.body().getNormalizedRotation(new Quaternionf());
  var delta=new Quaternionf(rotation).mul(primary.rotation()).mul(new Quaternionf(oldRotation).mul(oldPrimary.rotation()).invert());
  var weapon=new Quaternionf(delta).mul(idle.weapon());var grip=body.transformPosition(new Vector3f(primary.hand()));
  var tip=attach(idle.tip(),idle.grip(),grip,delta);var top=attach(idle.stringTop(),idle.grip(),grip,delta);var bottom=attach(idle.stringBottom(),idle.grip(),grip,delta);var nock=new Vector3f(top).lerp(bottom,.5f);
  return new Frame(body,idle.head(),right,left,r,l,rk,lk,grip,weapon,idle.scale(),tip,top,bottom,nock,0,feet(body,r,l,rk,lk));
 }
 private static Vector3f attach(Vector3f point,Vector3f oldGrip,Vector3f grip,Quaternionf delta){return delta.transform(new Vector3f(point).sub(oldGrip)).add(grip);}
 private static Arm swing(Arm base,double x,double y,double z,boolean left,boolean slim){
  var rotation=new Quaternionf().rotationZYX(rad(z),rad(y),rad(x)).mul(base.rotation());float bend=base.bend();
  var hand=shoulder(left,slim).add(rotation.transform(v((left?1:-1)*(slim?.03125:.0625),.25,0))).add(new Quaternionf(rotation).rotateX(bend).transform(v(0,.375,0)));
  return new Arm(rotation,bend,hand,new Quaternionf(rotation).rotateX(bend).transform(v(0,1,0)));
 }
 public static Frame look(Frame f,double yaw,double pitch){
  double wrapped=(yaw+180)%360;if(wrapped<0)wrapped+=360;wrapped-=180;
  var head=f.body().getNormalizedRotation(new Quaternionf()).invert().mul(new Quaternionf().rotationY(rad(Math.max(-70,Math.min(70,wrapped)))).rotateX(rad(Math.max(-50,Math.min(50,pitch)))));
  return new Frame(f.body(),head,f.right(),f.left(),f.rightLeg(),f.leftLeg(),f.rightKnee(),f.leftKnee(),f.grip(),f.weapon(),f.scale(),f.tip(),f.stringTop(),f.stringBottom(),f.nock(),f.height(),f.feet());
 }
 private WeaponLocomotion(){}
}
