package com.gx.velveteclipse.network;

import com.gx.velveteclipse.client.ClientBattleState;
import com.gx.velveteclipse.util.RuntimeBridge;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public record BattleSnapshotS2C(
   boolean active,
   boolean playerTurn,
   String personaId,
   String personaName,
   int hp,
   int maxHp,
   int sp,
   int maxSp,
   String enemyName,
   int enemyHp,
   int enemyMaxHp,
   int pulseTonics,
   List<String> skills,
   List<String> roster,
   String message,
   List<String> passives
) {
   private static final String SEP = "\u001f";

   public BattleSnapshotS2C(
      boolean active,
      boolean playerTurn,
      String personaId,
      String personaName,
      int hp,
      int maxHp,
      int sp,
      int maxSp,
      String enemyName,
      int enemyHp,
      int enemyMaxHp,
      int pulseTonics,
      List<String> skills,
      List<String> roster,
      String message
   ) {
      this(active,playerTurn,personaId,personaName,hp,maxHp,sp,maxSp,enemyName,enemyHp,enemyMaxHp,pulseTonics,skills,roster,message,List.of());
   }
   public BattleSnapshotS2C {
      skills=skills==null?List.of():List.copyOf(skills);roster=roster==null?List.of():List.copyOf(roster);passives=passives==null?List.of():List.copyOf(passives);
      personaId=safe(personaId);personaName=safe(personaName);enemyName=safe(enemyName);message=safe(message);
   }

   public static BattleSnapshotS2C empty() {
      return new BattleSnapshotS2C(false, false, "", "Sem Persona", 120, 120, 45, 45, "", 0, 0, 3, List.of(), List.of(), "");
   }

   public static void encode(BattleSnapshotS2C var0, FriendlyByteBuf var1) {
      RuntimeBridge.writeUtf(var1, var0.toPayload());
   }

   public static BattleSnapshotS2C decode(FriendlyByteBuf var0) {
      return fromPayload(RuntimeBridge.readUtf(var0));
   }

   public static void handle(BattleSnapshotS2C var0, Supplier<Context> var1) {
      Context var2 = (Context)var1.get();
      var2.enqueueWork(() -> ClientBattleState.accept(var0));
      var2.setPacketHandled(true);
   }

   public String toPayload() {
      return String.join(
         "\u001f",
         this.active ? "1" : "0",
         this.playerTurn ? "1" : "0",
         this.personaId,
         this.personaName,
         Integer.toString(this.hp),
         Integer.toString(this.maxHp),
         Integer.toString(this.sp),
         Integer.toString(this.maxSp),
         this.enemyName,
         Integer.toString(this.enemyHp),
         Integer.toString(this.enemyMaxHp),
         Integer.toString(this.pulseTonics),
         String.join(",", this.skills),
         String.join(",", this.roster),
         this.message.replace("\u001f", " "),
         String.join(",",this.passives)
      );
   }

   public static BattleSnapshotS2C fromPayload(String var0) {
      try {
         String[] var1 = var0.split("\u001f", -1);
         if (var1.length < 15) {
            return empty();
         } else {
            List var2 = var1[12].isBlank() ? List.of() : Arrays.asList(var1[12].split(","));
            List var3 = var1[13].isBlank() ? List.of() : Arrays.asList(var1[13].split(","));
            return new BattleSnapshotS2C(
               "1".equals(var1[0]),
               "1".equals(var1[1]),
               var1[2],
               var1[3],
               Integer.parseInt(var1[4]),
               Integer.parseInt(var1[5]),
               Integer.parseInt(var1[6]),
               Integer.parseInt(var1[7]),
               var1[8],
               Integer.parseInt(var1[9]),
               Integer.parseInt(var1[10]),
               Integer.parseInt(var1[11]),
               var2,
               var3,
               var1[14],
               var1.length>15&&!var1[15].isBlank()?Arrays.asList(var1[15].split(",")):List.of()
            );
         }
      } catch (Throwable var4) {
         return empty();
      }
   }

   private static String safe(String var0) {
      return var0 == null ? "" : var0.replace("\u001f", " ");
   }
}
