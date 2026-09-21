package com.gx.velveteclipse.client.equipment;
import com.gx.velveteclipse.client.*;
import com.gx.velveteclipse.equipment.Supplies;
public final class SupplyBattleHud {
 public static void paint(HudDesign.Canvas c,int w,int h){
  var items=ClientBattleUiState.items();double age=ClientBattleUiState.age();int first=ClientBattleUiState.selected()/7*7;
  double u=PersonaBattleDesign.unit(w,h);int x=(int)(180*u),y=(int)PersonaBattleDesign.top(w,h),right=(int)(550*u);
  c.fill(0,y,right,h-y,0xF20B24AC);c.styled("ITEM",(int)(25*u),y+(int)(20*u),(float)(4*u),0xFF58FFFF);
  for(int i=first;i<Math.min(first+7,items.size());i++){
   String id=items.get(i);var supply=Supplies.get(id);var box=PersonaBattleDesign.row(w,h,i-first,age);boolean selected=i==ClientBattleUiState.selected();
   c.fill(box.x(),box.y(),box.w(),box.h(),selected?0xFFF8FFFF:0xFF041442);
   c.styled(supply==null?"Pulse Tonic":supply.name(),box.x()+6,box.y()+4,(float)u,selected?0xFF021039:0xFFFFFFFF);
   int count=supply==null?ClientBattleState.snapshot().pulseTonics():EquipmentScreen.bag().count(id);
   c.styled("x"+count,box.x()+box.w()-(int)(40*u),box.y()+4,(float)u,selected?0xFF021039:0xFF5BFFFF);
  }
  String detail=items.isEmpty()?"Nenhum consumível disponível.":items.get(ClientBattleUiState.selected()).equals("pulse_tonic")?"Recupera 30% do HP.":Supplies.get(items.get(ClientBattleUiState.selected())).description();
  c.styled(detail.length()>61?detail.substring(0,58)+"…":detail,x,y+(int)(267*u),(float)u,0xFFFFFFFF);
  c.styled("SETAS  Item    ENTER  Escolher alvo   F1  Detalhes    ESC  Voltar",(int)(10*u),h-(int)(15*u),(float)u,0xFF70FFFF);
 }
}
