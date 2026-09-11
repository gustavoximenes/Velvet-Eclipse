package com.gx.velveteclipse.client;

/** A fast eye strip with diagonal framing, leaving the world above/below visible. */
public final class WeaknessCutinDesign {
    public interface Canvas extends HudDesign.Canvas {void eyes(int x,int y,int width,int height,float alpha);
        default void fragment(double[] xy,double[] uv,float alpha){}}
    public record Layout(int x,int y,int width,int height){}
    public static Layout layout(int w,int h,double t){return new Layout((int)(-w*(1-EvokerPose.smooth(t/3))),h*31/100,w,Math.max(30,h*34/100));}
    public static void paint(Canvas c,int w,int h,double t){
        double a=WeaknessCutinState.alpha(t);if(a<=0)return;
        var p=layout(w,h,t);int x=p.x(),y=p.y(),bh=p.height();
        c.fill(x,y,w,bh,color(0x06103B,a));
        c.eyes(x+w/20,y+2,w*9/10,bh-4,(float)a);
        // Dark blue corners and sharp cyan rails frame the enlarged eyes.
        for(int row=0;row<bh;row++){
            int edge=(int)((bh-row)*.34);
            c.fill(x,y+row,edge,1,color(0x06103B,a));
            c.fill(x+w-(int)(row*.20),y+row,(int)(row*.20),1,color(0x06103B,a));
        }
        c.fill(x,y,w,2,color(0x6EF6FF,a));c.fill(x,y+bh-2,w,2,color(0x285BFF,a));
        for(int i=0;i<7;i++){
            int sy=y+3+i*Math.max(3,bh/8),length=w/12+i*2;
            c.fill(x,sy,length,1,color(0x76F6FF,a*.65));c.fill(x+w-length,sy+4,length,1,color(0x76F6FF,a*.65));
        }
        // Double exclamation, white outline and dark interior like the reference.
        for(int i=0;i<2;i++){
            int bx=x+w*80/100+i*Math.max(13,w/22),top=y-bh/3,eh=bh*6/5,ew=Math.max(7,w/32);
            slant(c,bx,top,ew,eh,color(0xF4FEFF,a));slant(c,bx+2,top+3,Math.max(2,ew-4),eh-6,color(0x061B36,a));
            slant(c,bx-eh/4-2,top+eh+5,ew,Math.max(5,bh/6),color(0xF4FEFF,a));
        }
        for(int i=0;i<12;i++){
            int sx=x+(i*83+27)%Math.max(1,w),sy=y-(int)((i%4+1)*3+t*(.25+i%3*.1));
            c.fill(sx,sy,2,2,color(0xB5FCFF,a*.8));
        }
    }
    public static boolean summonVisible(double t){return t>=12&&t<51;}
    public static double shatter(double t){return EvokerPose.smooth((t-38)/13);}
    public static void paintSummon(Canvas c,int w,int h,double t){
        if(!summonVisible(t))return;
        double entry=EvokerPose.smooth((t-12)/4),breakup=shatter(t),alpha=entry*(1-breakup);
        // Separate jagged eye panels frame a clear central window for the live player/Evoker.
        // The very same vertices and UVs split and fly outward on tick 38 (trigger).
        double[][] pieces={
            {0,0,.22,0,.18,.48,0,.68},{.22,0,.43,0,.38,.52,.18,.48},
            {0,.68,.18,.48,.25,1,0,1},{.18,.48,.38,.52,.42,1,.25,1},
            {.64,0,.81,0,.86,.52,.63,.48},{.81,0,1,0,1,.62,.86,.52},
            {.63,.48,.86,.52,.81,1,.60,1},{.86,.52,1,.62,1,1,.81,1}};
        for(int n=0;n<pieces.length;n++){
            double[] uv=pieces[n],xy=new double[uv.length];double center=0,cy=0;
            for(int i=0;i<uv.length;i+=2){center+=uv[i]/4;cy+=uv[i+1]/4;}
            double shift=(n<4?-1:1)*w*.40*breakup,dy=(cy-.5)*h*.9*breakup;
            double angle=(n%2==0?1:-1)*breakup*.62,cos=Math.cos(angle),sin=Math.sin(angle);
            for(int i=0;i<uv.length;i+=2){double px=(uv[i]-center)*w,py=(uv[i+1]-cy)*h*.29;
                xy[i]=center*w+px*cos-py*sin+shift-w*(1-entry);
                xy[i+1]=h*.26+cy*h*.29+px*sin+py*cos+dy;}
            BattleGraphics.poly(c,xy,color(0x142FFF,alpha));c.fragment(xy,uv,(float)alpha);
        }
        if(t<38){
            int x=(int)(w*.82-w*(1-entry)),top=(int)(h*.18),eh=(int)(h*.23);
            slant(c,x,top,Math.max(8,w/36),eh,color(0xE9FFFF,alpha));
            slant(c,x+2,top+3,Math.max(3,w/36-4),eh-6,color(0x071326,alpha));
            slant(c,x-eh/4,top+eh+5,Math.max(7,w/36),Math.max(7,h/30),color(0xE9FFFF,alpha));
        }else for(int i=0;i<16;i++){
            double a=i*Math.PI/8,dist=(.12+breakup*.4)*h;
            double x=w*.52+Math.cos(a)*dist,y=h*.40+Math.sin(a)*dist;
            BattleGraphics.poly(c,new double[]{x,y,x+9*(1-breakup),y+3,x-3,y+14*(1-breakup)},color(0xCBFFFF,alpha));
        }
    }
    private static void slant(Canvas c,int x,int y,int w,int h,int color){for(int row=0;row<h;row++)c.fill(x-row/4,y+row,w,1,color);}
    private static int color(int rgb,double a){return (Math.min(255,Math.max(0,(int)(255*a)))<<24)|rgb;}
}
