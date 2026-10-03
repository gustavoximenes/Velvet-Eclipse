package com.gx.velveteclipse.tests;
import com.gx.velveteclipse.darkhour.*;
import com.gx.velveteclipse.client.tartarus.TartarusMeshData;
import com.google.gson.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import io.netty.buffer.Unpooled;
import java.nio.file.*;
import java.util.*;
import static com.gx.velveteclipse.darkhour.TartarusInteriorLayout.*;

/** Navigation, migration, native geometry and minimap contracts; no game automation. */
public final class TartarusReworkAudit {
 private static int checks;
 private static void check(boolean b,String message){checks++;if(!b)throw new AssertionError(message);}
 private static final int[][] DIR={{0,1},{0,-1},{1,0},{-1,0}};
 private static Set<Point> reachable(Set<Point> tiles,Point start){var seen=new HashSet<Point>();var queue=new ArrayDeque<Point>();queue.add(start);seen.add(start);while(!queue.isEmpty()){var p=queue.remove();for(var d:DIR){var q=new Point(p.x()+d[0],p.z()+d[1]);if(tiles.contains(q)&&seen.add(q))queue.add(q);}}return seen;}
 public static void main(String[] args)throws Exception{
  Files.createDirectories(Path.of("build/reports/tartarus-rework"));var gson=new Gson();
  var variants=new HashSet<String>();
  for(long seed:new long[]{0,1,8353,781241,Long.MIN_VALUE,Long.MAX_VALUE})for(int f:new int[]{1,7,9,11,24,26,49,99,249}){
   var old=legacyFloor(seed,f);var p=floor(seed,f);String label=" seed="+seed+" floor="+f;
   check(p.entry().equals(old.entry())&&p.stairs().equals(old.stairs())&&p.spawns().equals(old.spawns()),"random graph anchors retained"+label);
   check(props(seed,f,p).equals(props(seed,f,old)),"treasure identity and coordinates retained"+label);
   var tiles=TartarusMapGeometry.tiles(p);check(tiles.equals(p.walkable()),"shared minimap footprint");var seen=reachable(tiles,p.entry());
   check(seen.contains(p.stairs()),"stair route connected"+label);
   for(var at:p.spawns())check(seen.contains(at),"encounter route connected"+label+" at="+at);
   for(var prop:props(seed,f,p))check(seen.contains(prop.at()),"treasure connected"+label);
   var occupied=new HashSet<Point>();for(var c:p.cells())if((c.y()==1||c.y()==2)&&!Set.of("scenic_light","back","forward").contains(c.material()))occupied.add(new Point(c.x(),c.z()));
   var ground=new HashSet<>(tiles);ground.removeAll(occupied);var accessible=reachable(ground,p.entry());
   for(var at:p.spawns())check(accessible.contains(at),"physical two-block route to encounter"+label+" "+at);
   check(accessible.contains(new Point(p.stairs().x(),p.stairs().z()+6)),"physical route to staircase foot"+label);
   for(var piece:p.architecture())if(piece.model().startsWith("room_")){
    var parts=piece.model().split("_");int mask=Integer.parseInt(parts[3]);
    if(parts[2].equals("platforms")){
     check(mask==5||mask==10,"twin stairs only on a straight connection");
     for(int sign:new int[]{-1,1}){int x=piece.x()+(mask==10?sign*24:0),z=piece.z()+(mask==5?sign*24:0);check(p.architecture().stream().anyMatch(next->next.model().startsWith("room_")&&next.x()==x&&next.z()==z),"twin stairs connect to a room at both ends");}
    }
    for(var q:TartarusArchitecture.entrances(Integer.parseInt(parts[1]),mask)){var at=new Point(q.x()+piece.x(),q.z()+piece.z());check(!occupied.contains(at),"five-wide doorway clearance "+piece+" "+at);}
   }
   for(var at:p.spawns())for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)check(!occupied.contains(new Point(at.x()+x,at.z()+z)),"encounter 3x3 clearance"+label);
   for(var prop:props(seed,f,p))check(!occupied.contains(prop.at()),"treasure clearance"+label);
   for(var piece:p.architecture())if(piece.model().startsWith("room_")){variants.add(piece.model().split("_")[2]);for(var q:TartarusArchitecture.footprint(piece.model(),"blocked"))check(!tiles.contains(new Point(piece.x()+q.x(),piece.z()+q.z())),"pillar excluded from map");}
   var anchors=TartarusMeshPlacement.anchors(p);check(anchors.size()<200,"bounded section anchors");for(var a:anchors){check(a.spec().instances().size()<2048,"bounded mesh save packet");for(var c:p.cells())if(c.x()==a.x()&&c.y()==a.y()&&c.z()==a.z())check(!Set.of("back","forward","scenic_stairs","velvet_door").contains(c.material()),"anchor never replaces interaction");}
   if(seed==781241&&f==7)Files.writeString(Path.of("build/reports/tartarus-rework/floor-7.json"),gson.toJson(p));
  }
  check(variants.containsAll(Set.of("plain","long","pillars","platforms","stairs")),"all room variants used");
  for(int f:new int[]{0,10,20,25,50,100,250}){
   var p=floor(781241,f);check(p.entry().equals(legacyFloor(781241,f).entry()),"fixed arrival retained");
   var tiles=TartarusMapGeometry.tiles(p);check(reachable(tiles,p.entry()).contains(p.stairs()),"fixed stair connected "+f);
   var accesses=TartarusMapGeometry.accesses(p,f,false);check(accesses.stream().anyMatch(m->m.kind()==3)&&accesses.stream().anyMatch(m->m.kind()==4)&&accesses.stream().anyMatch(m->m.kind()==5),"return, terminal, Velvet mapped "+f);
   if(f==0){check(accesses.stream().anyMatch(m->m.kind()==7),"clock portal marked");check(p.cells().stream().noneMatch(c->c.material().equals("forward")&&c.z()!=-22),"clock trigger recessed at visible plane");for(int x=-2;x<=2;x++)for(int z=-22;z<=-17;z++){final int xx=x,zz=z;check(p.cells().stream().noneMatch(c->c.x()==xx&&c.z()==zz&&c.y()>=17&&c.y()<=20&&c.material().equals("scenic_wall")),"clock approach clear");}}
   if(bossFloor(f))check(accesses.stream().anyMatch(m->m.kind()==8)&&TartarusMapGeometry.accesses(p,f,true).stream().noneMatch(m->m.kind()==8),"gate marker follows defeat");
   if(f==25)check(accesses.stream().anyMatch(m->m.kind()==6),"Monad mapped");
   Files.writeString(Path.of("build/reports/tartarus-rework/floor-"+f+".json"),gson.toJson(p));
  }
  migration();mesh();codec();doorAccess();mapPreview();
  Files.writeString(Path.of("build/reports/tartarus-rework/summary.json"),gson.toJson(Map.of("checks",checks,"seeds",6,"normalFloors",54,"fixedFloors",7,"inGame",false,"result","PASS")));
  System.out.println("PASS Tartarus rework: "+checks+" checks, 54 generated floors, fixed hall/guardians/transitions, mesh partition, save migration and minimap. Offline only.");
 }
 private static void migration(){
  var s=new TartarusDungeonService.State();s.built.add(10);s.repaired.add(10);s.intervals.add(10);s.defeated.add(10);s.population.count(99,10);s.progress.generated.put(10,s.progress.generation(10));s.progress.treasures.add("25:1");s.appearanceInProgress.add(10);
  var slot=new CompoundTag();slot.m_128379_("defeated",true);s.slots.put("10:0",slot);var prop=new CompoundTag();prop.m_128359_("entity","a8d41dbf-e65e-4aba-bb40-744059a4b7fd");s.props.put("10:terminal:0",prop);
  var reloaded=TartarusDungeonService.State.load(s.m_7176_(new CompoundTag()));
  check(reloaded.defeated.equals(s.defeated)&&reloaded.slots.get("10:0").m_128471_("defeated")&&reloaded.props.get("10:terminal:0").m_128461_("entity").equals(prop.m_128461_("entity"))&&reloaded.progress.treasures.equals(s.progress.treasures),"migration restart retains bosses, enemies, prop UUIDs and loot");
  check(!TartarusAppearance.ready(reloaded,10),"partial migration never exposed");reloaded.appearance43.add(10);reloaded.appearanceInProgress.remove(10);
  var old=TartarusDungeonService.State.load(reloaded.m_7176_(new CompoundTag()));check(old.appearanceVersions.get(10)==1&&!TartarusAppearance.ready(old,10),"tested WIP worlds require repaired architecture");
  reloaded.appearanceVersions.put(10,TartarusArchitecture.VERSION);check(TartarusAppearance.ready(reloaded,10),"complete migration ready");check(TartarusAppearance.ready(TartarusDungeonService.State.load(reloaded.m_7176_(new CompoundTag())),10),"complete version persists without rebuild");
 }
 private static void mesh()throws Exception{
  var catalog=JsonParser.parseString(Files.readString(Path.of("src/main/resources/assets/velvet_eclipse/tartarus/catalog.json"))).getAsJsonObject();
  for(var entry:catalog.entrySet()){
   float[] raw=TartarusMeshData.load(entry.getKey());check(raw.length/11==entry.getValue().getAsJsonObject().get("vertices").getAsInt(),"native vertex count "+entry.getKey());for(float v:raw)if(!Float.isFinite(v))throw new AssertionError("nonfinite geometry");
  }
  // Area conservation at a section edge catches missing or doubled triangles in a seam.
  var a=new float[]{15,1,0,0,0,1,1,1,1,1,0};var b=a.clone();b[0]=18;var c=a.clone();c[1]=4;
  var left=TartarusMeshData.clip(List.of(a,b,c),0,16,false);var right=TartarusMeshData.clip(List.of(a,b,c),0,16,true);check(Math.abs(area(left)+area(right)-4.5)<1e-5,"clipped mesh conserves surface at chunk boundary");
 }
 private static double area(List<float[]> p){double sum=0;for(int i=1;i+1<p.size();i++){var a=p.get(0);var b=p.get(i);var c=p.get(i+1);sum+=Math.abs((b[0]-a[0])*(c[1]-a[1])-(b[1]-a[1])*(c[0]-a[0]))*.5;}return sum;}
 private static void codec(){
  var p=floor(1,25);var chart=new TartarusMapData();chart.reveal(25,0,275);check(!chart.known(25,0,236),"stairs remain hidden before exploration");
  var markers=TartarusMapGeometry.accesses(p,25,false);var view=new TartarusMapNetwork.View(25,-22,232,45,49,new byte[(45*49+7)/8],markers);var b=new FriendlyByteBuf(Unpooled.buffer());try{TartarusMapNetwork.View.encode(view,b);check(TartarusMapNetwork.View.decode(b).markers().equals(markers),"all new access markers survive network");}finally{b.release();}
 }
 private static void doorAccess(){
  var directions=new net.minecraft.core.Direction[]{net.minecraft.core.Direction.NORTH,net.minecraft.core.Direction.EAST,net.minecraft.core.Direction.SOUTH,net.minecraft.core.Direction.WEST};
  double[][] centers={{.5,.125},{.875,.5},{.5,.875},{.125,.5}};
  for(int i=0;i<4;i++){var d=directions[i];double x=centers[i][0],z=centers[i][1];check(com.gx.velveteclipse.room.VelvetDoorPassage.inOpening(d,x,0,z),"door crossing orientation "+d);check(com.gx.velveteclipse.room.VelvetDoorPassage.inOpening(d,x+(i%2==0?1:0),0,z+(i%2==1?1:0)),"both door leaves accept crossing "+d);check(!com.gx.velveteclipse.room.VelvetDoorPassage.inOpening(d,x+3,0,z+3),"no remote door crossing "+d);check(!com.gx.velveteclipse.room.VelvetDoorPassage.inOpening(d,x,5,z),"door height bounded");}
 }
 private static void mapPreview()throws Exception{
  var image=new java.awt.image.BufferedImage(1400,870,java.awt.image.BufferedImage.TYPE_INT_RGB);var g=image.createGraphics();g.setColor(new java.awt.Color(0x071026));g.fillRect(0,0,1400,870);g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,java.awt.RenderingHints.VALUE_ANTIALIAS_ON);var painter=new Painter(g);
  painter.text("MINIMAPA • CONTORNOS REAIS DO NOVO TÁRTARO",25,20,26,0xFFF3FAFF,0);painter.text("Renderização offline do HUD do mod • exemplo com setores explorados",25,58,16,0xFF8CD9E8,0);
  int index=0;for(int f:new int[]{0,7,10,25}){
   var p=floor(781241,f);var tiles=TartarusMapGeometry.tiles(p);int minX=tiles.stream().mapToInt(Point::x).min().orElseThrow(),minZ=tiles.stream().mapToInt(Point::z).min().orElseThrow(),w=tiles.stream().mapToInt(Point::x).max().orElseThrow()-minX+1,h=tiles.stream().mapToInt(Point::z).max().orElseThrow()-minZ+1;byte[] bits=new byte[(w*h+7)/8];for(var at:tiles){int i=(at.z()-minZ)*w+at.x()-minX;bits[i/8]|=(byte)(1<<(i%8));}
   var markers=new ArrayList<>(TartarusMapGeometry.accesses(p,f,false));markers.add(new TartarusMapNetwork.Marker(1,p.stairs().x()+.5f,f==0?5.5f:p.stairs().z()+4.5f));for(var prop:props(781241,f,p))if(prop.kind().equals("treasure"))markers.add(new TartarusMapNetwork.Marker(2,prop.at().x()+.5f,prop.at().z()+.5f));
   var view=new TartarusMapNetwork.View(f,minX,minZ,w,h,bits,markers);com.gx.velveteclipse.client.tartarus.TartarusMapDesign.paint(painter,view,40+index%2*690,120+index/2*370,305,p.entry().x()+.5,p.entry().z()+.5,180,true);
   painter.text(f==0?"Hall / relógio e Velvet":f==7?"7F / salas e pilares":f==10?"10F / guardião e bloqueio":"25F / serviços e tesouros",370+index%2*690,170+index/2*370,16,0xFF8CD9E8,0);index++;
  }g.dispose();javax.imageio.ImageIO.write(image,"png",Path.of("build/reports/tartarus-rework/minimapa.png").toFile());
 }
 private record Painter(java.awt.Graphics2D g) implements com.gx.velveteclipse.client.menu.MenuDesign.Canvas {
  public void fill(double x,double y,double w,double h,int c){g.setColor(new java.awt.Color(c,true));g.fill(new java.awt.geom.Rectangle2D.Double(x,y,w,h));}
  public void poly(double[] a,int c){var p=new java.awt.geom.Path2D.Double();p.moveTo(a[0],a[1]);for(int i=2;i<a.length;i+=2)p.lineTo(a[i],a[i+1]);p.closePath();g.setColor(new java.awt.Color(c,true));g.fill(p);}
  public void text(String s,double x,double y,double size,int c,double angle){var old=g.getTransform();g.translate(x,y);g.rotate(Math.toRadians(angle));g.setColor(new java.awt.Color(c,true));g.setFont(new java.awt.Font("SansSerif",java.awt.Font.BOLD,(int)size));g.drawString(s,0,(int)(size*.8));g.setTransform(old);}
  public double width(String s,double size){return s.length()*size*.55;}public void item(int slot,double x,double y){}
 }
}
