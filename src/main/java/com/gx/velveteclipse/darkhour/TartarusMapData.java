package com.gx.velveteclipse.darkhour;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
/** Per-player fog of war, deliberately independent of permanent terminal unlocks. */
public final class TartarusMapData {
 public long cycle=-1;
 private final Map<Integer,Set<Integer>> sectors=new HashMap<>();
 private final Set<String> chests=new HashSet<>();
 public static int sector(double x,double z){return (Math.floorDiv((int)Math.floor(x)+6,12)<<16)|(Math.floorDiv((int)Math.floor(z)+6,12)&65535);}
 public boolean reset(long next){if(cycle==next)return false;cycle=next;sectors.clear();chests.clear();return true;}
 public boolean reveal(int floor,double x,double z){return sectors.computeIfAbsent(floor,k->new HashSet<>()).add(sector(x,z));}
 public boolean known(int floor,double x,double z){return sectors.getOrDefault(floor,Set.of()).contains(sector(x,z));}
 public boolean seeChest(int floor,int index){return chests.add(floor+":"+index);}
 public boolean chest(int floor,int index){return chests.contains(floor+":"+index);}
 public boolean merge(TartarusMapData other){if(other==this||other.cycle!=cycle)return false;boolean changed=false;for(var e:other.sectors.entrySet())changed|=sectors.computeIfAbsent(e.getKey(),k->new HashSet<>()).addAll(e.getValue());changed|=chests.addAll(other.chests);return changed;}
 public CompoundTag save(){var t=new CompoundTag();t.m_128356_("cycle",cycle);var floors=new CompoundTag();sectors.forEach((f,s)->floors.m_128385_(f.toString(),s.stream().mapToInt(Integer::intValue).toArray()));t.m_128365_("floors",floors);var seen=new CompoundTag();chests.forEach(k->seen.m_128379_(k,true));t.m_128365_("chests",seen);return t;}
 public static TartarusMapData load(CompoundTag t){var m=new TartarusMapData();if(t.m_128441_("cycle"))m.cycle=t.m_128454_("cycle");var f=t.m_128469_("floors");for(String k:f.m_128431_())try{var s=new HashSet<Integer>();for(int id:f.m_128465_(k))s.add(id);m.sectors.put(Integer.parseInt(k),s);}catch(NumberFormatException ignored){}m.chests.addAll(t.m_128469_("chests").m_128431_());return m;}
}
