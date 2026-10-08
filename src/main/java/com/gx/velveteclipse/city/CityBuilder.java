package com.gx.velveteclipse.city;
import java.util.*;import net.minecraft.core.BlockPos;import net.minecraft.server.level.ServerLevel;
import com.gx.velveteclipse.darkhour.*;
/** Bounded, restartable construction in an isolated city dimension. */
public final class CityBuilder {
 private static final String[] MATERIALS={"paving","brick","plaster","wood","glass","school_floor","leaves","roof","light"};
 private record Anchor(CityPlan.Mesh mesh,int x,int y,int z){}
 private static List<Anchor> anchors;
 public static int progress(CityState state){return state.choice!=1?0:state.ready?100:Math.min(99,(int)(100L*state.cursor/(CityPlan.get().cells().length+anchors().size())));}
 private static List<Anchor> anchors(){if(anchors==null){var a=new ArrayList<Anchor>();for(var m:CityPlan.get().meshes())for(int x=m.minX();x<=m.maxX();x+=16)for(int y=m.minY();y<=m.maxY();y+=16)for(int z=m.minZ();z<=m.maxZ();z+=16)a.add(new Anchor(m,x,y,z));anchors=List.copyOf(a);}return anchors;}
 public static boolean step(ServerLevel level,CityState state){var plan=CityPlan.get();int n=plan.cells().length;int limit=state.cursor+1200;
  while(state.cursor<n&&state.cursor<limit){int[] c=plan.cells()[state.cursor++];var pos=new BlockPos(c[0],c[1],c[2]);
   if(c[3]==30){com.gx.velveteclipse.room.VelvetRoomService.placeDoor(level,pos,net.minecraft.core.Direction.EAST);continue;}if(c[3]==31)continue;
   var block=c[3]==29?CityRegistry.BLOCKS.get("tartarus_access").get():c[3]>=20?CityRegistry.BLOCKS.get("shop_"+CityCatalog.SHOPS.get(c[3]-20)).get():CityRegistry.BLOCKS.get(MATERIALS[Math.max(0,Math.min(8,c[3]))]).get();var bs=block.m_49966_();if(c.length>4&&c[4]==1&&block instanceof TartarusScenicBlock)bs=bs.m_61124_(TartarusScenicBlock.SCENIC,true);if(c.length>5&&block instanceof CitySurfaceBlock)bs=bs.m_61124_(CitySurfaceBlock.HEIGHT,c[5]);level.m_7731_(pos,bs,2);}
  int budget=8;var aa=anchors();while(state.cursor>=n&&state.cursor<n+aa.size()&&budget-->0){var a=aa.get(state.cursor-n);var m=a.mesh;BlockPos pos=null;
   search:for(int dy=15;dy>=0;dy--)for(int dz=0;dz<16;dz++)for(int dx=0;dx<16;dx++){var candidate=new BlockPos(a.x+dx,a.y+dy,a.z+dz);var old=level.m_8055_(candidate);if(old.m_60795_()||old.m_60734_() instanceof TartarusMeshBlock){pos=candidate;break search;}}
   if(pos==null)throw new IllegalStateException("City section has no free geometry anchor: "+m.name());
   var bs=DarkHourRegistry.ARCHITECTURE.get("mesh").get().m_49966_().m_61124_(TartarusMeshBlock.SOLID,false).m_61124_(TartarusMeshBlock.LIGHT,0);level.m_7731_(pos,bs,2);if(level.m_7702_(pos) instanceof TartarusMeshBlockEntity be)be.configure(new TartarusMeshBlockEntity.Spec(List.of(new TartarusMeshBlockEntity.Instance(m.name(),m.x()-pos.m_123341_(),m.y()-pos.m_123342_(),m.z()-pos.m_123343_())),a.x-pos.m_123341_(),a.y-pos.m_123342_(),a.z-pos.m_123343_(),true));state.cursor++;
  }
  state.m_77762_();return state.cursor>=n+aa.size();
 }
}
