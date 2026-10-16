package com.gx.velveteclipse.city;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.*;

/** Unticked and synchronized as a compact tile identifier, not a network vertex array. */
public final class CityArchitectureEntity extends BlockEntity {
    public static final ModelProperty<Integer> TILE=new ModelProperty<>();
    private int id=0;
    public CityArchitectureEntity(BlockPos pos,BlockState state){super(CityRegistry.ARCHITECTURE_ENTITY.get(),pos,state);if(state.m_60713_(CityRegistry.TOWER.get()))id=com.gx.velveteclipse.darkhour.TartarusExterior.plan().sample();}
    public int id(){return id;}
    public void configure(int value){id=CityArchitecture.tile(value)==null?-1:value;m_6596_();
        if(f_58857_!=null&&!f_58857_.f_46443_){
            var tile=CityArchitecture.tile(id);var state=m_58900_();int light=tile==null?0:tile.light();
            if(state.m_61143_(CityArchitectureBlock.LIGHT)!=light)
                f_58857_.m_7731_(f_58858_,state.m_61124_(CityArchitectureBlock.LIGHT,light),3);
        }
        refresh();}
    private void refresh(){requestModelDataUpdate();if(f_58857_!=null)f_58857_.m_7260_(f_58858_,m_58900_(),m_58900_(),3);}
    @Override protected void m_183515_(CompoundTag tag){super.m_183515_(tag);tag.m_128405_("city_tile",id);}
    @Override public void m_142466_(CompoundTag tag){super.m_142466_(tag);int value=tag.m_128441_("city_tile")?tag.m_128451_("city_tile"):-1;id=CityArchitecture.tile(value)==null?-1:value;refresh();}
    @Override public CompoundTag m_5995_(){var tag=new CompoundTag();m_183515_(tag);return tag;}
    @Override public ClientboundBlockEntityDataPacket m_58483_(){return ClientboundBlockEntityDataPacket.m_195640_(this);}
    @Override public ModelData getModelData(){return ModelData.builder().with(TILE,id).build();}
}
