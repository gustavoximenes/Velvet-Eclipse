/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.RenderPlayerEvent$Pre
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 */
package com.gx.velveteclipse.client;

import com.gx.velveteclipse.client.PersonaCinematics;
import com.gx.velveteclipse.client.PlayerAnimatorEvokerBridge;
import com.gx.velveteclipse.util.RuntimeBridge;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="velvet_eclipse", bus=Mod.EventBusSubscriber.Bus.FORGE, value={Dist.CLIENT})
public final class ClientPlayerEvokerAnimation {
    private static boolean started;

    private ClientPlayerEvokerAnimation() {
    }

    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre pre) {
        try {
            EvokerHandLayer.attach(pre.getRenderer());
            Object object = RuntimeBridge.clientPlayer();
            if (object == null) {
                return;
            }
            Object object2 = ClientPlayerEvokerAnimation.invokeNoArg(pre, "getEntity");
            if (object2 != object) {
                return;
            }
            boolean bl = PersonaCinematics.skillSequenceActive();
            int n = PersonaCinematics.skillTicks();
            if (bl && n >= 1 && n <= 60) {
                if (!started || !EvokerPlayerAnimation.isPlaying(object)) {
                    started = EvokerPlayerAnimation.play(object);
                }
            } else if (started || EvokerPlayerAnimation.isPlaying(object)) {
                EvokerPlayerAnimation.stop(object);
                started = false;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static Object invokeNoArg(Object object, String string) {
        GenericDeclaration genericDeclaration;
        if (object == null) {
            return null;
        }
        for (genericDeclaration = object.getClass(); genericDeclaration != null; genericDeclaration = ((Class)genericDeclaration).getSuperclass()) {
            try {
                Method method = ((Class)genericDeclaration).getDeclaredMethod(string, new Class[0]);
                method.setAccessible(true);
                return method.invoke(object, new Object[0]);
            }
            catch (Throwable throwable) {
                continue;
            }
        }
        try {
            genericDeclaration = object.getClass().getMethod(string, new Class[0]);
            return ((Method)genericDeclaration).invoke(object, new Object[0]);
        }
        catch (Throwable throwable) {
            return null;
        }
    }
}

