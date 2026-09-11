package com.gx.velveteclipse.forge;

import com.gx.velveteclipse.VelvetEclipseMod;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.RegisterEvent;

public final class ModelItemRegistry {
   private static final String[] MODELS = new String[]{"cinder_wisp", "nocturne_knight", "sylphid", "lumen_hare", "iron_oni", "umbra_slime", "evoker"};
   private static final Map<String, Object> ITEMS = new LinkedHashMap<>();
   private static boolean registrationSubmitted;

   private ModelItemRegistry() {
   }

   @SubscribeEvent
   public static void onRegister(RegisterEvent var0) {
      try {
         Class var1 = Class.forName("net.minecraftforge.registries.ForgeRegistries$Keys");
         Field var2 = var1.getField("ITEMS");
         Object var3 = var2.get(null);
         if (var3 != null) {
            Object var4 = var0.getClass().getMethod("getRegistryKey").invoke(var0);
            if (var3.equals(var4)) {
               if (!registrationSubmitted) {
                  Method var5 = null;

                  for (Method var9 : var0.getClass().getMethods()) {
                     if (var9.getName().equals("register") && var9.getParameterCount() == 3) {
                        Class[] var10 = var9.getParameterTypes();
                        if (ResourceLocation.class.isAssignableFrom(var10[1]) && Supplier.class.isAssignableFrom(var10[2])) {
                           var5 = var9;
                           break;
                        }
                     }
                  }

                  if (var5 == null) {
                     throw new NoSuchMethodException("RegisterEvent.register(ResourceKey,ResourceLocation,Supplier)");
                  } else {
                     for (String var16 : MODELS) {
                        ResourceLocation var17 = new ResourceLocation("velvet_eclipse", "model_" + var16);
                        Supplier var11 = () -> {
                           try {
                              Object var1x = EvokerItemFactory.createForModel(var16);
                              if (var1x == null) {
                                 throw new IllegalStateException("Item constructor unavailable");
                              } else {
                                 ITEMS.put(var16, var1x);
                                 return var1x;
                              }
                           } catch (Throwable var2x) {
                              throw new IllegalStateException("Failed to create render item " + var16, var2x);
                           }
                        };
                        var5.invoke(var0, var3, var17, var11);
                     }

                     registrationSubmitted = true;
                     VelvetEclipseMod.LOGGER.info("Velvet Eclipse: registered 7 OBJ render items in the ITEMS registry.");
                  }
               }
            }
         }
      } catch (Throwable var12) {
         VelvetEclipseMod.LOGGER.error("Velvet Eclipse OBJ item registry failed: " + var12, new Object[]{var12});
         throw new RuntimeException("Velvet Eclipse OBJ item registration failed", var12);
      }
   }

   public static String itemId(String var0) {
      String var1 = isKnown(var0) ? var0 : "cinder_wisp";
      return "velvet_eclipse:model_" + var1;
   }

   public static boolean isKnown(String var0) {
      if (var0 == null) {
         return false;
      } else {
         for (String var4 : MODELS) {
            if (var4.equals(var0)) {
               return true;
            }
         }

         return false;
      }
   }

   public static int registeredCount() {
      return ITEMS.size();
   }

   private static Object createPlainItem() throws Exception {
      Class var0 = Class.forName("net.minecraft.world.item.Item");
      Class var1 = Class.forName("net.minecraft.world.item.Item$Properties");
      Object var2 = var1.getConstructor().newInstance();

      for (Constructor var6 : var0.getConstructors()) {
         if (var6.getParameterCount() == 1 && var6.getParameterTypes()[0].isInstance(var2)) {
            return var6.newInstance(var2);
         }
      }

      return null;
   }
}
