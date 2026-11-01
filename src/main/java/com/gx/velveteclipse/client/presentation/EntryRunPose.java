package com.gx.velveteclipse.client.presentation;
import org.joml.*;
import java.lang.Math;
import com.gx.velveteclipse.equipment.EquipmentCatalog.Family;
import static com.gx.velveteclipse.client.presentation.ApprovedWeaponPose.*;
/** Approved entry revision 2: sagittal forward lean, no screen or torso roll. */
public final class EntryRunPose {
 private static Arm arm(boolean left,boolean slim,double x,double y,double z,double bend){
  var q=new Quaternionf().rotationZYX(rad(z),rad(y),rad(x));float b=rad(bend);
  var hand=shoulder(left,slim).add(q.transform(v((left?1:-1)*(slim?.03125:.0625),.25,0))).add(new Quaternionf(q).rotateX(b).transform(v(0,.375,0)));
  return new Arm(q,b,hand,new Quaternionf(q).rotateX(b).transform(v(0,1,0)));
 }
 public static Frame sample(Family family,double time,boolean slim){
  double phase=time*2*Math.PI*2.05,stride=Math.sin(phase);
  var body=new Matrix4f().translation(0,.75f,0).rotate(new Quaternionf().rotationZYX(0,rad(stride*4),rad(12))).translate(0,-.75f,0);
  var right=arm(false,slim,-14-stride*21,-9,13,-38-Math.max(0,stride)*19);
  var left=arm(true,slim,20+stride*46,10,-20,-68+stride*17);
  var rl=v(stride*53,0,6);var ll=v(-stride*53,0,-6);
  float rk=rad(18+Math.max(0,-stride)*66),lk=rad(18+Math.max(0,stride)*66);
  var grip=body.transformPosition(new Vector3f(right.hand()));
  var direction=body.transformDirection(v(-.15,.70,-.70).normalize());
  var weapon=new Quaternionf().rotationTo(v(0,1,0),direction);float scale=.82f;
  var tip=new Matrix4f().translation(grip).rotate(weapon).scale(scale).transformPosition(v(0,1.625,0));
  var top=new Vector3f(grip);var bottom=new Vector3f(grip);var nock=new Vector3f(grip);
  if(family!=Family.SWORD&&family!=Family.NONE){
   // Keep the approved two-handed/left-handed carry contacts for the other weapon rigs.
   var carry=WeaponLocomotion.sample(family,slim,phase,1);
   right=carry.right();left=carry.left();
   var delta=body.getNormalizedRotation(new Quaternionf()).mul(carry.body().getNormalizedRotation(new Quaternionf()).invert());
   grip=body.transformPosition(new Vector3f((family==Family.BOW?left:right).hand()));
   weapon=new Quaternionf(delta).mul(carry.weapon());scale=carry.scale();
   tip=delta.transform(new Vector3f(carry.tip()).sub(carry.grip())).add(grip);
   top=delta.transform(new Vector3f(carry.stringTop()).sub(carry.grip())).add(grip);
   bottom=delta.transform(new Vector3f(carry.stringBottom()).sub(carry.grip())).add(grip);
   nock=delta.transform(new Vector3f(carry.nock()).sub(carry.grip())).add(grip);
  }
  return new Frame(body,new Quaternionf().rotationX(rad(-9)),right,left,rl,ll,rk,lk,grip,weapon,scale,tip,top,bottom,nock,.045+.03*Math.cos(phase*2),feet(body,rl,ll,rk,lk));
 }
 private EntryRunPose(){}
}
