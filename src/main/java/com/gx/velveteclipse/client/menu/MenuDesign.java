package com.gx.velveteclipse.client.menu;
import java.util.*;

/** Shared coordinates for drawing, pointer picking and offline layout validation. */
public final class MenuDesign {
    public static final int W=900,H=500,WHITE=0xFFF6FFFF,CYAN=0xFF79F5FF,INK=0xFF03163D,RED=0xFFF11A4D,MUTED=0xFF9BBBDD;
    public static final String[] TABS={"SKILL","ITEM","EQUIP","PERSONA","STATS","QUEST","SOCIAL LINK","CALENDAR","SYSTEM"};
    public static final String[] HELP={"Habilidades da Persona","Itens e consumiveis","Armas e armaduras","Personas e afinidades","Atributos do jogador","Avancos do mundo","Vinculos com aldeoes","Dias e fases da lua","Opcoes do jogo"};
    public interface Canvas {
        void fill(double x,double y,double w,double h,int color);
        void poly(double[] xy,int color);
        void text(String text,double x,double y,double size,int color,double angle);
        double width(String text,double size);
        void item(int slot,double x,double y);
    }
    public record Rect(double x,double y,double w,double h){public boolean contains(double px,double py){return px>=x&&px<x+w&&py>=y&&py<y+h;}}
    public record Row(String title,String tag,int slot){}
    public record View(boolean home,int tab,int selected,int offset,List<Row> rows,String title,List<String> detail,
                       int[] stats,String action,String message,String player,int wallet,long day,long time,double seconds,boolean motion,
                       double selection,double entered,double pageAge){}
    private static final int[] HOME_X={374,418,405,374,405,397,365,373,395};
    private static final int[] HOME_WIDTH={116,101,137,186,137,141,266,224,182};
    private static final int[] ANGLE={-20,-16,-14,-14,-11,-10,-9,-7,9};
    public static Rect tab(int i,boolean home){return home?new Rect(HOME_X[i],158+i*29,HOME_WIDTH[i],31):new Rect(25,145+i*28,185,25);}
    public static Rect row(int i){return new Rect(248,144+i*33,284,31);}
    public static Rect action(){return new Rect(551,410,300,33);}
    public static Rect back(){return new Rect(27,454,154,25);}
    public static int pickTab(double x,double y,boolean home){for(int i=0;i<TABS.length;i++){Rect r=tab(i,home);double a=Math.toRadians(home?ANGLE[i]:-4),dx=x-r.x,dy=y-r.y;
        if(new Rect(0,0,r.w,r.h).contains(dx*Math.cos(a)+dy*Math.sin(a),-dx*Math.sin(a)+dy*Math.cos(a)))return i;}return -1;}
    public static int pickRow(double x,double y){for(int i=0;i<8;i++)if(row(i).contains(x,y))return i;return -1;}
    public static void background(Canvas c,View v){
        for(int y=0;y<H;y++){
            double t=y/(double)H;
            int color=blend(0xFF05CEDD,0xFF031A9D,Math.min(1,t*1.65));c.fill(0,y,W,1,color);
        }
        double time=v.motion?v.seconds:0;
        for(int i=0;i<24;i++){
            double x=(i*81+Math.sin(time*.24+i)*32)%1040-60,y=19+i%7*18;
            c.poly(new double[]{x,y,x+118,y-5,x+144,y+3,x+28,y+11},0x1824FFFF);
        }
        for(int i=0;i<11;i++){double x=i*108-80;c.poly(new double[]{x,0,x+35,0,x+150,500,x+135,500},0x0725E8FF);}
        c.poly(new double[]{0,0,326,0,163,500,0,500},WHITE);
        c.poly(new double[]{0,85,135,108,105,155,0,134},0xFF00030F);
        c.poly(new double[]{0,349,141,381,109,430,0,403},0xFF00030F);
        c.text("MAIN",29,220,59,0xFF00030F,90);
        if(!v.home)c.poly(new double[]{0,0,230,0,230,500,0,500},0xCD06183D);
    }
    public static void foreground(Canvas c,View v){
        if(v.home){
            c.fill(61,28,140,46,WHITE);c.fill(61,28,140,1,INK);c.fill(61,73,140,1,INK);
            c.text(String.format(Locale.ROOT,"%,d",v.wallet),72,35,18,INK,0);c.text("EMERALDS / CARTEIRA",72,59,7,INK,0);
            c.text(v.player.toUpperCase(Locale.ROOT),590,42,16,WHITE,-4);
        }else{
            c.text("MENU",27,35,34,WHITE,-8);c.text(v.player,27,94,10,CYAN,0);
            c.poly(new double[]{238,55,864,37,855,93,239,110},WHITE);
            c.text(TABS[v.tab],257,62,31,INK,-2);
        }
        int lo=Math.max(0,Math.min(8,(int)v.selection)),hi=Math.min(8,lo+1);double f=v.selection-lo;
        Rect from=tab(lo,v.home),to=tab(hi,v.home);
        double ix=from.x+(to.x-from.x)*f,iy=from.y+(to.y-from.y)*f;
        double iw=v.home?Math.min(280,c.width(TABS[v.tab],39)+18):191;
        double intro=v.motion?MenuMotion.entry(v.entered,v.tab):1;
        ix+=(1-intro)*150;
        if(v.home){double angle=ANGLE[lo]+(ANGLE[hi]-ANGLE[lo])*f;
            ribbon(c,ix,iy,iw,angle,RED,intro,true);ribbon(c,ix,iy,iw,angle,WHITE,intro,false);}
        if(!v.home){
            // Compact tabs use a shallower ribbon, keeping adjacent labels readable.
            c.fill(0,128,228,276,0xFF091C47);
            c.poly(new double[]{ix-4,iy-3,ix+186,iy-16,ix+178,iy+21,ix-8,iy+26},WHITE);
            c.fill(ix-9,iy+5,3,25,RED);
        }
        for(int i=0;i<TABS.length;i++){
            Rect r=tab(i,v.home);boolean active=i==v.tab;
            double show=v.motion?MenuMotion.entry(v.entered,i):1;
            c.text(TABS[i],r.x+(1-show)*150,r.y,v.home?39:20,alpha(active?INK:CYAN,show),v.home?ANGLE[i]:-4);
        }
        if(v.home){
            c.text(HELP[v.tab],626,426,20,WHITE,-2);
            c.text("ENTER  Confirmar    TAB / ESC  Fechar",580,473,9,WHITE,0);
            c.text("Dia "+v.day,757,397,10,CYAN,0);
        }else{
            c.text(v.title,248,117,12,CYAN,0);
            c.fill(241,139,300,272,0xB8051745);c.fill(545,139,314,266,0xAB031340);
            for(int i=0;i<Math.min(8,v.rows.size()-v.offset);i++){
                int index=i+v.offset;Row item=v.rows.get(index);Rect r=row(i);boolean active=index==v.selected;
                if(active)c.poly(new double[]{r.x-4,r.y,r.x+r.w,r.y-2,r.x+r.w-8,r.y+r.h,r.x-8,r.y+r.h},WHITE);
                int color=active?INK:WHITE;double x=r.x+8;
                if(item.slot>=0){c.item(item.slot,x,r.y+5);x+=23;}
                c.text(fit(c,item.title,r.w-(x-r.x)-9,15),x,r.y+2,15,color,0);
                c.text(fit(c,item.tag,r.w-(x-r.x)-9,10),x,r.y+20,10,active?0xFF416583:MUTED,0);
            }
            if(v.rows.size()>8)c.text((v.selected+1)+" / "+v.rows.size()+"   RODA / SETAS",252,414,8,MUTED,0);
            if(v.tab==7)calendar(c,v);
            else{
                double y=154;
                for(String line:v.detail){for(String wrapped:wrap(c,line,278,14)){c.text(wrapped,557,y,14,WHITE,0);y+=18;if(y>387)break;}if(y>387)break;y+=5;}
                if(v.stats!=null){
                    String[] keys={"ST","MA","EN","AG","LU"};y=Math.min(y+7,274);
                    for(int i=0;i<5;i++){c.text(keys[i],557,y,10,CYAN,0);c.fill(583,y+3,216,5,0xFF173769);c.fill(583,y+3,Math.min(216,v.stats[i]*2.18),5,WHITE);c.text(""+v.stats[i],813,y,10,WHITE,0);y+=22;}
                }
            }
            if(!v.action.isEmpty()){Rect a=action();c.poly(new double[]{a.x,a.y,a.x+a.w,a.y-3,a.x+a.w-10,a.y+a.h,a.x-8,a.y+a.h},RED);c.text(fit(c,v.action,278,15),a.x+11,a.y+8,15,WHITE,0);}
            c.text("<  VOLTAR",29,460,11,WHITE,0);
            c.text("ENTER  Confirmar    ESC  Voltar    TAB  Fechar",472,476,9,WHITE,0);
        }
        if(!v.message.isBlank()){
            c.fill(245,449,614,19,0xEB00113D);c.text(fit(c,v.message,596,9),253,454,9,CYAN,0);
        }
    }
    private static void calendar(Canvas c,View v){
        long first=Math.floorDiv(v.day-1,28)*28+1;
        c.text("DIAS "+first+" - "+(first+27),559,152,13,CYAN,0);
        for(int i=0;i<28;i++){
            long day=first+i;int x=558+(i%7)*40,y=182+(i/7)*43;
            c.fill(x,y,35,35,day==v.day?WHITE:0xFF143D83);c.text(""+day,x+5,y+6,11,day==v.day?INK:WHITE,0);
            if((day-1)%8==0)c.fill(x+5,y+26,24,2,day==v.day?RED:CYAN);
        }
        int hour=(int)(Math.floorMod(v.time,24000)/1000+6)%24;
        c.text(String.format(Locale.ROOT,"%02d:00  /  DIA %d",hour,v.day),559,369,11,WHITE,0);
        c.text("Traco ciano: lua cheia (ciclo de 8 dias)",559,390,8,MUTED,0);
    }
    public static String fit(Canvas c,String s,double width,double size){if(c.width(s,size)<=width)return s;while(!s.isEmpty()&&c.width(s+"...",size)>width)s=s.substring(0,s.length()-1);return s+"...";}
    static List<String> wrap(Canvas c,String s,int width,int size){List<String> result=new ArrayList<>();String line="";for(String word:s.split(" ")){if(!line.isEmpty()&&c.width(line+" "+word,size)>width){result.add(line);line="";}line+=(line.isEmpty()?"":" ")+word;}if(!line.isEmpty())result.add(fit(c,line,width,size));return result;}
    static int blend(int a,int b,double t){int c=0xFF000000;for(int shift:new int[]{0,8,16})c|=(int)(((a>>shift)&255)*(1-t)+((b>>shift)&255)*t)<<shift;return c;}
    static int alpha(int color,double opacity){return (color&0xFFFFFF)|((int)((color>>>24)*MenuMotion.clamp(opacity))<<24);}
    private static void ribbon(Canvas c,double x,double y,double w,double angle,int color,double alpha,boolean red){
        double[] p=red?new double[]{-25,31,w+15,-10,w+8,30,-37,42}:new double[]{-10,-2,w+3,-7,w-3,27,-18,33};
        double a=Math.toRadians(angle);for(int i=0;i<p.length;i+=2){double px=p[i],py=p[i+1];p[i]=x+px*Math.cos(a)-py*Math.sin(a);p[i+1]=y+px*Math.sin(a)+py*Math.cos(a);}c.poly(p,alpha(color,alpha));
    }
}
