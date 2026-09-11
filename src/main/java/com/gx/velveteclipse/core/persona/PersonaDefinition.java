package com.gx.velveteclipse.core.persona;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PersonaDefinition {
   private final String id;
   private final String displayName;
   private final Arcana arcana;
   private final int baseLevel;
   private final PersonaStats baseStats;
   private final Map<Element, Affinity> affinities;
   private final Map<Integer, List<String>> learnset;

   public PersonaDefinition(String var1, String var2, Arcana var3, int var4, PersonaStats var5, Map<Element, Affinity> var6, Map<Integer, List<String>> var7) {
      if (var1 == null || var1.isBlank()) {
         throw new IllegalArgumentException("Persona id is required.");
      } else if (var2 == null || var2.isBlank()) {
         throw new IllegalArgumentException("Persona name is required.");
      } else if (var4 < 1) {
         throw new IllegalArgumentException("Base level must be at least 1.");
      } else {
         this.id = var1;
         this.displayName = var2;
         this.arcana = var3;
         this.baseLevel = var4;
         this.baseStats = var5;
         EnumMap var8 = new EnumMap<>(Element.class);

         for (Element var12 : Element.values()) {
            var8.put(var12, Affinity.NORMAL);
         }

         if (var6 != null) {
            var8.putAll(var6);
            // Existing physical affinities also apply to swords unless explicitly overridden.
            if(!var6.containsKey(Element.SLASH))var8.put(Element.SLASH,var6.getOrDefault(Element.PHYSICAL,Affinity.NORMAL));
         }

         this.affinities = Collections.unmodifiableMap(var8);
         LinkedHashMap var13 = new LinkedHashMap();
         if (var7 != null) {
            var7.forEach((var1x, var2x) -> var13.put(var1x, List.copyOf(var2x)));
         }

         this.learnset = Collections.unmodifiableMap(var13);
      }
   }

   public String id() {
      return this.id;
   }

   public String displayName() {
      return this.displayName;
   }

   public Arcana arcana() {
      return this.arcana;
   }

   public int baseLevel() {
      return this.baseLevel;
   }

   public PersonaStats baseStats() {
      return this.baseStats;
   }

   public Map<Element, Affinity> affinities() {
      return this.affinities;
   }

   public Map<Integer, List<String>> learnset() {
      return this.learnset;
   }

   public Affinity affinity(Element var1) {
      return this.affinities.getOrDefault(var1, Affinity.NORMAL);
   }
}
