package com.gx.velveteclipse.client.vfx;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
/** Make Photon OBJ shapes available to the vanilla model manager and atlas. */
@Mod.EventBusSubscriber(modid="velvet_eclipse", value=Dist.CLIENT, bus=Mod.EventBusSubscriber.Bus.MOD)
public final class ElementalModels {
    @SubscribeEvent public static void register(ModelEvent.RegisterAdditional event) { ElementalPhoton.registerModels(event::register); }
    private ElementalModels() {}
}
