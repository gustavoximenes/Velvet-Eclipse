package com.gx.velveteclipse.tests;
import com.gx.velveteclipse.room.VelvetRoomLayout;
import com.gx.velveteclipse.client.*;
import com.google.gson.*;
import com.gx.velveteclipse.room.VelvetDoorGeometry;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import java.nio.file.*;
import java.util.*;
import com.mojang.blaze3d.vertex.*;

public final class VelvetRoomAudit {
 private static int checks;
 private static void check(boolean b,String message){checks++;if(!b)throw new AssertionError(message);}
 public static void main(String[] args)throws Exception {
  var root=Path.of("src/main/resources");var type=JsonParser.parseString(Files.readString(root.resolve("data/velvet_eclipse/dimension_type/velvet_room.json")));
  var dimension=type.getAsJsonObject();check(dimension.get("height").getAsInt()==256&&dimension.get("min_y").getAsInt()==0&&dimension.get("logical_height").getAsInt()==256,"dimension height and bounds");check(!dimension.get("has_skylight").getAsBoolean()&&dimension.get("fixed_time").getAsInt()==18000,"enclosed blue-night room");var generator=JsonParser.parseString(Files.readString(root.resolve("data/velvet_eclipse/dimension/velvet_room.json"))).getAsJsonObject();check(generator.get("type").getAsString().equals("velvet_eclipse:velvet_room"),"dimension resolves its type");check(generator.getAsJsonObject("generator").getAsJsonObject("settings").getAsJsonArray("layers").get(0).getAsJsonObject().get("block").getAsString().equals("minecraft:air"),"void generator leaves room volume empty");
  var assets=root.resolve("assets/velvet_eclipse");int models=0;
  for(var folder:List.of("block","item"))try(var stream=Files.list(assets.resolve("models/"+folder))){for(var file:stream.filter(p->p.getFileName().toString().startsWith("velvet_")).toList()){
   var model=JsonParser.parseString(Files.readString(file)).getAsJsonObject();check(model.has("parent")||model.has("elements"),"model geometry or parent");if(model.has("elements"))for(var element:model.getAsJsonArray("elements")){var e=element.getAsJsonObject();for(int i=0;i<3;i++)check(e.getAsJsonArray("from").get(i).getAsDouble()>=-16&&e.getAsJsonArray("to").get(i).getAsDouble()<=32&&e.getAsJsonArray("from").get(i).getAsDouble()<=e.getAsJsonArray("to").get(i).getAsDouble(),"legal cuboid bounds");check(e.getAsJsonObject("faces").size()==6,"six textured faces");}models++;
  }}check(models>=20,"all props have models");
  var cells=VelvetRoomLayout.cells();var map=new HashMap<String,String>();for(var c:cells){check(c.y()>=0&&c.y()<256,"plan fits dimension height");map.put(c.x()+","+c.y()+","+c.z(),c.material());}
  for(int x=-8;x<=8;x++)for(int z=-9;z<=11;z++){check(map.containsKey(x+",63,"+z),"complete floor");check(map.containsKey(x+",73,"+z),"complete ceiling");}
  for(int[] seat:VelvetRoomLayout.seats()){check(map.containsKey(seat[0]+",63,"+seat[2]),"seat has floor");check(!map.containsKey(seat[0]+",65,"+seat[2]),"seated head has clearance");}
  var bs=JsonParser.parseString(Files.readString(assets.resolve("blockstates/velvet_door.json"))).getAsJsonObject().getAsJsonObject("variants");check(bs.size()==32,"all half/facing/hinge/open combinations");
  for(var entry:bs.entrySet()){String name=entry.getValue().getAsJsonObject().get("model").getAsString().split(":")[1];check(Files.exists(assets.resolve("models/"+name+".json")),"door variant points to real model");}
  // A standing 0.6-wide, 1.8-tall player fits the open visual frame and collider.
  for(var facing:List.of(Direction.NORTH,Direction.SOUTH,Direction.WEST,Direction.EAST))for(boolean left:new boolean[]{false,true}){
   boolean northSouth=facing==Direction.NORTH||facing==Direction.SOUTH;
   var crossing=northSouth?new AABB(.2,0,-.3,.8,1.8,1.3):new AABB(-.3,0,.2,1.3,1.8,.8);
   check(intersects(crossing,VelvetDoorGeometry.bounds(facing,false,left)),"closed door stops traversal");
   check(!intersects(crossing,VelvetDoorGeometry.bounds(facing,true,left)),"both open hinges leave player clearance");
  }
  for(var entry:bs.entrySet()){
   var variant=entry.getValue().getAsJsonObject();String name=variant.get("model").getAsString().split(":")[1];var model=JsonParser.parseString(Files.readString(assets.resolve("models/"+name+".json"))).getAsJsonObject();boolean upper=entry.getKey().contains("half=upper");
   // Visual posts and lintel also have to clear the body, even though only the leaf collides.
   if(entry.getKey().contains("open=true"))for(var cube:model.getAsJsonArray("elements")){
    var e=cube.getAsJsonObject();var a=e.getAsJsonArray("from");var b=e.getAsJsonArray("to");
    var box=new AABB(a.get(0).getAsDouble()/16,(upper?1:0)+a.get(1).getAsDouble()/16,a.get(2).getAsDouble()/16,b.get(0).getAsDouble()/16,(upper?1:0)+b.get(1).getAsDouble()/16,b.get(2).getAsDouble()/16);
    check(!intersects(box,new AABB(.2,0,-.3,.8,1.8,1.3)),"open mesh does not clip head or shoulders");
   }
  }
  Files.createDirectories(Path.of("build/reports/room"));Files.writeString(Path.of("build/reports/room/layout.json"),new Gson().toJson(cells));
  for(boolean slim:new boolean[]{false,true})for(int tick:new int[]{24,50,104,140}){
   var mesh=new Mesh();var rig=AllOutPlayerRenderer.body(new PoseStack(),mesh,slim,Set.of(),tick,true);check(mesh.quads.size()>30,"native player mesh emitted");
   for(var quad:mesh.quads)for(var v:quad)check(Double.isFinite(v[0]+v[1]+v[2])&&v[3]>=0&&v[3]<=1&&v[4]>=0&&v[4]<=1,"finite positions and valid skin UV");
   if(tick==140){var head=new Mesh();var frame=new PoseStack();frame.m_252781_(new org.joml.Quaternionf().rotationZ(-.05f).rotateY(-.20f));frame.m_85837_(0,-.55,0);rig.m_171324_("head").m_104301_(frame,head,15728880,655360);
    for(int[] size:new int[][]{{640,480},{1280,720},{3440,1440}})for(var quad:head.quads)for(var v:quad){double x=size[0]*.68+v[0]*size[1]*.58,y=size[1]*.70+v[1]*size[1]*.58;check(x>=0&&x<=size[0]&&y>=size[1]*.02&&y<=size[1],"victory head stays inside the viewport");}
   }
   Files.writeString(Path.of("build/reports/room/pose-"+tick+(slim?"-slim":"")+".json"),new Gson().toJson(mesh.quads));
  }
  System.out.println("PASS velvet-room: "+checks+" checks; dimension data, "+models+" model resource checks, sealed room, seat clearance, 32 door variants, open/closed collision, visual passage clearance and standard/slim All-Out framing.");
 }
 private static boolean intersects(AABB a,AABB b){return a.f_82291_>b.f_82288_&&a.f_82288_<b.f_82291_&&a.f_82292_>b.f_82289_&&a.f_82289_<b.f_82292_&&a.f_82293_>b.f_82290_&&a.f_82290_<b.f_82293_;}
 private static final class Mesh implements VertexConsumer {
  List<List<double[]>> quads=new ArrayList<>();List<double[]> vertices=new ArrayList<>();double[] point;
  public VertexConsumer m_5483_(double x,double y,double z){point=new double[]{x,y,z,0,0};return this;}
  public VertexConsumer m_6122_(int r,int g,int b,int a){return this;}public VertexConsumer m_7421_(float u,float v){point[3]=u;point[4]=v;return this;}
  public VertexConsumer m_7122_(int a,int b){return this;}public VertexConsumer m_7120_(int a,int b){return this;}public VertexConsumer m_5601_(float a,float b,float c){return this;}
  public void m_5752_(){vertices.add(point);if(vertices.size()==4){quads.add(vertices);vertices=new ArrayList<>();}}
  public void m_7404_(int a,int b,int c,int d){}public void m_141991_(){}
 }
}
