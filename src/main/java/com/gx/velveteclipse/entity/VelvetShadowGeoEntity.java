package com.gx.velveteclipse.entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

public class VelvetShadowGeoEntity extends Slime implements GeoEntity {
    private final AnimatableInstanceCache geoCache=GeckoLibUtil.createInstanceCache(this);
    public VelvetShadowGeoEntity(EntityType<? extends Slime> type,Level level){super(type,level);}
    @Override protected boolean m_8028_(){return !getPersistentData().m_128471_("raid_actor")&&!m_19880_().contains("velvet_battle_enemy")&&super.m_8028_();}
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<VelvetShadowGeoEntity>(this,"main",3,state->{
            float pitch=m_146909_();
            if(pitch>=30)return state.setAndContinue(RawAnimation.begin().thenPlay("action.physical"));
            if(pitch<=-30)return state.setAndContinue(RawAnimation.begin().thenPlay("action.magic"));
            if(f_20916_>0)return state.setAndContinue(RawAnimation.begin().thenPlay("action.hit"));
            return state.setAndContinue(RawAnimation.begin().thenLoop("misc.idle"));
        }));
    }
    public AnimatableInstanceCache getAnimatableInstanceCache(){return geoCache;}
}
