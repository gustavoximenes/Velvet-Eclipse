package com.gx.velveteclipse.client.vfx;

import com.google.gson.*;
import com.lowdragmc.lowdraglib.utils.Range;
import com.lowdragmc.photon.client.fx.IEffect;
import com.lowdragmc.photon.client.gameobject.IFXObject;
import com.lowdragmc.photon.client.gameobject.emitter.beam.*;
import com.lowdragmc.photon.client.gameobject.emitter.trail.*;
import com.lowdragmc.photon.client.gameobject.emitter.particle.*;
import com.lowdragmc.photon.client.gameobject.emitter.data.*;
import com.lowdragmc.photon.client.gameobject.emitter.data.material.TextureMaterial;
import com.lowdragmc.photon.client.gameobject.emitter.data.number.*;
import com.lowdragmc.photon.client.gameobject.emitter.data.shape.Cone;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import org.joml.*;
import java.io.Reader;
import java.util.*;
import java.util.function.Supplier;

/** Finite Photon skill recipe. The existing summoning renderer is deliberately independent. */
public final class PhotonSkillInstance implements AutoCloseable {
    public record Recipe(String id, List<JsonObject> layers) {}
    private record Live(IFXObject fx, JsonObject layer, int start, int end) {}
    private final IEffect effect;
    private final Supplier<Vector3f> anchor;
    private final List<JsonObject> pending;
    private final List<Live> live = new ArrayList<>();
    private boolean closed;
    private int lastTick=-1;

    public static List<Recipe> read(Reader reader) {
        var result=new ArrayList<Recipe>();
        for (var value:JsonParser.parseReader(reader).getAsJsonArray()) {
            var obj=value.getAsJsonObject();var layers=new ArrayList<JsonObject>();
            for(var l:obj.getAsJsonArray("layers"))layers.add(l.getAsJsonObject());
            result.add(new Recipe(obj.get("id").getAsString(),List.copyOf(layers)));
        }
        return List.copyOf(result);
    }
    public PhotonSkillInstance(Recipe recipe, IEffect effect, Supplier<Vector3f> anchor) {
        if(recipe.layers.size()>64)throw new IllegalArgumentException("Audit emitter ceiling");
        this.effect=Objects.requireNonNull(effect);this.anchor=Objects.requireNonNull(anchor);
        this.pending=new ArrayList<>(recipe.layers);
    }
    public void tick(int elapsed) {
        if(closed || elapsed==lastTick)return;
        if(elapsed<lastTick)throw new IllegalArgumentException("Monotonic client timeline required");
        lastTick=elapsed;
        // Do not burst all stale particles after suspension or an event arriving too late.
        pending.removeIf(l->{int start=i(l,"start"),end=start+i(l,"life")+opt(l,"tail",0);
            if(elapsed>=end)return true;
            if(elapsed>=start){if(elapsed==start)spawn(l,elapsed);return true;}return false;});
        var at=new Vector3f(anchor.get());
        for(var it=live.iterator();it.hasNext();) {
            var x=it.next();if(elapsed>=x.end){x.fx.remove(true);it.remove();continue;}
            // Follow the explicit supplied anchor. Ground impacts should use a frozen point.
            var offset=offset(x.layer,(elapsed-x.start)/(float)i(x.layer,"life"));
            x.fx.updatePos(new Vector3f(at).add(offset));
        }
    }
    private void spawn(JsonObject l,int elapsed) {
        String type=s(l,"type");IFXObject object;
        if(type.equals("beam")) {
            var c=new BeamConfig();c.setLooping(false);c.setDuration(i(l,"life"));c.setStartDelay(0);c.setEmitRate(new Constant(1));
            c.getEnd().set(vec(l.getAsJsonArray("b")).sub(vec(l.getAsJsonArray("a"))));
            c.setWidth(new Fade(f(l,"width"),false));c.setColor(new Fade(color(l),true));
            material(c.material,"spark",false);c.renderer.setBloomEffect(false);object=new BeamEmitter(c);
        } else if(type.equals("trail")) {
            var c=new TrailConfig();c.setLooping(false);c.setDuration(i(l,"life"));c.setStartDelay(0);
            c.setTime(i(l,"tail"));c.setMinVertexDistance(.025f);c.setParallelRendering(false);
            c.setWidthOverTrail(new Constant(f(l,"width")));c.setColorOverTrail(new Fade(color(l),true));
            material(c.material,"spark",false);c.renderer.setBloomEffect(false);object=new TrailEmitter(c);
        } else if(type.equals("sprite")) {
            var p=new ParticleEmitter();var c=p.config;String tex=s(l,"texture");
            c.setDuration(1);c.setLooping(false);c.setStartDelay(new Constant(0));c.setStartLifetime(new Constant(i(l,"life")));
            c.setStartSpeed(new Constant(f(l,"speed")));c.setStartSize(new NumberFunction3(l.getAsJsonArray("size").get(0).getAsFloat(),l.getAsJsonArray("size").get(1).getAsFloat(),1));
            c.setStartColor(new Constant(color(l)));c.setMaxParticles(i(l,"count"));c.setParallelRendering(false);c.setParallelUpdate(false);
            c.emission.setEmissionRate(new Constant(0));var b=new EmissionSetting.Burst();b.time=0;b.cycles=1;b.interval=1;b.probability=1;b.setCount(new Constant(i(l,"count")));c.emission.setBursts(List.of(b));
            var shape=new Cone();shape.setAngle(18);shape.setRadius(f(l,"radius"));shape.setRadiusThickness(1);shape.setArc(360);c.shape.setShape(shape);
            c.colorOverLifetime.setEnable(true);c.colorOverLifetime.setColor(new Fade(0xffffffff,true));
            c.renderer.setRenderMode(RendererSetting.Particle.Mode.Billboard);c.renderer.setBloomEffect(false);
            if(tex.equals("fire") || tex.equals("smoke")) {
                c.uvAnimation.setEnable(true);c.uvAnimation.setTiles(new Range(8,4));c.uvAnimation.setAnimation(UVAnimationSetting.Animation.WholeSheet);
                c.uvAnimation.setStartFrame(new Constant(0));c.uvAnimation.setCycle(1);c.uvAnimation.setFrameOverTime(new Frames());
            }
            material(c.material,tex,s(l,"blend").equals("alpha"));object=p;
        } else throw new IllegalArgumentException("Unknown layer "+type);
        object.emmit(effect,new Vector3f(anchor.get()).add(offset(l,0)),new Quaternionf(),new Vector3f(1));
        live.add(new Live(object,l,elapsed,elapsed+i(l,"life")+opt(l,"tail",0)));
    }
    private static Vector3f offset(JsonObject l,float t) {
        return switch(s(l,"type")) {
            case "sprite" -> vec(l.getAsJsonArray("at"));
            case "beam" -> vec(l.getAsJsonArray("a"));
            case "trail" -> {var ps=l.getAsJsonArray("points");float n=java.lang.Math.max(0,java.lang.Math.min(1,t))*(ps.size()-1);int a=(int)n,b=java.lang.Math.min(ps.size()-1,a+1);yield vec(ps.get(a).getAsJsonArray()).lerp(vec(ps.get(b).getAsJsonArray()),n-a);}
            default -> throw new IllegalArgumentException();
        };
    }
    private static void material(MaterialSetting c,String tex,boolean alpha) {
        c.setMaterial(new TextureMaterial(new ResourceLocation("velvet_eclipse:textures/vfx/reload/"+tex+".png")));
        c.setDepthTest(true);c.setDepthMask(false);c.setCull(false);var b=c.getBlendMode();b.setEnableBlend(true);
        b.setSrcColorFactor(GlStateManager.SourceFactor.SRC_ALPHA);
        b.setDstColorFactor(alpha?GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA:GlStateManager.DestFactor.ONE);
    }
    private static Vector3f vec(JsonArray a){return new Vector3f(a.get(0).getAsFloat(),a.get(1).getAsFloat(),a.get(2).getAsFloat());}
    private static String s(JsonObject o,String k){return o.get(k).getAsString();}
    private static int i(JsonObject o,String k){return o.get(k).getAsInt();}
    private static int opt(JsonObject o,String k,int def){return o.has(k)?i(o,k):def;}
    private static float f(JsonObject o,String k){return o.get(k).getAsFloat();}
    private static int color(JsonObject l){return 0xff000000|Integer.parseInt(s(l,"color").substring(1),16);}
    public boolean done(){return pending.isEmpty() && live.isEmpty();}
    @Override public void close(){live.forEach(l->l.fx.remove(true));live.clear();pending.clear();closed=true;}
    private static final class Frames extends Constant {
        Frames(){super(0);}
        private Number sample(float t){return java.lang.Math.min(31,java.lang.Math.max(0,(int)(t*32)));}
        @Override public Number get(RandomSource r,float t){return sample(t);}
        @Override public Number get(float t,Supplier<Float> r){return sample(t);}
        @Override public NumberFunction copy(){return new Frames();}
    }
    private static final class Fade extends Constant {
        private final Number initial;private final boolean color;
        Fade(Number initial,boolean color){super(initial);this.initial=initial;this.color=color;}
        private Number sample(float t){float a=java.lang.Math.max(0,java.lang.Math.min(1,(1-t)*4));
            if(color)return Integer.valueOf(((int)(255*a)<<24)|(initial.intValue()&0xffffff));
            return Float.valueOf(initial.floatValue()*a);}
        @Override public Number get(RandomSource r,float t){return sample(t);}
        @Override public Number get(float t,Supplier<Float> r){return sample(t);}
        @Override public NumberFunction copy(){return new Fade(initial,color);}
    }
}
