package com.gx.velveteclipse.client.presentation;

/** Shot offsets relative to attacker -> target. Continuous dolly inside each deliberate cut. */
public final class BattleShot {
 public record Frame(double side,double height,double depth,double focus,double focusHeight,float fov,float roll){}
 private static double ease(double t){t=Math.max(0,Math.min(1,t));return t*t*(3-2*t);}
 public static Frame sample(String kind,double tick){
  if(kind.equals("skill")){
   // Front three-quarter waist-up shot; positive depth is in front of the caster.
   if(tick<45){double t=ease(tick/45);return new Frame(1.65-.15*t,1.85,3.05-.2*t,0,1.18,57,-1);}
   // Pull out on the flash to fit the caster AND the Persona behind them.
   if(tick<72){double t=ease((tick-45)/27);return new Frame(2.5+.3*t,2.5,4.4+.3*t,0,1.65,64,1);}
   if(tick<96){double t=ease((tick-72)/24);return new Frame(2.5-.5*t,1.75,-2.8,.86,1,59,0);}
   return new Frame(3,2.1,-3.4,.4,1,65,0);
  }
  if(kind.equals("enemy_melee")){double t=ease(tick/44);return new Frame(-3.4,2.05,-1.8,.5+t*.20,1,64,0);}
  if(kind.equals("enemy")){double t=ease(tick/28);return new Frame(-3.2,2,-2.8,.55+t*.25,1,62,0);}
  if(kind.equals("attack")){double t=ease(tick/44);return new Frame(2.7-t*.7,1.4+t*.8,-2.8,.55,1,61,-2+(float)t*2);}
  return new Frame(2.8,2.1,-3.2,.22,1,65,0);
 }
}
