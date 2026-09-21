package com.gx.velveteclipse.forge;
import java.lang.reflect.Constructor;

import com.gx.velveteclipse.VelvetEclipseMod;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.ForgeRegistries.Keys;

@EventBusSubscriber(
   modid = "velvet_eclipse",
   bus = Bus.MOD
)
public final class ShadowRegistry {
   public static final ResourceLocation SHADOW_ID = new ResourceLocation("velvet_eclipse", "shadow");
   private static EntityType<?> SHADOW;

   private ShadowRegistry() {
   }

   @SubscribeEvent
   public static void onRegister(RegisterEvent var0) {
      var0.register(Keys.ENTITY_TYPES, var0x -> {
         EntityType var1 = buildShadowType();
         if (var1 == null) {
            VelvetEclipseMod.LOGGER.error("Velvet Eclipse: could not construct shadow entity type.");
         } else {
            SHADOW = var1;
            var0x.register(SHADOW_ID, var1);
            VelvetEclipseMod.LOGGER.info("Velvet Eclipse: registered entity velvet_eclipse:shadow.");
         }
      });
   }

   @SubscribeEvent
   public static void onAttributes(EntityAttributeCreationEvent var0) {
      if (SHADOW == null) {
         VelvetEclipseMod.LOGGER.error("Velvet Eclipse: cannot register Shadow attributes because entity type is null.");
      } else {
         try {
            Class var1 = Class.forName("net.minecraft.world.entity.Mob");
            Method var2 = findMethod(var1, new String[]{"createMobAttributes", "m_21552_"}, true, 0);
            if (var2 == null) {
               Class var3 = Class.forName("net.minecraft.world.entity.LivingEntity");
               var2 = findMethod(var3, new String[]{"createLivingAttributes", "m_21183_"}, true, 0);
            }

            if (var2 == null) {
               throw new NoSuchMethodException("Mob/LivingEntity attribute builder");
            }

            Object var8 = var2.invoke(null);
            if (var8 == null) {
               throw new IllegalStateException("LivingEntity attribute builder was null");
            }

            Method var4 = findMethod(var8.getClass(), new String[]{"build", "m_22265_"}, false, 0);
            if (var4 == null) {
               throw new NoSuchMethodException("AttributeSupplier.Builder.build/m_22265_");
            }

            Object var5 = var4.invoke(var8);
            if (!(var5 instanceof AttributeSupplier var6)) {
               throw new IllegalStateException("Unexpected attribute supplier: " + var5);
            }

            var0.put((EntityType)SHADOW, var6);
            VelvetEclipseMod.LOGGER.info("Velvet Eclipse: registered default attributes for velvet_eclipse:shadow.");
         } catch (Throwable var7) {
            VelvetEclipseMod.LOGGER.error("Velvet Eclipse: Shadow attribute registration FAILED: " + var7);
         }
      }
   }

   public static EntityType<?> shadowType() {
      return SHADOW;
   }

   public static boolean isRegistered() {
      return SHADOW != null;
   }

   public static boolean isShadow(Object var0) {
      if(var0 instanceof com.gx.velveteclipse.shadow.FieldShadowEntity)return true;
      if (var0 != null && SHADOW != null) {
         try {
            Object var1 = invokeNoArg(var0, "getType", "m_6095_");
            return var1 == SHADOW;
         } catch (Throwable var2) {
            return false;
         }
      } else {
         return false;
      }
   }

   private static EntityType<?> buildShadowType() {
      try {
         Class var0 = Class.forName("net.minecraft.world.entity.EntityType");
         Class var1 = Class.forName("net.minecraft.world.entity.EntityType$Builder");
         Class var2 = Class.forName("net.minecraft.world.entity.EntityType$EntityFactory");
         Class var3 = Class.forName("net.minecraft.world.entity.MobCategory");
         Class var4 = Class.forName("com.gx.velveteclipse.entity.VelvetShadowGeoEntity");
         Object var5 = Proxy.newProxyInstance(
            var2.getClassLoader(),
            new Class[]{var2},
            (proxy, method, args) -> {
               if(method.getDeclaringClass()==Object.class) return switch(method.getName()) {
                  case "toString" -> "VelvetShadowFactory";
                  case "hashCode" -> System.identityHashCode(proxy);
                  case "equals" -> proxy==(args==null||args.length==0?null:args[0]);
                  default -> null;
               };
               if(args!=null&&args.length>=2) for(Constructor<?> c:var4.getConstructors()) {
                  Class<?>[] types=c.getParameterTypes();
                  if(types.length==2&&types[0].isInstance(args[0])&&types[1].isInstance(args[1])) {
                     Object mob=c.newInstance(args[0],args[1]);configureShadowMob(mob);return mob;
                  }
               }
               return null;
            }
         );
         Object[] var6 = var3.getEnumConstants();
         if (var6 != null && var6.length != 0) {
            Object var7 = var6[0];

            for (Object var11 : var6) {
               if (var11 instanceof Enum var12 && "MONSTER".equals(var12.name())) {
                  var7 = var11;
                  break;
               }
            }

            Method var17 = findMethod(var1, new String[]{"of", "m_20704_"}, true, 2, var2, var3);
            if (var17 == null) {
               for (Method var24 : var1.getMethods()) {
                  if (Modifier.isStatic(var24.getModifiers())
                     && var24.getParameterCount() == 2
                     && var24.getReturnType() == var1
                     && var24.getParameterTypes()[0].isAssignableFrom(var2)
                     && var24.getParameterTypes()[1] == var3) {
                     var17 = var24;
                     break;
                  }
               }
            }

            if (var17 == null) {
               return null;
            } else {
               Object var19 = var17.invoke(null, var5, var7);
               Method var21 = findMethod(var1, new String[]{"sized", "m_20699_"}, false, 2, float.class, float.class);
               if (var21 != null) {
                  var19 = var21.invoke(var19, 1.2F, 1.35F);
               }

               Method var23 = findMethod(var1, new String[]{"clientTrackingRange", "m_20702_"}, false, 1, int.class);
               if (var23 != null) {
                  var19 = var23.invoke(var19, 8);
               }

               Method var25 = findMethod(var1, new String[]{"updateInterval", "m_20717_"}, false, 1, int.class);
               if (var25 != null) {
                  var19 = var25.invoke(var19, 3);
               }

               Method var13 = findMethod(var1, new String[]{"build", "m_20712_"}, false, 1, String.class);
               if (var13 == null) {
                  return null;
               } else {
                  Object var14 = var13.invoke(var19, SHADOW_ID.toString());
                  return var14 instanceof EntityType var15 ? var15 : (EntityType)var14;
               }
            }
         } else {
            return null;
         }
      } catch (Throwable var16) {
         VelvetEclipseMod.LOGGER.error("Velvet Eclipse shadow registry build failed: " + var16);
         return null;
      }
   }

   private static void configureShadowMob(Object var0) {
      if (var0 != null) {
         for (String var4 : new String[]{"setPersistenceRequired", "m_21530_"}) {
            try {
               var0.getClass().getMethod(var4).invoke(var0);
               break;
            } catch (Throwable var7) {
            }
         }

         for (String var11 : new String[]{"addTag", "m_20049_"}) {
            try {
               var0.getClass().getMethod(var11, String.class).invoke(var0, "velvet_shadow_mob");
               break;
            } catch (Throwable var6) {
            }
         }
      }
   }

   private static Method findMethod(Class<?> var0, String[] var1, boolean var2, int var3, Class<?>... var4) {
      for (String var8 : var1) {
         try {
            Method var9 = var0.getMethod(var8, var4);
            if (!var2 || Modifier.isStatic(var9.getModifiers())) {
               return var9;
            }
         } catch (Throwable var12) {
         }
      }

      for (Method var16 : var0.getMethods()) {
         if (var16.getParameterCount() == var3 && (!var2 || Modifier.isStatic(var16.getModifiers()))) {
            Class[] var17 = var16.getParameterTypes();
            boolean var10 = var17.length == var4.length;

            for (int var11 = 0; var10 && var11 < var17.length; var11++) {
               if (var4[var11].isPrimitive()) {
                  var10 = var17[var11] == var4[var11];
               } else {
                  var10 = var17[var11].isAssignableFrom(var4[var11]) || var4[var11].isAssignableFrom(var17[var11]);
               }
            }

            if (var10) {
               return var16;
            }
         }
      }

      return null;
   }

   private static Object invokeNoArg(Object var0, String... var1) throws Exception {
      for (String var5 : var1) {
         try {
            return var0.getClass().getMethod(var5).invoke(var0);
         } catch (NoSuchMethodException var7) {
         }
      }

      for (Method var11 : var0.getClass().getMethods()) {
         if (var11.getParameterCount() == 0 && var11.getReturnType().getName().equals("net.minecraft.world.entity.EntityType")) {
            return var11.invoke(var0);
         }
      }

      return null;
   }
}
