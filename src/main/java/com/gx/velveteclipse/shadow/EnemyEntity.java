package com.gx.velveteclipse.shadow;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

/** A revealed Shadow actor, separate from summonable Personas and roaming encounter tokens. */
public final class EnemyEntity extends PathfinderMob implements GeoEntity {
 private final AnimatableInstanceCache cache=GeckoLibUtil.createInstanceCache(this);
 public EnemyEntity(EntityType<? extends EnemyEntity> type,Level level){super(type,level);m_21530_();m_21557_(true);m_20242_(true);m_20331_(true);}
 @Override protected void m_8099_(){}
 @Override public AnimatableInstanceCache getAnimatableInstanceCache(){return cache;}
 @Override public void registerControllers(AnimatableManager.ControllerRegistrar c){c.add(new AnimationController<EnemyEntity>(this,"main",3,s->{String clip=m_146909_()<=-30?"action.magic":m_146909_()>=30?"action.physical":"misc.idle";return s.setAndContinue(clip.startsWith("action")?RawAnimation.begin().thenPlay(clip):RawAnimation.begin().thenLoop(clip));}));}
}
