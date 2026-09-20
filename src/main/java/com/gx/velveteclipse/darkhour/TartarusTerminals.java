package com.gx.velveteclipse.darkhour;
import com.gx.velveteclipse.forge.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.entity.player.PlayerEvent;
import java.util.*;
@Mod.EventBusSubscriber(modid="velvet_eclipse")
public final class TartarusTerminals {
 private static final String DISCOVERED="velvet_eclipse:terminals",PROP="tartarus_prop";
 public static void discover(ServerPlayer p,int f){if(!TartarusInteriorLayout.fixedFloor(f))return;var floors=TartarusProgress.discovered(p.getPersistentData().m_128465_(DISCOVERED));if(floors.add(f)){p.getPersistentData().m_128385_(DISCOVERED,floors.stream().mapToInt(Integer::intValue).toArray());p.m_5661_(Component.m_237113_(f==0?"Terminal do hall descoberto":"Terminal do andar "+f+" descoberto"),true);}}
 public static void discoverFloor(ServerLevel l,int f){for(var p:l.m_6907_())if(TartarusDungeonService.floor(p)==f)discover(p,f);}
 public static void ensureProps(ServerLevel l,TartarusDungeonService.State s,int f){
  for(var spec:TartarusInteriorLayout.props(s.progress.seed(l.m_7328_(),f),f,s.plan(l,f))){
   String id=f+":"+spec.kind()+":"+spec.index();if(spec.kind().equals("treasure")&&s.progress.treasures.contains(f+":"+spec.index()))continue;
   if(s.props.containsKey(id))continue;
   var e=TartarusPropRegistry.type(spec.kind()).m_20615_(l);if(e==null)continue;double x=TartarusInteriorLayout.origin(f)+spec.at().x()+.5,z=spec.at().z()+.5;l.m_46745_(new BlockPos((int)x,65,(int)z));e.m_6034_(x,65,z);e.m_146922_(spec.yaw());
   e.getPersistentData().m_128359_(PROP,id);e.getPersistentData().m_128356_("tartarus_generation",s.progress.generation(f));
   var t=new CompoundTag();t.m_128359_("entity",e.m_20148_().toString());s.props.put(id,t);if(!l.m_7967_(e))s.props.remove(id);s.m_77762_();
  }
 }
 private static boolean available(ServerPlayer p){return p.m_6084_()&&TartarusDungeonService.inside(p)&&BattleService.current(p)==null&&!p.m_20159_()&&!com.gx.velveteclipse.exploration.ExplorationService.blocksBattleStart(p);}
 private static boolean near(ServerPlayer p,TartarusPropEntity e){if(!available(p)||e.m_20275_(p.m_20185_(),p.m_20186_(),p.m_20189_())>25||TartarusDungeonService.floor(e)!=TartarusDungeonService.floor(p))return false;var s=TartarusDungeonService.data(p.m_284548_());var slot=s.props.get(e.getPersistentData().m_128461_(PROP));return TartarusDungeonService.ready(s,TartarusDungeonService.floor(p))&&slot!=null&&slot.m_128461_("entity").equals(e.m_20148_().toString());}
 public static void interact(ServerPlayer p,TartarusPropEntity e){
  if(!near(p,e))return;int f=TartarusDungeonService.floor(p);var s=TartarusDungeonService.data(p.m_284548_());String id=e.getPersistentData().m_128461_(PROP);
  if(e.m_6095_()==TartarusPropRegistry.TERMINAL.get()){discover(p,f);var floors=new ArrayList<>(TartarusProgress.discovered(p.getPersistentData().m_128465_(DISCOVERED)));Collections.reverse(floors);TartarusTerminalNetwork.reply(p,floors);return;}
  if(e.m_6095_()==TartarusPropRegistry.MONAD.get()){p.m_5661_(Component.m_237113_("A porta de Monad permanece selada."),true);return;}
  if(e.m_6095_()!=TartarusPropRegistry.TREASURE.get())return;String key=f+":"+id.substring(id.lastIndexOf(':')+1);
  if(!s.progress.treasures.add(key))return;
  var data=PlayerPersonaData.load(p);long yen=250L+f*45L;data.addYen(yen);data.addPulseTonics(1);data.save(p);s.props.remove(id);s.m_77762_();e.m_146870_();BattleService.sync(p,"Tesouro • ¥ "+yen+" e 1 Pulse Tonic");com.gx.velveteclipse.shuffle.ShuffleService.status(p,"");p.m_5661_(Component.m_237113_("Tesouro recolhido: ¥ "+yen+" e 1 Pulse Tonic"),false);
 }
 public static void request(ServerPlayer p,TartarusTerminalNetwork.Request r){
  if(!available(p))return;
  if(r.floor()==-1){if(p.m_284548_().m_6815_(r.entity()) instanceof TartarusPropEntity e)interact(p,e);return;}
  int here=TartarusDungeonService.floor(p);var at=TartarusInteriorLayout.terminal(here);if(at==null||p.m_20275_(TartarusInteriorLayout.origin(here)+at.x()+.5,65,at.z()+.5)>25)return;
  var s=TartarusDungeonService.data(p.m_284548_());if(!TartarusDungeonService.ready(s,here)||!TartarusProgress.discovered(p.getPersistentData().m_128465_(DISCOVERED)).contains(r.floor()))return;
  TartarusDungeonService.teleportToFloor(p,r.floor());
 }
 @SubscribeEvent public static void clonePlayer(PlayerEvent.Clone e){e.getEntity().getPersistentData().m_128385_(DISCOVERED,e.getOriginal().getPersistentData().m_128465_(DISCOVERED));}
}
