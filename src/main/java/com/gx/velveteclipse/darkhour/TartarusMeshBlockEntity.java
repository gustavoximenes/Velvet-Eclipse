package com.gx.velveteclipse.darkhour;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraftforge.client.model.data.*;

public final class TartarusMeshBlockEntity extends BlockEntity {
 public record Instance(String model,int x,int y,int z){}
 public record Spec(List<Instance> instances,int clipX,int clipY,int clipZ,boolean clipped){}
 public static final ModelProperty<Spec> MESH=new ModelProperty<>();
 public static final ModelProperty<BlockPos> CUTAWAY_ORIGIN=new ModelProperty<>();
 private Spec spec=new Spec(List.of(),0,0,0,false);
 public TartarusMeshBlockEntity(BlockPos p,BlockState s){super(DarkHourRegistry.MESH_ENTITY.get(),p,s);}
 public void configure(Spec value){spec=value;m_6596_();requestModelDataUpdate();if(f_58857_!=null)f_58857_.m_7260_(f_58858_,m_58900_(),m_58900_(),3);}
 @Override protected void m_183515_(CompoundTag tag){super.m_183515_(tag);var list=new ListTag();for(var i:spec.instances()){var e=new CompoundTag();e.m_128359_("model",i.model());e.m_128405_("x",i.x());e.m_128405_("y",i.y());e.m_128405_("z",i.z());list.add(e);}tag.m_128365_("pieces",list);tag.m_128379_("clipped",spec.clipped());tag.m_128405_("clipX",spec.clipX());tag.m_128405_("clipY",spec.clipY());tag.m_128405_("clipZ",spec.clipZ());}
 @Override public void m_142466_(CompoundTag tag){super.m_142466_(tag);var out=new ArrayList<Instance>();var list=tag.m_128437_("pieces",10);for(int i=0;i<Math.min(list.size(),2048);i++){var e=list.m_128728_(i);String model=e.m_128461_("model");if(model.matches("[a-z0-9_-]{1,80}"))out.add(new Instance(model,e.m_128451_("x"),e.m_128451_("y"),e.m_128451_("z")));}spec=new Spec(List.copyOf(out),tag.m_128451_("clipX"),tag.m_128451_("clipY"),tag.m_128451_("clipZ"),tag.m_128471_("clipped"));requestModelDataUpdate();if(f_58857_!=null&&f_58857_.f_46443_)f_58857_.m_7260_(f_58858_,m_58900_(),m_58900_(),3);}
 @Override public CompoundTag m_5995_(){var t=new CompoundTag();m_183515_(t);return t;}
 @Override public ClientboundBlockEntityDataPacket m_58483_(){return ClientboundBlockEntityDataPacket.m_195640_(this);}
 @Override public ModelData getModelData(){var data=ModelData.builder().with(MESH,spec);if(f_58857_!=null&&f_58857_.m_46472_().equals(TartarusDungeonService.DIMENSION))data.with(CUTAWAY_ORIGIN,f_58858_);return data.build();}
}
