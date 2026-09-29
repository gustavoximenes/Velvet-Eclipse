package com.gx.velveteclipse.core.battle;
/** Same clock for server displacement, client pose and hit effects. */
public final class MeleeTimeline {
 public static final int IMPACT=19,END=44;
 private static double ease(double x){x=Math.max(0,Math.min(1,x));return x*x*(3-2*x);}
 public static double progress(double t){return t<=12?ease((t-2)/10):t<30?1:1-ease((t-30)/14);}
 public static double slash(double t){return t>=12&&t<=30?t-12:-1;}
}
