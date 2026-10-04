package com.gx.velveteclipse.client.presentation;
import java.util.List;
import com.gx.velveteclipse.network.BattleRosterS2C;
import com.gx.velveteclipse.network.BattleSceneS2C;

public final class EncounterVisibility {
 public static boolean participant(int id,boolean active,List<BattleRosterS2C.Target> roster){return id>=0&&active&&roster.stream().anyMatch(t->t.entityId()==id&&t.hp()>0);}
 public static boolean ownScene(BattleSceneS2C.Scene scene,boolean active,List<BattleRosterS2C.Target> roster){return active&&roster.stream().anyMatch(t->!t.ally()&&t.entityId()>=0&&scene.enemies().contains(t.entityId()));}
 public static boolean hidden(int id,boolean active,List<BattleRosterS2C.Target> roster,List<BattleSceneS2C.Scene> scenes){return !participant(id,active,roster)&&scenes.stream().filter(s->!ownScene(s,active,roster)).anyMatch(s->s.enemies().contains(id));}
 private EncounterVisibility(){}
}
