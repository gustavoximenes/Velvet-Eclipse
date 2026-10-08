package com.gx.velveteclipse.city;
import com.gx.velveteclipse.equipment.*;
import java.util.*;
/** Store ownership is enforced on the server, independently of the selected HUD. */
public final class CityCatalog {
 public static final List<String> SHOPS=List.of("aohige","police","mayoido","blue","chagall","mandragora","arcade","escapade","kenneth");
 public static final List<String> NAMES=List.of("Aohige Pharmacy","Tatsumi East Police","Mayoido Antiques","Be Blue V","Chagall Café","Mandragora","Game Parade","Club Escapade","Kenneth Arnold");
 public static String owner(String item){if(EquipmentCatalog.get(item)!=null)return "police";var s=Supplies.get(item);if(s==null)return "";return DamageItems.get(item)!=null||s.sp()!=0?"mayoido":"aohige";}
 public static List<CityNetwork.Row> rows(String shop,int tier){var r=new ArrayList<CityNetwork.Row>();
  for(var g:EquipmentCatalog.all())if(owner(g.id()).equals(shop)&&ShopStock.available(g.id(),tier))r.add(new CityNetwork.Row(g.id(),g.name(),g.metric()+" "+g.value()+" • "+EquipmentCatalog.label(g.slot()),g.price()));
  for(var s:Supplies.all())if(owner(s.id()).equals(shop))r.add(new CityNetwork.Row(s.id(),s.name(),s.description(),s.price()));
  switch(shop){case "chagall"->r.add(new CityNetwork.Row("coffee","Tomar café","Recupera 20 SP.",500));case "mandragora"->r.add(new CityNetwork.Row("sing","Cantar","Treinar coragem uma vez por dia.",800));case "arcade"->r.add(new CityNetwork.Row("train","Treinamento","Treinar a experiência do jogador uma vez por dia.",1500));case "escapade"->r.add(new CityNetwork.Row("fortune","Consultar a adividente","Recupera HP e SP para sua próxima expedição.",2000));case "blue"->r.add(new CityNetwork.Row("party","Sua party","Relembrar habilidades de sua Persona.",0));default->{}}
  return List.copyOf(r);
 }
}
