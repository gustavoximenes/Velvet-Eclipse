package com.gx.velveteclipse.client.city;

import com.gx.velveteclipse.client.menu.MenuDesign.Canvas;
import com.gx.velveteclipse.city.CityNetwork;
import java.util.*;

/** Layout shared by native Minecraft drawing, input picking and offline proofs. */
public final class CityShopDesign {
    public static final int W=1280,H=720,WHITE=0xffefffff,INK=0xff040838,CYAN=0xff6affea;
    public record ListBox(int x,int y,int width,int step,int count){}
    public record View(String shop,String title,String page,String category,List<CityNetwork.Row> rows,List<CityNetwork.Row> known,int selected,int offset,long yen,String message,boolean confirm,int quantity,boolean waiting,String member){}
    public static ListBox box(View v){return switch(v.shop()){
        case "aohige"->new ListBox(278,420,630,39,6);
        case "police"->v.page().equals("root")?new ListBox(235,288,590,58,5):new ListBox(100,263,630,45,8);
        case "mayoido"->v.page().equals("root")?new ListBox(190,313,660,63,3):new ListBox(115,238,630,47,8);
        case "blue"->v.page().equals("root")?new ListBox(65,281,725,57,6):new ListBox(691,270,510,43,8);
        default->new ListBox(756,239,438,57,4);
    };}
    public static int pick(View v,double x,double y){var b=box(v);if(x<b.x()||x>=b.x()+b.width()||y<b.y()||y>=b.y()+b.step()*b.count())return -1;int row=v.offset()+(int)((y-b.y())/b.step());return row<v.rows().size()?row:-1;}
    private static String money(long n){return String.format(Locale.ROOT,"%,d",n).replace(',','.');}
    private static void text(Canvas c,String s,double x,double y,double size,int color){c.body(s,x,y,size,color,0);}
    private static String fit(Canvas c,String s,double width,double size){while(s.length()>1&&c.bodyWidth(s,size)>width)s=s.substring(0,s.length()-2)+"…";return s;}
    private static void wrap(Canvas c,String s,double x,double y,double width,int lines,int size,int color){String line="";int n=0;for(String word:s.split(" ")){if(c.bodyWidth(line+word,size)>width&&!line.isEmpty()){text(c,line,x,y+n++*(size+5),size,color);line="";if(n==lines)return;}line+=word+" ";}if(n<lines)text(c,fit(c,line,width,size),x,y+n*(size+5),size,color);}
    public static void draw(Canvas c,View v){
        var b=box(v);boolean root=v.page().equals("root"),activity=!Set.of("aohige","police","mayoido","blue").contains(v.shop());var selected=v.rows().isEmpty()?null:v.rows().get(Math.min(v.selected(),v.rows().size()-1));
        // The world remains live under these UI panels.
        switch(v.shop()){
            case "aohige"->{c.fill(270,361,646,59,INK);text(c,"Suprimentos para a próxima expedição",292,383,25,CYAN);c.fill(920,537,360,183,INK);if(selected!=null){wrap(c,selected.detail(),945,560,300,3,22,CYAN);text(c,"Em estoque",947,661,19,WHITE);c.text(""+selected.owned(),1160,647,37,0xff839af9,0);}}
            case "police"->{if(root){c.fill(215,184,627,93,0xff000000);text(c,"PRIVATE CATALOGUE",239,191,17,WHITE);c.text("DIRECTORY",231,218,61,WHITE,0);c.text("POLICE",35,602,109,WHITE,-90);c.text("Do que você precisa?",848,465,39,WHITE,-5);}else{c.fill(92,204,646,58,0xff080809);c.text(v.category(),115,219,32,WHITE,0);c.fill(789,354,443,248,0xe8000000);if(selected!=null){wrap(c,selected.name(),815,380,384,2,30,WHITE);text(c,selected.metric(),815,467,26,WHITE);text(c,selected.from()+" > "+selected.to(),1040,462,31,0xffff5853);text(c,"Preço  ¥ "+money(selected.price()),815,521,26,WHITE);text(c,selected.owned()>0?"Já adquirido":"Disponível",815,561,21,CYAN);}}}
            case "mayoido"->{if(root)text(c,"Bem-vindo! Fique à vontade.",205,268,30,CYAN);else{c.text(v.category(),123,186,35,WHITE,0);c.fill(764,350,406,202,0xe1270820);text(c,"Materiais / disponibilidade",789,375,24,CYAN);if(selected!=null){wrap(c,selected.detail(),789,420,350,4,23,WHITE);text(c,selected.owned()>0?"Em estoque: "+selected.owned():"",789,529,20,CYAN);}}}
            case "blue"->{if(root){c.text("SELECT",36,69,167,WHITE,-5);text(c,"Relembrar habilidade",73,238,31,WHITE);c.poly(new double[]{820,235,1260,288,1210,569,814,502},0xe2ffffff);if(selected!=null){c.text(fit(c,selected.name(),367,49),843,414,49,INK,-5);wrap(c,selected.detail(),842,492,320,2,23,INK);}}else{c.fill(0,95,1280,106,INK);c.text(v.member(),56,119,43,WHITE,0);text(c,"BE BLUE V / RELEMBRAR HABILIDADE",58,177,20,CYAN);c.text("HABILIDADES ATUAIS",58,251,28,CYAN,0);for(int i=0;i<Math.min(12,v.known().size());i++){int col=i%2,row=i/2;c.fill(57+col*294,300+row*43,280,40,0xe304133f);text(c,fit(c,v.known().get(i).name(),265,21),65+col*294,309+row*43,21,WHITE);}c.text("SELECT SKILL",694,222,35,WHITE,0);if(selected!=null)wrap(c,selected.detail(),65,578,560,3,21,WHITE);}}
            default->{c.poly(new double[]{24,65,537,45,565,138,17,164},0xf1051236);text(c,"PAULOWNIA MALL",49,77,18,CYAN);c.text(v.title(),49,106,36,WHITE,-2);String prompt=switch(v.shop()){case "chagall"->"Uma pausa para um café?";case "mandragora"->"Quer cantar e treinar sua coragem?";case "arcade"->"Qual será o seu próximo desafio?";case "escapade"->"Gostaria de consultar sua sorte?";default->"A vitrine de Kenneth Arnold está em exposição.";};c.poly(new double[]{31,532,1189,499,1250,665,63,694},0xf2051858);c.poly(new double[]{55,500,456,478,445,526,51,548},0xff42dde3);text(c,v.title(),78,505,24,INK);wrap(c,prompt,91,569,1020,2,30,WHITE);if(selected!=null)text(c,selected.detail(),91,630,21,CYAN);}
        }
        if(!v.shop().equals("mayoido")){c.fill(986,28,252,83,0xff000000);c.fill(986,28,252,2,WHITE);text(c,"¥",1001,36,32,WHITE);text(c,money(v.yen()),1050,36,32,WHITE);text(c,"A pagar",1000,79,17,CYAN);long pay=selected==null||root&&Set.of("police","blue").contains(v.shop())?0:(long)selected.price()*v.quantity();text(c,money(pay),1170,77,21,CYAN);}
        if(v.rows().isEmpty()){c.fill(b.x(),b.y(),b.width(),88,0xde060b2b);wrap(c,v.shop().equals("blue")?"Nenhuma habilidade natural disponível para relembrar.":"Nenhum item disponível nesta categoria.",b.x()+18,b.y()+19,b.width()-36,2,22,WHITE);}
        for(int i=0;i<Math.min(b.count(),v.rows().size()-v.offset());i++){var row=v.rows().get(i+v.offset());boolean active=i+v.offset()==v.selected();int y=b.y()+i*b.step();int bg=v.shop().equals("mayoido")?(active?0xff1b1020:0xffadf9d5):(active?INK:0xecefffff);int fg=active?CYAN:INK;c.fill(b.x(),y,b.width(),b.step()-2,bg);if(active)c.fill(b.x()-6,y,6,b.step()-2,v.shop().equals("mayoido")?0xffdd274e:CYAN);String end=row.price()>0?"¥ "+money(row.price()):row.owned()>0?"× "+row.owned():"";text(c,fit(c,row.name(),b.width()-170,26),b.x()+20,y+8,26,fg);text(c,end,b.x()+b.width()-147,y+11,22,fg);}
        if(v.rows().size()>b.count())text(c,(v.selected()+1)+" / "+v.rows().size()+"  ROLAR",b.x()+b.width()-140,b.y()-26,18,CYAN);
        c.fill(14,677,285,32,0xde050820);text(c,"ENTER  OK       ESC  Voltar",25,682,18,WHITE);
        if(!v.message().isBlank()){c.fill(311,677,935,33,0xf5051232);text(c,fit(c,v.message(),904,18),323,683,18,0xffffffaa);}
        if(v.confirm()&&selected!=null){c.fill(0,0,1280,720,0x99000013);c.poly(new double[]{281,232,1005,197,1030,503,261,539},INK);c.poly(new double[]{287,228,1008,193,1008,202,287,237},CYAN);text(c,"Confirmar seleção?",317,257,37,WHITE);text(c,fit(c,selected.name(),630,29),319,321,29,CYAN);text(c,"Quantidade: "+v.quantity()+"     ESQ / DIR",319,370,24,WHITE);text(c,"Total: ¥ "+money((long)selected.price()*v.quantity()),681,370,24,WHITE);c.fill(638,440,328,47,CYAN);text(c,v.waiting()?"Aguarde…":"ENTER  Confirmar",667,451,25,INK);text(c,"ESC  Cancelar",326,453,23,WHITE);}
    }
    private CityShopDesign(){}
}
