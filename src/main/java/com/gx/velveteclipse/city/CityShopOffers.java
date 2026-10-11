package com.gx.velveteclipse.city;

import java.util.*;
import com.gx.velveteclipse.equipment.*;
import com.gx.velveteclipse.core.persona.*;
import com.gx.velveteclipse.forge.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import com.gx.velveteclipse.shuffle.SkillCardRegistry;

/** Authoritative shop inventory. Audit fixture characters, balances and prices never enter the game. */
public final class CityShopOffers {
    public record Recipe(String id,String output,boolean card,Map<String,Integer> materials){}
    // The mod's existing elemental gems are usable materials; no fictional audit inventory.
    public static final List<Recipe> RECIPES=List.of(
        new Recipe("card_bufu","bufu",true,Map.of("bufula_gem",2)),
        new Recipe("card_agi","agi",true,Map.of("agilao_gem",2)),
        new Recipe("card_zio","zio",true,Map.of("zionga_gem",2)),
        new Recipe("card_garu","garu",true,Map.of("garula_gem",2)));
    public static List<CityNetwork.Row> rows(ServerPlayer p,String shop){
        var bag=EquipmentData.load(p);var out=new ArrayList<CityNetwork.Row>();
        if(shop.equals("blue")){
            for(var member:com.gx.velveteclipse.party.PartyService.members(p)){
                String key=member.m_20148_().toString();out.add(new CityNetwork.Row(key,member.m_7755_().getString(),member==p?"Selecione a habilidade natural que deseja relembrar.":"Este jogador confirma o próprio atendimento.",0,"member",0,"",0,0));
                var wallet=PlayerPersonaData.load(member);
                for(var held:wallet.rosterInstances()){
                    var def=PersonaRegistry.persona(held.definitionId()).orElse(null);if(def==null)continue;
                    for(String skill:new TreeSet<>(held.learnedSkills())){var s=PersonaRegistry.skill(skill).orElse(null);if(s!=null&&out.size()<150)out.add(new CityNetwork.Row(held.definitionId()+"/"+skill,s.displayName(),def.displayName(),0,"known:"+key,0,"",0,0));}
                    var forgotten=new TreeSet<String>();for(var entry:def.learnset().entrySet())if(entry.getKey()<=held.level())forgotten.addAll(entry.getValue());forgotten.removeAll(held.learnedSkills());
                    for(String skill:forgotten){var s=PersonaRegistry.skill(skill).orElse(null);if(s!=null&&out.size()<190)out.add(new CityNetwork.Row(held.definitionId()+"/"+skill,s.displayName(),def.displayName()+" · "+SkillDescriptions.describe(s),3000,"skill:"+key,0,"",0,0));}
                }
            }return out;
        }
        for(var row:CityCatalog.rows(shop,ShopProgress.tier(p))){var gear=EquipmentCatalog.get(row.id());var equipped=gear==null?null:EquipmentCatalog.get(bag.equipped.get(gear.slot()));
            out.add(new CityNetwork.Row(row.id(),row.name(),row.detail(),row.price(),gear!=null?gear.slot().name():"items",gear==null?bag.count(row.id()):bag.owned.contains(row.id())?1:0,gear==null?"":gear.metric(),equipped==null?0:equipped.value(),gear==null?0:gear.value()));
        }
        if(shop.equals("police"))for(String id:bag.owned){var g=EquipmentCatalog.get(id);if(g!=null&&!bag.equipped.containsValue(id)&&g.sell()>0)out.add(new CityNetwork.Row(id,g.name(),"Equipamento guardado · venda definitiva",g.sell(),"sell",1,g.metric(),g.value(),0));}
        if(shop.equals("mayoido")){
            for(var recipe:RECIPES){String detail=materials(recipe,bag);var skill=PersonaRegistry.skill(recipe.output()).orElseThrow();out.add(new CityNetwork.Row(recipe.id(),"Carta "+skill.displayName(),detail,0,"cards",0,"",0,0));}
            // Equipment fusion uses the real, unlocked catalog and consumes gems instead of audit-only materials.
            for(var gear:EquipmentCatalog.all())if(ShopStock.available(gear.id(),ShopProgress.tier(p))&&gear.slot()==EquipmentCatalog.Slot.WEAPON){int n=Math.max(2,(gear.price()+799)/800);out.add(new CityNetwork.Row("forge_"+gear.id(),gear.name(),"Gema Agilao: "+bag.count("agilao_gem")+" / "+n+" · "+gear.metric()+" "+gear.value(),0,"forge",bag.owned.contains(gear.id())?1:0,gear.metric(),0,gear.value()));}
        }return List.copyOf(out);
    }
    private static String materials(Recipe r,EquipmentLoadout bag){var text=new ArrayList<String>();for(var e:r.materials().entrySet()){var supply=Supplies.get(e.getKey());text.add((supply==null?e.getKey():supply.name())+": "+bag.count(e.getKey())+" / "+e.getValue());}return String.join(" · ",text);}
    public static String buyQuantity(EquipmentLoadout bag,PlayerPersonaData wallet,String id,int qty){
        var supply=Supplies.get(id);if(supply==null||qty<1||qty>99)return "Quantidade ou consumível inválido.";
        if(bag.count(id)+qty>999)return "Inventário cheio.";if(!wallet.spendYen((long)supply.price()*qty))return "Yen insuficiente.";
        for(int i=0;i<qty;i++)bag.addSupply(id);return "Compra concluída: "+qty+" × "+supply.name()+".";
    }
    public static String sellStored(EquipmentLoadout bag,PlayerPersonaData wallet,String id){
        var gear=EquipmentCatalog.get(id);if(gear==null||!bag.owned.contains(id)||bag.equipped.containsValue(id)||gear.sell()<=0)return "O equipamento não está disponível para venda.";
        bag.owned.remove(id);wallet.addYen(gear.sell());return "Equipamento vendido.";
    }
    public static String transact(ServerPlayer p,String shop,CityNetwork.Request r){
        if(!Set.of("buyqty","sell","trade","recallSkill").contains(r.action()))return null;
        var wallet=PlayerPersonaData.load(p);var bag=EquipmentData.load(p);String result;
        if(r.action().equals("buyqty")){
            String[] parts=r.id().split("\\|",-1);int qty;try{qty=Integer.parseInt(parts[0]);}catch(RuntimeException e){return "Quantidade inválida.";}
            if(parts.length!=2||qty<1||qty>99||!CityRules.canPurchase(shop,parts[1],ShopProgress.tier(p)))return "Item indisponível.";
            result=buyQuantity(bag,wallet,parts[1],qty);
        }else if(r.action().equals("sell")){
            if(!shop.equals("police"))return "Atendimento indisponível.";result=sellStored(bag,wallet,r.id());
        }else if(r.action().equals("recallSkill")){
            if(!shop.equals("blue"))return "Atendimento indisponível.";String[] parts=r.id().split("/",-1);if(parts.length!=2)return "Habilidade inválida.";
            var persona=wallet.instance(parts[0]);var def=PersonaRegistry.persona(parts[0]).orElse(null);
            if(persona==null||def==null||persona.learnedSkills().contains(parts[1])||def.learnset().entrySet().stream().noneMatch(e->e.getKey()<=persona.level()&&e.getValue().contains(parts[1])))return "Esta Persona não pode relembrar essa habilidade.";
            if(!wallet.spendYen(3000))return "Yen insuficiente.";persona.inheritSkill(parts[1]);wallet.updatePersona(persona);result="Habilidade relembrada.";
        }else {
            if(!shop.equals("mayoido"))return "Troca indisponível.";
            if(r.id().startsWith("forge_")){
                var gear=EquipmentCatalog.get(r.id().substring(6));if(gear==null||gear.slot()!=EquipmentCatalog.Slot.WEAPON||!ShopStock.available(gear.id(),ShopProgress.tier(p)))return "Receita indisponível.";
                if(bag.owned.contains(gear.id()))return "Equipamento já adquirido.";int count=Math.max(2,(gear.price()+799)/800);if(bag.count("agilao_gem")<count)return "Materiais insuficientes.";
                for(int i=0;i<count;i++)bag.consume("agilao_gem");bag.grant(gear.id());result="Equipamento produzido.";
            }else {
                var recipe=RECIPES.stream().filter(v->v.id().equals(r.id())).findFirst().orElse(null);if(recipe==null)return "Receita indisponível.";
                if(recipe.materials().entrySet().stream().anyMatch(e->bag.count(e.getKey())<e.getValue()))return "Materiais insuficientes.";
                var card=new ItemStack(SkillCardRegistry.CARDS.get(recipe.output()).get());var inventory=p.m_150109_();boolean space=inventory.m_36062_()>=0;for(int i=0;i<36&&!space;i++){var stack=inventory.m_8020_(i);space=ItemStack.m_150942_(stack,card)&&stack.m_41613_()<64;}
                if(!space||!inventory.m_36054_(card))return "Libere espaço no inventário para o cartão.";
                recipe.materials().forEach((id,n)->{for(int i=0;i<n;i++)bag.consume(id);});result="Troca concluída. O cartão está no inventário.";
            }
        }
        EquipmentData.save(p,bag);wallet.save(p);BattleService.sync(p,result);EquipmentService.sync(p,"sync","");return result;
    }
    private CityShopOffers(){}
}
