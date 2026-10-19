package com.gx.velveteclipse.city;

import com.google.gson.Gson;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Approved attendants and explicit service markers. No invented shopkeepers. */
public final class CityShopAccess {
    public record Point(String shop,String asset,boolean npc,double x,double y,double z,
                        float yaw,float scale,float width,float height,int[] anchor) {
        public UUID uuid(){return UUID.nameUUIDFromBytes(("velvet_eclipse:city/shop/"+shop).getBytes(StandardCharsets.UTF_8));}
        public double distanceSquared(double px,double py,double pz){return (x-px)*(x-px)+(y-py)*(y-py)+(z-pz)*(z-pz);}
    }
    private static final List<Point> POINTS=load();
    private static List<Point> load(){
        try(var in=new InputStreamReader(Objects.requireNonNull(CityShopAccess.class.getResourceAsStream("/data/velvet_eclipse/city/shop-access.json")),StandardCharsets.UTF_8)){
            var points=List.of(new Gson().fromJson(in,Point[].class));
            if(points.size()!=CityCatalog.SHOPS.size()||points.stream().map(Point::shop).distinct().count()!=points.size()
                ||points.stream().anyMatch(p->!CityCatalog.SHOPS.contains(p.shop())||p.anchor().length!=3))throw new IllegalStateException("Invalid shop access points");
            return points;
        }catch(java.io.IOException e){throw new IllegalStateException("Shop access points",e);}
    }
    public static List<Point> all(){return POINTS;}
    public static Point get(String shop){return POINTS.stream().filter(p->p.shop().equals(shop)).findFirst().orElse(null);}
    public static boolean hasNpc(String shop){var point=get(shop);return point!=null&&point.npc();}
    public static String name(String shop){int index=CityCatalog.SHOPS.indexOf(shop);return index<0?"":CityCatalog.NAMES.get(index);}
    public static boolean permits(boolean cityReady,boolean available,boolean inside,boolean managed,boolean present,boolean matches,double distanceSquared){
        return cityReady&&available&&inside&&managed&&present&&matches&&Double.isFinite(distanceSquared)&&distanceSquared<=36;
    }
    private CityShopAccess(){}
}
