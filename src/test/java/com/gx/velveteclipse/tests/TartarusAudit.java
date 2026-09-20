package com.gx.velveteclipse.tests;
import com.gx.velveteclipse.darkhour.*;
import com.gx.velveteclipse.core.persona.*;
import com.gx.velveteclipse.core.battle.*;
import com.google.gson.Gson;
import net.minecraft.nbt.CompoundTag;
import java.util.*;
import java.nio.file.*;

/** Focused regression audit for the grounded tower, procedural traversal and persistence. */
public final class TartarusAudit {
 private static int checks;
 private static void check(boolean b,String what){checks++;if(!b)throw new AssertionError(what);}
 private static String key(int x,int y,int z){return x+","+y+","+z;}
 private static Map<String,String> cells(TartarusInteriorLayout.Plan p){var result=new HashMap<String,String>();for(var c:p.cells())result.put(key(c.x(),c.y(),c.z()),c.material());return result;}
 private static boolean passable(String m){return m==null||Set.of("forward","back","exit","summit").contains(m);}
 private static void reachable(TartarusInteriorLayout.Plan plan){
  var voxels=cells(plan);var entry=plan.entry();var reached=new HashSet<TartarusInteriorLayout.Point>();var queue=new ArrayDeque<TartarusInteriorLayout.Point>();reached.add(entry);queue.add(entry);
  while(!queue.isEmpty()){var at=queue.remove();for(var d:List.of(new TartarusInteriorLayout.Point(1,0),new TartarusInteriorLayout.Point(-1,0),new TartarusInteriorLayout.Point(0,1),new TartarusInteriorLayout.Point(0,-1))){var n=new TartarusInteriorLayout.Point(at.x()+d.x(),at.z()+d.z());if(plan.walkable().contains(n)&&passable(voxels.get(key(n.x(),1,n.z())))&&passable(voxels.get(key(n.x(),2,n.z())))&&reached.add(n))queue.add(n);}}
  check(reached.contains(new TartarusInteriorLayout.Point(plan.stairs().x(),plan.stairs().z()+5)),"stair base reachable through actual blocks");
  check(reached.containsAll(plan.spawns()),"all Shadow rooms reachable through actual blocks");
  for(int step=0;step<8;step++){int x=plan.stairs().x(),z=plan.stairs().z()+4-step,y=1+step;check("stairs".equals(voxels.get(key(x,y,z))),"stair tread continuous");check(passable(voxels.get(key(x,y+1,z)))&&passable(voxels.get(key(x,y+2,z))),"stair headroom");}
 }
 public static void main(String[] args)throws Exception {
  var shapes=new HashSet<Integer>();
  for(int f:new int[]{1,2,4,7,20,83,85,166,167,248,250}){
   var p=TartarusInteriorLayout.floor(781241,f);reachable(p);shapes.add(p.walkable().hashCode());
   check(p.spawns().size()>=7,"seven distinct Shadow slots");check(p.cells().stream().map(c->key(c.x(),c.y(),c.z())).distinct().count()==p.cells().size(),"no duplicate blocks");
   for(var back:p.cells().stream().filter(c->c.material().equals("back")).toList())check(!p.walkable().contains(new TartarusInteriorLayout.Point(back.x(),back.z())),"return portal never occupies a corridor or room passage");
   check(p.cells().stream().allMatch(c->Math.abs(c.x())<96&&Math.abs(c.z())<96&&c.y()>=0&&c.y()<=13),"floors stay within separate plots");
   check(p.cells().stream().anyMatch(c->c.material().equals(f==250?"summit":"forward")),"last floor terminates instead of floor 251");
  }
  check(shapes.size()==11,"different floor layouts");check(TartarusInteriorLayout.floor(123,7).cells().equals(TartarusInteriorLayout.floor(123,7).cells()),"same seed restores same floor after restart");
  check(!TartarusInteriorLayout.floor(124,7).cells().equals(TartarusInteriorLayout.floor(123,7).cells()),"different world seeds differ");
  for(int f=1;f<=250;f++){check(TartarusInteriorLayout.floorAt(TartarusInteriorLayout.origin(f)+50)==f,"floor detection stable at room edge");check(TartarusInteriorLayout.hp(f)>TartarusInteriorLayout.hp(f-1),"HP increases on every floor");}
  try{TartarusInteriorLayout.origin(251);throw new AssertionError("floor 251 accepted");}catch(IllegalArgumentException expected){checks++;}
  var hall=TartarusInteriorLayout.hall();var map=cells(hall);
  check(hall.cells().stream().filter(c->c.material().equals("velvet_door")).count()==1,"one permanent Velvet Room door");
  check("forward".equals(map.get(key(0,17,-18))),"clock portal at landing feet height");
  for(int step=0;step<16;step++){int y=step+1,z=5-step;check("blue_stairs".equals(map.get(key(0,y,z))),"blue processional stair");check(passable(map.get(key(0,y+1,z)))&&passable(map.get(key(0,y+2,z))),"hall stair headroom");}
  var tower=TartarusLayout.create(250,1);var solid=new HashMap<String,Integer>();for(var c:tower)solid.put(key(c.x(),c.y(),c.z()),c.material());
  check(solid.get(key(0,1,24))==4,"exterior black portal");
  for(int y=27;y<238;y+=28){int tier=y/28,r=Math.max(4,20-tier*2),cx=(tier%3-1)*2,cz=((tier/2)%3-1)*2;for(int x=-r;x<=r;x++)for(int z=-r;z<=r;z++)check(solid.containsKey(key(cx+x,y,cz+z)),"tier roof sealed");}
  var state=new TartarusService.State();state.y=64;state.height=256;state.drop=1;state.column=12;state.scanY=65;state.scanBottom=48;state.leveled=false;state.owner="owner";
  var restored=TartarusService.State.load(state.m_7176_(new CompoundTag()));check(restored.scanBottom==48&&restored.scanY==65&&restored.column==12&&!restored.leveled,"leveling resumes correct original column depth after restart");
  var old=state.m_7176_(new CompoundTag());old.m_128405_("format",2);check(TartarusService.State.load(old).removing,"old floating tower removed on upgrade");
  var dungeon=new TartarusDungeonService.State();dungeon.built.addAll(List.of(0,1,250));var slot=new CompoundTag();slot.m_128359_("entity","uuid");slot.m_128359_("battle","owner");dungeon.slots.put("1:0",slot);
  var copy=TartarusDungeonService.State.load(dungeon.m_7176_(new CompoundTag()));check(copy.built.equals(dungeon.built)&&copy.slots.get("1:0").m_128461_("battle").equals("owner"),"shared floor and active encounter slots persist");
  PersonaRegistry.bootstrap();var def=PersonaRegistry.persona("umbra_slime").orElseThrow();var a=new BattleCombatant(UUID.randomUUID(),"a",BattleSide.ENEMIES,new PersonaInstance(def),85,85,30,30);var b=new BattleCombatant(UUID.randomUUID(),"b",BattleSide.ENEMIES,new PersonaInstance(def.id(),251,0,new PersonaInstance(def).learnedSkills()),2085,2085,280,280);check(b.stats().strength()>a.stats().strength()&&b.stats().endurance()>a.stats().endurance(),"floor levels increase real combat stats");
  var out=Path.of("build/reports/tartarus");Files.createDirectories(out);Files.writeString(out.resolve("hall.json"),new Gson().toJson(hall.cells()));Files.writeString(out.resolve("floor.json"),new Gson().toJson(TartarusInteriorLayout.floor(781241,7).cells()));
  var resources=Path.of("src/main/resources");check(Files.exists(resources.resolve("data/velvet_eclipse/dimension/tartarus.json")),"new dimension present");
  for(String m:hall.cells().stream().map(TartarusInteriorLayout.Cell::material).distinct().toList())check(Files.exists(resources.resolve("assets/velvet_eclipse/blockstates/"+(m.equals("velvet_door")?"velvet_door":"tartarus_"+m)+".json")),"hall palette model: "+m);
  System.out.println("PASS tartarus: "+checks+" checks; 11 representative floors, connected rooms, stair/portal clearance, tier closure, seed stability, floor 250 cap, combat progression and saved state. Offline only.");
 }
}
