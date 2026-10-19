package com.gx.velveteclipse.city;

import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.network.NetworkHooks;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

/** The type fixes the shop on both sides; clients cannot choose a different store. */
public final class CityShopActor extends Entity implements GeoEntity {
    private final String shop;
    private boolean managed;
    private final AnimatableInstanceCache cache=GeckoLibUtil.createInstanceCache(this);
    public CityShopActor(EntityType<? extends CityShopActor> type,Level level,String shop){
        super(type,level);this.shop=shop;m_20242_(true);m_20331_(true);
        m_6593_(com.gx.velveteclipse.localization.ModText.component(CityShopAccess.name(shop)));
    }
    public String shop(){return shop;}
    public boolean managed(){return managed;}
    public void manage(){managed=true;}
    @Override protected void m_8097_(){}
    @Override protected void m_7378_(CompoundTag tag){managed=tag.m_128471_("CityShopManaged");}
    @Override protected void m_7380_(CompoundTag tag){tag.m_128379_("CityShopManaged",managed);}
    @Override public Packet<ClientGamePacketListener> m_5654_(){return NetworkHooks.getEntitySpawningPacket(this);}
    @Override public boolean m_6087_(){return true;}
    @Override public boolean m_6097_(){return false;}
    @Override public boolean m_5830_(){return false;}
    @Override public boolean m_6469_(DamageSource source,float amount){
        // Creative display copies are removable. City attendants/markers are protected.
        if(!managed&&source.m_7639_() instanceof Player p&&p.m_150110_().f_35937_&&!m_9236_().f_46443_)m_146870_();
        return false;
    }
    @Override public InteractionResult m_6096_(Player player,InteractionHand hand){
        if(hand==InteractionHand.MAIN_HAND&&player instanceof ServerPlayer server)CityService.shop(server,this);
        return InteractionResult.SUCCESS;
    }
    @Override public AnimatableInstanceCache getAnimatableInstanceCache(){return cache;}
    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers){
        controllers.add(new AnimationController<CityShopActor>(this,"idle",0,state->state.setAndContinue(RawAnimation.begin().thenLoop("misc.idle"))));
    }
}
