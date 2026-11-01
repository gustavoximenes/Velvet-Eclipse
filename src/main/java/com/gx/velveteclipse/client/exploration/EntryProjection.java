package com.gx.velveteclipse.client.exploration;
import java.util.*;
import org.joml.Vector3f;
import static com.gx.velveteclipse.exploration.ExplorationRules.smooth;
/** Perspective dolly and polygon clipping around the arena's opening diamond. */
public final class EntryProjection {
 public static Vector3f project(double x,double y,double z,int w,int h,double tick){
  double t=tick/20.,p=smooth((t-.60)/1.15),reveal=smooth(p*2),distance=1.85+3.70*reveal;
  var direction=new Vector3f(-.66f,.13f,-.74f).normalize();var target=new Vector3f(0,(float)(1.24-.19*reveal),0);
  var eye=new Vector3f(direction).mul((float)distance).add(target);var front=new Vector3f(direction).negate();
  var right=new Vector3f(front).cross(0,1,0).normalize();var up=new Vector3f(right).cross(front);
  var v=new Vector3f((float)x,(float)y,(float)z).sub(eye);double depth=v.dot(front),f=h/(2*Math.tan(Math.toRadians(19)));
  return new Vector3f((float)(w*(.22+.10*p)+v.dot(right)*f/Math.max(.05,depth)),(float)(h*(.53-.03*p)-v.dot(up)*f/Math.max(.05,depth)),(float)depth);
 }
 public static List<double[]> outside(double[] polygon,int w,int h,double tick){
  if(polygon.length<6)return List.of();if(tick<=30.4)return List.of(polygon);
  double amount=smooth((tick-30.4)/8.6),cx=w*.66,cy=h*.48,rx=w*1.25*amount,ry=h*1.45*amount;
  if(rx<.001||ry<.001)return List.of(polygon);
  List<double[]> result=new ArrayList<>();double[] inside=polygon;
  for(int sx:new int[]{-1,1})for(int sy:new int[]{-1,1}){
   double a=sx/rx,b=sy/ry,c=-1-a*cx-b*cy;
   double[] out=clip(inside,a,b,c);if(out.length>=6)result.add(out);
   inside=clip(inside,-a,-b,-c);if(inside.length<6)return result;
  }return result;
 }
 public static double[] clip(double[] in,double a,double b,double c){
  if(in.length<6)return new double[0];List<Double> out=new ArrayList<>();
  for(int i=0;i<in.length;i+=2){int j=(i+in.length-2)%in.length;double x=in[i],y=in[i+1],px=in[j],py=in[j+1],d=a*x+b*y+c,prev=a*px+b*py+c;
   if((d>=0)!=(prev>=0)){double t=prev/(prev-d);out.add(px+(x-px)*t);out.add(py+(y-py)*t);}
   if(d>=0){out.add(x);out.add(y);}
  }return out.stream().mapToDouble(Double::doubleValue).toArray();
 }
 private EntryProjection(){}
}
