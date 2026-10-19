package com.gx.velveteclipse.tests;

import com.gx.velveteclipse.city.*;
import com.gx.velveteclipse.localization.DisplayText;
import com.gx.velveteclipse.room.CreativeCategory;
import com.google.gson.*;
import java.nio.file.*;
import java.util.*;
import javax.imageio.ImageIO;
import net.minecraft.world.phys.AABB;
import software.bernie.geckolib.util.JsonUtil;
import software.bernie.geckolib.loading.json.raw.Model;
import software.bernie.geckolib.loading.object.*;

/** Focused offline audit: access policy, original assets, actual collision and animated bounds. */
public final class CityShopAccessAudit {
    private static int checks;
    private static final Gson JSON=new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String,int[]> CELLS=new HashMap<>();
    private static void check(boolean value,String reason){checks++;if(!value)throw new AssertionError(reason);}
    private static String key(int x,int y,int z){return x+","+y+","+z;}
    private static boolean clear(AABB box){
        for(int x=(int)Math.floor(box.f_82288_);x<=Math.floor(box.f_82291_);x++)
        for(int y=(int)Math.floor(box.f_82289_);y<=Math.floor(box.f_82292_);y++)
        for(int z=(int)Math.floor(box.f_82290_);z<=Math.floor(box.f_82293_);z++){
            var cell=CELLS.get(key(x,y,z));if(cell==null)continue;
            if(cell[3]==40){for(var b:CityArchitecture.tile(cell[4]).boxes())if(box.m_82381_(new AABB(x+b[0],y+b[1],z+b[2],x+b[3],y+b[4],z+b[5])))return false;}
            else if(box.m_82381_(new AABB(x,y,z,x+1,y+1,z+1)))return false;
        }return true;
    }
    private static JsonObject read(Path p)throws Exception{return JsonParser.parseString(Files.readString(p)).getAsJsonObject();}
    public static void main(String[] args)throws Exception {
        var points=CityShopAccess.all();var plan=CityPlan.get();for(var cell:plan.cells())CELLS.put(key(cell[0],cell[1],cell[2]),cell);
        check(points.size()==9,"all nine shop destinations covered");
        check(points.stream().filter(CityShopAccess.Point::npc).map(CityShopAccess.Point::shop).collect(java.util.stream.Collectors.toSet()).equals(Set.of("police","aohige","mayoido")),"only accepted NPCs; Be Blue V skipped");
        check(points.stream().map(CityShopAccess.Point::uuid).distinct().count()==9,"one persistent UUID per shop, independent of visiting player");
        check(!CityShopAccess.hasNpc("blue")&&!CityShopAccess.hasNpc("missing"),"no fictitious attendant fallback");
        check(CityShopAccess.permits(true,true,true,true,true,true,36),"nearby city visit permitted");
        for(int invalid=0;invalid<6;invalid++){
            boolean[] flags={true,true,true,true,true,true};flags[invalid]=false;
            check(!CityShopAccess.permits(flags[0],flags[1],flags[2],flags[3],flags[4],flags[5],1),"reject unavailable / remote / display / removed / wrong actor: "+invalid);
        }
        for(double distance:new double[]{36.01,10000,Double.NaN,Double.POSITIVE_INFINITY})check(!CityShopAccess.permits(true,true,true,true,true,true,distance),"distance enforced server-side");
        var assets=Path.of("src/main/resources/assets/velvet_eclipse");var reports=new ArrayList<Object>();
        var output=Path.of("build/reports/shop-access");Files.createDirectories(output);
        for(var point:points){
            check(CityShopAccess.get(point.shop()).uuid().equals(point.uuid()),"deterministic UUID on repeated lookup");
            var anchor=point.anchor();check(plan.doors().stream().anyMatch(d->d.x()==anchor[0]&&d.y()==anchor[1]&&d.z()==anchor[2]&&d.target().equals("shop:"+point.shop())),"actor bound to corresponding real counter");
            check(CELLS.get(key(anchor[0],anchor[1],anchor[2]))[3]==40,"terminal still exists in current city");
            check(clear(new AABB(point.x()-point.width()/2,point.y()+.001,point.z()-point.width()/2,point.x()+point.width()/2,point.y()+point.height(),point.z()+point.width()/2)),"actor hitbox outside architecture: "+point.shop());
            if(point.npc())check(!clear(new AABB(point.x()-.1,point.y()-.03,point.z()-.1,point.x()+.1,point.y(),point.z()+.1)),"NPC supported by actual floor");
            var raw=Files.readString(assets.resolve("geo/entity/"+point.asset()+".geo.json"));var geo=JsonUtil.GEO_GSON.fromJson(raw,Model.class);
            var original=JsonParser.parseString(raw).getAsJsonObject().getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();
            var clips=read(assets.resolve("animations/entity/"+point.asset()+".animation.json")).getAsJsonObject("animations");
            var animations=JsonUtil.GEO_GSON.fromJson(clips,BakedAnimations.class);check(animations.getAnimation("misc.idle")!=null,"idle baked by native GeckoLib");
            int cuboids=0;for(var bone:original.getAsJsonArray("bones"))if(bone.getAsJsonObject().has("cubes"))for(var c:bone.getAsJsonObject().getAsJsonArray("cubes")){
                cuboids++;var cube=c.getAsJsonObject();check(cube.getAsJsonArray("uv").size()==2,"native Box UV retained");for(var n:cube.getAsJsonArray("size"))check(n.getAsDouble()>0&&n.getAsDouble()==n.getAsInt(),"integer cuboid size");
            }
            var texture=ImageIO.read(assets.resolve("textures/entity/"+point.asset()+".png").toFile());
            check(texture.getWidth()==original.getAsJsonObject("description").get("texture_width").getAsInt(),"texture width matches native UV");
            if(point.npc()){
                String name=point.asset().substring(5);var source=Path.of("tools/approved_models/shop_npcs/"+name+"/draft");
                check(Arrays.equals(Files.readAllBytes(source.resolve(name+".png")),Files.readAllBytes(assets.resolve("textures/entity/"+point.asset()+".png"))),"approved atlas unchanged");
                var authored=read(source.resolve(name+".geo.json")).getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject().getAsJsonArray("bones");var runtime=original.getAsJsonArray("bones");
                check(authored.size()==runtime.size(),"approved hierarchy retained");
                for(int i=0;i<authored.size();i++){
                    var a=authored.get(i).getAsJsonObject();var b=runtime.get(i).getAsJsonObject();check(a.get("name").equals(b.get("name")),"bone preserved");
                    check(a.getAsJsonArray("pivot").get(0).getAsDouble()==-b.getAsJsonArray("pivot").get(0).getAsDouble(),"single runtime axis conversion");
                    if(a.has("cubes"))for(int j=0;j<a.getAsJsonArray("cubes").size();j++){var ac=a.getAsJsonArray("cubes").get(j).getAsJsonObject();var bc=b.getAsJsonArray("cubes").get(j).getAsJsonObject();check(ac.get("size").equals(bc.get("size"))&&ac.get("uv").equals(bc.get("uv")),"authoring dimensions and atlas retained");}
                }
                String item="city_"+point.shop()+"_model";check(CreativeCategory.of(item,false)==CreativeCategory.NPCS,"NPC item appears in creative NPC category");
                check(Files.exists(assets.resolve("models/item/"+item+".json")),"placement item has a model");
                for(String language:List.of("en_us","pt_br"))check(read(assets.resolve("lang/"+language+".json")).has("item.velvet_eclipse."+item),"localized placement item");
            }
            double[] bounds={1e9,1e9,1e9,-1e9,-1e9,-1e9};double duration=clips.getAsJsonObject("misc.idle").get("animation_length").getAsDouble();
            for(int frame=0;frame<=12;frame++){
                double tick=duration*20*frame/12.;var posed=BakedModelFactory.DEFAULT_FACTORY.constructGeoModel(GeometryTree.fromModel(geo));
                for(var track:animations.getAnimation("misc.idle").boneAnimations()){
                    var bone=posed.getBone(track.boneName()).orElseThrow();var r=track.rotationKeyFrames();var p=track.positionKeyFrames();
                    bone.setRotX(bone.getRotX()+IzanagiAudit.sample(r.xKeyframes(),tick));bone.setRotY(bone.getRotY()+IzanagiAudit.sample(r.yKeyframes(),tick));bone.setRotZ(bone.getRotZ()+IzanagiAudit.sample(r.zKeyframes(),tick));
                    bone.setPosX(IzanagiAudit.sample(p.xKeyframes(),tick));bone.setPosY(IzanagiAudit.sample(p.yKeyframes(),tick));bone.setPosZ(IzanagiAudit.sample(p.zKeyframes(),tick));
                }
                var faces=new ArrayList<Object>();for(var b:posed.topLevelBones())IzanagiAudit.dump(b,new com.mojang.blaze3d.vertex.PoseStack(),faces);
                var data=JSON.toJsonTree(faces).getAsJsonArray();double angle=Math.toRadians(180-point.yaw());
                for(var face:data){
                    double[] faceBox={1e9,1e9,1e9,-1e9,-1e9,-1e9};
                    for(var entry:face.getAsJsonObject().getAsJsonArray("vertices")){
                        var v=entry.getAsJsonArray();double x=v.get(0).getAsDouble()*point.scale()/16,y=v.get(1).getAsDouble()*point.scale()/16,z=v.get(2).getAsDouble()*point.scale()/16;
                        double[] xyz={x,y,z};for(int a=0;a<3;a++){check(Double.isFinite(xyz[a]),"finite animated coordinate");bounds[a]=Math.min(bounds[a],xyz[a]);bounds[a+3]=Math.max(bounds[a+3],xyz[a]);}
                        double[] world={point.x()+x*Math.cos(angle)+z*Math.sin(angle),point.y()+y,point.z()-x*Math.sin(angle)+z*Math.cos(angle)};
                        for(int a=0;a<3;a++){faceBox[a]=Math.min(faceBox[a],world[a]);faceBox[a+3]=Math.max(faceBox[a+3],world[a]);}
                    }
                    // A small margin ignores contact at the floor, but still catches wall/furniture penetration.
                    if(faceBox[4]>point.y()+.015)check(clear(new AABB(faceBox[0]+.00001,faceBox[1]+.00001,faceBox[2]+.00001,faceBox[3]-.00001,faceBox[4]-.00001,faceBox[5]-.00001)),"animated mesh clear of city: "+point.shop()+" / "+frame+" / "+face.getAsJsonObject().get("bone"));
                }
                if(frame==0)Files.writeString(output.resolve(point.shop()+"-native.json"),JSON.toJson(data));
            }
            check(bounds[1]>=-.015,"soles do not sink into floor: "+point.shop());
            check(bounds[4]<=point.height()+.05,"click box covers animated model height: "+point.shop());
            check(Math.max(Math.abs(bounds[0]),Math.abs(bounds[3]))<=point.width()/2+.1,"model fits interaction width");
            check(Math.max(Math.abs(bounds[2]),Math.abs(bounds[5]))<=point.width()/2+.01,"interaction reaches hands, feet and chair depth");
            reports.add(Map.of("shop",point.shop(),"npc",point.npc(),"cuboids",cuboids,"worldBoundsRelative",bounds,"poses",13));
        }
        DisplayText.language("en_us");check(DisplayText.text("Clique direito: acessar loja").equals("Right-click: open shop"),"English default prompt");
        DisplayText.language("pt_br");check(DisplayText.text("Clique direito: acessar loja").equals("Clique direito: acessar loja"),"Portuguese prompt");DisplayText.language("en_us");
        Files.writeString(output.resolve("validation.json"),JSON.toJson(Map.of("status","passed","checks",checks,"shops",reports,"inGameTest",false,"jarGenerated",false)));
        System.out.println("PASS shop access: "+checks+" checks; 3 NPCs / 6 markers; native models, collision, access rules and localization.");
    }
}
