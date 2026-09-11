package com.gx.velveteclipse.forge;

import java.lang.reflect.Constructor;

public final class EvokerItemFactory {
   private EvokerItemFactory() {
   }

   public static Object createForModel(String var0) throws Exception {
      Class var1 = Class.forName("net.minecraft.world.item.Item$Properties");
      Object var2 = var1.getConstructor().newInstance();
      Class var3 = Class.forName("evoker".equals(var0) ? "net.minecraft.world.item.SpyglassItem" : "net.minecraft.world.item.Item");

      for (Constructor var7 : var3.getConstructors()) {
         Class[] var8 = var7.getParameterTypes();
         if (var8.length == 1 && var8[0].isInstance(var2)) {
            return var7.newInstance(var2);
         }
      }

      throw new NoSuchMethodException("Compatible item constructor unavailable for " + var0);
   }
}
