package com.gx.velveteclipse.core.fusion;
import com.gx.velveteclipse.core.persona.*;
import java.util.*;
/** Reload arcana table; special results require their exact ingredients. */
public final class FusionService {
 private static final com.google.gson.JsonObject CHART=ReloadCatalogue.json("fusion-chart.json").getAsJsonObject();
 private static Arcana arcana(Arcana a,Arcana b){
  if(a==b)return a;var races=CHART.getAsJsonArray("races");int x=-1,y=-1;
  for(int i=0;i<races.size();i++){if(races.get(i).getAsString().equalsIgnoreCase(a.name()))x=i;if(races.get(i).getAsString().equalsIgnoreCase(b.name()))y=i;}
  if(x<0||y<0)return Arcana.FOOL;String result=CHART.getAsJsonArray("table").get(Math.max(x,y)).getAsJsonArray().get(Math.min(x,y)).getAsString();
  return result.equals("-")?null:Arcana.valueOf(result.toUpperCase(Locale.ROOT));
 }
 public static Optional<FusionResult> preview(PersonaInstance a,PersonaInstance b){return preview(Arrays.asList(a,b),"");}
 public static Optional<FusionResult> preview(List<PersonaInstance> sources,String special){
  if(sources==null||sources.size()<2||sources.size()>6||sources.stream().anyMatch(Objects::isNull))return Optional.empty();
  var ids=new HashSet<String>();for(var p:sources)if(!ids.add(p.definitionId())||PersonaRegistry.persona(p.definitionId()).isEmpty())return Optional.empty();
  PersonaDefinition result=null;
  if(special!=null&&!special.isBlank()){
   var recipe=ReloadCatalogue.special().get(special);if(recipe==null||recipe.size()!=ids.size()||!ids.equals(new HashSet<>(recipe)))return Optional.empty();result=PersonaRegistry.persona(special).orElse(null);
  }else{
   for(var entry:ReloadCatalogue.special().entrySet())if(entry.getValue().size()==ids.size()&&ids.equals(new HashSet<>(entry.getValue())))result=PersonaRegistry.persona(entry.getKey()).orElse(null);
   if(result==null){
    if(sources.size()!=2)return Optional.empty();var a=PersonaRegistry.persona(sources.get(0).definitionId()).orElseThrow();var b=PersonaRegistry.persona(sources.get(1).definitionId()).orElseThrow();var arcana=arcana(a.arcana(),b.arcana());if(arcana==null)return Optional.empty();
    int target=(sources.get(0).level()+sources.get(1).level())/2+1;
    var pool=PersonaRegistry.personas().values().stream().filter(d->ReloadCatalogue.ids().contains(d.id())&&d.arcana()==arcana&&!ids.contains(d.id())&&!ReloadCatalogue.special().containsKey(d.id())).sorted(Comparator.comparingInt(PersonaDefinition::baseLevel).thenComparing(PersonaDefinition::id)).toList();
    if(a.arcana()==b.arcana())result=pool.stream().filter(d->d.baseLevel()<target).reduce((x,y)->y).orElse(null);
    else result=pool.stream().filter(d->d.baseLevel()>=target).findFirst().orElse(pool.isEmpty()?null:pool.get(pool.size()-1));
   }
  }
  if(result==null)return Optional.empty();var skills=new TreeSet<String>();sources.forEach(p->skills.addAll(p.learnedSkills()));skills.removeAll(new PersonaInstance(result).learnedSkills());
  skills.removeIf(id->PersonaRegistry.skill(id).isEmpty());return Optional.of(new FusionResult(result.id(),List.copyOf(skills)));
 }
}
