package com.gx.velveteclipse.city;
/** Server-owned, one-time decision; never inferred from who logs in first. */
public final class CityRules {
 public static boolean canChoose(int choice,String storedOwner,String authenticated,String host){return choice==0&&host!=null&&!host.isBlank()&&host.equals(authenticated)&&host.equals(storedOwner);}
 public static boolean canPurchase(String shop,String item,int tier){return CityCatalog.owner(item).equals(shop)&&com.gx.velveteclipse.equipment.ShopStock.available(item,tier);}
 private CityRules(){}
}
