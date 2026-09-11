package com.gx.velveteclipse.forge;
import com.gx.velveteclipse.core.battle.*;
import com.gx.velveteclipse.util.RuntimeBridge;
public final class BattleKnowledge {
 private static final String KEY="velvet_eclipse:enemy_affinities_v1";
 public static AffinityKnowledge load(Object player){return AffinityKnowledge.decode(RuntimeBridge.tagGetString(RuntimeBridge.persistentData(player),KEY));}
 public static void learn(Object player,String enemy,DamageOutcome hit){var k=load(player);k.learn(enemy,hit);RuntimeBridge.tagPutString(RuntimeBridge.persistentData(player),KEY,k.encode());}
 public static void copy(Object from,Object to){RuntimeBridge.tagPutString(RuntimeBridge.persistentData(to),KEY,RuntimeBridge.tagGetString(RuntimeBridge.persistentData(from),KEY));}
}
