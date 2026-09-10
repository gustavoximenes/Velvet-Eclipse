package com.gx.velveteclipse.client.exploration;

import com.gx.velveteclipse.client.HudDesign.Canvas;
import static com.gx.velveteclipse.exploration.ExplorationRules.*;

/** Entry wipe composited with the actual local player model and skin. */
public final class EntryTransition {
    private static int fade;
    private static double finishedTime;
    public static void release(){finishedTime=ExplorationClient.ticks();fade=9;}
    public static void tick(){if(fade>0)fade--;}
    public static void reset(){fade=0;finishedTime=0;EntryPlayerRenderer.reset();}
    public static boolean visible(){return ExplorationClient.entry()||fade>0;}
    public static void paint(Canvas c,int w,int h,float partial){
        double t=ExplorationClient.entry()?ExplorationClient.ticks()+partial:finishedTime+9-fade+partial;
        double alpha=ExplorationClient.entry()?smooth((t-5)/8):Math.max(0,(fade-partial)/9.0);
        if(alpha<=0)return;
        int black=((int)(alpha*255)<<24)|0x000416;
        int sweep=(int)(w*smooth((t-3)/9));
        c.fill(0,0,ExplorationClient.entry()?sweep:w,h,black);
        double unit=h/230.0;
        c.playerModel(w,h,t,(float)alpha);
        for(int i=0;i<6;i++)c.fill((int)(w*.7+i*13*unit),(int)(h*.35+i*12*unit),(int)(45*unit),Math.max(1,(int)unit),((int)(90*alpha)<<24)|0x70EAFF);
        c.styled("BATTLE",(int)(w*.63),(int)(h*.77),(float)(2.8*unit),((int)(255*alpha)<<24)|0xF1FEFF);
    }
}
