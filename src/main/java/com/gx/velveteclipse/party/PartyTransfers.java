package com.gx.velveteclipse.party;
import com.gx.velveteclipse.equipment.*;
import com.gx.velveteclipse.forge.PlayerPersonaData;
public final class PartyTransfers {
 public static boolean yen(PlayerPersonaData from,PlayerPersonaData to,long amount){if(from==to||amount<=0||amount>9_999_999_999L-to.yen()||from.yen()<amount)return false;if(!from.spendYen(amount))return false;to.addYen(amount);return true;}
 public static boolean supply(EquipmentLoadout from,EquipmentLoadout to,String id,int amount){if(from==to||amount<=0||amount>999||Supplies.get(id)==null||from.count(id)<amount||to.count(id)>999-amount)return false;for(int i=0;i<amount;i++){from.consume(id);to.addSupply(id);}return true;}
 public static boolean gear(EquipmentLoadout from,EquipmentLoadout to,String id){if(from==to||EquipmentCatalog.get(id)==null||!from.owned.contains(id)||from.equipped.containsValue(id)||to.owned.contains(id))return false;from.owned.remove(id);to.grant(id);return true;}
}
