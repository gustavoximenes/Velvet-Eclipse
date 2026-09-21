package com.gx.velveteclipse.client.menu;
import com.gx.velveteclipse.VelvetEclipseMod;
import com.gx.velveteclipse.menu.MenuSnapshot;
import com.gx.velveteclipse.shadow.*;
import com.gx.velveteclipse.client.render.VelvetGeoEntityRenderer;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import java.util.*;
/** Isolated display actors: never inserted into the world or used for encounters. */
public final class BestiaryModelPreview {
 private record Actor(EnemyEntity entity,VelvetGeoEntityRenderer<EnemyEntity> renderer) {}
 private final Map<String,Actor> actors=new HashMap<>();private final Set<String> failures=new HashSet<>();
 private Actor actor(MenuSnapshot.Enemy entry){
  if(actors.containsKey(entry.asset()))return actors.get(entry.asset());var mc=Minecraft.m_91087_();if(mc.f_91073_==null)return null;
  var entity=EnemyRegistry.typeFor(EnemyRegistry.TYPES.containsKey(entry.id())?entry.id():"cowardly_maya").m_20615_(mc.f_91073_);
  var dispatcher=mc.m_91290_();var context=new EntityRendererProvider.Context(dispatcher,mc.m_91291_(),mc.m_91289_(),dispatcher.m_234586_(),mc.m_91098_(),mc.m_167973_(),mc.f_91062_);
  var actor=new Actor(entity,new VelvetGeoEntityRenderer<>(context,entry.asset()));actors.put(entry.asset(),actor);return actor;
 }
 public void draw(GuiGraphics g,MenuSnapshot.Enemy entry,double seconds){
  if(failures.contains(entry.asset()))return;Actor a;
  try{a=actor(entry);}catch(RuntimeException|LinkageError error){fail(entry.asset(),error);return;}if(a==null)return;
  var mc=Minecraft.m_91087_();var pose=g.m_280168_();var dispatcher=mc.m_91290_();var camera=new Quaternionf(dispatcher.m_253208_());var e=a.entity();
  e.f_19797_=(int)(seconds*20);float yaw=180;e.f_20883_=e.f_20884_=e.f_20885_=e.f_20886_=yaw;e.m_146922_(yaw);e.f_19859_=yaw;e.m_146926_(0);
  g.m_280262_();pose.m_85836_();
  try{float size=entry.asset().equals("priestess")?28:entry.asset().equals("wealth_hand")?34:54;pose.m_85837_(703,299,MenuDepth.MODEL);pose.m_252931_(new Matrix4f().scaling(size,size,-size));var tilt=new Quaternionf().rotationX(.06f);pose.m_252781_(new Quaternionf().rotationZ((float)Math.PI).mul(tilt));Lighting.m_166384_();dispatcher.m_252923_(new Quaternionf(tilt).conjugate());RenderSystem.runAsFancy(()->a.renderer().m_7392_(e,yaw,mc.m_91296_(),pose,g.m_280091_(),15728880));}
  catch(RuntimeException|LinkageError error){fail(entry.asset(),error);}finally{try{g.m_280262_();}finally{dispatcher.m_252923_(camera);pose.m_85849_();Lighting.m_84931_();}}
 }
 private void fail(String id,Throwable error){if(failures.add(id))VelvetEclipseMod.LOGGER.error("Bestiary preview failed: "+id,error);}
}
