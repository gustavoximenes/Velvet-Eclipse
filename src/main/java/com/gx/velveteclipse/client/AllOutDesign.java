package com.gx.velveteclipse.client;
import com.gx.velveteclipse.core.battle.*;
import java.util.*;

/** One scene painter shared by the live renderer and the full-sequence offline preview. */
public final class AllOutDesign {
 public record Frame(double x,double y,float size,float yaw,float lean,float roll,float depth,float depthScale){
  public Frame(double x,double y,float size,float yaw,float lean,float roll){this(x,y,size,yaw,lean,roll,120,1);}
 }
 public record Puff(int variant,double x,double y,double size,float rotation,float alpha,boolean front){}
 public interface Canvas extends HudDesign.Canvas {
  void actor(int index,Frame frame,double tick);
  void smoke(Puff puff);
 }
 private static double e(double t){return AllOutTimeline.ease(t);}
 public static double density(int count){return Math.max(1,Math.ceil((count-1)/2.)*.475);}
 public static Frame frame(int w,int h,double t,int index,int count,AllOutStyle style){
  boolean main=index==0;double lane=count<=4?new double[]{.17,-.16,-.37,.37}[index]:main?0:((index+1)/2)*(index%2==0?1:-1)*.19/density(count);
  if(t<38)return opening(w,h,t,index,count);
  if(t<90){double p=e((t-38)/25);return new Frame(w*(.52+lane*(1-p)),h*(.56-.13*p)-Math.abs(Math.sin((t-38)*.67))*h*.013*(1-p),h*(float)((main?.42:.30)*(1-p*.8)),(float)(Math.PI-.20),.30f,-.06f);}
  if(t<130){double p=e((t-90)/30);return new Frame(w*(.53-.18*p),h*(.52+.05*p)-Math.sin(p*Math.PI)*h*.012,h*(float)(.25+.15*p),(float)(-.8+.55*p),0,-.04f);}
  float yaw=switch(style){case MITSURU->-.58f;case YUKARI->.20f;case JUNPEI->-.34f;case AKIHIKO->-.38f;default->-.2f;};
  double settle=1+.045*(1-e((t-130)/5));return new Frame(w*.72,h*.67,h*.53f*(float)settle,yaw,0,-.07f);
 }
 /** Close-up of the initiating player, with allies staggered to their left. */
 private static Frame opening(int w,int h,double t,int index,int count){
  double settle=e((t-12)/8),ready=e((t-30)/8),push=1+.022*settle;
  if(index==0)return new Frame(w*(.89-.012*settle),h*(1.27+.012*ready),h*1.20f*(float)push,.52f,(float)(.10+.05*ready),-.10f,320,.035f);
  // Near ally at the lower left; middle and far allies remain visible above/behind.
  double x,y,size;float yaw;
  if(index==1){x=.47;y=1.25;size=.65;yaw=.48f;}
  else if(index==2){x=.54;y=.82;size=.55;yaw=.36f;}
  else if(index==3){x=.20;y=.90;size=.38;yaw=.58f;}
  else {int slot=index-4;double crowd=Math.max(1,(count-4)/3.);x=.28-(slot%3)*.105/crowd;y=.57+(slot/3)*.17;size=.31/Math.sqrt(crowd);yaw=.42f;}
  // Each close-up occupies its own depth band. A background arm must stay behind
  // the initiating player's face even when its local pose extends toward camera.
  return new Frame(w*(x-.004*settle),h*y,h*(float)(size*push),yaw,(float)(.07+.055*ready),-.075f,320-index*60,.035f);
 }
 public static void paint(Canvas c,int w,int h,double t,boolean victory,AllOutStyle style,String name,int count){
  var phase=AllOutTimeline.phase(t,victory);
  if(phase==AllOutTimeline.Phase.END)return;
  if(phase==AllOutTimeline.Phase.WIPE||phase==AllOutTimeline.Phase.RETURN){double a=phase==AllOutTimeline.Phase.WIPE?e(t/12):1-e((t-(victory?176:90))/(victory?12:14));double edge=w*1.14*a;if(a>0)poly(c,new double[]{0,0,edge,0,edge-w*.14,h,0,h},0xFF020C2B);return;}
  if(phase==AllOutTimeline.Phase.VICTORY){card(c,w,h,t,style,name);if(count>0)c.actor(0,frame(w,h,t,0,count,style),t);motto(c,w,h,style);return;}
  arena(c,w,h,t);
  if(t>=50)for(var p:puffs(w,h,t))if(!p.front)c.smoke(p);
  if(phase==AllOutTimeline.Phase.LINEUP||phase==AllOutTimeline.Phase.RUSH){
   if(t>=38)streaks(c,w,h,t);
   // Far members first, the initiating player last. Each actor is drawn once.
   for(int i=count-1;i>=0;i--)c.actor(i,frame(w,h,t,i,count,style),t);
  }else if(phase==AllOutTimeline.Phase.FINISH&&count>0)c.actor(0,frame(w,h,t,0,count,style),t);
  if(t>=50)for(var p:puffs(w,h,t))if(p.front)c.smoke(p);
  impacts(c,w,h,t);debris(c,w,h,t);
 }
 public static void arena(HudDesign.Canvas c,int w,int h,double tick){
  c.fill(0,0,w,h,0xFF03071C);
  for(int i=0;i<24;i++){double p=i/24.;int blue=(int)(17+p*22);c.fill(0,(int)(h*p*.53),w,(int)Math.ceil(h*.53/24),0xFF000000|3<<16|8<<8|blue);}
  // Perspective floor with a low, gently banked camera; no floating diamonds or ribbons.
  boolean opening=tick<38;double bank=opening?-.075:-.055+.025*e((tick-38)/45),scroll=e((tick-38)/24)*.55;
  for(int row=18;row>=0;row--){double z0=.7+row*.50-scroll,z1=z0+.50;
   for(int col=-23;col<23;col++){
    double[] p=new double[8];double[][] q={{col*.70,z0},{(col+1)*.70,z0},{(col+1)*.70,z1},{col*.70,z1}};
    for(int n=0;n<4;n++){double sx=q[n][0]/(q[n][1]+1.3),sy=1/(q[n][1]+1.3);p[n*2]=w*(.5+sx*.53);p[n*2+1]=h*((opening?.72:.33)+sy*(opening?.73:1.50))+bank*(p[n*2]-w*.5);}
    double fog=Math.min(.82,row/24.);int color=((row+col)&1)==0?mix(0xFF4384A5,0xFF07142D,fog):mix(0xFF06132B,0xFF040B20,fog);poly(c,p,color);
   }
  }
 }
 public static List<Puff> puffs(int w,int h,double tick){
  var list=new ArrayList<Puff>();if(tick<50||tick>=126)return list;
  double birth=e((tick-50)/14),exit=e((tick-88)/32),fade=1-e((tick-107)/18),r=Math.min(w*.30,h*.57);
  for(int i=0;i<30;i++){
   double a=i*2.399,ring=.32+(i%5)*.15,swell=1+.10*Math.sin(tick*.18+i);
   double spread=birth*(1+exit*.72),x=w*.52+Math.cos(a)*r*ring*spread,y=h*.54+Math.sin(a)*r*.28*ring*spread-exit*r*.15;
   double size=r*(.66+(i%4)*.08)*birth*swell;
   boolean front=tick<90&&i%4==0;
   list.add(new Puff(i%8,x,y,size,(float)(i*.7+tick*(i%2==0?.008:-.008)),(float)(fade*(.72+i%3*.07)),front));
  }return list;
 }
 private static void impacts(HudDesign.Canvas c,int w,int h,double t){
  for(int hit=0;hit<6;hit++){double age=t-(59+hit*5);if(age<0||age>3.5)continue;double p=age/3.5,a=hit*2.41,r=Math.min(w,h)*(.15+.22*p),x=w*.52+Math.cos(a)*w*.055,y=h*.52+Math.sin(a)*h*.035;
   double dx=Math.cos(a)*r,dy=Math.sin(a)*r*.62,thick=h*.022*(1-p);int alpha=(int)((1-p)*235);
   poly(c,new double[]{x-dx,y-dy,x-dy*.1,y+dx*.1-thick,x+dx,y+dy,x+dy*.1,y-dx*.1+thick},alpha<<24|0xF5FBFF);
   if(hit==5&&age<1)c.fill(0,0,w,h,0x58F1FBFF);
  }
 }
 public static void debris(HudDesign.Canvas c,int w,int h,double tick){
  double age=tick-84;if(age<0||age>29)return;double p=age/29.;int alpha=(int)(255*(1-p));
  for(int i=0;i<38;i++){double a=i*2.399,r=(.06+p*(.75+i%4*.13)),x=w*(.52+Math.cos(a)*r*.53),y=h*(.52+Math.sin(a)*r*.54+p*p*.25);double s=Math.min(w,h)*(.006+i%5*.003)*(1-p*.45),rot=a+p*4;
   double[] shape=new double[10];double[][] v={{-1,-.2},{-.2,-.8},{.6,-.4},{1,.3},{-.4,.65}};
   for(int k=0;k<5;k++){shape[k*2]=x+(v[k][0]*Math.cos(rot)-v[k][1]*Math.sin(rot))*s;shape[k*2+1]=y+(v[k][0]*Math.sin(rot)+v[k][1]*Math.cos(rot))*s;}
   poly(c,shape,alpha<<24|(i%4==0?0xC50930:0x020611));
  }
 }
 private static void streaks(HudDesign.Canvas c,int w,int h,double t){for(int i=0;i<12;i++){double a=(i+.2)*Math.PI/6,r=.53+((t*.045+i*.13)%1)*.4,x=w*(.52+Math.cos(a)*r),y=h*(.46+Math.sin(a)*r),dx=Math.cos(a)*w*.14,dy=Math.sin(a)*h*.14;poly(c,new double[]{x,y,x+dx,y+dy,x+2,y+1},0x456FACCC);}}
 public static void card(HudDesign.Canvas c,int w,int h,double tick,AllOutStyle style,String name){
  c.fill(0,0,w,h,style.color);double tilt=style==AllOutStyle.MITSURU?.18:-.12;
  // Large ink fields and a sharp character cutout area, distinct from the battle HUD.
  poly(c,new double[]{w*.56,0,w*.70,0,w*.93,h,w*.76,h},0x25040A24);
  poly(c,new double[]{0,h*.77,w*.38,h*.60,w*.55,h,0,h},0xFF070B25);
  for(int i=0;i<12;i++){double x=w*(.44+i*.055);poly(c,new double[]{x,0,x+2,0,x+w*tilt,h,x+w*tilt-2,h},0x1DFFFFFF);}
  for(int i=0;i<10;i++){double x=w*(.85+Math.sin(i*1.9)*.13),y=h*(.2+i*.085),s=h*(.012+i%3*.006);poly(c,new double[]{x-s,y,x,y-s,x+s*.7,y+s*.5,x-s*.6,y+s},i%3==0?0xFFF5FFFF:0xFF090B20);}
  c.angled(name.toUpperCase(Locale.ROOT),(int)(w*.035),(int)(h*.88),(float)Math.min(w/440.,w/(Math.max(1,name.length())*19.)),0xFFF7FCFF,-6);
 }
 public static void motto(HudDesign.Canvas c,int w,int h,AllOutStyle style){
  String[] lines=switch(style){case MAKOTO->new String[]{"DONE AND","DUSTED."};case YUKARI->new String[]{"THERE YOU","HAVE IT."};case JUNPEI->new String[]{"THAT'S","THE GAME!"};case AKIHIKO->new String[]{"ON TO THE","NEXT BOUT."};case MITSURU->new String[]{"KNEEL","BEFORE ME."};};
  for(int i=0;i<lines.length;i++){float size=(float)Math.min(h/105.,w/(lines[i].length()*16.5));int x=(int)(w*(.035+i*.018)),y=(int)(h*(.35+i*.19));c.angled(lines[i],x+2,y+2,size,0xFF071127,-9);c.angled(lines[i],x,y,size,0xFFFFFFFF,-9);}
 }
 private static int mix(int a,int b,double t){return 0xff000000|((int)(((a>>16)&255)*(1-t)+((b>>16)&255)*t)<<16)|((int)(((a>>8)&255)*(1-t)+((b>>8)&255)*t)<<8)|(int)((a&255)*(1-t)+(b&255)*t);}
 private static void poly(HudDesign.Canvas c,double[] xy,int color){BattleGraphics.poly(c,xy,color);}
}
