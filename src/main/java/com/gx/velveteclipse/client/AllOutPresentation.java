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
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){if(e.phase==TickEvent.Phase.END&&cue!=null){if(Minecraft.m_91087_().f_91073_==null||++ticks>AllOutTimeline.duration(cue.victory())+20)reset();}}
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
  if(phase==AllOutTimeline.Phase.LINEUP||phase==AllOutTimeline.Phase.RUSH){
   int side=0;double progress=phase==AllOutTimeline.Phase.RUSH?AllOutTimeline.ease((t-38)/24):0;
   for(var p:party){boolean main=p.m_19879_()==cue.leader();int slot=main?0:++side;double offset=slot==0?0:((slot+1)/2)*(slot%2==0?-1:1);
    double density=formationDensity(party.size()),x=w*.5+offset*w*.19/density,y=h*(main?.45:.42);float size=(float)(h*(main?.35:.28)/Math.sqrt(density));
    x+=(w*.5-x)*progress;y+=(h*.43-y)*progress;size*=1-progress*.82;
    AllOutPlayerRenderer.draw(g,p,x,y,size,t,cue.victory(),(float)(progress*3.0-(main?.14:offset*.22)),false);
   }
  }else if((phase==AllOutTimeline.Phase.FINISH||phase==AllOutTimeline.Phase.VICTORY)&&leader instanceof AbstractClientPlayer player){
   if(phase==AllOutTimeline.Phase.VICTORY){BattleGraphics.poly(c,new double[]{0,h*.12,w,0,w,h*.74,0,h},0xFF05A9DE);c.angled("VICTORY",(int)(w*.04),(int)(h*.52),(float)(w/130.),0xFF086AAB,-9);}
   AllOutPlayerRenderer.draw(g,player,w*(phase==AllOutTimeline.Phase.VICTORY?.62:.48),h*(phase==AllOutTimeline.Phase.VICTORY?.46:.42),h*(phase==AllOutTimeline.Phase.VICTORY?.58f:.37f),t,true,-.30f,phase==AllOutTimeline.Phase.VICTORY);
  }
  if(phase==AllOutTimeline.Phase.CLOUD||phase==AllOutTimeline.Phase.FINISH){g.m_280262_();g.m_280168_().m_85836_();g.m_280168_().m_85837_(0,0,300);cloud(c,w,h,t,phase==AllOutTimeline.Phase.FINISH?Math.max(0,(120-t)/30):1);g.m_280262_();g.m_280168_().m_85849_();}
  if(phase==AllOutTimeline.Phase.LINEUP)c.angled("ALL-OUT ATTACK",(int)(w*.07),(int)(h*.09),(float)(w/285.),0xFFF6FFFF,-8);
  if(phase==AllOutTimeline.Phase.VICTORY)c.styled("THAT'S ALL!",(int)(w*.06),(int)(h*.84),(float)(w/300.),0xFFF6FFFF);
 }
 public static double formationDensity(int members){return Math.max(1,Math.ceil((members-1)/2.)*.475);}
 public static void background(HudDesign.Canvas c,int w,int h,double t){
  c.fill(0,0,w,h,0xFF030C36);BattleGraphics.poly(c,new double[]{0,0,w*.4,0,w,h*.35,w,h*.8},0xFF103B99);
  for(int i=0;i<18;i++){double x=(i/17.)*w;BattleGraphics.poly(c,new double[]{w*.5,h*.37,x-1,h,x+1,h},0xFF164EAB);}
  for(int i=0;i<8;i++){double y=h*(.4+Math.pow(i/7.,2)*.6);c.fill(0,(int)y,w,1,0xFF2265B0);}
  for(int i=0;i<18;i++){double x=(i*79.0+t*1.6)%w,y=(i*47.0)%h;BattleGraphics.poly(c,new double[]{x,y-4,x+3,y,x,y+4,x-2,y},0xFF4CB9F5);}
 }
 public static void cloud(HudDesign.Canvas c,int w,int h,double tick,double fade){
  if(fade<=0)return;int alpha=(int)(fade*245);double radius=Math.min(w*.27,h*.40);
  for(int i=0;i<24;i++){double a=i*2.399+tick*.035,x=w*.5+Math.cos(a)*radius*(i%5)/5.,y=h*.53+Math.sin(a)*radius*.55*(i%5)/5.;int shade=170+i%5*17;double size=radius*(.24+(i%3)*.045);double[] xy=new double[24];for(int n=0;n<12;n++){xy[n*2]=x+Math.cos(n*Math.PI/6)*size;xy[n*2+1]=y+Math.sin(n*Math.PI/6)*size;}BattleGraphics.poly(c,xy,(alpha<<24)|(shade<<16)|((shade+4)<<8)|Math.min(255,shade+8));}
  for(int i=0;i<7;i++){double a=i*.897+tick*.23,x=w*.5+Math.cos(a)*radius*.85,y=h*.5+Math.sin(a)*radius*.5;double s=radius*(i%2==0?.35:.2);BattleGraphics.poly(c,new double[]{x-s,y+3,x+s,y-3,x+2,y+s,x-2,y-s},(alpha<<24)|0xA6F9FF);}
 }
}
