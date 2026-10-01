package com.gx.velveteclipse.client.room;
import com.gx.velveteclipse.room.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.List;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;

@Mod.EventBusSubscriber(modid="velvet_eclipse",value=Dist.CLIENT)
public final class VelvetEntranceScreen extends Screen {
 private static Boolean pending;private static long pendingAt;
 private final boolean seated;private final long began=System.nanoTime();private final VelvetScene scene=VelvetScene.load("entrance"),room=VelvetScene.load("menu");
 private VelvetEntranceScreen(boolean seated){super(Component.m_237113_("Velvet Room"));this.seated=seated;}
 public static void enter(boolean seated){pending=seated;pendingAt=System.nanoTime();}
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){if(e.phase!=TickEvent.Phase.END||pending==null)return;var mc=Minecraft.m_91087_();if(mc.f_91074_!=null&&VelvetRoomService.inside(mc.f_91074_)){boolean seat=pending;pending=null;mc.m_91152_(new VelvetEntranceScreen(seat));}else if(System.nanoTime()-pendingAt>15_000_000_000L)pending=null;}
 private double time(){return(System.nanoTime()-began)/1e9;}
 private void finish(){super.m_7379_();if(seated&&f_96541_.f_91074_!=null&&f_96541_.f_91074_.m_20202_() instanceof VelvetSeatEntity)FusionNetwork.archiveRequest(new VelvetArchive.Request("open","",List.of(),List.of(),"",""));}
 @Override public boolean m_7043_(){return false;}
 @Override public void m_86600_(){if(f_96541_.f_91074_==null||!VelvetRoomService.inside(f_96541_.f_91074_)){super.m_7379_();return;}if(time()>5.65)finish();}
 @Override public boolean m_7933_(int key,int scan,int mods){if(key==340||key==344){FusionNetwork.archiveRequest(new VelvetArchive.Request("stand","",List.of(),List.of(),"",""));super.m_7379_();}else if(key==256)finish();return true;}
 @Override public void m_88315_(GuiGraphics g,int x,int y,float partial){double t=time();g.m_280509_(0,0,f_96543_,f_96544_,0xFF07082B);if(t<5.1&&scene!=null)scene.draw(g,f_96543_,f_96544_,t,"",0);else if(room!=null)room.draw(g,f_96543_,f_96544_,0,"",0);
  if(t<.85){g.m_280509_(0,0,f_96543_,f_96544_,0xFF123BFA);double u=smooth(t/.85),width=24+f_96543_*1.18*u;g.m_280509_((int)(f_96543_*.5-width*.5),0,(int)(f_96543_*.5+width*.5),f_96544_,0xFFF1F5FF);}
  double alpha=t>=.85&&t<1.25?1-smooth((t-.85)/.4):t>4.65&&t<5.1?smooth((t-4.65)/.45):t>=5.1?1-smooth((t-5.1)/.55):0;
  if(alpha>0)g.m_280509_(0,0,f_96543_,f_96544_,((int)(alpha*255)<<24)|0xD7E7FF);
 }
 private static double smooth(double t){t=Math.max(0,Math.min(1,t));return t*t*(3-2*t);}
}
