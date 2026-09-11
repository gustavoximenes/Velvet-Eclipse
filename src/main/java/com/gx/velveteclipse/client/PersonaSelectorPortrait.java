package com.gx.velveteclipse.client;

import com.gx.velveteclipse.client.exploration.EntrySilhouette;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import io.github.kosmx.bendylib.ICuboidBuilder;
import io.github.kosmx.bendylib.impl.BendableCuboid;
import org.joml.Quaternionf;
import org.lwjgl.opengl.GL11;
import java.util.*;

/** An isolated copy of the player's vanilla skin mesh; no substitute entity or fixed portrait.
 * The palette texture is derived from the currently loaded skin and never replaces it. */
public final class PersonaSelectorPortrait {
    private static final ResourceLocation PALETTE=new ResourceLocation("velvet_eclipse","dynamic/persona_portrait");
    private static ResourceLocation source;
    private static int sourceId=-1;
    private static long captured;
    private static ModelPart root;
    private static boolean slimModel;
    private static BendableCuboid arm,sleeve;
    private static EvokerHandLayer gun;
    public static int posterize(int abgr,int threshold){
        int r=abgr&255,g=abgr>>>8&255,b=abgr>>>16&255;
        // Continuous blue duotone preserves the skin's facial pixels, hair and clothing seams.
        double tone=Math.min(1,(r*3+g*6+b)/10.0/(Math.max(24,threshold)*1.7));
        int rgb=tone<.45?mix(0x102975,0x298BD5,tone/.45):tone<.75?mix(0x298BD5,0x7BD3F3,(tone-.45)/.30):mix(0x7BD3F3,0xF6FFFF,(tone-.75)/.25);
        return (abgr&0xFF000000)|((rgb&255)<<16)|(rgb&0xFF00)|((rgb>>16)&255);
    }
    private static int mix(int a,int b,double t){int c=0;for(int shift:new int[]{0,8,16})c|=(int)Math.round((a>>shift&255)+((b>>shift&255)-(a>>shift&255))*t)<<shift;return c;}
    private static ResourceLocation texture(ResourceLocation skin){
        var manager=Minecraft.m_91087_().m_91097_();var original=manager.m_118506_(skin);int id=original.m_117963_();
        long now=System.nanoTime();if(skin.equals(source)&&id==sourceId&&now-captured<3_000_000_000L)return PALETTE;
        int active=GL11.glGetInteger(34016); // GL_ACTIVE_TEXTURE
        RenderSystem.activeTexture(33984); // GL_TEXTURE0
        int prior=GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        try{
            RenderSystem.bindTexture(id);int w=GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D,0,GL11.GL_TEXTURE_WIDTH),h=GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D,0,GL11.GL_TEXTURE_HEIGHT);
            if(w<1||h<1||w>2048||h>2048)return null;
            NativeImage pixels=new NativeImage(w,h,false);
            try{
                pixels.m_85045_(0,false);
                long sum=0,count=0;for(int y=0;y<h;y++)for(int x=0;x<w;x++){int p=pixels.m_84985_(x,y);if((p>>>24)>100){sum+=((p&255)*3+(p>>>8&255)*6+(p>>>16&255))/10;count++;}}
                int threshold=(int)Math.max(40,Math.min(195,count==0?120:sum/count+12));
                for(int y=0;y<h;y++)for(int x=0;x<w;x++)pixels.m_84988_(x,y,posterize(pixels.m_84985_(x,y),threshold));
                manager.m_118495_(PALETTE,new DynamicTexture(pixels));pixels=null;
                source=skin;sourceId=id;captured=now;return PALETTE;
            }finally{if(pixels!=null)pixels.close();}
        }finally{RenderSystem.bindTexture(prior);RenderSystem.activeTexture(active);}
    }
    private static void model(boolean slim){
        if(root!=null&&slim==slimModel)return;slimModel=slim;
        root=LayerDefinition.m_171565_(PlayerModel.m_170825_(CubeDeformation.f_171458_,slim),64,64).m_171564_();
        arm=bent(slim,40,16,0);sleeve=bent(slim,40,32,.25f);gun=new EvokerHandLayer(null);
    }
    private static BendableCuboid bent(boolean slim,int u,int v,float inflate){return new BendableCuboid.Builder().setDirection(Direction.UP).build(new ICuboidBuilder.Data(u,v,slim?-2:-3,-2,-2,slim?3:4,12,4,inflate,inflate,inflate,false,64,64));}
    public static void draw(GuiGraphics g,int x,int y,int width,int height,float alpha){
        var mc=Minecraft.m_91087_();var player=mc.f_91074_;if(player==null||alpha<=0)return;
        boolean slim="slim".equals(player.m_108564_());model(slim);g.m_280262_();ResourceLocation palette=texture(player.m_108560_());
        var ps=g.m_280168_();float[] previous=RenderSystem.getShaderColor().clone();
        float fogStart=RenderSystem.getShaderFogStart(),fogEnd=RenderSystem.getShaderFogEnd();ps.m_85836_();
        try{
            // World fog must not recolor a GUI portrait; flush it before restoring the world state.
            RenderSystem.setShaderColor(1,1,1,1);RenderSystem.setShaderFogStart(Float.MAX_VALUE/2);RenderSystem.setShaderFogEnd(Float.MAX_VALUE);
            // Keep the portrait below later GUI text; it is clipped to the left column.
            g.m_280588_(Math.max(0,x),Math.max(0,y),Math.max(1,x+width),y+height);
            ps.m_85837_(x+width*.54,y+height*.34,120);
            float size=height*.54f;ps.m_85841_(size,size,-size);
            ps.m_252781_(new Quaternionf().rotationZ(-.11f).rotateY(-.20f));
            for(String name:List.of("head","hat","body","right_arm","left_arm","right_leg","left_leg","left_sleeve","right_sleeve","left_pants","right_pants","jacket"))root.m_171324_(name).m_233569_();
            var head=root.m_171324_("head");head.m_171327_(.17f,-.07f,-.05f);root.m_171324_("hat").m_104315_(head);
            var r=EvokerPose.arm(34,slim);var right=root.m_171324_("right_arm");right.m_171327_(r.rotation().getX(),r.rotation().getY(),r.rotation().getZ());
            root.m_171324_("left_arm").m_171327_(-.08f,.03f,-.13f);root.m_171324_("left_sleeve").m_104315_(root.m_171324_("left_arm"));
            // Unlike the emissive overlay pass, Forge's unlit pass writes depth for the complete mesh.
            var out=palette==null?new EntrySilhouette(g.m_280091_().m_6299_(RenderType.m_269166_()),alpha):g.m_280091_().m_6299_(net.minecraftforge.client.ForgeRenderTypes.getUnlitTranslucent(palette));
            for(String name:List.of("head","body","left_arm","right_leg","left_leg"))root.m_171324_(name).m_104306_(ps,out,15728880,655360,1,1,1,alpha);
            var layers=Map.of("hat",net.minecraft.world.entity.player.PlayerModelPart.HAT,"left_sleeve",net.minecraft.world.entity.player.PlayerModelPart.LEFT_SLEEVE,
                "jacket",net.minecraft.world.entity.player.PlayerModelPart.JACKET,"left_pants",net.minecraft.world.entity.player.PlayerModelPart.LEFT_PANTS_LEG,"right_pants",net.minecraft.world.entity.player.PlayerModelPart.RIGHT_PANTS_LEG);
            layers.forEach((name,part)->{if(player.m_36170_(part))root.m_171324_(name).m_104306_(ps,out,15728880,655360,1,1,1,alpha);});
            ps.m_85836_();right.m_104299_(ps);arm.applyBend(r.bend().getX(),r.bend().getY());arm.render(ps.m_85850_(),out,1,1,1,alpha,15728880,655360);
            if(player.m_36170_(net.minecraft.world.entity.player.PlayerModelPart.RIGHT_SLEEVE)){sleeve.applyBend(r.bend().getX(),r.bend().getY());sleeve.render(ps.m_85850_(),out,1,1,1,alpha,15728880,655360);}ps.m_85849_();
            // The same prop and hand binding used by the real summon animation.
            ps.m_85836_();var orientation=EvokerHandLayer.bindHand(ps,right,r.bend(),slim);ps.m_252781_(orientation.invert());ps.m_252781_(EvokerPose.weaponRotation(34));
            gun.renderPortrait(ps,g.m_280091_().m_6299_(RenderType.m_269166_()),alpha);ps.m_85849_();
        }finally{g.m_280262_();g.m_280618_();ps.m_85849_();RenderSystem.setShaderFogStart(fogStart);RenderSystem.setShaderFogEnd(fogEnd);RenderSystem.setShaderColor(previous[0],previous[1],previous[2],previous[3]);}
    }
    public static void reset(){source=null;sourceId=-1;captured=0;root=null;arm=sleeve=null;gun=null;}
}
