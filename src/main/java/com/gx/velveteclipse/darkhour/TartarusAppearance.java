package com.gx.velveteclipse.darkhour;
/** Persistent visual version is independent from population and progression. */
public final class TartarusAppearance {
 public static boolean generated(TartarusDungeonService.State s,int f){return s.built.contains(f)&&s.progress.current(f)&&(TartarusInteriorLayout.fixedFloor(f)||s.population.initialized(f))&&(!TartarusInteriorLayout.transitionFloor(f)||s.transitions19.contains(f));}
 public static boolean ready(TartarusDungeonService.State s,int f){return generated(s,f)&&s.appearanceVersions.getOrDefault(f,0)>=TartarusArchitecture.VERSION&&!s.appearanceInProgress.contains(f);}
 private TartarusAppearance(){}
}
