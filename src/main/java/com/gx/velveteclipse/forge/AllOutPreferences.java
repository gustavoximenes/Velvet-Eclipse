package com.gx.velveteclipse.forge;
import com.gx.velveteclipse.core.battle.AllOutStyle;
import com.gx.velveteclipse.util.RuntimeBridge;
public final class AllOutPreferences {
 private static final String KEY="velvet_eclipse:all_out_style";
 public static AllOutStyle get(Object player){return AllOutStyle.parse(RuntimeBridge.tagGetString(RuntimeBridge.persistentData(player),KEY));}
 public static boolean set(Object player,String id){if(!AllOutStyle.valid(id))return false;RuntimeBridge.tagPutString(RuntimeBridge.persistentData(player),KEY,id);return true;}
 public static void copy(Object from,Object to){set(to,get(from).id());}
}
