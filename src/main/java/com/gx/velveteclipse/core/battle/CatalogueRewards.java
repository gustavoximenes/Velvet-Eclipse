package com.gx.velveteclipse.core.battle;

import com.gx.velveteclipse.core.persona.*;
import com.gx.velveteclipse.equipment.EquipmentBonuses;
import com.gx.velveteclipse.forge.PlayerPersonaData;

/** Victory-only recovery and experience for inactive stock. */
public final class CatalogueRewards {
 public static void victory(PlayerPersonaData data,int xp){
  var active=data.activeInstance();var bonuses=EquipmentBonuses.NONE.withSkills(active.learnedSkills());
  int restore=bonuses.has("victory cry")?100:bonuses.has("life aid")?8:0;
  if(data.hp()>0&&restore>0)data.setResources(data.hp()+data.maxHp()*restore/100,data.sp()+data.maxSp()*restore/100);
  for(var p:data.rosterInstances())if(!p.definitionId().equals(active.definitionId())){
   var b=EquipmentBonuses.NONE.withSkills(p.learnedSkills());int percent=b.has("growth 3")?100:b.has("growth 2")?50:b.has("growth 1")?25:0;
   if(percent>0){p.addExperience(PersonaRegistry.persona(p.definitionId()).orElseThrow(),xp*percent/100);data.updatePersona(p);}
  }
 }
 private CatalogueRewards(){}
}
