package com.gx.velveteclipse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;

/** Same textured mesh as the approved cutscene prop and player hand layer. */
public final class EvokerItemRenderer extends BlockEntityWithoutLevelRenderer {
    private final ModelPart gun=EvokerHandLayer.createParts().get("root");
    private static final ResourceLocation TEXTURE=new ResourceLocation("velvet_eclipse","textures/entity/evoker_fx.png");
    public EvokerItemRenderer(){super(Minecraft.m_91087_().m_167982_(),Minecraft.m_91087_().m_167973_());}
    @Override public void m_108829_(ItemStack stack,ItemDisplayContext context,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        pose.m_85836_();
        try{pose.m_252880_(.56f,.5f,.5f);pose.m_85841_(1,-1,-1);gun.m_104301_(pose,buffers.m_6299_(RenderType.m_110452_(TEXTURE)),light,overlay);}
        finally{pose.m_85849_();}
    }
}
