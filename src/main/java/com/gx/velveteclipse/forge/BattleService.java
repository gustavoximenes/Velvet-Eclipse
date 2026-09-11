package com.gx.velveteclipse.forge;

import com.gx.velveteclipse.VelvetEclipseMod;
import com.gx.velveteclipse.core.battle.BattleAction;
import com.gx.velveteclipse.core.battle.BattleCombatant;
import com.gx.velveteclipse.core.battle.BattleResolution;
import com.gx.velveteclipse.core.battle.BattleSession;
import com.gx.velveteclipse.core.battle.BattleSide;
import com.gx.velveteclipse.core.battle.BattleState;
import com.gx.velveteclipse.core.persona.PersonaDefinition;
import com.gx.velveteclipse.core.persona.PersonaInstance;
import com.gx.velveteclipse.core.persona.PersonaRegistry;
import com.gx.velveteclipse.network.BattleSnapshotS2C;
import com.gx.velveteclipse.network.ModNetwork;
import com.gx.velveteclipse.util.RuntimeBridge;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class BattleService {
   private static final Map<UUID, BattleSession> PLAYER_BATTLES = new HashMap<>();
   private static final Map<UUID, Object> PLAYER_OBJECTS = new HashMap<>();
   private static final Map<UUID, Integer> AI_DELAY = new HashMap<>();
   private static final Map<UUID, Integer> PERSONA_SETTLE = new HashMap<>();
   private static final Map<UUID, Integer> ENEMY_SETTLE = new HashMap<>();
   private static final Map<UUID, double[]> BATTLE_ANCHORS = new HashMap<>();
   private static final Map<UUID, Float> BATTLE_YAWS = new HashMap<>();
   private static final Map<UUID, Float> BATTLE_PITCHES = new HashMap<>();
   private static final Map<UUID,String> FINISHING = new HashMap<>();

   private BattleService() {
   }

   public static BattleSession startDemo(Object var0) {
      if(com.gx.velveteclipse.exploration.ExplorationService.blocksBattleStart(var0)) return current(var0);
      UUID var1 = RuntimeBridge.playerUuid(var0);
      stopSilent(var0);
      PlayerPersonaData var2 = PlayerPersonaData.load(var0);
      BattleCombatant var3 = new BattleCombatant(
         var1, RuntimeBridge.playerName(var0), BattleSide.PLAYERS, var2.activeInstance(), var2.maxHp(), var2.hp(), var2.maxSp(), var2.sp()
      );
      PersonaInstance var4 = new PersonaInstance(PersonaRegistry.persona("umbra_slime").orElseThrow());
      BattleCombatant var5 = new BattleCombatant(UUID.randomUUID(), "Umbra Slime", BattleSide.ENEMIES, var4, 85, 85, 30, 30);
      BattleSession var6 = new BattleSession(List.of(var3, var5));
      PLAYER_BATTLES.put(var1, var6);
      PLAYER_OBJECTS.put(var1, var0);
      BATTLE_ANCHORS.put(var1, RuntimeBridge.playerPosition(var0));
      BATTLE_YAWS.put(var1, RuntimeBridge.playerYaw(var0));
      BATTLE_PITCHES.put(var1, Math.max(-12.0F, Math.min(18.0F, RuntimeBridge.playerPitch(var0))));
      RuntimeBridge.setEntityRotation(var0, BATTLE_YAWS.get(var1), 2.0F);
      VelvetEclipseMod.LOGGER.info("Velvet Eclipse: BattleSession created for " + RuntimeBridge.playerName(var0));

      try {
         VisualBattleActors.spawnBattle(var0, var2.activePersona(), "umbra_slime");
      } catch (Throwable var9) {
         VelvetEclipseMod.LOGGER.error("Velvet Eclipse battle actors failed, battle will continue: " + var9);
         RuntimeBridge.sendMessage(var0, "§e[Velvet Eclipse] A batalha iniciou, mas os atores 3D falharam. Veja o log.");
      }

      try {
         sync(var0, "A Shadow blocks your path.");
      } catch (Throwable var8) {
         VelvetEclipseMod.LOGGER.error("Velvet Eclipse initial battle sync failed, server battle remains active: " + var8);
         RuntimeBridge.sendMessage(var0, "§e[Velvet Eclipse] BattleSession ativa, mas a HUD não sincronizou. Use /velvet diagnostics.");
      }

      scheduleAiIfNeeded(var1, var6, 28);
      return var6;
   }

   public static BattleSession current(Object var0) {
      return PLAYER_BATTLES.get(RuntimeBridge.playerUuid(var0));
   }

   public static boolean hasActiveBattle(Object var0) {
      BattleSession var1 = current(var0);
      return var1 != null && var1.state() == BattleState.ACTIVE;
   }

   public static void stop(Object var0) {
      stopSilent(var0);
      sync(var0, "Battle aborted.");
   }

   private static void stopSilent(Object var0) {
      if (var0 != null) {
         UUID var1 = RuntimeBridge.playerUuid(var0);
         MeleeBattleServer.clear(var0);
         FINISHING.remove(var1);
         PLAYER_BATTLES.remove(var1);
         PLAYER_OBJECTS.remove(var1);
         AI_DELAY.remove(var1);
         PERSONA_SETTLE.remove(var1);
         ENEMY_SETTLE.remove(var1);
         BATTLE_ANCHORS.remove(var1);
         BATTLE_YAWS.remove(var1);
         BATTLE_PITCHES.remove(var1);
         VisualBattleActors.clear(var0);
      }
   }

   public static boolean act(Object var0, BattleAction var1) {
      BattleSession var2 = current(var0);
      UUID var3 = RuntimeBridge.playerUuid(var0);
      if (var2 == null) {
         sync(var0, "No active battle.");
         return false;
      } else if (var2.currentActor() != null && var2.currentActor().id().equals(var3)) {
         if(var2.prepared()!=null&&!var2.prepared().action().equals(var1))return false;
         PlayerPersonaData var4 = PlayerPersonaData.load(var0);
         if (var1.type().name().equals("ITEM")) {
            if (!var4.consumePulseTonic()) {
               sync(var0, "No Pulse Tonic remaining.");
               return false;
            }

            var4.save(var0);
         }

         if (var1.type().name().equals("SKILL")) {
            VisualBattleActors.pulsePersona(var0, var4.activePersona());
            PERSONA_SETTLE.put(var3, 12);
         }

         var prepared=var2.prepared();
         BattleResolution var5 = var2.perform(var3, var1);
         if(prepared!=null&&var2.prepared()==null&&prepared.action().equals(var1))var2.find(var1.targetId()).ifPresent(target->BattleKnowledge.learn(var0,target.persona().definitionId(),prepared.hit()));
         String var6 = battleText(var5);
         persistPlayerResources(var0, var2);
         if (var2.state() != BattleState.ACTIVE) {
            if(MeleeBattleServer.active(var0)||PersonaCinematicServer.suppressLegacySettle(var0)){
               FINISHING.put(var3,var6);sync(var0,var6);return true;
            }
            finish(var0, var2, var6);
            return true;
         } else {
            sync(var0, var6);
            scheduleAiIfNeeded(var3, var2, 28);
            return true;
         }
      } else {
         sync(var0, "Wait for your turn.");
         return false;
      }
   }

   public static boolean switchPersona(Object var0, String var1) {
      BattleSession var2 = current(var0);
      UUID var3 = RuntimeBridge.playerUuid(var0);
      if (var2 != null && var2.prepared()==null && !PersonaCinematicServer.suppressLegacySettle(var0) && !MeleeBattleServer.active(var0) && var2.currentActor() != null && var2.currentActor().id().equals(var3)) {
         PlayerPersonaData var4 = PlayerPersonaData.load(var0);
         PersonaInstance var5 = var4.instance(var1);
         if (var5 != null && var4.equipPersona(var1)) {
            var2.find(var3).ifPresent(var1x -> var1x.setPersona(var5));
            var4.save(var0);
            String var6 = PersonaRegistry.persona(var1).map(PersonaDefinition::displayName).orElse(var1);
            VisualBattleActors.replacePersona(var0, var1);
            sync(var0, "PERSONA → " + var6);
            return true;
         } else {
            sync(var0, "That Persona is not in your roster.");
            return false;
         }
      } else {
         sync(var0, "Persona can only be switched on your turn.");
         return false;
      }
   }

   public static void handleNetworkAction(Object var0, String var1, String var2) {
      if (var0 != null && var1 != null) {
         if((current(var0)!=null&&current(var0).prepared()!=null)||PersonaCinematicServer.suppressLegacySettle(var0)||MeleeBattleServer.active(var0))return;
         VelvetEclipseMod.LOGGER.info("Velvet Eclipse server received battle action '" + var1 + "' from " + RuntimeBridge.playerName(var0));
         switch (var1) {
            case "start_demo":
               if (current(var0) == null) {
                  startDemo(var0);
               }
               break;
            case "attack":
               actFirstTarget(var0, "");
               break;
            case "skill":
               skillFirstTarget(var0, var2);
               break;
            case "persona":
               switchPersona(var0, var2);
               break;
            case "guard":
               act(var0, BattleAction.guard());
               break;
            case "item":
               act(var0, BattleAction.item("pulse_tonic"));
               break;
            case "escape":
               act(var0, BattleAction.escape());
         }
      }
   }

   public static boolean actFirstTarget(Object var0, String var1) {
      BattleSession var2 = current(var0);
      if (var2 != null && var2.currentActor() != null) {
         BattleCombatant var3 = var2.enemiesOf(var2.currentActor()).stream().findFirst().orElse(null);
         return var3 != null && MeleeBattleServer.start(var0, BattleAction.attack(var3.id()));
      } else {
         return false;
      }
   }

   public static boolean skillFirstTarget(Object var0, String var1) {
      return PersonaDelayedActions.queueSkill(var0, var1);
   }

   public static void tickServer() {
      PersonaCinematicServer.tick();
      MeleeBattleServer.tick();

      for (UUID var2 : new ArrayList<>(PLAYER_BATTLES.keySet())) {
         BattleSession var3 = PLAYER_BATTLES.get(var2);
         Object var4 = PLAYER_OBJECTS.get(var2);
         if (var3 != null && var4 != null) {
            if(FINISHING.containsKey(var2)&&!MeleeBattleServer.active(var4)&&!PersonaCinematicServer.suppressLegacySettle(var4)){
               finish(var4,var3,FINISHING.remove(var2));continue;
            }
            double[] var5 = BATTLE_ANCHORS.get(var2);
            if (var5 != null && !MeleeBattleServer.active(var4)) {
               RuntimeBridge.freezePlayer(var4, var5);
            }

            Integer var6 = PERSONA_SETTLE.get(var2);
            if (var6 != null) {
               if (var6 <= 1) {
                  PERSONA_SETTLE.remove(var2);
                  PlayerPersonaData var7 = PlayerPersonaData.load(var4);
                  VisualBattleActors.settlePersona(var4, var7.activePersona());
               } else {
                  PERSONA_SETTLE.put(var2, var6 - 1);
               }
            }

            Integer var9 = ENEMY_SETTLE.get(var2);
            if (var9 != null) {
               if (var9 <= 1) {
                  ENEMY_SETTLE.remove(var2);
                  VisualBattleActors.settleEnemy(var4);
               } else {
                  ENEMY_SETTLE.put(var2, var9 - 1);
               }
            }

            if (var3.state() == BattleState.ACTIVE && !MeleeBattleServer.active(var4) && !PersonaCinematicServer.suppressLegacySettle(var4)) {
               if (var3.currentActor() != null && var3.currentActor().side() == BattleSide.ENEMIES) {
                  int var8 = AI_DELAY.getOrDefault(var2, 24) - 1;
                  if (var8 > 0) {
                     AI_DELAY.put(var2, var8);
                  } else {
                     AI_DELAY.remove(var2);
                     runOneEnemyTurn(var4, var3);
                  }
               } else {
                  AI_DELAY.remove(var2);
               }
            }
         }
      }
   }

   public static void sync(Object var0, String var1) {
      if (var0 != null) {
         PlayerPersonaData var2 = PlayerPersonaData.load(var0);
         BattleSession var3 = current(var0);
         PersonaInstance var4 = var2.activeInstance();
         int var5 = var2.hp();
         int var6 = var2.sp();
         int var7 = var2.maxHp();
         int var8 = var2.maxSp();
         String var9 = "";
         int var10 = 0;
         int var11 = 0;
         boolean var12 = false;
         boolean var13 = var3 != null && (var3.state() == BattleState.ACTIVE||FINISHING.containsKey(RuntimeBridge.playerUuid(var0)));
         if (var13) {
            UUID var14 = RuntimeBridge.playerUuid(var0);
            BattleCombatant var15 = var3.find(var14).orElse(null);
            if (var15 != null) {
               var4 = var15.persona();
               var5 = var15.hp();
               var6 = var15.sp();
               var7 = var15.maxHp();
               var8 = var15.maxSp();
            }

            BattleCombatant var16 = var3.participants().stream().filter(var0x -> var0x.side() == BattleSide.ENEMIES).findFirst().orElse(null);
            if (var16 != null) {
               var9 = var16.name();
               var10 = var16.hp();
               var11 = var16.maxHp();
            }

            var12 = var3.currentActor() != null && var3.currentActor().id().equals(var14);
         }

         var enemy=var3==null?null:var3.participants().stream().filter(e->e.side()==BattleSide.ENEMIES&&e.alive()).findFirst().orElse(null);
         Object visual=VisualBattleActors.enemyActor(var0);
         ModNetwork.sendPresentation(var0,new com.gx.velveteclipse.network.BattleTargetS2C(
            visual instanceof net.minecraft.world.entity.Entity entity?entity.m_19879_():-1,
            enemy==null?"":enemy.persona().definitionId(),enemy==null?"":BattleKnowledge.load(var0).forEnemy(enemy.persona().definitionId())));
         PersonaDefinition var18 = PersonaRegistry.persona(var4.definitionId()).orElse(null);
         String var19 = var18 == null ? var4.definitionId() : var18.displayName();
         ArrayList var20 = new ArrayList<>(var4.learnedSkills());
         ArrayList var17 = new ArrayList<>(var2.rosterIds());
         ModNetwork.sendToPlayer(
            var0,
            new BattleSnapshotS2C(
               var13, var12, var4.definitionId(), var19, var5, var7, var6, var8, var9, var10, var11, var2.pulseTonics(), var20, var17, var1 == null ? "" : var1
            )
         );
      }
   }

   private static void scheduleAiIfNeeded(UUID var0, BattleSession var1, int var2) {
      if (var1 != null && var1.state() == BattleState.ACTIVE && var1.currentActor() != null && var1.currentActor().side() == BattleSide.ENEMIES) {
         AI_DELAY.put(var0, Math.max(1, var2));
      }
   }

   private static void runOneEnemyTurn(Object var0, BattleSession var1) {
      UUID var2 = RuntimeBridge.playerUuid(var0);
      if (var1.state() == BattleState.ACTIVE && var1.currentActor() != null && var1.currentActor().side() == BattleSide.ENEMIES) {
         BattleCombatant var3 = var1.currentActor();
         BattleCombatant var4 = var1.participants().stream().filter(var0x -> var0x.side() == BattleSide.PLAYERS && var0x.alive()).findFirst().orElse(null);
         if (var4 != null) {
            String var5 = var3.persona().learnedSkills().stream().findFirst().orElse(null);
            VisualBattleActors.pulseEnemy(var0);
            ENEMY_SETTLE.put(var2, 11);
            BattleAction var6;
            if (var5 != null && var3.sp() >= PersonaRegistry.skill(var5).map(var0x -> var0x.spCost()).orElse(999)) {
               var6 = BattleAction.skill(var5, var4.id());
            } else {
               var6 = BattleAction.attack(var4.id());
            }

            BattleResolution var7 = var1.perform(var3.id(), var6);
            String var8 = battleText(var7);
            persistPlayerResources(var0, var1);
            if (var1.state() != BattleState.ACTIVE) {
               finish(var0, var1, var8);
            } else {
               sync(var0, var8);
               if (var1.currentActor() != null && var1.currentActor().side() == BattleSide.ENEMIES) {
                  AI_DELAY.put(var2, 24);
               }
            }
         }
      }
   }

   private static String battleText(BattleResolution var0) {
      return var0 != null && var0.log() != null && !var0.log().isEmpty() ? String.join("  •  ", var0.log()) : "";
   }

   private static void persistPlayerResources(Object var0, BattleSession var1) {
      UUID var2 = RuntimeBridge.playerUuid(var0);
      var1.find(var2).ifPresent(var1x -> {
         PlayerPersonaData var2x = PlayerPersonaData.load(var0);
         var2x.setResources(var1x.hp(), var1x.sp());
         var2x.updatePersona(var1x.persona());
         var2x.equipPersona(var1x.persona().definitionId());
         var2x.save(var0);
      });
   }

   private static void finish(Object var0, BattleSession var1, String var2) {
      PlayerPersonaData var3 = PlayerPersonaData.load(var0);
      UUID var4 = RuntimeBridge.playerUuid(var0);
      var1.find(var4).ifPresent(var1x -> {
         var3.setResources(var1x.hp(), var1x.sp());
         var3.updatePersona(var1x.persona());
         var3.equipPersona(var1x.persona().definitionId());
      });
      String var5;
      if (var1.state() == BattleState.PLAYERS_WON) {
         PersonaInstance var6 = var3.activeInstance();
         PersonaDefinition var7 = PersonaRegistry.persona(var6.definitionId()).orElseThrow();
         int var8 = var6.level();
         var6.addExperience(var7, 80);
         var3.updatePersona(var6);
         boolean var9 = var3.givePersona("umbra_slime");
         var5 = "VICTORY  •  +80 EXP";
         if (var6.level() > var8) {
            var5 = var5 + "  •  LEVEL UP " + var6.level();
         }

         if (var9) {
            var5 = var5 + "  •  Umbra Slime registered";
         }
      } else if (var1.state() == BattleState.ENEMIES_WON) {
         var5 = "DEFEAT";
      } else {
         var5 = "BATTLE ENDED";
      }

      if (var2 != null && !var2.isBlank()) {
         var5 = var2 + "  •  " + var5;
      }

      var3.save(var0);
      MeleeBattleServer.clear(var0);
      FINISHING.remove(var4);
      PLAYER_BATTLES.remove(var4);
      PLAYER_OBJECTS.remove(var4);
      AI_DELAY.remove(var4);
      PERSONA_SETTLE.remove(var4);
      ENEMY_SETTLE.remove(var4);
      BATTLE_ANCHORS.remove(var4);
      BATTLE_YAWS.remove(var4);
      BATTLE_PITCHES.remove(var4);
      VisualBattleActors.clear(var0);
      sync(var0, var5);
   }
}
