package com.gx.velveteclipse.roguelike;
import java.util.*;
import com.gx.velveteclipse.core.persona.*;

/** Immutable encounter snapshot. Items, All-Out and instant-kill mechanics are separate. */
public record RogueBonuses(RogueCatalog.Focus focus,Set<String> active) {
 public static final RogueBonuses NONE=new RogueBonuses(RogueCatalog.Focus.NONE,Set.of());
 public RogueBonuses {focus=focus==null?RogueCatalog.Focus.NONE:focus;active=active==null?Set.of():Set.copyOf(active);}
 public boolean attack(SkillDefinition s){return s!=null&&s.isDamageSkill()&&!s.id().equals("__all_out")&&s.element()!=Element.NONE;}
 private double sum(java.util.function.ToDoubleFunction<RogueCatalog.Card> f){return active.stream().map(RogueCatalog::card).filter(Objects::nonNull).mapToDouble(f).sum();}
 public double damage(SkillDefinition s){
  if(!attack(s))return 1;
  boolean magic=s.usesMagicStat(),basic=s.id().equals("__basic_attack");
  double role=focus==RogueCatalog.Focus.WEAKNESS?.5:!basic&&(magic&&focus==RogueCatalog.Focus.DPS_MAGIC||!magic&&focus==RogueCatalog.Focus.DPS_PHYSICAL)?1.10:1;
  return role*Math.max(.1,1+sum(c->basic&&(c.id().equals("strength")||c.id().equals("magician"))?0:magic?c.magic():c.physical()));
 }
 public double healing(){return (focus==RogueCatalog.Focus.SUPPORT_HEAL?1.15:1)*Math.max(.1,1+sum(RogueCatalog.Card::heal));}
 public int heal(int base){return base<=0?0:Math.max(1,(int)Math.round(base*healing()));}
 public int debuffTurns(){return (focus==RogueCatalog.Focus.SUPPORT_DEBUFF?1:0)+(int)sum(c->c.debuff());}
 public double incoming(){return Math.max(.2,1+sum(RogueCatalog.Card::incoming));}
 public boolean oneMore(){return active.stream().map(RogueCatalog::card).filter(Objects::nonNull).noneMatch(RogueCatalog.Card::noOneMore);}
 public int sp(SkillDefinition skill,int equipmentCost){return attack(skill)?discount(equipmentCost,(focus==RogueCatalog.Focus.WEAKNESS?.5:1)*Math.max(.1,1+sum(RogueCatalog.Card::sp))):equipmentCost;}
 public int hp(SkillDefinition skill,int equipmentPayment){return attack(skill)&&focus==RogueCatalog.Focus.WEAKNESS?discount(equipmentPayment,.5):equipmentPayment;}
 private static int discount(int base,double factor){return base<=0?0:Math.max(1,(int)Math.ceil(base*factor-1e-9));}
}
