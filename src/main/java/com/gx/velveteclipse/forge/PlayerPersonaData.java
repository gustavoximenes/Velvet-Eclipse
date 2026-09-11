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
   private String activePersona = "cinder_wisp";
   private final LinkedHashMap<String, PersonaInstance> roster = new LinkedHashMap<>();

   public PlayerPersonaData() {
      PersonaRegistry.bootstrap();
      PersonaDefinition var1 = PersonaRegistry.persona(this.activePersona).orElseThrow();
      this.roster.put(this.activePersona, new PersonaInstance(var1));
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
      if (var2 != null && !this.roster.containsKey(var1)) {
         this.roster.put(var1, new PersonaInstance(var2));
         return true;
      } else {
         return false;
      }
   }

   public boolean givePersonaInstance(PersonaInstance var1) {
      if (var1 != null && !PersonaRegistry.persona(var1.definitionId()).isEmpty()) {
         this.roster.put(var1.definitionId(), copy(var1));
         return true;
      } else {
         return false;
      }
   }

   public boolean removePersona(String var1) {
      if (var1 != null && this.roster.size() > 1 && this.roster.containsKey(var1)) {
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
      } else if (!var1.equals(var2) && this.roster.containsKey(var1) && this.roster.containsKey(var2)) {
         boolean var4 = this.activePersona.equals(var1) || this.activePersona.equals(var2);
         this.roster.remove(var1);
         this.roster.remove(var2);
         this.roster.put(var3.definitionId(), copy(var3));
         if (var4 || !this.roster.containsKey(this.activePersona)) {
            this.activePersona = var3.definitionId();
         }

         return true;
      } else {
         return false;
      }
   }

   public boolean equipPersona(String var1) {
      if (!this.roster.containsKey(var1)) {
         return false;
      } else {
         this.activePersona = var1;
         return true;
      }
   }

   public PersonaInstance activeInstance() {
      PersonaInstance var1 = this.roster.get(this.activePersona);
      if (var1 == null) {
         PersonaDefinition var2 = PersonaRegistry.persona("cinder_wisp").orElseThrow();
         var1 = new PersonaInstance(var2);
         this.roster.put(var2.id(), var1);
         this.activePersona = var2.id();
      }

      return copy(var1);
   }

   public PersonaInstance instance(String var1) {
      PersonaInstance var2 = this.roster.get(var1);
      return var2 == null ? null : copy(var2);
   }

   public void updatePersona(PersonaInstance var1) {
      if (var1 != null && this.roster.containsKey(var1.definitionId())) {
         this.roster.put(var1.definitionId(), copy(var1));
      }
   }

   public void setResources(int var1, int var2) {
      this.hp = clamp(var1, 0, this.maxHp);
      this.sp = clamp(var2, 0, this.maxSp);
   }

   public void restore() {
      this.hp = this.maxHp;
      this.sp = this.maxSp;
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

   public static PlayerPersonaData load(Object var0) {
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
               var1.roster.clear();
               if (!var4[7].isBlank()) {
                  for (String var8 : var4[7].split(";")) {
                     PersonaInstance var9 = decodePersona(var8);
                     if (var9 != null) {
                        var1.roster.put(var9.definitionId(), var9);
                     }
                  }
               }

               if (var1.roster.isEmpty()) {
                  PersonaDefinition var11 = PersonaRegistry.persona("cinder_wisp").orElseThrow();
                  var1.roster.put(var11.id(), new PersonaInstance(var11));
               }

               if (!var1.roster.containsKey(var1.activePersona)) {
                  var1.activePersona = var1.roster.keySet().iterator().next();
               }

               var1.hp = clamp(var1.hp, 0, var1.maxHp);
               var1.sp = clamp(var1.sp, 0, var1.maxSp);
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
      Object var2 = RuntimeBridge.persistentData(var1);
      RuntimeBridge.tagPutString(var2, "velvet_eclipse:data_v1", this.encode());
   }

   public static void copyPersisted(Object var0, Object var1) {
      BattleKnowledge.copy(var0,var1);
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
         + String.join(";", var1);
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
