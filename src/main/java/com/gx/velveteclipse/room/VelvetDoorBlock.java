package com.gx.velveteclipse.room;
import net.minecraft.core.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;

public final class VelvetDoorBlock extends DoorBlock {
 public VelvetDoorBlock(){super(BlockBehaviour.Properties.m_284310_().m_60913_(3,12).m_60955_().m_60953_(s->10),BlockSetType.f_271132_);}
 @Override public void m_213897_(BlockState state,net.minecraft.server.level.ServerLevel level,BlockPos pos,net.minecraft.util.RandomSource random){
  // Only key-summoned doors schedule this tick. Hall and room exit doors persist.
  if(state.m_61143_(f_52730_)==DoubleBlockHalf.LOWER){level.m_7731_(pos,Blocks.f_50016_.m_49966_(),3);if(level.m_8055_(pos.m_7494_()).m_60713_(this))level.m_7731_(pos.m_7494_(),Blocks.f_50016_.m_49966_(),3);}
 }
 @Override public net.minecraft.world.phys.shapes.VoxelShape m_5940_(BlockState s,BlockGetter l,BlockPos p,net.minecraft.world.phys.shapes.CollisionContext c){
  var box=VelvetDoorGeometry.bounds(s.m_61143_(f_52726_),s.m_61143_(f_52727_),s.m_61143_(f_52728_)==DoorHingeSide.LEFT);
  return Block.m_49796_(box.f_82288_*16,box.f_82289_*16,box.f_82290_*16,box.f_82291_*16,box.f_82292_*16,box.f_82293_*16);
 }
 @Override public InteractionResult m_6227_(BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){
  if(!level.f_46443_)m_153165_(player,level,state,pos,!state.m_61143_(f_52727_));return InteractionResult.SUCCESS;
 }
 @Override public void m_7892_(BlockState state,Level level,BlockPos pos,Entity entity){
  if(entity instanceof ServerPlayer player&&state.m_61143_(f_52727_))VelvetRoomService.cross(player,pos);
 }
}
