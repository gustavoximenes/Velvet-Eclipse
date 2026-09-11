package com.gx.velveteclipse.core.battle;

import com.gx.velveteclipse.core.persona.Affinity;
import com.gx.velveteclipse.core.persona.Element;

public record DamageOutcome(
   Element element, Affinity affinity, int damage, boolean critical, boolean newlyDowned, boolean reflected, boolean drained, boolean missed
) {
}
