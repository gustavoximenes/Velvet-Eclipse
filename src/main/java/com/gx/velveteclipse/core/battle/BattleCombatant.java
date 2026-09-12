package com.gx.velveteclipse.core.battle;

import com.gx.velveteclipse.core.persona.PersonaDefinition;
import com.gx.velveteclipse.core.persona.PersonaInstance;
import com.gx.velveteclipse.core.persona.PersonaRegistry;
import com.gx.velveteclipse.core.persona.PersonaStats;
import java.util.UUID;

public final class BattleCombatant {
   private final UUID id;
   private final String name;
   private final BattleSide side;
   private PersonaInstance persona;
   private int maxHp;
   private int hp;
   private int maxSp;
   private int sp;
   private boolean guarding;
   private boolean downed;
   private int attackTurns, defenseTurns, defenseStage;
   private boolean attackWaiting, defenseWaiting;

   public double attackMultiplier(){return attackTurns>0?1.33:1;}
   public double defenseMultiplier(){return defenseTurns>0?(defenseStage>0?1.33:.66):1;}
   public int attackTurns(){return attackTurns;}
   public int defenseTurns(){return defenseTurns;}
   public void attackStatus(boolean waiting){attackTurns=3;attackWaiting=waiting;}
   public void defenseStatus(int stage,boolean waiting){
      // Opposing effects cancel; repeated effects refresh duration without stacking.
      if(defenseTurns>0&&defenseStage!=stage){defenseTurns=0;defenseStage=0;defenseWaiting=false;}
      else {defenseStage=stage;defenseTurns=3;defenseWaiting=waiting;}
   }
   public void beginTurn(){
      if(attackWaiting)attackWaiting=false;else if(attackTurns>0)--attackTurns;
      if(defenseWaiting)defenseWaiting=false;else if(defenseTurns>0)--defenseTurns;
   }

   public BattleCombatant(UUID var1, String var2, BattleSide var3, PersonaInstance var4, int var5, int var6, int var7, int var8) {
      this.id = var1;
      this.name = var2;
      this.side = var3;
      this.persona = var4;
      this.maxHp = Math.max(1, var5);
      this.hp = clamp(var6, 0, this.maxHp);
      this.maxSp = Math.max(0, var7);
      this.sp = clamp(var8, 0, this.maxSp);
   }

   public UUID id() {
      return this.id;
   }

   public String name() {
      return this.name;
   }

   public BattleSide side() {
      return this.side;
   }

   public PersonaInstance persona() {
      return this.persona;
   }

   public void setPersona(PersonaInstance var1) {
      if (var1 != null) {
         this.persona = var1;
      }
   }

   public int maxHp() {
      return this.maxHp;
   }

   public int hp() {
      return this.hp;
   }

   public int maxSp() {
      return this.maxSp;
   }

   public int sp() {
      return this.sp;
   }

   public boolean guarding() {
      return this.guarding;
   }

   public boolean downed() {
      return this.downed;
   }

   public boolean alive() {
      return this.hp > 0;
   }

   public PersonaDefinition personaDefinition() {
      return PersonaRegistry.persona(this.persona.definitionId())
         .orElseThrow(() -> new IllegalStateException("Unknown persona: " + this.persona.definitionId()));
   }

   public PersonaStats stats() {
      PersonaStats var1 = this.personaDefinition().baseStats();
      int var2 = Math.max(0, this.persona.level() - this.personaDefinition().baseLevel());
      return new PersonaStats(
         var1.strength() + var2 / 2, var1.magic() + var2 / 2, var1.endurance() + var2 / 2, var1.agility() + var2 / 3, var1.luck() + var2 / 3
      );
   }

   public void damage(int var1) {
      int var2 = Math.max(0, var1);
      if (this.guarding) {
         var2 = Math.max(1, var2 / 2);
      }

      this.hp = clamp(this.hp - var2, 0, this.maxHp);
   }

   public void heal(int var1) {
      this.hp = clamp(this.hp + Math.max(0, var1), 0, this.maxHp);
   }

   public boolean spendSp(int var1) {
      if (var1 >= 0 && this.sp >= var1) {
         this.sp -= var1;
         return true;
      } else {
         return false;
      }
   }

   public boolean spendHpPercent(int var1) {
      if (var1 <= 0) {
         return true;
      } else {
         int var2 = Math.max(1, this.maxHp * var1 / 100);
         if (this.hp <= var2) {
            return false;
         } else {
            this.hp -= var2;
            return true;
         }
      }
   }

   public void setGuarding(boolean var1) {
      this.guarding = var1;
   }

   public void setDowned(boolean var1) {
      this.downed = var1;
   }

   private static int clamp(int var0, int var1, int var2) {
      return Math.max(var1, Math.min(var2, var0));
   }
}
