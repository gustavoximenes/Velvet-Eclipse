package com.gx.velveteclipse.equipment;
import com.google.gson.Gson;
import com.gx.velveteclipse.core.persona.Element;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Reload base-game facts, shipped on both sides; transactions always use the server copy. */
public final class EquipmentCatalog {
 public enum Slot {WEAPON,ARMOR,FOOTWEAR,ACCESSORY}
 public enum Family {NONE,SWORD,BOW,GREATSWORD,FISTS,RAPIER,GUN,KNIFE,SPEAR,AXE;
  public Element element(){return switch(this){case BOW,GUN,SPEAR->Element.PIERCE;case FISTS,AXE->Element.PHYSICAL;default->Element.SLASH;};}
 }
 public record Gear(String id,String name,Slot slot,Family family,int value,int price,int originalPrice,int sell,String effect) {
  public String metric(){return switch(slot){case WEAPON->"ATQ";case ARMOR->"DEF";case FOOTWEAR->"EVA";case ACCESSORY->"EFEITO";};}
 }
 private static final Map<String,Gear> ALL=load();
 private static Map<String,Gear> load(){
  try(var in=EquipmentCatalog.class.getResourceAsStream("/data/velvet_eclipse/reload_equipment.json")){
   if(in==null)throw new IOException("Missing Reload equipment catalog");
   var result=new LinkedHashMap<String,Gear>();
   for(var g:new Gson().fromJson(new InputStreamReader(in,StandardCharsets.UTF_8),Gear[].class)){
    if(g.id()==null||g.slot()==null||g.family()==null||g.price()<0||g.value()<0||result.put(g.id(),g)!=null)throw new IOException("Invalid equipment: "+g.id());
   }return Collections.unmodifiableMap(result);
  }catch(IOException e){throw new ExceptionInInitializerError(e);}
 }
 public static Gear get(String id){return ALL.get(id);}
 public static Collection<Gear> all(){return ALL.values();}
 public static String label(Slot s){return switch(s){case WEAPON->"ARMA";case ARMOR->"ARMADURA";case FOOTWEAR->"CALÇADO";case ACCESSORY->"ACESSÓRIO";};}
}
