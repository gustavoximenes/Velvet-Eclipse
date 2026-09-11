package com.gx.velveteclipse.client.render;
import com.google.gson.*;
import java.io.Reader;
import java.util.*;

/** CPU-side mesh data; deliberately independent of Minecraft initialization. */
public final class BoneMeshData {
    private final Map<String,float[]> bones;
    private BoneMeshData(Map<String,float[]> bones){this.bones=Collections.unmodifiableMap(bones);}
    public Set<String> names(){return bones.keySet();}
    public float[] vertices(String name){return bones.get(name);}
    public static BoneMeshData read(Reader reader) {
        JsonObject root=JsonParser.parseReader(reader).getAsJsonObject();
        if(root.get("format").getAsInt()!=1)throw new IllegalArgumentException("Unsupported bone mesh format");
        Map<String,float[]> map=new LinkedHashMap<>();int total=0;
        for(var entry:root.getAsJsonObject("bones").entrySet()) {
            JsonArray a=entry.getValue().getAsJsonArray();total+=a.size();
            if(a.size()%32!=0||total>2000000)throw new IllegalArgumentException("Invalid quad count");
            float[] vertices=new float[a.size()];
            for(int i=0;i<vertices.length;i++) {
                float v=a.get(i).getAsFloat();if(!Float.isFinite(v))throw new IllegalArgumentException("Non-finite vertex");
                vertices[i]=v;
            }
            for(int i=0;i<vertices.length;i+=8) {
                if(vertices[i+3]<0||vertices[i+3]>1||vertices[i+4]<0||vertices[i+4]>1)throw new IllegalArgumentException("UV out of bounds");
                double n=Math.sqrt(vertices[i+5]*vertices[i+5]+vertices[i+6]*vertices[i+6]+vertices[i+7]*vertices[i+7]);
                if(Math.abs(n-1)>.005)throw new IllegalArgumentException("Invalid vertex normal");
            }
            map.put(entry.getKey(),vertices);
        }
        return new BoneMeshData(map);
    }
}
