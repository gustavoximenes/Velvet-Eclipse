package com.gx.velveteclipse.client.menu;
import com.gx.velveteclipse.core.battle.AllOutStyle;

/** A single dropdown, shared drawing/hit regions for mouse and keyboard. */
public final class AllOutSelectDesign {
 public static MenuDesign.Rect preview(){return new MenuDesign.Rect(38,145,470,264);}
 public static com.gx.velveteclipse.client.AllOutDesign.Frame frame(){var r=preview();return new com.gx.velveteclipse.client.AllOutDesign.Frame(r.x()+r.w()*.60,r.y()+r.h()*.53,(float)(r.h()*.40),-.20f,0,-.05f);}
 public static MenuDesign.Rect field(){return new MenuDesign.Rect(557,145,290,28);}
 public static MenuDesign.Rect option(int i){return new MenuDesign.Rect(557,175+i*38,290,38);}
 public static int pick(double x,double y){for(int i=0;i<AllOutStyle.values().length;i++)if(option(i).contains(x,y))return i;return -1;}
 public static void paint(MenuDesign.Canvas c,AllOutStyle saved,boolean open,int cursor){
  c.text("ANIMAÇÃO ALL-OUT",38,62,34,MenuDesign.CYAN,0);
  c.body("Sua skin executa o estilo selecionado",40,111,14,MenuDesign.WHITE,0);
  c.body("ESTILO",557,119,13,MenuDesign.CYAN,0);
  c.body("PRÉVIA EM MOVIMENTO",48,417,11,MenuDesign.CYAN,0);
  c.body("ENTER Confirmar    ESC Cancelar",557,407,13,MenuDesign.WHITE,0);
  var r=field();c.fill(r.x(),r.y(),r.w(),r.h(),MenuDesign.WHITE);
  c.text((open?AllOutStyle.values()[cursor]:saved).label,r.x()+10,r.y()+6,16,MenuDesign.INK,0);
  double x=r.x()+r.w()-18,y=r.y()+13;c.poly(open?new double[]{x-5,y+3,x+5,y+3,x,y-3}:new double[]{x-5,y-3,x+5,y-3,x,y+3},MenuDesign.INK);
  if(!open)return;
  c.fill(555,173,294,194,MenuDesign.CYAN);
  for(int i=0;i<AllOutStyle.values().length;i++){
   var s=AllOutStyle.values()[i];r=option(i);boolean focus=i==cursor;
   c.fill(r.x(),r.y(),r.w(),r.h(),focus?MenuDesign.WHITE:MenuDesign.INK);
   c.fill(r.x()+4,r.y()+6,3,18,s.color);
   c.text(s.label,r.x()+14,r.y()+6,16,focus?MenuDesign.INK:MenuDesign.WHITE,0);
   if(s==saved)c.text("ATUAL",r.x()+r.w()-46,r.y()+11,8,focus?MenuDesign.INK:MenuDesign.CYAN,0);
  }
 }
}
