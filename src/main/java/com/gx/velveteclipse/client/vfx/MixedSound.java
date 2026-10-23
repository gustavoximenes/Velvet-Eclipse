package com.gx.velveteclipse.client.vfx;
import com.gx.velveteclipse.presentation.AudioPreferences.Bus;
import net.minecraft.client.resources.sounds.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
/** Tickable gain follows the local mixer without restarting music/cinematics. */
public final class MixedSound extends SimpleSoundInstance implements TickableSoundInstance {
 private final Bus bus;private final float gain;
 public MixedSound(ResourceLocation id,Bus bus,float gain,boolean loop,boolean relative,double x,double y,double z){super(id,ClientAudio.source(bus),gain,1,RandomSource.m_216327_(),loop,0,relative?SoundInstance.Attenuation.NONE:SoundInstance.Attenuation.LINEAR,relative?0:x,relative?0:y,relative?0:z,relative);this.bus=bus;this.gain=gain;}
 @Override public float m_7769_(){return gain*ClientAudio.volume(bus);}
 @Override public boolean m_7801_(){return false;}
 @Override public void m_7788_(){}
 @Override public boolean m_7784_(){return true;}
}
