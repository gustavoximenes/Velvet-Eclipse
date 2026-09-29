package com.gx.velveteclipse.client.exploration;

import dev.kosmx.playerAnim.core.util.Vec3f;
/** Shared authored pose used by the real player and its 3D entry portrait. Radians. */
public final class ExplorationPose {
    private static final double[] TIMES={0,3,5,7,10,14,18};
    // Minecraft faces -Z in model space: negative X brings the hand forward.
    // Keep the blade in front through the server's hit window (ticks 4..7).
    private static final double[] ARM_X={.08,-1.95,-1.48,-.48,-.06,.06,.08};
    private static final double[] ARM_Y={-.16,-.92,-.42,.95,.72,.12,-.16};
    private static final double[] ARM_Z={.10,.72,.26,-.72,-.50,.02,.10};
    private static final double[] TURN={0,-.50,-.20,.62,.48,.10,0};
    public static double curve(double[] values,double tick){
        if(tick<=TIMES[0])return values[0];if(tick>=18)return values[6];
        int i=0;while(tick>TIMES[i+1])i++;
        double span=TIMES[i+1]-TIMES[i],t=(tick-TIMES[i])/span;
        double a=i==0?0:(values[i+1]-values[i-1])/(TIMES[i+1]-TIMES[i-1]);
        double b=i==5?0:(values[i+2]-values[i])/(TIMES[i+2]-TIMES[i]);
        return (2*t*t*t-3*t*t+1)*values[i]+(t*t*t-2*t*t+t)*span*a+(-2*t*t*t+3*t*t)*values[i+1]+(t*t*t-t*t)*span*b;
    }
    public static Vec3f rotation(String part,double phase,double speed,double attackTick){
        double v=Math.max(0,Math.min(1,speed)),stride=Math.sin(phase)*v;
        boolean attack=attackTick>=0;double turn=attack?curve(TURN,attackTick):0;
        double action=attack?Math.pow(Math.sin(Math.min(1,attackTick/18)*Math.PI),2):0;
        double blend=attackBlend(attackTick);
        return switch(part){
            // Player Animator applies "body" before Minecraft's Y inversion.
            case "body" -> vector(-.025-.19*v-.10*action,-.045*stride+turn,-.025*stride+.05*action);
            case "head" -> vector(-.08*v,-turn*.32,0);
            case "rightArm" -> vector(mix(.08+.24*v+.07*stride,curve(ARM_X,attackTick),blend),mix(-.16,curve(ARM_Y,attackTick),blend),mix(.10+.04*v,curve(ARM_Z,attackTick),blend));
            case "leftArm" -> vector(-.12-.57*stride-.48*action,.08+.22*action,-.09-.20*action);
            case "rightLeg" -> vector(.73*stride-.27*action,-.12*action,.015+.08*action);
            case "leftLeg" -> vector(-.73*stride+.20*action,.10*action,-.015-.06*action);
            default -> Vec3f.ZERO;
        };
    }
    public static Vec3f bend(String part,double phase,double speed,double attackTick){
        double v=Math.max(0,Math.min(1,speed));
        double angle=switch(part){
            case "rightArm" -> -(.10+.04*v+(attackTick>=0?.78*Math.max(0,1-Math.abs(attackTick-3)/4)*com.gx.velveteclipse.exploration.ExplorationRules.smooth(attackTick/2):0));
            case "leftArm" -> -(.12+.38*v);
            // Fold the recovering leg behind the hips, not the forward plant.
            case "rightLeg" -> .03+.90*Math.pow(Math.max(0,Math.sin(phase)),2)*v;
            case "leftLeg" -> .03+.90*Math.pow(Math.max(0,-Math.sin(phase)),2)*v;
            default -> 0;
        };
        return vector(0,angle,0);
    }
    private static Vec3f vector(double x,double y,double z){return new Vec3f((float)x,(float)y,(float)z);}
    public static double attackBlend(double tick){return tick<0?0:com.gx.velveteclipse.exploration.ExplorationRules.smooth(tick/2)*(1-com.gx.velveteclipse.exploration.ExplorationRules.smooth((tick-12)/6));}
    /** Relaxed wrist trails the lowered blade; attacking rotates it into the cut. */
    public static float wristPitch(double tick){return (float)mix(.34,.22,attackBlend(tick));}
    public static float wristRoll(double tick){return (float)mix(.64,0,attackBlend(tick));}
    public static double bodyBob(double phase,double speed,double age){return Math.cos(phase*2)*speed*.045+Math.sin(age*.06)*.004;}
    private static double mix(double from,double to,double t){return from+(to-from)*t;}
}
