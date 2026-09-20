package com.gx.velveteclipse.raid;
import net.minecraftforge.eventbus.api.SubscribeEvent;import net.minecraftforge.fml.common.Mod;import net.minecraftforge.event.level.BlockEvent;import net.minecraftforge.event.entity.player.PlayerInteractEvent;
@Mod.EventBusSubscriber(modid="velvet_eclipse")
public final class RaidProtection {
 @SubscribeEvent public static void place(BlockEvent.EntityPlaceEvent e){if(e.getLevel() instanceof net.minecraft.world.level.Level l&&l.m_46472_().equals(RaidService.DIMENSION))e.setCanceled(true);}
 @SubscribeEvent public static void breakBlock(BlockEvent.BreakEvent e){if(RaidService.inside(e.getPlayer()))e.setCanceled(true);}
 @SubscribeEvent public static void item(PlayerInteractEvent.RightClickItem e){if(RaidService.inside(e.getEntity()))e.setCanceled(true);}
 @SubscribeEvent public static void block(PlayerInteractEvent.RightClickBlock e){if(RaidService.inside(e.getEntity()))e.setCanceled(true);}
 @SubscribeEvent public static void explosion(net.minecraftforge.event.level.ExplosionEvent.Start e){if(e.getLevel().m_46472_().equals(RaidService.DIMENSION))e.setCanceled(true);}
 @SubscribeEvent public static void teleport(net.minecraftforge.event.entity.EntityTeleportEvent e){if(RaidService.inside(e.getEntity()))e.setCanceled(true);}
}
