package com.gx.velveteclipse.client.menu;
import com.gx.velveteclipse.core.battle.AllOutStyle;

/** A single dropdown, shared drawing/hit regions for mouse and keyboard. */
public final class AllOutSelectDesign {
 public static MenuDesign.Rect field(){return new MenuDesign.Rect(557,205,290,28);}
 public static MenuDesign.Rect option(int i){return new MenuDesign.Rect(557,235+i*30,290,30);}
 public static int pick(double x,double y){for(int i=0;i<AllOutStyle.values().length;i++)if(option(i).contains(x,y))return i;return -1;}
 public static void paint(MenuDesign.Canvas c,AllOutStyle saved,boolean open,int cursor){
  var r=field();c.fill(r.x(),r.y(),r.w(),r.h(),MenuDesign.WHITE);
  c.text((open?AllOutStyle.values()[cursor]:saved).label,r.x()+10,r.y()+6,16,MenuDesign.INK,0);
  double x=r.x()+r.w()-18,y=r.y()+13;c.poly(open?new double[]{x-5,y+3,x+5,y+3,x,y-3}:new double[]{x-5,y-3,x+5,y-3,x,y+3},MenuDesign.INK);
  if(!open)return;
  c.fill(555,233,294,154,MenuDesign.CYAN);
  for(int i=0;i<AllOutStyle.values().length;i++){
   var s=AllOutStyle.values()[i];r=option(i);boolean focus=i==cursor;
   c.fill(r.x(),r.y(),r.w(),r.h(),focus?MenuDesign.WHITE:MenuDesign.INK);
   c.fill(r.x()+4,r.y()+6,3,18,s.color);
   c.text(s.label,r.x()+14,r.y()+6,16,focus?MenuDesign.INK:MenuDesign.WHITE,0);
   if(s==saved)c.text("ATUAL",r.x()+r.w()-46,r.y()+11,8,focus?MenuDesign.INK:MenuDesign.CYAN,0);
  }
 }
}
