package com.gx.velveteclipse.core.battle;

import java.util.List;

public record BattleResolution(BattleState state, boolean oneMore, List<String> log, boolean weaknessHit) {
   public BattleResolution(BattleState state,boolean oneMore,List<String> log){this(state,oneMore,log,false);}
}
