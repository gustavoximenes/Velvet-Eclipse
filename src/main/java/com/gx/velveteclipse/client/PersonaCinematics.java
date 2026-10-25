/*
 * Decompiled with CFR 0.152.
 */
package com.gx.velveteclipse.client;

import com.gx.velveteclipse.client.ClientBattleState;
import com.gx.velveteclipse.network.BattleSnapshotS2C;
import com.gx.velveteclipse.util.RuntimeBridge;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class PersonaCinematics {
    private static boolean wasActive;
    private static int introTicks;
    private static boolean skillSequence;
    private static int skillTicks;
    private static float battleYaw;
    private static String activeSkill;
    private static int skillEnd = 153;
    private static boolean weakness;
    private static boolean melee;
    private static int meleeTicks;
    private static net.minecraft.world.phys.Vec3 meleeStart,meleeEnd;
    public static boolean meleeActive(){return melee;}
    public static int meleeTicks(){return meleeTicks;}
    public static boolean weakness(){return skillSequence&&weakness;}
    public static void reset(){skillSequence=false;melee=false;weakness=false;skillTicks=meleeTicks=0;activeSkill="";wasActive=false;introTicks=0;}
    public static void accept(com.gx.velveteclipse.network.BattleCueS2C cue){
        BattleTargetHud.lockActionTarget(cue.target());
        if("cancel".equals(cue.type())){reset();return;}
        if("skill".equals(cue.type())){onAction("skill",cue.skill());weakness=cue.weakness();}
        if("attack".equals(cue.type())&&ClientBattleState.snapshot().active()){
            battleYaw=RuntimeBridge.playerYaw(RuntimeBridge.clientPlayer());
            melee=true;meleeTicks=0;weakness=false;
            meleeStart=new net.minecraft.world.phys.Vec3(cue.x(),cue.y(),cue.z());
            meleeEnd=new net.minecraft.world.phys.Vec3(cue.tx(),cue.ty(),cue.tz());
        }
    }

    private PersonaCinematics() {
    }

    public static void onAction(String string, String string2) {
        if (skillSequence || !"skill".equals(string)) {
            return;
        }
        BattleSnapshotS2C battleSnapshotS2C = ClientBattleState.snapshot();
        if (battleSnapshotS2C == null || !battleSnapshotS2C.active()) {
            return;
        }
        Object object = RuntimeBridge.clientPlayer();
        if (object != null) {
            battleYaw = RuntimeBridge.playerYaw(object);
        }
        activeSkill = string2 == null ? "" : string2;
        skillEnd = com.gx.velveteclipse.presentation.PersonaActionTiming.forAction(battleSnapshotS2C.personaId(), activeSkill).finish();
        skillSequence = true;
        skillTicks = 0;
    }

    public static void tick(Object object) {
        boolean bl;
        if (!PersonaCinematics.isEndPhase(object)) {
            return;
        }
        BattleSnapshotS2C battleSnapshotS2C = ClientBattleState.snapshot();
        boolean bl2 = bl = battleSnapshotS2C != null && battleSnapshotS2C.active();
        if (bl && !wasActive) {
            Object object2 = RuntimeBridge.clientPlayer();
            if (object2 != null) {
                battleYaw = RuntimeBridge.playerYaw(object2);
            }
            introTicks = 26;
        }
        if (!bl) {
            introTicks = 0;
            skillSequence = false;
            melee=false;weakness=false;
            skillTicks = 0;
            activeSkill = "";
        } else {
            if(melee){
                ++meleeTicks;var player=net.minecraft.client.Minecraft.m_91087_().f_91074_;
                if(player!=null&&meleeStart!=null){var desired=meleeStart.m_165921_(meleeEnd,com.gx.velveteclipse.core.battle.MeleeTimeline.progress(meleeTicks));
                    var target=net.minecraft.client.Minecraft.m_91087_().f_91073_.m_6815_(BattleTargetHud.entityId());
                    if(target!=null)RuntimeBridge.setEntityRotation(player,(float)Math.toDegrees(Math.atan2(-(target.m_20185_()-meleeStart.f_82479_),target.m_20189_()-meleeStart.f_82481_)),0);
                    player.m_20256_(net.minecraft.world.phys.Vec3.f_82478_);player.m_6478_(net.minecraft.world.entity.MoverType.SELF,desired.m_82546_(player.m_20182_()));}
                if(meleeTicks>=com.gx.velveteclipse.core.battle.MeleeTimeline.END){melee=false;RuntimeBridge.setEntityRotation(player,battleYaw,0);}
            }
            if (introTicks > 0) {
                --introTicks;
            }
            if (skillSequence && ++skillTicks > skillEnd) {
                skillSequence = false;
                activeSkill = "";
            }
        }
        wasActive = bl;
    }

    public static void applyCamera(Object object) {
        try {
            double[] dArray;
            double[] dArray2;
            BattleSnapshotS2C battleSnapshotS2C = ClientBattleState.snapshot();
            if (battleSnapshotS2C == null || !battleSnapshotS2C.active() || object == null) {
                return;
            }
            Object object2 = RuntimeBridge.clientPlayer();
            if (object2 == null) {
                return;
            }
            double[] dArray3 = RuntimeBridge.playerPosition(object2);
            if (dArray3 == null || dArray3.length < 3) {
                return;
            }
            float f = -1.2f;
            double partial=0; Object delta=invokeNoArg(object,"getPartialTick"); if(delta instanceof Number)partial=Math.max(0,Math.min(1,((Number)delta).doubleValue()));
            double time=skillTicks+partial;
            if (skillSequence && time <= 51) {
                dArray2 = RuntimeBridge.localOffset(dArray3, battleYaw, -.85, 1.90, 3.45);
                dArray = RuntimeBridge.localOffset(dArray3, battleYaw, -0.05, 1.34, 0.02);
                f = -1.5f;
            double push=ease(time/37.0);
                dArray2=mix(dArray2,RuntimeBridge.localOffset(dArray3,battleYaw,-.62,1.87,3.05),push);
                dArray=mix(dArray,RuntimeBridge.localOffset(dArray3,battleYaw,-0.04,1.42,0.0),push);
            } else if (skillSequence && time <= skillEnd) {
                dArray2 = RuntimeBridge.localOffset(dArray3, battleYaw, -3.25, 2.15, 0.65);
                dArray = RuntimeBridge.localOffset(dArray3, battleYaw, 0.0, 1.18, 2.85);
                double dolly=ease((time-52)/42.0);
                dArray2=mix(dArray2,RuntimeBridge.localOffset(dArray3,battleYaw,-2.9,2.05,0.95),dolly);
                if(time>skillEnd-10){double back=ease((time-skillEnd+10)/10.0);dArray2=mix(dArray2,RuntimeBridge.localOffset(dArray3,battleYaw,-2.55,2.25,-4.35),back);dArray=mix(dArray,RuntimeBridge.localOffset(dArray3,battleYaw,0.05,1.08,4.65),back);}
                f = -0.6f;
            } else if (melee && meleeStart!=null) {
                double[] origin={meleeStart.f_82479_,meleeStart.f_82480_,meleeStart.f_82481_};
                dArray2=RuntimeBridge.localOffset(origin,battleYaw,-3.05,2.12,1.05);
                dArray=RuntimeBridge.localOffset(origin,battleYaw,0,1.02,3.8);
                double side=ease((meleeTicks+partial)/7.0)*(1-ease((meleeTicks+partial-30)/14.0));
                dArray2=mix(RuntimeBridge.localOffset(origin,battleYaw,-2.55,2.25,-4.35),dArray2,side);
                dArray=mix(RuntimeBridge.localOffset(origin,battleYaw,.05,1.08,4.65),dArray,side);
            } else if (introTicks > 0) {
                dArray2 = RuntimeBridge.localOffset(dArray3, battleYaw, -2.85, 2.45, -4.75);
                dArray = RuntimeBridge.localOffset(dArray3, battleYaw, 0.0, 1.1, 4.35);
            } else {
                dArray2 = RuntimeBridge.localOffset(dArray3, battleYaw, -2.55, 2.25, -4.35);
                dArray = RuntimeBridge.localOffset(dArray3, battleYaw, 0.05, 1.08, 4.65);
            }
            Object object3 = PersonaCinematics.invokeNoArg(object, "getCamera");
            if (object3 == null) {
                return;
            }
            if(object instanceof net.minecraftforge.client.event.ViewportEvent.ComputeCameraAngles event){
                var pivot=new net.minecraft.world.phys.Vec3(dArray3[0],dArray3[1]+1.3,dArray3[2]);
                var desired=new net.minecraft.world.phys.Vec3(dArray2[0],dArray2[1],dArray2[2]);
                var focus=new net.minecraft.world.phys.Vec3(dArray[0],dArray[1],dArray[2]);
                com.gx.velveteclipse.client.exploration.ExplorationCamera.place(event,
                    com.gx.velveteclipse.client.exploration.ExplorationCamera.collide(pivot,desired),focus,0);
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    public static boolean skillSequenceActive() {
        return skillSequence;
    }

    public static int skillTicks() {
        return skillTicks;
    }

    public static String activeSkill() {
        return activeSkill;
    }

    private static double ease(double x){x=Math.max(0,Math.min(1,x));return x*x*(3-2*x);}
    private static double[] mix(double[] a,double[] b,double x){return new double[]{a[0]+(b[0]-a[0])*x,a[1]+(b[1]-a[1])*x,a[2]+(b[2]-a[2])*x};}
    private static float[] lookAt(double[] dArray, double[] dArray2) {
        double d = dArray2[0] - dArray[0];
        double d2 = dArray2[1] - dArray[1];
        double d3 = dArray2[2] - dArray[2];
        double d4 = Math.sqrt(d * d + d3 * d3);
        float f = (float)Math.toDegrees(Math.atan2(-d, d3));
        float f2 = (float)(-Math.toDegrees(Math.atan2(d2, d4)));
        return new float[]{f, f2};
    }

    private static void setCameraPosition(Object object, double d, double d2, double d3) {
        for (String string : new String[]{"setPosition", "setPos", "m_90584_"}) {
            for (Class<?> clazz = object.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
                try {
                    Method method = clazz.getDeclaredMethod(string, Double.TYPE, Double.TYPE, Double.TYPE);
                    method.setAccessible(true);
                    method.invoke(object, d, d2, d3);
                    return;
                }
                catch (Throwable throwable) {
                    continue;
                }
            }
        }
    }

    private static Object invokeNoArg(Object object, String string) {
        if (object == null) {
            return null;
        }
        for (Class<?> clazz = object.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            try {
                Method method = clazz.getDeclaredMethod(string, new Class[0]);
                method.setAccessible(true);
                return method.invoke(object, new Object[0]);
            }
            catch (Throwable throwable) {
                continue;
            }
        }
        try {
            return object.getClass().getMethod(string, new Class[0]).invoke(object, new Object[0]);
        }
        catch (Throwable throwable) {
            return null;
        }
    }

    private static void invokeFloat(Object object, String string, float f) {
        if (object == null) {
            return;
        }
        for (Class<?> clazz = object.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            try {
                Method method = clazz.getDeclaredMethod(string, Float.TYPE);
                method.setAccessible(true);
                method.invoke(object, Float.valueOf(f));
                return;
            }
            catch (Throwable throwable) {
                continue;
            }
        }
    }

    private static boolean isEndPhase(Object object) {
        if (object == null) {
            return true;
        }
        try {
            Enum enum_;
            Field field = object.getClass().getField("phase");
            Object object2 = field.get(object);
            return object2 instanceof Enum && "END".equals((enum_ = (Enum)object2).name());
        }
        catch (Throwable throwable) {
            return true;
        }
    }

    static {
        activeSkill = "";
    }
}

