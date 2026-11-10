package com.gx.velveteclipse.core.fusion;
import com.gx.velveteclipse.forge.PlayerPersonaData;
import com.gx.velveteclipse.core.persona.*;
import java.util.*;
/** No mutation until commit. Existing result progress is preserved instead of overwritten. */
public final class FusionTransaction {
 public record Preview(String first,String second,PersonaInstance result,int requiredLevel,boolean allowed,boolean merging,String message){}
 public static Preview preview(PlayerPersonaData data,String a,String b,String skill){
  var recipe=FusionService.preview(data.instance(a),data.instance(b)).orElse(null);if(recipe==null)return null;
  if(skill!=null&&!skill.isBlank()&&!recipe.inheritableSkills().contains(skill))return null;
  var def=PersonaRegistry.persona(recipe.personaId()).orElseThrow();var old=data.rewardInstance(def.id());
  var result=old==null?new PersonaInstance(def):old;
  String inherited=skill==null||skill.isBlank()?recipe.inheritableSkills().stream().findFirst().orElse(null):skill;
  if(inherited!=null)result.inheritSkill(inherited);
  int required=result.level();boolean allowed=data.playerLevel()>=required;
  int bonus=FusionArcanaBonus.apply(data,result);
  String message=allowed?(old==null?"As duas Personas serão consumidas.":"Resultado já possuído: nível, EXP e habilidades serão preservados. As duas fontes serão consumidas."):"Requer nível de jogador "+required+". Seu nível: "+data.playerLevel()+".";
  if(!SupremeFusion.unlocked(data,def.id())){allowed=false;message=SupremeFusion.lockedMessage(def.id());}
  return new Preview(a,b,result,required,allowed,old!=null,allowed?FusionArcanaBonus.describe(data,result,bonus)+" "+message:message);
 }
 public static boolean commit(PlayerPersonaData data,String a,String b,String skill){var p=preview(data,a,b,skill);return p!=null&&p.allowed()&&data.exchangeFusion(List.of(a,b),p.result());}
 public static String fingerprint(PlayerPersonaData data){
  var rows=data.rosterInstances().stream().map(p->p.definitionId()+":"+p.level()+":"+p.experience()+":"+String.join(",",new TreeSet<>(p.learnedSkills()))).sorted().toList();
  return data.playerLevel()+"/"+data.activePersona()+"/"+String.join(";",rows)+"/"+data.fusionRankFingerprint();
 }
}
