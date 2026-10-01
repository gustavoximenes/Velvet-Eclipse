package com.gx.velveteclipse.client.room;
import com.gx.velveteclipse.room.VelvetRoomService;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import java.io.IOException;

/** Room-only shadow lift after OptiFine's tone mapping, before HUD/cinematic rendering. */
@Mod.EventBusSubscriber(modid="velvet_eclipse",bus=Mod.EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class VelvetRoomShaderLighting {
 private static ShaderInstance shader;private static int texture,width,height;private static boolean failed;
 @SubscribeEvent public static void shaders(RegisterShadersEvent e)throws IOException{
  e.registerShader(new ShaderInstance(e.getResourceProvider(),new ResourceLocation("velvet_eclipse","velvet_room_light"),DefaultVertexFormat.f_85817_),s->shader=s);
 }
 public static void render(){
  var mc=Minecraft.m_91087_();if(failed||shader==null||mc.f_91074_==null||!VelvetRoomService.inside(mc.f_91074_))return;
  var previous=RenderSystem.getShader();int active=GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE),logicalTexture=RenderSystem.getShaderTexture(0);
  RenderSystem.activeTexture(GL13.GL_TEXTURE0);int binding=GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
  boolean depth=GL11.glIsEnabled(GL11.GL_DEPTH_TEST),blend=GL11.glIsEnabled(GL11.GL_BLEND),cull=GL11.glIsEnabled(GL11.GL_CULL_FACE),write=GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
  try{
   int[] viewport=new int[4];GL11.glGetIntegerv(GL11.GL_VIEWPORT,viewport);if(viewport[2]<=0||viewport[3]<=0)return;
   if(texture==0)texture=GL11.glGenTextures();RenderSystem.bindTexture(texture);
   if(width!=viewport[2]||height!=viewport[3]){width=viewport[2];height=viewport[3];GL11.glTexImage2D(GL11.GL_TEXTURE_2D,0,GL11.GL_RGBA8,width,height,0,GL11.GL_RGBA,GL11.GL_UNSIGNED_BYTE,0L);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MIN_FILTER,GL11.GL_LINEAR);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MAG_FILTER,GL11.GL_LINEAR);}
   GL11.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D,0,0,0,viewport[0],viewport[1],width,height);
   RenderSystem.disableDepthTest();RenderSystem.depthMask(false);RenderSystem.disableCull();RenderSystem.disableBlend();
   shader.m_173350_("RoomColor",texture);RenderSystem.setShader(()->shader);
   var b=Tesselator.m_85913_().m_85915_();b.m_166779_(VertexFormat.Mode.QUADS,DefaultVertexFormat.f_85817_);
   b.m_5483_(-1,-1,0).m_7421_(0,0).m_5752_();b.m_5483_(1,-1,0).m_7421_(1,0).m_5752_();b.m_5483_(1,1,0).m_7421_(1,1).m_5752_();b.m_5483_(-1,1,0).m_7421_(0,1).m_5752_();BufferUploader.m_231202_(b.m_231175_());
  }catch(RuntimeException e){failed=true;com.mojang.logging.LogUtils.getLogger().error("Room shader lighting unavailable; keeping emitted block/model light",e);}
  finally{RenderSystem.setShader(()->previous);RenderSystem.setShaderTexture(0,logicalTexture);RenderSystem.activeTexture(GL13.GL_TEXTURE0);RenderSystem.bindTexture(binding);RenderSystem.activeTexture(active);RenderSystem.depthMask(write);if(depth)RenderSystem.enableDepthTest();else RenderSystem.disableDepthTest();if(blend)RenderSystem.enableBlend();else RenderSystem.disableBlend();if(cull)RenderSystem.enableCull();else RenderSystem.disableCull();}
 }
 private VelvetRoomShaderLighting(){}
}
