package com.gx.velveteclipse.raid;
import java.util.*;
/** A single walkable train: vestibule, two guarded cars and the observation car. */
public final class TrainLayout {
 public record Cell(int x,int y,int z,String material){}
 public static int width(int z){return z>=84?8:5;}
 public static List<Cell> build(){
  var cells=new ArrayList<Cell>();
  for(int z=0;z<=125;z++)for(int x=-9;x<=9;x++)for(int y=0;y<=8;y++){
   int w=width(z);String m="air";
   if(Math.abs(x)<=w){
    if(y==0)m=Math.abs(x)<=2?"floor":"metal";
    else if(y==7)m=(Math.abs(x)==2&&z%8<6)?"lamp":"metal";
    else if(y<7&&(Math.abs(x)==w||z==0||z==125))m=y>=3&&y<=5&&z%8>=2&&z%8<=6?"window":y==2&&Math.abs(x)==w&&z%8>=2&&z%8<=6?"seat":"wall";
    else if(y==1&&Math.abs(x)==w-1&&z%8>=2&&z%8<=6)m="seat";
    else if(y<6&&y>=1&&Math.abs(x)==w-2&&z%16==1)m="rail";
    else if(y==6&&Math.abs(x)==2&&z%4==0)m="rail";
    if((z==40||z==74)&&y>0&&y<7)m="metal";
   }
   cells.add(new Cell(x,y,z,m));
  }return List.copyOf(cells);
 }
 public static int encounterZ(int stage){return stage==0?31:stage==1?65:108;}
 public static int gateZ(int stage){return stage==0?40:74;}
}
