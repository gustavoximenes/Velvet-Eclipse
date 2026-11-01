package com.gx.velveteclipse.exploration;
import com.gx.velveteclipse.client.exploration.*;
import com.gx.velveteclipse.client.presentation.*;
import com.gx.velveteclipse.client.HudDesign;
import com.gx.velveteclipse.core.battle.EncounterOpening;
import com.gx.velveteclipse.equipment.EquipmentCatalog.Family;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.network.FriendlyByteBuf;
import io.netty.buffer.Unpooled;
import org.joml.Vector3f;
import java.util.*;
import java.nio.file.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
/** Production pose/projection/packet/session regressions. No Minecraft client is launched. */
public final class EntryReleaseAudit {
 static int checks;
 static void check(boolean b,String why){checks++;if(!b)throw new AssertionError(why);}
 static class Mesh implements VertexConsumer {
  java.util.List<double[]> polygons=new ArrayList<>();double[] q=new double[12];int n;
  public VertexConsumer m_5483_(double x,double y,double z){check(Double.isFinite(x)&&Double.isFinite(y)&&Double.isFinite(z),"finite native rig");q[n++]=x;q[n++]=y;q[n++]=z;return this;}
  public VertexConsumer m_6122_(int r,int g,int b,int a){return this;}public VertexConsumer m_7421_(float u,float v){return this;}public VertexConsumer m_7122_(int a,int b){return this;}public VertexConsumer m_7120_(int a,int b){return this;}public VertexConsumer m_5601_(float x,float y,float z){return this;}
  public void m_5752_(){if(n==12){polygons.add(q);q=new double[12];n=0;}}public void m_7404_(int a,int b,int c,int d){}public void m_141991_(){}
 }
 static Mesh mesh(Family family,boolean slim,double tick){
  var f=EntryRunPose.sample(family,Math.max(0,(tick-12)/20),slim);
  var primary=family==Family.BOW?f.left():f.right();check(f.body().transformPosition(new Vector3f(primary.hand())).distance(f.grip())<.00001,"weapon follows actual primary wrist "+family);
  var p=new PoseStack();p.m_85841_(.9375f,.9375f,.9375f);p.m_85837_(0,f.feet()+f.height(),0);p.m_85841_(1,-1,-1);var m=new Mesh();
  ApprovedAttackRenderer.body(p,m,f,slim,Set.of(),15728880);
  // WeaponModels selects a RenderType even with a sink. Native mesh is read directly offline.
  try{var method=WeaponModels.class.getDeclaredMethod("model",Family.class);method.setAccessible(true);var model=(net.minecraft.client.model.geom.ModelPart)method.invoke(null,family==Family.NONE?Family.SWORD:family);
   p.m_85836_();p.m_85837_(f.grip().x,f.grip().y,f.grip().z);p.m_252781_(f.weapon());p.m_85841_(f.scale(),f.scale(),f.scale());model.m_104301_(p,m,15728880,655360);p.m_85849_();
  }catch(ReflectiveOperationException e){throw new AssertionError(e);}
  return m;
 }
 static class Canvas implements HudDesign.Canvas {
  final Graphics2D g;final Family family;final boolean slim;
  Canvas(Graphics2D g,Family family,boolean slim){this.g=g;this.family=family;this.slim=slim;}
  public void fill(int x,int y,int w,int h,int color){g.setColor(new Color(color,true));g.fillRect(x,y,w,h);}
  public void poly(double[] xy,int color){if(xy.length<6)return;var p=new Path2D.Double();p.moveTo(xy[0],xy[1]);for(int i=2;i<xy.length;i+=2)p.lineTo(xy[i],xy[i+1]);p.closePath();g.setColor(new Color(color,true));g.fill(p);}
  public void text(String s,int x,int y,int color){}public int width(String s){return s.length()*8;}
  public void playerModel(int w,int h,double t,float alpha){for(var q:mesh(family,slim,t).polygons){double[] xy=new double[8];for(int i=0;i<4;i++){var p=EntryProjection.project(q[i*3],q[i*3+1],q[i*3+2],w,h,t);check(p.isFinite()&&p.z>.05,"finite perspective in front of near plane");xy[2*i]=p.x;xy[2*i+1]=p.y;}for(var poly:EntryProjection.outside(xy,w,h,t))poly(poly,0xff1205ff);}}
 }
 public static void main(String[] args)throws Exception{
  var id=UUID.randomUUID();var other=UUID.randomUUID();
  for(var opening:EncounterOpening.values()){
   var p=new ExplorationNetwork.Entry(id,opening.ordinal(),13);var b=new FriendlyByteBuf(Unpooled.buffer());
   try{ExplorationNetwork.Entry.encode(p,b);check(p.equals(ExplorationNetwork.Entry.decode(b))&&!b.isReadable(),"entry packet round trip");}finally{b.release();}
   var state=new ExplorationNetwork.State(ExplorationRules.Mode.BATTLE_ENTRY.ordinal(),4,19,25,34,1,2,3,4,5,6,opening.ordinal());
   b=new FriendlyByteBuf(Unpooled.buffer());try{ExplorationNetwork.State.encode(state,b);check(state.equals(ExplorationNetwork.State.decode(b))&&!b.isReadable(),"formation packet keeps opening and sequence");}finally{b.release();}
   check(EntryTimeline.duration(opening)>ExplorationRules.ENTRY_READY,"formation before presentation completes");
   EntryTransition.reset();EntryTransition.membership(id);EntryTransition.accept(new ExplorationNetwork.Entry(other,opening.ordinal(),0));check(!EntryTransition.visible(),"outsider cannot start overlay");
   EntryTransition.accept(new ExplorationNetwork.Entry(id,opening.ordinal(),0));check(EntryTransition.visible(),"member begins");
   for(int t=0;t<EntryTimeline.duration(opening);t++){check(EntryTransition.visible(),"command gate holds entire presentation");EntryTransition.tick();}
   check(!EntryTransition.visible(),"entry expires");EntryTransition.accept(new ExplorationNetwork.Entry(id,opening.ordinal(),0));check(!EntryTransition.visible(),"old packet cannot restart");
   EntryTransition.membership(other);EntryTransition.accept(new ExplorationNetwork.Entry(id,opening.ordinal(),30));check(!EntryTransition.visible(),"old session ignored");
   EntryTransition.accept(new ExplorationNetwork.Entry(other,opening.ordinal(),0));EntryTransition.reset();check(!EntryTransition.visible(),"logout/world/session cleanup");
   if(opening==EncounterOpening.AMBUSH)for(int t=0;t<74;t++)check(!EntryTimeline.running(opening,t),"ambush never runs blue clip");
   if(opening==EncounterOpening.NEUTRAL)for(int t=0;t<74;t++)check(EntryTimeline.banner(opening,t)==0,"neutral has no advantage banner");
  }
  EntryTransition.prepare(EncounterOpening.ADVANTAGE.ordinal(),3);EntryTransition.cancelPreparation();check(!EntryTransition.visible(),"failed formation cancels entry");
  EntryTransition.prepare(EncounterOpening.ADVANTAGE.ordinal(),8);EntryTransition.membership(id);EntryTransition.accept(new ExplorationNetwork.Entry(id,EncounterOpening.ADVANTAGE.ordinal(),10));check(EntryTransition.visible()&&EntryTransition.time(0)==10,"formation hands off to same presentation clock");EntryTransition.cancelPreparation();check(EntryTransition.visible(),"completed handoff survives preparation cleanup");EntryTransition.reset();
  for(var family:Family.values())for(boolean slim:new boolean[]{false,true})for(double t=12;t<39;t+=.5){
   for(var q:mesh(family,slim,t).polygons)for(int i=0;i<4;i++){var p=EntryProjection.project(q[i*3],q[i*3+1],q[i*3+2],960,540,t);check(p.isFinite()&&p.z>.05,"dolly projection valid for "+family);}
  }
  var out=Path.of("build/reports/entry-release");Files.createDirectories(out);
  for(int[] size:new int[][]{{800,600},{960,540},{1260,540},{480,270}}){
   var montage=new BufferedImage(size[0]*3,size[1]*3,BufferedImage.TYPE_INT_RGB);var g=montage.createGraphics();int row=0;
   for(var variant:java.util.List.of(EncounterOpening.ADVANTAGE,EncounterOpening.NEUTRAL,EncounterOpening.AMBUSH)){
    int col=0;for(double t:new double[]{17,28,34}){var cell=(Graphics2D)g.create();cell.translate(col*size[0],row*size[1]);cell.setClip(0,0,size[0],size[1]);cell.setColor(new Color(0x314655));cell.fillRect(0,0,size[0],size[1]);cell.setColor(new Color(0x53727b));for(int x=0;x<size[0];x+=40)cell.drawLine(x,0,x,size[1]);
     EntryTransition.paint(new Canvas(cell,Family.SWORD,false),size[0],size[1],variant,t);cell.setColor(Color.WHITE);cell.drawString(variant+" / tick "+t+" / offline production geometry",10,size[1]-15);cell.dispose();col++;
    }row++;
   }g.dispose();ImageIO.write(montage,"PNG",out.resolve("entry-"+size[0]+"x"+size[1]+".png").toFile());
  }
  System.out.println("PASS entry release: "+checks+" checks; native standard/slim rigs, all weapon families, perspective, codec, membership, stale packets, cleanup, four aspect/GUI sizes. Offline only.");
 }
}
