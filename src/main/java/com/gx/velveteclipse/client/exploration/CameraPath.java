package com.gx.velveteclipse.client.exploration;
import net.minecraft.world.phys.Vec3;
import com.gx.velveteclipse.exploration.ExplorationRules;

/** Pure camera geometry, tested with floors/walls/ceilings independently of Minecraft. */
public final class CameraPath {
    public record Parameters(double distance,double height,double side,double pitch,double fov){}
    public static final Parameters PARAMETERS=new Parameters(3.8,1.30,.68,11,62);
    public record Frame(Vec3 pivot,Vec3 desired,Vec3 focus){}
    @FunctionalInterface public interface RayProbe {double hitDistance(Vec3 from,Vec3 to);}
    public static Frame exploration(Vec3 feet,double yawDegrees,double lookPitch,boolean entry){
        Vec3 pivot=feet.m_82520_(0,PARAMETERS.height(),0);
        double yaw=Math.toRadians(yawDegrees),pitch=Math.toRadians(entry?12:Math.max(5,Math.min(38,lookPitch*.55+PARAMETERS.pitch())));
        double distance=entry?4.1:PARAMETERS.distance(),side=entry?1.05:PARAMETERS.side();
        Vec3 desired=pivot.m_82520_(Math.sin(yaw)*distance*Math.cos(pitch)+Math.cos(yaw)*side,Math.sin(pitch)*distance,-Math.cos(yaw)*distance*Math.cos(pitch)+Math.sin(yaw)*side);
        Vec3 focus=pivot.m_82520_(-Math.sin(yaw)*.8,-.30,Math.cos(yaw)*.8);
        return new Frame(pivot,desired,focus);
    }
    public static Vec3 clip(Vec3 pivot,Vec3 desired,RayProbe world){
        Vec3 delta=desired.m_82546_(pivot);double length=delta.m_82553_(),fraction=1;
        for(int i=0;i<9;i++){
            Vec3 offset=i==8?Vec3.f_82478_:new Vec3((i&1)==0?-.16:.16,(i&2)==0?-.16:.16,(i&4)==0?-.16:.16);
            double distance=world.hitDistance(pivot.m_82549_(offset),desired.m_82549_(offset));
            if(Double.isFinite(distance))fraction=Math.min(fraction,ExplorationRules.collisionFraction(length,distance));
        }
        return pivot.m_82549_(delta.m_82490_(fraction));
    }
}
