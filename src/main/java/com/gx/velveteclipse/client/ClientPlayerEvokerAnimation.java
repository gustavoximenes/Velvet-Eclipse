package com.gx.velveteclipse.client;
import com.gx.velveteclipse.client.presentation.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
@Mod.EventBusSubscriber(modid="velvet_eclipse",value=Dist.CLIENT)
public final class ClientPlayerEvokerAnimation {
 @SubscribeEvent public static void render(RenderPlayerEvent.Pre e){
  EvokerHandLayer.attach(e.getRenderer());var player=e.getEntity();
  if(player instanceof net.minecraft.client.player.AbstractClientPlayer p){
   com.gx.velveteclipse.client.exploration.ExplorationAnimation.orient(p);
   CombatActorAnimation.attach(p);
   if(BattleDirector.summoning(p.m_20148_())&&BattleDirector.age(0)<=60)EvokerPlayerAnimation.play(p);else EvokerPlayerAnimation.stop(p);
  }
 }
}
