package com.gx.velveteclipse.darkhour;

import com.google.gson.Gson;
import com.gx.velveteclipse.city.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

/** Approved clock tower, authored once and baked into native chunk-sized fragments. */
public final class TartarusExterior {
    public static final int REVISION=1,HEIGHT=238,TILE=1000;
    public static final int MIN_X=-32,MAX_X=32,MIN_Z=-32,MAX_Z=42;
    public record Plan(int revision,int height,int sample,int[][] cells,String sourceHash){}
    private static final Plan PLAN=load();
    private static final List<TartarusLayout.Cell> CELLS=Arrays.stream(PLAN.cells()).map(c->new TartarusLayout.Cell(c[0],c[1],c[2],c[3])).toList();
    private static Plan load(){try(var in=new GZIPInputStream(Objects.requireNonNull(TartarusExterior.class.getResourceAsStream("/data/velvet_eclipse/tartarus/exterior.json.gz")))){return new Gson().fromJson(new InputStreamReader(in,StandardCharsets.UTF_8),Plan.class);}catch(IOException e){throw new IllegalStateException("Tartarus exterior",e);}}
    public static Plan plan(){return PLAN;}
    public static List<TartarusLayout.Cell> cells(){return CELLS;}
    public static BlockState state(TartarusLayout.Cell c){return c.material()==4?DarkHourRegistry.PORTAL.get().m_49966_():CityRegistry.TOWER.get().m_49966_().m_61124_(CityArchitectureBlock.LIGHT,Objects.requireNonNull(CityArchitecture.tile(c.material()-TILE)).light());}
    public static void configure(ServerLevel level,BlockPos pos,TartarusLayout.Cell c){if(c.material()>=TILE&&level.m_7702_(pos) instanceof CityArchitectureEntity tile)tile.configure(c.material()-TILE);}
    private TartarusExterior(){}
}
