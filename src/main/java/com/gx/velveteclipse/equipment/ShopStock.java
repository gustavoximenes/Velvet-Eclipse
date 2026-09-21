package com.gx.velveteclipse.equipment;
import java.util.Set;
/** Reload's initial Police Station stock, before the May 9 train operation. */
public final class ShopStock {
 public static final Set<String> INITIAL=Set.of("saber","gladius","short_bow","rainbow","bastard_sword","kishido_blade","rash_guard","chest_protector","high_tech_sandals","rubber_sole","pixie_dust_talisman","micro_sash","power_band","magic_band","guard_band","speed_band","luck_band");
 public static boolean available(String id,boolean unlocked){return EquipmentCatalog.get(id)!=null?(unlocked||INITIAL.contains(id)):Supplies.get(id)!=null;}
}
