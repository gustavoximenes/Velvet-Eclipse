package com.gx.velveteclipse.forge;

import com.gx.velveteclipse.util.RuntimeBridge;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class VisualBattleActors {
   private static final Map<UUID, Object> ENEMY = new HashMap<>();
   private static final Map<UUID, Object> PERSONA = new HashMap<>();
   private static final Map<UUID, String> ENEMY_MODEL = new HashMap<>();

   private VisualBattleActors() {
   }

   public static void spawnBattle(Object var0, String var1, String var2) {
      clear(var0);
      UUID var3 = RuntimeBridge.playerUuid(var0);
      ENEMY_MODEL.put(var3, var2);
      double[] var4 = RuntimeBridge.playerPosition(var0);
      float var5 = RuntimeBridge.playerYaw(var0);
      double[] var6 = RuntimeBridge.localOffset(var4, var5, 0.0, 0.05, 5.25);
      double[] var7 = RuntimeBridge.localOffset(var4, var5, -0.55, -3.25, -0.65);
      Object var8 = spawnEnemy(var0, var6, var5 + 180.0F);
      Object var9 = spawnPersona(var0, var1, var7, var5);
      if (var8 != null) {
         ENEMY.put(var3, var8);
      }

      if (var9 != null) {
         PERSONA.put(var3, var9);
      }
   }

   public static void replacePersona(Object var0, String var1) {
      UUID var2 = RuntimeBridge.playerUuid(var0);
      RuntimeBridge.discardObjectEntity(PERSONA.remove(var2));
      double[] var3 = RuntimeBridge.playerPosition(var0);
      float var4 = RuntimeBridge.playerYaw(var0);
      double[] var5 = RuntimeBridge.localOffset(var3, var4, -0.55, -3.25, -0.65);
      Object var6 = spawnPersona(var0, var1, var5, var4);
      if (var6 != null) {
         PERSONA.put(var2, var6);
      }
   }

   public static void pulsePersona(Object var0, String var1) {
      if (!PersonaCinematicHooks.shouldSuppressPulse(var0, var1)) {
         UUID var2 = RuntimeBridge.playerUuid(var0);
         Object var3 = PERSONA.get(var2);
         if (var3 == null || RuntimeBridge.isEntityRemoved(var3)) {
            replacePersona(var0, var1);
            var3 = PERSONA.get(var2);
         }

         if (var3 != null) {
            double[] var4 = RuntimeBridge.playerPosition(var0);
            float var5 = RuntimeBridge.playerYaw(var0);
            double[] var6 = RuntimeBridge.localOffset(var4, var5, -0.15, 0.62, 2.2);
            RuntimeBridge.setEntityPosition(var3, var6[0], var6[1], var6[2]);
            RuntimeBridge.setEntityRotation(var3, var5, 0.0F);
         }
      }
   }

   public static void settlePersona(Object var0, String var1) {
      if (!PersonaCinematicServer.suppressLegacySettle(var0)) {
         UUID var2 = RuntimeBridge.playerUuid(var0);
         Object var3 = PERSONA.get(var2);
         if (var3 != null && !RuntimeBridge.isEntityRemoved(var3)) {
            hidePersona(var0, var3);
         } else {
            replacePersona(var0, var1);
         }
      }
   }

   public static void pulseEnemy(Object var0) {
      Object var1 = ENEMY.get(RuntimeBridge.playerUuid(var0));
      if (var1 != null && !RuntimeBridge.isEntityRemoved(var1)) {
         double[] var2 = RuntimeBridge.playerPosition(var0);
         float var3 = RuntimeBridge.playerYaw(var0);
         double[] var4 = RuntimeBridge.localOffset(var2, var3, 0.0, 0.05, 3.45);
         RuntimeBridge.setEntityPosition(var1, var4[0], var4[1], var4[2]);
         RuntimeBridge.setEntityRotation(var1, var3 + 180.0F, 0.0F);
      }
   }

   public static void settleEnemy(Object var0) {
      Object var1 = ENEMY.get(RuntimeBridge.playerUuid(var0));
      if (var1 != null && !RuntimeBridge.isEntityRemoved(var1)) {
         double[] var2 = RuntimeBridge.playerPosition(var0);
         float var3 = RuntimeBridge.playerYaw(var0);
         double[] var4 = RuntimeBridge.localOffset(var2, var3, 0.0, 0.05, 5.25);
         RuntimeBridge.setEntityPosition(var1, var4[0], var4[1], var4[2]);
         RuntimeBridge.setEntityRotation(var1, var3 + 180.0F, 0.0F);
      }
   }

   public static void clear(Object var0) {
      if (var0 != null) {
         UUID var1 = RuntimeBridge.playerUuid(var0);
         RuntimeBridge.discardObjectEntity(ENEMY.remove(var1));
         RuntimeBridge.discardObjectEntity(PERSONA.remove(var1));
         ENEMY_MODEL.remove(var1);
         PersonaCinematicServer.clear(var0);
      }
   }

   public static Object enemyActor(Object var0) {
      return var0 == null ? null : ENEMY.get(RuntimeBridge.playerUuid(var0));
   }

   public static Object personaActor(Object var0) {
      return var0 == null ? null : PERSONA.get(RuntimeBridge.playerUuid(var0));
   }

   private static void hidePersona(Object var0, Object var1) {
      double[] var2 = RuntimeBridge.playerPosition(var0);
      float var3 = RuntimeBridge.playerYaw(var0);
      double[] var4 = RuntimeBridge.localOffset(var2, var3, -0.55, -3.25, -0.65);
      RuntimeBridge.setEntityPosition(var1, var4[0], var4[1], var4[2]);
      RuntimeBridge.setEntityRotation(var1, var3, 0.0F);
   }

   private static Object spawnEnemy(Object var0, double[] var1, float var2) {
      try {
         Object var3 = invokeStatic("com.gx.velveteclipse.forge.ShadowRegistry", "shadowType", new Class[0], new Object[0]);
         Object var4 = RuntimeBridge.createEntityFromType(var0, var3, var1[0], var1[1], var1[2]);
         if (var4 != null) {
            RuntimeBridge.configureBattleActor(var4, false);
            RuntimeBridge.setEntityRotation(var4, var2, 0.0F);
         }

         return var4;
      } catch (Throwable var5) {
         return null;
      }
   }

   private static Object spawnPersona(Object var0, String var1, double[] var2, float var3) {
      try {
         Object var4 = invokeStatic("com.gx.velveteclipse.forge.PersonaMobRegistry", "typeFor", new Class[]{String.class}, new Object[]{var1});
         Object var5 = RuntimeBridge.createEntityFromType(var0, var4, var2[0], var2[1], var2[2]);
         if (var5 != null) {
            invokeStatic(
               "com.gx.velveteclipse.forge.PersonaMobRegistry", "configurePersonaMob", new Class[]{Object.class, String.class}, new Object[]{var5, var1}
            );
            RuntimeBridge.configureBattleActor(var5, true);
            RuntimeBridge.setEntityRotation(var5, var3, 0.0F);
         }

         return var5;
      } catch (Throwable var6) {
         return null;
      }
   }

   private static Object invokeStatic(String var0, String var1, Class<?>[] var2, Object[] var3) throws Exception {
      Class var4 = Class.forName(var0);
      Method var5 = var4.getMethod(var1, var2);
      return var5.invoke(null, var3);
   }
}
