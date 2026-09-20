package com.gx.velveteclipse.client.tartarus;
import com.gx.velveteclipse.client.menu.MenuDesign;
import java.util.List;
public final class TerminalDesign {
 public static MenuDesign.Rect row(int i){return new MenuDesign.Rect(46,147+i*37,487,35);}
 private static void disc(MenuDesign.Canvas c,double x,double y,double r,int color){double[] v=new double[96];for(int i=0;i<48;i++){double a=i*Math.PI/24;v[i*2]=x+Math.cos(a)*r;v[i*2+1]=y+Math.sin(a)*r;}c.poly(v,color);}
 public static void paint(MenuDesign.Canvas c,List<Integer> floors,int current,int selected,int offset,double age){
  c.fill(0,0,900,500,0xF004211B);disc(c,738,28,220,0xFF5D9430);disc(c,753,12,188,0xFFB7E663);
  for(int i=0;i<8;i++){double x=540+i*57,y=80+(i%3)*19;c.poly(new double[]{x,y,x+95,y-7,x+161,y+20,x+33,y+40},0xA0265132);}
  c.poly(new double[]{527,0,579,0,657,500,609,500},0x3534EA9B);c.fill(31,52,524,396,0xD308361E);
  c.poly(new double[]{51,68,80,68,65,101},0xFF85FF28);disc(c,65,54,8,0xFF85FF28);c.poly(new double[]{30,111,65,97,100,111,65,123},0xFF85FF28);
  c.text("SELECT TERMINAL",111,66,38,0xFFF6FFF4,0);c.fill(45,117,490,1,0xFFCCFFAE);c.text("Para qual andar deseja ir?",208,124,12,0xFFDCFFC9,0);
  for(int i=0;i<Math.min(8,floors.size()-offset);i++){int idx=i+offset,f=floors.get(idx);var r=row(i);boolean hit=idx==selected;double slide=Math.max(0,1-age*4)*65;c.fill(r.x()-slide,r.y(),r.w(),r.h(),hit?0xFFF4FFF4:0x36377131);c.fill(r.x(),r.y()+r.h(),r.w(),1,0x728ED57A);int col=hit?0xFF002318:0xFFC5FFB4;c.text(f==0?"ENTRADA":f+" F",348-slide,r.y()+1,30,col,-4);if(f==current)c.text("ATUAL",70,r.y()+12,11,col,0);}
  if(offset>0)c.poly(new double[]{541,159,553,159,547,149},0xFFC5FFB4);if(offset+8<floors.size())c.poly(new double[]{541,410,553,410,547,420},0xFFC5FFB4);
  c.text(current==0?"HALL":current+" F",614,357,67,0xFF7DFFA0,-7);c.text("TARTARUS",627,428,17,0xFFD9FFAE,0);
  c.text("SETAS  Selecionar    ENTER  Teleportar    ESC  Voltar",48,470,12,0xFFE8FFDD,0);
 }
}
