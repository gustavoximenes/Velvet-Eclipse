package com.gx.velveteclipse.client.city;

import com.gx.velveteclipse.city.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;

public final class CityShopActorRenderer extends GeoEntityRenderer<CityShopActor> {
    public CityShopActorRenderer(EntityRendererProvider.Context context,CityShopAccess.Point point){
        super(context,new DefaultedEntityGeoModel<>(new ResourceLocation("velvet_eclipse",point.asset())));
        withScale(point.scale());f_114477_=point.npc()?.25f:0;
    }
    @Override protected void applyRotations(CityShopActor actor,PoseStack stack,float age,float bodyYaw,float partial){super.applyRotations(actor,stack,age,actor.m_146908_(),partial);}
    @Override public boolean m_6512_(CityShopActor actor){
        var mc=Minecraft.m_91087_();return mc.f_91074_!=null&&mc.f_91080_==null&&actor.m_20280_(mc.f_91074_)<64;
    }
    @Override protected int m_6086_(CityShopActor actor,net.minecraft.core.BlockPos at){return CityShopAccess.hasNpc(actor.shop())?super.m_6086_(actor,at):15;}
    @Override protected void m_7649_(CityShopActor actor,net.minecraft.network.chat.Component name,PoseStack stack,net.minecraft.client.renderer.MultiBufferSource out,int light){
        var mc=Minecraft.m_91087_();boolean aimed=mc.f_91077_ instanceof net.minecraft.world.phys.EntityHitResult hit&&hit.m_82443_()==actor;
        String text=CityShopAccess.name(actor.shop());if(aimed)text+=" • "+com.gx.velveteclipse.localization.DisplayText.text("Clique direito: acessar loja");
        super.m_7649_(actor,com.gx.velveteclipse.localization.ModText.component(text),stack,out,light);
    }
    @Mod.EventBusSubscriber(modid="velvet_eclipse",bus=Mod.EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Registration {
        @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers event){
            for(var point:CityShopAccess.all())event.registerEntityRenderer(CityShopActors.TYPES.get(point.shop()).get(),context->new CityShopActorRenderer(context,point));
        }
    }
}
