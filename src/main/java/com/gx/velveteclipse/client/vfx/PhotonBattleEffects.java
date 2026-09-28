package com.gx.velveteclipse.client.vfx;

import com.gx.velveteclipse.core.persona.*;
import com.lowdragmc.photon.client.fx.IEffect;
import com.lowdragmc.photon.client.gameobject.IFXObject;
import com.lowdragmc.photon.client.gameobject.emitter.beam.*;
import com.lowdragmc.photon.client.gameobject.emitter.particle.*;
import com.lowdragmc.photon.client.gameobject.emitter.data.*;
import com.lowdragmc.photon.client.gameobject.emitter.data.material.TextureMaterial;
import com.lowdragmc.photon.client.gameobject.emitter.data.number.*;
import com.lowdragmc.photon.client.gameobject.emitter.data.shape.Cone;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;
import org.joml.Quaternionf;
import java.util.*;
import java.util.function.Supplier;

/** Real Photon emitters. All instances are finite, depth-tested and bounded per client. */
@Mod.EventBusSubscriber(modid="velvet_eclipse",value=Dist.CLIENT)
public final class PhotonBattleEffects {
    private record Live(IFXObject object,long expires,ImpactAnchor anchor,Vector3f offset){}
    private static final Deque<Live> live=new ArrayDeque<>();
    private static net.minecraft.client.multiplayer.ClientLevel level;
    public static void reset(){for(var x:live)x.object.remove(true);live.clear();SpellCoreRenderer.reset();level=null;}
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event){
        if(event.phase!=TickEvent.Phase.END)return;var world=Minecraft.m_91087_().f_91073_;
        if(world!=level){reset();return;}
        if(world!=null){long time=world.m_46467_();live.removeIf(x->{if(!x.object.m_107276_()||time>=x.expires){x.object.remove(true);return true;}if(x.anchor!=null){var at=x.anchor.position(1);x.object.updatePos(new Vector3f((float)at.f_82479_,(float)at.f_82480_,(float)at.f_82481_).add(x.offset));}return false;});}
    }
    public static void impact(String skill,double x,double y,double z,long seed){impact(skill,new com.gx.velveteclipse.network.BattleLightningS2C.Strike(x,y,z,seed));}
    public static void impact(String skill,com.gx.velveteclipse.network.BattleLightningS2C.Strike target){
        impact(skill,SpellVisuals.family(skill),SpellVisuals.scale(skill),new ImpactAnchor(target),target.seed());
    }
    public static void impact(Element family,float scale,double x,double y,double z,long seed){
        impact("",family,scale,new ImpactAnchor(new com.gx.velveteclipse.network.BattleLightningS2C.Strike(x,y,z,seed)),seed);
    }
    private static void impact(String skill,Element family,float scale,ImpactAnchor anchor,long seed){
        var world=Minecraft.m_91087_().f_91073_;if(world==null)return;if(world!=level){reset();level=world;}
        var at=anchor.position(1);var plan=PhotonEffectPlan.create(family,scale,seed);Vector3f origin=new Vector3f((float)at.f_82479_,(float)at.f_82480_,(float)at.f_82481_);
        SpellCoreRenderer.add(skill,family,scale,anchor,seed);
        for(var b:plan.beams()){
            var config=new BeamConfig();config.setLooping(false);config.setDuration(b.life());config.setStartDelay(b.delay());config.setEmitRate(new Constant(1));
            config.getEnd().set(b.to().x()-b.from().x(),b.to().y()-b.from().y(),b.to().z()-b.from().z());
            config.setWidth(new Fade(b.width(),false));config.setColor(new Fade(b.color(),true));material(config.material,"velvet_eclipse:textures/vfx/beam_glow.png");config.renderer.setBloomEffect(false);
            var beam=new BeamEmitter(config);emit(beam,new Vector3f(origin).add(b.from().x(),b.from().y(),b.from().z()),b.delay()+b.life()+4,anchor,new Vector3f(b.from().x(),b.from().y(),b.from().z()));
        }
        particles(origin,plan.color(),plan.sparks(),plan.speed(),plan.size(),plan.life(),"photon:textures/particle/laser.png");
        if(family==Element.FIRE||family==Element.NUCLEAR||family==Element.ALMIGHTY)particles(new Vector3f(origin).add(0,.5f,0),plan.color(),10,.035f,.85f,18,"photon:textures/particle/smoke.png");
    }
    public static void summon(double x,double y,double z){
        impact(Element.SUPPORT,.85f,x,y,z,41);if(level==null)return;
        for(int i=0;i<6;i++){double a=i*Math.PI/3;var c=new BeamConfig();c.setLooping(false);c.setDuration(18);c.getEnd().set(0,3.6f,0);c.setWidth(new Fade(.14f,false));c.setColor(new Fade(0xFF59DCFF,true));material(c.material,"velvet_eclipse:textures/vfx/beam_glow.png");c.renderer.setBloomEffect(false);emit(new BeamEmitter(c),new Vector3f((float)(x+Math.cos(a)*.5),(float)y,(float)(z+Math.sin(a)*.5)),22);}
        particles(new Vector3f((float)x,(float)y+.7f,(float)z),0xFFA6EEFF,28,.1f,.17f,28,"photon:textures/particle/laser.png");
    }
    public static void trigger(double x,double y,double z){
        var world=Minecraft.m_91087_().f_91073_;if(world==null)return;if(world!=level){reset();level=world;}
        particles(new Vector3f((float)x,(float)y,(float)z),0xFFBBF8FF,16,.13f,.12f,13,"photon:textures/particle/laser.png");
    }
    private static void particles(Vector3f origin,int color,int count,float speed,float size,int life,String texture){
        var emitter=new ParticleEmitter();var c=emitter.config;c.setDuration(2);c.setLooping(false);c.setStartDelay(new Constant(0));c.setStartLifetime(new RandomConstant(life*.6f,life,true));c.setStartSpeed(new RandomConstant(speed*.5f,speed,true));
        c.setStartSize(new NumberFunction3(size,size,size));c.setStartColor(new Constant(color));c.setMaxParticles(count);c.setParallelUpdate(false);c.setParallelRendering(false);
        c.emission.setEmissionRate(new Constant(0));var burst=new EmissionSetting.Burst();burst.time=0;burst.cycles=1;burst.interval=1;burst.probability=1;burst.setCount(new Constant(count));c.emission.setBursts(List.of(burst));
        var cone=new Cone();cone.setAngle(65);cone.setRadius(.35f);cone.setRadiusThickness(1);cone.setArc(360);c.shape.setShape(cone);
        c.colorOverLifetime.setEnable(true);c.colorOverLifetime.setColor(new Fade(0xFFFFFFFF,true));
        c.sizeOverLifetime.setEnable(true);c.sizeOverLifetime.setSize(new NumberFunction3(new Fade(1,false),new Fade(1,false),new Fade(1,false)));
        material(c.material,texture);c.renderer.setBloomEffect(false);emit(emitter,origin,life+5);
    }
    private static void material(MaterialSetting m,String texture){m.setMaterial(new TextureMaterial(new ResourceLocation(texture)));m.setDepthTest(true);m.setDepthMask(false);m.setCull(false);var blend=m.getBlendMode();blend.setEnableBlend(true);blend.setSrcColorFactor(GlStateManager.SourceFactor.SRC_ALPHA);blend.setDstColorFactor(GlStateManager.DestFactor.ONE);}
    private static void emit(IFXObject object,Vector3f pos,int life){
        emit(object,pos,life,null,null);
    }
    private static void emit(IFXObject object,Vector3f pos,int life,ImpactAnchor anchor,Vector3f offset){
        while(live.size()>=320)live.removeFirst().object.remove(true);
        var world=level;IEffect effect=()->world;object.emmit(effect,pos,new Quaternionf(),new Vector3f(1));live.addLast(new Live(object,world.m_46467_()+life,anchor,offset));
    }
    private static final class Fade extends Constant {
        private final float value;private final int color;private final boolean tint;
        Fade(Number value,boolean tint){super(value);this.value=value.floatValue();this.color=value.intValue();this.tint=tint;}
        private Number sample(float t){float a=Math.max(0,Math.min(1,(1-t)*1.4f));return tint?(Number)(Integer.valueOf(((int)(a*255)<<24)|(color&0xFFFFFF))):Float.valueOf(value*a);}
        @Override public Number get(RandomSource random,float t){return sample(t);}
        @Override public Number get(float t,Supplier<Float> random){return sample(t);}
        @Override public NumberFunction copy(){if(tint)return new Fade(Integer.valueOf(color),true);return new Fade(Float.valueOf(value),false);}
    }
}
