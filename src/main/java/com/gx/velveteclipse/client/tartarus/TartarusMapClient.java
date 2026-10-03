package com.gx.velveteclipse.client.tartarus;
import com.gx.velveteclipse.darkhour.*;import com.gx.velveteclipse.client.*;import com.gx.velveteclipse.client.menu.MenuCanvas;
import net.minecraft.client.*;import net.minecraft.client.gui.GuiGraphics;import net.minecraft.client.gui.screens.Screen;import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;import net.minecraftforge.fml.common.Mod;import net.minecraftforge.eventbus.api.SubscribeEvent;import net.minecraftforge.event.TickEvent;import net.minecraftforge.client.event.*;
@Mod.EventBusSubscriber(modid="velvet_eclipse",value=Dist.CLIENT)
public final class TartarusMapClient {
 public static final KeyMapping MAP=new KeyMapping("key.velvet_eclipse.tartarus_map",com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM,77,ClientKeyMappings.CATEGORY);
 private static TartarusMapNetwork.View view=TartarusMapNetwork.View.empty();private static final PausableCountdown countdown=new PausableCountdown();
 public static void accept(TartarusMapNetwork.View v){view=v;}
 public static void clock(int milliseconds){countdown.synchronize(milliseconds,System.nanoTime(),Minecraft.m_91087_().m_91104_());}
 private static int remaining(){return countdown.remaining(System.nanoTime(),Minecraft.m_91087_().m_91104_());}
 public static double clockHeight(){return remaining()>0&&!ClientBattleState.snapshot().active()?47:0;}
 public static boolean deadlineVisible(){return remaining()>0;}
 private static boolean available(){var m=Minecraft.m_91087_();return m.f_91074_!=null&&TartarusDungeonService.inside(m.f_91074_)&&view.floor()==TartarusDungeonService.floor(m.f_91074_)&&!ClientBattleState.snapshot().active()&&!AllOutPresentation.active()&&!com.gx.velveteclipse.client.exploration.ExplorationClient.blocksMenu();}
 @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e){view=TartarusMapNetwork.View.empty();clock(0);}
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){if(e.phase!=TickEvent.Phase.END)return;remaining();var m=Minecraft.m_91087_();while(MAP.m_90859_())if(m.f_91080_==null&&available())m.m_91152_(new Expanded());}
 @SubscribeEvent public static void hud(RenderGuiEvent.Post e){var m=Minecraft.m_91087_();if(m.f_91074_==null)return;int w=m.m_91268_().m_85445_(),h=m.m_91268_().m_85446_();var g=e.getGuiGraphics();var c=new MenuCanvas(g);g.m_286007_(() -> {
  int ms=remaining();if(ms>0){double x=com.gx.velveteclipse.client.darkhour.DeadlineLayout.x(w,ClientBattleState.snapshot().active());int sec=(int)((ms+999L)/1000);c.poly(new double[]{x,5,x+132,5,x+126,46,x-8,46},0xEB04221C);c.fill(x,12,2,26,0xFF73FF57);c.text("DARK HOUR",x+8,10,11,0xFF98FF58,0);c.text(String.format("%02d:%02d",sec/60,sec%60),x+8,23,22,ms<60000?0xFFFF5673:0xFFF0FFD7,0);}
  if(available()&&m.f_91080_==null){double size=Math.min(100,h*.25),y=18,x=w-size-20;TartarusMapDesign.paint(c,view,x,y,size,m.f_91074_.m_20185_()-TartarusInteriorLayout.origin(view.floor()),m.f_91074_.m_20189_(),m.f_91074_.m_146908_(),false);c.text(ClientKeyMappings.label(MAP)+"  MAPA",x,y+size+36,9,0xFFCDEAFF,0);}
 });}
 public static final class Expanded extends Screen {
  Expanded(){super(Component.m_237113_("Mapa do Tártaro"));}
  @Override public boolean m_7043_(){return false;}
  @Override public void m_86600_(){if(!available())m_7379_();}
  @Override public boolean m_7933_(int key,int scan,int mods){if(MAP.m_90832_(key,scan)){m_7379_();return true;}return super.m_7933_(key,scan,mods);}
  @Override public void m_88315_(GuiGraphics g,int mx,int my,float partial){var p=Minecraft.m_91087_().f_91074_;if(p==null||!available())return;g.m_280509_(0,0,f_96543_,f_96544_,0xEC030D24);double size=Math.min(f_96544_-110,f_96543_*.6),x=(f_96543_-size)/2,y=40;var c=new MenuCanvas(g);g.m_286007_(() -> {
   c.text("TARTARUS / MAP",18,12,22,0xFF78F5FF,-3);TartarusMapDesign.paint(c,view,x,y,size,p.m_20185_()-TartarusInteriorLayout.origin(view.floor()),p.m_20189_(),p.m_146908_(),true);
   c.text("Shadow • Escada • Tesouro • Retorno • Terminal • Velvet • Monad • Portal • Bloqueio",18,f_96544_-27,8,0xFFCBE9FF,0);c.text(ClientKeyMappings.label(MAP)+" / ESC  Fechar • Explore para revelar os setores e acessos",18,f_96544_-14,9,0xFF78F5FF,0);
  });}
 }
 @Mod.EventBusSubscriber(modid="velvet_eclipse",bus=Mod.EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
 public static final class Registration{@SubscribeEvent public static void keys(RegisterKeyMappingsEvent e){e.register(MAP);}}
}
