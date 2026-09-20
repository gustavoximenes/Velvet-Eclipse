package com.gx.velveteclipse.client.raid;
import com.gx.velveteclipse.client.menu.MenuDesign;
import com.gx.velveteclipse.raid.RaidNetwork;
/** Shared geometry for runtime HUD and offline visual review. */
public final class RaidDesign {
  public static void lobby(MenuDesign.Canvas c,RaidNetwork.View data){
   c.fill(0,0,900,500,0xF2071332);c.poly(new double[]{0,0,520,0,280,500,0,500},0xFF113FBC);c.poly(new double[]{20,30,835,10,812,112,0,132},0xFF102267);
   c.text("OPERAÇÃO MONORAIL",44,38,36,0xFFF6FFFF,-2);c.text("PORTA MONAD / 25F",48,92,16,0xFF71F8F3,0);
   c.text("EQUIPE  "+data.names().size()+" / "+data.capacity(),48,146,22,0xFF71F8F3,0);
   for(int i=0;i<data.names().size();i++){int col=i/6,row=i%6;double x=50+col*400,y=192+row*35;c.poly(new double[]{x,y,x+366,y-3,x+357,y+26,x-8,y+29},i==0?0xFF0EDACE:0xFF254C93);c.text(String.format("%02d",i+1),x+6,y+4,17,i==0?0xFF051932:0xFF71F8F3,0);c.text(data.names().get(i)+(i==0?"  / LÍDER":""),x+48,y+4,16,i==0?0xFF051932:0xFFF6FFFF,0);}
   c.text(data.mode().equals("building")?"AGUARDE O EMBARQUE":"ESC  Sair",42,456,17,0xFFBCDADD,0);c.poly(new double[]{555,432,855,424,840,478,540,483},0xFF0EDACE);
   c.text(data.mode().equals("building")?"PREPARANDO O TREM…":data.leader()?"ENTER  EMBARCAR":"AGUARDANDO O LÍDER",568,447,19,0xFF04182E,0);
  }
  public static void dialogue(MenuDesign.Canvas c,RaidNetwork.View data,boolean waiting){
   c.poly(new double[]{15,312,242,303,208,500,0,500},0xEE143BB0);c.poly(new double[]{196,352,872,331,898,482,155,495},0xFF154FFC);c.poly(new double[]{218,366,854,348,880,468,177,480},0xF3041036);
   c.poly(new double[]{185,334,459,313,440,360,160,373},0xFF0071EE);
   String speaker=data.names().isEmpty()?"EQUIPE":data.names().get(Math.min(data.speaker(),data.names().size()-1));c.text(speaker,227,329,22,0xFF64FFF4,0);
   StringBuilder line=new StringBuilder();int y=383;for(String word:data.line().split(" ")){if(c.width(line+word,19)>596){c.text(line.toString(),231,y,19,0xFFF4FFFF,0);line.setLength(0);y+=26;}line.append(word).append(' ');}c.text(line.toString(),231,y,19,0xFFF4FFFF,0);
   c.text(waiting?"AGUARDANDO A EQUIPE":"ENTER  Continuar",674,455,12,0xFF72F3F5,0);
  }
}
