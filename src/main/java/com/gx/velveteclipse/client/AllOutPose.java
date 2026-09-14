package com.gx.velveteclipse.client;
import dev.kosmx.playerAnim.core.util.Vec3f;
import com.gx.velveteclipse.core.battle.AllOutTimeline;
public final class AllOutPose {
 public static Vec3f rotation(String bone,double tick,boolean victory){
  double boundary=boundary(tick,victory);
  if(boundary>=0){var a=rawRotation(bone,boundary-.001,victory);var b=rawRotation(bone,tick,victory);float f=(float)AllOutTimeline.ease((tick-boundary)/8);return v(a.getX()+(b.getX()-a.getX())*f,a.getY()+(b.getY()-a.getY())*f,a.getZ()+(b.getZ()-a.getZ())*f);}
  return rawRotation(bone,tick,victory);
 }
 private static double boundary(double tick,boolean victory){for(double t:new double[]{38,victory?90:-100,victory?120:-100})if(tick>=t&&tick<t+8)return t;return -1;}
 private static Vec3f rawRotation(String bone,double tick,boolean victory){
  var phase=AllOutTimeline.phase(tick,victory);double run=Math.sin((tick-38)*.64);
  if(phase==AllOutTimeline.Phase.RUSH||phase==AllOutTimeline.Phase.CLOUD)return switch(bone){
   case "head"->v(-.18,-.10,0);case "right_arm"->v(-.4-run*.7,-.12,.17);case "left_arm"->v(-.4+run*.7,.12,-.17);
   case "right_leg"->v(run*.95,.07,.1);case "left_leg"->v(-run*.95,-.07,-.1);default->v(0,0,0);};
  if(phase==AllOutTimeline.Phase.VICTORY)return switch(bone){
   case "head"->v(.02,-.28,.10);case "body"->v(0,-.12,-.10);case "right_arm"->v(-.30,-.18,1.23);case "left_arm"->v(.10,.08,-.24);
   case "right_leg"->v(.06,.12,.06);case "left_leg"->v(-.12,-.18,-.08);default->v(0,0,0);};
  if(phase==AllOutTimeline.Phase.FINISH)return switch(bone){
   case "head"->v(-.08,-.35,.08);case "right_arm"->v(-1.36,-.58,.25);case "left_arm"->v(-.7,.4,-.4);
   case "right_leg"->v(-.3,.22,.14);case "left_leg"->v(.45,-.28,-.13);default->v(0,0,0);};
  return switch(bone){case "head"->v(-.13,-.15,.04);case "body"->v(.08,-.12,0);case "right_arm"->v(-1.12,-.5,.55);case "left_arm"->v(-.42,.32,-.38);case "right_leg"->v(.1,.12,.16);case "left_leg"->v(-.12,-.18,-.18);default->v(0,0,0);};
 }
 public static float bend(String bone,double tick,boolean victory){double boundary=boundary(tick,victory);if(boundary>=0){float a=rawBend(bone,boundary-.001,victory),b=rawBend(bone,tick,victory);return a+(b-a)*(float)AllOutTimeline.ease((tick-boundary)/8);}return rawBend(bone,tick,victory);}
 private static float rawBend(String bone,double tick,boolean victory){var phase=AllOutTimeline.phase(tick,victory);return phase==AllOutTimeline.Phase.VICTORY?(bone.equals("right_arm")?.10f:.14f):phase==AllOutTimeline.Phase.FINISH?(bone.equals("right_arm")?.36f:1.0f):(phase==AllOutTimeline.Phase.RUSH||phase==AllOutTimeline.Phase.CLOUD)?.95f:.65f;}
 private static Vec3f v(double x,double y,double z){return new Vec3f((float)x,(float)y,(float)z);}
}
