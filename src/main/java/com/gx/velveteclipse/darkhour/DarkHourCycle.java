package com.gx.velveteclipse.darkhour;

/** Minecraft's 24,000-tick clock remains authoritative, including /time and gamerules. */
public final class DarkHourCycle {
 public static final int DAY=24000,START=18000,WARNING=17800;
 private DarkHourCycle(){}
 public static int time(long dayTime){return (int)Math.floorMod(dayTime,DAY);}
 public static boolean active(long dayTime){return time(dayTime)>=START;}
 public static boolean warning(long dayTime){int t=time(dayTime);return t>=WARNING&&t<START;}
 public static double approach(long dayTime){return Math.max(0,Math.min(1,(time(dayTime)-WARNING)/(double)(START-WARNING)));}
}
