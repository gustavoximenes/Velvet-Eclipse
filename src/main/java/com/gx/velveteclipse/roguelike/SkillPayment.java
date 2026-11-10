package com.gx.velveteclipse.roguelike;
import com.gx.velveteclipse.core.persona.SkillDefinition;
import com.gx.velveteclipse.equipment.EquipmentBonuses;

/** The same integer payment is displayed by the client and charged by the server. */
public record SkillPayment(int sp,int hp) {
 public static SkillPayment of(SkillDefinition s,int maxHp,EquipmentBonuses equipment,RogueBonuses rogue){
  int sp=s.spCost()>0&&equipment.has("spell master")?Math.max(1,s.spCost()/2):s.spCost();
  int percent=s.hpPercentCost()>0&&equipment.has("arms master")?Math.max(1,s.hpPercentCost()/2):s.hpPercentCost();
  return new SkillPayment(rogue.sp(s,sp),rogue.hp(s,percent<=0?0:Math.max(1,maxHp*percent/100)));
 }
 public boolean affordable(int currentHp,int currentSp){return currentSp>=sp&&(hp==0||currentHp>hp);}
 public String label(){return hp>0?hp+" HP":sp>0?sp+" SP":"-";}
}
