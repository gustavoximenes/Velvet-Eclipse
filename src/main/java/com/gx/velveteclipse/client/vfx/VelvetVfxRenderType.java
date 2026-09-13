package com.gx.velveteclipse.client.vfx;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.io.IOException;

/** Velvet's own emissive shader and feathered mesh pipeline; no Minecraft particles/textures. */
@Mod.EventBusSubscriber(modid="velvet_eclipse",bus=Mod.EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class VelvetVfxRenderType extends RenderType {
 public static final VertexFormat FORMAT=new VertexFormat(ImmutableMap.of(
  "Position",new VertexFormatElement(0,VertexFormatElement.Type.FLOAT,VertexFormatElement.Usage.POSITION,3),
  "Color",new VertexFormatElement(0,VertexFormatElement.Type.UBYTE,VertexFormatElement.Usage.COLOR,4),
  "UV0",new VertexFormatElement(0,VertexFormatElement.Type.FLOAT,VertexFormatElement.Usage.UV,2)));
 private static ShaderInstance shader;
 public static final RenderType GLOW=new VelvetVfxRenderType();
 private VelvetVfxRenderType(){super("velvet_eclipse:spell_glow",FORMAT,VertexFormat.Mode.QUADS,32768,false,true,()->{
  RenderSystem.setShader(()->shader);RenderSystem.enableBlend();RenderSystem.blendFuncSeparate(770,1,1,0);RenderSystem.enableDepthTest();RenderSystem.depthMask(false);RenderSystem.disableCull();
 },()->{RenderSystem.depthMask(true);RenderSystem.enableCull();RenderSystem.defaultBlendFunc();RenderSystem.disableBlend();});}
 public static boolean ready(){return shader!=null;}
 @SubscribeEvent public static void register(RegisterShadersEvent event)throws IOException{
  event.registerShader(new ShaderInstance(event.getResourceProvider(),new ResourceLocation("velvet_eclipse","spell_glow"),FORMAT),loaded->shader=loaded);
 }
}
