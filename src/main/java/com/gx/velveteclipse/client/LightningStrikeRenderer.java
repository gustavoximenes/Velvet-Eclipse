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

/** Bright layered ribbons use the vanilla lightning shader; no damaging lightning entity is spawned. */
@Mod.EventBusSubscriber(modid="velvet_eclipse",bus=Mod.EventBusSubscriber.Bus.FORGE,value=Dist.CLIENT)
public final class LightningStrikeRenderer {
    @SubscribeEvent public static void render(RenderLevelStageEvent event) {
        if(event.getStage()!=RenderLevelStageEvent.Stage.AFTER_PARTICLES||!PersonaCinematics.skillSequenceActive())return;
        var skill=PersonaRegistry.skill(PersonaCinematics.activeSkill()).orElse(null);
        if(skill==null||skill.element()!=Element.ELECTRIC)return;
        double tick=PersonaCinematics.skillTicks()+event.getPartialTick();double alpha=LightningStrike.opacity(tick);
        if(alpha<=0)return;
        var mc=Minecraft.m_91087_();if(mc.f_91073_==null)return;
        var target=mc.f_91073_.m_6815_(BattleTargetHud.entityId());if(target==null)return;
        var p=target.m_20318_(event.getPartialTick());var camera=event.getCamera().m_90583_();
        var pose=event.getPoseStack();pose.m_85836_();
        var buffers=mc.m_91269_().m_110104_();var type=RenderType.m_110502_();
        try {
            pose.m_85837_(p.f_82479_-camera.f_82479_,p.f_82480_-camera.f_82480_,p.f_82481_-camera.f_82481_);
            var out=buffers.m_6299_(type);var matrix=pose.m_85850_().m_252922_();
            var eye=new Vector3f((float)(camera.f_82479_-p.f_82479_),(float)(camera.f_82480_-p.f_82480_),(float)(camera.f_82481_-p.f_82481_));
            float bottom=(float)(6.2*(1-Math.min(1,(tick-82)/4)));
            for(var segment:LightningStrike.bolts(target.m_19879_()*31L+(int)Math.max(0,(tick-86)/2))) {
                var a=segment.from();var b=segment.to();if(a.y<bottom)continue;
                if(b.y<bottom)b=new Vector3f(a).lerp(b,(a.y-bottom)/(a.y-b.y));
                ribbon(out,matrix,eye,a,b,.11f,182,126,255,(int)(alpha*85));
                ribbon(out,matrix,eye,a,b,.028f,255,244,255,(int)(alpha*250));
            }
            if(tick>=86) {
                float expand=(float)(tick-86);var center=new Vector3f(0,.22f,0);
                for(int i=0;i<14;i++) {
                    double angle=i*Math.PI*2/14;
                    float length=(i%3==0?1.35f:.8f)*(1+expand*.08f);
                    var end=new Vector3f((float)Math.cos(angle)*length,.2f+(float)Math.abs(Math.sin(angle))*length,(float)Math.sin(angle)*length*.55f);
                    ribbon(out,matrix,eye,center,end,.095f,255,142,229,(int)(alpha*130));
                    ribbon(out,matrix,eye,center,end,.035f,255,255,255,(int)(alpha*255));
                }
            }
            buffers.m_109912_(type);
        } finally {pose.m_85849_();}
    }
    private static void ribbon(VertexConsumer out,Matrix4f matrix,Vector3f eye,Vector3f a,Vector3f b,float width,int r,int g,int blue,int alpha) {
        var side=new Vector3f(b).sub(a).cross(new Vector3f(eye).sub(a));
        if(side.lengthSquared()<.000001f)side.set(1,0,0);side.normalize().mul(width);
        vertex(out,matrix,new Vector3f(a).sub(side),r,g,blue,alpha);
        vertex(out,matrix,new Vector3f(b).sub(side),r,g,blue,alpha);
        vertex(out,matrix,new Vector3f(b).add(side),r,g,blue,alpha);
        vertex(out,matrix,new Vector3f(a).add(side),r,g,blue,alpha);
    }
    private static void vertex(VertexConsumer out,Matrix4f m,Vector3f p,int r,int g,int b,int a){out.m_252986_(m,p.x,p.y,p.z).m_6122_(r,g,b,a).m_5752_();}
}
