package com.gx.velveteclipse.darkhour;
import com.gx.velveteclipse.forge.BattleService;import com.gx.velveteclipse.raid.RaidService;
import net.minecraft.nbt.CompoundTag;import net.minecraft.server.level.*;import net.minecraft.world.level.saveddata.SavedData;import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.SubscribeEvent;import net.minecraftforge.fml.common.Mod;import net.minecraftforge.event.entity.player.PlayerEvent;
@Mod.EventBusSubscriber(modid="velvet_eclipse")
public final class DarkHourDeadline {
 public static final class State extends SavedData {
  DarkHourTimer timer=new DarkHourTimer();long lastNanos,nanoRemainder;
  static State load(CompoundTag t){var s=new State();s.timer=DarkHourTimer.load(t);return s;}
  @Override public CompoundTag m_7176_(CompoundTag t){return timer.save();}
 }
 private static State state(ServerLevel l){return l.m_8895_().m_164861_(State::load,State::new,"velvet_dark_hour_clock");}
 public static int start(net.minecraft.commands.CommandSourceStack source){var l=source.m_81377_().m_129783_();var s=state(l);s.timer.running=false;s.lastNanos=0;s.m_77762_();return net.minecraft.server.commands.TimeCommand.m_139077_(source,DarkHourCycle.START);}
 public static void tick(ServerLevel l){
  var s=state(l);long now=System.nanoTime(),delta=s.lastNanos==0?0:Math.max(0,now-s.lastNanos)+s.nanoRemainder,elapsed=delta/1_000_000;s.nanoRemainder=delta%1_000_000;s.lastNanos=now;
  boolean ended=s.timer.advance(l.m_46468_(),elapsed);
  if(ended){if(DarkHourCycle.active(l.m_46468_()))l.m_8615_((Math.floorDiv(l.m_46468_(),24000)+1)*24000);RaidService.endDarkHour();var dungeon=l.m_7654_().m_129880_(TartarusDungeonService.DIMENSION);if(dungeon!=null)TartarusDungeonService.cancelTransfers(dungeon);for(var p:l.m_7654_().m_6846_().m_11314_())evacuate(p);s.m_77762_();}
  else if(s.timer.running)l.m_8615_(s.timer.skyTime());
  if(ended||l.m_46467_()%20==0){if(s.timer.running)s.m_77762_();for(var p:l.m_7654_().m_6846_().m_11314_())TartarusMapNetwork.send(p,new TartarusMapNetwork.Clock((int)s.timer.remaining));}
 }
 private static boolean needsExit(ServerPlayer p){return TartarusDungeonService.inside(p)||RaidService.inside(p)||com.gx.velveteclipse.room.VelvetRoomService.inside(p)&&p.getPersistentData().m_128469_("velvet_eclipse:room_return").m_128461_("dimension").equals("velvet_eclipse:tartarus");}
 public static void evacuate(ServerPlayer p){if(!needsExit(p))return;BattleService.abortEncounter(p);com.gx.velveteclipse.exploration.ExplorationService.expire(p);p.m_6915_();p.m_8127_();p.getPersistentData().m_128473_("velvet_eclipse:room_return");p.getPersistentData().m_128473_("velvet_eclipse:raid_return");TartarusDungeonService.leave(p);p.m_5661_(Component.m_237113_("A Dark Hour terminou. Você retornou ao mundo."),false);}
 @SubscribeEvent(priority=net.minecraftforge.eventbus.api.EventPriority.LOWEST) public static void login(PlayerEvent.PlayerLoggedInEvent e){if(e.getEntity() instanceof ServerPlayer p){var l=p.m_20194_().m_129783_();var dungeon=p.m_20194_().m_129880_(TartarusDungeonService.DIMENSION);boolean stale=dungeon!=null&&p.getPersistentData().m_128441_("velvet_eclipse:tartarus_entered_cycle")&&p.getPersistentData().m_128454_("velvet_eclipse:tartarus_entered_cycle")!=TartarusDungeonService.data(dungeon).progress.cycle;if(!DarkHourService.active(l)||stale)evacuate(p);TartarusMapNetwork.send(p,new TartarusMapNetwork.Clock((int)state(l).timer.remaining));}}
}
