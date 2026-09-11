package com.gx.velveteclipse.exploration;

/** Shared tick clock and geometry: neither side invents its own attack duration. */
public final class ExplorationRules {
    private ExplorationRules() {}
    public static final int HIT_BEGIN=4, HIT_END=7, ATTACK_END=18;
    public static final int ENTRY_RUN_BEGIN=3, ENTRY_RUN_END=23, ENTRY_READY=32;
    public static final double REACH=2.9, CONE_COS=Math.cos(Math.toRadians(62));
    public enum Mode { NORMAL, EXPLORATION, EXPLORATION_ATTACK, BATTLE_ENTRY, BATTLE, SKILL_CINEMATIC }
    public static boolean hitWindow(int tick){return tick>=HIT_BEGIN&&tick<=HIT_END;}
    public static boolean canToggle(Mode mode){return mode==Mode.NORMAL||mode==Mode.EXPLORATION;}
    public static boolean canAttack(Mode mode){return mode==Mode.EXPLORATION;}
    public static boolean locksInput(Mode mode){return mode==Mode.BATTLE_ENTRY||mode==Mode.SKILL_CINEMATIC;}
    public static Mode cameraMode(boolean skill,boolean entry,boolean battle,boolean attack,boolean explore){
        return skill?Mode.SKILL_CINEMATIC:entry?Mode.BATTLE_ENTRY:battle?Mode.BATTLE:attack?Mode.EXPLORATION_ATTACK:explore?Mode.EXPLORATION:Mode.NORMAL;
    }
    public static boolean inSweep(double dx,double dy,double dz,double forwardX,double forwardZ){
        double horizontal=dx*dx+dz*dz;
        return Math.abs(dy)<=1.8&&horizontal<=REACH*REACH&&horizontal>.0001&&
            (dx*forwardX+dz*forwardZ)/Math.sqrt(horizontal)>=CONE_COS;
    }
    public static double smooth(double t){t=Math.max(0,Math.min(1,t));return t*t*(3-2*t);}
    public static double entryProgress(double tick){return smooth((tick-ENTRY_RUN_BEGIN)/(ENTRY_RUN_END-ENTRY_RUN_BEGIN));}
    public static double collisionFraction(double requested,double hitDistance){return requested<=0?0:Math.max(0,Math.min(1,(hitDistance-.18)/requested));}
}
