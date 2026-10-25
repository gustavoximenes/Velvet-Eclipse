package com.gx.velveteclipse.tests;

import com.google.gson.*;
import com.mojang.blaze3d.vertex.PoseStack;
import java.nio.file.*;
import java.util.*;
import software.bernie.geckolib.cache.object.*;
import software.bernie.geckolib.loading.json.raw.Model;
import software.bernie.geckolib.loading.object.*;
import software.bernie.geckolib.util.*;

/** Authoring tool: measure native geometry through the whole animation, without an OpenGL window. */
public final class PersonaFramingExport {
    static final Path ROOT=Path.of("src/main/resources/assets/velvet_eclipse");
    static final Map<String,Double> SCALES=Map.of("apsaras",.0625,"jack_frost",.125,"forneus",.125,"chimera",.125,"pixie",.085,"izanagi",.25,"arsene",.125,"orpheus",.16,"nekomata",.125,"onmoraki",.125);
    public static void main(String[] args)throws Exception {
        var profiles=new LinkedHashMap<String,Object>();
        if(args.length>0){
            var existing=JsonParser.parseString(Files.readString(ROOT.resolve("persona-preview-bounds.json"))).getAsJsonObject();
            existing.entrySet().forEach(e->profiles.put(e.getKey(),e.getValue()));
        }
        com.gx.velveteclipse.core.persona.PersonaRegistry.bootstrap();
        for(String id:com.gx.velveteclipse.core.persona.PersonaRegistry.personas().keySet()) {
            if(args.length>0&&!Arrays.asList(args).contains(id))continue;
            String asset=com.gx.velveteclipse.client.menu.PersonaDetails.asset(id);
            Path file=ROOT.resolve("geo/entity/"+asset+".geo.json");if(!Files.exists(file))continue;
            Model model=JsonUtil.GEO_GSON.fromJson(Files.readString(file),Model.class);
            var raw=JsonParser.parseString(Files.readString(ROOT.resolve("animations/entity/"+asset+".animation.json"))).getAsJsonObject().getAsJsonObject("animations");
            var clips=JsonUtil.GEO_GSON.fromJson(raw,BakedAnimations.class);
            double[] idle=empty(), action=empty();
            double[][] sheet=new double[72][4];var projections=new org.joml.Matrix4f[72];
            for(int i=0;i<72;i++){sheet[i]=new double[]{1e9,1e9,-1e9,-1e9};projections[i]=com.gx.velveteclipse.client.menu.ApprovedPersonaFraming.projection(id,i*5);}
            for(String clip:List.of("misc.idle","action.magic","action.physical")) {
                var animation=clips.getAnimation(clip);if(animation==null)continue;
                double length=raw.getAsJsonObject(clip).has("animation_length")?raw.getAsJsonObject(clip).get("animation_length").getAsDouble()*20:40;
                for(double tick=0;tick<=length+.001;tick+=.5) {
                    var posed=BakedModelFactory.DEFAULT_FACTORY.constructGeoModel(GeometryTree.fromModel(model));
                    for(var track:animation.boneAnimations()) {
                        var bone=posed.getBone(track.boneName()).orElse(null);if(bone==null)continue;
                        var r=track.rotationKeyFrames();var p=track.positionKeyFrames();
                        bone.setRotX(bone.getRotX()+IzanagiAudit.sample(r.xKeyframes(),tick));bone.setRotY(bone.getRotY()+IzanagiAudit.sample(r.yKeyframes(),tick));bone.setRotZ(bone.getRotZ()+IzanagiAudit.sample(r.zKeyframes(),tick));
                        bone.setPosX(IzanagiAudit.sample(p.xKeyframes(),tick));bone.setPosY(IzanagiAudit.sample(p.yKeyframes(),tick));bone.setPosZ(IzanagiAudit.sample(p.zKeyframes(),tick));
                    }
                    for(var bone:posed.topLevelBones()) measure(bone,new PoseStack(),clip.equals("misc.idle")?idle:action,SCALES.getOrDefault(asset,1.),clip.equals("misc.idle")?sheet:null,projections);
                }
            }
            if(idle[0]==1e9)for(var bone:BakedModelFactory.DEFAULT_FACTORY.constructGeoModel(GeometryTree.fromModel(model)).topLevelBones())measure(bone,new PoseStack(),idle,SCALES.getOrDefault(asset,1.),sheet,projections);
            union(action,idle);pad(idle);pad(action);profiles.put(id,Map.of("idle",idle,"action",action,"sheet",sheet));
        }
        Files.writeString(ROOT.resolve("persona-preview-bounds.json"),new GsonBuilder().setPrettyPrinting().create().toJson(profiles));
        System.out.println("Exported native animated menu bounds: "+profiles.size()+" Personas");
    }
    static double[] empty(){return new double[]{1e9,1e9,1e9,-1e9,-1e9,-1e9};}
    static void union(double[] a,double[] b){for(int i=0;i<3;i++){a[i]=Math.min(a[i],b[i]);a[i+3]=Math.max(a[i+3],b[i+3]);}}
    static void pad(double[] b){for(int i=0;i<3;i++){b[i]-=.025;b[i+3]+=.025;}}
    static void measure(GeoBone bone,PoseStack pose,double[] bounds,double scale,double[][] sheet,org.joml.Matrix4f[] projections){
        pose.m_85836_();RenderUtils.prepMatrixForBone(pose,bone);
        for(var cube:bone.getCubes()){
            pose.m_85836_();RenderUtils.translateToPivotPoint(pose,cube);RenderUtils.rotateMatrixAroundCube(pose,cube);RenderUtils.translateAwayFromPivotPoint(pose,cube);
            for(var quad:cube.quads())if(quad!=null)for(var v:quad.vertices()){
                var p=new org.joml.Vector3f(v.position()).mulPosition(pose.m_85850_().m_252922_()).mul((float)scale);
                for(int a=0;a<3;a++){double n=p.get(a);if(!Double.isFinite(n))throw new IllegalStateException("Non-finite pose");bounds[a]=Math.min(bounds[a],n);bounds[a+3]=Math.max(bounds[a+3],n);}
                if(sheet!=null)for(int i=0;i<sheet.length;i++){
                    var q=projections[i].transformPosition(p,new org.joml.Vector3f());var b=sheet[i];
                    b[0]=Math.min(b[0],q.x);b[1]=Math.min(b[1],q.y);b[2]=Math.max(b[2],q.x);b[3]=Math.max(b[3],q.y);
                }
            }
            pose.m_85849_();
        }
        for(var child:bone.getChildBones())measure(child,pose,bounds,scale,sheet,projections);pose.m_85849_();
    }
}
