package com.gx.velveteclipse.core.battle;

import com.gx.velveteclipse.core.persona.Element;
import com.gx.velveteclipse.core.persona.PersonaRegistry;
import com.gx.velveteclipse.core.persona.SkillDefinition;
import com.gx.velveteclipse.core.persona.SkillTarget;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

public final class BattleSession {
   private final UUID id = UUID.randomUUID();
   private final List<BattleCombatant> turnOrder;
   private final Random random;
   private int turnIndex;
   public record Prepared(UUID actor, BattleAction action, DamageOutcome hit) {
      public boolean cutin(){return action.type()==BattleActionType.SKILL&&isWeaknessHit(hit);}
   }
   private Prepared prepared;
   public Prepared prepared(){return prepared;}
   public void cancelPrepared(){prepared=null;}
   public static SkillDefinition sword(){return new SkillDefinition("__basic_attack","Attack",Element.SLASH,35,96,0,0,true,SkillTarget.ONE_ENEMY);}
   /** Reserve one validated action and its random outcome until the animation reaches impact. */
   public Prepared prepare(UUID actorId,BattleAction action){
      if(prepared!=null||state!=BattleState.ACTIVE||currentActor()==null||!currentActor().id().equals(actorId))return null;
      var actor=currentActor();var target=validEnemyTarget(actor,action.targetId());
      var skill=action.type()==BattleActionType.ATTACK?sword():action.type()==BattleActionType.SKILL?PersonaRegistry.skill(action.skillId()).orElse(null):null;
      if(skill==null||target==null||!actor.alive())return null;
      if(action.type()==BattleActionType.SKILL&&(!actor.persona().learnedSkills().contains(skill.id())||actor.sp()<skill.spCost()
         ||(skill.hpPercentCost()>0&&actor.hp()<=Math.max(1,actor.maxHp()*skill.hpPercentCost()/100))))return null;
      prepared=new Prepared(actorId,action,DamageCalculator.roll(actor,target,skill,random));return prepared;
   }
   private DamageOutcome damage(BattleCombatant actor,BattleCombatant target,SkillDefinition skill){
      if(prepared==null)return DamageCalculator.resolve(actor,target,skill,random);
      var hit=prepared.hit();prepared=null;return DamageCalculator.apply(actor,target,hit);
   }
   private BattleState state = BattleState.ACTIVE;

   public BattleSession(List<BattleCombatant> var1) {
      this(var1, new Random());
   }

   public BattleSession(List<BattleCombatant> var1, Random var2) {
      if (var1 != null && var1.size() >= 2) {
         this.random = var2;
         this.turnOrder = new ArrayList<>(var1);
         this.turnOrder.sort(Comparator.<BattleCombatant>comparingInt(var0 -> var0.stats().agility()).reversed().thenComparing(var0 -> var0.id().toString()));
         this.turnIndex = this.firstLivingIndex(0);
         this.updateState();
      } else {
         throw new IllegalArgumentException("A battle needs at least two participants.");
      }
   }

   public UUID id() {
      return this.id;
   }

   public BattleState state() {
      return this.state;
   }

   public List<BattleCombatant> participants() {
      return List.copyOf(this.turnOrder);
   }

   public BattleCombatant currentActor() {
      return this.state != BattleState.ACTIVE ? null : this.turnOrder.get(this.turnIndex);
   }

   public Optional<BattleCombatant> find(UUID var1) {
      return this.turnOrder.stream().filter(var1x -> var1x.id().equals(var1)).findFirst();
   }

   public List<BattleCombatant> enemiesOf(BattleCombatant var1) {
      return this.turnOrder.stream().filter(BattleCombatant::alive).filter(var1x -> var1x.side() != var1.side()).toList();
   }

   public BattleResolution perform(UUID var1, BattleAction var2) {
      if(prepared!=null&&(!prepared.actor().equals(var1)||!prepared.action().equals(var2)))
         return new BattleResolution(state,false,List.of("An action is already in progress."));
      ArrayList var3 = new ArrayList();
      if (this.state != BattleState.ACTIVE) {
         return new BattleResolution(this.state, false, List.of("The battle has already ended."));
      } else {
         BattleCombatant var4 = this.currentActor();
         if (var4 == null || !var4.id().equals(var1)) {
            return new BattleResolution(this.state, false, List.of("It is not that combatant's turn."));
         } else if (!var4.alive()) {
            this.advanceTurn();
            return new BattleResolution(this.state, false, List.of(var4.name() + " cannot act."));
         } else {
            var4.setGuarding(false);
            boolean var5 = false;
            boolean weaknessHit = false;
            switch (var2.type()) {
               case ITEM:
                  if (!"pulse_tonic".equals(var2.skillId())) {
                     return new BattleResolution(this.state, false, List.of("Unknown item."));
                  }

                  int var10 = var4.hp();
                  var4.heal(Math.max(1, var4.maxHp() * 30 / 100));
                  var3.add(var4.name() + " used Pulse Tonic and recovered " + (var4.hp() - var10) + " HP.");
                  break;
               case GUARD:
                  var4.setGuarding(true);
                  var3.add(var4.name() + " braces for the next attack.");
                  break;
               case ESCAPE:
                  this.state = BattleState.ABORTED;
                  var3.add(var4.name() + " escaped the battle.");
                  return new BattleResolution(this.state, false, List.copyOf(var3));
               case ATTACK:
                  BattleCombatant var9 = this.validEnemyTarget(var4, var2.targetId());
                  if (var9 == null) {
                     return new BattleResolution(this.state, false, List.of("Invalid target."));
                  }

                  SkillDefinition var11 = sword();
                  DamageOutcome var12 = damage(var4, var9, var11);
                  weaknessHit = isWeaknessHit(var12);
                  var5 = this.appendOutcome(var3, var4, var9, var11, var12);
                  break;
               case SKILL:
                  SkillDefinition var6 = PersonaRegistry.skill(var2.skillId()).orElse(null);
                  if (var6 == null) {
                     return new BattleResolution(this.state, false, List.of("Unknown skill: " + var2.skillId()));
                  }

                  if (!var4.persona().learnedSkills().contains(var6.id())) {
                     return new BattleResolution(this.state, false, List.of(var4.name() + " has not learned " + var6.displayName() + "."));
                  }

                  if (var6.spCost() > 0 && !var4.spendSp(var6.spCost())) {
                     return new BattleResolution(this.state, false, List.of("Not enough SP."));
                  }

                  if (var6.hpPercentCost() > 0 && !var4.spendHpPercent(var6.hpPercentCost())) {
                     return new BattleResolution(this.state, false, List.of("Not enough HP to use that physical skill."));
                  }

                  BattleCombatant var7 = this.validEnemyTarget(var4, var2.targetId());
                  if (var7 == null) {
                     return new BattleResolution(this.state, false, List.of("Invalid target."));
                  }

                  DamageOutcome var8 = damage(var4, var7, var6);
                  weaknessHit = isWeaknessHit(var8);
                  var5 = this.appendOutcome(var3, var4, var7, var6, var8);
            }

            this.updateState();
            if (this.state == BattleState.ACTIVE && !var5) {
               this.advanceTurn();
            } else if (var5) {
               var3.add("ONE MORE! " + var4.name() + " acts again.");
            }

            this.updateState();
            return new BattleResolution(this.state, var5, List.copyOf(var3), weaknessHit);
         }
      }
   }

   private static boolean isWeaknessHit(DamageOutcome hit){return hit.damage()>0&&!hit.missed()&&!hit.reflected()&&!hit.drained()&&hit.affinity()==com.gx.velveteclipse.core.persona.Affinity.WEAK;}

   private boolean appendOutcome(List<String> var1, BattleCombatant var2, BattleCombatant var3, SkillDefinition var4, DamageOutcome var5) {
      if (var5.missed()) {
         var1.add(var2.name() + "'s " + var4.displayName() + " missed " + var3.name() + ".");
         return false;
      } else if (var5.reflected()) {
         var1.add(var3.name() + " repelled " + var4.displayName() + "; " + var2.name() + " took " + var5.damage() + " damage.");
         return false;
      } else if (var5.drained()) {
         var1.add(var3.name() + " drained " + var4.displayName() + " and recovered " + var5.damage() + " HP.");
         return false;
      } else if (var5.damage() == 0) {
         var1.add(var3.name() + " nullified " + var4.displayName() + ".");
         return false;
      } else {
         StringBuilder var6 = new StringBuilder(var2.name())
            .append(" used ")
            .append(var4.displayName())
            .append(" on ")
            .append(var3.name())
            .append(" for ")
            .append(var5.damage())
            .append(" damage");
         if (var5.affinity().name().equals("WEAK")) {
            var6.append(" [WEAK]");
         }

         if (var5.critical()) {
            var6.append(" [CRITICAL]");
         }

         var6.append(".");
         var1.add(var6.toString());
         if (!var3.alive()) {
            var1.add(var3.name() + " was defeated.");
         } else if (var5.newlyDowned()) {
            var1.add(var3.name() + " was knocked down.");
         }

         return var5.newlyDowned();
      }
   }

   private BattleCombatant validEnemyTarget(BattleCombatant var1, UUID var2) {
      return var2 == null
         ? this.enemiesOf(var1).stream().findFirst().orElse(null)
         : this.find(var2).filter(BattleCombatant::alive).filter(var1x -> var1x.side() != var1.side()).orElse(null);
   }

   private void advanceTurn() {
      if (this.state == BattleState.ACTIVE) {
         int var1 = this.firstLivingIndex(this.turnIndex + 1);
         this.turnIndex = var1;
         BattleCombatant var2 = this.currentActor();
         if (var2 != null && var2.downed()) {
            var2.setDowned(false);
         }
      }
   }

   private int firstLivingIndex(int var1) {
      int var2 = this.turnOrder.size();

      for (int var3 = 0; var3 < var2; var3++) {
         int var4 = Math.floorMod(var1 + var3, var2);
         if (this.turnOrder.get(var4).alive()) {
            return var4;
         }
      }

      return 0;
   }

   private void updateState() {
      boolean var1 = this.turnOrder.stream().anyMatch(var0 -> var0.side() == BattleSide.PLAYERS && var0.alive());
      boolean var2 = this.turnOrder.stream().anyMatch(var0 -> var0.side() == BattleSide.ENEMIES && var0.alive());
      if (!var1 && var2) {
         this.state = BattleState.ENEMIES_WON;
      } else if (var1 && !var2) {
         this.state = BattleState.PLAYERS_WON;
      } else if (!var1) {
         this.state = BattleState.ABORTED;
      }
   }
}
