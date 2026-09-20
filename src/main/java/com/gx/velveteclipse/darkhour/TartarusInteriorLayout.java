package com.gx.velveteclipse.darkhour;

import java.util.*;

/** Seeded, connected room graph. Floors occupy separate plots in the Tartarus dimension. */
public final class TartarusInteriorLayout {
 public static final int LAST_FLOOR=250,SPACING=192,Y=64;
 public record Point(int x,int z){}
 public record Cell(int x,int y,int z,String material){}
 public record Plan(List<Cell> cells,Point entry,Point stairs,List<Point> spawns,Set<Point> walkable){}
 private final Map<String,Cell> blocks=new LinkedHashMap<>();
 private void put(int x,int y,int z,String m){blocks.put(x+","+y+","+z,new Cell(x,y,z,m));}
 private void box(int x0,int y0,int z0,int x1,int y1,int z1,String m){for(int x=x0;x<=x1;x++)for(int z=z0;z<=z1;z++)for(int y=y0;y<=y1;y++)put(x,y,z,m);}
 public static int origin(int floor){if(floor<0||floor>LAST_FLOOR)throw new IllegalArgumentException("Floor 0..250");return floor*SPACING;}
 public static int floorAt(double x){return Math.max(0,Math.min(LAST_FLOOR,(int)Math.round(x/SPACING)));}
 public static int hp(int floor){return 85+Math.max(0,floor)*8;}
 public static int level(int floor){return 1+Math.max(0,floor);}
 public static Plan hall(){
  var b=new TartarusInteriorLayout();
  for(int x=-28;x<=28;x++)for(int z=-30;z<=30;z++){
   double ring=Math.hypot(x,z*.9);String m=Math.abs(x)<=4?"carpet":ring>20&&ring<22?"gold":((Math.floorDiv(x,3)+Math.floorDiv(z,3))&1)==0?"ivory":"tile";
   b.put(x,0,z,m);b.put(x,34,z,"wall");
   if(Math.abs(x)==28||Math.abs(z)==30)for(int y=1;y<34;y++)b.put(x,y,z,y==2||y==27?"gold":y>5&&y<24&&Math.floorMod(Math.abs(x)==28?z:x,7)<3?"window":"wall");
  }
  // Tall fluted columns frame the central processional staircase.
  for(int x:new int[]{-20,20})for(int z:new int[]{-22,-7,9,23}){
   b.box(x-2,1,z-2,x+2,2,z+2,"gold");b.box(x-1,3,z-1,x+1,25,z+1,"ivory");b.box(x-2,26,z-2,x+2,27,z+2,"gold");
   for(int y=3;y<26;y++)b.put(x,y,z-2,"ivory");
  }
  for(int step=0;step<16;step++){
   int z=5-step,y=1+step;
   b.box(-5,1,z,5,y-1,z,"ivory");b.box(-5,y,z,5,y,z,"blue_stairs");
   for(int x:new int[]{-7,7}){b.box(x,1,z,x,y,z,"ivory");b.put(x,y+1,z,"gold");}
  }
  b.box(-7,1,-17,7,16,-11,"ivory");b.box(-5,16,-17,5,16,-11,"carpet");
  // Golden clock and actual black passage below its dial.
  for(int dx=-8;dx<=8;dx++)for(int dy=-8;dy<=8;dy++){
   double r=Math.hypot(dx,dy);if(r<=8.4)b.put(dx,23+dy,-18,r>7?"gold":r>6.4?"ivory":"clock");
  }
  for(int i=0;i<12;i++){double a=i*Math.PI/6;b.put((int)Math.round(Math.sin(a)*6),23+(int)Math.round(Math.cos(a)*6),-17,"gold");}
  for(int y=23;y<=28;y++)b.put(0,y,-17,"hand");for(int x=0;x<4;x++)b.put(x,23,-17,"hand");
  b.box(-2,17,-18,2,20,-18,"forward");
  b.box(-3,1,29,3,5,29,"exit");
  // Velvet Room door in the left corner, framed in silver and blue.
  b.box(-24,1,-3,-24,5,3,"ivory");b.box(-23,1,-3,-23,4,-2,"wall");b.box(-23,1,2,-23,4,3,"wall");
  b.put(-23,1,0,"velvet_door");
  return new Plan(List.copyOf(b.blocks.values()),new Point(0,23),new Point(0,-17),List.of(),Set.of());
 }
 public static Plan floor(long seed,int floor){
  origin(floor);if(floor==0)return hall();
  var r=new Random(seed ^ (floor*0x9E3779B97F4A7C15L));var b=new TartarusInteriorLayout();
  int n=4;var centers=new ArrayList<Point>();for(int z=0;z<n;z++)for(int x=0;x<n;x++)centers.add(new Point(x*24-36,z*24-36));
  var links=new ArrayList<int[]>();var visited=new boolean[16];var stack=new ArrayDeque<Integer>();stack.push(0);visited[0]=true;
  while(!stack.isEmpty()){
   int at=stack.peek();var next=new ArrayList<Integer>();for(int v:new int[]{at-4,at+4,at-1,at+1})if(v>=0&&v<16&&!visited[v]&&(v/4==at/4||v%4==at%4))next.add(v);
   if(next.isEmpty()){stack.pop();continue;}int v=next.get(r.nextInt(next.size()));links.add(new int[]{at,v});visited[v]=true;stack.push(v);
  }
  for(int i=0;i<5;i++){int a=r.nextInt(12);links.add(new int[]{a,a+4});}
  var dist=new int[16];Arrays.fill(dist,-1);dist[0]=0;var queue=new ArrayDeque<Integer>();queue.add(0);
  while(!queue.isEmpty()){int a=queue.remove();for(var edge:links){int v=edge[0]==a?edge[1]:edge[1]==a?edge[0]:-1;if(v>=0&&dist[v]<0){dist[v]=dist[a]+1;queue.add(v);}}}
  int end=1;for(int i=1;i<16;i++)if(dist[i]>dist[end])end=i;
  var tiles=new HashSet<Point>();int[] radii=new int[16];
  for(int i=0;i<16;i++){var c=centers.get(i);int radius=radii[i]=i==end?10:7+r.nextInt(4);for(int x=-radius;x<=radius;x++)for(int z=-radius;z<=radius;z++)tiles.add(new Point(c.x+x,c.z+z));}
  for(var edge:links){var a=centers.get(edge[0]);var c=centers.get(edge[1]);for(int x=Math.min(a.x,c.x)-2;x<=Math.max(a.x,c.x)+2;x++)for(int z=Math.min(a.z,c.z)-2;z<=Math.max(a.z,c.z)+2;z++)tiles.add(new Point(x,z));}
  String accent=floor<=83?"window":floor<=166?"azure":"violet";
  // Floors, ceilings, closed external walls; corridors are cut from the same union.
  var ordered=tiles.stream().sorted(Comparator.comparingInt(Point::x).thenComparingInt(Point::z)).toList();
  for(var p:ordered){b.put(p.x,0,p.z,((p.x+p.z)&1)==0?"tile":"tile_dark");b.put(p.x,13,p.z,"wall");
   for(var d:List.of(new Point(1,0),new Point(-1,0),new Point(0,1),new Point(0,-1))){var side=new Point(p.x+d.x,p.z+d.z);if(!tiles.contains(side))for(int y=0;y<=13;y++)b.put(side.x,y,side.z,y==1||y==12?"trim":y>=5&&y<=10&&(Math.floorMod(side.x,6)!=0&&Math.floorMod(side.z,6)!=0)?accent:"wall");}
  }
  var spawns=new ArrayList<Point>();
  for(int i=0;i<16;i++){var c=centers.get(i);int radius=radii[i];
   for(int x=-radius+1;x<radius;x++)for(int z=-radius+1;z<radius;z++)if(Math.abs(x)==radius-1||Math.abs(z)==radius-1)b.put(c.x+x,0,c.z+z,"border");
   if(i!=end){for(int x=-4;x<=4;x++)for(int z=-4;z<=4;z++){double d=Math.hypot(x,z);if(d<4.5)b.put(c.x+x,0,c.z+z,d>3.2&&d<4.4?accent:"seal");}spawns.add(c);}
   for(int x:new int[]{-radius+1,radius-1})for(int z:new int[]{-radius+1,radius-1}){b.box(c.x+x,1,c.z+z,c.x+x,11,c.z+z,"column");b.put(c.x+x,12,c.z+z,"trim");}
  }
  var stairs=centers.get(end);
  for(int step=0;step<8;step++){
   int z=stairs.z+4-step,y=step+1;b.box(stairs.x-3,1,z,stairs.x+3,y-1,z,"wall");b.box(stairs.x-3,y,z,stairs.x+3,y,z,"stairs");
   for(int x:new int[]{stairs.x-4,stairs.x+4}){b.box(x,1,z,x,y,z,"column");b.put(x,y+1,z,accent);}
  }
  b.box(stairs.x-3,1,stairs.z-6,stairs.x+3,8,stairs.z-4,"wall");
  b.box(stairs.x-2,9,stairs.z-6,stairs.x+2,12,stairs.z-6,floor==LAST_FLOOR?"summit":"forward");
  // First room occupies the NW corner: its north wall can never carry a corridor.
  var start=centers.get(0);int backZ=start.z-radii[0]-1;
  b.box(start.x-3,0,backZ-1,start.x+3,5,backZ-1,"wall");
  b.box(start.x-2,1,backZ,start.x+2,4,backZ,"back");spawns.remove(start);
  return new Plan(List.copyOf(b.blocks.values()),new Point(start.x,start.z+3),stairs,List.copyOf(spawns),Set.copyOf(tiles));
 }
}
