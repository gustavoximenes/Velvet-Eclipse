package com.gx.velveteclipse.client.presentation;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Direction;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import java.util.*;

/** Two rigid skin segments. Rotating a joint never shears or lengthens its geometry. */
public final class RigidSkinLimb {
 private record Vertex(Vector3f pos,float u,float v,Vector3f normal){
  Vertex lerp(Vertex b,float t){return new Vertex(new Vector3f(pos).lerp(b.pos,t),u+(b.u-u)*t,v+(b.v-v)*t,normal);}
 }
 private final List<List<Vertex>> upper=new ArrayList<>(),lower=new ArrayList<>();
 private final float joint;
 public RigidSkinLimb(int u,int v,float x,float y,float z,int width,int height,int depth,float inflate,float joint){
  this.joint=joint/16;
  var collector=new Collector();new ModelPart.Cube(u,v,x,y,z,width,height,depth,inflate,inflate,inflate,false,64,64,EnumSet.allOf(Direction.class)).m_171332_(new PoseStack().m_85850_(),collector,15728880,655360,1,1,1,1);
  for(var face:collector.faces){var a=clip(face,true);var b=clip(face,false);if(a.size()>=3)upper.add(a);if(b.size()>=3)lower.add(b);}
  float x0=(x-inflate)/16,x1=(x+width+inflate)/16,z0=(z-inflate)/16,z1=(z+depth+inflate)/16;
  for(boolean low:new boolean[]{false,true}){var cap=new ArrayList<Vertex>();var normal=new Vector3f(0,low?-1:1,0);float[][] corners={{x0,z0},{x0,z1},{x1,z1},{x1,z0}};for(int i=0;i<4;i++){var c=corners[low?3-i:i];cap.add(new Vertex(new Vector3f(c[0],this.joint,c[1]),(u+depth+width*.5f)/64,(v+depth+(joint-y))/64,normal));}(low?lower:upper).add(cap);}
 }
 private List<Vertex> clip(List<Vertex> input,boolean top){var out=new ArrayList<Vertex>();for(int i=0;i<input.size();i++){var a=input.get(i);var b=input.get((i+1)%input.size());boolean ia=top?a.pos.y<=joint:a.pos.y>=joint,ib=top?b.pos.y<=joint:b.pos.y>=joint;if(ia)out.add(a);if(ia!=ib)out.add(a.lerp(b,(joint-a.pos.y)/(b.pos.y-a.pos.y)));}return out;}
 public void render(PoseStack pose,VertexConsumer out,float bend,int light){
  draw(pose,out,upper,light);pose.m_85836_();pose.m_85837_(0,joint,0);pose.m_252781_(new Quaternionf().rotationX(bend));pose.m_85837_(0,-joint,0);draw(pose,out,lower,light);pose.m_85849_();
 }
 private static void draw(PoseStack pose,VertexConsumer out,List<List<Vertex>> faces,int light){
  for(var face:faces){if(face.size()==4)for(var v:face)emit(pose,out,v,light);else for(int i=1;i<face.size()-1;i++)for(var v:List.of(face.get(0),face.get(i),face.get(i+1),face.get(i+1)))emit(pose,out,v,light);}
 }
 private static void emit(PoseStack pose,VertexConsumer out,Vertex v,int light){var point=pose.m_85850_().m_252922_().transformPosition(new Vector3f(v.pos));var normal=pose.m_85850_().m_252943_().transform(new Vector3f(v.normal)).normalize();out.m_5483_(point.x,point.y,point.z).m_6122_(255,255,255,255).m_7421_(v.u,v.v).m_7122_(0,10).m_7120_(light&65535,light>>>16).m_5601_(normal.x,normal.y,normal.z).m_5752_();}
 private static final class Collector implements VertexConsumer{
  final List<List<Vertex>> faces=new ArrayList<>();List<Vertex> face=new ArrayList<>();Vector3f pos,normal;float u,v;
  public VertexConsumer m_5483_(double x,double y,double z){pos=new Vector3f((float)x,(float)y,(float)z);return this;}public VertexConsumer m_6122_(int r,int g,int b,int a){return this;}public VertexConsumer m_7421_(float u,float v){this.u=u;this.v=v;return this;}public VertexConsumer m_7122_(int a,int b){return this;}public VertexConsumer m_7120_(int a,int b){return this;}public VertexConsumer m_5601_(float x,float y,float z){normal=new Vector3f(x,y,z);return this;}public void m_5752_(){face.add(new Vertex(pos,u,v,normal));if(face.size()==4){faces.add(face);face=new ArrayList<>();}}public void m_7404_(int r,int g,int b,int a){}public void m_141991_(){}
 }
}
