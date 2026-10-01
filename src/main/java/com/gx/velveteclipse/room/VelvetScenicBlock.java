package com.gx.velveteclipse.room;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/** Room-built blocks use quiet backing materials; creative placements keep their original art. */
public class VelvetScenicBlock extends Block {
 public static final BooleanProperty SCENIC=BooleanProperty.m_61465_("scenic");
 public VelvetScenicBlock(BlockBehaviour.Properties properties){super(properties);m_49959_(f_49792_.m_61090_().m_61124_(SCENIC,false));}
 @Override protected void m_7926_(StateDefinition.Builder<Block,BlockState> builder){builder.m_61104_(SCENIC);}
}
