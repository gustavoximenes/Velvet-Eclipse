package com.gx.velveteclipse.client.exploration;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import dev.kosmx.playerAnim.impl.IAnimatedPlayer;
import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.core.util.Vec3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Quaternionf;
import java.util.*;
import java.lang.reflect.Field;

/** Original prop; transforms once from the final animated right arm through its elbow. */
@Mod.EventBusSubscriber(modid="velvet_eclipse",bus=Mod.EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class ExplorationSwordLayer extends RenderLayer<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>> {
    private static final ResourceLocation TEXTURE=new ResourceLocation("velvet_eclipse","textures/entity/exploration_sword.png");
    private final ModelPart sword;
    public ExplorationSwordLayer(RenderLayerParent<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>> parent){
        super(parent);
        sword=new ModelPart(List.of(cube(0,-.65f,-2,-.5f,1.3f,4,1),cube(16,-2.1f,1.5f,-.6f,4.2f,.8f,1.2f),
            cube(32,-.64f,2.2f,-.16f,1.28f,16,.32f),cube(40,-.12f,2.2f,-.22f,.24f,15,.44f),cube(16,-.9f,-2.5f,-.7f,1.8f,.7f,1.4f)),Map.of());
    }
    private static ModelPart.Cube cube(int u,float x,float y,float z,float w,float h,float d){return new ModelPart.Cube(u,0,x,y,z,w,h,d,0,0,0,false,64,64,EnumSet.allOf(Direction.class));}
    public void drawProp(PoseStack pose,MultiBufferSource buffers,int light){
        WeaponModels.draw(com.gx.velveteclipse.client.equipment.EquipmentScreen.family(),pose,buffers,light);
    }
    @SubscribeEvent public static void layers(EntityRenderersEvent.AddLayers e){
        for(String skin:e.getSkins()){
            PlayerRenderer renderer=e.getSkin(skin);if(renderer==null)continue;
            gateHeldItems(renderer);renderer.m_115326_(new ExplorationSwordLayer(renderer));
        }
    }
    @SuppressWarnings({"unchecked","rawtypes"}) private static void gateHeldItems(PlayerRenderer renderer){
        // Resolve once when renderers are built. Preserve the original layer and inventory.
        Field field=null;
        for(String name:new String[]{"f_115291_","layers"})try{field=LivingEntityRenderer.class.getDeclaredField(name);field.setAccessible(true);break;}catch(ReflectiveOperationException ignored){}
        if(field==null)throw new IllegalStateException("Player render layers unavailable");
        try{
            List<RenderLayer<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>>> layers=(List)field.get(renderer);
            for(var it=layers.listIterator();it.hasNext();){var original=it.next();if(!(original instanceof ItemInHandLayer))continue;
                it.set(new RenderLayer<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>>(renderer){
                    @Override public void m_6494_(PoseStack pose,MultiBufferSource buffers,int light,AbstractClientPlayer player,float walk,float amount,float partial,float age,float yaw,float pitch){
                        if(com.gx.velveteclipse.client.presentation.BattleDirector.attacking(player.m_20148_())||com.gx.velveteclipse.client.presentation.BattleDirector.summoning(player.m_20148_())||player==Minecraft.m_91087_().f_91074_&&(ExplorationClient.swordVisible()||com.gx.velveteclipse.client.PersonaCinematics.skillSequenceActive()))return;
                        original.m_6494_(pose,buffers,light,player,walk,amount,partial,age,yaw,pitch);
                    }
                });
            }
        }catch(IllegalAccessException ex){throw new IllegalStateException("Cannot wrap held item renderer",ex);}
    }
    @Override public void m_6494_(PoseStack pose,MultiBufferSource buffers,int light,AbstractClientPlayer player,float walk,float amount,float partial,float age,float yaw,float pitch){
        boolean directed=com.gx.velveteclipse.client.presentation.BattleDirector.attacking(player.m_20148_());
        if(!directed&&(player!=Minecraft.m_91087_().f_91074_||(!ExplorationClient.swordVisible()&&!EntryPlayerRenderer.drawing())))return;
        if(com.gx.velveteclipse.client.presentation.BattleDirector.summoning(player.m_20148_()))return;
        pose.m_85836_();
        var family=directed?com.gx.velveteclipse.client.presentation.BattleDirector.weapon():com.gx.velveteclipse.client.equipment.EquipmentScreen.family();
        boolean bow=family==com.gx.velveteclipse.equipment.EquipmentCatalog.Family.BOW;
        if(bow)bindOtherHand(pose,player);else bindHand(pose,m_117386_().f_102811_,player);
        if(bow){pose.m_252781_(new Quaternionf().rotationZ((float)Math.PI/2).rotateX((float)Math.PI/2));pose.m_85841_(.82f,.82f,.82f);}
        else if(com.gx.velveteclipse.client.ClientBattleState.snapshot().active()&&!EntryPlayerRenderer.drawing())gripBattle(pose,directed?com.gx.velveteclipse.client.presentation.BattleDirector.age(partial):-1);
        else grip(pose,ExplorationClient.swordAttackTime(partial));
        WeaponModels.draw(family,pose,buffers,light);
        double t=directed?com.gx.velveteclipse.core.battle.MeleeTimeline.slash(com.gx.velveteclipse.client.presentation.BattleDirector.age(partial)):ExplorationClient.swordAttackTime(partial);
        if(bow&&t>=0&&t<4)WeaponModels.arrow(pose,buffers,light);
        if(!bow&&family!=com.gx.velveteclipse.equipment.EquipmentCatalog.Family.GUN&&t>=3&&t<11){
            VertexConsumer glow=buffers.m_6299_(RenderType.m_110502_());
            double fade=Math.min(1,(t-3)/2)*Math.max(0,(11-t)/4);
            for(int i=0;i<24;i++){
                double a=-i*.055,b=-(i+1)*.055;
                int alpha=(int)(fade*(1-i/24.0)*170);
                arc(glow,pose,a,1.34,alpha);arc(glow,pose,b,1.34,alpha);
                arc(glow,pose,b,1.10,0);arc(glow,pose,a,1.10,0);
            }
        }
        pose.m_85849_();
        if(family==com.gx.velveteclipse.equipment.EquipmentCatalog.Family.FISTS){pose.m_85836_();bindOtherHand(pose,player);WeaponModels.draw(family,pose,buffers,light);pose.m_85849_();}
    }
    private void bindOtherHand(PoseStack p,AbstractClientPlayer player){
        Vec3f bend=player instanceof IAnimatedPlayer animated?animated.playerAnimator_getAnimation().get3DTransform("leftArm",TransformType.BEND,Vec3f.ZERO):Vec3f.ZERO;
        bindHand(p,m_117386_().f_102812_,bend);
    }
    private static void arc(VertexConsumer out,PoseStack pose,double angle,double radius,int alpha){
        out.m_252986_(pose.m_85850_().m_252922_(),(float)(Math.sin(angle)*radius),(float)(Math.cos(angle)*radius),0).m_6122_(95,225,255,alpha).m_5752_();
    }
    public static void grip(PoseStack pose,double attackTick){
        pose.m_252781_(new Quaternionf().rotationZ(ExplorationPose.wristRoll(attackTick)).rotateX(ExplorationPose.wristPitch(attackTick)));
        // Blade length kept clear of the ground when the hand hangs by the hip.
        pose.m_85841_(.82f,.82f,.82f);
    }
    /** Relaxed battle stance: blade points outward, forward and down beside the leg. */
    public static void gripCombat(PoseStack pose){
        gripBattle(pose,-1);
    }
    public static void gripBattle(PoseStack pose,double meleeTick){
        double attack=com.gx.velveteclipse.core.battle.MeleeTimeline.slash(meleeTick);
        double action=meleeTick<0?0:com.gx.velveteclipse.client.EvokerPose.smooth(meleeTick/5)*(1-com.gx.velveteclipse.client.EvokerPose.smooth((meleeTick-34)/10));
        float roll=(float)(.64+(ExplorationPose.wristRoll(attack)-.64)*action);
        float pitch=(float)(-.65+(ExplorationPose.wristPitch(attack)+.65)*action);
        pose.m_252781_(new Quaternionf().rotationZ(roll).rotateX(pitch));
        pose.m_85841_(.82f,.82f,.82f);
    }
    public static void bindHand(PoseStack pose,ModelPart arm,AbstractClientPlayer player){
        Vec3f bend=Vec3f.ZERO;
        if(player instanceof IAnimatedPlayer animated){
            bend=animated.playerAnimator_getAnimation().get3DTransform("rightArm",TransformType.BEND,Vec3f.ZERO);
        }
        com.gx.velveteclipse.client.EvokerHandLayer.bindHand(pose,arm,bend,"slim".equals(player.m_108564_()));
    }
    public static void bindHand(PoseStack pose,ModelPart arm,Vec3f bend){
        arm.m_104299_(pose);pose.m_252880_(-.0625f,.25f,0);
        pose.m_252781_(new Quaternionf().rotateAxis(bend.getY(),(float)Math.cos(-bend.getX()),0,(float)Math.sin(-bend.getX())));
        pose.m_252880_(0,.375f,0);
    }
    private static void tip(VertexConsumer out,PoseStack pose,int light,float ax,float ay,float az,float bx,float by,float bz,float cx,float cy,float cz){
        point(out,pose,light,ax,ay,az);point(out,pose,light,bx,by,bz);point(out,pose,light,cx,cy,cz);point(out,pose,light,cx,cy,cz);
    }
    private static void point(VertexConsumer out,PoseStack pose,int light,float x,float y,float z){
        out.m_252986_(pose.m_85850_().m_252922_(),x,y,z).m_6122_(223,242,255,255).m_7421_(.52f,.02f).m_86008_(655360).m_85969_(light).m_252939_(pose.m_85850_().m_252943_(),0,0,z>=0?1:-1).m_5752_();
    }
}
