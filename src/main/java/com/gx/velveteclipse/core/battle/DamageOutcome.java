package com.gx.velveteclipse.core.battle;

import com.gx.velveteclipse.core.persona.Affinity;
import com.gx.velveteclipse.core.persona.Element;

/** newlyDowned also denotes a qualifying lethal hit for the 1 More reward. It does not set the downed state on a corpse. */
public record DamageOutcome(
   Element element, Affinity affinity, int damage, boolean critical, boolean newlyDowned, boolean reflected, boolean drained, boolean missed, String effect
) {
   public DamageOutcome(Element element,Affinity affinity,int damage,boolean critical,boolean newlyDowned,boolean reflected,boolean drained,boolean missed){this(element,affinity,damage,critical,newlyDowned,reflected,drained,missed,"");}
}
