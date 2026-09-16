package com.gx.velveteclipse.client;
import com.gx.velveteclipse.room.*;
import com.gx.velveteclipse.client.exploration.ExplorationCamera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.*;

public final class VelvetRoomClient {
 public static boolean seated(){var p=Minecraft.m_91087_().f_91074_;return p!=null&&VelvetRoomService.inside(p)&&p.m_20202_() instanceof VelvetSeatEntity;}
 public static void camera(ViewportEvent.ComputeCameraAngles e){var p=Minecraft.m_91087_().f_91074_;if(!seated())return;var at=p.m_20182_();ExplorationCamera.place(e,ExplorationCamera.collide(at.m_82520_(0,1.2,0),at.m_82520_(2.6,1.85,2.5)),at.m_82520_(-.8,1,-3),0);}
 @Mod.EventBusSubscriber(modid="velvet_eclipse",bus=Mod.EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
 public static final class Registration {
  @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){e.registerEntityRenderer(VelvetRoomRegistry.SEAT.get(),InvisibleSeat::new);e.registerEntityRenderer(VelvetRoomRegistry.IGOR.get(),c->new com.gx.velveteclipse.client.render.VelvetGeoEntityRenderer<IgorEntity>(c,"igor"));}
 }
 private static final class InvisibleSeat extends EntityRenderer<VelvetSeatEntity> {
  InvisibleSeat(EntityRendererProvider.Context context){super(context);}
  @Override public ResourceLocation m_5478_(VelvetSeatEntity e){return new ResourceLocation("velvet_eclipse","textures/block/velvet_floor.png");}
  @Override public void m_7392_(VelvetSeatEntity e,float yaw,float partial,com.mojang.blaze3d.vertex.PoseStack pose,net.minecraft.client.renderer.MultiBufferSource out,int light){}
 }
}
