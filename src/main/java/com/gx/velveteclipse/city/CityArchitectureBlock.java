package com.gx.velveteclipse.city;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.*;

/** One independently editable block, with only the visible architecture's local collision. */
public final class CityArchitectureBlock extends BaseEntityBlock {
    public static final IntegerProperty LIGHT=IntegerProperty.m_61631_("light",0,15);
    public CityArchitectureBlock(){this(false);}
    public CityArchitectureBlock(boolean tower){super(BlockBehaviour.Properties.m_284310_().m_60913_(tower?-1:3,tower?3600000:12).m_60955_().m_60953_(s->s.m_61143_(LIGHT)));m_49959_(f_49792_.m_61090_().m_61124_(LIGHT,0));}
    @Override protected void m_7926_(StateDefinition.Builder<Block,BlockState> b){b.m_61104_(LIGHT);}
    @Override public BlockEntity m_142194_(BlockPos p,BlockState s){return new CityArchitectureEntity(p,s);}
    @Override public RenderShape m_7514_(BlockState s){return RenderShape.MODEL;}
    @Override public VoxelShape m_5939_(BlockState s,BlockGetter level,BlockPos pos,CollisionContext c){return level.m_7702_(pos) instanceof CityArchitectureEntity tile?CityArchitecture.shape(tile.id()):Shapes.m_83040_();}
    @Override public VoxelShape m_5940_(BlockState s,BlockGetter level,BlockPos pos,CollisionContext c){var shape=m_5939_(s,level,pos,c);return shape.m_83281_()?Shapes.m_83144_():shape;}
}
