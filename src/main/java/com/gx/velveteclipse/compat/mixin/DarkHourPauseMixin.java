package com.gx.velveteclipse.compat.mixin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import com.gx.velveteclipse.darkhour.DarkHourDeadline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.function.BooleanSupplier;

/** IntegratedServer keeps ticking its pause branch while level ticks are suspended. */
@Mixin(value=IntegratedServer.class,remap=false)
public abstract class DarkHourPauseMixin {
    @Inject(method="m_5705_(Ljava/util/function/BooleanSupplier;)V",at=@At("HEAD"),remap=false)
    private void velvet$freezeDeadline(BooleanSupplier budget,CallbackInfo ci){
        if(Minecraft.m_91087_().m_91104_())DarkHourDeadline.suspend(((IntegratedServer)(Object)this).m_129783_());
    }
}
