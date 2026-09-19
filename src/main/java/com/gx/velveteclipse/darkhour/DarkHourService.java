package com.gx.velveteclipse.darkhour;
import com.gx.velveteclipse.forge.*;
import com.gx.velveteclipse.network.*;
import net.minecraft.core.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.*;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import static net.minecraft.commands.Commands.m_82127_;

@Mod.EventBusSubscriber(modid="velvet_eclipse")
public final class DarkHourService {
 private static final String FROZEN="velvet_eclipse_dark_hour";
 public static boolean active(Level level){return level!=null&&level.m_46472_().equals(Level.f_46428_)&&DarkHourCycle.active(level.m_46468_());}
 public static boolean eligibleType(Class<?> type){return Mob.class.isAssignableFrom(type)&&!com.gx.velveteclipse.entity.VelvetPersonaGeoEntity.class.isAssignableFrom(type)&&!com.gx.velveteclipse.entity.VelvetShadowGeoEntity.class.isAssignableFrom(type);}
 public static boolean eligible(Entity entity){return eligibleType(entity.getClass())&&!ShadowRegistry.isShadow(entity)&&!entity.m_19880_().contains("velvet_persona_actor")&&!entity.m_19880_().contains("velvet_battle_actor");}
 @SubscribeEvent public static void commands(RegisterCommandsEvent e){
  for(String name:new String[]{"dark_hour","darkhour"})e.getDispatcher().register(m_82127_("time").requires(s->s.m_6761_(2)).then(m_82127_("set").then(m_82127_(name).executes(c->net.minecraft.server.commands.TimeCommand.m_139077_(c.getSource(),DarkHourCycle.START)))));
  e.getDispatcher().register(m_82127_("locate").requires(s->s.m_6761_(2)).then(m_82127_("tartarus").executes(c->TartarusService.locate(c.getSource()))).then(m_82127_("structure").then(m_82127_("velvet_eclipse:tartarus").executes(c->TartarusService.locate(c.getSource())))));
  e.getDispatcher().register(m_82127_("velvet").then(m_82127_("tartarus").executes(c->TartarusService.locate(c.getSource())).then(m_82127_("owner").requires(s->s.m_6761_(4)).then(net.minecraft.commands.Commands.m_82129_("player",net.minecraft.commands.arguments.EntityArgument.m_91466_()).executes(c->TartarusService.owner(c.getSource(),net.minecraft.commands.arguments.EntityArgument.m_91474_(c,"player")))))));
 }
 @SubscribeEvent public static void levelTick(TickEvent.LevelTickEvent e){if(e.phase==TickEvent.Phase.END&&e.level instanceof ServerLevel level&&level.m_46472_().equals(Level.f_46428_))TartarusService.tick(level);}
 @SubscribeEvent public static void living(LivingEvent.LivingTickEvent e){
  var entity=e.getEntity();if(!(entity instanceof Mob mob)||entity.m_9236_().f_46443_)return;
  CompoundTag data=entity.getPersistentData();
  if(active(entity.m_9236_())&&eligible(entity)){
   if(!data.m_128441_(FROZEN)){var old=new CompoundTag();old.m_128379_("noAi",mob.m_21525_());old.m_128347_("x",mob.m_20185_());old.m_128347_("y",mob.m_20186_());old.m_128347_("z",mob.m_20189_());data.m_128365_(FROZEN,old);}
   mob.m_21557_(true);mob.m_20256_(Vec3.f_82478_);var old=data.m_128469_(FROZEN);mob.m_6034_(old.m_128459_("x"),old.m_128459_("y"),old.m_128459_("z"));
   e.setCanceled(true);
  }else if(data.m_128441_(FROZEN)){mob.m_21557_(data.m_128469_(FROZEN).m_128471_("noAi"));data.m_128473_(FROZEN);}
 }
 @SubscribeEvent public static void damage(LivingAttackEvent e){if(active(e.getEntity().m_9236_())&&eligible(e.getEntity()))e.setCanceled(true);}
 @SubscribeEvent public static void spawn(MobSpawnEvent.FinalizeSpawn e){
  if(ShadowRegistry.isShadow(e.getEntity())&&!active(e.getEntity().m_9236_())&&(e.getSpawnType()==MobSpawnType.NATURAL||e.getSpawnType()==MobSpawnType.CHUNK_GENERATION))e.setSpawnCancelled(true);
 }
}
