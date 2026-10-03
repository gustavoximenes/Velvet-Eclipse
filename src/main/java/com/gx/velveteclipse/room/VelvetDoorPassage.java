package com.gx.velveteclipse.room;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** The widened double-door aperture shares the original door's server-side routing. */
@Mod.EventBusSubscriber(modid="velvet_eclipse")
public final class VelvetDoorPassage {
 public static boolean inOpening(Direction facing,double x,double y,double z){
  if(y<-.1||y>4.2)return false;
  double along,across;switch(facing){case EAST->{along=x-.875;across=z-.5;}case SOUTH->{along=z-.875;across=x-.5;}case WEST->{along=x-.125;across=z-.5;}default->{along=z-.125;across=x-.5;}}
  return Math.abs(along)<.32&&Math.abs(across)<1.58;
 }
 @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent event){
  if(event.phase!=TickEvent.Phase.END||!(event.player instanceof ServerPlayer p)||p.f_19797_%2!=0||p.m_20159_())return;
  var world=p.m_284548_();var center=p.m_20183_();
  for(int dy=0;dy>=-3;dy--)for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++){
   BlockPos at=center.m_7918_(x,dy,z);var s=world.m_8055_(at);
   if(s.m_60734_() instanceof VelvetDoorBlock&&s.m_61143_(DoorBlock.f_52730_)==DoubleBlockHalf.LOWER&&s.m_61143_(DoorBlock.f_52727_)&&inOpening(s.m_61143_(DoorBlock.f_52726_),p.m_20185_()-at.m_123341_(),p.m_20186_()-at.m_123342_(),p.m_20189_()-at.m_123343_())){VelvetRoomService.cross(p,at);return;}
  }
 }
 private VelvetDoorPassage(){}
}
