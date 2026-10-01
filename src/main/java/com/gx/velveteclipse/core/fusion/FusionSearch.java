package com.gx.velveteclipse.core.fusion;

import com.gx.velveteclipse.core.persona.*;
import com.gx.velveteclipse.forge.PlayerPersonaData;
import java.util.*;

/** Enumerates recipes from consented stock without consuming inventory or creating tickets. */
public final class FusionSearch {
 public record Option(String id,String name,int level,String special,List<PartyFusion.Source> sources){}
 public static List<Option> available(Map<UUID,PlayerPersonaData> owners,UUID receiver,List<PartyFusion.Source> stock){
  if(!owners.containsKey(receiver))return List.of();
  var byId=new TreeMap<String,List<PartyFusion.Source>>();
  for(var source:new LinkedHashSet<>(stock))if(owners.containsKey(source.owner())&&owners.get(source.owner()).instance(source.persona())!=null)
   byId.computeIfAbsent(source.persona(),x->new ArrayList<>()).add(source);
  // Normal results depend on current ingredient levels, including party contributions.
  var levels=new TreeMap<String,List<List<PartyFusion.Source>>>();
  for(var entry:byId.entrySet()){
   entry.getValue().sort(Comparator.comparing((PartyFusion.Source s)->!s.owner().equals(receiver)).thenComparing(s->s.owner().toString()));
   var groups=new TreeMap<Integer,List<PartyFusion.Source>>();
   for(var s:entry.getValue())groups.computeIfAbsent(owners.get(s.owner()).instance(s.persona()).level(),x->new ArrayList<>()).add(s);
   levels.put(entry.getKey(),List.copyOf(groups.values()));
  }
  var ids=new ArrayList<>(byId.keySet());var out=new ArrayList<Option>();var seen=new HashSet<String>();
  for(int a=0;a<ids.size();a++)for(int b=a+1;b<ids.size();b++)
   for(var x:levels.get(ids.get(a)))for(var y:levels.get(ids.get(b)))add(out,seen,owners,receiver,List.of(x,y),"");
  for(var recipe:ReloadCatalogue.special().entrySet())if(byId.keySet().containsAll(recipe.getValue()))
   add(out,seen,owners,receiver,recipe.getValue().stream().map(byId::get).toList(),recipe.getKey());
  out.sort(Comparator.comparingInt(Option::level).thenComparing(Option::name).thenComparing(o->o.sources().toString()));return List.copyOf(out);
 }
 private static void add(List<Option> out,Set<String> seen,Map<UUID,PlayerPersonaData> owners,UUID receiver,List<List<PartyFusion.Source>> choices,String special){
  var recipe=FusionService.preview(choices.stream().map(c->owners.get(c.get(0).owner()).instance(c.get(0).persona())).toList(),special).orElse(null);
  if(recipe==null)return;
  String key=recipe.personaId()+choices.stream().map(c->c.get(0).persona()).sorted().toList();if(seen.contains(key))return;
  var data=owners.get(receiver);var result=data.rewardInstance(recipe.personaId());
  if(result==null)result=new PersonaInstance(PersonaRegistry.persona(recipe.personaId()).orElseThrow());
  if(result.level()>data.playerLevel())return;
  // A full recipient must contribute an input unless the result replaces an owned copy.
  boolean needsInput=!data.canReceivePersona(result.definitionId());
  List<PartyFusion.Source> source=null;
  if(needsInput){for(int i=0;i<choices.size()&&source==null;i++)if(choices.get(i).stream().anyMatch(s->s.owner().equals(receiver)))source=assign(owners,receiver,choices,i);}
  else source=assign(owners,receiver,choices,-1);
  if(source==null)return;
  var preview=PartyFusion.preview(owners,receiver,source,special,List.of());if(preview==null||!preview.allowed())return;
  var def=PersonaRegistry.persona(preview.result().definitionId()).orElseThrow();
  out.add(new Option(def.id(),def.displayName(),preview.result().level(),special,source));seen.add(key);
 }
 /** Capacity matching avoids exponential owner permutations for special recipes. */
 private static List<PartyFusion.Source> assign(Map<UUID,PlayerPersonaData> owners,UUID receiver,List<List<PartyFusion.Source>> choices,int locked){
  var assigned=new UUID[choices.size()];var used=new HashMap<UUID,List<Integer>>();
  if(locked>=0){assigned[locked]=receiver;used.computeIfAbsent(receiver,x->new ArrayList<>()).add(locked);}
  for(int i=0;i<choices.size();i++)if(i!=locked&&!augment(i,locked,choices,owners,receiver,assigned,used,new HashSet<>()))return null;
  var out=new ArrayList<PartyFusion.Source>();for(int i=0;i<assigned.length;i++)out.add(new PartyFusion.Source(assigned[i],choices.get(i).get(0).persona()));return List.copyOf(out);
 }
 private static boolean augment(int input,int locked,List<List<PartyFusion.Source>> choices,Map<UUID,PlayerPersonaData> owners,UUID receiver,UUID[] assigned,Map<UUID,List<Integer>> used,Set<UUID> visited){
  for(var source:choices.get(input)){
   UUID owner=source.owner();if(!visited.add(owner))continue;
   int capacity=owners.get(owner).rosterIds().size()-(owner.equals(receiver)?0:1);if(capacity<=0)continue;
   var slots=used.computeIfAbsent(owner,x->new ArrayList<>());
   if(slots.size()<capacity){slots.add(input);assigned[input]=owner;return true;}
   for(int index:new ArrayList<>(slots))if(index!=locked&&augment(index,locked,choices,owners,receiver,assigned,used,visited)){
    slots.remove(Integer.valueOf(index));slots.add(input);assigned[input]=owner;return true;
   }
  }return false;
 }
 private FusionSearch(){}
}
