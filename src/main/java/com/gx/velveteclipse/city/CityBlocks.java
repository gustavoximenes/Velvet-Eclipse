package com.gx.velveteclipse.city;

import com.gx.velveteclipse.darkhour.TartarusScenicBlock;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.*;

/** Resolves the exact native block states used by the plan; invalid states fail before construction. */
public final class CityBlocks {
    public static final List<String> MATERIALS=List.of("paving","brick","plaster","wood","glass","school_floor","leaves","roof","light");
    private static List<BlockState> palette;
    public static BlockState state(int[] c) {
        if(c[3]>=100) {
            if(palette==null)palette=CityPlan.get().palette().stream().map(CityBlocks::resolve).toList();
            return palette.get(c[3]-100);
        }
        String id=c[3]==29?"tartarus_access":c[3]>=20?"shop_"+CityCatalog.SHOPS.get(c[3]-20):MATERIALS.get(c[3]);
        var block=CityRegistry.BLOCKS.get(id).get();var state=block.m_49966_();
        if(c.length>4&&c[4]==1&&block instanceof TartarusScenicBlock)state=state.m_61124_(TartarusScenicBlock.SCENIC,true);
        if(c.length>5&&block instanceof CitySurfaceBlock)state=state.m_61124_(CitySurfaceBlock.HEIGHT,c[5]);
        return state;
    }
    public static BlockState resolve(CityPlan.Palette entry) {
        var key=new ResourceLocation(entry.block());
        if(!ForgeRegistries.BLOCKS.containsKey(key))throw new IllegalArgumentException("Unknown city block: "+key);
        var block=Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(key));var state=block.m_49966_();
        for(var p:entry.properties().entrySet()) {
            var property=block.m_49965_().m_61081_(p.getKey());
            if(property==null)throw new IllegalArgumentException("Unknown city property: "+key+"/"+p.getKey());
            state=value(state,property,p.getValue());
        }
        return state;
    }
    private static <T extends Comparable<T>> BlockState value(BlockState state,Property<T> property,String value) {
        return state.m_61124_(property,property.m_6215_(value).orElseThrow(()->new IllegalArgumentException("Invalid city property value: "+value)));
    }
    private CityBlocks(){}
}
