package com.gx.velveteclipse.tests;
import com.gx.velveteclipse.core.persona.*;
import com.gx.velveteclipse.core.battle.*;
import com.gx.velveteclipse.forge.*;
import com.gx.velveteclipse.client.LightningStrike;
import java.util.*;
import java.nio.file.*;
public final class IzanagiAudit {
 static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
 public static void main(String[]args)throws Exception{
  PersonaRegistry.bootstrap();var def=PersonaRegistry.persona("izanagi").orElseThrow();
  check(def.arcana()==Arcana.FOOL&&def.affinity(Element.ELECTRIC)==Affinity.RESIST,"Izanagi registration");
  check(new PersonaInstance(def).learnedSkills().equals(Set.of("zio","izanagi_cleave","rakukaja")),"P4 initial skills");
  check(def.learnset().get(3).equals(List.of("rakunda"))&&def.learnset().get(5).equals(List.of("tarukaja")),"P4 progression");
  var data=new PlayerPersonaData();check(data.givePersona("izanagi"),"add");check(!data.givePersona("izanagi"),"no duplicates");
  check(data.equipPersona("izanagi")&&data.removePersona("izanagi"),"remove active");check(data.activePersona().equals("cinder_wisp"),"active fallback");check(!data.removePersona("cinder_wisp"),"keep last persona");
  var dispatcher=new com.mojang.brigadier.CommandDispatcher<net.minecraft.commands.CommandSourceStack>();VelvetCommands.register(dispatcher);
  for(String verb:List.of("add","remove","give","equip"))check(dispatcher.findNode(List.of("velvet","persona",verb,"id"))!=null,"command "+verb);
  for(double d:new double[]{0,.2,2,5,12}){
   check(Math.abs(MeleeApproach.reachable(d,x->true)-d)<1e-8,"open route");check(MeleeApproach.reachable(d,x->false)==0,"blocked route permits stationary attack");
   double clipped=MeleeApproach.reachable(d,x->x<1.3);check(clipped<1.3&&clipped<=d,"wall/edge clips presentation");
  }
  for(int seed=0;seed<40;seed++){
   var direct=battle(seed);var delayed=battle(seed);var action=BattleAction.skill("mazio",new UUID(0,2));
   var prepared=delayed.prepare(new UUID(0,1),action);check(prepared.hits().size()==2,"Mazio reserves all enemies");
   check(delayed.find(new UUID(0,1)).orElseThrow().sp()==45,"reservation spends nothing");
   var a=direct.perform(new UUID(0,1),action);var b=delayed.perform(new UUID(0,1),action);check(a.equals(b),"AOE reservation preserves outcomes");
   check(delayed.find(new UUID(0,1)).orElseThrow().sp()==35,"Mazio cost once");check(delayed.prepared()==null,"reservation released");
  }
  var dir=Path.of("src/main/resources/assets/velvet_eclipse");
  check(!Files.exists(dir.resolve("meshes/entity/izanagi.mesh.json")),"rejected rounded mesh removed");
  var geometry=com.google.gson.JsonParser.parseString(Files.readString(dir.resolve("geo/entity/izanagi.geo.json"))).getAsJsonObject().getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();
  var rawModel=software.bernie.geckolib.util.JsonUtil.GEO_GSON.fromJson(Files.readString(dir.resolve("geo/entity/izanagi.geo.json")),software.bernie.geckolib.loading.json.raw.Model.class);
  var baked=software.bernie.geckolib.loading.object.BakedModelFactory.DEFAULT_FACTORY.constructGeoModel(software.bernie.geckolib.loading.object.GeometryTree.fromModel(rawModel));
  check(baked.getBone("weapon").isPresent()&&baked.getBone("head").isPresent(),"actual GeckoLib model bake");
  var approved=com.google.gson.JsonParser.parseString(Files.readString(Path.of("tools/izanagi/draft/izanagi.geo.json"))).getAsJsonObject().getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();
  for(var raw:approved.getAsJsonArray("bones")){
   var b=raw.getAsJsonObject();var actual=baked.getBone(b.get("name").getAsString()).orElseThrow();
   var pivot=b.getAsJsonArray("pivot");check(Math.abs(actual.getPivotX()-pivot.get(0).getAsDouble())<.001,"native pivot preserves approved side");
   if(b.has("rotation")){var rot=b.getAsJsonArray("rotation");check(Math.abs(actual.getRotX()-Math.toRadians(rot.get(0).getAsDouble()))<.001&&Math.abs(actual.getRotY()-Math.toRadians(rot.get(1).getAsDouble()))<.001&&Math.abs(actual.getRotZ()-Math.toRadians(rot.get(2).getAsDouble()))<.001,"native pose matches approved Cartesian rotations");}
  }
  var surfaceDump=new ArrayList<Object>();for(var bone:baked.topLevelBones())dump(bone,new com.mojang.blaze3d.vertex.PoseStack(),surfaceDump);
  Files.createDirectories(Path.of("build/reports/izanagi"));Files.writeString(Path.of("build/reports/izanagi/native-geometry.json"),new com.google.gson.Gson().toJson(surfaceDump));
  var names=new HashSet<String>();int cubes=0;
  for(var raw:geometry.getAsJsonArray("bones")) {
   var bone=raw.getAsJsonObject();check(names.add(bone.get("name").getAsString()),"unique bones");
   if(bone.has("parent"))check(names.contains(bone.get("parent").getAsString()),"parent precedes child");
   if(bone.has("cubes"))for(var cube:bone.getAsJsonArray("cubes")){cubes++;for(var n:cube.getAsJsonObject().getAsJsonArray("size"))check(n.getAsDouble()>=0&&Double.isFinite(n.getAsDouble()),"finite cuboid");}
  }
  check(names.size()==34&&cubes==123&&names.containsAll(List.of("weapon","right_hand","left_hand","head")),"approved voxel skeleton");
  var json=com.google.gson.JsonParser.parseString(Files.readString(dir.resolve("animations/entity/izanagi.animation.json"))).getAsJsonObject();
  var bakedAnimations=software.bernie.geckolib.util.JsonUtil.GEO_GSON.fromJson(json.getAsJsonObject("animations"),software.bernie.geckolib.loading.object.BakedAnimations.class);
  for(String clip:List.of("misc.idle","action.magic","action.physical"))check(bakedAnimations.getAnimation(clip)!=null,"actual GeckoLib animation bake "+clip);
  for(String clip:List.of("action.magic","action.physical")){
   var posed=software.bernie.geckolib.loading.object.BakedModelFactory.DEFAULT_FACTORY.constructGeoModel(software.bernie.geckolib.loading.object.GeometryTree.fromModel(rawModel));
   for(var track:bakedAnimations.getAnimation(clip).boneAnimations()){
    var bone=posed.getBone(track.boneName()).orElseThrow();var r=track.rotationKeyFrames();var pos=track.positionKeyFrames();
    bone.setRotX(bone.getRotX()+sample(r.xKeyframes(),28));bone.setRotY(bone.getRotY()+sample(r.yKeyframes(),28));bone.setRotZ(bone.getRotZ()+sample(r.zKeyframes(),28));
    bone.setPosX(sample(pos.xKeyframes(),28));bone.setPosY(sample(pos.yKeyframes(),28));bone.setPosZ(sample(pos.zKeyframes(),28));
   }
   var frame=new ArrayList<Object>();for(var bone:posed.topLevelBones())dump(bone,new com.mojang.blaze3d.vertex.PoseStack(),frame);
   Files.writeString(Path.of("build/reports/izanagi/native-"+clip.substring(7)+".json"),new com.google.gson.Gson().toJson(frame));
  }
  var idle=json.getAsJsonObject("animations").getAsJsonObject("misc.idle");check(idle.get("loop").getAsBoolean(),"looping idle");
  for(var entry:idle.getAsJsonObject("bones").entrySet())for(var track:entry.getValue().getAsJsonObject().entrySet()){
   var keys=track.getValue().getAsJsonObject();check(keys.get("0.0").equals(keys.get("4.8")),"seamless idle "+entry.getKey());
  }
  for(String clip:List.of("action.magic","action.physical")){
   var animation=json.getAsJsonObject("animations").getAsJsonObject(clip);check(animation.get("animation_length").getAsDouble()==2.5&&!animation.get("loop").getAsBoolean(),"timed one-shot attack");
   for(var entry:animation.getAsJsonObject("bones").entrySet())check(names.contains(entry.getKey()),"animation targets real bone");
  }
  support();
  for(int seed=0;seed<100;seed++){
   var bolts=LightningStrike.bolts(seed);check(bolts.size()==33,"bounded lightning geometry");
   check(bolts.stream().filter(s->s.to().distance(new org.joml.Vector3f(0,.15f,0))<.001).count()==3,"three descending bolts converge on enemy");
   check(bolts.stream().allMatch(s->s.from().isFinite()&&s.to().isFinite()),"finite lightning");
  }
  check(LightningStrike.opacity(81)==0&&LightningStrike.opacity(86)>0&&LightningStrike.opacity(94)==0,"effect timing");
  System.out.println("PASS Izanagi: P4 skills, functional support, commands, roster, safe melee, 40 AOE reservations, 123 native cuboids, 34 bones, three clips and lightning.");
 }
 static float sample(List<software.bernie.geckolib.core.keyframe.Keyframe<com.eliotlash.mclib.math.IValue>> frames,double tick){
  double elapsed=0;for(var frame:frames){if(tick<=elapsed+frame.length()+1e-7){double t=frame.length()==0?1:Math.max(0,Math.min(1,(tick-elapsed)/frame.length()));return (float)(frame.startValue().get()+(frame.endValue().get()-frame.startValue().get())*t);}elapsed+=frame.length();}
  return frames.isEmpty()?0:(float)frames.get(frames.size()-1).endValue().get();
 }
 static void dump(software.bernie.geckolib.cache.object.GeoBone bone,com.mojang.blaze3d.vertex.PoseStack pose,List<Object> result){
  pose.m_85836_();software.bernie.geckolib.util.RenderUtils.prepMatrixForBone(pose,bone);
  for(var cube:bone.getCubes()){
   pose.m_85836_();software.bernie.geckolib.util.RenderUtils.translateToPivotPoint(pose,cube);software.bernie.geckolib.util.RenderUtils.rotateMatrixAroundCube(pose,cube);software.bernie.geckolib.util.RenderUtils.translateAwayFromPivotPoint(pose,cube);
   for(var quad:cube.quads())if(quad!=null){
    var vertices=new ArrayList<Object>();for(var v:quad.vertices()){var p=new org.joml.Vector3f(v.position()).mulPosition(pose.m_85850_().m_252922_()).mul(16);vertices.add(List.of(p.x,p.y,p.z,v.texU()*512,v.texV()*512));}
    var n=new org.joml.Vector3f(quad.normal()).mul(pose.m_85850_().m_252943_()).normalize();
    result.add(Map.of("bone",bone.getName(),"vertices",vertices,"normal",List.of(n.x,n.y,n.z)));
   }pose.m_85849_();
  }
  for(var child:bone.getChildBones())dump(child,pose,result);pose.m_85849_();
 }
 static void support(){
  for(String id:List.of("rakukaja","rakunda","tarukaja")) {
   var session=battle(5);var actor=session.currentActor();actor.persona().inheritSkill(id);
   var target=id.equals("rakunda")?session.enemiesOf(actor).get(0):actor;
   var action=BattleAction.skill(id,target.id());var reserved=session.prepare(actor.id(),action);
   check(reserved!=null&&!reserved.cutin()&&reserved.hits().isEmpty(),"support never rolls damage or weakness");
   int hp=target.hp(),sp=actor.sp();var result=session.perform(actor.id(),action);
   check(!result.oneMore()&&!result.weaknessHit()&&target.hp()==hp&&actor.sp()==sp-12,"support costs once, no damage");
   double expected=id.equals("rakunda")?.66:1.33;
   check((id.equals("tarukaja")?target.attackMultiplier():target.defenseMultiplier())==expected,"support applies multiplier");
  }
  var session=battle(0);var actor=session.currentActor();var target=session.enemiesOf(actor).get(0);
  var skill=PersonaRegistry.skill("zio").orElseThrow();
  int normal=DamageCalculator.roll(actor,target,skill,new Random(2)).damage();
  SupportEffects.apply("tarukaja",actor,actor);
  check(DamageCalculator.roll(actor,target,skill,new Random(2)).damage()>normal,"Tarukaja affects actual damage");
  actor.beginTurn();actor.beginTurn();check(actor.attackTurns()==1,"three-turn duration");actor.beginTurn();check(actor.attackMultiplier()==1,"buff expires");
  SupportEffects.apply("rakukaja",actor,actor);SupportEffects.apply("rakunda",target,actor);check(actor.defenseMultiplier()==1,"opposing defense statuses cancel");
  actor.persona().inheritSkill("rakukaja");int sp=actor.sp();
  check(session.prepare(actor.id(),BattleAction.skill("rakukaja",target.id()))==null,"cannot buff enemy");
  session.perform(actor.id(),BattleAction.skill("rakukaja",target.id()));check(actor.sp()==sp,"invalid target spends no SP");
 }
 static BattleSession battle(int seed){
  var p=new PersonaInstance("izanagi",20,0,Set.of("zio","mazio"));
  var a=new BattleCombatant(new UUID(0,1),"P",BattleSide.PLAYERS,p,120,120,45,45);
  var b=new BattleCombatant(new UUID(0,2),"E1",BattleSide.ENEMIES,new PersonaInstance(PersonaRegistry.persona("umbra_slime").orElseThrow()),200,200,20,20);
  var c=new BattleCombatant(new UUID(0,3),"E2",BattleSide.ENEMIES,new PersonaInstance(PersonaRegistry.persona("umbra_slime").orElseThrow()),200,200,20,20);
  return new BattleSession(List.of(a,b,c),new Random(seed));
 }
}
