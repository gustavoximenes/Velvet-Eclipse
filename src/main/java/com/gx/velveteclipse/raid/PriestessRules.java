package com.gx.velveteclipse.raid;
import com.gx.velveteclipse.core.persona.Element;
import java.util.*;
/** Pure server rules: elapsed time is monotonic, phase penalties occur only once. */
public final class PriestessRules {
 public static final int DEFAULT_CAPACITY=5,MAX_CAPACITY=12;
 private static final Element[] TYPES={Element.FIRE,Element.ICE,Element.ELECTRIC,Element.WIND,Element.LIGHT,Element.DARK,Element.PSY};
 private final long start;private long penalty;private Long stopped;private int phase,turn;private Element weakness=Element.FIRE;
 public PriestessRules(long now){start=now;}
 public Element weakness(){return weakness;}public int phase(){return phase;}
 public boolean iceTurn(){return ++turn%2==0;}
 public long remaining(long now){return Math.max(0,300_000-Math.max(0,(stopped==null?now:stopped)-start)-penalty);}
 public void stop(long now){if(stopped==null)stopped=now;}
 public int update(int hp,int maxHp,Random random){
  if(hp<=0)return 0;int lost=0;
  while(phase<3&&(long)hp*100<=(long)maxHp*(75-phase*25)){
   int seconds=++phase==3?60:30;penalty+=seconds*1000L;lost+=seconds;
   var options=Arrays.stream(TYPES).filter(t->t!=weakness).toList();weakness=options.get(random.nextInt(options.size()));
  }return lost;
 }
 public static int bossHp(int players){return 500+150*Math.max(1,players);}
}
