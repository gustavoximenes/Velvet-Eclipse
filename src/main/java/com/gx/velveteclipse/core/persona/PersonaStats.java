package com.gx.velveteclipse.core.persona;

public record PersonaStats(int strength, int magic, int endurance, int agility, int luck) {
   public PersonaStats(int strength, int magic, int endurance, int agility, int luck) {
      if (strength >= 1 && magic >= 1 && endurance >= 1 && agility >= 1 && luck >= 1) {
         this.strength = strength;
         this.magic = magic;
         this.endurance = endurance;
         this.agility = agility;
         this.luck = luck;
      } else {
         throw new IllegalArgumentException("Persona stats must all be at least 1.");
      }
   }

   public int total() {
      return this.strength + this.magic + this.endurance + this.agility + this.luck;
   }
}
