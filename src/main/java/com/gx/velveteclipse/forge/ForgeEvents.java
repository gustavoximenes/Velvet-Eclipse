package com.gx.velveteclipse.forge;

import com.gx.velveteclipse.VelvetEclipseMod;
import com.gx.velveteclipse.util.RuntimeBridge;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.Clone;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(
   modid = "velvet_eclipse",
   bus = Bus.FORGE
)
public final class ForgeEvents {
   private ForgeEvents() {
   }

   @SubscribeEvent
   public static void onRegisterCommands(RegisterCommandsEvent var0) {
      try {
         VelvetEclipseMod.LOGGER.info("Velvet Eclipse: registering /velvet command tree...");
         VelvetCommands.register(var0.getDispatcher());
         VelvetEclipseMod.LOGGER.info("Velvet Eclipse: /velvet command tree registered.");
      } catch (Throwable var2) {
         VelvetEclipseMod.LOGGER.error("Velvet Eclipse: command registration FAILED: " + var2);
      }
   }

   @SubscribeEvent
   public static void onClone(Clone var0) {
      PlayerPersonaData.copyPersisted(var0.getOriginal(), var0.getEntity());
   }

   @SubscribeEvent
   public static void onLogin(PlayerLoggedInEvent var0) {
      Player var1 = var0.getEntity();
      PlayerPersonaData var2 = PlayerPersonaData.load(var1);
      var2.save(var1);
      RuntimeBridge.sendMessage(var1, "Velvet Eclipse v0.6.46 carregado. Cidade reconstruída em blocos, interiores conectados e Personas reenquadradas.");
      RuntimeBridge.sendMessage(var1, "§7Shadows surgem durante a Dark Hour. Teste com §f/time set dark_hour§7 e localize a torre com §f/velvet tartarus§7.");
      ShadowWorldService.playerLogin(var1);
      BattleService.sync(var1, "Ready");
   }

   @SubscribeEvent
   public static void onServerTick(ServerTickEvent var0) {
      if (var0.phase == Phase.END) {
         com.gx.velveteclipse.exploration.ExplorationService.tickServer();
         BattleService.tickServer();
         ShadowWorldService.tickServer();
      }
   }

   @SubscribeEvent
   public static void onLogout(PlayerLoggedOutEvent var0) {
      Player var1 = var0.getEntity();
      ShadowWorldService.playerLogout(var1);
      if (BattleService.current(var1) != null) {
         BattleService.stop(var1);
      }
   }
}
