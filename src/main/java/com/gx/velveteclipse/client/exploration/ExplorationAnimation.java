package com.gx.velveteclipse.client.exploration;

import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.*;
import dev.kosmx.playerAnim.core.util.Vec3f;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import net.minecraft.client.player.AbstractClientPlayer;
import static com.gx.velveteclipse.exploration.ExplorationRules.*;

/** One cached Player Animator layer, below the menu (800) and Evoker (900). */
public final class ExplorationAnimation implements IAnimation {
    public static final int PRIORITY=700;
    private static AbstractClientPlayer owner;
    private static ExplorationAnimation animation;
    private static ModifierLayer<IAnimation> layer;
    private double phase,previousPhase,weight,previousWeight,speed,previousSpeed,x,z,bank,previousBank;
    private float facing,previousFacing;
    private int age;
    private ExplorationAnimation(AbstractClientPlayer p){x=p.m_20185_();z=p.m_20189_();facing=previousFacing=p.m_146908_();}
    public static void update(AbstractClientPlayer p){
        if(owner!=p){clear();owner=p;animation=new ExplorationAnimation(p);layer=new ModifierLayer<>();layer.setAnimation(animation);PlayerAnimationAccess.getPlayerAnimLayer(p).addAnimLayer(PRIORITY,layer);}
        animation.advance(p);
    }
    public static void clear(){if(layer!=null){layer.setAnimation(null);if(owner!=null)PlayerAnimationAccess.getPlayerAnimLayer(owner).removeLayer(layer);}owner=null;animation=null;layer=null;}
    public static double phase(float partial){return animation==null?0:animation.previousPhase+(animation.phase-animation.previousPhase)*partial;}
    public static float facing(float partial){return animation==null?0:animation.previousFacing+(animation.facing-animation.previousFacing)*partial;}
    public static double speed(float partial){return animation==null?0:animation.previousSpeed+(animation.speed-animation.previousSpeed)*partial;}
    private void advance(AbstractClientPlayer p){
        previousPhase=phase;previousWeight=weight;previousSpeed=speed;previousBank=bank;age++;
        double dx=p.m_20185_()-x,dz=p.m_20189_()-z;x=p.m_20185_();z=p.m_20189_();
        double distance=Math.min(.6,Math.sqrt(dx*dx+dz*dz));
        previousFacing=facing;
        float viewDelta=ExplorationMotion.turn(0,p.m_146908_()-facing,180);
        double idleFacing=Math.abs(viewDelta)>55?p.m_146908_()-Math.copySign(55,viewDelta):facing;
        double desired=ExplorationClient.attacking()||ExplorationClient.entry()?p.m_146908_():distance>.015?Math.toDegrees(Math.atan2(-dx,dz)):idleFacing;
        facing=ExplorationMotion.turn(facing,(float)desired,ExplorationClient.attacking()?45:27);
        double target=p.m_20096_()?Math.min(1,distance/.28):0;
        if(com.gx.velveteclipse.client.PersonaCinematics.meleeActive())target=Math.min(1,distance/.28);
        if(ExplorationClient.entry())target=1;
        speed+=(target-speed)*(target>speed?.26:.42);
        double angle=Math.toRadians(p.m_146908_());double side=dx*Math.cos(angle)+dz*Math.sin(angle);bank+=(Math.max(-1,Math.min(1,side/.2))-bank)*.25;
        phase+=ExplorationClient.entry()?.7:distance*3.2;
        weight+=((ExplorationClient.swordVisible()?1:0)-weight)*.38;
        if(ExplorationClient.mode()==Mode.SKILL_CINEMATIC)weight=previousWeight=0;
    }
    @Override public void tick(){}
    @Override public void setupAnim(float partial){}
    @Override public boolean isActive(){return weight>.005;}
    @Override public Vec3f get3DTransform(String part,TransformType type,float partial,Vec3f base){
        if(!part.equals("body")&&!part.equals("head")&&!part.equals("rightArm")&&!part.equals("leftArm")&&!part.equals("rightLeg")&&!part.equals("leftLeg"))return base;
        double p=previousPhase+(phase-previousPhase)*partial,v=previousSpeed+(speed-previousSpeed)*partial,w=previousWeight+(weight-previousWeight)*partial;
        double t=ExplorationClient.ticks()+partial;
        double attack=ExplorationClient.swordAttackTime(partial);
        Vec3f sample;
        if(type==TransformType.ROTATION)sample=WeaponPose.rotation(com.gx.velveteclipse.client.equipment.EquipmentScreen.family(),part,p,v,attack);
        else if(type==TransformType.BEND)sample=WeaponPose.bend(com.gx.velveteclipse.client.equipment.EquipmentScreen.family(),part,p,v,attack);
        else if(type==TransformType.POSITION&&part.equals("body"))sample=new Vec3f(0,(float)ExplorationPose.bodyBob(p,v,age+partial),0);
        else return base;
        if(type==TransformType.ROTATION&&attack<0){
            double side=previousBank+(bank-previousBank)*partial;
            if(part.equals("body"))sample=new Vec3f(sample.getX(),sample.getY(),sample.getZ()+(float)(side*.055));
            if(part.equals("head"))sample=new Vec3f(base.getX()*.4f+sample.getX(),(float)(base.getY()*(1-v)*.45),sample.getZ());
        }
        double px=sample.getX(),py=sample.getY(),pz=sample.getZ();
        if(type==TransformType.ROTATION||type==TransformType.BEND)
            return new Vec3f((float)(base.getX()+(px-base.getX())*w),(float)(base.getY()+(py-base.getY())*w),(float)(base.getZ()+(pz-base.getZ())*w));
        return new Vec3f((float)(base.getX()+px*w),(float)(base.getY()+py*w),(float)(base.getZ()+pz*w));
    }
    public static double slash(double tick){return smooth((tick-3)/4)*(1-smooth((tick-9)/9));}
    public static void orient(AbstractClientPlayer p){
        if(p!=owner||animation==null||com.gx.velveteclipse.client.ClientBattleState.snapshot().active()||!ExplorationClient.enabled())return;
        p.f_20884_=animation.previousFacing;p.f_20883_=animation.facing;
    }
}
