package com.gx.velveteclipse.client.shuffle;
import com.gx.velveteclipse.client.menu.MenuDesign;
import com.gx.velveteclipse.shuffle.*;
import static com.gx.velveteclipse.client.menu.MenuDesign.*;

/** Original vector card faces, typographic sweep and blue/cyan menu language. */
public final class ShuffleDesign {
 public static Rect card(int i,int count,int selected){double gap=158,width=138;return new Rect((900-(count-1)*gap-width)/2+i*gap,132-(i==selected?12:0),width,211);}
 public static Rect row(int visible){return new Rect(45,115+visible*40,450,36);}
 public static Rect confirm(){return new Rect(607,451,253,35);}
 public static Rect back(){return new Rect(32,451,248,35);}
 public static void background(Canvas c,double age,double time,boolean teaching){
  c.fill(0,0,900,500,0xFF081254);
  c.poly(new double[]{0,0,900,0,900,315,0,479},0xFF1539EE);
  c.poly(new double[]{0,261,900,123,900,209,0,372},0xFF117ADE);
  for(int i=0;i<13;i++){double x=i*92-150+(time*19)%92;c.poly(new double[]{x,0,x+29,0,x+246,500,x+211,500},0x1431EFFF);}
  c.text(teaching?"SKILL CARD":"SHUFFLE TIME",36,22,43,WHITE,-5);
  c.text(teaching?"ESCOLHA UMA PERSONA":"ESCOLHA SEU DESTINO",41,81,12,CYAN,-3);
  c.poly(new double[]{0,376,900,349,900,500,0,500},0xF003103F);
 }
 public static void card(Canvas c,ShuffleSnapshot.Choice choice,Rect r,boolean selected,double reveal){
  double x=r.x(),y=r.y()+(1-reveal)*90,w=r.w(),h=r.h();
  c.poly(new double[]{x-3,y+3,x+w+5,y-4,x+w+5,y+h+3,x-3,y+h+8},selected?CYAN:0xFF547BB2);
  c.fill(x,y,w,h,0xFF04132C);c.fill(x+4,y+4,w-8,h-8,0xFFB9E1ED);c.fill(x+7,y+7,w-14,h-14,0xFF163D5C);
  for(int row=0;row<9;row++)for(int col=0;col<5;col++){double a=x+9+col*24,b=y+9+row*21;c.poly(new double[]{a+11,b,a+22,b+10,a+11,b+20,a,b+10},(row+col)%2==0?0x55547C9B:0x55223859);}
  c.fill(x+8,y+h-30,w-16,21,0xE605173C);
  if(choice.kind()==ShuffleRules.Kind.PERSONA){c.text("PERSONA",x+14,y+13,16,CYAN,0);c.text("Lv "+choice.amount(),x+14,y+h-25,13,WHITE,0);}
  else if(choice.kind()==ShuffleRules.Kind.DARK_HOUR){symbol(c,choice.kind(),x+w/2,y+90,43);c.text("+5 MIN",x+26,y+h-26,21,CYAN,0);}
  else {
   c.text(String.valueOf(choice.rank()),x+w-29,y+12,23,CYAN,0);c.text(String.valueOf(choice.rank()),x+13,y+h-29,20,WHITE,0);
   int count=Math.min(5,choice.rank());for(int i=0;i<count;i++)symbol(c,choice.kind(),x+w/2+(count>3?(i%2==0?-25:25):0),y+55+i*(count>3?21:37),count>3?17:24);
  }
 }
 private static void symbol(Canvas c,ShuffleRules.Kind kind,double x,double y,double s){
  switch(kind){
   case DARK_HOUR->{com.gx.velveteclipse.client.menu.PersonaGlyphs.disc(c,x,y,s,0xFF74FF66);com.gx.velveteclipse.client.menu.PersonaGlyphs.disc(c,x,y,s*.84,0xFF061B32);c.fill(x-2,y-s*.65,4,s*.7,0xFFB7FF94);c.fill(x,y-2,s*.48,4,0xFFB7FF94);c.text("+",x+s*.55,y+s*.3,s*.6,0xFFDDFFAE,0);}
   case CUPS->{c.poly(new double[]{x-s,y-s*.6,x+s,y-s*.6,x+s*.55,y+s*.45,x,y+s*.75,x-s*.55,y+s*.45},0xFF52E7FF);c.fill(x-3,y+s*.5,6,s*.6,0xFFAEFFFF);c.fill(x-s*.55,y+s,s*1.1,5,0xFF52E7FF);}
   case COINS->{double[] p=new double[24];for(int i=0;i<12;i++){p[i*2]=x+Math.cos(i*Math.PI/6)*s;p[i*2+1]=y+Math.sin(i*Math.PI/6)*s;}c.poly(p,0xFFFFD94A);c.text("¥",x-s*.45,y-s*.68,s*1.25,0xFF80650B,0);}
   case WANDS->{c.fill(x-2,y-s,4,s*2.4,0xFF4FFA89);for(int i=0;i<4;i++){double yy=y-s+i*s*.55;c.poly(new double[]{x,yy,x-s*.8,yy-s*.2,x-s*.5,yy+s*.3},0xFF85EE8A);c.poly(new double[]{x,yy+s*.25,x+s*.8,yy,x+s*.5,yy+s*.65},0xFF43C97B);}}
   case SWORDS->{c.poly(new double[]{x,y-s*1.3,x+7,y-s*.8,x+4,y+s*.4,x-4,y+s*.4,x-7,y-s*.8},0xFFFF436E);c.fill(x-14,y+s*.35,28,5,0xFFFDADCB);c.fill(x-3,y+s*.5,6,s*.6,0xFFFF436E);}
   default->{}
  }
 }
 public static void footer(Canvas c,String title,String detail,String message,boolean waiting,boolean done){
  double titleSize=Math.min(34,34*790/Math.max(790,c.width(title,34)));c.text(title,41,375,titleSize,WHITE,-2);c.text(detail,42,419,12,CYAN,0);
  if(!message.isBlank())c.text(message,42,441,10,0xFFFFFFA5,0);
  c.poly(new double[]{607,455,862,446,856,487,600,487},waiting?0xFF385987:CYAN);
  c.text(waiting?"AGUARDE…":done?"[ENTER] VOLTAR":"[ENTER] CONFIRMAR",623,462,19,INK,0);
  c.text("[ESC] "+(done?"VOLTAR":"GUARDAR / VOLTAR"),42,465,14,WHITE,0);
  c.poly(new double[]{325,473,333,468,333,478},CYAN);c.poly(new double[]{354,473,346,468,346,478},CYAN);c.text("SELECIONAR",365,465,13,CYAN,0);
 }
 public static void intro(Canvas c,double age){
  if(age>=1.25)return;double x=age<.3?-1000*(1-age/.3):age>1?1100*(age-1)/.25:0;
  c.poly(new double[]{x-50,165,x+960,45,x+960,340,x-50,453},0xF00935D5);
  c.poly(new double[]{x-50,310,x+960,190,x+960,285,x-50,412},0xFF19C7EE);
  c.text("SHUFFLE",x+85,200,78,WHITE,-8);c.text("TIME",x+552,220,78,WHITE,-8);
 }
 public static void badge(Canvas c,int count,String key,double width,double height){
  if(count<=0)return;double x=width-234,y=height-80;
  c.poly(new double[]{x+17,y,x+224,y-4,x+218,y+63,x,y+69},0xEE091A59);
  c.poly(new double[]{x+17,y,x+224,y-4,x+220,y+8,x+12,y+12},CYAN);
  c.text("SHUFFLE TIME",x+23,y+15,21,WHITE,-3);c.text("["+key+"] DISPONÍVEL  × "+count,x+20,y+43,11,CYAN,0);
 }
}
