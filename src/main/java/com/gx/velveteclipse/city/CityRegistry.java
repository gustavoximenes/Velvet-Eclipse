package com.gx.velveteclipse.city;
import com.gx.velveteclipse.room.VelvetRoomRegistry;
import net.minecraft.world.level.block.*;import net.minecraft.world.level.block.state.BlockBehaviour;import net.minecraft.world.item.*;import net.minecraftforge.registries.RegistryObject;
import java.util.*;
public final class CityRegistry {
 public static final Map<String,RegistryObject<Block>> BLOCKS=new LinkedHashMap<>();
 static {for(String id:List.of("paving","brick","plaster","wood","glass","school_floor","leaves","roof","light")){
  RegistryObject<Block> block=VelvetRoomRegistry.BLOCKS.register("city_"+id,()->new CitySurfaceBlock(BlockBehaviour.Properties.m_284310_().m_60913_(3,12).m_60953_(s->id.equals("light")?15:0)));BLOCKS.put(id,block);VelvetRoomRegistry.ITEMS.register("city_"+id,()->new BlockItem(block.get(),new Item.Properties()));
 }
 for(String id:CityCatalog.SHOPS){RegistryObject<Block> block=VelvetRoomRegistry.BLOCKS.register("city_shop_"+id,()->new Counter(id));BLOCKS.put("shop_"+id,block);VelvetRoomRegistry.ITEMS.register("city_shop_"+id,()->new BlockItem(block.get(),new Item.Properties()));}
 RegistryObject<Block> access=VelvetRoomRegistry.BLOCKS.register("city_tartarus_access",()->new Counter("tartarus"));BLOCKS.put("tartarus_access",access);VelvetRoomRegistry.ITEMS.register("city_tartarus_access",()->new BlockItem(access.get(),new Item.Properties()));}
 public static void init(){}
 private static final class Counter extends Block {private final String shop;Counter(String s){super(BlockBehaviour.Properties.m_284310_().m_60913_(3,12));shop=s;}
  @Override public net.minecraft.world.InteractionResult m_6227_(net.minecraft.world.level.block.state.BlockState s,net.minecraft.world.level.Level l,net.minecraft.core.BlockPos pos,net.minecraft.world.entity.player.Player p,net.minecraft.world.InteractionHand hand,net.minecraft.world.phys.BlockHitResult hit){if(p instanceof net.minecraft.server.level.ServerPlayer player&&hand==net.minecraft.world.InteractionHand.MAIN_HAND){if(shop.equals("tartarus"))CityService.enterTartarus(player);else CityService.shop(player,shop,pos);}return net.minecraft.world.InteractionResult.SUCCESS;}
 }
}
