package com.gx.velveteclipse.client;
import com.gx.velveteclipse.client.menu.*;
import com.gx.velveteclipse.core.persona.Element;
/** Convex polygons: batched triangles in game, scan conversion in headless previews. */
public final class BattleGraphics {
 public static void poly(HudDesign.Canvas c,double[] xy,int color){c.poly(xy,color);}
 public static void scan(HudDesign.Canvas c,double[] xy,int color){
  double min=Double.POSITIVE_INFINITY,max=Double.NEGATIVE_INFINITY;for(int i=1;i<xy.length;i+=2){min=Math.min(min,xy[i]);max=Math.max(max,xy[i]);}
  for(int y=(int)Math.floor(min);y<Math.ceil(max);y++){
   double left=Double.POSITIVE_INFINITY,right=Double.NEGATIVE_INFINITY;
   for(int i=0;i<xy.length;i+=2){int j=(i+2)%xy.length;double ay=xy[i+1],by=xy[j+1],scan=y+.5;
    if((ay<=scan&&by>scan)||(by<=scan&&ay>scan)){double x=xy[i]+(scan-ay)*(xy[j]-xy[i])/(by-ay);left=Math.min(left,x);right=Math.max(right,x);}}
   if(right>left)c.fill((int)Math.floor(left),y,(int)Math.ceil(right)-(int)Math.floor(left),1,color);
  }
 }
 public static void icon(HudDesign.Canvas c,Element element,double x,double y,double r){
  PersonaGlyphs.draw(new MenuDesign.Canvas(){
   public void fill(double x,double y,double w,double h,int color){c.fill((int)x,(int)y,(int)w,(int)h,color);}
   public void poly(double[] p,int color){BattleGraphics.poly(c,p,color);}
   public void text(String t,double x,double y,double size,int color,double a){}public double width(String t,double size){return 0;}public void item(int i,double x,double y){}
  },element,x,y,r,PersonaDetails.color(element));
 }
}
