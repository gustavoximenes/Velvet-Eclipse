package com.gx.velveteclipse.client.vfx;
import com.gx.velveteclipse.presentation.FeedbackLibrary;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import java.util.*;
/** Only explicitly addressed clients play these sounds; no world-wide combat broadcasts. */
public final class ReloadAudio {
 private record Playing(SoundInstance sound,long expiry,boolean combat){}
 private static final List<Playing> playing=new ArrayList<>();private static Object level;private static long uiAt;
 public static void clip(String id,boolean combat,double x,double y,double z){
  if(!FeedbackLibrary.clip(id))return;var mc=Minecraft.m_91087_();var w=mc.f_91073_;if(w==null)return;if(w!=level){reset(false);level=w;}
  if(playing.size()>=48){mc.m_91106_().m_120399_(playing.remove(0).sound);}
  boolean voice=id.startsWith("voice_");
  // Combat is session-scoped, so it remains audible from the cinematic camera without leaking to observers.
  var sound=new MixedSound(new ResourceLocation("velvet_eclipse:reload."+id),com.gx.velveteclipse.presentation.AudioPreferences.classify(id,combat),voice?.95f:.8f,false,combat,x,y,z);
  mc.m_91106_().m_120367_(sound);playing.add(new Playing(sound,w.m_46467_()+800,combat));
 }
 public static void route(String id,boolean combat,double x,double y,double z){for(var clip:FeedbackLibrary.audio(id))clip(clip,combat,x,y,z);}
 public static void ui(String id){var mc=Minecraft.m_91087_();if(mc==null||mc.f_91074_==null)return;long now=System.nanoTime();if(now-uiAt<65_000_000)return;uiAt=now;route(id,true,mc.f_91074_.m_20185_(),mc.f_91074_.m_20186_(),mc.f_91074_.m_20189_());}
 public static void tick(){var mc=Minecraft.m_91087_();if(mc.f_91073_!=level){reset(false);level=mc.f_91073_;}if(mc.f_91073_==null)return;playing.removeIf(p->{if(mc.f_91073_.m_46467_()>=p.expiry){mc.m_91106_().m_120399_(p.sound);return true;}return !mc.m_91106_().m_120403_(p.sound);});}
 public static void reset(boolean combatOnly){var mc=Minecraft.m_91087_();playing.removeIf(p->{if(!combatOnly||p.combat){mc.m_91106_().m_120399_(p.sound);return true;}return false;});if(!combatOnly)level=null;}
}
