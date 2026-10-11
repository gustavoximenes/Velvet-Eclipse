package com.gx.velveteclipse.tests;
import com.gx.velveteclipse.city.*;import com.gx.velveteclipse.equipment.*;import com.gx.velveteclipse.forge.PlayerPersonaData;
import com.google.gson.*;import java.nio.file.*;import java.util.*;
import software.bernie.geckolib.util.JsonUtil;import software.bernie.geckolib.loading.json.raw.Model;import software.bernie.geckolib.loading.object.*;
public final class CityIntegrationAudit {
 private static int checks;private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
 public static void main(String[] args)throws Exception{
  check(CityRules.canChoose(0,"owner","owner","owner"),"creator chooses");check(!CityRules.canChoose(0,"owner","guest","owner"),"guest cannot choose");check(!CityRules.canChoose(1,"owner","owner","owner"),"cannot change accepted choice");check(!CityRules.canChoose(-1,"owner","owner","owner"),"cannot change declined choice");check(!CityRules.canChoose(0,"","guest",""),"no first-login takeover");
  var saved=new CityState();saved.initialized=true;saved.choice=1;saved.owner="owner";saved.cursor=19322;saved.rebuilding=true;saved.cleanupCursor=183;saved.revision=1;var tag=saved.m_7176_(new net.minecraft.nbt.CompoundTag());var loader=CityState.class.getDeclaredMethod("load",net.minecraft.nbt.CompoundTag.class);loader.setAccessible(true);var resumed=(CityState)loader.invoke(null,tag);check(resumed.initialized&&resumed.choice==1&&resumed.owner.equals("owner")&&resumed.cursor==19322&&!resumed.ready&&resumed.rebuilding&&resumed.cleanupCursor==183&&resumed.revision==1,"choice and incomplete construction survive restart");
  com.gx.velveteclipse.core.persona.PersonaRegistry.bootstrap();
  for(var item:Supplies.all()){int owners=0;for(String shop:CityCatalog.SHOPS)if(CityRules.canPurchase(shop,item.id(),0))owners++;check(owners==1,"one store per supply "+item.id());}
  for(var entry:ShopStock.entries()){check(CityRules.canPurchase("police",entry.id(),entry.tier()),"permitted equipment");if(entry.tier()>0)check(!CityRules.canPurchase("police",entry.id(),entry.tier()-1),"permit not bypassed");check(!CityRules.canPurchase("aohige",entry.id(),9),"pharmacy cannot sell equipment");}
  var wallet=new PlayerPersonaData();var bag=EquipmentLoadout.starter();long before=wallet.yen();wallet.addYen(1000);var result=EquipmentTransactions.buy(bag,wallet,"medicine");check(result==EquipmentTransactions.Result.BOUGHT&&wallet.yen()==before+700&&bag.count("medicine")>0,"real inventory and wallet transaction");
  var plan=CityPlan.get();check(plan.places().size()>=25&&plan.meshes().isEmpty()&&plan.revision()==3,"packaged city destinations");check(plan.places().stream().map(CityPlan.Place::id).distinct().count()==plan.places().size(),"unique destinations");
  var root=Path.of("src/main/resources/assets/velvet_eclipse");var report=new LinkedHashMap<String,Object>();var out=Path.of("build/reports/city");Files.createDirectories(out);
  var language=JsonParser.parseString(Files.readString(root.resolve("lang/pt_br.json"))).getAsJsonObject();
  var materials=new ArrayList<String>(List.of("paving","brick","plaster","wood","glass","school_floor","leaves","roof","light","tartarus_access"));CityCatalog.SHOPS.forEach(s->materials.add("shop_"+s));
  for(String material:materials){String id="city_"+material;check(language.has("block.velvet_eclipse."+id),"localized creative block "+id);check(Files.exists(root.resolve("models/item/"+id+".json"))&&Files.exists(root.resolve("blockstates/"+id+".json")),"creative model "+id);check(com.gx.velveteclipse.room.CreativeCategory.of(id,true)==com.gx.velveteclipse.room.CreativeCategory.BLOCKS,"creative category");}
  for(String id:List.of("apsaras","jack_frost","forneus","chimera")){
   var authored=JsonParser.parseString(Files.readString(Path.of("tools/approved_models/"+id+"/draft/"+id+".geo.json"))).getAsJsonObject().getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();var geo=JsonUtil.GEO_GSON.fromJson(Files.readString(root.resolve("geo/entity/"+id+".geo.json")),Model.class);var baked=BakedModelFactory.DEFAULT_FACTORY.constructGeoModel(GeometryTree.fromModel(geo));int cubes=0;
   for(var element:authored.getAsJsonArray("bones")){var bone=element.getAsJsonObject();var nativeBone=baked.getBone(bone.get("name").getAsString()).orElseThrow();check(Math.abs(nativeBone.getPivotX()-bone.getAsJsonArray("pivot").get(0).getAsDouble())<.001,"native pivot "+id);if(bone.has("cubes"))cubes+=bone.getAsJsonArray("cubes").size();}
   var clips=JsonParser.parseString(Files.readString(root.resolve("animations/entity/"+id+".animation.json"))).getAsJsonObject().getAsJsonObject("animations");var animations=JsonUtil.GEO_GSON.fromJson(clips,BakedAnimations.class);
   for(String key:List.of("misc.idle","action.magic","action.physical")){check(animations.getAnimation(key)!=null,"native clip "+id+key);if(!key.equals("misc.idle"))check(clips.getAsJsonObject(key).get("animation_length").getAsDouble()==2.5,"combat duration "+id);for(var b:animations.getAnimation(key).boneAnimations())check(baked.getBone(b.boneName()).isPresent(),"animated bone exists");}
   var bounds=new double[]{1e9,1e9,1e9,-1e9,-1e9,-1e9};double scale=id.equals("apsaras")?.0625:.125;
   for(String clip:List.of("misc.idle","action.magic","action.physical"))for(double tick:new double[]{0,10,20,28,40,50}){
    var posed=BakedModelFactory.DEFAULT_FACTORY.constructGeoModel(GeometryTree.fromModel(geo));
    for(var track:animations.getAnimation(clip).boneAnimations()){var bone=posed.getBone(track.boneName()).orElseThrow();var r=track.rotationKeyFrames();var p=track.positionKeyFrames();bone.setRotX(bone.getRotX()+IzanagiAudit.sample(r.xKeyframes(),tick));bone.setRotY(bone.getRotY()+IzanagiAudit.sample(r.yKeyframes(),tick));bone.setRotZ(bone.getRotZ()+IzanagiAudit.sample(r.zKeyframes(),tick));bone.setPosX(IzanagiAudit.sample(p.xKeyframes(),tick));bone.setPosY(IzanagiAudit.sample(p.yKeyframes(),tick));bone.setPosZ(IzanagiAudit.sample(p.zKeyframes(),tick));}
    var faces=new ArrayList<Object>();for(var bone:posed.topLevelBones())IzanagiAudit.dump(bone,new com.mojang.blaze3d.vertex.PoseStack(),faces);var serialized=new Gson().toJsonTree(faces).getAsJsonArray();
    for(var face:serialized)for(var vertex:face.getAsJsonObject().getAsJsonArray("vertices")){var v=vertex.getAsJsonArray();for(int a=0;a<3;a++){double n=v.get(a).getAsDouble()*scale/16;check(Double.isFinite(n),"finite animated coordinate");bounds[a]=Math.min(bounds[a],n);bounds[a+3]=Math.max(bounds[a+3],n);}v.set(3,new JsonPrimitive(v.get(3).getAsDouble()*authored.getAsJsonObject("description").get("texture_width").getAsDouble()/512));v.set(4,new JsonPrimitive(v.get(4).getAsDouble()*authored.getAsJsonObject("description").get("texture_height").getAsDouble()/512));}
    if(tick==28)Files.writeString(out.resolve(id+"-"+clip+".json"),new Gson().toJson(serialized));
   }
   for(double[] rect:new double[][]{{340,199},{460,417}})for(int yaw=0;yaw<360;yaw+=15){
    var fit=com.gx.velveteclipse.client.menu.ApprovedPersonaFraming.fit(id,0,0,rect[0],rect[1],yaw,true);
    var transform=com.gx.velveteclipse.client.menu.ApprovedPersonaFraming.projection(id,yaw);
    for(int i=0;i<8;i++){var point=transform.transformPosition(new org.joml.Vector3f((float)bounds[(i&1)==0?0:3],(float)bounds[(i&2)==0?1:4],(float)bounds[(i&4)==0?2:5]));double px=fit.x()+point.x*fit.scale(),py=fit.y()+point.y*fit.scale();check(px>=0&&px<=rect[0]&&py>=0&&py<=rect[1],"native rotated geometry fits actual card transform "+id);}
   }
   check(Arrays.equals(Files.readAllBytes(root.resolve("textures/entity/"+id+".png")),Files.readAllBytes(Path.of("tools/approved_models/"+id+"/draft/"+id+".png"))),"approved atlas unchanged");report.put(id,Map.of("cuboids",cubes,"animations",3,"bounds",bounds));
  }
  Files.writeString(out.resolve("integration.json"),new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("checks",checks,"models",report,"cityCells",plan.cells().length)));
  System.out.println("PASS city integration: "+checks+" checks; choice, store separation, real transactions and GeckoLib models.");
 }
}
