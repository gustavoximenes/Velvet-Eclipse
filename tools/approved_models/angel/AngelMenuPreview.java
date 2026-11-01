package com.gx.velveteclipse.tests;

import com.gx.velveteclipse.client.menu.*;
import com.gx.velveteclipse.core.persona.*;
import com.gx.velveteclipse.menu.MenuSnapshot.Persona;
import com.gx.velveteclipse.localization.DisplayText;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import javax.imageio.ImageIO;

/** Export the production layout and catalogue, without replacing any mod resource. */
public final class AngelMenuPreview {
 public static void main(String[] args)throws Exception{
  Path assets=Path.of(args[0]),out=Path.of(args[1]);Files.createDirectories(out);
  PersonaFramingAudit.display=Font.createFont(Font.TRUETYPE_FONT,assets.resolve("font/display.ttf").toFile());
  PersonaFramingAudit.body=Font.createFont(Font.TRUETYPE_FONT,assets.resolve("font/body.ttf").toFile());
  DisplayText.language("en_us");PersonaRegistry.bootstrap();
  var d=PersonaRegistry.persona("angel").orElseThrow();var b=d.baseStats();
  var skills=d.learnset().entrySet().stream().filter(e->e.getKey()<=d.baseLevel()).flatMap(e->e.getValue().stream()).distinct().map(s->PersonaRegistry.skill(s).orElseThrow()).toList();
  var affinities=d.affinities().entrySet().stream().map(e->e.getKey().name()+" "+e.getValue().name()).toList();
  var p=new Persona(d.id(),d.displayName(),d.arcana().name(),d.baseLevel(),0,PersonaInstance.experienceForNextLevel(d.baseLevel()),new int[]{b.strength(),b.magic(),b.endurance(),b.agility(),b.luck()},skills,affinities);
  var v=new PersonaSheetDesign.View(p,false,0,1,0,-1,1,0,false,"","");
  for(boolean foreground:new boolean[]{false,true}){
   var im=new BufferedImage(1800,1000,BufferedImage.TYPE_INT_ARGB);var g=im.createGraphics();g.scale(2,2);
   var c=new PersonaFramingAudit.Canvas(g){
    @Override public void text(String s,double x,double y,double size,int color,double angle){super.text(DisplayText.text(s),x,y,size,color,angle);}
    @Override public double width(String s,double size){return super.width(DisplayText.text(s),size);}
   };
   if(foreground)PersonaSheetDesign.foreground(c,v);else PersonaSheetDesign.background(c,v);
   g.dispose();ImageIO.write(im,"PNG",out.resolve(foreground?"menu-foreground.png":"menu-background.png").toFile());
  }
  Files.writeString(out.resolve("menu-catalogue.json"),new com.google.gson.Gson().toJson(p));
  System.out.println("PASS: Angel production menu layers exported; original catalogue; no production writes.");
 }
}
