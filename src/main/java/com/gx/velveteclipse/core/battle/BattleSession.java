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
   private EncounterOpening opening=EncounterOpening.STANDARD;
   private boolean openingRound;
   private void agilityOrder(){turnOrder.sort(Comparator.<BattleCombatant>comparingInt(c->c.stats().agility()).reversed().thenComparing(c->c.id().toString()));}
   private UUID bonusActor;
   private final java.util.Set<UUID> batonChain=new java.util.HashSet<>();
   private int tacticalEvent;
   private String tacticalKind="";
   public int tacticalEvent(){return tacticalEvent;}
   public String tacticalKind(){return tacticalKind;}
   public boolean oneMore(){return state==BattleState.ACTIVE&&bonusActor!=null;}
   public boolean canSwitchPersona(UUID actor){return !oneMore()&&prepared==null&&currentActor()!=null&&currentActor().id().equals(actor);}
   public List<BattleCombatant> batonTargets(){
      var actor=currentActor();if(!oneMore()||actor==null||actor.side()!=BattleSide.PLAYERS)return List.of();
      return alliesOf(actor).stream().filter(a->!a.id().equals(actor.id())&&!a.downed()&&!batonChain.contains(a.id())).toList();
   }
   public boolean batonPass(UUID actor,UUID target){
      if(prepared!=null||!oneMore()||currentActor()==null||!currentActor().id().equals(actor)||batonTargets().stream().noneMatch(a->a.id().equals(target)))return false;
      currentActor().setBatonBoost(false);batonChain.add(actor);batonChain.add(target);bonusActor=target;currentActor().setBatonBoost(true);
      tacticalKind="SHIFT";tacticalEvent++;return true;
   }
   public boolean canAllOut(UUID actor){
      var a=currentActor();if(!oneMore()||a==null||!a.id().equals(actor)||a.side()!=BattleSide.PLAYERS||a.downed())return false;
      var enemies=enemiesOf(a);return !enemies.isEmpty()&&enemies.stream().allMatch(BattleCombatant::downed);
   }
   public List<BattleCombatant> allOutFighters(){var actor=currentActor();return actor==null?List.of():alliesOf(actor).stream().filter(a->!a.downed()).toList();}
   public SkillDefinition allOutSkill(){return new SkillDefinition("__all_out","All-Out Attack",Element.NONE,30+allOutFighters().size()*22,100,0,0,false,SkillTarget.ALL_ENEMIES);}
   public record Prepared(UUID actor, BattleAction action, DamageOutcome hit, java.util.Map<UUID,DamageOutcome> hits) {
      public boolean cutin(){return action.type()==BattleActionType.SKILL&&hits.values().stream().anyMatch(BattleSession::isWeaknessHit);}
   }
   private Prepared prepared;
   public Prepared prepared(){return prepared;}
   public void cancelPrepared(){prepared=null;}
   public static SkillDefinition sword(){return new SkillDefinition("__basic_attack","Attack",Element.SLASH,35,96,0,0,true,SkillTarget.ONE_ENEMY);}
   private static SkillDefinition basic(BattleCombatant actor){return new SkillDefinition("__basic_attack","Attack",actor.attackElement(),actor.equipment().attackPower(),96,0,0,true,SkillTarget.ONE_ENEMY);}
   /** Reserve one validated action and its random outcome until the animation reaches impact. */
   public Prepared prepare(UUID actorId,BattleAction action){
      if(prepared!=null||state!=BattleState.ACTIVE||currentActor()==null||!currentActor().id().equals(actorId))return null;
      var actor=currentActor();
      if(action.type()==BattleActionType.ALL_OUT&&!canAllOut(actorId))return null;
      var skill=action.type()==BattleActionType.ALL_OUT?allOutSkill():action.type()==BattleActionType.ITEM&&com.gx.velveteclipse.equipment.DamageItems.get(action.skillId())!=null?com.gx.velveteclipse.equipment.DamageItems.get(action.skillId()).skill():action.type()==BattleActionType.ATTACK?basic(actor):action.type()==BattleActionType.SKILL?PersonaRegistry.skill(action.skillId()).orElse(null):null;
      var target=skill==null?null:validSkillTarget(actor,action.targetId(),skill);
      if(skill==null||target==null||!actor.alive())return null;
      if(action.type()==BattleActionType.SKILL&&(!actor.persona().learnedSkills().contains(skill.id())||actor.sp()<actor.spCost(skill.spCost())
         ||(skill.hpPercentCost()>0&&actor.hp()<=Math.max(1,actor.maxHp()*actor.hpCost(skill.hpPercentCost())/100))))return null;
      var hits=new java.util.LinkedHashMap<UUID,DamageOutcome>();
      if(!skill.isDamageSkill()) {
         if(skill.element()!=Element.HEALING&&!SupportEffects.supported(skill.id()))return null;
         prepared=new Prepared(actorId,action,null,java.util.Map.of());return prepared;
      }
      var targets=skill.target()==SkillTarget.ALL_ENEMIES?enemiesOf(actor):List.of(target);
      for(var victim:targets)hits.put(victim.id(),action.type()==BattleActionType.ITEM?com.gx.velveteclipse.equipment.DamageItems.roll(com.gx.velveteclipse.equipment.DamageItems.get(action.skillId()),victim):DamageCalculator.roll(actor,victim,skill,random));
      prepared=new Prepared(actorId,action,hits.get(target.id()),java.util.Map.copyOf(hits));return prepared;
   }
   private DamageOutcome damage(BattleCombatant actor,BattleCombatant target,SkillDefinition skill){
      if(prepared==null)return DamageCalculator.resolve(actor,target,skill,random);
      var hit=prepared.hits().get(target.id());return DamageCalculator.apply(actor,target,hit);
   }
   private BattleState state = BattleState.ACTIVE;

   public BattleSession(List<BattleCombatant> var1) {
      this(var1, new Random());
   }

   public BattleSession(List<BattleCombatant> var1, Random var2) { this(var1,var2,EncounterOpening.STANDARD); }
   public BattleSession(List<BattleCombatant> var1, Random var2,EncounterOpening mode) {
      if (var1 != null && var1.size() >= 2) {
         this.random = var2;
         this.turnOrder = new ArrayList<>(var1);
         for(var source:turnOrder)for(var ally:turnOrder)if(source.side()==ally.side())source.applyPartyPassives(ally);
         this.turnOrder.sort(Comparator.<BattleCombatant>comparingInt(var0 -> var0.stats().agility()).reversed().thenComparing(var0 -> var0.id().toString()));
         this.opening=mode;this.openingRound=mode!=EncounterOpening.STANDARD;
         if(openingRound){java.util.Collections.shuffle(turnOrder,random);if(mode!=EncounterOpening.NEUTRAL){var first=mode==EncounterOpening.ADVANTAGE?BattleSide.PLAYERS:BattleSide.ENEMIES;turnOrder.sort(Comparator.comparingInt(c->c.side()==first?0:1));}}
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
      return state!=BattleState.ACTIVE?null:bonusActor==null?turnOrder.get(turnIndex):find(bonusActor).orElse(null);
   }

   public Optional<BattleCombatant> find(UUID var1) {
      return this.turnOrder.stream().filter(var1x -> var1x.id().equals(var1)).findFirst();
   }

   public List<BattleCombatant> enemiesOf(BattleCombatant var1) {
      return this.turnOrder.stream().filter(BattleCombatant::alive).filter(var1x -> var1x.side() != var1.side()).toList();
   }

   public double escapeChance(){var a=currentActor();if(a==null)return 0;double agility=enemiesOf(a).stream().mapToInt(c->c.stats().agility()).average().orElse(a.stats().agility());return Math.max(.20,Math.min(.60,.35+(a.stats().agility()-agility)*.01));}
   /** A failed attempt consumes the turn; the caller detaches the successful player. */
   public boolean attemptEscape(UUID actor){
      if(state!=BattleState.ACTIVE||prepared!=null||currentActor()==null||!currentActor().id().equals(actor))return false;
      currentActor().setGuarding(false);boolean success=random.nextDouble()<escapeChance();if(!success)advanceTurn();return success;
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
               case ALL_OUT:
                  if(!canAllOut(var1))return new BattleResolution(state,false,List.of("All-Out Attack indisponível."));
                  var allOut=allOutSkill();
                  for(var enemy:enemiesOf(var4)){
                     var hit=damage(var4,enemy,allOut);appendOutcome(var3,var4,enemy,allOut,hit);
                     enemy.setDowned(false);
                  }
                  break;
               case ITEM:
                  var attackItem=com.gx.velveteclipse.equipment.DamageItems.get(var2.skillId());
                  if(attackItem!=null){var first=validEnemyTarget(var4,var2.targetId());if(first==null)return new BattleResolution(state,false,List.of("Escolha um inimigo vivo."));for(var victim:attackItem.all()?enemiesOf(var4):List.of(first)){var roll=prepared==null?com.gx.velveteclipse.equipment.DamageItems.roll(attackItem,victim):prepared.hits().get(victim.id());var hit=DamageCalculator.apply(var4,victim,roll);weaknessHit|=isWeaknessHit(hit);var5|=appendOutcome(var3,var4,victim,attackItem.skill(),hit);if(victim.alive()&&hit.damage()>0&&!hit.drained()&&!hit.reflected()&&random.nextInt(100)<10){if(attackItem.element()==Element.ICE)victim.afflict("FREEZE");else if(attackItem.element()==Element.ELECTRIC)victim.afflict("SHOCK");}}break;}
                  if (!"pulse_tonic".equals(var2.skillId())) {
                     var supply=com.gx.velveteclipse.equipment.Supplies.get(var2.skillId());
                     var recipient=var2.targetId()==null?var4:find(var2.targetId()).filter(c->c.side()==var4.side()).orElse(null);
                     if(recipient==null||!com.gx.velveteclipse.equipment.Supplies.useful(supply,recipient))return new BattleResolution(state,false,List.of("Item indisponível para este alvo."));
                     com.gx.velveteclipse.equipment.Supplies.apply(supply,recipient);var3.add(var4.name()+" usou "+supply.name()+" em "+recipient.name()+".");break;
                  }

                  var tonicTarget=var2.targetId()==null?var4:find(var2.targetId()).filter(c->c.alive()&&c.side()==var4.side()).orElse(null);
                  if(tonicTarget==null)return new BattleResolution(state,false,List.of("Alvo inválido."));
                  int var10 = tonicTarget.hp();tonicTarget.heal(Math.max(1,tonicTarget.maxHp()*30/100));
                  var3.add(var4.name()+" usou Pulse Tonic em "+tonicTarget.name()+": +"+(tonicTarget.hp()-var10)+" HP.");
                  break;
               case GUARD:
                  var4.setGuarding(true);
                  var3.add(var4.name() + " braces for the next attack.");
                  break;
               case ESCAPE:
                  boolean escaped=attemptEscape(var1);if(escaped)this.state=BattleState.ABORTED;
                  var3.add(escaped?var4.name()+" fugiu da batalha.":"A fuga falhou! Você perdeu o turno.");
                  return new BattleResolution(this.state, false, List.copyOf(var3));
               case ATTACK:
                  BattleCombatant var9 = this.validEnemyTarget(var4, var2.targetId());
                  if (var9 == null) {
                     return new BattleResolution(this.state, false, List.of("Invalid target."));
                  }

                  SkillDefinition var11 = basic(var4);
                  DamageOutcome var12 = damage(var4, var9, var11);
                  if(!var12.missed()&&!var12.reflected()&&!var12.drained()&&var12.damage()>0&&var9.alive()){
                     String ailment=var4.equipment().ailment();if(ailment.equals("RANDOM AILMENT"))ailment=List.of("POISON","CHARM","CONFUSE","FEAR","RAGE","DISTRESS").get(random.nextInt(6));
                     if(!ailment.isEmpty()&&random.nextInt(100)<(var4.equipment().ailmentChance()+var4.equipment().ailmentBoost(ailment))/(var9.equipment().resistant(ailment)?2:1)){var9.afflict(ailment);var3.add(var9.name()+": "+ailment);}
                  }
                  var4.useCharge(false);weaknessHit = isWeaknessHit(var12);
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

                  BattleCombatant var7 = this.validSkillTarget(var4, var2.targetId(), var6);
                  if (var7 == null) return new BattleResolution(this.state,false,List.of("Invalid target."));
                  if(!var6.isDamageSkill()&&var6.element()!=Element.HEALING&&!SupportEffects.supported(var6.id()))
                     return new BattleResolution(this.state,false,List.of("Unsupported skill."));
                  if (var6.spCost() > 0 && !var4.spendSp(var6.spCost())) {
                     return new BattleResolution(this.state, false, List.of("Not enough SP."));
                  }

                  if (var6.hpPercentCost() > 0 && !var4.spendHpPercent(var6.hpPercentCost())) {
                     return new BattleResolution(this.state, false, List.of("Not enough HP to use that physical skill."));
                  }

                  if(!var6.isDamageSkill()) {
                     var allies=var6.target()==SkillTarget.ALL_ALLIES?alliesOf(var4):List.of(var7);
                     for(var ally:allies){
                        if(var6.element()==Element.HEALING){int before=ally.hp();ally.heal(Math.max(1,var6.power()));var3.add(var4.name()+" usou "+var6.displayName()+" em "+ally.name()+": +"+(ally.hp()-before)+" HP.");}
                        else var3.add(var4.name()+" usou "+var6.displayName()+" em "+ally.name()+". "+SupportEffects.apply(var6.id(),var4,ally));
                     }
                     break;
                  }

                  var targets=var6.target()==SkillTarget.ALL_ENEMIES?enemiesOf(var4):List.of(var7);
                  for(var victim:targets){
                     DamageOutcome hit=damage(var4,victim,var6);
                     weaknessHit|=isWeaknessHit(hit);
                     var5|=this.appendOutcome(var3,var4,victim,var6,hit);
                     String status=var6.element()==Element.ICE?"FREEZE":var6.element()==Element.ELECTRIC?"SHOCK":"";
                     if(!status.isEmpty()&&victim.alive()&&!hit.missed()&&!hit.drained()&&!hit.reflected()&&hit.damage()>0&&random.nextInt(100)<(10+var4.equipment().ailmentBoost(status))/(victim.equipment().resistant(status)?2:1)){victim.afflict(status);if(!victim.ailment().isEmpty())var3.add(victim.name()+": "+victim.ailment());}
                  }
            }

            if(var2.type()==BattleActionType.SKILL){var used=PersonaRegistry.skill(var2.skillId()).orElse(null);if(used!=null&&used.isDamageSkill())var4.useCharge(used.usesMagicStat());}
            prepared=null;
            var4.setBatonBoost(false);
            this.updateState();
            if (this.state == BattleState.ACTIVE && !var5) {
               this.advanceTurn();
            } else if (this.state == BattleState.ACTIVE && var5) {
               bonusActor=var4.id();batonChain.add(var4.id());tacticalKind="1 MORE";tacticalEvent++;
               var3.add("ONE MORE! " + var4.name() + " acts again.");
            }

            this.updateState();
            return new BattleResolution(this.state, this.state==BattleState.ACTIVE&&var5, List.copyOf(var3), weaknessHit);
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

   public BattleCombatant validSkillTarget(BattleCombatant actor,UUID id,SkillDefinition skill) {
      if(skill.target()==SkillTarget.SELF)return id==null||id.equals(actor.id())?actor:null;
      if(skill.target()==SkillTarget.ONE_ALLY||skill.target()==SkillTarget.ALL_ALLIES)
         return id==null?actor:find(id).filter(BattleCombatant::alive).filter(t->t.side()==actor.side()).orElse(null);
      return validEnemyTarget(actor,id);
   }

   /** Join at the end of the existing order; never steal the active or reserved turn. */
   public boolean join(BattleCombatant ally) {
      if(state!=BattleState.ACTIVE||ally==null||!ally.alive()||ally.side()!=BattleSide.PLAYERS||find(ally.id()).isPresent())return false;
      // Allies joining while the opening player phase is running share its advantage.
      if(openingRound&&opening==EncounterOpening.ADVANTAGE&&turnOrder.get(turnIndex).side()==BattleSide.PLAYERS){int at=turnIndex+1;while(at<turnOrder.size()&&turnOrder.get(at).side()==BattleSide.PLAYERS)at++;turnOrder.add(at,ally);}else turnOrder.add(ally);return true;
   }

   public boolean leave(UUID id) {
      int index=-1;for(int i=0;i<turnOrder.size();i++)if(turnOrder.get(i).id().equals(id)){index=i;break;}
      if(index<0||turnOrder.get(index).side()!=BattleSide.PLAYERS)return false;
      boolean active=index==turnIndex;
      boolean bonusLeaving=id.equals(bonusActor);
      if(prepared!=null&&(prepared.actor().equals(id)||id.equals(prepared.action().targetId())))prepared=null;
      turnOrder.remove(index);if(index<turnIndex)turnIndex--;
      if(active&&bonusActor!=null&&!bonusLeaving)turnIndex=Math.floorMod(turnIndex-1,Math.max(1,turnOrder.size()));
      if(bonusLeaving){bonusActor=null;batonChain.clear();}
      updateState();
      if(state==BattleState.ACTIVE&&bonusLeaving&&!active)advanceTurn();
      else if(state==BattleState.ACTIVE&&active&&bonusActor==null){turnIndex=firstLivingIndex(turnIndex);if(!beginCurrentTurn())advanceTurn();}
      return true;
   }

   public List<BattleCombatant> alliesOf(BattleCombatant actor) {
      return turnOrder.stream().filter(BattleCombatant::alive).filter(c->c.side()==actor.side()).toList();
   }

   private BattleCombatant validEnemyTarget(BattleCombatant var1, UUID var2) {
      return var2 == null
         ? this.enemiesOf(var1).stream().findFirst().orElse(null)
         : this.find(var2).filter(BattleCombatant::alive).filter(var1x -> var1x.side() != var1.side()).orElse(null);
   }

   private void advanceTurn() {
      bonusActor=null;batonChain.clear();for(var actor:turnOrder)actor.setBatonBoost(false);
      if(state!=BattleState.ACTIVE)return;
      for(int attempts=0;attempts<=turnOrder.size();attempts++){
         int next=firstLivingIndex(turnIndex+1);
         if(openingRound&&next<=turnIndex){openingRound=false;agilityOrder();next=firstLivingIndex(0);}
         turnIndex=next;if(beginCurrentTurn())return;
      }
   }

   private boolean beginCurrentTurn(){var actor=currentActor();actor.beginTurn();boolean skip=actor.skipsNextTurn()||actor.hasAilment("FREEZE,SHOCK,FEAR,CHARM,DIZZY");actor.setDowned(false);return !skip;}

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
