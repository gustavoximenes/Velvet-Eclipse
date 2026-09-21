package com.gx.velveteclipse.client.equipment;
import com.gx.velveteclipse.equipment.*;
import com.gx.velveteclipse.client.menu.MenuDepth;
import com.gx.velveteclipse.room.*;
import com.gx.velveteclipse.client.render.VelvetGeoEntityRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.joml.Quaternionf;
import org.joml.Matrix4f;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
/** Uses the registered model and texture, never a replacement portrait image. */
public final class ElizabethPreview {
 private static final float PREVIEW_YAW=180f;
 private ElizabethEntity entity;private VelvetGeoEntityRenderer<ElizabethEntity> renderer;private boolean failed;
 public void draw(GuiGraphics g,double seconds){
  if(failed)return;var mc=Minecraft.m_91087_();if(mc.f_91073_==null)return;
  var dispatcher=mc.m_91290_();var camera=new Quaternionf(dispatcher.m_253208_());var pose=g.m_280168_();
  g.m_280262_();pose.m_85836_();
  try{
   if(entity==null){entity=new ElizabethEntity(EquipmentRegistry.ELIZABETH.get(),mc.f_91073_);var context=new EntityRendererProvider.Context(dispatcher,mc.m_91291_(),mc.m_91289_(),dispatcher.m_234586_(),mc.m_91098_(),mc.m_167973_(),mc.f_91062_);renderer=new VelvetGeoEntityRenderer<>(context,"elizabeth");}
   // GeckoLib 4.8.4 actuallyRender uses bodyYaw=0 for non-LivingEntity actors.
   // Entity yaw alone is ignored for Igor; rotate the preview matrix explicitly.
   entity.f_19797_=(int)(seconds*20);entity.m_146922_(PREVIEW_YAW);entity.f_19859_=PREVIEW_YAW;
   pose.m_85837_(171,460,MenuDepth.MODEL);pose.m_252931_(new Matrix4f().scaling(145,145,-145));
   var tilt=new Quaternionf().rotationX(.035f);pose.m_252781_(new Quaternionf().rotationZ((float)java.lang.Math.PI).mul(tilt));

   Lighting.m_166384_();dispatcher.m_252923_(new Quaternionf(tilt).conjugate());
   RenderSystem.runAsFancy(()->renderer.m_7392_(entity,PREVIEW_YAW,mc.m_91296_(),pose,g.m_280091_(),15728880));
  }catch(RuntimeException|LinkageError e){failed=true;com.gx.velveteclipse.VelvetEclipseMod.LOGGER.error("Igor fusion portrait",e);}
  finally{try{g.m_280262_();}finally{dispatcher.m_252923_(camera);pose.m_85849_();Lighting.m_84931_();}}
 }
}
