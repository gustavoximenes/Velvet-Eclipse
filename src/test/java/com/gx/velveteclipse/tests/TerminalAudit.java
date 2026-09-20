package com.gx.velveteclipse.tests;
import com.gx.velveteclipse.darkhour.*;
import com.gx.velveteclipse.client.tartarus.TerminalDesign;
import com.gx.velveteclipse.client.menu.MenuDesign;
import java.util.*;
import java.nio.file.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import io.netty.buffer.Unpooled;
import com.google.gson.*;
public final class TerminalAudit {
 private static int checks;
 private static void check(boolean b,String m){checks++;if(!b)throw new AssertionError(m);}
 public static void main(String[] args)throws Exception{
  for(int f=1;f<=250;f++)check(TartarusInteriorLayout.shadowLimit(f)==(f%25==0?0:f%10==0?1:20),"guardian/transition precedence");
  var out=Path.of("build/reports/terminals");Files.createDirectories(out);
  for(int f:new int[]{0,1,24,25,26,50,250}){
   var p=TartarusInteriorLayout.floor(84,f);var props=TartarusInteriorLayout.props(84,f,p);var blocks=new HashMap<String,String>();for(var c:p.cells())blocks.put(c.x()+","+c.y()+","+c.z(),c.material());
   check(props.stream().filter(s->s.kind().equals("treasure")).count()<=3,"three chest maximum");
   for(var prop:props){int h=prop.kind().equals("terminal")?4:prop.kind().equals("monad")?5:2;for(int y=1;y<=h;y++)check(!blocks.containsKey(prop.at().x()+","+y+","+prop.at().z()),"prop clear of architecture: "+f+" "+prop.kind());
    for(var other:props)if(prop!=other)check(Math.hypot(prop.at().x()-other.at().x(),prop.at().z()-other.at().z())>=4,"props have separate footprints");
    for(var cell:p.cells())if(cell.material().equals("velvet_door"))check(Math.hypot(prop.at().x()-cell.x(),prop.at().z()-cell.z())>=4,"terminal/treasure clear of Velvet door");
   }
   if(TartarusInteriorLayout.bossFloor(f))for(int x=-2;x<=2;x++)for(int y=1;y<=12;y++)check((y<=4?"boss_gate":"barrier").equals(blocks.get(x+","+y+","+TartarusInteriorLayout.GATE_Z)),"guardian partition reaches ceiling");
   if(TartarusInteriorLayout.transitionFloor(f)){check(props.stream().anyMatch(s->s.kind().equals("monad"))==(f==25),"sealed decorative door reserved for floor 25");check(props.stream().filter(s->s.kind().equals("treasure")).count()==3,"fixed transition treasures");}
   if(f==250)check(p.cells().stream().anyMatch(c->c.material().equals("summit")),"floor 250 terminates");
   if(f==25||f==50){Files.writeString(out.resolve("floor-"+f+".json"),new Gson().toJson(p));Files.writeString(out.resolve("props-"+f+".json"),new Gson().toJson(props));}
  }
  var progress=new TartarusProgress();check(!progress.observe(18000),"initial clock adoption");progress.generated.put(1,0L);progress.generated.put(25,-1L);progress.treasures.addAll(List.of("1:0","25:0"));
  check(!progress.observe(18001)&&progress.current(1),"same Dark Hour never rerolls");check(!progress.observe(24000),"dawn retains ledger until next night");check(progress.observe(42000)&&progress.cycle==1,"next Dark Hour renews exactly once");
  check(!progress.current(1)&&progress.current(25),"only ordinary floors expire");check(!progress.treasures.contains("1:0")&&progress.treasures.contains("25:0"),"fixed treasures persist");
  var restored=TartarusProgress.load(progress.save());check(!restored.observe(42010)&&restored.cycle==1&&restored.treasures.contains("25:0"),"reload does not reset current night");check(restored.observe(66000),"skipped whole cycle is detected");
  long seed0=progress.seed(84,1);progress.observe(0);progress.observe(18000);check(seed0!=progress.seed(84,1),"time command starts a new layout");
  var a=TartarusInteriorLayout.floor(seed0,1);var b=TartarusInteriorLayout.floor(progress.seed(84,1),1);check(!a.walkable().equals(b.walkable()),"new cycle changes the actual room graph");
  check(TartarusInteriorLayout.floor(1,25).cells().equals(TartarusInteriorLayout.floor(900,25).cells()),"transition layout fixed across seeds");check(progress.treasures.add("25:1")&&!progress.treasures.add("25:1"),"treasure claim shared and once only");
  var discovered=TartarusProgress.discovered(new int[]{0,10,25,25,50,11,-1,251});check(discovered.equals(new TreeSet<>(List.of(0,10,25,50))),"only discovered terminal floors retained; invalid/duplicates removed");
  var state=new TartarusDungeonService.State();state.progress=progress;state.defeated.add(50);state.built.add(25);var copy=TartarusDungeonService.State.load(state.m_7176_(new CompoundTag()));check(copy.defeated.contains(50)&&copy.progress.treasures.contains("25:1"),"world ledger and defeated guardian survive save");
  var response=new TartarusTerminalNetwork.Response(25,List.of(50,25,10,0));var buffer=new FriendlyByteBuf(Unpooled.buffer());TartarusTerminalNetwork.Response.encode(response,buffer);check(response.equals(TartarusTerminalNetwork.Response.decode(buffer)),"destination packet round trip");buffer.release();
  var dir=Path.of("src/main/resources/assets/velvet_eclipse");
  for(String name:List.of("terminal","treasure","monad")){
   String stem="tartarus_"+name;String json=Files.readString(dir.resolve("geo/entity/"+stem+".geo.json"));var model=software.bernie.geckolib.util.JsonUtil.GEO_GSON.fromJson(json,software.bernie.geckolib.loading.json.raw.Model.class);var baked=software.bernie.geckolib.loading.object.BakedModelFactory.DEFAULT_FACTORY.constructGeoModel(software.bernie.geckolib.loading.object.GeometryTree.fromModel(model));check(baked.getBone("root").isPresent(),"native model bake "+stem);
   var raw=JsonParser.parseString(json).getAsJsonObject().getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();for(var bone:raw.getAsJsonArray("bones"))if(bone.getAsJsonObject().has("cubes"))for(var entry:bone.getAsJsonObject().getAsJsonArray("cubes")){var cube=entry.getAsJsonObject();var size=cube.getAsJsonArray("size");for(var n:size)check(n.getAsInt()>0&&n.getAsDouble()==n.getAsInt(),"integer positive Box UV dimensions");var uv=cube.getAsJsonArray("uv");check(uv.get(0).getAsInt()+2*(size.get(0).getAsInt()+size.get(2).getAsInt())<512&&uv.get(1).getAsInt()+size.get(1).getAsInt()+size.get(2).getAsInt()<512,"UV fits atlas");}
   var animation=JsonParser.parseString(Files.readString(dir.resolve("animations/entity/"+stem+".animation.json"))).getAsJsonObject().getAsJsonObject("animations");check(software.bernie.geckolib.util.JsonUtil.GEO_GSON.fromJson(animation,software.bernie.geckolib.loading.object.BakedAnimations.class).getAnimation("misc.idle")!=null,"native idle bake");
   check(Files.exists(dir.resolve("textures/entity/"+stem+"_glowmask.png")),"glowing detail atlas");
  }
  var c=new Preview();TerminalDesign.paint(c,List.of(100,90,80,75,70,60,50,40,30,25,20,10,0),25,9,5,2);c.save(out.resolve("terminal.png"));
  for(int i=0;i<8;i++)check(TerminalDesign.row(i).y()+TerminalDesign.row(i).h()<450,"selector rows fit HUD");
  System.out.println("PASS terminals: "+checks+" focused checks; cycle renewal, fixed progress, chests, gate height, prop spacing, destination codec and actual GeckoLib models. Offline only.");
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
