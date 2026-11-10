package com.gx.velveteclipse.core.battle;
import com.gx.velveteclipse.core.persona.*;
import java.util.*;

/** Shared mechanics for the complete learnable Reload catalogue. */
public final class SkillEffects {
 public static boolean supported(SkillDefinition skill){return !skill.isPassive()&&(ReloadCatalogue.info(skill.id())!=null||SupportEffects.supported(skill.id())||skill.element()==Element.HEALING);}
 public static boolean revival(SkillDefinition s){var i=s==null?null:ReloadCatalogue.info(s.id());return i!=null&&i.effect().equals("FMTRecarm");}
 public static String apply(SkillDefinition s,BattleCombatant actor,BattleCombatant target,Random rng){
  var i=ReloadCatalogue.info(s.id());boolean waiting=actor!=target;int before=target.hp();
  if(i==null){if(s.element()==Element.HEALING)target.heal(actor.rogue().heal(s.power()));else {String result=SupportEffects.apply(s.id(),actor,target);target.extendDebuff(s.id(),actor.rogue().debuffTurns());return result;}}
  else switch(i.effect()){
   case "FMTPersonaKaja"->{int stage=s.id().endsWith("kaja")?1:-1;switch(i.detail()){case "attack"->target.attackStatus(stage,waiting);case "defense"->target.defenseStatus(stage,waiting);default->target.accuracyStatus(stage,waiting);}}
   case "FMTElemCharge"->target.chargeUp(i.detail().equals("magic"));
   case "Next $2 attack x$1 dmg and crit up"->{target.chargeUp(false);target.criticalBuff(15);}
   case "FMTElemKarn"->target.wall(!i.detail().equals("phys"));
   case "FMTElemBreak"->target.breakElement(element(i.detail()));
   case "$2 +$1% for 3 turns"->target.criticalBuff((int)i.value());
   case "FMTFoulBreathN"->target.foul();
   case "FMTCureAilment"->{if(s.id().equals("dekaja"))target.clearStages(true);else if(s.id().equals("dekunda"))target.clearStages(false);else{target.cure(i.detail().toUpperCase(Locale.ROOT).replace(" ",""));if(i.detail().contains("Down"))target.setDowned(false);}}
   case "FMTRecarm"->{if(!target.alive())target.heal(actor.rogue().heal(Math.max(1,target.maxHp()*(int)i.value()/100)));target.setDowned(false);}
   case "$1% $2, user's HP reduced to 1"->target.heal(actor.rogue().heal(target.maxHp()));
   case "$1% $2 + Sukukaja"->{target.heal(actor.rogue().heal(target.maxHp()/2));target.accuracyStatus(1,waiting);}
   case "FMTExact"->{if(i.detail().equals("HP restore"))target.heal(actor.rogue().heal(target.maxHp()*(int)i.value()/100));else inflict(actor,target,i.detail(),i.value(),rng);}
   case "FMTBase"->{switch(s.id()){
    case "heat_riser"->{target.attackStatus(1,waiting);target.defenseStatus(1,waiting);target.accuracyStatus(1,waiting);}
    case "debilitate"->{target.attackStatus(-1,waiting);target.defenseStatus(-1,waiting);target.accuracyStatus(-1,waiting);}
    case "best_friends"->{target.chargeUp(false);target.chargeUp(true);}
    case "salvation"->{target.heal(actor.rogue().heal(target.maxHp()));target.cure("ALL");target.setDowned(false);}
    case "amrita_shower","amrita_drop"->{target.cure("ALL");target.setDowned(false);}
    default->target.heal(actor.rogue().heal((int)Math.round(s.power()*(actor.equipment().has("divine grace")?1.5:1))));
   }}
   default->{if(s.element()==Element.HEALING)target.heal(actor.rogue().heal(s.power()));else throw new IllegalArgumentException("Unimplemented active effect "+s.id()+" / "+i.effect());}
  }
  if(actor.side()!=target.side())target.extendDebuff(s.id(),actor.rogue().debuffTurns());
  return actor.name()+" usou "+s.displayName()+" em "+target.name()+"."+(target.hp()>before?" +"+(target.hp()-before)+" HP.":!target.ailment().isEmpty()?" "+target.ailment():"");
 }
 public static void afterHit(SkillDefinition s,BattleCombatant actor,BattleCombatant target,DamageOutcome hit,Random rng){
  if(hit.missed()||hit.reflected()||hit.drained()||hit.damage()<=0)return;var i=ReloadCatalogue.info(s.id());
  if(s.id().equals("life_drain"))actor.heal(hit.damage());
  if(!target.alive())return;
  if(s.id().equals("dream_needle")){inflict(actor,target,"SLEEP",40,rng);return;}
  if(i!=null&&(i.effect().equals("FMTExact")||i.effect().contains("ignores resistances"))&&!i.detail().equals("-")&&!i.detail().equals("Instakill"))inflict(actor,target,i.detail(),i.value(),rng);
  else if(i==null&&(s.element()==Element.ICE||s.element()==Element.ELECTRIC))inflict(actor,target,s.element()==Element.ICE?"FREEZE":"SHOCK",10,rng);
 }
 public static void inflict(BattleCombatant actor,BattleCombatant target,String kind,double chance,Random rng){
  if(target.knockdownImmune())return;
  if(kind.equals("ailment"))kind=List.of("POISON","CHARM","CONFUSE","FEAR","RAGE","DISTRESS").get(rng.nextInt(6));
  kind=kind.toUpperCase(Locale.ROOT);if(kind.contains(",")){var types=kind.split(",");kind=types[rng.nextInt(types.length)].trim();}
  chance=(chance+actor.equipment().ailmentBoost(kind))*target.ailmentVulnerability()/(target.equipment().resistant(kind)?2:1);
  if(rng.nextDouble()*100<chance){if(kind.equals("DOWN"))target.knockDown(Element.NONE,false);else target.afflict(kind);}
 }
 public static Element element(String name){return switch(name){case "Strike"->Element.PHYSICAL;case "Elec"->Element.ELECTRIC;default->Element.valueOf(name.toUpperCase(Locale.ROOT));};}
 public static double conditional(SkillDefinition s,BattleCombatant a,BattleCombatant t){var i=ReloadCatalogue.info(s.id());if(i==null)return 1;return switch(i.detail()){case "on Downed foes"->t.downed()?1.5:1;case "during Full Moon"->Math.floorMod(a.moonDay(),28)==14?1.75:1;case "during New Moon"->Math.floorMod(a.moonDay(),28)==0?1.75:1;default->1;};}
 private SkillEffects(){}
}
