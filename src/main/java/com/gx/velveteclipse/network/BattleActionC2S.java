package com.gx.velveteclipse.network;

import com.gx.velveteclipse.VelvetEclipseMod;
import com.gx.velveteclipse.forge.BattleService;
import com.gx.velveteclipse.util.RuntimeBridge;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;

public record BattleActionC2S(String action, String argument,java.util.UUID target) {
   public BattleActionC2S(String action,String argument){this(action,argument,null);}
   public BattleActionC2S(String action, String argument,java.util.UUID target) {
      action = action == null ? "" : action;
      argument = argument == null ? "" : argument;
      this.action = action;
      this.argument = argument;this.target=target;
   }

   public static void encode(BattleActionC2S var0, FriendlyByteBuf var1) {
      RuntimeBridge.writeUtf(var1, var0.action);
      RuntimeBridge.writeUtf(var1, var0.argument);var1.writeBoolean(var0.target!=null);if(var0.target!=null)var1.m_130077_(var0.target);
   }

   public static BattleActionC2S decode(FriendlyByteBuf var0) {
      return new BattleActionC2S(RuntimeBridge.readUtf(var0), RuntimeBridge.readUtf(var0),var0.readBoolean()?var0.m_130259_():null);
   }

   public static void handle(BattleActionC2S var0, Supplier<Context> var1) {
      Context var2 = (Context)var1.get();
      ServerPlayer var3 = var2.getSender();
      if (var3 != null) {
         var2.enqueueWork(() -> {
            try {
               VelvetEclipseMod.LOGGER.info("Velvet Eclipse C2S action: " + var0.action + (var0.argument.isBlank() ? "" : " / " + var0.argument));
               BattleService.handleNetworkAction(var3, var0.action, var0.argument,var0.target);
            } catch (Throwable var3x) {
               VelvetEclipseMod.LOGGER.error("Velvet Eclipse C2S handler failed: " + var3x);
               RuntimeBridge.sendMessage(var3, "§c[Velvet Eclipse] Falha ao processar ação de batalha: " + var3x.getClass().getSimpleName());
               var battle=BattleService.current(var3);if(battle!=null)battle.cancelPrepared();
               com.gx.velveteclipse.forge.PersonaCinematicServer.clear(var3);
               com.gx.velveteclipse.forge.MeleeBattleServer.clear(var3);
               ModNetwork.sendPresentation(var3,new BattleCueS2C("cancel","",false,0,0,0,0,0,0));
               BattleService.sync(var3,"Ação interrompida. Escolha novamente.");
            }
         });
      }

      var2.setPacketHandled(true);
   }
}
