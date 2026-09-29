package com.gx.velveteclipse.client;
import com.gx.velveteclipse.core.battle.AllOutTimeline;

/** Shared by the live painter and offline layout/animation previews. */
public final class BattleTacticalDesign {
 public static BattleMenuLayout.Rect pass(int w,int h){return BattleMenuLayout.main(w,h)[6];}
 public static BattleMenuLayout.Rect go(int w,int h){return new BattleMenuLayout.Rect(w/2,h-48,w/4,40,"GO",0);}
 public static BattleMenuLayout.Rect cancel(int w,int h){return new BattleMenuLayout.Rect(w*3/4,h-48,w/4-8,40,"CANCEL",0);}
 public static void paint(HudDesign.Canvas c,int w,int h,boolean more,boolean pass,boolean prompt,String kind,double age){
  double u=Math.max(.55,Math.min(w/640.,h/360.));
  if(more)c.text("1 MORE",(int)(12*u),(int)(48*u),0xFF72F8FF);
  if(age>=0&&age<42){
   double enter=AllOutTimeline.ease(age/7),leave=AllOutTimeline.ease((age-31)/11),slide=(1-enter+leave)*w*.65;
   double x=w*.46+slide,y=h*.43;
   BattleGraphics.poly(c,new double[]{x-30*u,y+52*u,x+330*u,y-42*u,x+350*u,y+47*u,x+8*u,y+123*u},0xEA1429EF);
   BattleGraphics.poly(c,new double[]{x-4*u,y+49*u,x+300*u,y-23*u,x+260*u,y+72*u,x-18*u,y+123*u},0xDA147AEF);
   c.angled(kind,(int)x,(int)y,(float)(5*u),0xFF14B9F4,-12);c.angled(kind,(int)x+6,(int)y-4,(float)(5*u),0xFFFFFFFF,-12);
  }
  if(prompt){
   BattleGraphics.poly(c,new double[]{w*.40,h,w*.48,h-109*u,w,h-169*u,w,h},0xF4162CDD);
   c.angled("ALL-OUT ATTACK",(int)(w*.50),h-(int)(116*u),(float)(2.1*u),0xFF70F4FF,-11);
   c.angled("TODOS OS INIMIGOS CAÍDOS",(int)(w*.49),h-(int)(73*u),(float)(.9*u),0xFF70F4FF,-11);
   var yes=go(w,h);var no=cancel(w,h);c.fill(yes.x(),yes.y(),yes.w()-5,yes.h(),0xFF1548D6);c.fill(no.x(),no.y(),no.w(),no.h(),0xFF0B207A);
   c.styled("[ENTER] GO!",yes.x()+8,yes.y()+10,(float)(1.4*u),0xFFB2FF38);c.styled("[N] CANCEL",no.x()+5,no.y()+12,(float)(1.05*u),0xFFFFD6DF);
  }
 }
}
