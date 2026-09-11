package com.gx.velveteclipse.client;

import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.*;
import dev.kosmx.playerAnim.core.util.Vec3f;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import net.minecraft.client.player.AbstractClientPlayer;

/** The player and the prop sample the same time, including partial frames. */
public final class EvokerPlayerAnimation {
    private static AbstractClientPlayer owner;
    private static ModifierLayer<IAnimation> layer;
    public static boolean play(Object object){
        if(!(object instanceof AbstractClientPlayer player))return false;
        if(owner==player&&layer!=null)return true;
        clear();owner=player;layer=new ModifierLayer<>();
        final boolean slim="slim".equals(player.m_108564_());
        layer.setAnimation(new IAnimation(){
            public void tick(){}public void setupAnim(float partial){}
            public boolean isActive(){return PersonaCinematics.skillSequenceActive()&&PersonaCinematics.skillTicks()<=60;}
            public Vec3f get3DTransform(String part,TransformType type,float partial,Vec3f base){
                double t=PersonaCinematics.skillTicks()+partial,w=EvokerPose.blend(t);Vec3f value;
                if(part.equals("rightArm")){
                    var arm=EvokerPose.arm(t,slim);
                    if(type==TransformType.ROTATION)value=arm.rotation();else if(type==TransformType.BEND)value=arm.bend();else return base;
                }else if(type==TransformType.ROTATION){
                    value=switch(part){case "head"->new Vec3f(.04f,-.04f,-.035f);case "body"->new Vec3f(-.02f,.06f,0);
                        case "leftArm"->new Vec3f(-.09f,.06f,-.13f);case "rightLeg"->new Vec3f(-.04f,0,.025f);case "leftLeg"->new Vec3f(.04f,0,-.025f);default->base;};
                }else if(type==TransformType.BEND&&part.equals("leftArm"))value=new Vec3f(0,-.15f,0);
                else return base;
                return new Vec3f((float)(base.getX()+(value.getX()-base.getX())*w),(float)(base.getY()+(value.getY()-base.getY())*w),(float)(base.getZ()+(value.getZ()-base.getZ())*w));
            }
        });
        PlayerAnimationAccess.getPlayerAnimLayer(player).addAnimLayer(900,layer);return true;
    }
    public static boolean isPlaying(Object player){return owner==player&&layer!=null;}
    public static void stop(Object player){if(owner==player)clear();}
    public static void clear(){if(owner!=null&&layer!=null)PlayerAnimationAccess.getPlayerAnimLayer(owner).removeLayer(layer);owner=null;layer=null;}
}
