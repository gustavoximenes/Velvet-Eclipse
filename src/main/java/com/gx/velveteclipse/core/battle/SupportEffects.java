package com.gx.velveteclipse.core.battle;

/** Persona 4 support skills. Statuses belong to the combatant, not the equipped Persona. */
public final class SupportEffects {
    private SupportEffects() {}
    public static boolean supported(String id) {
        return id.equals("rakukaja") || id.equals("rakunda") || id.equals("tarukaja") || id.equals("tarunda") || id.equals("sukunda");
    }
    public static String apply(String id, BattleCombatant actor, BattleCombatant target) {
        boolean waitForTargetTurn = actor != target;
        return switch(id) {
            case "rakukaja" -> {target.defenseStatus(1,waitForTargetTurn);yield "Defesa aumentada por 3 turnos.";}
            case "rakunda" -> {target.defenseStatus(-1,waitForTargetTurn);yield "Defesa reduzida por 3 turnos.";}
            case "tarukaja" -> {target.attackStatus(waitForTargetTurn);yield "Ataque aumentado por 3 turnos.";}
            case "tarunda" -> {target.attackStatus(-1,waitForTargetTurn);yield "Ataque reduzido por 3 turnos.";}
            case "sukunda" -> {target.accuracyStatus(-1,waitForTargetTurn);yield "Precisão e evasão reduzidas por 3 turnos.";}
            default -> throw new IllegalArgumentException("Unsupported effect: " + id);
        };
    }
}
