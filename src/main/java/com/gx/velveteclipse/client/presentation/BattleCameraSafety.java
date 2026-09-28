package com.gx.velveteclipse.client.presentation;
import net.minecraft.world.phys.Vec3;
import java.util.function.BiFunction;

/** Searches clear alternate angles instead of letting a wall push a shot inside the head. */
public final class BattleCameraSafety {
 public static Vec3 choose(Vec3 focus,Vec3 desired,BiFunction<Vec3,Vec3,Vec3> clip){
  Vec3 best=clip.apply(focus,desired);double wanted=desired.m_82554_(focus);
  if(best.m_82554_(focus)>=Math.min(2.4,wanted*.85))return best;
  Vec3 offset=desired.m_82546_(focus);double score=best.m_82554_(focus);
  for(double angle:new double[]{45,-45,90,-90,135,-135,180}){
   double a=Math.toRadians(angle),x=offset.f_82479_*Math.cos(a)-offset.f_82481_*Math.sin(a),z=offset.f_82479_*Math.sin(a)+offset.f_82481_*Math.cos(a);
   Vec3 p=clip.apply(focus,focus.m_82520_(x,Math.max(.45,offset.f_82480_),z));double d=p.m_82554_(focus);
   if(d>score+.05){best=p;score=d;}if(d>=Math.min(2.4,wanted*.85))break;
  }
  return best;
 }
}
