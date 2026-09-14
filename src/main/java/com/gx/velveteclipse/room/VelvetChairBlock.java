package com.gx.velveteclipse.room;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;

public final class VelvetChairBlock extends HorizontalDirectionalBlock {
 public VelvetChairBlock(){super(BlockBehaviour.Properties.m_284310_().m_60913_(3,12).m_60955_());m_49959_(f_49792_.m_61090_().m_61124_(f_54117_,Direction.NORTH));}
 @Override public BlockState m_5573_(net.minecraft.world.item.context.BlockPlaceContext c){var player=c.m_43723_();return m_49966_().m_61124_(f_54117_,player==null?Direction.NORTH:player.m_6350_().m_122424_());}
 @Override protected void m_7926_(StateDefinition.Builder<Block,BlockState> builder){builder.m_61104_(f_54117_);}
 @Override public VoxelShape m_5940_(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return Block.m_49796_(2,0,2,14,11,14);}
 @Override public InteractionResult m_6227_(BlockState s,Level l,BlockPos p,Player player,InteractionHand h,BlockHitResult hit){if(player instanceof ServerPlayer sp)VelvetRoomService.sit(sp,p);return InteractionResult.SUCCESS;}
}
