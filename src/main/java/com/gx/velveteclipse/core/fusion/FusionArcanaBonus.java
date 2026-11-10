package com.gx.velveteclipse.core.fusion;

import com.gx.velveteclipse.core.persona.*;
import com.gx.velveteclipse.forge.PlayerPersonaData;
import com.gx.velveteclipse.roguelike.ArcanaUpCatalog;

/** Called only on detached fusion results. Previewing never changes a roster or rank. */
public final class FusionArcanaBonus {
    public static int apply(PlayerPersonaData recipient, PersonaInstance result) {
        var definition = PersonaRegistry.persona(result.definitionId()).orElseThrow();
        int rank = recipient.fusionRank(definition.arcana());
        if(rank == 0 || result.level() >= 99)return 0;
        int next = PersonaInstance.experienceForNextLevel(result.level());
        int xp = (next * rank + 9) / 10;
        if(result.level() == 98)xp = Math.min(xp, Math.max(0, next - result.experience()));
        result.addExperience(definition, xp);
        if(result.level() >= 99)result.setLevel(definition, 99);
        return xp;
    }
    public static String describe(PlayerPersonaData recipient, PersonaInstance result, int xp) {
        if(xp <= 0)return "";
        var arcana = PersonaRegistry.persona(result.definitionId()).orElseThrow().arcana();
        return " Arcana Up: " + ArcanaUpCatalog.of(arcana).name() + " / Rank " + recipient.fusionRank(arcana) + " / +" + xp + " EXP.";
    }
    private FusionArcanaBonus() {}
}
