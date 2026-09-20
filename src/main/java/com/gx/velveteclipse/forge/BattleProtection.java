package com.gx.velveteclipse.forge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraft.server.level.ServerPlayer;

/** Includes the results phase: tactical damage uses mod resources, never vanilla damage. */
@Mod.EventBusSubscriber(modid="velvet_eclipse")
public final class BattleProtection {
 @SubscribeEvent public static void damage(LivingAttackEvent e){if(BattleService.current(e.getEntity())!=null)e.setCanceled(true);}
 @SubscribeEvent public static void attack(AttackEntityEvent e){if(BattleService.current(e.getEntity())!=null)e.setCanceled(true);}
 @SubscribeEvent public static void mine(PlayerInteractEvent.LeftClickBlock e){if(BattleService.current(e.getEntity())!=null)e.setCanceled(true);}
 @SubscribeEvent public static void block(PlayerInteractEvent.RightClickBlock e){if(BattleService.current(e.getEntity())!=null)e.setCanceled(true);}
 @SubscribeEvent public static void item(PlayerInteractEvent.RightClickItem e){if(BattleService.current(e.getEntity())!=null)e.setCanceled(true);}
 @SubscribeEvent public static void entity(PlayerInteractEvent.EntityInteract e){if(BattleService.current(e.getEntity())!=null)e.setCanceled(true);}
 @SubscribeEvent public static void entityAt(PlayerInteractEvent.EntityInteractSpecific e){if(BattleService.current(e.getEntity())!=null)e.setCanceled(true);}
 @SubscribeEvent public static void container(PlayerContainerEvent.Open e){if(e.getEntity() instanceof ServerPlayer p&&BattleService.current(p)!=null)p.m_6915_();}
}
