package com.gx.velveteclipse.forge;

import com.gx.velveteclipse.core.persona.PersonaDefinition;
import com.gx.velveteclipse.core.persona.PersonaInstance;
import com.gx.velveteclipse.core.persona.PersonaRegistry;
import com.gx.velveteclipse.util.RuntimeBridge;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class PlayerPersonaData {
   private static final String DATA_KEY = "velvet_eclipse:data_v1";
   private int maxHp = 120;
   private int hp = 120;
   private int maxSp = 45;
   private int sp = 45;
   private int pulseTonics = 3;
   private int playerLevel=1, playerExperience;
   private long yen;
   private int personaCapacity=2;
   // Only migration/switching old overflow uses this reserve. New acquisitions cannot bypass the cap.
   private final LinkedHashMap<String,PersonaInstance> reserve=new LinkedHashMap<>();
   private final LinkedHashMap<String,PersonaInstance> compendium=new LinkedHashMap<>();
   public Collection<PersonaInstance> compendiumInstances(){registerOwned();return compendium.values().stream().map(PlayerPersonaData::copy).toList();}
   public PersonaInstance registeredPersona(String id){registerOwned();var p=compendium.get(id);return p==null?null:copy(p);}
   public static long summonPrice(PersonaInstance p){return 200L+40L*p.level()*p.level()+50L*p.learnedSkills().size();}
   public boolean summon(String id){var p=registeredPersona(id);if(p==null||roster.containsKey(id)||reserve.containsKey(id)||!canReceivePersona(id)||yen<summonPrice(p))return false;yen-=summonPrice(p);return givePersonaInstance(p);}
   private void register(PersonaInstance p){
      var old=compendium.get(p.definitionId());if(old==null){compendium.put(p.definitionId(),copy(p));return;}
      var skills=new LinkedHashSet<>(old.learnedSkills());skills.addAll(p.learnedSkills());
      int level=Math.max(old.level(),p.level()),xp=old.level()>p.level()?old.experience():p.level()>old.level()?p.experience():Math.max(old.experience(),p.experience());
      compendium.put(p.definitionId(),new PersonaInstance(p.definitionId(),level,xp,skills));
   }
   private void registerOwned(){roster.values().forEach(this::register);reserve.values().forEach(this::register);}
   public boolean canExchange(Collection<String> consumed,PersonaInstance result){
      if(new java.util.HashSet<>(consumed).size()!=consumed.size()||!roster.keySet().containsAll(consumed))return false;
      var left=new java.util.HashSet<>(roster.keySet());left.removeAll(consumed);if(result!=null)left.add(result.definitionId());
      return !left.isEmpty()&&left.size()<=personaCapacity&&(result==null||PersonaRegistry.persona(result.definitionId()).isPresent());
   }
   public boolean exchangeFusion(Collection<String> consumed,PersonaInstance result){
      if(!canExchange(consumed,result))return false;registerOwned();consumed.forEach(roster::remove);
      if(result!=null){roster.put(result.definitionId(),copy(result));reserve.remove(result.definitionId());register(result);}
      if(!roster.containsKey(activePersona))activePersona=result==null?roster.keySet().iterator().next():result.definitionId();return true;
   }
   public int personaCapacity(){return personaCapacity;}
   public Set<String> reserveIds(){return Set.copyOf(reserve.keySet());}
   public Collection<PersonaInstance> reserveInstances(){return reserve.values().stream().map(PlayerPersonaData::copy).toList();}
   public boolean canReceivePersona(String id){return roster.containsKey(id)||roster.size()<personaCapacity;}
   public boolean expandPersonaCapacity(){if(personaCapacity>=8)return false;personaCapacity++;return true;}
   private void enforceCapacity(){
      for(String id:new ArrayList<>(roster.keySet()))if(roster.size()>personaCapacity&&!id.equals(activePersona))reserve.put(id,roster.remove(id));
      reserve.keySet().removeAll(roster.keySet());
   }
   private com.gx.velveteclipse.equipment.EquipmentBonuses equipment=com.gx.velveteclipse.equipment.EquipmentBonuses.NONE;
   public boolean spendYen(long price){if(price<0||yen<price)return false;yen-=price;return true;}
   public long yen(){return yen;}
   public void addYen(long amount){if(amount>0)yen=Math.min(9_999_999_999L,yen+Math.min(amount,9_999_999_999L));}
   public int playerLevel(){return playerLevel;}
   public int playerExperience(){return playerExperience;}
   public void setPlayerLevel(int level){if(level<1||level>99)throw new IllegalArgumentException("Player level must be 1..99");playerLevel=level;playerExperience=0;}
   public boolean setPersonaLevel(String id,int level){
      var instance=roster.get(id);if(instance==null)return false;
      instance.setLevel(PersonaRegistry.persona(id).orElseThrow(),level);return true;
   }
   public int playerNextExperience(){return PersonaInstance.experienceForNextLevel(playerLevel);}
   public void addPlayerExperience(int amount){
      if(amount<=0||playerLevel>=99)return;
      long xp=(long)playerExperience+amount;
      while(playerLevel<99&&xp>=playerNextExperience()){xp-=playerNextExperience();playerLevel++;}
      playerExperience=playerLevel==99?0:(int)xp;
   }
   private String activePersona = "";
   private boolean awakened;
   public boolean hasPersona(){return !roster.isEmpty();}
   public boolean awakened(){return awakened;}
   public boolean completeAwakening(String id){
      if(awakened||hasPersona()||!com.gx.velveteclipse.awakening.AwakeningTimeline.STARTERS.contains(id))return false;
      if(!givePersona(id))return false;activePersona=id;awakened=true;return true;
   }
   private final LinkedHashMap<String, PersonaInstance> roster = new LinkedHashMap<>();

   public PlayerPersonaData() {
      PersonaRegistry.bootstrap();

   }

   public int maxHp() {
      return this.maxHp+equipment.hp();
   }

   public int hp() {
      return this.hp;
   }

   public int maxSp() {
      return this.maxSp+equipment.sp();
   }

   public int sp() {
      return this.sp;
   }

   public int pulseTonics() {
      return this.pulseTonics;
   }

   public String activePersona() {
      return this.activePersona;
   }

   public Set<String> rosterIds() {
      return Set.copyOf(this.roster.keySet());
   }

   public Collection<PersonaInstance> rosterInstances() {
      return List.copyOf(this.roster.values());
   }

   public boolean givePersona(String var1) {
      PersonaDefinition var2 = PersonaRegistry.persona(var1).orElse(null);
      if (var2 != null && !this.roster.containsKey(var1) && canReceivePersona(var1) && !reserve.containsKey(var1)) {
         this.roster.put(var1, new PersonaInstance(var2));
         if(activePersona.isBlank())activePersona=var1;
         register(this.roster.get(var1));
         return true;
      } else {
         return false;
      }
   }

   public boolean givePersonaInstance(PersonaInstance var1) {
      if (var1 != null && !PersonaRegistry.persona(var1.definitionId()).isEmpty() && canReceivePersona(var1.definitionId())) {
         reserve.remove(var1.definitionId());
         this.roster.put(var1.definitionId(), copy(var1));
         if(activePersona.isBlank())activePersona=var1.definitionId();
         register(var1);
         return true;
      } else {
         return false;
      }
   }

   public boolean removePersona(String var1) {
      if (var1 != null && this.roster.size() > 1 && this.roster.containsKey(var1)) {
         registerOwned();
         this.roster.remove(var1);
         if (var1.equals(this.activePersona)) {
            this.activePersona = this.roster.keySet().iterator().next();
         }

         return true;
      } else {
         return false;
      }
   }

   public boolean replaceFusion(String var1, String var2, PersonaInstance var3) {
      if (var1 == null || var2 == null || var3 == null) {
         return false;
      } else if (!var1.equals(var2) && this.roster.containsKey(var1) && this.roster.containsKey(var2)
           && !var3.definitionId().equals(var1) && !var3.definitionId().equals(var2) && var3.level()<=playerLevel
           && PersonaRegistry.persona(var3.definitionId()).isPresent()) {
         boolean var4 = this.activePersona.equals(var1) || this.activePersona.equals(var2);
         registerOwned();register(var3);
         this.roster.remove(var1);
         this.roster.remove(var2);
         this.roster.put(var3.definitionId(), copy(var3));
         reserve.remove(var3.definitionId());
         if (var4 || !this.roster.containsKey(this.activePersona)) {
            this.activePersona = var3.definitionId();
         }

         return true;
      } else {
         return false;
      }
   }

   public boolean equipPersona(String var1) {
      if(reserve.containsKey(var1)){
         var selected=reserve.remove(var1);
         if(roster.size()>=personaCapacity)reserve.put(activePersona,roster.remove(activePersona));
         roster.put(var1,selected);
      }
      if (!this.roster.containsKey(var1)) {
         return false;
      } else {
         this.activePersona = var1;
         return true;
      }
   }

   public PersonaInstance activeInstance() {
      PersonaInstance var1 = this.roster.get(this.activePersona);
      return var1==null?null:copy(var1);
   }

   public PersonaInstance instance(String var1) {
      PersonaInstance var2 = this.roster.get(var1);
      return var2 == null ? null : copy(var2);
   }
   public PersonaInstance rewardInstance(String id){var held=roster.get(id);if(held==null)held=reserve.get(id);return held==null?null:copy(held);}

   public void updatePersona(PersonaInstance var1) {
      if (var1 != null && this.roster.containsKey(var1.definitionId())) {
         this.roster.put(var1.definitionId(), copy(var1));
      }
   }

   public void setResources(int var1, int var2) {
      this.hp = clamp(var1, 0, maxHp());
      this.sp = clamp(var2, 0, maxSp());
   }

   public void restore() {
      this.hp = maxHp();
      this.sp = maxSp();
   }

   public boolean consumePulseTonic() {
      if (this.pulseTonics <= 0) {
         return false;
      } else {
         this.pulseTonics--;
         return true;
      }
   }

   public void addPulseTonics(int var1) {
      this.pulseTonics = Math.max(0, this.pulseTonics + var1);
   }

   public static PlayerPersonaData load(Object p){var d=loadBase(p);d.equipment=com.gx.velveteclipse.equipment.EquipmentData.load(p).bonuses();d.setResources(d.hp,d.sp);d.registerOwned();return d;}
   private static PlayerPersonaData loadBase(Object var0) {
      PlayerPersonaData var1 = new PlayerPersonaData();
      Object var2 = RuntimeBridge.persistentData(var0);
      String var3 = RuntimeBridge.tagGetString(var2, "velvet_eclipse:data_v1");
      if (var3 != null && !var3.isBlank()) {
         try {
            String[] var4 = var3.split("\\|", -1);
            if (var4.length >= 8 && "V1".equals(var4[0])) {
               var1.hp = Integer.parseInt(var4[1]);
               var1.maxHp = Math.max(1, Integer.parseInt(var4[2]));
               var1.sp = Integer.parseInt(var4[3]);
               var1.maxSp = Math.max(0, Integer.parseInt(var4[4]));
               var1.activePersona = var4[5];
               var1.pulseTonics = Math.max(0, Integer.parseInt(var4[6]));
               if(var4.length>=10){var1.playerLevel=clamp(Integer.parseInt(var4[8]),1,99);var1.playerExperience=clamp(Integer.parseInt(var4[9]),0,var1.playerNextExperience()-1);}
               if(var4.length>=11)var1.yen=Math.max(0,Math.min(9_999_999_999L,Long.parseLong(var4[10])));
               if(var4.length>=12)var1.personaCapacity=clamp(Integer.parseInt(var4[11]),2,8);
               if(var4.length>=13&&!var4[12].isBlank())for(String row:var4[12].split(";")){var saved=decodePersona(row);if(saved!=null)var1.reserve.put(saved.definitionId(),saved);}
               if(var4.length>=14&&!var4[13].isBlank())for(String row:var4[13].split(";")){var saved=decodePersona(row);if(saved!=null)var1.compendium.put(saved.definitionId(),saved);}
               var1.roster.clear();
               if (!var4[7].isBlank()) {
                  for (String var8 : var4[7].split(";")) {
                     PersonaInstance var9 = decodePersona(var8);
                     if (var9 != null) {
                        var1.roster.put(var9.definitionId(), var9);
                     }
                  }
               }

               if (!var1.roster.containsKey(var1.activePersona)) {
                  var1.activePersona = var1.roster.isEmpty()?"":var1.roster.keySet().iterator().next();
               }
               var1.awakened=var4.length>=15?Boolean.parseBoolean(var4[14]):!var1.roster.isEmpty();
               var1.enforceCapacity();

               var1.hp = Math.max(0,var1.hp);
               var1.sp = Math.max(0,var1.sp);
               return var1;
            } else {
               return var1;
            }
         } catch (Throwable var10) {
            return new PlayerPersonaData();
         }
      } else {
         return var1;
      }
   }

   public void save(Object var1) {
      registerOwned();
      Object var2 = RuntimeBridge.persistentData(var1);
      RuntimeBridge.tagPutString(var2, "velvet_eclipse:data_v1", this.encode());
   }

   public static void copyPersisted(Object var0, Object var1) {
      BattleKnowledge.copy(var0,var1);
      com.gx.velveteclipse.equipment.EquipmentData.copy(var0,var1);
      Object var2 = RuntimeBridge.persistentData(var0);
      Object var3 = RuntimeBridge.persistentData(var1);
      String var4 = RuntimeBridge.tagGetString(var2, "velvet_eclipse:data_v1");
      if (var4 != null && !var4.isBlank()) {
         RuntimeBridge.tagPutString(var3, "velvet_eclipse:data_v1", var4);
      }
   }

   public String describeRoster() {
      ArrayList var1 = new ArrayList();

      for (PersonaInstance var3 : this.roster.values()) {
         String var4 = PersonaRegistry.persona(var3.definitionId()).map(PersonaDefinition::displayName).orElse(var3.definitionId());
         var1.add(var3.definitionId() + "(Lv" + var3.level() + ", " + var4 + ")");
      }

      return String.join(", ", var1);
   }

   private String encode() {
      registerOwned();
      ArrayList var1 = new ArrayList();

      for (PersonaInstance var3 : this.roster.values()) {
         var1.add(encodePersona(var3));
      }

      return "V1|"
         + this.hp
         + "|"
         + this.maxHp
         + "|"
         + this.sp
         + "|"
         + this.maxSp
         + "|"
         + this.activePersona
         + "|"
         + this.pulseTonics
         + "|"
         + String.join(";", var1) + "|" + playerLevel + "|" + playerExperience + "|" + yen + "|" + personaCapacity + "|" + reserve.values().stream().map(PlayerPersonaData::encodePersona).collect(java.util.stream.Collectors.joining(";")) + "|" + compendium.values().stream().map(PlayerPersonaData::encodePersona).collect(java.util.stream.Collectors.joining(";"))+"|"+awakened;
   }

   private static String encodePersona(PersonaInstance var0) {
      return var0.definitionId() + "," + var0.level() + "," + var0.experience() + "," + String.join("+", var0.learnedSkills());
   }

   private static PersonaInstance decodePersona(String var0) {
      try {
         String[] var1 = var0.split(",", 4);
         if (var1.length >= 3 && !PersonaRegistry.persona(var1[0]).isEmpty()) {
            LinkedHashSet var2 = new LinkedHashSet();
            if (var1.length >= 4 && !var1[3].isBlank()) {
               for (String var6 : var1[3].split("\\+")) {
                  if (PersonaRegistry.skill(var6).isPresent()) {
                     var2.add(var6);
                  }
               }
            }

            PersonaInstance var8 = new PersonaInstance(var1[0], Integer.parseInt(var1[1]), Integer.parseInt(var1[2]), var2);
            if (var8.learnedSkills().isEmpty()) {
               PersonaDefinition var9 = PersonaRegistry.persona(var1[0]).orElseThrow();
               return new PersonaInstance(var9);
            } else {
               return var8;
            }
         } else {
            return null;
         }
      } catch (Throwable var7) {
         return null;
      }
   }

   private static PersonaInstance copy(PersonaInstance var0) {
      return new PersonaInstance(var0.definitionId(), var0.level(), var0.experience(), var0.learnedSkills());
   }

   private static int clamp(int var0, int var1, int var2) {
      return Math.max(var1, Math.min(var2, var0));
   }
}
