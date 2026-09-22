package com.gx.velveteclipse.equipment;
import com.google.gson.Gson;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
/** Actual Reload Police Station stock; fusion/treasure-only gear stays out of the shop. */
public final class ShopStock {
 public static final int LAST_TIER=9,PERMIT_COUNT=10;
 public record Entry(String id,int tier,String date){}
 private static final Map<String,Entry> STOCK=load();
 public static final Set<String> INITIAL=STOCK.values().stream().filter(e->e.tier()==0).map(Entry::id).collect(java.util.stream.Collectors.toUnmodifiableSet());
 private static Map<String,Entry> load(){
  try(var in=ShopStock.class.getResourceAsStream("/data/velvet_eclipse/reload_shop.json")){
   if(in==null)throw new IOException("Missing shop progression");var result=new LinkedHashMap<String,Entry>();
   for(var e:new Gson().fromJson(new InputStreamReader(in,StandardCharsets.UTF_8),Entry[].class))
    if(EquipmentCatalog.get(e.id())==null||e.tier()<0||e.tier()>LAST_TIER||result.put(e.id(),e)!=null)throw new IOException("Invalid shop row: "+e.id());
   return Collections.unmodifiableMap(result);
  }catch(IOException e){throw new ExceptionInInitializerError(e);}
 }
 public static Collection<Entry> entries(){return STOCK.values();}
 public static boolean available(String id,int tier){var e=STOCK.get(id);return e!=null?e.tier()<=Math.max(0,Math.min(LAST_TIER,tier)):Supplies.get(id)!=null;}
 public static int tier(int permits){int tier=0;while(tier<PERMIT_COUNT&&(permits&(1<<tier))!=0)tier++;return Math.min(LAST_TIER,tier);}
 public static int bit(int floor){return floor>=25&&floor<=250&&floor%25==0?1<<(floor/25-1):0;}
 public static int nextFloor(int permits){for(int f=25;f<=250;f+=25)if((permits&bit(f))==0)return f;return 0;}
 public static String period(int tier){return new String[]{"Estoque inicial","10/05","14/06","18/07","08/08","10/09","06/10","06/11","10/12","01/01"}[Math.max(0,Math.min(LAST_TIER,tier))];}
}
