package com.gx.velveteclipse.room;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

/** Door leaf collision in block units, including both hinge choices. */
public final class VelvetDoorGeometry {
 private VelvetDoorGeometry(){}
 public static AABB bounds(Direction facing,boolean open,boolean left){
  Direction side=facing;
  if(open)side=left?switch(facing){case NORTH->Direction.WEST;case WEST->Direction.SOUTH;case SOUTH->Direction.EAST;default->Direction.NORTH;}:switch(facing){case NORTH->Direction.EAST;case EAST->Direction.SOUTH;case SOUTH->Direction.WEST;default->Direction.NORTH;};
  return switch(side){case NORTH->new AABB(0,0,0,1,1,3/16.);case SOUTH->new AABB(0,0,13/16.,1,1,1);case WEST->new AABB(0,0,0,3/16.,1,1);default->new AABB(13/16.,0,0,1,1,1);};
 }
}
