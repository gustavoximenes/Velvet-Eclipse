package com.gx.velveteclipse.client;

import java.util.List;
import java.util.Locale;
import com.gx.velveteclipse.client.BattleMenuLayout.Rect;

/** The same painter can target Minecraft or an offline visual audit. */
public final class HudDesign {
    public static final int INK=0xF008142E, NAVY=0xED102D62, BLUE=0xFF1664D9,
        CYAN=0xFF66EEFF, WHITE=0xFFF3FCFF, MUTED=0xFF9FBDD8, RED=0xFFFF637F;
    public interface Canvas {
        void fill(int x, int y, int w, int h, int color);
        default void poly(double[] points,int color){BattleGraphics.scan(this,points,color);}
        void text(String text, int x, int y, int color);
        int width(String text);
        default void styled(String text,int x,int y,float scale,int color){text(text,x,y,color);}
        default void angled(String text,int x,int y,float scale,int color,float degrees){styled(text,x,y,scale,color);}
        default void personaPortrait(int x,int y,int w,int h,float alpha){}
        default void portrait(int x,int y,int size){}
        default String playerName(){return "PLAYER";}
        default void playerModel(int width,int height,double time,float alpha){}
    }
    public record Entry(String key, String name, String detail, boolean enabled) {}
    public record View(boolean active, boolean playerTurn, boolean locked, String persona,
        int hp, int maxHp, int sp, int maxSp, String enemy, int enemyHp, int enemyMaxHp,
        double hpRatio, double spRatio, double enemyRatio, double enemyTrail,
        String message, String page, List<Entry> entries, List<String> keys, String backKey,
        double time, boolean cinematic, double skillTime, String skillName) {}

    private HudDesign() {}
    public static void paint(Canvas c, View s, int w, int h, double mx, double my) {
        if (!s.active) { exploration(c,s,w,h); return; }
        if(!s.cinematic){BattleHudDesign.paint(c,s,w,h,mx,my);return;}

        // During a Persona attack only the enemy has a visible health panel.
        if (s.cinematic) { cinematic(c,s,w,h); return; }
        if (s.page.equals("MAIN")) {
            commands(c,s,w,h,mx,my);
            message(c,s,w,h);
        } else submenu(c,s,w,h,mx,my);
    }
    private static void exploration(Canvas c,View s,int w,int h) {
        Rect panel=explorationPanel(w,h);
        int width=panel.w(),x=panel.x(),y=panel.y();
        ribbon(c,x,y,width,30,INK,8);
        c.fill(x,y,3,30,CYAN);
        c.portrait(x+5,y+2,26);
        c.text(fit(c,c.playerName(),width-45),x+37,y+4,WHITE);
        c.text("HP",x+37,y+17,MUTED); bar(c,x+54,y+20,58,3,s.hpRatio,0xFF89F0BD);
        c.text("SP",x+124,y+17,MUTED); bar(c,x+141,y+20,Math.max(8,width-150),3,s.spRatio,CYAN);
    }
    public static Rect explorationPanel(int w,int h){return new Rect(8,h-80,Math.min(226,w-16),30,"",-1);}
    /** A compact footer attached to the player card, above the vanilla hotbar. */
    public static void explorationHints(Canvas c,int w,int h,String toggle){
        Rect panel=explorationPanel(w,h);
        int x=panel.x(),y=panel.y()+33,width=panel.w(),half=width/2;
        ribbon(c,x,y,width,16,0xEF0B1B48,5);
        c.fill(x+5,y,width-10,1,0xB366EEFF);
        hint(c,x+6,y+3,half-10,toggle,"EXPLORAR");
        c.fill(x+half-1,y+4,1,8,0xFF305487);
        hint(c,x+half+5,y+3,width-half-12,"M1","ATACAR");
    }
    private static void hint(Canvas c,int x,int y,int width,String key,String action){
        key=fit(c,key,30);
        int keyWidth=Math.max(14,c.width(key)+6);
        ribbon(c,x,y,keyWidth,11,CYAN,3);
        c.text(key,x+(keyWidth-c.width(key))/2,y+1,0xFF071530);
        c.styled(fit(c,action,width-keyWidth-5),x+keyWidth+5,y+1,.9f,WHITE);
    }
    private static void enemy(Canvas c,View s,int x,int y,int width) {
        ribbon(c,x,y,width,40,INK,10);
        c.fill(x,y,3,40,RED);
        c.text("SHADOW",x+10,y+4,RED);
        c.text(fit(c,upper(s.enemy),width-22),x+10,y+15,WHITE);
        int bw=width-20;
        bar(c,x+10,y+29,bw,4,s.enemyTrail,0xFFEEE1B9);
        int fill=(int)Math.round(bw*clamp(s.enemyRatio));
        if(fill>0)c.fill(x+10,y+29,fill,4,RED);
        c.fill(x+10,y+35,(int)Math.round(bw*clamp(s.enemyHp/(double)Math.max(1,s.enemyMaxHp))),1,0x9966EEFF);
    }
    private static void player(Canvas c,View s,int x,int y,int width) {
        ribbon(c,x,y,width,51,INK,10);
        c.fill(x+9,y,width-19,2,CYAN);
        c.text(fit(c,upper(s.persona),width-20),x+10,y+7,WHITE);
        stat(c,"HP",s.hp,x+10,y+23,width-20,s.hpRatio,s.hpRatio<.25?RED:0xFF88EABD);
        stat(c,"SP",s.sp,x+10,y+37,width-20,s.spRatio,CYAN);
    }
    private static void stat(Canvas c,String label,int value,int x,int y,int width,double ratio,int color) {
        c.text(label,x,y-2,MUTED);
        String number=Integer.toString(value);
        c.text(number,x+width-c.width(number),y-2,WHITE);
        bar(c,x+19,y+2,Math.max(8,width-25-c.width(number)),4,ratio,color);
    }
    private static void commands(Canvas c,View s,int w,int h,double mx,double my) {
        Rect[] rects=BattleMenuLayout.main(w,h);
        String caption=s.locked?"ACTION IN PROGRESS":s.playerTurn?"CHOOSE YOUR ACTION":"SHADOW TURN";
        c.text(caption,rects[0].x()+3,rects[0].y()-12,s.playerTurn?CYAN:MUTED);
        for(int i=0;i<rects.length;i++) {
            Rect r=rects[i]; boolean hover=r.contains(mx,my)&&s.playerTurn&&!s.locked;
            int color=hover?CYAN:s.locked||!s.playerTurn?0xCC132139:(i==0?BLUE:NAVY);
            ribbon(c,r.x()+2,r.y()+2,r.w(),r.h(),0x66030818,6);
            ribbon(c,r.x(),r.y(),r.w(),r.h(),color,6);
            int ink=hover?INK:WHITE;
            icon(c,i,r.x()+10,r.y()+(r.h()-9)/2,ink);
            c.text(r.label(),r.x()+28,r.y()+(r.h()-8)/2,ink);
            String key=fit(c,s.keys.get(i),23);
            c.text(key,r.x()+r.w()-11-c.width(key),r.y()+(r.h()-8)/2,hover?INK:CYAN);
        }
    }
    static void submenu(Canvas c,View s,int w,int h,double mx,double my) {
        int rows=Math.min(7,s.entries.size());
        Rect p=BattleMenuLayout.submenuPanel(w,h,rows);
        ribbon(c,p.x(),p.y(),p.w(),p.h(),INK,8);
        c.fill(p.x()+8,p.y(),p.w()-16,2,CYAN);
        c.text(s.page.equals("SKILLS")?"PERSONA / SKILLS":"PERSONA / ROSTER",p.x()+10,p.y()+8,CYAN);
        for(int i=0;i<rows;i++) {
            Entry e=s.entries.get(i); Rect r=BattleMenuLayout.submenuRow(w,h,rows,i);
            boolean hover=r.contains(mx,my)&&e.enabled;
            ribbon(c,r.x(),r.y(),r.w(),r.h(),hover?CYAN:0xDE173768,4);
            int color=hover?INK:e.enabled?WHITE:0xFF7890AB;
            int y=r.y()+(r.h()-8)/2;
            c.text(fit(c,e.key,20),r.x()+5,y,hover?INK:CYAN);
            int dw=c.width(e.detail);
            c.text(fit(c,e.name,r.w()-38-dw),r.x()+29,y,color);
            c.text(e.detail,r.x()+r.w()-7-dw,y,color);
        }
        Rect back=BattleMenuLayout.back(w,h,rows);
        if(back.contains(mx,my))ribbon(c,back.x(),back.y(),back.w(),back.h(),BLUE,4);
        c.text(fit(c,s.backKey+"  BACK",back.w()-12),back.x()+7,back.y()+4,MUTED);
    }
    private static void message(Canvas c,View s,int w,int h) {
        if(s.message==null||s.message.isBlank())return;
        int width=Math.min(276,w-182),x=w-width-9;
        int y=h-(w>=600?92:47);
        boolean more=upper(s.message).contains("ONE MORE");
        ribbon(c,x,y,width,more?34:32,more?BLUE:INK,7);
        c.fill(x+7,y+5,2,22,more?WHITE:CYAN);
        String remaining=s.message.replace('\n',' ');
        for(int i=0;i<2&&!remaining.isEmpty();i++) {
            String line=fit(c,remaining,width-24);
            if(line.endsWith("...")) {
                int count=Math.max(1,line.length()-3);
                int space=remaining.lastIndexOf(' ',count);
                if(space>count/2)count=space;
                line=remaining.substring(0,count); remaining=remaining.substring(count).stripLeading();
            } else remaining="";
            c.text(line,x+15,y+6+i*11,WHITE);
        }
    }
    private static void cinematic(Canvas c,View s,int w,int h) {
        double t=s.skillTime;
        // Edge streaks and short accents leave the actor and target visible.
        double summon=Math.max(0,1-Math.abs(t-47)/8);
        double trigger=Math.max(0,1-Math.abs(t-38)/3);
        double dissolve=Math.max(0,1-Math.abs(t-104)/6);
        int a=(int)(70*Math.max(summon,trigger));
        if(a>0) {
            c.fill(0,0,w,2,(a<<24)|0x66EEFF);
            c.fill(0,h-2,w,2,(a<<24)|0x66EEFF);
            for(int i=0;i<6;i++) {
                int y=(i+1)*h/7; int len=10+(int)(summon*35)+i*3;
                c.fill(0,y,len,1,(a<<24)|0x66EEFF);
                c.fill(w-len,y+3,len,1,(a<<24)|0x66EEFF);
            }
        }
        double enter=clamp((t-44)/7),exit=clamp((101-t)/7);
        if(enter*exit>0) {
            int width=Math.min(238,w-20),x=10-(int)((1-enter)*width),y=h-38;
            ribbon(c,x,y,width,27,withAlpha(INK,enter*exit),9);
            c.fill(x+8,y+4,2,19,withAlpha(CYAN,enter*exit));
            c.text(fit(c,upper(s.skillName),width-25),x+16,y+6,withAlpha(WHITE,enter*exit));
            c.fill(x+16,y+20,(int)((width-32)*clamp((t-46)/48)),1,withAlpha(CYAN,enter*exit));
        }
    }
    public static String fit(Canvas c,String text,int pixels) {
        if(text==null||pixels<=0)return "";
        if(c.width(text)<=pixels)return text;
        String suffix="...";
        if(c.width(suffix)>pixels)return "";
        int end=text.length();
        while(end>0&&c.width(text.substring(0,end)+suffix)>pixels)end--;
        return text.substring(0,end)+suffix;
    }
    private static String upper(String s){return s==null?"":s.toUpperCase(Locale.ROOT);}
    private static double clamp(double v){return Math.max(0,Math.min(1,v));}
    private static int withAlpha(int color,double alpha){return ((int)((color>>>24)*clamp(alpha))<<24)|(color&0xFFFFFF);}
    private static void bar(Canvas c,int x,int y,int w,int h,double value,int color) {
        c.fill(x,y,w,h,0xFF243B59);
        int fill=(int)Math.round(w*clamp(value));
        if(fill>0){c.fill(x,y,fill,h,color);c.fill(x,y,fill,1,0x66FFFFFF);}
    }
    private static void ribbon(Canvas c,int x,int y,int w,int h,int color,int cut) {
        cut=Math.min(cut,h/2);
        for(int row=0;row<h;row++) {
            int left=Math.max(0,cut-row),right=Math.max(0,cut-(h-1-row));
            c.fill(x+left,y+row,w-left-right,1,color);
        }
    }
    private static void icon(Canvas c,int i,int x,int y,int color) {
        if(i==0){for(int j=0;j<7;j++)c.fill(x+j,y+6-j,2,2,color);c.fill(x,y+5,5,1,color);}
        if(i==1){c.fill(x+1,y,7,1,color);c.fill(x+1,y,1,8,color);c.fill(x+7,y,1,8,color);c.fill(x+2,y+7,5,1,color);c.fill(x+3,y+3,3,2,color);}
        if(i==2){for(int j=0;j<5;j++){c.fill(x+4-j,y+j,1,1,color);c.fill(x+4+j,y+j,1,1,color);}c.fill(x+4,y+2,1,7,color);}
        if(i==3){c.fill(x+3,y,3,2,color);c.fill(x+1,y+3,7,6,color);c.fill(x+3,y+4,3,1,INK);}
        if(i==4){c.fill(x,y,9,2,color);for(int j=0;j<6;j++)c.fill(x+j/2,y+2+j,9-(j/2)*2,1,color);}
        if(i==5){c.fill(x,y+4,8,1,color);for(int j=0;j<4;j++){c.fill(x+4+j,y+1+j,1,1,color);c.fill(x+4+j,y+7-j,1,1,color);}}
    }
}
