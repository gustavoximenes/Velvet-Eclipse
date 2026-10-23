package com.gx.velveteclipse.presentation;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
/** Shared, immutable allowlist; loading it does not initialize a client or a sound engine. */
public final class FeedbackLibrary {
 private static final Map<String,List<String>> AUDIO=Collections.unmodifiableMap(read("audio-routes.json",new TypeToken<Map<String,List<String>>>(){}.getType()));
 private static final Map<String,String> VFX=Collections.unmodifiableMap(read("skill-vfx.json",new TypeToken<Map<String,String>>(){}.getType()));
 private static final Set<String> CLIPS=AUDIO.values().stream().flatMap(Collection::stream).collect(java.util.stream.Collectors.toUnmodifiableSet());
 private static <T>T read(String name,java.lang.reflect.Type type){try(var in=new InputStreamReader(Objects.requireNonNull(FeedbackLibrary.class.getResourceAsStream("/data/velvet_eclipse/presentation/"+name)),StandardCharsets.UTF_8)){return new Gson().fromJson(in,type);}catch(IOException e){throw new IllegalStateException(name,e);}}
 public static List<String> audio(String route){return AUDIO.getOrDefault(route,List.of());}
 public static boolean clip(String id){return CLIPS.contains(id);}
 public static String profile(String skill){return VFX.getOrDefault(skill,"");}
 public static Map<String,String> bindings(){return VFX;}
 public static Map<String,List<String>> routes(){return AUDIO;}
 public static String weapon(String family){return switch(family){case "BOW"->"bow";case "GUN"->"gun";case "SPEAR"->"pierce";case "FISTS"->"strike";default->"slash";};}
 private FeedbackLibrary(){}
}
