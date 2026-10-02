package com.gx.velveteclipse.menu;
import com.gx.velveteclipse.core.persona.*;
import com.gx.velveteclipse.forge.PlayerPersonaData;
import com.gx.velveteclipse.equipment.*;

/** Source pays once; recipient receives recovery only after all conditions pass. */
public final class MenuRecovery {
    public static MenuActions.Result apply(PlayerPersonaData from,PlayerPersonaData to,EquipmentLoadout bag,String action,String id){
        if(from.hp()<=0||to.hp()<=0)return new MenuActions.Result("Não é possível usar agora.",false);
        int hp=0,sp=0,cost=0;
        if(action.equals("heal_target")){
            var skill=PersonaRegistry.skill(id).orElse(null);
            if(skill==null||!from.hasPersona()||!from.activeInstance().learnedSkills().contains(id)||skill.element()!=Element.HEALING||skill.hpPercentCost()>0)return new MenuActions.Result("Habilidade indisponível.",false);
            if(from.sp()<skill.spCost())return new MenuActions.Result("SP insuficiente.",false);
            hp=Math.max(1,skill.power());cost=skill.spCost();
        }else if(action.equals("tonic_target"))hp=Math.max(1,to.maxHp()*30/100);
        else if(action.equals("supply_target")){
            var supply=Supplies.get(id);if(supply==null||DamageItems.get(id)!=null||supply.hp()==0&&supply.sp()==0)return new MenuActions.Result("Use este item em combate.",false);
            hp=Supplies.amount(supply.hp(),to.maxHp());sp=Supplies.amount(supply.sp(),to.maxSp());
        }else return new MenuActions.Result("Ação indisponível.",false);
        if((hp==0||to.hp()>=to.maxHp())&&(sp==0||to.sp()>=to.maxSp()))return new MenuActions.Result("O aliado já está recuperado.",false);
        if(action.equals("tonic_target")&&!from.consumePulseTonic())return new MenuActions.Result("Sem Pulse Tonics.",false);
        if(action.equals("supply_target")&&!bag.consume(id))return new MenuActions.Result("Sem este item.",false);
        from.setResources(from.hp(),from.sp()-cost);to.setResources(to.hp()+hp,to.sp()+sp);
        return new MenuActions.Result("Recuperação aplicada.",true);
    }
}
