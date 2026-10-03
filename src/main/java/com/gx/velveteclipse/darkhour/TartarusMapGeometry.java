package com.gx.velveteclipse.darkhour;
import java.util.*;
/** Shared world footprint, including pillar holes and the actual staircase routes. */
public final class TartarusMapGeometry {
 public static Set<TartarusInteriorLayout.Point> tiles(TartarusInteriorLayout.Plan plan){
  if(!plan.architecture().isEmpty())return plan.walkable();
  var out=new HashSet<TartarusInteriorLayout.Point>();for(var c:plan.cells())if(c.y()==0)out.add(new TartarusInteriorLayout.Point(c.x(),c.z()));
  for(var c:plan.cells())if(c.y()==1&&Set.of("wall","column","gold").contains(c.material()))out.remove(new TartarusInteriorLayout.Point(c.x(),c.z()));return Set.copyOf(out);
 }
 public static List<TartarusMapNetwork.Marker> accesses(TartarusInteriorLayout.Plan plan,int floor,boolean defeated){
  var out=new ArrayList<TartarusMapNetwork.Marker>();
  var returns=plan.cells().stream().filter(c->c.material().equals("back")||c.material().equals("exit")).toList();
  if(!returns.isEmpty())out.add(new TartarusMapNetwork.Marker(3,(float)returns.stream().mapToInt(TartarusInteriorLayout.Cell::x).average().orElse(0)+.5f,(float)returns.stream().mapToInt(TartarusInteriorLayout.Cell::z).average().orElse(0)+.5f));
  for(var c:plan.cells())if(c.material().equals("velvet_door"))out.add(new TartarusMapNetwork.Marker(5,c.x()+.5f,c.z()+.5f));
  var terminal=TartarusInteriorLayout.terminal(floor);if(terminal!=null)out.add(new TartarusMapNetwork.Marker(4,terminal.x()+.5f,terminal.z()+.5f));
  if(floor==25)out.add(new TartarusMapNetwork.Marker(6,21.5f,256.5f));
  if(floor==0)out.add(new TartarusMapNetwork.Marker(7,.5f,-21.5f));
  if(TartarusInteriorLayout.bossFloor(floor)&&!defeated)out.add(new TartarusMapNetwork.Marker(8,.5f,TartarusInteriorLayout.GATE_Z+.5f));
  return List.copyOf(out);
 }
 private TartarusMapGeometry(){}
}
