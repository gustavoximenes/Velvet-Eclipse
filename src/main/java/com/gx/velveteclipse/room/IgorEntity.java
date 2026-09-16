package com.gx.velveteclipse.room;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkHooks;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;
/** Authored sitting pose: no hostile mob AI, equipment or battle membership. */
public final class IgorEntity extends Entity implements GeoEntity {
 private final AnimatableInstanceCache cache=GeckoLibUtil.createInstanceCache(this);
 public IgorEntity(EntityType<? extends IgorEntity> type,Level level){super(type,level);m_20242_(true);m_20331_(true);}
 @Override protected void m_8097_(){}
 @Override protected void m_7378_(CompoundTag tag){}
 @Override protected void m_7380_(CompoundTag tag){}
 @Override public Packet<ClientGamePacketListener> m_5654_(){return NetworkHooks.getEntitySpawningPacket(this);}
 @Override public boolean m_6087_(){return true;}
 @Override public InteractionResult m_6096_(Player player,InteractionHand hand){
  if(player instanceof ServerPlayer server)FusionRoomService.open(server);
  return InteractionResult.SUCCESS;
 }
 @Override public AnimatableInstanceCache getAnimatableInstanceCache(){return cache;}
 @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers){controllers.add(new AnimationController<IgorEntity>(this,"idle",0,s->s.setAndContinue(RawAnimation.begin().thenLoop("misc.idle"))));}
}
