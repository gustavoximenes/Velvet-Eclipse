package com.gx.velveteclipse.darkhour;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
/** The generated dungeon cannot be edited, including creative block placement. */
@Mod.EventBusSubscriber(modid="velvet_eclipse")
public final class TartarusProtection {
 @SubscribeEvent public static void place(BlockEvent.EntityPlaceEvent e){if(e.getLevel() instanceof net.minecraft.world.level.Level l&&l.m_46472_().equals(TartarusDungeonService.DIMENSION))e.setCanceled(true);}
 @SubscribeEvent public static void breakBlock(BlockEvent.BreakEvent e){if(TartarusDungeonService.inside(e.getPlayer()))e.setCanceled(true);}
 @SubscribeEvent public static void useBlock(PlayerInteractEvent.RightClickBlock e){if(TartarusDungeonService.inside(e.getEntity())&&e.getItemStack().m_41720_() instanceof net.minecraft.world.item.BlockItem)e.setCanceled(true);}
 @SubscribeEvent public static void bucket(net.minecraftforge.event.entity.player.FillBucketEvent e){if(TartarusDungeonService.inside(e.getEntity()))e.setCanceled(true);}
}
