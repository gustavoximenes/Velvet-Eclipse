package com.gx.velveteclipse.shadow;

import com.gx.velveteclipse.core.persona.*;
import java.util.*;

/** Enemy data never enters the summon/fusion/Shuffle Time Persona registry. */
public record EnemyDefinition(String id,String displayName,String family,int firstFloor,int lastFloor,
 int level,int hp,int sp,PersonaStats stats,Map<Element,Affinity> affinities,Set<String> skills,
 Element attack,int weight,boolean guardian) {
 public EnemyDefinition {affinities=Map.copyOf(affinities);skills=Collections.unmodifiableSet(new LinkedHashSet<>(skills));}
 public Affinity affinity(Element element){return affinities.getOrDefault(element,Affinity.NORMAL);}
 public String asset(){return "enemy_"+id;}
}
