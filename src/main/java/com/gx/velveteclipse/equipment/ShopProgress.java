package com.gx.velveteclipse.equipment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
@Mod.EventBusSubscriber(modid="velvet_eclipse")
public final class ShopProgress {
 private static final String NOTICE="velvet_eclipse:shop_train_notice";
 public static final class State extends SavedData {
  boolean unlocked;
  static State load(CompoundTag t){var s=new State();s.unlocked=t.m_128471_("train_defeated");return s;}
  @Override public CompoundTag m_7176_(CompoundTag t){t.m_128379_("train_defeated",unlocked);return t;}
 }
 private static State state(ServerPlayer p){return p.m_20194_().m_129783_().m_8895_().m_164861_(State::load,State::new,"velvet_shop_progress");}
 public static boolean unlocked(ServerPlayer p){var s=state(p);if(!s.unlocked&&com.gx.velveteclipse.forge.BestiaryService.entries(p).stream().anyMatch(e->e.id().equals("raid_2")&&e.defeated()>0))unlock(p);return s.unlocked;}
 public static void unlock(ServerPlayer p){var s=state(p);if(s.unlocked)return;s.unlocked=true;s.m_77762_();for(var other:p.m_20194_().m_6846_().m_11314_()){notify(other);EquipmentService.sync(other,"sync","");}}
 private static void notify(ServerPlayer p){if(p.getPersistentData().m_128471_(NOTICE))return;p.getPersistentData().m_128379_(NOTICE,true);p.m_5661_(Component.m_237113_("ELIZABETH • A Sacerdotisa do trem foi derrotada! Novos equipamentos estão disponíveis na loja do hall."),false);p.m_5661_(Component.m_237113_("NOVOS EQUIPAMENTOS DISPONÍVEIS • Visite Elizabeth"),true);}
 @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent e){if(e.getEntity() instanceof ServerPlayer p&&unlocked(p))notify(p);}
 @SubscribeEvent public static void clone(PlayerEvent.Clone e){e.getEntity().getPersistentData().m_128379_(NOTICE,e.getOriginal().getPersistentData().m_128471_(NOTICE));}
}
