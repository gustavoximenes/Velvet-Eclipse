package com.gx.velveteclipse.room;
import net.minecraft.core.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.phys.shapes.*;
/** Thin ornamental panels follow the wall instead of occupying a full block. */
public final class VelvetGrilleBlock extends HorizontalDirectionalBlock {
 public VelvetGrilleBlock(){super(BlockBehaviour.Properties.m_284310_().m_60913_(3,12).m_60955_());m_49959_(f_49792_.m_61090_().m_61124_(f_54117_,Direction.NORTH));}
 @Override protected void m_7926_(StateDefinition.Builder<Block,BlockState> b){b.m_61104_(f_54117_);}
 @Override public BlockState m_5573_(net.minecraft.world.item.context.BlockPlaceContext c){var p=c.m_43723_();return m_49966_().m_61124_(f_54117_,p==null?Direction.NORTH:p.m_6350_().m_122424_());}
 @Override public VoxelShape m_5940_(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return switch(s.m_61143_(f_54117_)){case SOUTH->Block.m_49796_(0,0,1,16,16,3);case EAST->Block.m_49796_(1,0,0,3,16,16);case WEST->Block.m_49796_(13,0,0,15,16,16);default->Block.m_49796_(0,0,13,16,16,15);};}
}
