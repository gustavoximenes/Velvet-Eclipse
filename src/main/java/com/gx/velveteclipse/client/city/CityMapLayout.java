package com.gx.velveteclipse.client.city;

import com.google.gson.Gson;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Shared dimensions exported alongside map pixels; markers cannot drift after a city rebuild. */
public final class CityMapLayout {
    public record Layer(String id,int minX,int minZ,int maxX,int maxZ,int floor) {
        public double u(double x){return (x-minX)/(maxX-minX+1.0);}
        public double v(double z){return (z-minZ)/(maxZ-minZ+1.0);}
        public boolean contains(double x,double y,double z){return floor>=0&&x>=minX+2&&x<=maxX-1&&z>=minZ+2&&z<=maxZ-1&&y>=floor-.1&&y<floor+6;}
    }
    private static final Layer[] LAYERS=load();
    private static Layer[] load(){try(var in=CityMapLayout.class.getResourceAsStream("/assets/velvet_eclipse/city-map.json")){return new Gson().fromJson(new InputStreamReader(Objects.requireNonNull(in),StandardCharsets.UTF_8),Layer[].class);}catch(java.io.IOException e){throw new IllegalStateException(e);}}
    public static Layer world(){return LAYERS[0];}
    public static Layer at(double x,double y,double z){return Arrays.stream(LAYERS).filter(l->l.contains(x,y,z)).max(Comparator.comparingInt(Layer::floor)).orElse(world());}
    private CityMapLayout(){}
}
