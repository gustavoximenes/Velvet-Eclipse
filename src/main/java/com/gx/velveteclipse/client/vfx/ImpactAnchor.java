package com.gx.velveteclipse.client.vfx;
import com.gx.velveteclipse.network.BattleLightningS2C.Strike;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.Minecraft;

/** Keeps the last visible feet when a killing hit removes the actor; never substitutes another target. */
public final class ImpactAnchor {
 private final Strike target;private Vec3 last;
 public ImpactAnchor(Strike target){this.target=target;last=new Vec3(target.x(),target.y(),target.z());}
 public Vec3 position(float partial){
  var world=Minecraft.m_91087_().f_91073_;var e=world==null||target.entityId()<0?null:world.m_6815_(target.entityId());
  return update(e==null?null:e.m_20148_(),e==null?null:e.m_20318_(partial));
 }
 /** Also used by the offline audit to exercise entity removal and recycled numeric IDs. */
 public Vec3 update(java.util.UUID identity,Vec3 position){if(position!=null&&target.entityUuid()!=null&&target.entityUuid().equals(identity))last=position;return last;}
}
