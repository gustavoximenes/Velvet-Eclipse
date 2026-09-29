package com.gx.velveteclipse.client.vfx;
import com.gx.velveteclipse.client.*;
import com.gx.velveteclipse.core.persona.*;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;
import java.util.*;

/** Emissive spell bodies plus Photon sparks. Core geometry survives custom particle-pass filtering. */
@Mod.EventBusSubscriber(modid="velvet_eclipse",value=Dist.CLIENT)
public final class SpellCoreRenderer {
 private record Effect(String skill,Element family,float scale,ImpactAnchor anchor,long seed,long began,Object world){}
 private static final Deque<Effect> effects=new ArrayDeque<>();
 public static void reset(){effects.clear();}
 public static void add(String skill,Element family,float scale,ImpactAnchor anchor,long seed){
  var w=Minecraft.m_91087_().f_91073_;if(w==null)return;while(effects.size()>=20)effects.removeFirst();effects.addLast(new Effect(skill,family,scale,anchor,seed,w.m_46467_(),w));
 }
 @SubscribeEvent public static void render(RenderLevelStageEvent event){
  if(event.getStage()!=RenderLevelStageEvent.Stage.AFTER_PARTICLES)return;
  var mc=Minecraft.m_91087_();var world=mc.f_91073_;if(world==null){reset();return;}
  double now=world.m_46467_()+event.getPartialTick();effects.removeIf(e->e.world!=world||now-e.began>=(e.skill.equals("__summon")?SummonVisual.LIFE:28));if(effects.isEmpty())return;
  var camera=event.getCamera().m_90583_();var buffers=mc.m_91269_().m_110104_();var type=VelvetVfxRenderType.type();var out=buffers.m_6299_(type);var pose=event.getPoseStack();
  for(var e:effects){double age=now-e.began;var pos=e.anchor.position(event.getPartialTick());var eye=new Vector3f((float)(camera.f_82479_-pos.f_82479_),(float)(camera.f_82480_-pos.f_82480_),(float)(camera.f_82481_-pos.f_82481_));
   pose.m_85836_();pose.m_85837_(pos.f_82479_-camera.f_82479_,pos.f_82480_+.03-camera.f_82480_,pos.f_82481_-camera.f_82481_);var matrix=pose.m_85850_().m_252922_();
   for(var s:geometry(e.skill,e.family,e.scale,age,e.seed)){
    Vector3f d=new Vector3f(s.to()).sub(s.from()),toward=new Vector3f(eye).sub(s.from());var side=new Vector3f(d).cross(toward);
    if(side.lengthSquared()<.00001f)side.set(1,0,0);side.normalize().mul(s.width());
    int c=s.color(),a=(int)(s.alpha()*255);Vector3f[] v={new Vector3f(s.from()).sub(side),new Vector3f(s.from()).add(side),new Vector3f(s.to()).add(side),new Vector3f(s.to()).sub(side)};
    for(int n=0;n<4;n++)VelvetVfxRenderType.vertex(out,matrix,v[n],c>>16&255,c>>8&255,c&255,a,n==0||n==3?0:1,n<2?0:1);
   }pose.m_85849_();
  }buffers.m_109912_(type);
 }
 public static List<SpellImpact.Stroke> geometry(String skill,Element family,float scale,double age,long seed){
  if(skill.equals("__summon"))return SummonVisual.geometry(age,seed);
  if(!skill.isEmpty()&&switch(family){case FIRE,DARK,WIND,ICE,LIGHT,NUCLEAR,PSY,ALMIGHTY->true;default->false;})return SpellImpact.geometry(skill,age,seed);
  var strokes=new ArrayList<SpellImpact.Stroke>();for(var b:PhotonEffectPlan.create(family,scale,seed).beams()){
   double t=age-b.delay();if(t<0||t>=b.life())continue;float fade=(float)Math.min(1,(b.life()-t)/6);
   var from=new Vector3f(b.from().x(),b.from().y(),b.from().z());var to=new Vector3f(b.to().x(),b.to().y(),b.to().z());
   strokes.add(new SpellImpact.Stroke(from,to,b.width(),b.color(),fade*.75f));
   strokes.add(new SpellImpact.Stroke(from,to,b.width()*.3f,0xFFFFFF,fade));
  }return strokes;
 }
}
