package com.gx.velveteclipse.shadow;
import com.gx.velveteclipse.room.VelvetRoomRegistry;import net.minecraft.world.entity.*;import net.minecraft.world.item.*;import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.fml.common.Mod;import net.minecraftforge.eventbus.api.SubscribeEvent;
@Mod.EventBusSubscriber(modid="velvet_eclipse",bus=Mod.EventBusSubscriber.Bus.MOD)
public final class FieldShadowRegistry {
 public static final RegistryObject<EntityType<FieldShadowEntity>> TYPE=VelvetRoomRegistry.ENTITIES.register("field_shadow",()->EntityType.Builder.<FieldShadowEntity>m_20704_(FieldShadowEntity::new,MobCategory.MONSTER).m_20699_(1.3f,1.55f).m_20702_(10).m_20717_(2).m_20712_("velvet_eclipse:field_shadow"));
 public static final RegistryObject<Item> NORMAL=VelvetRoomRegistry.ITEMS.register("field_shadow_spawn",()->spawnItem(false));
 public static final RegistryObject<Item> RED=VelvetRoomRegistry.ITEMS.register("red_shadow_spawn",()->spawnItem(true));
 private static Item spawnItem(boolean rare){return new Item(new Item.Properties()){
  @Override public net.minecraft.world.InteractionResult m_6225_(net.minecraft.world.item.context.UseOnContext c){
   if(c.m_43725_().f_46443_)return net.minecraft.world.InteractionResult.SUCCESS;var e=TYPE.get().m_20615_(c.m_43725_());if(e==null)return net.minecraft.world.InteractionResult.FAIL;var at=c.m_8083_().m_121945_(c.m_43719_());e.m_6034_(at.m_123341_()+.5,at.m_123342_(),at.m_123343_()+.5);e.initialize(java.util.UUID.randomUUID().getMostSignificantBits());e.rare(rare);e.m_146922_(c.m_43723_()==null?0:c.m_43723_().m_146908_()+180);e.getPersistentData().m_128379_("manual_shadow",true);if(com.gx.velveteclipse.darkhour.TartarusDungeonService.inside(e)&&(com.gx.velveteclipse.darkhour.TartarusInteriorLayout.fixedFloor(com.gx.velveteclipse.darkhour.TartarusDungeonService.floor(e))||!c.m_43725_().m_6443_(FieldShadowEntity.class,e.m_20191_().m_82400_(35),other->FieldShadowEntity.sameRoom(e,other)).isEmpty()))return net.minecraft.world.InteractionResult.FAIL;if(!c.m_43725_().m_7967_(e))return net.minecraft.world.InteractionResult.FAIL;if(c.m_43723_()!=null&&!c.m_43723_().m_150110_().f_35937_)c.m_43722_().m_41774_(1);return net.minecraft.world.InteractionResult.SUCCESS;
  }
 };}
 @SubscribeEvent public static void attributes(net.minecraftforge.event.entity.EntityAttributeCreationEvent e){e.put(TYPE.get(),Mob.m_21552_().m_22268_(net.minecraft.world.entity.ai.attributes.Attributes.f_22279_,.27).m_22268_(net.minecraft.world.entity.ai.attributes.Attributes.f_22276_,30).m_22265_());}
 public static void init(){}
}
