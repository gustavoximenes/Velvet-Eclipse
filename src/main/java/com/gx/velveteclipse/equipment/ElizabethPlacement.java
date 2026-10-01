package com.gx.velveteclipse.equipment;
import com.gx.velveteclipse.room.*;
import com.gx.velveteclipse.darkhour.*;
import net.minecraft.server.level.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
@Mod.EventBusSubscriber(modid="velvet_eclipse")
public final class ElizabethPlacement {
 @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent e){
  if(e.phase!=TickEvent.Phase.END||!(e.player instanceof ServerPlayer p)||p.f_19797_%40!=0)return;
  // VelvetRoomService alone owns the room residents, including legacy migration.
  if(TartarusDungeonService.inside(p)&&TartarusInteriorLayout.floorAt(p.m_20185_())==0&&TartarusDungeonService.ready(TartarusDungeonService.data(p.m_284548_()),0))ensure(p,-21,65,4.5,-60,true);
 }
 private static void ensure(ServerPlayer p,double x,double y,double z,float yaw,boolean merchant){
  var level=p.m_284548_();var at=new BlockPos((int)Math.floor(x),(int)y,(int)Math.floor(z));
  if(p.m_20275_(x,y,z)>4096||!level.m_143319_(new ChunkPos(at).m_45588_()))return;
  var box=new AABB(x-1,y-.2,z-1,x+1,y+3,z+1);
  if(!level.m_45976_(ElizabethEntity.class,box).isEmpty())return;
  var npc=EquipmentRegistry.ELIZABETH.get().m_20615_(level);if(npc==null)return;
  npc.m_6034_(x,y,z);npc.m_146922_(yaw);npc.getPersistentData().m_128379_("elizabeth_merchant",merchant);level.m_7967_(npc);
 }
}
