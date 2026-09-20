package com.gx.velveteclipse.forge;

import com.gx.velveteclipse.VelvetEclipseMod;
import com.gx.velveteclipse.util.RuntimeBridge;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(
   modid = "velvet_eclipse",
   bus = Bus.FORGE
)
public final class ShadowWorldService {
   private static final Map<UUID, Object> SHADOWS = new HashMap<>();
   private static final Map<UUID, Object> PLAYERS = new HashMap<>();
   private static final Map<UUID, Long> NEXT_SPAWN = new HashMap<>();
   private static long serverTicks;

   private ShadowWorldService() {
   }

   public static void playerLogin(Object var0) {
      if (var0 != null) {
         UUID var1 = RuntimeBridge.playerUuid(var0);
         PLAYERS.put(var1, var0);
         NEXT_SPAWN.put(var1, serverTicks + 60L);
      }
   }

   public static void playerLogout(Object var0) {
      if (var0 != null) {
         UUID var1 = RuntimeBridge.playerUuid(var0);
         PLAYERS.remove(var1);
         NEXT_SPAWN.remove(var1);
      }
   }

   @SubscribeEvent
   public static void onEntityJoin(EntityJoinLevelEvent var0) {
      if (!RuntimeBridge.isClientLevel(var0.getLevel())) {
         Entity var1 = var0.getEntity();
         if (ShadowRegistry.isShadow(var1)) {
            registerShadow(var1);
         }
      }
   }

   @SubscribeEvent
   public static void onEntityLeave(EntityLeaveLevelEvent var0) {
      if (!RuntimeBridge.isClientLevel(var0.getLevel())) {
         Entity var1 = var0.getEntity();
         if (ShadowRegistry.isShadow(var1)) {
            unregisterShadow(var1);
         }
      }
   }

   public static void tickServer() {
      serverTicks++;
      if (serverTicks % 10L == 0L) {
         cleanupRemoved();
         detectEncounters();
      }

      if (serverTicks % 20L == 0L) {
         ensureWorldShadows();
      }
   }

   public static Object spawnNear(Object var0) {
      if (var0 != null && ShadowRegistry.shadowType() != null) {
         double[] var1 = RuntimeBridge.playerPosition(var0);
         double[] var2 = RuntimeBridge.localOffset(var1, RuntimeBridge.playerYaw(var0), 0.0, 0.0, 5.0);
         int var3 = (int)Math.floor(var2[0]);
         int var4 = (int)Math.floor(var2[2]);
         int var5 = RuntimeBridge.surfaceY(var0, var3, var4);
         double var6 = Math.max(var1[1], (double)var5 + 0.05);
         return spawnAt(var0, (double)var3 + 0.5, var6, (double)var4 + 0.5, "manual", false);
      } else {
         return null;
      }
   }

   private static Object spawnNatural(Object var0) {
      if (var0 instanceof Entity e && com.gx.velveteclipse.darkhour.DarkHourService.active(e.m_9236_()) && ShadowRegistry.shadowType() != null) {
         double[] var1 = RuntimeBridge.playerPosition(var0);
         double var2 = ThreadLocalRandom.current().nextDouble(0.0, Math.PI * 2);
         double var4 = ThreadLocalRandom.current().nextDouble(8.0, 13.0);
         int var6 = (int)Math.floor(var1[0] + Math.cos(var2) * var4);
         int var7 = (int)Math.floor(var1[2] + Math.sin(var2) * var4);
         int var8 = RuntimeBridge.surfaceY(var0, var6, var7);
         return spawnAt(var0, (double)var6 + 0.5, (double)var8 + 0.05, (double)var7 + 0.5, "natural", true);
      } else {
         return null;
      }
   }

   private static Object spawnAt(Object var0, double var1, double var3, double var5, String var7, boolean var8) {
      Object var9 = RuntimeBridge.createEntityFromType(var0, ShadowRegistry.shadowType(), var1, var3, var5);
      if (var9 != null) {
         if (var8) {
            addEntityTag(var9, "velvet_encounter_shadow");
         }

         registerShadow(var9);
         VelvetEclipseMod.LOGGER
            .info("Velvet Eclipse: spawned " + var7 + " real Shadow mob at " + String.format(Locale.ROOT, "%.1f %.1f %.1f", var1, var3, var5));
      } else {
         VelvetEclipseMod.LOGGER.error("Velvet Eclipse: failed to create " + var7 + " Shadow mob.");
      }

      return var9;
   }

   public static int shadowCount() {
      return SHADOWS.size();
   }

   public static int nearbyCount(Object var0, double var1) {
      if (var0 == null) {
         return 0;
      } else {
         double var3 = var1 * var1;
         int var5 = 0;

         for (Object var7 : SHADOWS.values()) {
            if (var7 != null
               && !RuntimeBridge.isEntityRemoved(var7)
               && RuntimeBridge.sameWorld(var0, var7)
               && RuntimeBridge.distanceSquared(var0, var7) <= var3) {
               var5++;
            }
         }

         return var5;
      }
   }

   public static Object nearest(Object var0, double var1) {
      if (var0 == null) {
         return null;
      } else {
         double var3 = var1 * var1;
         Object var5 = null;

         for (Object var7 : SHADOWS.values()) {
            if (var7 != null && !RuntimeBridge.isEntityRemoved(var7) && RuntimeBridge.sameWorld(var0, var7)) {
               double var8 = RuntimeBridge.distanceSquared(var0, var7);
               if (var8 <= var3) {
                  var3 = var8;
                  var5 = var7;
               }
            }
         }

         return var5;
      }
   }

   public static boolean startNearestBattle(Object var0) {
      if(com.gx.velveteclipse.room.VelvetRoomService.inside(var0)||com.gx.velveteclipse.exploration.ExplorationService.preparing(var0)) return false;
      Object var1 = nearest(var0, 6.0);
      if (var1 == null || com.gx.velveteclipse.exploration.ExplorationService.reserved(var1)) {
         return false;
      } else {
         startEncounter(var0,var1);
         return true;
      }
   }

   private static void ensureWorldShadows() {
      // Natural encounter Shadows dissolve at dawn. Explicit summons and battle
      // actors stay under their own lifecycle and are never removed here.
      for(Object shadow:new ArrayList<>(SHADOWS.values()))if(shadow instanceof Entity e&&hasEntityTag(shadow,"velvet_encounter_shadow")&&!com.gx.velveteclipse.darkhour.TartarusDungeonService.dungeonShadow(shadow)&&!com.gx.velveteclipse.darkhour.DarkHourService.active(e.m_9236_())&&!com.gx.velveteclipse.exploration.ExplorationService.reserved(shadow))consumeShadow(shadow);
      for (Object var1 : new ArrayList<>(PLAYERS.values())) {
         if (var1 instanceof Entity e && com.gx.velveteclipse.darkhour.DarkHourService.active(e.m_9236_()) && BattleService.current(var1) == null) {
            UUID var2 = RuntimeBridge.playerUuid(var1);
            long var3 = NEXT_SPAWN.getOrDefault(var2, 0L);
            if (serverTicks >= var3) {
               if (nearbyCount(var1, 28.0) < 1) {
                  spawnNatural(var1);
                  NEXT_SPAWN.put(var2, serverTicks + 600L);
               } else {
                  NEXT_SPAWN.put(var2, serverTicks + 400L);
               }
            }
         }
      }
   }

   private static void detectEncounters() {
      for (Object var1 : new ArrayList<>(PLAYERS.values())) {
         if (var1 != null && !com.gx.velveteclipse.room.VelvetRoomService.inside(var1) && !com.gx.velveteclipse.exploration.ExplorationService.active(var1) && BattleService.current(var1) == null) {
            Object var2 = nearestEncounterShadow(var1, 2.35);
            if (var2 != null && !com.gx.velveteclipse.exploration.ExplorationService.reserved(var2)) {
               RuntimeBridge.sendMessage(var1, "§5[Velvet Eclipse] §fUma Shadow encontrou você!");
               startEncounter(var1,var2);
            }
         }
      }
   }

   private static void registerShadow(Object var0) {
      if (var0 != null) {
         UUID var1 = RuntimeBridge.playerUuid(var0);
         if (!SHADOWS.containsKey(var1)) {
            SHADOWS.put(var1, var0);
            VelvetEclipseMod.LOGGER.info("Velvet Eclipse: real world Shadow mob registered " + var1);
         }
      }
   }

   private static void unregisterShadow(Object var0) {
      if (var0 != null) {
         SHADOWS.remove(RuntimeBridge.playerUuid(var0));
      }
   }

   public static void beginReservedEncounter(Object player, Object shadow) {
      if (!com.gx.velveteclipse.exploration.ExplorationService.preparing(player) || com.gx.velveteclipse.exploration.ExplorationService.blocksBattleStart(player) ||
          !com.gx.velveteclipse.exploration.ExplorationService.reserved(shadow) || !ShadowRegistry.isShadow(shadow) ||
          RuntimeBridge.isEntityRemoved(shadow) || !RuntimeBridge.sameWorld(player,shadow) || BattleService.current(player)!=null) return;
      startEncounter(player,shadow);
   }

   private static void startEncounter(Object player,Object shadow) {
      BattleService.startEncounter(player,1,com.gx.velveteclipse.darkhour.TartarusDungeonService.floor(player),com.gx.velveteclipse.darkhour.TartarusDungeonService.guardian(shadow));
      if(BattleService.current(player)!=null){com.gx.velveteclipse.darkhour.TartarusDungeonService.encounterStarted(player,shadow);consumeShadow(shadow);}
   }
   private static void consumeShadow(Object var0) {
      if (var0 != null) {
         SHADOWS.remove(RuntimeBridge.playerUuid(var0));
         RuntimeBridge.discardObjectEntity(var0);
      }
   }

   private static Object nearestEncounterShadow(Object var0, double var1) {
      if (var0 == null) {
         return null;
      } else {
         double var3 = var1 * var1;
         Object var5 = null;

         for (Object var7 : SHADOWS.values()) {
            if (var7 != null && !RuntimeBridge.isEntityRemoved(var7) && RuntimeBridge.sameWorld(var0, var7) && hasEntityTag(var7, "velvet_encounter_shadow")) {
               double var8 = RuntimeBridge.distanceSquared(var0, var7);
               if (var8 <= var3) {
                  var3 = var8;
                  var5 = var7;
               }
            }
         }

         return var5;
      }
   }

   private static void addEntityTag(Object var0, String var1) {
      if (var0 != null) {
         for (String var5 : new String[]{"addTag", "m_20049_"}) {
            try {
               var0.getClass().getMethod(var5, String.class).invoke(var0, var1);
               return;
            } catch (Throwable var7) {
            }
         }
      }
   }

   private static boolean hasEntityTag(Object var0, String var1) {
      if (var0 == null) {
         return false;
      } else {
         for (String var5 : new String[]{"getTags", "m_19880_"}) {
            try {
               if (var0.getClass().getMethod(var5).invoke(var0) instanceof Set var7) {
                  return var7.contains(var1);
               }
            } catch (Throwable var8) {
            }
         }

         return false;
      }
   }

   private static void cleanupRemoved() {
      for (Entry var1 : new ArrayList<>(SHADOWS.entrySet())) {
         if (RuntimeBridge.isEntityRemoved(var1.getValue())) {
            SHADOWS.remove(var1.getKey());
         }
      }
   }
}
