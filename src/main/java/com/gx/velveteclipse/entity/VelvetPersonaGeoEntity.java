/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.monster.Vex
 *  net.minecraft.world.level.Level
 *  software.bernie.geckolib.animatable.GeoEntity
 *  software.bernie.geckolib.core.animatable.GeoAnimatable
 *  software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache
 *  software.bernie.geckolib.core.animation.AnimatableManager$ControllerRegistrar
 *  software.bernie.geckolib.core.animation.AnimationController
 *  software.bernie.geckolib.core.animation.AnimationState
 *  software.bernie.geckolib.core.animation.RawAnimation
 *  software.bernie.geckolib.core.object.PlayState
 *  software.bernie.geckolib.util.GeckoLibUtil
 */
package com.gx.velveteclipse.entity;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class VelvetPersonaGeoEntity
extends Vex
implements GeoEntity {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);
    private String personaId;
    private static final net.minecraft.network.syncher.EntityDataAccessor<String> SUMMONER=net.minecraft.network.syncher.SynchedEntityData.m_135353_(VelvetPersonaGeoEntity.class,net.minecraft.network.syncher.EntityDataSerializers.f_135030_);
    @Override protected void m_8097_(){super.m_8097_();f_19804_.m_135372_(SUMMONER,"");}
    public void summoner(java.util.UUID id){f_19804_.m_135381_(SUMMONER,id.toString());}
    public String summoner(){return f_19804_.m_135370_(SUMMONER);}
    private final boolean evokerFxType;

    public VelvetPersonaGeoEntity(EntityType<? extends Vex> entityType, Level level) {
        super(entityType, level);
        String string = String.valueOf(entityType).toLowerCase();
        this.evokerFxType = string.contains("evoker");
        this.personaId = this.evokerFxType ? "evoker_fx" : string.contains("arsene") ? "arsene" : string.contains("orpheus") ? "orpheus" : string.contains("izanagi") ? "izanagi" : "cinder_wisp";
        var key=net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getKey(entityType);if(key!=null&&key.m_135815_().startsWith("persona_"))this.personaId=key.m_135815_().substring(8);
    }

    public void setPersonaId(String string) {
        if (string != null && !string.isBlank()) {
            this.personaId = string;
        }
    }

    public String personaId() {
        return this.personaId;
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        AnimationController animationController = new AnimationController((GeoAnimatable)this, "main", 3, this::animationPredicate);
        try {
            Method method = null;
            for (Method method2 : controllerRegistrar.getClass().getMethods()) {
                Class<?> clazz;
                if (!"add".equals(method2.getName()) || method2.getParameterCount() != 1 || !AnimationController.class.isAssignableFrom(clazz = method2.getParameterTypes()[0]) && !clazz.isArray()) continue;
                method = method2;
                if (!clazz.isArray()) break;
            }
            if (method == null) {
                throw new NoSuchMethodException("ControllerRegistrar.add");
            }
            Class<?> clazz = method.getParameterTypes()[0];
            if (clazz.isArray()) {
                Object object = Array.newInstance(clazz.getComponentType(), 1);
                Array.set(object, 0, animationController);
                method.invoke(controllerRegistrar, object);
            } else {
                method.invoke(controllerRegistrar, animationController);
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private PlayState animationPredicate(AnimationState<VelvetPersonaGeoEntity> animationState) {
        String string = this.selectAnimation();
        if (string.startsWith("action.")) {
            return animationState.setAndContinue(RawAnimation.begin().thenPlay(string));
        }
        return animationState.setAndContinue(RawAnimation.begin().thenLoop(string));
    }

    private String selectAnimation() {
        if (this.isEvokerFx()) {
            return "action.evoker_full";
        }
        float f = this.readPitch();
        if (f <= -30.0f) {
            return "action.magic";
        }
        if (f >= 30.0f) {
            return "action.physical";
        }
        return "misc.idle";
    }

    private boolean isEvokerFx() {
        if (this.evokerFxType) {
            return true;
        }
        if (this.personaId != null && this.personaId.startsWith("evoker_fx")) {
            return true;
        }
        for (String string : new String[]{"getType", "m_6095_"}) {
            try {
                Object object = ((Object)((Object)this)).getClass().getMethod(string, new Class[0]).invoke((Object)this, new Object[0]);
                if (!String.valueOf(object).toLowerCase().contains("evoker")) continue;
                return true;
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        return false;
    }

    private float readPitch() {
        for (String string : new String[]{"getXRot", "m_146909_"}) {
            try {
                Object object = ((Object)((Object)this)).getClass().getMethod(string, new Class[0]).invoke((Object)this, new Object[0]);
                if (!(object instanceof Number)) continue;
                Number number = (Number)object;
                return number.floatValue();
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        return 0.0f;
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}

