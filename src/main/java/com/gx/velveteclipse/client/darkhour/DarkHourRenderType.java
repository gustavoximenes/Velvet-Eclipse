package com.gx.velveteclipse.client.darkhour;
import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.*;
/** Depth-tested opaque coffins and alpha-blended decals, with no lightning additive blending. */
final class DarkHourRenderType extends RenderType {
 static final RenderType SOLID=new DarkHourRenderType(true),DECAL=new DarkHourRenderType(false);
 private DarkHourRenderType(boolean solid){super("velvet_eclipse:dark_hour_"+solid,DefaultVertexFormat.f_85815_,VertexFormat.Mode.QUADS,32768,false,!solid,()->{
  RenderSystem.setShader(GameRenderer::m_172811_);RenderSystem.enableDepthTest();RenderSystem.depthMask(solid);RenderSystem.disableCull();RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();
 },()->{RenderSystem.depthMask(true);RenderSystem.enableCull();RenderSystem.disableBlend();});}
}
