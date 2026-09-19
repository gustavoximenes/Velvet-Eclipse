package com.gx.velveteclipse.client.darkhour;
import com.gx.velveteclipse.client.*;

/** Original vector clock artwork; shares the mod's HUD canvas and scales with the GUI. */
public final class DarkHourClockDesign {
 private static final String[] ROMAN={"XII","I","II","III","IV","V","VI","VII","VIII","IX","X","XI"};
 public static void paint(HudDesign.Canvas c,int w,int h,double progress,boolean green,double age){
  c.fill(0,0,w,h,0xFA000402);double r=Math.min(w*.29,h*.41),cx=w*.5,cy=h*.49;
  int ink=green?0xFF49D322:0xFFE3E6DE,dim=green?0x55319918:0x55838683;
  face(c,w*.1,h*.1,r*.85,progress+.04,dim,-.3);face(c,w*.94,h*.87,r*1.3,progress,dim,.22);
  c.angled(green?"00:00:00":"23:59:59",(int)(w*.67),(int)(h*.13),2.2f,dim,-14);
  c.angled(green?"00:00":"23:59",(int)(-w*.05),(int)(h*.77),3.6f,dim,-20);
  face(c,cx,cy,r,progress,ink,0);
  String title=green?"DARK HOUR":"THE HIDDEN HOUR";float scale=(float)Math.max(.8,Math.min(2,w/500.));
  c.styled(title,(int)(cx-title.length()*5*scale/2),(int)(cy+r+18),scale,ink);
  if(green)c.fill(0,0,w,Math.max(1,(int)(3+Math.sin(age)*2)),0xFF4EC728);
 }
 private static void face(HudDesign.Canvas c,double x,double y,double r,double progress,int ink,double tilt){
  ring(c,x,y,r,r*.028,ink);ring(c,x,y,r*.79,r*.017,ink);
  for(int i=0;i<60;i++){double a=i*Math.PI/30+tilt;line(c,x+Math.sin(a)*r*.95,y-Math.cos(a)*r*.95,x+Math.sin(a)*r*(i%5==0?.89:.92),y-Math.cos(a)*r*(i%5==0?.89:.92),i%5==0?2:1,ink);}
  for(int i=0;i<12;i++){double a=i*Math.PI/6+tilt;String s=ROMAN[i];double px=x+Math.sin(a)*r*.855,py=y-Math.cos(a)*r*.855;float scale=(float)Math.max(.7,r/92);c.angled(s,(int)(px-s.length()*3*scale),(int)(py-4*scale),scale,ink,(float)Math.toDegrees(tilt));}
  double minute=(-Math.PI*.3)*(1-Math.max(0,Math.min(1,progress)))+tilt;
  double second=-Math.PI*2*(1-Math.max(0,Math.min(1,progress)))+tilt;
  line(c,x,y,x+Math.sin(minute)*r*.72,y-Math.cos(minute)*r*.72,r*.037,ink);
  line(c,x,y,x+Math.sin(second)*r*.93,y-Math.cos(second)*r*.93,r*.014,ink);
  ring(c,x,y,r*.038,r*.038,ink);c.styled(progress>=1?"AM":"PM",(int)(x+r*.23),(int)(y+r*.35),(float)Math.max(.7,r/100),ink);
 }
 private static void ring(HudDesign.Canvas c,double x,double y,double r,double width,int color){for(int i=0;i<96;i++){double a=i*Math.PI/48,b=(i+1)*Math.PI/48;BattleGraphics.poly(c,new double[]{x+Math.cos(a)*r,y+Math.sin(a)*r,x+Math.cos(b)*r,y+Math.sin(b)*r,x+Math.cos(b)*(r-width),y+Math.sin(b)*(r-width),x+Math.cos(a)*(r-width),y+Math.sin(a)*(r-width)},color);}}
 private static void line(HudDesign.Canvas c,double x,double y,double u,double v,double width,int color){double d=Math.max(.001,Math.hypot(u-x,v-y)),dx=-(v-y)/d*width,dy=(u-x)/d*width;BattleGraphics.poly(c,new double[]{x+dx,y+dy,u+dx,v+dy,u-dx,v-dy,x-dx,y-dy},color);}
}
