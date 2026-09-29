package com.gx.velveteclipse.client.darkhour;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import java.lang.reflect.Field;
import java.nio.*;
import java.io.IOException;

/** Replace only empty-depth sky after OptiFine composition. Terrain, hands and HUD are preserved. */
@Mod.EventBusSubscriber(modid="velvet_eclipse",bus=Mod.EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class DarkHourShaderSky {
 private static ShaderInstance shader;private static Field dfb,depth,projection,view,shadow,ready;private static boolean initialized,failed;
 private static Field field(Class<?> c,String name)throws ReflectiveOperationException{var f=c.getDeclaredField(name);f.setAccessible(true);return f;}
 @SubscribeEvent public static void register(RegisterShadersEvent e)throws IOException{
  e.registerShader(new ShaderInstance(e.getResourceProvider(),new ResourceLocation("velvet_eclipse","dark_hour_sky"),DefaultVertexFormat.f_85817_),s->shader=s);
 }
 public static void render(){
  var mc=Minecraft.m_91087_();if(shader==null||failed||mc.f_91073_==null||!DarkHourClient.active())return;
  try{
   if(!initialized){var c=Class.forName("net.optifine.shaders.Shaders",false,DarkHourShaderSky.class.getClassLoader());dfb=field(c,"dfb");projection=field(c,"projectionInverse");view=field(c,"modelViewInverse");shadow=field(c,"isShadowPass");ready=field(c,"isShaderPackInitialized");depth=field(Class.forName("net.optifine.shaders.ShadersFramebuffer",false,c.getClassLoader()),"depthTextures");initialized=true;}
   if(shadow.getBoolean(null)||!ready.getBoolean(null))return;var framebuffer=dfb.get(null);if(framebuffer==null)return;
   var textures=(IntBuffer)depth.get(framebuffer);if(textures==null||textures.limit()==0||textures.get(0)<=0)return;
   var inverseProjection=new Matrix4f(((FloatBuffer)projection.get(null)).duplicate().position(0));var inverseView=new Matrix4f(((FloatBuffer)view.get(null)).duplicate().position(0));
   draw(textures.get(0),inverseProjection,inverseView,(mc.f_91073_.m_46467_()%120000)/20f);
  }catch(ReflectiveOperationException|RuntimeException ex){failed=true;System.getLogger("Velvet Eclipse").log(System.Logger.Level.WARNING,"Dark Hour shader sky unavailable; keeping the normal sky renderer",ex);}
 }
 private static void draw(int texture,Matrix4f inverseProjection,Matrix4f inverseView,float time){
  var previousShader=RenderSystem.getShader();boolean depthTest=GL11.glIsEnabled(GL11.GL_DEPTH_TEST),blend=GL11.glIsEnabled(GL11.GL_BLEND),cull=GL11.glIsEnabled(GL11.GL_CULL_FACE),depthWrite=GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
  int previousTexture=RenderSystem.getShaderTexture(0);
  try{
   RenderSystem.disableDepthTest();RenderSystem.depthMask(false);RenderSystem.disableCull();RenderSystem.disableBlend();
   shader.m_173350_("SkyDepth",texture);shader.m_173348_("InverseProjection").m_5679_(inverseProjection);shader.m_173348_("InverseView").m_5679_(inverseView);shader.m_173348_("SkyTime").m_5985_(time);
   RenderSystem.setShader(()->shader);var b=Tesselator.m_85913_().m_85915_();b.m_166779_(VertexFormat.Mode.QUADS,DefaultVertexFormat.f_85817_);
   b.m_5483_(-1,-1,0).m_7421_(0,0).m_5752_();b.m_5483_(1,-1,0).m_7421_(1,0).m_5752_();b.m_5483_(1,1,0).m_7421_(1,1).m_5752_();b.m_5483_(-1,1,0).m_7421_(0,1).m_5752_();BufferUploader.m_231202_(b.m_231175_());
  }finally{RenderSystem.setShader(()->previousShader);RenderSystem.setShaderTexture(0,previousTexture);RenderSystem.depthMask(depthWrite);if(depthTest)RenderSystem.enableDepthTest();else RenderSystem.disableDepthTest();if(cull)RenderSystem.enableCull();else RenderSystem.disableCull();if(blend)RenderSystem.enableBlend();else RenderSystem.disableBlend();}
 }
 private DarkHourShaderSky(){}
}
