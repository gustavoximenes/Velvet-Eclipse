package com.gx.velveteclipse.client.presentation;

import java.util.*;
import java.util.function.BiFunction;
import net.minecraft.world.phys.Vec3;

/** Keep the lens inside the car. Visibility takes precedence over the outdoor shoulder offset. */
public final class TrainBattleCamera {
 public static BattleFraming.View frame(Vec3 player,List<Vec3> enemies,double fov,double aspect,BiFunction<Vec3,Vec3,Vec3> clip){
  double center=Math.rint((player.f_82479_-.5)/256)*256+.5;
  var subjects=new ArrayList<>(enemies);
  for(double y:new double[]{.05,1.95})for(double x:new double[]{-.4,.4})subjects.add(player.m_82520_(x,y,0));
  BattleFraming.View best=null;double bestScore=-Double.MAX_VALUE;
  double[] offsets=player.f_82481_>=84?new double[]{-6.4,-5.2,-4.1,-3.4,-2.6,-1.6,0,1.6,2.6,3.4,4.1,5.2,6.4}:new double[]{-4.1,-3.4,-2.6,-1.6,0,1.6,2.6,3.4,4.1};
  for(double x:offsets)for(double back:new double[]{4.8,3.6,2.4,6,7.2,9.6})for(double height:new double[]{2.6,3.4}){
   var eye=new Vec3(center+x,player.f_82480_+height,player.f_82481_-back);
   if(clip.apply(player.m_82520_(0,1.4,0),eye).m_82554_(eye)>.15)continue;
   int blocked=0;for(var p:subjects)if(clip.apply(p.f_82480_<player.f_82480_+.3?p.m_82520_(0,.25,0):p,eye).m_82554_(eye)>.22)blocked++;
   for(double lens:new double[]{fov,44,52,60,68,76})for(double anchor:new double[]{125,155,185,215}){
    var focus=BattleFraming.commandFocus(player,eye,lens,aspect,anchor);
    int cropped=0;for(var p:subjects)if(!BattleFraming.commandContains(List.of(p),eye,focus,lens,aspect))cropped++;
    double score=-blocked*1000-cropped*100-Math.abs(x-(player.f_82479_-center-2))*2-Math.abs(back-4.8)*3-(lens-fov)*.15-(height-2.6)-(anchor-125)*.15;
    if(score>bestScore){bestScore=score;best=new BattleFraming.View(eye,focus,lens);}
   }
  }
  return best!=null?best:BattleFraming.command(player,enemies,fov,aspect,clip);
 }
 private TrainBattleCamera(){}
}
