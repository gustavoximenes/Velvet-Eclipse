package com.gx.velveteclipse.client.shuffle;
import com.gx.velveteclipse.client.*;
import com.gx.velveteclipse.client.menu.*;
import com.gx.velveteclipse.menu.MenuNetwork;
import com.gx.velveteclipse.shuffle.*;
import net.minecraft.client.*;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.TickEvent;
public final class ShuffleClient {
 public static int count;public static long yen;private static long gainedUntil;
 public static final KeyMapping OPEN=new KeyMapping("key.velvet_eclipse.shuffle",KeyConflictContext.IN_GAME,InputConstants.Type.KEYSYM,80,"key.categories.velvet_eclipse");
 public static void accept(String json){var d=MenuNetwork.JSON.fromJson(json,ShuffleSnapshot.class);if(d==null)return;if(d.count()>count)gainedUntil=System.nanoTime()+3_000_000_000L;count=d.count();yen=d.yen();var mc=Minecraft.m_91087_();
  if(d.screen().equals("shuffle")||d.screen().equals("skill")){if(mc.f_91074_!=null&&!ClientBattleState.snapshot().active())mc.m_91152_(new ShuffleScreen(d));}
  else if(mc.f_91080_ instanceof ShuffleScreen s&&!d.screen().equals("status"))s.receive(d);
 }
 @Mod.EventBusSubscriber(modid="velvet_eclipse",bus=Mod.EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
 public static class Register {@SubscribeEvent public static void keys(RegisterKeyMappingsEvent e){e.register(OPEN);}}
 @Mod.EventBusSubscriber(modid="velvet_eclipse",bus=Mod.EventBusSubscriber.Bus.FORGE,value=Dist.CLIENT)
 public static class Events {
  @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){if(e.phase!=TickEvent.Phase.END)return;var mc=Minecraft.m_91087_();if(mc.f_91074_==null){count=0;yen=0;}
   while(OPEN.m_90859_())if(mc.f_91074_!=null&&mc.f_91080_==null&&!ClientBattleState.snapshot().active()&&!AllOutPresentation.active()&&!com.gx.velveteclipse.client.exploration.ExplorationClient.blocksMenu())ShuffleNetwork.request("open","","");
  }
  @SubscribeEvent public static void hud(RenderGuiEvent.Post e){if(count<=0||!ExplorationHud.visible())return;ExplorationHud.draw(e.getGuiGraphics(),(c,l)->ExplorationHudDesign.shuffle(c,l,count,ClientKeyMappings.label(OPEN),System.nanoTime()<gainedUntil));}

 }
}
