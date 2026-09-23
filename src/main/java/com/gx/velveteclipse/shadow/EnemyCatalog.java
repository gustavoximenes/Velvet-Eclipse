package com.gx.velveteclipse.shadow;

import com.gx.velveteclipse.core.persona.*;
import java.util.*;
import static com.gx.velveteclipse.core.persona.Element.*;
import static com.gx.velveteclipse.core.persona.Affinity.*;

/** Reload's Thebel and initial Arqa bestiary; source/version notes: docs/SHADOWS-RELOAD.md. */
public final class EnemyCatalog {
 private static final Map<String,EnemyDefinition> ALL=new LinkedHashMap<>();
 static {
  add("cowardly_maya","Cowardly Maya","maya",2,19,2,83,20,20,false,SLASH,Map.of(SLASH,WEAK,FIRE,WEAK),"frost_shard");
  add("muttering_tiara","Muttering Tiara","tiara",2,21,3,88,40,20,false,PHYSICAL,Map.of(WIND,WEAK),"agi","rakunda");
  add("merciless_maya","Merciless Maya","maya",3,9,2,83,15,20,false,SLASH,Map.of(FIRE,RESIST,ICE,WEAK),"agi");
  add("magic_hand","Magic Hand","hand",7,21,4,97,25,20,false,PHYSICAL,Map.of(FIRE,WEAK,ELECTRIC,RESIST),"frost_shard");
  add("obsessed_cupid","Obsessed Cupid","cupid",9,21,5,101,15,20,false,PIERCE,Map.of(ICE,WEAK,WIND,RESIST),"gale_cut","dia");
  add("trance_twins","Trance Twins","twins",12,21,6,110,20,20,false,PHYSICAL,Map.of(ICE,RESIST,ELECTRIC,WEAK,LIGHT,WEAK),"frost_shard");
  add("wealth_hand","Wealth Hand","hand",12,21,7,250,30,1,false,PHYSICAL,Map.of(PHYSICAL,WEAK),"cleave");
  add("resolved_checkmate","Resolved Checkmate","checkmate",12,21,10,935,50,1,false,PHYSICAL,Map.of(ELECTRIC,WEAK),"cleave","agi");
  add("grave_beetle","Grave Beetle","beetle",14,21,7,282,15,20,false,PHYSICAL,Map.of(WIND,WEAK,ELECTRIC,RESIST),"cleave");
  add("heat_balance","Heat Balance","balance",23,34,10,170,50,20,false,PHYSICAL,Map.of(FIRE,RESIST,ICE,RESIST,ELECTRIC,WEAK,WIND,WEAK),"agi","frost_shard");
  add("frivolous_maya","Frivolous Maya","maya",23,37,9,142,15,20,false,SLASH,Map.of(PHYSICAL,WEAK,ICE,WEAK,WIND,RESIST),"gale_cut");
  add("treasure_hand","Treasure Hand","hand",23,41,12,336,50,1,false,PHYSICAL,Map.of(LIGHT,WEAK),"cleave");
  add("yearning_checkmate","Yearning Checkmate","checkmate",23,41,14,1656,80,1,false,PHYSICAL,Map.of(DARK,WEAK),"cleave","gale_cut");
  add("laughing_table","Laughing Table","table",24,34,9,162,25,20,false,PHYSICAL,Map.of(PIERCE,RESIST,ICE,RESIST,WIND,WEAK),"frost_shard");
  add("grieving_tiara","Grieving Tiara","tiara",24,41,10,176,50,20,false,PHYSICAL,Map.of(FIRE,WEAK,ICE,NULL),"frost_shard");
  add("black_raven","Black Raven","raven",25,37,11,192,40,20,false,SLASH,Map.of(FIRE,NULL,WIND,RESIST,LIGHT,WEAK),"agi");
  add("ruthless_ice_raven","Ruthless Ice Raven","raven",5,5,5,218,300,0,true,SLASH,Map.of(FIRE,WEAK,ICE,RESIST,WIND,RESIST),"frost_shard","raid_ice_storm");
  add("barbaric_beast_wheel","Barbaric Beast Wheel","wheel",11,11,7,390,300,0,true,PHYSICAL,Map.of(FIRE,RESIST,WIND,WEAK),"cleave");
  add("swift_axle","Swift Axle","axle",17,17,9,422,300,0,true,SLASH,Map.of(SLASH,RESIST,PHYSICAL,RESIST,ELECTRIC,WEAK,WIND,RESIST),"gale_cut","tarukaja");
 }
 private static void add(String id,String name,String family,int first,int last,int level,int hp,int sp,int weight,boolean boss,Element attack,Map<Element,Affinity> affinities,String...skills){
  // Combat stat growth is adapted to this mod; affinities, base HP/SP and floors are Reload data.
  int s=Math.max(2,level),m=Math.max(3,level+2);
  if(family.equals("beetle")||family.equals("wheel")){s+=3;m=2;}
  ALL.put(id,new EnemyDefinition(id,name,family,first,last,level,hp,sp,new PersonaStats(s,m,Math.max(3,level),Math.max(2,level-1),Math.max(2,level/2)),affinities,new LinkedHashSet<>(List.of(skills)),attack,weight,boss));
 }
 public static Collection<EnemyDefinition> all(){return Collections.unmodifiableCollection(ALL.values());}
 public static EnemyDefinition get(String id){return Objects.requireNonNull(ALL.get(id),"Unknown Shadow: "+id);}
 public static int sourceFloor(int floor){return floor<2?2:floor==22?21:Math.min(25,floor);}
 public static List<EnemyDefinition> pool(int floor){int f=sourceFloor(floor);return ALL.values().stream().filter(e->!e.guardian()&&e.firstFloor()<=f&&e.lastFloor()>=f).toList();}
 public static EnemyDefinition guardian(int floor){return get(switch(Math.floorMod(floor/10-1,3)){case 0->"ruthless_ice_raven";case 1->"barbaric_beast_wheel";default->"swift_axle";});}
}
