package com.gx.velveteclipse.client.tartarus;
import com.gx.velveteclipse.darkhour.TartarusTerminalNetwork;
import com.gx.velveteclipse.client.menu.MenuCanvas;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
public final class TerminalScreen extends Screen {
 private final TartarusTerminalNetwork.Response data;private int selected,offset;private final long opened=System.nanoTime();
 public TerminalScreen(TartarusTerminalNetwork.Response data){super(Component.m_237113_("Selecionar terminal"));this.data=data;selected=Math.max(0,data.floors().indexOf(data.current()));offset=Math.max(0,selected-7);}
 @Override public boolean m_7043_(){return false;}
 @Override public void m_86600_(){if(com.gx.velveteclipse.client.ClientBattleState.snapshot().active()||!com.gx.velveteclipse.darkhour.TartarusDungeonService.inside(net.minecraft.client.Minecraft.m_91087_().f_91074_))m_7379_();}
 private void move(int n){selected=Math.floorMod(selected+n,data.floors().size());offset=Math.min(offset,selected);if(selected>=offset+8)offset=selected-7;}
 private void choose(){TartarusTerminalNetwork.request(-1,data.floors().get(selected));m_7379_();}
 @Override public boolean m_7933_(int k,int sc,int mods){if(k==265){move(-1);return true;}if(k==264){move(1);return true;}if(k==257||k==335){choose();return true;}return super.m_7933_(k,sc,mods);}
 private double scale(){return Math.min(f_96543_/900.0,f_96544_/500.0);}private double left(){return (f_96543_-900*scale())/2;}private double top(){return (f_96544_-500*scale())/2;}
 @Override public boolean m_6375_(double x,double y,int button){if(button!=0)return true;for(int i=0;i<Math.min(8,data.floors().size()-offset);i++)if(TerminalDesign.row(i).contains((x-left())/scale(),(y-top())/scale())){if(selected==i+offset)choose();else selected=i+offset;return true;}return true;}
 @Override public boolean m_6050_(double x,double y,double delta){move(delta>0?-1:1);return true;}
 @Override public void m_88315_(GuiGraphics g,int mx,int my,float partial){g.m_280509_(0,0,f_96543_,f_96544_,0xEE00130E);var pose=g.m_280168_();pose.m_85836_();pose.m_85837_(left(),top(),0);pose.m_85841_((float)scale(),(float)scale(),1);try{g.m_286007_(() -> TerminalDesign.paint(new MenuCanvas(g),data.floors(),data.current(),selected,offset,(System.nanoTime()-opened)/1e9));}finally{g.m_280262_();pose.m_85849_();}}
}
