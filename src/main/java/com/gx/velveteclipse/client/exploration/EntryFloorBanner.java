package com.gx.velveteclipse.client.exploration;
import com.gx.velveteclipse.core.battle.EncounterOpening;
import com.gx.velveteclipse.exploration.EntryTimeline;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
/** Ground-space decal over the real arena; private to the participating client's entry. */
@Mod.EventBusSubscriber(modid="velvet_eclipse",value=Dist.CLIENT)
public final class EntryFloorBanner {
 @SubscribeEvent public static void render(RenderLevelStageEvent e){
  if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_ENTITIES||!EntryTransition.visible())return;
  var mc=Minecraft.m_91087_();if(mc.f_91074_==null)return;
  double opacity=EntryTimeline.banner(EntryTransition.opening(),EntryTransition.time(e.getPartialTick()));if(opacity<=0)return;
  boolean pt=com.gx.velveteclipse.localization.DisplayText.portuguese();
  String name=EntryTransition.opening()==EncounterOpening.AMBUSH?"shadow":"sees";
  var texture=new ResourceLocation("velvet_eclipse","textures/gui/entry/"+name+(pt?"_pt":"_en")+".png");
  var buffers=mc.m_91269_().m_110104_();var out=buffers.m_6299_(RenderType.m_110473_(texture));
  var p=mc.f_91074_;var eye=e.getCamera().m_90583_();double yaw=Math.toRadians(p.m_146908_()),cs=Math.cos(yaw),sn=Math.sin(yaw);
  double cx=p.m_20185_()-sn*1.2,cy=p.m_20186_()+.045,cz=p.m_20189_()+cs*1.2;
  var matrix=e.getPoseStack().m_85850_().m_252922_();
  for(double[] v:new double[][]{{-3.4,-.85,0,1},{-3.4,.85,0,0},{3.4,.85,1,0},{3.4,-.85,1,1}}){
   out.m_252986_(matrix,(float)(cx+cs*v[0]-sn*v[1]-eye.f_82479_),(float)(cy-eye.f_82480_),(float)(cz+sn*v[0]+cs*v[1]-eye.f_82481_))
      .m_6122_(255,255,255,(int)(opacity*255)).m_7421_((float)v[2],(float)v[3]).m_7122_(0,10).m_7120_(240,240).m_5601_(0,1,0).m_5752_();
  }
  buffers.m_109911_();
 }
 private EntryFloorBanner(){}
}
