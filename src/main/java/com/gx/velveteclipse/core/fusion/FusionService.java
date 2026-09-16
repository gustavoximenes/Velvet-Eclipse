package com.gx.velveteclipse.core.fusion;
import com.gx.velveteclipse.core.persona.*;
import java.util.*;
/** Stable, commutative recipes for the current catalog, including equal arcanas. */
public final class FusionService {
 private static final Map<Set<Arcana>,Arcana> RULES=Map.of(
  Set.of(Arcana.MAGICIAN,Arcana.LOVERS),Arcana.STAR,
  Set.of(Arcana.FOOL,Arcana.MAGICIAN),Arcana.STRENGTH,
  Set.of(Arcana.FOOL,Arcana.STRENGTH),Arcana.MOON,
  Set.of(Arcana.MOON,Arcana.STAR),Arcana.FOOL);
 public static Optional<FusionResult> preview(PersonaInstance a,PersonaInstance b){
  if(a==null||b==null||a.definitionId().equals(b.definitionId()))return Optional.empty();
  var da=PersonaRegistry.persona(a.definitionId()).orElse(null);var db=PersonaRegistry.persona(b.definitionId()).orElse(null);
  if(da==null||db==null)return Optional.empty();
  var pair=new HashSet<Arcana>();pair.add(da.arcana());pair.add(db.arcana());var arcana=RULES.get(pair);
  long target=((long)a.level()+b.level())/2+1;
  var result=PersonaRegistry.personas().values().stream().filter(d->!d.id().equals(da.id())&&!d.id().equals(db.id()))
   .min(Comparator.<PersonaDefinition>comparingInt(d->arcana!=null&&d.arcana()==arcana?0:1)
    .thenComparingLong(d->Math.abs(d.baseLevel()-target)).thenComparingInt(PersonaDefinition::baseLevel).thenComparing(PersonaDefinition::id)).orElse(null);
  if(result==null)return Optional.empty();
  var skills=new TreeSet<String>();skills.addAll(a.learnedSkills());skills.addAll(b.learnedSkills());result.learnset().values().forEach(skills::removeAll);
  return Optional.of(new FusionResult(result.id(),List.copyOf(skills)));
 }
}
