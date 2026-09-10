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
    private double phase,previousPhase,weight,previousWeight,speed,previousSpeed,x,z;
    private int age;
    private ExplorationAnimation(AbstractClientPlayer p){x=p.m_20185_();z=p.m_20189_();}
    public static void update(AbstractClientPlayer p){
        if(owner!=p){clear();owner=p;animation=new ExplorationAnimation(p);layer=new ModifierLayer<>();layer.setAnimation(animation);PlayerAnimationAccess.getPlayerAnimLayer(p).addAnimLayer(PRIORITY,layer);}
        animation.advance(p);
    }
    public static void clear(){if(layer!=null){layer.setAnimation(null);if(owner!=null)PlayerAnimationAccess.getPlayerAnimLayer(owner).removeLayer(layer);}owner=null;animation=null;layer=null;}
    private void advance(AbstractClientPlayer p){
        previousPhase=phase;previousWeight=weight;previousSpeed=speed;age++;
        double dx=p.m_20185_()-x,dz=p.m_20189_()-z;x=p.m_20185_();z=p.m_20189_();
        double distance=Math.min(.6,Math.sqrt(dx*dx+dz*dz));
        double target=p.m_20096_()?Math.min(1,distance/.28):0;
        if(ExplorationClient.entry())target=1;
        speed+=(target-speed)*.32;
        phase+=ExplorationClient.entry()?.52:distance*2.05;
        weight+=((ExplorationClient.swordVisible()?1:0)-weight)*.38;
        if(ExplorationClient.mode()==Mode.BATTLE||ExplorationClient.mode()==Mode.SKILL_CINEMATIC)weight=previousWeight=0;
    }
    @Override public void tick(){}
    @Override public void setupAnim(float partial){}
    @Override public boolean isActive(){return weight>.005;}
    @Override public Vec3f get3DTransform(String part,TransformType type,float partial,Vec3f base){
        if(!part.equals("body")&&!part.equals("head")&&!part.equals("rightArm")&&!part.equals("leftArm")&&!part.equals("rightLeg")&&!part.equals("leftLeg"))return base;
        double p=previousPhase+(phase-previousPhase)*partial,v=previousSpeed+(speed-previousSpeed)*partial,w=previousWeight+(weight-previousWeight)*partial;
        double t=ExplorationClient.ticks()+partial;
        double attack=ExplorationClient.attacking()?t:-1;
        Vec3f sample;
        if(type==TransformType.ROTATION)sample=ExplorationPose.rotation(part,p,v,attack);
        else if(type==TransformType.BEND)sample=ExplorationPose.bend(part,p,v,attack);
        else if(type==TransformType.POSITION&&part.equals("body"))sample=new Vec3f(0,(float)ExplorationPose.bodyBob(p,v,age+partial),0);
        else return base;
        double px=sample.getX(),py=sample.getY(),pz=sample.getZ();
        if(type==TransformType.ROTATION||type==TransformType.BEND)
            return new Vec3f((float)(base.getX()+(px-base.getX())*w),(float)(base.getY()+(py-base.getY())*w),(float)(base.getZ()+(pz-base.getZ())*w));
        return new Vec3f((float)(base.getX()+px*w),(float)(base.getY()+py*w),(float)(base.getZ()+pz*w));
    }
    public static double slash(double tick){return smooth((tick-3)/4)*(1-smooth((tick-9)/9));}
}
