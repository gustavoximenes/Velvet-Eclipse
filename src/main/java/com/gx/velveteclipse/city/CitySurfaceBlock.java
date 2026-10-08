package com.gx.velveteclipse.city;
import com.gx.velveteclipse.darkhour.TartarusScenicBlock;
import net.minecraft.core.BlockPos;import net.minecraft.world.level.BlockGetter;import net.minecraft.world.level.block.Block;import net.minecraft.world.level.block.state.*;import net.minecraft.world.level.block.state.properties.IntegerProperty;import net.minecraft.world.phys.shapes.*;
/** Surface heights follow authored floors, steps and furniture at 1/16-block precision. */
public final class CitySurfaceBlock extends TartarusScenicBlock {
 public static final IntegerProperty HEIGHT=IntegerProperty.m_61631_("height",1,16);
 public CitySurfaceBlock(BlockBehaviour.Properties p){super(p);m_49959_(m_49966_().m_61124_(HEIGHT,16));}
 @Override protected void m_7926_(StateDefinition.Builder<Block,BlockState>b){super.m_7926_(b);b.m_61104_(HEIGHT);}
 @Override public VoxelShape m_5939_(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return Block.m_49796_(0,0,0,16,s.m_61143_(HEIGHT),16);}
 @Override public VoxelShape m_5940_(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return m_5939_(s,l,p,c);}
}
