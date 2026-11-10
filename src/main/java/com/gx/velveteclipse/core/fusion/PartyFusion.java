package com.gx.velveteclipse.core.fusion;
import com.gx.velveteclipse.core.persona.*;
import com.gx.velveteclipse.forge.PlayerPersonaData;
import java.util.*;

/** Pure transaction. Server validates consent, proximity and expiry separately. */
public final class PartyFusion {
 public record Source(UUID owner,String persona){}
 public record Preview(PersonaInstance result,List<String> inheritable,boolean allowed,String message){}
 public static Preview preview(Map<UUID,PlayerPersonaData> owners,UUID receiver,List<Source> sources,String special,List<String> skills){
  if(sources==null||sources.size()<2||sources.size()>6||skills==null||skills.size()>2||new HashSet<>(skills).size()!=skills.size()||!owners.containsKey(receiver))return null;
  var instances=new ArrayList<PersonaInstance>();for(var source:sources){var owner=owners.get(source.owner());if(owner==null)return null;var p=owner.instance(source.persona());if(p==null)return null;instances.add(p);}
  var recipe=FusionService.preview(instances,special).orElse(null);if(recipe==null)return null;
  var data=owners.get(receiver);var result=data.rewardInstance(recipe.personaId());if(result==null)result=new PersonaInstance(PersonaRegistry.persona(recipe.personaId()).orElseThrow());
  var available=new TreeSet<>(recipe.inheritableSkills());available.removeAll(result.learnedSkills());if(!available.containsAll(skills))return null;
  for(String id:skills)result.inheritSkill(id);
  boolean allowed=result.level()<=data.playerLevel();String message=allowed?"As Personas listadas serão consumidas. Até duas habilidades podem ser herdadas.":"Requer nível de jogador "+result.level()+".";
  int bonus=FusionArcanaBonus.apply(data,result);
  var groups=group(sources);groups.putIfAbsent(receiver,new ArrayList<>());
  for(var e:groups.entrySet())if(!owners.get(e.getKey()).canExchange(e.getValue(),e.getKey().equals(receiver)?result:null)){allowed=false;message="Falta espaço no destinatário ou a contribuição deixaria alguém sem Persona.";}
  if(!SupremeFusion.unlocked(data,result.definitionId())){allowed=false;message=SupremeFusion.lockedMessage(result.definitionId());}
  return new Preview(result,List.copyOf(available),allowed,allowed?FusionArcanaBonus.describe(data,result,bonus)+" "+message:message);
 }
 public static boolean commit(Map<UUID,PlayerPersonaData> owners,UUID receiver,List<Source> sources,String special,List<String> skills){
  var p=preview(owners,receiver,sources,special,skills);if(p==null||!p.allowed())return false;
  var groups=group(sources);groups.putIfAbsent(receiver,new ArrayList<>());
  for(var e:groups.entrySet())if(!owners.get(e.getKey()).exchangeFusion(e.getValue(),e.getKey().equals(receiver)?p.result():null))throw new IllegalStateException("Validated exchange failed");
  return true;
 }
 private static Map<UUID,List<String>> group(List<Source> sources){var m=new LinkedHashMap<UUID,List<String>>();for(var s:sources)m.computeIfAbsent(s.owner(),x->new ArrayList<>()).add(s.persona());return m;}
 private PartyFusion(){}
}
