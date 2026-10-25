package com.gx.velveteclipse.presentation;
import com.gx.velveteclipse.core.battle.BattleSession;
/** A delayed result belongs to one reservation in one encounter, and can be claimed only once. */
public final class PersonaImpactGate {
    private final BattleSession session;
    private final BattleSession.Prepared reservation;
    private final PersonaActionTiming.Plan plan;
    private boolean claimed;
    public PersonaImpactGate(BattleSession session, PersonaActionTiming.Plan plan) {
        this.session = java.util.Objects.requireNonNull(session);
        this.reservation = java.util.Objects.requireNonNull(session.prepared());
        this.plan = java.util.Objects.requireNonNull(plan);
    }
    public boolean valid(BattleSession current) { return current == session && (claimed || current.prepared() == reservation); }
    public boolean claim(BattleSession current, int tick) {
        if (claimed || !valid(current) || tick < plan.impact()) return false;
        claimed = true; return true;
    }
    public void cancel() { if (session.prepared() == reservation) session.cancelPrepared(); claimed = true; }
}
