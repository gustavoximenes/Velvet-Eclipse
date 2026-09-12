package com.gx.velveteclipse.client.menu;

import com.gx.velveteclipse.core.persona.*;
import com.gx.velveteclipse.menu.MenuSnapshot.Persona;
import java.util.*;

/** Presentation derived from the synced combat values, with no invented skill effects. */
public final class PersonaDetails {
    public static final Element[] ELEMENTS={Element.PHYSICAL,Element.FIRE,Element.ICE,Element.ELECTRIC,Element.WIND,
        Element.PSY,Element.NUCLEAR,Element.LIGHT,Element.DARK,Element.ALMIGHTY};
    public record Next(int level,SkillDefinition skill){}
    public static String asset(String id){return switch(id){case "cinder_wisp" -> "thanatos";case "umbra_slime" -> "wealth_hand";default -> id;};}
    public static String arcana(String value){return value.isEmpty()?"":value.substring(0,1)+value.substring(1).toLowerCase(Locale.ROOT).replace('_',' ');}
    public static long remainingXp(Persona p){return Math.max(0L,(long)p.nextXp()-p.xp());}
    public static Affinity affinity(Persona p,Element e){
        if(e==Element.ALMIGHTY)return Affinity.NORMAL;
        for(String value:p.affinities()){
            String[] parts=value.trim().split("\\s+");
            if(parts.length==2&&parts[0].equals(e.name()))try{return Affinity.valueOf(parts[1]);}catch(IllegalArgumentException ignored){}
        }
        return Affinity.NORMAL;
    }
    public static Optional<Next> next(Persona p){
        return PersonaRegistry.persona(p.id()).stream().flatMap(d->d.learnset().entrySet().stream())
            .filter(e->e.getKey()>p.level()).sorted(Map.Entry.comparingByKey())
            .flatMap(e->e.getValue().stream().filter(id->p.skills().stream().noneMatch(s->s.id().equals(id)))
                .flatMap(id->PersonaRegistry.skill(id).stream()).map(s->new Next(e.getKey(),s))).findFirst();
    }
    public static String cost(SkillDefinition s){return s.spCost()>0?s.spCost()+" SP":s.hpPercentCost()>0?s.hpPercentCost()+"% HP":"Sem custo";}
    public static String element(Element e){return switch(e){
        case SLASH -> "Corte";case PHYSICAL -> "Físico";case FIRE -> "Fogo";case ICE -> "Gelo";case ELECTRIC -> "Elétrico";
        case WIND -> "Vento";case PSY -> "Psíquico";case NUCLEAR -> "Nuclear";case LIGHT -> "Luz";
        case DARK -> "Trevas";case ALMIGHTY -> "Onipotente";case HEALING -> "Cura";case SUPPORT -> "Suporte";case AILMENT -> "Condição";};}
    public static String target(SkillTarget t){return switch(t){case ONE_ENEMY -> "um inimigo";case ALL_ENEMIES -> "todos os inimigos";
        case SELF -> "o usuário";case ONE_ALLY -> "um aliado";case ALL_ALLIES -> "todos os aliados";};}
    public static String description(SkillDefinition s){
        if(s.id().equals("rakukaja"))return "Aumenta a defesa de um aliado por 3 turnos.";
        if(s.id().equals("rakunda"))return "Reduz a defesa de um inimigo por 3 turnos.";
        if(s.id().equals("tarukaja"))return "Aumenta o ataque de um aliado por 3 turnos.";
        if(s.element()==Element.HEALING)return "Recupera "+s.power()+" HP de "+target(s.target())+".";
        if(!s.isDamageSkill())return "Habilidade de "+element(s.element()).toLowerCase(Locale.ROOT)+" para "+target(s.target())+".";
        return "Dano de "+element(s.element()).toLowerCase(Locale.ROOT)+" contra "+target(s.target())+". Usa "+(s.usesMagicStat()?"Magia (Ma)":"Força (St)")+"."
            +(s.canCritical()?" Pode causar crítico.":"")+(s.element()==Element.ALMIGHTY?" Ignora afinidades.":"");
    }
    public static String affinityName(Affinity a){return switch(a){case NORMAL -> "Normal";case WEAK -> "Fraqueza";case RESIST -> "Resistência";
        case NULL -> "Anula";case REPEL -> "Reflete";case DRAIN -> "Absorve";};}
    public static String affinityDescription(Affinity a){return switch(a){case NORMAL -> "Recebe dano normal deste elemento.";case WEAK -> "Recebe 1,5x o dano. O golpe pode derrubar a Persona.";
        case RESIST -> "Reduz o dano deste elemento pela metade.";case NULL -> "Anula o dano deste elemento.";
        case REPEL -> "Devolve o dano ao atacante.";case DRAIN -> "Converte o dano recebido em recuperação de HP.";};}
    public static int color(Element e){return switch(e){case PHYSICAL, SLASH -> 0xFFFFB12D;case FIRE -> 0xFFFF555A;case ICE -> 0xFF67E5FF;
        case ELECTRIC -> 0xFFFFFF32;case WIND -> 0xFF57FFA6;case PSY -> 0xFFFF80B9;case NUCLEAR -> 0xFF53CFF5;
        case LIGHT -> 0xFFE7FFFF;case DARK -> 0xFFB470F8;case ALMIGHTY -> 0xFFD9E2F4;case HEALING -> 0xFF56FFD4;
        case SUPPORT -> 0xFF82A2FF;case AILMENT -> 0xFFB2E454;};}
}
