/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.EntityType
 */
package com.gx.velveteclipse.forge;

import com.gx.velveteclipse.VelvetEclipseMod;
import com.gx.velveteclipse.core.persona.PersonaRegistry;
import com.gx.velveteclipse.forge.BattleService;
import com.gx.velveteclipse.forge.PersonaDelayedActions;
import com.gx.velveteclipse.forge.PersonaMobRegistry;
import com.gx.velveteclipse.forge.PlayerPersonaData;
import com.gx.velveteclipse.forge.VisualBattleActors;
import com.gx.velveteclipse.util.RuntimeBridge;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.world.entity.EntityType;

public final class PersonaCinematicServer {
    private static final Map<UUID, Sequence> SEQUENCES = new LinkedHashMap<UUID, Sequence>();

    private PersonaCinematicServer() {
    }

    public static void armSkill(Object object, String string) {
        String string2;
        if (object == null || string == null || string.isBlank()) {
            return;
        }
        UUID uUID = RuntimeBridge.playerUuid(object);
        if (uUID == null) {
            return;
        }
        if (SEQUENCES.containsKey(uUID)) return;
        Sequence sequence = SEQUENCES.remove(uUID);
        if (sequence != null) {
            PersonaCinematicServer.clearSequence(uUID, sequence);
        }
        Sequence sequence2 = new Sequence(uUID, object, string, (string2 = PlayerPersonaData.load(object).activePersona()) == null || string2.isBlank() ? "cinder_wisp" : string2);
        sequence2.magic = PersonaCinematicServer.isMagicSkill(string);
        SEQUENCES.put(uUID, sequence2);
    }

    public static boolean interceptPulse(Object object, String string) {
        Sequence sequence;
        if (object == null) {
            return false;
        }
        UUID uUID = RuntimeBridge.playerUuid(object);
        Sequence sequence2 = sequence = uUID == null ? null : SEQUENCES.get(uUID);
        if (sequence == null) {
            return false;
        }
        if (string != null && !string.isBlank()) {
            sequence.personaId = string;
        }
        return true;
    }

    public static boolean suppressLegacySettle(Object object) {
        if (object == null) {
            return false;
        }
        UUID uUID = RuntimeBridge.playerUuid(object);
        return uUID != null && SEQUENCES.containsKey(uUID);
    }

    public static void clear(Object object) {
        if (object == null) {
            return;
        }
        UUID uUID = RuntimeBridge.playerUuid(object);
        if (uUID == null) {
            return;
        }
        Sequence sequence = SEQUENCES.remove(uUID);
        if (sequence != null) {
            PersonaCinematicServer.clearSequence(uUID, sequence);
        }
    }

    public static void tick() {
        for (Map.Entry<UUID, Sequence> entry : new ArrayList<Map.Entry<UUID, Sequence>>(SEQUENCES.entrySet())) {
            UUID uUID = entry.getKey();
            Sequence sequence = entry.getValue();
            if (SEQUENCES.get(uUID) != sequence) continue;
            try {
                double[] object;
                if (sequence.player == null || BattleService.current(sequence.player) == null) {
                    PersonaCinematicServer.finishSequence(uUID, sequence);
                    continue;
                }
                ++sequence.tick;
                sequence.base = RuntimeBridge.playerPosition(sequence.player);
                RuntimeBridge.hardFreezePlayer(sequence.player, sequence.base, sequence.yaw, 0.0f);
                if (VisualBattleActors.enemyActor(sequence.player) != null) {
                    VisualBattleActors.pulseEnemy(sequence.player);
                }
                if (sequence.tick < 46 && VisualBattleActors.personaActor(sequence.player) != null) {
                    RuntimeBridge.setEntityPosition(VisualBattleActors.personaActor(sequence.player), sequence.base[0], sequence.base[1] - 40.0, sequence.base[2]);
                }
                object = RuntimeBridge.localOffset(sequence.base, sequence.yaw, 0.0, 0.92, -1.4);
                double[] dArray = RuntimeBridge.localOffset(sequence.base, sequence.yaw, 0.0, 0.92, -1.4);
                double[] dArray2 = RuntimeBridge.localOffset(sequence.base, sequence.yaw, 0.0, 0.92, sequence.magic?-1.4:1.4);
                if (sequence.tick == 46) {
                    VisualBattleActors.replacePersona(sequence.player, sequence.personaId);
                    PersonaCinematicServer.setPersonaVisualState(sequence, sequence.personaId);
                    PersonaCinematicServer.movePersona(sequence, (double)object[0], (double)object[1], (double)object[2], sequence.yaw);
                }
                if (sequence.tick >= 46 && sequence.tick <= 54) {
                    double d = (double)(sequence.tick - 46) / 8.0; d = d*d*(3.0-2.0*d);
                    PersonaCinematicServer.movePersona(sequence, PersonaCinematicServer.lerp((double)object[0], dArray[0], d), PersonaCinematicServer.lerp((double)object[1], dArray[1], d), PersonaCinematicServer.lerp((double)object[2], dArray[2], d), sequence.yaw);
                    PersonaCinematicServer.setPersonaVisualState(sequence, sequence.personaId);
                }
                if (sequence.tick >= 55 && sequence.tick <= (fullAttack(sequence.personaId)?107:94)) {
                    PersonaCinematicServer.movePersonaAttack(sequence, dArray2[0], dArray2[1], dArray2[2], sequence.yaw, sequence.magic);
                }
                if (!fullAttack(sequence.personaId) && sequence.tick >= 95 && sequence.tick <= 107) {
                    PersonaCinematicServer.movePersona(sequence, dArray[0], dArray[1], dArray[2], sequence.yaw);
                    PersonaCinematicServer.setPersonaVisualState(sequence, sequence.personaId);
                }
                CinematicEffects.tick(sequence.player, sequence.tick, sequence.skillId, sequence.magic, sequence.base, sequence.yaw);
                if(sequence.tick==108){PersonaCinematicServer.finishSequence(uUID,sequence);continue;}
                if (sequence.tick != 86 || sequence.resolved) continue;
                sequence.resolved = PersonaDelayedActions.resolve(sequence.player, sequence.skillId);
                if (SEQUENCES.get(uUID) != sequence) continue;
                PersonaCinematicServer.setPersonaVisualState(sequence, sequence.personaId);
            }
            catch (Throwable throwable) {
                VelvetEclipseMod.LOGGER.error("Persona cinematic failed: " + String.valueOf(throwable), throwable);
                if (SEQUENCES.get(uUID) != sequence) continue;
                PersonaCinematicServer.finishSequence(uUID, sequence);
                com.gx.velveteclipse.network.ModNetwork.sendPresentation(sequence.player,new com.gx.velveteclipse.network.BattleCueS2C("cancel","",false,0,0,0,0,0,0));
                BattleService.sync(sequence.player,"Animação interrompida.");
            }
        }
    }

    private static void finishSequence(UUID uUID, Sequence sequence) {
        SEQUENCES.remove(uUID, sequence);
        PersonaCinematicServer.clearSequence(uUID, sequence);
    }

    private static void discardEvoker(Sequence sequence) {
        if (sequence.evoker != null) {
            try {
                RuntimeBridge.discardObjectEntity(sequence.evoker);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            sequence.evoker = null;
        }
    }

    private static boolean fullAttack(String id){return id.equals("izanagi")||id.equals("arsene")||id.equals("orpheus")||com.gx.velveteclipse.core.persona.ReloadCatalogue.provisional(id);}

    private static boolean isMagicSkill(String string) {
        Object object;
        try {
            object = PersonaRegistry.skill(string);
            if (((Optional)object).isPresent()) {
                return ((com.gx.velveteclipse.core.persona.SkillDefinition)((Optional)object).get()).usesMagicStat();
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        object = string == null ? "" : string.toLowerCase();
        return ((String)object).contains("agi") || ((String)object).contains("bufu") || ((String)object).contains("zio") || ((String)object).contains("garu") || ((String)object).contains("psi") || ((String)object).contains("frei") || ((String)object).contains("kouha") || ((String)object).contains("eiha") || ((String)object).contains("dia") || ((String)object).contains("media") || ((String)object).contains("spell") || ((String)object).contains("magic");
    }

    private static void setPersonaVisualState(Sequence sequence, String string) {
        Object object = VisualBattleActors.personaActor(sequence.player);
        if (object == null) {
            return;
        }
        try {
            object.getClass().getMethod("setPersonaId", String.class).invoke(object, string);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static void movePersona(Sequence sequence, double d, double d2, double d3, float f) {
        Object object = VisualBattleActors.personaActor(sequence.player);
        if (object == null || RuntimeBridge.isEntityRemoved(object)) {
            VisualBattleActors.replacePersona(sequence.player, sequence.personaId);
            object = VisualBattleActors.personaActor(sequence.player);
        }
        if (object != null) {
            RuntimeBridge.setEntityPosition(object, d, d2, d3);
            RuntimeBridge.setEntityRotation(object, f, 0.0f);
        }
    }

    private static void movePersonaAttack(Sequence sequence, double d, double d2, double d3, float f, boolean bl) {
        Object object = VisualBattleActors.personaActor(sequence.player);
        if (object == null || RuntimeBridge.isEntityRemoved(object)) {
            VisualBattleActors.replacePersona(sequence.player, sequence.personaId);
            object = VisualBattleActors.personaActor(sequence.player);
        }
        if (object != null) {
            RuntimeBridge.setEntityPosition(object, d, d2, d3);
            RuntimeBridge.setEntityRotation(object, f, bl ? -45.0f : 45.0f);
        }
    }

    private static void clearSequence(UUID uUID, Sequence sequence) {
        var session=BattleService.current(sequence.player);if(session!=null)session.cancelPrepared();
        try {
            PersonaCinematicServer.discardEvoker(sequence);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            if (sequence.player != null) {
                Object object = VisualBattleActors.personaActor(sequence.player);
                if (object != null && !RuntimeBridge.isEntityRemoved(object)) {
                    double[] dArray = RuntimeBridge.playerPosition(sequence.player);
                    RuntimeBridge.setEntityPosition(object, dArray[0], dArray[1] - 40.0, dArray[2]);
                    RuntimeBridge.setEntityRotation(object, RuntimeBridge.playerYaw(sequence.player), 0.0f);
                }
                if (BattleService.current(sequence.player) != null) {
                    VisualBattleActors.settleEnemy(sequence.player);
                }
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static double lerp(double d, double d2, double d3) {
        return d + (d2 - d) * d3;
    }

    private static double clamp01(double d) {
        return d < 0.0 ? 0.0 : (d > 1.0 ? 1.0 : d);
    }

    static final class Sequence {
        final UUID playerId;
        final Object player;
        final String skillId;
        String personaId;
        int tick;
        boolean resolved;
        boolean magic;
        double[] base;
        float yaw;
        Object evoker;

        Sequence(UUID uUID, Object object, String string, String string2) {
            this.playerId = uUID;
            this.player = object;
            this.skillId = string;
            this.personaId = string2;
            this.yaw = RuntimeBridge.playerYaw(object);
            var session=BattleService.current(object);
            var target=session==null||session.prepared()==null?null:BattleService.targetActor(object,session.prepared().action().targetId());
            if(target!=null){var origin=RuntimeBridge.playerPosition(object);double dx=target.m_20185_()-origin[0],dz=target.m_20189_()-origin[2];if(dx*dx+dz*dz>.001)this.yaw=(float)Math.toDegrees(Math.atan2(-dx,dz));}
        }
    }
}
