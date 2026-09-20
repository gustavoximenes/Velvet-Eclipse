package com.gx.velveteclipse.room;
import com.gx.velveteclipse.core.fusion.*;
import com.gx.velveteclipse.forge.*;
import com.gx.velveteclipse.menu.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import java.util.*;
@Mod.EventBusSubscriber(modid="velvet_eclipse",bus=Mod.EventBusSubscriber.Bus.FORGE)
public final class FusionRoomService {
 private static final Map<UUID,FusionTicket> tickets=new HashMap<>();
 private static final Map<UUID,Long> greetings=new HashMap<>(),requests=new HashMap<>();
 private static boolean available(ServerPlayer p){return VelvetRoomService.inside(p)&&p.m_6084_()&&BattleService.current(p)==null&&Math.abs(p.m_20185_())<10&&p.m_20186_()>62&&p.m_20186_()<74&&p.m_20189_()>-10&&p.m_20189_()<13;}
 public static void greetLater(ServerPlayer p){greetings.put(p.m_20148_(),p.m_9236_().m_46467_()+15);}
 public static void refresh(ServerPlayer p,String message){tickets.remove(p.m_20148_());if(available(p))reply(p,null,message,false);}
 public static void open(ServerPlayer p){if(available(p)){tickets.remove(p.m_20148_());reply(p,null,"Bem-vindo à Velvet Room. Gostaria de fundir suas Personas?",true);}}
 public static void handle(ServerPlayer p,FusionNetwork.Request r){
  var id=p.m_20148_();if(r.action().equals("close")){tickets.remove(id);return;}
  if(!available(p)){tickets.remove(id);return;}
  long now=p.m_9236_().m_46467_();Long last=requests.put(id,now);if(last!=null&&now-last<2)return;
  var data=PlayerPersonaData.load(p);
  switch(r.action()){
   case "open" -> open(p);
   case "cancel" -> {tickets.remove(id);reply(p,null,"Escolha as Personas para a fusão.",false);}
   case "preview" -> {
    tickets.remove(id);var preview=FusionTransaction.preview(data,r.first(),r.second(),null);
    if(preview==null){reply(p,null,"Selecione duas Personas diferentes que você possui.",false);return;}
    var ticket=new FusionTicket(data,r.first(),r.second(),now);tickets.put(id,ticket);reply(p,ticket,preview.message(),false);
   }
   case "confirm" -> {
    var ticket=tickets.remove(id);
    if(ticket==null||!ticket.confirm(data,r.token(),now)){reply(p,null,"A prévia expirou ou os dados mudaram. Selecione as Personas novamente.",false);return;}
    data.save(p);BattleService.sync(p,"Fusão concluída");reply(p,null,"Fusão concluída. O resultado já está entre suas Personas.",false);
   }
   default -> {}
  }
 }
 private static void reply(ServerPlayer p,FusionTicket ticket,String message,boolean open){
  var data=PlayerPersonaData.load(p);var owned=data.rosterInstances().stream().map(MenuService::describe).toList();
  var recipes=new ArrayList<FusionSnapshot.Recipe>();
  for(int i=0;i<owned.size();i++)for(int k=i+1;k<owned.size();k++){
   var r=FusionTransaction.preview(data,owned.get(i).id(),owned.get(k).id(),null);if(r!=null){var result=MenuService.describe(r.result());recipes.add(new FusionSnapshot.Recipe(r.first(),r.second(),result.id(),result.name(),result.level()));}
  }
  recipes.sort(Comparator.comparingInt(FusionSnapshot.Recipe::level).thenComparing(FusionSnapshot.Recipe::name).thenComparing(FusionSnapshot.Recipe::first));
  var preview=ticket==null?null:FusionTransaction.preview(data,ticket.first,ticket.second,null);
  FusionNetwork.reply(p,new FusionSnapshot(data.playerLevel(),data.playerExperience(),data.playerNextExperience(),owned,recipes,
   preview==null?null:MenuService.describe(preview.result()),preview==null?"":preview.first(),preview==null?"":preview.second(),ticket==null?"":ticket.token,
   preview!=null&&preview.allowed(),preview!=null&&preview.merging(),message,open));
 }
 @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent e){
  if(e.phase!=TickEvent.Phase.END||!(e.player instanceof ServerPlayer p))return;
  var id=p.m_20148_();if(!available(p)){greetings.remove(id);tickets.remove(id);return;}
  if(p.f_19797_%100==0)VelvetRoomService.build(p.m_284548_());
  var time=greetings.get(id);if(time!=null&&p.m_9236_().m_46467_()>=time){greetings.remove(id);open(p);}
 }
 @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){var id=e.getEntity().m_20148_();tickets.remove(id);greetings.remove(id);requests.remove(id);}
}
