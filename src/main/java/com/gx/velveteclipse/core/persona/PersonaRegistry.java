package com.gx.velveteclipse.core.persona;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class PersonaRegistry {
   private static final Map<String, SkillDefinition> SKILLS = new LinkedHashMap<>();
   private static final Map<String, PersonaDefinition> PERSONAS = new LinkedHashMap<>();
   private static boolean bootstrapped;

   private PersonaRegistry() {
   }

   public static synchronized void bootstrap() {
      if (!bootstrapped) {
         registerSkill(new SkillDefinition("single_shot","Single Shot",Element.PIERCE,48,95,0,8,true,SkillTarget.ONE_ENEMY));
         registerSkill(new SkillDefinition("raid_ice_storm","Tempestade Glacial",Element.ICE,40,100,10,0,false,SkillTarget.ALL_ENEMIES));
         registerSkill(new SkillDefinition("agi", "Agi", Element.FIRE, 44, 96, 3, 0, false, SkillTarget.ONE_ENEMY));
         registerSkill(new SkillDefinition("frost_shard", "Frost Shard", Element.ICE, 42, 96, 4, 0, false, SkillTarget.ONE_ENEMY));
         registerSkill(new SkillDefinition("volt_spark", "Volt Spark", Element.ELECTRIC, 43, 95, 4, 0, false, SkillTarget.ONE_ENEMY));
         registerSkill(new SkillDefinition("gale_cut", "Gale Cut", Element.WIND, 42, 98, 4, 0, false, SkillTarget.ONE_ENEMY));
         registerSkill(new SkillDefinition("cleave", "Cleave", Element.PHYSICAL, 52, 92, 0, 8, true, SkillTarget.ONE_ENEMY));
         registerSkill(new SkillDefinition("lumen_ray", "Lumen Ray", Element.LIGHT, 48, 95, 5, 0, false, SkillTarget.ONE_ENEMY));
         registerSkill(new SkillDefinition("shade_bite", "Shade Bite", Element.DARK, 48, 95, 5, 0, false, SkillTarget.ONE_ENEMY));
         registerSkill(new SkillDefinition("dia", "Dia", Element.HEALING, 50, 100, 3, 0, false, SkillTarget.ONE_ALLY));
         registerSkill(new SkillDefinition("media", "Media", Element.HEALING, 40, 100, 7, 0, false, SkillTarget.ALL_ALLIES));
         registerPersona(
            persona(
               "cinder_wisp",
               "Cinder Wisp",
               Arcana.MAGICIAN,
               1,
               new PersonaStats(2, 5, 3, 4, 3),
               Map.of(Element.FIRE, Affinity.RESIST, Element.ICE, Affinity.WEAK),
               Map.of(1, List.of("agi"), 4, List.of("cleave"))
            )
         );
         registerPersona(
            persona(
               "nocturne_knight",
               "Nocturne Knight",
               Arcana.FOOL,
               4,
               new PersonaStats(6, 3, 5, 4, 3),
               Map.of(Element.DARK, Affinity.RESIST, Element.LIGHT, Affinity.WEAK),
               Map.of(4, List.of("cleave", "shade_bite"))
            )
         );
         registerPersona(
            persona(
               "sylphid",
               "Sylphid",
               Arcana.LOVERS,
               3,
               new PersonaStats(2, 5, 3, 6, 4),
               Map.of(Element.WIND, Affinity.RESIST, Element.ELECTRIC, Affinity.WEAK),
               Map.of(3, List.of("gale_cut", "dia"), 6, List.of("media"))
            )
         );
         registerPersona(
            persona(
               "lumen_hare",
               "Lumen Hare",
               Arcana.STAR,
               5,
               new PersonaStats(3, 6, 3, 7, 6),
               Map.of(Element.LIGHT, Affinity.RESIST, Element.DARK, Affinity.WEAK),
               Map.of(5, List.of("lumen_ray", "gale_cut"))
            )
         );
         registerPersona(
            persona(
               "iron_oni",
               "Iron Oni",
               Arcana.STRENGTH,
               7,
               new PersonaStats(8, 2, 8, 3, 3),
               Map.of(Element.PHYSICAL, Affinity.RESIST, Element.PSY, Affinity.WEAK),
               Map.of(7, List.of("cleave"))
            )
         );
         registerPersona(
            persona(
               "umbra_slime",
               "Umbra Slime",
               Arcana.MOON,
               2,
               new PersonaStats(3, 3, 4, 2, 2),
               Map.of(Element.FIRE, Affinity.WEAK, Element.DARK, Affinity.RESIST),
               Map.of(2, List.of("shade_bite"))
            )
         );
         registerSkill(new SkillDefinition("zio","Zio",Element.ELECTRIC,43,96,4,0,false,SkillTarget.ONE_ENEMY));
         registerSkill(new SkillDefinition("mazio","Mazio",Element.ELECTRIC,43,95,10,0,false,SkillTarget.ALL_ENEMIES));
         registerSkill(new SkillDefinition("zionga","Zionga",Element.ELECTRIC,75,96,8,0,false,SkillTarget.ONE_ENEMY));
         registerSkill(new SkillDefinition("mazionga","Mazionga",Element.ELECTRIC,75,95,16,0,false,SkillTarget.ALL_ENEMIES));
         registerSkill(new SkillDefinition("ziodyne","Ziodyne",Element.ELECTRIC,115,96,12,0,false,SkillTarget.ONE_ENEMY));
         registerSkill(new SkillDefinition("maziodyne","Maziodyne",Element.ELECTRIC,115,95,24,0,false,SkillTarget.ALL_ENEMIES));
         // Persona 4's first acquisition. Keep the older Personas' Cleave balance intact.
         registerSkill(new SkillDefinition("izanagi_cleave","Cleave",Element.PHYSICAL,52,95,0,5,true,SkillTarget.ONE_ENEMY));
         registerSkill(new SkillDefinition("rakukaja","Rakukaja",Element.SUPPORT,0,100,12,0,false,SkillTarget.ONE_ALLY));
         registerSkill(new SkillDefinition("rakunda","Rakunda",Element.SUPPORT,0,100,12,0,false,SkillTarget.ONE_ENEMY));
         registerSkill(new SkillDefinition("tarukaja","Tarukaja",Element.SUPPORT,0,100,12,0,false,SkillTarget.ONE_ALLY));
         registerPersona(persona("izanagi","Izanagi",Arcana.FOOL,1,new PersonaStats(3,2,2,3,2),
             Map.of(Element.ELECTRIC,Affinity.RESIST,Element.WIND,Affinity.WEAK,Element.DARK,Affinity.NULL),
             Map.of(1,List.of("zio","izanagi_cleave","rakukaja"),3,List.of("rakunda"),5,List.of("tarukaja"))));
         // Royal learnset; the user-facing name Eiga is used for Arsène's initial curse spell.
         registerSkill(new SkillDefinition("eiga","Eiga",Element.DARK,44,96,4,0,false,SkillTarget.ONE_ENEMY));
         registerSkill(new SkillDefinition("arsene_cleave","Cleave",Element.SLASH,52,95,0,6,true,SkillTarget.ONE_ENEMY));
         registerSkill(new SkillDefinition("sukunda","Sukunda",Element.SUPPORT,0,100,8,0,false,SkillTarget.ONE_ENEMY));
         registerSkill(new SkillDefinition("dream_needle","Dream Needle",Element.PIERCE,48,90,0,8,true,SkillTarget.ONE_ENEMY));
         registerSkill(new SkillDefinition("adverse_resolve","Adverse Resolve",Element.SUPPORT,0,100,0,0,false,SkillTarget.SELF));
         registerSkill(new SkillDefinition("bash","Bash",Element.PHYSICAL,48,90,0,7,true,SkillTarget.ONE_ENEMY));
         registerSkill(new SkillDefinition("tarunda","Tarunda",Element.SUPPORT,0,100,8,0,false,SkillTarget.ONE_ENEMY));
         registerPersona(persona("arsene","Arsène",Arcana.FOOL,1,new PersonaStats(2,2,2,3,1),
             Map.of(Element.ICE,Affinity.WEAK,Element.LIGHT,Affinity.WEAK,Element.DARK,Affinity.RESIST),
             Map.of(1,List.of("eiga"),2,List.of("arsene_cleave"),4,List.of("sukunda"),5,List.of("dream_needle"),7,List.of("adverse_resolve"))));
         registerPersona(persona("orpheus","Orpheus",Arcana.FOOL,1,new PersonaStats(2,2,2,3,2),
             Map.of(Element.FIRE,Affinity.RESIST,Element.ELECTRIC,Affinity.WEAK,Element.DARK,Affinity.WEAK),
             Map.of(1,List.of("agi"),3,List.of("dia"),4,List.of("bash"),5,List.of("tarunda"))));
         ReloadCatalogue.register();
         bootstrapped = true;
      }
   }

   private static PersonaDefinition persona(
      String var0, String var1, Arcana var2, int var3, PersonaStats var4, Map<Element, Affinity> var5, Map<Integer, List<String>> var6
   ) {
      return new PersonaDefinition(var0, var1, var2, var3, var4, var5, var6);
   }

   public static void registerSkill(SkillDefinition var0) {
      SKILLS.put(var0.id(), var0);
   }

   public static void registerPersona(PersonaDefinition var0) {
      PERSONAS.put(var0.id(), var0);
   }

   static void registerCatalogueSkill(SkillDefinition skill){SKILLS.putIfAbsent(skill.id(),skill);}
   static void registerCataloguePersona(PersonaDefinition persona){PERSONAS.putIfAbsent(persona.id(),persona);}
   public static String canonicalSkillId(String id) { return switch(id){case "ember"->"agi";case "eiha"->"eiga";case "maeiha"->"maeiga";default->id;}; }

   public static Optional<SkillDefinition> skill(String var0) {
      bootstrap();
      return Optional.ofNullable(SKILLS.get(canonicalSkillId(var0)));
   }

   public static Optional<PersonaDefinition> persona(String var0) {
      bootstrap();
      return Optional.ofNullable(PERSONAS.get(var0));
   }

   public static Map<String, SkillDefinition> skills() {
      bootstrap();
      return Collections.unmodifiableMap(SKILLS);
   }

   public static Map<String, PersonaDefinition> personas() {
      bootstrap();
      return Collections.unmodifiableMap(PERSONAS);
   }
}
