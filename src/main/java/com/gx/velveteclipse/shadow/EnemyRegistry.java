package com.gx.velveteclipse.shadow;
import com.gx.velveteclipse.room.VelvetRoomRegistry;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraftforge.registries.RegistryObject;
import java.util.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Mod.EventBusSubscriber(modid="velvet_eclipse",bus=Mod.EventBusSubscriber.Bus.MOD)
public final class EnemyRegistry {
 public static final Map<String,RegistryObject<EntityType<EnemyEntity>>> TYPES=new LinkedHashMap<>();
 static {for(var def:EnemyCatalog.all()){
  var type=VelvetRoomRegistry.ENTITIES.register(def.asset(),()->EntityType.Builder.<EnemyEntity>m_20704_(EnemyEntity::new,MobCategory.MONSTER).m_20699_(1.2f,1.7f).m_20702_(10).m_20717_(2).m_20712_("velvet_eclipse:"+def.asset()));TYPES.put(def.id(),type);
  VelvetRoomRegistry.ITEMS.register(def.asset()+"_spawn",()->new Item(new Item.Properties()){
   @Override public net.minecraft.world.InteractionResult m_6225_(net.minecraft.world.item.context.UseOnContext c){
    if(c.m_43725_().f_46443_)return net.minecraft.world.InteractionResult.SUCCESS;
    var e=type.get().m_20615_(c.m_43725_());if(e==null)return net.minecraft.world.InteractionResult.FAIL;var at=c.m_8083_().m_121945_(c.m_43719_());e.m_6034_(at.m_123341_()+.5,at.m_123342_(),at.m_123343_()+.5);e.m_6593_(net.minecraft.network.chat.Component.m_237113_(def.displayName()));float yaw=c.m_43723_()==null?0:c.m_43723_().m_146908_()+180;e.m_146922_(yaw);e.m_5616_(yaw);e.m_5618_(yaw);
    if(!c.m_43725_().m_7967_(e))return net.minecraft.world.InteractionResult.FAIL;if(c.m_43723_()!=null&&!c.m_43723_().m_150110_().f_35937_)c.m_43722_().m_41774_(1);return net.minecraft.world.InteractionResult.SUCCESS;
   }
  });
 }}
 public static EntityType<EnemyEntity> typeFor(String id){return Objects.requireNonNull(TYPES.get(id),"Unknown enemy type: "+id).get();}
 @SubscribeEvent public static void attributes(net.minecraftforge.event.entity.EntityAttributeCreationEvent e){for(var type:TYPES.values())e.put(type.get(),Mob.m_21552_().m_22265_());}
 public static void init(){}
}
