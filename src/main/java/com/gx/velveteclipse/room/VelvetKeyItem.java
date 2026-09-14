package com.gx.velveteclipse.room;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;

public final class VelvetKeyItem extends Item {
 public VelvetKeyItem(){super(new Item.Properties().m_41487_(1).m_41486_().m_41497_(Rarity.RARE));}
 @Override public InteractionResultHolder<ItemStack> m_7203_(Level level,Player player,InteractionHand hand){
  if(player instanceof ServerPlayer p)VelvetRoomService.summonDoor(p);return InteractionResultHolder.m_19092_(player.m_21120_(hand),level.f_46443_);
 }
 @Override public InteractionResult m_6225_(UseOnContext context){if(context.m_43723_() instanceof ServerPlayer p)VelvetRoomService.summonDoor(p);return InteractionResult.SUCCESS;}
}
