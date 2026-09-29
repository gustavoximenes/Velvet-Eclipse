package com.gx.velveteclipse.forge;

import com.gx.velveteclipse.util.RuntimeBridge;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class VisualBattleActors {
   private static final Map<UUID, Map<UUID, net.minecraft.world.entity.Entity>> ENEMIES = new HashMap<>();
   private static final Map<UUID, Object> PERSONA = new HashMap<>();
   public static void bindEnemy(UUID battle,UUID id,net.minecraft.world.entity.Entity e){ENEMIES.computeIfAbsent(battle,k->new java.util.LinkedHashMap<>()).put(id,e);}

   public static void spawnBattle(Object player,String persona,com.gx.velveteclipse.core.battle.TerrainFormation.Layout layout) {
      var session=BattleService.current(player);if(session==null)return;
      clearEnemies(session.id());var actors=new java.util.LinkedHashMap<UUID,net.minecraft.world.entity.Entity>();ENEMIES.put(session.id(),actors);
      var enemies=session.participants().stream().filter(c->c.side()==com.gx.velveteclipse.core.battle.BattleSide.ENEMIES).toList();
      double[] origin=RuntimeBridge.playerPosition(player);float yaw=RuntimeBridge.playerYaw(player);
      for(int i=0;i<enemies.size();i++){
         var point=layout.enemies().get(i);double[] at={point.x(),point.y(),point.z()};
         Object entity=spawnEnemy(player,at,yaw+180,enemies.get(i).persona().definitionId());if(entity instanceof net.minecraft.world.entity.Entity e)actors.put(enemies.get(i).id(),e);
      }
      replacePersona(player,persona);
   }
   public static boolean repairFormation(Object player,boolean boss){
      var session=BattleService.current(player);if(session==null)return true;var actors=ENEMIES.get(session.id());if(actors==null||actors.isEmpty()||actors.values().stream().allMatch(BattleTerrain::valid))return true;
      var layout=BattleTerrain.plan(player,actors.size(),boss);if(layout==null)return false;int i=0;
      for(var actor:actors.values()){var p=layout.enemies().get(i++);RuntimeBridge.setEntityPosition(actor,p.x(),p.y(),p.z());RuntimeBridge.setEntityRotation(actor,(float)layout.yaw()+180,0);}
      return true;
   }
   public static net.minecraft.world.entity.Entity enemy(UUID battle,UUID id){var actors=ENEMIES.get(battle);return actors==null?null:actors.get(id);}
   public static void clearEnemies(UUID battle){var actors=ENEMIES.remove(battle);if(actors!=null)actors.values().forEach(RuntimeBridge::discardObjectEntity);}
   public static void prune(com.gx.velveteclipse.core.battle.BattleSession session){
      var actors=ENEMIES.get(session.id());if(actors==null)return;
      for(var c:session.participants())if(!c.alive())RuntimeBridge.discardObjectEntity(actors.remove(c.id()));
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

   // Enemy positions belong to the encounter formation and never follow an individual player.
   public static void pulseEnemy(Object player) {}
   public static void settleEnemy(Object player) {}
   public static void clear(Object player) {
      if(player==null)return;
      PersonaCinematicServer.clear(player);
      RuntimeBridge.discardObjectEntity(PERSONA.remove(RuntimeBridge.playerUuid(player)));
   }
   public static Object enemyActor(Object player){return BattleService.targetActor(player,BattleService.targetId(player));}

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

   private static Object spawnEnemy(Object var0, double[] var1, float var2,String persona) {
      try {
         Object var3 = com.gx.velveteclipse.shadow.EnemyRegistry.typeFor(persona);
         Object var4 = RuntimeBridge.createEntityFromType(var0, var3, var1[0], var1[1], var1[2]);
         if (var4 != null) {
            if(var4 instanceof net.minecraft.world.entity.Entity e)e.m_6593_(net.minecraft.network.chat.Component.m_237113_(com.gx.velveteclipse.shadow.EnemyCatalog.get(persona).displayName()));
            RuntimeBridge.configureBattleActor(var4, false);
            if(var4 instanceof net.minecraft.world.entity.Entity e)e.m_20242_(true);
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
            if(var5 instanceof com.gx.velveteclipse.entity.VelvetPersonaGeoEntity persona)persona.summoner(RuntimeBridge.playerUuid(var0));
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
