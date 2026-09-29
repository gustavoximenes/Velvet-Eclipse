package com.gx.velveteclipse.exploration;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import java.util.function.Predicate;

/** Shared straight-flight solution: the preview and authoritative shot use identical geometry. */
public final class BowAim {
 public static final double RANGE=10;
 public record Trace(Vec3 start,Vec3 end,Entity target){}
 public static Vec3 direction(float yaw,float pitch){double y=Math.toRadians(yaw),p=Math.toRadians(pitch);return new Vec3(-Math.sin(y)*Math.cos(p),-Math.sin(p),Math.cos(y)*Math.cos(p));}
 public static Trace trace(Entity shooter,Predicate<Entity> eligible){
  var from=shooter.m_146892_().m_82520_(0,-.18,0);var to=from.m_82549_(direction(shooter.m_146908_(),shooter.m_146909_()).m_82490_(RANGE));var world=shooter.m_9236_();
  var block=world.m_45547_(new ClipContext(from,to,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,shooter));
  if(block.m_6662_()!=HitResult.Type.MISS)to=block.m_82450_();
  Entity target=null;double nearest=from.m_82554_(to);
  for(var e:world.m_6443_(Entity.class,shooter.m_20191_().m_82400_(RANGE),e->e!=shooter&&e.m_6084_()&&eligible.test(e))){
   var hit=e.m_20191_().m_82400_(.12).m_82371_(from,to);if(hit.isEmpty())continue;double distance=hit.get().m_82554_(from);if(distance<nearest){nearest=distance;target=e;}
  }
  return new Trace(from,from.m_82549_(to.m_82546_(from).m_82541_().m_82490_(nearest)),target);
 }
 private BowAim(){}
}
