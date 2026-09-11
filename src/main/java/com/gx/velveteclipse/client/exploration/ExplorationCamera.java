package com.gx.velveteclipse.client.exploration;

import net.minecraft.client.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import net.minecraftforge.client.event.ViewportEvent;
import java.lang.reflect.Method;

public final class ExplorationCamera {
    public static final CameraPath.Parameters PARAMETERS=CameraPath.PARAMETERS;
    private static final Method POSITION=positionMethod();
    private static Vec3 previous;
    private static long lastFrame;
    private static Method positionMethod(){
        for(String name:new String[]{"m_90584_","setPosition"})try{Method m=Camera.class.getDeclaredMethod(name,double.class,double.class,double.class);m.setAccessible(true);return m;}catch(ReflectiveOperationException ignored){}
        throw new IllegalStateException("Camera position method unavailable");
    }
    public static void reset(){previous=null;lastFrame=0;}
    public static void apply(ViewportEvent.ComputeCameraAngles e,boolean entry){
        var mc=Minecraft.m_91087_();var p=mc.f_91074_;if(p==null||mc.f_91073_==null)return;
        float partial=(float)e.getPartialTick();
        // m_20318_ is interpolated FEET. m_20299_ is interpolated eyes.
        Vec3 feet=p.m_20318_(partial);
        var frame=CameraPath.exploration(feet,entry?ExplorationClient.yaw()-12:p.m_146908_(),p.m_146909_(),entry);
        Vec3 target=frame.pivot(),desired=frame.desired();
        long now=System.nanoTime();double dt=lastFrame==0?1:Math.min(.1,(now-lastFrame)*1e-9);lastFrame=now;
        Vec3 smooth=previous==null||previous.m_82554_(desired)>8?desired:previous.m_165921_(desired,1-Math.exp(-dt*16));
        Vec3 position=collide(target,smooth);previous=position;
        Vec3 focus=frame.focus();
        place(e,position,focus,0);
    }
    public static Vec3 collide(Vec3 target,Vec3 desired){
        var mc=Minecraft.m_91087_();var p=mc.f_91074_;if(p==null||mc.f_91073_==null)return desired;
        return CameraPath.clip(target,desired,(origin,end)->{
            var hit=mc.f_91073_.m_45547_(new ClipContext(origin,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));
            return hit.m_6662_()==HitResult.Type.MISS?Double.POSITIVE_INFINITY:hit.m_82450_().m_82554_(origin);
        });
    }

    public static void place(ViewportEvent.ComputeCameraAngles e,Vec3 position,Vec3 focus,float roll){
        Vec3 view=focus.m_82546_(position);
        e.setYaw((float)Math.toDegrees(Math.atan2(-view.f_82479_,view.f_82481_)));
        e.setPitch((float)-Math.toDegrees(Math.atan2(view.f_82480_,Math.sqrt(view.f_82479_*view.f_82479_+view.f_82481_*view.f_82481_))));e.setRoll(roll);
        try{POSITION.invoke(e.getCamera(),position.f_82479_,position.f_82480_,position.f_82481_);}catch(ReflectiveOperationException ex){throw new IllegalStateException("Cannot apply exploration camera",ex);}
    }
}
