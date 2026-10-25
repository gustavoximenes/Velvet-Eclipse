package com.gx.velveteclipse.client.vfx;

import com.google.gson.*;
import com.lowdragmc.lowdraglib.client.renderer.impl.IModelRenderer;
import com.lowdragmc.lowdraglib.utils.Range;
import com.lowdragmc.photon.client.fx.IEffect;
import com.lowdragmc.photon.client.gameobject.emitter.particle.*;
import com.lowdragmc.photon.client.gameobject.emitter.data.*;
import com.lowdragmc.photon.client.gameobject.emitter.data.material.TextureMaterial;
import com.lowdragmc.photon.client.gameobject.emitter.data.number.*;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import org.joml.Vector3f;
import java.util.*;
import java.util.function.*;

/** Finite Photon 1.1.17 instance for approved elemental meshes and flipbooks.
 * The caller supplies a confirmed, frozen impact anchor and owns cancellation.
 */
public final class PhotonElementalInstance implements AutoCloseable {
    private record Live(ParticleEmitter emitter, JsonObject part, int end) {}
    private final List<JsonObject> pending = new ArrayList<>();
    private final List<Live> live = new ArrayList<>();
    private final IEffect effect;
    private final Vector3f anchor;
    private int last = -1;
    private boolean closed;

    public PhotonElementalInstance(JsonObject recipe, IEffect effect, Vector3f anchor) {
        this.effect=Objects.requireNonNull(effect);this.anchor=new Vector3f(anchor);
        for(var p:recipe.getAsJsonArray("parts"))pending.add(p.getAsJsonObject().deepCopy());
        if(pending.size()>96)throw new IllegalArgumentException("Draft part ceiling");
        for(var p:pending)if(i(p,"start")<0||i(p,"life")<=0||i(p,"start")+i(p,"life")>80)throw new IllegalArgumentException("Finite timeline required");
    }
    public static void registerModels(JsonArray recipes, Consumer<ResourceLocation> register) {
        var names=new HashSet<String>();
        for(var r:recipes)for(var p:r.getAsJsonObject().getAsJsonArray("parts")){
            var id=p.getAsJsonObject().get("model").getAsString();
            if(!id.equals("fire_flipbook")&&!id.equals("ground_glow")&&names.add(id))register.accept(resource("models",id));
        }
    }
    public void tick(int age) {
        if(closed||age==last)return;
        if(age<last)throw new IllegalArgumentException("Monotonic timeline");last=age;
        pending.removeIf(p->{int start=i(p,"start");if(age<start)return false;
            if(age==start)spawn(p);return true;}); // A late tick never bursts every expired stage.
        for(var it=live.iterator();it.hasNext();){var l=it.next();if(age>=l.end){l.emitter.remove(true);it.remove();continue;}
            var p=l.part;float u=(age-i(p,"start"))/(float)i(p,"life");
            l.emitter.updatePos(new Vector3f(anchor).add(vec(p,"position").lerp(vec(p,"endPosition"),u)));
            // Photon Model mode reads the particle's own size/rotation, not the emitter transform.
            // Growth and spin therefore live in the particle config; only its anchor moves here.
        }
    }
    private void spawn(JsonObject p) {
        var emitter=new ParticleEmitter();configure(emitter.config,p);
        emitter.emmit(effect,new Vector3f(anchor).add(vec(p,"position")),new org.joml.Quaternionf(),new Vector3f(1,1,1));
        live.add(new Live(emitter,p,i(p,"start")+i(p,"life")));
    }
    static void configureMotion(ParticleConfig c,JsonObject p) {
        c.setDuration(1);c.setLooping(false);c.setStartDelay(new Constant(0));
        c.setStartLifetime(new Constant(i(p,"life")));c.setStartSpeed(new Constant(0));
        var id=p.get("model").getAsString();boolean fire=id.equals("fire_flipbook"),ground=id.equals("ground_glow");
        var size=vec(p,"scale");if(fire||ground)size.mul(.5f); // Billboard corners are [-1,1], not [-.5,.5].
        c.setStartSize(new NumberFunction3(size.x,size.y,size.z));var rotation=vec(p,"rotation");
        c.setStartRotation(new NumberFunction3(rotation.x,rotation.y,rotation.z));
        c.sizeOverLifetime.setEnable(true);var grow=new Growth(p.getAsJsonArray("growth"));
        c.sizeOverLifetime.setSize(new NumberFunction3(grow,grow.copy(),grow.copy()));
        var spin=vec(p,"spin");c.rotationOverLifetime.setEnable(true);
        // Photon 1.1.17 names its X/Y channels yaw/pitch respectively.
        c.rotationOverLifetime.setYaw(new Spin(spin.x));c.rotationOverLifetime.setPitch(new Spin(spin.y));c.rotationOverLifetime.setRoll(new Spin(spin.z));
        c.setSimulationSpace(ParticleConfig.Space.Local);c.setMaxParticles(1);
        var point=new com.lowdragmc.photon.client.gameobject.emitter.data.shape.Cone();point.setRadius(0);point.setAngle(0);c.shape.setShape(point);
        c.setParallelUpdate(false);c.setParallelRendering(false);
        c.emission.setEmissionRate(new Constant(0));var burst=new EmissionSetting.Burst();
        burst.time=0;burst.cycles=1;burst.interval=1;burst.probability=1;burst.setCount(new Constant(1));c.emission.setBursts(List.of(burst));
        c.setStartColor(new Constant(0xff000000|Integer.parseInt(p.get("color").getAsString().substring(1),16)));
        c.colorOverLifetime.setEnable(true);c.colorOverLifetime.setColor(new Alpha(p.get("alpha").getAsFloat()));
    }
    private static void configure(ParticleConfig c,JsonObject p) {
        configureMotion(c,p);
        var id=p.get("model").getAsString();boolean fire=id.equals("fire_flipbook"),ground=id.equals("ground_glow");
        if(fire||ground){
            c.renderer.setRenderMode(ground?RendererSetting.Particle.Mode.Horizontal:RendererSetting.Particle.Mode.Billboard);
            c.material.setMaterial(new TextureMaterial(resource("textures",ground?"glow":"fire")));
            if(fire){
            c.uvAnimation.setEnable(true);c.uvAnimation.setTiles(new Range(8,4));
            c.uvAnimation.setAnimation(UVAnimationSetting.Animation.WholeSheet);c.uvAnimation.setCycle(1);
            c.uvAnimation.setStartFrame(new Constant(0));c.uvAnimation.setFrameOverTime(new Frames());
            }
        }else{
            c.renderer.setRenderMode(RendererSetting.Particle.Mode.Model);
            c.renderer.setModel(new IModelRenderer(resource("models",id)));
            c.renderer.setUseBlockUV(true);c.renderer.setShade(false);
            c.material.setMaterial(new TextureMaterial(resource("textures","white")));
        }
        c.renderer.setBloomEffect(false);c.material.setDepthTest(true);c.material.setDepthMask(false);c.material.setCull(false);
        var blend=c.material.getBlendMode();blend.setEnableBlend(true);blend.setSrcColorFactor(GlStateManager.SourceFactor.SRC_ALPHA);
        blend.setDstColorFactor(p.get("additive").getAsBoolean()?GlStateManager.DestFactor.ONE:GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    }
    private static ResourceLocation resource(String kind,String id){return new ResourceLocation("velvet_eclipse",kind.equals("textures")?"textures/vfx/audit02/"+id+".png":"vfx/audit02/"+id);}
    private static int i(JsonObject p,String k){return p.get(k).getAsInt();}
    private static Vector3f vec(JsonObject p,String k){var a=p.getAsJsonArray(k);return new Vector3f(a.get(0).getAsFloat(),a.get(1).getAsFloat(),a.get(2).getAsFloat());}
    private static float growth(JsonObject p,float u){var g=p.getAsJsonArray("growth");float a=g.get(0).getAsFloat(),b=g.get(1).getAsFloat(),c=g.get(2).getAsFloat();return u<.27f?a+(b-a)*u/.27f:b+(c-b)*(u-.27f)/.73f;}
    public boolean done(){return pending.isEmpty()&&live.isEmpty();}
    @Override public void close(){live.forEach(l->l.emitter.remove(true));live.clear();pending.clear();closed=true;}
    private static final class Growth extends Constant{
        private final float a,b,c;Growth(JsonArray g){this(g.get(0).getAsFloat(),g.get(1).getAsFloat(),g.get(2).getAsFloat());}
        Growth(float a,float b,float c){super(1);this.a=a;this.b=b;this.c=c;}
        private Number value(float u){return u<.27f?a+(b-a)*u/.27f:b+(c-b)*(u-.27f)/.73f;}
        @Override public Number get(RandomSource r,float u){return value(u);}
        @Override public Number get(float u,Supplier<Float> r){return value(u);}
        @Override public NumberFunction copy(){return new Growth(a,b,c);}
    }
    private static final class Spin extends Constant{
        private final float angle;Spin(float angle){super(0);this.angle=angle;}
        @Override public Number get(RandomSource r,float u){return angle*u;}
        @Override public Number get(float u,Supplier<Float> r){return angle*u;}
        @Override public NumberFunction copy(){return new Spin(angle);}
    }
    private static final class Alpha extends Constant{
        private final float gain;Alpha(float gain){super(0xffffffff);this.gain=gain;}
        private Number value(float u){return ((int)(255*gain*Math.min(1,u*12+.2f)*Math.min(1,(1-u)*4))<<24)|0xffffff;}
        @Override public Number get(RandomSource r,float u){return value(u);}
        @Override public Number get(float u,Supplier<Float> r){return value(u);}
        @Override public NumberFunction copy(){return new Alpha(gain);}
    }
    private static final class Frames extends Constant{
        Frames(){super(0);}private Number value(float u){return Math.max(0,Math.min(31,(int)(u*31)));}
        @Override public Number get(RandomSource r,float u){return value(u);}
        @Override public Number get(float u,Supplier<Float> r){return value(u);}
        @Override public NumberFunction copy(){return new Frames();}
    }
}
