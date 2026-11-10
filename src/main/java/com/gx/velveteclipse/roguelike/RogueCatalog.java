package com.gx.velveteclipse.roguelike;
import java.util.*;

/** Prototype balance, independent of Minecraft and of display language. */
public final class RogueCatalog {
 public enum Focus {
  NONE("Unassigned","Choose a class focus outside combat."),
  DPS_PHYSICAL("DPS / Physical","+10% damage with physical skills."),
  DPS_MAGIC("DPS / Magic","+10% damage with magical skills."),
  SUPPORT_HEAL("Support / Healing","+15% healing from skills."),
  SUPPORT_DEBUFF("Support / Debuffs","Attack, defense and accuracy debuffs last 1 extra turn."),
  WEAKNESS("Weakness Hitter","Attack skills cost 50% less SP or HP. Physical and elemental attacks deal 50% less damage. Weakness knockdowns remain intact.");
  public final String label,description;
  Focus(String label,String description){this.label=label;this.description=description;}
  public String id(){return name().toLowerCase(Locale.ROOT);}
  public static Focus parse(String id){try{return valueOf(id.toUpperCase(Locale.ROOT));}catch(Exception e){return NONE;}}
 }
 public record Card(String id,int number,String arcana,String name,String benefit,String drawback,
                    double physical,double magic,double heal,double sp,double incoming,int debuff,boolean noOneMore){
  public boolean pact(){return !drawback.isEmpty();}
 }
 public static final List<Card> CARDS=List.of(
  new Card("magician",1,"Magician","Arcane Spark","+8% magical skill damage.","",0,.08,0,0,0,0,false),
  new Card("priestess",2,"Priestess","Gentle Grace","+10% healing from skills.","",0,0,.10,0,0,0,false),
  new Card("chariot",7,"Chariot","Unstoppable","+35% physical and magical attack damage.","Cannot gain One More or receive Shift. You may join an ally's All-Out Attack.",.35,.35,0,0,0,0,true),
  new Card("hermit",9,"Hermit","Lingering Doubt","Attack, defense and accuracy debuffs last 1 extra turn.","",0,0,0,0,0,1,false),
  new Card("strength",11,"Strength","Steady Hand","+8% physical skill damage.","",.08,0,0,0,0,0,false),
  new Card("hanged_man",12,"Hanged Man","Selfless Grace","+30% healing from skills.","Physical and magical attack damage reduced by 15%.",-.15,-.15,.30,0,0,0,false),
  new Card("temperance",14,"Temperance","Measured Breath","Attack skills cost 10% less SP.","",0,0,0,-.10,0,0,false),
  new Card("devil",15,"Devil","Reckless Power","+25% physical and magical attack damage.","Take 20% more damage.",.25,.25,0,0,.20,0,false),
  new Card("star",17,"Star","Quiet Shelter","Take 8% less damage.","",0,0,0,0,-.08,0,false));
 public static Card card(String id){return CARDS.stream().filter(c->c.id().equals(id)).findFirst().orElse(null);}
 /** One roll per newly claimed Tartarus chest; a chest may contain no card. */
 public static String roll(Random random){return random.nextDouble()<.25?CARDS.get(random.nextInt(CARDS.size())).id():"";}
 /** Preserve 25% Buff drops; add a separate 10% Up band. At most one card per chest. */
 public static String rollTreasure(Random random){double chance=random.nextDouble();return chance<.25?CARDS.get(random.nextInt(CARDS.size())).id():chance<.35?ArcanaUpCatalog.CARDS.get(random.nextInt(ArcanaUpCatalog.CARDS.size())).key():"";}
 private RogueCatalog(){}
}
