package com.gx.velveteclipse.client.tartarus;
import com.gx.velveteclipse.darkhour.*;
import com.gx.velveteclipse.client.*;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.*;
public final class TerminalClient {
 private static boolean available(){var m=Minecraft.m_91087_();return m.f_91074_!=null&&m.f_91080_==null&&!ClientBattleState.snapshot().active()&&!AllOutPresentation.active()&&!com.gx.velveteclipse.client.exploration.ExplorationClient.blocksMenu();}
 private static TartarusPropEntity nearest(){var p=Minecraft.m_91087_().f_91074_;if(p==null)return null;return p.m_9236_().m_45976_(TartarusPropEntity.class,p.m_20191_().m_82400_(5)).stream().filter(e->e.m_20275_(p.m_20185_(),p.m_20186_(),p.m_20189_())<=25).min(java.util.Comparator.comparingDouble(e->e.m_20275_(p.m_20185_(),p.m_20186_(),p.m_20189_()))).orElse(null);}
 public static void open(TartarusTerminalNetwork.Response r){if(available()&&!r.floors().isEmpty())Minecraft.m_91087_().m_91152_(new TerminalScreen(r));}
 @Mod.EventBusSubscriber(modid="velvet_eclipse",bus=Mod.EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
 public static class Registration {
  @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){for(String name:new String[]{"terminal","treasure","monad"})e.registerEntityRenderer(TartarusPropRegistry.type(name),c->new TartarusPropRenderer(c,name));}
 }
 @Mod.EventBusSubscriber(modid="velvet_eclipse",value=Dist.CLIENT)
 public static class Events {
  @SubscribeEvent public static void key(InputEvent.Key e){if(e.getAction()==1&&(e.getKey()==257||e.getKey()==335)&&available()){var entity=nearest();if(entity!=null)TartarusTerminalNetwork.request(entity.m_19879_(),-1);}}
  @SubscribeEvent public static void hud(RenderGuiEvent.Post e){if(!available())return;var entity=nearest();if(entity==null)return;var mc=Minecraft.m_91087_();var g=e.getGuiGraphics();int w=mc.m_91268_().m_85445_(),h=mc.m_91268_().m_85446_();String label=entity.m_6095_()==TartarusPropRegistry.TERMINAL.get()?"ENTER  Selecionar destino":entity.m_6095_()==TartarusPropRegistry.TREASURE.get()?"ENTER  Abrir tesouro":"ENTER  Porta de Monad";int size=mc.f_91062_.m_92895_(label);g.m_280509_(w/2-size/2-12,h-57,w/2+size/2+12,h-32,0xED052919);g.m_280509_(w/2-size/2-12,h-57,w/2-size/2-9,h-32,0xFF66FF9B);g.m_280056_(mc.f_91062_,label,w/2-size/2,h-49,0xFFF0FFF3,false);}
 }
}
