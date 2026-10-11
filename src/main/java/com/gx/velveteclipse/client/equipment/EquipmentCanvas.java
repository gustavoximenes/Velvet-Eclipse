package com.gx.velveteclipse.client.equipment;
import com.gx.velveteclipse.client.menu.*;
import net.minecraft.client.gui.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.joml.Quaternionf;
public final class EquipmentCanvas implements MenuDesign.Canvas {
 private final GuiGraphics g;private final Font font;public EquipmentCanvas(GuiGraphics g,Font f){this.g=g;font=f;}
 private Component styled(String t,double s){return com.gx.velveteclipse.localization.ModText.component(t).m_130938_(v->v.m_131150_(new ResourceLocation("velvet_eclipse",s>=20?"display":"body")));}
 public void fill(double x,double y,double w,double h,int color){g.m_280509_((int)x,(int)y,(int)(x+w),(int)(y+h),color);}
 public void poly(double[] xy,int color){MenuGeometry.draw(g.m_280091_().m_6299_(net.minecraft.client.renderer.RenderType.m_285907_()),g.m_280168_().m_85850_().m_252922_(),xy,color);}
 public void text(String t,double x,double y,double size,int color,double angle){var p=g.m_280168_();p.m_85836_();p.m_85837_(x,y,0);p.m_252781_(new Quaternionf().rotationZ((float)Math.toRadians(angle)));p.m_85841_((float)(size/11),(float)(size/11),1);g.m_280614_(font,styled(t,size),0,0,color,false);p.m_85849_();}
 public double width(String t,double s){return font.m_92852_(styled(t,s))*s/11;}
 public void item(int slot,double x,double y){}
 public void wrap(String t,double x,double y,double size,double width,int lines){t=com.gx.velveteclipse.localization.DisplayText.text(t);String line="";int row=0;for(String word:t.split(" ")){if(!line.isEmpty()&&width(line+" "+word,size)>width){text(line,x,y+row*(size+3),size,MenuDesign.WHITE,0);if(++row>=lines)return;line=word;}else line+=line.isEmpty()?word:" "+word;}if(row<lines)text(line,x,y+row*(size+3),size,MenuDesign.WHITE,0);}
}
