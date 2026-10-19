package com.gx.velveteclipse.client.menu;
import com.gx.velveteclipse.menu.MenuSnapshot;
import java.util.*;
import static com.gx.velveteclipse.client.menu.MenuDesign.*;

/** The approved 1280x720 composition, mapped to the existing inspector's 900x500 canvas. */
public final class ReloadMenuDesign {
    public static final double SX=900./1280,SY=500./720;
    public static Rect rect(double x,double y,double w,double h){return new Rect(x*SX,y*SY,w*SX,h*SY);}
    public static Rect row(int tab,int index,boolean gear){
        if(tab==0)return rect(615,121+index*55,596,49);
        if(tab==1)return rect(627,132+index*48,590,45);
        if(tab==2)return gear?rect(60,229+index*51,714,48):rect(653,117+index*76,480,68);
        if(tab==3)return rect(20,190+index*48,644,45);
        if(tab==9)return rect(33,174+index*64,556,60);
        if(tab==8)return rect(724,155+index*78,520,70);
        return rect(38,151+index*50,713,46);
    }
    public static Rect member(int index){return rect(14,97+index*85,537,73);}
    public static int visibleRows(int tab,boolean gear){return tab==8||tab==9||tab==2&&gear?6:8;}
    public static Rect action(){return rect(36,605,680,43);}
    public static Rect back(){return rect(16,679,160,34);}
    public static int pickRow(int tab,boolean gear,double x,double y){for(int i=0;i<visibleRows(tab,gear);i++)if(row(tab,i,gear).contains(x,y))return i;return -1;}
    public static int pickHome(double x,double y){x/=SX;y/=SY;for(int i=0;i<TABS.length;i++){double px=526+Math.sin(i*.55)*23,py=154+i*44,a=Math.toRadians(-16+i*.8),dx=x-px,dy=y-py,lx=dx*Math.cos(a)+dy*Math.sin(a),ly=-dx*Math.sin(a)+dy*Math.cos(a);if(lx>=-28&&lx<=com.gx.velveteclipse.localization.DisplayText.text(TABS[i]).length()*24+40&&ly>=-8&&ly<=52)return i;}return -1;}
    public static Canvas scaled(Canvas target){return new Canvas(){
        public void fill(double x,double y,double w,double h,int color){target.fill(x*SX,y*SY,w*SX,h*SY,color);}
        public void poly(double[] p,int color){double[] q=p.clone();for(int i=0;i<q.length;i++)q[i]*=i%2==0?SX:SY;target.poly(q,color);}
        public void text(String t,double x,double y,double size,int color,double angle){target.text(t,x*SX,y*SY,size*SY,color,angle);}
        public void body(String t,double x,double y,double size,int color,double angle){target.body(t,x*SX,y*SY,size*SY,color,angle);}
        public double width(String t,double size){return target.width(t,size*SY)/SX;}
        public double bodyWidth(String t,double size){return target.bodyWidth(t,size*SY)/SX;}
        public void item(int slot,double x,double y){target.item(slot,x*SX,y*SY);}
    };}
    public static void backdrop(Canvas target,double seconds,boolean motion){
        var c=scaled(target);double time=motion?seconds:0;
        for(int y=0;y<720;y++){double t=y/720.;int color=t<.23?blend(0xFF52FFED,0xFF1488D5,t/.23):t<.57?blend(0xFF1488D5,0xFF173BA1,(t-.23)/.34):blend(0xFF173BA1,0xFF061CB5,(t-.57)/.43);c.fill(0,y,1280,1,color);}
        for(int i=0;i<14;i++){double x=i*107-110,depth=.4+Math.abs(x-690)/970,y=244-depth*143,w=92*depth,h=211*depth;
            c.poly(new double[]{x,y,x+w,y+w*.14,x+w,y+h,x,y+h+w*.13},0x7605276A);
            for(int r=0;r<3;r++)for(int col=0;col<2;col++){double xx=x+9+col*w*.45,yy=y+13+r*h*.26;c.poly(new double[]{xx,yy,xx+w*.31,yy+4,xx+w*.31,yy+h*.18,xx,yy+h*.18+3},0x76288BB6);}
            line(c,x,y-140,x,y+h+180,3,0x76053764);
        }
        for(int i=-5;i<10;i++)line(c,684,318,i*233,720,6,0x76063077);
        for(double y=360;y<770;y+=Math.max(23,(y-300)*.29))line(c,0,y,1280,y-28,5,0x76043573);
        for(int i=0;i<5;i++){double y=340+i*i*13;c.poly(new double[]{460-i*35,y,875+i*29,y-10,884+i*34,y+4,449-i*39,y+13},0x76071E67);}
        for(int i=0;i<13;i++){double x=(i*139+Math.sin(time*.15+i)*15)%1390-80;c.poly(new double[]{x,12+i%5*23,x+152,8+i%5*23,x+197,27+i%5*23,x+48,46+i%5*23},0x24A6FFFF);}
        for(int i=0;i<5;i++)c.poly(new double[]{180+i*197,0,214+i*197,0,75+i*207,670,-1+i*207,680},0x0C72FFFF);
        line(c,760,0,775,258,3,0x88043570);
        for(int i=0;i<48;i++){double a=i*Math.PI/24,b=(i+1)*Math.PI/24;line(c,775+Math.cos(a)*82,276+Math.sin(a)*28,775+Math.cos(b)*82,276+Math.sin(b)*28,4,0x7004174A);}
        if(motion)for(int i=0;i<24;i++){double x=i*223%1260,y=710-((time*10+i*91)%760),r=3+i%5;for(int j=0;j<10;j++){double a=j*Math.PI/5,b=(j+1)*Math.PI/5;line(c,x+Math.cos(a)*r,y+Math.sin(a)*r,x+Math.cos(b)*r,y+Math.sin(b)*r,1,0x44A2FFFF);}}
    }
    public static void background(Canvas target,View v,boolean gear){
        backdrop(target,v.seconds(),v.motion());var c=scaled(target);
        if(!v.home()&&v.tab()==8)c.fill(708,141,554,484,0xE8061638);
        if(v.home()){var panel=ReloadMenuOpening.layer(c,-530*(1-ReloadMenuOpening.panel(v.entered(),v.motion())),0,1);panel.poly(new double[]{0,0,496,0,258,720,0,720},WHITE);for(int i=0;i<8;i++)panel.poly(new double[]{0,62+i*85,178-i*8,94+i*81,0,79+i*87},0xFF02030B);panel.text("MAIN",1,527,171,0xFF02030B,-90);}
        else switch(v.tab()){
            case 0->{c.poly(new double[]{0,0,126,0,84,462,805,720,0,720},WHITE);c.poly(new double[]{1000,720,1280,394,1280,720},WHITE);}
            case 1->{}
            case 2->{c.poly(new double[]{403,0,562,0,1192,375,947,603,309,179},WHITE);c.poly(new double[]{0,206,610,159,1043,720,0,720},0xE60C205A);}
            case 3->{c.poly(new double[]{0,0,880,0,863,124,0,208},WHITE);c.text("PERSONA",-29,-43,193,0xFFB8C0C8,0);}
            case 4->c.poly(new double[]{533,-10,1280,-10,1280,720,805,720},WHITE);
            case 7->c.poly(new double[]{804,-20,1280,-20,1280,720,568,720,775,570,824,358},WHITE);
            case 8->{c.poly(new double[]{0,0,655,0,930,720,0,720},WHITE);c.text("SYSTEM",180,462,180,0xFF000410,36);}
            case 9->{c.poly(new double[]{16,120,620,70,657,720,0,720},0xEC071238);c.poly(new double[]{842,0,1280,0,1280,720,717,720},WHITE);}
            default->c.poly(new double[]{15,150,795,101,840,688,0,718},0xEB06133D);
        }
    }
    public static void foreground(Canvas target,View v,MenuSnapshot data,int actor,boolean gear,int gearSlot){
        var c=scaled(target);
        if(!v.home()&&v.tab()==8)c.fill(708,141,554,484,0xE8061638);
        if(v.home()){
            c.fill(115,34,166,64,INK);c.fill(117,36,162,60,WHITE);c.body(String.format(Locale.ROOT,"¥ %,d",v.wallet()),123,37,31,0xFF050913,0);c.body("YEN / CARTEIRA",126,76,12,INK,0);
            for(int i=0;i<TABS.length;i++){double x=526+Math.sin(i*.55)*23,y=154+i*44,angle=-16+i*.8,w=c.width(TABS[i],53)+25;boolean active=i==v.tab();
                double progress=ReloadMenuOpening.option(v.entered(),i,v.motion());var entry=ReloadMenuOpening.layer(c,155*(1-progress),60*(1-progress),progress);
                if(active){rotated(entry,new double[]{-22,13,w+19,-11,w-9,56,-41,70},x,y,angle,RED);rotated(entry,new double[]{-13,5,w+7,-5,w-10,48,-21,55},x,y,angle,WHITE);}entry.text(TABS[i],x,y+2,active?53:45,active?INK:CYAN,angle);
            }
            footer(c,HELP[v.tab()]);return;
        }
        if(v.tab()!=3&&v.tab()!=8)c.text(TABS[v.tab()],-14,-14,149,0xB8EDF9FF,0);
        if(v.tab()==1){c.poly(new double[]{0,0,1280,0,1280,114,0,82},WHITE);c.text("ITEM",-14,-14,149,0xFFBBDAE8,0);c.body("CONSUMÍVEIS / INVENTÁRIO",675,51,25,INK,0);}
        if(v.tab()<=2&&!gear||v.tab()==4)roster(c,data,actor);
        if(v.tab()==2&&gear){c.poly(new double[]{32,42,757,26,746,89,23,105},WHITE);c.body(v.player(),158,50,36,INK,0);c.body("SELECIONE PARA COMPARAR E EQUIPAR",63,153,22,CYAN,0);}
        if(v.tab()==0)c.text("CUSTO",1120,79,29,INK,0);
        if(v.tab()==3){
            for(int i=0;i<Math.min(8,v.rows().size()-v.offset());i++){int n=i+v.offset();var p=data.personas().get(n);double y=190+i*48;boolean active=n==v.selected();if(active)selected(c,27,y,625,43);c.fill(48,y+6,166,30,active?CYAN:0xFF081949);c.body(fitBody(c,PersonaDetails.arcana(p.arcana()),148,23),57,y+9,23,active?INK:CYAN,0);c.body("Lv "+p.level()+" · "+p.name(),229,y+7,27,active?INK:CYAN,0);if(p.id().equals(data.active()))c.text("E",13,y+10,25,CYAN,0);}
            c.text("WHO AM I?",683,466,116,0x7744DCEB,0);c.body(data.personas().size()+" / "+data.personaCapacity()+" Personas",48,606,24,CYAN,0);c.body("E  Equipada",48,646,21,WHITE,0);
        }else if(v.tab()==7)calendar(c,v);
        else if(v.tab()==4)stats(c,data,actor);
        else{
            for(int i=0;i<Math.min(visibleRows(v.tab(),gear),v.rows().size()-v.offset());i++){
                int n=i+v.offset();var row=v.rows().get(n);Rect box=row(v.tab(),i,gear);double x=box.x()/SX,y=box.y()/SY,w=box.w()/SX,h=box.h()/SY;boolean active=n==v.selected();
                if(active&&!(v.tab()==2&&!gear))selected(c,x+7,y,w-18,h-6);else if(v.tab()>2&&v.tab()!=8)c.fill(x,y,w,h-3,0x9506133D);
                double tx=x+20;if(row.slot()>=0){c.item(row.slot(),tx,y+7);tx+=32;}
                if(v.tab()==0||v.tab()==1){String tag=row.tag().split("   ",2)[v.tab()==0&&row.tag().contains("   ")?1:0];if(v.tab()==1)tag=tag.split(" ",2)[0];c.body(fitBody(c,row.title(),w-160,29),tx+12,y+4,29,active?INK:CYAN,0);c.body(tag,x+w-35-c.bodyWidth(tag,23),y+7,23,active?INK:CYAN,0);}
                else if(v.tab()==2&&!gear){if(active)selected(c,x+47,y+29,400,36);else c.poly(new double[]{x+47,y+25,x+469,y+24,x+457,y+67,x+37,y+69},0xDD06163C);c.fill(x+4,y,160,23,0xFF092572);c.body(row.title(),x+12,y+2,18,WHITE,0);c.body(fitBody(c,row.tag(),380,29),x+67,y+31,29,active?INK:CYAN,0);}
                else{c.body(fitBody(c,row.title(),w-35, v.tab()==8?34:29),tx,y+4,v.tab()==8?34:29,active?INK:CYAN,0);if(!row.tag().isBlank())c.body(fitBody(c,row.tag(),w-45,17),tx,y+h-24,17,active?0xFF254967:WHITE,0);}
            }
            if(v.tab()==9&&!data.bestiary().isEmpty())bestiary(c,data.bestiary().get(Math.min(v.selected(),data.bestiary().size()-1)));
            else if(v.tab()!=8&&v.tab()>4||v.tab()==2&&gear){double y=v.tab()==2?557:563;for(String line:v.detail()){c.body(fitBody(c,line,700,22),47,y,22,CYAN,0);y+=28;if(y>625)break;}}
            else if(v.tab()==0||v.tab()==1){double y=v.tab()==0?569:552;for(String line:v.detail()){c.body(fitBody(c,line,555,21),641,y,21,CYAN,0);y+=25;if(y>603)break;}}
        }
        if(!v.action().isEmpty()&&v.tab()>4&&v.tab()!=7&&v.tab()!=8){selected(c,39,617,676,35);c.body(fitBody(c,v.action(),650,23),53,620,23,INK,0);}
        if(v.rows().isEmpty()&&v.tab()!=4&&v.tab()!=7)c.body("Nenhum registro disponível.",v.tab()<=2?640:45,178,25,CYAN,0);
        if(v.tab()==8&&v.selected()==4){c.fill(26,530,662,112,0xED06163C);double y=542;for(String line:v.detail()){c.body(fitBody(c,line,632,20),41,y,20,CYAN,0);y+=29;if(y>620)break;}}
        c.body("ESC  Voltar",18,682,20,WHITE,0);
        String help=v.tab()==3?"Selecione uma Persona para inspecionar.":v.tab()==0?"Qual habilidade você quer usar?":v.tab()==1?"Qual item você quer usar?":v.tab()==2?"Qual equipamento você quer trocar?":HELP[v.tab()];footer(c,v.tab()==8&&v.selected()==4?"ESQ / DIR  Dificuldade":v.tab()==8&&v.selected()==3?"ENTER  Português / English":help);
        if(!v.message().isBlank()){c.fill(30,652,1175,26,0xEB00113D);c.body(fitBody(c,v.message(),1140,19),43,655,19,CYAN,0);}
    }
    public static void roster(Canvas c,MenuSnapshot data,int actor){
        int start=actor/4*4;int[] colors={0xFF17AEF5,0xFFE62694,0xFF04A6C4,0xFFE5A719};
        for(int i=0;i<Math.min(4,data.members().size()-start);i++){var p=data.members().get(start+i);double x=26,y=97+i*85;boolean active=start+i==actor;
            c.poly(new double[]{x,y,x+525,y-9,x+505,y+59,x-12,y+68},active?WHITE:0xFF00040F);c.poly(new double[]{x,y,x+93,y-2,x+129,y+60,x-14,y+67},colors[i]);
            c.body(start+i==0?"PLAYER":"PARTY",x-10,y+7,16,active?INK:WHITE,-19);c.body(fitBody(c,p.name(),167,28),x+126,y+9,28,active?INK:WHITE,0);c.body("Lv "+p.level(),x+290,y+19,18,active?INK:WHITE,0);c.body(p.hp()+" / "+p.maxHp(),x+365,y+10,22,active?INK:CYAN,0);c.body(p.sp()+" SP",x+388,y+38,19,active?INK:0xFFE5D952,0);
        }
        if(data.members().size()>4)c.body("Q / E  Aliado  "+(actor+1)+" / "+data.members().size(),34,453,20,CYAN,0);
    }
    private static void stats(Canvas c,MenuSnapshot data,int actor){if(data.members().isEmpty())return;var m=data.members().get(Math.min(actor,data.members().size()-1));c.poly(new double[]{557,178,1260,127,1260,598,517,666},0xF4050920);c.text(m.name(),623,211,53,WHITE,-5);c.body("Lv "+m.level(),625,290,40,CYAN,-5);c.body(m.hp()+" / "+m.maxHp()+" HP",621,360,30,WHITE,0);c.body(m.sp()+" / "+m.maxSp()+" SP",621,403,30,WHITE,0);if(m.persona()!=null){c.body(m.persona().name()+" · Lv "+m.persona().level(),621,458,31,CYAN,0);String[] keys={"St","Ma","En","Ag","Lu"};for(int i=0;i<5;i++){int n=m.persona().stats()[i];c.body(keys[i]+" "+n,631,507+i*25,20,WHITE,0);c.fill(707,511+i*25,380,9,0xFF233D6A);c.fill(707,511+i*25,Math.min(380,n*3.8),9,CYAN);}}}
    private static void calendar(Canvas c,View v){long start=Math.floorDiv(v.day()-1,28)*28+1;c.text("DIAS",96,144,45,WHITE,0);c.text(start+"–"+(start+27),294,93,102,INK,0);for(int i=0;i<28;i++){int x=100+i%7*94,y=286+i/7*72;long day=start+i;if(day==v.day())selected(c,x-8,y,65,50);c.body(""+day,x+7,y+9,35,day==v.day()?INK:WHITE,0);if((day-1)%8==0)c.fill(x,y+57,52,3,CYAN);}c.text("EVENTS",909,59,76,INK,0);c.fill(828,149,411,63,INK);c.text("DARK HOUR",849,160,36,CYAN,0);c.body("Calendário do mundo",848,255,29,INK,0);c.body("Dia "+v.day(),864,469,43,INK,0);c.body("Ciano: lua cheia",849,545,25,INK,0);}
    private static void bestiary(Canvas c,MenuSnapshot.Enemy enemy){c.poly(new double[]{632,65,1204,37,1227,111,647,139},INK);c.text(fit(c,enemy.name(),543,43),664,76,43,WHITE,-3);c.fill(658,441,555,165,0xF5071743);c.body("AFINIDADES DESCOBERTAS",676,452,19,CYAN,0);var known=BestiaryDesign.known(enemy.affinities());var elements=com.gx.velveteclipse.client.EnemyAffinityHud.ELEMENTS;for(int i=0;i<elements.length;i++){int x=679+i%7*76,y=480+i/7*52;var affinity=known.get(elements[i]);c.body(com.gx.velveteclipse.equipment.DamageItems.label(elements[i]),x,y,12,WHITE,0);c.body(affinity==null?"?":BestiaryDesign.label(affinity),x,y+19,affinity==null?23:12,CYAN,0);}c.body("Vistos: "+enemy.seen()+" · Derrotas: "+enemy.defeated(),51,575,26,CYAN,0);}
    public static void target(Canvas target,List<MenuSnapshot.Member> members,int selected,String title){var c=scaled(target);c.fill(0,0,1280,720,0xAC02081E);c.poly(new double[]{295,116,1050,75,1020,632,253,666},0xFF05143A);c.text("ESCOLHA O ALVO",324,124,46,CYAN,-3);c.body(fitBody(c,title,640,28),326,184,28,WHITE,0);int start=selected/4*4;for(int i=0;i<Math.min(4,members.size()-start);i++){var m=members.get(start+i);double y=251+i*79;if(start+i==selected)selected(c,319,y,646,67);c.body(m.name(),338,y+6,31,start+i==selected?INK:CYAN,0);c.body(m.hp()+" / "+m.maxHp()+" HP     "+m.sp()+" / "+m.maxSp()+" SP",343,y+42,19,start+i==selected?INK:WHITE,0);}c.body("ENTER Confirmar     ESC Cancelar",329,603,24,CYAN,0);}
    private static void footer(Canvas c,String text){c.poly(new double[]{858,628,1280,628,1280,715,825,715},0xE8061638);c.body(fitBody(c,text,401,23),869,637,23,WHITE,0);c.body("Setas Selecionar   Enter Confirmar   Esc Voltar",879,681,17,WHITE,0);}
    public static String fitBody(Canvas c,String value,double width,double size){value=com.gx.velveteclipse.localization.DisplayText.text(value);if(c.bodyWidth(value,size)<=width)return value;String cut=value;while(!cut.isEmpty()&&c.bodyWidth(cut+"…",size)>width)cut=cut.substring(0,cut.length()-1);return cut+"…";}
    private static void selected(Canvas c,double x,double y,double w,double h){c.poly(new double[]{x-12,y+3,x+w+10,y-4,x+w,y+h,x-19,y+h+3},RED);c.poly(new double[]{x-6,y+5,x+w+7,y+2,x+w-2,y+h-4,x-12,y+h-1},WHITE);}
    private static void rotated(Canvas c,double[] p,double x,double y,double angle,int color){double a=Math.toRadians(angle);for(int i=0;i<p.length;i+=2){double xx=p[i],yy=p[i+1];p[i]=x+xx*Math.cos(a)-yy*Math.sin(a);p[i+1]=y+xx*Math.sin(a)+yy*Math.cos(a);}c.poly(p,color);}
    private static void line(Canvas c,double x,double y,double u,double v,double w,int color){double d=Math.hypot(u-x,v-y);if(d==0)return;double nx=(v-y)/d*w/2,ny=(x-u)/d*w/2;c.poly(new double[]{x+nx,y+ny,u+nx,v+ny,u-nx,v-ny,x-nx,y-ny},color);}
}
