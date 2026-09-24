package com.gx.velveteclipse.tests;

import com.gx.velveteclipse.core.persona.*;
import com.gx.velveteclipse.core.battle.*;
import com.gx.velveteclipse.core.fusion.*;
import com.gx.velveteclipse.forge.PlayerPersonaData;
import com.gx.velveteclipse.shuffle.*;
import com.gx.velveteclipse.client.SpellImpact;
import com.gx.velveteclipse.network.*;
import com.google.gson.*;
import java.nio.file.*;
import java.util.*;

/** Focused integration checks for the Reload catalogue, transactions and runtime assets. */
public final class ReloadCatalogueAudit {
 private static int checks;
 private static final Path ASSETS=Path.of("src/main/resources/assets/velvet_eclipse"),REPORT=Path.of("build/reports/reload-catalogue");
 private static final Random HIT=new Random(){@Override public int nextInt(int n){return 0;}@Override public double nextDouble(){return 0;}};
 private static void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
 private static PersonaInstance persona(String id){return new PersonaInstance(PersonaRegistry.persona(id).orElseThrow());}
 private static BattleCombatant fighter(int id,Set<String> skills){return new BattleCombatant(new UUID(0,id),"Fighter "+id,id<10?BattleSide.PLAYERS:BattleSide.ENEMIES,new PersonaInstance("orpheus",60,0,skills),4000,4000,500,500);}
 private static PlayerPersonaData stock(String... ids){var d=new PlayerPersonaData();while(d.expandPersonaCapacity()){}d.setPlayerLevel(99);for(String id:ids)check(d.givePersona(id),"acquire "+id);return d;}
 public static final class Player {final net.minecraft.nbt.CompoundTag tag=new net.minecraft.nbt.CompoundTag();public net.minecraft.nbt.CompoundTag getPersistentData(){return tag;}}
 public static void main(String[] args)throws Exception{Files.createDirectories(REPORT);catalogue();combat();fusion();compendium();shuffle();assets();effects();System.out.println("PASS Reload catalogue: "+checks+" checks; "+ReloadCatalogue.ids().size()+" Personas, "+PersonaRegistry.skills().size()+" skills, party fusion, two-skill inheritance, Compendium, floor pools, GeckoLib and VFX.");}
 private static void catalogue()throws Exception{
  check(ReloadCatalogue.ids().size()==173,"173 base-game compendium Personas");check(PersonaRegistry.persona("izanagi").isPresent()&&PersonaRegistry.persona("arsene").isPresent(),"approved extras preserved");
  var translated=JsonParser.parseString(Files.readString(ASSETS.resolve("lang/pt_br.json"))).getAsJsonObject();
  for(var p:PersonaRegistry.personas().values())for(var group:p.learnset().values())for(String id:group)check(PersonaRegistry.skill(id).isPresent(),"learnset skill exists: "+p.id()+"/"+id);
  for(var s:PersonaRegistry.skills().values()){
   var item=ASSETS.resolve("models/item/skill_card_"+s.id()+".json");check(Files.exists(item),"card model "+s.id());check(translated.has("item.velvet_eclipse.skill_card_"+s.id()),"card translation "+s.id());
   var info=ReloadCatalogue.info(s.id());if(info==null)continue;
   check(!SkillDescriptions.describe(s).isBlank(),"explanation for "+s.id());
   check(info.minHits()>0&&info.maxHits()>=info.minHits(),"hit range "+s.id());
   if(!s.isPassive()&&!s.isDamageSkill()){var actor=fighter(1,Set.of(s.id()));var target=fighter(s.target()==SkillTarget.ONE_ENEMY||s.target()==SkillTarget.ALL_ENEMIES?10:2,Set.of());if(SkillEffects.revival(s))target.damage(4000);SkillEffects.apply(s,actor,target,HIT);check(true,"active effect implemented "+s.id());}
   if(s.isPassive()){var a=fighter(1,Set.of(s.id()));var t=fighter(10,Set.of());var battle=new BattleSession(List.of(a,t),HIT,EncounterOpening.ADVANTAGE);check(battle.prepare(a.id(),BattleAction.skill(s.id(),a.id()))==null,"passive cannot be cast "+s.id());}
  }
 }
 private static void combat(){
  var a=fighter(1,Set.of("spirit_drain"));var b=fighter(10,Set.of());a.drainSp(100);var spell=PersonaRegistry.skill("spirit_drain").orElseThrow();var roll=DamageCalculator.roll(a,b,spell,HIT);check(b.hp()==4000&&b.sp()==500&&a.sp()==400,"drain preparation is pure");var hit=DamageCalculator.apply(a,b,roll);SkillEffects.afterHit(spell,a,b,hit,HIT);check(b.hp()==4000&&b.sp()==480&&a.sp()==420,"Spirit Drain transfers only SP, once");
  a=fighter(1,Set.of("mudo"));b=fighter(10,Set.of("survival_trick"));spell=PersonaRegistry.skill("mudo").orElseThrow();roll=DamageCalculator.roll(a,b,spell,HIT);check(b.hp()==4000&&roll.effect().equals("instant_guard"),"survival reserved without mutation");DamageCalculator.apply(a,b,roll);check(b.hp()==1,"survival trick saves lethal instant hit");DamageCalculator.resolve(a,b,spell,HIT);check(!b.alive(),"survival once per battle");
  b=fighter(10,Set.of("survival_trick"));b.damage(5000);check(!b.alive(),"Survival Trick does not save ordinary damage");
  b=fighter(10,Set.of("firm_stance"));b.setGuarding(true);b.damage(0);check(b.hp()==4000,"null damage stays zero when guarded");
  b=fighter(10,Set.of("null_fire"));check(b.affinity(Element.FIRE)==Affinity.NULL,"passive null applies");b.breakElement(Element.FIRE);check(b.affinity(Element.FIRE)==Affinity.NORMAL,"break suppresses passive null");for(int t=0;t<4;t++)b.beginTurn();check(b.affinity(Element.FIRE)==Affinity.NULL,"break expires");b.breakElement(Element.DARK);check(b.affinity(Element.DARK)==Affinity.WEAK,"break preserves weakness");
  a=fighter(1,Set.of("spell_master","arms_master","fire_amp"));check(a.spCost(12)==6&&a.hpCost(20)==10&&a.equipment().damage(Element.FIRE,true)==1.5,"equipped Persona passives alter costs and damage");a.setPersona(persona("pixie"));check(a.spCost(12)==12&&a.hpCost(20)==20,"switch removes passives");
  for(String id:List.of("agi","bufu","garu","kouha","eiga","megido","power_slash","spirit_drain","mudo"))for(int seed=0;seed<4;seed++){
   var first=fighter(1,Set.of(id));var target=fighter(10,Set.of());var clone=fighter(1,Set.of(id));var other=fighter(10,Set.of());
   var direct=new BattleSession(List.of(first,target),new Random(seed),EncounterOpening.ADVANTAGE);var delayed=new BattleSession(List.of(clone,other),new Random(seed),EncounterOpening.ADVANTAGE);var action=BattleAction.skill(id,target.id());
   check(delayed.prepare(clone.id(),action)!=null,"prepare "+id);check(first.hp()==clone.hp()&&first.sp()==clone.sp()&&target.hp()==other.hp(),"no early damage or spend "+id);direct.perform(first.id(),action);delayed.perform(clone.id(),action);check(first.hp()==clone.hp()&&first.sp()==clone.sp()&&target.hp()==other.hp()&&target.sp()==other.sp(),"reserved outcome identical "+id);
  }
  var reviver=fighter(1,Set.of("samarecarm"));var fallen=fighter(2,Set.of());fallen.damage(5000);var enemy=fighter(10,Set.of());var battle=new BattleSession(List.of(reviver,fallen,enemy),HIT);check(battle.validSkillTarget(reviver,fallen.id(),PersonaRegistry.skill("samarecarm").orElseThrow())==fallen,"revival accepts fallen ally");SkillEffects.apply(PersonaRegistry.skill("samarecarm").orElseThrow(),reviver,fallen,HIT);check(fallen.hp()==fallen.maxHp(),"revival restores HP");
  var data=stock("pixie");var inactive=data.instance("pixie");inactive.inheritSkill("growth_2");data.updatePersona(inactive);var current=data.activeInstance();current.inheritSkill("victory_cry");data.updatePersona(current);data.setResources(1,1);CatalogueRewards.victory(data,100);check(data.hp()==data.maxHp()&&data.sp()==data.maxSp(),"victory cry recovers HP and SP");check(data.instance("pixie").experience()==50,"growth works in stock");
  var snapshot=new BattleSnapshotS2C(true,true,"orpheus","Orpheus",80,100,40,50,"Enemy",10,10,0,List.of("agi"),List.of("orpheus"),"",List.of("spell_master"));check(BattleSnapshotS2C.fromPayload(snapshot.toPayload()).equals(snapshot),"passive costs travel in battle snapshot");
 }
 private static void fusion(){
  for(var recipe:ReloadCatalogue.special().entrySet()){
   var ingredients=recipe.getValue().stream().map(ReloadCatalogueAudit::persona).toList();var r=FusionService.preview(ingredients,recipe.getKey()).orElseThrow();check(r.personaId().equals(recipe.getKey()),"special ingredients: "+recipe.getKey());check(FusionService.preview(ingredients.subList(1,ingredients.size()),recipe.getKey()).isEmpty(),"incomplete special rejected");
  }
  var ids=new ArrayList<>(ReloadCatalogue.ids());var rng=new Random(812);for(int i=0;i<150;i++){var a=persona(ids.get(rng.nextInt(ids.size())));var b=persona(ids.get(rng.nextInt(ids.size())));check(FusionService.preview(a,b).equals(FusionService.preview(b,a)),"commutative arcana result");}
  UUID first=new UUID(0,1),second=new UUID(0,2);var receiver=stock("angel","silky");var donor=stock("unicorn");var ingredient=receiver.instance("angel");ingredient.inheritSkill("fire_amp");ingredient.inheritSkill("spell_master");receiver.updatePersona(ingredient);var owners=Map.of(first,receiver,second,donor);var sources=List.of(new PartyFusion.Source(first,"angel"),new PartyFusion.Source(first,"silky"),new PartyFusion.Source(second,"unicorn"));
  var fp=FusionTransaction.fingerprint(receiver)+FusionTransaction.fingerprint(donor);var preview=PartyFusion.preview(owners,first,sources,"fortuna",List.of());check(preview!=null&&preview.allowed()&&preview.inheritable().containsAll(List.of("fire_amp","spell_master")),"party preview offers ingredient skills");check(fp.equals(FusionTransaction.fingerprint(receiver)+FusionTransaction.fingerprint(donor)),"preview consumes nothing");
  check(!PartyFusion.commit(owners,first,sources,"fortuna",List.of("fire_amp","spell_master","agi")),"third inherited skill rejected");check(!PartyFusion.commit(owners,first,sources,"fortuna",List.of("armageddon")),"foreign skill rejected");check(!PartyFusion.commit(owners,first,sources,"fortuna",List.of("fire_amp","fire_amp")),"duplicate skill rejected");check(fp.equals(FusionTransaction.fingerprint(receiver)+FusionTransaction.fingerprint(donor)),"rejected transaction atomic");
  check(PartyFusion.commit(owners,first,sources,"fortuna",List.of("fire_amp","spell_master")),"cooperative fusion commits");check(receiver.instance("fortuna").learnedSkills().containsAll(List.of("fire_amp","spell_master")),"both chosen skills inherited");check(receiver.instance("angel")==null&&receiver.instance("silky")==null&&donor.instance("unicorn")==null,"only correct owners lose ingredients");check(donor.rosterIds().size()==1&&receiver.registeredPersona("angel")!=null&&donor.registeredPersona("unicorn")!=null,"donor retains a Persona and consumed ingredients remain registered");check(!PartyFusion.commit(owners,first,sources,"fortuna",List.of()),"replay cannot consume again");
 }
 private static void compendium(){
  var data=stock("pixie");var p=data.instance("pixie");p.setLevel(PersonaRegistry.persona("pixie").orElseThrow(),30);p.inheritSkill("fire_amp");p.inheritSkill("spell_master");data.updatePersona(p);check(data.removePersona("pixie"),"remove ingredient");check(!data.summon("pixie"),"cannot summon without yen");data.addYen(200000);long before=data.yen(),price=PlayerPersonaData.summonPrice(p);check(data.summon("pixie")&&data.yen()==before-price,"summon debits exact price once");check(data.instance("pixie").level()==30&&data.instance("pixie").learnedSkills().contains("fire_amp"),"summon preserves level and inherited skill");check(!data.summon("pixie")&&data.yen()==before-price,"duplicate summon no charge");
  var player=new Player();data.save(player);var loaded=PlayerPersonaData.load(player);check(loaded.registeredPersona("pixie").learnedSkills().equals(p.learnedSkills()),"archive save reload roundtrip");
  player.tag.m_128359_("velvet_eclipse:data_v1","V1|100|120|20|45|cinder_wisp|3|cinder_wisp,12,41,ember+zio|8|4|5000|2|");loaded=PlayerPersonaData.load(player);check(loaded.registeredPersona("cinder_wisp").learnedSkills().equals(Set.of("agi","zio")),"legacy V1 migrates into Compendium");
 }
 private static void shuffle(){
  var random=new Random(321);for(int floor=1;floor<=261;floor++)for(int n=0;n<5;n++){
   var round=ShuffleRules.roll(floor,Set.of(),random);check(round.cards().size()>=1&&round.cards().size()<=5,"bounded Shuffle hand");for(var c:round.cards()){
    if(c.kind()==ShuffleRules.Kind.PERSONA)check(ReloadShuffle.personas(floor).containsKey(c.id())&&c.amount()==PersonaRegistry.persona(c.id()).orElseThrow().baseLevel(),"floor Persona pool "+floor);
    if(c.kind()==ShuffleRules.Kind.SWORDS)check(ReloadCatalogue.info(c.id()).rank()==c.rank()&&c.rank()>0,"rank-gated Skill Card "+floor);
   }
  }
  check(ReloadShuffle.personas(2).keySet().equals(Set.of("pixie","apsaras","angel")),"first-floor pool");check(!ReloadShuffle.cards(1).contains("megidolaon")&&!ReloadShuffle.cards(1).contains("fire_amp"),"endgame skills absent early");
 }
 private static void assets()throws Exception{
  for(String id:ReloadCatalogue.ids()){
   String model=Files.readString(ASSETS.resolve("geo/entity/"+id+".geo.json"));var raw=software.bernie.geckolib.util.JsonUtil.GEO_GSON.fromJson(model,software.bernie.geckolib.loading.json.raw.Model.class);var baked=software.bernie.geckolib.loading.object.BakedModelFactory.DEFAULT_FACTORY.constructGeoModel(software.bernie.geckolib.loading.object.GeometryTree.fromModel(raw));check(!baked.topLevelBones().isEmpty(),"native model bake "+id);
   var clips=JsonParser.parseString(Files.readString(ASSETS.resolve("animations/entity/"+id+".animation.json"))).getAsJsonObject().getAsJsonObject("animations");var animations=software.bernie.geckolib.util.JsonUtil.GEO_GSON.fromJson(clips,software.bernie.geckolib.loading.object.BakedAnimations.class);for(String clip:List.of("misc.idle","action.magic","action.physical"))check(animations.getAnimation(clip)!=null,"runtime clip "+id+" / "+clip);
   check(Files.exists(ASSETS.resolve("textures/entity/"+id+".png")),"texture "+id);var item=ASSETS.resolve("models/item/"+id+"_model.json");check(Files.exists(item),"creative figure "+id);String texture=JsonParser.parseString(Files.readString(item)).getAsJsonObject().getAsJsonObject("textures").get("layer0").getAsString().split(":")[1];check(Files.exists(ASSETS.resolve("textures/"+texture+".png")),"figure texture "+id);
   if(Set.of("pixie","jack_frost","angel","eligor","unicorn","orpheus_telos").contains(id)){var faces=new ArrayList<Object>();for(var bone:baked.topLevelBones())IzanagiAudit.dump(bone,new com.mojang.blaze3d.vertex.PoseStack(),faces);var json=new Gson().toJsonTree(faces).getAsJsonArray();for(var face:json)for(var v:face.getAsJsonObject().getAsJsonArray("vertices")){var vertex=v.getAsJsonArray();vertex.set(3,new JsonPrimitive(vertex.get(3).getAsDouble()/8));vertex.set(4,new JsonPrimitive(vertex.get(4).getAsDouble()/64));}Files.writeString(REPORT.resolve(id+".json"),new Gson().toJson(json));}
  }
 }
 private static void effects()throws Exception{
  for(String id:List.of("agi","eiga","garu","garula","garudyne","magarudyne","kouha","makougaon","bufu","mabufudyne","frei","mafreidyne","psi","mapsiodyne","megido","megidolaon")){
   check(SpellVisuals.supported(id),"VFX supported "+id);for(int age=0;age<23;age+=2){var strokes=SpellImpact.geometry(id,age,23);check(!strokes.isEmpty()&&strokes.size()<1200,"bounded VFX "+id);check(strokes.stream().allMatch(s->s.from().isFinite()&&s.to().isFinite()&&s.width()>0&&s.alpha()>0&&s.alpha()<=1),"finite VFX "+id);}
   var packet=new BattleSpellS2C(id,List.of(new BattleLightningS2C.Strike(5,65,7,23)));var buffer=new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());try{BattleSpellS2C.encode(packet,buffer);check(packet.equals(BattleSpellS2C.decode(buffer)),"VFX codec "+id);}finally{buffer.release();}
   Files.writeString(REPORT.resolve(id+"-effect.json"),new Gson().toJson(SpellImpact.geometry(id,4,23)));
  }
 }
}
