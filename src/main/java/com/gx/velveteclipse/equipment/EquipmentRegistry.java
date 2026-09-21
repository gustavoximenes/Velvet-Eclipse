package com.gx.velveteclipse.equipment;
import com.gx.velveteclipse.room.VelvetRoomRegistry;
import com.gx.velveteclipse.forge.BattleService;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.registries.RegistryObject;
public final class EquipmentRegistry {
 public static final RegistryObject<EntityType<ElizabethEntity>> ELIZABETH=VelvetRoomRegistry.ENTITIES.register("elizabeth",()->EntityType.Builder.<ElizabethEntity>m_20704_(ElizabethEntity::new,MobCategory.MISC).m_20699_(.7f,2.4f).m_20702_(10).m_20717_(3).m_20712_("velvet_eclipse:elizabeth"));
 static {
  VelvetRoomRegistry.ITEMS.register("elizabeth_model",()->new Item(new Item.Properties()){
   @Override public InteractionResult m_6225_(UseOnContext c){if(c.m_43723_()==null||BattleService.current(c.m_43723_())!=null)return InteractionResult.FAIL;var at=c.m_8083_().m_121945_(c.m_43719_());var level=c.m_43725_();var bounds=new net.minecraft.world.phys.AABB(at.m_123341_()+.15,at.m_123342_(),at.m_123343_()+.15,at.m_123341_()+.85,at.m_123342_()+2.5,at.m_123343_()+.85);if(!level.m_45756_(null,bounds))return InteractionResult.FAIL;if(!level.f_46443_){var npc=ELIZABETH.get().m_20615_(level);if(npc==null)return InteractionResult.FAIL;npc.m_6034_(at.m_123341_()+.5,at.m_123342_(),at.m_123343_()+.5);npc.m_146922_(c.m_43723_().m_146908_()+180);if(!level.m_7967_(npc))return InteractionResult.FAIL;if(!c.m_43723_().m_150110_().f_35937_)c.m_43722_().m_41774_(1);}return InteractionResult.SUCCESS;}
  });
  for(var g:EquipmentCatalog.all())VelvetRoomRegistry.ITEMS.register("gear_"+g.id(),()->new Voucher(g.id(),false));
  for(var s:Supplies.all())VelvetRoomRegistry.ITEMS.register(s.id(),()->new Voucher(s.id(),true));
 }
 public static void init(){}
 private static final class Voucher extends Item {
  private final String id;private final boolean supply;
  Voucher(String id,boolean supply){super(new Item.Properties().m_41487_(supply?64:1));this.id=id;this.supply=supply;}
  @Override public InteractionResultHolder<ItemStack> m_7203_(Level level,Player player,InteractionHand hand){var stack=player.m_21120_(hand);if(BattleService.current(player)!=null)return InteractionResultHolder.m_19098_(stack);if(player instanceof ServerPlayer p){var data=EquipmentData.load(p);boolean changed=supply?data.addSupply(id):data.grant(id);if(changed){EquipmentData.save(p,data);if(!p.m_150110_().f_35937_)stack.m_41774_(1);EquipmentService.sync(p,"sync","Adicionado à coleção.");}p.m_5661_(Component.m_237113_(changed?"Adicionado ao menu do Tab.":"Você já possui este equipamento."),true);}return InteractionResultHolder.m_19090_(stack);}
 }
}
