package com.gx.velveteclipse.client.exploration;
import com.gx.velveteclipse.exploration.*;
import com.gx.velveteclipse.client.*;
import com.gx.velveteclipse.client.vfx.VelvetVfxRenderType;
import com.gx.velveteclipse.equipment.EquipmentCatalog.Family;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

/** A world-space aim marker avoids the parallax error of a centered third-person crosshair. */
@Mod.EventBusSubscriber(modid="velvet_eclipse",value=Dist.CLIENT)
public final class BowReticle {
 @SubscribeEvent public static void render(RenderLevelStageEvent e){
  if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_PARTICLES)return;var mc=Minecraft.m_91087_();
  if(mc.f_91074_==null||mc.f_91080_!=null||ClientBattleState.snapshot().active()||!ExplorationClient.enabled()||ExplorationClient.entry()||com.gx.velveteclipse.client.equipment.EquipmentScreen.family()!=Family.BOW)return;
  var shot=BowAim.trace(mc.f_91074_,q->com.gx.velveteclipse.forge.ShadowRegistry.isShadow(q)&&!q.m_19880_().contains("velvet_battle_enemy"));
  var camera=e.getCamera().m_90583_();var buffers=mc.m_91269_().m_110104_();var type=VelvetVfxRenderType.type();var out=buffers.m_6299_(type);var matrix=e.getPoseStack().m_85850_().m_252922_();
  int color=shot.target()==null?0x92EAFF:0x81FFB5;var eye=camera.m_82546_(shot.end()).m_82541_();var right=new Vec3(eye.f_82481_,0,-eye.f_82479_);if(right.m_82556_()<.001)right=new Vec3(1,0,0);right=right.m_82541_();var up=eye.m_82537_(right).m_82541_();
  var at=shot.end().m_82549_(eye.m_82490_(.035));double size=.07+camera.m_82554_(at)*.009;
  for(int i=0;i<4;i++){double a=i*Math.PI/2,b=(i+1)*Math.PI/2;line(out,matrix,camera,at.m_82549_(right.m_82490_(Math.cos(a)*size)).m_82549_(up.m_82490_(Math.sin(a)*size)),at.m_82549_(right.m_82490_(Math.cos(b)*size)).m_82549_(up.m_82490_(Math.sin(b)*size)),.014,color,230);}
  for(int i=2;i<24;i++)line(out,matrix,camera,shot.start().m_165921_(shot.end(),i/24.),shot.start().m_165921_(shot.end(),(i+.4)/24.),.009,color,130);
  buffers.m_109912_(type);
 }
 private static void line(com.mojang.blaze3d.vertex.VertexConsumer out,org.joml.Matrix4f matrix,Vec3 camera,Vec3 a,Vec3 b,double width,int color,int alpha){
  var side=b.m_82546_(a).m_82537_(camera.m_82546_(a)).m_82541_().m_82490_(width);
  var points=new Vec3[]{a.m_82546_(side),a.m_82549_(side),b.m_82549_(side),b.m_82546_(side)};
  for(int i=0;i<4;i++){var p=points[i].m_82546_(camera);VelvetVfxRenderType.vertex(out,matrix,new Vector3f((float)p.f_82479_,(float)p.f_82480_,(float)p.f_82481_),color>>16&255,color>>8&255,color&255,alpha,i==0||i==3?0:1,i<2?0:1);}
 }
 private BowReticle(){}
}
