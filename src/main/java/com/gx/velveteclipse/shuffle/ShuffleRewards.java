package com.gx.velveteclipse.shuffle;
import com.gx.velveteclipse.forge.PlayerPersonaData;
import com.gx.velveteclipse.core.persona.PersonaRegistry;
import java.util.function.Predicate;

/** Called on the server thread. Invalid/replayed offers never mutate player data. */
public final class ShuffleRewards {
 public record Result(ShuffleRules.Card card,String message){}
 public static Result claim(ShuffleData shuffle,PlayerPersonaData data,String token,int index,Predicate<String> deliverSkillCard){
  var round=shuffle.first();if(round==null||!round.token().equals(token)||index<0||index>=round.cards().size())return new Result(null,"Essa seleção não está mais disponível.");
  var card=round.cards().get(index);
  if(card.kind()==ShuffleRules.Kind.PERSONA&&!data.canReceivePersona(card.id()))return new Result(null,"Limite de "+data.personaCapacity()+" Personas atingido. Use um Contrato de Igor ou faça uma fusão. Sua seleção foi preservada.");
  if(card.kind()==ShuffleRules.Kind.SWORDS&&!deliverSkillCard.test(card.id()))return new Result(null,"Libere um espaço no inventário para receber o Skill Card.");
  switch(card.kind()){
   case PERSONA->data.givePersonaInstance(ShuffleRules.personaReward(card,data.rewardInstance(card.id())));
   case CUPS->data.setResources(data.hp()+Math.max(1,data.maxHp()*card.amount()/100),data.sp());
   case COINS->data.addYen(card.amount());
   case WANDS->{data.addPlayerExperience(card.amount());var persona=data.activeInstance();persona.addExperience(PersonaRegistry.persona(persona.definitionId()).orElseThrow(),card.amount());data.updatePersona(persona);}
   default->{}
  }
  shuffle.consume(token);return new Result(card,"Recompensa recebida!");
 }
 public static boolean teach(PlayerPersonaData data,String personaId,String skill){
  skill=PersonaRegistry.canonicalSkillId(skill);
  var persona=data.instance(personaId);if(persona==null||PersonaRegistry.skill(skill).isEmpty()||persona.learnedSkills().contains(skill))return false;
  persona.inheritSkill(skill);data.updatePersona(persona);return true;
 }
}
