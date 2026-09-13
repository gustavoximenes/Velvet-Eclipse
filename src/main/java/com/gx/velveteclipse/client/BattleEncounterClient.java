package com.gx.velveteclipse.client;

import com.gx.velveteclipse.network.*;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import java.util.*;

/** The observer sees a dust cloud and can opt into the nearby shared encounter. */
@Mod.EventBusSubscriber(modid="velvet_eclipse",bus=Mod.EventBusSubscriber.Bus.FORGE,value=Dist.CLIENT)
public final class BattleEncounterClient {
    private static List<BattleSceneS2C.Scene> scenes=List.of();
    private static UUID dismissed,shown;
    private static long received,opened;
    public static void accept(BattleSceneS2C packet){
        scenes=packet.scenes();received=System.nanoTime();
        if(dismissed!=null&&scenes.stream().noneMatch(s->s.id().equals(dismissed)&&s.joinable()))dismissed=null;
        var invite=invitation();UUID next=invite==null?null:invite.id();if(!Objects.equals(next,shown)){shown=next;opened=System.nanoTime();}
    }
    private static boolean fresh(){return Minecraft.m_91087_().f_91073_!=null&&(System.nanoTime()-received)<2_000_000_000L;}
    private static BattleSceneS2C.Scene invitation(){return !fresh()||ClientBattleState.snapshot().active()?null:scenes.stream().filter(s->s.joinable()&&!s.id().equals(dismissed)).findFirst().orElse(null);}
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e){scenes=List.of();dismissed=shown=null;BattleTargetHud.reset();LightningStrikeRenderer.reset();BattleTacticsClient.reset();AllOutPresentation.reset();}
    public static boolean hidden(int id){return fresh()&&scenes.stream().anyMatch(s->s.enemies().contains(id));}
    @SubscribeEvent public static void hide(RenderLivingEvent.Pre<?,?> event){if(hidden(event.getEntity().m_19879_()))event.setCanceled(true);}
    @SubscribeEvent public static void key(InputEvent.Key event){
        if(event.getAction()!=1||Minecraft.m_91087_().f_91080_!=null)return;var invite=invitation();if(invite==null)return;
        if(event.getKey()==74){ModNetwork.sendAction("join",invite.id().toString());dismissed=invite.id();}
        if(event.getKey()==78)dismissed=invite.id();
    }
    public static void paint(HudDesign.Canvas c,int w,int h){
        var invite=invitation();if(invite==null)return;
        double t=Math.min(1,(System.nanoTime()-opened)/240_000_000.);t=1-Math.pow(1-t,3);
        int width=Math.min(300,w-24),x=(w-width)/2,y=(int)(h*.69+(1-t)*24);
        BattleGraphics.poly(c,new double[]{x-8,y,x+width,y-8,x+width+8,y+57,x,y+65},0xF1081030);
        BattleGraphics.poly(c,new double[]{x-8,y,x+width,y-8,x+width-2,y-4,x-10,y+4},0xFF53F1FA);
        c.styled("JUNTAR-SE AO COMBATE?",x+12,y+9,1.2f,0xFFF5FFFF);
        String leader=invite.leader();while(c.width(leader)>width-25&&leader.length()>4)leader=leader.substring(0,leader.length()-1);
        c.text(leader,x+12,y+25,0xFF74F4FF);
        c.fill(x+10,y+40,width/2-12,16,0xFFEDFAFF);c.text("[J] ENTRAR",x+18,y+44,0xFF061030);
        c.fill(x+width/2+4,y+40,width/2-14,16,0xFF192A58);c.text("[N] AGORA NÃO",x+width/2+10,y+44,0xFFD6ECFF);
    }
    @SubscribeEvent public static void smoke(RenderLevelStageEvent event){
        if(event.getStage()!=RenderLevelStageEvent.Stage.AFTER_PARTICLES||!fresh()||scenes.isEmpty())return;
        var mc=Minecraft.m_91087_();var camera=event.getCamera().m_90583_();var pose=event.getPoseStack();
        var buffers=mc.m_91269_().m_110104_();var type=RenderType.m_110502_();var out=buffers.m_6299_(type);
        double time=System.nanoTime()*1e-9;
        for(var scene:scenes){
            pose.m_85836_();pose.m_85837_(scene.x()-camera.f_82479_,scene.y()-camera.f_82480_,scene.z()-camera.f_82481_);
            var matrix=pose.m_85850_().m_252922_();var eye=new Vector3f((float)(camera.f_82479_-scene.x()),(float)(camera.f_82480_-scene.y()),(float)(camera.f_82481_-scene.z())).normalize();
            var right=new Vector3f(0,1,0).cross(eye);if(right.lengthSquared()<.001f)right.set(1,0,0);right.normalize();var up=new Vector3f(eye).cross(right).normalize();
            // Dense overlapping puffs cover the complete enemy formation, without filling the world with entities.
            for(int i=0;i<28;i++){
                double a=i*2.399+time*.18;float radius=scene.radius()*(float)Math.sqrt((i%9)/9.0);
                var center=new Vector3f((float)Math.cos(a)*radius,.6f+(i%4)*.4f+(float)Math.sin(time*1.4+i)*.13f,(float)Math.sin(a)*radius*.6f);
                float size=.75f+(i%3)*.19f;int shade=145+i%5*17;
                puff(out,matrix,center,right,up,size,shade,shade+3,shade,220);
            }
            for(int i=0;i<5;i++){
                double a=i*1.256+time*.7;var center=new Vector3f((float)Math.cos(a)*scene.radius(),1.5f+(float)Math.sin(time*2+i)*.6f,(float)Math.sin(a)*scene.radius()*.6f);
                puff(out,matrix,center,right,up,.09f,255,245,176,240);
            }
            pose.m_85849_();
        }
        buffers.m_109912_(type);
    }
    private static void puff(VertexConsumer out,Matrix4f matrix,Vector3f center,Vector3f right,Vector3f up,float radius,int r,int g,int b,int alpha){
        for(int i=0;i<12;i++){
            double a=i*Math.PI*2/12,beta=(i+1)*Math.PI*2/12;
            var p=new Vector3f(center).fma((float)Math.cos(a)*radius,right).fma((float)Math.sin(a)*radius,up);
            var q=new Vector3f(center).fma((float)Math.cos(beta)*radius,right).fma((float)Math.sin(beta)*radius,up);
            vertex(out,matrix,center,r,g,b,alpha);vertex(out,matrix,p,r,g,b,alpha);vertex(out,matrix,q,r,g,b,alpha);vertex(out,matrix,center,r,g,b,alpha);
        }
    }
    private static void vertex(VertexConsumer out,Matrix4f m,Vector3f p,int r,int g,int b,int a){out.m_252986_(m,p.x,p.y,p.z).m_6122_(r,g,b,a).m_5752_();}
}
