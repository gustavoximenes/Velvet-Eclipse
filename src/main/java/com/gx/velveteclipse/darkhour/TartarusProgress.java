package com.gx.velveteclipse.darkhour;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
/** World progress and individual terminal discoveries have separate lifetimes. */
public final class TartarusProgress {
 public long cycle,day=-1;public boolean active,initialized;
 public final Map<Integer,Long> generated=new HashMap<>();
 public final Set<String> treasures=new HashSet<>();
 public boolean observe(long time){boolean next=DarkHourCycle.active(time);long d=Math.floorDiv(time,24000);boolean renew=initialized&&next&&(!active||d!=day);if(renew){cycle++;treasures.removeIf(k->!TartarusInteriorLayout.fixedFloor(Integer.parseInt(k.split(":")[0])));}initialized=true;active=next;day=d;return renew;}
 public long generation(int f){return TartarusInteriorLayout.fixedFloor(f)?-1:cycle;}
 public long seed(long world,int f){return world^(TartarusInteriorLayout.fixedFloor(f)?0:cycle*0x632BE59BD9B4E019L);}
 public boolean current(int f){return Objects.equals(generated.get(f),generation(f));}
 public static SortedSet<Integer> discovered(int[] values){var out=new TreeSet<Integer>();for(int f:values)if(f>=0&&f<=250&&TartarusInteriorLayout.fixedFloor(f))out.add(f);return out;}
 public static TartarusProgress load(CompoundTag t){var p=new TartarusProgress();p.cycle=t.m_128454_("cycle");p.day=t.m_128454_("day");p.active=t.m_128471_("active");p.initialized=t.m_128471_("initialized");var floors=t.m_128437_("generated",10);for(int i=0;i<floors.size();i++){var f=floors.m_128728_(i);p.generated.put(f.m_128451_("f"),f.m_128454_("g"));}for(String k:t.m_128469_("treasures").m_128431_())p.treasures.add(k);return p;}
 public CompoundTag save(){var t=new CompoundTag();t.m_128356_("cycle",cycle);t.m_128356_("day",day);t.m_128379_("active",active);t.m_128379_("initialized",initialized);var floors=new net.minecraft.nbt.ListTag();generated.forEach((f,g)->{var v=new CompoundTag();v.m_128405_("f",f);v.m_128356_("g",g);floors.add(v);});t.m_128365_("generated",floors);var chest=new CompoundTag();treasures.forEach(k->chest.m_128379_(k,true));t.m_128365_("treasures",chest);return t;}
}
