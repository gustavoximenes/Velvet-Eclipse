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
 public static final java.util.Map<String,RegistryObject<Block>> ARCHITECTURE=new java.util.LinkedHashMap<>();
 private static final DeferredRegister<net.minecraft.world.level.block.entity.BlockEntityType<?>> ENTITIES=DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES,"velvet_eclipse");
 static {
  for(String name:java.util.List.of("ivory","tile","tile_dark","wall","column","gold","carpet","clock","hand","border","seal","window","azure","violet","amber","rose"))
   INTERIOR.put(name,BLOCKS.register("tartarus_"+name,()->new Block(props().m_60953_(s->java.util.Set.of("window","azure","violet","amber","rose","clock","hand").contains(name)?12:0))));
  INTERIOR.put("blue_stairs",BLOCKS.register("tartarus_blue_stairs",()->new StairBlock(()->INTERIOR.get("carpet").get().m_49966_(),props())));
  for(String route:java.util.List.of("forward","back","exit","summit"))INTERIOR.put(route,BLOCKS.register("tartarus_"+route,()->new TartarusPortalBlock(route)));
  for(String name:java.util.List.of("tile","wall","window","light"))ARCHITECTURE.put(name,BLOCKS.register("tartarus_reworked_"+name,()->new TartarusScenicBlock((name.equals("light")?props().m_60910_().m_60955_():props()).m_60953_(s->name.equals("light")||name.equals("window")?15:0))));
  ARCHITECTURE.put("stairs",BLOCKS.register("tartarus_reworked_stairs",()->new TartarusScenicBlock.Stair(()->ARCHITECTURE.get("wall").get().m_49966_(),props())));
  for(String name:java.util.List.of("column","ivory_column","lantern","rail","cornice","seal","arch","forward","back","stair"))ARCHITECTURE.put("kit_"+name,BLOCKS.register("tartarus_kit_"+name,()->new TartarusMeshBlock("kit_"+name)));
  ARCHITECTURE.put("mesh",BLOCKS.register("tartarus_architecture",()->new TartarusMeshBlock("empty")));
  ARCHITECTURE.put("emissive_mesh",BLOCKS.register("tartarus_architecture_emissive",()->new TartarusMeshBlock("emissive")));
 }
 public static final RegistryObject<net.minecraft.world.level.block.entity.BlockEntityType<TartarusMeshBlockEntity>> MESH_ENTITY=ENTITIES.register("tartarus_architecture",()->new net.minecraft.world.level.block.entity.BlockEntityType<TartarusMeshBlockEntity>(null,ARCHITECTURE.values().stream().map(RegistryObject::get).filter(b->b instanceof TartarusMeshBlock).collect(java.util.stream.Collectors.toSet()),null){
  // Explicit factory avoids exposing Minecraft's private nested supplier in the standalone SRG API.
  @Override public TartarusMeshBlockEntity m_155264_(net.minecraft.core.BlockPos p,net.minecraft.world.level.block.state.BlockState s){return new TartarusMeshBlockEntity(p,s);}
 });
 public static void register(IEventBus bus){BLOCKS.register(bus);ENTITIES.register(bus);}
 public static java.util.List<RegistryObject<Block>> creativeBlocks(){return BLOCKS.getEntries().stream().filter(b->!b.getId().m_135815_().startsWith("tartarus_architecture")).toList();}
 public static boolean temporary(Block b){return b==STONE.get()||b==TRIM.get()||b==LIGHT.get()||b==STAIRS.get()||b==PORTAL.get();}
}
