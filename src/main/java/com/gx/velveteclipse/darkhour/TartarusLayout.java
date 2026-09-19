package com.gx.velveteclipse.darkhour;
import java.util.*;

/** Deterministic hollow, traversable tower; y coordinates are relative to the entrance foundation. */
public final class TartarusLayout {
 public record Cell(int x,int y,int z,int material){}
 public static final int STONE=0,TRIM=1,LIGHT=2;
 public static final int FRONT=27,STAIR_Z=28,SAFE_Z=42;
 private final Map<String,Cell> cells=new LinkedHashMap<>();
 private final int height;
 private TartarusLayout(int height){this.height=height;}
 private void put(int x,int y,int z,int material){if(y>=-96&&y<height)cells.put(x+","+y+","+z,new Cell(x,y,z,material));}
 private void box(int x0,int y0,int z0,int x1,int y1,int z1,int m){for(int y=y0;y<=y1;y++)for(int x=x0;x<=x1;x++)for(int z=z0;z<=z1;z++)put(x,y,z,m);}
 public static List<Cell> create(int height,int stairDrop){return create(height,stairDrop,false);}
 public static List<Cell> legacy(int height,int stairDrop){return create(height,stairDrop,true);}
 private static List<Cell> create(int height,int stairDrop,boolean legacy){
  if(height<40||height>512||stairDrop<1||stairDrop>96)throw new IllegalArgumentException("Tower dimensions");
  var b=new TartarusLayout(height);
  b.box(-25,0,-25,25,0,27,STONE);
  // Wide processional staircase on the south side. Each full-block step has
  // a half-height stair block supplied by the runtime palette (material 3).
  for(int step=0;step<=stairDrop;step++)for(int x=-4;x<=4;x++){
   b.put(x,-step,STAIR_Z+step,3);
   if(legacy)for(int y=-stairDrop;y<-step;y++)b.put(x,y,STAIR_Z+step,STONE);
  }
  for(int y=1;y<height-12;y++){
   int tier=y/28,r=Math.max(4,20-tier*2),cx=(tier%3-1)*2,cz=((tier/2)%3-1)*2;
   for(int x=-r;x<=r;x++)for(int z=-r;z<=r;z++){
    boolean edge=Math.abs(x)==r||Math.abs(z)==r;
    if(!edge)continue;
    // Front doorway and slit windows. Every 28 blocks a solid belt ties the tiers together.
    if(z==r&&Math.abs(x+cx)<=2&&y<9)continue;
    int material=y%28<3?TRIM:(x%4==0||z%4==0)&&y%7<4?LIGHT:STONE;
    b.put(x+cx,y,z+cz,material);
   }
   if(y%28==0)b.box(cx-r,y,cz-r,cx+r,y,cz+r,TRIM);
  }
  // Exterior spiral flying buttress, broken balconies and staggered turrets.
  for(int y=8;y<height-15;y++){
   double a=y*.095;int r=Math.max(6,26-y/14),x=(int)Math.round(Math.cos(a)*r),z=(int)Math.round(Math.sin(a)*r);
   b.box(x-1,y,z-1,x+1,y,z+1,y%5==0?LIGHT:TRIM);
  }
  for(int k=0;k<7;k++){
   int base=12+k*(height-50)/7,r=Math.max(5,20-base/14),x=k%2==0?-r-3:r+3,z=k%3==0?-r:r/2;
   for(int y=base;y<Math.min(height-2,base+34);y++){int w=y<base+22?3:Math.max(0,3-(y-base-22)/3);for(int dx=-w;dx<=w;dx++)for(int dz=-w;dz<=w;dz++)if(Math.abs(dx)==w||Math.abs(dz)==w)b.put(x+dx,y,z+dz,y%7==0?LIGHT:TRIM);}
  }
  // Circular luminous seals on the front face of the stacked structure.
  for(int y=18;y<height-26;y+=38){int tier=y/28,r=Math.max(4,20-tier*2),cx=(tier%3-1)*2,z=r+((tier/2)%3-1)*2+1;
   for(int dx=-5;dx<=5;dx++)for(int dy=-5;dy<=5;dy++){double d=Math.hypot(dx,dy);if(d<5.5)b.put(cx+dx,y+dy,z,d>4.2?TRIM:d<2||Math.abs(dx)==Math.abs(dy)?LIGHT:STONE);}
  }
  // Entrance: columns, a pediment, grille flanking an open, usable arch.
  for(int x:new int[]{-8,-5,5,8}){b.box(x,1,25,x,10,25,TRIM);b.box(x-1,1,24,x+1,1,26,TRIM);b.box(x-1,10,24,x+1,10,26,TRIM);}
  b.box(-9,11,24,9,11,26,TRIM);
  for(int y=12;y<=16;y++)b.box(-(16-y)*2,y,25,(16-y)*2,y,25,y==16?LIGHT:TRIM);
  for(int x=-17;x<=17;x++)if(Math.abs(x)>3&&x%2==0)b.box(x,1,27,x,5,27,TRIM);
  b.box(-3,1,24,-3,7,24,LIGHT);b.box(3,1,24,3,7,24,LIGHT);b.box(-3,8,24,3,8,24,LIGHT);
  for(int dx=-2;dx<=2;dx++)for(int dy=-2;dy<=2;dy++)if(dx*dx+dy*dy<=5)b.put(dx,7+dy,25,dx==0||dy==0?LIGHT:TRIM);
  // Ground hall floor and a central spine terminating at the build ceiling.
  b.box(-12,0,-12,12,0,26,TRIM);
  for(int y=height-14;y<height;y++){int r=(height-y)/5;for(int x=-r;x<=r;x++)for(int z=-r;z<=r;z++)b.put(x,y,z,y%4==0||y==height-1?LIGHT:TRIM);}
  return b.cells.values().stream().sorted(Comparator.comparingInt(Cell::y)).toList();
 }
}
