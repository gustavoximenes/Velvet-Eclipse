package com.gx.velveteclipse.shadow;
import com.gx.velveteclipse.core.persona.*;import java.util.*;
/** Explicit ordinary-enemy allowlist: bosses, raid actors and player-only Personas cannot leak in. */
public final class ShadowEncounterRules {
 public static final List<String> ORDINARY=List.of("cinder_wisp","umbra_slime","sylphid","nocturne_knight","lumen_hare","iron_oni");
 public record Enemy(String persona,int level,int hp,int sp,Set<String> skills){}
 public static boolean rare(long seed){return new Random(seed^0x74E9D3B518L).nextInt(100)<5;}
 public static List<Enemy> roll(long seed,int floor,boolean rare){
  var r=new Random(seed);int base=Math.max(2,floor+1);var pool=ORDINARY.stream().map(id->PersonaRegistry.persona(id).orElseThrow()).filter(p->p.baseLevel()<=base).toList();
  var out=new ArrayList<Enemy>();for(int i=0,count=1+r.nextInt(5);i<count;i++){var def=pool.get(r.nextInt(pool.size()));int level=Math.max(def.baseLevel(),base+r.nextInt(5)-2)+(rare?8:0);var skills=new LinkedHashSet<String>();def.learnset().forEach((l,s)->{if(l<=level)skills.addAll(s);});if(skills.stream().map(id->PersonaRegistry.skill(id).orElseThrow()).noneMatch(s->s.isDamageSkill()&&s.usesMagicStat()))skills.add("frost_shard");out.add(new Enemy(def.id(),level,(int)((65+level*7)*(rare?2.2:1)),30+level*3,Set.copyOf(skills)));}return List.copyOf(out);
 }
 public static int xp(int floor,int enemies,boolean rare){return (80+Math.max(0,floor)*4)*enemies*(rare?3:1);}
 public static long yen(int floor,int enemies,boolean rare){return (60L+Math.max(0,floor)*25L)*enemies*(rare?4:1);}
}
