package com.gx.velveteclipse.client.presentation;
import org.joml.*;
import java.lang.Math;
import static com.gx.velveteclipse.client.presentation.ApprovedWeaponPose.*;
final class ApprovedSwordPose {
 static double ease(double t){t=Math.max(0,Math.min(1,t));return t*t*(3-2*t);}
 static final double[] TIMES={0,.14,.27,.36,.46,.58,.72,.92,1.16};
 static double key(double t,double... v){if(t<0)return v[0];for(int i=0;i<TIMES.length-1;i++)if(t<=TIMES[i+1])return v[i]+(v[i+1]-v[i])*ease((t-TIMES[i])/(TIMES[i+1]-TIMES[i]));return v[v.length-1];}
 static float rad(double d){return (float)Math.toRadians(d);}
 static double[] rotation(String part,double t){return switch(part){
  case "head" -> new double[]{key(t,0,7,-6,-4,0,4,7,0,0),key(t,-10,-12,-23,-8,22,24,8,-10,-10),0};
  case "body" -> new double[]{key(t,2,12,-8,-7,6,14,20,4,2),key(t,0,-32,-55,-22,42,64,25,0,0),key(t,-2,4,-9,-7,7,10,0,-2,-2)};
  case "right_arm" -> new double[]{key(t,8,-22,-82,-92,-87,-70,-30,7,8),key(t,-12,-60,-74,-30,60,88,18,-12,-12),key(t,8,30,44,32,-17,-31,8,8,8)};
  case "left_arm" -> new double[]{key(t,-4,-22,-45,-40,8,23,14,-4,-4),key(t,8,15,16,0,-18,-25,5,8,8),key(t,-6,-18,-29,-34,-22,-19,-8,-6,-6)};
  case "right_leg" -> new double[]{key(t,5,25,22,16,12,5,18,5,5),0,key(t,3,7,8,7,4,4,5,3,3)};
  default -> new double[]{key(t,-6,12,-48,-52,-30,-6,28,-6,-6),0,key(t,-3,-7,-7,-7,-4,-4,-4,-3,-3)};
 };}
 static double bend(String part,double t){return switch(part){
  case "right_arm" -> key(t,-10,-52,-62,-38,-8,-14,-23,-10,-10);
  case "left_arm" -> key(t,-6,-16,-28,-38,-24,-16,-12,-6,-6);
  case "right_leg" -> key(t,0,30,38,32,28,18,34,0,0);
  default -> key(t,0,26,55,68,53,12,40,0,0);
 };}
 static double height(double t){return key(t,0,0,.20,.42,.32,.08,0,0,0);}
 static double[] run(String name,double phase){return switch(name){
  case "body" -> new double[]{10,Math.sin(phase)*5,Math.cos(phase)*2};
  case "head" -> new double[]{-7,0,0};
  case "right_arm" -> new double[]{8+Math.sin(phase)*13,-12,10};
  case "left_arm" -> new double[]{-Math.sin(phase)*32,8,-8};
  case "right_leg" -> new double[]{Math.sin(phase)*36,0,3};
  default -> new double[]{-Math.sin(phase)*36,0,-3};
 };}
 static double runBend(String name,double phase){return name.endsWith("arm")?-22:Math.max(0,Math.sin(phase+(name.equals("right_leg")?0:Math.PI)))*48;}

 static Quaternionf q(double[] a){return new Quaternionf().rotationZYX(rad(a[2]),rad(a[1]),rad(a[0]));}
 static Arm arm(boolean left,double t,boolean slim){
  var rotation=q(rotation(left?"left_arm":"right_arm",t));float angle=rad(bend(left?"left_arm":"right_arm",t));
  var hand=shoulder(left,slim).add(rotation.transform(v((left?1:-1)*(slim?.03125:.0625),.25,0))).add(new Quaternionf(rotation).rotateX(angle).transform(v(0,.375,0)));
  return new Arm(rotation,angle,hand,new Quaternionf(rotation).rotateX(angle).transform(v(0,1,0)));
 }
 static Frame sample(double seconds,boolean slim){
  double t=Math.max(0,Math.min(1.16,seconds));var b=rotation("body",t);var body=new Matrix4f().translation(0,.75f,0).rotate(q(b)).translate(0,-.75f,0);
  var ra=arm(false,t,slim);var la=arm(true,t,slim);var grip=body.transformPosition(new Vector3f(ra.hand()));
  double az=key(t,-28,-95,-100,-55,55,110,85,-28,-28)+b[1],vertical=key(t,.38,-.75,-.38,-.05,-.03,.06,.40,.38,.38);
  var direction=v(Math.sin(Math.toRadians(az))*Math.sqrt(1-vertical*vertical),vertical,-Math.cos(Math.toRadians(az))*Math.sqrt(1-vertical*vertical));
  var wr=q(b).mul(new Quaternionf().rotationTo(v(0,1,0),direction));var tip=new Matrix4f().translation(grip).rotate(wr).scale(.82f).transformPosition(v(0,1.625,0));
  var r=rotation("right_leg",t);var l=rotation("left_leg",t);var rl=v(r[0],r[1],r[2]);var ll=v(l[0],l[1],l[2]);float rk=rad(bend("right_leg",t)),lk=rad(bend("left_leg",t));
  return new Frame(body,q(rotation("head",t)),ra,la,rl,ll,rk,lk,grip,wr,.82f,tip,v(0,0,0),v(0,0,0),grip,seconds<0?0:height(t),feet(body,rl,ll,rk,lk));
 }
}
