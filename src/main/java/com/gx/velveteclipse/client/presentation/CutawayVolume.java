package com.gx.velveteclipse.client.presentation;
import java.util.*;
import net.minecraft.world.phys.Vec3;

/** Immutable world-space sight corridors; safe to query from chunk compilation workers. */
public record CutawayVolume(Vec3 eye,List<Vec3> subjects,double floor) {
 public CutawayVolume { subjects=List.copyOf(subjects); }
 public boolean intersects(double x0,double y0,double z0,double x1,double y1,double z1){
  if(y1<=floor)return false;
  // Padding clears the near plane and the body silhouette, not only the center ray.
  double pad=.7;double[] lo={x0-pad,Math.max(floor,y0)-pad,z0-pad},hi={x1+pad,y1+pad,z1+pad};
  for(var subject:subjects){
   double[] start={eye.f_82479_,eye.f_82480_,eye.f_82481_},end={subject.f_82479_,subject.f_82480_,subject.f_82481_};
   double enter=0,exit=.99;
   for(int axis=0;axis<3&&enter<=exit;axis++){
    double d=end[axis]-start[axis];
    if(Math.abs(d)<1e-9){if(start[axis]<lo[axis]||start[axis]>hi[axis]){enter=2;break;}}
    else {double a=(lo[axis]-start[axis])/d,b=(hi[axis]-start[axis])/d;enter=Math.max(enter,Math.min(a,b));exit=Math.min(exit,Math.max(a,b));}
   }
   if(enter<=exit)return true;
  }
  return false;
 }
 public double[] bounds(){
  double[] b={eye.f_82479_,Math.min(floor,eye.f_82480_),eye.f_82481_,eye.f_82479_,eye.f_82480_,eye.f_82481_};
  for(var p:subjects){b[0]=Math.min(b[0],p.f_82479_);b[1]=Math.min(b[1],p.f_82480_);b[2]=Math.min(b[2],p.f_82481_);b[3]=Math.max(b[3],p.f_82479_);b[4]=Math.max(b[4],p.f_82480_);b[5]=Math.max(b[5],p.f_82481_);}
  for(int i=0;i<3;i++){b[i]-=1;b[i+3]+=1;}return b;
 }
}
