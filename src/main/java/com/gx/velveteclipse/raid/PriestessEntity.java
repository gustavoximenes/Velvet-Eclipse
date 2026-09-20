package com.gx.velveteclipse.raid;
import net.minecraft.world.entity.*;import net.minecraft.world.level.Level;import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraftforge.network.NetworkHooks;import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;import software.bernie.geckolib.core.animation.*;import software.bernie.geckolib.util.GeckoLibUtil;
/** Stationary non-mob actor. Never participates in movement AI or coffin conversion. */
public final class PriestessEntity extends Entity implements GeoEntity {
 private final AnimatableInstanceCache cache=GeckoLibUtil.createInstanceCache(this);
 public PriestessEntity(EntityType<? extends PriestessEntity> type,Level level){super(type,level);m_20242_(true);m_20331_(true);}
 @Override protected void m_8097_(){}@Override protected void m_7378_(CompoundTag tag){}@Override protected void m_7380_(CompoundTag tag){}
 @Override public Packet<ClientGamePacketListener> m_5654_(){return NetworkHooks.getEntitySpawningPacket(this);}
 @Override public boolean m_6087_(){return true;}
 @Override public net.minecraft.world.InteractionResult m_6096_(net.minecraft.world.entity.player.Player p,net.minecraft.world.InteractionHand h){if(!m_9236_().f_46443_&&p.m_150110_().f_35937_&&p.m_6144_()&&getPersistentData().m_128471_("display_only"))m_146870_();return net.minecraft.world.InteractionResult.SUCCESS;}
 @Override public AnimatableInstanceCache getAnimatableInstanceCache(){return cache;}
 @Override public void registerControllers(AnimatableManager.ControllerRegistrar c){c.add(new AnimationController<PriestessEntity>(this,"main",3,s->s.setAndContinue(m_146909_()<-20?RawAnimation.begin().thenPlay("action.attack"):RawAnimation.begin().thenLoop("misc.idle"))));}
}
