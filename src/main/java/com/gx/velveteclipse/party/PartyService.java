package com.gx.velveteclipse.party;
import com.gx.velveteclipse.equipment.*;
import com.gx.velveteclipse.forge.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.*;
@Mod.EventBusSubscriber(modid="velvet_eclipse")
public final class PartyService {
 public static final class State extends SavedData {
  Map<String,com.gx.velveteclipse.darkhour.TartarusMapData> maps=new HashMap<>();
  PartyRoster roster=new PartyRoster();Map<String,String> names=new HashMap<>();
  @Override public CompoundTag m_7176_(CompoundTag t){t.m_128359_("roster",PartyNetwork.JSON.toJson(roster));var n=new CompoundTag();names.forEach(n::m_128359_);t.m_128365_("names",n);var m=new CompoundTag();maps.forEach((id,chart)->m.m_128365_(id,chart.save()));t.m_128365_("maps",m);return t;}
  static State load(CompoundTag t){var s=new State();try{var r=PartyNetwork.JSON.fromJson(t.m_128461_("roster"),PartyRoster.class);if(r!=null){r.sanitize();s.roster=r;}}catch(RuntimeException ignored){}var n=t.m_128469_("names");for(String id:n.m_128431_())s.names.put(id,n.m_128461_(id));var m=t.m_128469_("maps");for(String id:m.m_128431_())s.maps.put(id,com.gx.velveteclipse.darkhour.TartarusMapData.load(m.m_128469_(id)));return s;}
 }
 private record Offer(String token,Map<String,ItemStack> stacks){}
 private static final Map<UUID,Offer> offers=new HashMap<>();
 private static final Map<UUID,Long> gate=new HashMap<>();
 private static State state(ServerPlayer p){return p.m_20194_().m_129783_().m_8895_().m_164861_(State::load,State::new,"velvet_party");}
 private static String id(ServerPlayer p){return p.m_20148_().toString();}
 private static ServerPlayer online(ServerPlayer p,String id){try{return p.m_20194_().m_6846_().m_11259_(UUID.fromString(id));}catch(IllegalArgumentException e){return null;}}
 public static List<ServerPlayer> members(ServerPlayer p){var g=state(p).roster.group(id(p));return g==null?List.of(p):g.members().stream().map(s->online(p,s)).filter(Objects::nonNull).toList();}
 public static boolean mergeMap(ServerPlayer p,com.gx.velveteclipse.darkhour.TartarusMapData chart,long cycle){var s=state(p);var g=s.roster.group(id(p));if(g==null)return false;var common=s.maps.computeIfAbsent(g.id(),key->new com.gx.velveteclipse.darkhour.TartarusMapData());boolean dirty=common.reset(cycle);dirty|=common.merge(chart);boolean changed=chart.merge(common);if(dirty)s.m_77762_();return changed;}
 private static boolean available(ServerPlayer p){return p!=null&&p.m_6084_()&&BattleService.current(p)==null&&!com.gx.velveteclipse.exploration.ExplorationService.preparing(p);}
 private static PartyNetwork.Person person(ServerPlayer p,State s,String id){var other=online(p,id);return new PartyNetwork.Person(id,other==null?s.names.getOrDefault(id,"Jogador offline"):other.m_7755_().getString(),other!=null);}
 public static void view(ServerPlayer p,boolean open,String message){
  var s=state(p);s.names.put(id(p),p.m_7755_().getString());s.m_77762_();var group=s.roster.group(id(p));var bag=EquipmentData.load(p);var items=new ArrayList<PartyNetwork.Asset>();var stacks=new HashMap<String,ItemStack>();
  bag.supplies.forEach((key,n)->{var item=Supplies.get(key);if(item!=null&&n>0)items.add(new PartyNetwork.Asset("s:"+key,item.name(),n));});
  for(String key:bag.owned)if(!bag.equipped.containsValue(key)){var gear=EquipmentCatalog.get(key);if(gear!=null)items.add(new PartyNetwork.Asset("g:"+key,gear.name(),1));}
  for(int i=0;i<36;i++){var stack=p.m_150109_().m_8020_(i);if(!stack.m_41619_()){String key="i:"+i;stacks.put(key,stack.m_41777_());items.add(new PartyNetwork.Asset(key,stack.m_41786_().getString(),stack.m_41613_()));}}
  String token=UUID.randomUUID().toString();offers.put(p.m_20148_(),new Offer(token,stacks));var invite=s.roster.invitation(id(p),System.currentTimeMillis());
  PartyNetwork.send(p,new PartyNetwork.View(open,id(p),group==null?"":group.members().get(0),group==null?List.of():group.members().stream().map(key->person(p,s,key)).toList(),p.m_20194_().m_6846_().m_11314_().stream().filter(other->other!=p&&s.roster.group(id(other))==null).limit(200).map(other->person(p,s,id(other))).toList(),items,PlayerPersonaData.load(p).yen(),invite==null?"":person(p,s,invite.from()).name(),message,token));
 }
 public static void handle(ServerPlayer p,PartyNetwork.Request r){
  if(!available(p))return;long now=System.currentTimeMillis();if(now-gate.getOrDefault(p.m_20148_(),0L)<150)return;gate.put(p.m_20148_(),now);
  if(r.action().equals("open")||r.action().equals("refresh")){view(p,r.action().equals("open"),"");return;}
  var offer=offers.get(p.m_20148_());if(offer==null||!offer.token().equals(r.token())){view(p,false,"A lista mudou. Selecione novamente.");return;}offers.remove(p.m_20148_());
  var s=state(p);String message;var target=online(p,r.target());
  switch(r.action()){
   case "create"->message=s.roster.create(id(p));
   case "invite"->{message=target==null?"Jogador offline.":s.roster.invite(id(p),id(target),now);if(target!=null&&s.roster.invitation(id(target),now)!=null){target.m_5661_(Component.m_237113_(p.m_7755_().getString()+" convidou você para uma party. Tab > PARTY para aceitar."),false);view(target,false,"Novo convite para party.");}}
   case "accept"->message=s.roster.accept(id(p),now);
   case "decline"->{s.roster.decline(id(p));message="Convite recusado.";}
   case "leave"->message=s.roster.leave(id(p));
   case "kick"->message=s.roster.kick(id(p),r.target());
   case "yen","item"->{
    if(!available(target)||!s.roster.same(id(p),id(target))){view(p,false,"Escolha um integrante online e fora de combate.");return;}
    boolean ok=false;
    if(r.action().equals("yen")){var from=PlayerPersonaData.load(p);var to=PlayerPersonaData.load(target);ok=PartyTransfers.yen(from,to,r.amount());if(ok){from.save(p);to.save(target);}}
    else if(r.amount()>0&&r.amount()<=999){String key=r.item();int amount=(int)r.amount();var from=EquipmentData.load(p);var to=EquipmentData.load(target);
     if(key.startsWith("s:"))ok=PartyTransfers.supply(from,to,key.substring(2),amount);
     else if(key.startsWith("g:")&&amount==1)ok=PartyTransfers.gear(from,to,key.substring(2));
     else if(key.startsWith("i:")&&offer.stacks().containsKey(key))try{
      int slot=Integer.parseInt(key.substring(2));var stack=p.m_150109_().m_8020_(slot);var expected=offer.stacks().get(key);
      if(ItemStack.m_41728_(stack,expected)&&stack.m_41613_()>=amount){int empty=-1;for(int i=0;i<36;i++)if(target.m_150109_().m_8020_(i).m_41619_()){empty=i;break;}
       if(empty>=0){var delivered=stack.m_41777_();delivered.m_41764_(amount);target.m_150109_().m_6836_(empty,delivered);stack.m_41774_(amount);p.m_150109_().m_6596_();target.m_150109_().m_6596_();ok=true;}
      }
     }catch(NumberFormatException ignored){}
     if(ok){EquipmentData.save(p,from);EquipmentData.save(target,to);}
    }
    message=ok?"Transferência concluída.":"Transferência recusada. Confira quantidade, saldo, item equipado e espaço livre do destinatário.";
    if(ok){BattleService.sync(p,message);BattleService.sync(target,"Recebido de "+p.m_7755_().getString()+".");target.m_5661_(Component.m_237113_("PARTY • Você recebeu "+(r.action().equals("yen")?"¥ "+r.amount():r.amount()+" item(ns)")+" de "+p.m_7755_().getString()+"."),false);view(target,false,"Transferência recebida.");}
   }
   default->{return;}
  }
  s.m_77762_();view(p,false,message);for(var member:p.m_20194_().m_6846_().m_11314_())if(member!=p&&s.roster.same(id(p),id(member)))view(member,false,"");
 }
 @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){offers.remove(e.getEntity().m_20148_());gate.remove(e.getEntity().m_20148_());}
 @SubscribeEvent public static void stop(net.minecraftforge.event.server.ServerStoppedEvent e){offers.clear();gate.clear();}
}
