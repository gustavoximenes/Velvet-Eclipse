package com.gx.velveteclipse.compat.mixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Optional OptiFine hook after the pack's final composition, before the vanilla HUD. */
@Pseudo
@Mixin(targets="net.optifine.shaders.Shaders",remap=false)
public abstract class DarkHourShaderSkyMixin {
 @Inject(method="endRender()V",at=@At("RETURN"),require=0,remap=false)
 private static void velvet$darkHourSky(CallbackInfo ci){com.gx.velveteclipse.client.darkhour.DarkHourShaderSky.render();com.gx.velveteclipse.client.room.VelvetRoomShaderLighting.render();}
}
