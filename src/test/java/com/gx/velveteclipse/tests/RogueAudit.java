package com.gx.velveteclipse.tests;
import com.gx.velveteclipse.roguelike.*;
import com.gx.velveteclipse.core.battle.*;
import com.gx.velveteclipse.core.persona.*;
import com.gx.velveteclipse.equipment.*;
import com.gx.velveteclipse.client.menu.*;
import com.gx.velveteclipse.localization.DisplayText;
import com.google.gson.*;
import java.util.*;
import java.nio.file.*;
import com.mojang.brigadier.*;
import com.mojang.brigadier.builder.*;
import com.mojang.brigadier.arguments.*;

public final class RogueAudit {
 static int checks;static void check(boolean ok,String msg){checks++;if(!ok)throw new AssertionError(msg);}
 static RogueBonuses rogue(RogueCatalog.Focus f,String...cards){return new RogueBonuses(f,Set.of(cards));}
 static BattleCombatant actor(boolean enemy){return new BattleCombatant(UUID.randomUUID(),enemy?"Enemy":"Player",enemy?BattleSide.ENEMIES:BattleSide.PLAYERS,new PersonaInstance(PersonaRegistry.persona(enemy?"rogue_enemy":"rogue_player").orElseThrow()),1000,1000,100,100);}
 static SkillDefinition skill(String id){return PersonaRegistry.skill(id).orElseThrow();}
 public static final class Player {final net.minecraft.nbt.CompoundTag tag=new net.minecraft.nbt.CompoundTag();public net.minecraft.nbt.CompoundTag getPersistentData(){return tag;}}
 public static void main(String[] args)throws Exception{
  PersonaRegistry.bootstrap();
  PersonaRegistry.registerSkill(new SkillDefinition("rogue_fire","Test fire",Element.FIRE,100,100,7,0,false,SkillTarget.ONE_ENEMY));
  PersonaRegistry.registerSkill(new SkillDefinition("rogue_slash","Test slash",Element.SLASH,100,100,0,9,false,SkillTarget.ONE_ENEMY));
  PersonaRegistry.registerPersona(new PersonaDefinition("rogue_player","Player",Arcana.FOOL,1,new PersonaStats(20,20,20,99,1),Map.of(),Map.of(1,List.of("rogue_fire","rogue_slash","dia","tarunda","rakunda","sukunda","debilitate"))));
  PersonaRegistry.registerPersona(new PersonaDefinition("rogue_enemy","Enemy",Arcana.FOOL,1,new PersonaStats(20,20,20,1,1),Map.of(Element.FIRE,Affinity.WEAK),Map.of()));
  var fire=skill("rogue_fire");var slash=skill("rogue_slash");var weak=rogue(RogueCatalog.Focus.WEAKNESS);var none=RogueBonuses.NONE;
  check(weak.damage(fire)==.5&&weak.damage(slash)==.5,"half damage both attack types");
  check(weak.sp(fire,7)==4&&weak.hp(slash,9)==5&&weak.sp(fire,0)==0&&weak.hp(slash,1)==1,"half actual payments ceil and nonzero minimum");
  check(weak.sp(skill("dia"),4)==4&&weak.sp(skill("tarunda"),8)==8,"no discount for healing or support");
  check(rogue(RogueCatalog.Focus.DPS_PHYSICAL).damage(slash)==1.10&&rogue(RogueCatalog.Focus.DPS_PHYSICAL).damage(fire)==1,"physical focus only");
  check(rogue(RogueCatalog.Focus.DPS_MAGIC).damage(fire)==1.10&&rogue(RogueCatalog.Focus.DPS_MAGIC).damage(slash)==1,"magic focus only");
  for(int max:new int[]{1,9,101,120,1000})for(int percent:new int[]{0,1,7,50,100})for(int sp:new int[]{0,1,7,100}){
   if(percent>0&&sp>0)continue;var s=new SkillDefinition("payment","Payment",Element.SLASH,10,100,sp,percent,false,SkillTarget.ONE_ENEMY);
   var a=new BattleCombatant(UUID.randomUUID(),"A",BattleSide.PLAYERS,new PersonaInstance(PersonaRegistry.persona("rogue_player").orElseThrow()),max,max,100,100).rogue(weak);
   var payment=SkillPayment.of(s,max,a.equipment(),weak);check(payment.sp()==a.spCost(s)&&payment.hp()==a.hpPayment(s),"HUD and server costs identical");
   int hp=a.hp(),mana=a.sp();boolean expected=payment.affordable(hp,mana);check(a.spendSkill(s)==expected,"payment affordability");check(a.hp()==hp-(expected?payment.hp():0)&&a.sp()==mana-(expected?payment.sp():0),"atomic nonlethal charge");
  }
  var master=new EquipmentBonuses(List.of(new EquipmentCatalog.Gear("test","Test",EquipmentCatalog.Slot.ACCESSORY,EquipmentCatalog.Family.NONE,0,1,1,1,"+Spell Master, +Arms Master")));
  check(SkillPayment.of(slash,101,master,weak).hp()==2&&SkillPayment.of(fire,101,master,weak).sp()==2,"discount combines with legacy masters once");
  var profile=new RogueProfile();profile.focus="weakness";profile.collect("chariot");check(profile.active.isEmpty(),"collection alone has no effect");
  check(profile.activate("chariot")&&!profile.activate("chariot")&&!profile.activate("forged"),"unique known activation");
  check(profile.bonuses(false).equals(none)&&profile.active.size()==1,"disabled mode suspends without deleting");
  var before=profile.bonuses(true);profile.activate("devil");check(before.active().size()==1,"encounter snapshot immutable");
  var original=new Player();RogueService.save(original,profile);var clone=new Player();RogueService.save(clone,RogueService.load(original));check(RogueService.load(clone).active.equals(profile.active)&&RogueService.load(clone).focus.equals("weakness"),"death copy keeps focus and cards");
  original.tag.m_128359_("velvet_eclipse:roguelike_v1","{bad");check(RogueService.load(original).active.isEmpty(),"malformed save safely empty");
  var broken=new Gson().fromJson("{\"focus\":\"forged\",\"active\":[\"star\",\"hax\"],\"collected\":null}",RogueProfile.class).normalized();check(broken.focus.equals("none")&&broken.active.equals(Set.of("star"))&&broken.collected.equals(Set.of("star")),"normalization rejects unknown effects");
  var offers=new ArcanaConfirmation();UUID p1=UUID.randomUUID(),p2=UUID.randomUUID();var o=offers.open(p1,"star",2,100);
  check(offers.take(p2,o.token(),101)==null&&offers.take(p1,"forged",101)==null&&offers.peek(p1,101)!=null,"tokens bound to player");
  check(offers.take(p1,o.token(),101).slot()==2&&offers.take(p1,o.token(),101)==null,"single use token");o=offers.open(p1,"star",40,100);check(offers.take(p1,o.token(),1301)==null,"expiry");
  o=offers.open(p1,"star",2,100);offers.clear(p1);check(offers.take(p1,o.token(),101)==null,"cancel has no activation");
  for(boolean enabled:new boolean[]{false,true})for(boolean editable:new boolean[]{false,true})for(String held:List.of("star","devil","")){var d=new RogueProfile();boolean expected=enabled&&editable&&held.equals("star");check(d.activateFromItem("star",held,enabled,editable)==expected&&d.active.size()==(expected?1:0),"moved/missing card, disabled mode and combat reject without effect");if(expected)check(!d.activateFromItem("star",held,true,true),"duplicate cannot consume another item");}
  var healer=actor(false).rogue(rogue(RogueCatalog.Focus.SUPPORT_HEAL));var target=actor(false);target.damage(600);int hp=target.hp();SkillEffects.apply(skill("dia"),healer,target,new Random(1));var plain=actor(false);plain.damage(600);int base=plain.hp();SkillEffects.apply(skill("dia"),actor(false),plain,new Random(1));check(target.hp()-hp==Math.round((plain.hp()-base)*1.15),"support heals actual skill");
  for(String id:List.of("tarunda","rakunda","sukunda","debilitate")){
   var a=actor(false).rogue(rogue(RogueCatalog.Focus.SUPPORT_DEBUFF,"hermit"));var enemy=actor(true);SkillEffects.apply(skill(id),a,enemy,new Random(1));
   check((id.equals("tarunda")?enemy.attackTurns():id.equals("rakunda")?enemy.defenseTurns():enemy.accuracyTurns())==5,"debuff class plus card extends "+id);
  }
  var a=actor(false).rogue(rogue(RogueCatalog.Focus.SUPPORT_DEBUFF));var enemy=actor(true);enemy.attackStatus(true);SkillEffects.apply(skill("tarunda"),a,enemy,new Random(1));check(enemy.attackTurns()==0,"debuff cancellation never resurrects a timer");
  var defender=actor(true).rogue(rogue(RogueCatalog.Focus.NONE,"devil"));defender.damage(100);check(defender.hp()==880,"pact incoming drawback");
  for(String card:List.of("star","devil")){defender=actor(true).rogue(rogue(RogueCatalog.Focus.NONE,card));int initial=defender.hp();var hit=DamageCalculator.resolve(actor(false),defender,slash,new Random(3));check(initial-defender.hp()==hit.damage(),"result displays actual Arcana damage taken");}
  for(var focus:RogueCatalog.Focus.values())for(var offensive:List.of(slash,fire)){var attacker=actor(false).rogue(rogue(focus));var raw=DamageCalculator.roll(actor(false),actor(true),offensive,new Random(2));var changed=DamageCalculator.roll(attacker,actor(true),offensive,new Random(2));check(Math.abs(changed.damage()-raw.damage()*attacker.rogue().damage(offensive))<=2,"real damage calculator uses class focus");}
  for(boolean pact:new boolean[]{false,true}){
   a=actor(false).rogue(pact?rogue(RogueCatalog.Focus.WEAKNESS,"chariot"):weak);enemy=actor(true);var session=new BattleSession(List.of(a,enemy),new Random(3));int mana=a.sp(),enemyHp=enemy.hp();var action=BattleAction.skill("rogue_fire",enemy.id());
   check(session.prepare(a.id(),action)!=null&&a.sp()==mana&&enemy.hp()==enemyHp,"animation preparation is mutation free");var result=session.perform(a.id(),action);
   check(result.accepted()&&result.weaknessHit()&&result.hits().get(enemy.id()).newlyDowned()&&result.oneMore()!=pact,"pact only removes One More; real weakness remains: "+result);check(a.sp()==mana-4,"half SP charged at impact");
  }
  var leader=actor(false);var ally=actor(false).rogue(rogue(RogueCatalog.Focus.NONE,"chariot"));enemy=actor(true);var battle=new BattleSession(List.of(leader,ally,enemy),new Random(4));var first=battle.currentActor();first.rogue(none);var other=first==leader?ally:leader;other.rogue(rogue(RogueCatalog.Focus.NONE,"chariot"));battle.perform(first.id(),BattleAction.skill("rogue_fire",enemy.id()));
  check(battle.oneMore()&&!battle.batonTargets().contains(other)&&battle.canAllOut(first.id())&&battle.allOutFighters().contains(other),"no Shift to pact; still joins ally All-Out");
  check(weak.damage(battle.allOutSkill())==1,"All-Out unchanged");
  var basic=new SkillDefinition("__basic_attack","Attack",Element.SLASH,10,100,0,0,false,SkillTarget.ONE_ENEMY);check(weak.damage(basic)==.5&&rogue(RogueCatalog.Focus.DPS_PHYSICAL,"strength").damage(basic)==1,"basic attack roles correctly scoped");
  int drops=0;var ids=new HashSet<String>();var random=new Random(402);for(int i=0;i<10000;i++){String id=RogueCatalog.roll(random);if(!id.isEmpty()){drops++;ids.add(id);check(RogueCatalog.card(id)!=null,"known drop");}}check(drops>2300&&drops<2700&&ids.size()==9,"random 25 percent, not guaranteed");
  check(RoguePermissions.owner(p1,p1,"")&&!RoguePermissions.owner(p2,p1,p2.toString())&&!RoguePermissions.owner(p1,null,"")&&RoguePermissions.owner(p1,null,p1.toString()),"owner identity, no first join or OP assumption");
  var dispatcher=new CommandDispatcher<String>();dispatcher.register(LiteralArgumentBuilder.<String>literal("gamerule").then(LiteralArgumentBuilder.<String>literal("velvetRoguelike").executes(c->1).then(RequiredArgumentBuilder.<String,Boolean>argument("value",BoolArgumentType.bool()).executes(c->1))));
  dispatcher.register(LiteralArgumentBuilder.<String>literal("execute").then(LiteralArgumentBuilder.<String>literal("run").redirect(dispatcher.getRoot())));
  for(String cmd:List.of("gamerule velvetRoguelike true","execute run gamerule velvetRoguelike false"))check(RoguePermissions.writesRule(dispatcher.parse(cmd,"owner").getContext()),"direct/wrapped rule write identified");check(!RoguePermissions.writesRule(dispatcher.parse("gamerule velvetRoguelike","owner").getContext()),"read is not blocked");
  // UI: prompt first selection is Cancel; disabled/guest cannot toggle; all hit boxes fit.
  var state=new RogueNetwork.State(false,true,false,true,"weakness",List.of("star"),List.of("star"),"","","");
  for(int page=0;page<3;page++){var v=new RogueDesign.View(state,page,0,0,false);var buttons=RogueDesign.buttons(v);for(int i=0;i<buttons.size();i++){var b=buttons.get(i);var r=b.rect();check(r.x()>=0&&r.y()>=0&&r.x()+r.w()<=900&&r.y()+r.h()<=500,"UI bounds");check(RogueDesign.hit(v,r.x()+r.w()/2,r.y()+r.h()/2)==i,"drawing and mouse agree");if(b.action().equals("toggle"))check(!b.enabled(),"guest toggle disabled");}}
  var prompt=new RogueNetwork.State(true,true,true,true,"weakness",List.of("star"),List.of(),"","star","token");check(RogueDesign.buttons(new RogueDesign.View(prompt,0,0,0,false)).get(0).action().equals("cancel"),"confirmation defaults to cancel");
  for(int i=0;i<MenuDesign.TABS.length;i++){double angle=Math.toRadians(-16+i*.8),x=526+Math.sin(i*.55)*23+25*Math.cos(angle)-16*Math.sin(angle),y=140+i*38+25*Math.sin(angle)+16*Math.cos(angle);check(ReloadMenuDesign.pickHome(x*ReloadMenuDesign.SX,y*ReloadMenuDesign.SY)==i,"Tab home clickable option "+i);}
  for(var card:RogueCatalog.CARDS){check(com.gx.velveteclipse.room.CreativeCategory.of("arcana_buff_"+card.id(),false)==com.gx.velveteclipse.room.CreativeCategory.ARCANA_BUFFS,"creative category");}
  check(Files.readString(Path.of("src/main/java/com/gx/velveteclipse/roguelike/RogueRules.java")).contains("create.invoke(null,false,changed)"),"gamerule registered off by default");
  Path out=Path.of("build/reports/roguelike");Files.createDirectories(out);Files.writeString(out.resolve("validation.json"),new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("checks",checks,"passed",true,"inGame",false,"dropSample",drops)));
  var wire=new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());try{var request=new RogueNetwork.Request("confirm",UUID.randomUUID().toString());RogueNetwork.Request.encode(request,wire);check(request.equals(RogueNetwork.Request.decode(wire)),"request codec round trip");wire.clear();var response=new RogueNetwork.Response(new Gson().toJson(prompt));RogueNetwork.Response.encode(response,wire);check(response.equals(RogueNetwork.Response.decode(wire)),"state codec round trip");}finally{wire.release();}
  RoguePreview.export(out);
  for(var card:RogueCatalog.CARDS){var resource=Path.of("src/main/resources/assets/velvet_eclipse");check(Files.exists(resource.resolve("models/item/arcana_buff_"+card.id()+".json"))&&Files.exists(resource.resolve("textures/item/arcana_buff_"+card.id()+".png")),"physical item assets");}
  Files.writeString(out.resolve("validation.json"),new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("checks",checks,"passed",true,"inGame",false,"dropSample",drops)));
  System.out.println("PASS roguelike: "+checks+" offline checks, costs/classes/pacts/weakness/persistence/loot/permissions/confirmation/layout; no Minecraft and no JAR.");
 }
}
