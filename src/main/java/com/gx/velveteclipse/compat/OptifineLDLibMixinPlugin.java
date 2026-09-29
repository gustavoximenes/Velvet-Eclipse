package com.gx.velveteclipse.compat;

import java.util.List;
import java.util.Set;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

/** Early bootstrap code: intentionally does not load Minecraft, LDLib or Forge mod classes. */
public final class OptifineLDLibMixinPlugin implements IMixinConfigPlugin {
    public void onLoad(String mixinPackage) {}
    public String getRefMapperConfig() { return null; }
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) { return true; }
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
    public List<String> getMixins() { return null; }
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        if (targetClassName.equals("net.minecraft.client.resources.model.ModelBakery") && ModelBakeryLogBridge.normalize(targetClass) > 0)
            System.getLogger("Velvet Eclipse compatibility").log(System.Logger.Level.INFO,
                    "Adapted OptiFine ModelBakery warning overload for LDLib; model loading and shaders unchanged.");
    }
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
