package com.gx.velveteclipse.compat.mixin;

import net.minecraft.client.resources.language.ClientLanguage;
import com.gx.velveteclipse.client.localization.ClientLocalization;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** I18n keeps its own ClientLanguage reference; override only this mod's registered keys. */
@Mixin(value=ClientLanguage.class,remap=false)
public abstract class ModLanguageMixin {
    @Inject(method="m_118919_(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;",at=@At("HEAD"),cancellable=true,remap=false)
    private void velvet$language(String key,String fallback,CallbackInfoReturnable<String> ci){
        String text=ClientLocalization.key(key,fallback);if(text!=null)ci.setReturnValue(text);
    }
}
