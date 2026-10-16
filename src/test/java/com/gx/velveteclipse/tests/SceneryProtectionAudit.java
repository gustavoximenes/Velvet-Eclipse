package com.gx.velveteclipse.tests;

import com.gx.velveteclipse.city.*;
import net.minecraft.resources.*;
import net.minecraft.world.item.*;
import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

public final class SceneryProtectionAudit {
    static int checks;
    static void check(boolean ok,String reason){checks++;if(!ok)throw new AssertionError(reason);}
    public static void main(String[] args)throws Exception {
        for(String id:List.of("velvet_eclipse:city","velvet_eclipse:velvet_room"))
            check(SceneryProtection.protectedLocation(new ResourceLocation(id)),"Authored dimension protected");
        for(String id:List.of("minecraft:overworld","minecraft:the_nether","minecraft:the_end","other:city"))
            check(!SceneryProtection.protectedLocation(new ResourceLocation(id)),"Other worlds unaffected: "+id);
        // Exercise real vanilla item inheritance without starting Minecraft registries.
        for(var type:List.of(BlockItem.class,BucketItem.class,SolidBucketItem.class,FlintAndSteelItem.class,
            BoneMealItem.class,FireChargeItem.class,EndCrystalItem.class,ArmorStandItem.class,HangingEntityItem.class))
            check(SceneryProtection.altersSceneryType(type),"Building/destructive held item denied: "+type);
        check(!SceneryProtection.altersSceneryType(Item.class),"Ordinary mod item remains usable");
        var last=CityArchitecture.tile(CityArchitecture.size()-1);float[] raw=new float[last.offset()+last.length()];
        try(var in=new DataInputStream(new GZIPInputStream(SceneryProtectionAudit.class.getResourceAsStream("/assets/velvet_eclipse/city/architecture.bin.gz")))){for(int i=0;i<raw.length;i++)raw[i]=in.readFloat();}
        var used=new HashSet<Integer>();for(var c:CityPlan.get().cells())if(c[3]==40)used.add(c[4]);
        for(var d:CityFurniture.catalogue().doors())for(var c:d.cells())for(int mode:new int[]{4,5})used.add(c[mode]);
        for(int id:used){var t=CityArchitecture.tile(id);int emission=0;
            // Match the exporter's round-to-even quantization, including 12.5 -> 12.
            for(int i=t.offset()+10;i<t.offset()+t.length();i+=13){if(raw[i]>=.2)emission=Math.max(emission,Math.min(15,(int)Math.rint(9+raw[i]*5)));
                check(raw[i+1]>=-.0001&&raw[i+1]<=1.0001&&raw[i+2]>=-.0001&&raw[i+2]<=1.0001,"UV stays within atlas tile "+id);}
            check(t.light()>=emission,"Opening a door cannot discard static fixture lighting: "+id);
        }
        System.out.println("PASS scenery protection/materials: "+checks+" checks; protected dimensions, held-item policy, UV atlas bounds and light in both door states");
    }
}
