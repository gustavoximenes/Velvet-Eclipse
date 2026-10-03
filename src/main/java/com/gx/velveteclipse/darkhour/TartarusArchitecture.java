package com.gx.velveteclipse.darkhour;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import static com.gx.velveteclipse.darkhour.TartarusInteriorLayout.*;

/** Approved visual templates applied after the original random graph has finished. */
public final class TartarusArchitecture {
 public static final int VERSION=2;
 public record Piece(String model,int x,int y,int z){}
 public record Bounds(double minX,double minY,double minZ,double maxX,double maxY,double maxZ){}
 private static final class Catalog {
  static final JsonObject DATA=read();
  private static JsonObject read(){try(var in=TartarusArchitecture.class.getResourceAsStream("/assets/velvet_eclipse/tartarus/catalog.json")){
   if(in==null)throw new IllegalStateException("Missing Tartarus architecture catalog");return JsonParser.parseReader(new InputStreamReader(in,StandardCharsets.UTF_8)).getAsJsonObject();
  }catch(IOException e){throw new IllegalStateException(e);}}
 }
 public static JsonObject template(String id){var t=Catalog.DATA.getAsJsonObject(id);if(t==null)throw new IllegalArgumentException("Unknown architecture "+id);return t;}
 public static Bounds bounds(String id){var a=template(id).getAsJsonArray("bounds");return new Bounds(a.get(0).getAsDouble(),a.get(1).getAsDouble(),a.get(2).getAsDouble(),a.get(3).getAsDouble(),a.get(4).getAsDouble(),a.get(5).getAsDouble());}
 public static Set<Point> footprint(String id,String key){var out=new HashSet<Point>();var a=template(id).getAsJsonArray(key);if(a!=null)for(var el:a){var p=el.getAsJsonArray();out.add(new Point(p.get(0).getAsInt(),p.get(1).getAsInt()));}return out;}
 private static String key(int x,int y,int z){return x+","+y+","+z;}
 private static void put(Map<String,Cell> out,int x,int y,int z,String material){out.put(key(x,y,z),new Cell(x,y,z,material));}
 private static long mix(long x){x=(x^(x>>>30))*0xbf58476d1ce4e5b9L;x=(x^(x>>>27))*0x94d049bb133111ebL;return x^(x>>>31);}
 private static int radius(Plan p,Point c){return p.cells().stream().filter(v->v.y()==1&&v.material().equals("column")&&Math.abs(v.x()-c.x())==Math.abs(v.z()-c.z())&&Math.abs(v.x()-c.x())>=6&&Math.abs(v.x()-c.x())<10).mapToInt(v->Math.abs(v.x()-c.x())+1).min().orElse(10);}
 private static final int[][] DIR={{0,-1},{1,0},{0,1},{-1,0}};
 public static Plan apply(long seed,int floor,Plan old){
  if(fixedFloor(floor))return fixed(floor,old);
  var cells=new LinkedHashMap<String,Cell>();var pieces=new ArrayList<Piece>();var tiles=new HashSet<Point>();var covered=new HashSet<Point>();var blocked=new HashSet<Point>();int[] radii=new int[16];for(int i=0;i<16;i++)radii[i]=radius(old,new Point(i%4*24-36,i/4*24-36));
  for(int i=0;i<16;i++){
   var center=new Point(i%4*24-36,i/4*24-36);int r=radii[i],mask=0;
   for(int d=0;d<4;d++){var q=new Point(center.x()+DIR[d][0]*12,center.z()+DIR[d][1]*12);if(old.walkable().contains(q))mask|=1<<d;}
   if(i==0)mask|=1; // permanent return portal on the first room's north wall
   String kind=center.equals(old.stairs())?"stairs":i==0?"plain":switch((int)Math.floorMod(mix(seed^i*0x632be59bd9b4e019L^floor),4)){case 0->"plain";case 1->"long";case 2->"pillars";default->r>=9?"platforms":"plain";};
   // Twin stairs require two opposed links to neighbouring rooms. At a curve,
   // branch or dead end choose another template instead of cutting a platform.
   if(kind.equals("platforms")&&mask!=5&&mask!=10)kind="plain";
   String id="room_"+r+"_"+kind+"_"+mask;pieces.add(new Piece(id,center.x(),0,center.z()));
   for(var q:footprint(id,"tiles")){var at=new Point(q.x()+center.x(),q.z()+center.z());tiles.add(at);covered.add(at);}
   for(var q:footprint(id,"blocked")){var at=new Point(q.x()+center.x(),q.z()+center.z());blocked.add(at);for(int y=1;y<14;y++)put(cells,at.x(),y,at.z(),"scenic_wall");}
   var collision=template(id).getAsJsonArray("collision");
   if(collision!=null)for(var element:collision){var c=element.getAsJsonArray();put(cells,center.x()+c.get(0).getAsInt(),c.get(1).getAsInt(),center.z()+c.get(2).getAsInt(),c.get(3).getAsString());}
  }
  // Preserve all narrow graph links between module footprints; do not draw a wall over a connection.
  for(var p:old.walkable())if(!covered.contains(p)){
   boolean inRoom=false;for(int i=0;i<16;i++){int x=i%4*24-36,z=i/4*24-36;int r=radii[i];if(Math.abs(p.x()-x)<=r&&Math.abs(p.z()-z)<=r){inRoom=true;break;}}
   if(!inRoom)tiles.add(p);
  }
  for(var p:tiles){
   put(cells,p.x(),0,p.z(),"scenic_tile");put(cells,p.x(),14,p.z(),"scenic_wall");int mask=0;
   for(int d=0;d<4;d++){int x=p.x()+DIR[d][0],z=p.z()+DIR[d][1];if(tiles.contains(new Point(x,z)))mask|=1<<d;else for(int y=1;y<14;y++)put(cells,x,y,z,"scenic_wall");}
   if(!covered.contains(p))pieces.add(new Piece("passage_"+mask+"_"+Math.floorMod(p.x()+p.z(),7),p.x(),0,p.z()));
  }
  // Existing stairs and functional portal cells keep their exact destinations and anchors.
  for(var c:old.cells())if(Set.of("stairs","back","forward","summit").contains(c.material()))put(cells,c.x(),c.y(),c.z(),c.material().equals("stairs")?"scenic_stairs":c.material());
  var st=old.stairs();for(var c:old.cells())if(c.material().equals("wall")&&Math.abs(c.x()-st.x())<=3&&c.z()>=st.z()-6&&c.z()<=st.z()+4&&c.y()>0&&c.y()<=8)put(cells,c.x(),c.y(),c.z(),"scenic_wall");
  // Reserve the five-wide doorway and its approach after all decoration/collision.
  // Future variants cannot accidentally seal a connection in the generated graph.
  for(var piece:pieces)if(piece.model().startsWith("room_")){
   String[] parts=piece.model().split("_");int r=Integer.parseInt(parts[1]),mask=Integer.parseInt(parts[3]);
   for(var p:entrances(r,mask))for(int y=1;y<=4;y++){
    String k=key(piece.x()+p.x(),y,piece.z()+p.z());var cell=cells.get(k);
    if(cell!=null&&cell.material().startsWith("scenic_"))cells.remove(k);
   }
  }
  old.cells().stream().filter(c->c.material().equals("back")).findFirst().ifPresent(c->pieces.add(new Piece("kit_back",-36,1,c.z()+1)));
  // Real block light, spaced through the rooms and links, separate from material emission.
  lights(cells,pieces,tiles);
  var navigable=new HashSet<>(tiles);navigable.removeAll(blocked);
  for(var cell:cells.values())if(cell.y()==1&&cell.material().equals("scenic_wall"))navigable.remove(new Point(cell.x(),cell.z()));
  for(var cell:old.cells())if(cell.material().equals("stairs"))navigable.add(new Point(cell.x(),cell.z()));
  for(int x=-3;x<=3;x++)for(int z=-6;z<=-4;z++)navigable.add(new Point(st.x()+x,st.z()+z));
  return new Plan(List.copyOf(cells.values()),old.entry(),old.stairs(),old.spawns(),Set.copyOf(navigable),List.copyOf(pieces));
 }
 private static Plan fixed(int floor,Plan old){
  var cells=new LinkedHashMap<String,Cell>();
  for(var c:old.cells()){
   String m=c.material();if(floor==0&&Set.of("clock","hand").contains(m))continue;
   if(m.equals("velvet_door")||Set.of("forward","back","exit","summit","boss_gate","barrier").contains(m)){put(cells,c.x(),c.y(),c.z(),m);continue;}
   // Old square columns and door surrounds must not remain as invisible obstacles under the new forms.
   if(c.y()>0&&floor==0){
    boolean shell=Math.abs(c.x())==28||Math.abs(c.z())==30||c.y()==34;
    boolean steps=Math.abs(c.x())<=5&&c.z()>=-10&&c.z()<=5&&c.y()<=6-c.z();
    boolean landing=c.x()>=-6&&c.x()<=5&&c.z()>=-17&&c.z()<=-11&&c.y()<=16;
    if(!shell&&!steps&&!landing)continue;
   }else if(c.y()>0&&m.equals("column"))continue;
   else if(c.y()>0&&bossFloor(floor)&&c.x()>=-9&&c.x()<=-8&&c.z()>=289&&c.z()<=295&&c.y()<=5)continue;
   String next=m.equals("stairs")||m.equals("blue_stairs")?"scenic_stairs":m.equals("window")||m.equals("azure")||m.equals("violet")||m.equals("amber")||m.equals("rose")?"scenic_window":c.y()==0?"scenic_tile":"scenic_wall";
   put(cells,c.x(),c.y(),c.z(),next);
  }
  if(floor==0)for(int x:new int[]{-20,20})for(int z:new int[]{-22,-7,9,23})pillar(cells,x,z,26,1.3);
  else if(bossFloor(floor)){
   int[] centers={292,256,220},radii={9,14,10};for(int i=0;i<3;i++)for(int x:new int[]{-radii[i]+1,radii[i]-1})for(int z:new int[]{centers[i]-radii[i]+1,centers[i]+radii[i]-1})pillar(cells,x,z,11,.55);
   if(floor%20==0)for(int x:new int[]{-13,13})for(int z:new int[]{249,256,263})pillar(cells,x,z,11,.56);
  }
  if(floor==0){
   // Match the approved clock tunnel: no old solid disk or early invisible trigger across the opening.
   cells.values().removeIf(c->c.x()>=-3&&c.x()<=3&&c.y()>=17&&c.y()<=22&&c.z()>=-23&&c.z()<=-17);
   for(int x=-2;x<=2;x++)for(int z=-23;z<=-17;z++)put(cells,x,16,z,"scenic_tile");
   for(int x=-2;x<=2;x++)for(int y=17;y<=21;y++)put(cells,x,y,-22,"forward");
  }
  String id=floor==0?"hall":transitionFloor(floor)?"andar-25":floor%20==0?"andar-20":"andar-10";
  var pieces=new ArrayList<Piece>();pieces.add(new Piece(id,0,0,floor==0?0:256));
  var returns=old.cells().stream().filter(c->c.material().equals("back")||c.material().equals("exit")).toList();if(!returns.isEmpty())pieces.add(new Piece("kit_return",0,1,returns.get(0).z()));
  if(bossFloor(floor)){put(cells,-1,1,GATE_Z,"guardian_mesh_left");put(cells,0,1,GATE_Z,"guardian_mesh_right");}
  var nav=new HashSet<Point>();for(var c:cells.values())if(c.y()==0)nav.add(new Point(c.x(),c.z()));
  for(var c:cells.values())if(c.y()==1&&Set.of("scenic_wall","scenic_window").contains(c.material()))nav.remove(new Point(c.x(),c.z()));
  // Stair risers and platforms remain on the chart, even though the base is solid below their tread.
  for(var c:old.cells())if(c.material().equals("stairs")||c.material().equals("blue_stairs"))nav.add(new Point(c.x(),c.z()));
  if(floor==0)for(int x=-6;x<=5;x++)for(int z=-17;z<=-11;z++)nav.add(new Point(x,z));
  else{int w=transitionFloor(floor)?4:3;for(int x=-w;x<=w;x++)for(int z=old.stairs().z()-6;z<=old.stairs().z()-4;z++)nav.add(new Point(x,z));}
  if(floor==0)for(int x=-2;x<=2;x++)for(int z=-23;z<=-17;z++)nav.add(new Point(x,z));
  lights(cells,pieces,nav);
  return new Plan(List.copyOf(cells.values()),old.entry(),old.stairs(),old.spawns(),Set.copyOf(nav),List.copyOf(pieces));
 }
 private static void pillar(Map<String,Cell> cells,int x,int z,int top,double radius){for(int xx=x-2;xx<=x+1;xx++)for(int zz=z-2;zz<=z+1;zz++)if(Math.hypot(xx+.5-x,zz+.5-z)<radius+.25)for(int y=1;y<=top;y++)put(cells,xx,y,zz,"scenic_wall");}
 private static void lights(Map<String,Cell> cells,List<Piece> pieces,Set<Point> tiles){
  for(var p:tiles)if(Math.floorMod(p.x(),7)==0&&Math.floorMod(p.z(),7)==0){int y=4;String k=key(p.x(),y,p.z());if(!cells.containsKey(k))put(cells,p.x(),y,p.z(),"scenic_light");}
  for(var piece:pieces){var ls=template(piece.model()).getAsJsonArray("lights");if(ls==null)continue;for(var el:ls){var a=el.getAsJsonArray();int x=piece.x()+(int)Math.floor(a.get(0).getAsDouble()),y=piece.y()+(int)Math.floor(a.get(1).getAsDouble()),z=piece.z()+(int)Math.floor(a.get(2).getAsDouble());if(!cells.containsKey(key(x,y,z)))put(cells,x,y,z,"scenic_light");}}
  for(var piece:pieces){var glow=template(piece.model()).getAsJsonArray("glowLights");if(glow==null)continue;for(var el:glow){var a=el.getAsJsonArray();int x=piece.x()+a.get(0).getAsInt(),y=piece.y()+a.get(1).getAsInt(),z=piece.z()+a.get(2).getAsInt();var old=cells.get(key(x,y,z));if(old==null)put(cells,x,y,z,"scenic_light");else if(Set.of("scenic_tile","scenic_wall").contains(old.material()))put(cells,x,y,z,"scenic_window");}}
 }
 public static Set<Point> entrances(int radius,int mask){
  var reserved=new HashSet<Point>();for(int d=0;d<4;d++)if((mask&(1<<d))!=0)for(int length=radius-3;length<=radius+2;length++)for(int width=-2;width<=2;width++)reserved.add(new Point(DIR[d][0]*length+(d%2==0?width:0),DIR[d][1]*length+(d%2!=0?width:0)));
  return reserved;
 }
 private TartarusArchitecture(){}
}
