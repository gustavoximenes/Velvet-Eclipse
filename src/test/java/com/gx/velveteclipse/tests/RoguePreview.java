package com.gx.velveteclipse.tests;
import com.gx.velveteclipse.roguelike.*;
import com.gx.velveteclipse.client.menu.*;
import com.gx.velveteclipse.localization.DisplayText;
import java.nio.file.*;
import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import javax.imageio.ImageIO;
import com.google.gson.*;

/** Exports the actual production drawing, not an unrelated mockup. */
public final class RoguePreview {
 public static final class Canvas implements MenuDesign.Canvas {
  final BufferedImage image;final Graphics2D g;final Font display,body;
  Canvas(int w,int h,double scale)throws Exception{image=new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);g=image.createGraphics();g.scale(scale,scale);g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);var fonts=Path.of("src/main/resources/assets/velvet_eclipse/font");display=Font.createFont(Font.TRUETYPE_FONT,fonts.resolve("display.ttf").toFile());body=Font.createFont(Font.TRUETYPE_FONT,fonts.resolve("body.ttf").toFile());}
  public void fill(double x,double y,double w,double h,int color){g.setColor(new Color(color,true));g.fill(new Rectangle2D.Double(x,y,w,h));}
  public void poly(double[] p,int color){var shape=new Path2D.Double();shape.moveTo(p[0],p[1]);for(int i=2;i<p.length;i+=2)shape.lineTo(p[i],p[i+1]);shape.closePath();g.setColor(new Color(color,true));g.fill(shape);}
  private void text(String s,double x,double y,double size,int color,double angle,Font font){var old=g.getTransform();g.translate(x,y);g.rotate(Math.toRadians(angle));g.setFont(font.deriveFont((float)size));g.setColor(new Color(color,true));g.drawString(DisplayText.text(s),0,g.getFontMetrics().getAscent());g.setTransform(old);}
  public void text(String s,double x,double y,double size,int color,double angle){text(s,x,y,size,color,angle,size>=20?display:body);}
  public void body(String s,double x,double y,double size,int color,double angle){text(s,x,y,size,color,angle,body);}
  public double width(String s,double size){return g.getFontMetrics((size>=20?display:body).deriveFont((float)size)).stringWidth(DisplayText.text(s));}
  public double bodyWidth(String s,double size){return g.getFontMetrics(body.deriveFont((float)size)).stringWidth(DisplayText.text(s));}
  public void item(int slot,double x,double y){}
  void save(Path file)throws Exception{Files.createDirectories(file.getParent());g.dispose();ImageIO.write(image,"png",file.toFile());}
 }
 public static void export(Path out)throws Exception{
  var json=new GsonBuilder().setPrettyPrinting().create();var all=RogueCatalog.CARDS.stream().map(RogueCatalog.Card::id).toList();
  for(String lang:java.util.List.of("en_us","pt_br")){
   DisplayText.language(lang);
   for(int page=0;page<3;page++)for(boolean enabled:new boolean[]{false,true}){
    var state=new RogueNetwork.State(false,enabled,true,true,"weakness",all,java.util.List.of("magician","priestess","star"),"","","");
    for(int selected=0;selected<(page==0?9:1);selected++){var v=new RogueDesign.View(state,page,selected, page==0?selected+4:page,false);var c=new Canvas(1800,1000,2);RogueDesign.paint(c,v);c.save(out.resolve(lang+"-"+page+"-"+selected+"-"+enabled+".png"));}
   }
   for(var card:RogueCatalog.CARDS){var s=new RogueNetwork.State(true,true,true,true,"weakness",all,java.util.List.of(),"",card.id(),"preview-token");var c=new Canvas(1800,1000,2);RogueDesign.paint(c,new RogueDesign.View(s,0,0,0,false));c.save(out.resolve(lang+"-confirm-"+card.id()+".png"));}
  }
  DisplayText.language("en_us");
  // Audits export candidates only; tests must not overwrite shipped item assets.
  var resources=out.resolve("items");Files.createDirectories(resources.resolve("models/item"));
  for(var card:RogueCatalog.CARDS){
   var c=new Canvas(64,64,1);RogueDesign.tarot(c,card,15,4,34,56,true);c.save(resources.resolve("textures/item/arcana_buff_"+card.id()+".png"));
   var model=resources.resolve("models/item/arcana_buff_"+card.id()+".json");Files.writeString(model,json.toJson(Map.of("parent","minecraft:item/generated","textures",Map.of("layer0","velvet_eclipse:item/arcana_buff_"+card.id()))));
  }
  Files.writeString(out.resolve("catalog.json"),json.toJson(RogueCatalog.CARDS));
  var menu=new MenuDesign.View(true,11,0,0,java.util.List.of(),"",java.util.List.of(),null,"","","Player",47407,14,5000,1,false,11,10,10);
  var c=new Canvas(1800,1000,2);ReloadMenuDesign.background(c,menu,false);ReloadMenuDesign.foreground(c,menu,com.gx.velveteclipse.menu.MenuSnapshot.empty(),0,false,0);c.save(out.resolve("tab-arcana.png"));
 }
}
