package com.gx.velveteclipse.client.localization;

import com.gx.velveteclipse.localization.*;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.loading.FMLPaths;
import java.nio.file.*;
import java.io.IOException;
import java.util.*;

/** Local preference: independent of the world's owner and Minecraft's selected language. */
@Mod.EventBusSubscriber(modid="velvet_eclipse",value=Dist.CLIENT)
public final class ClientLocalization {
    private static final Map<String,String> EN_KEYS=TextCatalog.read("/assets/velvet_eclipse/lang/en_us.json");
    private static final Map<String,String> PT_KEYS=TextCatalog.read("/assets/velvet_eclipse/lang/pt_br.json");
    private static String selected="en_us";
    private static boolean loaded;
    public static String code(){load();return selected;}
    public static String text(String source){return DisplayText.text(source);}
    public static boolean toggle(){return select(code().equals("en_us")?"pt_br":"en_us");}
    public static boolean select(String code){
        if(!Set.of("en_us","pt_br").contains(code))return false;
        try{
            LanguagePreference.save(config(),code);
            selected=code;DisplayText.language(code);loaded=true;install(true);return true;
        }catch(IOException e){com.gx.velveteclipse.VelvetEclipseMod.LOGGER.error("Could not save mod language",e);return false;}
    }
    private static Path config(){return FMLPaths.CONFIGDIR.get().resolve("velvet-eclipse-language.txt");}
    private static void load(){if(loaded)return;loaded=true;selected=LanguagePreference.read(config());DisplayText.language(selected);}
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){if(e.phase==TickEvent.Phase.START){load();install(false);}}
    private static void install(boolean refresh){
        Language active=Language.m_128107_();
        if(refresh||!(active instanceof DisplayLanguage)){
            Language delegate=active instanceof DisplayLanguage m?m.base:active;
            // A new Language instance also invalidates cached TranslatableContents and
            // MutableComponent glyph order when the user changes this preference.
            Language.m_128114_(new DisplayLanguage(delegate,ClientLocalization::key));
        }
    }
    public static String key(String key,String fallback){
        if(key.equals(ModText.LITERAL_KEY)){load();return text(fallback.replace("%%","%")).replace("%","%%");}
        if(!EN_KEYS.containsKey(key))return null;
        return (code().equals("pt_br")?PT_KEYS:EN_KEYS).getOrDefault(key,EN_KEYS.get(key));
    }
    private ClientLocalization(){}
}
