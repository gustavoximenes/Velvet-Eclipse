package com.gx.velveteclipse.client.exploration;
import com.gx.velveteclipse.equipment.EquipmentCatalog.Family;
import dev.kosmx.playerAnim.core.util.Vec3f;
/** Category-specific keyed poses share the existing server impact window, ticks 4..7. */
public final class WeaponPose {
 private static Vec3f v(double x,double y,double z){return new Vec3f((float)x,(float)y,(float)z);}
 private static double k(double t,double... n){return ExplorationPose.curve(n,t);}
 public static Vec3f rotation(Family f,String part,double phase,double speed,double t){
  var base=ExplorationPose.rotation(part,phase,speed,t);if(f==Family.NONE||f==Family.SWORD||f==Family.KNIFE)return base;
  boolean right=part.equals("rightArm"),left=part.equals("leftArm");if(!right&&!left)return base;
  double b=ExplorationPose.attackBlend(t),x,y,z;
  switch(f){
   case BOW -> {x=right?-.4:-.4;y=right?-.4:.2;z=right?.2:-.2;x+=b*((right?-1.55:-1.45)-x);y+=b*((right?-1.05:.1)-y);z+=b*((right?.25:-.08)-z);}
   case GUN -> {x=-1.15+b*(-.20);y=right?-.15:.45;z=right?.1:-.25;}
   case FISTS -> {x=right?-.8:-1.05;y=right?-.18:.25;z=right?.22:-.18;if(right){x+=b*(k(t,-.8,-1.2,-1.65,-1.5,-.9,-.8,-.8)-x);y+=b*k(t,0,-.45,.05,.3,.2,0,0);}}
   case RAPIER -> {x=right?-.45:-.15;y=right?-.45:.1;z=right?.12:-.2;if(right){x+=b*(k(t,-.45,-1.05,-1.55,-1.5,-.5,-.45,-.45)-x);y+=b*.35;}else x+=b*.5;}
   case SPEAR -> {x=right?-.55:-1;y=right?-.45:.45;z=right?.13:-.15;x+=b*(k(t,-.55,-1.7,-1.9,-1.4,-.7,-.55,-.55)-x);y+=b*(right?.5:-.3);}
   default -> {x=right?-.6:-.7;y=right?-.3:.45;z=right?.16:-.2;x+=b*(k(t,-.6,-2.5,-1.7,-.8,-.4,-.6,-.6)-x);y+=b*(right?.3:-.3);}
  }return v(x,y,z);
 }
 public static Vec3f bend(Family f,String part,double phase,double speed,double t){if(!part.endsWith("Arm")||f==Family.SWORD||f==Family.KNIFE||f==Family.NONE)return ExplorationPose.bend(part,phase,speed,t);double b=ExplorationPose.attackBlend(t);return v(0,f==Family.BOW&&part.equals("rightArm")?-1.1*b:f==Family.FISTS?-.9*(1-b):-.35*(1-b),0);}
}
