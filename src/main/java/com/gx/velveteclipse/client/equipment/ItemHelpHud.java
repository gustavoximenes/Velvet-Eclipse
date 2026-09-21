package com.gx.velveteclipse.client.equipment;
import com.gx.velveteclipse.client.*;
import com.gx.velveteclipse.equipment.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
public final class ItemHelpHud {
 private static boolean open;
 public static boolean active(){return open&&ClientBattleState.snapshot().active()&&ClientBattleState.snapshot().playerTurn()&&ClientBattleUiState.page()==ClientBattleUiState.Page.ITEMS;}
 public static void toggle(){if(ClientBattleUiState.page()==ClientBattleUiState.Page.ITEMS)open=!open;Minecraft.m_91087_().f_91066_.f_92062_=false;}
 public static void close(){open=false;}
 public static void paint(GuiGraphics g,int w,int h){if(!active()){open=false;return;}var items=ClientBattleUiState.items();if(items.isEmpty())return;int i=Math.min(ClientBattleUiState.selected(),items.size()-1);String id=items.get(i);var s=Supplies.get(id);String text=s==null?"Recupera 30% do HP máximo de um aliado. Consome um item e seu turno.":s.description()+" Consome um item e seu turno. Fraquezas podem conceder 1 More.";var p=g.m_280168_();double scale=Math.min(w/900.,h/500.);p.m_85836_();p.m_85837_((w-900*scale)/2,(h-500*scale)/2,100);p.m_85841_((float)scale,(float)scale,1);var c=new EquipmentCanvas(g,Minecraft.m_91087_().f_91062_);c.fill(135,125,630,285,0xFC041537);c.text(s==null?"Pulse Tonic":s.name(),162,153,32,0xFF69FFFF,-2);c.wrap(text,164,222,22,572,6);c.text("F1 / ESC / ENTER  Voltar",165,378,17,0xFF69FFFF,0);p.m_85849_();}
}
