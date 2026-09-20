package com.gx.velveteclipse.core.battle;

import com.gx.velveteclipse.core.persona.Affinity;
import com.gx.velveteclipse.core.persona.Element;
import com.gx.velveteclipse.core.persona.SkillDefinition;
import java.util.Random;

public final class DamageCalculator {
   private DamageCalculator() {
   }

   public static DamageOutcome resolve(BattleCombatant var0, BattleCombatant var1, SkillDefinition var2, Random var3) {
      return apply(var0,var1,roll(var0,var1,var2,var3));
   }

   /** Roll once on the authoritative server; no resources or entity state change here. */
   public static DamageOutcome roll(BattleCombatant var0, BattleCombatant var1, SkillDefinition var2, Random var3) {
      if (var3.nextInt(100) >= var2.accuracy()) {
         return new DamageOutcome(var2.element(), var1.affinity(var2.element()), 0, false, false, false, false, true);
      } else {
         int var4 = var2.usesMagicStat() ? var0.stats().magic() : var0.stats().strength();
         int var5 = Math.max(1, var1.stats().endurance());
         int var6 = Math.max(1, var2.power() * (var4 + 8) / (var5 + 8));
         var6 = Math.max(1,(int)Math.round(var6 * var0.attackMultiplier() * var0.batonMultiplier() / var1.defenseMultiplier()));
         Affinity var7 = var2.element() == Element.ALMIGHTY || var2.element() == Element.NONE ? Affinity.NORMAL : var1.affinity(var2.element());
         boolean var8 = var2.canCritical() && var3.nextInt(100) < Math.min(35, 4 + var0.stats().luck() * 2);
         if (var8) {
            var6 = (int)Math.ceil((double)var6 * 1.35);
         }

         boolean var9 = false;
         boolean var10 = false;
         int var11;
         switch (var7) {
            case WEAK:
               var11 = (int)Math.ceil((double)var6 * 1.5);
               break;
            case RESIST:
               var11 = Math.max(1, var6 / 2);
               break;
            case NULL:
               var11 = 0;
               break;
            case REPEL:
               var11 = var6;
               var9 = true;
               break;
            case DRAIN:
               var11 = var6;
               var10 = true;
               break;
            default:
               var11 = var6;
         }

         return new DamageOutcome(var2.element(),var7,var11,var8,false,var9,var10,false);
      }

   }

   public static DamageOutcome apply(BattleCombatant attacker,BattleCombatant target,DamageOutcome hit){
      boolean down=false;
      if(!hit.missed()){
         if(hit.reflected())attacker.damage(hit.damage());
         else if(hit.drained())target.heal(hit.damage());
         else {
            target.damage(hit.damage());
            if(target.alive()&&hit.damage()>0&&(hit.affinity()==Affinity.WEAK||hit.critical()))down=target.knockDown(hit.element(),hit.affinity()==Affinity.WEAK);
         }
      }
      return new DamageOutcome(hit.element(),hit.affinity(),hit.damage(),hit.critical(),down,hit.reflected(),hit.drained(),hit.missed());
   }
}
