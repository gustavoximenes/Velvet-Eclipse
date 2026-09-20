package com.gx.velveteclipse.client;

import com.gx.velveteclipse.VelvetEclipseMod;
import com.gx.velveteclipse.client.ClientBattleUiState.Page;
import com.gx.velveteclipse.network.BattleSnapshotS2C;
import com.gx.velveteclipse.network.ModNetwork;
import com.mojang.blaze3d.platform.InputConstants.Type;
import java.lang.reflect.Method;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.InputEvent.Key;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

public final class ClientKeyMappings {
   public static final String CATEGORY = "key.categories.velvet_eclipse";
   public static final KeyMapping ATTACK = mapping("battle_attack", 49);
   public static final KeyMapping PERSONA = mapping("battle_persona", 50);
   public static final KeyMapping SKILL = mapping("battle_skill", 51);
   public static final KeyMapping ITEM = mapping("battle_item", 52);
   public static final KeyMapping GUARD = mapping("battle_guard", 53);
   public static final KeyMapping ESCAPE = mapping("battle_escape", 54);
   public static final KeyMapping OPTION_7 = mapping("battle_option_7", 55);
   public static final KeyMapping BACK = mapping("battle_back", 259);
   private static final Map<KeyMapping, Boolean> LAST_DOWN = new IdentityHashMap<>();

   private ClientKeyMappings() {
   }

   private static KeyMapping mapping(String var0, int var1) {
      return new KeyMapping("key.velvet_eclipse." + var0, Type.KEYSYM, var1, "key.categories.velvet_eclipse");
   }

   private static boolean pressedCompat(KeyMapping var0) {
      if (var0 == null) {
         return false;
      } else {
         boolean var1 = consumeClickCompat(var0);
         boolean var2 = isDownCompat(var0);
         boolean var3 = Boolean.TRUE.equals(LAST_DOWN.put(var0, var2));
         return var1 || var2 && !var3;
      }
   }

   private static boolean isDownCompat(KeyMapping var0) {
      for (String var4 : new String[]{"isDown", "m_90857_"}) {
         try {
            Method var5 = var0.getClass().getMethod(var4);
            if (var5.invoke(var0) instanceof Boolean var7) {
               return var7;
            }
         } catch (NoSuchMethodException var8) {
         } catch (Throwable var9) {
            VelvetEclipseMod.LOGGER.error("Velvet Eclipse could not poll key mapping using " + var4 + ": " + var9);
            return false;
         }
      }

      return false;
   }

   private static boolean consumeClickCompat(KeyMapping var0) {
      if (var0 == null) {
         return false;
      } else {
         for (String var4 : new String[]{"consumeClick", "m_90859_"}) {
            try {
               Method var5 = var0.getClass().getMethod(var4);
               if (var5.invoke(var0) instanceof Boolean var7) {
                  return var7;
               }
            } catch (NoSuchMethodException var8) {
            } catch (Throwable var9) {
               VelvetEclipseMod.LOGGER.error("Velvet Eclipse could not read key mapping using " + var4 + ": " + var9);
               return false;
            }
         }

         return false;
      }
   }

   private static boolean matchesCompat(KeyMapping var0, int var1, int var2) {
      if (var0 == null) {
         return false;
      } else {
         for (String var6 : new String[]{"matches", "m_90832_"}) {
            try {
               Method var7 = var0.getClass().getMethod(var6, int.class, int.class);
               if (var7.invoke(var0, var1, var2) instanceof Boolean var9) {
                  return var9;
               }
            } catch (NoSuchMethodException var10) {
            } catch (Throwable var11) {
               VelvetEclipseMod.LOGGER.error("Velvet Eclipse could not match key mapping using " + var6 + ": " + var11);
               return false;
            }
         }

         return false;
      }
   }

   public static String label(KeyMapping var0) {
      if (var0 == null) {
         return "?";
      } else {
         for (String var4 : new String[]{"getTranslatedKeyMessage", "m_90863_"}) {
            try {
               Method var5 = var0.getClass().getMethod(var4);
               Object var6 = var5.invoke(var0);
               if (var6 != null) {
                  Method var7 = var6.getClass().getMethod("getString");
                  Object var8 = var7.invoke(var6);
                  if (var8 != null && !var8.toString().isBlank()) {
                     return var8.toString();
                  }
               }
            } catch (NoSuchMethodException var9) {
            } catch (Throwable var10) {
            }
         }

         return "?";
      }
   }

   public static String optionLabel(int var0) {
      return switch (var0) {
         case 0 -> label(ATTACK);
         case 1 -> label(PERSONA);
         case 2 -> label(SKILL);
         case 3 -> label(ITEM);
         case 4 -> label(GUARD);
         case 5 -> label(ESCAPE);
         case 6 -> label(OPTION_7);
         default -> "?";
      };
   }

   public static String actionLabel(int var0) {
      return switch (var0) {
         case 0 -> label(ATTACK);
         case 1 -> label(PERSONA);
         case 2 -> label(SKILL);
         case 3 -> label(ITEM);
         case 4 -> label(GUARD);
         case 5 -> label(ESCAPE);
         default -> "?";
      };
   }

   @EventBusSubscriber(
      modid = "velvet_eclipse",
      bus = Bus.FORGE,
      value = {Dist.CLIENT}
   )
   public static final class GameplayInput {
      private GameplayInput() {
      }

      @SubscribeEvent
      public static void onClientTick(ClientTickEvent var0) {
         if (var0.phase == Phase.END) {
            BattleSnapshotS2C var1 = ClientBattleState.snapshot();
            boolean inGame=net.minecraft.client.Minecraft.m_91087_().f_91080_==null;
            if (ClientKeyMappings.pressedCompat(ClientKeyMappings.BACK) && inGame && var1 != null && var1.active()) {
               ClientBattleUiState.back();
            }

            if (inGame && var1 != null && var1.active() && var1.playerTurn() && !ClientBattleUiState.actionLocked() && !BattleTacticsClient.prompt()) {
               if (ClientBattleUiState.page() != Page.MAIN) {
                  if (ClientKeyMappings.pressedCompat(ClientKeyMappings.ATTACK)) {
                     ClientBattleUiState.selectVisible(0, var1);
                  }

                  if (ClientKeyMappings.pressedCompat(ClientKeyMappings.PERSONA)) {
                     ClientBattleUiState.selectVisible(1, var1);
                  }

                  if (ClientKeyMappings.pressedCompat(ClientKeyMappings.SKILL)) {
                     ClientBattleUiState.selectVisible(2, var1);
                  }

                  if (ClientKeyMappings.pressedCompat(ClientKeyMappings.ITEM)) {
                     ClientBattleUiState.selectVisible(3, var1);
                  }

                  if (ClientKeyMappings.pressedCompat(ClientKeyMappings.GUARD)) {
                     ClientBattleUiState.selectVisible(4, var1);
                  }

                  if (ClientKeyMappings.pressedCompat(ClientKeyMappings.ESCAPE)) {
                     ClientBattleUiState.selectVisible(5, var1);
                  }

                  if (ClientKeyMappings.pressedCompat(ClientKeyMappings.OPTION_7)) {
                     ClientBattleUiState.selectVisible(6, var1);
                  }
               } else {
                  if (ClientKeyMappings.pressedCompat(ClientKeyMappings.ATTACK)) {
                     ClientBattleUiState.attack();
                  }

                  if (ClientKeyMappings.pressedCompat(ClientKeyMappings.PERSONA)) {
                     ClientBattleUiState.open(Page.PERSONAS);
                  }

                  if (ClientKeyMappings.pressedCompat(ClientKeyMappings.SKILL)) {
                     ClientBattleUiState.open(Page.SKILLS);
                  }

                  if (ClientKeyMappings.pressedCompat(ClientKeyMappings.ITEM)) {
                     ModNetwork.sendAction("item", "pulse_tonic");
                  }

                  if (ClientKeyMappings.pressedCompat(ClientKeyMappings.GUARD)) {
                     ModNetwork.sendAction("guard", "");
                  }

                  if (ClientKeyMappings.pressedCompat(ClientKeyMappings.ESCAPE)) {
                     ModNetwork.sendAction("escape", "");
                  }

                  if (ClientKeyMappings.pressedCompat(ClientKeyMappings.OPTION_7)) {
                  }
               }
            } else {
               drainBattleActionClicks();
            }
         }
      }

      @SubscribeEvent
      public static void onKey(Key var0) {
         if(BattleResultsClient.key(var0)||BattleTacticsClient.key(var0))return;
         var mc=net.minecraft.client.Minecraft.m_91087_();var snapshot=ClientBattleState.snapshot();
         if(mc.f_91080_==null&&snapshot.active()&&(var0.getAction()==1||var0.getAction()==2)){
            switch(var0.getKey()){
               case 265 -> ClientBattleUiState.move(-1);case 264 -> ClientBattleUiState.move(1);
               case 81,263 -> ClientBattleUiState.cycle(-1);case 69,262 -> ClientBattleUiState.cycle(1);
               case 257,335 -> ClientBattleUiState.confirm(snapshot);
            }
         }
      }

      private static void drainBattleActionClicks() {
         ClientKeyMappings.pressedCompat(ClientKeyMappings.ATTACK);
         ClientKeyMappings.pressedCompat(ClientKeyMappings.PERSONA);
         ClientKeyMappings.pressedCompat(ClientKeyMappings.SKILL);
         ClientKeyMappings.pressedCompat(ClientKeyMappings.ITEM);
         ClientKeyMappings.pressedCompat(ClientKeyMappings.GUARD);
         ClientKeyMappings.pressedCompat(ClientKeyMappings.ESCAPE);
         ClientKeyMappings.pressedCompat(ClientKeyMappings.OPTION_7);
      }
   }

   @EventBusSubscriber(
      modid = "velvet_eclipse",
      bus = Bus.MOD,
      value = {Dist.CLIENT}
   )
   public static final class Registration {
      private Registration() {
      }

      @SubscribeEvent
      public static void register(RegisterKeyMappingsEvent var0) {
         var0.register(ClientKeyMappings.ATTACK);
         var0.register(ClientKeyMappings.PERSONA);
         var0.register(ClientKeyMappings.SKILL);
         var0.register(ClientKeyMappings.ITEM);
         var0.register(ClientKeyMappings.GUARD);
         var0.register(ClientKeyMappings.ESCAPE);
         var0.register(ClientKeyMappings.OPTION_7);
         var0.register(ClientKeyMappings.BACK);
         VelvetEclipseMod.LOGGER.info("Registered Velvet Eclipse key mappings.");
      }
   }
}
