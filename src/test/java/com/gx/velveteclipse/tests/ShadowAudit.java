package com.gx.velveteclipse.tests;

import com.gx.velveteclipse.shadow.*;
import com.gx.velveteclipse.core.battle.*;
import com.gx.velveteclipse.core.persona.*;
import com.gx.velveteclipse.darkhour.*;
import java.util.*;
import java.nio.file.*;
import com.google.gson.*;

/** Focused offline regression for perception, openings, finite encounters and native assets. */
public final class ShadowAudit {
 private static int checks;
 private static void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
 private static BattleCombatant fighter(int id,BattleSide side,String persona,Set<String> skills){return new BattleCombatant(new UUID(0,id),"Fighter "+id,side,new PersonaInstance(persona,15,0,skills),5000,5000,200,200);}
 public static void main(String[] args)throws Exception{
  PersonaRegistry.bootstrap();awareness();openings();magic();encounters();population();assets();reloadAssets();escapeSlots();
  System.out.println("PASS Shadows: "+checks+" checks; recognition/search, front/back, party opening order, enemy magic/One More, no enemy All-Out, stable 1..5 rosters, rare rewards, one Shadow per room and GeckoLib model/animation bake. Offline only.");
 }
 private static void awareness(){
  var brain=new ShadowAwareness();for(int i=0;i<39;i++){brain.update(true,false,true);check(brain.phase==ShadowAwareness.Phase.NOTICE,"recognition must wait two full seconds");}brain.update(true,false,true);check(brain.phase==ShadowAwareness.Phase.CHASE,"40 visible ticks confirm target");
  brain.reset();for(int i=0;i<25;i++)brain.update(true,false,true);brain.update(false,false,true);check(brain.phase==ShadowAwareness.Phase.SEARCH&&brain.seen==0,"lost line of sight cancels partial recognition");for(int i=0;i<39;i++)brain.update(true,false,true);check(brain.phase==ShadowAwareness.Phase.NOTICE,"must reacquire for full duration");brain.update(true,false,true);brain.attack();for(int i=0;i<12;i++)brain.update(true,true,true);check(brain.phase==ShadowAwareness.Phase.ATTACK&&brain.ticks==12,"attack windup preserved");
  brain=new ShadowAwareness();brain.update(false,true,true);check(brain.phase==ShadowAwareness.Phase.SEARCH,"close unseen player triggers search");for(int i=0;i<60;i++)brain.update(false,false,false);check(brain.phase==ShadowAwareness.Phase.IDLE,"search times out");
  for(int yaw:new int[]{0,90,180,270,-90}){double x=-Math.sin(Math.toRadians(yaw)),z=Math.cos(Math.toRadians(yaw));check(ShadowAwareness.front(yaw,x,z)&&!ShadowAwareness.behind(yaw,x,z),"front cone rotates with body");check(ShadowAwareness.behind(yaw,-x,-z)&&!ShadowAwareness.front(yaw,-x,-z),"rear advantage rotates with body");check(ShadowAwareness.front(yaw+180,-x,-z),"180 degree search faces rear player");}
 }
 private static void openings(){
  var firstActors=new HashSet<BattleSide>();
  for(var mode:List.of(EncounterOpening.ADVANTAGE,EncounterOpening.AMBUSH,EncounterOpening.NEUTRAL))for(int seed=0;seed<50;seed++){
   var fighters=new ArrayList<BattleCombatant>();for(int i=1;i<=3;i++)fighters.add(fighter(i,BattleSide.PLAYERS,"cinder_wisp",Set.of("ember")));for(int i=10;i<15;i++)fighters.add(fighter(i,BattleSide.ENEMIES,"umbra_slime",Set.of("shade_bite")));
   var s=new BattleSession(fighters,new Random(seed),mode);if(mode==EncounterOpening.NEUTRAL)firstActors.add(s.currentActor().side());
   int firstCount=mode==EncounterOpening.ADVANTAGE?3:5;var first=mode==EncounterOpening.ADVANTAGE?BattleSide.PLAYERS:BattleSide.ENEMIES;var seen=new HashSet<UUID>();
   for(int i=0;i<8;i++){var a=s.currentActor();check(seen.add(a.id()),"each fighter gets exactly one opening action");if(mode!=EncounterOpening.NEUTRAL&&i<firstCount)check(a.side()==first,"entire favored team acts before other team");s.perform(a.id(),BattleAction.guard());}
   var expected=fighters.stream().sorted(Comparator.<BattleCombatant>comparingInt(c->c.stats().agility()).reversed().thenComparing(c->c.id().toString())).findFirst().orElseThrow();check(s.currentActor()==expected,"opening advantage ends after first round");
  }
  check(firstActors.size()==2,"neutral can begin with either side");
  var p=fighter(1,BattleSide.PLAYERS,"cinder_wisp",Set.of("ember"));var e=fighter(10,BattleSide.ENEMIES,"umbra_slime",Set.of("shade_bite"));var s=new BattleSession(List.of(p,e),new Random(1),EncounterOpening.ADVANTAGE);
  var ally=fighter(2,BattleSide.PLAYERS,"cinder_wisp",Set.of("ember"));check(s.join(ally)&&s.currentActor()==p,"joining never steals reserved turn");s.perform(p.id(),BattleAction.guard());check(s.currentActor()==ally,"joining party shares rear advantage");s.perform(ally.id(),BattleAction.guard());check(s.currentActor()==e,"enemy follows all opening allies");
 }
 private static void magic(){
  var p=fighter(1,BattleSide.PLAYERS,"cinder_wisp",Set.of("ember"));var enemy=fighter(10,BattleSide.ENEMIES,"umbra_slime",Set.of("frost_shard","cleave"));var s=new BattleSession(List.of(p,enemy),new Random(){@Override public int nextInt(int n){return 50%n;}},EncounterOpening.AMBUSH);
  int sp=enemy.sp();var result=s.perform(enemy.id(),BattleAction.skill("frost_shard",p.id()));check(result.oneMore()&&s.currentActor()==enemy&&p.downed(),"enemy weakness magic grants One More");check(enemy.sp()==sp-4,"enemy spell pays SP");check(!s.canAllOut(enemy.id())&&s.prepare(enemy.id(),BattleAction.allOut())==null,"enemy cannot use All-Out even against fully downed party");
  enemy.damage(enemy.hp()-1);enemy.spendSp(enemy.sp());for(int seed=0;seed<30;seed++)check(EnemyTactics.choose(enemy,new Random(seed))==null,"unaffordable spells and HP attacks fall back to basic attack");
  var mixed=fighter(11,BattleSide.ENEMIES,"umbra_slime",Set.of("frost_shard","cleave"));int magic=0,basic=0;for(int seed=0;seed<200;seed++){var skill=EnemyTactics.choose(mixed,new Random(seed));if(skill==null)basic++;else if(skill.usesMagicStat())magic++;}check(magic>0&&basic>0,"enemy can select both magic and physical attacks");
 }
 private static void encounters(){
  int red=0;var sizes=new HashSet<Integer>();var types=new HashSet<String>();
  for(int seed=0;seed<2000;seed++){
   if(ShadowEncounterRules.rare(seed))red++;int floor=seed%40+1;var roster=ShadowEncounterRules.roll(seed,floor,false);var rare=ShadowEncounterRules.roll(seed,floor,true);sizes.add(roster.size());check(roster.size()>=1&&roster.size()<=5&&roster.equals(ShadowEncounterRules.roll(seed,floor,false)),"roster persists across escape/re-entry");
   for(int i=0;i<roster.size();i++){var e=roster.get(i);types.add(e.persona());check(ShadowEncounterRules.ORDINARY.contains(e.persona())&&PersonaRegistry.persona(e.persona()).isEmpty()&&EnemyCatalog.pool(floor).contains(EnemyCatalog.get(e.persona())),"ordinary floor appropriate enemy only");check(rare.get(i).level()>e.level()&&rare.get(i).hp()>e.hp()*2,"red encounter significantly stronger");check(e.skills().stream().allMatch(id->PersonaRegistry.skill(id).isPresent()),"every enemy uses registered skills");}
  }
  check(red>=70&&red<=130,"rare spawn rate approximately five percent");check(sizes.size()==5&&types.size()==16,"all encounter sizes and ordinary types reachable");check(ShadowEncounterRules.xp(10,3,true)==ShadowEncounterRules.xp(10,3,false)*3&&ShadowEncounterRules.yen(10,3,true)==ShadowEncounterRules.yen(10,3,false)*4,"rare reward multipliers");check(ShadowEncounterRules.yen(1,1,false)>0,"ordinary victories grant yen");
 }
 private static void population(){
  var quota=new TartarusPopulation();quota.range(64,64);
  for(int floor:List.of(1,9,10,25,26,50,250)){var plan=TartarusInteriorLayout.floor(763,floor);var positions=TartarusPopulation.positions(plan);check(new HashSet<>(positions).size()==positions.size(),"unique rooms only");check(quota.count(763,floor)<=positions.size(),"configured high population never exceeds rooms");}
  var plan=TartarusInteriorLayout.floor(763,1);var rooms=new HashSet<String>();for(var p:TartarusPopulation.positions(plan))check(rooms.add(Math.floorDiv(p.x()+48,24)+":"+Math.floorDiv(p.z()+48,24)),"one Shadow per logical room");
  var legacy=new net.minecraft.nbt.CompoundTag();legacy.m_128405_("min",10);legacy.m_128405_("max",20);var counts=new net.minecraft.nbt.CompoundTag();counts.m_128405_("1",20);legacy.m_128365_("counts",counts);check(TartarusPopulation.load(legacy).count(0,1)==10,"legacy 10..20 population migrates to the existing 5..10 range");
 }
 private static void assets()throws Exception{
  var root=Path.of("src/main/resources/assets/velvet_eclipse");var raw=Files.readString(root.resolve("geo/entity/field_shadow.geo.json"));var model=software.bernie.geckolib.util.JsonUtil.GEO_GSON.fromJson(raw,software.bernie.geckolib.loading.json.raw.Model.class);var baked=software.bernie.geckolib.loading.object.BakedModelFactory.DEFAULT_FACTORY.constructGeoModel(software.bernie.geckolib.loading.object.GeometryTree.fromModel(model));
  check(baked.getBone("crown").isPresent()&&baked.getBone("eyes").isPresent(),"native model bakes articulated silhouette");var names=new HashSet<String>();var geo=JsonParser.parseString(raw).getAsJsonObject().getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();
  for(var b:geo.getAsJsonArray("bones")){var bone=b.getAsJsonObject();check(names.add(bone.get("name").getAsString()),"unique bones");if(bone.has("parent"))check(names.contains(bone.get("parent").getAsString()),"valid skeleton parent");if(bone.has("cubes"))for(var value:bone.getAsJsonArray("cubes")){var c=value.getAsJsonObject();var size=c.getAsJsonArray("size");for(var n:size)check(n.getAsInt()>0&&n.getAsDouble()==n.getAsInt(),"integer cuboids");var uv=c.getAsJsonArray("uv");check(uv.get(0).getAsInt()+2*(size.get(0).getAsInt()+size.get(2).getAsInt())<=512&&uv.get(1).getAsInt()+size.get(1).getAsInt()+size.get(2).getAsInt()<=512,"Box UV fits atlas");}}
  var animations=JsonParser.parseString(Files.readString(root.resolve("animations/entity/field_shadow.animation.json"))).getAsJsonObject().getAsJsonObject("animations");var clips=software.bernie.geckolib.util.JsonUtil.GEO_GSON.fromJson(animations,software.bernie.geckolib.loading.object.BakedAnimations.class);
  for(String clip:List.of("misc.idle","action.attack")){check(clips.getAnimation(clip)!=null,"native animation bake "+clip);var a=animations.getAsJsonObject(clip);for(var entry:a.getAsJsonObject("bones").entrySet()){check(names.contains(entry.getKey()),"animation only uses real bones");if(clip.equals("misc.idle"))for(var channel:entry.getValue().getAsJsonObject().entrySet()){var keys=channel.getValue().getAsJsonObject();check(keys.get("0").equals(keys.get("4.8")),"seamless idle loop");}}}
  var attack=clips.getAnimation("action.attack");for(var track:attack.boneAnimations())if(track.boneName().equals("body"))check(Math.abs(IzanagiAudit.sample(track.rotationKeyFrames().xKeyframes(),11)-Math.toRadians(17))<.001,"native attack rotation matches authored forward impact");
  for(String stem:List.of("field_shadow","red_shadow")){check(javax.imageio.ImageIO.read(root.resolve("textures/entity/"+stem+".png").toFile()).getWidth()==512,"texture atlas exists");check(Files.exists(root.resolve("textures/entity/"+stem+"_glowmask.png")),"emissive eyes available");check(Files.exists(root.resolve("models/item/"+stem+"_spawn.json")),"creative placement item model exists");for(String locale:List.of("pt_br","en_us"))check(JsonParser.parseString(Files.readString(root.resolve("lang/"+locale+".json"))).getAsJsonObject().has("item.velvet_eclipse."+stem+"_spawn"),"localized creative item");}
 }
 private static void escapeSlots(){
  var slot=new net.minecraft.nbt.CompoundTag();var source=UUID.randomUUID().toString();var battle=UUID.randomUUID();slot.m_128359_("entity",source);slot.m_128356_("seed",876543);
  TartarusEncounterSlot.begin(slot,battle);check(!TartarusEncounterSlot.release(slot,UUID.randomUUID()),"other battle cannot release a source");
  check(TartarusEncounterSlot.release(slot,battle)&&source.equals(slot.m_128461_("entity"))&&!slot.m_128471_("defeated")&&slot.m_128454_("seed")==876543,"escape keeps source identity, seed and unspent encounter");
  check(!TartarusEncounterSlot.release(slot,battle),"repeated cleanup cannot spend the encounter");
  TartarusEncounterSlot.begin(slot,battle);TartarusEncounterSlot.recover(slot);check(source.equals(slot.m_128461_("entity"))&&slot.m_128461_("battle").isEmpty(),"restart recovers retained source without duplication");
  slot.m_128359_("battle",battle.toString());TartarusEncounterSlot.recover(slot);check(slot.m_128461_("entity").isEmpty(),"legacy discarded pre-upgrade source can recover once");
 }
 private static void reloadAssets()throws Exception{
  var root=Path.of("src/main/resources/assets/velvet_eclipse");check(EnemyCatalog.all().size()==19,"19 dedicated Reload definitions");
  for(var def:EnemyCatalog.all()){
   check(PersonaRegistry.persona(def.id()).isEmpty(),"enemies are not summonable Personas");
   var fighter=BattleCombatant.enemy(UUID.randomUUID(),def.displayName(),def,def.level(),def.hp(),def.sp(),def.skills());
   check(fighter.stats().equals(def.stats())&&fighter.attackElement()==def.attack(),"enemy combat attributes independent from PersonaRegistry");
   for(var element:Element.values())check(fighter.affinity(element)==def.affinity(element),"runtime enemy affinities match data");
   var raw=Files.readString(root.resolve("geo/entity/"+def.asset()+".geo.json"));var model=software.bernie.geckolib.util.JsonUtil.GEO_GSON.fromJson(raw,software.bernie.geckolib.loading.json.raw.Model.class);
   var baked=software.bernie.geckolib.loading.object.BakedModelFactory.DEFAULT_FACTORY.constructGeoModel(software.bernie.geckolib.loading.object.GeometryTree.fromModel(model));check(baked.getBone("body").isPresent(),"native enemy model bakes "+def.id());
   var json=JsonParser.parseString(raw).getAsJsonObject().getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();var bones=new HashSet<String>();
   for(var value:json.getAsJsonArray("bones")){var b=value.getAsJsonObject();bones.add(b.get("name").getAsString());if(b.has("cubes"))for(var cube:b.getAsJsonArray("cubes")){var c=cube.getAsJsonObject();var size=c.getAsJsonArray("size");var uv=c.getAsJsonArray("uv");check(uv.get(0).getAsDouble()+2*(size.get(0).getAsDouble()+size.get(2).getAsDouble())<=512&&uv.get(1).getAsDouble()+size.get(1).getAsDouble()+size.get(2).getAsDouble()<=512,"enemy Box UV inside texture");}}
   var animations=JsonParser.parseString(Files.readString(root.resolve("animations/entity/"+def.asset()+".animation.json"))).getAsJsonObject().getAsJsonObject("animations");
   var clips=software.bernie.geckolib.util.JsonUtil.GEO_GSON.fromJson(animations,software.bernie.geckolib.loading.object.BakedAnimations.class);
   for(String clip:List.of("misc.idle","action.physical","action.magic")){check(clips.getAnimation(clip)!=null,"idle/physical/magic animation bakes");for(String b:animations.getAsJsonObject(clip).getAsJsonObject("bones").keySet())check(bones.contains(b),"animation uses authored skeleton");}
   check(javax.imageio.ImageIO.read(root.resolve("textures/entity/"+def.asset()+".png").toFile()).getWidth()==512,"enemy texture");check(Files.exists(root.resolve("models/item/"+def.asset()+"_spawn.json")),"creative item model");check(Files.exists(root.resolve("textures/item/"+def.asset()+"_spawn.png")),"creative icon");
   for(String locale:List.of("pt_br","en_us"))check(JsonParser.parseString(Files.readString(root.resolve("lang/"+locale+".json"))).getAsJsonObject().has("item.velvet_eclipse."+def.asset()+"_spawn"),"creative enemy translated");
  }
  check(EnemyCatalog.get("wealth_hand").affinity(Element.PHYSICAL)==Affinity.WEAK&&EnemyCatalog.get("wealth_hand").affinity(Element.SLASH)==Affinity.NORMAL,"Reload Strike is distinct from Slash");
  check(EnemyCatalog.get("laughing_table").affinity(Element.PIERCE)==Affinity.RESIST&&EnemyCatalog.get("grieving_tiara").affinity(Element.ICE)==Affinity.NULL,"Pierce resistance and ice immunity");
  check(EnemyCatalog.pool(9).stream().noneMatch(EnemyDefinition::guardian),"bosses excluded from random encounters");
  var e=EnemyCatalog.get("obsessed_cupid");var enemy=BattleCombatant.enemy(UUID.randomUUID(),e.displayName(),e,e.level(),999,99,e.skills());var player=fighter(99,BattleSide.PLAYERS,"cinder_wisp",Set.of("ember"));var session=new BattleSession(List.of(player,enemy),new Random(1),EncounterOpening.AMBUSH);
  var action=session.prepare(enemy.id(),BattleAction.attack(player.id()));check(action!=null&&action.hits().values().stream().allMatch(h->h.element()==Element.PIERCE),"enemy basic attack uses its own damage type");
 }

}
