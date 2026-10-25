package com.gx.velveteclipse.tests;
import com.gx.velveteclipse.presentation.*;
import com.gx.velveteclipse.core.battle.*;
import com.gx.velveteclipse.core.persona.*;
import com.gx.velveteclipse.network.*;
import com.google.gson.*;
import java.nio.file.*;
import java.util.*;

/** Runs real reservations/results against the production impact gate and delivered GeckoLib durations. */
public final class PersonaTimingAudit {
    private static int checks;
    private static void check(boolean ok,String why){checks++;if(!ok)throw new AssertionError(why);}
    private static BattleSession battle(int players,int enemies){
        var roster=new ArrayList<BattleCombatant>();
        for(int i=0;i<players+enemies;i++){boolean player=i<players;var def=PersonaRegistry.persona(player?"timing_player":"timing_enemy").orElseThrow();roster.add(new BattleCombatant(new UUID(player?1:2,i),"Actor "+i,player?BattleSide.PLAYERS:BattleSide.ENEMIES,new PersonaInstance(def),10000,10000,100,100));}
        return new BattleSession(roster,new Random(){@Override public int nextInt(int bound){return Math.min(50,bound-1);}});
    }
    public static void main(String[] args)throws Exception{
        PersonaRegistry.bootstrap(); var plans=new TreeMap<String,Object>();
        for(var persona:PersonaRegistry.personas().keySet())for(boolean magic:new boolean[]{false,true}){
            var plan=PersonaActionTiming.forClip(persona,magic);
            String asset=com.gx.velveteclipse.client.menu.PersonaDetails.asset(persona);
            var raw=JsonParser.parseString(Files.readString(Path.of("src/main/resources/assets/velvet_eclipse/animations/entity/"+asset+".animation.json"))).getAsJsonObject().getAsJsonObject("animations").getAsJsonObject(magic?"action.magic":"action.physical");
            double duration=raw.get("animation_length").getAsDouble()*20;
            check(plan.impact()>PersonaActionTiming.START+PersonaActionTiming.TRANSITION+duration,"full exported clip and transition precede result: "+persona);
            check(plan.attacking(plan.animationEnd())&&plan.attacking(plan.impact()-1)&&!plan.attacking(plan.impact()),"no clipped recovery: "+persona);
            check(plan.finish()>plan.impact()+35&&plan.finish()<=320,"full effect tail and bounded wire duration");
            check(PersonaActionTiming.impactFromDuration(plan.finish())==plan.impact(),"all viewers use the transmitted duration");
            for(int tick=0;tick<plan.impact();tick++)if(!InvocationFeedback.at(tick,persona).isEmpty())check(tick==38||tick==46||tick==54,"only invocation feedback before complete attack");
            plans.put(persona+"/"+(magic?"magic":"physical"),Map.of("clipTicks",plan.animationTicks(),"end",plan.animationEnd(),"impact",plan.impact(),"finish",plan.finish()));
        }
        check(PersonaActionTiming.forClip("onmoraki",false).animationTicks()==48&&PersonaActionTiming.forClip("onmoraki",true).animationTicks()==56,"Onmoraki full original authoring lengths retained");
        PersonaRegistry.registerSkill(new SkillDefinition("timing_single","Single",Element.FIRE,40,100,4,0,true,SkillTarget.ONE_ENEMY));
        PersonaRegistry.registerSkill(new SkillDefinition("timing_area","Area",Element.FIRE,40,100,8,0,true,SkillTarget.ALL_ENEMIES));
        PersonaRegistry.registerPersona(new PersonaDefinition("timing_player","Player",Arcana.FOOL,1,new PersonaStats(6,6,6,99,6),Map.of(),Map.of(1,List.of("timing_single","timing_area"))));
        PersonaRegistry.registerPersona(new PersonaDefinition("timing_enemy","Enemy",Arcana.FOOL,1,new PersonaStats(6,6,6,1,6),Map.of(Element.FIRE,Affinity.WEAK),Map.of()));
        for(int players:new int[]{1,2,3,4,8})for(String skill:List.of("timing_single","timing_area")){
            var s=battle(players,3);var actor=s.currentActor();var enemy=s.enemiesOf(actor).get(0);var action=BattleAction.skill(skill,enemy.id());
            check(s.prepare(actor.id(),action)!=null,"reserve solo and party action");var plan=PersonaActionTiming.forAction("onmoraki",skill);var gate=new PersonaImpactGate(s,plan);
            int sp=actor.sp();var hp=s.enemiesOf(actor).stream().map(BattleCombatant::hp).toList();
            for(int tick=0;tick<plan.impact();tick++){check(!gate.claim(s,tick),"no early result");check(actor.sp()==sp&&s.enemiesOf(actor).stream().map(BattleCombatant::hp).toList().equals(hp),"no HP/SP mutation while summoned Persona animates");check(!s.oneMore(),"no premature One More");}
            check(!gate.claim(battle(1,1),plan.impact()),"another encounter cannot consume the reservation");
            check(gate.claim(s,plan.impact()),"claim at result boundary");var result=s.perform(actor.id(),action);
            check(result.accepted()&&result.oneMore()&&result.weaknessHit(),"weakness and One More emitted by real result");
            check(result.hits().size()==(skill.equals("timing_area")?3:1),"correct individual/area targets");
            check(actor.sp()==sp-(skill.equals("timing_area")?8:4),"resource cost occurs once at result");
            for(int tick=plan.impact();tick<=plan.finish()+10;tick++)check(!gate.claim(s,tick),"no replay, including One More");
            var buf=new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
            try{var cue=new BattleDirectionS2C(actor.id(),1,2,"skill",skill,"SWORD",0,64,0,0,64,3,plan.finish());BattleDirectionS2C.encode(cue,buf);check(cue.equals(BattleDirectionS2C.decode(buf)),"party camera clock packet round trip");}finally{buf.release();}
        }
        for(int cancel:new int[]{0,37,38,54,86,112,118}){
            var s=battle(1,1);var a=s.currentActor();var e=s.enemiesOf(a).get(0);var action=BattleAction.skill("timing_single",e.id());s.prepare(a.id(),action);
            var gate=new PersonaImpactGate(s,PersonaActionTiming.forClip("onmoraki",true));int hp=e.hp(),sp=a.sp();
            for(int tick=0;tick<=cancel;tick++)check(!gate.claim(s,tick),"cancellation before impact");s.cancelPrepared();
            check(!gate.claim(s,119)&&e.hp()==hp&&a.sp()==sp,"cancelled cast has no late damage/cost");
            s.prepare(a.id(),action);check(!gate.claim(s,120),"same skill new reservation cannot be consumed by old animation");
        }
        var server=Files.readString(Path.of("src/main/java/com/gx/velveteclipse/forge/PersonaCinematicServer.java"));
        check(server.contains("sequence.gate.claim(current, sequence.tick)")&&!server.contains("sequence.tick != 86")&&!server.contains("fullAttack("),"server uses tested gate for every Persona");
        check(server.contains("current != sequence.session")&&server.contains("session.prepared()==sequence.reservation"),"cleanup cannot cancel a different session/action");
        var res=Path.of("src/main/resources");var data=JsonParser.parseString(Files.readString(res.resolve("data/velvet_eclipse/presentation/elemental-effects.json"))).getAsJsonObject();
        var recipes=new HashMap<String,JsonObject>();for(var item:data.getAsJsonArray("recipes")){var r=item.getAsJsonObject();String id=r.get("id").getAsString();recipes.put(id,r);var parts=r.getAsJsonArray("parts");check(parts.size()<=64,"finite particle budget");int first=100;
            for(var part:parts){var p=part.getAsJsonObject();int start=p.get("start").getAsInt(),life=p.get("life").getAsInt();first=Math.min(first,start);check(start>=0&&life>0&&start+life<=35,"effect fits completed attack result tail");String model=p.get("model").getAsString();if(!Set.of("fire_flipbook","ground_glow").contains(model)){var json=res.resolve("assets/velvet_eclipse/models/vfx/audit02/"+model+".json");check(Files.exists(json)&&Files.exists(json.resolveSibling(model+".obj")),"actual Photon model dependency exists");}}
            check(first==0,"first visible stage starts at confirmed damage");
        }
        check(recipes.size()==22&&data.getAsJsonObject("bindings").size()==38,"approved elemental coverage");
        for(var entry:data.getAsJsonObject("bindings").entrySet()){check(PersonaRegistry.skill(entry.getKey()).isPresent()&&recipes.containsKey(entry.getValue().getAsString()),"catalogue-to-recipe binding");}
        check(data.getAsJsonObject("bindings").get("eiga").getAsString().equals("eiha"),"preserve existing catalogue ID / verified source mapping");
        for(String id:List.of("maralagidyne","tarukaja","dekunda","megido","hama","mudo"))check(!data.getAsJsonObject("bindings").has(id),"preserve effects outside elemental audit: "+id);
        var dir=Path.of("build/reports/persona-timing");Files.createDirectories(dir);Files.writeString(dir.resolve("timeline.json"),new GsonBuilder().setPrettyPrinting().create().toJson(plans));Files.writeString(dir.resolve("validation.json"),"{\"checks\":"+checks+",\"status\":\"passed\",\"inGame\":false}\n");
        System.out.println("PASS Persona timing: "+checks+" checks; packaged clips, real solo/party reservations, area damage, cancellation, replay, wire clocks and elemental resources.");
    }
}
