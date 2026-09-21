package com.gx.velveteclipse.client.exploration;
import com.gx.velveteclipse.exploration.ExplorationNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Quaternionf;
import java.util.*;
/** The server sends one finite flight; rendering never deals damage or starts encounters. */
@Mod.EventBusSubscriber(modid="velvet_eclipse",value=Dist.CLIENT)
public final class ArrowFlight {
 private record Shot(Vec3 start,Vec3 end,double began,double seconds,Object world){}
 private static final List<Shot> shots=new ArrayList<>();
 public static void accept(ExplorationNetwork.Shot s){var mc=Minecraft.m_91087_();if(mc.f_91073_==null)return;shots.add(new Shot(new Vec3(s.sx(),s.sy(),s.sz()),new Vec3(s.ex(),s.ey(),s.ez()),System.nanoTime()*1e-9,s.ticks()/20.,mc.f_91073_));if(shots.size()>64)shots.remove(0);}
 @SubscribeEvent public static void render(RenderLevelStageEvent e){if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_ENTITIES)return;var mc=Minecraft.m_91087_();double now=System.nanoTime()*1e-9;shots.removeIf(s->s.world()!=mc.f_91073_||now-s.began()>s.seconds()+.06);if(shots.isEmpty())return;var camera=e.getCamera().m_90583_();var pose=e.getPoseStack();var buffers=mc.m_91269_().m_110104_();for(var s:shots){double t=Math.min(1,(now-s.began())/s.seconds());var pos=s.start().m_165921_(s.end(),t);var d=s.end().m_82546_(s.start()).m_82541_();pose.m_85836_();pose.m_85837_(pos.f_82479_-camera.f_82479_,pos.f_82480_-camera.f_82480_,pos.f_82481_-camera.f_82481_);pose.m_252781_(new Quaternionf().rotationTo(-1,0,0,(float)d.f_82479_,(float)d.f_82480_,(float)d.f_82481_));WeaponModels.arrow(pose,buffers,15728880);pose.m_85849_();}buffers.m_109911_();}
}
