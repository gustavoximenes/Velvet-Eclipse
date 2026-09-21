package com.gx.velveteclipse.darkhour;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
/** A floor rolls once per generation; changing settings never resurrects defeated slots. */
public final class TartarusPopulation {
 public int minimum=10,maximum=20;
 private final Map<Integer,Integer> counts=new HashMap<>();
 public boolean initialized(int floor){return counts.containsKey(floor);}
 public void range(int min,int max){if(min<1||max>64||min>max)throw new IllegalArgumentException("Use 1 <= mínimo <= máximo <= 64.");minimum=min;maximum=max;}
 public int count(long seed,int floor){if(TartarusInteriorLayout.fixedFloor(floor))return TartarusInteriorLayout.shadowLimit(floor);return Math.min(14,counts.computeIfAbsent(floor,f->minimum+new Random(seed^f*0x51DE43L).nextInt(maximum-minimum+1)));}
 public void reset(int floor){counts.remove(floor);}
 public static List<TartarusInteriorLayout.Point> positions(TartarusInteriorLayout.Plan plan){
  var out=new ArrayList<TartarusInteriorLayout.Point>();
  // One encounter per room; entry and stair rooms remain safe.
  out.addAll(new LinkedHashSet<>(plan.spawns()));
  return List.copyOf(out);
 }
 public CompoundTag save(){var t=new CompoundTag();t.m_128405_("min",minimum);t.m_128405_("max",maximum);var c=new CompoundTag();counts.forEach((f,n)->c.m_128405_(f.toString(),n));t.m_128365_("counts",c);return t;}
 public static TartarusPopulation load(CompoundTag t){var p=new TartarusPopulation();if(t.m_128441_("min"))try{p.range(t.m_128451_("min"),t.m_128451_("max"));}catch(IllegalArgumentException ignored){}var c=t.m_128469_("counts");for(String key:c.m_128431_())try{int f=Integer.parseInt(key),n=c.m_128451_(key);if(f>0&&f<=250&&n>0&&n<=64)p.counts.put(f,n);}catch(NumberFormatException ignored){}return p;}
}
