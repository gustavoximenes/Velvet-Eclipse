package com.gx.velveteclipse.localization;

import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import java.util.Map;
import java.util.function.BiFunction;

/** A new instance invalidates Minecraft's component caches without reloading textures. */
public final class DisplayLanguage extends Language {
    public final Language base;
    private final BiFunction<String,String,String> lookup;
    public DisplayLanguage(Language base,BiFunction<String,String,String> lookup){this.base=base;this.lookup=lookup;}
    public String m_118919_(String key,String fallback){String own=lookup.apply(key,fallback);return own==null?base.m_118919_(key,fallback):own;}
    public boolean m_6722_(String key){return lookup.apply(key,"")!=null||base.m_6722_(key);}
    public boolean m_6627_(){return base.m_6627_();}
    public FormattedCharSequence m_5536_(FormattedText text){return base.m_5536_(text);}
    public Map<String,String> getLanguageData(){return base.getLanguageData();}
}
