package com.gx.velveteclipse.tests;
import com.gx.velveteclipse.raid.*;import com.gx.velveteclipse.core.battle.*;import com.gx.velveteclipse.core.persona.*;
import com.gx.velveteclipse.client.menu.MenuDesign;import com.gx.velveteclipse.client.raid.RaidDesign;
import java.util.*;import java.nio.file.*;import com.google.gson.*;
public final class RaidAudit {
 private static int checks;
 private static void check(boolean b,String m){checks++;if(!b)throw new AssertionError(m);}
 public static void main(String[] args)throws Exception{
  var r=new PriestessRules(1000);var random=new Random(4);
  check(r.remaining(1000)==300000&&r.weakness()==Element.FIRE,"five real minutes, initial fire weakness");
  check(r.update(76,100,random)==0,"no early phase");
  check(r.update(75,100,random)==30&&r.phase()==1&&r.weakness()!=Element.FIRE,"75 percent phase");
  var first=r.weakness();check(r.update(75,100,random)==0,"same threshold cannot drain twice");
  check(r.update(50,100,random)==30&&r.weakness()!=first,"50 percent phase changes weakness again");
  check(r.update(25,100,random)==60&&r.phase()==3,"25 percent phase takes a full minute");
  check(r.remaining(61000)==120000,"elapsed time and penalties both count");
  check(r.remaining(400000)==0,"timeout clamps at zero");
  var burst=new PriestessRules(0);check(burst.update(20,100,random)==120,"one large hit crossing all thresholds applies each once");
  check(burst.update(100,100,random)==0&&burst.update(25,100,random)==0,"healing cannot retrigger phases");
  var killed=new PriestessRules(0);check(killed.update(0,100,random)==0&&killed.phase()==0,"lethal hit wins rather than losing to a time penalty");
  check(!killed.iceTurn()&&killed.iceTurn()&&!killed.iceTurn()&&killed.iceTurn(),"physical and area ice alternate every two boss turns");
  check(new PriestessRules(0).remaining(0)==300000,"retry has a fresh full timer");
  var victory=new PriestessRules(0);victory.stop(12000);check(victory.remaining(900000)==288000,"victory freezes the timer while waiting for result acknowledgements");
  PersonaRegistry.bootstrap();var def=PersonaRegistry.persona("umbra_slime").orElseThrow();
  var boss=new BattleCombatant(UUID.randomUUID(),"Sacerdotisa",BattleSide.ENEMIES,new PersonaInstance(def),999,999,999,999);boss.setRaidBoss(Element.FIRE);
  var actor=new BattleCombatant(UUID.randomUUID(),"Player",BattleSide.PLAYERS,new PersonaInstance(PersonaRegistry.persona("cinder_wisp").orElseThrow()),999,999,999,999);
  var hit=new DamageOutcome(Element.FIRE,Affinity.WEAK,30,true,false,false,false,false);
  for(int i=0;i<2;i++){var result=DamageCalculator.apply(actor,boss,hit);check(!result.newlyDowned()&&!boss.downed()&&!boss.skipsNextTurn(),"weakness plus crit never knocks down or stuns raid boss");}
  check(boss.hp()==939,"weakness damage still applies");boss.setDowned(true);check(!boss.downed(),"direct knockdown cannot bypass boss immunity");
  boss.setRaidBoss(Element.WIND);check(boss.affinity(Element.FIRE)==Affinity.NORMAL&&boss.affinity(Element.WIND)==Affinity.WEAK,"old weakness removed");
  var session=new BattleSession(List.of(actor,boss),new Random(8));check(!session.canAllOut(actor.id()),"all-out unavailable against standing boss");
  var ice=PersonaRegistry.skill("raid_ice_storm").orElseThrow();check(ice.element()==Element.ICE&&ice.target()==SkillTarget.ALL_ENEMIES,"ice is an area attack");
  check(com.gx.velveteclipse.shuffle.ShuffleRules.skillLevel(ice.id())==12,"new area skill card is gated above starting floors");
  boss.setRaidBoss(Element.FIRE);
  while(session.currentActor()!=actor)session.perform(session.currentActor().id(),BattleAction.guard());
  int hpBefore=boss.hp();var actualHit=session.perform(actor.id(),BattleAction.skill("ember",boss.id()));
  check(boss.hp()<hpBefore&&actualHit.weaknessHit()&&!session.oneMore()&&!session.canAllOut(actor.id())&&!boss.skipsNextTurn(),"real session: fire gives extra damage only, without bonus turn, all-out or stun");
  var group=new ArrayList<BattleCombatant>();group.add(new BattleCombatant(UUID.randomUUID(),"Boss",BattleSide.ENEMIES,new PersonaInstance("umbra_slime",12,0,Set.of(ice.id())),999,999,999,999));
  for(int i=0;i<5;i++)group.add(new BattleCombatant(UUID.randomUUID(),"Ally "+i,BattleSide.PLAYERS,new PersonaInstance(PersonaRegistry.persona("cinder_wisp").orElseThrow()),999,999,999,999));
  var areaSession=new BattleSession(group,new Random(0));while(areaSession.currentActor()!=group.get(0))areaSession.perform(areaSession.currentActor().id(),BattleAction.guard());
  areaSession.perform(group.get(0).id(),BattleAction.skill(ice.id(),group.get(1).id()));check(group.subList(1,6).stream().allMatch(c->c.hp()<999),"ice actually damages all five raid members in a combat session");
  var map=new HashMap<String,String>();for(var c:TrainLayout.build())map.put(c.x()+","+c.y()+","+c.z(),c.material());
  check(TrainLayout.build().stream().noneMatch(c->c.material().startsWith("tartarus")),"train uses its own architecture materials");
  for(int stage=0;stage<2;stage++)for(int x=-5;x<=5;x++)for(int y=1;y<7;y++)check("metal".equals(map.get(x+","+y+","+TrainLayout.gateZ(stage))),"subboss gate seals entire width to ceiling");
  for(int count=1;count<=PriestessRules.MAX_CAPACITY;count++)for(int stage=0;stage<3;stage++)for(int i=0;i<count;i++){
   int x=(int)Math.floor(.5+(i%3-1)*2.1),z=(int)Math.floor(TrainLayout.encounterZ(stage)-7-(i/3)*2.1);
   check(!"air".equals(map.get(x+",0,"+z))&&"air".equals(map.get(x+",1,"+z))&&"air".equals(map.get(x+",2,"+z)),"formation has floor and headroom");
  }
  var a=Path.of("src/main/resources/assets/velvet_eclipse");
  var raw=software.bernie.geckolib.util.JsonUtil.GEO_GSON.fromJson(Files.readString(a.resolve("geo/entity/priestess.geo.json")),software.bernie.geckolib.loading.json.raw.Model.class);
  var baked=software.bernie.geckolib.loading.object.BakedModelFactory.DEFAULT_FACTORY.constructGeoModel(software.bernie.geckolib.loading.object.GeometryTree.fromModel(raw));check(baked.getBone("head").isPresent()&&baked.getBone("hair_black_tip").isPresent(),"real GeckoLib bake of approved head and braid rig");
  var anim=JsonParser.parseString(Files.readString(a.resolve("animations/entity/priestess.animation.json"))).getAsJsonObject().getAsJsonObject("animations");
  var clips=software.bernie.geckolib.util.JsonUtil.GEO_GSON.fromJson(anim,software.bernie.geckolib.loading.object.BakedAnimations.class);check(clips.getAnimation("misc.idle")!=null&&clips.getAnimation("action.attack")!=null,"native idle and attack bake");
  for(String name:List.of("floor","wall","window","seat","metal","lamp","rail"))check(Files.exists(a.resolve("models/item/train_"+name+".json"))&&Files.exists(a.resolve("textures/block/train_"+name+".png")),"creative train block resources");
  var out=Path.of("build/reports/raid");Files.createDirectories(out);Files.writeString(out.resolve("train-layout.json"),new Gson().toJson(TrainLayout.build()));
  var names=java.util.stream.IntStream.range(0,12).mapToObj(i->"Player_"+(i+1)).toList();
  var view=new RaidNetwork.View("lobby",names,12,true,5,0,"",0,"",0);
  var buf=new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());RaidNetwork.View.encode(view,buf);check(view.equals(RaidNetwork.View.decode(buf)),"full 12-player roster packet round trip");buf.release();
  var hud=new Preview();RaidDesign.lobby(hud,view);hud.save(out.resolve("lobby.png"));
  var dialog=new RaidNetwork.View("dialogue",names,12,true,6,0,"Fiquem juntos. Vamos atravessar o trem e descobrir o que está controlando tudo isso.",0,"",0);
  hud=new Preview();RaidDesign.dialogue(hud,dialog,false);hud.save(out.resolve("dialogue.png"));
  System.out.println("PASS raid: "+checks+" checks; timer phases, retry, changing affinity, boss immunity, area ice, sealed train gates, 1–12 player formation and native GeckoLib assets. Offline only.");
 }
 private static final class Preview implements MenuDesign.Canvas {
  final java.awt.image.BufferedImage image=new java.awt.image.BufferedImage(900,500,2);final java.awt.Graphics2D g=image.createGraphics();final java.awt.Font display,body;
  Preview()throws Exception{display=java.awt.Font.createFont(0,Path.of("src/main/resources/assets/velvet_eclipse/font/display.ttf").toFile());body=java.awt.Font.createFont(0,Path.of("src/main/resources/assets/velvet_eclipse/font/body.ttf").toFile());g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,java.awt.RenderingHints.VALUE_ANTIALIAS_ON);}
  public void fill(double x,double y,double w,double h,int color){g.setColor(new java.awt.Color(color,true));g.fill(new java.awt.geom.Rectangle2D.Double(x,y,w,h));}
  public void poly(double[] xy,int color){var p=new java.awt.geom.Path2D.Double();p.moveTo(xy[0],xy[1]);for(int i=2;i<xy.length;i+=2)p.lineTo(xy[i],xy[i+1]);p.closePath();g.setColor(new java.awt.Color(color,true));g.fill(p);}
  public void text(String t,double x,double y,double size,int color,double angle){var old=g.getTransform();g.translate(x,y);g.rotate(Math.toRadians(angle));g.setColor(new java.awt.Color(color,true));g.setFont((size>=20?display:body).deriveFont((float)size));g.drawString(t,0,g.getFontMetrics().getAscent());g.setTransform(old);}
  public double width(String t,double s){return t.length()*s*.5;}public void item(int slot,double x,double y){}
  void save(Path p)throws Exception{javax.imageio.ImageIO.write(image,"png",p.toFile());g.dispose();}
 }
}
