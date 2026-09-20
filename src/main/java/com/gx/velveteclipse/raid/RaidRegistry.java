package com.gx.velveteclipse.raid;
import com.gx.velveteclipse.room.VelvetRoomRegistry;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.RegistryObject;
import java.util.*;
public final class RaidRegistry {
 public static final Map<String,RegistryObject<Block>> BLOCKS=new LinkedHashMap<>();
 static {for(String name:List.of("floor","wall","window","seat","metal","lamp","rail")){
  RegistryObject<Block> b=VelvetRoomRegistry.BLOCKS.register("train_"+name,()->new Block(BlockBehaviour.Properties.m_284310_().m_60913_(-1,3600000).m_60955_().m_60953_(s->name.equals("lamp")?13:name.equals("window")?5:0)){
   @Override public net.minecraft.world.phys.shapes.VoxelShape m_5940_(net.minecraft.world.level.block.state.BlockState s,net.minecraft.world.level.BlockGetter l,net.minecraft.core.BlockPos p,net.minecraft.world.phys.shapes.CollisionContext c){return name.equals("rail")?Block.m_49796_(6,0,6,10,16,10):Block.m_49796_(0,0,0,16,16,16);}
  });
  BLOCKS.put(name,b);VelvetRoomRegistry.ITEMS.register("train_"+name,()->new BlockItem(b.get(),new Item.Properties()));
 }}
 public static final RegistryObject<EntityType<PriestessEntity>> PRIESTESS=VelvetRoomRegistry.ENTITIES.register("priestess",()->EntityType.Builder.<PriestessEntity>m_20704_(PriestessEntity::new,MobCategory.MISC).m_20699_(4,4).m_20702_(12).m_20717_(1).m_20712_("velvet_eclipse:priestess"));
 public static final RegistryObject<Item> FIGURE=VelvetRoomRegistry.ITEMS.register("priestess_figure",()->new Item(new Item.Properties().m_41487_(1)){
  @Override public net.minecraft.world.InteractionResult m_6225_(net.minecraft.world.item.context.UseOnContext c){
   if(c.m_43725_().f_46443_)return net.minecraft.world.InteractionResult.SUCCESS;
   var e=PRIESTESS.get().m_20615_(c.m_43725_());if(e==null)return net.minecraft.world.InteractionResult.FAIL;
   var p=c.m_8083_().m_121945_(c.m_43719_());e.m_6034_(p.m_123341_()+.5,p.m_123342_(),p.m_123343_()+.5);e.m_146922_(c.m_43723_()==null?0:c.m_43723_().m_146908_()+180);e.getPersistentData().m_128379_("display_only",true);
   if(!c.m_43725_().m_7967_(e))return net.minecraft.world.InteractionResult.FAIL;
   if(c.m_43723_()!=null&&!c.m_43723_().m_150110_().f_35937_)c.m_43722_().m_41774_(1);return net.minecraft.world.InteractionResult.SUCCESS;
  }
 });
 public static void init(){}
}
