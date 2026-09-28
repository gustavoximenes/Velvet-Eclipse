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
import com.gx.velveteclipse.core.battle.AllOutStyle;
import com.gx.velveteclipse.equipment.EquipmentCatalog.Family;

/** Isolated copies of each participant's standard/slim mesh, textured with that actual player's skin. */
public final class AllOutPlayerRenderer {
 private record Rig(ModelPart root,BendableCuboid right,BendableCuboid left,BendableCuboid rightSleeve,BendableCuboid leftSleeve,BendableCuboid rightLeg,BendableCuboid leftLeg,BendableCuboid rightPants,BendableCuboid leftPants){}
 private static final Map<Boolean,Rig> rigs=new HashMap<>();
 private static final com.gx.velveteclipse.client.exploration.ExplorationSwordLayer sword=new com.gx.velveteclipse.client.exploration.ExplorationSwordLayer(null);
 private static BendableCuboid arm(boolean slim,boolean left,boolean sleeve){return new BendableCuboid.Builder().setDirection(Direction.UP).build(new ICuboidBuilder.Data(left?(sleeve?48:32):40,left?48:sleeve?32:16,left?-1:slim?-2:-3,-2,-2,slim?3:4,12,4,sleeve?.25f:0,sleeve?.25f:0,sleeve?.25f:0,false,64,64));}
 private static BendableCuboid leg(boolean left,boolean pants){return new BendableCuboid.Builder().setDirection(Direction.UP).build(new ICuboidBuilder.Data(left?(pants?0:16):0,left?48:pants?32:16,-2,0,-2,4,12,4,pants?.25f:0,pants?.25f:0,pants?.25f:0,false,64,64));}
 private static Rig rig(boolean slim){return rigs.computeIfAbsent(slim,s->new Rig(LayerDefinition.m_171565_(PlayerModel.m_170825_(CubeDeformation.f_171458_,s),64,64).m_171564_(),arm(s,false,false),arm(s,true,false),arm(s,false,true),arm(s,true,true),leg(false,false),leg(true,false),leg(false,true),leg(true,true)));}
 public static void draw(GuiGraphics g,AbstractClientPlayer player,double x,double y,float size,double tick,boolean victory,float yaw,boolean palette){
  draw(g,player,x,y,size,tick,victory,yaw,palette,AllOutStyle.MAKOTO,com.gx.velveteclipse.client.equipment.EquipmentScreen.family());
 }
 public static void draw(GuiGraphics g,AbstractClientPlayer player,double x,double y,float size,double tick,boolean victory,float yaw,boolean palette,AllOutStyle style,Family family){
  if(palette)return;
  draw(g,player,new AllOutDesign.Frame(x,y,size,yaw,0,-.05f),tick,victory,style,family,0);
 }
 public static void draw(GuiGraphics g,AbstractClientPlayer player,AllOutDesign.Frame frame,double tick,boolean victory,AllOutStyle style,Family family,int participant){
  boolean slim="slim".equals(player.m_108564_());var rig=rig(slim);var root=rig.root;g.m_280262_();
  var texture=player.m_108560_();
  var pose=g.m_280168_();float fogStart=RenderSystem.getShaderFogStart(),fogEnd=RenderSystem.getShaderFogEnd();float[] color=RenderSystem.getShaderColor().clone();pose.m_85836_();
  try{
   RenderSystem.setShaderColor(1,1,1,1);RenderSystem.setShaderFogStart(Float.MAX_VALUE/2);RenderSystem.setShaderFogEnd(Float.MAX_VALUE);
   transform(pose,frame);
   var out=g.m_280091_().m_6299_(net.minecraftforge.client.ForgeRenderTypes.getUnlitTranslucent(texture));
   var visible=java.util.EnumSet.noneOf(net.minecraft.world.entity.player.PlayerModelPart.class);for(var part:net.minecraft.world.entity.player.PlayerModelPart.values())if(player.m_36170_(part))visible.add(part);body(pose,out,slim,visible,tick,victory,style,family,participant);
   // Each participant carries their own weapon. Unarmed final gestures keep the face clear.
   weapon(pose,g.m_280091_(),slim,tick,victory,style,family);
  }finally{g.m_280262_();pose.m_85849_();RenderSystem.setShaderFogStart(fogStart);RenderSystem.setShaderFogEnd(fogEnd);RenderSystem.setShaderColor(color[0],color[1],color[2],color[3]);}
 }
 public static void transform(com.mojang.blaze3d.vertex.PoseStack pose,AllOutDesign.Frame frame){
  pose.m_85837_(frame.x(),frame.y(),frame.depth());pose.m_85841_(frame.size(),frame.size(),-frame.size()*frame.depthScale());
  pose.m_252781_(new Quaternionf().rotationZ(frame.roll()).rotateY(frame.yaw()).rotateX(frame.lean()));pose.m_85837_(0,-.55,0);
 }
 public static void weapon(com.mojang.blaze3d.vertex.PoseStack pose,net.minecraft.client.renderer.MultiBufferSource out,boolean slim,double tick,boolean victory,AllOutStyle style,Family family){
  weaponGeometry(pose,slim,tick,victory,style,family,(f,p)->com.gx.velveteclipse.client.exploration.WeaponModels.draw(f,p,out,15728880));
 }
 public static void weaponGeometry(com.mojang.blaze3d.vertex.PoseStack pose,boolean slim,double tick,boolean victory,AllOutStyle style,Family family,java.util.function.BiConsumer<Family,com.mojang.blaze3d.vertex.PoseStack> mesh){
  if(tick>=120&&style!=AllOutStyle.MITSURU&&style!=AllOutStyle.JUNPEI&&style!=AllOutStyle.YUKARI)return;
  if(tick>=90&&style==AllOutStyle.AKIHIKO)return;
  var root=rig(slim).root;String hand=family==Family.BOW?"left_arm":"right_arm";
  pose.m_85836_();var wrist=EvokerHandLayer.bindHand(pose,root.m_171324_(hand),new Vec3f(0,AllOutPose.bend(hand,tick,victory,style,family),0),slim);
  if(hand.equals("left_arm"))pose.m_85837_(slim?.0625:.125,0,0);
  if(family==Family.BOW){pose.m_252781_(wrist.invert());pose.m_252781_(new Quaternionf().rotationY(.45f).rotateZ(-.12f));pose.m_85841_(.68f,.68f,.68f);}
  else if(family==Family.SPEAR){pose.m_252781_(wrist.invert());pose.m_252781_(new Quaternionf().rotationZ((float)Math.PI+.1f));pose.m_85841_(.67f,.67f,.67f);}
  else com.gx.velveteclipse.client.exploration.ExplorationSwordLayer.gripCombat(pose);
  if(tick>=90&&tick<120&&style==AllOutStyle.MAKOTO){float fade=(float)(1-com.gx.velveteclipse.core.battle.AllOutTimeline.ease((tick-114)/6));pose.m_252781_(new Quaternionf().rotationY((float)(com.gx.velveteclipse.core.battle.AllOutTimeline.ease((tick-92)/18)*Math.PI*2)));pose.m_85841_(fade,fade,fade);}
  mesh.accept(family,pose);pose.m_85849_();
 }

 public static ModelPart body(com.mojang.blaze3d.vertex.PoseStack pose,com.mojang.blaze3d.vertex.VertexConsumer out,boolean slim,Set<net.minecraft.world.entity.player.PlayerModelPart> visible,double tick,boolean victory){return body(pose,out,slim,visible,tick,victory,AllOutStyle.MAKOTO);}
 public static ModelPart body(com.mojang.blaze3d.vertex.PoseStack pose,com.mojang.blaze3d.vertex.VertexConsumer out,boolean slim,Set<net.minecraft.world.entity.player.PlayerModelPart> visible,double tick,boolean victory,AllOutStyle style){return body(pose,out,slim,visible,tick,victory,style,Family.SWORD);}
 public static ModelPart body(com.mojang.blaze3d.vertex.PoseStack pose,com.mojang.blaze3d.vertex.VertexConsumer out,boolean slim,Set<net.minecraft.world.entity.player.PlayerModelPart> visible,double tick,boolean victory,AllOutStyle style,Family family){return body(pose,out,slim,visible,tick,victory,style,family,0);}
 public static ModelPart body(com.mojang.blaze3d.vertex.PoseStack pose,com.mojang.blaze3d.vertex.VertexConsumer out,boolean slim,Set<net.minecraft.world.entity.player.PlayerModelPart> visible,double tick,boolean victory,AllOutStyle style,Family family,int participant){var rig=rig(slim);var root=rig.root;
   for(String name:List.of("head","hat","body","right_arm","left_arm","right_leg","left_leg","left_sleeve","right_sleeve","left_pants","right_pants","jacket")){var part=root.m_171324_(name);part.m_233569_();var rot=AllOutPose.rotation(name,tick,victory,style,family,participant);part.m_171327_(rot.getX(),rot.getY(),rot.getZ());}
   root.m_171324_("hat").m_104315_(root.m_171324_("head"));root.m_171324_("jacket").m_104315_(root.m_171324_("body"));root.m_171324_("left_pants").m_104315_(root.m_171324_("left_leg"));root.m_171324_("right_pants").m_104315_(root.m_171324_("right_leg"));
   for(String name:List.of("head","body"))root.m_171324_(name).m_104301_(pose,out,15728880,655360);
   for(boolean left:new boolean[]{false,true}){String name=left?"left_leg":"right_leg";pose.m_85836_();root.m_171324_(name).m_104299_(pose);float bend=AllOutPose.knee(name,tick,victory);var limb=left?rig.leftLeg:rig.rightLeg;limb.applyBend(0,bend);limb.render(pose.m_85850_(),out,1,1,1,1,15728880,655360);
    if(visible.contains(left?net.minecraft.world.entity.player.PlayerModelPart.LEFT_PANTS_LEG:net.minecraft.world.entity.player.PlayerModelPart.RIGHT_PANTS_LEG)){var pants=left?rig.leftPants:rig.rightPants;pants.applyBend(0,bend);pants.render(pose.m_85850_(),out,1,1,1,1,15728880,655360);}pose.m_85849_();}
   var overlays=Map.of("hat",net.minecraft.world.entity.player.PlayerModelPart.HAT,"jacket",net.minecraft.world.entity.player.PlayerModelPart.JACKET);
   overlays.forEach((name,part)->{if(visible.contains(part))root.m_171324_(name).m_104301_(pose,out,15728880,655360);});
   for(boolean left:new boolean[]{false,true}){String name=left?"left_arm":"right_arm";pose.m_85836_();root.m_171324_(name).m_104299_(pose);float bend=AllOutPose.bend(name,tick,victory,style,family,participant);var limb=left?rig.left:rig.right;limb.applyBend(0,bend);limb.render(pose.m_85850_(),out,1,1,1,1,15728880,655360);
    if(visible.contains(left?net.minecraft.world.entity.player.PlayerModelPart.LEFT_SLEEVE:net.minecraft.world.entity.player.PlayerModelPart.RIGHT_SLEEVE)){var sleeve=left?rig.leftSleeve:rig.rightSleeve;sleeve.applyBend(0,bend);sleeve.render(pose.m_85850_(),out,1,1,1,1,15728880,655360);}pose.m_85849_();}
  return root;
 }
}
