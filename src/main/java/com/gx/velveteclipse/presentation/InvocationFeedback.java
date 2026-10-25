package com.gx.velveteclipse.presentation;

import java.util.List;

/** Server invocation markers, using the Persona captured when the action was armed. */
public final class InvocationFeedback {
    public record Cue(String kind, String key) {}

    public static List<Cue> at(int tick, String persona) {
        return switch (tick) {
            case 38 -> persona == null || persona.isBlank()
                    ? List.of(new Cue("audio", "summon_evoker"))
                    : List.of(new Cue("audio", "summon_evoker"), new Cue("voice", persona));
            case 46 -> List.of(new Cue("audio", "summon_summon"));
            case 54 -> List.of(new Cue("audio", "summon_persona_aura"));
            default -> List.of();
        };
    }

    private InvocationFeedback() {}
}
