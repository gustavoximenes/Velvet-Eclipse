package com.gx.velveteclipse.core.battle;
import java.util.function.DoublePredicate;
/** Conservative swept approach. A zero-length route still permits the slash animation and impact. */
public final class MeleeApproach {
 private MeleeApproach(){}
 public static double reachable(double distance,DoublePredicate clear){
  if(!Double.isFinite(distance)||distance<=0)return 0;
  distance=Math.min(distance,12);int samples=Math.max(1,(int)Math.ceil(distance/.075));double safe=0;
  for(int i=1;i<=samples;i++){double d=Math.min(distance,distance*i/samples);if(!clear.test(d))break;safe=d;}
  return safe;
 }
}
