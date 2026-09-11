package com.gx.velveteclipse.menu;
import com.gx.velveteclipse.core.persona.*;
import com.gx.velveteclipse.forge.PlayerPersonaData;

/** Server-side resource rules, also exercised without starting a Minecraft client. */
public final class MenuActions {
    public record Result(String message,boolean changed){}
    public static Result apply(PlayerPersonaData data,String action,String argument,boolean battle){
        if(battle)return new Result("Aguarde o fim do combate.",false);
        if(action.equals("equip")){
            if(data.activePersona().equals(argument))return new Result("Persona ja equipada.",false);
            boolean ok=data.equipPersona(argument);return new Result(ok?"Persona equipada.":"Persona indisponivel.",ok);
        }
        if(action.equals("tonic")){
            if(data.hp()>=data.maxHp())return new Result("HP ja esta completo.",false);
            if(data.hp()<=0)return new Result("Nao e possivel usar agora.",false);
            if(!data.consumePulseTonic())return new Result("Sem Pulse Tonics.",false);
            data.setResources(data.hp()+Math.max(1,data.maxHp()*30/100),data.sp());return new Result("Pulse Tonic usado: +30% HP.",true);
        }
        if(action.equals("heal")){
            SkillDefinition skill=PersonaRegistry.skill(argument).orElse(null);
            if(skill==null||!data.activeInstance().learnedSkills().contains(argument)||skill.element()!=Element.HEALING||skill.hpPercentCost()>0)
                return new Result("Habilidade indisponivel fora do combate.",false);
            if(data.hp()<=0||data.hp()>=data.maxHp())return new Result("Nao ha HP para recuperar.",false);
            if(data.sp()<skill.spCost())return new Result("SP insuficiente.",false);
            data.setResources(data.hp()+Math.max(1,skill.power()),data.sp()-skill.spCost());return new Result("Cura aplicada.",true);
        }
        return new Result("",false);
    }
}
