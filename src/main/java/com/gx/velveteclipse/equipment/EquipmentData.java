package com.gx.velveteclipse.equipment;
import com.google.gson.Gson;
import com.gx.velveteclipse.util.RuntimeBridge;
public final class EquipmentData {
 private static final String KEY="velvet_eclipse:equipment_v1";private static final Gson JSON=new Gson();
 public static EquipmentLoadout load(Object p){String raw=RuntimeBridge.tagGetString(RuntimeBridge.persistentData(p),KEY);if(raw==null||raw.isBlank())return EquipmentLoadout.starter();try{var d=JSON.fromJson(raw,EquipmentLoadout.class);if(d==null)return EquipmentLoadout.starter();d.sanitize();return d;}catch(RuntimeException e){return EquipmentLoadout.starter();}}
 public static void save(Object p,EquipmentLoadout data){data.sanitize();RuntimeBridge.tagPutString(RuntimeBridge.persistentData(p),KEY,JSON.toJson(data));}
 public static void copy(Object from,Object to){save(to,load(from));}
}
