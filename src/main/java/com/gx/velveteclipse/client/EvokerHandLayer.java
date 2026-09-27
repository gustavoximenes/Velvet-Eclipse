package com.gx.velveteclipse.client;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import dev.kosmx.playerAnim.impl.IAnimatedPlayer;
import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.core.util.Vec3f;
import org.joml.Quaternionf;
import java.util.*;
import java.lang.reflect.Method;
/** A cached prop drawn in the player's hand space, after the real Bendy transform. */
public final class EvokerHandLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
 private static final Set<Object> ATTACHED=Collections.newSetFromMap(new WeakHashMap<>());
 private static final ResourceLocation TEXTURE=new ResourceLocation("velvet_eclipse","textures/entity/evoker_fx.png");
 private final ModelPart gun;
 private final ModelPart slide;
 public static void attach(Object renderer) {
  if(ATTACHED.contains(renderer))return;
  try { for(Class<?> c=renderer.getClass();c!=null;c=c.getSuperclass()) for(Method m:c.getDeclaredMethods())
   if((m.getName().equals("m_115326_")||m.getName().equals("addLayer"))&&m.getParameterCount()==1) {
    m.setAccessible(true);m.invoke(renderer,new EvokerHandLayer((RenderLayerParent)renderer)); ATTACHED.add(renderer);return;
   }
  }catch(Exception e){throw new IllegalStateException("Cannot attach Evoker hand layer",e);}
 }
 public EvokerHandLayer(RenderLayerParent<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>> parent){super(parent);
  var parts=createParts();gun=parts.get("root");slide=parts.get("slide");
 }
 public static Map<String,ModelPart> createParts(){
  Map<String,ModelPart> parts=new LinkedHashMap<>();
  parts.put("markings",part(List.of(cube(0,32,-0.855f,-0.08999999999999989f,-0.4275f,0.1575f,0.099f,0.027f),cube(0,32,-0.4949999999999999f,-0.08999999999999989f,-0.4275f,0.1575f,0.099f,0.027f),cube(0,32,-0.135f,-0.08999999999999989f,-0.4275f,0.1575f,0.099f,0.027f),cube(0,32,0.22499999999999998f,-0.08999999999999989f,-0.4275f,0.1575f,0.099f,0.027f),cube(0,32,-0.855f,-0.08999999999999989f,0.4005f,0.1575f,0.099f,0.027f),cube(0,32,-0.4949999999999999f,-0.08999999999999989f,0.4005f,0.1575f,0.099f,0.027f),cube(0,32,-0.135f,-0.08999999999999989f,0.4005f,0.1575f,0.099f,0.027f),cube(0,32,0.22499999999999998f,-0.08999999999999989f,0.4005f,0.1575f,0.099f,0.027f)),Map.of()));
parts.get("markings").m_104227_(0f,-0.405f,0f);
parts.put("accent",part(List.of(cube(48,0,0.022500000000000006f,0.3600000000000001f,-0.3555f,0.306f,1.1925f,0.045f),cube(56,0,0.07200000000000001f,0.4724999999999999f,-0.378f,0.207f,0.9675f,0.036f),cube(48,0,0.022500000000000006f,0.3600000000000001f,0.3105f,0.306f,1.1925f,0.045f),cube(56,0,0.07200000000000001f,0.4724999999999999f,0.342f,0.207f,0.9675f,0.036f)),Map.of()));
parts.get("accent").m_104227_(0f,0f,0f);
parts.put("grip",part(List.of(cube(32,0,-0.54f,-0.1349999999999998f,-0.315f,1.215f,2.0025f,0.63f),cube(0,26,-0.6075f,1.755f,-0.369f,1.35f,0.2025f,0.738f)),Map.of("accent",parts.get("accent"))));
parts.get("grip").m_104227_(0.9f,0.22499999999999998f,0f);
parts.get("grip").m_171327_(0f,0f,0.3141592653589793f);
parts.put("slide",part(List.of(cube(0,4,-2.115f,-0.40949999999999986f,-0.4185f,4.095f,0.162f,0.837f),cube(0,8,1.53f,-0.5264999999999999f,-0.2835f,0.3375f,0.144f,0.567f)),Map.of()));
parts.get("slide").m_104227_(0f,-0.4950000000000001f,0f);
parts.put("weapon",part(List.of(cube(0,0,-2.34f,-0.9450000000000001f,-0.405f,4.59f,0.855f,0.81f),cube(0,12,-2.6550000000000002f,-0.765f,-0.324f,0.63f,0.7425f,0.648f),cube(0,18,-1.845f,-0.11249999999999999f,-0.342f,3.33f,0.45f,0.684f),cube(0,24,-1.1249999999999998f,0.5085f,-0.2475f,0.9675f,0.234f,0.495f),cube(0,25,-1.1249999999999998f,0.2024999999999999f,-0.2025f,0.216f,0.54f,0.405f)),Map.of("slide",parts.get("slide"),"grip",parts.get("grip"),"markings",parts.get("markings"))));
parts.get("weapon").m_104227_(0f,-1.53f,0f);
parts.put("root",part(List.of(),Map.of("weapon",parts.get("weapon"))));
parts.get("root").m_104227_(-0.9675f,0.405f,0f);

  return parts;
 }

 private static ModelPart part(List<ModelPart.Cube> cubes,Map<String,ModelPart> children){return new ModelPart(cubes,children);}
 private static ModelPart.Cube cube(int u,int v,float x,float y,float z,float dx,float dy,float dz){return new ModelPart.Cube(u,v,x,y,z,dx,dy,dz,0,0,0,false,64,64,EnumSet.allOf(Direction.class));}
 @Override public void m_6494_(PoseStack ps, MultiBufferSource buffers,int light,AbstractClientPlayer player,float walk,float amount,float partial,float age,float headYaw,float headPitch){
  if(!com.gx.velveteclipse.client.presentation.BattleDirector.summoning(player.m_20148_()))return;
  float t=(float)com.gx.velveteclipse.client.presentation.BattleDirector.age(partial);
  if(t<1||t>59)return;
  ModelPart arm=m_117386_().f_102811_;
  Vec3f bend=Vec3f.ZERO;
  if(player instanceof IAnimatedPlayer animated)bend=animated.playerAnimator_getAnimation().get3DTransform("rightArm",TransformType.BEND,Vec3f.ZERO);
  ps.m_85836_();
  Quaternionf handRotation=bindHand(ps,arm,bend,"slim".equals(player.m_108564_()));
  // Keep the wrist orientation deliberate while its position follows the actual hand.
  ps.m_252781_(handRotation.invert());
  ps.m_252781_(EvokerPose.weaponRotation(t));
  float recoil=t>=38&&t<=42?(float)Math.sin((t-38)/4f*Math.PI):0;
  float visible=Math.min(smooth((t-1)/4f),1-smooth((t-54)/5f));
  ps.m_85841_(visible,visible,visible);
  slide.f_104200_=0.45f*recoil;
  gun.m_104301_(ps,buffers.m_6299_(RenderType.m_110452_(TEXTURE)),light,655360);
  ps.m_85849_();
 }
 private static float smooth(float x){x=Math.max(0,Math.min(1,x));return x*x*(3-2*x);}
 public void renderPortrait(PoseStack ps,com.mojang.blaze3d.vertex.VertexConsumer out,float alpha){
  gun.m_104301_(ps,new com.gx.velveteclipse.client.exploration.EntrySilhouette(out,alpha,0xF6FFFF),15728880,655360);
 }
 public static Quaternionf bindHand(PoseStack ps,ModelPart arm,Vec3f bend,boolean slim){
  Quaternionf elbow=new Quaternionf().rotateAxis(bend.getY(),(float)Math.cos(bend.getX()),0,(float)Math.sin(bend.getX()));
  Quaternionf upper=new Quaternionf().rotationZYX(arm.f_104205_,arm.f_104204_,arm.f_104203_);
  arm.m_104299_(ps);ps.m_252880_(slim?-.03125f:-.0625f,.25f,0);ps.m_252781_(elbow);ps.m_252880_(0,.375f,0);
  return upper.mul(elbow);
 }
}
