package com.gx.velveteclipse.tests;
import java.util.*;
import java.nio.file.*;
import com.google.gson.*;
import software.bernie.geckolib.util.JsonUtil;
import software.bernie.geckolib.loading.json.raw.Model;
import software.bernie.geckolib.loading.object.*;
import com.mojang.blaze3d.vertex.PoseStack;

/** Load the delivered resources through GeckoLib, rather than validating only the authoring preview. */
public final class ApprovedModelsAudit {
 static int checks;static void check(boolean b,String s){checks++;if(!b)throw new AssertionError(s);}
 public static void main(String[] args)throws Exception{
  var root=Path.of("src/main/resources/assets/velvet_eclipse");var output=Path.of("build/reports/model-combat");Files.createDirectories(output);
  var dimensions=new LinkedHashMap<String,Object>();
  for(String id:List.of("pixie","elizabeth")){
   var source=Path.of("tools/approved_models/"+id+"/draft");double scale=id.equals("pixie")?.085:.11;
   var authored=JsonParser.parseString(Files.readString(source.resolve(id+".geo.json"))).getAsJsonObject().getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();
   String raw=Files.readString(root.resolve("geo/entity/"+id+".geo.json"));var geo=JsonUtil.GEO_GSON.fromJson(raw,Model.class);var tree=GeometryTree.fromModel(geo);var baked=BakedModelFactory.DEFAULT_FACTORY.constructGeoModel(tree);
   var converted=JsonParser.parseString(raw).getAsJsonObject().getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject().getAsJsonArray("bones");
   int bones=0,cubes=0;
   for(var entry:authored.getAsJsonArray("bones")){
    var bone=entry.getAsJsonObject();var nativeBone=baked.getBone(bone.get("name").getAsString()).orElseThrow();
    check(Math.abs(nativeBone.getPivotX()-bone.getAsJsonArray("pivot").get(0).getAsDouble())<.001,"single coordinate conversion: "+id+" "+nativeBone.getName());
    if(bone.has("rotation")){var r=bone.getAsJsonArray("rotation");check(Math.abs(nativeBone.getRotX()-Math.toRadians(r.get(0).getAsDouble()))<.001&&Math.abs(nativeBone.getRotY()-Math.toRadians(r.get(1).getAsDouble()))<.001&&Math.abs(nativeBone.getRotZ()-Math.toRadians(r.get(2).getAsDouble()))<.001,"native rest orientation");}
    if(bone.has("cubes")){var approved=bone.getAsJsonArray("cubes");var runtime=converted.get(bones).getAsJsonObject().getAsJsonArray("cubes");cubes+=approved.size();check(approved.size()==runtime.size(),"all approved facial/hair cuboids retained");
     for(int i=0;i<approved.size();i++){var a=approved.get(i).getAsJsonObject();var b=runtime.get(i).getAsJsonObject();check(a.get("size").equals(b.get("size"))&&a.get("uv").equals(b.get("uv")),"native dimensions and Box UV unchanged");}
    }bones++;
   }
   check(cubes==(id.equals("pixie")?210:289)&&bones==(id.equals("pixie")?40:45),"exact approved revision "+id);
   check(Arrays.equals(Files.readAllBytes(source.resolve(id+".png")),Files.readAllBytes(root.resolve("textures/entity/"+id+".png"))),"approved texture unchanged "+id);
   var clips=JsonParser.parseString(Files.readString(root.resolve("animations/entity/"+id+".animation.json"))).getAsJsonObject().getAsJsonObject("animations");var animations=JsonUtil.GEO_GSON.fromJson(clips,BakedAnimations.class);
   double[] bounds={Double.MAX_VALUE,Double.MAX_VALUE,Double.MAX_VALUE,-Double.MAX_VALUE,-Double.MAX_VALUE,-Double.MAX_VALUE};
   for(String clip:id.equals("pixie")?List.of("misc.idle","action.magic","action.physical"):List.of("misc.idle")){
    check(animations.getAnimation(clip)!=null,"controller clip exists "+id+" "+clip);var definition=clips.getAsJsonObject(clip);double length=definition.get("animation_length").getAsDouble();
    check(length==(clip.equals("misc.idle")?(id.equals("pixie")?4:4.8):2.5),"idle duration / combat clip finishes before hiding");
    for(double tick:new double[]{0,10,20,28,40,length*20}){
     var posed=BakedModelFactory.DEFAULT_FACTORY.constructGeoModel(tree);
     for(var track:animations.getAnimation(clip).boneAnimations()){
      var bone=posed.getBone(track.boneName()).orElseThrow();var r=track.rotationKeyFrames();var p=track.positionKeyFrames();
      bone.setRotX(bone.getRotX()+IzanagiAudit.sample(r.xKeyframes(),tick));bone.setRotY(bone.getRotY()+IzanagiAudit.sample(r.yKeyframes(),tick));bone.setRotZ(bone.getRotZ()+IzanagiAudit.sample(r.zKeyframes(),tick));
      bone.setPosX(IzanagiAudit.sample(p.xKeyframes(),tick));bone.setPosY(IzanagiAudit.sample(p.yKeyframes(),tick));bone.setPosZ(IzanagiAudit.sample(p.zKeyframes(),tick));
     }
     var faces=new ArrayList<Object>();for(var bone:posed.topLevelBones())IzanagiAudit.dump(bone,new PoseStack(),faces);var serialized=new Gson().toJsonTree(faces).getAsJsonArray();
     for(var face:serialized)for(var vertex:face.getAsJsonObject().getAsJsonArray("vertices")){var v=vertex.getAsJsonArray();for(int a=0;a<3;a++){double n=v.get(a).getAsDouble()*scale/16;check(Double.isFinite(n),"finite animated world-space vertex");bounds[a]=Math.min(bounds[a],n);bounds[a+3]=Math.max(bounds[a+3],n);}v.set(3,new JsonPrimitive(v.get(3).getAsDouble()*2));v.set(4,new JsonPrimitive(v.get(4).getAsDouble()*2));}
     if(tick==28)Files.writeString(output.resolve(id+"-"+clip+".json"),new Gson().toJson(serialized));
    }
   }
   check(bounds[4]<(id.equals("pixie")?2.5:2.8)&&bounds[4]>(id.equals("pixie")?1.1:2),"world height stays appropriate "+id+" "+Arrays.toString(bounds));dimensions.put(id,Map.of("bounds",bounds,"scale",scale,"bones",bones,"cubes",cubes));
  }
  Files.writeString(output.resolve("native-model-bounds.json"),new Gson().toJson(dimensions));
  System.out.println("PASS approved models: "+checks+" checks; Pixie03 / Elizabeth05, native GeckoLib geometry, textures, transforms and action clips.");
 }
}
