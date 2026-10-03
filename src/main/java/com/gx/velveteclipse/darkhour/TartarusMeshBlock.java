package com.gx.velveteclipse.darkhour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.shapes.*;

/** Unticked block geometry, compiled into the normal chunk buffers by Forge model data. */
public final class TartarusMeshBlock extends BaseEntityBlock {
 public static final BooleanProperty SOLID=BooleanProperty.m_61465_("solid");
 public static final IntegerProperty LIGHT=IntegerProperty.m_61631_("light",0,15);
 private final String model;
 public TartarusMeshBlock(String model){super(BlockBehaviour.Properties.m_284310_().m_60913_(3,12).m_60955_().m_60953_(s->s.m_61143_(LIGHT)));this.model=model;m_49959_(f_49792_.m_61090_().m_61124_(SOLID,false).m_61124_(LIGHT,model.equals("kit_lantern")?14:0));}
 public String model(){return model;}
 @Override protected void m_7926_(StateDefinition.Builder<Block,BlockState> b){b.m_61104_(SOLID,LIGHT);}
 @Override public BlockEntity m_142194_(BlockPos p,BlockState s){return new TartarusMeshBlockEntity(p,s);}
 @Override public RenderShape m_7514_(BlockState s){return RenderShape.MODEL;}
 @Override public VoxelShape m_5939_(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return s.m_61143_(SOLID)?Shapes.m_83144_():Shapes.m_83040_();}
 @Override public VoxelShape m_5940_(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return model.equals("empty")||model.equals("emissive")?m_5939_(s,l,p,c):Shapes.m_83144_();}
}
