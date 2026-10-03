package com.gx.velveteclipse.darkhour;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
/** A normal creative building block; scenic state supplies collision beneath approved chunk geometry. */
public class TartarusScenicBlock extends Block {
 public static final BooleanProperty SCENIC=BooleanProperty.m_61465_("scenic");
 public TartarusScenicBlock(BlockBehaviour.Properties p){super(p);m_49959_(f_49792_.m_61090_().m_61124_(SCENIC,false));}
 @Override protected void m_7926_(StateDefinition.Builder<Block,BlockState> b){b.m_61104_(SCENIC);}
 @Override public RenderShape m_7514_(BlockState s){return s.m_61143_(SCENIC)?RenderShape.INVISIBLE:RenderShape.MODEL;}
 public static final class Stair extends StairBlock {
  public Stair(java.util.function.Supplier<BlockState> base,BlockBehaviour.Properties p){super(base,p);m_49959_(m_49966_().m_61124_(SCENIC,false));}
  @Override protected void m_7926_(StateDefinition.Builder<Block,BlockState> b){super.m_7926_(b);b.m_61104_(SCENIC);}
  @Override public RenderShape m_7514_(BlockState s){return s.m_61143_(SCENIC)?RenderShape.INVISIBLE:RenderShape.MODEL;}
 }
}
