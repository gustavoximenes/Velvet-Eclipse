package com.gx.velveteclipse.tests;
import com.gx.velveteclipse.equipment.*;
import com.gx.velveteclipse.core.persona.*;
import com.gx.velveteclipse.core.battle.*;
import com.gx.velveteclipse.forge.*;
import net.minecraft.nbt.CompoundTag;
import java.util.*;
import java.nio.file.*;
public final class EquipmentAudit {
 private static int checks;private static void check(boolean b,String m){checks++;if(!b)throw new AssertionError(m);}
 public static final class Player {final CompoundTag tag=new CompoundTag();public CompoundTag getPersistentData(){return tag;}}
 private static EquipmentBonuses bonus(String effect){return new EquipmentBonuses(List.of(new EquipmentCatalog.Gear("test","Test",EquipmentCatalog.Slot.ACCESSORY,EquipmentCatalog.Family.NONE,0,1,1,1,effect)));}
 private static BattleCombatant actor(){return new BattleCombatant(UUID.randomUUID(),"Player",BattleSide.PLAYERS,new PersonaInstance(PersonaRegistry.persona("cinder_wisp").orElseThrow()),200,100,80,10);}
 public static void main(String[] args)throws Exception{
  PersonaRegistry.bootstrap();check(EquipmentCatalog.all().size()==635,"all 635 base-game entries");
  for(var family:EquipmentCatalog.Family.values())check(EquipmentCatalog.all().stream().anyMatch(g->g.family()==family),"catalog category "+family);
  var bag=EquipmentLoadout.starter();var wallet=new PlayerPersonaData();check(!bag.equip("lucifer_s_blade"),"unowned gear cannot be equipped");
  check(EquipmentTransactions.buy(bag,wallet,"gladius")==EquipmentTransactions.Result.NO_YEN&&wallet.yen()==0&&!bag.owned.contains("gladius"),"insufficient balance has no side effects");
  wallet.addYen(3000);check(EquipmentTransactions.buy(bag,wallet,"gladius")==EquipmentTransactions.Result.BOUGHT&&wallet.yen()==400,"server catalog price charged");
  check(EquipmentTransactions.buy(bag,wallet,"gladius")==EquipmentTransactions.Result.OWNED&&wallet.yen()==400,"duplicate equipment purchase is harmless");
  check(EquipmentTransactions.buy(bag,wallet,"forged_id")==EquipmentTransactions.Result.UNKNOWN&&wallet.yen()==400,"unknown item cannot debit yen");
  check(bag.equip("gladius")&&bag.bonuses().hp()==10,"owned equipment applies HP passive");
  check(EquipmentTransactions.buy(bag,wallet,"medicine")==EquipmentTransactions.Result.BOUGHT&&bag.count("medicine")==1&&wallet.yen()==100,"consumable purchase");
  check(bag.consume("medicine")&&!bag.consume("medicine"),"single item consumed once");
  var p=new Player();EquipmentData.save(p,bag);wallet.save(p);var cloned=new Player();PlayerPersonaData.copyPersisted(p,cloned);
  check(EquipmentData.load(cloned).equipped.equals(bag.equipped)&&PlayerPersonaData.load(cloned).yen()==100,"death clone preserves collection, loadout, wallet");
  check(PlayerPersonaData.load(p).maxHp()==130,"persistent resource maximum includes gear");
  var d=PlayerPersonaData.load(p);d.restore();d.save(p);bag.remove(EquipmentCatalog.Slot.WEAPON);EquipmentData.save(p,bag);check(PlayerPersonaData.load(p).hp()==120,"unequip clamps current HP to new maximum");
  check(bonus("All Stats +7, Magic +3").stats(new PersonaStats(1,1,1,1,1)).magic()==11,"stat bonuses stack by type");
  check(bonus("+Null Fire").affinity(Element.FIRE,Affinity.WEAK)==Affinity.NULL,"null overrides inherited weakness");
  check(bonus("+Null Fire").affinity(Element.ICE,Affinity.WEAK)==Affinity.WEAK,"null does not expose or override other element");
  check(bonus("+Fire Boost, +Fire Amp").damage(Element.FIRE,true)==1.875,"boost and amp stack without duplicating equipment");
  check(bonus("+Reduce magic dmg (high)").reduction(Element.ICE)==.7,"incoming elemental reduction");
  check(bonus("+100 HP and SP").hp()==100&&bonus("+100 HP and SP").sp()==100,"dual resource bonus");
  var c=actor().equipment(bonus("+Spell Master, +Arms Master"));check(c.spCost(0)==0&&c.spCost(10)==5&&c.hpCost(10)==5,"cost reduction preserves zero costs");
  c.afflict("POISON");check(Supplies.useful(Supplies.get("dis_poison"),c),"status remedy recognizes poison");Supplies.apply(Supplies.get("dis_poison"),c);check(c.ailment().isEmpty(),"poison remedy cures its target");
  c.afflict("SHOCK");Supplies.apply(Supplies.get("amrita_soda"),c);check(c.ailment().isEmpty(),"Amrita cures all ailments");
  Supples(c);
  c=actor().equipment(bonus("+Null all ailments"));c.afflict("FEAR");check(c.ailment().isEmpty(),"all-ailment immunity");
  c=actor().equipment(bonus("+Enduring Soul"));c.damage(1000);check(c.hp()==200,"Enduring Soul restores once");c.damage(1000);check(!c.alive(),"Enduring Soul cannot create immortality");
  c=actor().equipment(bonus("+Endure Dark"));c.elementalDamage(1000,Element.FIRE);check(!c.alive(),"element-specific Endure cannot guard unrelated damage");
  c=actor().equipment(bonus("+Endure Dark"));c.instantDamage(c.survivesInstant(Element.DARK,new java.util.Random(1)));check(c.hp()==1,"element-specific Endure protects instant death of the correct element");
  var json=new com.google.gson.Gson();var response=new EquipmentNetwork.Snapshot(bag,123,"shop","",12,true,UUID.randomUUID().toString());check(json.fromJson(json.toJson(response),EquipmentNetwork.Snapshot.class).bag().owned.equals(bag.owned),"network collection round-trip");
  var resources=Path.of("src/main/resources/assets/velvet_eclipse");for(var gear:EquipmentCatalog.all())check(Files.exists(resources.resolve("models/item/gear_"+gear.id()+".json")),"creative model "+gear.id());
  for(var f:EquipmentCatalog.Family.values())if(f!=EquipmentCatalog.Family.NONE)check(Files.exists(resources.resolve("weapons/"+f.name().toLowerCase(Locale.ROOT)+".json")),"native weapon asset "+f);
  check(Files.exists(resources.resolve("geo/entity/elizabeth.geo.json"))&&Files.exists(resources.resolve("animations/entity/elizabeth.animation.json")),"approved Elizabeth runtime export");
  System.out.println("PASS equipment: "+checks+" checks; full catalog, purchase/ownership, wallet and death persistence, combat passives, supplies, native assets and creative models. Offline only.");
 }
 private static void Supples(BattleCombatant c){int hp=c.hp();Supplies.apply(Supplies.get("medicine"),c);check(c.hp()==hp+50,"HP item effect");Supplies.apply(Supplies.get("snuff_soul"),c);check(c.sp()==60,"SP item effect");}
}
