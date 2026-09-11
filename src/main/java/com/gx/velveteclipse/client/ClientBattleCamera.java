package com.gx.velveteclipse.client;

import com.gx.velveteclipse.client.exploration.*;
import com.gx.velveteclipse.exploration.ExplorationRules.Mode;
import com.gx.velveteclipse.util.RuntimeBridge;
import net.minecraft.client.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** The single camera owner: skill > entry > battle > exploration > vanilla. */
@Mod.EventBusSubscriber(modid="velvet_eclipse",bus=Mod.EventBusSubscriber.Bus.FORGE,value=Dist.CLIENT)
public final class ClientBattleCamera {
    private static CameraType previousCamera;
    private static boolean battle;
    private static boolean releasedCursor;
    private static double[] anchor;
    private static float lockedYaw;
    @SubscribeEvent public static void onClientTick(TickEvent.ClientTickEvent e){
        PersonaCinematics.tick(e);if(e.phase!=TickEvent.Phase.END)return;
        ExplorationClient.tick();var mc=Minecraft.m_91087_();
        boolean valid=mc.f_91074_!=null&&mc.f_91073_!=null&&mc.f_91074_.m_6084_();
        boolean active=valid&&ClientBattleState.snapshot().active();
        if(active!=battle)ExplorationCamera.reset();
        boolean controlled=valid&&(active||ExplorationClient.enabled());
        if(controlled){if(previousCamera==null)previousCamera=mc.f_91066_.m_92176_();mc.f_91066_.m_92157_(CameraType.THIRD_PERSON_BACK);}
        else restore();
        if(active){
            if(!battle){anchor=RuntimeBridge.playerPosition(mc.f_91074_);lockedYaw=mc.f_91074_.m_146908_();}
            if(anchor!=null&&!PersonaCinematics.meleeActive())RuntimeBridge.freezeLocalClientPlayerPosition(anchor);
            RuntimeBridge.setEntityRotation(mc.f_91074_,lockedYaw,2);
        }else anchor=null;
        if(active&&mc.f_91080_==null){if(mc.f_91067_.m_91600_()){mc.f_91067_.m_91602_();releasedCursor=true;}}
        else if(!active&&releasedCursor){if(mc.f_91080_==null&&valid)mc.f_91067_.m_91601_();releasedCursor=false;}
        battle=active;
    }
    private static void restore(){
        if(previousCamera!=null){Minecraft.m_91087_().f_91066_.m_92157_(previousCamera);previousCamera=null;}
        ExplorationCamera.reset();anchor=null;
    }
    @SubscribeEvent public static void onCameraAngles(ViewportEvent.ComputeCameraAngles e){
        if(previousCamera==null)return;
        Mode mode=ExplorationClient.mode();
        if(mode==Mode.SKILL_CINEMATIC||mode==Mode.BATTLE)PersonaCinematics.applyCamera(e);
        else if(mode!=Mode.NORMAL)ExplorationCamera.apply(e,mode==Mode.BATTLE_ENTRY);
    }
    @SubscribeEvent public static void fov(ViewportEvent.ComputeFov e){
        if(previousCamera!=null&&ExplorationClient.swordVisible())e.setFOV(ExplorationClient.entry()?66:ExplorationCamera.PARAMETERS.fov());
    }
}
