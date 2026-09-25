package com.gx.velveteclipse.tests;

import com.gx.velveteclipse.awakening.AwakeningTimeline;
import com.gx.velveteclipse.forge.PlayerPersonaData;
import com.gx.velveteclipse.menu.MenuActions;
import com.google.gson.JsonParser;
import net.minecraft.nbt.CompoundTag;
import java.nio.file.*;
import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;
import org.joml.Matrix4f;
import org.joml.Vector4f;

/** Focused tests for first-run data, malicious/out-of-order input and exported 3D motion. */
public final class AwakeningAudit {
    static int checks;
    static void check(boolean b,String message){checks++;if(!b)throw new AssertionError(message);}
    public static final class Player {final CompoundTag data=new CompoundTag();public CompoundTag getPersistentData(){return data;}}
    public static void main(String[] args)throws Exception{
        var p=new Player();var data=PlayerPersonaData.load(p);
        check(!data.hasPersona()&&data.activeInstance()==null&&data.activePersona().isEmpty(),"new player really has no Persona");
        check(data.compendiumInstances().isEmpty(),"no free entry in compendium");
        data.save(p);data=PlayerPersonaData.load(p);check(!data.hasPersona()&&!data.awakened(),"save/load preserves unawakened state");
        check(!MenuActions.apply(data,"heal","dia",false).changed(),"empty roster menu safely rejects healing skill");
        var clone=new Player();PlayerPersonaData.copyPersisted(p,clone);check(!PlayerPersonaData.load(clone).hasPersona(),"death does not grant a default Persona");
        for(String id:AwakeningTimeline.STARTERS){
            var d=new PlayerPersonaData();d.addYen(1570);d.setResources(90,21);
            check(!d.completeAwakening("thanatos")&&!d.completeAwakening(""),"unoffered IDs rejected");
            check(d.completeAwakening(id)&&d.activePersona().equals(id)&&d.rosterIds().equals(Set.of(id)),"exact chosen starter equipped: "+id);
            check(d.registeredPersona(id)!=null&&d.personaCapacity()==2,"compendium registration and capacity");
            check(d.hp()==90&&d.sp()==21&&d.yen()==1570,"awakening preserves resources");
            check(!d.completeAwakening(id)&&!d.completeAwakening("izanagi"),"reward replay does not grant again");
            d.save(p);d=PlayerPersonaData.load(p);check(d.awakened()&&d.rosterIds().equals(Set.of(id)),"completed state persists");
            PlayerPersonaData.copyPersisted(p,clone);check(PlayerPersonaData.load(clone).awakened(),"completion survives clone");
            var session=new AwakeningTimeline.Session(1000);
            check(!session.choose(id,16999)&&!session.ready(1_000_000),"cannot choose or finish before intro");
            check(!session.choose("untrusted",17000)&&session.choose(id,17000),"choice whitelist");
            check(!session.choose("orpheus",18000)&&session.persona().equals(id),"choice is immutable");
            long end=17000+(long)Math.ceil((AwakeningTimeline.duration(id)-23)*1000);
            check(!session.finish(end-1)&&session.finish(end)&&!session.finish(end+1),"full playback required; finish exactly once");
            check(Math.abs(AwakeningTimeline.visualTime(33.2,id)-30)<1e-8,"contract starts after original Persona voice");
        }
        p.data.m_128359_("velvet_eclipse:data_v1","V1|90|120|22|45|cinder_wisp|3|cinder_wisp,24,41,agi+zio;izanagi,9,4,zio|28|0|7600");
        data=PlayerPersonaData.load(p);check(data.awakened()&&data.rosterIds().size()==2&&data.activePersona().equals("cinder_wisp"),"legacy ownership preserved");
        check(data.activeInstance().level()==24&&data.yen()==7600&&data.playerLevel()==28,"legacy stats preserved");
        check(!data.completeAwakening("arsene"),"old player cannot claim an extra starter");
        var command=new PlayerPersonaData();command.givePersona("izanagi");check(command.activePersona().equals("izanagi")&&!command.completeAwakening("orpheus"),"command-created ownership remains valid");
        var root=Path.of("src/main/resources");
        var achievement=JsonParser.parseString(Files.readString(root.resolve("data/velvet_eclipse/advancements/awakening.json"))).getAsJsonObject();
        check(achievement.getAsJsonObject("display").get("title").getAsString().equals("I am thou Thou art I"),"exact advancement name");
        check(achievement.getAsJsonObject("criteria").getAsJsonObject("awakened").get("trigger").getAsString().equals("minecraft:impossible"),"only server award, not login/inventory trigger");
        var recipe=JsonParser.parseString(Files.readString(root.resolve("data/velvet_eclipse/recipes/evoker.json"))).getAsJsonObject();check(recipe.getAsJsonObject("result").get("item").getAsString().equals("velvet_eclipse:model_evoker"),"crafts existing Evoker");
        var assets=root.resolve("assets/velvet_eclipse");
        for(String name:List.of("intro","orpheus","izanagi","arsene"))scene(assets.resolve("awakening/"+name+".bin.gz"),name);
        var sounds=JsonParser.parseString(Files.readString(assets.resolve("sounds.json"))).getAsJsonObject();
        for(String id:List.of("intro","orpheus_choice","orpheus_continuation","izanagi_choice","izanagi_continuation","arsene_choice","arsene_continuation")){
            check(sounds.has("awakening."+id),"audio declared: "+id);byte[] bytes=Files.readAllBytes(assets.resolve("sounds/awakening/"+id+".ogg"));check(bytes.length>1000&&new String(bytes,0,4).equals("OggS"),"native streaming OGG: "+id);
        }
        System.out.println("PASS awakening: "+checks+" checks; fresh/legacy/clone persistence, exact rewards, timing, geometry, camera projection, audio and recipe. Offline only.");
    }
    static void scene(Path file,String name)throws Exception{
        try(var in=new DataInputStream(new GZIPInputStream(Files.newInputStream(file)))){
            check(in.readInt()==0x56454157&&in.readInt()==1,"scene format");int meshes=in.readInt();boolean classic=false,slim=false,player=false;
            for(int i=0;i<meshes;i++){String texture=new String(in.readNBytes(in.readInt()));int variant=in.readInt(),vertices=in.readInt();classic|=variant==1;slim|=variant==2;player|=texture.equals("player");check(vertices%4==0,"complete faces");for(int j=0;j<vertices*5;j++)check(Float.isFinite(in.readFloat()),"finite geometry/UV");in.readInt();}
            check(player&&classic&&slim,"both live skin models included, no baked avatar");float start=in.readFloat(),fps=in.readFloat();int count=in.readInt();check(start==(name.equals("intro")?0:23)&&fps==20&&count>300,"shot timing");
            for(int frame=0;frame<count;frame++){
                float[] camera=new float[14];for(int j=0;j<14;j++)camera[j]=in.readFloat();
                // Compare our actual GPU view/projection with the approved Cartesian projection.
                var eye=new org.joml.Vector3f(camera[0],camera[1],camera[2]);var front=new org.joml.Vector3f(camera[9],camera[10],camera[11]);var up=new org.joml.Vector3f(camera[6],camera[7],camera[8]);
                var m=new Matrix4f().perspective(2*(float)Math.atan(270/camera[12]),960f/540,.08f,100f).rotateZ(-camera[13]).lookAt(eye,new org.joml.Vector3f(eye).add(front),up);
                var point=new Vector4f(-1.7f,1.6f,0,1);m.transform(point);double dx=-1.7-camera[0],dy=1.6-camera[1],dz=-camera[2],depth=dx*camera[9]+dy*camera[10]+dz*camera[11];
                double x=(dx*camera[3]+dy*camera[4]+dz*camera[5])*camera[12]/depth,y=-(dx*camera[6]+dy*camera[7]+dz*camera[8])*camera[12]/depth,px=480+Math.cos(camera[13])*x-Math.sin(camera[13])*y,py=270+Math.sin(camera[13])*x+Math.cos(camera[13])*y;
                if(depth>.1)check(Math.abs(px-(point.x/point.w+1)*480)<.03&&Math.abs(py-(1-point.y/point.w)*270)<.03,"GPU camera matches reference");
                for(int i=0;i<meshes;i++)for(int j=0;j<16;j++)check(Float.isFinite(in.readFloat()),"finite pose");
            }
            check(in.read()==-1,"complete scene, no truncation");
        }
    }
}
