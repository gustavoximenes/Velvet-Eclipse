package com.gx.velveteclipse.tests;

import com.gx.velveteclipse.city.*;
import com.gx.velveteclipse.client.city.CityMapLayout;
import com.google.gson.*;
import java.nio.file.*;
import java.util.*;

/** Architectural invariants: one visible native block per cell, rooms inside their building, resumable migration. */
public final class CityNativeModelAudit {
    private static int checks;
    private static void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
    public static void main(String[] args)throws Exception {
        var plan=CityPlan.get();check(plan.revision()==2&&plan.meshes().isEmpty(),"No old mesh overlays in city revision 2");
        var coordinates=new HashSet<String>();int lights=0,stairs=0,doors=0;
        var root=Path.of("src/main/resources/assets/velvet_eclipse");
        for(int[] c:plan.cells()) {
            check(coordinates.add(c[0]+","+c[1]+","+c[2]),"One block per position");
            check(c.length==5&&c[4]==0,"Visible native architecture, without hidden supports");
            if(c[3]>=100) {
                var entry=plan.palette().get(c[3]-100);check(entry.block().startsWith("minecraft:"),"Known native material namespace");
                if(entry.block().endsWith("_stairs"))stairs++;
                if(entry.block().endsWith("_door"))doors++;
                if(entry.block().contains("froglight"))lights++;
            } else check(c[3]<=8||c[3]>=20&&c[3]<=31,"Known city block");
        }
        check(stairs>100&&doors>100&&lights>80,"Physical stairs, doors and native light sources");
        for(var building:plan.buildings()) {
            var layer=CityMapLayout.at((building.minX()+building.maxX())/2.,building.floors()[1],(building.minZ()+building.maxZ())/2.);
            check(layer.id().startsWith(building.id())&&layer.floor()==building.floors()[1],"Minimap follows occupied floor");
            check(Files.exists(root.resolve("textures/gui/city/"+layer.id()+".png")),"Packaged interior minimap");
        }
        var dorm=plan.buildings().stream().filter(b->b.id().equals("dorm")).findFirst().orElseThrow();
        for(String id:List.of("bedroom","lounge","kitchen","reading","bathroom","command","roof")){
            var room=CityPlan.place(id);check(room.x()>dorm.minX()&&room.x()<dorm.maxX()&&room.z()>dorm.minZ()&&room.z()<dorm.maxZ(),"Room lies inside the dorm footprint: "+id);
        }
        check(plan.doors().stream().allMatch(d->d.target().equals("tartarus")),"No teleport links to detached interiors");
        var old=CityPlan.legacy();check(old.cells().length==223805&&old.meshes().size()==354,"Exact old blueprint retained for scoped cleanup");
        var fresh=new CityState();check(!CityBuilder.beginRevision(fresh),"Undecided worlds not rebuilt");fresh.choice=-1;check(!CityBuilder.beginRevision(fresh),"City-disabled world untouched");
        var migrating=new CityState();migrating.choice=1;migrating.ready=true;migrating.cursor=old.cells().length+354;
        check(CityBuilder.beginRevision(migrating)&&migrating.rebuilding&&!migrating.ready&&migrating.cursor==0,"Old layout starts native migration");
        migrating.cleanupCursor=293;check(!CityBuilder.beginRevision(migrating)&&migrating.cleanupCursor==293,"Restart does not reset cleanup");
        migrating.revision=2;migrating.rebuilding=false;migrating.ready=true;check(!CityBuilder.beginRevision(migrating),"Current city stays intact");
        var report=Map.of("checks",checks,"nativeBlocks",plan.cells().length,"architectureMeshes",0,"stairs",stairs,"doorHalves",doors,"lights",lights,"physicalInteriors",true,"migration",true);
        var out=Path.of("build/reports/city/native.json");Files.createDirectories(out.getParent());Files.writeString(out,new GsonBuilder().setPrettyPrinting().create().toJson(report));
        System.out.println("PASS native city revision: "+report);
    }
}
