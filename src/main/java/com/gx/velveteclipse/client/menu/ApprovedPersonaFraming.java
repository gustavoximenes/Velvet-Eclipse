package com.gx.velveteclipse.client.menu;

import com.google.gson.Gson;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/** Bounds are sampled from the native GeckoLib rigs, after their actual renderer scale. */
public final class ApprovedPersonaFraming {
    public record Profile(double[] idle, double[] action, double[][] sheet) {}
    public record Fit(double x, double y, float scale) {}
    private static final Map<String, Profile> PROFILES = load();
    private static Map<String, Profile> load() {
        try (var in = ApprovedPersonaFraming.class.getResourceAsStream("/assets/velvet_eclipse/persona-preview-bounds.json")) {
            if (in == null) return Map.of();
            var type = new com.google.gson.reflect.TypeToken<Map<String, Profile>>() {}.getType();
            return new Gson().fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), type);
        } catch (java.io.IOException e) { throw new IllegalStateException("Persona preview bounds", e); }
    }
    public static float facing(String id) {
        return switch (id) { case "forneus" -> 35; case "chimera" -> 24; case "jack_frost" -> -14; default -> 0; };
    }
    public static float tilt(String id) { return id.equals("forneus") ? .24f : .06f; }
    public static Matrix4f projection(String id, float yaw) {
        return new Matrix4f().scaling(1, 1, -1).rotateZ((float)Math.PI)
            .rotateX(tilt(id)).rotateY((float)Math.toRadians(180-yaw));
    }
    /** Screen occupancy measured from the supplied Reload references, in the 900 x 500 canvas.
     * The original status sheet overlays the lower body with stats. It does not miniaturize the rig.
     * Keep the reference scale while rotating; only reduce it when it would leave the viewport.
     */
    public static Fit sheet(String id, float yaw, double revealOffset) {
        var profile=PROFILES.get(id);
        if(profile==null||profile.sheet()==null)return fit(id,495+revealOffset,34,395,412,yaw,false);
        double[] reference=projectedIdle(id,180+facing(id)), current=projectedIdle(id,yaw);
        double extent=switch(id){case "apsaras"->412;case "chimera"->418;case "jack_frost"->264;case "pixie"->246;case "forneus"->466;default->378;};
        double centerX=id.equals("forneus")?646:692;
        double centerY=switch(id){case "apsaras","chimera"->236;case "jack_frost","pixie"->276;case "forneus"->224;default->246;};
        double scale=extent/Math.max(.1,id.equals("forneus")?reference[2]-reference[0]:reference[3]-reference[1]);
        // Keep geometry inside the frame, including horns and the animated tail, in all rotations.
        double width=current[2]-current[0],height=current[3]-current[1];
        scale=Math.min(scale,Math.min(2*Math.min(centerX-396,891-centerX)/Math.max(.1,width),2*Math.min(centerY-8,452-centerY)/Math.max(.1,height)));
        return new Fit(centerX+revealOffset-(current[0]+current[2])*.5*scale,centerY-(current[1]+current[3])*.5*scale,(float)scale);
    }
    public static double[] projectedIdle(String id,float yaw) {
        var profile=PROFILES.get(id);var frames=profile.sheet();double angle=(yaw%360+360)%360,step=360./frames.length;
        int lo=(int)Math.floor(angle/step);double[] a=frames[lo],b=frames[(lo+1)%frames.length];
        // A point follows a sinusoid between samples; pad the endpoint envelope by its chord sag.
        var box=profile.idle();double radius=Math.hypot(Math.max(Math.abs(box[0]),Math.abs(box[3])),Math.max(Math.abs(box[2]),Math.abs(box[5])));
        double pad=.002+radius*(1-Math.cos(Math.toRadians(step*.5)));
        return new double[]{Math.min(a[0],b[0])-pad,Math.min(a[1],b[1])-pad,Math.max(a[2],b[2])+pad,Math.max(a[3],b[3])+pad};
    }
    public static Fit fit(String id, double x, double y, double width, double height, float yaw, boolean action) {
        var profile = PROFILES.get(id);
        double[] b = profile == null ? new double[]{-.7, 0, -.7, .7, 2.4, .7} : action ? profile.action() : profile.idle();
        var projection = projection(id, yaw);
        double minX=1e9, minY=1e9, maxX=-1e9, maxY=-1e9;
        for (int i=0;i<8;i++) {
            var p = projection.transformPosition(new Vector3f((float)b[(i&1)==0?0:3], (float)b[(i&2)==0?1:4], (float)b[(i&4)==0?2:5]));
            minX=Math.min(minX,p.x); maxX=Math.max(maxX,p.x); minY=Math.min(minY,p.y); maxY=Math.max(maxY,p.y);
        }
        float scale=(float)Math.max(1,Math.min((width-16)/Math.max(.1,maxX-minX),(height-16)/Math.max(.1,maxY-minY)));
        return new Fit(x+width*.5-(minX+maxX)*.5*scale, y+height*.5-(minY+maxY)*.5*scale, scale);
    }
    private ApprovedPersonaFraming() {}
}
