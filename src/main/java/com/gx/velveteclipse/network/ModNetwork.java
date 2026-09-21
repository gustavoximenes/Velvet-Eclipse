package com.gx.velveteclipse.network;

import com.gx.velveteclipse.VelvetEclipseMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetwork {
   private static final String PROTOCOL = "14";
   public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
      new ResourceLocation("velvet_eclipse", "main"), () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals
   );
   private static boolean registered;
   private static int weaknessEvent;

   private ModNetwork() {
   }

   public static synchronized void register() {
      if (!registered) {
         int var0 = 0;
         CHANNEL.registerMessage(var0++, BattleSnapshotS2C.class, BattleSnapshotS2C::encode, BattleSnapshotS2C::decode, BattleSnapshotS2C::handle,java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT));
         CHANNEL.registerMessage(var0++, BattleActionC2S.class, BattleActionC2S::encode, BattleActionC2S::decode, BattleActionC2S::handle,java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_SERVER));
         CHANNEL.registerMessage(var0++, WeaknessHitS2C.class, WeaknessHitS2C::encode, WeaknessHitS2C::decode, WeaknessHitS2C::handle,java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT));
         CHANNEL.registerMessage(var0++,BattleCueS2C.class,BattleCueS2C::encode,BattleCueS2C::decode,BattleCueS2C::handle,java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT));
         CHANNEL.registerMessage(var0++,BattleTargetS2C.class,BattleTargetS2C::encode,BattleTargetS2C::decode,BattleTargetS2C::handle,java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT));
         CHANNEL.registerMessage(var0++,BattleRosterS2C.class,BattleRosterS2C::encode,BattleRosterS2C::decode,BattleRosterS2C::handle,java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT));
         CHANNEL.registerMessage(var0++,BattleSceneS2C.class,BattleSceneS2C::encode,BattleSceneS2C::decode,BattleSceneS2C::handle,java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT));
         CHANNEL.registerMessage(var0++,BattleLightningS2C.class,BattleLightningS2C::encode,BattleLightningS2C::decode,BattleLightningS2C::handle,java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT));
         CHANNEL.registerMessage(var0++,BattleTacticsS2C.class,BattleTacticsS2C::encode,BattleTacticsS2C::decode,BattleTacticsS2C::handle,java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT));
         CHANNEL.registerMessage(var0++,AllOutCueS2C.class,AllOutCueS2C::encode,AllOutCueS2C::decode,AllOutCueS2C::handle,java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT));
         CHANNEL.registerMessage(var0++,DarkHourS2C.class,DarkHourS2C::encode,DarkHourS2C::decode,DarkHourS2C::handle,java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT));
         CHANNEL.registerMessage(var0++,DarkHourViewC2S.class,DarkHourViewC2S::encode,DarkHourViewC2S::decode,DarkHourViewC2S::handle,java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_SERVER));
         CHANNEL.registerMessage(var0++,BattleResultsS2C.class,BattleResultsS2C::encode,BattleResultsS2C::decode,BattleResultsS2C::handle,java.util.Optional.of(net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT));
         registered = true;
         VelvetEclipseMod.LOGGER.info("Velvet Eclipse network channel registered (protocol 11, 13 packets).");
      }
   }

   public static boolean isRegistered() {
      return registered;
   }
   public static void sendPresentation(Object player,Object packet){if(player instanceof ServerPlayer p)CHANNEL.send(PacketDistributor.PLAYER.with(()->p),packet);}
   public static void sendWeaknessHit(Object player){
      if(player instanceof ServerPlayer p)CHANNEL.send(PacketDistributor.PLAYER.with(()->p),new WeaknessHitS2C(++weaknessEvent));
   }

   public static void sendToPlayer(Object var0, BattleSnapshotS2C var1) {
      if (var0 instanceof ServerPlayer var2) {
         CHANNEL.send(PacketDistributor.PLAYER.with(() -> var2), var1);
      }
   }

   public static void sendAction(String type,String value){sendAction(type,value,null);}
   public static void sendAction(String var0, String var1,java.util.UUID target) {
      try {
         CHANNEL.sendToServer(new BattleActionC2S(var0, var1,target));
         VelvetEclipseMod.LOGGER.info("Velvet Eclipse client sent action: " + var0);
      } catch (Throwable var3) {
         VelvetEclipseMod.LOGGER.error("Velvet Eclipse could not send client action '" + var0 + "': " + var3);
      }
   }
}
