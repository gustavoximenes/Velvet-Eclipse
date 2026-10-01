package com.gx.velveteclipse.client.room;
import com.gx.velveteclipse.room.*;
import com.gx.velveteclipse.client.menu.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.*;
import static com.gx.velveteclipse.client.menu.MenuDesign.*;

/** Presentation after a successful server transaction; skipping never repeats that transaction. */
public final class VelvetFusionScreen extends Screen {
 private final VelvetArchive.View result;private final Runnable done;
 private final VelvetScene scene=VelvetScene.load("fusion"),room=VelvetScene.load("reveal");
 private final PersonaModelPreview persona=new PersonaModelPreview();
 private final long began=System.nanoTime();private boolean ended;
 public VelvetFusionScreen(VelvetArchive.View result,Runnable done){super(Component.m_237113_("Fusão"));this.result=result;this.done=done;}
 private static int alpha(int color,double value){return ((int)(255*Math.max(0,Math.min(1,value)))<<24)|(color&0xFFFFFF);}
 private double time(){return(System.nanoTime()-began)/1e9;}
 private void finish(){if(!ended){ended=true;done.run();}}
 @Override public boolean m_7043_(){return false;}
 @Override public void m_86600_(){if(f_96541_.f_91074_==null||!VelvetRoomService.inside(f_96541_.f_91074_)){super.m_7379_();return;}if(time()>=12.7)finish();}
 @Override public void m_7379_(){finish();}
 @Override public boolean m_7933_(int key,int scan,int mods){if(key==256||key==257){finish();return true;}if(key==340||key==344){FusionNetwork.archiveRequest(new VelvetArchive.Request("stand","",List.of(),List.of(),"",""));ended=true;super.m_7379_();return true;}return true;}
 @Override public void m_88315_(GuiGraphics g,int mx,int my,float partial){
  double t=time();g.m_280509_(0,0,f_96543_,f_96544_,0xFF030421);
  if(t<7.6&&scene!=null)scene.draw(g,f_96543_,f_96544_,t,"",result.sources().size());
  else if(room!=null)room.draw(g,f_96543_,f_96544_,t-7.6,"",0);
  double scale=Math.min(f_96543_/900.,f_96544_/500.);var pose=g.m_280168_();pose.m_85836_();pose.m_85837_((f_96543_-900*scale)/2,(f_96544_-500*scale)/2,0);pose.m_85841_((float)scale,(float)scale,1);var c=new MenuCanvas(g);
  try{
   if(t>=7.6){
    g.m_286007_(() ->{for(int i=0;i<60;i++){double u=((t-7.6)*.43+i*.754877)%1,angle=i*2.399+t*.3,x=450+Math.cos(angle)*(u*200+35),y=390-u*330;c.poly(new double[]{x,y-4,x+2,y,x,y+4,x-2,y},alpha(CYAN,(1-u)*.8));}});
    persona.drawReveal(g,result.result().id(),225,23-Math.sin((t-7.6)*2.1)*8,460,417,t-7.6);
    pose.m_85837_(0,0,MenuDepth.FOREGROUND);
    if(t>9)g.m_286007_(() ->{double a=VelvetScene.smooth((t-9)/.5);c.poly(new double[]{35,404,379,389,366,466,24,480},alpha(0xFF040F40,a*.93));c.text(PersonaDetails.arcana(result.result().arcana())+" / Lv "+result.result().level(),48,414,15,alpha(CYAN,a),0);c.text(fit(c,result.result().name().toUpperCase(Locale.ROOT),307,28),48,444,28,alpha(WHITE,a),0);});
   }
   double flash=t>7.35&&t<7.6?(t-7.35)/.25:t>=7.6?Math.max(0,1-(t-7.6)/.55):0;
   if(flash>0)g.m_286007_(() ->c.fill(0,0,900,500,alpha(0xFFE9F6FF,flash)));
  }finally{g.m_280262_();pose.m_85849_();}
 }
}
