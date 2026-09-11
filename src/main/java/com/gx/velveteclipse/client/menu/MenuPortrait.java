package com.gx.velveteclipse.client.menu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.PlayerModelPart;
import dev.kosmx.playerAnim.api.layered.*;
import dev.kosmx.playerAnim.minecraftApi.*;
import org.joml.Quaternionf;
import com.gx.velveteclipse.VelvetEclipseMod;

public final class MenuPortrait extends RemotePlayer {
    private final AbstractClientPlayer source;
    private AnimationStack stack;
    private MenuPoseAnimation animation;
    private double lastRender=System.nanoTime()*1e-9;
    public MenuPortrait(AbstractClientPlayer source){
        super(source.f_108545_,source.m_36316_());this.source=source;
        try{
            var clip=PlayerAnimationRegistry.getAnimation(new ResourceLocation("velvet_eclipse","menu_float"));
            if(clip==null)throw new IllegalStateException("menu_float animation missing");
            stack=PlayerAnimationAccess.getPlayerAnimLayer(this);animation=new MenuPoseAnimation(clip);
            stack.addAnimLayer(800,animation);
        }catch(RuntimeException e){VelvetEclipseMod.LOGGER.error("Menu portrait animation unavailable; displaying static skin",e);}
    }
    @Override public ResourceLocation m_108560_(){return source==null?super.m_108560_():source.m_108560_();}
    @Override public String m_108564_(){return source==null?super.m_108564_():source.m_108564_();}
    @Override public boolean m_36170_(PlayerModelPart part){return source==null||source.m_36170_(part);}
    public void advance(boolean motion){if(motion&&stack!=null){stack.tick();f_19797_++;}}
    public void draw(GuiGraphics g,double homeAmount,double elapsed,double seconds,boolean motion){
        if(animation!=null)animation.advance(motion?seconds-lastRender:0);lastRender=seconds;
        double entry=motion?MenuMotion.ease(elapsed/.52):1;
        double drift=motion?Math.sin(seconds*.8)*4:0;
        double x=116+homeAmount*88-(1-entry)*190,y=97-homeAmount*61+drift;
        double rotation=(179+homeAmount*12)+(motion?Math.sin(seconds*.45)*1.5:0);
        int size=(int)(94+homeAmount*110);
        var pose=g.m_280168_();g.m_280262_();pose.m_85836_();
        try{
            pose.m_85837_(x,y,20);pose.m_252781_(new Quaternionf().rotationZ((float)Math.toRadians(rotation)));
            InventoryScreen.renderEntityInInventoryFollowsAngle(g,0,0,size,-.20f,.05f,this);
        }finally{g.m_280262_();pose.m_85849_();}
    }
}
