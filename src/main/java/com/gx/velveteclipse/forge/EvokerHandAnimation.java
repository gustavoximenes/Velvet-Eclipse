package com.gx.velveteclipse.forge;

import com.gx.velveteclipse.util.RuntimeBridge;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class EvokerHandAnimation {
   private static final Map<UUID, Object> SAVED_MAIN_HAND = new HashMap<>();
   private static final Map<UUID, Integer> SAVED_SLOT = new HashMap<>();
   private static final Map<UUID, UUID> VISUAL_EVOKER = new HashMap<>();
   private static final String EVOKER_ID = "velvet_eclipse:model_evoker";

   private EvokerHandAnimation() {
   }

   public static void begin(Object var0) {
      if (var0 != null) {
         try {
            UUID var1 = RuntimeBridge.playerUuid(var0);
            if (var1 == null) {
               return;
            }

            if (!SAVED_MAIN_HAND.containsKey(var1)) {
               Object var2 = getInventory(var0);
               int var3 = selectedSlot(var2);
               Object var4 = getInventoryItem(var2, var3);
               if (var4 == null) {
                  var4 = getMainHand(var0);
               }

               if (var4 != null) {
                  SAVED_MAIN_HAND.put(var1, var4);
               }

               SAVED_SLOT.put(var1, var3);
            }

            Object var6 = makeStack("velvet_eclipse:model_evoker");
            if (var6 != null) {
               installSelectedStack(var0, var6);
               setMainHand(var0, var6);
               syncInventory(var0);
            }

            swing(var0);
            spawnVisual(var0, 0.0);
         } catch (Throwable var5) {
         }
      }
   }

   public static void tick(Object var0, int var1) {
      if (var0 != null && var1 >= 0) {
         try {
            UUID var2 = RuntimeBridge.playerUuid(var0);
            if (var2 == null) {
               return;
            }

            if (!SAVED_SLOT.containsKey(var2)) {
               begin(var0);
            }

            Object var3 = makeStack("velvet_eclipse:model_evoker");
            if (var3 != null) {
               installSelectedStack(var0, var3);
               setMainHand(var0, var3);
               syncInventory(var0);
            }

            if (var1 == 1 || var1 == 3) {
               swing(var0);
            }

            if (var1 >= 4 && var1 <= 13) {
               startUsing(var0);
            }

            if (var1 <= 4) {
               double var4 = clamp((double)(var1 - 1) / 3.0);
               spawnVisualDraw(var0, var4);
            } else {
               double var7 = clamp((double)(var1 - 4) / 9.0);
               spawnVisualRaise(var0, var7);
            }
         } catch (Throwable var6) {
         }
      }
   }

   public static void restore(Object var0) {
      if (var0 != null) {
         try {
            stopUsing(var0);
            UUID var1 = RuntimeBridge.playerUuid(var0);
            if (var1 == null) {
               return;
            }

            discardVisual(var0, var1);
            Object var2 = SAVED_MAIN_HAND.remove(var1);
            Integer var3 = SAVED_SLOT.remove(var1);
            if (var2 != null) {
               Object var4 = getInventory(var0);
               int var5 = var3 != null ? var3 : selectedSlot(var4);
               setInventoryItem(var4, var5, var2);
               setMainHand(var0, var2);
               markInventoryChanged(var4);
               syncInventory(var0);
            }
         } catch (Throwable var6) {
         }
      }
   }

   private static void spawnVisual(Object var0, double var1) {
      spawnVisualDraw(var0, var1);
   }

   private static void spawnVisualDraw(Object var0, double var1) {
      double var3 = lerp(-0.62, -0.44, var1);
      double var5 = lerp(0.72, 1.04, var1);
      double var7 = lerp(0.11, 0.13, var1);
      placeVisual(var0, var3, var5, var7);
   }

   private static void spawnVisualRaise(Object var0, double var1) {
      double var3 = var1 * var1 * (3.0 - 2.0 * var1);
      double var5 = lerp(-0.44, -0.29, var3);
      double var7 = lerp(1.04, 1.58, var3);
      double var9 = lerp(0.13, 0.035, var3);
      placeVisual(var0, var5, var7, var9);
   }

   private static void placeVisual(Object var0, double var1, double var3, double var5) {
      UUID var7 = RuntimeBridge.playerUuid(var0);
      if (var7 != null) {
         discardVisual(var0, var7);
         double[] var8 = RuntimeBridge.playerPosition(var0);
         float var9 = RuntimeBridge.playerYaw(var0);
         double[] var10 = RuntimeBridge.localOffset(var8, var9, var1, var3, var5);
         UUID var11 = RuntimeBridge.spawnItemDisplay(
            var0, "velvet_eclipse:model_evoker", var10[0], var10[1], var10[2], 0.0F, var9 - 88.0F, "velvet_evoker_hand"
         );
         if (var11 != null) {
            VISUAL_EVOKER.put(var7, var11);
         }
      }
   }

   private static void discardVisual(Object var0, UUID var1) {
      UUID var2 = VISUAL_EVOKER.remove(var1);
      if (var2 != null) {
         RuntimeBridge.discardEntity(var0, var2);
      }
   }

   private static Object getInventory(Object var0) throws Exception {
      for (String var4 : new String[]{"getInventory", "m_150109_"}) {
         try {
            Method var5 = var0.getClass().getMethod(var4);
            Object var6 = var5.invoke(var0);
            if (var6 != null) {
               return var6;
            }
         } catch (ReflectiveOperationException var7) {
         }
      }

      for (Method var11 : var0.getClass().getMethods()) {
         if (var11.getParameterCount() == 0 && var11.getReturnType().getName().equals("net.minecraft.world.entity.player.Inventory")) {
            Object var12 = var11.invoke(var0);
            if (var12 != null) {
               return var12;
            }
         }
      }

      throw new NoSuchMethodException("Player inventory getter");
   }

   private static int selectedSlot(Object var0) {
      if (var0 == null) {
         return 0;
      } else {
         for (String var4 : new String[]{"selected", "f_35977_"}) {
            try {
               Field var5 = findField(var0.getClass(), var4);
               if (var5 != null) {
                  var5.setAccessible(true);
                  return var5.getInt(var0);
               }
            } catch (Throwable var6) {
            }
         }

         return 0;
      }
   }

   private static Object getInventoryItem(Object var0, int var1) {
      if (var0 == null) {
         return null;
      } else {
         for (String var5 : new String[]{"getItem", "m_8020_"}) {
            try {
               Method var6 = var0.getClass().getMethod(var5, int.class);
               return var6.invoke(var0, var1);
            } catch (ReflectiveOperationException var8) {
            }
         }

         for (String var12 : new String[]{"getSelected", "m_36056_"}) {
            try {
               Method var13 = var0.getClass().getMethod(var12);
               return var13.invoke(var0);
            } catch (ReflectiveOperationException var7) {
            }
         }

         return null;
      }
   }

   private static void installSelectedStack(Object var0, Object var1) throws Exception {
      Object var2 = getInventory(var0);
      int var3 = selectedSlot(var2);
      setInventoryItem(var2, var3, var1);
      markInventoryChanged(var2);
   }

   private static void setInventoryItem(Object var0, int var1, Object var2) throws Exception {
      Class var3 = Class.forName("net.minecraft.world.item.ItemStack");

      for (String var7 : new String[]{"setItem", "m_6836_"}) {
         try {
            Method var8 = var0.getClass().getMethod(var7, int.class, var3);
            var8.invoke(var0, var1, var2);
            return;
         } catch (ReflectiveOperationException var9) {
         }
      }

      for (Method var13 : var0.getClass().getMethods()) {
         Class[] var14 = var13.getParameterTypes();
         if (var14.length == 2
            && var14[0] == int.class
            && var14[1].getName().equals("net.minecraft.world.item.ItemStack")
            && var13.getReturnType() == void.class) {
            var13.invoke(var0, var1, var2);
            return;
         }
      }

      throw new NoSuchMethodException("Inventory.setItem");
   }

   private static void markInventoryChanged(Object var0) {
      if (var0 != null) {
         for (String var4 : new String[]{"setChanged", "m_6596_"}) {
            try {
               Method var5 = var0.getClass().getMethod(var4);
               var5.invoke(var0);
               return;
            } catch (Throwable var6) {
            }
         }
      }
   }

   private static void syncInventory(Object var0) {
      for (String var4 : new String[]{"inventoryMenu", "f_36095_", "containerMenu", "f_36096_"}) {
         try {
            Field var5 = findField(var0.getClass(), var4);
            if (var5 != null) {
               var5.setAccessible(true);
               Object var6 = var5.get(var0);
               if (var6 != null) {
                  invokeNoArg(var6, "broadcastChanges", "m_38946_");
                  invokeNoArg(var6, "broadcastFullState", "m_182423_");
               }
            }
         } catch (Throwable var7) {
         }
      }
   }

   private static Object getMainHand(Object var0) throws Exception {
      for (String var4 : new String[]{"getMainHandItem", "m_21205_"}) {
         try {
            Method var5 = var0.getClass().getMethod(var4);
            if (var5.getParameterCount() == 0) {
               return var5.invoke(var0);
            }
         } catch (ReflectiveOperationException var6) {
         }
      }

      return null;
   }

   private static void setMainHand(Object var0, Object var1) throws Exception {
      Class var2 = Class.forName("net.minecraft.world.entity.EquipmentSlot");
      Object var3 = enumConstant(var2, "MAINHAND");
      Class var4 = Class.forName("net.minecraft.world.item.ItemStack");

      for (String var8 : new String[]{"setItemSlot", "m_8061_"}) {
         try {
            Method var9 = var0.getClass().getMethod(var8, var2, var4);
            var9.invoke(var0, var3, var1);
            return;
         } catch (ReflectiveOperationException var10) {
         }
      }
   }

   private static void swing(Object var0) throws Exception {
      Class var1 = Class.forName("net.minecraft.world.InteractionHand");
      Object var2 = enumConstant(var1, "MAIN_HAND");

      for (String var6 : new String[]{"swing", "m_6674_"}) {
         try {
            Method var7 = var0.getClass().getMethod(var6, var1);
            var7.invoke(var0, var2);
            return;
         } catch (ReflectiveOperationException var8) {
         }
      }
   }

   private static void startUsing(Object var0) throws Exception {
      Class var1 = Class.forName("net.minecraft.world.InteractionHand");
      Object var2 = enumConstant(var1, "MAIN_HAND");

      for (String var6 : new String[]{"startUsingItem", "m_6672_"}) {
         try {
            Method var7 = var0.getClass().getMethod(var6, var1);
            var7.invoke(var0, var2);
            return;
         } catch (ReflectiveOperationException var8) {
         }
      }
   }

   private static void stopUsing(Object var0) throws Exception {
      for (String var4 : new String[]{"stopUsingItem", "m_21253_", "m_5810_"}) {
         try {
            Method var5 = var0.getClass().getMethod(var4);
            if (var5.getParameterCount() == 0) {
               var5.invoke(var0);
               return;
            }
         } catch (ReflectiveOperationException var6) {
         }
      }
   }

   private static Object makeStack(String var0) throws Exception {
      Class var1 = Class.forName("net.minecraft.core.registries.BuiltInRegistries");
      Object var2 = null;

      for (String var6 : new String[]{"ITEM", "f_257033_"}) {
         try {
            Field var7 = var1.getField(var6);
            var2 = var7.get(null);
            if (var2 != null) {
               break;
            }
         } catch (ReflectiveOperationException var13) {
         }
      }

      if (var2 == null) {
         return null;
      } else {
         Class var14 = Class.forName("net.minecraft.resources.ResourceLocation");
         Object var15 = var14.getConstructor(String.class).newInstance(var0);
         Object var16 = null;

         for (String var9 : new String[]{"get", "m_7745_"}) {
            try {
               Method var10 = var2.getClass().getMethod(var9, var14);
               var16 = var10.invoke(var2, var15);
               if (var16 != null) {
                  break;
               }
            } catch (ReflectiveOperationException var12) {
            }
         }

         if (var16 == null) {
            return null;
         } else {
            Class var18 = Class.forName("net.minecraft.world.item.ItemStack");

            for (Constructor var23 : var18.getConstructors()) {
               Class[] var11 = var23.getParameterTypes();
               if (var11.length == 1 && var11[0].isInstance(var16)) {
                  return var23.newInstance(var16);
               }
            }

            return null;
         }
      }
   }

   private static Field findField(Class<?> var0, String var1) {
      for (Class var2 = var0; var2 != null; var2 = var2.getSuperclass()) {
         try {
            return var2.getDeclaredField(var1);
         } catch (NoSuchFieldException var4) {
         }
      }

      return null;
   }

   private static void invokeNoArg(Object var0, String... var1) {
      for (String var5 : var1) {
         try {
            Method var6 = var0.getClass().getMethod(var5);
            var6.invoke(var0);
            return;
         } catch (Throwable var7) {
         }
      }
   }

   private static Object enumConstant(Class<?> var0, String var1) {
      Object[] var2 = var0.getEnumConstants();
      if (var2 != null) {
         for (Object var6 : var2) {
            if (((Enum)var6).name().equals(var1)) {
               return var6;
            }
         }
      }

      return null;
   }

   private static double lerp(double var0, double var2, double var4) {
      return var0 + (var2 - var0) * var4;
   }

   private static double clamp(double var0) {
      return Math.max(0.0, Math.min(1.0, var0));
   }
}
