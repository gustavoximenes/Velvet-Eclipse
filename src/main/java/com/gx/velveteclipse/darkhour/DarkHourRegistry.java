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
 public static final RegistryObject<Block> PORTAL=BLOCKS.register("tartarus_portal",()->new TartarusPortalBlock("entrance"));
 public static final java.util.Map<String,RegistryObject<Block>> INTERIOR=new java.util.LinkedHashMap<>();
 static {
  for(String name:java.util.List.of("ivory","tile","tile_dark","wall","column","gold","carpet","clock","hand","border","seal","window","azure","violet"))
   INTERIOR.put(name,BLOCKS.register("tartarus_"+name,()->new Block(props().m_60953_(s->java.util.Set.of("window","azure","violet","clock","hand").contains(name)?12:0))));
  INTERIOR.put("blue_stairs",BLOCKS.register("tartarus_blue_stairs",()->new StairBlock(()->INTERIOR.get("carpet").get().m_49966_(),props())));
  for(String route:java.util.List.of("forward","back","exit","summit"))INTERIOR.put(route,BLOCKS.register("tartarus_"+route,()->new TartarusPortalBlock(route)));
 }
 public static void register(IEventBus bus){BLOCKS.register(bus);}
 public static java.util.List<RegistryObject<Block>> creativeBlocks(){var all=new java.util.ArrayList<RegistryObject<Block>>(java.util.List.of(STONE,TRIM,LIGHT,STAIRS,PORTAL));all.addAll(INTERIOR.values());return java.util.List.copyOf(all);}
 public static boolean temporary(Block b){return b==STONE.get()||b==TRIM.get()||b==LIGHT.get()||b==STAIRS.get()||b==PORTAL.get();}
}
