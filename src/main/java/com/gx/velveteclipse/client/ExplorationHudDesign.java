package com.gx.velveteclipse.client;

import com.gx.velveteclipse.client.menu.MenuDesign;

/** Production painter also used by the offline screenshots and layout checks. */
public final class ExplorationHudDesign {
    private static final int INK=0xe6091831,CYAN=0xff75efff,WHITE=0xffecf9ff,MUTED=0xffa3bdca;
    public static void vitals(MenuDesign.Canvas c,ExplorationHudLayout l,HudDesign.View v,String name){
        double x=10,y=l.vitalsY();c.fill(x,y,178,43,INK);c.fill(x,y,2,43,CYAN);
        while(name.length()>1&&c.width(name,10)>158)name=name.substring(0,name.length()-2)+"…";
        c.text(name,x+8,y+4,10,WHITE,0);
        resource(c,"HP",x+8,y+18,v.hp(),v.maxHp(),v.hpRatio(),0xff89efbe);
        resource(c,"SP",x+8,y+30,v.sp(),v.maxSp(),v.spRatio(),CYAN);
    }
    private static void resource(MenuDesign.Canvas c,String title,double x,double y,int n,int max,double ratio,int color){
        c.text(title,x,y,7,MUTED,0);c.fill(x+16,y+2,92,3,0xff294355);c.fill(x+16,y+2,92*Math.max(0,Math.min(1,ratio)),3,color);
        c.text(n+" / "+max,x+112,y,6.5,WHITE,0);
    }
    public static void clock(MenuDesign.Canvas c,int ms){
        int sec=(int)((ms+999L)/1000);c.fill(10,10,119,41,0xe506251f);c.fill(10,10,2,41,0xff8aef75);
        c.text("DARK HOUR",18,15,9,0xffa8f38d,0);c.text(String.format("%02d:%02d",sec/60,sec%60),18,27,20,ms<60000?0xffff6c7d:0xffeffbd7,0);
    }
    public static void shuffle(MenuDesign.Canvas c,ExplorationHudLayout l,int count,String key,boolean fresh){
        if(count<=0)return;double x=l.shuffleX(),y=l.bottom()-22.5;c.fill(x,y,135,22.5,INK);c.fill(x,y,2,22.5,fresh?0xffeeffbd:CYAN);
        c.text("["+key+"]  SHUFFLE TIME  × "+count,x+7,y+7,8, fresh?0xffeeffbd:CYAN,0);
    }
    private ExplorationHudDesign(){}
}
