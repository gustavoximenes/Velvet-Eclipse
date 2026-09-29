package com.gx.velveteclipse.forge;
import com.gx.velveteclipse.core.battle.TerrainFormation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;

/** Physical floor/ceiling/edge checks include the visible model's clearance. Never edits blocks. */
final class BattleTerrain {
 static TerrainFormation.Layout plan(Object player,int count,boolean boss){
  if(!(player instanceof Entity p))return null;
  double radius=boss?1.5:.8,height=boss?4:2.5;
  return TerrainFormation.solve(new TerrainFormation.Point(p.m_20185_(),p.m_20186_(),p.m_20189_()),p.m_146908_(),count,boss?3.2:1.95,
   (x,z,y)->ground(p,x,z,y,radius,height),(a,b)->visible(p,a,b));
 }
 static boolean visible(Entity p,TerrainFormation.Point a,TerrainFormation.Point b){
  var from=new Vec3(a.x(),a.y()+1.3,a.z());var to=new Vec3(b.x(),b.y()+1.25,b.z());
  return p.m_9236_().m_45547_(new ClipContext(from,to,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p)).m_6662_()==HitResult.Type.MISS;
 }
 static TerrainFormation.Point ground(Entity p,double x,double z,double nearY,double radius,double height){
  var world=p.m_9236_();var from=new Vec3(x,nearY+2.2,z);var to=new Vec3(x,nearY-2.2,z);
  var hit=world.m_45547_(new ClipContext(from,to,ClipContext.Block.COLLIDER,ClipContext.Fluid.ANY,p));
  if(hit.m_6662_()==HitResult.Type.MISS)return null;
  double y=hit.m_82450_().f_82480_+.015;
  if(!world.m_45756_(p,new AABB(x-radius,y,z-radius,x+radius,y+height,z+radius)))return null;
  for(double dx:new double[]{-radius*.8,0,radius*.8})for(double dz:new double[]{-radius*.8,0,radius*.8}){
   var foot=new Vec3(x+dx,y+.08,z+dz);var floor=world.m_45547_(new ClipContext(foot,foot.m_82520_(0,-.3,0),ClipContext.Block.COLLIDER,ClipContext.Fluid.ANY,p));
   if(floor.m_6662_()==HitResult.Type.MISS)return null;
   var state=world.m_8055_(BlockPos.m_274561_(x+dx,y-.05,z+dz));
   if(!state.m_60819_().m_76178_())return null;
   var id=net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(state.m_60734_());
   if(id!=null&&java.util.Set.of("magma_block","cactus","fire","soul_fire","campfire","soul_campfire","sweet_berry_bush","powder_snow").contains(id.m_135815_()))return null;
  }
  return new TerrainFormation.Point(x,y,z);
 }
 static boolean valid(Entity actor){
  boolean boss=actor.m_7755_().getString().startsWith("Guardião");double radius=boss?1.5:.8;
  var floor=ground(actor,actor.m_20185_(),actor.m_20189_(),actor.m_20186_(),radius,boss?4:2.5);
  return floor!=null&&Math.abs(floor.y()-actor.m_20186_())<.12;
 }
 private BattleTerrain(){}
}
