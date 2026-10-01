package com.gx.velveteclipse.client.vfx;
import com.gx.velveteclipse.client.presentation.BattleDirector;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.joml.Vector3f;

/** Driven by the same synchronized cue on the acting and observing clients. */
@Mod.EventBusSubscriber(modid="velvet_eclipse",value=Dist.CLIENT)
public final class SharedSummonRenderer {
 private static final ResourceLocation WHITE=new ResourceLocation("velvet_eclipse","textures/vfx/solid_white.png");
 @SubscribeEvent public static void render(RenderLevelStageEvent event){
  if(event.getStage()!=RenderLevelStageEvent.Stage.AFTER_PARTICLES)return;var actor=BattleDirector.summonActor();if(actor==null)return;
  double tick=BattleDirector.age(event.getPartialTick());if(SharedSummonFlames.life(tick)<=0)return;
  var mc=Minecraft.m_91087_();var camera=event.getCamera().m_90583_();var at=actor.m_20318_(event.getPartialTick());double yaw=Math.toRadians(BattleDirector.castingYaw(actor.m_20148_(),actor.m_146908_()));
  var back=new Vector3f((float)Math.sin(yaw),0,(float)-Math.cos(yaw));var right=new Vector3f(1,0,0).rotate(event.getCamera().m_253121_());right.y=0;if(right.lengthSquared()<.001)right.set(1,0,0);right.normalize();
  var pose=event.getPoseStack();pose.m_85836_();
  try{
   pose.m_85837_(at.f_82479_+back.x*SharedSummonFlames.SEPARATION/2-camera.f_82479_,at.f_82480_-camera.f_82480_,at.f_82481_+back.z*SharedSummonFlames.SEPARATION/2-camera.f_82481_);
   var buffers=mc.m_91269_().m_110104_();var type=net.minecraftforge.client.ForgeRenderTypes.getUnlitTranslucent(WHITE);var out=buffers.m_6299_(type);var matrix=pose.m_85850_().m_252922_();
   for(var face:SharedSummonFlames.geometry(tick,right,back))for(var p:face.vertices())out.m_252986_(matrix,p.x,p.y,p.z).m_6122_(face.color()>>16&255,face.color()>>8&255,face.color()&255,(int)(face.alpha()*255)).m_7421_(.5f,.5f).m_86008_(net.minecraft.client.renderer.texture.OverlayTexture.f_118083_).m_85969_(15728880).m_5601_(0,1,0).m_5752_();
   buffers.m_109912_(type);
  }finally{pose.m_85849_();}
 }
}
