package com.gx.velveteclipse.client.menu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.joml.Quaternionf;
public final class MenuCanvas implements MenuDesign.Canvas {
 private final GuiGraphics g;public MenuCanvas(GuiGraphics g){this.g=g;}
 private Component styled(String t,double size){return Component.m_237113_(t).m_130938_(s->s.m_131150_(new ResourceLocation("velvet_eclipse",size>=20?"display":"body")));}
 public void fill(double x,double y,double w,double h,int color){if(w>0&&h>0&&(color>>>24)>0)g.m_280509_((int)x,(int)y,(int)Math.ceil(x+w),(int)Math.ceil(y+h),color);}
 public void poly(double[] points,int color){if((color>>>24)==0)return;var buffer=g.m_280091_().m_6299_(net.minecraft.client.renderer.RenderType.m_285907_());MenuGeometry.draw(buffer,g.m_280168_().m_85850_().m_252922_(),points,color);}
 public void text(String t,double x,double y,double size,int color,double angle){if((color>>>24)<5)return;var p=g.m_280168_();p.m_85836_();p.m_85837_(x,y,0);p.m_252781_(new Quaternionf().rotationZ((float)Math.toRadians(angle)));p.m_85841_((float)(size/11),(float)(size/11),1);g.m_280614_(Minecraft.m_91087_().f_91062_,styled(t,size),0,0,color,false);p.m_85849_();}
 public double width(String t,double size){return Minecraft.m_91087_().f_91062_.m_92852_(styled(t,size))*size/11;}
 public void item(int slot,double x,double y){}
}
