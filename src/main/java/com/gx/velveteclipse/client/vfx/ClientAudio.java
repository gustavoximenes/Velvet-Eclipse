package com.gx.velveteclipse.client.vfx;
import com.gx.velveteclipse.presentation.AudioPreferences;
import com.gx.velveteclipse.presentation.AudioPreferences.Bus;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.fml.loading.FMLPaths;
/** Sound categories remain independent of Minecraft's Music slider. Master volume still applies. */
public final class ClientAudio {
 private static AudioPreferences values;
 private static java.nio.file.Path path(){return FMLPaths.CONFIGDIR.get().resolve("velvet-eclipse-audio.properties");}
 public static float volume(Bus bus){if(values==null)values=AudioPreferences.read(path());return values.get(bus);}
 public static boolean set(Bus bus,float value){volume(bus);float old=values.get(bus);values.set(bus,value);try{values.save(path());return true;}catch(java.io.IOException e){values.set(bus,old);com.gx.velveteclipse.VelvetEclipseMod.LOGGER.error("Could not save audio settings",e);return false;}}
 public static SoundSource source(Bus bus){return switch(bus){case VOICE->SoundSource.VOICE;case COMBAT->SoundSource.PLAYERS;case INTERACTION->SoundSource.BLOCKS;case UI,CINEMATIC->SoundSource.MASTER;};}
 private ClientAudio(){}
}
