package com.gx.velveteclipse.shuffle;
import com.gx.velveteclipse.core.persona.*;
import java.util.*;
import com.google.gson.*;
/** Exact floor pools. On original boss gaps use the preceding band, never the next. */
public final class ReloadShuffle {
 private static final JsonArray PERSONAS=ReloadCatalogue.json("shuffle-personas.json").getAsJsonArray(),RANKS=ReloadCatalogue.json("shuffle-ranks.json").getAsJsonArray();
 private static JsonObject band(JsonArray rows,int floor){var result=rows.get(0).getAsJsonObject();for(var row:rows){var o=row.getAsJsonObject();if(o.get("first").getAsInt()>floor)break;result=o;}return result;}
 public static Map<String,Integer> personas(int floor){var out=new LinkedHashMap<String,Integer>();band(PERSONAS,floor).getAsJsonObject("personas").entrySet().forEach(e->{if(PersonaRegistry.persona(e.getKey()).isPresent())out.put(e.getKey(),e.getValue().getAsInt());});return out;}
 public static int rank(int floor,Random rng){var b=band(RANKS,floor);return b.get("low").getAsInt()+rng.nextInt(b.get("high").getAsInt()-b.get("low").getAsInt()+1);}
 public static String persona(int floor,Random rng){var pool=personas(floor);int roll=rng.nextInt(pool.values().stream().mapToInt(Integer::intValue).sum());for(var e:pool.entrySet()){roll-=e.getValue();if(roll<0)return e.getKey();}throw new IllegalStateException("Empty floor pool");}
 public static List<String> cards(int rank){return PersonaRegistry.skills().keySet().stream().filter(id->{var info=ReloadCatalogue.info(id);return info!=null&&info.rank()==rank;}).sorted().toList();}
 public static int level(int floor){return personas(floor).keySet().stream().mapToInt(id->PersonaRegistry.persona(id).orElseThrow().baseLevel()).max().orElse(1);}
}
