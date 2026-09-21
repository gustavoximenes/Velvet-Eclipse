package com.gx.velveteclipse.client.menu;
import com.gx.velveteclipse.core.persona.Element;
import com.gx.velveteclipse.client.menu.MenuDesign.Canvas;

/** Original geometric icons, drawn as convex polygons by both the game and the audit renderer. */
public final class PersonaGlyphs {
    static void shape(Canvas c,double x,double y,double r,int color,double... xy){
        double[] points=xy.clone();for(int i=0;i<points.length;i+=2){points[i]=x+points[i]*r;points[i+1]=y+points[i+1]*r;}c.poly(points,color);
    }
    public static void disc(Canvas c,double x,double y,double r,int color){
        double[] points=new double[40];for(int i=0;i<20;i++){double a=i*Math.PI/10;points[i*2]=x+Math.cos(a)*r;points[i*2+1]=y+Math.sin(a)*r;}c.poly(points,color);
    }
    public static void draw(Canvas c,Element e,double x,double y,double r,int color){
        switch(e){
            case PHYSICAL -> {shape(c,x,y,r,color,-.8,.65,.3,-.75,.95,-.9,.5,-.15);shape(c,x,y,r,color,-.8,-.1,-.6,-.35,.2,.5,0,.72);}
            case PIERCE -> {shape(c,x,y,r,color,-.2,.9,-.2,-.3,-.65,-.3,0,-1,.65,-.3,.2,-.3,.2,.9);}
            case SLASH -> {shape(c,x,y,r,color,-.95,.47,.82,-.8,.25,.10,.54,.16);shape(c,x,y,r,color,-.78,.70,.48,.20,.25,.60,-.48,.83);}
            case FIRE -> {shape(c,x,y,r,color,-.7,.45,-.2,-.2,-.1,-.95,.8,.35);shape(c,x,y,r,color,-.7,.45,.8,.35,.2,.85,-.35,.8);shape(c,x,y,r,0xFF080D43,-.18,.65,.2,-.03,.4,.55);}
            case ICE -> {shape(c,x,y,r,color,-.7,-.35,0,-.95,.7,.45,0,.9);shape(c,x,y,r,0xFF080D43,-.08,-.7,.2,.2,.4,.6,.03,.3);}
            case ELECTRIC -> {shape(c,x,y,r,color,.2,-.95,.85,-.95,-.25,.05,-.6,.02);shape(c,x,y,r,color,-.25,.05,.4,-.05,-.6,.95,-.2,.2);}
            case WIND -> {shape(c,x,y,r,color,-.95,-.65,.7,-.75,.1,-.12,-.9,-.12);shape(c,x,y,r,color,.95,.1,-.1,.1,-.8,.72,.8,.6);}
            case LIGHT -> {shape(c,x,y,r,color,0,-1,.25,-.2,1,0,.2,.15);shape(c,x,y,r,color,0,1,-.2,.2,-1,0,-.2,-.15);}
            case DARK -> {disc(c,x,y,r*.81,color);disc(c,x+r*.25,y-r*.25,r*.6,0xFF080D43);shape(c,x,y,r,color,-.4,.1,.5,.3,.75,.8,-.5,.7);}
            case PSY -> {disc(c,x,y,r*.7,color);disc(c,x,y,r*.38,0xFF080D43);shape(c,x,y,r,color,-.85,-.1,-.6,-.65,.85,.1,.6,.65);}
            case NUCLEAR -> {disc(c,x,y,r*.22,color);for(int i=0;i<3;i++){double a=i*Math.PI*2/3;double[] p={.25,-.25,.85,-.45,.8,.45,.25,.25};for(int n=0;n<p.length;n+=2){double px=p[n],py=p[n+1];p[n]=px*Math.cos(a)-py*Math.sin(a);p[n+1]=px*Math.sin(a)+py*Math.cos(a);}shape(c,x,y,r,color,p);}}
            case HEALING -> {shape(c,x,y,r,color,-.22,-.8,.22,-.8,.22,.8,-.22,.8);shape(c,x,y,r,color,-.8,-.22,.8,-.22,.8,.22,-.8,.22);}
            case SUPPORT -> {shape(c,x,y,r,color,-.8,.35,0,-.65,.8,.35,.35,.35);shape(c,x,y,r,color,-.3,.2,.3,.2,.3,.85,-.3,.85);}
            case AILMENT -> {disc(c,x,y,r*.8,color);disc(c,x-r*.25,y-r*.1,r*.2,0xFF080D43);disc(c,x+r*.25,y-r*.1,r*.2,0xFF080D43);shape(c,x,y,r,0xFF080D43,-.35,.4,.35,.4,.2,.55,-.2,.55);}
            default -> {shape(c,x,y,r,color,-1,-.6,0,-.15,1,-.6,.45,.2);shape(c,x,y,r,color,-1,.6,-.4,-.15,0,.15,1,.6);}
        }
    }
}
