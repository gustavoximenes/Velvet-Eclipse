package com.gx.velveteclipse.core.battle;
import java.util.*;
/** Persistent counters contain only revealed enemies, never an undiscovered catalog. */
public final class BestiaryProgress {
 public record Entry(int seen,int defeated,int level) {}
 private final Map<String,Entry> entries=new TreeMap<>();
 public Map<String,Entry> entries(){return Collections.unmodifiableMap(entries);}
 public void seen(String id,int level){var old=entries.getOrDefault(id,new Entry(0,0,1));entries.put(id,new Entry(increment(old.seen()),old.defeated(),Math.max(old.level(),level)));}
 public void defeated(String id,int level){var old=entries.getOrDefault(id,new Entry(1,0,level));entries.put(id,new Entry(old.seen(),increment(old.defeated()),Math.max(old.level(),level)));}
 private static int increment(int n){return n==Integer.MAX_VALUE?n:n+1;}
 public String encode(){return String.join(";",entries.entrySet().stream().map(e->e.getKey()+":"+e.getValue().seen()+":"+e.getValue().defeated()+":"+e.getValue().level()).toList());}
 public static BestiaryProgress decode(String text){var out=new BestiaryProgress();if(text==null)return out;for(String row:text.split(";")){var v=row.split(":");if(v.length!=4||!v[0].matches("[a-z0-9_]+"))continue;try{int seen=Integer.parseInt(v[1]),defeated=Integer.parseInt(v[2]),level=Integer.parseInt(v[3]);if(seen>0&&defeated>=0&&level>=1&&level<=999)out.entries.put(v[0],new Entry(seen,defeated,level));}catch(NumberFormatException ignored){}}return out;}
}
