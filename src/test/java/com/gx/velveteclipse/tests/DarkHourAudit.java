package com.gx.velveteclipse.tests;
import com.gx.velveteclipse.darkhour.*;
import com.gx.velveteclipse.client.darkhour.DarkHourClockDesign;
import com.gx.velveteclipse.client.*;
import com.gx.velveteclipse.client.menu.*;
import com.gx.velveteclipse.room.FusionSnapshot;
import com.google.gson.*;
import java.util.*;
import java.util.List;
import java.nio.file.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public final class DarkHourAudit {
 private static int checks;
 private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
 public static void main(String[] args)throws Exception {
  for(long day:new long[]{0,1,1000,1000000}){
   int dark=0,warn=0;for(int t=0;t<24000;t++){long clock=day*24000+t;if(DarkHourCycle.active(clock))dark++;if(DarkHourCycle.warning(clock))warn++;}
   check(dark==6000&&warn==200,"one dark period and one warning in every vanilla day");
   check(!DarkHourCycle.active(day*24000+17999)&&DarkHourCycle.active(day*24000+18000),"midnight transition");
   check(DarkHourCycle.active(day*24000+23999)&&!DarkHourCycle.active((day+1)*24000),"dawn transition restores day");
  }
  check(DarkHourCycle.time(-1)==23999,"negative command time normalized");
  check(!DarkHourService.eligibleType(com.gx.velveteclipse.entity.VelvetPersonaGeoEntity.class),"summoned Personas and evoker FX are never coffins, without tags");
  check(!DarkHourService.eligibleType(com.gx.velveteclipse.entity.VelvetShadowGeoEntity.class),"Shadows exempt even before registry initialization");
  check(!DarkHourService.eligibleType(net.minecraft.world.entity.player.Player.class),"players exempt");
  check(DarkHourService.eligibleType(net.minecraft.world.entity.animal.Cow.class),"normal passive mobs become coffins");
  check(DarkHourService.eligibleType(net.minecraft.world.entity.monster.Zombie.class),"normal hostile mobs become coffins");
  check(DarkHourService.eligibleType(net.minecraft.world.entity.monster.Vex.class),"vanilla Vex remains a normal mob, only Persona subclass is exempt");
  var dispatcher=new com.mojang.brigadier.CommandDispatcher<net.minecraft.commands.CommandSourceStack>();
  dispatcher.register(net.minecraft.commands.Commands.m_82127_("locate").then(net.minecraft.commands.Commands.m_82127_("structure").then(net.minecraft.commands.Commands.m_82129_("structure",com.mojang.brigadier.arguments.StringArgumentType.word()).executes(c->1))));
  DarkHourService.commands(new net.minecraftforge.event.RegisterCommandsEvent(dispatcher,net.minecraft.commands.Commands.CommandSelection.ALL,null));
  check(dispatcher.findNode(List.of("locate","tartarus")).getCommand()!=null,"locate tartarus is executable");
  check(dispatcher.findNode(List.of("locate","structure","velvet_eclipse:tartarus")).getCommand()!=null,"native locate structure route resolves tower");
  check(dispatcher.findNode(List.of("locate","structure","structure")).getCommand()!=null,"existing vanilla structure argument preserved");
  check(dispatcher.findNode(List.of("velvet","tartarus","owner","player")).getCommand()!=null,"dedicated owner configuration registered");
  UUID host=new UUID(0,9),guest=new UUID(0,1),owner=new UUID(0,5);
  var players=List.of(new TartarusPlacement.Player(guest,false,false,4),new TartarusPlacement.Player(host,true,false,0),new TartarusPlacement.Player(owner,false,true,4));
  check(TartarusPlacement.preferred(players).equals(host),"LAN host takes priority even when not first in player list");
  check(TartarusPlacement.preferred(players.stream().filter(p->!p.host()).toList()).equals(owner),"dedicated owner takes priority over other operators");
  check(TartarusPlacement.preferred(List.of())==null,"empty server does not generate tower");
  for(int server=2;server<=32;server++)for(int client=2;client<=32;client++)for(int attempt=0;attempt<12;attempt++){
   int range=TartarusPlacement.range(server,client);var candidate=TartarusPlacement.candidate(-127,219,range,attempt);
   check(TartarusPlacement.visible(-127,219,candidate.x(),candidate.z(),range),"every candidate is inside the effective render radius");
   check(Math.abs(candidate.x()+127)<=64&&Math.abs(candidate.z()-219)<=64,"retry never expands search away from host");
  }
  var saved=new TartarusService.State();saved.x=12;saved.y=90;saved.z=-60;saved.height=230;saved.drop=26;saved.index=120;saved.owner=owner.toString();saved.anchor=host.toString();saved.placed.add(12345L);saved.placed.add(67890L);saved.removing=true;
  var restored=TartarusService.State.load(saved.m_7176_(new net.minecraft.nbt.CompoundTag()));
  check(restored.placed.equals(saved.placed)&&restored.owner.equals(saved.owner)&&restored.anchor.equals(saved.anchor)&&restored.removing&&restored.index==saved.index,"cleanup journal and owner survive restart");
  var legacy=saved.m_7176_(new net.minecraft.nbt.CompoundTag());legacy.m_128473_("format");legacy.m_128405_("drop",4);var migrated=TartarusService.State.load(legacy);
  check(migrated.removing&&migrated.placed.size()==saved.index,"previous placement format is cleaned before moving tower near host");
  for(int height:new int[]{80,192,250,256,384})for(int drop:new int[]{1,4,8,32,96}){
   var cells=TartarusLayout.create(height,drop);var map=new HashMap<String,TartarusLayout.Cell>();int previous=-97;
   for(var c:cells){check(c.y()>=-96&&c.y()<height,"all blocks inside plan limits");check(c.y()>=previous,"bottom-up animation order");previous=c.y();check(map.put(key(c.x(),c.y(),c.z()),c)==null,"one block per location");}
   check(cells.get(cells.size()-1).y()==height-1,"spire reaches final legal block");check(cells.size()<90000,"bounded generation workload");
   for(int z=0;z<=TartarusLayout.STAIR_Z;z++)for(int y=1;y<=3;y++)check(!map.containsKey(key(0,y,z))||map.get(key(0,y,z)).material()==4,"open three-block-high approach and walk-through portal");
   for(int step=0;step<=drop;step++)for(int x=-3;x<=3;x++){var c=map.get(key(x,-step,TartarusLayout.STAIR_Z+step));check(c!=null&&c.material()==3,"continuous wide staircase");check(!map.containsKey(key(x,2-step,TartarusLayout.STAIR_Z+step)),"stairs have headroom");}
  }
  var out=Path.of("build/reports/dark-hour");Files.createDirectories(out);
  Files.writeString(out.resolve("tower.json"),new Gson().toJson(TartarusLayout.create(250,4)));
  var assets=Path.of("src/main/resources/assets/velvet_eclipse");
  for(String block:List.of("tartarus_stone","tartarus_trim","tartarus_light","tartarus_stairs")){
   var states=JsonParser.parseString(Files.readString(assets.resolve("blockstates/"+block+".json"))).getAsJsonObject().getAsJsonObject("variants");
   check(states.size()==(block.endsWith("stairs")?40:1),"all state permutations have models");
   for(var e:states.entrySet()){var p=e.getValue().getAsJsonObject().get("model").getAsString().split(":")[1];check(Files.exists(assets.resolve("models/"+p+".json")),"block model exists");}
  }
  for(String material:List.of("stone","trim","light")){var im=ImageIO.read(assets.resolve("textures/block/tartarus_"+material+".png").toFile());check(im.getWidth()==16&&im.getHeight()==16,"native pixel textures");}
  for(boolean green:new boolean[]{false,true}){var canvas=new Preview(900,500);DarkHourClockDesign.paint(canvas,900,500,green?1:.88,green,24);canvas.save(out.resolve(green?"clock-green.png":"clock-warning.png"));}
  var snap=new FusionSnapshot(7,37,100,List.of(),List.of(),null,"","","",false,false,"Bem-vindo à Velvet Room. Gostaria de fundir suas Personas?",false);
  var canvas=new Preview(900,500);var view=new FusionDesign.View(snap,0,0,0,"",false,1);FusionDesign.background(canvas,view);FusionDesign.foreground(canvas,view);canvas.save(out.resolve("fusion-hud.png"));
  canvas=new Preview(900,500);FusionDesign.help(canvas);canvas.save(out.resolve("fusion-help.png"));
  System.out.println("PASS dark-hour: "+checks+" checks; Persona/coffin exclusions, host priority, 11532 bounded placement candidates, persistence journal, clock boundaries, 25 tower layouts, staircase/entrance clearance, height limit and resources; offline HUD previews generated.");
 }
 private static String key(int x,int y,int z){return x+","+y+","+z;}
 private static final class Preview implements MenuDesign.Canvas,HudDesign.Canvas {
  final BufferedImage im;final Graphics2D g;final Font display,body;
  Preview(int w,int h)throws Exception{im=new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);g=im.createGraphics();g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);var assets=Path.of("src/main/resources/assets/velvet_eclipse/font");display=Font.createFont(Font.TRUETYPE_FONT,assets.resolve("display.ttf").toFile());body=Font.createFont(Font.TRUETYPE_FONT,assets.resolve("body.ttf").toFile());}
  public void fill(int x,int y,int w,int h,int c){fill((double)x,y,w,h,c);}
  public void fill(double x,double y,double w,double h,int c){check(Double.isFinite(x+y+w+h)&&w>=0&&h>=0,"finite draw rectangle");g.setColor(new Color(c,true));g.fill(new Rectangle2D.Double(x,y,w,h));}
  public void poly(double[] xy,int c){var p=new Path2D.Double();p.moveTo(xy[0],xy[1]);for(int i=2;i<xy.length;i+=2)p.lineTo(xy[i],xy[i+1]);p.closePath();g.setColor(new Color(c,true));g.fill(p);}
  private void glyph(String s,double x,double y,double size,int c,double angle,Font f){var old=g.getTransform();g.translate(x,y);g.rotate(Math.toRadians(angle));g.setColor(new Color(c,true));g.setFont(f.deriveFont((float)size));g.drawString(s,0,g.getFontMetrics().getAscent());g.setTransform(old);}
  public void text(String s,double x,double y,double size,int c,double angle){glyph(s,x,y,size,c,angle,size>=18?display:body);}
  public void text(String s,int x,int y,int c){glyph(s,x,y,10,c,0,body);}
  public void styled(String s,int x,int y,float scale,int c){glyph(s,x,y,12*scale,c,0,display);}
  public void angled(String s,int x,int y,float scale,int c,float angle){glyph(s,x,y,12*scale,c,angle,display);}
  public double width(String s,double size){return g.getFontMetrics(body.deriveFont((float)size)).stringWidth(s);}
  public int width(String s){return (int)width(s,10);}
  public void item(int s,double x,double y){}
  void save(Path path)throws Exception{g.dispose();ImageIO.write(im,"png",path.toFile());}
 }
}
