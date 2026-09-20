package com.gx.velveteclipse.tests;
import com.gx.velveteclipse.darkhour.*;
import com.gx.velveteclipse.core.battle.*;
import com.gx.velveteclipse.client.*;
import com.gx.velveteclipse.network.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import io.netty.buffer.Unpooled;
import java.util.*;
import java.nio.file.*;
import com.google.gson.Gson;
import com.mojang.blaze3d.vertex.*;

public final class IntervalAudit {
 private static int checks;
 private static void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
 private static Set<TartarusInteriorLayout.Point> reachable(TartarusInteriorLayout.Plan plan,boolean defeated){
  var blocked=new HashSet<TartarusInteriorLayout.Point>();
  for(var c:plan.cells())if((c.y()==1||c.y()==2)&&!Set.of("back","forward").contains(c.material())&&!(defeated&&c.material().equals("boss_gate")))blocked.add(new TartarusInteriorLayout.Point(c.x(),c.z()));
  var found=new HashSet<TartarusInteriorLayout.Point>();var q=new ArrayDeque<TartarusInteriorLayout.Point>();q.add(plan.entry());found.add(plan.entry());
  while(!q.isEmpty()){var p=q.remove();for(int[] d:new int[][]{{1,0},{-1,0},{0,1},{0,-1}}){var next=new TartarusInteriorLayout.Point(p.x()+d[0],p.z()+d[1]);if(plan.walkable().contains(next)&&!blocked.contains(next)&&found.add(next))q.add(next);}}
  return found;
 }
 public static void main(String[] args)throws Exception{
  for(int f=1;f<=250;f++)check(TartarusInteriorLayout.shadowLimit(f)==(f%10==0?1:f%25==0?0:7),"one guardian every tenth floor, seven ordinary Shadows otherwise");
  var out=Path.of("build/reports/interval");Files.createDirectories(out);
  for(int f:new int[]{10,50,170,250}){
   var p=TartarusInteriorLayout.floor(481,f);var before=reachable(p,false);var after=reachable(p,true);
   check(p.spawns().equals(List.of(new TartarusInteriorLayout.Point(0,256))),"guardian exactly in second room center");
   check(before.contains(p.spawns().get(0)),"guardian reachable from preparation");
   check(!before.contains(new TartarusInteriorLayout.Point(0,225)),"stairs blocked before guardian defeat");
   check(after.contains(new TartarusInteriorLayout.Point(0,225)),"stairs reachable after gate removal");
   check(before.contains(new TartarusInteriorLayout.Point(-6,292)),"Velvet door approach reachable");
   check(p.cells().stream().filter(c->c.material().equals("velvet_door")).count()==1,"one permanent preparation door");
   check(p.cells().stream().filter(c->c.material().equals("back")).allMatch(c->c.z()==302&&!p.walkable().contains(new TartarusInteriorLayout.Point(c.x(),c.z()))),"return portal remains off passage");
   check(p.cells().stream().allMatch(c->Math.abs(c.x())<96&&c.z()>180&&c.z()<320&&c.y()<=13),"new interval plot avoids old generated geometry");
   for(int z:new int[]{237,276})for(int x=-14;x<=14;x++)check(p.walkable().contains(new TartarusInteriorLayout.Point(x,z))==(Math.abs(x)<=2),"single centered corridor");
   for(int step=0;step<8;step++){int y=step+1,z=224-step;check(p.cells().stream().anyMatch(c->c.x()==0&&c.z()==z&&c.y()==y&&c.material().equals("stairs")),"continuous exit staircase");check(p.cells().stream().noneMatch(c->c.x()==0&&c.z()==z&&(c.y()==y+1||c.y()==y+2)),"stair head clearance");}
   if(f==10)Files.writeString(out.resolve("floor.json"),new Gson().toJson(p.cells()));
  }
  var state=new TartarusDungeonService.State();state.built.add(3);state.intervals.add(3);state.defeated.add(3);var restored=TartarusDungeonService.State.load(state.m_7176_(new CompoundTag()));check(restored.intervals.contains(3)&&restored.defeated.contains(3)&&restored.built.contains(3),"guardian defeat and migration persist on restart");
  var one=UUID.randomUUID();var two=UUID.randomUUID();var gate=new BattleResultsGate();check(gate.begin(List.of(one,two)),"first finish grants rewards");check(!gate.begin(List.of(one,two)),"repeat finish cannot grant rewards again");check(!gate.release(UUID.randomUUID()),"outsider cannot release results");check(gate.release(one)&&!gate.release(one)&&gate.waiting(two),"each member must confirm; repeated Enter harmless");check(gate.release(two)&&!gate.waiting(two),"last confirmation releases final member");
  var packet=new BattleResultsS2C(UUID.randomUUID().toString(),true,240,5,6,20,125,"Izanagi",6,"NENHUM ITEM",true);var buf=new FriendlyByteBuf(Unpooled.buffer());try{BattleResultsS2C.encode(packet,buf);check(packet.equals(BattleResultsS2C.decode(buf))&&!buf.isReadable(),"results codec preserves rewards and battle token");}finally{buf.release();}
  var commands=new com.mojang.brigadier.CommandDispatcher<net.minecraft.commands.CommandSourceStack>();
  var vanilla=commands.register(net.minecraft.commands.Commands.m_82127_("teleport").then(net.minecraft.commands.Commands.m_82127_("untouched").executes(c->9)));
  commands.register(net.minecraft.commands.Commands.m_82127_("tp").redirect(vanilla));TartarusCommands.register(commands);
  for(String alias:new String[]{"tp","teleport"})for(int f:new int[]{0,3,249,250}){var parsed=commands.parse(alias+" tartarus floor "+f,null);check(!parsed.getReader().canRead()&&parsed.getExceptions().isEmpty(),"floor command parses through vanilla alias redirect");}
  check(commands.execute("tp untouched",null)==9,"ordinary teleport subtree preserved");
  for(int f:new int[]{-1,251}){var parsed=commands.parse("tp tartarus floor "+f,null);check(parsed.getReader().canRead()||!parsed.getExceptions().isEmpty(),"floor command rejects outside 0..250");}
  var preview=new Preview();BattleResultsDesign.paint(preview,packet,900,500,2);preview.save(out.resolve("results.png"));
  preview=new Preview();AllOutPresentation.background(preview,900,500,45);preview.save(out.resolve("arena.png"));
  preview=new Preview();AllOutPresentation.victoryBackdrop(preview,900,500,"PLAYER");preview.save(out.resolve("victory.png"));
  for(boolean slim:new boolean[]{false,true})for(int tick:new int[]{24,50,104,140}){
   var mesh=new Mesh();var rig=AllOutPlayerRenderer.body(new PoseStack(),mesh,slim,Set.of(),tick,true);
   if(tick==140){var hand=new PoseStack();EvokerHandLayer.bindHand(hand,rig.m_171324_("right_arm"),new dev.kosmx.playerAnim.core.util.Vec3f(0,AllOutPose.bend("right_arm",tick,true),0),slim);var point=hand.m_85850_().m_252922_().transformPosition(new org.joml.Vector3f());check(Math.abs(point.x())<.08&&point.y()<.10&&point.y()>-.04&&point.z()<-.17,"victory hand reaches collar with standard and slim arms");}check(mesh.quads.size()>30,"actual standard/slim mesh emitted");
   for(var quad:mesh.quads)for(var v:quad)check(Double.isFinite(v[0]+v[1]+v[2])&&v[3]>=0&&v[3]<=1&&v[4]>=0&&v[4]<=1,"native mesh finite and skin UV valid");
   Files.writeString(out.resolve("pose-"+tick+(slim?"-slim":"")+".json"),new Gson().toJson(mesh.quads));
  }
  check(AllOutPose.bend("right_arm",140,true)<-1.3,"victory arm folds toward collar instead of outside shoulder");
  System.out.println("PASS interval: "+checks+" checks; guardian rooms/gate/Velvet access, persisted defeat, independent result acknowledgement and codec, native standard/slim poses. Offline previews generated.");
 }
 private static final class Preview implements HudDesign.Canvas{
  final java.awt.image.BufferedImage image=new java.awt.image.BufferedImage(900,500,2);final java.awt.Graphics2D g=image.createGraphics();final java.awt.Font display,body;
  Preview()throws Exception{display=java.awt.Font.createFont(0,Path.of("src/main/resources/assets/velvet_eclipse/font/display.ttf").toFile());body=java.awt.Font.createFont(0,Path.of("src/main/resources/assets/velvet_eclipse/font/body.ttf").toFile());g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,java.awt.RenderingHints.VALUE_ANTIALIAS_ON);fill(0,0,900,500,0xFF030B2B);}
  public void fill(int x,int y,int w,int h,int color){g.setColor(new java.awt.Color(color,true));g.fillRect(x,y,w,h);}
  public int width(String text){return text.length()*6;}
  public void text(String t,int x,int y,int color){draw(t,x,y,11,color,0,body);}
  public void styled(String t,int x,int y,float scale,int color){angled(t,x,y,scale,color,0);}
  public void angled(String t,int x,int y,float scale,int color,float degrees){draw(t,x,y,scale*11,color,degrees,display);}
  private void draw(String text,int x,int y,float size,int color,float angle,java.awt.Font font){var old=g.getTransform();g.translate(x,y);g.rotate(Math.toRadians(angle));g.setFont(font.deriveFont(size));g.setColor(new java.awt.Color(color,true));g.drawString(text,0,g.getFontMetrics().getAscent());g.setTransform(old);}
  void save(Path p)throws Exception{javax.imageio.ImageIO.write(image,"png",p.toFile());g.dispose();}
 }
 private static final class Mesh implements VertexConsumer {
  List<List<double[]>> quads=new ArrayList<>();List<double[]> vertices=new ArrayList<>();double[] point;
  public VertexConsumer m_5483_(double x,double y,double z){point=new double[]{x,y,z,0,0};return this;}
  public VertexConsumer m_6122_(int r,int g,int b,int a){return this;}public VertexConsumer m_7421_(float u,float v){point[3]=u;point[4]=v;return this;}
  public VertexConsumer m_7122_(int a,int b){return this;}public VertexConsumer m_7120_(int a,int b){return this;}public VertexConsumer m_5601_(float a,float b,float c){return this;}
  public void m_5752_(){vertices.add(point);if(vertices.size()==4){quads.add(vertices);vertices=new ArrayList<>();}}public void m_7404_(int a,int b,int c,int d){}public void m_141991_(){}
 }
}
