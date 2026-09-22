package com.gx.velveteclipse.equipment;
import com.gx.velveteclipse.forge.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import java.util.*;
@Mod.EventBusSubscriber(modid="velvet_eclipse")
public final class EquipmentService {
 private record Visit(UUID npc,String token,long time){}
 private static final Map<UUID,Visit> VISITS=new HashMap<>();
 private static final Map<UUID,Long> GATE=new HashMap<>();
 public static void sync(ServerPlayer p,String view,String message){reply(p,view,message,null);}
 private static void reply(ServerPlayer p,String view,String message,ElizabethEntity npc){
  Visit v=VISITS.get(p.m_20148_());EquipmentNetwork.reply(p,new EquipmentNetwork.Snapshot(EquipmentData.load(p),PlayerPersonaData.load(p).yen(),view,message,npc==null?-1:npc.m_19879_(),npc!=null&&npc.merchant(),v==null?"":v.token(),ShopProgress.tier(p)));
 }
 public static void talk(ServerPlayer p,ElizabethEntity npc){
  if(BattleService.current(p)!=null||!p.m_6084_()||p.m_20280_(npc)>36)return;
  VISITS.put(p.m_20148_(),new Visit(npc.m_20148_(),UUID.randomUUID().toString(),System.currentTimeMillis()));reply(p,"dialogue","",npc);
 }
 private static ElizabethEntity merchant(ServerPlayer p,String token){
  var v=VISITS.get(p.m_20148_());if(v==null||!v.token().equals(token)||System.currentTimeMillis()-v.time()>600000)return null;
  var e=p.m_284548_().m_8791_(v.npc());return e instanceof ElizabethEntity npc&&npc.merchant()&&p.m_20280_(npc)<=36?npc:null;
 }
 public static void handle(ServerPlayer p,EquipmentNetwork.Request r){
  if(BattleService.current(p)!=null||!p.m_6084_())return;
  long now=System.currentTimeMillis();if(now-GATE.getOrDefault(p.m_20148_(),0L)<120)return;GATE.put(p.m_20148_(),now);
  var bag=EquipmentData.load(p);var data=PlayerPersonaData.load(p);String message="";ElizabethEntity npc=null;String view="update";
  switch(r.action()){
   case "open" -> view=r.id().equals("items")?"items":"equip";
   case "sync" -> view="sync";
   case "equip" -> {if(bag.equip(r.id()))message="Equipamento alterado.";else message="Você não possui esse equipamento ou ele já está equipado.";}
   case "remove" -> {try{bag.remove(EquipmentCatalog.Slot.valueOf(r.id()));message="Equipamento removido.";}catch(IllegalArgumentException ignored){return;}}
   case "shop","buy" -> {
    npc=merchant(p,r.token());if(npc==null){sync(p,"closed","Aproxime-se da Elizabeth para comprar.");return;}
    if(r.action().equals("shop")){view="shop";break;}
    if(!ShopStock.available(r.id(),ShopProgress.tier(p))){message="Este equipamento exige uma Permissão de Compra posterior ou não faz parte do estoque da loja.";break;}
    var result=EquipmentTransactions.buy(bag,data,r.id());
    message=switch(result){case BOUGHT->"Compra concluída.";case UNKNOWN->"Item desconhecido.";case OWNED->"Este equipamento já está na sua coleção.";case FULL->"Limite deste item atingido.";case NO_YEN->"Yen insuficiente.";};
    if(result==EquipmentTransactions.Result.BOUGHT)VISITS.put(p.m_20148_(),new Visit(npc.m_20148_(),UUID.randomUUID().toString(),now));
   }
   case "heal" -> {npc=merchant(p,r.token());if(npc==null){sync(p,"closed","Aproxime-se da Elizabeth do hall para se curar.");return;}data.restore();p.m_21153_(p.m_21233_());for(var effect:java.util.List.copyOf(p.m_21220_()))if(!effect.m_19544_().m_19486_())p.m_21195_(effect.m_19544_());VISITS.put(p.m_20148_(),new Visit(npc.m_20148_(),UUID.randomUUID().toString(),now));message="HP, SP e estados negativos restaurados. Boa viagem!";}
   case "use" -> {var s=Supplies.get(r.id());if(s==null)return;if(DamageItems.get(r.id())!=null){message="Use este item em combate, escolhendo um inimigo.";break;}if(s.hp()==0&&s.sp()==0){message="Use este item em combate para curar o estado de um aliado.";break;}if(data.hp()>=data.maxHp()&&s.hp()!=0||data.sp()>=data.maxSp()&&s.sp()!=0){message="Você já está recuperado.";break;}if(bag.consume(s.id())){data.setResources(data.hp()+Supplies.amount(s.hp(),data.maxHp()),data.sp()+Supplies.amount(s.sp(),data.maxSp()));message=s.name()+" utilizado.";}}
   default -> {return;}
  }
  EquipmentData.save(p,bag);data.save(p);var adjusted=PlayerPersonaData.load(p);adjusted.save(p);BattleService.sync(p,message);reply(p,view,message,npc);
 }
 @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent e){if(e.getEntity() instanceof ServerPlayer p)sync(p,"sync","");}
 @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent e){if(e.getEntity() instanceof ServerPlayer p)sync(p,"sync","");}
 @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent e){if(e.getEntity() instanceof ServerPlayer p){VISITS.remove(p.m_20148_());sync(p,"sync","");}}
 @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){VISITS.remove(e.getEntity().m_20148_());GATE.remove(e.getEntity().m_20148_());}
}
