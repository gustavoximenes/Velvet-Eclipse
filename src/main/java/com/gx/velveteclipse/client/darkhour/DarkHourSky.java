package com.gx.velveteclipse.client.darkhour;
import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

@Mod.EventBusSubscriber(modid="velvet_eclipse",bus=Mod.EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class DarkHourSky extends DimensionSpecialEffects.OverworldEffects {
 @Override public boolean renderClouds(ClientLevel l,int ticks,float partial,PoseStack p,double x,double y,double z,Matrix4f projection){return DarkHourClient.active();}
 @Override public boolean renderSnowAndRain(ClientLevel l,int ticks,float partial,LightTexture light,double x,double y,double z){return DarkHourClient.active();}
 @Override public boolean tickRain(ClientLevel l,int ticks,Camera camera){return DarkHourClient.active();}
 @Override public void adjustLightmapColors(ClientLevel l,float partial,float sky,float block,float gamma,int skyLight,int blockLight,org.joml.Vector3f color){if(DarkHourClient.active())color.set(Math.max(.10f,color.x*.65f),Math.max(.26f,color.y*.92f),Math.max(.14f,color.z*.68f));}
 @SubscribeEvent public static void register(RegisterDimensionSpecialEffectsEvent e){e.register(new ResourceLocation("minecraft","overworld"),new DarkHourSky());}
 @Override public boolean renderSky(ClientLevel level,int ticks,float partial,PoseStack pose,Camera camera,Matrix4f projection,boolean foggy,Runnable fog){
  if(!DarkHourClient.active())return false;fog.run();
  RenderSystem.disableDepthTest();RenderSystem.depthMask(false);RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();RenderSystem.disableCull();RenderSystem.setShader(GameRenderer::m_172811_);
  var b=Tesselator.m_85913_().m_85915_();var m=pose.m_85850_().m_252922_();
  try{
   b.m_166779_(VertexFormat.Mode.QUADS,DefaultVertexFormat.f_85815_);
   // Inward sky sphere with drifting emerald cloud banks, not a flat screen filter.
   double time=(level.m_46467_()+partial)*.003;
   for(int lat=0;lat<24;lat++)for(int lon=0;lon<64;lon++){
    double a=-Math.PI/2+lat*Math.PI/24,c=a+Math.PI/24,u=lon*Math.PI/32,v=u+Math.PI/32;
    double cloud=Math.pow(Math.max(0,Math.sin(lon*.47+Math.sin(lat*.62)+time)+Math.sin(lat*.9-lon*.21-time*.5))*.5,2);
    int green=(int)(26+cloud*59+Math.max(0,Math.sin(a))*12),red=(int)(5+cloud*16),blue=(int)(14+cloud*27),color=0xFF000000|(red<<16)|(green<<8)|blue;
    point(b,m,a,u,color);point(b,m,a,v,color);point(b,m,c,v,color);point(b,m,c,u,color);
   }
   // Moon plane in the southern sky, surrounded by layered translucent halos.
   pose.m_85836_();pose.m_252781_(new org.joml.Quaternionf().rotationX((float)Math.toRadians(27)));var moon=pose.m_85850_().m_252922_();
   for(int layer=9;layer>=0;layer--){float r=13+layer*1.7f;int color=layer==0?0xFFF0F78A:((9-layer)*2+5)<<24|0xB5EF54;
    for(int i=0;i<96;i++){double a=i*Math.PI/48,c=(i+1)*Math.PI/48;DarkHourClient.quad(b,moon,0,25,-80,(float)Math.cos(a)*r,25+(float)Math.sin(a)*r,-80,(float)Math.cos(c)*r,25+(float)Math.sin(c)*r,-80,0,25,-80,color);}
   }
   for(int i=0;i<26;i++){double a=i*2.399;float r=(float)Math.sqrt(i/26.)*11,x=(float)Math.cos(a)*r,y=25+(float)Math.sin(a)*r,size=1.1f+i%3*.5f;DarkHourClient.quad(b,moon,x-size,y-size,-79.9f,x+size,y-size,-79.9f,x+size,y+size,-79.9f,x-size,y+size,-79.9f,0x25788030);}
   pose.m_85849_();BufferUploader.m_231202_(b.m_231175_());
  }finally{RenderSystem.enableCull();RenderSystem.disableBlend();RenderSystem.depthMask(true);RenderSystem.enableDepthTest();}
  return true;
 }
 private static void point(VertexConsumer b,Matrix4f m,double lat,double lon,int color){DarkHourClient.vertex(b,m,(float)(Math.cos(lat)*Math.cos(lon)*180),(float)(Math.sin(lat)*180),(float)(Math.cos(lat)*Math.sin(lon)*180),color);}
}
