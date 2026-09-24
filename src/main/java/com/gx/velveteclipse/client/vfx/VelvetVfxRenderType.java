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
 private static final RenderType COMPAT=RenderType.m_234335_(new ResourceLocation("velvet_eclipse","textures/vfx/beam_glow.png"),false);
 private static final boolean OPTIFINE=optifine();
 private static boolean optifine(){try{Class.forName("net.optifine.Config",false,VelvetVfxRenderType.class.getClassLoader());return true;}catch(ClassNotFoundException e){try{Class.forName("net.optifine.shaders.Shaders",false,VelvetVfxRenderType.class.getClassLoader());return true;}catch(ClassNotFoundException ignored){return false;}}}
 public static boolean compatibility(){return OPTIFINE||shader==null;}
 public static RenderType type(){return compatibility()?COMPAT:GLOW;}
 public static void vertex(VertexConsumer out,org.joml.Matrix4f matrix,org.joml.Vector3f p,int r,int g,int b,int a,float u,float v){
  var vertex=out.m_252986_(matrix,p.x,p.y,p.z).m_6122_(r,g,b,a).m_7421_(u,v);
  if(compatibility())vertex.m_86008_(net.minecraft.client.renderer.texture.OverlayTexture.f_118083_).m_85969_(15728880).m_5601_(0,1,0);
  vertex.m_5752_();
 }
 private VelvetVfxRenderType(){super("velvet_eclipse:spell_glow",FORMAT,VertexFormat.Mode.QUADS,32768,false,true,()->{
  RenderSystem.setShader(()->shader);RenderSystem.enableBlend();RenderSystem.blendFuncSeparate(770,1,1,0);RenderSystem.enableDepthTest();RenderSystem.depthMask(false);RenderSystem.disableCull();
 },()->{RenderSystem.depthMask(true);RenderSystem.enableCull();RenderSystem.defaultBlendFunc();RenderSystem.disableBlend();});}
 public static boolean ready(){return shader!=null;}
 @SubscribeEvent public static void register(RegisterShadersEvent event)throws IOException{
  event.registerShader(new ShaderInstance(event.getResourceProvider(),new ResourceLocation("velvet_eclipse","spell_glow"),FORMAT),loaded->shader=loaded);
 }
}
