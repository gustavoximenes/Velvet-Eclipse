package com.gx.velveteclipse.shuffle;
import com.gx.velveteclipse.core.persona.*;
import java.util.*;

/** Reward values and floor gates are shared by the server and the focused audit. */
public final class ShuffleRules {
 public enum Kind {PERSONA,CUPS,COINS,WANDS,SWORDS}
 public record Card(Kind kind,String id,int amount,int rank){}
 public record Round(String token,int floor,List<Card> cards){}
 public static int level(int floor){return Math.max(1,Math.min(99,floor));}
 public static int skillLevel(String id){
  return switch(id){case "mazio"->8;case "zionga"->20;case "mazionga"->32;case "ziodyne"->50;case "maziodyne"->65;
   default->PersonaRegistry.personas().values().stream().flatMap(p->p.learnset().entrySet().stream()).filter(e->e.getValue().contains(id)).mapToInt(Map.Entry::getKey).min().orElse(1);};
 }
 public static Round roll(int floor,Set<String> owned,Random random){
  floor=Math.max(1,Math.min(250,floor));int cap=level(floor),rank=Math.min(10,1+(floor-1)/25);
  var types=new ArrayList<>(List.of(Kind.values()));Collections.shuffle(types,random);int n=1+random.nextInt(5);var cards=new ArrayList<Card>();
  for(var type:types.subList(0,n))switch(type){
   case PERSONA->{var eligible=PersonaRegistry.personas().values().stream().filter(p->p.baseLevel()<=cap).sorted(Comparator.comparing(PersonaDefinition::id)).toList();var missing=eligible.stream().filter(p->!owned.contains(p.id())).toList();var pool=missing.isEmpty()?eligible:missing;var p=pool.get(random.nextInt(pool.size()));cards.add(new Card(type,p.id(),cap,rank));}
   case CUPS->cards.add(new Card(type,"",Math.min(60,20+floor/5),rank));
   case COINS->cards.add(new Card(type,"",100+floor*75,rank));
   case WANDS->cards.add(new Card(type,"",60+floor*20,rank));
   case SWORDS->{var pool=PersonaRegistry.skills().keySet().stream().filter(id->skillLevel(id)<=cap).sorted().toList();cards.add(new Card(type,pool.get(random.nextInt(pool.size())),1,rank));}
  }
  return new Round(UUID.randomUUID().toString(),floor,List.copyOf(cards));
 }
 public static PersonaInstance personaReward(Card card,PersonaInstance old){
  var definition=PersonaRegistry.persona(card.id()).orElseThrow();int level=Math.max(definition.baseLevel(),Math.max(card.amount(),old==null?0:old.level()));
  var skills=new LinkedHashSet<String>();definition.learnset().forEach((n,list)->{if(n<=level)skills.addAll(list);});if(old!=null)skills.addAll(old.learnedSkills());
  return new PersonaInstance(card.id(),level,old==null?0:old.experience(),skills);
 }
}
