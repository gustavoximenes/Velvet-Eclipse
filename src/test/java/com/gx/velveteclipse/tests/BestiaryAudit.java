package com.gx.velveteclipse.tests;
import com.gx.velveteclipse.core.battle.*;
import com.gx.velveteclipse.core.persona.*;
import com.gx.velveteclipse.forge.*;
import com.gx.velveteclipse.menu.*;
import com.gx.velveteclipse.client.menu.*;
import net.minecraft.nbt.CompoundTag;
import java.util.*;
import java.nio.file.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
public final class BestiaryAudit {
 private static int checks;private static void check(boolean b,String message){checks++;if(!b)throw new AssertionError(message);}
 public static final class Player {private final CompoundTag data=new CompoundTag();public CompoundTag getPersistentData(){return data;}}
 public static void main(String[] args)throws Exception{
  var player=new Player();check(BestiaryService.entries(player).isEmpty(),"new player cannot see unrevealed catalog");
  BestiaryService.seen(player,"heat_balance",10);BestiaryService.seen(player,"heat_balance",18);var seen=BestiaryService.entries(player).get(0);
  check(seen.seen()==2&&seen.defeated()==0&&seen.level()==18&&seen.affinities().isEmpty(),"seen and defeated are independent; no implicit affinities");
  BattleKnowledge.learn(player,"heat_balance",new DamageOutcome(Element.FIRE,Affinity.RESIST,10,false,false,false,false,false));
  BattleKnowledge.learn(player,"heat_balance",new DamageOutcome(Element.ICE,Affinity.RESIST,0,false,false,false,false,true));
  check(BestiaryDesign.known(BestiaryService.entries(player).get(0).affinities()).equals(Map.of(Element.FIRE,Affinity.RESIST)),"only successful tested element appears");
  BestiaryService.defeated(player,"heat_balance",18);var clone=new Player();BattleKnowledge.copy(player,clone);var entry=BestiaryService.entries(clone).get(0);
  check(entry.seen()==2&&entry.defeated()==1&&entry.affinities().equals("FIRE:RESIST"),"death clone preserves counters and discoveries");
  var reloaded=new Player();reloaded.data.m_128391_(player.data);check(BestiaryService.entries(reloaded).equals(BestiaryService.entries(player)),"world save data preserves bestiary");
  var separate=new Player();check(BestiaryService.entries(separate).isEmpty(),"knowledge remains per player");
  BattleKnowledge.learn(separate,"cowardly_maya",new DamageOutcome(Element.FIRE,Affinity.WEAK,10,false,false,false,false,false));check(BestiaryService.entries(separate).get(0).defeated()==0,"legacy observations do not invent victories");
  BestiaryService.seen(player,"raid_2",30);BattleKnowledge.learn(player,"raid_2",new DamageOutcome(Element.ICE,Affinity.NORMAL,10,false,false,false,false,false));check(BestiaryService.entries(player).stream().anyMatch(e->e.id().equals("raid_2")&&e.changing()&&e.affinities().equals("ICE:NORMAL")),"raid boss records observed rather than secret changing weaknesses");
  check(BestiaryProgress.decode("broken;foo:0:7:2;heat_balance:1:0:999").entries().size()==1,"damaged counters ignored; high-level guardians retained");
  var snapshot=new MenuSnapshot(100,100,20,20,1,"",java.util.List.of(),java.util.List.of(),java.util.List.of(),1,0,"",1,0,58,500,BestiaryService.entries(player));var gson=new com.google.gson.Gson();check(gson.fromJson(gson.toJson(snapshot),MenuSnapshot.class).bestiary().equals(snapshot.bestiary()),"menu network JSON round-trip");
  check(MenuDesign.TABS.length==10&&MenuDesign.TABS[9].equals("BESTIÁRIO"),"bestiary reachable as tenth tab");
  for(boolean home:java.util.List.of(false,true)){var r=MenuDesign.tab(9,home);check(MenuDesign.pickTab(r.x()+35,r.y()+8,home)==9,"bestiary pointer target reachable");}
  var image=new BufferedImage(900,500,BufferedImage.TYPE_INT_ARGB);var c=new Canvas(image);
  var rows=java.util.List.of(new MenuDesign.Row("Heat Balance","DERROTADO / 1 vez",-1),new MenuDesign.Row("Sacerdotisa","VISTO / Ainda não derrotado",-1));
  var view=new MenuDesign.View(false,9,0,0,rows,"Inimigos encontrados",java.util.List.of(),null,"","","Jogador",500,1,0,3,false,9,3,3);
  MenuDesign.background(c,view);MenuDesign.foreground(c,view);BestiaryDesign.paint(c,entry);
  c.g.drawImage(ImageIO.read(Path.of("src/main/resources/assets/velvet_eclipse/textures/item/enemy_heat_balance_spawn.png").toFile()),650,199,106,106,null);
  check(c.texts.contains("?")&&c.texts.contains("RESISTE")&&!c.texts.contains("FRACO"),"UI does not expose hidden weakness");
  var out=Path.of("build/reports/bestiary-preview.png");Files.createDirectories(out.getParent());ImageIO.write(image,"png",out.toFile());c.g.dispose();
  System.out.println("PASS bestiary: "+checks+" checks; persistent seen/defeated counters, private discoveries, death/reload, raid observations, menu transport and layout. Offline only.");
 }
 private static final class Canvas implements MenuDesign.Canvas {
  final Graphics2D g;final Font body,display;final Set<String> texts=new HashSet<>();
  Canvas(BufferedImage image)throws Exception{g=image.createGraphics();g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);body=Font.createFont(Font.TRUETYPE_FONT,Path.of("src/main/resources/assets/velvet_eclipse/font/body.ttf").toFile());display=Font.createFont(Font.TRUETYPE_FONT,Path.of("src/main/resources/assets/velvet_eclipse/font/display.ttf").toFile());}
  public void fill(double x,double y,double w,double h,int color){g.setColor(new Color(color,true));g.fill(new Rectangle2D.Double(x,y,w,h));}
  public void poly(double[] xy,int color){var path=new Path2D.Double();path.moveTo(xy[0],xy[1]);for(int i=2;i<xy.length;i+=2)path.lineTo(xy[i],xy[i+1]);path.closePath();g.setColor(new Color(color,true));g.fill(path);}
  private Font font(double size){return (size>=20?display:body).deriveFont((float)size);}
  public void text(String text,double x,double y,double size,int color,double angle){texts.add(text);var old=g.getTransform();g.translate(x,y);g.rotate(Math.toRadians(angle));g.setFont(font(size));g.setColor(new Color(color,true));g.drawString(text,0,g.getFontMetrics().getAscent());g.setTransform(old);}
  public double width(String text,double size){return g.getFontMetrics(font(size)).stringWidth(text);}
  public void item(int slot,double x,double y){}
 }
}
