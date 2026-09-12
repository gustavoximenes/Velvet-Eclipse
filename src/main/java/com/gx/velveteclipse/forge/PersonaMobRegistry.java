package com.gx.velveteclipse.forge;

import com.gx.velveteclipse.VelvetEclipseMod;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.LinkedHashMap;
import java.util.Map;
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
public final class PersonaMobRegistry {
   private static final String[] IDS = new String[]{"cinder_wisp", "umbra_slime", "nocturne_knight", "sylphid", "iron_oni", "lumen_hare", "izanagi", "evoker_fx"};
   private static final Map<String, EntityType<?>> TYPES = new LinkedHashMap<>();

   private PersonaMobRegistry() {
   }

   @SubscribeEvent
   public static void onRegister(RegisterEvent var0) {
      var0.register(Keys.ENTITY_TYPES, var0x -> {
         for (String var4 : IDS) {
            EntityType var5 = buildVexType(var4);
            if (var5 != null) {
               var0x.register(new ResourceLocation("velvet_eclipse", "persona_" + var4), var5);
               TYPES.put(var4, var5);
            }
         }
      });
   }

   @SubscribeEvent
   public static void onAttributes(EntityAttributeCreationEvent var0) {
      try {
         Method var1 = findMethod(Class.forName("net.minecraft.world.entity.monster.Vex"), new String[]{"createAttributes", "m_34040_"}, true, 0);
         if (var1 == null) {
            var1 = findMethod(Class.forName("net.minecraft.world.entity.Mob"), new String[]{"createMobAttributes", "m_21552_"}, true, 0);
         }

         if (var1 == null) {
            throw new NoSuchMethodException("Vex/Mob attribute builder");
         }

         Object var2 = var1.invoke(null);
         Method var3 = findMethod(var2.getClass(), new String[]{"build", "m_22265_"}, false, 0);
         if (var3 == null) {
            throw new NoSuchMethodException("AttributeSupplier.Builder.build");
         }

         if (!(var3.invoke(var2) instanceof AttributeSupplier var5)) {
            throw new IllegalStateException("Unexpected attribute supplier");
         }

         for (EntityType var7 : TYPES.values()) {
            var0.put(var7, var5);
         }

         VelvetEclipseMod.LOGGER.info("Registered persona entity attributes: " + TYPES.size());
      } catch (Throwable var8) {
         VelvetEclipseMod.LOGGER.error("Failed to register persona mob attributes: " + var8, var8);
      }
   }

   public static EntityType<?> typeFor(String var0) {
      EntityType var1 = TYPES.get(var0);
      return var1 != null ? var1 : TYPES.get("cinder_wisp");
   }

   public static Map<String, EntityType<?>> all() {
      return Map.copyOf(TYPES);
   }

   public static boolean isRegistered() {
      return TYPES.size() == IDS.length;
   }

   private static EntityType<?> buildVexType(String var0) {
      try {
         Class var1 = Class.forName("net.minecraft.world.entity.EntityType$Builder");
         Class var2 = Class.forName("net.minecraft.world.entity.EntityType$EntityFactory");
         Class var3 = Class.forName("net.minecraft.world.entity.MobCategory");
         Class var4 = Class.forName("com.gx.velveteclipse.entity.VelvetPersonaGeoEntity");
         Object var5 = Proxy.newProxyInstance(var2.getClassLoader(), new Class[]{var2}, (var1x, var2x, var3x) -> {
            Constructor var4x = var4.getConstructor(Class.forName("net.minecraft.world.entity.EntityType"), Class.forName("net.minecraft.world.level.Level"));
            return var4x.newInstance(var3x[0], var3x[1]);
         });
         Object var6 = enumConstant(var3, "CREATURE");
         if (var6 == null) {
            var6 = enumConstant(var3, "MISC");
         }

         Method var7 = findMethod(var1, new String[]{"of", "m_20704_"}, true, 2, var2, var3);
         if (var7 == null) {
            return null;
         } else {
            Object var8 = var7.invoke(null, var5, var6);
            Method var9 = findMethod(var1, new String[]{"sized", "m_20699_"}, false, 2, float.class, float.class);
            if (var9 != null) {
               float var10 = "evoker_fx".equals(var0) ? 0.65F : 0.9F;
               float var11 = "evoker_fx".equals(var0) ? 0.45F : 1.85F;
               var8 = var9.invoke(var8, var10, var11);
            }

            Method var14 = findMethod(var1, new String[]{"clientTrackingRange", "m_20702_"}, false, 1, int.class);
            if (var14 != null) {
               var8 = var14.invoke(var8, 10);
            }

            Method var15 = findMethod(var1, new String[]{"updateInterval", "m_20717_"}, false, 1, int.class);
            if (var15 != null) {
               var8 = var15.invoke(var8, 2);
            }

            Method var12 = findMethod(var1, new String[]{"build", "m_20712_"}, false, 1, String.class);
            return var12 == null ? null : (EntityType)var12.invoke(var8, "velvet_eclipse:persona_" + var0);
         }
      } catch (Throwable var13) {
         VelvetEclipseMod.LOGGER.error("Failed building persona type " + var0 + ": " + var13);
         return null;
      }
   }

   public static void configurePersonaMob(Object var0, String var1) {
      if (var0 != null) {
         invokeBoolean(var0, true, "setNoAi", "m_21557_");
         invokeBoolean(var0, true, "setInvulnerable", "m_20331_");
         invokeBoolean(var0, true, "setSilent", "m_20256_");
         invokeBoolean(var0, true, "setNoGravity", "m_20242_");
         invokeVoid(var0, "setPersistenceRequired", "m_21530_");
         addTag(var0, "velvet_persona_actor");
         addTag(var0, "persona:" + var1);

         try {
            var0.getClass().getMethod("setPersonaId", String.class).invoke(var0, var1);
         } catch (Throwable var3) {
         }
      }
   }

   private static void addTag(Object var0, String var1) {
      for (String var5 : new String[]{"addTag", "m_20049_"}) {
         try {
            var0.getClass().getMethod(var5, String.class).invoke(var0, var1);
            return;
         } catch (Throwable var7) {
         }
      }
   }

   private static void invokeBoolean(Object var0, boolean var1, String... var2) {
      for (String var6 : var2) {
         try {
            var0.getClass().getMethod(var6, boolean.class).invoke(var0, var1);
            return;
         } catch (Throwable var8) {
         }
      }
   }

   private static void invokeVoid(Object var0, String... var1) {
      for (String var5 : var1) {
         try {
            var0.getClass().getMethod(var5).invoke(var0);
            return;
         } catch (Throwable var7) {
         }
      }
   }

   private static Object enumConstant(Class<?> var0, String var1) {
      Object[] var2 = var0.getEnumConstants();
      if (var2 != null) {
         for (Object var6 : var2) {
            if (var6 instanceof Enum var7 && var1.equals(var7.name())) {
               return var6;
            }
         }

         if (var2.length > 0) {
            return var2[0];
         }
      }

      return null;
   }

   private static Method findMethod(Class<?> var0, String[] var1, boolean var2, int var3, Class<?>... var4) {
      for (String var8 : var1) {
         try {
            Method var9 = var0.getMethod(var8, var4);
            if (!var2 || Modifier.isStatic(var9.getModifiers())) {
               return var9;
            }
         } catch (Throwable var13) {
         }
      }

      for (Method var17 : var0.getMethods()) {
         if (var17.getParameterCount() == var3) {
            for (String var12 : var1) {
               if (var12.equals(var17.getName()) && (!var2 || Modifier.isStatic(var17.getModifiers()))) {
                  return var17;
               }
            }
         }
      }

      return null;
   }
}
