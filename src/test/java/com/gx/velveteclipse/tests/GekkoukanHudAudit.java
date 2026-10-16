package com.gx.velveteclipse.tests;
import com.gx.velveteclipse.city.*;
import com.gx.velveteclipse.client.*;
import com.gx.velveteclipse.client.menu.MenuDesign;
import com.gx.velveteclipse.localization.*;
import java.nio.file.*;import java.util.*;import java.util.List;
import java.awt.*;import java.awt.geom.*;import java.awt.image.BufferedImage;import javax.imageio.ImageIO;

public final class GekkoukanHudAudit {
 static int checks;static void check(boolean yes,String why){checks++;if(!yes)throw new AssertionError(why);}
 public static void main(String[] args)throws Exception{
  check(CityPlan.get().revision()==6,"New city revision");check(CityPlan.legacy(5).revision()==5,"Exact last installed plan retained for migration");
  for(int[] size:new int[][]{{1920,1080},{1366,768},{1280,720},{2560,1440}})for(int gui:new int[]{1,2,3,4}){
   var l=ExplorationHudLayout.of(size[0]/gui,size[1]/gui);
   check(l.vitalsY()>51&&l.mapX()>188,"Cards do not collide");
   check(l.bottom()*l.scale()<size[1]/(double)gui-50,"Minecraft HUD remains reserved");
   double physical=178*l.scale()*gui;double expected=356*Math.min(size[0]/1920.,size[1]/1080.);
   check(Math.abs(physical-expected)<2,"Consistent physical card size across GUI scales");
  }
  for(String lang:List.of("en_us","pt_br")){
   var t=new TextCatalog(TextCatalog.read("/assets/velvet_eclipse/localization/"+lang+".json"));
   check(t.translate("DARK HOUR").equals("DARK HOUR"),"Complete Dark Hour title");check(t.translate("[P]  SHUFFLE TIME  × 2").equals("[P]  SHUFFLE TIME  × 2"),"Complete Shuffle Time title");
  }
  var state=new CityState();state.choice=1;state.revision=4;state.ready=true;
  check(CityBuilder.beginRevision(state)&&state.rebuilding&&!state.ready&&state.targetRevision==6,"Installed revision 4 schedules migration");
  check(!CityBuilder.beginRevision(state),"Repeated ticks do not reset the migration cursor");
  var night=new CityNightSchool.State();var tag=night.m_7176_(new net.minecraft.nbt.CompoundTag());tag.m_128405_("phase",3);tag.m_128405_("schoolRevision",0);
  var load=CityNightSchool.State.class.getDeclaredMethod("load",net.minecraft.nbt.CompoundTag.class);load.setAccessible(true);var restored=(CityNightSchool.State)load.invoke(null,tag);
  check(restored.m_7176_(new net.minecraft.nbt.CompoundTag()).m_128451_("phase")==4,"Old nighttime campus restores before new campus builds");
  check(CityNightSchool.contains(80,-163)&&CityNightSchool.contains(54,-115)&&!CityNightSchool.contains(85,-60),"Gym and gallery transform; station stays outside school footprint");
  long trees=Arrays.stream(CityPlan.get().cells()).filter(c->c[3]>=100).filter(c->{String id=CityPlan.get().palette().get(c[3]-100).block();return (id.equals("minecraft:oak_log")||id.equals("minecraft:oak_leaves")||id.equals("minecraft:cherry_log")||id.equals("minecraft:cherry_leaves"))&&c[2]<-54&&c[0]>-30&&c[0]<30;}).count();check(trees>=959,"Approved trees are native Minecraft blocks");
  var assets=Path.of("src/main/resources/assets/velvet_eclipse");
  for(String id:List.of("school_stone","school_plaster","school_tiles","school_metal","school_wood")){
   check(Files.exists(assets.resolve("models/item/city_"+id+".json")),"School creative material has item model");
   check(com.gx.velveteclipse.room.CreativeCategory.of("city_"+id,true)==com.gx.velveteclipse.room.CreativeCategory.BLOCKS,"School material in Blocks tab");
   for(String lang:List.of("en_us","pt_br"))check(TextCatalog.read("/assets/velvet_eclipse/lang/"+lang+".json").containsKey("block.velvet_eclipse.city_"+id),"School material localized");
  }
  var p=Path.of("build/reports/gekkoukan");Files.createDirectories(p);render(p.resolve("hud-compacta.png"),assets);
  Files.writeString(p.resolve("hud-validation.json"),"{\"checks\":"+checks+",\"resolutions\":4,\"guiScales\":4,\"inGame\":false}");
  System.out.println("PASS Gekkoukan/HUD: "+checks+" checks; migration, school footprint, native trees, creative materials, 16 HUD layouts");
 }
 static void render(Path file,Path assets)throws Exception{
  var im=new BufferedImage(1920,1080,BufferedImage.TYPE_INT_RGB);var g=im.createGraphics();var scene=Path.of("build/reports/gekkoukan/preview/frames/gekkoukan-fachada.png");
  if(Files.exists(scene))g.drawImage(ImageIO.read(scene.toFile()),0,0,1920,1080,null);else {g.setColor(new Color(0xff203139));g.fillRect(0,0,1920,1080);}
  g.scale(2,2);var c=new Canvas(g,assets);var l=ExplorationHudLayout.of(960,540);
  var v=new HudDesign.View(false,false,false,"",86,120,43,65,"",0,0,86./120,43./65,0,0,"","",List.of(),List.of(),"",0,false,0,"");
  ExplorationHudDesign.vitals(c,l,v,"XmenBrasil");ExplorationHudDesign.clock(c,530000);ExplorationHudDesign.shuffle(c,l,1,"P",false);
  var map=ImageIO.read(assets.resolve("textures/gui/city/map.png").toFile());g.drawImage(map,(int)l.mapX(),10,(int)l.mapX()+97,107,215,135,324,237,null);c.poly(new double[]{l.mapX()+48,55,l.mapX()+44,64,l.mapX()+52,64},0xffffff96);c.text("M  CITY / TRAVEL",l.mapX(),112,7,0xff8bffff,0);
  c.fill(389,517,182,22,0xbb1c2528);for(int i=0;i<9;i++){c.fill(391+i*20,519,18,18,0xff8b9086);c.fill(393+i*20,521,14,14,0xff353d3c);}c.fill(474,263,12,1,0xfffaf9e3);c.fill(479,258,1,12,0xfffaf9e3);
  c.fill(0,0,960,8,0xe6091831);c.text("HUD DE PRODUÇÃO · CENÁRIO E ILUMINAÇÃO OFFLINE · HOTBAR ILUSTRATIVA",10,0,6,0xffbde9ef,0);g.dispose();ImageIO.write(im,"png",file.toFile());
 }
 static final class Canvas implements MenuDesign.Canvas {
  final Graphics2D g;final Font body,display;Canvas(Graphics2D g,Path a)throws Exception{this.g=g;body=Font.createFont(Font.TRUETYPE_FONT,a.resolve("font/body.ttf").toFile());display=Font.createFont(Font.TRUETYPE_FONT,a.resolve("font/display.ttf").toFile());g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);}
  public void fill(double x,double y,double w,double h,int color){g.setColor(new Color(color,true));g.fill(new Rectangle2D.Double(x,y,w,h));}
  public void poly(double[] p,int color){var path=new Path2D.Double();path.moveTo(p[0],p[1]);for(int i=2;i<p.length;i+=2)path.lineTo(p[i],p[i+1]);path.closePath();g.setColor(new Color(color,true));g.fill(path);}
  public void text(String t,double x,double y,double size,int color,double a){g.setFont((size>=20?display:body).deriveFont((float)size));g.setColor(new Color(color,true));g.drawString(t,(float)x,(float)y+g.getFontMetrics().getAscent());}
  public double width(String t,double size){g.setFont(body.deriveFont((float)size));return g.getFontMetrics().stringWidth(t);}
  public void item(int i,double x,double y){}
 }
}
