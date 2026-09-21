package com.gx.velveteclipse.shadow;

import com.gx.velveteclipse.core.battle.BattleCombatant;
import com.gx.velveteclipse.core.persona.*;
import java.util.*;

/** Chooses only affordable attacks; a basic attack always remains available. */
public final class EnemyTactics {
 public static SkillDefinition choose(BattleCombatant actor,Random random){
  if(actor.hasAilment("RAGE"))return null;
  var skills=actor.persona().learnedSkills().stream().sorted().map(id->PersonaRegistry.skill(id).orElse(null))
   .filter(Objects::nonNull).filter(SkillDefinition::isDamageSkill)
   .filter(s->s.spCost()<=actor.sp()&&(s.hpPercentCost()==0||actor.hp()>Math.max(1,actor.maxHp()*s.hpPercentCost()/100))).toList();
  if(skills.isEmpty()||random.nextInt(5)==0)return null;
  var magic=skills.stream().filter(SkillDefinition::usesMagicStat).toList();
  var pool=!magic.isEmpty()&&random.nextInt(10)<7?magic:skills;
  return pool.get(random.nextInt(pool.size()));
 }
}
