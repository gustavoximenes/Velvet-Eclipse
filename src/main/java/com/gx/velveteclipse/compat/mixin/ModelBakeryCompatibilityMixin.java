package com.gx.velveteclipse.compat.mixin;

import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.client.resources.model.ModelBakery;

/** Schedules the logging-overload bridge before LDLib's priority-1000 redirect. */
@Mixin(value = ModelBakery.class, priority = 2000, remap = false)
public abstract class ModelBakeryCompatibilityMixin {}
