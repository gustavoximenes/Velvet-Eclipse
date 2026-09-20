package com.gx.velveteclipse.client;
import com.gx.velveteclipse.core.battle.AllOutTimeline;
import com.gx.velveteclipse.network.AllOutCueS2C;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.*;

/** Shared cinematic stage. It presents the real party without teleporting anyone through terrain. */
@Mod.EventBusSubscriber(modid="velvet_eclipse",bus=Mod.EventBusSubscriber.Bus.FORGE,value=Dist.CLIENT)
public final class AllOutPresentation {
 private static AllOutCueS2C cue;private static int ticks;
 public static void accept(AllOutCueS2C p){if(!p.active()){reset();return;}cue=p;ticks=0;PersonaCinematics.reset();}
 public static boolean active(){return cue!=null;}
 public static void reset(){cue=null;ticks=0;}
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){if(e.phase==TickEvent.Phase.END&&cue!=null&&!Minecraft.m_91087_().m_91104_()){if(Minecraft.m_91087_().f_91073_==null||++ticks>AllOutTimeline.duration(cue.victory())+20)reset();}}
 public static void paint(GuiGraphics g,HudDesign.Canvas c,int w,int h,float partial){
  if(cue==null)return;double t=ticks+partial;var phase=AllOutTimeline.phase(t,cue.victory());
  if(phase==AllOutTimeline.Phase.WIPE||phase==AllOutTimeline.Phase.RETURN||phase==AllOutTimeline.Phase.END){
   double amount=phase==AllOutTimeline.Phase.WIPE?AllOutTimeline.ease(t/12):phase==AllOutTimeline.Phase.END?0:1-AllOutTimeline.ease((t-(cue.victory()?154:90))/(cue.victory()?12:14));
   BattleGraphics.poly(c,new double[]{0,0,w*amount+w*.1,0,w*amount-w*.1,h,0,h},0xFF142BEC);return;
  }
  background(c,w,h,t);
  var mc=Minecraft.m_91087_();if(mc.f_91073_==null)return;
  var party=new ArrayList<AbstractClientPlayer>();for(int id:cue.fighters()){var e=mc.f_91073_.m_6815_(id);if(e instanceof AbstractClientPlayer p)party.add(p);}
  var leader=mc.f_91073_.m_6815_(cue.leader());party.sort(Comparator.comparingInt(p->p.m_19879_()==cue.leader()?1:0));
  if(phase==AllOutTimeline.Phase.FINISH){cloud(c,w,h,t,Math.max(0,(117-t)/27));fragments(c,w,h,t);}
  if(phase==AllOutTimeline.Phase.LINEUP||phase==AllOutTimeline.Phase.RUSH){
   int side=0;double progress=phase==AllOutTimeline.Phase.RUSH?AllOutTimeline.ease((t-38)/24):0;
   for(var p:party){boolean main=p.m_19879_()==cue.leader();int slot=main?0:++side;
    double x,y;float size,yaw;
    if(phase==AllOutTimeline.Phase.LINEUP){
     double density=formationDensity(party.size());double offset=slot==0?0:((slot+1)/2)*(slot%2==0?1:-1);
     x=party.size()<=4?w*(main?.68:slot==1?.23:slot==2?.43:.84):w*.5+offset*w*.19/density;
     y=h*(main?.59:.48);size=(float)(h*(main?.49:.31)/Math.sqrt(density));yaw=main?-.2f:slot%2==0?-.4f:.4f;
    }else{
     double lane=main?-.24:((slot%2==0?1:-1)*(1+slot/2))*.15/formationDensity(party.size());
     x=w*(.5+lane*(1-progress));y=h*(.68-progress*.22);size=(float)(h*(main?.66:.43)*(1-progress*.78));yaw=(float)(Math.PI-.12+lane*.7);
    }
    // Cyan rim uses the same skin silhouette and native slim/standard mesh.
    AllOutPlayerRenderer.draw(g,p,x-2,y,size*1.018f,t,cue.victory(),yaw,true);
    AllOutPlayerRenderer.draw(g,p,x,y,size,t,cue.victory(),yaw,false);
   }
  }else if((phase==AllOutTimeline.Phase.FINISH||phase==AllOutTimeline.Phase.VICTORY)&&leader instanceof AbstractClientPlayer player){
   if(phase==AllOutTimeline.Phase.VICTORY)victoryBackdrop(c,w,h,player.m_36316_().getName());
   boolean card=phase==AllOutTimeline.Phase.VICTORY;
   AllOutPlayerRenderer.draw(g,player,w*(card?.74:.30),h*(card?.59:.52),h*(card?.48f:.42f),t,true,-.17f,card);
  }
  if(phase==AllOutTimeline.Phase.CLOUD){g.m_280262_();g.m_280168_().m_85836_();g.m_280168_().m_85837_(0,0,300);cloud(c,w,h,t,1);fragments(c,w,h,t);if(Math.abs(t-AllOutTimeline.IMPACT)<1.5)c.fill(0,0,w,h,0xB6BBFFFF);g.m_280262_();g.m_280168_().m_85849_();}
  if(phase==AllOutTimeline.Phase.LINEUP){c.angled("ALL-OUT ATTACK",(int)(w*.04),(int)(h*.10),(float)(w/225.),0xFF39DFFF,-9);c.angled("ALL-OUT ATTACK",(int)(w*.045),(int)(h*.09),(float)(w/225.),0xFFF6FFFF,-9);}
  if(phase==AllOutTimeline.Phase.VICTORY)c.styled("THAT'S ALL!",(int)(w*.06),(int)(h*.83),(float)(w/230.),0xFFF6FFFF);
 }
 public static void victoryBackdrop(HudDesign.Canvas c,int w,int h,String name){
  c.fill(0,0,w,h,0xFF03B6E8);
  BattleGraphics.poly(c,new double[]{w*.35,0,w,0,w,h*.82,w*.74,h*.47},0xFF007F9F);
  BattleGraphics.poly(c,new double[]{0,h*.78,w*.46,h*.52,w*.66,h,w*.06,h},0xFF034B7C);
  c.angled("VICTORY",(int)(w*.025),(int)(h*.49),(float)(w/110.),0xFF05779F,-10);
  c.angled(name.toUpperCase(java.util.Locale.ROOT),(int)(w*.08),(int)(h*.69),(float)Math.min(w/120.,w/(name.length()*8.)),0xFF002C64,-10);
 }
 public static void fragments(HudDesign.Canvas c,int w,int h,double tick){
  double age=Math.max(0,tick-68);
  for(int i=0;i<26;i++){double a=i*2.399,r=(.08+age*.013)*(i%4+1),x=w*(.5+Math.cos(a)*r*.24),y=h*(.52+Math.sin(a)*r*.27);double k=(5+i%7)*w/640.;BattleGraphics.poly(c,new double[]{x-k,y-k*.4,x+k,y,x-k*.4,y+k*.7},i%3==0?0xFFDA124F:0xFF030D36);}
 }

 public static double formationDensity(int members){return Math.max(1,Math.ceil((members-1)/2.)*.475);}
 public static void background(HudDesign.Canvas c,int w,int h,double t){
  c.fill(0,0,w,h,0xFF02092C);
  BattleGraphics.poly(c,new double[]{0,0,w*.3,0,w,h*.22,w,h*.43},0xFF09277B);
  // Perspective checkerboard, matching the blue arena used for the party rush.
  for(int row=0;row<13;row++){
   double d0=Math.pow(row/13.,2),d1=Math.pow((row+1)/13.,2),y0=h*(.35+.65*d0),y1=h*(.35+.65*d1);
   for(int col=-15;col<15;col++){
    double x0=w*.5+col*w*(.012+.16*d0),x1=w*.5+(col+1)*w*(.012+.16*d0),x2=w*.5+(col+1)*w*(.012+.16*d1),x3=w*.5+col*w*(.012+.16*d1);
    BattleGraphics.poly(c,new double[]{x0,y0,x1,y0,x2,y1,x3,y1},((col+row)&1)==0?0xFF1766AE:0xFF051445);
   }
  }
  for(int i=0;i<22;i++){double x=(i*79.0+t*2.4)%w,y=(i*47.0)%h;BattleGraphics.poly(c,new double[]{x,y-5,x+4,y,x,y+5,x-2,y},0xFF61DAFF);}
 }

 public static void cloud(HudDesign.Canvas c,int w,int h,double tick,double fade){
  if(fade<=0)return;int alpha=(int)(fade*245);double radius=Math.min(w*.27,h*.40);
  for(int i=0;i<24;i++){double a=i*2.399+tick*.035,x=w*.5+Math.cos(a)*radius*(i%5)/5.,y=h*.53+Math.sin(a)*radius*.55*(i%5)/5.;int shade=170+i%5*17;double size=radius*(.24+(i%3)*.045);double[] xy=new double[24];for(int n=0;n<12;n++){xy[n*2]=x+Math.cos(n*Math.PI/6)*size;xy[n*2+1]=y+Math.sin(n*Math.PI/6)*size;}BattleGraphics.poly(c,xy,(alpha<<24)|(shade<<16)|((shade+4)<<8)|Math.min(255,shade+8));}
  for(int i=0;i<7;i++){double a=i*.897+tick*.23,x=w*.5+Math.cos(a)*radius*.85,y=h*.5+Math.sin(a)*radius*.5;double s=radius*(i%2==0?.35:.2);BattleGraphics.poly(c,new double[]{x-s,y+3,x+s,y-3,x+2,y+s,x-2,y-s},(alpha<<24)|0xA6F9FF);}
 }
}
