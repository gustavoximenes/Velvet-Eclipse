package com.gx.velveteclipse.equipment;
import java.util.*;
/** Pure ownership/equip rules. A player can use every party member's equipment. */
public final class EquipmentLoadout {
 public Set<String> owned=new LinkedHashSet<>();
 public Map<EquipmentCatalog.Slot,String> equipped=new EnumMap<>(EquipmentCatalog.Slot.class);
 public Map<String,Integer> supplies=new LinkedHashMap<>();
 public static EquipmentLoadout starter(){var d=new EquipmentLoadout();d.grant("shortsword");d.equip("shortsword");return d;}
 public void sanitize(){
  if(owned==null)owned=new LinkedHashSet<>();owned.removeIf(id->EquipmentCatalog.get(id)==null);
  if(equipped==null)equipped=new EnumMap<>(EquipmentCatalog.Slot.class);
  equipped.entrySet().removeIf(e->!owned.contains(e.getValue())||EquipmentCatalog.get(e.getValue()).slot()!=e.getKey());
  if(supplies==null)supplies=new LinkedHashMap<>();supplies.entrySet().removeIf(e->Supplies.get(e.getKey())==null||e.getValue()==null||e.getValue()<=0);supplies.replaceAll((k,v)->Math.min(999,v));
 }
 public boolean grant(String id){return EquipmentCatalog.get(id)!=null&&owned.add(id);}
 public boolean equip(String id){var g=EquipmentCatalog.get(id);if(g==null||!owned.contains(id))return false;return !id.equals(equipped.put(g.slot(),id));}
 public boolean remove(EquipmentCatalog.Slot slot){return equipped.remove(slot)!=null;}
 public boolean addSupply(String id){if(Supplies.get(id)==null||count(id)>=999)return false;supplies.put(id,count(id)+1);return true;}
 public int count(String id){return supplies.getOrDefault(id,0);}
 public boolean consume(String id){int n=count(id);if(n<1)return false;if(n==1)supplies.remove(id);else supplies.put(id,n-1);return true;}
 public EquipmentBonuses bonuses(){return new EquipmentBonuses(equipped.values().stream().map(EquipmentCatalog::get).filter(Objects::nonNull).toList());}
}
