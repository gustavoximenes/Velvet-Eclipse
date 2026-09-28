package com.gx.velveteclipse.tests;

import com.gx.velveteclipse.core.battle.*;
import com.gx.velveteclipse.core.persona.*;
import com.gx.velveteclipse.client.*;
import com.gx.velveteclipse.network.*;
import java.util.*;
import java.nio.file.*;
import net.minecraft.network.FriendlyByteBuf;
import io.netty.buffer.Unpooled;

/** Turn semantics and synchronized presentation, exercised without a running game. */
public final class TacticalAudit {
 private static int checks;
 private static void check(boolean b,String m){checks++;if(!b)throw new AssertionError(m);}
 private static UUID id(int n){return new UUID(0,n);}
 private static BattleCombatant player(int n){return new BattleCombatant(id(n),"Player "+n,BattleSide.PLAYERS,new PersonaInstance("izanagi",20,0,Set.of("ember","tarukaja")),2000,2000,500,500);}
 private static BattleSession battle(int count){
  var list=new ArrayList<BattleCombatant>();list.add(player(1));list.add(player(2));list.add(player(3));
  for(int i=0;i<count;i++)list.add(new BattleCombatant(id(10+i),"Enemy "+i,BattleSide.ENEMIES,new PersonaInstance(PersonaRegistry.persona("umbra_slime").orElseThrow()),2000,2000,100,100));
  return new BattleSession(list,new Random(){@Override public int nextInt(int bound){return 50%bound;}});
 }
 private static void down(BattleSession s,int enemy){var a=s.currentActor();check(s.perform(a.id(),BattleAction.skill("ember",id(enemy))).oneMore(),"fresh weakness grants bonus turn");}
 public static void main(String[] args)throws Exception{
  PersonaRegistry.bootstrap();
  for(boolean twice:new boolean[]{false,true}){
   var s=battle(1);down(s,10);var e=s.find(id(10)).orElseThrow();check(e.downed()&&!e.skipsNextTurn(),"first knockdown preserves next action");
   if(twice){check(!s.perform(id(1),BattleAction.skill("ember",id(10))).oneMore(),"repeat weakness does not grant another bonus");check(e.skipsNextTurn(),"second same weakness schedules one skipped action");}
   else s.perform(id(1),BattleAction.guard());
   s.perform(id(2),BattleAction.guard());s.perform(id(3),BattleAction.guard());check(s.currentActor().id().equals(id(twice?1:10)),"correct recovery or skipped turn");
   check(!e.downed()&&!e.skipsNextTurn(),"recovery clears both conditions");
  }
  var e=battle(1).find(id(10)).orElseThrow();e.knockDown(Element.FIRE,true);e.knockDown(Element.ELECTRIC,true);check(!e.skipsNextTurn(),"different weakness does not cause lost action");e.knockDown(Element.SLASH,false);check(!e.skipsNextTurn(),"critical on downed enemy does not cause lost action");e.knockDown(Element.FIRE,true);check(e.skipsNextTurn(),"same weakness still tracked");
  var s=battle(2);check(s.canSwitchPersona(id(1))&&!s.batonPass(id(1),id(2))&&!s.canAllOut(id(1)),"no tactics before bonus");down(s,10);
  check(!s.canSwitchPersona(id(1))&&!s.canAllOut(id(1)),"persona locked and standing enemy prevents all-out");
  check(!s.batonPass(id(1),id(1))&&!s.batonPass(id(1),id(10))&&!s.batonPass(id(1),id(99)),"reject self, enemy and unknown receiver");
  s.find(id(3)).orElseThrow().setDowned(true);check(s.batonTargets().size()==1,"downed allies excluded");s.find(id(3)).orElseThrow().setDowned(false);
  check(s.batonPass(id(1),id(2))&&s.currentActor().id().equals(id(2))&&s.oneMore(),"receiver owns bonus");check(!s.batonPass(id(2),id(1)),"no cycling back to visited player");
  var p=s.currentActor();var victim=s.find(id(11)).orElseThrow();var skill=new SkillDefinition("probe","Probe",Element.NONE,70,100,0,0,false,SkillTarget.ONE_ENEMY);
  p.setBatonBoost(false);int base=DamageCalculator.roll(p,victim,skill,new Random(1)).damage();p.attackStatus(false);int buff=DamageCalculator.roll(p,victim,skill,new Random(1)).damage();p.setBatonBoost(true);int both=DamageCalculator.roll(p,victim,skill,new Random(1)).damage();
  check(Math.abs(both-buff*1.5)<=2&&both>base*1.8,"baton damage multiplier combines independently with normal buff");
  s.perform(id(2),BattleAction.guard());check(!s.oneMore()&&p.batonMultiplier()==1&&s.currentActor().id().equals(id(2)),"bonus consumed; receiver retains scheduled regular action");
  s.perform(id(2),BattleAction.guard());check(s.currentActor().id().equals(id(3)),"normal order resumes once");
  for(int removed:new int[]{1,2,3}){s=battle(2);down(s,10);s.batonPass(id(1),id(2));check(s.leave(id(removed)),"leave during bonus");if(removed!=2){check(s.currentActor().id().equals(id(2)),"non-actor departure preserves bonus");s.perform(id(2),BattleAction.guard());}var visited=new HashSet<UUID>();for(int i=0;i<s.participants().size();i++){check(visited.add(s.currentActor().id()),"no duplicate after departure");s.perform(s.currentActor().id(),BattleAction.guard());}}
  for(int count=1;count<=5;count++)for(boolean lethal:new boolean[]{false,true}){
   s=battle(count);for(int i=0;i<count;i++)down(s,10+i);check(s.canAllOut(id(1)),"all living foes down unlocks all-out");
   if(lethal)for(var enemy:s.enemiesOf(s.currentActor()))enemy.damage(enemy.hp()-1);
   var hp=new HashMap<UUID,Integer>();for(var c:s.participants())hp.put(c.id(),c.hp());int sp=s.currentActor().sp();
   var prepared=s.prepare(id(1),BattleAction.allOut());check(prepared!=null&&prepared.hits().size()==count&&!prepared.cutin(),"reserve all targets once without cut-in");
   check(prepared.hits().values().stream().allMatch(hit->hit.element()==Element.NONE&&hit.affinity()==Affinity.NORMAL&&!hit.missed()&&!hit.reflected()&&!hit.drained()),"elementless damage ignores all affinities");
   check(s.prepare(id(1),BattleAction.allOut())==null&&!s.batonPass(id(1),id(2)),"reservation prevents duplicate action or transfer");
   var result=s.perform(id(1),BattleAction.allOut());check(!result.oneMore()&&!result.weaknessHit()&&s.find(id(1)).orElseThrow().sp()==sp,"all-out no cost, weakness or bonus");
   for(var c:s.participants())check(c.hp()==(c.side()==BattleSide.PLAYERS?hp.get(c.id()):Math.max(0,hp.get(c.id())-prepared.hits().get(c.id()).damage())),"exact once-only damage to enemies, no friendly fire");
   check((s.state()==BattleState.PLAYERS_WON)==lethal,"lethal and nonlethal results");
  }
  for(boolean skip:new boolean[]{false,true}){s=battle(1);var next=s.find(id(2)).orElseThrow();next.knockDown(Element.FIRE,true);if(skip)next.knockDown(Element.FIRE,true);s.leave(id(1));check(s.currentActor().id().equals(id(skip?3:2)),"departure honors next actor's recovery/skip");}
  // Raw reserved damage is insufficient to decide whether the finisher may play.
  for(String passive:List.of("endure","enduring_soul","firm_stance")){
   s=battle(1);down(s,10);var enemy=s.find(id(10)).orElseThrow();
   var hit=s.prepare(id(1),BattleAction.allOut()).hits().get(id(10));
   int remaining=passive.equals("firm_stance")?Math.max(2,hit.damage()*3/4):1;
   enemy.damage(enemy.hp()-remaining);
   enemy.setPersona(new PersonaInstance(enemy.persona().definitionId(),enemy.persona().level(),0,Set.of(passive)));
   check(hit.damage()>=enemy.hp(),"old damage-only prediction would report victory");
   s.perform(id(1),BattleAction.allOut());
   check(enemy.alive()&&s.state()==BattleState.ACTIVE,"survival passive prevents victory finisher: "+passive);
  }
  packets();presentation();
  System.out.println("PASS tactics: "+checks+" checks; first/repeated weakness, bonus ownership, isolated Baton Pass damage, departure recovery, 1..5 elementless all-out targets, codecs, animation phases and HUD previews.");
 }
 private static void packets(){var b=new FriendlyByteBuf(Unpooled.buffer());try{
  for(var p:List.of(BattleTacticsS2C.empty(),new BattleTacticsS2C(id(1),true,12,"SHIFT",true,List.of(id(2)),List.of(new BattleTacticsS2C.Down(20,false),new BattleTacticsS2C.Down(21,true))))){BattleTacticsS2C.encode(p,b);check(p.equals(BattleTacticsS2C.decode(b)),"tactics codec");}
  for(boolean victory:new boolean[]{false,true}){var p=new AllOutCueS2C(true,10,List.of(10,11,12),victory);AllOutCueS2C.encode(p,b);check(p.equals(AllOutCueS2C.decode(b)),"all-out codec");}
  for(boolean victory:new boolean[]{false,true}){var p=new AllOutCueS2C(true,10,List.of(10,11,12),victory,"junpei",Map.of(10,"BOW"),AllOutTimeline.IMPACT);AllOutCueS2C.encode(p,b);check(p.equals(AllOutCueS2C.decode(b)),"server outcome confirmation roundtrip");}
  for(UUID target:new UUID[]{null,id(10)}){var p=new BattleCueS2C("skill","zio",true,1,2,3,4,5,6,target);BattleCueS2C.encode(p,b);check(p.equals(BattleCueS2C.decode(b)),"cue preserves actual target");}
  check(!b.isReadable(),"no trailing codec bytes");
  BattleTargetHud.lockActionTarget(id(10));check(BattleTargetHud.selectedId().equals(id(10)),"authoritative action target remains enemy");BattleTargetHud.reset();
 }finally{b.release();}}
 private static void presentation()throws Exception{
  for(boolean victory:new boolean[]{false,true})for(double t=0;t<AllOutTimeline.duration(victory);t+=.25){var phase=AllOutTimeline.phase(t,victory);check(victory||phase!=AllOutTimeline.Phase.FINISH&&phase!=AllOutTimeline.Phase.VICTORY,"nonlethal never shows finisher");for(String bone:List.of("head","right_arm","left_arm","right_leg","left_leg")){var p=AllOutPose.rotation(bone,t,victory);check(Float.isFinite(p.getX()+p.getY()+p.getZ()),"finite native pose");if(t>12&&phase!=AllOutTimeline.Phase.RETURN){var before=AllOutPose.rotation(bone,t-.01,victory);check(Math.abs(p.getX()-before.getX())<.04&&Math.abs(p.getY()-before.getY())<.04,"continuous visible pose transitions at "+t+" / "+bone);}}}
  for(int[] size:new int[][]{{640,360},{1280,720},{1920,1080},{960,720}}){int w=size[0],h=size[1];var a=BattleTacticalDesign.go(w,h);var b=BattleTacticalDesign.cancel(w,h);check(a.x()+a.w()<=b.x()&&b.x()+b.w()<=w,"prompt buttons do not overlap");}
  for(int members=1;members<=14;members++){double maxOffset=Math.ceil((members-1)/2.)*.19/AllOutPresentation.formationDensity(members);check(maxOffset<=.401,"entire party fits cinematic stage");}
  Files.createDirectories(Path.of("build/reports"));
  for(String name:List.of("one-more","shift","all-out-prompt","all-out-cloud")){var c=new Capture();AllOutPresentation.background(c,1280,720,24);if(name.equals("all-out-cloud"))AllOutPresentation.cloud(c,1280,720,78,1);else BattleTacticalDesign.paint(c,1280,720,true,true,name.equals("all-out-prompt"),name.equals("shift")?"SHIFT":"1 MORE",name.equals("all-out-prompt")?-1:15);Files.writeString(Path.of("build/reports/"+name+".json"),new com.google.gson.Gson().toJson(c.ops));}
 }
 private static final class Capture implements HudDesign.Canvas {
  final List<Object> ops=new ArrayList<>();
  public void fill(int x,int y,int w,int h,int color){ops.add(Map.of("type","fill","x",x,"y",y,"w",w,"h",h,"color",color));}
  public void text(String s,int x,int y,int color){styled(s,x,y,1,color);}
  public void styled(String s,int x,int y,float scale,int color){angled(s,x,y,scale,color,0);}
  public void angled(String s,int x,int y,float scale,int color,float angle){ops.add(Map.of("type","text","text",s,"x",x,"y",y,"scale",scale,"color",color,"angle",angle));}
  public int width(String s){return s.length()*6;}
 }
}
