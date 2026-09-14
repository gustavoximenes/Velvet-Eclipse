package com.gx.velveteclipse.client;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.Direction;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.kosmx.bendylib.ICuboidBuilder;
import io.github.kosmx.bendylib.impl.BendableCuboid;
import dev.kosmx.playerAnim.core.util.Vec3f;
import org.joml.Quaternionf;
import java.util.*;

/** Isolated copies of each participant's standard/slim mesh, textured with that actual player's skin. */
public final class AllOutPlayerRenderer {
 private record Rig(ModelPart root,BendableCuboid right,BendableCuboid left,BendableCuboid rightSleeve,BendableCuboid leftSleeve){}
 private static final Map<Boolean,Rig> rigs=new HashMap<>();
 private static final com.gx.velveteclipse.client.exploration.ExplorationSwordLayer sword=new com.gx.velveteclipse.client.exploration.ExplorationSwordLayer(null);
 private static BendableCuboid arm(boolean slim,boolean left,boolean sleeve){return new BendableCuboid.Builder().setDirection(Direction.UP).build(new ICuboidBuilder.Data(left?(sleeve?48:32):40,left?48:sleeve?32:16,left?-1:slim?-2:-3,-2,-2,slim?3:4,12,4,sleeve?.25f:0,sleeve?.25f:0,sleeve?.25f:0,false,64,64));}
 private static Rig rig(boolean slim){return rigs.computeIfAbsent(slim,s->new Rig(LayerDefinition.m_171565_(PlayerModel.m_170825_(CubeDeformation.f_171458_,s),64,64).m_171564_(),arm(s,false,false),arm(s,true,false),arm(s,false,true),arm(s,true,true)));}
 public static void draw(GuiGraphics g,AbstractClientPlayer player,double x,double y,float size,double tick,boolean victory,float yaw,boolean palette){
  boolean slim="slim".equals(player.m_108564_());var rig=rig(slim);var root=rig.root;g.m_280262_();
  var texture=palette?PersonaSelectorPortrait.paletteTexture(player.m_108560_()):player.m_108560_();if(texture==null)texture=player.m_108560_();
  var pose=g.m_280168_();float fogStart=RenderSystem.getShaderFogStart(),fogEnd=RenderSystem.getShaderFogEnd();float[] color=RenderSystem.getShaderColor().clone();pose.m_85836_();
  try{
   RenderSystem.setShaderColor(1,1,1,1);RenderSystem.setShaderFogStart(Float.MAX_VALUE/2);RenderSystem.setShaderFogEnd(Float.MAX_VALUE);
   pose.m_85837_(x,y,120);pose.m_85841_(size,size,-size);pose.m_252781_(new Quaternionf().rotationZ(-.05f).rotateY(yaw));pose.m_85837_(0,-.55,0);
   var out=g.m_280091_().m_6299_(net.minecraftforge.client.ForgeRenderTypes.getUnlitTranslucent(texture));
   var visible=java.util.EnumSet.noneOf(net.minecraft.world.entity.player.PlayerModelPart.class);for(var part:net.minecraft.world.entity.player.PlayerModelPart.values())if(player.m_36170_(part))visible.add(part);body(pose,out,slim,visible,tick,victory);
   if(!palette){pose.m_85836_();EvokerHandLayer.bindHand(pose,root.m_171324_("right_arm"),new Vec3f(0,AllOutPose.bend("right_arm",tick,victory),0),slim);com.gx.velveteclipse.client.exploration.ExplorationSwordLayer.gripCombat(pose);sword.drawProp(pose,g.m_280091_(),15728880);pose.m_85849_();}
  }finally{g.m_280262_();pose.m_85849_();RenderSystem.setShaderFogStart(fogStart);RenderSystem.setShaderFogEnd(fogEnd);RenderSystem.setShaderColor(color[0],color[1],color[2],color[3]);}
 }

 public static ModelPart body(com.mojang.blaze3d.vertex.PoseStack pose,com.mojang.blaze3d.vertex.VertexConsumer out,boolean slim,Set<net.minecraft.world.entity.player.PlayerModelPart> visible,double tick,boolean victory){var rig=rig(slim);var root=rig.root;
   for(String name:List.of("head","hat","body","right_arm","left_arm","right_leg","left_leg","left_sleeve","right_sleeve","left_pants","right_pants","jacket")){var part=root.m_171324_(name);part.m_233569_();var rot=AllOutPose.rotation(name,tick,victory);part.m_171327_(rot.getX(),rot.getY(),rot.getZ());}
   root.m_171324_("hat").m_104315_(root.m_171324_("head"));root.m_171324_("jacket").m_104315_(root.m_171324_("body"));root.m_171324_("left_pants").m_104315_(root.m_171324_("left_leg"));root.m_171324_("right_pants").m_104315_(root.m_171324_("right_leg"));
   for(String name:List.of("head","body","right_leg","left_leg"))root.m_171324_(name).m_104301_(pose,out,15728880,655360);
   var overlays=Map.of("hat",net.minecraft.world.entity.player.PlayerModelPart.HAT,"jacket",net.minecraft.world.entity.player.PlayerModelPart.JACKET,"left_pants",net.minecraft.world.entity.player.PlayerModelPart.LEFT_PANTS_LEG,"right_pants",net.minecraft.world.entity.player.PlayerModelPart.RIGHT_PANTS_LEG);
   overlays.forEach((name,part)->{if(visible.contains(part))root.m_171324_(name).m_104301_(pose,out,15728880,655360);});
   for(boolean left:new boolean[]{false,true}){String name=left?"left_arm":"right_arm";pose.m_85836_();root.m_171324_(name).m_104299_(pose);float bend=AllOutPose.bend(name,tick,victory);var limb=left?rig.left:rig.right;limb.applyBend(0,bend);limb.render(pose.m_85850_(),out,1,1,1,1,15728880,655360);
    if(visible.contains(left?net.minecraft.world.entity.player.PlayerModelPart.LEFT_SLEEVE:net.minecraft.world.entity.player.PlayerModelPart.RIGHT_SLEEVE)){var sleeve=left?rig.leftSleeve:rig.rightSleeve;sleeve.applyBend(0,bend);sleeve.render(pose.m_85850_(),out,1,1,1,1,15728880,655360);}pose.m_85849_();}
  return root;
 }
}
