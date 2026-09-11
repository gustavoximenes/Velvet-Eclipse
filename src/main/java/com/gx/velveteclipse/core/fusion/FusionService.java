package com.gx.velveteclipse.core.fusion;

import com.gx.velveteclipse.core.persona.Arcana;
import com.gx.velveteclipse.core.persona.PersonaDefinition;
import com.gx.velveteclipse.core.persona.PersonaInstance;
import com.gx.velveteclipse.core.persona.PersonaRegistry;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class FusionService {
   private static final Map<Set<Arcana>, Arcana> ARCANA_RULES = Map.ofEntries(
      Map.entry(Set.of(Arcana.MAGICIAN, Arcana.LOVERS), Arcana.STAR),
      Map.entry(Set.of(Arcana.FOOL, Arcana.MAGICIAN), Arcana.STRENGTH),
      Map.entry(Set.of(Arcana.FOOL, Arcana.STRENGTH), Arcana.MOON),
      Map.entry(Set.of(Arcana.MOON, Arcana.STAR), Arcana.FOOL)
   );

   private FusionService() {
   }

   public static Optional<FusionResult> preview(PersonaInstance var0, PersonaInstance var1) {
      if (var0.definitionId().equals(var1.definitionId())) {
         return Optional.empty();
      } else {
         PersonaDefinition var2 = PersonaRegistry.persona(var0.definitionId()).orElse(null);
         PersonaDefinition var3 = PersonaRegistry.persona(var1.definitionId()).orElse(null);
         if (var2 != null && var3 != null) {
            if (var2.arcana() == var3.arcana()) {
               return Optional.empty();
            } else {
               Arcana var4 = ARCANA_RULES.get(Set.of(var2.arcana(), var3.arcana()));
               if (var4 == null) {
                  return Optional.empty();
               } else {
                  int var5 = Math.max(1, (var0.level() + var1.level()) / 2 + 1);
                  PersonaDefinition var6 = PersonaRegistry.personas()
                     .values()
                     .stream()
                     .filter(var1x -> var1x.arcana() == var4)
                     .filter(var2x -> !var2x.id().equals(var0.definitionId()) && !var2x.id().equals(var1.definitionId()))
                     .min(Comparator.comparingInt(var1x -> Math.abs(var1x.baseLevel() - var5)))
                     .orElse(null);
                  if (var6 == null) {
                     return Optional.empty();
                  } else {
                     LinkedHashSet var7 = new LinkedHashSet();
                     var7.addAll(var0.learnedSkills());
                     var7.addAll(var1.learnedSkills());
                     var6.learnset().values().forEach(var7::removeAll);
                     return Optional.of(new FusionResult(var6.id(), List.copyOf(var7)));
                  }
               }
            }
         } else {
            return Optional.empty();
         }
      }
   }
}
