package com.gx.velveteclipse.equipment;
import com.gx.velveteclipse.util.RuntimeBridge;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
@Mod.EventBusSubscriber(modid="velvet_eclipse")
public final class ShopProgress {
 private static final String KEY="velvet_eclipse:purchase_permits";
 public static int permits(Object player){try{return Integer.parseInt(RuntimeBridge.tagGetString(RuntimeBridge.persistentData(player),KEY))&1023;}catch(RuntimeException e){return 0;}}
 public static int tier(Object player){return ShopStock.tier(permits(player));}
 public static boolean redeem(Object player,int floor){int bit=ShopStock.bit(floor),old=permits(player);if(bit==0||(old&bit)!=0)return false;RuntimeBridge.tagPutString(RuntimeBridge.persistentData(player),KEY,Integer.toString(old|bit));return true;}
 public static String use(ServerPlayer p,int floor){
  int before=tier(p);if(!redeem(p,floor))return "Esta permissão já foi registrada. O item foi preservado.";
  int now=tier(p);String message=now>before?"Novos equipamentos disponíveis! Elizabeth liberou o estoque de "+ShopStock.period(now)+".":now==ShopStock.LAST_TIER?"Permissão registrada. Você já tem acesso ao estoque final de Reload.":"Permissão registrada. Falta a permissão do andar "+ShopStock.nextFloor(permits(p))+" para avançar o estoque.";
  if(now>before)p.m_5661_(com.gx.velveteclipse.localization.ModText.component("NOVOS EQUIPAMENTOS DISPONÍVEIS • Visite Elizabeth"),true);
  EquipmentService.sync(p,"sync",message);return message;
 }
 @SubscribeEvent public static void clone(PlayerEvent.Clone e){RuntimeBridge.tagPutString(RuntimeBridge.persistentData(e.getEntity()),KEY,Integer.toString(permits(e.getOriginal())));}
}
