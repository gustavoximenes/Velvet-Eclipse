package com.gx.velveteclipse.core.persona;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Validated, versioned factual data. Runtime never contacts a website. */
public final class ReloadCatalogue {
 public record SkillInfo(String id,String name,String element,int power,int accuracy,int sp,int hp,boolean critical,String target,boolean passive,int rank,int minLevel,String effect,String detail,double value,int minHits,int maxHits,int critRate,boolean universal,int source){}
 private static final Map<String,SkillInfo> SKILLS=new LinkedHashMap<>();
 private static final Set<String> IDS=new LinkedHashSet<>();
 private static final Map<String,List<String>> SPECIAL=new LinkedHashMap<>();
 public static JsonElement json(String file){
  try(var stream=ReloadCatalogue.class.getResourceAsStream("/data/velvet_eclipse/catalogue/"+file)){
   if(stream==null)throw new IllegalStateException("Missing catalogue: "+file);
   return JsonParser.parseReader(new InputStreamReader(stream,StandardCharsets.UTF_8));
  }catch(IOException e){throw new IllegalStateException(file,e);}
 }
 static void register(){
  var gson=new Gson();
  for(var row:json("skills.json").getAsJsonArray()){
   var s=gson.fromJson(row,SkillInfo.class);SKILLS.put(s.id(),s);
   // Preserve existing releases' spell balance and save IDs.
   PersonaRegistry.registerCatalogueSkill(new SkillDefinition(s.id(),s.name(),Element.valueOf(s.element()),s.power(),s.accuracy(),s.sp(),s.hp(),s.critical(),SkillTarget.valueOf(s.target())));
  }
  for(var row:json("personas.json").getAsJsonArray()){
   var o=row.getAsJsonObject();String id=o.get("id").getAsString();IDS.add(id);int[] stats=gson.fromJson(o.get("stats"),int[].class);
   var affinities=new EnumMap<Element,Affinity>(Element.class);o.getAsJsonObject("affinities").entrySet().forEach(e->affinities.put(Element.valueOf(e.getKey()),Affinity.valueOf(e.getValue().getAsString())));
   var learn=new TreeMap<Integer,List<String>>();o.getAsJsonObject("learnset").entrySet().forEach(e->learn.put(Integer.parseInt(e.getKey()),List.of(gson.fromJson(e.getValue(),String[].class))));
   PersonaRegistry.registerCataloguePersona(new PersonaDefinition(id,o.get("name").getAsString(),Arcana.valueOf(o.get("arcana").getAsString()),o.get("level").getAsInt(),new PersonaStats(stats[0],stats[1],stats[2],stats[3],stats[4]),affinities,learn));
  }
  json("special-fusions.json").getAsJsonObject().entrySet().forEach(e->SPECIAL.put(e.getKey(),List.of(gson.fromJson(e.getValue(),String[].class))));
 }
 public static SkillInfo info(String id){PersonaRegistry.bootstrap();return SKILLS.get(PersonaRegistry.canonicalSkillId(id));}
 public static Set<String> ids(){PersonaRegistry.bootstrap();return Collections.unmodifiableSet(IDS);}
 public static Map<String,List<String>> special(){PersonaRegistry.bootstrap();return Collections.unmodifiableMap(SPECIAL);}
 public static boolean provisional(String id){return ids().contains(id)&&!id.equals("orpheus")&&!id.equals("thanatos");}
 private ReloadCatalogue(){}
}
