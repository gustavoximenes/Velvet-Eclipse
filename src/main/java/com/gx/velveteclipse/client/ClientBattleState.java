package com.gx.velveteclipse.client;

import com.gx.velveteclipse.network.BattleSnapshotS2C;

public final class ClientBattleState {
   private static volatile BattleSnapshotS2C snapshot = BattleSnapshotS2C.empty();

   private ClientBattleState() {
   }

   public static BattleSnapshotS2C snapshot() {
      return snapshot;
   }

   public static void accept(BattleSnapshotS2C var0) {
      if (var0 != null) {
         BattleSnapshotS2C var1 = snapshot;
         snapshot = var0;
         if(!var0.active())BattleResultsClient.reset();
         else {var mc=net.minecraft.client.Minecraft.m_91087_();if(mc.f_91080_!=null&&!(mc.f_91080_ instanceof net.minecraft.client.gui.screens.PauseScreen))mc.m_91152_(null);}
         ClientBattleUiState.onSnapshot(var1, var0);
      }
   }
}
