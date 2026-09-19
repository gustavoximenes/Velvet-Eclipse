package com.gx.velveteclipse.darkhour;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.*;
import net.minecraftforge.eventbus.api.IEventBus;

public final class DarkHourRegistry {
 private static final DeferredRegister<Block> BLOCKS=DeferredRegister.create(ForgeRegistries.BLOCKS,"velvet_eclipse");
 private static BlockBehaviour.Properties props(){return BlockBehaviour.Properties.m_284310_().m_60913_(-1,3600000).m_60977_();}
 public static final RegistryObject<Block> STONE=BLOCKS.register("tartarus_stone",()->new Block(props()));
 public static final RegistryObject<Block> TRIM=BLOCKS.register("tartarus_trim",()->new Block(props()));
 public static final RegistryObject<Block> LIGHT=BLOCKS.register("tartarus_light",()->new Block(props().m_60953_(s->13)));
 public static final RegistryObject<Block> STAIRS=BLOCKS.register("tartarus_stairs",()->new StairBlock(()->STONE.get().m_49966_(),props()));
 public static void register(IEventBus bus){BLOCKS.register(bus);}
 public static boolean temporary(Block b){return b==STONE.get()||b==TRIM.get()||b==LIGHT.get()||b==STAIRS.get();}
}
