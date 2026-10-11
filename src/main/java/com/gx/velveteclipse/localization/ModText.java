package com.gx.velveteclipse.localization;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/** Identifies mod-owned display text without changing persisted names or gameplay identifiers. */
public final class ModText {
    public static final String LITERAL_KEY="velvet_eclipse.display_text";
    public static MutableComponent component(String text) {
        // A standard translatable component survives vanilla packet/NBT serialization. Each
        // recipient chooses its own language; dedicated servers keep the original fallback.
        return Component.m_264568_(LITERAL_KEY,text.replace("%","%%"));
    }
    private ModText(){}
}
