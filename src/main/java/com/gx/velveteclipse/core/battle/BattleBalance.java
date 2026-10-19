package com.gx.velveteclipse.core.battle;

import java.util.*;

/** Keeps unscaled HP and fractional rounding debt, so joining never resets battle progress. */
final class BattleBalance {
    private static final class Health {
        final int base;
        double ratio;
        int lastHp,lastMax;
        Health(BattleCombatant c){base=c.maxHp();lastHp=c.hp();lastMax=c.maxHp();ratio=(double)c.hp()/c.maxHp();}
        void resize(BattleCombatant c,int maximum){
            if(c.hp()!=lastHp)ratio=Math.max(0,Math.min(1,ratio+(double)(c.hp()-lastHp)/lastMax));
            if(!c.alive())ratio=0;
            c.resizeEnemyHealth(maximum,c.alive()?Math.max(1,(int)Math.round(ratio*maximum)):0);
            lastHp=c.hp();lastMax=c.maxHp();
        }
    }
    final BattleDifficulty difficulty;
    private final Map<UUID,Health> health=new HashMap<>();
    BattleBalance(BattleDifficulty difficulty,List<BattleCombatant> roster){
        this.difficulty=Objects.requireNonNull(difficulty);
        for(var c:roster)if(c.side()==BattleSide.ENEMIES)health.put(c.id(),new Health(c));
        update(roster);
    }
    void update(List<BattleCombatant> roster){
        // A KO is not a departure: count downed/unconscious players until they actually leave.
        int size=(int)roster.stream().filter(c->c.side()==BattleSide.PLAYERS).count();
        for(var c:roster)if(c.side()==BattleSide.ENEMIES){
            var h=health.get(c.id());
            h.resize(c,Math.max(1,(int)Math.min(Integer.MAX_VALUE,Math.round(h.base*difficulty.enemyHealth(size)))));
            c.encounterDamage(difficulty.enemyDamage(size),difficulty==BattleDifficulty.RELAXED);
        }
    }
}
