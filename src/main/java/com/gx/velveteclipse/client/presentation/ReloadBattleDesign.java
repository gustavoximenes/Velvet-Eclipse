package com.gx.velveteclipse.client.presentation;
import com.gx.velveteclipse.client.*;
import static com.gx.velveteclipse.client.HudDesign.*;

/** Resolution-independent Reload composition shared with the offline visual audit. */
public final class ReloadBattleDesign {
 public static final int WHITE=0xFFF5FFFF,CYAN=0xFF54ECFF,BLUE=0xFF153AFF,INK=0xDF030E2C;
 public static double unit(int w,int h){return Math.min(w/640.,h/360.);}
 public static BattleMenuLayout.Rect party(int w,int h,int count,int index){double u=unit(w,h);int row=(int)(55*u),height=count*row;return new BattleMenuLayout.Rect(w-(int)(119*u),(h-height)/2+index*row+(int)(26*u),(int)(114*u),(int)(50*u),"",index);}
 public static void command(Canvas c,View v,int index,BattleMenuLayout.Rect r,boolean hover){
  boolean large=index==0||index==2,available=v.playerTurn()&&!v.locked();
  float size=Math.max(.45f,r.h()/(large?13f:15f));int ink=available?WHITE:0xFF7793B7;
  if(hover&&available){BattleHudDesign.ribbon(c,r.x()-3,r.y()+2,r.w()+6,r.h(),0xFF20DAFF);BattleHudDesign.ribbon(c,r.x(),r.y()-1,r.w(),r.h(),BLUE);}
  else if(large)BattleHudDesign.ribbon(c,r.x(),r.y(),r.w(),r.h(),0xA004174D);
  String key=v.keys().get(index);int badge=Math.max(8,r.h()-6);c.fill(r.x()+4,r.y()+3,badge,badge,hover?WHITE:0xFF163C96);
  c.styled(key,r.x()+6,r.y()+4,Math.max(.5f,size*.6f),hover?INK:CYAN);
  c.angled(r.label(),r.x()+badge+10,r.y()+3,size,ink,-5);
 }
 public static void commandsBackdrop(Canvas c,View v,int w,int h){
  double u=BattleMenuLayout.unit(w,h);int x=(int)(48*u),y=h-(int)(66*u),r=(int)(72*u);
  for(int row=-r;row<=r;row++){int edge=(int)Math.sqrt(Math.max(0,r*r-row*row));c.fill(x-edge,y+row,Math.max(2,(int)(4*u)),1,BLUE);}
  c.angled(v.locked()?"ACTION":v.playerTurn()?"COMMAND":"WAIT",(int)(16*u),h-(int)(151*u),(float)(.8*u),CYAN,-5);
 }
 public static void partyCard(Canvas c,BattleMenuLayout.Rect r,String name,int hp,int sp,boolean turn,boolean down){
  double u=r.w()/114.;int x=r.x(),y=r.y(),w=r.w();
  BattleGraphics.poly(c,new double[]{x+17*u,y,x+w,y-13*u,x+w-7*u,y+35*u,x,y+43*u},down?0xCF411637:INK);
  BattleGraphics.poly(c,new double[]{x+w-44*u,y-9*u,x+w,y-15*u,x+w-4*u,y+29*u,x+w-48*u,y+33*u},turn?CYAN:BLUE);
  c.styled(fit(c,name,Math.max(20,(int)(64*u))),x+(int)(9*u),y+(int)(2*u),(float)(.85*u),turn?CYAN:WHITE);
  c.angled(Integer.toString(hp),x+(int)(21*u),y+(int)(13*u),(float)(1.65*u),down?0xFFFF729A:WHITE,-9);
  c.angled(Integer.toString(sp),x+(int)(38*u),y+(int)(31*u),(float)(1.0*u),CYAN,-9);
  if(turn)c.angled("NOW",x+w-(int)(36*u),y-(int)(20*u),(float)(.85*u),WHITE,-9);
 }
 public static void skillBanner(Canvas c,String skill,int w,int h,double age){
  double u=unit(w,h),appear=Math.min(1,Math.max(0,age/7));int x=(int)((24-220*(1-appear))*u),y=(int)(32*u),width=(int)(230*u),height=(int)(27*u);
  BattleHudDesign.ribbon(c,x-3,y+3,width,height,0xFF36DFFF);BattleHudDesign.ribbon(c,x,y,width,height,BLUE);
  c.styled(fit(c,skill,Math.max(25,(int)(width/Math.max(.65,u))-20)),x+(int)(13*u),y+(int)(6*u),(float)(1.2*u),WHITE);
 }
}
