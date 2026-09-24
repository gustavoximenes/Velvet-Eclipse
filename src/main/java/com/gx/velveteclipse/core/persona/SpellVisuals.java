package com.gx.velveteclipse.core.persona;
import java.util.Set;
/** The packet and renderer share exactly the same family/scale decision. */
public final class SpellVisuals {
 private static final Set<Element> FAMILIES=Set.of(Element.FIRE,Element.DARK,Element.WIND,Element.LIGHT,Element.ICE,Element.NUCLEAR,Element.PSY,Element.ALMIGHTY);
 public static boolean supported(String id){var s=PersonaRegistry.skill(id).orElse(null);return s!=null&&s.isDamageSkill()&&s.power()>0&&FAMILIES.contains(s.element());}
 public static Element family(String id){return PersonaRegistry.skill(id).map(SkillDefinition::element).orElse(Element.NONE);}
 public static float scale(String id){return PersonaRegistry.skill(id).map(s->(float)Math.min(1.65,.9+s.power()/250.0)).orElse(1f);}
 private SpellVisuals(){}
}
