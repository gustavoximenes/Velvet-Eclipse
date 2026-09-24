package com.gx.velveteclipse.equipment;
import com.gx.velveteclipse.core.persona.*;
import java.util.*;
import java.util.regex.*;

/** Passive vocabulary shared by every Reload item, with explicit mod-scale damage conversion. */
public final class EquipmentBonuses {
 public static final EquipmentBonuses NONE=new EquipmentBonuses(List.of());
 private final List<EquipmentCatalog.Gear> gear;private final String effects;
 public EquipmentBonuses(Collection<EquipmentCatalog.Gear> gear){this.gear=List.copyOf(gear);effects=String.join(",",gear.stream().map(g->g.effect().toLowerCase(Locale.ROOT)).toList());}
 private EquipmentBonuses(List<EquipmentCatalog.Gear> gear,String effects){this.gear=gear;this.effects=effects;}
 public EquipmentBonuses withSkills(Collection<String> ids){var names=ids.stream().map(PersonaRegistry::skill).flatMap(Optional::stream).filter(SkillDefinition::isPassive).map(s->s.displayName().toLowerCase(Locale.ROOT)).toList();return new EquipmentBonuses(gear,effects+","+String.join(",",names));}
 public boolean exact(String effect){return java.util.Arrays.stream(effects.split(",")).anyMatch(e->e.trim().replace("+", "").equals(effect));}
 public boolean has(String effect){return effects.contains(effect.toLowerCase(Locale.ROOT));}
 public EquipmentCatalog.Gear weapon(){return gear.stream().filter(g->g.slot()==EquipmentCatalog.Slot.WEAPON).findFirst().orElse(null);}
 public EquipmentCatalog.Family family(){return weapon()==null?EquipmentCatalog.Family.SWORD:weapon().family();}
 public int value(EquipmentCatalog.Slot slot){return gear.stream().filter(g->g.slot()==slot).mapToInt(EquipmentCatalog.Gear::value).sum();}
 private int sum(String pattern){var m=Pattern.compile(pattern).matcher(effects);int n=0;while(m.find())n+=Integer.parseInt(m.group(1));return n;}
 public int hp(){return sum("\\+(\\d+) hp");}
 public int sp(){return sum("\\+(\\d+) sp")+sum("\\+(\\d+) hp and sp");}
 public int stat(String key){return sum(key+" \\+(\\d+)")+sum("all stats \\+(\\d+)");}
 public PersonaStats stats(PersonaStats s){return new PersonaStats(s.strength()+stat("strength"),s.magic()+stat("magic"),s.endurance()+stat("endurance"),s.agility()+stat("agility"),s.luck()+stat("luck"));}
 public int attackPower(){return weapon()==null?35:Math.max(10,Math.round(weapon().value()*.55f));}
 public double armor(){return 1+value(EquipmentCatalog.Slot.ARMOR)/250.0;}
 public static boolean physical(Element e){return e==Element.PHYSICAL||e==Element.SLASH||e==Element.PIERCE;}
 public static String key(Element e){return e==Element.PHYSICAL?"strike":e==Element.ELECTRIC?"elec":e.name().toLowerCase(Locale.ROOT);}
 public Element element(){for(var e:Element.values())if(has(key(e)+" attack")||e==Element.ELECTRIC&&has("electric attack"))return e;return family().element();}
 public Affinity affinity(Element e,Affinity base){if(e==Element.ALMIGHTY||e==Element.NONE)return base;String k=key(e);if(exact("drain "+k))return Affinity.DRAIN;if(exact("repel "+k))return Affinity.REPEL;if(has("null all except almighty")||exact("null "+k)||physical(e)&&exact("null phys"))return Affinity.NULL;if(exact("resist "+k)||physical(e)&&exact("resist phys"))return base==Affinity.NORMAL||base==Affinity.WEAK?Affinity.RESIST:base;return base;}
 private double tier(String prefix){return has(prefix+" (high)")?.30:has(prefix+" (med)")?.20:has(prefix+" (low)")?.10:0;}
 public double reduction(Element e){return 1-Math.max(tier("reduce "+key(e)+" dmg"),tier("reduce "+(physical(e)?"phys":"magic")+" dmg"));}
 public double evade(Element e){if(has("firm stance"))return 0;double n=Math.min(.15,value(EquipmentCatalog.Slot.FOOTWEAR)/900.0);n+=Math.max(tier(key(e)+" evasion"),tier((physical(e)?"physical":"magic")+" evasion"));if(e==Element.ELECTRIC)n=Math.max(n,tier("electricity evasion"));n=Math.max(n,has("evade "+key(e))?.4:has("dodge "+key(e))?.15:0);if(physical(e)&&has("vidyaraja's blessing")||!physical(e)&&e!=Element.ALMIGHTY&&has("angelic grace"))n=Math.max(n,.15);return Math.min(.65,Math.max(n,has("ali dance")?.5:0));}
 public double damage(Element e,boolean magic){String k=key(e);return (has(k+" boost")?1.25:1)*(has(k+" amp")?1.5:1)*(magic&&e!=Element.ALMIGHTY&&has("magic ability")?1.25:1);}
 public int critical(){return Math.max((int)(tier("critical rate up")*100),has("crit rate amp")?15:has("crit rate boost")?5:0);}
 public int counter(){return has("high counter")?20:has("counterstrike")?15:has("counter")?10:0;}
 public int regen(){return has("regenerate 3")?6:has("regenerate 2")?4:has("regenerate 1")?2:0;}
 public int invigorate(){return has("invigorate 3")?7:has("invigorate 2")?5:has("invigorate 1")?3:0;}
 public int xp(int base){return (int)Math.round(base*(has("50% exp")?1.5:has("15% exp")?1.15:1));}
 public String ailment(){for(String a:List.of("poison","charm","confuse","fear","rage","distress","shock","freeze","random ailment"))if(tier(a)>0)return a.toUpperCase(Locale.ROOT);return "";}
 public int ailmentBoost(String a){return (has(a.toLowerCase(Locale.ROOT)+" boost")?15:0)+(has("ailment boost")?10:0);}
 public int ailmentChance(){return (int)(tier(ailment().toLowerCase(Locale.ROOT))*100);}
 public boolean immune(String a){return has("null all ailments")||has("null "+a)||(has("unshaken will")&&Set.of("CONFUSE","FEAR","RAGE","CHARM","DISTRESS").contains(a));}
 public boolean resistant(String a){return has("resist ailments")||has("resist "+a);}
 public SkillDefinition displayCost(SkillDefinition s){return new SkillDefinition(s.id(),s.displayName(),s.element(),s.power(),s.accuracy(),s.spCost()>0&&has("spell master")?Math.max(1,s.spCost()/2):s.spCost(),s.hpPercentCost()>0&&has("arms master")?Math.max(1,s.hpPercentCost()/2):s.hpPercentCost(),s.canCritical(),s.target());}
 public String description(){return effects;}
}
