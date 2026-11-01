package com.gx.velveteclipse.client.render;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;
import software.bernie.geckolib.cache.object.GeoBone;

public final class VelvetGeoEntityRenderer<T extends Entity & GeoAnimatable> extends GeoEntityRenderer<T> {
    private final boolean thanatos,izanagi;
    @Override public boolean m_5523_(T entity,net.minecraft.client.renderer.culling.Frustum frustum,double x,double y,double z){
        // Animated/downed meshes can extend far outside the entity's physical collision box.
        return com.gx.velveteclipse.client.BattleEncounterClient.participant(entity.m_19879_())||super.m_5523_(entity,frustum,x,y,z);
    }
    @Override public void m_7392_(T entity,float yaw,float partial,PoseStack stack,net.minecraft.client.renderer.MultiBufferSource buffers,int light){
        if(entity.m_20145_()||com.gx.velveteclipse.client.BattleEncounterClient.hidden(entity.m_19879_()))return;
        stack.m_85836_();
        try{
            if(com.gx.velveteclipse.client.BattleTacticsClient.downed(entity.m_19879_())){stack.m_85837_(0,.28,0);stack.m_252781_(new org.joml.Quaternionf().rotationZ(1.40f).rotateX(.12f));}
            if(entity.m_7755_().getString().startsWith("Guardião do Tártaro"))stack.m_85841_(2.2f,2.2f,2.2f);
            if(entity.m_7755_().getString().startsWith("Guardião do Trem"))stack.m_85841_(1.7f,1.7f,1.7f);
            super.m_7392_(entity,yaw,partial,stack,buffers,com.gx.velveteclipse.room.VelvetRoomService.inside(entity)?15728880:light);
        }finally{stack.m_85849_();}
    }
    public VelvetGeoEntityRenderer(EntityRendererProvider.Context context,String asset) {
        super(context,new DefaultedEntityGeoModel<T>(new ResourceLocation("velvet_eclipse",asset)){
            @Override public ResourceLocation getTextureResource(T entity){if(entity instanceof com.gx.velveteclipse.shadow.FieldShadowEntity field&&field.rare())return new ResourceLocation("velvet_eclipse","textures/entity/red_shadow.png");return super.getTextureResource(entity);}
        });
        thanatos=asset.equals("thanatos");izanagi=asset.equals("izanagi");this.f_114477_=thanatos?.45f:.55f;
        // Preserve authoring dimensions/Box UV; scale the model uniformly in both world and menus.
        if(izanagi||asset.equals("igor"))withScale(.25f);
        if(asset.equals("elizabeth"))withScale(.11f);
        if(asset.equals("pixie"))withScale(.085f);
        if(asset.equals("apsaras"))withScale(.0625f);
        if(java.util.Set.of("jack_frost","forneus","chimera","nekomata","onmoraki","angel").contains(asset))withScale(.125f);
        if(asset.equals("arsene"))withScale(.125f);
        if(asset.equals("orpheus"))withScale(.16f);
        if(asset.equals("priestess"))withScale(.4f);
        if(asset.equals("field_shadow"))withScale(.48f);
        if(asset.startsWith("enemy_"))withScale(.6f);
        if(asset.equals("field_shadow")||asset.equals("wealth_hand")||izanagi||asset.startsWith("tartarus_"))addRenderLayer(new AutoGlowingGeoLayer<T>(this));
    }
    @Override public net.minecraft.client.renderer.RenderType getRenderType(T entity,ResourceLocation texture,net.minecraft.client.renderer.MultiBufferSource buffers,float partial){
        if(entity instanceof com.gx.velveteclipse.entity.VelvetPersonaGeoEntity persona&&!persona.summoner().isEmpty())return net.minecraft.client.renderer.RenderType.m_110473_(texture);
        if(com.gx.velveteclipse.room.VelvetRoomService.inside(entity))return net.minecraftforge.client.ForgeRenderTypes.getUnlitTranslucent(texture);
        return super.getRenderType(entity,texture,buffers,partial);
    }
    @Override public software.bernie.geckolib.core.object.Color getRenderColor(T entity,float partial,int light){
        if(entity instanceof com.gx.velveteclipse.entity.VelvetPersonaGeoEntity persona&&!persona.summoner().isEmpty()){
            float alpha=com.gx.velveteclipse.client.presentation.BattleDirector.personaAlpha(persona.summoner(),partial);
            return software.bernie.geckolib.core.object.Color.ofRGBA(.55f+.45f*alpha,.85f+.15f*alpha,1f,alpha);
        }return super.getRenderColor(entity,partial,light);
    }
    @Override protected void applyRotations(T entity,PoseStack stack,float age,float bodyYaw,float partial){
        // GeckoLib supplies bodyYaw=0 for non-living entities. Props still have a saved world yaw.
        super.applyRotations(entity,stack,age,entity instanceof com.gx.velveteclipse.darkhour.TartarusPropEntity||entity instanceof com.gx.velveteclipse.raid.PriestessEntity||entity instanceof com.gx.velveteclipse.equipment.ElizabethEntity?entity.m_146908_():bodyYaw,partial);
    }
    @Override public void renderCubesOfBone(PoseStack stack,GeoBone bone,VertexConsumer out,int light,int overlay,float r,float g,float b,float a) {
        if(thanatos&&ThanatosMesh.draw(stack,bone,out,light,overlay,r,g,b,a))return;
        // Izanagi uses native GeckoLib cuboids and pixel UVs.
        super.renderCubesOfBone(stack,bone,out,light,overlay,r,g,b,a);
    }
}
