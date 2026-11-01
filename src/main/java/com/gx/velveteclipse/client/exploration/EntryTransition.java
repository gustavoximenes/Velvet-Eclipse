package com.gx.velveteclipse.client.exploration;
import com.gx.velveteclipse.client.HudDesign.Canvas;
import com.gx.velveteclipse.core.battle.EncounterOpening;
import com.gx.velveteclipse.exploration.*;
import java.util.UUID;
import static com.gx.velveteclipse.exploration.ExplorationRules.smooth;
/** Session-local presentation. The server owns the opening and the command gate. */
public final class EntryTransition {
 private static UUID session;
 private static EncounterOpening opening=EncounterOpening.NEUTRAL;
 private static int age,lastServerAge=-1;
 private static boolean active,preparing;
 public static void membership(UUID id){if(!id.equals(session)){session=id;lastServerAge=-1;if(!preparing){active=false;age=0;}}}
 public static void prepare(int mode,int tick){opening=EntryTimeline.opening(mode);age=tick;active=preparing=true;}
 public static void cancelPreparation(){if(preparing)reset();}
 public static void accept(ExplorationNetwork.Entry p){
  if(!p.session().equals(session)||p.elapsed()<0||p.elapsed()<=lastServerAge||p.elapsed()>200||p.opening()<0||p.opening()>=EncounterOpening.values().length)return;
  lastServerAge=p.elapsed();opening=EntryTimeline.opening(p.opening());age=Math.max(age,p.elapsed());preparing=false;active=age<EntryTimeline.duration(opening);
 }
 public static void tick(){if(active){age++;if(age>=EntryTimeline.duration(opening))active=false;}}
 public static void reset(){session=null;active=preparing=false;age=0;lastServerAge=-1;EntryPlayerRenderer.reset();}
 public static boolean visible(){return active;}
 public static EncounterOpening opening(){return opening;}
 public static double time(float partial){return age+partial;}
 public static void paint(Canvas c,int w,int h,float partial){if(visible())paint(c,w,h,opening,time(partial));}
 /** Pure painter also used by the offline production renderer. */
 public static void paint(Canvas c,int w,int h,EncounterOpening variant,double t){
  if(variant==EncounterOpening.AMBUSH){
   double tint=smooth((t-11)/4.4)*(1-smooth((t-53)/8));c.fill(0,0,w,h,((int)(82*tint)<<24)|0xEF0037);
   if(t>=12.4&&t<20){double band=h*1.5*smooth((t-12.4)/7.6);
    c.poly(new double[]{0,0,w,0,w,-h*.2-band,0,h*.8-band},0xFF030511);
    c.poly(new double[]{0,h*.8+band,w,-h*.2+band,w,h,0,h},0xFF030511);
   }
   if(t>4&&t<12){double radius=h*(.12*(1-smooth((t-5)/9))+.04);
    for(int i=0;i<11;i++){double a=i*Math.PI*2/11,cs=Math.cos(a),sn=Math.sin(a),x=w*.46,y=h*.56;
     c.poly(new double[]{x+cs*radius*.3,y+sn*radius*.3,x+cs*radius*2-2,y+sn*radius,x+cs*radius*2+2,y+sn*radius},0xFFFFE4A5);}
   }return;
  }
  if(t<12){if(t>4.8){double width=1+w*1.7*smooth((t-4.8)/7.2);c.poly(new double[]{w*.43-width/2,-h,w*.43+width/2,-h,w*.80+width/2,h*2,w*.80-width/2,h*2},0xFF1205FF);}return;}
  if(t<39){for(var polygon:EntryProjection.outside(new double[]{0,0,w,0,w,h,0,h},w,h,t))c.poly(polygon,0xFF000000);c.playerModel(w,h,t,1);}
 }
 private EntryTransition(){}
}
