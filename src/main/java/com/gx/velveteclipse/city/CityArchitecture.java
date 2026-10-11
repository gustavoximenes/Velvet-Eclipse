package com.gx.velveteclipse.city;

import com.google.gson.Gson;
import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;
import net.minecraft.world.phys.shapes.*;

/** Shared, immutable local geometry metadata. Servers never load vertex buffers. */
public final class CityArchitecture {
    public record Tile(int offset,int length,double[][] boxes,int light){}
    private static final Tile[] TILES=load();
    private static final Map<Integer,VoxelShape> SHAPES=new HashMap<>();
    private static Tile[] load(){try(var in=new GZIPInputStream(Objects.requireNonNull(CityArchitecture.class.getResourceAsStream("/data/velvet_eclipse/city/architecture.json.gz")))){return new Gson().fromJson(new InputStreamReader(in,java.nio.charset.StandardCharsets.UTF_8),Tile[].class);}catch(IOException e){throw new IllegalStateException("City architecture metadata",e);}}
    public static Tile tile(int id){return id>=0&&id<TILES.length?TILES[id]:null;}
    public static int size(){return TILES.length;}
    public static synchronized VoxelShape shape(int id){return SHAPES.computeIfAbsent(id,k->{var tile=tile(k);var shape=Shapes.m_83040_();if(tile!=null)for(var b:tile.boxes())shape=Shapes.m_83110_(shape,Shapes.m_83048_(b[0],b[1],b[2],b[3],b[4],b[5]));return shape.m_83296_();});}
    private CityArchitecture(){}
}
