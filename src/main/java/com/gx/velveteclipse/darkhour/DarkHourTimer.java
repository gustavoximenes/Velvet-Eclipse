package com.gx.velveteclipse.darkhour;
import net.minecraft.nbt.CompoundTag;
/** Thirty minutes of server runtime, saved across restarts independently of /time speed. */
public final class DarkHourTimer {
 public static final int DURATION=30*60*1000;
 public long remaining,day=-1;public boolean running;
 public boolean advance(long dayTime,long elapsed){
  if(!DarkHourCycle.active(dayTime)){boolean ended=running;running=false;remaining=0;return ended;}
  long d=Math.floorDiv(dayTime,24000);
  if(!running||day!=d){day=d;remaining=DURATION;running=true;return false;}
  remaining=Math.max(0,remaining-Math.max(0,elapsed));if(remaining==0){running=false;return true;}return false;
 }
 // Reserve one tick for vanilla's increment so sunrise cannot expire us early.
 public long skyTime(){return day*24000+DarkHourCycle.START+Math.min(5998,(DURATION-Math.max(1,remaining))*6000/DURATION);}
 public CompoundTag save(){var t=new CompoundTag();t.m_128356_("remaining",remaining);t.m_128356_("day",day);t.m_128379_("running",running);return t;}
 public static DarkHourTimer load(CompoundTag t){var r=new DarkHourTimer();r.remaining=Math.max(0,Math.min(DURATION,t.m_128454_("remaining")));r.day=t.m_128454_("day");r.running=t.m_128471_("running");return r;}
}
