package com.gx.velveteclipse.presentation;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/** Local mixer; missing or malformed fields fall back independently to 100%. */
public final class AudioPreferences {
 public enum Bus { VOICE, COMBAT, INTERACTION, UI, CINEMATIC }
 private final EnumMap<Bus,Float> levels=new EnumMap<>(Bus.class);
 public float get(Bus bus){return levels.getOrDefault(bus,1f);}
 public void set(Bus bus,float value){levels.put(bus,Float.isFinite(value)?Math.max(0,Math.min(1,value)):1f);}
 public static Bus classify(String clip,boolean combat){if(clip.startsWith("voice_"))return Bus.VOICE;if(Set.of("cursor","battle_target","battle_confirm","cancel","error").contains(clip))return Bus.UI;return combat?Bus.COMBAT:Bus.INTERACTION;}
 public static AudioPreferences read(Path path){var p=new AudioPreferences();var raw=new Properties();try(var r=Files.newBufferedReader(path)){raw.load(r);}catch(IOException|IllegalArgumentException e){return p;}for(var b:Bus.values())try{p.set(b,Float.parseFloat(raw.getProperty(b.name(),"1")));}catch(NumberFormatException ignored){}return p;}
 public void save(Path path)throws IOException{Files.createDirectories(path.getParent());var temp=path.resolveSibling(path.getFileName()+".tmp");var raw=new Properties();for(var b:Bus.values())raw.setProperty(b.name(),Float.toString(get(b)));try(var w=Files.newBufferedWriter(temp)){raw.store(w,"Velvet Eclipse local audio mixer");}try{Files.move(temp,path,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}catch(AtomicMoveNotSupportedException e){Files.move(temp,path,StandardCopyOption.REPLACE_EXISTING);}}
}
