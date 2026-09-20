package com.gx.velveteclipse.core.persona;

import java.util.LinkedHashSet;
import java.util.Set;

public final class PersonaInstance {
   private final String definitionId;
   private int level;
   private int experience;
   private final LinkedHashSet<String> learnedSkills = new LinkedHashSet<>();

   public PersonaInstance(PersonaDefinition var1) {
      this.definitionId = var1.id();
      this.level = var1.baseLevel();
      this.experience = 0;
      this.learnSkillsForLevel(var1, this.level);
   }

   public PersonaInstance(String var1, int var2, int var3, Set<String> var4) {
      this.definitionId = var1;
      this.level = Math.max(1, var2);
      this.experience = Math.max(0, var3);
      if (var4 != null) {
         this.learnedSkills.addAll(var4);
      }
   }

   public String definitionId() {
      return this.definitionId;
   }

   public int level() {
      return this.level;
   }

   public int experience() {
      return this.experience;
   }

   /** Administrative level change preserves learned/inherited skills and resets level progress. */
   public void setLevel(PersonaDefinition definition,int value) {
      if(value<1||value>99||!definitionId.equals(definition.id()))throw new IllegalArgumentException("Persona level must be 1..99 and match its definition");
      level=value;experience=0;learnSkillsForLevel(definition,value);
   }

   public Set<String> learnedSkills() {
      return Set.copyOf(this.learnedSkills);
   }

   public void addExperience(PersonaDefinition var1, int var2) {
      if (var2 > 0) {
         this.experience += var2;

         while (this.experience >= experienceForNextLevel(this.level)) {
            this.experience = this.experience - experienceForNextLevel(this.level);
            this.level++;
            this.learnSkillsForLevel(var1, this.level);
         }
      }
   }

   public void inheritSkill(String var1) {
      if (var1 != null && !var1.isBlank()) {
         this.learnedSkills.add(var1);
      }
   }

   private void learnSkillsForLevel(PersonaDefinition var1, int var2) {
      var1.learnset().forEach((var2x, var3) -> {
         if (var2x <= var2) {
            this.learnedSkills.addAll(var3);
         }
      });
   }

   public static int experienceForNextLevel(int var0) {
      return 50 + var0 * var0 * 8;
   }
}
