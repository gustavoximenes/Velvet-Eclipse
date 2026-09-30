package com.gx.velveteclipse.client.presentation;
import net.minecraft.world.phys.Vec3;
import java.util.*;
import java.util.function.BiFunction;

/** Fits feet and crowns, then scores unobstructed alternatives around the same subject. */
public final class BattleFraming {
 public record View(Vec3 eye,Vec3 focus,double fov){public View(Vec3 eye,Vec3 focus){this(eye,focus,36);}}
 /** Approved full-body composition: the command actor is left, enemies in the open center. */
 public static View command(Vec3 player,List<Vec3> enemies,double fov,double aspect,BiFunction<Vec3,Vec3,Vec3> clip){
  Vec3 center=Vec3.f_82478_;for(var p:enemies)center=center.m_82549_(p);center=enemies.isEmpty()?player.m_82520_(0,0,5):center.m_82490_(1./enemies.size());
  var forward=new Vec3(center.f_82479_-player.f_82479_,0,center.f_82481_-player.f_82481_);forward=forward.m_82556_()<.001?new Vec3(0,0,1):forward.m_82541_();var side=new Vec3(forward.f_82481_,0,-forward.f_82479_);
  var subjects=new ArrayList<Vec3>(enemies);for(double y:new double[]{0,1.95})for(double x:new double[]{-.4,.4})subjects.add(player.m_82549_(side.m_82490_(x)).m_82520_(0,y,0));
  View best=null;double bestScore=Double.NEGATIVE_INFINITY;
  // Terrain can change the viewing angle, but must not shrink the actor to a
  // distant miniature inside a fixed-size command menu.
  for(double scale:new double[]{1,.92,1.08,.84,1.16})for(double angle:new double[]{0,18,-18,36,-36,60,-60,85,-85,110,-110,145,-145,180}){
   double r=Math.toRadians(angle),sx=(-3.8*Math.cos(r)+4.8*Math.sin(r))*scale,sz=(-3.8*Math.sin(r)-4.8*Math.cos(r))*scale;
   var desired=player.m_82549_(side.m_82490_(sx)).m_82549_(forward.m_82490_(sz)).m_82520_(0,2.4*scale,0);
   var eye=clip.apply(player.m_82520_(0,1.1,0),desired);
   // A clipped camera needs a NEW orientation, otherwise the avatar slides across the HUD.
   double distance=eye.m_82554_(player.m_82520_(0,1.1,0));
   // In a genuinely narrow room, compensate the lens instead of zooming
   // into the avatar. Prefer clear positions at the reference distance.
   double lens=Math.min(70,Math.max(fov,Math.toDegrees(2*Math.atan(6.1*Math.tan(Math.toRadians(fov/2))/Math.max(.5,distance)))));
   for(double extra:new double[]{0,4,8,12}){
   double candidateLens=Math.min(70,Math.max(lens,fov+extra));
   var focus=commandFocus(player,eye,candidateLens,aspect);double score=0;
   for(var point:subjects){if(commandContains(List.of(point),eye,focus,candidateLens,aspect))score+=10;if(clip.apply(point.m_82520_(0,.25,0),eye).m_82554_(eye)<.22)score+=1;}
   // A clipped player is a hard composition failure, irrespective of enemy count.
   var bounds=List.of(player,player.m_82520_(0,1.95,0));
   if(!commandContains(bounds,eye,focus,candidateLens,aspect))score-=1000;
   score-=Math.abs(angle)*.07+Math.abs(scale-1)*4+Math.max(0,5.2-distance)*18+(candidateLens-fov)*.12;
   if(score>bestScore){bestScore=score;best=new View(eye,focus,candidateLens);}
   if(angle==0&&scale==1&&commandContains(subjects,eye,focus,candidateLens,aspect)&&eye.m_82554_(desired)<.15&&extra==0)return best;
   }
  }
  return best;
 }
 /** Keep the command pose at the same three-quarter angle after choosing a wall-safe shot. */
 public static float commandYaw(Vec3 player,Vec3 eye){return (float)Math.toDegrees(Math.atan2(player.f_82479_-eye.f_82479_,eye.f_82481_-player.f_82481_))-35;}
 /** Anchor the actor's waist to the same virtual 640x360 canvas as the command fan. */
 public static Vec3 commandFocus(Vec3 player,Vec3 eye,double fov,double aspect){
  double unit=Math.min(aspect/640,1./360),top=1-360*unit;
  double gx=(2*125*unit/aspect-1)*Math.tan(Math.toRadians(fov/2))*aspect;
  double gy=(1-2*(top+230*unit))*Math.tan(Math.toRadians(fov/2));
  var delta=player.m_82520_(0,.95,0).m_82546_(eye);double yaw=Math.atan2(delta.f_82479_,delta.f_82481_),pitch=Math.atan2(delta.f_82480_,Math.hypot(delta.f_82479_,delta.f_82481_));
  for(int i=0;i<24;i++){
   var front=new Vec3(Math.sin(yaw)*Math.cos(pitch),Math.sin(pitch),Math.cos(yaw)*Math.cos(pitch));
   var right=new Vec3(-Math.cos(yaw),0,Math.sin(yaw));var up=right.m_82537_(front);
   double depth=delta.m_82526_(front);
   yaw-=Math.atan2(delta.m_82526_(right),depth)-Math.atan(gx);
   pitch+=Math.atan2(delta.m_82526_(up),depth)-Math.atan(gy);
  }
  return eye.m_82520_(Math.sin(yaw)*Math.cos(pitch)*8,Math.sin(pitch)*8,Math.cos(yaw)*Math.cos(pitch)*8);
 }
 public static boolean commandContains(List<Vec3> points,Vec3 eye,Vec3 focus,double fov,double aspect){
  var back=eye.m_82546_(focus).m_82541_();var right=new Vec3(back.f_82481_,0,-back.f_82479_).m_82541_();var up=back.m_82537_(right).m_82541_();double tan=Math.tan(Math.toRadians(fov/2));
  for(var p:points){var d=p.m_82546_(eye);double z=-d.m_82526_(back);if(z<=.1)return false;double x=d.m_82526_(right)/(z*tan*aspect),y=d.m_82526_(up)/(z*tan);if(x<-.91||x>.71||y<-.88||y>.89)return false;}return true;
 }
 /** Keep the camera at the active player's shoulder; retreat only to include enemy bounds. */
 public static View shoulder(Vec3 player,List<Vec3> enemies,double fov,double aspect,BiFunction<Vec3,Vec3,Vec3> clip){
  if(enemies.isEmpty())return new View(player.m_82520_(1.1,1.8,-2),player.m_82520_(0,1.3,4));
  Vec3 focus=Vec3.f_82478_;for(var p:enemies)focus=focus.m_82549_(p);focus=focus.m_82490_(1./enemies.size());
  var forward=new Vec3(focus.f_82479_-player.f_82479_,0,focus.f_82481_-player.f_82481_).m_82541_();
  var side=new Vec3(forward.f_82481_,0,-forward.f_82479_);View best=null;double bestScore=Double.POSITIVE_INFINITY;
  for(double back:new double[]{1.8,2.3,3,4,5.5,7,9})for(double offset:new double[]{1.15,1.6,-1.15,-1.6}){
   var desired=player.m_82520_(0,1.85,0).m_82549_(side.m_82490_(offset)).m_82549_(forward.m_82490_(-back));
   var eye=clip.apply(player.m_82520_(0,1.5,0),desired);if(eye.m_82554_(desired)>.2)continue;
   if(!contains(enemies,eye,focus,fov,aspect))continue;
   boolean clear=true;for(var p:enemies)if(clip.apply(p.m_82520_(0,.25,0),eye).m_82554_(eye)>.22){clear=false;break;}
   if(!clear)continue;double score=back+Math.abs(offset)*.15+(offset<0?.45:0);
   if(score<bestScore){bestScore=score;best=new View(eye,focus);}
  }
  return best!=null?best:fit(enemies,player.m_82520_(side.f_82479_*1.15-forward.f_82479_*3,2,side.f_82481_*1.15-forward.f_82481_*3),fov,aspect,clip);
 }
 public static boolean contains(List<Vec3> points,Vec3 eye,Vec3 focus,double fov,double aspect){
  var back=eye.m_82546_(focus).m_82541_();var right=new Vec3(back.f_82481_,0,-back.f_82479_).m_82541_();var up=back.m_82537_(right).m_82541_();
  double vertical=Math.tan(Math.toRadians(fov*.5));
  for(var p:points){var v=p.m_82546_(eye);double depth=-v.m_82526_(back);if(depth<=.1||Math.abs(v.m_82526_(right))>depth*vertical*aspect*.87||Math.abs(v.m_82526_(up))>depth*vertical*.78)return false;}return true;
 }
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
