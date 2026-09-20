package com.gx.velveteclipse.darkhour;
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
/** Persistent authored cuboids. Not a mob, Shadow, coffin or combat participant. */
public final class TartarusPropEntity extends Entity implements GeoEntity {
 private final AnimatableInstanceCache cache=GeckoLibUtil.createInstanceCache(this);
 public TartarusPropEntity(EntityType<? extends TartarusPropEntity> type,Level level){super(type,level);m_20242_(true);m_20331_(true);}
 @Override protected void m_8097_(){}
 @Override protected void m_7378_(CompoundTag tag){}
 @Override protected void m_7380_(CompoundTag tag){}
 @Override public Packet<ClientGamePacketListener> m_5654_(){return NetworkHooks.getEntitySpawningPacket(this);}
 @Override public boolean m_6087_(){return true;}
 @Override public InteractionResult m_6096_(Player p,InteractionHand h){if(getPersistentData().m_128471_(TartarusPropItem.PLACED)){if(p instanceof ServerPlayer s){if(p.m_150110_().f_35937_&&p.m_6144_())m_146870_();else s.m_5661_(net.minecraft.network.chat.Component.m_237113_("Peça decorativa • SHIFT + clique no criativo para remover."),true);}return InteractionResult.SUCCESS;}if(p instanceof ServerPlayer s)TartarusTerminals.interact(s,this);return InteractionResult.SUCCESS;}
 @Override public AnimatableInstanceCache getAnimatableInstanceCache(){return cache;}
 @Override public void registerControllers(AnimatableManager.ControllerRegistrar c){c.add(new AnimationController<TartarusPropEntity>(this,"idle",0,s->s.setAndContinue(RawAnimation.begin().thenLoop("misc.idle"))));}
}
