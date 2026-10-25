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
  for(String id:List.of("pixie","elizabeth","nekomata","onmoraki")){
   var source=Path.of("tools/approved_models/"+id+"/draft");double scale=switch(id){case "pixie"->.085;case "nekomata","onmoraki"->.125;default->.11;};
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
   check(cubes==switch(id){case "pixie"->210;case "nekomata"->266;case "onmoraki"->96;default->289;}&&bones==switch(id){case "pixie"->40;case "nekomata"->47;case "onmoraki"->20;default->45;},"exact approved revision "+id);
   check(Arrays.equals(Files.readAllBytes(source.resolve(id+".png")),Files.readAllBytes(root.resolve("textures/entity/"+id+".png"))),"approved texture unchanged "+id);
   var clips=JsonParser.parseString(Files.readString(root.resolve("animations/entity/"+id+".animation.json"))).getAsJsonObject().getAsJsonObject("animations");var animations=JsonUtil.GEO_GSON.fromJson(clips,BakedAnimations.class);
   double[] bounds={Double.MAX_VALUE,Double.MAX_VALUE,Double.MAX_VALUE,-Double.MAX_VALUE,-Double.MAX_VALUE,-Double.MAX_VALUE};
   for(String clip:id.equals("elizabeth")?List.of("misc.idle"):List.of("misc.idle","action.magic","action.physical")){
    check(animations.getAnimation(clip)!=null,"controller clip exists "+id+" "+clip);var definition=clips.getAsJsonObject(clip);double length=definition.get("animation_length").getAsDouble();
    check(length==(clip.equals("misc.idle")?(switch(id){case "pixie"->4;case "nekomata"->3.2;case "onmoraki"->3.6;default->4.8;}):id.equals("onmoraki")?(clip.equals("action.magic")?2.8:2.4):2.5),"idle duration / combat clip finishes before hiding");
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
   check(bounds[4]<(id.equals("elizabeth")?2.8:2.5)&&bounds[4]>(id.equals("elizabeth")?2:1.1),"world height stays appropriate "+id+" "+Arrays.toString(bounds));dimensions.put(id,Map.of("bounds",bounds,"scale",scale,"bones",bones,"cubes",cubes));
   if(id.equals("nekomata")){
    for(String clip:List.of("action.magic","action.physical")){
     var rootTrack=Arrays.stream(animations.getAnimation(clip).boneAnimations()).filter(b->b.boneName().equals("nekomata")).findFirst().orElseThrow();
     check(Math.abs(IzanagiAudit.sample(rootTrack.rotationKeyFrames().yKeyframes(),50)-2*Math.PI)<.0001,"native full turn survives conversion "+clip);
     var original=animations.getAnimation("misc.idle");var action=animations.getAnimation(clip);
     for(var track:action.boneAnimations()){
      var rest=Arrays.stream(original.boneAnimations()).filter(b->b.boneName().equals(track.boneName())).findFirst().orElseThrow();
      var a=track.rotationKeyFrames();var b=rest.rotationKeyFrames();
      // Euler deltas can use equivalent representations; compare the full baked pose.
      var base=baked.getBone(track.boneName()).orElseThrow();
      var ma=new org.joml.Matrix4f().rotateZYX(base.getRotZ()+IzanagiAudit.sample(a.zKeyframes(),50),base.getRotY()+IzanagiAudit.sample(a.yKeyframes(),50),base.getRotX()+IzanagiAudit.sample(a.xKeyframes(),50));
      var mb=new org.joml.Matrix4f().rotateZYX(base.getRotZ()+IzanagiAudit.sample(b.zKeyframes(),0),base.getRotY()+IzanagiAudit.sample(b.yKeyframes(),0),base.getRotX()+IzanagiAudit.sample(b.xKeyframes(),0));
      check(ma.equals(mb,.0001f),"native attack returns to idle "+track.boneName());
     }
    }
    check(com.gx.velveteclipse.room.CreativeCategory.of("nekomata_model",false)==com.gx.velveteclipse.room.CreativeCategory.NPCS,"existing placement item remains creative");
   }
  }
  Files.writeString(output.resolve("native-model-bounds.json"),new Gson().toJson(dimensions));
  System.out.println("PASS approved models: "+checks+" checks; Pixie03 / Elizabeth05 / Nekomata02 / Onmoraki01, native GeckoLib geometry, textures, transforms and action clips.");
 }
}
