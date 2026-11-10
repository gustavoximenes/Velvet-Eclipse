package com.gx.velveteclipse.forge;

import com.gx.velveteclipse.core.battle.BattleCombatant;
import com.gx.velveteclipse.core.persona.PersonaInstance;

/** A finished encounter keeps membership, but its combatant predates the XP rewards. */
public record PlayerBattleView(int hp, int maxHp, int sp, int maxSp, PersonaInstance persona) {
    public static PlayerBattleView of(PlayerPersonaData saved, BattleCombatant actor, boolean resultsStarted) {
        if (actor == null || resultsStarted)
            return new PlayerBattleView(saved.hp(), saved.maxHp(), saved.sp(), saved.maxSp(), saved.activeInstance());
        return new PlayerBattleView(actor.hp(), actor.maxHp(), actor.sp(), actor.maxSp(), actor.persona());
    }
}
