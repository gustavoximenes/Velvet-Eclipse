package com.gx.velveteclipse.client.menu;
import dev.kosmx.playerAnim.api.layered.*;
import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.util.Vec3f;

/** Inventory rendering supplies partialTick=1; keep our own fraction for fluid preview motion. */
public final class MenuPoseAnimation implements IAnimation {
    private final KeyframeAnimationPlayer player;
    private double fraction;
    public MenuPoseAnimation(KeyframeAnimation clip){player=new KeyframeAnimationPlayer(clip);}
    public void advance(double seconds){
        fraction+=Math.max(0,Math.min(.1,seconds))*20;
        while(fraction>=1){player.tick();fraction-=1;}
    }
    @Override public void tick(){}
    @Override public boolean isActive(){return player.isActive();}
    @Override public void setupAnim(float ignored){player.setupAnim((float)fraction);}
    @Override public Vec3f get3DTransform(String part,TransformType type,float ignored,Vec3f value){return player.get3DTransform(part,type,(float)fraction,value);}
}
