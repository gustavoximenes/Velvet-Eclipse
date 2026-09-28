package com.gx.velveteclipse.forge;
import com.gx.velveteclipse.core.battle.*;
import com.gx.velveteclipse.util.RuntimeBridge;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/** One reversible melee route, owned by the encounter's reserved action. */
final class EnemyBattleMotion {
 final Entity actor;final Vec3 start,end;final float yaw,attackYaw;final int began;
 EnemyBattleMotion(Entity actor,Entity target,int began){
  this.actor=actor;this.began=began;start=actor.m_20182_();yaw=actor.m_146908_();
  Vec3 delta=target.m_20182_().m_82546_(start);double distance=Math.sqrt(delta.f_82479_*delta.f_82479_+delta.f_82481_*delta.f_82481_);
  Vec3 direction=distance<.001?Vec3.f_82478_:new Vec3(delta.f_82479_/distance,0,delta.f_82481_/distance);
  boolean supported=!actor.m_9236_().m_45756_(actor,actor.m_20191_().m_82386_(0,-.65,0));
  double reach=MeleeApproach.reachable(Math.max(0,distance-1.15),d->{
   var box=actor.m_20191_().m_82383_(direction.m_82490_(d));
   return actor.m_9236_().m_45756_(actor,box)&&(!supported||!actor.m_9236_().m_45756_(actor,box.m_82386_(0,-.65,0)));
  });
  end=start.m_82549_(direction.m_82490_(reach));attackYaw=(float)Math.toDegrees(Math.atan2(-delta.f_82479_,delta.f_82481_));
 }
 void tick(int now){
  int age=now-began;Vec3 at=start.m_165921_(end,MeleeTimeline.progress(age));
  if(actor.m_9236_().m_45756_(actor,actor.m_20191_().m_82383_(at.m_82546_(actor.m_20182_()))))RuntimeBridge.setEntityPosition(actor,at.f_82479_,at.f_82480_,at.f_82481_);
  RuntimeBridge.setEntityRotation(actor,attackYaw,age>=9&&age<30?45:10);actor.m_20256_(Vec3.f_82478_);
 }
 void restore(){RuntimeBridge.setEntityPosition(actor,start.f_82479_,start.f_82480_,start.f_82481_);RuntimeBridge.setEntityRotation(actor,yaw,0);}
}
