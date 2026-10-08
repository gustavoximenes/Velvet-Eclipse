package com.gx.velveteclipse.client.menu;

import com.gx.velveteclipse.VelvetEclipseMod;
import com.gx.velveteclipse.client.render.VelvetGeoEntityRenderer;
import com.gx.velveteclipse.entity.VelvetPersonaGeoEntity;
import com.gx.velveteclipse.forge.PersonaMobRegistry;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import java.util.*;

/** Menu-only actors are never added to a level and never run AI or server actions. */
public final class PersonaModelPreview {
    private final Map<String,Actor> actors=new HashMap<>();
    private final Set<String> failed=new HashSet<>();
    private double time,last=-1,rotation,targetRotation;
    private static final class Actor {
        final PreviewEntity entity;final VelvetGeoEntityRenderer<PreviewEntity> renderer;
        Actor(PreviewEntity entity,VelvetGeoEntityRenderer<PreviewEntity> renderer){this.entity=entity;this.renderer=renderer;}
    }
    public static final class PreviewEntity extends VelvetPersonaGeoEntity {
        double animationTicks;
        PreviewEntity(EntityType<? extends Vex> type,Level level){super(type,level);}
        // GeckoLib 4.8.4 adds Minecraft's partial tick to DataTickets.TICK itself.
        // Subtract it here so the preview remains continuous at any render frame rate.
        @Override public double getTick(Object unused){return clockData(timeTicks(),Minecraft.m_91087_().m_91296_());}
        private double timeTicks(){return animationTicks;}
    }
    public static double clockData(double animationTicks,float partial){return animationTicks-partial;}
    public void rotate(int direction){targetRotation+=direction*15;}
    public void advance(double now,boolean motion){
        double dt=last<0?0:Math.min(.1,Math.max(0,now-last));last=now;
        if(motion)time+=dt;
        rotation=motion?MenuMotion.approach(rotation,targetRotation,dt):targetRotation;
    }
    public String error(String id){return failed.contains(id)?"Modelo indisponível. Reabra o menu para tentar novamente.":"";}
    @SuppressWarnings("unchecked") private Actor actor(String id){
        if(failed.contains(id))return null;
        if(actors.containsKey(id))return actors.get(id);
        var mc=Minecraft.m_91087_();if(mc.f_91073_==null)return null;
        var type=PersonaMobRegistry.all().get(id);
        if(type==null)throw new IllegalStateException("No registered Persona model for "+id);
        var entity=new PreviewEntity((EntityType<? extends Vex>)type,mc.f_91073_);
        PersonaMobRegistry.configurePersonaMob(entity,id);
        var dispatcher=mc.m_91290_();
        var context=new EntityRendererProvider.Context(dispatcher,mc.m_91291_(),mc.m_91289_(),dispatcher.m_234586_(),mc.m_91098_(),mc.m_167973_(),mc.f_91062_);
        var actor=new Actor(entity,new VelvetGeoEntityRenderer<>(context,PersonaDetails.asset(id)));actors.put(id,actor);return actor;
    }
    public void draw(GuiGraphics g,PersonaSheetDesign.View v){
        String id=v.persona().id();Actor actor;
        try{actor=actor(id);}catch(RuntimeException|LinkageError e){fail(id,e);return;}
        if(actor==null)return;
        var mc=Minecraft.m_91087_();var pose=g.m_280168_();var dispatcher=mc.m_91290_();
        var camera=new Quaternionf(dispatcher.m_253208_());
        var entity=actor.entity;entity.animationTicks=time*20;entity.f_19797_=(int)entity.animationTicks;
        float yaw=(float)(180+rotation+(v.motion()?Math.sin(time*.6)*4:0));
        entity.f_20883_=entity.f_20884_=entity.f_20885_=entity.f_20886_=yaw;
        entity.m_146922_(yaw);entity.f_19859_=yaw;entity.m_146926_(0);
        g.m_280262_();pose.m_85836_();
        try{
            // Identical coordinate convention to the vanilla inventory renderer, with an independent Gecko renderer.
            final float size=ApprovedPersonaFraming.sheetScale(id,id.equals("cinder_wisp")?186:id.equals("umbra_slime")?219:id.equals("izanagi")?160:id.equals("arsene")||id.equals("orpheus")?170:185);
            pose.m_85837_(PersonaSheetDesign.modelX(v),440+(v.motion()?Math.sin(time*1.25)*3:0),MenuDepth.MODEL);
            pose.m_252931_(new Matrix4f().scaling(size,size,-size));
            var tilt=new Quaternionf().rotationX(.06f);
            pose.m_252781_(new Quaternionf().rotationZ((float)Math.PI).mul(tilt));
            Lighting.m_166384_();dispatcher.m_252923_(new Quaternionf(tilt).conjugate());
            float partial=mc.m_91296_();
            RenderSystem.runAsFancy(()->actor.renderer.m_7392_(entity,yaw,partial,pose,g.m_280091_(),15728880));
        }catch(RuntimeException|LinkageError e){fail(id,e);}
        finally{try{g.m_280262_();}finally{dispatcher.m_252923_(camera);pose.m_85849_();Lighting.m_84931_();}}
    }
    private void fail(String id,Throwable error){if(failed.add(id))VelvetEclipseMod.LOGGER.error("Persona menu preview failed for "+id,error);}
    public void drawCard(GuiGraphics g,String id,double x,double y,double w,double h){
        drawCard(g,id,x,y,w,h,-1);
    }
    public void drawReveal(GuiGraphics g,String id,double x,double y,double w,double h,double seconds){
        drawCard(g,id,x,y,w,h,seconds);
    }
    private void drawCard(GuiGraphics g,String id,double x,double y,double w,double h,double reveal){
        Actor a;try{a=actor(id);}catch(RuntimeException|LinkageError e){fail(id,e);return;}if(a==null)return;
        var mc=Minecraft.m_91087_();var pose=g.m_280168_();var dispatcher=mc.m_91290_();var camera=new Quaternionf(dispatcher.m_253208_());
        var e=a.entity;e.animationTicks=(reveal<0?time:reveal<3.125?reveal*.8:2.5+reveal-3.125)*20;e.f_19797_=(int)e.animationTicks;float yaw=(float)(180+(reveal<0?rotation:-9+20*Math.sin(reveal*.6)));
        e.f_20883_=e.f_20884_=e.f_20885_=e.f_20886_=yaw;e.m_146922_(yaw);e.f_19859_=yaw;e.m_146926_(reveal>=0&&reveal<3.125?-40:0);
        g.m_280262_();pose.m_85836_();
        try{float size=ApprovedPersonaFraming.cardScale(id,w,h,(float)(h/(id.equals("umbra_slime")?2.0:2.7)));pose.m_85837_(x+w*.5,y+h-8+ApprovedPersonaFraming.baselineOffset(id,size),MenuDepth.MODEL);pose.m_252931_(new Matrix4f().scaling(size,size,-size));var tilt=new Quaternionf().rotationX(.06f);pose.m_252781_(new Quaternionf().rotationZ((float)Math.PI).mul(tilt));Lighting.m_166384_();dispatcher.m_252923_(new Quaternionf(tilt).conjugate());RenderSystem.runAsFancy(()->a.renderer.m_7392_(e,yaw,mc.m_91296_(),pose,g.m_280091_(),15728880));}
        catch(RuntimeException|LinkageError error){fail(id,error);}finally{try{g.m_280262_();}finally{dispatcher.m_252923_(camera);pose.m_85849_();Lighting.m_84931_();}}
    }
}
