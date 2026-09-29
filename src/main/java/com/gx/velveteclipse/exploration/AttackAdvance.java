package com.gx.velveteclipse.exploration;
import com.gx.velveteclipse.core.battle.WeaponAttackTiming;
import com.gx.velveteclipse.equipment.EquipmentCatalog.Family;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;

/** Fixed attack anchor prevents client prediction and server movement from adding twice. */
public final class AttackAdvance {
 public static void move(Entity player,Vec3 start,Family family,float yaw,double tick){
  if(start==null||WeaponAttackTiming.ranged(family))return;
  double advance=WeaponAttackTiming.advance(family,tick),angle=Math.toRadians(yaw);
  var desired=new Vec3(start.f_82479_-Math.sin(angle)*advance,player.m_20186_(),start.f_82481_+Math.cos(angle)*advance);
  var delta=desired.m_82546_(player.m_20182_());if(delta.m_82556_()>2)return;
  var world=player.m_9236_();
  if(!world.m_45756_(player,player.m_20191_().m_82383_(delta)))return;
  if(world.m_45547_(new ClipContext(desired.m_82520_(0,.1,0),desired.m_82520_(0,-.6,0),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,player)).m_6662_()==HitResult.Type.MISS)return;
  player.m_6478_(MoverType.SELF,delta);
 }
 private AttackAdvance(){}
}
