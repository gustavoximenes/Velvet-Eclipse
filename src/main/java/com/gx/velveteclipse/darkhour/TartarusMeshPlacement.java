package com.gx.velveteclipse.darkhour;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import static com.gx.velveteclipse.darkhour.TartarusInteriorLayout.*;

/** Partition meshes at the same 16-block boundaries used by native chunk culling. */
public final class TartarusMeshPlacement {
 public record Anchor(int x,int y,int z,boolean solid,int light,TartarusMeshBlockEntity.Spec spec,boolean emissive){
  public Anchor(int x,int y,int z,boolean solid,int light,TartarusMeshBlockEntity.Spec spec){this(x,y,z,solid,light,spec,false);}
 }
 private record Section(int x,int y,int z){}
 private static boolean replaceable(Cell c){return c==null||Set.of("scenic_tile","scenic_wall","scenic_window","scenic_light").contains(c.material());}
 public static List<Anchor> anchors(Plan plan){
  var sections=new TreeMap<Section,List<TartarusArchitecture.Piece>>(Comparator.comparingInt(Section::x).thenComparingInt(Section::y).thenComparingInt(Section::z));
  for(var p:plan.architecture()){
   var b=TartarusArchitecture.bounds(p.model());int x0=Math.floorDiv((int)Math.floor(b.minX()+p.x()),16)*16,y0=Math.floorDiv((int)Math.floor(b.minY()+p.y()),16)*16,z0=Math.floorDiv((int)Math.floor(b.minZ()+p.z()),16)*16;
   int x1=Math.floorDiv((int)Math.floor(b.maxX()+p.x()-.00001),16)*16,y1=Math.floorDiv((int)Math.floor(b.maxY()+p.y()-.00001),16)*16,z1=Math.floorDiv((int)Math.floor(b.maxZ()+p.z()-.00001),16)*16;
   for(int x=x0;x<=x1;x+=16)for(int y=y0;y<=y1;y+=16)for(int z=z0;z<=z1;z+=16)sections.computeIfAbsent(new Section(x,y,z),k->new ArrayList<>()).add(p);
  }
  var cells=new HashMap<String,Cell>();for(var c:plan.cells())cells.put(c.x()+","+c.y()+","+c.z(),c);
  var result=new ArrayList<Anchor>();var used=new HashSet<String>();
  for(var e:sections.entrySet()){
   for(boolean emission:new boolean[]{false,true}){
   var section=e.getKey();int ax=section.x(),ay=section.y(),az=section.z();Cell cell=cells.get(ax+","+ay+","+az);
   if(!replaceable(cell)||used.contains(ax+","+ay+","+az)){
    boolean found=false;for(int dy=0;dy<16&&!found;dy++)for(int dz=0;dz<16&&!found;dz++)for(int dx=0;dx<16&&!found;dx++){
     int x=section.x()+dx,y=section.y()+dy,z=section.z()+dz;var c=cells.get(x+","+y+","+z);if(replaceable(c)&&!used.contains(x+","+y+","+z)){ax=x;ay=y;az=z;cell=c;found=true;}}
    if(!found)throw new IllegalStateException("No safe mesh anchor in section");
   }
   var instances=new ArrayList<TartarusMeshBlockEntity.Instance>();for(var p:e.getValue())instances.add(new TartarusMeshBlockEntity.Instance(p.model(),p.x()-ax,p.y()-ay,p.z()-az));
   boolean solid=cell!=null&&!cell.material().equals("scenic_light");int light=cell!=null&&Set.of("scenic_light","scenic_window").contains(cell.material())?15:0;
   used.add(ax+","+ay+","+az);
   result.add(new Anchor(ax,ay,az,solid,light,new TartarusMeshBlockEntity.Spec(List.copyOf(instances),section.x()-ax,section.y()-ay,section.z()-az,true),emission));
   }
  }
  return List.copyOf(result);
 }
 public static void place(ServerLevel level,int floor,Anchor anchor){
  var at=new BlockPos(origin(floor)+anchor.x(),64+anchor.y(),anchor.z());
  var state=DarkHourRegistry.ARCHITECTURE.get(anchor.emissive()?"emissive_mesh":"mesh").get().m_49966_().m_61124_(TartarusMeshBlock.SOLID,anchor.solid()).m_61124_(TartarusMeshBlock.LIGHT,anchor.light());
  level.m_7731_(at,state,2);if(level.m_7702_(at) instanceof TartarusMeshBlockEntity entity)entity.configure(anchor.spec());
 }
 public static void gate(ServerLevel level,int floor,int x){
  int section=x<0?-16:0;
  var spec=new TartarusMeshBlockEntity.Spec(List.of(new TartarusMeshBlockEntity.Instance("kit_gate",-x,-1,0)),section-x,-1,-13,true);
  place(level,floor,new Anchor(x,1,GATE_Z,true,7,spec));
  int ex=x<0?-2:1;var emission=new TartarusMeshBlockEntity.Spec(List.of(new TartarusMeshBlockEntity.Instance("kit_gate",-ex,-1,0)),section-ex,-1,-13,true);
  place(level,floor,new Anchor(ex,1,GATE_Z,true,15,emission,true));
 }
 private TartarusMeshPlacement(){}
}
