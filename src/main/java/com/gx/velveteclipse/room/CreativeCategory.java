package com.gx.velveteclipse.room;
import com.gx.velveteclipse.equipment.*;
/** One destination for every registered item, including future items through the general fallback. */
public enum CreativeCategory {
 GENERAL("velvet_key"),SKILL_CARDS("skill_card_ember"),ENEMIES("field_shadow_spawn"),
 NPCS("elizabeth_model"),BLOCKS("tartarus_stone"),PROPS("tartarus_terminal"),WEAPONS("gear_saber"),
 ARMOR("gear_rash_guard"),FOOTWEAR("gear_high_tech_sandals"),ACCESSORIES("gear_power_band"),SUPPLIES("medicine");
 public final String icon;
 CreativeCategory(String icon){this.icon=icon;}
 public static CreativeCategory of(String id,boolean block){
  if(block)return BLOCKS;
  if(id.startsWith("skill_card_"))return SKILL_CARDS;
  if(id.endsWith("_spawn")||id.equals("priestess_figure"))return ENEMIES;
  if(id.startsWith("model_")&&!id.equals("model_evoker"))return NPCS;
  if(id.endsWith("_model"))return NPCS;
  if(id.equals("tartarus_terminal")||id.equals("tartarus_treasure")||id.equals("tartarus_monad"))return PROPS;
  if(id.startsWith("gear_")){var g=EquipmentCatalog.get(id.substring(5));if(g!=null)return switch(g.slot()){case WEAPON->WEAPONS;case ARMOR->ARMOR;case FOOTWEAR->FOOTWEAR;case ACCESSORY->ACCESSORIES;};}
  return Supplies.get(id)!=null?SUPPLIES:GENERAL;
 }
}
