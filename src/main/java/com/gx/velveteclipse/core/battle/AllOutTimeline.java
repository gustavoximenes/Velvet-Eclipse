package com.gx.velveteclipse.core.battle;
public final class AllOutTimeline {
 public static final int IMPACT=84,RETURN=104,VICTORY_END=166;
 public enum Phase { WIPE, LINEUP, RUSH, CLOUD, FINISH, VICTORY, RETURN, END }
 public static int duration(boolean victory){return victory?VICTORY_END:RETURN;}
 public static Phase phase(double tick,boolean victory){
  if(tick<0||tick>=duration(victory))return Phase.END;
  if(tick<12)return Phase.WIPE;if(tick<38)return Phase.LINEUP;if(tick<62)return Phase.RUSH;if(tick<90)return Phase.CLOUD;
  if(!victory)return Phase.RETURN;if(tick<120)return Phase.FINISH;if(tick<154)return Phase.VICTORY;return Phase.RETURN;
 }
 public static double ease(double value){double t=Math.max(0,Math.min(1,value));return t*t*(3-2*t);}
}
