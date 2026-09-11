package com.gx.velveteclipse.forge;

import java.util.List;
import java.util.Map;

public final class VisualModelLibrary {
   private static final Map<String, List<VisualModelLibrary.Piece>> MODELS = Map.of(
      "cinder_wisp",
      List.of(
         p("BLACK_CONCRETE", -0.42, 0.1, -0.3, 0.84F, 0.42F, 0.6F),
         p("ORANGE_CONCRETE", -0.32, 0.45, -0.24, 0.64F, 0.72F, 0.48F),
         p("MAGMA_BLOCK", -0.22, 1.04, -0.18, 0.44F, 0.55F, 0.36F),
         r("YELLOW_STAINED_GLASS", -0.12, 1.45, -0.1, 0.24F, 0.46F, 0.2F, 18.0F),
         p("SEA_LANTERN", -0.08, 0.77, -0.34, 0.16F, 0.16F, 0.16F),
         p("SEA_LANTERN", 0.12, 0.77, -0.34, 0.16F, 0.16F, 0.16F),
         r("ORANGE_STAINED_GLASS", -0.72, 0.64, -0.1, 0.34F, 0.22F, 0.24F, -28.0F),
         r("ORANGE_STAINED_GLASS", 0.38, 0.64, -0.1, 0.34F, 0.22F, 0.24F, 28.0F)
      ),
      "nocturne_knight",
      List.of(
         p("POLISHED_BLACKSTONE", -0.38, 0.0, -0.28, 0.76F, 0.55F, 0.56F),
         p("BLACK_CONCRETE", -0.32, 0.52, -0.24, 0.64F, 0.9F, 0.48F),
         p("PURPLE_CONCRETE", -0.25, 1.35, -0.23, 0.5F, 0.38F, 0.46F),
         p("AMETHYST_BLOCK", -0.42, 1.7, -0.17, 0.18F, 0.45F, 0.18F),
         p("AMETHYST_BLOCK", 0.24, 1.7, -0.17, 0.18F, 0.45F, 0.18F),
         r("POLISHED_BLACKSTONE", -0.7, 0.74, -0.16, 0.3F, 0.9F, 0.28F, -18.0F),
         r("POLISHED_BLACKSTONE", 0.4, 0.74, -0.16, 0.3F, 0.9F, 0.28F, 18.0F),
         p("MAGENTA_STAINED_GLASS", -0.16, 1.48, -0.34, 0.12F, 0.12F, 0.1F),
         p("MAGENTA_STAINED_GLASS", 0.06, 1.48, -0.34, 0.12F, 0.12F, 0.1F)
      ),
      "sylphid",
      List.of(
         p("WHITE_CONCRETE", -0.24, 0.42, -0.18, 0.48F, 0.78F, 0.36F),
         p("LIGHT_BLUE_CONCRETE", -0.2, 1.18, -0.18, 0.4F, 0.38F, 0.36F),
         p("CYAN_STAINED_GLASS", -0.8, 0.72, -0.08, 0.52F, 0.84F, 0.18F),
         p("CYAN_STAINED_GLASS", 0.28, 0.72, -0.08, 0.52F, 0.84F, 0.18F),
         r("LIGHT_BLUE_STAINED_GLASS", -0.72, 1.42, -0.02, 0.42F, 0.5F, 0.14F, -24.0F),
         r("LIGHT_BLUE_STAINED_GLASS", 0.3, 1.42, -0.02, 0.42F, 0.5F, 0.14F, 24.0F),
         p("SEA_LANTERN", -0.12, 1.3, -0.28, 0.1F, 0.1F, 0.1F),
         p("SEA_LANTERN", 0.04, 1.3, -0.28, 0.1F, 0.1F, 0.1F)
      ),
      "lumen_hare",
      List.of(
         p("WHITE_CONCRETE", -0.34, 0.24, -0.25, 0.68F, 0.72F, 0.5F),
         p("QUARTZ_BLOCK", -0.26, 0.92, -0.22, 0.52F, 0.46F, 0.44F),
         p("SEA_LANTERN", -0.2, 1.37, -0.14, 0.18F, 0.7F, 0.2F),
         p("SEA_LANTERN", 0.02, 1.37, -0.14, 0.18F, 0.7F, 0.2F),
         p("GOLD_BLOCK", -0.08, 0.02, -0.08, 0.16F, 0.18F, 0.16F),
         p("LIGHT_BLUE_STAINED_GLASS", -0.17, 1.05, -0.34, 0.12F, 0.12F, 0.1F),
         p("LIGHT_BLUE_STAINED_GLASS", 0.05, 1.05, -0.34, 0.12F, 0.12F, 0.1F)
      ),
      "iron_oni",
      List.of(
         p("BLACKSTONE", -0.48, 0.0, -0.32, 0.96F, 0.6F, 0.64F),
         p("IRON_BLOCK", -0.4, 0.56, -0.3, 0.8F, 0.82F, 0.6F),
         p("RED_CONCRETE", -0.34, 1.35, -0.28, 0.68F, 0.46F, 0.56F),
         r("POLISHED_BLACKSTONE", -0.58, 1.7, -0.16, 0.22F, 0.58F, 0.22F, -22.0F),
         r("POLISHED_BLACKSTONE", 0.36, 1.7, -0.16, 0.22F, 0.58F, 0.22F, 22.0F),
         p("REDSTONE_BLOCK", -0.2, 1.47, -0.39, 0.14F, 0.12F, 0.1F),
         p("REDSTONE_BLOCK", 0.06, 1.47, -0.39, 0.14F, 0.12F, 0.1F),
         r("IRON_BLOCK", -0.78, 0.6, -0.18, 0.3F, 0.92F, 0.34F, -14.0F),
         r("IRON_BLOCK", 0.48, 0.6, -0.18, 0.3F, 0.92F, 0.34F, 14.0F)
      ),
      "umbra_slime",
      List.of(
         p("BLACK_CONCRETE", -0.58, 0.18, -0.4, 1.16F, 0.62F, 0.8F),
         p("POLISHED_BLACKSTONE", -0.46, 0.68, -0.32, 0.92F, 0.54F, 0.64F),
         p("LIGHT_BLUE_STAINED_GLASS", -0.34, 1.04, -0.43, 0.68F, 0.28F, 0.18F),
         p("SEA_LANTERN", -0.23, 0.89, -0.52, 0.15F, 0.13F, 0.1F),
         p("SEA_LANTERN", 0.08, 0.89, -0.52, 0.15F, 0.13F, 0.1F),
         r("BLUE_STAINED_GLASS", -0.84, 0.22, -0.12, 0.28F, 0.3F, 0.78F, -24.0F),
         r("BLUE_STAINED_GLASS", 0.56, 0.22, -0.12, 0.28F, 0.3F, 0.78F, 24.0F),
         r("PURPLE_STAINED_GLASS", -0.64, -0.1, 0.2, 0.22F, 0.24F, 0.9F, -35.0F),
         r("PURPLE_STAINED_GLASS", 0.42, -0.1, 0.2, 0.22F, 0.24F, 0.9F, 35.0F),
         p("BLACK_CONCRETE", -0.12, -0.18, -0.12, 0.24F, 0.22F, 0.24F)
      )
   );

   private VisualModelLibrary() {
   }

   private static VisualModelLibrary.Piece p(String var0, double var1, double var3, double var5, float var7, float var8, float var9) {
      return new VisualModelLibrary.Piece(var0, var1, var3, var5, var7, var8, var9, 0.0F);
   }

   private static VisualModelLibrary.Piece r(String var0, double var1, double var3, double var5, float var7, float var8, float var9, float var10) {
      return new VisualModelLibrary.Piece(var0, var1, var3, var5, var7, var8, var9, var10);
   }

   public static List<VisualModelLibrary.Piece> model(String var0) {
      return MODELS.getOrDefault(var0, MODELS.get("cinder_wisp"));
   }

   public static record Piece(String blockField, double x, double y, double z, float sx, float sy, float sz, float yaw) {
   }
}
