package com.gx.velveteclipse.exploration;
import com.gx.velveteclipse.core.battle.EncounterOpening;
/** Shared presentation timing, in game ticks. Formation exists before its reveal. */
public final class EntryTimeline {
 public static EncounterOpening opening(int id){return id>=0&&id<EncounterOpening.values().length?EncounterOpening.values()[id]:EncounterOpening.NEUTRAL;}
 public static int duration(EncounterOpening o){return o==EncounterOpening.AMBUSH?62:74;}
 public static boolean running(EncounterOpening o,double tick){return o!=EncounterOpening.AMBUSH&&tick>=12&&tick<39;}
 public static double banner(EncounterOpening o,double tick){
  if(o!=EncounterOpening.ADVANTAGE&&o!=EncounterOpening.AMBUSH)return 0;
  double begin=o==EncounterOpening.AMBUSH?21:43.2,end=o==EncounterOpening.AMBUSH?61:73;
  return ExplorationRules.smooth((tick-begin)/3.6)*(1-ExplorationRules.smooth((tick-end+6)/6));
 }
 private EntryTimeline(){}
}
