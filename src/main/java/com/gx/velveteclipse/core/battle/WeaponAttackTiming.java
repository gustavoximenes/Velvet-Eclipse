package com.gx.velveteclipse.core.battle;
import com.gx.velveteclipse.equipment.EquipmentCatalog.Family;

/** Server impacts and both client presentation paths share the approved clip clock. */
public final class WeaponAttackTiming {
 public static double seconds(Family f){return switch(f){case BOW->.96;case GREATSWORD->.92;case FISTS,GUN->.70;case RAPIER->.68;case KNIFE->.62;case SPEAR->.80;case AXE->.98;default->.696;};}
 public static double authoredImpact(Family f){return switch(f){case BOW->.52;case GREATSWORD->.55;case FISTS,KNIFE,AXE->.58;case GUN,RAPIER,SPEAR->.48;default->.46;};}
 public static double impactTick(Family f){return authoredImpact(f)/1.16*seconds(f)*20;}
 public static int end(Family f){return (int)Math.ceil(seconds(f)*20);}
 public static boolean hitWindow(Family f,int tick){int at=(int)Math.round(impactTick(f));return tick>=at&&tick<=at+2;}
 public static double authored(Family f,double elapsed){return elapsed<0?-1:Math.min(1.16,elapsed/(seconds(f)*20)*1.16);}
 public static double battleStart(Family f){return (f==Family.BOW?16:MeleeTimeline.IMPACT)-impactTick(f);}
 public static double battleElapsed(Family f,double tick){double age=tick-battleStart(f);return age<0||age>seconds(f)*20?-1:age;}
 public static boolean ranged(Family f){return f==Family.BOW||f==Family.GUN;}
 public static double advance(Family f,double tick){
  double t=authored(f,Math.max(0,tick));if(ranged(f))return 0;
  if(f==Family.SWORD||f==Family.NONE)return .9*smooth((t-.14)/.58);
  double[] time={0,.18,.34,.48,.58,.72,.92,1.16};
  double[] v=switch(f){case RAPIER,SPEAR->new double[]{0,-.08,.17,.56,.57,.50,.46,.46};case FISTS->new double[]{0,0,.20,.24,.41,.42,.40,.40};default->new double[]{0,-.04,.19,.35,.46,.46,.46,.46};};
  for(int i=0;i<7;i++)if(t<=time[i+1])return v[i]+(v[i+1]-v[i])*smooth((t-time[i])/(time[i+1]-time[i]));return v[7];
 }
 private static double smooth(double t){t=Math.max(0,Math.min(1,t));return t*t*(3-2*t);}
 private WeaponAttackTiming(){}
}
