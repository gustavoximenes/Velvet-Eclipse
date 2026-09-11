package com.gx.velveteclipse.tests;
import com.gx.velveteclipse.core.battle.*;
import com.gx.velveteclipse.core.persona.*;
import java.util.*;

/** Identical seeded actions execute against both 0.5.30 and the recovered sources. */
public final class CombatScenario {
    public static String run(){
        PersonaRegistry.bootstrap();StringBuilder out=new StringBuilder();
        String[] personas={"cinder_wisp","nocturne_knight","sylphid","lumen_hare","umbra_slime"};
        String[] skills={"ember","frost_shard","volt_spark","gale_cut","cleave","lumen_ray","shade_bite"};
        for(int seed=0;seed<120;seed++){
            var a=new BattleCombatant(new UUID(0,1),"Player",BattleSide.PLAYERS,new PersonaInstance(PersonaRegistry.persona(personas[seed%5]).orElseThrow()),350,350,200,200);
            var b=new BattleCombatant(new UUID(0,2),"Shadow",BattleSide.ENEMIES,new PersonaInstance(PersonaRegistry.persona(personas[(seed+1)%5]).orElseThrow()),350,350,200,200);
            var battle=new BattleSession(List.of(a,b),new Random(seed));
            for(int step=0;step<80&&battle.currentActor()!=null;step++){
                var actor=battle.currentActor();var enemies=battle.enemiesOf(actor);if(enemies.isEmpty())break;
                UUID target=enemies.get(0).id();
                BattleAction action=switch(step%9){case 0->BattleAction.guard();case 1->BattleAction.item("pulse_tonic");case 2->BattleAction.attack(target);default->BattleAction.skill(skills[(seed+step)%skills.length],target);};
                var result=battle.perform(actor.id(),action);
                // Preserve the 0.5.30 golden format; the cosmetic flag is tested separately.
                out.append("BattleResolution[state=").append(result.state()).append(", oneMore=").append(result.oneMore()).append(", log=").append(result.log()).append(']').append('|').append(a.hp()).append('/').append(a.sp()).append('/').append(a.downed()).append('|').append(b.hp()).append('/').append(b.sp()).append('/').append(b.downed()).append('\n');
            }
        }
        return out.toString();
    }
    public static void main(String[] args)throws Exception{System.out.println(java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(run().getBytes(java.nio.charset.StandardCharsets.UTF_8))));}
}
