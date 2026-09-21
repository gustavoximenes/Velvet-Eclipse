package com.gx.velveteclipse.shadow;
import com.gx.velveteclipse.core.persona.*;import java.util.*;
/** Explicit ordinary-enemy allowlist: bosses, raid actors and player-only Personas cannot leak in. */
public final class ShadowEncounterRules {
 public static final List<String> ORDINARY=EnemyCatalog.all().stream().filter(e->!e.guardian()).map(EnemyDefinition::id).toList();
 public record Enemy(String persona,int level,int hp,int sp,Set<String> skills){}
 public static boolean rare(long seed){return new Random(seed^0x74E9D3B518L).nextInt(100)<5;}
 public static List<Enemy> roll(long seed,int floor,boolean rare){
  var r=new Random(seed);var pool=EnemyCatalog.pool(floor);int total=pool.stream().mapToInt(EnemyDefinition::weight).sum();var out=new ArrayList<Enemy>();
  for(int i=0,count=1+r.nextInt(5);i<count;i++){
   int pick=r.nextInt(total);EnemyDefinition def=pool.get(0);for(var candidate:pool){pick-=candidate.weight();if(pick<0){def=candidate;break;}}
   int growth=Math.max(0,floor-25),level=Math.min(99,def.level()+growth/2+(rare?8:0));
   out.add(new Enemy(def.id(),level,(int)((def.hp()+growth*7)*(rare?2.2:1)),def.sp()+growth*2+(rare?25:0),def.skills()));
  }return List.copyOf(out);
 }
 public static int xp(int floor,int enemies,boolean rare){return (80+Math.max(0,floor)*4)*enemies*(rare?3:1);}
 public static long yen(int floor,int enemies,boolean rare){return (60L+Math.max(0,floor)*25L)*enemies*(rare?4:1);}
}
