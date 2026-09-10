package com.gx.velveteclipse.client.exploration;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.joml.Quaternionf;

/** Draws the local player's OWN renderer/model/skin. No proxy or generic entity. */
public final class EntryPlayerRenderer {
    private static PlayerRenderer renderer;
    private static ExplorationSwordLayer sword;
    private static ModelPart[] parts;
    private static float[][] saved;
    private static boolean[][] flags;
    private static boolean drawing;
    public static boolean drawing(){return drawing;}
    public static void reset(){renderer=null;sword=null;parts=null;saved=null;flags=null;drawing=false;}
    public static void draw(GuiGraphics g,int w,int h,double time,float alpha){
        var mc=Minecraft.m_91087_();AbstractClientPlayer player=mc.f_91074_;if(player==null||alpha<=0)return;
        if(!(mc.m_91290_().m_114382_(player) instanceof PlayerRenderer current))return;
        PlayerModel<AbstractClientPlayer> model=current.m_7200_();
        if(renderer!=current){
            renderer=current;sword=new ExplorationSwordLayer(current);
            parts=new ModelPart[]{model.f_102808_,model.f_102809_,model.f_102810_,model.f_102811_,model.f_102812_,model.f_102813_,model.f_102814_,model.f_103374_,model.f_103375_,model.f_103376_,model.f_103377_,model.f_103378_};
            saved=new float[parts.length][9];flags=new boolean[parts.length][2];
        }
        save();g.m_280262_();var pose=g.m_280168_();pose.m_85836_();drawing=true;
        try{
            com.mojang.blaze3d.platform.Lighting.m_166384_();
            for(ModelPart part:parts){part.m_233569_();part.f_104207_=true;part.f_233556_=false;}
            model.f_102809_.f_104207_=player.m_36170_(net.minecraft.world.entity.player.PlayerModelPart.HAT);
            model.f_103374_.f_104207_=player.m_36170_(net.minecraft.world.entity.player.PlayerModelPart.LEFT_SLEEVE);
            model.f_103375_.f_104207_=player.m_36170_(net.minecraft.world.entity.player.PlayerModelPart.RIGHT_SLEEVE);
            model.f_103376_.f_104207_=player.m_36170_(net.minecraft.world.entity.player.PlayerModelPart.LEFT_PANTS_LEG);
            model.f_103377_.f_104207_=player.m_36170_(net.minecraft.world.entity.player.PlayerModelPart.RIGHT_PANTS_LEG);
            model.f_103378_.f_104207_=player.m_36170_(net.minecraft.world.entity.player.PlayerModelPart.JACKET);
            double phase=time*.52;
            pose.m_85837_(w*.43+(time/32)*w*.035,h*.49,1000);
            // GUI looks along -Z; Minecraft skin FRONT is -Z in model space.
            float size=h*.33f;pose.m_85841_(size,size,-size);
            pose.m_252781_(new Quaternionf().rotationZ(-.19f).rotateY(-.72f).rotateX(.22f));
            pose.m_85837_(0,-.55,0);
            posePart(model.f_102808_,"head",phase);posePart(model.f_102811_,"rightArm",phase);posePart(model.f_102812_,"leftArm",phase);
            posePart(model.f_102813_,"rightLeg",phase);posePart(model.f_102814_,"leftLeg",phase);
            model.f_102810_.m_171327_(0,0,0);
            model.f_102809_.m_104315_(model.f_102808_);
            model.f_103374_.m_104315_(model.f_102812_);model.f_103375_.m_104315_(model.f_102811_);
            model.f_103376_.m_104315_(model.f_102814_);model.f_103377_.m_104315_(model.f_102813_);model.f_103378_.m_104315_(model.f_102810_);
            // Unlit, translucent, uncullled quads: preserve this player's mesh,
            // cover its skin colors with the reference's uniform blue silhouette.
            var out=new EntrySilhouette(g.m_280091_().m_6299_(RenderType.m_269166_()),alpha);
            for(ModelPart part:parts)part.m_104301_(pose,out,15728880,655360);
            sword.m_6494_(pose,ignored -> out,15728880,player,0,0,0,(float)time,0,0);
        }finally{
            try{g.m_280262_();}finally{pose.m_85849_();restore();drawing=false;com.mojang.blaze3d.platform.Lighting.m_84931_();}
        }
    }
    private static void posePart(ModelPart part,String name,double phase){var v=ExplorationPose.rotation(name,phase,1,-1);part.m_171327_(v.getX(),v.getY(),v.getZ());}
    private static void save(){
        for(int i=0;i<parts.length;i++){ModelPart p=parts[i];float[] s=saved[i];s[0]=p.f_104200_;s[1]=p.f_104201_;s[2]=p.f_104202_;s[3]=p.f_104203_;s[4]=p.f_104204_;s[5]=p.f_104205_;s[6]=p.f_233553_;s[7]=p.f_233554_;s[8]=p.f_233555_;flags[i][0]=p.f_104207_;flags[i][1]=p.f_233556_;}
    }
    private static void restore(){
        for(int i=0;i<parts.length;i++){ModelPart p=parts[i];float[] s=saved[i];p.m_104227_(s[0],s[1],s[2]);p.m_171327_(s[3],s[4],s[5]);p.f_233553_=s[6];p.f_233554_=s[7];p.f_233555_=s[8];p.f_104207_=flags[i][0];p.f_233556_=flags[i][1];}
    }
}
