package com.gx.velveteclipse.shuffle;
import com.google.gson.Gson;
import com.gx.velveteclipse.util.RuntimeBridge;
import java.util.*;

/** Offers are persisted when earned; closing the screen cannot reroll them. */
public final class ShuffleData {
 private static final String KEY="velvet_eclipse:shuffle_v1";
 private static final Gson JSON=new Gson();
 private final List<ShuffleRules.Round> rounds=new ArrayList<>();
 public List<ShuffleRules.Round> rounds(){return List.copyOf(rounds);}
 public ShuffleRules.Round first(){return rounds.isEmpty()?null:rounds.get(0);}
 public void add(ShuffleRules.Round round){rounds.add(round);}
 public boolean consume(String token){if(first()==null||!first().token().equals(token))return false;rounds.remove(0);return true;}
 public static ShuffleData load(Object player){
  var d=new ShuffleData();String json=RuntimeBridge.tagGetString(RuntimeBridge.persistentData(player),KEY);
  if(json!=null&&!json.isBlank()){var list=JSON.fromJson(json,ShuffleRules.Round[].class);if(list!=null)d.rounds.addAll(Arrays.asList(list));}return d;
 }
 public void save(Object player){RuntimeBridge.tagPutString(RuntimeBridge.persistentData(player),KEY,JSON.toJson(rounds));}
 public static void copy(Object from,Object to){RuntimeBridge.tagPutString(RuntimeBridge.persistentData(to),KEY,RuntimeBridge.tagGetString(RuntimeBridge.persistentData(from),KEY));}
}
