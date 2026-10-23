package com.gx.velveteclipse.tests;
import com.gx.velveteclipse.city.*;
import net.minecraft.world.phys.*;
import java.util.*;

/** Exercise arrival search against real exported collision, including blocked and fractional exits. */
public final class CityFunctionAudit {
 static int checks;static void check(boolean b,String why){checks++;if(!b)throw new AssertionError(why);}
 static AABB box(double[] b,int x,int y,int z){return new AABB(b[0]+x,b[1]+y,b[2]+z,b[3]+x,b[4]+y,b[5]+z);}
 public static void main(String[] args){
  var cells=new HashMap<String,int[]>();for(var c:CityPlan.get().cells())cells.put(c[0]+","+c[1]+","+c[2],c);
  for(String id:List.of("velvet_exit","gekkoukan","bedroom")){
   var p=CityPlan.place(id);var solids=new ArrayList<AABB>();
   for(var c:CityPlan.get().cells())if(Math.abs(c[0]-p.x())<6&&Math.abs(c[2]-p.z())<6&&Math.abs(c[1]-p.y())<6){
    if(c[3]==40)for(var b:CityArchitecture.tile(c[4]).boxes())solids.add(box(b,c[0],c[1],c[2]));
    else if(c[3]<20)solids.add(new AABB(c[0],c[1],c[2],c[0]+1,c[1]+(c.length>5?c[5]/16.:1),c[2]+1));
   }
   var at=CityArrival.find(p.x()+.5,p.y(),p.z()+.5,3,b->solids.stream().noneMatch(b::m_82381_));check(at!=null,"Real arrival "+id);
  }
  var floor=new AABB(-8,-1,-8,8,.375,8);var found=CityArrival.find(.5,0,.5,3,b->!b.m_82381_(floor));check(found!=null&&Math.abs(found.f_82480_-.378)<.0001,"Fractional floor height");
  check(CityArrival.find(0,0,0,3,b->true)==null,"Never teleport into an unloaded/empty void");
  check(CityArrival.find(0,0,0,3,b->false)==null,"No destination if completely obstructed");
  var obstacles=List.of(new AABB(-8,-1,-8,8,0,8),new AABB(0,0,0,1,3,1));found=CityArrival.find(.5,0,.5,3,b->obstacles.stream().noneMatch(b::m_82381_));check(found!=null&&(found.f_82479_!=.5||found.f_82481_!=.5),"Find nearby free space instead of rejecting the entire city");
  var f=CityFurniture.catalogue();check(f.seats().size()==173&&f.doors().size()==34,"All authored seat and door anchors, including school");var ids=new HashSet<String>();
  for(var s:f.seats()){check(ids.add(s.id()),"Unique seat");var a=s.anchor();check(cells.get(a[0]+","+a[1]+","+a[2])[4]==s.tile(),"Seat anchored to its real tile");check(s.y()<s.bounds()[4]&&s.y()>=s.bounds()[1],"Seat within furniture");}
  var touched=new HashSet<String>();
  for(var d:f.doors()){
   check(ids.add(d.id()),"Unique door");boolean differs=false;
   for(var c:d.cells()){
    check(touched.add(c[0]+","+c[1]+","+c[2]),"Independent doors cannot overwrite the same tile");
    for(int mode:new int[]{4,5}){var tile=CityArchitecture.tile(c[mode]);check(tile!=null,"Variant exists");for(var b:tile.boxes())check(b[0]>=0&&b[1]>=0&&b[2]>=0&&b[3]<=1&&b[4]<=1&&b[5]<=1,"Local door collision");}
    differs|=!Arrays.deepEquals(CityArchitecture.tile(c[4]).boxes(),CityArchitecture.tile(c[5]).boxes());
   }
   check(differs,"Door collision follows the moving leaf");
  }
  var front=f.doors().stream().filter(d->d.id().equals("dorm_entrance")).findFirst().orElseThrow();
  var passage=new AABB(-100.3,65.2,77.3,-99.7,67,78.1);
  boolean openBlocked=false,closedBlocked=false;
  for(var c:front.cells()){
   for(var b:CityArchitecture.tile(c[4]).boxes())openBlocked|=box(b,c[0],c[1],c[2]).m_82381_(passage);
   for(var b:CityArchitecture.tile(c[5]).boxes())closedBlocked|=box(b,c[0],c[1],c[2]).m_82381_(passage);
  }
  check(!openBlocked&&closedBlocked,"Entrance opens the passage and closing restores the actual leaf collision");
  var nurse=f.doors().stream().filter(d->d.id().equals("school_enfermaria")).findFirst().orElseThrow();
  check(CityFurniture.doorLeafHit(nurse,false,new net.minecraft.core.BlockPos(22,65,-117),new Vec3(22.5,65.4,-116.10)),"Closed school leaf can be clicked");
  check(CityFurniture.doorLeafHit(nurse,true,new net.minecraft.core.BlockPos(21,65,-116),new Vec3(21.565,65.4,-115.2)),"Opened school leaf can be clicked at rotated position");
  check(!CityFurniture.doorLeafHit(nurse,false,new net.minecraft.core.BlockPos(21,65,-117),new Vec3(21.2,65.4,-116.1)),"Wall beside closed leaf is not a door handle despite sharing its block");
  check(!CityFurniture.doorLeafHit(nurse,true,new net.minecraft.core.BlockPos(22,65,-117),new Vec3(22.5,65.4,-116.10)),"Empty old leaf position does not toggle an open door");
  System.out.println("PASS city functions: "+checks+" checks; "+f.seats().size()+" seats, "+f.doors().size()+" doors, actual city return floors and blocked-arrival cases");
 }
}
