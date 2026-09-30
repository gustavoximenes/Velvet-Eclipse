package com.gx.velveteclipse.client.exploration;

import com.gx.velveteclipse.client.presentation.ApprovedAttackRenderer;
import com.gx.velveteclipse.client.equipment.EquipmentScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.player.PlayerModelPart;
import org.joml.Quaternionf;
import java.util.EnumSet;

/** Same rigid standard/slim player rig as the field; never mutates the live renderer model. */
public final class EntryPlayerRenderer {
 private static boolean drawing;
 public static boolean drawing(){return drawing;}
 public static void reset(){drawing=false;}
 public static void draw(GuiGraphics g,int w,int h,double time,float alpha){
  var player=Minecraft.m_91087_().f_91074_;if(player==null||alpha<=0)return;
  boolean slim="slim".equals(player.m_108564_());var family=EquipmentScreen.family();
  var frame=ApprovedAttackRenderer.travelPose(family,slim,time*.52,1);
  var visible=EnumSet.noneOf(PlayerModelPart.class);for(var layer:PlayerModelPart.values())if(player.m_36170_(layer))visible.add(layer);
  g.m_280262_();var pose=g.m_280168_();pose.m_85836_();drawing=true;
  try{
   pose.m_85837_(w*.43+(time/32)*w*.035,h*.49,1000);
   float size=h*.33f*ApprovedAttackRenderer.PLAYER_SCALE;pose.m_85841_(size,size,-size);
   pose.m_252781_(new Quaternionf().rotationZ(-.19f).rotateY(-.72f).rotateX(.22f));pose.m_85837_(0,-.55,0);
   var out=new EntrySilhouette(g.m_280091_().m_6299_(RenderType.m_269166_()),alpha);
   ApprovedAttackRenderer.body(pose,out,frame,slim,visible,15728880);
   ApprovedAttackRenderer.weapon(pose,ignored->out,15728880,family,frame,-1);
  }finally{try{g.m_280262_();}finally{pose.m_85849_();drawing=false;}}
 }
 private EntryPlayerRenderer(){}
}
