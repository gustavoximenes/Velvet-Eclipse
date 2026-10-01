package com.gx.velveteclipse.tests;

import com.gx.velveteclipse.room.*;
import com.gx.velveteclipse.client.room.FusionCardTextures;
import com.gx.velveteclipse.core.persona.PersonaRegistry;
import com.gx.velveteclipse.core.fusion.PartyFusion;
import java.util.*;
import java.nio.file.*;
import com.google.gson.*;
import javax.imageio.ImageIO;

/** Regression scenarios for v3 migration, delayed entity loading and real ingredient fronts. */
public final class VelvetRoomFixAudit {
 private static int checks;
 private static void check(boolean pass,String message){checks++;if(!pass)throw new AssertionError(message);}
 private static String key(VelvetRoomLayout.Cell c){return c.x()+","+c.y()+","+c.z();}
 public static void main(String[] args)throws Exception{
  for(int capacity:new int[]{1,4,8,64}){
   var physical=new HashMap<String,String>();for(var cell:VelvetRoomLayout.cells(capacity))physical.put(key(cell),cell.material());
   var modern=new HashMap<>(physical);
   for(var cell:VelvetRoomLayout.retiredDecoration())physical.put(key(cell),cell.material());
   // Regression: occupied chairs/door and unrelated blocks must survive migration.
   for(var seat:VelvetRoomLayout.seats(capacity))physical.put(seat[0]+","+seat[1]+","+seat[2],"occupied chair");
   String door="0,64,"+(VelvetRoomLayout.depth(capacity)-1);physical.put(door,"door");physical.put("5,64,5","player block");
   for(var cell:VelvetRoomLayout.retiredDecoration()){check(!modern.containsKey(key(cell)),"retired decor is not rebuilt");physical.remove(key(cell),cell.material());}
   for(var cell:VelvetRoomLayout.cells(capacity))physical.put(key(cell),cell.material());
   check(physical.get(door).equals("door")&&physical.get("5,64,5").equals("player block"),"exit and unrelated blocks preserved");
   for(var seat:VelvetRoomLayout.seats(capacity))check(physical.get(seat[0]+","+seat[1]+","+seat[2]).equals("occupied chair"),"occupied seats survive");
   for(var cell:VelvetRoomLayout.retiredDecoration())check(!physical.containsKey(key(cell)),"legacy ornament removed");
   check(modern.keySet().stream().noneMatch(k->k.equals("0,64,-2")||k.equals("0,64,-5")),"no old table/couch underneath new meshes");
  }
  var saved=UUID.randomUUID();var legacy=UUID.randomUUID();var residents=new ArrayList<UUID>();int[] spawned={0};
  java.util.function.Supplier<UUID> spawn=()->{spawned[0]++;var id=UUID.randomUUID();residents.add(id);return id;};
  check(VelvetRoomResidents.reconcile(false,List.copyOf(residents),saved.toString(),id->id,spawn,residents::remove)==null&&spawned[0]==0,"unloaded entities must never trigger a duplicate spawn");
  residents.add(legacy);residents.add(saved);
  var keep=VelvetRoomResidents.reconcile(true,List.copyOf(residents),saved.toString(),id->id,spawn,residents::remove);
  check(keep.equals(saved)&&residents.equals(List.of(saved))&&spawned[0]==0,"retain canonical saved resident and discard old overlap");
  for(int tick=0;tick<20;tick++)VelvetRoomResidents.reconcile(true,List.copyOf(residents),saved.toString(),id->id,spawn,residents::remove);
  check(residents.size()==1&&spawned[0]==0,"repeat joins/ticks do not spawn copies");
  residents.clear();var fresh=VelvetRoomResidents.reconcile(true,List.copyOf(residents),saved.toString(),id->id,spawn,residents::remove);
  check(fresh!=null&&residents.equals(List.of(fresh))&&spawned[0]==1,"missing resident restored once when entities are ready");
  PersonaRegistry.bootstrap();var assets=Path.of("src/main/resources/assets/velvet_eclipse");
  for(var p:PersonaRegistry.personas().values()){
   String path=FusionCardTextures.front(p.id());check(!path.equals("velvet_fusion_card"),"registered ingredient has its own front: "+p.id());
   var image=ImageIO.read(assets.resolve("textures/entity/"+path+".png").toFile());check(image.getWidth()==256&&image.getHeight()==384,"card dimensions");
   var colors=new HashSet<Integer>();int modelPixels=0;for(int y=35;y<310;y++)for(int x=30;x<226;x++){int pixel=image.getRGB(x,y);colors.add(pixel);if((pixel&0xFFFFFF)!=0x0A154E)modelPixels++;}check(colors.size()>=5&&modelPixels>1500,"actual Persona pixels on front, including simple legacy rigs: "+p.id());
  }
  var members=List.of(new PartyFusion.Source(UUID.randomUUID(),"pixie"),new PartyFusion.Source(UUID.randomUUID(),"apsaras"),new PartyFusion.Source(UUID.randomUUID(),"jack_frost"));
  check(members.stream().map(s->FusionCardTextures.front(s.persona())).distinct().count()==3,"cooperative ingredients select distinct portraits");
  check(FusionCardTextures.front("../invalid").equals("velvet_fusion_card"),"invalid resource ID falls back safely");
  check(FusionCardTextures.front("not_registered").equals("velvet_fusion_card"),"unknown source uses a real fallback");
  for(String block:List.of("floor","wall","trim","table")){
   var variants=JsonParser.parseString(Files.readString(assets.resolve("blockstates/velvet_"+block+".json"))).getAsJsonObject().getAsJsonObject("variants");
   check(variants.has("scenic=false")&&variants.has("scenic=true"),"old creative and room-built states both resolve");
   for(var value:variants.entrySet()){String model=value.getValue().getAsJsonObject().get("model").getAsString().split(":")[1];check(Files.exists(assets.resolve("models/"+model+".json")),"backing model exists");}
  }
  System.out.println("PASS Velvet Room fixes: "+checks+" checks; legacy decor migration, occupied seats/exit, delayed NPC loading, singleton recovery, all registered Persona portraits and block states. Offline only.");
 }
}
