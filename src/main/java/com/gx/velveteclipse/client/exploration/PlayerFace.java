package com.gx.velveteclipse.client.exploration;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.PlayerModelPart;

public final class PlayerFace {
    public static void draw(GuiGraphics g,int x,int y,int size){
        draw(g,Minecraft.m_91087_().f_91074_,x,y,size);
    }
    public static void draw(GuiGraphics g,net.minecraft.client.player.AbstractClientPlayer player,int x,int y,int size){
        if(player==null||size<1)return;
        // Managed GUI fills are deferred, whereas blit submits immediately.
        // Finish the blue backing first; otherwise it overwrites the skin later.
        g.m_280262_();var pose=g.m_280168_();pose.m_85836_();pose.m_252880_(0,0,50);
        float[] color=RenderSystem.getShaderColor();float r=color[0],green=color[1],b=color[2],a=color[3];
        try{
            RenderSystem.setShaderColor(1,1,1,1);RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();
            g.m_280411_(player.m_108560_(),x,y,size,size,8,8,8,8,64,64);
            if(player.m_36170_(PlayerModelPart.HAT))g.m_280411_(player.m_108560_(),x,y,size,size,40,8,8,8,64,64);
        }finally{g.m_280262_();pose.m_85849_();RenderSystem.setShaderColor(r,green,b,a);}
    }
}
