/*
 * Decompiled with CFR 0.152.
 */
package com.gx.velveteclipse.client;

import com.gx.velveteclipse.VelvetEclipseMod;
import java.lang.reflect.Constructor;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.WeakHashMap;

public final class PlayerAnimatorEvokerBridge {
    private static final Map<Object, Object> LAYERS = new WeakHashMap<Object, Object>();
    private static final Map<Object, Boolean> PLAYING = new WeakHashMap<Object, Boolean>();
    private static boolean warned;

    private PlayerAnimatorEvokerBridge() {
    }

    public static boolean play(Object object) {
        if (object == null) {
            return false;
        }
        try {
            Object object2;
            Object object3 = LAYERS.get(object);
            if (object3 == null) {
                object3 = PlayerAnimatorEvokerBridge.createAndAttachLayer(object);
                if (object3 == null) {
                    return false;
                }
                LAYERS.put(object, object3);
            }
            if ((object2 = PlayerAnimatorEvokerBridge.lookupAnimation()) == null) {
                PlayerAnimatorEvokerBridge.warnOnce("PlayerAnimator could not find velvet_eclipse:evoker_summon");
                return false;
            }
            Object object4 = PlayerAnimatorEvokerBridge.createKeyframePlayer(object2);
            if (object4 == null) {
                PlayerAnimatorEvokerBridge.warnOnce("PlayerAnimator KeyframeAnimationPlayer could not be constructed");
                return false;
            }
            if (!PlayerAnimatorEvokerBridge.invokeOneArg(object3, "setAnimation", object4)) {
                PlayerAnimatorEvokerBridge.warnOnce("PlayerAnimator ModifierLayer.setAnimation could not be invoked");
                return false;
            }
            PLAYING.put(object, Boolean.TRUE);
            return true;
        }
        catch (Throwable throwable) {
            PlayerAnimatorEvokerBridge.warnOnce("PlayerAnimator Evoker integration failed: " + String.valueOf(throwable));
            return false;
        }
    }

    public static void stop(Object object) {
        if (object == null) {
            return;
        }
        try {
            Object object2 = LAYERS.get(object);
            if (object2 != null) {
                PlayerAnimatorEvokerBridge.invokeOneArgAllowNull(object2, "setAnimation", null);
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        PLAYING.remove(object);
    }

    public static boolean isPlaying(Object object) {
        return Boolean.TRUE.equals(PLAYING.get(object));
    }

    private static Object createAndAttachLayer(Object object) throws Exception {
        
        Class<?> clazz = Class.forName("dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess");
        Method method2 = null;
        for (Method method3 : clazz.getMethods()) {
            if (!"getPlayerAnimLayer".equals(method3.getName()) || method3.getParameterCount() != 1) continue;
            method2 = method3;
            break;
        }
        if (method2 == null) {
            throw new NoSuchMethodException("PlayerAnimationAccess.getPlayerAnimLayer");
        }
        Object object2 = method2.invoke(null, object);
        if (object2 == null) {
            return null;
        }
        Class<?> clazz2 = Class.forName("dev.kosmx.playerAnim.api.layered.ModifierLayer");
        Object obj = clazz2.getConstructor(new Class[0]).newInstance(new Object[0]);
        Method method3 = null;
        for (Method method4 : object2.getClass().getMethods()) {
            if (!"addAnimLayer".equals(method4.getName()) || method4.getParameterCount() != 2) continue;
            method3 = method4;
            break;
        }
        if (method3 == null) {
            throw new NoSuchMethodException("AnimationStack.addAnimLayer");
        }
        method3.invoke(object2, 900, obj);
        return obj;
    }

    private static Object lookupAnimation() throws Exception {
        GenericDeclaration genericDeclaration;
        Class<?> clazz = Class.forName("net.minecraft.resources.ResourceLocation");
        Object object = null;
        try {
            genericDeclaration = clazz.getConstructor(String.class, String.class);
            object = ((Constructor)genericDeclaration).newInstance("velvet_eclipse", "evoker_summon");
        }
        catch (Throwable throwable) {
            for (Method method : clazz.getMethods()) {
                if (("fromNamespaceAndPath".equals(method.getName()) || "tryBuild".equals(method.getName())) && method.getParameterCount() == 2 && (object = method.invoke(null, "velvet_eclipse", "evoker_summon")) != null) break;
            }
        }
        if (object == null) {
            throw new IllegalStateException("ResourceLocation creation failed");
        }
        genericDeclaration = Class.forName("dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry");
        for (Method method : ((Class)genericDeclaration).getMethods()) {
            if (!"getAnimation".equals(method.getName()) || method.getParameterCount() != 1) continue;
            return method.invoke(null, object);
        }
        throw new NoSuchMethodException("PlayerAnimationRegistry.getAnimation");
    }

    private static Object createKeyframePlayer(Object object) throws Exception {
        Class<?> clazz = Class.forName("dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer");
        for (Constructor<?> constructor : clazz.getConstructors()) { if (constructor.getParameterCount()==2 && constructor.getParameterTypes()[1]==int.class) return constructor.newInstance(object, Math.max(0, PersonaCinematics.skillTicks()-1)); }
        for (Constructor<?> constructor : clazz.getConstructors()) {
            Class<?>[] classArray = constructor.getParameterTypes();
            if (classArray.length != 1 || !classArray[0].isInstance(object)) continue;
            return constructor.newInstance(object);
        }
        for (Constructor<?> constructor : clazz.getConstructors()) {
            if (constructor.getParameterCount() != 1) continue;
            try {
                return constructor.newInstance(object);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        return null;
    }

    private static boolean invokeOneArg(Object object, String string, Object object2) {
        if (object == null || object2 == null) {
            return false;
        }
        for (Method method : object.getClass().getMethods()) {
            Class<?> clazz;
            if (!string.equals(method.getName()) || method.getParameterCount() != 1 || !(clazz = method.getParameterTypes()[0]).isInstance(object2) && !clazz.isAssignableFrom(object2.getClass())) continue;
            try {
                method.invoke(object, object2);
                return true;
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        return false;
    }

    private static boolean invokeOneArgAllowNull(Object object, String string, Object object2) {
        if (object == null) {
            return false;
        }
        for (Method method : object.getClass().getMethods()) {
            if (!string.equals(method.getName()) || method.getParameterCount() != 1) continue;
            try {
                method.invoke(object, object2);
                return true;
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        return false;
    }

    private static void warnOnce(String string) {
        if (warned) {
            return;
        }
        warned = true;
        try {
            VelvetEclipseMod.LOGGER.error(string);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }
}

