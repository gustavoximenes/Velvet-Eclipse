package com.gx.velveteclipse.client.presentation;
import net.minecraft.world.phys.Vec3;
import java.util.*;
import java.util.function.BiFunction;

/** Fits feet and crowns, then scores unobstructed alternatives around the same subject. */
public final class BattleFraming {
 public record View(Vec3 eye,Vec3 focus){}
 public static double distance(List<Vec3> points,Vec3 focus,Vec3 outward,double fov,double aspect){
  var back=outward.m_82541_();var right=new Vec3(back.f_82481_,0,-back.f_82479_).m_82541_();
  var up=back.m_82537_(right).m_82541_();double vertical=Math.tan(Math.toRadians(fov*.5))*.69,horizontal=vertical*Math.max(.65,aspect)*.90,d=4;
  for(var p:points){var v=p.m_82546_(focus);double forward=v.m_82526_(back);d=Math.max(d,forward+Math.max(Math.abs(v.m_82526_(right))/horizontal,Math.abs(v.m_82526_(up))/vertical));}
  return d+.35;
 }
 public static View fit(List<Vec3> points,Vec3 preferred,double fov,double aspect,BiFunction<Vec3,Vec3,Vec3> clip){
  if(points.isEmpty())return new View(preferred,Vec3.f_82478_);
  Vec3 center=Vec3.f_82478_;for(var p:points)center=center.m_82549_(p);center=center.m_82490_(1./points.size());
  Vec3 outward=preferred.m_82546_(center);if(outward.m_82556_()<.01)outward=new Vec3(2,2,-5);
  View best=null;double bestScore=Double.NEGATIVE_INFINITY;
  for(double angle:new double[]{0,20,-20,40,-40,70,-70,110,-110,180}){
   double r=Math.toRadians(angle);var back=new Vec3(outward.f_82479_*Math.cos(r)-outward.f_82481_*Math.sin(r),Math.max(1,outward.f_82480_),outward.f_82479_*Math.sin(r)+outward.f_82481_*Math.cos(r)).m_82541_();
   double required=distance(points,center,back,fov,aspect);var desired=center.m_82549_(back.m_82490_(required));var eye=clip.apply(center,desired);
   double clear=0;for(var p:points)if(clip.apply(p.m_82520_(0,.25,0),eye).m_82554_(eye)<.22)clear++;
   double score=clear*10-Math.abs(angle)*.025-Math.max(0,required-eye.m_82554_(center))*12;
   if(score>bestScore){bestScore=score;best=new View(eye,center);}
   if(angle==0&&clear==points.size()&&eye.m_82554_(desired)<.1)break;
  }
  return best;
 }
 private BattleFraming(){}
}
