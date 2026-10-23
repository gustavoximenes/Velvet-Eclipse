package com.gx.velveteclipse.tests;
import com.gx.velveteclipse.presentation.*;
import com.gx.velveteclipse.presentation.AudioPreferences.Bus;
import com.gx.velveteclipse.core.battle.*;
import com.gx.velveteclipse.core.persona.*;
import com.gx.velveteclipse.network.*;
import com.gx.velveteclipse.client.vfx.ClientAudio;
import com.google.gson.*;
import java.util.*;
import java.nio.file.*;

public final class FeedbackAudit {
 private static int checks;
 private static void check(boolean ok,String why){checks++;if(!ok)throw new AssertionError(why);}
 private static DamageOutcome hit(Affinity a,boolean down,boolean crit){return new DamageOutcome(Element.FIRE,a,20,crit,down,false,false,false);}
 private static BattleResolution result(boolean more,DamageOutcome... hits){var map=new LinkedHashMap<UUID,DamageOutcome>();for(int i=0;i<hits.length;i++)map.put(new UUID(0,i),hits[i]);return new BattleResolution(BattleState.ACTIVE,more,List.of(),true,map,true);}
 public static void main(String[] args)throws Exception {
  check(result(true,hit(Affinity.WEAK,true,false)).weaknessOneMore(),"new weakness down grants voice");
  check(!result(true,hit(Affinity.WEAK,false,false),hit(Affinity.NORMAL,true,true)).weaknessOneMore(),"weakness on downed victim plus critical elsewhere must not announce persona");
  check(!result(false,hit(Affinity.WEAK,true,false)).weaknessOneMore(),"final victory is not One More");
  check(!result(true,hit(Affinity.NORMAL,true,true)).weaknessOneMore(),"critical alone has no name call");
  for(var h:List.of(new DamageOutcome(Element.FIRE,Affinity.WEAK,0,false,true,false,false,false),new DamageOutcome(Element.FIRE,Affinity.WEAK,20,false,true,true,false,false),new DamageOutcome(Element.FIRE,Affinity.WEAK,20,false,true,false,true,false),new DamageOutcome(Element.FIRE,Affinity.WEAK,20,false,true,false,false,true)))check(!result(true,h).weaknessOneMore(),"blocked/reflected/absorbed/missed does not call");
  var gate=new FeedbackSession();var a=UUID.randomUUID();var b=UUID.randomUUID();check(!gate.accept(a,1),"no session");gate.start(a);check(gate.accept(a,1),"first");check(!gate.accept(a,1),"duplicate");check(!gate.accept(b,2),"other party");gate.start(a);check(!gate.accept(a,1),"duplicate start cannot reset gate");check(gate.accept(a,2),"next");gate.clear();check(!gate.accept(a,3),"left encounter");gate.start(b);check(gate.accept(b,1),"new encounter");
  var prefs=new AudioPreferences();for(var bus:Bus.values())check(prefs.get(bus)==1,"default audible");prefs.set(Bus.COMBAT,.35f);prefs.set(Bus.VOICE,0);check(prefs.get(Bus.COMBAT)==.35f&&prefs.get(Bus.INTERACTION)==1,"independent gains");var path=Path.of("build/reports/feedback/preferences.properties");prefs.save(path);var restored=AudioPreferences.read(path);check(restored.get(Bus.VOICE)==0&&restored.get(Bus.COMBAT)==.35f,"persist silence and volume");Files.writeString(path,"VOICE=NaN\nCOMBAT=garbage\nUI=-5\nINTERACTION=3\n");var malformed=AudioPreferences.read(path);check(malformed.get(Bus.VOICE)==1&&malformed.get(Bus.COMBAT)==1&&malformed.get(Bus.UI)==0&&malformed.get(Bus.INTERACTION)==1,"safe malformed/old config");
  for(var bus:Bus.values())check(ClientAudio.source(bus)!=net.minecraft.sounds.SoundSource.MUSIC,"music slider cannot mute "+bus);
  check(AudioPreferences.classify("voice_pixie",true)==Bus.VOICE,"voice bus");check(AudioPreferences.classify("cursor",true)==Bus.UI,"UI bus");check(AudioPreferences.classify("cloth_soft",false)==Bus.INTERACTION,"furniture bus");check(AudioPreferences.classify("evoker",true)==Bus.COMBAT,"evocation sound bus");
  PersonaRegistry.bootstrap();
  PersonaRegistry.registerSkill(new SkillDefinition("feedback_cast","Cast",Element.FIRE,40,100,4,0,true,SkillTarget.ONE_ENEMY));
  PersonaRegistry.registerPersona(new PersonaDefinition("feedback_player","Player",Arcana.FOOL,1,new PersonaStats(6,6,6,99,6),Map.of(),Map.of(1,List.of("feedback_cast"))));
  PersonaRegistry.registerPersona(new PersonaDefinition("feedback_enemy","Enemy",Arcana.FOOL,1,new PersonaStats(6,6,6,1,6),Map.of(Element.FIRE,Affinity.WEAK),Map.of()));
  var player=new BattleCombatant(new UUID(1,1),"P",BattleSide.PLAYERS,new PersonaInstance(PersonaRegistry.persona("feedback_player").orElseThrow()),10000,10000,100,100);
  var enemy=new BattleCombatant(new UUID(1,2),"E",BattleSide.ENEMIES,new PersonaInstance(PersonaRegistry.persona("feedback_enemy").orElseThrow()),10000,10000,100,100);
  var battle=new BattleSession(List.of(player,enemy),new Random(){public int nextInt(int bound){return Math.min(50,bound-1);}});
  for(var action:List.of(BattleAction.attack(enemy.id()),BattleAction.guard(),BattleAction.allOut()))check(com.gx.velveteclipse.forge.FeedbackServer.targets(battle,player.id(),action,id->null).isEmpty(),"basic action does not resolve null skill ID");
  var action=BattleAction.skill("feedback_cast",enemy.id());var reserved=battle.prepare(player.id(),action);check(reserved!=null&&!reserved.hit().newlyDowned(),"reservation is not a confirmed down");
  var applied=battle.perform(player.id(),action);check(applied.accepted()&&applied.hits().get(enemy.id()).newlyDowned()&&applied.weaknessOneMore(),"voice uses applied damage, not prediction");
  check(!battle.perform(enemy.id(),BattleAction.guard()).accepted(),"out of turn produces no feedback");check(applied.hits().get(enemy.id()).newlyDowned(),"past result immutable after next action");
  var pts=new ArrayList<BattleFeedbackS2C.Target>();for(int i=0;i<BattleFeedbackS2C.MAX_TARGETS;i++)pts.add(new BattleFeedbackS2C.Target(new BattleLightningS2C.Strike(i,64,i,9,i,new UUID(2,i)),"weak"));var packet=new BattleFeedbackS2C(a,19,"skill","maragi",pts);var buf=new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());try{BattleFeedbackS2C.encode(packet,buf);check(packet.equals(BattleFeedbackS2C.decode(buf)),"128-target identity/outcome wire round trip");}finally{buf.release();}
  pts.add(pts.get(0));try{new BattleFeedbackS2C(a,20,"skill","maragi",pts);throw new AssertionError("unbounded packet");}catch(IllegalArgumentException expected){checks++;}
  check(FeedbackLibrary.routes().size()==446,"complete approved routes");check(FeedbackLibrary.bindings().size()==171,"active skill bindings");check(FeedbackLibrary.routes().keySet().stream().filter(k->k.startsWith("voice_for_")).count()==181,"all personas, including no-model personas");
  var profiles=new HashSet<String>();var root=Path.of("src/main/resources/data/velvet_eclipse/presentation");for(var p:JsonParser.parseString(Files.readString(root.resolve("photon-effects.json"))).getAsJsonArray()){var obj=p.getAsJsonObject();var id=obj.get("id").getAsString();check(profiles.add(id)&&!id.equals("summon"),"unique attack profile; summoning excluded");check(obj.getAsJsonArray("layers").size()<=64,"bounded emitters");for(var layer:obj.getAsJsonArray("layers")){var l=layer.getAsJsonObject();int start=l.get("start").getAsInt(),life=l.get("life").getAsInt(),tail=l.has("tail")?l.get("tail").getAsInt():0;check(start>=0&&life>0&&start+life+tail<=100,"finite lifetime "+id);}}
  for(var entry:FeedbackLibrary.bindings().entrySet()){check(profiles.contains(entry.getValue()),"profile present "+entry.getKey());check(!FeedbackLibrary.audio("skill_"+entry.getKey()).isEmpty(),"audio present "+entry.getKey());}
  var sounds=JsonParser.parseString(Files.readString(Path.of("src/main/resources/assets/velvet_eclipse/sounds.json"))).getAsJsonObject();for(var clip:FeedbackLibrary.routes().values().stream().flatMap(Collection::stream).distinct().toList()){check(sounds.has("reload."+clip),"registered sound "+clip);var bytes=Files.readAllBytes(Path.of("src/main/resources/assets/velvet_eclipse/sounds/reload/"+clip+".ogg"));check(bytes.length>100&&new String(bytes,0,4,java.nio.charset.StandardCharsets.US_ASCII).equals("OggS"),"OGG present "+clip);}
  Files.writeString(Path.of("build/reports/feedback/validation.json"),"{\"status\":\"passed\",\"checks\":"+checks+",\"inGame\":false}\n");System.out.println("PASS reload feedback: "+checks+" checks; applied damage, sessions, packet bounds, local mixer, resources.");
 }
}
