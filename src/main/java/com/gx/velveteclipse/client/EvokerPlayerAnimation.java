package com.gx.velveteclipse.client;

import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.*;
import dev.kosmx.playerAnim.core.util.Vec3f;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import net.minecraft.client.player.AbstractClientPlayer;

/** The player and the prop sample the same time, including partial frames. */
public final class EvokerPlayerAnimation {
    private static final java.util.Map<AbstractClientPlayer,ModifierLayer<IAnimation>> layers=new java.util.WeakHashMap<>();
    public static boolean play(Object object){
        if(!(object instanceof AbstractClientPlayer player))return false;
        if(layers.containsKey(player))return true;
        var layer=new ModifierLayer<IAnimation>();layers.put(player,layer);
        final boolean slim="slim".equals(player.m_108564_());
        layer.setAnimation(new IAnimation(){
            public void tick(){}public void setupAnim(float partial){}
            public boolean isActive(){return com.gx.velveteclipse.client.presentation.BattleDirector.summoning(player.m_20148_())&&com.gx.velveteclipse.client.presentation.BattleDirector.age(0)<=60;}
            public Vec3f get3DTransform(String part,TransformType type,float partial,Vec3f base){
                double t=com.gx.velveteclipse.client.presentation.BattleDirector.age(partial),w=EvokerPose.blend(t);Vec3f value;
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
    public static boolean isPlaying(Object player){return layers.containsKey(player);}
    public static void stop(Object player){var layer=layers.remove(player);if(layer!=null&&player instanceof AbstractClientPlayer p)PlayerAnimationAccess.getPlayerAnimLayer(p).removeLayer(layer);}
    public static void clear(){for(var p:java.util.List.copyOf(layers.keySet()))stop(p);}
}
