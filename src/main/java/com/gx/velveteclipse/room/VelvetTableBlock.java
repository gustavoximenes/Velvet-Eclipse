package com.gx.velveteclipse.room;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.phys.shapes.*;

/** Collision proxy under the authored round table. Only the old creative table draws a block model. */
public final class VelvetTableBlock extends VelvetScenicBlock {
 private static final VoxelShape ROUND=round();
 public VelvetTableBlock(){super(BlockBehaviour.Properties.m_284310_().m_60913_(3,12).m_60955_());}
 private static VoxelShape round(){
  VoxelShape shape=Shapes.m_83040_();
  for(int i=-8;i<8;i++){
   double x=(i+.5)*.2,z=Math.sqrt(1.61*1.61-x*x);
   shape=Shapes.m_83110_(shape,Block.m_49796_((.5+i*.2)*16,0,(.56-z)*16,(.5+(i+1)*.2)*16,17.46,(.56+z)*16));
  }
  return shape;
 }
 @Override public RenderShape m_7514_(BlockState s){return s.m_61143_(SCENIC)?RenderShape.INVISIBLE:RenderShape.MODEL;}
 @Override public VoxelShape m_5940_(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return s.m_61143_(SCENIC)?ROUND:Block.m_49796_(-8,0,-8,24,14,24);}
}
