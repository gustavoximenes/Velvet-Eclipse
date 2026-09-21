package com.gx.velveteclipse.room;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.*;

public final class VelvetRoomRegistry {
 public static final DeferredRegister<Block> BLOCKS=DeferredRegister.create(ForgeRegistries.BLOCKS,"velvet_eclipse");
 public static final DeferredRegister<Item> ITEMS=DeferredRegister.create(ForgeRegistries.ITEMS,"velvet_eclipse");
 public static final DeferredRegister<EntityType<?>> ENTITIES=DeferredRegister.create(ForgeRegistries.ENTITY_TYPES,"velvet_eclipse");
 public static final DeferredRegister<CreativeModeTab> TABS=DeferredRegister.create(Registries.f_279569_,"velvet_eclipse");
 private static BlockBehaviour.Properties solid(){return BlockBehaviour.Properties.m_284310_().m_60913_(3,12);}
 public static final RegistryObject<Block> FLOOR=BLOCKS.register("velvet_floor",()->new Block(solid()));
 public static final RegistryObject<Block> WALL=BLOCKS.register("velvet_wall",()->new Block(solid()));
 public static final RegistryObject<Block> TRIM=BLOCKS.register("velvet_trim",()->new Block(solid().m_60953_(s->8)));
 public static final RegistryObject<Block> GATE=BLOCKS.register("velvet_grille",VelvetGrilleBlock::new);
 public static final RegistryObject<VelvetDoorBlock> DOOR=BLOCKS.register("velvet_door",VelvetDoorBlock::new);
 public static final RegistryObject<VelvetChairBlock> CHAIR=BLOCKS.register("velvet_chair",VelvetChairBlock::new);
 public static final RegistryObject<Block> TABLE=BLOCKS.register("velvet_table",()->new Block(solid().m_60955_()){
  @Override public net.minecraft.world.phys.shapes.VoxelShape m_5940_(net.minecraft.world.level.block.state.BlockState s,net.minecraft.world.level.BlockGetter l,net.minecraft.core.BlockPos p,net.minecraft.world.phys.shapes.CollisionContext c){return Block.m_49796_(-8,0,-8,24,14,24);}
 });
 public static final RegistryObject<Item> KEY=ITEMS.register("velvet_key",VelvetKeyItem::new);
 static {for(var block:BLOCKS.getEntries())ITEMS.register(block.getId().m_135815_(),()->new BlockItem(block.get(),new Item.Properties()));}
 static {for(var block:com.gx.velveteclipse.darkhour.DarkHourRegistry.creativeBlocks())ITEMS.register(block.getId().m_135815_(),()->new BlockItem(block.get(),new Item.Properties()));}
 public static final RegistryObject<EntityType<VelvetSeatEntity>> SEAT=ENTITIES.register("velvet_seat",()->EntityType.Builder.<VelvetSeatEntity>m_20704_(VelvetSeatEntity::new,MobCategory.MISC).m_20699_(.1f,.1f).m_20702_(8).m_20717_(1).m_20712_("velvet_eclipse:velvet_seat"));
 public static final RegistryObject<EntityType<IgorEntity>> IGOR=ENTITIES.register("igor",()->EntityType.Builder.<IgorEntity>m_20704_(IgorEntity::new,MobCategory.MISC).m_20699_(.9f,2f).m_20702_(10).m_20717_(3).m_20712_("velvet_eclipse:igor"));
 public static final RegistryObject<CreativeModeTab> TAB=TABS.register("velvet_eclipse",()->CreativeModeTab.builder().m_257941_(Component.m_237115_("itemGroup.velvet_eclipse")).m_257737_(()->new ItemStack(KEY.get())).m_257501_((parameters,output)->{ForgeRegistries.ITEMS.getEntries().stream().filter(e->e.getKey().m_135782_().m_135827_().equals("velvet_eclipse")).sorted(java.util.Comparator.comparing(e->e.getKey().toString())).forEach(e->output.m_246326_(e.getValue()));}).m_257652_());
 public static void register(IEventBus bus){com.gx.velveteclipse.shadow.FieldShadowRegistry.init();com.gx.velveteclipse.shadow.EnemyRegistry.init();com.gx.velveteclipse.darkhour.TartarusPropRegistry.init();com.gx.velveteclipse.raid.RaidRegistry.init();BLOCKS.register(bus);ITEMS.register(bus);ENTITIES.register(bus);TABS.register(bus);}
}
