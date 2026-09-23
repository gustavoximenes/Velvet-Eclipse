package com.gx.velveteclipse.tests;

import com.gx.velveteclipse.core.persona.*;
import com.gx.velveteclipse.core.battle.*;
import com.gx.velveteclipse.forge.PlayerPersonaData;
import com.gx.velveteclipse.client.SpellImpact;
import com.gx.velveteclipse.network.*;
import com.google.gson.*;
import java.nio.file.*;
import java.util.*;

/** Executable combat scenarios, legacy save round-trips and real GeckoLib baking. */
public final class StarterPersonasAudit {
    private static int checks;
    private static void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
    public static final class Player {final net.minecraft.nbt.CompoundTag tag=new net.minecraft.nbt.CompoundTag();public net.minecraft.nbt.CompoundTag getPersistentData(){return tag;}}
    private static final Random HIT=new Random(){@Override public int nextInt(int bound){return 0;}};
    private static BattleCombatant fighter(int id,String persona,int level,Set<String> skills){return new BattleCombatant(new UUID(0,id),persona,id<10?BattleSide.PLAYERS:BattleSide.ENEMIES,new PersonaInstance(persona,level,0,skills),2000,2000,200,200);}
    public static void main(String[] args)throws Exception{
        PersonaRegistry.bootstrap();catalog();migration();combat();models();effects();
        System.out.println("PASS starter Personas: "+checks+" checks; learnsets, save migration, costs, debuffs, sleep, ambush passive, native rigs, retimed attacks and spell packets/geometry.");
    }
    private static void catalog(){
        var a=PersonaRegistry.persona("arsene").orElseThrow();var o=PersonaRegistry.persona("orpheus").orElseThrow();
        check(new PersonaInstance(a).learnedSkills().equals(Set.of("eiga")),"Arsène starts with a single Eiga; no separate Eiha");
        check(!PersonaRegistry.skills().containsKey("eiha"),"no duplicate curse tier");
        var ai=new PersonaInstance(a);ai.setLevel(a,7);check(ai.learnedSkills().equals(Set.of("eiga","arsene_cleave","sukunda","dream_needle","adverse_resolve")),"Royal level progression");
        var oi=new PersonaInstance(o);check(oi.learnedSkills().equals(Set.of("agi")),"Reload initial Agi");oi.setLevel(o,5);check(oi.learnedSkills().equals(Set.of("agi","dia","bash","tarunda")),"Reload progression at 3,4,5");
        check(a.affinity(Element.ICE)==Affinity.WEAK&&a.affinity(Element.LIGHT)==Affinity.WEAK&&a.affinity(Element.DARK)==Affinity.RESIST,"Arsène affinities");
        check(o.affinity(Element.FIRE)==Affinity.RESIST&&o.affinity(Element.ELECTRIC)==Affinity.WEAK&&o.affinity(Element.DARK)==Affinity.WEAK,"Orpheus affinities");
        check(PersonaRegistry.skill("bash").orElseThrow().element()==Element.PHYSICAL&&!PersonaRegistry.skill("bash").orElseThrow().usesMagicStat(),"Bash uses strike and strength");
        check(PersonaRegistry.skill("dream_needle").orElseThrow().element()==Element.PIERCE,"Royal gun maps to piercing");
        for(String id:List.of("arsene","orpheus")){
            var data=new PlayerPersonaData();check(data.givePersona(id)&&data.equipPersona(id),"can acquire and equip "+id);
            check(data.setPersonaLevel(id,7)&&data.activeInstance().level()==7,"command progression "+id);
            var card=new com.gx.velveteclipse.shuffle.ShuffleRules.Card(com.gx.velveteclipse.shuffle.ShuffleRules.Kind.PERSONA,id,7,1);
            check(com.gx.velveteclipse.shuffle.ShuffleRules.personaReward(card,null).level()==7,"shuffle acquisition "+id);
        }
    }
    private static void migration(){
        var player=new Player();player.tag.m_128359_("velvet_eclipse:data_v1","V1|100|120|20|45|cinder_wisp|3|cinder_wisp,12,41,ember+agi+zio|8|4|5000|2|");
        var data=PlayerPersonaData.load(player);check(data.activeInstance().learnedSkills().equals(Set.of("agi","zio")),"old and new fire IDs deduplicate");
        check(data.activeInstance().level()==12&&data.activeInstance().experience()==41&&data.yen()==5000,"migration preserves progression and yen");
        data.save(player);check(!player.tag.m_128461_("velvet_eclipse:data_v1").contains("ember"),"saved data becomes canonical");
        check(!com.gx.velveteclipse.shuffle.ShuffleRewards.teach(data,"cinder_wisp","ember"),"legacy card cannot consume itself teaching duplicate Agi");
        check(PersonaRegistry.skill("ember").orElseThrow().id().equals("agi")&&!PersonaRegistry.skills().containsKey("ember"),"legacy lookup works without duplicate catalogue entry");
        var a=fighter(1,"orpheus",20,Set.of("ember"));var b=fighter(10,"umbra_slime",1,Set.of("shade_bite"));
        var s=new BattleSession(List.of(a,b),HIT);var action=BattleAction.skill("ember",b.id());
        check(s.prepare(a.id(),action)!=null,"legacy queued fire action remains usable");s.perform(a.id(),action);check(a.sp()==197&&b.hp()<2000,"legacy action uses Agi cost and damage");
    }
    private static void combat(){
        for(String id:List.of("agi","eiga","arsene_cleave","dream_needle","bash")){
            for(int seed=0;seed<16;seed++){
                var a=fighter(1,"arsene",20,Set.of(id));var b=fighter(10,"iron_oni",1,Set.of("cleave"));
                var c=fighter(1,"arsene",20,Set.of(id));var d=fighter(10,"iron_oni",1,Set.of("cleave"));
                var direct=new BattleSession(List.of(a,b),new Random(seed));var delayed=new BattleSession(List.of(c,d),new Random(seed));
                var action=BattleAction.skill(id,b.id());check(delayed.prepare(c.id(),action)!=null,"reserve "+id);
                check(d.hp()==2000&&c.sp()==200&&c.hp()==2000,"reservation applies nothing");
                var r=direct.perform(a.id(),action);var q=delayed.perform(c.id(),action);
                check(r.equals(q)&&a.hp()==c.hp()&&a.sp()==c.sp()&&b.hp()==d.hp()&&b.ailment().equals(d.ailment()),"delayed and direct outcome match "+id);
                var skill=PersonaRegistry.skill(id).orElseThrow();check(a.sp()==200-skill.spCost()&&a.hp()==2000-2000*skill.hpPercentCost()/100,"cost once "+id);
            }
        }
        for(String id:List.of("sukunda","tarunda")){
            var a=fighter(1,"orpheus",20,Set.of(id));var b=fighter(10,"iron_oni",1,Set.of("cleave"));var s=new BattleSession(List.of(a,b),HIT);
            check(s.prepare(a.id(),BattleAction.skill(id,a.id()))==null,"debuff rejects friendly target");
            var action=BattleAction.skill(id,b.id());check(s.prepare(a.id(),action).hits().isEmpty(),"debuff reserves no damage");s.perform(a.id(),action);
            check(b.hp()==2000&&a.sp()==192,"debuff costs once without damage");
            check(id.equals("tarunda")?b.attackMultiplier()==.66:b.accuracyMultiplier()==.75,"debuff actual multiplier");
            b.beginTurn();b.beginTurn();check(id.equals("tarunda")?b.attackTurns()==1:b.accuracyTurns()==1,"last affected turn");b.beginTurn();check(id.equals("tarunda")?b.attackMultiplier()==1:b.accuracyMultiplier()==1,"debuff expires");
        }
        var a=fighter(1,"arsene",20,Set.of("dream_needle","adverse_resolve"));var b=fighter(10,"iron_oni",1,Set.of("cleave"));
        var spell=PersonaRegistry.skill("eiga").orElseThrow();var nearMiss=new Random(){@Override public int nextInt(int bound){return 85%bound;}};
        check(!DamageCalculator.roll(a,b,spell,nearMiss).missed(),"normal accuracy at fixed roll");a.accuracyStatus(-1,false);check(DamageCalculator.roll(a,b,spell,nearMiss).missed(),"Sukunda reduces outgoing hit chance");a.accuracyStatus(1,false);b.accuracyStatus(-1,false);check(!DamageCalculator.roll(a,b,spell,nearMiss).missed(),"debuff reduces target evasion");
        b.attackStatus(-1,false);b.attackStatus(true);check(b.attackMultiplier()==1,"Tarukaja cancels Tarunda");
        var session=new BattleSession(List.of(a,b),HIT);session.perform(a.id(),BattleAction.skill("dream_needle",b.id()));check(b.hasAilment("SLEEP"),"Dream Needle actually inflicts sleep");
        if(session.currentActor()==a)session.perform(a.id(),BattleAction.guard());check(session.currentActor()==a,"sleep skips enemy action");
        b.damage(1);check(!b.hasAilment("SLEEP"),"damage wakes sleep");b.setRaidBoss(Element.FIRE);b.afflict("SLEEP");check(!b.hasAilment("SLEEP"),"raid boss status immunity preserved");
        for(var opening:EncounterOpening.values()){
            a=fighter(1,"arsene",7,Set.of("adverse_resolve"));b=fighter(10,"iron_oni",1,Set.of("cleave"));session=new BattleSession(List.of(a,b),HIT,opening);
            check(a.passiveCritical()==(opening==EncounterOpening.AMBUSH?20:0),"passive only in enemy ambush");a.setPersona(new PersonaInstance(PersonaRegistry.persona("orpheus").orElseThrow()));check(a.passiveCritical()==0,"switching away removes equipped passive");
        }
        a=fighter(1,"arsene",20,Set.of("adverse_resolve"));b=fighter(10,"iron_oni",1,Set.of("cleave"));session=new BattleSession(List.of(a,b),HIT);
        check(session.prepare(a.id(),BattleAction.skill("adverse_resolve",a.id()))==null,"passive cannot be submitted as an action");
        a.afflict("SLEEP");b.afflict("SLEEP");session.perform(a.id(),BattleAction.guard());
        check(!session.currentActor().hasAilment("SLEEP"),"all-asleep roster advances until a fighter can actually act");
        a=fighter(1,"arsene",20,Set.of("eiga"));b=fighter(10,"iron_oni",1,Set.of("cleave"));
        b.equipment(new com.gx.velveteclipse.equipment.EquipmentBonuses(List.of(new com.gx.velveteclipse.equipment.EquipmentCatalog.Gear("test","Test",com.gx.velveteclipse.equipment.EquipmentCatalog.Slot.ACCESSORY,com.gx.velveteclipse.equipment.EquipmentCatalog.Family.NONE,0,1,1,1,"Insta-Heal"))));
        session=new BattleSession(List.of(a,b),HIT);b.afflict("SLEEP");session.perform(a.id(),BattleAction.guard());check(session.currentActor()==b&&!b.hasAilment("SLEEP"),"Insta-Heal cures sleep before losing a turn");
    }
    private static void models()throws Exception{
        Path resources=Path.of("src/main/resources/assets/velvet_eclipse");var report=Path.of("build/reports/starter-personas");Files.createDirectories(report);
        for(String id:List.of("arsene","orpheus")){
            String raw=Files.readString(resources.resolve("geo/entity/"+id+".geo.json"));
            var model=software.bernie.geckolib.util.JsonUtil.GEO_GSON.fromJson(raw,software.bernie.geckolib.loading.json.raw.Model.class);
            var baked=software.bernie.geckolib.loading.object.BakedModelFactory.DEFAULT_FACTORY.constructGeoModel(software.bernie.geckolib.loading.object.GeometryTree.fromModel(model));
            var authored=JsonParser.parseString(Files.readString(Path.of("tools/personas/"+id+"/draft/"+id+".geo.json"))).getAsJsonObject().getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();int cubes=0;
            for(var entry:authored.getAsJsonArray("bones")){
                var bone=entry.getAsJsonObject();var nativeBone=baked.getBone(bone.get("name").getAsString()).orElseThrow();
                check(Math.abs(nativeBone.getPivotX()-bone.getAsJsonArray("pivot").get(0).getAsDouble())<.001,"GeckoLib X-axis matches approved rig");
                if(bone.has("rotation")){var r=bone.getAsJsonArray("rotation");check(Math.abs(nativeBone.getRotX()-Math.toRadians(r.get(0).getAsDouble()))<.001&&Math.abs(nativeBone.getRotY()-Math.toRadians(r.get(1).getAsDouble()))<.001&&Math.abs(nativeBone.getRotZ()-Math.toRadians(r.get(2).getAsDouble()))<.001,"native rest rotation");}
                if(bone.has("cubes"))cubes+=bone.getAsJsonArray("cubes").size();
            }
            check(cubes==(id.equals("arsene")?264:220),"complete approved cuboids");
            check(Arrays.equals(Files.readAllBytes(resources.resolve("textures/entity/"+id+".png")),Files.readAllBytes(Path.of("tools/personas/"+id+"/draft/"+id+".png"))),"texture unchanged including Orpheus magic colors");
            var clips=JsonParser.parseString(Files.readString(resources.resolve("animations/entity/"+id+".animation.json"))).getAsJsonObject().getAsJsonObject("animations");
            var animations=software.bernie.geckolib.util.JsonUtil.GEO_GSON.fromJson(clips,software.bernie.geckolib.loading.object.BakedAnimations.class);
            for(String clip:List.of("misc.idle","action.magic","action.physical")){
                check(animations.getAnimation(clip)!=null,"GeckoLib bakes "+id+clip);
                var c=clips.getAsJsonObject(clip);check(c.get("animation_length").getAsDouble()==(clip.equals("misc.idle")?4.8:2.5),"clip completes before actor is hidden");
                var posed=software.bernie.geckolib.loading.object.BakedModelFactory.DEFAULT_FACTORY.constructGeoModel(software.bernie.geckolib.loading.object.GeometryTree.fromModel(model));
                for(var track:animations.getAnimation(clip).boneAnimations()){
                    var bone=posed.getBone(track.boneName()).orElseThrow();var r=track.rotationKeyFrames();var p=track.positionKeyFrames();
                    bone.setRotX(bone.getRotX()+IzanagiAudit.sample(r.xKeyframes(),28));bone.setRotY(bone.getRotY()+IzanagiAudit.sample(r.yKeyframes(),28));bone.setRotZ(bone.getRotZ()+IzanagiAudit.sample(r.zKeyframes(),28));
                    bone.setPosX(IzanagiAudit.sample(p.xKeyframes(),28));bone.setPosY(IzanagiAudit.sample(p.yKeyframes(),28));bone.setPosZ(IzanagiAudit.sample(p.zKeyframes(),28));
                }
                var surfaces=new ArrayList<Object>();for(var bone:posed.topLevelBones())IzanagiAudit.dump(bone,new com.mojang.blaze3d.vertex.PoseStack(),surfaces);
                // The shared geometry dumper uses a 512px atlas; these approved rigs use 1024px.
                var serialized=new Gson().toJsonTree(surfaces).getAsJsonArray();for(var face:serialized)for(var vertex:face.getAsJsonObject().getAsJsonArray("vertices")){var v=vertex.getAsJsonArray();v.set(3,new JsonPrimitive(v.get(3).getAsDouble()*2));v.set(4,new JsonPrimitive(v.get(4).getAsDouble()*2));}
                Files.writeString(report.resolve(id+"-"+clip+".json"),new Gson().toJson(serialized));
            }
            for(String item:List.of(id+"_model")){check(Files.exists(resources.resolve("models/item/"+item+".json"))&&Files.exists(resources.resolve("textures/item/"+item+".png")),"usable model item assets");}
        }
    }
    private static void effects()throws Exception{
        for(String id:List.of("agi","eiga")){
            check(SpellImpact.geometry(id,-1,5).isEmpty()&&SpellImpact.geometry(id,23,5).isEmpty(),"spell lifetime bounded");
            for(int age=0;age<23;age++)for(int seed=0;seed<4;seed++){
                var strokes=SpellImpact.geometry(id,age,seed);check(!strokes.isEmpty()&&strokes.size()<850,"bounded custom geometry");
                check(strokes.stream().allMatch(s->s.from().isFinite()&&s.to().isFinite()&&s.width()>0&&s.alpha()>0&&s.alpha()<=1),"valid spell vertices");
            }
            var packet=new BattleSpellS2C(id,List.of(new BattleLightningS2C.Strike(9,64,-4,15),new BattleLightningS2C.Strike(-8,32,45,12)));
            var buffer=new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());try{BattleSpellS2C.encode(packet,buffer);check(packet.equals(BattleSpellS2C.decode(buffer)),"spell target codec");}finally{buffer.release();}
            var frames=new LinkedHashMap<String,Object>();for(int age:new int[]{0,4,8,14,20})frames.put(""+age,SpellImpact.geometry(id,age,15));
            Files.writeString(Path.of("build/reports/starter-personas/"+id+"-effect.json"),new Gson().toJson(frames));
        }
        boolean rejected=false;try{new BattleSpellS2C("agi",List.of(new BattleLightningS2C.Strike(Double.NaN,0,0,1)));}catch(IllegalArgumentException e){rejected=true;}check(rejected,"nonfinite packet rejected");
    }
}
