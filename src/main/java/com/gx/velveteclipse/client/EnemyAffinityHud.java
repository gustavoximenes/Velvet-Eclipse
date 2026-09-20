package com.gx.velveteclipse.client;
import com.gx.velveteclipse.core.persona.*;import java.util.*;
public final class EnemyAffinityHud {
 public static final Element[] ELEMENTS={Element.SLASH,Element.PHYSICAL,Element.FIRE,Element.ICE,Element.ELECTRIC,Element.WIND,Element.PSY,Element.NUCLEAR,Element.LIGHT,Element.DARK,Element.ALMIGHTY};
 public static void paint(HudDesign.Canvas c,String known,int w,int h,double u,boolean targeting){
  var table=new EnumMap<Element,Affinity>(Element.class);for(String pair:known.split(",")){String[] a=pair.split(":");if(a.length==2)try{table.put(Element.valueOf(a[0]),Affinity.valueOf(a[1]));}catch(IllegalArgumentException ignored){}}
  int step=(int)(25*u),width=step*ELEMENTS.length,x=(w-width)/2,y=targeting?h-(int)(110*u):(int)(36*u);
  c.fill(x-7,y-5,width+14,(int)(34*u),0xEA06112C);
  for(int i=0;i<ELEMENTS.length;i++){int cx=x+i*step+step/2;BattleGraphics.icon(c,ELEMENTS[i],cx,y+7*u,6*u);var a=table.get(ELEMENTS[i]);int by=y+(int)(20*u);
   if(a==null)c.text("?",cx-2,by-3,0xFFFCFFFF);else if(a==Affinity.WEAK)c.text("!",cx-2,by-3,0xFFFFDA4F);else if(a!=Affinity.NORMAL){
    double r=5*u;if(a==Affinity.DRAIN){double pulse=1+Math.sin(System.nanoTime()/200_000_000.)*.25;BattleGraphics.poly(c,new double[]{cx,by-r*1.8,cx+r*1.7,by,cx,by+r*1.8,cx-r*1.7,by},((int)(85*pulse)<<24)|0x67FFD0);}
    BattleGraphics.poly(c,new double[]{cx-r,by-r,cx+r,by-r,cx+r,by+r*.2,cx,by+r,cx-r,by+r*.2},a==Affinity.DRAIN?0xFF64FFC1:0xFFB2EAFF);
    if(a==Affinity.DRAIN){c.fill(cx-1,by-3,2,6,0xFF064D3B);c.fill(cx-3,by-1,6,2,0xFF064D3B);}else if(a==Affinity.NULL)c.text("×",cx-3,by-4,0xFF11234E);else if(a==Affinity.REPEL)c.text("↗",cx-3,by-4,0xFF11234E);
   }
  }
 }
}
