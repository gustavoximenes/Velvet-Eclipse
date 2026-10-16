package com.gx.velveteclipse.city;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.item.*;
import net.minecraftforge.event.entity.EntityMobGriefingEvent;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.level.*;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;

/** Server-authoritative protection of the authored dimensions, including creative players.
 * Direct construction, furniture changes and the Dark Hour journal remain server operations.
 */
@Mod.EventBusSubscriber(modid="velvet_eclipse")
public final class SceneryProtection {
    public static boolean protectedDimension(ResourceKey<Level> dimension) {
        return protectedLocation(dimension.m_135782_());
    }
    public static boolean protectedLocation(ResourceLocation id) {
        return id.m_135827_().equals("velvet_eclipse")&&(id.m_135815_().equals("city")||id.m_135815_().equals("velvet_room"));
    }
    private static boolean protects(LevelAccessor level) {
        return level instanceof Level world&&protectedDimension(world.m_46472_());
    }
    public static boolean altersScenery(Item item) {
        return altersSceneryType(item.getClass());
    }
    public static boolean altersSceneryType(Class<? extends Item> type) {
        return BlockItem.class.isAssignableFrom(type)||BucketItem.class.isAssignableFrom(type)||SolidBucketItem.class.isAssignableFrom(type)
            ||FlintAndSteelItem.class.isAssignableFrom(type)||FireChargeItem.class.isAssignableFrom(type)||BoneMealItem.class.isAssignableFrom(type)
            ||HangingEntityItem.class.isAssignableFrom(type)||ArmorStandItem.class.isAssignableFrom(type)||EndCrystalItem.class.isAssignableFrom(type);
    }
    @SubscribeEvent public static void breakBlock(BlockEvent.BreakEvent e) {if(protects(e.getLevel()))e.setCanceled(true);}
    @SubscribeEvent public static void place(BlockEvent.EntityPlaceEvent e) {if(protects(e.getLevel()))e.setCanceled(true);}
    @SubscribeEvent public static void bucket(FillBucketEvent e) {if(protects(e.getLevel()))e.setCanceled(true);}
    @SubscribeEvent public static void tool(BlockEvent.BlockToolModificationEvent e) {if(protects(e.getLevel()))e.setCanceled(true);}
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void useBlock(PlayerInteractEvent.RightClickBlock e) {
        // Deny only the held item's placement. Doors, seats, shops and the exit still activate.
        if(protects(e.getLevel())&&altersScenery(e.getItemStack().m_41720_()))e.setUseItem(Event.Result.DENY);
    }
    @SubscribeEvent public static void useItem(PlayerInteractEvent.RightClickItem e) {
        if(protects(e.getLevel())&&altersScenery(e.getItemStack().m_41720_()))e.setCanceled(true);
    }
    @SubscribeEvent public static void explosion(ExplosionEvent.Detonate e) {if(protects(e.getLevel()))e.getAffectedBlocks().clear();}
    @SubscribeEvent public static void piston(PistonEvent.Pre e) {if(protects(e.getLevel()))e.setCanceled(true);}
    @SubscribeEvent public static void portal(BlockEvent.PortalSpawnEvent e) {if(protects(e.getLevel()))e.setCanceled(true);}
    @SubscribeEvent public static void fluid(BlockEvent.FluidPlaceBlockEvent e) {if(protects(e.getLevel()))e.setCanceled(true);}
    @SubscribeEvent public static void trample(BlockEvent.FarmlandTrampleEvent e) {if(protects(e.getLevel()))e.setCanceled(true);}
    @SubscribeEvent public static void grief(EntityMobGriefingEvent e) {if(protects(e.getEntity().m_9236_()))e.setResult(Event.Result.DENY);}
    private SceneryProtection(){}
}
