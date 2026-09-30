package com.gx.velveteclipse.client.presentation;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.PlayerModelPart;
import org.joml.Vector3f;
import java.util.*;

/** Inverted mesh hull: depth-tested blue rim, including with shader postprocessing disabled. */
public final class CommandOutline {
 public static final float WIDTH=.009f;
 private static final ResourceLocation COLOR=new ResourceLocation("velvet_eclipse","textures/gui/command_outline.png");
 public static void draw(PoseStack pose,MultiBufferSource buffers,ApprovedWeaponPose.Frame frame,boolean slim,Set<PlayerModelPart> visible){
  // Outer skin boxes contain transparent pixels; a solid hull around them
  // produced floating boxes around the hat, sleeves and trouser cuffs.
  var hull=new Hull();ApprovedAttackRenderer.body(pose,hull,frame,slim,Set.of(),15728880);
  hull.emit(buffers.m_6299_(RenderType.m_110452_(COLOR)));
 }
 public static final class Hull implements VertexConsumer {
  private record Vertex(Vector3f position,Vector3f normal){}
  private final List<Vertex> vertices=new ArrayList<>();
  private final Map<Vector3f,Vector3f> normals=new HashMap<>();private Vector3f position,normal;
  public VertexConsumer m_5483_(double x,double y,double z){position=new Vector3f((float)x,(float)y,(float)z);return this;}
  public VertexConsumer m_6122_(int r,int g,int b,int a){return this;}public VertexConsumer m_7421_(float u,float v){return this;}
  public VertexConsumer m_7122_(int a,int b){return this;}public VertexConsumer m_7120_(int a,int b){return this;}
  public VertexConsumer m_5601_(float x,float y,float z){normal=new Vector3f(x,y,z);return this;}
  public void m_5752_(){vertices.add(new Vertex(position,normal));normals.computeIfAbsent(position,k->new Vector3f()).add(normal);}
  public void m_7404_(int r,int g,int b,int a){}public void m_141991_(){}
  public void emit(VertexConsumer out){
   for(int face=0;face<vertices.size();face+=4)for(int index=3;index>=0;index--){
    var v=vertices.get(face+index);var offset=new Vector3f(normals.get(v.position));
    if(offset.lengthSquared()>.000001)offset.normalize(WIDTH);var p=new Vector3f(v.position).add(offset);
    out.m_5483_(p.x,p.y,p.z).m_6122_(255,255,255,255).m_7421_(.5f,.5f).m_7122_(0,10).m_7120_(240,240).m_5601_(-v.normal.x,-v.normal.y,-v.normal.z).m_5752_();
   }
  }
 }
 private CommandOutline(){}
}
