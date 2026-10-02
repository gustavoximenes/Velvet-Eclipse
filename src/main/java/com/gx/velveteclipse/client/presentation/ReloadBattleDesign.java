package com.gx.velveteclipse.client.presentation;
import com.gx.velveteclipse.client.*;
import static com.gx.velveteclipse.client.HudDesign.*;

/** Resolution-independent Reload composition shared with the offline visual audit. */
public final class ReloadBattleDesign {
 public static final int WHITE=0xFFF5FFFF,CYAN=0xFF54ECFF,BLUE=0xFF153AFF,INK=0xDF030E2C;
 public static double unit(int w,int h){return Math.min(w/640.,h/360.);}
 public static BattleMenuLayout.Rect party(int w,int h,int count,int index){return party(w,h,count,index,false);}
 public static BattleMenuLayout.Rect party(int w,int h,int count,int index,boolean clock){double u=unit(w,h),row=Math.min(59,(300.-85)/Math.max(1,count-1));double y=Math.min(h*.32,h-(count*row+12)*u);if(clock){y=Math.max(52+13*u,y);row=Math.min(row,Math.max(1,(h-y-51*u-4)/(Math.max(1,count-1)*u)));}return new BattleMenuLayout.Rect(w-(int)(84*u),(int)(y+index*row*u),(int)(78*u),(int)(50*u),"",index);}
 private static void disc(Canvas c,double x,double y,double r,int color){for(int i=0;i<28;i++){double a=i*Math.PI*2/28,b=(i+1)*Math.PI*2/28;BattleGraphics.poly(c,new double[]{x,y,x+Math.cos(a)*r,y+Math.sin(a)*r,x+Math.cos(b)*r,y+Math.sin(b)*r},color);}}
 public static void command(Canvas c,View v,int index,BattleMenuLayout.Rect r,boolean hover){
  double u=r.h()/(double)(index==0?40:index==2?29:index==3?23:index==4?24:index==6?25:17);
  boolean available=v.playerTurn()&&!v.locked()&&(index!=6||BattleTacticsClient.canPass());int color=available?WHITE:0xFF7188A9;
  if(index==1||index==5){c.styled(v.keys().get(index)+"  "+r.label(),r.x()+2,r.y()+2,(float)(.82*u),hover&&available?CYAN:color);return;}
  if(hover&&available)BattleHudDesign.ribbon(c,r.x()-3,r.y()+2,r.w(),r.h()-2,0xB0174CDE);
  double top=r.y()-switch(index){case 0->292;case 2->244;case 3->275;case 4->261;default->223;}*u;
  double bx=(index==0||index==2?192:index==3?176.7:index==4?206.7:225)*u,by=top+(index==0?296.7:index==2?264.7:index==3?280:index==4?279.3:238)*u;
  if(index!=6){disc(c,bx,by,7.5*u,0xFFE0FFFF);disc(c,bx,by,6.4*u,0xFF071536);c.styled(v.keys().get(index),(int)(bx-2.8*u),(int)(by-5*u),(float)(.88*u),color);}
  float scale=(float)((index==0?1.95:index==2?1.72:index==6?1.05:1.45)*u);
  int x=(int)((index==0?194:index==2?115.3:index==3?138.7:index==4?218.7:232)*u),y=(int)(top+(index==0?301.3:index==2?252:index==3?276:index==4?263.3:231.3)*u);
  c.angled(r.label(),x+1,y+1,scale,0xFF030C26,-10);c.angled(r.label(),x,y,scale,color,-10);
 }
 public static void commandsBackdrop(Canvas c,View v,int w,int h){
  double u=unit(w,h),top=h-360*u,cx=98*u,cy=top+206*u;
  // True crescent follows the approved character framing; transparent center leaves the skin visible.
  for(int i=0;i<40;i++){double a=Math.toRadians(70+i*5.5),b=Math.toRadians(70+(i+1)*5.5);BattleGraphics.poly(c,new double[]{cx+Math.cos(a)*77*u,cy+Math.sin(a)*85*u,cx+Math.cos(b)*77*u,cy+Math.sin(b)*85*u,cx+12*u+Math.cos(b)*66*u,cy+Math.sin(b)*79*u,cx+12*u+Math.cos(a)*66*u,cy+Math.sin(a)*79*u},BLUE);}
  c.angled(v.locked()?"ACTION":v.playerTurn()?"COMMAND":"WAIT",(int)(37*u),(int)(top+168*u),(float)(.88*u),BLUE,-10);
  disc(c,179*u,top+260*u,23*u,BLUE);disc(c,205*u,top+274*u,22*u,0xFF1384DE);disc(c,225*u,top+245*u,21*u,0xFF2046DA);
  c.styled("TAB  ANALISAR",(int)(15*u),(int)(top+340*u),(float)(.78*u),WHITE);
 }
 public static void partyCard(Canvas c,BattleMenuLayout.Rect r,String name,int hp,int sp,boolean turn,boolean down){
  double u=r.w()/78.;int x=r.x(),y=r.y();
  BattleGraphics.poly(c,new double[]{x+9*u,y+3*u,x+78*u,y-8*u,x+77*u,y+38*u,x,y+51*u},down?0xDF471B32:INK);
  BattleGraphics.poly(c,new double[]{x+2*u,y+3*u,x+27*u,y-13*u,x+48*u,y+3*u,x+22*u,y+34*u},turn?BLUE:0xFF183C88);
  c.angled(Integer.toString(hp),x+(int)(42*u),y+(int)(11*u),(float)(1.35*u),down?0xFFFF729A:WHITE,-9);
  c.angled(Integer.toString(sp),x+(int)(44*u),y+(int)(38*u),(float)(1.13*u),WHITE,-9);
 }
 public static void enemyHeader(Canvas c,String name,int w,int h){double u=unit(w,h);BattleHudDesign.ribbon(c,(int)(16*u),(int)(17*u),(int)(206*u),(int)(22*u),0xEDAC071B);c.styled(fit(c,name,158),(int)(33*u),(int)(21*u),(float)(1.13*u),WHITE);}
 public static void skillBanner(Canvas c,String skill,int w,int h,double age){
  double u=unit(w,h),appear=Math.min(1,Math.max(0,age/7));int x=(int)((24-220*(1-appear))*u),y=(int)(32*u),width=(int)(230*u),height=(int)(27*u);
  BattleHudDesign.ribbon(c,x-3,y+3,width,height,0xFF36DFFF);BattleHudDesign.ribbon(c,x,y,width,height,BLUE);
  c.styled(fit(c,skill,Math.max(25,(int)(width/Math.max(.65,u))-20)),x+(int)(13*u),y+(int)(6*u),(float)(1.2*u),WHITE);
 }
}
