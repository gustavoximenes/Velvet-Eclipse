package com.gx.velveteclipse.darkhour;
import com.gx.velveteclipse.room.VelvetRoomRegistry;
import net.minecraft.world.entity.*;
import net.minecraftforge.registries.RegistryObject;
public final class TartarusPropRegistry {
 public static final RegistryObject<EntityType<TartarusPropEntity>> TERMINAL=register("terminal",3,4),TREASURE=register("treasure",1.5f,1.25f),MONAD=register("monad",.6f,5);
 private static RegistryObject<EntityType<TartarusPropEntity>> register(String name,float w,float h){return VelvetRoomRegistry.ENTITIES.register("tartarus_"+name,()->EntityType.Builder.<TartarusPropEntity>m_20704_(TartarusPropEntity::new,MobCategory.MISC).m_20699_(w,h).m_20702_(12).m_20717_(20).m_20712_("velvet_eclipse:tartarus_"+name));}
 public static EntityType<TartarusPropEntity> type(String kind){return switch(kind){case "terminal"->TERMINAL.get();case "treasure"->TREASURE.get();case "monad"->MONAD.get();default->throw new IllegalArgumentException(kind);};}
 public static final java.util.Map<String,RegistryObject<net.minecraft.world.item.Item>> ITEMS=new java.util.LinkedHashMap<>();
 static {for(String kind:java.util.List.of("terminal","treasure","monad"))ITEMS.put(kind,VelvetRoomRegistry.ITEMS.register("tartarus_"+kind,()->new TartarusPropItem(kind)));}
 public static void init(){}
}
