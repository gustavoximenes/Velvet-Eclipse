package com.gx.velveteclipse.client.exploration;
import com.gx.velveteclipse.client.presentation.*;
import com.gx.velveteclipse.client.equipment.EquipmentScreen;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix4f;
import java.util.Set;
/** Approved articulated player geometry, projected in 3D; no rotated 2D sprite. */
public final class EntryPlayerRenderer {
 private static boolean drawing;
 public static boolean drawing(){return drawing;}
 public static void reset(){drawing=false;}
 public static void draw(GuiGraphics g,int w,int h,double time,float alpha){
  var player=Minecraft.m_91087_().f_91074_;if(player==null||alpha<=0)return;
  boolean slim="slim".equals(player.m_108564_());var family=EquipmentScreen.family();
  var frame=EntryRunPose.sample(family,Math.max(0,(time-12)/20),slim);
  g.m_280262_();drawing=true;
  try{
   var out=new Projected(g.m_280091_().m_6299_(RenderType.m_269166_()),new Matrix4f(g.m_280168_().m_85850_().m_252922_()),w,h,time,alpha);
   var pose=new PoseStack();pose.m_85841_(.9375f,.9375f,.9375f);pose.m_85837_(0,frame.feet()+frame.height(),0);pose.m_85841_(1,-1,-1);
   // Transparent skin overlays are not solid boxes. The actual skin remains on the arena player.
   ApprovedAttackRenderer.body(pose,out,frame,slim,Set.of(),15728880);
   ApprovedAttackRenderer.weapon(pose,ignored->out,15728880,family,frame,-1);
  }finally{try{g.m_280262_();}finally{drawing=false;}}
 }
 private static final class Projected implements VertexConsumer {
  final VertexConsumer out;final Matrix4f gui;final int w,h,alpha;final double tick;final double[] quad=new double[8];int n;
  Projected(VertexConsumer out,Matrix4f gui,int w,int h,double tick,float alpha){this.out=out;this.gui=gui;this.w=w;this.h=h;this.tick=tick;this.alpha=Math.round(alpha*255);}
  public VertexConsumer m_5483_(double x,double y,double z){var p=EntryProjection.project(x,y,z,w,h,tick);quad[n++]=p.x;quad[n++]=p.y;return this;}
  public VertexConsumer m_6122_(int r,int g,int b,int a){return this;}public VertexConsumer m_7421_(float u,float v){return this;}
  public VertexConsumer m_7122_(int a,int b){return this;}public VertexConsumer m_7120_(int a,int b){return this;}public VertexConsumer m_5601_(float x,float y,float z){return this;}
  public void m_5752_(){if(n<8)return;n=0;
   double[] clipped=quad.clone();clipped=EntryProjection.clip(clipped,1,0,0);clipped=EntryProjection.clip(clipped,-1,0,w);clipped=EntryProjection.clip(clipped,0,1,0);clipped=EntryProjection.clip(clipped,0,-1,h);
   for(var poly:EntryProjection.outside(clipped,w,h,tick))for(int i=2;i+3<poly.length;i+=2){vertex(poly,0);vertex(poly,i);vertex(poly,i+2);vertex(poly,i+2);}
  }
  private void vertex(double[] p,int i){out.m_252986_(gui,(float)p[i],(float)p[i+1],1000).m_6122_(18,5,255,alpha).m_5752_();}
  public void m_7404_(int a,int b,int c,int d){}public void m_141991_(){}
 }
 private EntryPlayerRenderer(){}
}
