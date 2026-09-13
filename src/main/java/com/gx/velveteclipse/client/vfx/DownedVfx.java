package com.gx.velveteclipse.client.vfx;
import com.gx.velveteclipse.client.BattleTacticsClient;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@Mod.EventBusSubscriber(modid="velvet_eclipse",bus=Mod.EventBusSubscriber.Bus.FORGE,value=Dist.CLIENT)
public final class DownedVfx {
 @SubscribeEvent public static void render(RenderLevelStageEvent event){
  if(event.getStage()!=RenderLevelStageEvent.Stage.AFTER_PARTICLES||!VelvetVfxRenderType.ready())return;
  var mc=Minecraft.m_91087_();if(mc.f_91073_==null)return;var camera=event.getCamera().m_90583_();var buffers=mc.m_91269_().m_110104_();var out=buffers.m_6299_(VelvetVfxRenderType.GLOW);
  double tick=mc.f_91073_.m_46467_()+event.getPartialTick();var pose=event.getPoseStack();
  for(var down:BattleTacticsClient.downed()){
   var entity=mc.f_91073_.m_6815_(down.entityId());if(entity==null)continue;var p=entity.m_20318_(event.getPartialTick());pose.m_85836_();
   pose.m_85837_(p.f_82479_-camera.f_82479_,p.f_82480_+1.12-camera.f_82480_,p.f_82481_-camera.f_82481_);var matrix=pose.m_85850_().m_252922_();
   var eye=new Vector3f((float)(camera.f_82479_-p.f_82479_),(float)(camera.f_82480_-p.f_82480_-1.12),(float)(camera.f_82481_-p.f_82481_));
   var right=new Vector3f(0,1,0).cross(eye);if(right.lengthSquared()<.001f)right.set(1,0,0);right.normalize();var up=new Vector3f(eye).normalize().cross(right).normalize();
   for(int i=0;i<5;i++){
    double angle=tick*(down.dizzy()?.095:.06)+i*Math.PI*2/5;var center=new Vector3f((float)Math.cos(angle)*.65f,(float)Math.sin(angle*2)*.04f,(float)Math.sin(angle)*.45f);
    float size=down.dizzy()?.17f:.13f;
    for(int j=0;j<10;j++){
     var a=point(center,right,up,j,size);var b=point(center,right,up,j+1,size);
     vertex(out,matrix,center,255,238,52,.5f);vertex(out,matrix,a,255,244,94,.2f);vertex(out,matrix,b,255,244,94,.8f);vertex(out,matrix,center,255,238,52,.5f);
    }
   }
   for(int i=0;i<36;i++){
    double a=i*Math.PI/18;var c=new Vector3f((float)Math.cos(a)*.65f,0,(float)Math.sin(a)*.45f);float size=.016f;
    vertex(out,matrix,new Vector3f(c).fma(-size,right).fma(-size,up),255,232,82,0);vertex(out,matrix,new Vector3f(c).fma(size,right).fma(-size,up),255,232,82,1);vertex(out,matrix,new Vector3f(c).fma(size,right).fma(size,up),255,232,82,1);vertex(out,matrix,new Vector3f(c).fma(-size,right).fma(size,up),255,232,82,0);
   }
   pose.m_85849_();
  }
  buffers.m_109912_(VelvetVfxRenderType.GLOW);
 }
 private static Vector3f point(Vector3f c,Vector3f r,Vector3f u,int i,float size){double a=i*Math.PI/5-Math.PI/2;float s=i%2==0?size:size*.42f;return new Vector3f(c).fma((float)Math.cos(a)*s,r).fma((float)Math.sin(a)*s,u);}
 private static void vertex(VertexConsumer out,Matrix4f m,Vector3f p,int r,int g,int b,float u){out.m_252986_(m,p.x,p.y,p.z).m_6122_(r,g,b,245).m_7421_(u,.5f).m_5752_();}
}
