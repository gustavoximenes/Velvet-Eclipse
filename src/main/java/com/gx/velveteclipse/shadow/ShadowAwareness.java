package com.gx.velveteclipse.shadow;
/** Tick-based perception. Visibility must remain continuous for two full seconds. */
public final class ShadowAwareness {
 public enum Phase { IDLE, SEARCH, NOTICE, CHASE, ATTACK }
 public Phase phase=Phase.IDLE;public int ticks,seen,lost,cooldown;
 public void reset(){phase=Phase.IDLE;ticks=seen=lost=0;cooldown=60;}
 public void update(boolean visible,boolean close,boolean validTarget){
  ticks++;if(cooldown>0)cooldown--;
  if(phase==Phase.ATTACK)return;
  if(phase==Phase.CHASE){if(validTarget){if(visible)lost=0;else lost++;if(lost<=60)return;}phase=Phase.SEARCH;ticks=seen=0;return;}
  if(visible){if(phase!=Phase.NOTICE){phase=Phase.NOTICE;ticks=0;seen=0;}if(++seen>=40){phase=Phase.CHASE;ticks=lost=0;}return;}
  seen=0;if(phase==Phase.NOTICE){phase=Phase.SEARCH;ticks=0;}
  if(phase==Phase.IDLE&&close&&cooldown==0){phase=Phase.SEARCH;ticks=0;}
  if(phase==Phase.SEARCH&&ticks>=60)reset();
 }
 public void attack(){phase=Phase.ATTACK;ticks=0;}
 public static double frontDot(double yaw,double dx,double dz){double d=Math.hypot(dx,dz);return d<.001?1:(-Math.sin(Math.toRadians(yaw))*dx+Math.cos(Math.toRadians(yaw))*dz)/d;}
 public static boolean front(double yaw,double dx,double dz){return frontDot(yaw,dx,dz)>=.5;}
 public static boolean behind(double yaw,double dx,double dz){return frontDot(yaw,dx,dz)<=-.5;}
}
