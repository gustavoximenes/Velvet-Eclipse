package com.gx.velveteclipse.progression;
import com.google.gson.Gson;
import com.gx.velveteclipse.forge.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
@Mod.EventBusSubscriber(modid="velvet_eclipse")
public final class ProgressionRewards {
 private static final Gson JSON=new Gson();
 public static final class State extends SavedData {
  RewardLedger ledger=new RewardLedger();
  static State load(CompoundTag t){var s=new State();var data=JSON.fromJson(t.m_128461_("ledger"),RewardLedger.class);if(data!=null)s.ledger=data;return s;}
  @Override public CompoundTag m_7176_(CompoundTag t){t.m_128359_("ledger",JSON.toJson(ledger));return t;}
 }
 private static State state(ServerPlayer p){return p.m_20194_().m_129783_().m_8895_().m_164861_(State::load,State::new,"velvet_progression_rewards");}
 public static boolean boss(ServerPlayer p){var s=state(p);if(!s.ledger.boss(p.m_20148_().toString()))return false;s.m_77762_();p.m_5661_(com.gx.velveteclipse.localization.ModText.component("Primeira vitória! Permissão de Compra • 25F e Contrato de Igor recebidos. Use os itens fora da batalha."),false);return true;}
 public static boolean chest(ServerPlayer p,int floor){var s=state(p);if(!s.ledger.chest(p.m_20148_().toString(),floor))return false;s.m_77762_();deliver(p);p.m_5661_(com.gx.velveteclipse.localization.ModText.component("Permissão de Compra • "+floor+"F recebida. Se o inventário estiver cheio, a entrega fica guardada até liberar espaço."),false);return true;}
 public static boolean claimed(ServerPlayer p,int floor){return state(p).ledger.claimed(p.m_20148_().toString(),floor);}
 private static void deliver(ServerPlayer p){
  if(!p.m_6084_()||BattleService.current(p)!=null)return;var s=state(p);String id=p.m_20148_().toString();
  for(String item:s.ledger.pending(id)){
   var registered=ProgressionItems.ITEMS.get(item);if(registered==null)continue;
   // A real empty slot is required even in creative, which can discard overflowing items.
   var inventory=p.m_150109_();int empty=inventory.m_36062_();if(empty<0)return;
   inventory.m_6836_(empty,new ItemStack(registered.get()));inventory.m_6596_();p.f_36095_.m_38946_();s.ledger.delivered(id,item);s.m_77762_();
  }
 }
 @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent e){if(e.getEntity() instanceof ServerPlayer p){
  // Migrate actual first-clear participants, never the old global shop unlock.
  if(BestiaryService.entries(p).stream().anyMatch(x->x.id().equals("raid_2")&&x.defeated()>0))boss(p);deliver(p);
 }}
 @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent e){if(e.phase==TickEvent.Phase.END&&e.player instanceof ServerPlayer p&&p.f_19797_%40==0)deliver(p);}
}
