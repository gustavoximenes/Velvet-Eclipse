package com.gx.velveteclipse.client.presentation;
import com.gx.velveteclipse.equipment.EquipmentCatalog.Family;
import org.joml.*;
import java.lang.Math;
/** Approved offline curves, sampled in native model coordinates. No client singleton required. */
public final class ApprovedWeaponPose {
 static final double[] TIME={0,.18,.34,.48,.58,.72,.92,1.16};
 static double smooth(double a){a=Math.max(0,Math.min(1,a));return a*a*(3-2*a);}
 static float rad(double x){return (float)Math.toRadians(x);}
 static double k(double t,double... a){for(int i=0;i<7;i++)if(t<=TIME[i+1])return a[i]+(a[i+1]-a[i])*smooth((t-TIME[i])/(TIME[i+1]-TIME[i]));return a[7];}
 static Vector3f v(double x,double y,double z){return new Vector3f((float)x,(float)y,(float)z);}
 static Vector3f track(double t,double[][] p){return v(k(t,p[0][0],p[1][0],p[2][0],p[3][0],p[4][0],p[5][0],p[6][0],p[7][0]),k(t,p[0][1],p[1][1],p[2][1],p[3][1],p[4][1],p[5][1],p[6][1],p[7][1]),k(t,p[0][2],p[1][2],p[2][2],p[3][2],p[4][2],p[5][2],p[6][2],p[7][2]));}
 static Vector3f body(Family f,double t){return switch(f){
  case BOW -> v(k(t,2,3,2,2,0,2,2,2),k(t,-12,-26,-32,-32,-29,-20,-12,-12),0);
  case GUN -> v(k(t,2,6,7,7,0,5,2,2),k(t,0,-10,-12,-12,-9,-8,0,0),0);
  case FISTS -> v(k(t,5,12,8,10,6,12,5,5),k(t,-6,-28,25,-30,36,18,-6,-6),k(t,0,2,4,-3,5,2,0,0));
  case RAPIER,SPEAR -> v(k(t,2,7,12,22,19,12,2,2),k(t,-8,-22,-18,9,12,-5,-8,-8),0);
  case GREATSWORD -> v(k(t,4,15,-10,4,21,26,7,4),k(t,0,-28,-45,-10,52,58,8,0),k(t,-2,-6,-8,0,10,9,0,-2));
  case AXE -> v(k(t,4,20,-13,-3,29,28,8,4),k(t,0,-20,-28,-14,20,28,6,0),k(t,0,-7,-5,0,7,5,0,0));
  default -> v(k(t,3,15,-3,3,10,18,4,3),k(t,0,-42,32,55,-38,-20,0,0),k(t,-2,-5,4,6,-8,-3,0,-2));
 };}
 static Vector3f grip(Family f,double t){return switch(f){
  case BOW -> track(t,new double[][]{{.37,.62,0},{.14,.20,-.45},{.10,.16,-.52},{.10,.16,-.52},{.10,.18,-.50},{.18,.28,-.39},{.36,.60,0},{.37,.62,0}});
  case GUN -> track(t,new double[][]{{-.32,.66,-.04},{-.14,.31,-.40},{-.12,.24,-.49},{-.12,.24,-.49},{-.12,.18,-.38},{-.13,.30,-.43},{-.31,.63,-.06},{-.32,.66,-.04}});
  case GREATSWORD -> track(t,new double[][]{{-.19,.48,-.19},{-.18,.16,-.29},{-.08,-.13,-.25},{-.05,.09,-.43},{.23,.40,-.26},{.27,.51,-.07},{-.15,.50,-.18},{-.19,.48,-.19}});
  case AXE -> track(t,new double[][]{{-.19,.47,-.17},{-.29,.26,-.16},{-.05,-.15,-.22},{.0,-.05,-.32},{.04,.48,-.38},{.06,.58,-.22},{-.17,.49,-.17},{-.19,.47,-.17}});
  case SPEAR -> track(t,new double[][]{{-.30,.49,.14},{-.33,.46,.23},{-.28,.40,.07},{-.22,.34,-.07},{-.20,.35,-.08},{-.29,.43,.07},{-.30,.49,.14},{-.30,.49,.14}});
  case RAPIER -> track(t,new double[][]{{-.38,.54,-.08},{-.39,.40,.06},{-.33,.34,-.21},{-.20,.26,-.56},{-.21,.29,-.54},{-.31,.41,-.18},{-.38,.54,-.08},{-.38,.54,-.08}});
  case KNIFE -> track(t,new double[][]{{-.39,.60,-.02},{-.42,.39,.02},{.08,.28,-.46},{.24,.32,-.28},{-.42,.31,-.35},{-.39,.43,-.12},{-.39,.59,-.02},{-.39,.60,-.02}});
  default -> track(t,new double[][]{{-.34,.29,-.25},{-.36,.20,-.18},{-.32,.20,-.23},{-.33,.24,-.11},{-.14,.20,-.55},{-.30,.28,-.24},{-.34,.29,-.25},{-.34,.29,-.25}});
 };}
 static Vector3f direction(Family f,double t){
  double az,vertical;
  switch(f){
   case GREATSWORD -> {az=k(t,-28,-80,-100,-45,65,105,-20,-28);vertical=k(t,.20,-.45,-.90,-.32,.30,.52,.22,.20);}
   case AXE -> {az=k(t,-25,-70,-95,-30,5,10,-20,-25);vertical=k(t,.25,-.3,-.97,-.87,.38,.54,.26,.25);}
   case SPEAR -> {az=k(t,20,25,18,12,12,16,20,20);vertical=k(t,-.10,-.06,-.04,.015,.03,-.07,-.10,-.10);}
   case RAPIER -> {az=k(t,10,16,5,0,0,8,10,10);vertical=k(t,-.12,-.22,-.06,.02,.02,-.07,-.12,-.12);}
   default -> {az=k(t,-28,-90,55,110,-85,-50,-28,-28);vertical=k(t,.34,-.20,.02,.15,-.04,.30,.34,.34);}
  }
  double r=Math.sqrt(1-vertical*vertical);return v(Math.sin(Math.toRadians(az))*r,vertical,-Math.cos(Math.toRadians(az))*r);
 }
 static Vector3f freeHand(Family f,double t){
  if(f==Family.BOW)return track(t,new double[][]{{-.36,.69,0},{-.10,.19,-.25},{.10,.16,.07},{.10,.16,.10},{-.29,.10,.15},{-.32,.24,-.05},{-.35,.64,0},{-.36,.69,0}});
  if(f==Family.FISTS)return track(t,new double[][]{{.32,.24,-.23},{.34,.21,-.14},{.09,.19,-.55},{.28,.24,-.24},{.35,.23,-.14},{.29,.25,-.22},{.32,.24,-.23},{.32,.24,-.23}});
  if(f==Family.RAPIER)return track(t,new double[][]{{.37,.63,.02},{.39,.47,.12},{.43,.48,.18},{.47,.41,.23},{.43,.46,.24},{.40,.51,.11},{.37,.63,.02},{.37,.63,.02}});
  return track(t,new double[][]{{.37,.64,.01},{.42,.40,.13},{.40,.33,.02},{.45,.37,.11},{.41,.42,.15},{.40,.50,.05},{.37,.64,.01},{.37,.64,.01}});
 }
 static double knee(Family f,boolean left,double t){
  if(f==Family.BOW||f==Family.GUN)return k(t,5,9,10,10,12,8,5,5);
  if(f==Family.RAPIER||f==Family.SPEAR)return left?k(t,3,10,8,10,12,8,3,3):k(t,3,17,30,49,42,24,3,3);
  if(f==Family.GREATSWORD||f==Family.AXE)return left?k(t,5,35,26,32,38,35,9,5):k(t,5,32,27,25,29,35,7,5);
  return left?k(t,3,16,42,29,22,20,3,3):k(t,4,20,23,19,20,24,4,4);
 }
 static Vector3f leg(Family f,boolean left,double t){
  double side=left?-5:5;
  if(f==Family.BOW||f==Family.GUN)return v(left?-10:10,0,side);
  if(f==Family.RAPIER||f==Family.SPEAR)return v(left?k(t,-5,8,15,28,24,10,-5,-5):k(t,5,15,-18,-52,-46,-13,5,5),0,side);
  if(f==Family.AXE||f==Family.GREATSWORD)return v(left?k(t,-9,12,-22,-27,-35,10,-9,-9):k(t,9,23,16,9,18,24,9,9),0,side);
  return v(left?k(t,-8,10,-36,-22,-12,13,-8,-8):k(t,8,20,8,17,24,23,8,8),0,side);
 }
 static double height(Family f,double t){return switch(f){case GREATSWORD->k(t,0,0,.16,.20,.03,0,0,0);case KNIFE->k(t,0,0,.22,.10,0,0,0,0);default->0;};}
 static double advance(Family f,double t){return switch(f){case BOW,GUN->0;case RAPIER,SPEAR->k(t,0,-.08,.17,.56,.57,.50,.46,.46);case FISTS->k(t,0,0,.20,.24,.41,.42,.40,.40);default->k(t,0,-.04,.19,.35,.46,.46,.46,.46);};}
 static Vector3f shoulder(boolean left,boolean slim){return v(left?.3125:-.3125,slim?.15625:.125,0);}
 static Vector3f limited(Vector3f target,Vector3f center,float radius){var d=new Vector3f(target).sub(center);if(d.length()>radius)d.normalize(radius);if(d.length()<.37f)d.normalize(.37f);return new Vector3f(center).add(d);}
 public record Arm(Quaternionf rotation,float bend,Vector3f hand,Vector3f forward){}
 static Arm solve(Vector3f target,boolean left,boolean slim){
  var center=shoulder(left,slim);var diff=new Vector3f(target).sub(center);float lateral=(left?1:-1)*(slim?.03125f:.0625f);
  float radius=diff.length();var radial=new Vector3f(diff).normalize();var hint=v(left?.55:-.55,1,.1);
  var normal=hint.cross(radial).normalize().mul((float)Math.sqrt(1-lateral*lateral/(radius*radius))).add(new Vector3f(radial).mul(lateral/radius));
  var planar=new Vector3f(diff).sub(new Vector3f(normal).mul(lateral));float distance=Math.max(.126f,Math.min(.624f,planar.length()));var dir=planar.normalize();var side=new Vector3f(dir).cross(normal).normalize();
  float x=(.25f*.25f+distance*distance-.375f*.375f)/(2*distance);float h=(float)Math.sqrt(Math.max(0,.25f*.25f-x*x));
  var elbow=new Vector3f(dir).mul(x).add(side.mul(h));var upper=new Vector3f(elbow).normalize();var fore=new Vector3f(dir).mul(distance).sub(elbow).normalize();
  float angle=(float)Math.acos(Math.max(-1,Math.min(1,upper.dot(fore))));var z=new Vector3f(normal).cross(upper).normalize();
  Quaternionf q=new Quaternionf().setFromNormalized(new Matrix3f().setColumn(0,normal).setColumn(1,upper).setColumn(2,z));
  var end=new Vector3f(center).add(q.transform(v(lateral,.25,0))).add(new Quaternionf(q).rotateX(angle).transform(v(0,.375,0)));
  return new Arm(q,angle,end,new Quaternionf(q).rotateX(angle).transform(v(0,1,0)));
 }

 public record Frame(Matrix4f body,Quaternionf head,Arm right,Arm left,Vector3f rightLeg,Vector3f leftLeg,float rightKnee,float leftKnee,Vector3f grip,Quaternionf weapon,float scale,Vector3f tip,Vector3f stringTop,Vector3f stringBottom,Vector3f nock,double height,double feet){}
 public static Frame sample(Family family,double seconds,boolean slim){
  if(family==Family.NONE||family==Family.SWORD)return ApprovedSwordPose.sample(seconds,slim);
  double t=Math.max(0,Math.min(1.16,seconds));var b=body(family,t);
  var bodyMatrix=new Matrix4f().translation(0,.75f,0).rotate(new Quaternionf().rotationZYX(rad(b.z),rad(b.y),rad(b.x))).translate(0,-.75f,0);var inverse=new Matrix4f(bodyMatrix).invert();
    Vector3f primary=grip(family,t),right,left;Quaternionf weaponRotation;Vector3f dir=direction(family,t);float size=family==Family.SPEAR?.79f:family==Family.GREATSWORD?.78f:.78f;
    if(family==Family.BOW){var draw=freeHand(family,t);var aim=new Vector3f(primary).sub(draw);aim.y=0;if(aim.length()<.15)aim.set(0,0,-1);aim.normalize();weaponRotation=new Quaternionf().rotationTo(v(-1,0,0),aim);right=draw;left=primary;dir=aim;}
    else if(family==Family.GUN){weaponRotation=new Quaternionf().rotationX(rad(k(t,48,10,0,0,-12,4,45,48)));right=primary;left=new Vector3f(primary).add(.12f,.065f,.045f);}
    else {weaponRotation=new Quaternionf().rotationTo(v(0,1,0),dir);right=primary;left=family==Family.SPEAR?new Vector3f(primary).add(new Vector3f(dir).mul(.46f)):family==Family.GREATSWORD||family==Family.AXE?new Vector3f(primary).sub(new Vector3f(dir).mul(.23f)):freeHand(family,t);}
    boolean paired=family==Family.SPEAR||family==Family.GREATSWORD||family==Family.AXE||family==Family.GUN;
    if(paired){var separation=new Vector3f(left).sub(right);var rs=bodyMatrix.transformPosition(shoulder(false,slim));var ls=bodyMatrix.transformPosition(shoulder(true,slim));for(int iteration=0;iteration<18;iteration++){right=limited(right,rs,.57f);left=limited(new Vector3f(right).add(separation),ls,.57f);right=new Vector3f(left).sub(separation);}primary=right;left=new Vector3f(right).add(separation);}
    right=limited(right,bodyMatrix.transformPosition(shoulder(false,slim)),.58f);left=limited(left,bodyMatrix.transformPosition(shoulder(true,slim)),.58f);
    var ra=solve(inverse.transformPosition(new Vector3f(right)),false,slim);var la=solve(inverse.transformPosition(new Vector3f(left)),true,slim);
    var actualRight=bodyMatrix.transformPosition(new Vector3f(ra.hand));var actualLeft=bodyMatrix.transformPosition(new Vector3f(la.hand));
    primary=family==Family.BOW?actualLeft:actualRight;

  if(family==Family.FISTS)weaponRotation=new Quaternionf().rotationTo(v(0,1,0),bodyMatrix.transformDirection(new Vector3f(ra.forward)));
  var wm=new Matrix4f().translation(primary).rotate(weaponRotation).scale(size);
  double reach=switch(family){case GREATSWORD->33/16.;case AXE->28/16.;case SPEAR->39/16.;case RAPIER->29/16.;case KNIFE->1;case FISTS->.20;default->0;};
  var tip=wm.transformPosition(family==Family.GUN?v(0,.13,-.875):v(0,reach,0));
  var top=wm.transformPosition(v(4.55/16.,-17/16.,0));var bottom=wm.transformPosition(v(4.55/16.,17/16.,0));
  var nock=t>=.16&&t<.52?actualRight:new Vector3f(top).lerp(bottom,.5f);
  var rl=leg(family,false,t);var ll=leg(family,true,t);float rk=rad(knee(family,false,t)),lk=rad(knee(family,true,t));
  return new Frame(bodyMatrix,new Quaternionf().rotationZYX(rad(-b.z*.5),rad(-b.y*.75),rad(-b.x*.65)),ra,la,rl,ll,rk,lk,primary,weaponRotation,size,tip,top,bottom,nock,seconds<0?0:height(family,t),feet(bodyMatrix,rl,ll,rk,lk));
 }
 static double feet(Matrix4f body,Vector3f right,Vector3f left,float rk,float lk){
  double maximum=0;
  for(boolean l:new boolean[]{false,true}){
   Vector3f angles=l?left:right;float bend=l?lk:rk;
   Matrix4f m=new Matrix4f(body).translate(l?.125f:-.125f,.75f,0).rotate(new Quaternionf().rotationZYX(rad(angles.z),rad(angles.y),rad(angles.x))).translate(0,.375f,0).rotateX(bend).translate(0,-.375f,0);
   for(float x:new float[]{-.125f,.125f})for(float z:new float[]{-.125f,.125f})maximum=Math.max(maximum,m.transformPosition(new Vector3f(x,.75f,z)).y);
  }return maximum;
 }
 private ApprovedWeaponPose(){}
}
