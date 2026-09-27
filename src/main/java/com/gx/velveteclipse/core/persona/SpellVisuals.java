package com.gx.velveteclipse.core.persona;
import java.util.Set;
/** The packet and renderer share exactly the same family/scale decision. */
public final class SpellVisuals {
 public static boolean supported(String id){return PersonaRegistry.skill(id).map(s->s.element()!=Element.NONE&&!s.isPassive()).orElse(false);}
 public static Element family(String id){return PersonaRegistry.skill(id).map(SkillDefinition::element).orElse(Element.NONE);}
 public static float scale(String id){return PersonaRegistry.skill(id).map(s->(float)Math.min(1.65,.9+s.power()/250.0)).orElse(1f);}
 private SpellVisuals(){}
}
