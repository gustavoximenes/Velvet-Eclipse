package com.gx.velveteclipse.core.persona;

public record SkillDefinition(
   String id, String displayName, Element element, int power, int accuracy, int spCost, int hpPercentCost, boolean canCritical, SkillTarget target
) {
   public SkillDefinition(
      String id, String displayName, Element element, int power, int accuracy, int spCost, int hpPercentCost, boolean canCritical, SkillTarget target
   ) {
      if (id == null || id.isBlank()) {
         throw new IllegalArgumentException("Skill id is required.");
      } else if (displayName == null || displayName.isBlank()) {
         throw new IllegalArgumentException("Skill name is required.");
      } else if (power < 0) {
         throw new IllegalArgumentException("Skill power cannot be negative.");
      } else if (accuracy < 1 || accuracy > 100) {
         throw new IllegalArgumentException("Accuracy must be 1..100.");
      } else if (spCost < 0) {
         throw new IllegalArgumentException("SP cost cannot be negative.");
      } else if (hpPercentCost < 0 || hpPercentCost > 100) {
         throw new IllegalArgumentException("HP percent cost must be 0..100.");
      } else if (spCost > 0 && hpPercentCost > 0) {
         throw new IllegalArgumentException("A skill cannot have both SP and HP cost in the initial ruleset.");
      } else {
         this.id = id;
         this.displayName = displayName;
         this.element = element;
         this.power = power;
         this.accuracy = accuracy;
         this.spCost = spCost;
         this.hpPercentCost = hpPercentCost;
         this.canCritical = canCritical;
         this.target = target;
      }
   }

   public boolean usesMagicStat() {
      if(id.equals("__basic_attack"))return false;
      return switch (this.element) {
         case PHYSICAL, SLASH, PIERCE, NONE -> false;
         case HEALING, SUPPORT, AILMENT -> true;
         default -> true;
      };
   }

   public boolean isDamageSkill() {
      return this.power > 0 && this.element != Element.HEALING && this.element != Element.SUPPORT;
   }
}
