package com.gx.velveteclipse.forge;
import com.gx.velveteclipse.core.battle.*;
import com.gx.velveteclipse.menu.MenuSnapshot;
import com.gx.velveteclipse.shadow.*;
import com.gx.velveteclipse.util.RuntimeBridge;
import java.util.*;
public final class BestiaryService {
 private static final String KEY="velvet_eclipse:bestiary_v1";
 private static BestiaryProgress load(Object p){return BestiaryProgress.decode(RuntimeBridge.tagGetString(RuntimeBridge.persistentData(p),KEY));}
 private static void save(Object p,BestiaryProgress data){RuntimeBridge.tagPutString(RuntimeBridge.persistentData(p),KEY,data.encode());}
 public static void seen(Object p,String id,int level){var d=load(p);d.seen(id,level);save(p,d);}
 public static void defeated(Object p,String id,int level){var d=load(p);d.defeated(id,level);save(p,d);}
 public static void copy(Object from,Object to){RuntimeBridge.tagPutString(RuntimeBridge.persistentData(to),KEY,RuntimeBridge.tagGetString(RuntimeBridge.persistentData(from),KEY));}
 public static List<MenuSnapshot.Enemy> entries(Object p){
  var data=load(p);var knowledge=BattleKnowledge.load(p);var result=new ArrayList<MenuSnapshot.Enemy>();
  // Import old observations without inventing pre-upgrade defeat totals.
  for(var def:EnemyCatalog.all())if(!data.entries().containsKey(def.id())&&!knowledge.forEnemy(def.id()).isEmpty())data.seen(def.id(),def.level());
  for(var item:data.entries().entrySet()){
   String id=item.getKey(),name,asset;var def=EnemyCatalog.all().stream().filter(d->d.id().equals(id)).findFirst().orElse(null);
   if(def!=null){name=def.displayName();asset=def.asset();}
   else if(id.equals("raid_0")){name="Sentinela do Trem";asset="wealth_hand";}
   else if(id.equals("raid_1")){name="Condutor Sombrio";asset="wealth_hand";}
   else if(id.equals("raid_2")){name="Sacerdotisa";asset="priestess";}
   else continue;
   var value=item.getValue();result.add(new MenuSnapshot.Enemy(id,name,asset,value.level(),value.seen(),value.defeated(),knowledge.forEnemy(id),id.equals("raid_2")));
  }
  result.sort(Comparator.comparing(MenuSnapshot.Enemy::name));return List.copyOf(result);
 }
}
