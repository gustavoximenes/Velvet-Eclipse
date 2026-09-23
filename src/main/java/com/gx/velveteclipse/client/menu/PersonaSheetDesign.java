package com.gx.velveteclipse.client.menu;
import com.gx.velveteclipse.core.persona.*;
import com.gx.velveteclipse.menu.MenuSnapshot.Persona;
import com.gx.velveteclipse.client.menu.MenuDesign.Canvas;
import com.gx.velveteclipse.client.menu.MenuDesign.Rect;
import java.util.*;
import static com.gx.velveteclipse.client.menu.MenuDesign.*;

/** The same layout paints the actual Screen and the exported offline previews. */
public final class PersonaSheetDesign {
    public record View(Persona persona,boolean equipped,int index,int count,int skill,int affinity,double age,double seconds,boolean motion,String message,String modelError){}
    public static Rect skill(int visibleIndex){return new Rect(25+(visibleIndex%2)*229,267+(visibleIndex/2)*27,222,25);}
    public static Rect affinity(int index){return new Rect(36+index*46,174-index*7.7,40,62);}
    public static Rect back(){return new Rect(20,477,99,21);}
    public static Rect previous(){return new Rect(26,109,59,42);}
    public static Rect next(){return new Rect(494,18,58,42);}
    public static Rect equip(){return new Rect(694,477,190,21);}
    public static Rect details(){return new Rect(551,367,300,33);}
    public static Rect model(){return new Rect(492,10,400,452);}
    public static int pickSkill(double x,double y){for(int i=0;i<8;i++)if(skill(i).contains(x,y))return i;return -1;}
    public static int pickAffinity(double x,double y){for(int i=0;i<10;i++)if(affinity(i).contains(x,y))return i;return -1;}
    public static double reveal(View v){return v.motion?MenuMotion.ease(v.age/.42):1;}
    public static double modelX(View v){return 691+80*(1-reveal(v));}
    public static void background(Canvas c,View v){
        for(int y=0;y<500;y++)c.fill(0,y,900,1,MenuDesign.blend(0xFF040A32,0xFF071AB7,Math.max(0,(y-160)/390.0)));
        c.poly(new double[]{900,4,900,211,0,397,0,322},0xFF133EFF);
        c.poly(new double[]{900,228,900,298,0,490,0,409},0xFF0D26F5);
        c.poly(new double[]{467,234,900,99,900,500,455,500},0x4700003A);
        double time=v.motion?v.seconds:0;
        for(int i=0;i<7;i++){double x=513+i*70+Math.sin(time*.25+i)*18;c.poly(new double[]{x,-10,x+2,-10,x-150,490,x-153,490},0x1643D5FF);}
        c.text("PERSONA",516,249,89,0x142CE3FF,-11);
        c.poly(new double[]{0,244,498,203,490,220,0,273},0xC0050D47);
    }
    public static void foreground(Canvas c,View v){foreground(c,v,true);}
    public static void foreground(Canvas c,View v,boolean browsing){
        Persona p=v.persona;double show=reveal(v),slide=(1-show)*-90;
        c.poly(new double[]{0,65+slide,568,-39+slide,585,64+slide,0,171+slide},WHITE);
        c.poly(new double[]{0,63+slide,568,-41+slide,568,-37+slide,0,67+slide},RED);
        c.text("ARCANA",125,54+slide,8,0xFF97A6B2,-10);
        c.text(fit(c,PersonaDetails.arcana(p.arcana()),162,39),137,68+slide,39,0xFFB1B9C0,-10);
        c.text(fit(c,p.name(),235,25),270,49+slide,25,INK,-10);
        c.text("Lv "+p.level(),126,121+slide,25,INK,-10);
        c.text("NEXT EXP",226,99+slide,9,INK,-10);
        c.text(String.format(Locale.ROOT,"%,d",PersonaDetails.remainingXp(p)),258,101+slide,23,INK,-10);
        if(browsing){c.text("< Q",32,119+slide,24,INK,-10);c.text("E >",500,26+slide,24,INK,-10);}
        c.text(browsing?(v.index+1)+" / "+v.count+"  PERSONAS":"RESULTADO DA FUSÃO",27,24,10,CYAN,0);
        if(v.equipped){c.poly(new double[]{645,24,861,24,846,49,630,49},CYAN);c.text("PERSONA EQUIPADA",657,29,12,INK,0);}
        for(int i=0;i<PersonaDetails.ELEMENTS.length;i++){
            Element e=PersonaDetails.ELEMENTS[i];Rect r=affinity(i);double y=r.y()+(1-show)*22;double x=r.x()+20;
            if(i==v.affinity)PersonaGlyphs.disc(c,x,y+15,20,WHITE);
            PersonaGlyphs.disc(c,x,y+15,17,0xFF080D43);PersonaGlyphs.draw(c,e,x,y+15,12,PersonaDetails.color(e));
            Affinity a=PersonaDetails.affinity(p,e);
            String mark=switch(a){case WEAK -> "!";case RESIST -> "R";case NULL -> "N";case REPEL -> "Rf";case DRAIN -> "Ab";default -> "-";};
            if(a==Affinity.RESIST)c.poly(new double[]{x-8,y+36,x+8,y+36,x+7,y+48,x,y+53,x-7,y+48},0xFFC7D9E8);
            c.text(mark,x-c.width(mark,a==Affinity.WEAK?22:12)/2,y+35,a==Affinity.WEAK?22:12,a==Affinity.WEAK?0xFFFF759F:a==Affinity.RESIST?INK:WHITE,0);
            c.text(new String[]{"FIS","FOG","GEL","ELE","VEN","PSI","NUC","LUZ","TRV","ONI"}[i],x-9,y+55,7,MUTED,0);
        }
        c.text("SKILLS",27,244,20,CYAN,-4);
        int page=v.skill/8,pages=Math.max(1,(p.skills().size()+7)/8);
        c.text("SETAS  Selecionar  /  "+(page+1)+" de "+pages,246,249,9,MUTED,0);
        c.fill(18,263,464,146,0xDF030730);
        for(int i=0;i<8;i++){
            Rect r=skill(i);int index=page*8+i;boolean selected=index==v.skill&&v.affinity<0&&index<p.skills().size();
            double entry=v.motion?MenuMotion.ease((v.age-i*.017)/.28):1;double shift=-(1-entry)*34;
            c.fill(r.x()+shift,r.y(),r.w(),r.h(),MenuDesign.alpha(selected?WHITE:0xFF242655,entry));
            if(index<p.skills().size()){
                var s=p.skills().get(index);if(selected)c.fill(r.x()-4+shift,r.y(),3,r.h(),RED);
                PersonaGlyphs.disc(c,r.x()+15+shift,r.y()+12,10,0xFF080D43);PersonaGlyphs.draw(c,s.element(),r.x()+15+shift,r.y()+12,8,PersonaDetails.color(s.element()));
                c.text(fit(c,s.displayName(),175,15),r.x()+31+shift,r.y()+4,15,MenuDesign.alpha(selected?INK:WHITE,entry),0);
            }else c.text("-",r.x()+14+shift,r.y()+4,15,MenuDesign.alpha(0xFF62698F,entry),0);
        }
        c.text("NEXT SKILL",29,386,15,0xFF597EFF,-4);
        var next=PersonaDetails.next(p);
        if(next.isPresent()){
            var n=next.get();c.text("Lv "+n.level(),150,386,15,CYAN,0);PersonaGlyphs.draw(c,n.skill().element(),214,396,9,PersonaDetails.color(n.skill().element()));
            c.text(fit(c,n.skill().displayName(),239,14),234,388,14,CYAN,0);
        }else c.text("Nenhuma habilidade futura",154,389,11,MUTED,0);
        stats(c,p,show);
        c.fill(18,416,464,54,0xEF020D34);
        if(v.affinity>=0){
            Element e=PersonaDetails.ELEMENTS[v.affinity];Affinity a=PersonaDetails.affinity(p,e);
            c.text(PersonaDetails.element(e)+" / "+PersonaDetails.affinityName(a),29,421,14,CYAN,0);
            wrapped(c,PersonaDetails.affinityDescription(a),29,442,442,11,WHITE);
        }else if(!p.skills().isEmpty()){
            var s=p.skills().get(Math.min(v.skill,p.skills().size()-1));
            c.text(fit(c,s.displayName(),186,14),29,421,14,CYAN,0);
            c.text(PersonaDetails.cost(s)+(s.isPassive()?"  /  Efeito automático":s.isDamageSkill()?"  /  Poder "+s.power()+"  /  Acerto "+s.accuracy()+"%":"  /  Suporte por 3 turnos"),225,423,10,WHITE,0);
            wrapped(c,PersonaDetails.description(s),29,442,442,11,WHITE);
        }else c.text("Esta Persona ainda não possui habilidades.",29,435,13,MUTED,0);
        c.text("A / D  Girar modelo",640,455,9,MUTED,0);
        if(!v.modelError.isBlank()){c.fill(553,189,304,43,0xED020D34);wrapped(c,v.modelError,565,196,275,12,WHITE);}
        c.fill(0,475,900,25,0xF2020A2A);
        c.text("ESC  Voltar",22,482,10,WHITE,0);c.text("Q / E  Persona     X  Lista     F  Afinidades",149,482,10,WHITE,0);
        Rect eq=equip();c.fill(eq.x(),eq.y(),eq.w(),eq.h(),v.equipped?0xFF123668:RED);
        c.text(v.equipped?"ENTER  Equipada":"ENTER  Equipar",eq.x()+17,eq.y()+4,11,WHITE,0);
        if(!v.message.isBlank()){c.fill(517,62,365,22,0xEE020D34);c.text(fit(c,v.message,349,10),526,68,10,CYAN,0);}
    }
    private static void stats(Canvas c,Persona p,double show){
        c.poly(new double[]{575,349,878,297,890,421,550,477},0xDA03052E);
        String[] keys={"St","Ma","En","Ag","Lu"};int[] values=p.stats();
        for(int i=0;i<5;i++){
            double y=358+i*22,x=591-i*3;int value=i<values.length?values[i]:0;
            c.text(keys[i],x,y,19,CYAN,-9);c.text(""+value,x+34,y-6,20,WHITE,-9);
            double start=x+75,by=y-1,w=207,len=Math.min(99,Math.max(0,value))/99.0*w*show;
            c.poly(new double[]{start,by,start+w,by-35,start+w,by-29,start,by+6},0xFF202857);
            if(len>0)c.poly(new double[]{start,by,start+len,by-len*35/w,start+len,by-len*35/w+6,start,by+6},0xFF33E5FF);
        }
    }
    static void wrapped(Canvas c,String text,double x,double y,int width,int size,int color){
        for(String line:MenuDesign.wrap(c,text,width,size)){if(y>467)break;c.text(line,x,y,size,color,0);y+=13;}
    }
    public static void rosterHint(Canvas c){
        Rect r=details();c.poly(new double[]{r.x(),r.y(),r.x()+r.w(),r.y()-3,r.x()+r.w()-10,r.y()+r.h(),r.x()-8,r.y()+r.h()},0xFF3DDAEE);
        c.text("X  Ver status e habilidades",r.x()+13,r.y()+8,15,INK,0);
    }
}
