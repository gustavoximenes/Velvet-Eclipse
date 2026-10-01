package com.gx.velveteclipse.client.room;
import com.gx.velveteclipse.room.VelvetRoomService;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import java.io.*;
import java.util.zip.GZIPInputStream;

/** The same authored elevator geometry appears in the physical room and its menus. */
@Mod.EventBusSubscriber(modid="velvet_eclipse",value=Dist.CLIENT)
public final class VelvetRoomArchitecture {
 private static final ResourceLocation TEXTURE=new ResourceLocation("velvet_eclipse","textures/entity/velvet_room_palette.png");
 private static float[] mesh;private static boolean failed;
 private static float[] mesh(){
  if(mesh!=null||failed)return mesh;
  try(var raw=VelvetRoomArchitecture.class.getResourceAsStream("/assets/velvet_eclipse/room/architecture.bin.gz")){
   if(raw==null)throw new IOException("Room architecture missing");
   try(var in=new DataInputStream(new GZIPInputStream(raw))){var bytes=in.readAllBytes();if(bytes.length%80!=0||bytes.length>8_000_000)throw new IOException("Invalid room architecture");mesh=new float[bytes.length/4];var b=java.nio.ByteBuffer.wrap(bytes);for(int i=0;i<mesh.length;i++)mesh[i]=b.getFloat();}
  }catch(IOException e){failed=true;com.mojang.logging.LogUtils.getLogger().error("Velvet Room architecture failed to load",e);}return mesh;
 }
 @SubscribeEvent public static void render(RenderLevelStageEvent event){
  if(event.getStage()!=RenderLevelStageEvent.Stage.AFTER_PARTICLES)return;var mc=Minecraft.m_91087_();if(mc.f_91074_==null||!VelvetRoomService.inside(mc.f_91074_))return;var v=mesh();if(v==null)return;
  var eye=event.getCamera().m_90583_();var pose=event.getPoseStack();pose.m_85836_();
  try{
   pose.m_85837_(.5-eye.f_82479_,64.016-eye.f_82480_,-1.37-eye.f_82481_);pose.m_252781_(new org.joml.Quaternionf().rotationY((float)Math.PI));
   var type=net.minecraftforge.client.ForgeRenderTypes.getUnlitTranslucent(TEXTURE);var buffers=mc.m_91269_().m_110104_();var out=buffers.m_6299_(type);var matrix=pose.m_85850_().m_252922_();
   for(int i=0;i<v.length;i+=5)out.m_252986_(matrix,v[i],v[i+1],v[i+2]).m_6122_(255,255,255,255).m_7421_(v[i+3],v[i+4]).m_86008_(net.minecraft.client.renderer.texture.OverlayTexture.f_118083_).m_85969_(15728880).m_5601_(0,1,0).m_5752_();
   buffers.m_109912_(type);
  }finally{pose.m_85849_();}
 }
}
