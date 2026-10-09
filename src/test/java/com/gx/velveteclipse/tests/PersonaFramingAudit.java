package com.gx.velveteclipse.tests;

import com.gx.velveteclipse.client.menu.*;
import com.gx.velveteclipse.menu.MenuSnapshot.Persona;
import com.gx.velveteclipse.core.persona.*;
import com.google.gson.*;
import java.nio.file.*;
import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

/** Check the Reload-scale viewport; lower-body attribute overlay is intentional in the original layout. */
public final class PersonaFramingAudit {
    static Font display,body;
    static class Canvas implements MenuDesign.Canvas {
        final Graphics2D g;Canvas(Graphics2D g){this.g=g;g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);}
        Font font(double size){return(size>=20?display:body).deriveFont((float)size);}
        public void fill(double x,double y,double w,double h,int color){g.setColor(new Color(color,true));g.fill(new Rectangle2D.Double(x,y,w,h));}
        public void poly(double[] p,int color){var path=new Path2D.Double();path.moveTo(p[0],p[1]);for(int i=2;i<p.length;i+=2)path.lineTo(p[i],p[i+1]);path.closePath();g.setColor(new Color(color,true));g.fill(path);}
        public void text(String text,double x,double y,double size,int color,double angle){var old=g.getTransform();g.translate(x,y);g.rotate(Math.toRadians(angle));g.setFont(font(size));g.setColor(new Color(color,true));g.drawString(text,0,g.getFontMetrics().getAscent()*.8f);g.setTransform(old);}
        public double width(String text,double size){return font(size).getStringBounds(text,g.getFontRenderContext()).getWidth();}
        public void item(int slot,double x,double y){}
    }
    public static void main(String[] args)throws Exception {
        var root=Path.of("src/main/resources/assets/velvet_eclipse");var out=Path.of("build/reports/city/menu");Files.createDirectories(out);
        display=Font.createFont(Font.TRUETYPE_FONT,root.resolve("font/display.ttf").toFile());body=Font.createFont(Font.TRUETYPE_FONT,root.resolve("font/body.ttf").toFile());
        var profiles=JsonParser.parseString(Files.readString(root.resolve("persona-preview-bounds.json"))).getAsJsonObject();int checks=0;
        for(var entry:profiles.entrySet())for(float yaw=0;yaw<360;yaw+=2.5f){
            var a=ApprovedPersonaFraming.projectedIdle(entry.getKey(),yaw);var f=ApprovedPersonaFraming.sheet(entry.getKey(),yaw,0);
            for(int i=0;i<4;i++){
                double x=f.x()+a[(i&1)==0?0:2]*f.scale(),y=f.y()+a[(i&2)==0?1:3]*f.scale();checks++;
                if(x<395.99||x>891.01||y<7.99||y>452.01)throw new AssertionError("Model crosses viewport: "+entry.getKey());
            }
        }
        PersonaRegistry.bootstrap();var frames=new LinkedHashMap<String,Object>();
        for(String id:java.util.List.of("apsaras","jack_frost","forneus","chimera")){
            var d=PersonaRegistry.persona(id).orElseThrow();var b=d.baseStats();var skills=d.learnset().entrySet().stream().filter(e->e.getKey()<=d.baseLevel()).flatMap(e->e.getValue().stream()).distinct().map(s->PersonaRegistry.skill(s).orElseThrow()).toList();
            var p=new Persona(id,d.displayName(),d.arcana().name(),d.baseLevel(),0,PersonaInstance.experienceForNextLevel(d.baseLevel()),new int[]{b.strength(),b.magic(),b.endurance(),b.agility(),b.luck()},skills,java.util.List.of());
            var view=new PersonaSheetDesign.View(p,false,0,4,0,-1,1,0,false,"","");
            for(boolean front:new boolean[]{false,true}){
                var im=new BufferedImage(1260,700,BufferedImage.TYPE_INT_ARGB);var g=im.createGraphics();g.scale(1.4,1.4);var canvas=new Canvas(g);if(front)PersonaSheetDesign.foreground(canvas,view);else PersonaSheetDesign.background(canvas,view);g.dispose();ImageIO.write(im,"PNG",out.resolve(id+(front?"-foreground.png":"-background.png")).toFile());
            }
            float yaw=180+ApprovedPersonaFraming.facing(id);var fit=ApprovedPersonaFraming.sheet(id,yaw,0);
            var extent=ApprovedPersonaFraming.projectedIdle(id,yaw);double width=(extent[2]-extent[0])*fit.scale(),height=(extent[3]-extent[1])*fit.scale();
            double expected=switch(id){case "apsaras"->412;case "chimera"->418;case "jack_frost"->264;default->466;};
            if(Math.abs((id.equals("forneus")?width:height)-expected)>1)throw new AssertionError("Reference occupancy lost: "+id+" / "+width+" x "+height);
            frames.put(id,Map.of("x",fit.x(),"y",fit.y(),"scale",fit.scale(),"matrix",ApprovedPersonaFraming.projection(id,yaw).get(new float[16]),"rendererScale",PersonaFramingExport.SCALES.get(id)));
        }
        Files.writeString(out.resolve("transforms.json"),new Gson().toJson(frames));
        System.out.println("PASS menu framing: "+profiles.size()+" Personas, "+checks+" rotated viewport limits and four Reload reference occupancies; offline layout layers exported.");
    }
}
