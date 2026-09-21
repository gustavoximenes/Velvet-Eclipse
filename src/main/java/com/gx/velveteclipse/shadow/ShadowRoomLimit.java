package com.gx.velveteclipse.shadow;

import com.gx.velveteclipse.darkhour.*;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Enforces room occupancy for generation, creative placement and operator summons alike. */
@Mod.EventBusSubscriber(modid="velvet_eclipse")
public final class ShadowRoomLimit {
 @SubscribeEvent(priority=net.minecraftforge.eventbus.api.EventPriority.HIGH) public static void join(EntityJoinLevelEvent event){
  if(event.getLevel().f_46443_||!(event.getEntity() instanceof FieldShadowEntity shadow)||!TartarusDungeonService.inside(shadow))return;
  if(TartarusInteriorLayout.fixedFloor(TartarusDungeonService.floor(shadow))||!event.getLevel().m_6443_(FieldShadowEntity.class,shadow.m_20191_().m_82400_(35),other->other!=shadow&&other.m_6084_()&&FieldShadowEntity.sameRoom(shadow,other)).isEmpty())event.setCanceled(true);
 }
}
