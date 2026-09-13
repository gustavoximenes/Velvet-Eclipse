package com.gx.velveteclipse.client;

import com.gx.velveteclipse.core.persona.*;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/** Original shader, beam geometry, feathered glow and impact star. No vanilla lightning or particles. */
@Mod.EventBusSubscriber(modid="velvet_eclipse",bus=Mod.EventBusSubscriber.Bus.FORGE,value=Dist.CLIENT)
public final class LightningStrikeRenderer {
    private static java.util.List<com.gx.velveteclipse.network.BattleLightningS2C.Strike> strikes=java.util.List.of();
    private static long startTick;
    public static void reset(){strikes=java.util.List.of();}
    public static void accept(com.gx.velveteclipse.network.BattleLightningS2C packet){var mc=Minecraft.m_91087_();if(mc.f_91073_==null)return;strikes=packet.strikes();startTick=mc.f_91073_.m_46467_();}
    @SubscribeEvent public static void render(RenderLevelStageEvent event) {
        if(event.getStage()!=RenderLevelStageEvent.Stage.AFTER_PARTICLES||strikes.isEmpty()||!com.gx.velveteclipse.client.vfx.VelvetVfxRenderType.ready())return;
        var mc=Minecraft.m_91087_();if(mc.f_91073_==null){reset();return;}
        double tick=82+mc.f_91073_.m_46467_()-startTick+event.getPartialTick();double alpha=LightningStrike.opacity(tick);
        if(tick>=94){reset();return;}if(alpha<=0)return;
        var camera=event.getCamera().m_90583_();
        for(var strike:strikes){var p=new net.minecraft.world.phys.Vec3(strike.x(),strike.y(),strike.z());
        var pose=event.getPoseStack();pose.m_85836_();
        var buffers=mc.m_91269_().m_110104_();var type=com.gx.velveteclipse.client.vfx.VelvetVfxRenderType.GLOW;
        try {
            pose.m_85837_(p.f_82479_-camera.f_82479_,p.f_82480_-camera.f_82480_,p.f_82481_-camera.f_82481_);
            var out=buffers.m_6299_(type);var matrix=pose.m_85850_().m_252922_();
            var eye=new Vector3f((float)(camera.f_82479_-p.f_82479_),(float)(camera.f_82480_-p.f_82480_),(float)(camera.f_82481_-p.f_82481_));
            float bottom=(float)(6.2*(1-Math.min(1,(tick-82)/4)));
            for(var segment:LightningStrike.bolts(strike.seed()+(int)Math.max(0,(tick-86)/2))) {
                var a=segment.from();var b=segment.to();if(a.y<bottom)continue;
                if(b.y<bottom)b=new Vector3f(a).lerp(b,(a.y-bottom)/(a.y-b.y));
                ribbon(out,matrix,eye,a,b,.19f,172,116,248,(int)(alpha*60));
                ribbon(out,matrix,eye,a,b,.078f,225,185,255,(int)(alpha*175));
                ribbon(out,matrix,eye,a,b,.036f,255,250,255,(int)(alpha*255));
            }
            if(tick>=86) {
                float expand=(float)(tick-86);var center=new Vector3f(0,.22f,0);
                for(int i=0;i<22;i++) {
                    double angle=i*Math.PI*2/22;
                    float length=(i%3==0?1.8f:1.05f)*(1+expand*.08f);
                    var end=new Vector3f((float)Math.cos(angle)*length,.2f+(float)Math.abs(Math.sin(angle))*length,(float)Math.sin(angle)*length*.55f);
                    ribbon(out,matrix,eye,center,end,.095f,255,142,229,(int)(alpha*130));
                    ribbon(out,matrix,eye,center,end,.035f,255,255,255,(int)(alpha*255));
                }
                // A white impact star with pink rays; the halo stays close to the ground.
                var up=new Vector3f(0,1,0);var right=new Vector3f(eye).cross(up);if(right.lengthSquared()<.001f)right.set(1,0,0);right.normalize();
                float flash=(float)Math.max(0,1-expand/5f);
                for(int i=0;i<10;i++){
                    double angle=i*Math.PI/5;float length=(i%2==0?1.15f:.55f)*(1+expand*.08f);
                    var tip=new Vector3f(center).fma((float)Math.cos(angle)*length,right).fma((float)Math.sin(angle)*length,up);
                    ribbon(out,matrix,eye,center,tip,.17f*flash,255,255,255,(int)(alpha*250));
                }
                ribbon(out,matrix,eye,new Vector3f(-1.8f,.1f,0),new Vector3f(1.8f,.1f,0),.1f*flash,255,154,217,(int)(alpha*200));
            }
            buffers.m_109912_(type);
        } finally {pose.m_85849_();}
        }
    }
    private static void ribbon(VertexConsumer out,Matrix4f matrix,Vector3f eye,Vector3f a,Vector3f b,float width,int r,int g,int blue,int alpha) {
        var side=new Vector3f(b).sub(a).cross(new Vector3f(eye).sub(a));
        if(side.lengthSquared()<.000001f)side.set(1,0,0);side.normalize().mul(width);
        vertex(out,matrix,new Vector3f(a).sub(side),r,g,blue,alpha,0,0);
        vertex(out,matrix,new Vector3f(b).sub(side),r,g,blue,alpha,0,1);
        vertex(out,matrix,new Vector3f(b).add(side),r,g,blue,alpha,1,1);
        vertex(out,matrix,new Vector3f(a).add(side),r,g,blue,alpha,1,0);
    }
    private static void vertex(VertexConsumer out,Matrix4f m,Vector3f p,int r,int g,int b,int a,float u,float v){out.m_252986_(m,p.x,p.y,p.z).m_6122_(r,g,b,a).m_7421_(u,v).m_5752_();}
}
