package com.gx.velveteclipse.tests;
import com.gx.velveteclipse.client.menu.*;
import com.gx.velveteclipse.client.AllOutPlayerRenderer;
import com.gx.velveteclipse.core.battle.AllOutStyle;
import com.gx.velveteclipse.equipment.EquipmentCatalog.Family;
import com.gx.velveteclipse.menu.MenuSnapshot;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.world.entity.player.PlayerModelPart;
import java.util.*;
import java.nio.file.*;
import com.google.gson.Gson;
public final class MenuFixAudit {
 static int checks;static void check(boolean ok,String msg){checks++;if(!ok)throw new AssertionError(msg);}
 public static void main(String[] args)throws Exception{
  var r=AllOutSelectDesign.preview();for(var s:AllOutStyle.values()){var box=AllOutSelectDesign.option(s.ordinal());check(r.x()+r.w()<box.x(),"animation and selector never overlap");check(AllOutSelectDesign.pick(box.x()+5,box.y()+10)==s.ordinal(),"selector mouse matches new rows");}
  check(r.y()+r.h()<450&&r.x()>0,"preview and footer inside 900x500 frame");
  for(boolean motion:new boolean[]{true,false}){var p=ReloadMenuOpening.portrait(1,motion);check(p.angle()==211&&p.x()==305&&p.y()==252,"final approved framing unchanged");for(int i=0;i<11;i++)check(ReloadMenuOpening.option(1,i,motion)==1,"all options fully visible");check(ReloadMenuOpening.splash(1,motion)==0,"no persistent opening overlay");}
  check(!ReloadMenuOpening.active(0,false)&&ReloadMenuOpening.portrait(0,false).angle()==211,"reduced motion bypasses opening");
  for(int fps:new int[]{30,60,144})for(int i=0;i<=fps;i++){double t=i/(double)fps;var p=ReloadMenuOpening.portrait(t,true);check(Double.isFinite(p.x()+p.y()+p.angle()),"finite opening transforms");check(p.angle()>=6&&p.angle()<=211,"bounded clockwise turn");}
  for(boolean slim:new boolean[]{false,true})for(var style:AllOutStyle.values())for(int tick=90;tick<176;tick+=2){var stack=new PoseStack();AllOutPlayerRenderer.transform(stack,AllOutSelectDesign.frame());var mesh=new Capture();AllOutPlayerRenderer.body(stack,mesh,slim,EnumSet.allOf(PlayerModelPart.class),tick,true,style,Family.SWORD,0);for(var face:mesh.quads)for(var v:face)check(v[0]>=r.x()&&v[0]<=r.x()+r.w()&&v[1]>=r.y()&&v[1]<=r.y()+r.h(),"entire All-Out body inside preview: "+style+" / "+tick+" / slim="+slim);}
  Path out=Path.of("build/reports/menu-fixes");Files.createDirectories(out);var poses=new ArrayList<Object>();
  for(int i=0;i<36;i++){double age=i/30.;String name=String.format("opening-%03d",i);var v=new MenuDesign.View(true,0,0,0,List.of(),"",List.of(),null,"","","Jogador",47407,14,5000,age,true,0,age,age);
   var bg=new ReloadMenuAudit.Preview();ReloadMenuDesign.background(bg,v,false);bg.save(out.resolve(name+"-background.png"));
   var fg=new ReloadMenuAudit.Preview();ReloadMenuOpening.paint(fg,age,true);ReloadMenuDesign.foreground(fg,v,MenuSnapshot.empty(),0,false,0);fg.save(out.resolve(name+"-foreground.png"));
   var p=ReloadMenuOpening.portrait(age,true);poses.add(Map.of("name",name,"angle",p.angle(),"x",p.x(),"y",p.y()));
  }
  Files.writeString(out.resolve("opening-poses.json"),new Gson().toJson(poses));
  for(var style:AllOutStyle.values()){
   var bg=new ReloadMenuAudit.Preview();bg.fill(0,0,900,500,0xFF03133C);bg.fill(r.x(),r.y(),r.w(),r.h(),style.color);bg.poly(new double[]{r.x(),r.y()+r.h()*.3,r.x()+r.w(),r.y()+r.h()*.08,r.x()+r.w(),r.y()+r.h()*.3,r.x(),r.y()+r.h()*.52},0x46031340);bg.save(out.resolve("allout-"+style.id()+"-background.png"));
   var fg=new ReloadMenuAudit.Preview();AllOutSelectDesign.paint(fg,AllOutStyle.MAKOTO,true,style.ordinal());fg.save(out.resolve("allout-"+style.id()+"-foreground.png"));
   var stack=new PoseStack();AllOutPlayerRenderer.transform(stack,AllOutSelectDesign.frame());var mesh=new Capture();AllOutPlayerRenderer.body(stack,mesh,true,EnumSet.allOf(PlayerModelPart.class),145,true,style,Family.SWORD,0);check(mesh.quads.size()>30,"native allout geometry available");Files.writeString(out.resolve("allout-"+style.id()+"-mesh.json"),new Gson().toJson(mesh.quads));
  }
  System.out.println("PASS menu fixes: "+checks+" checks; All-Out hit targets and separation, opening timing/endpoints/reduced motion, real pose meshes and 36 production opening frames exported. Offline only.");
 }
 static class Capture implements VertexConsumer {
  final List<List<double[]>> quads=new ArrayList<>();List<double[]> face=new ArrayList<>();double[] p;
  public VertexConsumer m_5483_(double x,double y,double z){p=new double[]{x,y,z,0,0};return this;}
  public VertexConsumer m_6122_(int r,int g,int b,int a){return this;}public VertexConsumer m_7421_(float u,float v){p[3]=u;p[4]=v;return this;}
  public VertexConsumer m_7122_(int a,int b){return this;}public VertexConsumer m_7120_(int a,int b){return this;}public VertexConsumer m_5601_(float a,float b,float c){return this;}
  public void m_5752_(){check(Double.isFinite(p[0]+p[1]+p[2]),"finite native mesh");face.add(p);if(face.size()==4){quads.add(face);face=new ArrayList<>();}}public void m_7404_(int a,int b,int c,int d){}public void m_141991_(){}
 }
}
