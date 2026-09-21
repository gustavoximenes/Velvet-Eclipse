package com.gx.velveteclipse.client.raid;
import com.gx.velveteclipse.raid.*;import com.gx.velveteclipse.client.menu.*;
import net.minecraft.client.Minecraft;import net.minecraft.client.gui.GuiGraphics;import net.minecraft.client.gui.screens.Screen;import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;import net.minecraftforge.fml.common.Mod;import net.minecraftforge.eventbus.api.SubscribeEvent;import net.minecraftforge.client.event.*;
@Mod.EventBusSubscriber(modid="velvet_eclipse",value=Dist.CLIENT)
public final class RaidClient {
 private static RaidNetwork.View data=RaidNetwork.View.empty();private static long received,changed;private static int sent=-1;
 public static void accept(RaidNetwork.View v){
  if(!v.mode().equals(data.mode())||v.revision()!=data.revision()){changed=System.nanoTime();sent=-1;}data=v;received=System.nanoTime();var m=Minecraft.m_91087_();
  boolean screen=v.mode().equals("lobby")||v.mode().equals("building")||v.mode().equals("dialogue");
  if(screen&&!(m.f_91080_ instanceof RaidScreen))m.m_91152_(new RaidScreen());else if(!screen&&m.f_91080_ instanceof RaidScreen)m.m_91152_(null);
 }
 @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e){data=RaidNetwork.View.empty();sent=-1;}
 @SubscribeEvent public static void screens(ScreenEvent.Opening e){if(data.mode().equals("dialogue")&&e.getNewScreen()!=null&&!(e.getNewScreen() instanceof RaidScreen))e.setCanceled(true);}
 @SubscribeEvent public static void hud(RenderGuiEvent.Post e){
  if(data.mode().isEmpty()||data.mode().equals("lobby")||data.mode().equals("building")||data.mode().equals("dialogue"))return;
  var m=Minecraft.m_91087_();if(m.f_91074_==null)return;var g=e.getGuiGraphics();int w=m.m_91268_().m_85445_();double scale=Math.min(w/900.0,m.m_91268_().m_85446_()/500.0);
  var pose=g.m_280168_();pose.m_85836_();pose.m_85837_(w-310*scale,8,200);pose.m_85841_((float)scale,(float)scale,1);
  try{g.m_286007_(() -> {var c=new MenuCanvas(g);c.poly(new double[]{12,0,304,0,288,85,0,85},0xED051333);c.fill(16,11,3,60,0xFF38E7F0);
   if(data.stage()==2&&!data.weakness().isEmpty()){
    long elapsed=data.mode().equals("battle")?(System.nanoTime()-received)/1_000_000:0;long ms=Math.max(0,data.milliseconds()-elapsed);long seconds=(ms+999)/1000;
    c.text("COLISÃO EM",28,9,13,0xFF72EEF3,0);c.text(String.format("%02d:%02d",seconds/60,seconds%60),28,24,33,ms<60000?0xFFFF5173:0xFFF3FFFF,0);
    c.text("FRAQUEZA  "+element(data.weakness()),29,65,12,0xFFBDE9E8,0);
   }else{c.text("OPERAÇÃO MONORAIL",28,12,19,0xFF72EEF3,0);c.text("SUBCHEFES  "+Math.min(2,data.stage())+" / 2",28,43,18,0xFFF3FFFF,0);}
  });}finally{g.m_280262_();pose.m_85849_();}
  if(data.line().startsWith("ACELERAÇÃO")&&System.nanoTime()-changed<4_500_000_000L){var c=new MenuCanvas(g);g.m_286007_(() -> c.text(data.line(),14,22,12,0xFFFF647C,0));}
 }
 private static String element(String s){return switch(s){case "UNKNOWN"->"?";case "FIRE"->"FOGO";case "ICE"->"GELO";case "WIND"->"VENTO";case "ELECTRIC"->"ELÉTRICO";case "LIGHT"->"LUZ";case "DARK"->"TREVAS";case "PSY"->"PSÍQUICO";default->s;};}
 private static void next(){if(sent==data.revision()||System.nanoTime()-changed<400_000_000L)return;sent=data.revision();RaidNetwork.request("next",sent);}
 public static final class RaidScreen extends Screen{
  RaidScreen(){super(Component.m_237113_("Operação Monorail"));}
  @Override public boolean m_7043_(){return false;}
  @Override public boolean m_6913_(){return data.mode().equals("lobby");}
  @Override public void m_7379_(){if(data.mode().equals("lobby")){RaidNetwork.request("leave",data.revision());super.m_7379_();}}
  @Override public boolean m_7933_(int key,int sc,int mods){if(key==257||key==335){if(data.mode().equals("dialogue"))next();else if(data.mode().equals("lobby")&&data.leader())RaidNetwork.request("start",data.revision());return true;}if(key==256){m_7379_();return true;}return true;}
  double scale(){return Math.min(f_96543_/900.0,f_96544_/500.0);}double left(){return (f_96543_-900*scale())/2;}double top(){return (f_96544_-500*scale())/2;}
  @Override public boolean m_6375_(double x,double y,int button){if(button!=0)return true;double px=(x-left())/scale(),py=(y-top())/scale();if(data.mode().equals("dialogue")){next();return true;}if(data.mode().equals("lobby")){if(py>431&&px>560&&data.leader())RaidNetwork.request("start",data.revision());else if(py>431&&px<210)m_7379_();}return true;}
  @Override public void m_88315_(GuiGraphics g,int mx,int my,float partial){var pose=g.m_280168_();pose.m_85836_();pose.m_85837_(left(),top(),0);pose.m_85841_((float)scale(),(float)scale(),1);
   try{var c=new MenuCanvas(g);g.m_286007_(() -> {if(data.mode().equals("dialogue"))RaidDesign.dialogue(c,data,sent==data.revision());else RaidDesign.lobby(c,data);});
    if(data.mode().equals("dialogue")&&!data.names().isEmpty()){
     var mc=Minecraft.m_91087_();String name=data.names().get(Math.min(data.speaker(),data.names().size()-1));
     if(mc.f_91073_!=null)for(var a:mc.f_91073_.m_6907_())if(a.m_7755_().getString().equals(name)){
      g.m_280411_(a.m_108560_(),70,279,104,104,8,8,8,8,64,64);
      if(a.m_36170_(net.minecraft.world.entity.player.PlayerModelPart.HAT))g.m_280411_(a.m_108560_(),70,279,104,104,40,8,8,8,64,64);
      g.m_280411_(a.m_108560_(),70,383,104,117,20,20,8,9,64,64);
      g.m_280411_(a.m_108560_(),31,383,39,117,44,20,3,9,64,64);
      g.m_280411_(a.m_108560_(),174,383,39,117,36,52,3,9,64,64);break;
     }
    }
   }finally{g.m_280262_();pose.m_85849_();}}

 }
 @Mod.EventBusSubscriber(modid="velvet_eclipse",bus=Mod.EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
 public static class Registration{@SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){e.registerEntityRenderer(RaidRegistry.PRIESTESS.get(),c->new com.gx.velveteclipse.client.render.VelvetGeoEntityRenderer<PriestessEntity>(c,"priestess"));}}
}
