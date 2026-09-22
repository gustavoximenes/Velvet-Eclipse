package com.gx.velveteclipse.tests;
import com.gx.velveteclipse.core.persona.*;
import com.gx.velveteclipse.core.battle.*;
import com.gx.velveteclipse.forge.PlayerPersonaData;
import com.gx.velveteclipse.equipment.*;
import com.gx.velveteclipse.progression.*;
import com.gx.velveteclipse.room.CreativeCategory;
import com.gx.velveteclipse.shadow.*;
import com.gx.velveteclipse.shuffle.*;
import com.google.gson.Gson;
import net.minecraft.nbt.CompoundTag;
import java.nio.file.*;
import java.util.*;

/** Focused regression checks for 0.6.26; no game launch required. */
public final class PermitAudit {
 private static int checks;
 private static void check(boolean ok,String why){checks++;if(!ok)throw new AssertionError(why);}
 public static final class Player {final CompoundTag data=new CompoundTag();public CompoundTag getPersistentData(){return data;}}
 public static void main(String[] args)throws Exception{PersonaRegistry.bootstrap();stock();rewards();capacity();oneMore();assets();System.out.println("PASS permits: "+checks+" checks; shop stages, durable first-clear rewards, persona cap/migration, lethal 1 More and categorized creative resources. Offline only.");}
 private static void stock(){
  check(ShopStock.entries().size()==250&&ShopStock.INITIAL.size()==17,"exact initial/full Police Station inventory");
  int previous=0;
  for(int tier=0;tier<=9;tier++){
   final int t=tier;int count=(int)EquipmentCatalog.all().stream().filter(g->ShopStock.available(g.id(),t)).count();check(count>previous,"every canonical shop period adds stock");previous=count;
   for(var e:ShopStock.entries())check(ShopStock.available(e.id(),tier)==(e.tier()<=tier),"stage gate: "+e.id());
  }
  check(!ShopStock.available("lucifer_s_blade",9)&&ShopStock.available("gimlet",9),"fusion-only weapon never enters police stock");
  var p=new Player();check(ShopProgress.tier(p)==0,"default personal tier");check(ShopProgress.redeem(p,75)&&ShopProgress.tier(p)==0,"out-of-order permit recorded without skipping stages");
  check(!ShopProgress.redeem(p,75)&&!ShopProgress.redeem(p,26),"duplicate and forged floor rejected");
  check(ShopProgress.redeem(p,25)&&ShopProgress.tier(p)==1,"first boss opens first new stock");
  check(ShopProgress.redeem(p,50)&&ShopProgress.tier(p)==3,"contiguous stored permits unlock in sequence");
  var saved=new Player();saved.data.m_128359_("velvet_eclipse:purchase_permits",p.data.m_128461_("velvet_eclipse:purchase_permits"));check(ShopProgress.tier(saved)==3,"permits survive persistent-data roundtrip");
  for(int floor=100;floor<=250;floor+=25)check(ShopProgress.redeem(p,floor),"each 25-floor permit accepted once");
  check(ShopProgress.tier(p)==9&&ShopStock.nextFloor(ShopProgress.permits(p))==0,"250F remains final canonical stock, no invented tier");
 }
 private static void rewards(){
  var r=new RewardLedger();check(r.boss("a")&&!r.boss("a"),"raid replay cannot duplicate first-clear drops");check(r.pending("a").equals(List.of("purchase_permit_25","igor_contract")),"both unique boss items queued");check(r.boss("b"),"every participant has independent first-clear reward");
  check(!r.chest("a",25)&&!r.chest("a",26)&&!RewardLedger.permitChest(50,1),"boss permit cannot be obtained from 25F treasure or wrong chest");
  for(int f=50;f<=250;f+=25){check(r.chest("a",f)&&!r.chest("a",f),"fixed chest only once per floor/player");check(r.chest("b",f),"another player can collect the same permit chest");}
  var gson=new Gson();r=gson.fromJson(gson.toJson(r),RewardLedger.class);check(!r.boss("a")&&!r.chest("a",50)&&r.pending("a").size()==11,"receipts and full-inventory queue persist across restart");
  r.delivered("a","igor_contract");r=gson.fromJson(gson.toJson(r),RewardLedger.class);check(!r.pending("a").contains("igor_contract")&&r.pending("b").contains("igor_contract"),"delivery removes only the correct player's pending item");
 }
 private static void capacity(){
  var d=new PlayerPersonaData();check(d.personaCapacity()==2&&d.givePersona("izanagi"),"initial capacity includes starter");check(!d.givePersona("iron_oni")&&!d.givePersonaInstance(new PersonaInstance(PersonaRegistry.persona("iron_oni").orElseThrow())),"all acquisition APIs enforce cap");
  var shuffle=new ShuffleData();var card=new ShuffleRules.Card(ShuffleRules.Kind.PERSONA,"iron_oni",20,1);shuffle.add(new ShuffleRules.Round("limit",25,List.of(card)));
  check(ShuffleRewards.claim(shuffle,d,"limit",0,x->true).card()==null&&shuffle.first()!=null,"full roster does not consume shuffle reward");
  check(d.expandPersonaCapacity()&&d.personaCapacity()==3,"contract adds exactly one slot");check(ShuffleRewards.claim(shuffle,d,"limit",0,x->true).card()!=null&&d.rosterIds().size()==3,"claim works after capacity increase");
  while(d.personaCapacity()<8)check(d.expandPersonaCapacity(),"contract increases up to eight");check(!d.expandPersonaCapacity()&&d.personaCapacity()==8,"ninth slot rejected without mutation");
  var p=new Player();d.save(p);check(PlayerPersonaData.load(p).personaCapacity()==8,"capacity persists");var clone=new Player();PlayerPersonaData.copyPersisted(p,clone);check(PlayerPersonaData.load(clone).personaCapacity()==8,"death clone retains capacity");
  // Simulate an old V1 save with more than two Personas, including inherited skills and levels.
  d.givePersona("lumen_hare");d.setPersonaLevel("iron_oni",37);var oni=d.instance("iron_oni");oni.inheritSkill("media");d.updatePersona(oni);d.equipPersona("iron_oni");d.save(p);
  String[] parts=p.data.m_128461_("velvet_eclipse:data_v1").split("\\|",-1);p.data.m_128359_("velvet_eclipse:data_v1",String.join("|",Arrays.copyOf(parts,11)));
  d=PlayerPersonaData.load(p);check(d.personaCapacity()==2&&d.rosterIds().size()==2&&d.reserveIds().size()==2&&d.activePersona().equals("iron_oni"),"legacy overflow archived while preserving equipped persona");
  var all=new HashSet<>(d.rosterIds());all.addAll(d.reserveIds());check(all.size()==4,"migration loses no persona");
  String reserve=d.reserveIds().iterator().next();check(d.equipPersona(reserve)&&d.rosterIds().size()==2&&d.reserveIds().contains("iron_oni"),"reserve selection exchanges active persona without bypassing capacity");
  d.save(p);d=PlayerPersonaData.load(p);check(d.rewardInstance("iron_oni").level()==37&&d.rewardInstance("iron_oni").learnedSkills().contains("media"),"reserved levels and inherited skills persist");
  check(d.expandPersonaCapacity()&&d.equipPersona("iron_oni")&&d.rosterIds().size()==3&&d.instance("iron_oni").level()==37,"contract makes room for preserved reserve");
  d=new PlayerPersonaData();d.givePersona("izanagi");d.setPlayerLevel(99);check(d.replaceFusion("cinder_wisp","izanagi",new PersonaInstance(PersonaRegistry.persona("iron_oni").orElseThrow()))&&d.rosterIds().size()==1&&d.personaCapacity()==2,"fusion remains possible at cap and releases a slot");
  d=new PlayerPersonaData();d.givePersona("izanagi");d.setPlayerLevel(99);var recipe=com.gx.velveteclipse.core.fusion.FusionTransaction.preview(d,"cinder_wisp","izanagi",null);String resultId=recipe.result().definitionId();d.save(p);parts=p.data.m_128461_("velvet_eclipse:data_v1").split("\\|",-1);parts[12]=resultId+",80,0,media";p.data.m_128359_("velvet_eclipse:data_v1",String.join("|",parts));d=PlayerPersonaData.load(p);
  check(com.gx.velveteclipse.core.fusion.FusionTransaction.commit(d,"cinder_wisp","izanagi",null)&&d.instance(resultId).level()==80&&d.instance(resultId).learnedSkills().contains("media")&&!d.reserveIds().contains(resultId),"fusion preserves a previously archived result and promotes it once");
 }
 private static UUID id(int n){return new UUID(0,n);}
 private static BattleCombatant player(int n){return new BattleCombatant(id(n),"Player",BattleSide.PLAYERS,new PersonaInstance("izanagi",30,0,Set.of("ember")),500,500,300,300);}
 private static BattleCombatant enemy(int n,int hp,Affinity fire){
  var def=new EnemyDefinition("target"+n,"Target","maya",1,1,1,hp,50,new PersonaStats(1,1,1,1,1),Map.of(Element.FIRE,fire),Set.of("ember"),Element.SLASH,1,false);
  return BattleCombatant.enemy(id(n),"Target",def,1,hp,50,Set.of("ember"));
 }
 private static void oneMore(){
  var deterministic=new Random(){@Override public int nextInt(int bound){return 0;}};
  var p=player(1);var ally=player(2);var weak=enemy(10,1,Affinity.WEAK);var other=enemy(11,500,Affinity.NORMAL);var s=new BattleSession(List.of(p,ally,weak,other),deterministic);
  var action=BattleAction.skill("ember",weak.id());check(s.prepare(p.id(),action)!=null&&weak.alive(),"animation preparation does not apply damage");
  var result=s.perform(p.id(),action);check(!weak.alive()&&!weak.downed()&&result.oneMore()&&s.oneMore()&&s.currentActor()==p,"lethal weakness retains attacker's 1 More");
  check(!s.canAllOut(p.id())&&s.batonPass(p.id(),ally.id()),"lethal bonus allows Shift but does not down untouched enemy");
  p=player(1);weak=enemy(10,1,Affinity.NORMAL);other=enemy(11,500,Affinity.NORMAL);s=new BattleSession(List.of(p,weak,other),deterministic);result=s.perform(p.id(),BattleAction.attack(weak.id()));check(!weak.alive()&&result.oneMore(),"lethal critical basic attack awards 1 More");
  p=player(1);weak=enemy(10,1,Affinity.NORMAL);other=enemy(11,500,Affinity.NORMAL);s=new BattleSession(List.of(p,weak,other),deterministic);result=s.perform(p.id(),BattleAction.skill("ember",weak.id()));check(!result.oneMore()&&!s.oneMore(),"ordinary neutral kill never creates 1 More");
  p=player(1);weak=enemy(10,1,Affinity.WEAK);s=new BattleSession(List.of(p,weak),deterministic);result=s.perform(p.id(),BattleAction.skill("ember",weak.id()));check(result.state()==BattleState.PLAYERS_WON&&!result.oneMore()&&!s.oneMore(),"last-enemy kill goes directly to victory");
  for(boolean immune:new boolean[]{false,true}){p=player(1);weak=enemy(10,1,Affinity.WEAK);if(immune)weak.setRaidBoss(Element.FIRE);else weak.setDowned(true);other=enemy(11,500,Affinity.NORMAL);s=new BattleSession(List.of(p,weak,other),deterministic);check(!s.perform(p.id(),BattleAction.skill("ember",weak.id())).oneMore(),"already-down or raid-immune lethal target grants no bonus");}
  for(Affinity a:List.of(Affinity.NULL,Affinity.DRAIN,Affinity.REPEL)){p=player(1);weak=enemy(10,1,a);other=enemy(11,500,Affinity.NORMAL);s=new BattleSession(List.of(p,weak,other),deterministic);check(!s.perform(p.id(),BattleAction.skill("ember",weak.id())).oneMore(),"null/drain/repel grants no bonus");}
  p=player(1);weak=enemy(10,1,Affinity.WEAK);other=enemy(11,500,Affinity.NORMAL);s=new BattleSession(List.of(p,weak,other),deterministic);check(s.perform(p.id(),new BattleAction(BattleActionType.ITEM,"maragi_gem",weak.id())).oneMore()&&!weak.alive(),"lethal weakness in an area item also earns bonus");
  var attacker=enemy(10,500,Affinity.NORMAL);var target=player(1);var hit=DamageCalculator.apply(attacker,target,new DamageOutcome(Element.FIRE,Affinity.WEAK,9999,false,false,false,false,false));check(hit.newlyDowned()&&!target.alive(),"same lethal eligibility applies to enemy attacks");
 }
 private static void assets()throws Exception{
  var root=Path.of("src/main/resources/assets/velvet_eclipse");var lang=new Gson().fromJson(Files.readString(root.resolve("lang/pt_br.json")),Map.class);
  for(var c:CreativeCategory.values()){check(lang.containsKey("itemGroup.velvet_eclipse."+c.name().toLowerCase(Locale.ROOT)),"localized tab "+c);check(Files.exists(root.resolve("models/item/"+c.icon+".json")),"real tab icon "+c);}
  for(int f=0;f<=250;f+=25){String id=f==0?"igor_contract":RewardLedger.permit(f);check(Files.exists(root.resolve("textures/item/"+id+".png"))&&Files.exists(root.resolve("models/item/"+id+".json"))&&lang.containsKey("item.velvet_eclipse."+id),"complete item assets "+id);check(CreativeCategory.of(id,false)==CreativeCategory.GENERAL,"progression creative destination");}
  for(var g:EquipmentCatalog.all())check(CreativeCategory.of("gear_"+g.id(),false)==switch(g.slot()){case WEAPON->CreativeCategory.WEAPONS;case ARMOR->CreativeCategory.ARMOR;case FOOTWEAR->CreativeCategory.FOOTWEAR;case ACCESSORY->CreativeCategory.ACCESSORIES;},"equipment category "+g.id());
  for(var e:EnemyCatalog.all())check(CreativeCategory.of(e.asset()+"_spawn",false)==CreativeCategory.ENEMIES,"enemy category "+e.id());
  for(String skill:PersonaRegistry.skills().keySet())check(CreativeCategory.of("skill_card_"+skill,false)==CreativeCategory.SKILL_CARDS,"skill card destination");
  check(CreativeCategory.of("future_block",true)==CreativeCategory.BLOCKS&&CreativeCategory.of("future_item",false)==CreativeCategory.GENERAL,"future content remains visible");
 }
}
