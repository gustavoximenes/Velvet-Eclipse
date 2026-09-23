package com.gx.velveteclipse.client;

import com.gx.velveteclipse.network.BattleSpellS2C;
import com.gx.velveteclipse.client.vfx.VelvetVfxRenderType;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import java.util.*;

/** Uses the same emissive custom shader as Zio; independent of the viewer's active Persona. */
@Mod.EventBusSubscriber(modid="velvet_eclipse",bus=Mod.EventBusSubscriber.Bus.FORGE,value=Dist.CLIENT)
public final class SpellImpactRenderer {
    private record Impact(BattleSpellS2C packet,long tick){}
    private static final List<Impact> active=new ArrayList<>();
    private static Object level;
    public static void reset(){active.clear();level=null;}
    public static void accept(BattleSpellS2C p){
        var world=Minecraft.m_91087_().f_91073_;if(world==null)return;
        if(level!=world)reset();level=world;
        if(active.size()>=8)active.remove(0);active.add(new Impact(p,world.m_46467_()));
    }
    @SubscribeEvent public static void render(RenderLevelStageEvent e){
        if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_PARTICLES)return;
        var mc=Minecraft.m_91087_();if(mc.f_91073_==null||mc.f_91073_!=level){reset();return;}
        double now=mc.f_91073_.m_46467_()+e.getPartialTick();active.removeIf(i->now-i.tick>=SpellImpact.LIFETIME||now<i.tick);
        if(active.isEmpty()||!VelvetVfxRenderType.ready())return;
        var camera=e.getCamera().m_90583_();var buffers=mc.m_91269_().m_110104_();var pose=e.getPoseStack();
        for(var impact:active)for(var target:impact.packet.targets()){
            pose.m_85836_();
            try{
                pose.m_85837_(target.x()-camera.f_82479_,target.y()-camera.f_82480_,target.z()-camera.f_82481_);
                var eye=new Vector3f((float)(camera.f_82479_-target.x()),(float)(camera.f_82480_-target.y()),(float)(camera.f_82481_-target.z()));
                var out=buffers.m_6299_(VelvetVfxRenderType.GLOW);var matrix=pose.m_85850_().m_252922_();
                for(var stroke:SpellImpact.geometry(impact.packet.skill(),now-impact.tick,target.seed()))ribbon(out,matrix,eye,stroke);
                buffers.m_109912_(VelvetVfxRenderType.GLOW);
            }finally{pose.m_85849_();}
        }
    }
    private static void ribbon(VertexConsumer out,Matrix4f matrix,Vector3f eye,SpellImpact.Stroke s){
        var side=new Vector3f(s.to()).sub(s.from()).cross(new Vector3f(eye).sub(s.from()));
        if(side.lengthSquared()<.000001f)side.set(1,0,0);side.normalize().mul(s.width());
        vertex(out,matrix,new Vector3f(s.from()).sub(side),s,0,0);vertex(out,matrix,new Vector3f(s.to()).sub(side),s,0,1);
        vertex(out,matrix,new Vector3f(s.to()).add(side),s,1,1);vertex(out,matrix,new Vector3f(s.from()).add(side),s,1,0);
    }
    private static void vertex(VertexConsumer out,Matrix4f matrix,Vector3f p,SpellImpact.Stroke s,float u,float v){
        out.m_252986_(matrix,p.x,p.y,p.z).m_6122_(s.color()>>16&255,s.color()>>8&255,s.color()&255,(int)(s.alpha()*255)).m_7421_(u,v).m_5752_();
    }
}
