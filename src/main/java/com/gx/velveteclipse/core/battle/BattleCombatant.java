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
   private com.gx.velveteclipse.shadow.EnemyDefinition enemy;
   public static BattleCombatant enemy(UUID id,String name,com.gx.velveteclipse.shadow.EnemyDefinition def,int level,int hp,int sp,java.util.Set<String> skills){
      var c=new BattleCombatant(id,name,BattleSide.ENEMIES,new PersonaInstance(def.id(),level,0,skills),hp,hp,sp,sp);c.enemy=def;return c;
   }
   public com.gx.velveteclipse.core.persona.Element attackElement(){return enemy==null?equipment().element():enemy.attack();}

   private com.gx.velveteclipse.equipment.EquipmentBonuses equipment=com.gx.velveteclipse.equipment.EquipmentBonuses.NONE;
   private String ailment="";private int ailmentTurns,openingTurns=3,partyEvasionTurns;private boolean endured,charged,concentrated;
   private com.gx.velveteclipse.equipment.EquipmentBonuses combined;
   public com.gx.velveteclipse.equipment.EquipmentBonuses equipment(){if(combined==null)combined=equipment.withSkills(persona.learnedSkills());return combined;}
   public void startPassives(){var b=equipment();if(b.has("auto tarukaja")||b.has("auto mataru"))attackStatus(true);if(b.has("auto rakukaja")||b.has("auto maraku"))defenseStatus(1,true);if(b.has("auto sukukaja")||b.has("auto masuku"))accuracyStatus(1,true);}
   private final java.util.EnumMap<com.gx.velveteclipse.core.persona.Element,Integer> breaks=new java.util.EnumMap<>(com.gx.velveteclipse.core.persona.Element.class);
   private boolean physicalWall,magicWall;private int criticalTurns,criticalBonus,foulTurns;
   public void chargeUp(boolean magic){if(magic)concentrated=true;else charged=true;}
   public void wall(boolean magic){if(magic)magicWall=true;else physicalWall=true;}
   public boolean reflects(com.gx.velveteclipse.core.persona.Element e){return com.gx.velveteclipse.equipment.EquipmentBonuses.physical(e)?physicalWall:e!=com.gx.velveteclipse.core.persona.Element.ALMIGHTY&&magicWall;}
   public void useWall(com.gx.velveteclipse.core.persona.Element e){if(com.gx.velveteclipse.equipment.EquipmentBonuses.physical(e))physicalWall=false;else magicWall=false;}
   public void breakElement(com.gx.velveteclipse.core.persona.Element e){breaks.put(e,4);}
   public void criticalBuff(int amount){criticalBonus=amount;criticalTurns=4;}
   public void foul(){foulTurns=4;}public double ailmentVulnerability(){return foulTurns>0?1.5:1;}
   public void clearStages(boolean buffs){if(attackTurns>0&&(attackStage>0)==buffs)attackTurns=0;if(defenseTurns>0&&(defenseStage>0)==buffs)defenseTurns=0;if(accuracyTurns>0&&(accuracyStage>0)==buffs)accuracyTurns=0;}
   public int drainSp(int amount){int n=Math.min(sp,Math.max(0,amount));sp-=n;return n;}
   public void sacrifice(){hp=1;sp=1;}
   public void sacrificeHp(){hp=1;}
   public void moonDay(long day){moonDay=day;}private long moonDay=14;public long moonDay(){return moonDay;}
   public double skillDamage(com.gx.velveteclipse.core.persona.SkillDefinition s){return (equipment().has(s.target()==com.gx.velveteclipse.core.persona.SkillTarget.ONE_ENEMY?"single-target boost":"multi-target boost")?1.15:1)*(equipment().has("raging tiger")&&hasAilment("RAGE")?1.5:1);}
   public boolean survivesInstant(com.gx.velveteclipse.core.persona.Element e,java.util.Random r){if(endured)return false;String key=e==com.gx.velveteclipse.core.persona.Element.LIGHT?"light":"dark";return equipment().has("endure "+key)||equipment().has("survival trick")||equipment().has("survive "+key)&&r.nextBoolean();}
   public void instantDamage(boolean survival){if(survival&&!endured){endured=true;hp=1;}else{hp=0;if(!endured&&(equipment().exact("endure")||equipment().has("enduring soul"))){endured=true;hp=equipment().has("enduring soul")?maxHp:1;}}}

   public BattleCombatant equipment(com.gx.velveteclipse.equipment.EquipmentBonuses b){equipment=b;combined=null;if(b.has("auto tarukaja")||b.has("auto mataru")||b.has("auto heat riser")||b.has("auto tarukabellion"))attackStatus(true);if(b.has("auto rakukaja")||b.has("auto maraku")||b.has("auto heat riser"))defenseStatus(1,true);charged=b.has("auto charge");concentrated=b.has("auto concentrate");if(b.has("orgia duration")){attackStatus(true);defenseStatus(1,true);attackTurns=defenseTurns=4;}return this;}
   public double extraEvasion(com.gx.velveteclipse.core.persona.Element e){
      if(partyEvasionTurns>0)return .2;
      if(openingTurns<=0)return 0;
      return equipment().has("auto sukukaja")||equipment().has("auto masuku")||equipment().has("auto heat riser")?.2:0;
   }
   public void applyPartyPassives(BattleCombatant other){if(equipment().has("auto mataru"))other.attackStatus(true);if(equipment().has("auto maraku"))other.defenseStatus(1,true);if(equipment().has("auto masuku"))other.partyEvasionTurns=3;}
   public int openingCritical(){return openingTurns>0&&(equipment().has("auto rebellion")||equipment().has("auto tarukabellion"))?12:0;}
   public boolean hasAilment(String kinds){return !ailment.isEmpty()&&(kinds.equals("ALL")||java.util.Arrays.asList(kinds.split(",")).contains(ailment));}
   public void cure(String kinds){if(hasAilment(kinds)){ailment="";ailmentTurns=0;}}
   public String ailment(){return ailment;}
   public void afflict(String kind){if(knockdownImmune||equipment().immune(kind))return;ailment=kind;ailmentTurns=3;}
   public void restoreSp(int n){sp=clamp(sp+Math.max(0,n),0,maxSp);}
   public double charge(boolean magic){return (magic?concentrated:charged)?2.3:1;}
   public void useCharge(boolean magic){if(magic)concentrated=false;else charged=false;}
   public int spCost(int n){return n>0&&equipment().has("spell master")?Math.max(1,n/2):n;}
   public int hpCost(int n){return n>0&&equipment().has("arms master")?Math.max(1,n/2):n;}
   private int maxHp;
   private int hp;
   private int maxSp;
   private int sp;
   private boolean guarding;
   private boolean downed;
   private boolean knockdownImmune;
   private com.gx.velveteclipse.core.persona.Element raidWeakness;
   public void setRaidBoss(com.gx.velveteclipse.core.persona.Element weak){knockdownImmune=true;raidWeakness=weak;setDowned(false);}
   public boolean knockdownImmune(){return knockdownImmune;}
   public com.gx.velveteclipse.core.persona.Affinity affinity(com.gx.velveteclipse.core.persona.Element e){
      var base=raidWeakness==null?equipment().affinity(e,enemy==null?personaDefinition().affinity(e):enemy.affinity(e)):e==raidWeakness?com.gx.velveteclipse.core.persona.Affinity.WEAK:com.gx.velveteclipse.core.persona.Affinity.NORMAL;
      String key=e==com.gx.velveteclipse.core.persona.Element.ELECTRIC?"electric":com.gx.velveteclipse.equipment.EquipmentBonuses.key(e);
      if(openingTurns>0&&equipment().has("anti-"+key+" master")&&(base==com.gx.velveteclipse.core.persona.Affinity.NORMAL||base==com.gx.velveteclipse.core.persona.Affinity.WEAK))base=com.gx.velveteclipse.core.persona.Affinity.RESIST;
      return breaks.containsKey(e)&&base!=com.gx.velveteclipse.core.persona.Affinity.WEAK?com.gx.velveteclipse.core.persona.Affinity.NORMAL:base;
   }
   private com.gx.velveteclipse.core.persona.Element downElement;
   private boolean skipNextTurn;
   private double batonMultiplier=1;
   public double batonMultiplier(){return batonMultiplier;}
   public void setBatonBoost(boolean active){batonMultiplier=active?(equipment().has("shift amp")?2:equipment().has("shift boost")?1.75:1.5):1;if(active){if(equipment().has("soul link"))restoreSp(8);if(equipment().has("soul chain"))restoreSp(5);}}
   public boolean skipsNextTurn(){return skipNextTurn;}
   public com.gx.velveteclipse.core.persona.Element downElement(){return downElement;}
   /** Only a repeated hit of the SAME weakness, before recovery, causes a lost turn. */
   public boolean knockDown(com.gx.velveteclipse.core.persona.Element element,boolean weakness){
      if(knockdownImmune)return false;
      boolean fresh=!downed;
      if(weakness){if(downed&&element==downElement)skipNextTurn=true;else if(fresh||downElement==null)downElement=element;}
      downed=true;return fresh;
   }
   private int attackTurns, attackStage=1, defenseTurns, defenseStage, accuracyTurns, accuracyStage;
   private boolean accuracyWaiting, ambushed;
   public void ambushed(boolean value){ambushed=value;}
   public int passiveCritical(){return (ambushed&&persona.learnedSkills().contains("adverse_resolve")?20:0)+(criticalTurns>0?criticalBonus:0);}
   public double accuracyMultiplier(){return accuracyTurns>0?(accuracyStage<0?.75:1.25):1;}
   public int accuracyTurns(){return accuracyTurns;}
   public void accuracyStatus(int stage,boolean waiting){
      if(accuracyTurns>0&&accuracyStage!=stage){accuracyTurns=0;accuracyStage=0;accuracyWaiting=false;}
      else {accuracyTurns=3;accuracyStage=stage;accuracyWaiting=waiting;}
   }
   private boolean attackWaiting, defenseWaiting;

   public double attackMultiplier(){return (attackTurns>0?(attackStage>0?1.33:.66):1)*(hasAilment("RAGE")?(equipment().has("attack when enraged")?2.5:1.5):1);}
   public double defenseMultiplier(){return (defenseTurns>0?(defenseStage>0?1.33:.66):1)*equipment().armor()*(hasAilment("DISTRESS")?.8:1);}
   public int attackTurns(){return attackTurns;}
   public int defenseTurns(){return defenseTurns;}
   public void attackStatus(boolean waiting){attackStatus(1,waiting);}
   public void attackStatus(int stage,boolean waiting){
      if(attackTurns>0&&attackStage!=stage){attackTurns=0;attackStage=0;attackWaiting=false;}
      else {attackStage=stage;attackTurns=3;attackWaiting=waiting;}
   }
   public void defenseStatus(int stage,boolean waiting){
      // Opposing effects cancel; repeated effects refresh duration without stacking.
      if(defenseTurns>0&&defenseStage!=stage){defenseTurns=0;defenseStage=0;defenseWaiting=false;}
      else {defenseStage=stage;defenseTurns=3;defenseWaiting=waiting;}
   }
   public void beginTurn(){breaks.replaceAll((e,n)->n-1);breaks.values().removeIf(n->n<=0);if(criticalTurns>0)criticalTurns--;if(foulTurns>0)foulTurns--;
      if(alive()){heal(maxHp*equipment().regen()/100);restoreSp(equipment().invigorate());}
      if(equipment().has("insta-heal"))cure("ALL");
      if(hasAilment("POISON"))hp=Math.max(1,hp-Math.max(1,maxHp/12));
      if(openingTurns>0)openingTurns--;if(partyEvasionTurns>0)partyEvasionTurns--;
      if(ailmentTurns>0){ailmentTurns-=equipment().has("fast heal")||equipment().has("faster overheat recovery")?2:1;if(ailmentTurns<=0)cure("ALL");}
      if(attackWaiting)attackWaiting=false;else if(attackTurns>0)--attackTurns;
      if(accuracyWaiting)accuracyWaiting=false;else if(accuracyTurns>0)--accuracyTurns;
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
         this.persona = var1;combined=null;
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
      PersonaStats var1 = enemy==null?this.personaDefinition().baseStats():enemy.stats();
      int var2 = Math.max(0, this.persona.level() - (enemy==null?this.personaDefinition().baseLevel():enemy.level()));
      return equipment().stats(new PersonaStats(
         var1.strength() + var2 / 2, var1.magic() + var2 / 2, var1.endurance() + var2 / 2, var1.agility() + var2 / 3, var1.luck() + var2 / 3
      ));
   }

   public void elementalDamage(int n,com.gx.velveteclipse.core.persona.Element e){
      damage(n);
   }
   public void damage(int var1) {
      int var2 = Math.max(0, var1);
      if(var2>0)cure("SLEEP");
      if (this.guarding && var2>0) {
         var2 = Math.max(1, var2 / 2);
      }

      if(var2>0&&equipment().has("firm stance"))var2=Math.max(1,var2/2);
      this.hp = clamp(this.hp - var2, 0, this.maxHp);
      if(hp==0&&!endured&&(equipment().exact("endure")||equipment().has("enduring soul"))){endured=true;hp=equipment().has("enduring soul")?maxHp:1;}
   }

   public void heal(int var1) {
      this.hp = clamp(this.hp + Math.max(0, var1), 0, this.maxHp);
   }

   public boolean spendSp(int var1) {
      var1=spCost(var1);
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
         int var2 = Math.max(1, this.maxHp * hpCost(var1) / 100);
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
      this.downed = var1&&!knockdownImmune;
      if(!var1){downElement=null;skipNextTurn=false;}
   }

   private static int clamp(int var0, int var1, int var2) {
      return Math.max(var1, Math.min(var2, var0));
   }
}
