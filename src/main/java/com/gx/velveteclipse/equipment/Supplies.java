package com.gx.velveteclipse.equipment;
import java.util.*;
import com.gx.velveteclipse.core.battle.BattleCombatant;
public final class Supplies {
 public record Supply(String id,String name,int price,int hp,int sp,String cures,String description){}
 private static final List<Supply> ALL=List.of(
  new Supply("medicine","Medicine",300,50,0,"","Recupera 50 HP."),
  new Supply("life_stone","Life Stone",500,-30,0,"","Recupera 30% do HP máximo."),
  new Supply("bead","Bead",4000,-100,0,"","Recupera todo o HP."),
  new Supply("snuff_soul","Snuff Soul",3000,0,50,"","Recupera 50 SP."),
  new Supply("chewing_soul","Chewing Soul",6000,0,100,"","Recupera 100 SP."),
  new Supply("precious_egg","Precious Egg",20000,0,-100,"","Recupera todo o SP."),
  new Supply("dis_poison","Dis-Poison",400,0,0,"POISON","Cura veneno."),
  new Supply("dis_charm","Dis-Charm",500,0,0,"CHARM","Cura encanto."),
  new Supply("dis_rage","Dis-Rage",500,0,0,"RAGE","Cura fúria."),
  new Supply("dis_patra","Dis-Patra",400,0,0,"DISTRESS,DIZZY,FEAR,CONFUSE","Cura aflição, tontura, medo e confusão."),
  new Supply("amrita_soda","Amrita Soda",1200,0,0,"ALL","Cura todos os estados negativos."));
 public static List<Supply> all(){return ALL;}
 public static Supply get(String id){return ALL.stream().filter(s->s.id().equals(id)).findFirst().orElse(null);}
 public static int amount(int v,int max){return v<0?Math.max(1,max*-v/100):v;}
 public static boolean useful(Supply s,BattleCombatant c){return s!=null&&c.alive()&&(s.hp()!=0&&c.hp()<c.maxHp()||s.sp()!=0&&c.sp()<c.maxSp()||!s.cures().isEmpty()&&c.hasAilment(s.cures()));}
 public static void apply(Supply s,BattleCombatant c){c.heal(amount(s.hp(),c.maxHp()));c.restoreSp(amount(s.sp(),c.maxSp()));c.cure(s.cures());}
}
