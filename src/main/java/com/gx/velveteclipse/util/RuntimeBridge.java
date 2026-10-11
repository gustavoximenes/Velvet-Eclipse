package com.gx.velveteclipse.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public final class RuntimeBridge {
   private RuntimeBridge() {
   }

   public static Object playerFromSource(Object var0) {
      if (var0 == null) {
         return null;
      } else {
         for (String var4 : new String[]{"getPlayer", "m_230896_", "getPlayerOrException", "m_81375_"}) {
            try {
               Method var5 = var0.getClass().getMethod(var4);
               Object var6 = var5.invoke(var0);
               if (var6 != null) {
                  return var6;
               }
            } catch (Throwable var8) {
            }
         }

         for (Method var12 : var0.getClass().getMethods()) {
            if (var12.getParameterCount() == 0 && var12.getReturnType().getName().endsWith("ServerPlayer")) {
               try {
                  Object var13 = var12.invoke(var0);
                  if (var13 != null) {
                     return var13;
                  }
               } catch (Throwable var7) {
               }
            }
         }

         return null;
      }
   }

   public static UUID playerUuid(Object var0) {
      if (var0 == null) {
         return UUID.randomUUID();
      } else {
         for (String var4 : new String[]{"getUUID", "m_20148_"}) {
            try {
               Method var5 = var0.getClass().getMethod(var4);
               Object var6 = var5.invoke(var0);
               if (var6 instanceof UUID) {
                  return (UUID)var6;
               }
            } catch (Throwable var9) {
            }
         }

         for (Method var13 : var0.getClass().getMethods()) {
            if (var13.getParameterCount() == 0 && var13.getReturnType() == UUID.class) {
               try {
                  Object var14 = var13.invoke(var0);
                  if (var14 instanceof UUID) {
                     return (UUID)var14;
                  }
               } catch (Throwable var8) {
               }
            }
         }

         return UUID.nameUUIDFromBytes(("velvet:" + System.identityHashCode(var0)).getBytes(StandardCharsets.UTF_8));
      }
   }

   public static String playerName(Object var0) {
      if (var0 == null) {
         return "Player";
      } else {
         for (String var4 : new String[]{"getScoreboardName", "m_6302_"}) {
            try {
               Method var5 = var0.getClass().getMethod(var4);
               Object var6 = var5.invoke(var0);
               if (var6 != null) {
                  return String.valueOf(var6);
               }
            } catch (Throwable var7) {
            }
         }

         return "Player";
      }
   }

   public static void sendMessage(Object var0, String var1) {
      if (var0 != null && var1 != null) {
         Object var2 = var0.getClass().getName().endsWith("CommandSourceStack") ? playerFromSource(var0) : var0;
         if (var2 != null) {
            try {
               Class var3 = Class.forName("net.minecraft.network.chat.Component");
               Object var4 = literalComponent(var3, var1);
               if (var4 == null) {
                  return;
               }

               for (String var8 : new String[]{"sendSystemMessage", "m_213846_"}) {
                  try {
                     Method var9 = var2.getClass().getMethod(var8, var3);
                     var9.invoke(var2, var4);
                     return;
                  } catch (Throwable var10) {
                  }
               }

               for (Method var16 : var2.getClass().getMethods()) {
                  if (var16.getParameterCount() == 1 && var16.getReturnType() == void.class && var16.getParameterTypes()[0].isAssignableFrom(var3)) {
                     try {
                        var16.invoke(var2, var4);
                        return;
                     } catch (Throwable var11) {
                     }
                  }
               }
            } catch (Throwable var12) {
            }
         }
      }
   }

   private static Object literalComponent(Class<?> componentType, String text) {
      return com.gx.velveteclipse.localization.ModText.component(text);
   }

   public static Object persistentData(Object var0) {
      if (var0 == null) {
         return null;
      } else {
         try {
            Method var8 = var0.getClass().getMethod("getPersistentData");
            return var8.invoke(var0);
         } catch (Throwable var7) {
            for (Method var4 : var0.getClass().getMethods()) {
               if (var4.getParameterCount() == 0 && var4.getReturnType().getName().endsWith("CompoundTag")) {
                  try {
                     return var4.invoke(var0);
                  } catch (Throwable var6) {
                  }
               }
            }

            return null;
         }
      }
   }

   public static String tagGetString(Object var0, String var1) {
      if (var0 == null) {
         return "";
      } else {
         for (String var5 : new String[]{"getString", "m_128461_"}) {
            try {
               Method var6 = var0.getClass().getMethod(var5, String.class);
               Object var7 = var6.invoke(var0, var1);
               return var7 == null ? "" : String.valueOf(var7);
            } catch (Throwable var9) {
            }
         }

         for (Method var13 : var0.getClass().getMethods()) {
            if (var13.getParameterCount() == 1 && var13.getParameterTypes()[0] == String.class && var13.getReturnType() == String.class) {
               try {
                  Object var14 = var13.invoke(var0, var1);
                  if (var14 != null) {
                     return String.valueOf(var14);
                  }
               } catch (Throwable var8) {
               }
            }
         }

         return "";
      }
   }

   public static void tagPutString(Object var0, String var1, String var2) {
      if (var0 != null) {
         for (String var6 : new String[]{"putString", "m_128359_"}) {
            try {
               Method var7 = var0.getClass().getMethod(var6, String.class, String.class);
               var7.invoke(var0, var1, var2);
               return;
            } catch (Throwable var9) {
            }
         }

         for (Method var13 : var0.getClass().getMethods()) {
            if (var13.getParameterCount() == 2 && Arrays.equals((Object[])var13.getParameterTypes(), (Object[])(new Class[]{String.class, String.class}))) {
               try {
                  var13.invoke(var0, var1, var2);
                  return;
               } catch (Throwable var8) {
               }
            }
         }
      }
   }

   public static String readUtf(Object var0) {
      if (var0 == null) {
         return "";
      } else {
         for (String var4 : new String[]{"readUtf", "m_130277_"}) {
            try {
               Method var5 = var0.getClass().getMethod(var4);
               Object var6 = var5.invoke(var0);
               if (var6 instanceof String) {
                  return (String)var6;
               }
            } catch (Throwable var8) {
            }
         }

         return "";
      }
   }

   public static void writeUtf(Object var0, String var1) {
      if (var0 != null) {
         for (String var5 : new String[]{"writeUtf", "m_130070_"}) {
            try {
               Method var6 = var0.getClass().getMethod(var5, String.class);
               var6.invoke(var0, var1 == null ? "" : var1);
               return;
            } catch (Throwable var8) {
            }
         }

         for (Method var12 : var0.getClass().getMethods()) {
            if (var12.getParameterCount() == 1 && var12.getParameterTypes()[0] == String.class) {
               try {
                  var12.invoke(var0, var1 == null ? "" : var1);
                  return;
               } catch (Throwable var7) {
               }
            }
         }
      }
   }

   public static Object minecraftInstance() {
      try {
         Class var0 = Class.forName("net.minecraft.client.Minecraft");

         for (String var4 : new String[]{"getInstance", "m_91087_"}) {
            try {
               Method var5 = var0.getMethod(var4);
               return var5.invoke(null);
            } catch (Throwable var6) {
            }
         }

         for (Method var12 : var0.getMethods()) {
            if (Modifier.isStatic(var12.getModifiers()) && var12.getParameterCount() == 0 && var12.getReturnType() == var0) {
               try {
                  return var12.invoke(null);
               } catch (Throwable var7) {
               }
            }
         }
      } catch (Throwable var8) {
      }

      return null;
   }

   public static Object clientPlayer() {
      Object var0 = minecraftInstance();
      if (var0 == null) {
         return null;
      } else {
         try {
            for (String var4 : new String[]{"player", "f_91074_"}) {
               try {
                  Field var5 = var0.getClass().getField(var4);
                  Object var6 = var5.get(var0);
                  if (var6 != null) {
                     return var6;
                  }
               } catch (Throwable var7) {
               }
            }

            return findFieldValueByTypeSuffix(var0, "LocalPlayer");
         } catch (Throwable var8) {
            return null;
         }
      }
   }

   public static Object minecraftFont() {
      Object var0 = minecraftInstance();
      if (var0 == null) {
         return null;
      } else {
         try {
            Class var1 = Class.forName("net.minecraft.client.gui.Font");

            for (Field var5 : var0.getClass().getFields()) {
               if (var1.isAssignableFrom(var5.getType())) {
                  try {
                     return var5.get(var0);
                  } catch (Throwable var7) {
                  }
               }
            }

            for (Field var13 : var0.getClass().getDeclaredFields()) {
               if (var1.isAssignableFrom(var13.getType())) {
                  try {
                     var13.setAccessible(true);
                     return var13.get(var0);
                  } catch (Throwable var8) {
                  }
               }
            }
         } catch (Throwable var9) {
         }

         return null;
      }
   }

   public static void guiFill(Object var0, int var1, int var2, int var3, int var4, int var5) {
      if (var0 != null) {
         for (String var9 : new String[]{"fill", "m_280509_"}) {
            try {
               Method var10 = var0.getClass().getMethod(var9, int.class, int.class, int.class, int.class, int.class);
               var10.invoke(var0, var1, var2, var3, var4, var5);
               return;
            } catch (Throwable var13) {
            }
         }

         for (Method var17 : var0.getClass().getMethods()) {
            Class[] var18 = var17.getParameterTypes();
            if (var17.getReturnType() == void.class
               && var18.length == 5
               && var18[0] == int.class
               && var18[1] == int.class
               && var18[2] == int.class
               && var18[3] == int.class
               && var18[4] == int.class) {
               try {
                  var17.invoke(var0, var1, var2, var3, var4, var5);
                  return;
               } catch (Throwable var12) {
               }
            }
         }
      }
   }

   public static void guiText(Object var0, String var1, int var2, int var3, int var4, boolean var5) {
      var1=com.gx.velveteclipse.localization.DisplayText.text(var1);
      if (var0 != null) {
         Object var6 = minecraftFont();
         if (var6 != null) {
            try {
               Class var7 = Class.forName("net.minecraft.client.gui.Font");

               for (String var11 : new String[]{"drawString", "m_280056_"}) {
                  try {
                     Method var12 = var0.getClass().getMethod(var11, var7, String.class, int.class, int.class, int.class, boolean.class);
                     var12.invoke(var0, var6, var1, var2, var3, var4, var5);
                     return;
                  } catch (Throwable var14) {
                  }
               }

               for (Method var20 : var0.getClass().getMethods()) {
                  Class[] var21 = var20.getParameterTypes();
                  if (var21.length == 6
                     && var21[0].isAssignableFrom(var7)
                     && var21[1] == String.class
                     && var21[2] == int.class
                     && var21[3] == int.class
                     && var21[4] == int.class
                     && var21[5] == boolean.class) {
                     try {
                        var20.invoke(var0, var6, var1, var2, var3, var4, var5);
                        return;
                     } catch (Throwable var15) {
                     }
                  }
               }
            } catch (Throwable var16) {
            }
         }
      }
   }

   public static double[] playerPosition(Object var0) {
      return var0 == null
         ? new double[]{0.0, 0.0, 0.0}
         : new double[]{invokeDouble(var0, "getX", "m_20185_"), invokeDouble(var0, "getY", "m_20186_"), invokeDouble(var0, "getZ", "m_20189_")};
   }

   public static float playerYaw(Object var0) {
      if (var0 == null) {
         return 0.0F;
      } else {
         for (String var4 : new String[]{"getYRot", "m_146908_"}) {
            try {
               Method var5 = var0.getClass().getMethod(var4);
               if (var5.invoke(var0) instanceof Number var7) {
                  return var7.floatValue();
               }
            } catch (Throwable var8) {
            }
         }

         return 0.0F;
      }
   }

   public static float playerPitch(Object var0) {
      if (var0 == null) {
         return 0.0F;
      } else {
         for (String var4 : new String[]{"getXRot", "m_146909_"}) {
            try {
               Method var5 = var0.getClass().getMethod(var4);
               if (var5.invoke(var0) instanceof Number var7) {
                  return var7.floatValue();
               }
            } catch (Throwable var8) {
            }
         }

         return 0.0F;
      }
   }

   public static void setPlayerRotation(Object var0, float var1, float var2) {
      if (var0 != null) {
         for (String var6 : new String[]{"setYRot", "m_146922_"}) {
            try {
               var0.getClass().getMethod(var6, float.class).invoke(var0, var1);
               break;
            } catch (Throwable var9) {
            }
         }

         for (String var13 : new String[]{"setXRot", "m_146926_"}) {
            try {
               var0.getClass().getMethod(var13, float.class).invoke(var0, var2);
               break;
            } catch (Throwable var8) {
            }
         }
      }
   }

   public static double[] rotateLocal(double var0, double var2, float var4) {
      double var5 = Math.toRadians((double)(-var4));
      double var7 = Math.cos(var5);
      double var9 = Math.sin(var5);
      return new double[]{var0 * var7 - var2 * var9, var0 * var9 + var2 * var7};
   }

   public static double[] localOffset(double[] var0, float var1, double var2, double var4, double var6) {
      double var8 = Math.toRadians((double)var1);
      double var10 = -Math.sin(var8);
      double var12 = Math.cos(var8);
      double var14 = Math.cos(var8);
      double var16 = Math.sin(var8);
      return new double[]{var0[0] + var14 * var2 + var10 * var6, var0[1] + var4, var0[2] + var16 * var2 + var12 * var6};
   }

   public static UUID spawnBlockDisplay(
      Object var0, String var1, double var2, double var4, double var6, float var8, float var9, float var10, float var11, String var12
   ) {
      try {
         Object var13 = serverLevel(var0);
         if (var13 == null) {
            return null;
         } else {
            Class var14 = Class.forName("net.minecraft.world.entity.EntityType");
            Object var15 = builtInRegistryValue("ENTITY_TYPE", "f_256780_", "minecraft:block_display");
            if (var15 == null) {
               for (String var19 : new String[]{"BLOCK_DISPLAY", "f_268573_"}) {
                  try {
                     var15 = var14.getField(var19).get(null);
                     break;
                  } catch (Throwable var31) {
                  }
               }
            }

            if (var15 == null) {
               return null;
            } else {
               Class var33 = Class.forName("net.minecraft.world.entity.Display$BlockDisplay");
               Object var34 = null;

               for (Method var21 : var14.getMethods()) {
                  if (var21.getName().equals("create") && var21.getParameterCount() == 1 && var21.getParameterTypes()[0].isInstance(var13)) {
                     var34 = var21.invoke(var15, var13);
                     if (var34 != null) {
                        break;
                     }
                  }
               }

               if (var34 == null) {
                  for (Constructor var46 : var33.getConstructors()) {
                     Class[] var22 = var46.getParameterTypes();
                     if (var22.length == 2 && var14.isAssignableFrom(var22[0]) && var22[1].isInstance(var13)) {
                        var34 = var46.newInstance(var15, var13);
                        break;
                     }
                  }
               }

               if (var34 == null) {
                  return null;
               } else {
                  boolean var37 = configureBlockDisplayByNbt(var34, var1, var2, var4, var6, var8, var9, var10, var11, var12);
                  if (!var37) {
                     Object var40 = builtInRegistryValue("BLOCK", "f_256975_", "minecraft:" + (var1 == null ? "stone" : var1.toLowerCase(Locale.ROOT)));
                     if (var40 == null) {
                        return null;
                     }

                     Object var43 = null;

                     for (String var24 : new String[]{"defaultBlockState", "m_49966_"}) {
                        try {
                           var43 = var40.getClass().getMethod(var24).invoke(var40);
                           break;
                        } catch (Throwable var30) {
                        }
                     }

                     if (var43 != null) {
                        Method var48 = findSingleArgMethod(var33, "setBlockState", var43.getClass());
                        if (var48 == null) {
                           var48 = findMethodByParamSuffix(var33, "BlockState");
                        }

                        if (var48 != null) {
                           try {
                              var48.setAccessible(true);
                              var48.invoke(var34, var43);
                           } catch (Throwable var28) {
                           }
                        }
                     }

                     Method var49 = findExactMethod(var34.getClass(), "setPos", double.class, double.class, double.class);
                     if (var49 == null) {
                        var49 = findExactMethod(var34.getClass(), "m_20343_", double.class, double.class, double.class);
                     }

                     if (var49 != null) {
                        var49.invoke(var34, var2, var4, var6);
                     }

                     invokeBooleanSetter(var34, true, "setNoGravity", "m_20242_");

                     for (String var25 : new String[]{"addTag", "m_20049_"}) {
                        try {
                           var34.getClass().getMethod(var25, String.class).invoke(var34, var12 == null ? "velvet_visual" : var12);
                           break;
                        } catch (Throwable var29) {
                        }
                     }
                  }

                  Method var41 = null;

                  for (String var57 : new String[]{"addFreshEntity", "m_7967_"}) {
                     for (Method var27 : var13.getClass().getMethods()) {
                        if (var27.getName().equals(var57) && var27.getParameterCount() == 1 && var27.getParameterTypes()[0].isInstance(var34)) {
                           var41 = var27;
                           break;
                        }
                     }

                     if (var41 != null) {
                        break;
                     }
                  }

                  if (var41 == null) {
                     for (Method var58 : var13.getClass().getMethods()) {
                        if (var58.getParameterCount() == 1
                           && var58.getReturnType() == boolean.class
                           && var58.getParameterTypes()[0].getName().equals("net.minecraft.world.entity.Entity")) {
                           var41 = var58;
                           break;
                        }
                     }
                  }

                  if (var41 != null) {
                     var41.invoke(var13, var34);
                  }

                  return playerUuid(var34);
               }
            }
         }
      } catch (Throwable var32) {
         return null;
      }
   }

   private static boolean configureBlockDisplayByNbt(
      Object var0, String var1, double var2, double var4, double var6, float var8, float var9, float var10, float var11, String var12
   ) {
      try {
         Class var13 = Class.forName("net.minecraft.nbt.TagParser");
         Method var14 = null;

         for (Method var18 : var13.getMethods()) {
            if (Modifier.isStatic(var18.getModifiers())
               && var18.getParameterCount() == 1
               && var18.getParameterTypes()[0] == String.class
               && var18.getReturnType().getName().equals("net.minecraft.nbt.CompoundTag")
               && (var18.getName().equals("parseTag") || var18.getName().startsWith("m_"))) {
               var14 = var18;
               break;
            }
         }

         if (var14 == null) {
            return false;
         } else {
            double var29 = Math.toRadians((double)var11) * 0.5;
            float var30 = (float)Math.sin(var29);
            float var31 = (float)Math.cos(var29);
            String var19 = "minecraft:" + (var1 == null ? "stone" : var1.toLowerCase(Locale.ROOT));
            String var20 = (var12 == null ? "velvet_visual" : var12).replace("\\", "").replace("\"", "");
            String var21 = String.format(
               Locale.ROOT,
               "{Pos:[%.6fd,%.6fd,%.6fd],NoGravity:1b,Tags:[\"%s\"],block_state:{Name:\"%s\"},transformation:{translation:[0.0f,0.0f,0.0f],left_rotation:[0.0f,%.8ff,0.0f,%.8ff],scale:[%.6ff,%.6ff,%.6ff],right_rotation:[0.0f,0.0f,0.0f,1.0f]}}",
               var2,
               var4,
               var6,
               var20,
               var19,
               var30,
               var31,
               var8,
               var9,
               var10
            );
            Object var22 = var14.invoke(null, var21);
            if (var22 == null) {
               return false;
            } else {
               Method var23 = null;

               for (Method var27 : var0.getClass().getMethods()) {
                  if ((var27.getName().equals("load") || var27.getName().equals("m_20258_"))
                     && var27.getParameterCount() == 1
                     && var27.getParameterTypes()[0].getName().equals("net.minecraft.nbt.CompoundTag")) {
                     var23 = var27;
                     break;
                  }
               }

               if (var23 == null) {
                  return false;
               } else {
                  var23.invoke(var0, var22);
                  return true;
               }
            }
         }
      } catch (Throwable var28) {
         return false;
      }
   }

   public static UUID spawnItemDisplay(Object var0, String var1, double var2, double var4, double var6, float var8, float var9, String var10) {
      return spawnMeshCarrier(var0, var1, var2, var4, var6, var8, var9, var10);
   }

   public static UUID spawnMeshCarrier(Object var0, String var1, double var2, double var4, double var6, float var8, float var9, String var10) {
      try {
         Object var11 = serverLevel(var0);
         if (var11 != null && var1 != null && !var1.isBlank()) {
            Object var12 = builtInRegistryValue("ENTITY_TYPE", "f_256780_", "minecraft:armor_stand");
            if (var12 == null) {
               return null;
            } else {
               Object var13 = null;

               for (String var17 : new String[]{"create", "m_20615_"}) {
                  for (Method var21 : var12.getClass().getMethods()) {
                     if (var21.getName().equals(var17) && var21.getParameterCount() == 1 && var21.getParameterTypes()[0].isInstance(var11)) {
                        try {
                           var13 = var21.invoke(var12, var11);
                        } catch (Throwable var27) {
                        }

                        if (var13 != null) {
                           break;
                        }
                     }
                  }

                  if (var13 != null) {
                     break;
                  }
               }

               if (var13 == null) {
                  return null;
               } else {
                  Class var29 = Class.forName("net.minecraft.nbt.TagParser");
                  Method var30 = null;

                  for (Method var37 : var29.getMethods()) {
                     if (Modifier.isStatic(var37.getModifiers())
                        && var37.getParameterCount() == 1
                        && var37.getParameterTypes()[0] == String.class
                        && var37.getReturnType().getName().equals("net.minecraft.nbt.CompoundTag")) {
                        var30 = var37;
                        break;
                     }
                  }

                  if (var30 == null) {
                     return null;
                  } else {
                     String var32 = (var10 == null ? "velvet_mesh" : var10).replace("\\", "").replace("\"", "");
                     String var34 = var1.replace("\\", "").replace("\"", "");
                     double var36 = var4 - 1.72;
                     String var38 = String.format(
                        Locale.ROOT,
                        "{Pos:[%.6fd,%.6fd,%.6fd],Rotation:[%.4ff,0.0f],NoGravity:1b,Invisible:1b,Invulnerable:1b,Silent:1b,NoBasePlate:1b,ShowArms:0b,Tags:[\"%s\"],ArmorItems:[{},{},{},{id:\"%s\",Count:1b}]}",
                        var2,
                        var36,
                        var6,
                        var9,
                        var32,
                        var34
                     );
                     Object var39 = var30.invoke(null, var38);
                     Method var22 = null;

                     for (Method var26 : var13.getClass().getMethods()) {
                        if ((var26.getName().equals("load") || var26.getName().equals("m_20258_"))
                           && var26.getParameterCount() == 1
                           && var26.getParameterTypes()[0].getName().equals("net.minecraft.nbt.CompoundTag")) {
                           var22 = var26;
                           break;
                        }
                     }

                     if (var22 == null) {
                        return null;
                     } else {
                        var22.invoke(var13, var39);
                        return !addFreshEntity(var11, var13) ? null : playerUuid(var13);
                     }
                  }
               }
            }
         } else {
            return null;
         }
      } catch (Throwable var28) {
         return null;
      }
   }

   public static Object setThirdPersonBattleCamera(Object var0) {
      Object var1 = minecraftInstance();
      if (var1 == null) {
         return var0;
      } else {
         try {
            Object var2 = null;

            for (String var6 : new String[]{"options", "f_91066_"}) {
               try {
                  Field var7 = var1.getClass().getField(var6);
                  var2 = var7.get(var1);
                  if (var2 != null) {
                     break;
                  }
               } catch (Throwable var13) {
               }
            }

            if (var2 == null) {
               var2 = findFieldValueByTypeSuffix(var1, "Options");
            }

            if (var2 == null) {
               return var0;
            } else {
               Class var15 = Class.forName("net.minecraft.client.CameraType");
               Object var16 = var0;
               if (var0 == null) {
                  for (String var8 : new String[]{"getCameraType", "m_92176_"}) {
                     try {
                        var16 = var2.getClass().getMethod(var8).invoke(var2);
                        break;
                     } catch (Throwable var12) {
                     }
                  }
               }

               Object var18 = null;

               for (Object var9 : var15.getEnumConstants()) {
                  if (var9 instanceof Enum var10 && "THIRD_PERSON_BACK".equals(var10.name())) {
                     var18 = var9;
                     break;
                  }
               }

               if (var18 != null) {
                  for (String var27 : new String[]{"setCameraType", "m_92157_"}) {
                     try {
                        var2.getClass().getMethod(var27, var15).invoke(var2, var18);
                        return var16;
                     } catch (Throwable var11) {
                     }
                  }
               }

               return var16;
            }
         } catch (Throwable var14) {
            return var0;
         }
      }
   }

   public static void restoreCamera(Object var0) {
      if (var0 != null) {
         Object var1 = minecraftInstance();
         if (var1 != null) {
            try {
               Object var2 = null;

               for (String var6 : new String[]{"options", "f_91066_"}) {
                  try {
                     Field var7 = var1.getClass().getField(var6);
                     var2 = var7.get(var1);
                     if (var2 != null) {
                        break;
                     }
                  } catch (Throwable var9) {
                  }
               }

               if (var2 == null) {
                  var2 = findFieldValueByTypeSuffix(var1, "Options");
               }

               if (var2 == null) {
                  return;
               }

               Class var12 = Class.forName("net.minecraft.client.CameraType");

               for (String var16 : new String[]{"setCameraType", "m_92157_"}) {
                  try {
                     var2.getClass().getMethod(var16, var12).invoke(var2, var0);
                     return;
                  } catch (Throwable var10) {
                  }
               }
            } catch (Throwable var11) {
            }
         }
      }
   }

   public static void discardEntity(Object var0, UUID var1) {
      if (var0 != null && var1 != null) {
         try {
            Object var2 = serverLevel(var0);
            if (var2 == null) {
               return;
            }

            Object var3 = null;

            for (String var7 : new String[]{"getEntity", "m_8791_"}) {
               try {
                  Method var8 = var2.getClass().getMethod(var7, UUID.class);
                  var3 = var8.invoke(var2, var1);
                  if (var3 != null) {
                     break;
                  }
               } catch (Throwable var9) {
               }
            }

            if (var3 != null) {
               for (String var15 : new String[]{"discard", "m_146870_"}) {
                  try {
                     var3.getClass().getMethod(var15).invoke(var3);
                     return;
                  } catch (Throwable var10) {
                  }
               }
            }
         } catch (Throwable var11) {
         }
      }
   }

   public static void freezePlayer(Object var0, double[] var1) {
      if (var0 != null && var1 != null && var1.length >= 3) {
         try {
            Method var2 = findExactMethod(var0.getClass(), "setPos", double.class, double.class, double.class);
            if (var2 == null) {
               var2 = findExactMethod(var0.getClass(), "m_6034_", double.class, double.class, double.class);
            }

            if (var2 == null) {
               var2 = findExactMethod(var0.getClass(), "m_20343_", double.class, double.class, double.class);
            }

            if (var2 != null) {
               var2.invoke(var0, var1[0], var1[1], var1[2]);
            }
         } catch (Throwable var4) {
         }

         try {
            Method var5 = findExactMethod(var0.getClass(), "setDeltaMovement", double.class, double.class, double.class);
            if (var5 == null) {
               var5 = findExactMethod(var0.getClass(), "m_20334_", double.class, double.class, double.class);
            }

            if (var5 != null) {
               var5.invoke(var0, 0.0, 0.0, 0.0);
            }
         } catch (Throwable var3) {
         }
      }
   }

   public static void hardFreezePlayer(Object var0, double[] var1, float var2, float var3) {
      freezePlayer(var0, var1);
      setPlayerRotation(var0, var2, var3);

      try {
         Object var4 = findFieldValueByTypeSuffix(var0, "ServerGamePacketListenerImpl");
         if (var4 == null) {
            for (String var8 : new String[]{"connection", "f_8906_"}) {
               try {
                  Field var9 = var0.getClass().getField(var8);
                  var4 = var9.get(var0);
                  if (var4 != null) {
                     break;
                  }
               } catch (Throwable var11) {
               }
            }
         }

         if (var4 != null) {
            for (String var16 : new String[]{"teleport", "m_9774_"}) {
               try {
                  var4.getClass()
                     .getMethod(var16, double.class, double.class, double.class, float.class, float.class)
                     .invoke(var4, var1[0], var1[1], var1[2], var2, var3);
                  break;
               } catch (Throwable var10) {
               }
            }
         }
      } catch (Throwable var12) {
      }
   }

   public static void freezeLocalClientPlayer(double[] var0, float var1, float var2) {
      Object var3 = clientPlayer();
      if (var3 != null && var0 != null) {
         freezePlayer(var3, var0);
         setPlayerRotation(var3, var1, var2);
      }
   }

   public static int guiScaledWidth() {
      try {
         Object var0 = minecraftInstance();
         Object var1 = invokeNoArgByReturnSuffix(var0, "Window", "getWindow");
         return (int)Math.round(invokeDouble(var1, "getGuiScaledWidth", "m_85445_"));
      } catch (Throwable var2) {
         return 320;
      }
   }

   public static int guiScaledHeight() {
      try {
         Object var0 = minecraftInstance();
         Object var1 = invokeNoArgByReturnSuffix(var0, "Window", "getWindow");
         return (int)Math.round(invokeDouble(var1, "getGuiScaledHeight", "m_85446_"));
      } catch (Throwable var2) {
         return 240;
      }
   }

   public static double guiMouseX() {
      try {
         Object var0 = minecraftInstance();
         Object var1 = findFieldValueByTypeSuffix(var0, "MouseHandler");
         Object var2 = invokeNoArgByReturnSuffix(var0, "Window", "getWindow");
         if (var1 != null && var2 != null) {
            double var3 = invokeDouble(var1, "xpos", "m_91589_");
            double var5 = invokeDouble(var2, "getScreenWidth", "m_85441_");
            double var7 = invokeDouble(var2, "getGuiScaledWidth", "m_85445_");
            return var5 <= 0.0 ? var3 : var3 * var7 / var5;
         } else {
            return -1.0;
         }
      } catch (Throwable var9) {
         return -1.0;
      }
   }

   public static double guiMouseY() {
      try {
         Object var0 = minecraftInstance();
         Object var1 = findFieldValueByTypeSuffix(var0, "MouseHandler");
         Object var2 = invokeNoArgByReturnSuffix(var0, "Window", "getWindow");
         if (var1 != null && var2 != null) {
            double var3 = invokeDouble(var1, "ypos", "m_91594_");
            double var5 = invokeDouble(var2, "getScreenHeight", "m_85442_");
            double var7 = invokeDouble(var2, "getGuiScaledHeight", "m_85446_");
            return var5 <= 0.0 ? var3 : var3 * var7 / var5;
         } else {
            return -1.0;
         }
      } catch (Throwable var9) {
         return -1.0;
      }
   }

   private static Object serverLevel(Object var0) {
      if (var0 == null) {
         return null;
      } else {
         for (String var4 : new String[]{"serverLevel", "m_284548_", "level", "m_9236_"}) {
            try {
               Method var5 = var0.getClass().getMethod(var4);
               Object var6 = var5.invoke(var0);
               if (var6 != null) {
                  return var6;
               }
            } catch (Throwable var7) {
            }
         }

         return null;
      }
   }

   private static double invokeDouble(Object var0, String... var1) {
      if (var0 == null) {
         return 0.0;
      } else {
         for (String var5 : var1) {
            try {
               Method var6 = var0.getClass().getMethod(var5);
               if (var6.invoke(var0) instanceof Number var8) {
                  return var8.doubleValue();
               }
            } catch (Throwable var9) {
            }
         }

         return 0.0;
      }
   }

   private static Method findExactMethod(Class<?> var0, String var1, Class<?>... var2) {
      for (Class var3 = var0; var3 != null; var3 = var3.getSuperclass()) {
         try {
            Method var4 = var3.getDeclaredMethod(var1, var2);
            var4.setAccessible(true);
            return var4;
         } catch (Throwable var5) {
         }
      }

      return null;
   }

   private static Method findSingleArgMethod(Class<?> var0, String var1, Class<?> var2) {
      for (Class var3 = var0; var3 != null; var3 = var3.getSuperclass()) {
         for (Method var7 : var3.getDeclaredMethods()) {
            if (var7.getName().equals(var1) && var7.getParameterCount() == 1 && var7.getParameterTypes()[0].isAssignableFrom(var2)) {
               return var7;
            }
         }
      }

      return null;
   }

   private static Method findMethodByParamSuffix(Class<?> var0, String var1) {
      for (Class var2 = var0; var2 != null; var2 = var2.getSuperclass()) {
         for (Method var6 : var2.getDeclaredMethods()) {
            if (var6.getParameterCount() == 1 && var6.getParameterTypes()[0].getSimpleName().endsWith(var1)) {
               return var6;
            }
         }
      }

      return null;
   }

   private static Object findFieldValueByTypeSuffix(Object var0, String var1) {
      if (var0 == null) {
         return null;
      } else {
         for (Class var2 = var0.getClass(); var2 != null; var2 = var2.getSuperclass()) {
            for (Field var6 : var2.getDeclaredFields()) {
               if (var6.getType().getSimpleName().endsWith(var1)) {
                  try {
                     var6.setAccessible(true);
                     return var6.get(var0);
                  } catch (Throwable var8) {
                  }
               }
            }
         }

         return null;
      }
   }

   private static Object invokeNoArgByReturnSuffix(Object var0, String var1, String var2) {
      if (var0 == null) {
         return null;
      } else {
         try {
            return var0.getClass().getMethod(var2).invoke(var0);
         } catch (Throwable var9) {
            for (Method var6 : var0.getClass().getMethods()) {
               if (var6.getParameterCount() == 0 && var6.getReturnType().getSimpleName().endsWith(var1)) {
                  try {
                     return var6.invoke(var0);
                  } catch (Throwable var8) {
                  }
               }
            }

            return null;
         }
      }
   }

   private static Object builtInRegistryValue(String var0, String var1, String var2) {
      try {
         Class var3 = Class.forName("net.minecraft.core.registries.BuiltInRegistries");
         Object var4 = null;

         for (String var8 : new String[]{var0, var1}) {
            try {
               var4 = var3.getField(var8).get(null);
               break;
            } catch (Throwable var15) {
            }
         }

         if (var4 == null) {
            return null;
         }

         Class var17 = Class.forName("net.minecraft.resources.ResourceLocation");
         Object var18 = var17.getConstructor(String.class).newInstance(var2);

         for (String var10 : new String[]{"get", "m_7745_"}) {
            try {
               Method var11 = var4.getClass().getMethod(var10, var17);
               Object var12 = var11.invoke(var4, var18);
               if (var12 != null) {
                  return var12;
               }
            } catch (Throwable var14) {
            }
         }

         for (Method var24 : var4.getClass().getMethods()) {
            if (var24.getParameterCount() == 1 && var24.getParameterTypes()[0] == var17) {
               try {
                  Object var25 = var24.invoke(var4, var18);
                  if (var25 != null && !(var25 instanceof Boolean) && !(var25 instanceof Optional)) {
                     return var25;
                  }
               } catch (Throwable var13) {
               }
            }
         }
      } catch (Throwable var16) {
      }

      return null;
   }

   public static boolean isClientLevel(Object var0) {
      if (var0 == null) {
         return false;
      } else {
         String var1 = var0.getClass().getName();
         if (!var1.endsWith("ClientLevel") && !var1.contains("client.multiplayer.ClientLevel")) {
            for (String var5 : new String[]{"isClientSide", "f_46443_"}) {
               try {
                  Field var6 = var0.getClass().getField(var5);
                  if (var6.get(var0) instanceof Boolean var8) {
                     return var8;
                  }
               } catch (Throwable var9) {
               }
            }

            return false;
         } else {
            return true;
         }
      }
   }

   public static Object worldOf(Object var0) {
      return serverLevel(var0);
   }

   public static boolean sameWorld(Object var0, Object var1) {
      Object var2 = serverLevel(var0);
      Object var3 = serverLevel(var1);
      return var2 != null && var2 == var3;
   }

   public static double[] entityPosition(Object var0) {
      return playerPosition(var0);
   }

   public static double distanceSquared(Object var0, Object var1) {
      double[] var2 = entityPosition(var0);
      double[] var3 = entityPosition(var1);
      double var4 = var2[0] - var3[0];
      double var6 = var2[1] - var3[1];
      double var8 = var2[2] - var3[2];
      return var4 * var4 + var6 * var6 + var8 * var8;
   }

   public static Object createEntityFromType(Object var0, Object var1, double var2, double var4, double var6) {
      if (var0 != null && var1 != null) {
         try {
            Object var8 = serverLevel(var0);
            if (var8 == null) {
               return null;
            } else {
               Object var9 = null;

               for (String var13 : new String[]{"create", "m_20615_"}) {
                  for (Method var17 : var1.getClass().getMethods()) {
                     if (var17.getName().equals(var13) && var17.getParameterCount() == 1 && var17.getParameterTypes()[0].isInstance(var8)) {
                        try {
                           var9 = var17.invoke(var1, var8);
                           if (var9 != null) {
                              break;
                           }
                        } catch (Throwable var20) {
                        }
                     }
                  }

                  if (var9 != null) {
                     break;
                  }
               }

               if (var9 == null) {
                  for (Method var25 : var1.getClass().getMethods()) {
                     if (var25.getParameterCount() == 1
                        && var25.getParameterTypes()[0].isInstance(var8)
                        && var25.getReturnType().getName().startsWith("net.minecraft.world.entity.")) {
                        try {
                           var9 = var25.invoke(var1, var8);
                           if (var9 != null) {
                              break;
                           }
                        } catch (Throwable var19) {
                        }
                     }
                  }
               }

               if (var9 == null) {
                  return null;
               } else {
                  setEntityPosition(var9, var2, var4, var6);
                  addFreshEntity(var8, var9);
                  return var9;
               }
            }
         } catch (Throwable var21) {
            return null;
         }
      } else {
         return null;
      }
   }

   public static void setEntityPosition(Object var0, double var1, double var3, double var5) {
      if (var0 != null) {
         for (String var10 : new String[]{"setPos", "m_6034_", "m_20343_"}) {
            try {
               Method var11 = var0.getClass().getMethod(var10, double.class, double.class, double.class);
               var11.invoke(var0, var1, var3, var5);
               return;
            } catch (Throwable var14) {
            }
         }

         for (Class var15 = var0.getClass(); var15 != null; var15 = var15.getSuperclass()) {
            for (Method var19 : var15.getDeclaredMethods()) {
               if (var19.getParameterCount() == 3
                  && var19.getParameterTypes()[0] == double.class
                  && var19.getParameterTypes()[1] == double.class
                  && var19.getParameterTypes()[2] == double.class) {
                  try {
                     var19.setAccessible(true);
                     var19.invoke(var0, var1, var3, var5);
                     return;
                  } catch (Throwable var13) {
                  }
               }
            }
         }
      }
   }

   public static void configureShadowAnchor(Object var0) {
      if (var0 != null) {
         invokeBooleanSetter(var0, true, "setNoGravity", "m_20242_");
         invokeBooleanSetter(var0, true, "setInvulnerable", "m_20331_");
         invokeBooleanSetter(var0, true, "setSilent", "m_20225_");
         invokeBooleanSetter(var0, true, "setInvisible", "m_6842_");

         for (String var4 : new String[]{"addTag", "m_20049_"}) {
            try {
               Method var5 = var0.getClass().getMethod(var4, String.class);
               var5.invoke(var0, "velvet_shadow");
               break;
            } catch (Throwable var6) {
            }
         }
      }
   }

   private static void invokeBooleanSetter(Object var0, boolean var1, String... var2) {
      for (String var6 : var2) {
         try {
            Method var7 = var0.getClass().getMethod(var6, boolean.class);
            var7.invoke(var0, var1);
            return;
         } catch (Throwable var8) {
         }
      }
   }

   public static void configureBattleActor(Object var0, boolean var1) {
      if (var0 != null) {
         invokeBooleanSetter(var0, true, "setNoAi", "m_21557_");
         invokeBooleanSetter(var0, true, "setInvulnerable", "m_20331_");
         invokeBooleanSetter(var0, true, "setSilent", "m_20256_", "m_20225_");
         invokeBooleanSetter(var0, var1, "setNoGravity", "m_20242_");

         for (String var5 : new String[]{"setPersistenceRequired", "m_21530_"}) {
            try {
               var0.getClass().getMethod(var5).invoke(var0);
               break;
            } catch (Throwable var9) {
            }
         }

         for (String var14 : new String[]{"addTag", "m_20049_"}) {
            try {
               var0.getClass().getMethod(var14, String.class).invoke(var0, var1 ? "velvet_battle_persona" : "velvet_battle_enemy");
               break;
            } catch (Throwable var8) {
            }
         }

         try {
            Method var11 = findExactMethod(var0.getClass(), "setDeltaMovement", double.class, double.class, double.class);
            if (var11 == null) {
               var11 = findExactMethod(var0.getClass(), "m_20334_", double.class, double.class, double.class);
            }

            if (var11 != null) {
               var11.invoke(var0, 0.0, 0.0, 0.0);
            }
         } catch (Throwable var7) {
         }
      }
   }

   public static void setEntityRotation(Object var0, float var1, float var2) {
      if (var0 != null) {
         for (String var6 : new String[]{"setYRot", "m_146922_"}) {
            try {
               var0.getClass().getMethod(var6, float.class).invoke(var0, var1);
               break;
            } catch (Throwable var11) {
            }
         }

         for (String var21 : new String[]{"setXRot", "m_146926_"}) {
            try {
               var0.getClass().getMethod(var21, float.class).invoke(var0, var2);
               break;
            } catch (Throwable var10) {
            }
         }

         for (String var22 : new String[]{"setYHeadRot", "m_5616_"}) {
            try {
               var0.getClass().getMethod(var22, float.class).invoke(var0, var1);
               break;
            } catch (Throwable var9) {
            }
         }

         for (String var23 : new String[]{"setYBodyRot", "m_5618_"}) {
            try {
               var0.getClass().getMethod(var23, float.class).invoke(var0, var1);
               break;
            } catch (Throwable var8) {
            }
         }
      }
   }

   public static void freezeLocalClientPlayerPosition(double[] var0) {
      Object var1 = clientPlayer();
      if (var1 != null && var0 != null) {
         try {
            Method var2 = findExactMethod(var1.getClass(), "setPos", double.class, double.class, double.class);
            if (var2 == null) {
               var2 = findExactMethod(var1.getClass(), "m_6034_", double.class, double.class, double.class);
            }

            if (var2 == null) {
               var2 = findExactMethod(var1.getClass(), "m_20343_", double.class, double.class, double.class);
            }

            if (var2 != null) {
               var2.invoke(var1, var0[0], var0[1], var0[2]);
            }
         } catch (Throwable var3) {
         }

         suppressLocalMovementInput();
      }
   }

   public static void offsetBattleCamera(Object var0) {
      if (var0 != null) {
         for (String var4 : new String[]{"move", "m_90568_"}) {
            try {
               var0.getClass().getMethod(var4, double.class, double.class, double.class).invoke(var0, -1.35, 0.25, 0.2);
               return;
            } catch (Throwable var6) {
            }
         }
      }
   }

   public static void suppressLocalMovementInput() {
      Object var0 = clientPlayer();
      if (var0 != null) {
         try {
            Object var1 = null;

            for (String var5 : new String[]{"input", "f_108611_"}) {
               try {
                  Field var6 = var0.getClass().getField(var5);
                  var1 = var6.get(var0);
                  if (var1 != null) {
                     break;
                  }
               } catch (Throwable var9) {
               }
            }

            if (var1 == null) {
               var1 = findFieldValueByTypeSuffix(var0, "Input");
            }

            if (var1 == null) {
               return;
            }

            for (String var17 : new String[]{"leftImpulse", "f_108566_", "forwardImpulse", "f_108567_"}) {
               try {
                  Field var19 = var1.getClass().getField(var17);
                  if (var19.getType() == float.class) {
                     var19.setFloat(var1, 0.0F);
                  }
               } catch (Throwable var8) {
               }
            }

            for (String var18 : new String[]{
               "up", "f_108568_", "down", "f_108569_", "left", "f_108570_", "right", "f_108571_", "jumping", "f_108572_", "shiftKeyDown", "f_108573_"
            }) {
               try {
                  Field var20 = var1.getClass().getField(var18);
                  if (var20.getType() == boolean.class) {
                     var20.setBoolean(var1, false);
                  }
               } catch (Throwable var7) {
               }
            }
         } catch (Throwable var10) {
         }
      }
   }

   public static boolean isEntityRemoved(Object var0) {
      if (var0 == null) {
         return true;
      } else {
         for (String var4 : new String[]{"isRemoved", "m_213877_"}) {
            try {
               Method var5 = var0.getClass().getMethod(var4);
               if (var5.invoke(var0) instanceof Boolean var7) {
                  return var7;
               }
            } catch (Throwable var8) {
            }
         }

         return false;
      }
   }

   public static void discardObjectEntity(Object var0) {
      if (var0 != null) {
         for (String var4 : new String[]{"discard", "m_146870_"}) {
            try {
               Method var5 = var0.getClass().getMethod(var4);
               var5.invoke(var0);
               return;
            } catch (Throwable var6) {
            }
         }
      }
   }

   public static int surfaceY(Object var0, int var1, int var2) {
      if (var0 == null) {
         return 64;
      } else {
         Object var3 = serverLevel(var0);
         if (var3 == null) {
            return (int)Math.floor(playerPosition(var0)[1]);
         } else {
            try {
               Class var4 = Class.forName("net.minecraft.world.level.levelgen.Heightmap$Types");
               Object[] var5 = var4.getEnumConstants();
               if (var5 != null && var5.length > 0) {
                  Object var6 = var5[0];

                  for (Object var10 : var5) {
                     if (var10 instanceof Enum var11 && "MOTION_BLOCKING_NO_LEAVES".equals(var11.name())) {
                        var6 = var10;
                        break;
                     }
                  }

                  for (String var22 : new String[]{"getHeight", "m_6924_"}) {
                     try {
                        Method var24 = var3.getClass().getMethod(var22, var4, int.class, int.class);
                        if (var24.invoke(var3, var6, var1, var2) instanceof Number var13) {
                           return var13.intValue();
                        }
                     } catch (Throwable var14) {
                     }
                  }

                  for (Method var23 : var3.getClass().getMethods()) {
                     Class[] var25 = var23.getParameterTypes();
                     if (var23.getReturnType() == int.class
                        && var25.length == 3
                        && var25[0] == var4
                        && var25[1] == int.class
                        && var25[2] == int.class
                        && var23.invoke(var3, var6, var1, var2) instanceof Number var27) {
                        return var27.intValue();
                     }
                  }
               }
            } catch (Throwable var15) {
            }

            return (int)Math.floor(playerPosition(var0)[1]);
         }
      }
   }

   private static boolean addFreshEntity(Object var0, Object var1) {
      if (var0 != null && var1 != null) {
         for (String var5 : new String[]{"addFreshEntity", "m_7967_"}) {
            for (Method var9 : var0.getClass().getMethods()) {
               if (var9.getName().equals(var5) && var9.getParameterCount() == 1 && var9.getParameterTypes()[0].isInstance(var1)) {
                  try {
                     if (var9.invoke(var0, var1) instanceof Boolean var11 && !var11) {
                        return false;
                     }

                     return true;
                  } catch (Throwable var13) {
                  }
               }
            }
         }

         for (Method var17 : var0.getClass().getMethods()) {
            if (var17.getParameterCount() == 1
               && var17.getReturnType() == boolean.class
               && var17.getParameterTypes()[0].getName().equals("net.minecraft.world.entity.Entity")) {
               try {
                  if (var17.invoke(var0, var1) instanceof Boolean var19 && var19) {
                     return true;
                  }

                  return false;
               } catch (Throwable var12) {
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }
}
