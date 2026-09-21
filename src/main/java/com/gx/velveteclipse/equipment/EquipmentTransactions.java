package com.gx.velveteclipse.equipment;
import com.gx.velveteclipse.forge.PlayerPersonaData;
/** Pure purchase operation after the server verifies the merchant, distance and nonce. */
public final class EquipmentTransactions {
 public enum Result {BOUGHT,UNKNOWN,OWNED,FULL,NO_YEN}
 public static Result buy(EquipmentLoadout bag,PlayerPersonaData wallet,String id){
  var gear=EquipmentCatalog.get(id);var supply=Supplies.get(id);if(gear==null&&supply==null)return Result.UNKNOWN;
  if(gear!=null&&bag.owned.contains(id))return Result.OWNED;
  if(supply!=null&&bag.count(id)>=999)return Result.FULL;
  if(!wallet.spendYen(gear!=null?gear.price():supply.price()))return Result.NO_YEN;
  if(gear!=null)bag.grant(id);else bag.addSupply(id);return Result.BOUGHT;
 }
}
