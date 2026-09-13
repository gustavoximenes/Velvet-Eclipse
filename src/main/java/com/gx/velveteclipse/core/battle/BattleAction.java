package com.gx.velveteclipse.core.battle;

import java.util.UUID;

public record BattleAction(BattleActionType type, String skillId, UUID targetId) {
   public static BattleAction allOut(){return new BattleAction(BattleActionType.ALL_OUT,"__all_out",null);}
   public static BattleAction attack(UUID var0) {
      return new BattleAction(BattleActionType.ATTACK, null, var0);
   }

   public static BattleAction skill(String var0, UUID var1) {
      return new BattleAction(BattleActionType.SKILL, var0, var1);
   }

   public static BattleAction item(String var0) {
      return new BattleAction(BattleActionType.ITEM, var0, null);
   }

   public static BattleAction guard() {
      return new BattleAction(BattleActionType.GUARD, null, null);
   }

   public static BattleAction escape() {
      return new BattleAction(BattleActionType.ESCAPE, null, null);
   }
}
