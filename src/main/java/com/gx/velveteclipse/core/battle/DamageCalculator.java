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
      var info=com.gx.velveteclipse.core.persona.ReloadCatalogue.info(var2.id());
      if(info!=null&&(info.detail().equals("Instakill")||info.effect().equals("FMTInstakillWhen")||info.effect().equals("FMTFracDamage")||info.effect().equals("FMTPersonaLifeDrainN")))return special(var0,var1,var2,info,var3);
      if (var3.nextInt(100) >= Math.max(5,(var1.equipment().has("firm stance")?100:var2.accuracy()*var0.accuracyMultiplier()/var1.accuracyMultiplier()*(1-Math.min(.8,var1.equipment().evade(var2.element())+var1.extraEvasion(var2.element()))))*(var0.hasAilment("DISTRESS,CONFUSE")?.6:1))) {
         return new DamageOutcome(var2.element(), var1.affinity(var2.element()), 0, false, false, false, false, true);
      } else {
         int var4 = var2.usesMagicStat() ? var0.stats().magic() : var0.stats().strength();
         int var5 = Math.max(1, var1.stats().endurance());
         int var6 = Math.max(1, var2.power() * (var4 + 8) / (var5 + 8));
         if(info!=null){int hits=info.minHits()+var3.nextInt(info.maxHits()-info.minHits()+1);var6*=hits;}
         var6=(int)Math.round(var6*var0.skillDamage(var2)*SkillEffects.conditional(var2,var0,var1));
         var6 = Math.max(1,(int)Math.round(var6 * var0.equipment().damage(var2.element(),var2.usesMagicStat()) * var0.charge(var2.usesMagicStat()) * var1.equipment().reduction(var2.element()) * (var1.downed()&&var0.equipment().has("dmg on downed enemy")?1.3:1) * (var2.id().equals("__all_out")&&var0.equipment().has("all-out attack dmg up")?1.3:1) * var0.attackMultiplier() * var0.batonMultiplier() / var1.defenseMultiplier()));
         if(var2.id().equals("armageddon"))var6=9999;
         var6=Math.max(1,(int)Math.round(var6*var0.encounterDamage()*var0.rogue().damage(var2)));
         Affinity var7 = var2.element() == Element.ALMIGHTY || var2.element() == Element.NONE ? Affinity.NORMAL : var1.affinity(var2.element());
         if(info!=null&&info.effect().contains("ignores resistances"))var7=Affinity.NORMAL;
         if(var1.reflects(var2.element()))var7=Affinity.REPEL;
         if(var2.id().equals("__basic_attack")&&var0.equipment().has("ignore enemy resistances")&&var7==Affinity.RESIST)var7=Affinity.NORMAL;
         if(com.gx.velveteclipse.equipment.EquipmentBonuses.physical(var2.element())&&var1.equipment().counter()>0&&var3.nextInt(100)<var1.equipment().counter())var7=Affinity.REPEL;
         boolean var8 = var2.canCritical() && var3.nextInt(100) < Math.min(60,(4 + var0.stats().luck()*2 + var0.equipment().critical()+var0.openingCritical()+var0.passiveCritical())*(var0.equipment().has("apt pupil")?2:1)/(var1.equipment().has("sharp student")?2:1));
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
         if(hit.effect().equals("sp_drain")){attacker.restoreSp(target.drainSp(hit.damage()));}
         else if(hit.effect().startsWith("instant")&&hit.damage()>0&&!hit.drained()){
            var victim=hit.reflected()?attacker:target;victim.instantDamage(hit.effect().equals("instant_guard"));
            down=!hit.reflected()&&!victim.alive()&&!victim.knockdownImmune()&&hit.affinity()==Affinity.WEAK;
            if(hit.reflected())target.useWall(hit.element());
         }
         else if(hit.reflected()){target.useWall(hit.element());attacker.elementalDamage(hit.damage(),hit.element());}
         else if(hit.drained())target.heal(hit.damage());
         else {
            boolean eligible=target.alive()&&!target.downed()&&!target.knockdownImmune()&&hit.damage()>0&&(hit.affinity()==Affinity.WEAK||hit.critical());
            target.elementalDamage(hit.damage(),hit.element());
            if(target.alive()&&hit.damage()>0&&(hit.affinity()==Affinity.WEAK||hit.critical()))down=target.knockDown(hit.element(),hit.affinity()==Affinity.WEAK);
            else if(!target.alive())down=eligible; // A lethal weakness/critical hit still earns 1 More; no corpse knockdown.
         }
      }
      int reported=hit.damage();
      if(reported>0&&!hit.missed()&&!hit.drained()&&!hit.effect().startsWith("instant")&&!hit.effect().equals("sp_drain"))
         reported=Math.max(1,(int)Math.round(reported*(hit.reflected()?attacker:target).rogue().incoming()));
      return new DamageOutcome(hit.element(),hit.affinity(),reported,hit.critical(),down,hit.reflected(),hit.drained(),hit.missed(),hit.effect());
   }
   private static DamageOutcome special(BattleCombatant a,BattleCombatant t,SkillDefinition s,com.gx.velveteclipse.core.persona.ReloadCatalogue.SkillInfo i,Random r){
      var affinity=s.element()==Element.ALMIGHTY?Affinity.NORMAL:t.affinity(s.element());boolean immune=t.knockdownImmune()&&!i.effect().equals("FMTPersonaLifeDrainN");
      if(t.reflects(s.element()))affinity=Affinity.REPEL;
      String effect=s.id().equals("spirit_drain")?"sp_drain":i.detail().equals("Instakill")||i.effect().equals("FMTInstakillWhen")?"instant":"";
      int damage=0;boolean miss=false;
      if(i.effect().equals("FMTPersonaLifeDrainN"))damage=Math.min((int)(i.value()*(a.equipment().has("drain ability")?2:1)),s.id().equals("spirit_drain")?t.sp():t.hp());
      else if(!immune){
         if(i.effect().equals("FMTFracDamage"))damage=Math.max(1,t.hp()*(int)i.value()/100);
         else{double chance=i.value()*(affinity==Affinity.WEAK?1.5:affinity==Affinity.RESIST?.5:1)*(a.equipment().has(s.element()==Element.LIGHT?"hama boost":"mudo boost")?1.5:1);chance*=Math.min(1,a.encounterDamage());if(i.effect().equals("FMTInstakillWhen")&&!t.hasAilment("FEAR"))chance=0;miss=r.nextDouble()*100>=chance;if(!miss)damage=t.hp()*3;}
      }
      if(!effect.equals("instant")&&damage>0)damage=Math.max(1,(int)Math.round(damage*a.encounterDamage()*a.rogue().damage(s)));
      if(effect.equals("instant")&&damage>0&&a.gentleInstant()&&t.side()==BattleSide.PLAYERS&&affinity!=Affinity.REPEL&&affinity!=Affinity.DRAIN){
         effect="";damage=Math.max(0,t.hp()-1);
      }
      if(immune||affinity==Affinity.NULL)damage=0;
      if(effect.equals("instant")&&!miss&&damage>0&&(affinity==Affinity.REPEL?a:t).survivesInstant(s.element(),r))effect="instant_guard";
      return new DamageOutcome(s.element(),immune?Affinity.NULL:affinity,damage,false,false,affinity==Affinity.REPEL,affinity==Affinity.DRAIN,miss,effect);
   }
}
