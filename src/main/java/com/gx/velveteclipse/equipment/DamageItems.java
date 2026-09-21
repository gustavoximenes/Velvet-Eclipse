package com.gx.velveteclipse.equipment;
import com.gx.velveteclipse.core.persona.*;
import com.gx.velveteclipse.core.battle.*;
import java.util.*;
/** Reload elemental consumables: fixed base damage, modified by the target's affinity. */
public final class DamageItems {
 public record Item(String id,String name,Element element,int damage,boolean all,int price){
  public SkillDefinition skill(){return new SkillDefinition("__item_"+id,name,element,damage,100,0,0,false,all?SkillTarget.ALL_ENEMIES:SkillTarget.ONE_ENEMY);}
  public String description(){return "Causa "+damage+" de dano de "+label(element)+" "+(all?"a todos os inimigos":"a um inimigo")+", antes de afinidades. Não gasta SP nem exige habilidade na Persona.";}
 }
 public static final List<Item> ALL=List.of(
  new Item("agilao_gem","Agilao Gem",Element.FIRE,100,false,1600),new Item("maragi_gem","Maragi Gem",Element.FIRE,50,true,1800),
  new Item("bufula_gem","Bufula Gem",Element.ICE,100,false,1600),new Item("mabufu_gem","Mabufu Gem",Element.ICE,50,true,1800),
  new Item("zionga_gem","Zionga Gem",Element.ELECTRIC,100,false,1600),new Item("mazio_gem","Mazio Gem",Element.ELECTRIC,50,true,1800),
  new Item("garula_gem","Garula Gem",Element.WIND,100,false,1600),new Item("magaru_gem","Magaru Gem",Element.WIND,50,true,1800),
  new Item("kouga_gem","Kouga Gem",Element.LIGHT,100,false,1800),new Item("makouha_gem","Makouha Gem",Element.LIGHT,50,true,2000),
  new Item("eiga_gem","Eiga Gem",Element.DARK,100,false,1800),new Item("maeiha_gem","Maeiha Gem",Element.DARK,50,true,2000),
  new Item("megido_gem","Megido Gem",Element.ALMIGHTY,100,true,5000),new Item("megidolaon_gem","Megidolaon Gem",Element.ALMIGHTY,200,true,15000),
  new Item("flame_magatama","Flame Magatama",Element.FIRE,150,false,3600),new Item("frigid_magatama","Frigid Magatama",Element.ICE,150,false,3600),
  new Item("shock_magatama","Shock Magatama",Element.ELECTRIC,150,false,3600),new Item("gale_magatama","Gale Magatama",Element.WIND,150,false,3600),
  new Item("holy_magatama","Holy Magatama",Element.LIGHT,150,false,4000),new Item("malice_magatama","Malice Magatama",Element.DARK,150,false,4000));
 public static Item get(String id){return ALL.stream().filter(i->i.id().equals(id)).findFirst().orElse(null);}
 public static Element element(String id){var item=get(id);return item==null?Element.HEALING:item.element();}
 public static String label(Element e){return switch(e){case SLASH->"Cortante";case PHYSICAL->"Concussão";case PIERCE->"Perfurante";case FIRE->"Fogo";case ICE->"Gelo";case ELECTRIC->"Elétrico";case WIND->"Vento";case LIGHT->"Luz";case DARK->"Trevas";case ALMIGHTY->"Onipotente";case PSY->"Psíquico";case NUCLEAR->"Nuclear";default->e.name();};}
 public static DamageOutcome roll(Item item,BattleCombatant target){var affinity=item.element()==Element.ALMIGHTY?Affinity.NORMAL:target.affinity(item.element());int damage=switch(affinity){case WEAK->item.damage()*3/2;case RESIST->item.damage()/2;case NULL->0;default->item.damage();};return new DamageOutcome(item.element(),affinity,damage,false,false,affinity==Affinity.REPEL,affinity==Affinity.DRAIN,false);}
}
