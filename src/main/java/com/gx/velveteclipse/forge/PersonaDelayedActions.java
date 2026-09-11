package com.gx.velveteclipse.forge;

import com.gx.velveteclipse.core.battle.BattleAction;
import com.gx.velveteclipse.core.battle.BattleCombatant;
import com.gx.velveteclipse.core.battle.BattleSession;
import com.gx.velveteclipse.util.RuntimeBridge;
import java.util.List;
import java.util.UUID;

public final class PersonaDelayedActions {
   private PersonaDelayedActions() {
   }

   private static BattleCombatant firstTarget(Object var0) {
      BattleSession var1 = BattleService.current(var0);
      if (var1 != null && var1.currentActor() != null) {
         UUID var2 = RuntimeBridge.playerUuid(var0);
         if (var2 != null && var2.equals(var1.currentActor().id())) {
            List<BattleCombatant> var3 = var1.enemiesOf(var1.currentActor());
            if (var3 != null && !var3.isEmpty()) {
               for (BattleCombatant var5 : var3) {
                  if (var5 != null && var5.hp() > 0) {
                     return var5;
                  }
               }

               return null;
            } else {
               return null;
            }
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   public static boolean queueSkill(Object var0, String var1) {
      if (var0 == null || var1 == null || var1.isBlank()) {
         return false;
      } else if (firstTarget(var0) == null) {
         BattleService.sync(var0,"Aguarde um turno válido para escolher a habilidade.");
         return false;
      } else {
         var session=BattleService.current(var0);
         var target=firstTarget(var0);
         if(session==null||target==null||session.prepare(RuntimeBridge.playerUuid(var0),BattleAction.skill(var1,target.id()))==null){BattleService.sync(var0,"Habilidade indisponível: confira HP, SP e turno.");return false;}
         PersonaCinematicServer.armSkill(var0, var1);
         double[] base=RuntimeBridge.playerPosition(var0);
         com.gx.velveteclipse.network.ModNetwork.sendPresentation(var0,new com.gx.velveteclipse.network.BattleCueS2C("skill",var1,session.prepared().cutin(),base[0],base[1],base[2],base[0],base[1],base[2]));
         String var2 = PlayerPersonaData.load(var0).activePersona();
         if (!PersonaCinematicServer.interceptPulse(var0, var2)) {
            PersonaCinematicServer.clear(var0);
            return false;
         } else {
            return true;
         }
      }
   }

   public static boolean resolve(Object var0, String var1) {
      if (var0 != null && var1 != null && !var1.isBlank()) {
         BattleCombatant var2 = firstTarget(var0);
         return var2 == null ? false : BattleService.act(var0, BattleAction.skill(var1, var2.id()));
      } else {
         return false;
      }
   }
}
