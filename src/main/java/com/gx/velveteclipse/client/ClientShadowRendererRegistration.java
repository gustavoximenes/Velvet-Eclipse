package com.gx.velveteclipse.client;

import com.gx.velveteclipse.VelvetEclipseMod;
import com.gx.velveteclipse.client.render.VelvetGeoEntityRenderer;
import com.gx.velveteclipse.forge.PersonaMobRegistry;
import com.gx.velveteclipse.forge.ShadowRegistry;
import java.util.Map.Entry;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(
   modid = "velvet_eclipse",
   bus = Bus.MOD,
   value = {Dist.CLIENT}
)
public final class ClientShadowRendererRegistration {
   private ClientShadowRendererRegistration() {
   }

   @SubscribeEvent
   public static void register(RegisterRenderers var0) {
      registerOne(var0, ShadowRegistry.shadowType(), "wealth_hand");

      for (Entry var2 : PersonaMobRegistry.all().entrySet()) {
         String var4 = (String)var2.getKey();

         String var3 = switch (var4) {
            case "cinder_wisp" -> "thanatos";
            case "umbra_slime" -> "wealth_hand";
            default -> (String)var2.getKey();
         };
         registerOne(var0, (EntityType<?>)var2.getValue(), var3);
      }
   }

   private static void registerOne(RegisterRenderers var0, EntityType<?> var1, String var2) {
      if (var1 == null) {
         VelvetEclipseMod.LOGGER.error("Velvet Eclipse: GeckoLib renderer registration skipped for null type " + var2);
      } else {
         var0.registerEntityRenderer(var1, new ClientShadowRendererRegistration.RuntimeRendererProvider(var2));
         VelvetEclipseMod.LOGGER.info("Velvet Eclipse: registered GeckoLib renderer for " + var2);
      }
   }

   private static final class RuntimeRendererProvider implements EntityRendererProvider<Entity> {
      private final String assetId;

      RuntimeRendererProvider(String var1) {
         this.assetId = var1;
      }

      public EntityRenderer<Entity> create(Context var1) {
         return new VelvetGeoEntityRenderer(var1, this.assetId);
      }

      public EntityRenderer<Entity> m_174009_(Context var1) {
         return new VelvetGeoEntityRenderer(var1, this.assetId);
      }
   }
}
