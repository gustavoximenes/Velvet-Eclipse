package com.gx.velveteclipse.forge;

public final class PersonaCinematicHooks {
   private PersonaCinematicHooks() {
   }

   public static boolean shouldSuppressPulse(Object var0, String var1) {
      return PersonaCinematicServer.interceptPulse(var0, var1) ? true : PersonaCinematicServer.suppressLegacySettle(var0);
   }
}
