package com.gx.velveteclipse.city;
import java.io.*;import java.util.*;import java.util.zip.GZIPInputStream;
import com.google.gson.Gson;
public final class CityPlan {
 public record Place(String id,String name,int x,int y,int z){}
 public record Mesh(String name,int x,int y,int z,int minX,int minY,int minZ,int maxX,int maxY,int maxZ){}
 public record Door(int x,int y,int z,String target){}
 public record Palette(String block,Map<String,String> properties){}
 public record Building(String id,int minX,int maxX,int minZ,int maxZ,int[] floors){}
 public record Route(String name,int[][] points){}
 public record Plan(int revision,int[][] cells,List<Mesh> meshes,List<Place> places,List<Door> doors,List<Palette> palette,List<Building> buildings,List<Route> routes){}
 private static Plan plan;
 public static synchronized Plan get(){if(plan==null)try(var in=new GZIPInputStream(Objects.requireNonNull(CityPlan.class.getResourceAsStream("/data/velvet_eclipse/city/plan.json.gz")))){plan=new Gson().fromJson(new InputStreamReader(in,java.nio.charset.StandardCharsets.UTF_8),Plan.class);}catch(IOException e){throw new IllegalStateException(e);}return plan;}
 public static Place place(String id){return get().places().stream().filter(p->p.id().equals(id)).findFirst().orElse(null);}
 public static Plan legacy(){try(var in=new GZIPInputStream(Objects.requireNonNull(CityPlan.class.getResourceAsStream("/data/velvet_eclipse/city/legacy-plan-v1.json.gz")))){return new Gson().fromJson(new InputStreamReader(in,java.nio.charset.StandardCharsets.UTF_8),Plan.class);}catch(IOException e){throw new IllegalStateException(e);}}
}
