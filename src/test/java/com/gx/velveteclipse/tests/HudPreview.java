package com.gx.velveteclipse.tests;
import com.gx.velveteclipse.client.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import javax.imageio.ImageIO;

/** Offline paint of the actual HUD layout; no claim of a captured Minecraft frame. */
public final class HudPreview {
    public static void main(String[] args)throws Exception{
        int w=480,h=270,scale=4;var image=new BufferedImage(w*scale,h*scale,BufferedImage.TYPE_INT_ARGB);var g=image.createGraphics();g.scale(scale,scale);
        g.setPaint(new GradientPaint(0,0,new Color(0x111322),w,h,new Color(0x222957)));g.fillRect(0,0,w,h);
        g.setColor(new Color(0x313859));for(int i=0;i<10;i++)g.drawLine(0,h/2+i*12,w,h/2+i*12);
        Font body=Font.createFont(Font.TRUETYPE_FONT,new File("src/main/resources/assets/velvet_eclipse/font/body.ttf")).deriveFont(10f);
        Font display=Font.createFont(Font.TRUETYPE_FONT,new File("src/main/resources/assets/velvet_eclipse/font/display.ttf"));
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        HudDesign.Canvas canvas=new HudDesign.Canvas(){
            public void fill(int x,int y,int w,int h,int c){g.setColor(new Color(c,true));g.fillRect(x,y,w,h);}
            public void text(String s,int x,int y,int c){g.setFont(body);g.setColor(new Color(c,true));g.drawString(s,x,y+8);}
            public int width(String s){g.setFont(body);return g.getFontMetrics().stringWidth(s);}
            public void styled(String s,int x,int y,float size,int c){g.setFont(display.deriveFont(11*size));g.setColor(new Color(c,true));g.drawString(s,x,y+9*size);}
            public void portrait(int x,int y,int size){g.setColor(new Color(0x3154AA));g.fillRect(x,y,size,size);text("SKIN",x+3,y+size/2,0xFF9FEAFF);}
        };
        var v=new HudDesign.View(true,true,false,"Thanatos",103,120,42,45,"Umbra Slime",64,85,103/120.,42/45.,64/85.,.86,"A Shadow blocks your path.","MAIN",List.of(),List.of("1","2","3","4","5","6"),"BACK",0,false,0,"");
        HudDesign.paint(canvas,v,w,h,-1,-1);
        canvas.text("PREVIA OFFLINE / retrato ilustrativo; no jogo usa a skin real",8,61,0xFF8DA6CD);
        g.dispose();ImageIO.write(image,"PNG",new File(args[0]));
    }
}
