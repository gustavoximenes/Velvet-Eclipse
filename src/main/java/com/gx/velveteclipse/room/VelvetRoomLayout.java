package com.gx.velveteclipse.room;
import java.util.*;
/** Deterministic room plan, shared by construction and offline checks/previews. */
public final class VelvetRoomLayout {
 public record Cell(int x,int y,int z,String material){}
 public static int depth(int maximum){return Math.max(11,8+((Math.max(4,maximum)-1)/4)*2);}
 public static List<Cell> cells(){return cells(4);}
 public static List<Cell> cells(int maximum){var cells=new ArrayList<Cell>();int depth=depth(maximum);
  for(int x=-8;x<=8;x++)for(int z=-9;z<=depth;z++){
   double r=Math.sqrt(x*x+(z+1)*(z+1));cells.add(new Cell(x,63,z,r>5.6&&r<6.5?"trim":"floor"));cells.add(new Cell(x,73,z,"wall"));
   if(x==-8||x==8||z==-9||z==depth)for(int y=64;y<73;y++)cells.add(new Cell(x,y,z,y==64||y==71?"trim":"wall"));
  }
  for(int x=-6;x<=6;x+=2)for(int y=65;y<=70;y++)cells.add(new Cell(x,y,-8,"grille"));
  for(int z=-6;z<=8;z+=3)for(int y=65;y<=70;y++){cells.add(new Cell(-7,y,z,"grille"));cells.add(new Cell(7,y,z,"grille"));}
  for(int x=-6;x<=6;x+=4)for(int z=-6;z<=8;z+=4)cells.add(new Cell(x,72,z,"trim"));
  return List.copyOf(cells);
 }
 public static int[][] seats(){return seats(4);}
 public static int[][] seats(int maximum){
  int count=Math.max(1,Math.min(64,maximum));int[][] out=new int[count][];
  int[] x={-1,1,-4,4};
  for(int i=0;i<count;i++)out[i]=new int[]{x[i%4],64,3+(i/4)*2-(i%4>=2?1:0)};return out;
 }
}
