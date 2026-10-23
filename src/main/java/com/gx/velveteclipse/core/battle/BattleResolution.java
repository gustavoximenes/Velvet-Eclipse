package com.gx.velveteclipse.core.battle;

import java.util.List;

public record BattleResolution(BattleState state, boolean oneMore, List<String> log, boolean weaknessHit,
 java.util.Map<java.util.UUID,DamageOutcome> hits,boolean accepted) {
   public BattleResolution {log=List.copyOf(log);hits=java.util.Map.copyOf(hits);}
   public BattleResolution(BattleState state,boolean oneMore,List<String> log){this(state,oneMore,log,false,java.util.Map.of(),false);}
   public BattleResolution(BattleState state,boolean oneMore,List<String> log,boolean weaknessHit){this(state,oneMore,log,weaknessHit,java.util.Map.of(),false);}
   /** A weakness on an already downed victim and a critical on another do not qualify. */
   public boolean weaknessOneMore(){return accepted&&oneMore&&hits.values().stream().anyMatch(h->h.newlyDowned()&&h.affinity()==com.gx.velveteclipse.core.persona.Affinity.WEAK&&h.damage()>0&&!h.missed()&&!h.reflected()&&!h.drained());}
}
