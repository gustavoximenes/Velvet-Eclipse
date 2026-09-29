package com.gx.velveteclipse.client.exploration;
/** Visual travel heading is independent of mouse/view yaw and does not alter movement packets. */
public final class ExplorationMotion {
 public static float turn(float current,float target,float limit){float delta=(target-current)%360;if(delta>180)delta-=360;if(delta< -180)delta+=360;return current+Math.max(-limit,Math.min(limit,delta));}
 private ExplorationMotion(){}
}
