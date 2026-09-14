package com.gx.velveteclipse.room;
import java.util.*;
/** Deterministic room plan, shared by construction and offline checks/previews. */
public final class VelvetRoomLayout {
 public record Cell(int x,int y,int z,String material){}
 public static List<Cell> cells(){var cells=new ArrayList<Cell>();
  for(int x=-8;x<=8;x++)for(int z=-9;z<=11;z++){
   double r=Math.sqrt(x*x+(z+1)*(z+1));cells.add(new Cell(x,63,z,r>5.6&&r<6.5?"trim":"floor"));cells.add(new Cell(x,73,z,"wall"));
   if(x==-8||x==8||z==-9||z==11)for(int y=64;y<73;y++)cells.add(new Cell(x,y,z,y==64||y==71?"trim":"wall"));
  }
  for(int x=-6;x<=6;x+=2)for(int y=65;y<=70;y++)cells.add(new Cell(x,y,-8,"grille"));
  for(int z=-6;z<=8;z+=3)for(int y=65;y<=70;y++){cells.add(new Cell(-7,y,z,"grille"));cells.add(new Cell(7,y,z,"grille"));}
  for(int x=-6;x<=6;x+=4)for(int z=-6;z<=8;z+=4)cells.add(new Cell(x,72,z,"trim"));
  return List.copyOf(cells);
 }
 public static int[][] seats(){return new int[][]{{0,64,6},{-3,64,5},{3,64,5},{-5,64,4},{5,64,4}};}
}
