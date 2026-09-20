package com.gx.velveteclipse.client;
import com.gx.velveteclipse.network.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.event.InputEvent;
public final class BattleResultsClient {
 private static BattleResultsS2C result;private static long opened,lastAck;
 public static boolean active(){return result!=null;}
 public static void accept(BattleResultsS2C p){result=p;opened=System.nanoTime();lastAck=0;}
 public static void reset(){result=null;}
 public static boolean key(InputEvent.Key e){if(!active())return false;long now=System.nanoTime();if(Minecraft.m_91087_().f_91080_==null&&e.getAction()==1&&(e.getKey()==257||e.getKey()==335)&&now-opened>700_000_000L&&now-lastAck>800_000_000L){lastAck=now;ModNetwork.sendAction("results_ack",result.battle());}return true;}
 public static void paint(GuiGraphics g,HudDesign.Canvas c,int w,int h){
  if(result==null)return;double age=(System.nanoTime()-opened)*1e-9;
  BattleResultsDesign.paint(c,result,w,h,age);
  var p=Minecraft.m_91087_().f_91074_;
  if(p!=null){g.m_280262_();AllOutPlayerRenderer.draw(g,p,w*.82,h*.51,h*.33f,142,true,-.28f,false);}
 }
}
