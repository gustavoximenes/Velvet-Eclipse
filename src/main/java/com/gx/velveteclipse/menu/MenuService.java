package com.gx.velveteclipse.menu;
import com.gx.velveteclipse.core.persona.*;
import com.gx.velveteclipse.forge.*;
import com.gx.velveteclipse.util.RuntimeBridge;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.Villager;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.*;

@Mod.EventBusSubscriber(modid="velvet_eclipse",bus=Mod.EventBusSubscriber.Bus.FORGE)
public final class MenuService {
    private static final String LINKS="velvet_eclipse:menu_links_v1";
    private static final MenuRequestGate REQUESTS=new MenuRequestGate();
    public static void refresh(ServerPlayer p,String message){MenuNetwork.reply(p,snapshot(p,PlayerPersonaData.load(p),message));}
    public static void handle(ServerPlayer player,String action,String argument){
        if(BattleService.current(player)!=null)return;
        long now=player.m_9236_().m_46467_();UUID id=player.m_20148_();
        if(!REQUESTS.allow(id,action,now))return;
        PlayerPersonaData data=PlayerPersonaData.load(player);
        var result=MenuActions.apply(data,action,argument,BattleService.hasActiveBattle(player));
        if(result.changed()){data.save(player);BattleService.sync(player,result.message());}
        MenuNetwork.reply(player,snapshot(player,data,result.message()));
    }
    private static MenuSnapshot snapshot(ServerPlayer player,PlayerPersonaData data,String message){
        List<MenuSnapshot.Persona> personas=new ArrayList<>();
        for(var instance:data.rosterInstances())personas.add(describe(instance));
        for(var instance:data.reserveInstances())personas.add(describe(instance));
        List<MenuSnapshot.Quest> quests=new ArrayList<>();
        for(var advancement:player.m_20194_().m_129889_().m_136028_()){
            var display=advancement.m_138320_();if(display==null)continue;
            var progress=player.m_8960_().m_135996_(advancement);
            // Hidden advancements are only disclosed when completed.
            if(display.m_14997_()&&!progress.m_8193_())continue;
            quests.add(new MenuSnapshot.Quest(display.m_14977_().getString(),display.m_14985_().getString(),progress.m_8193_(),progress.m_8213_()));
        }
        quests.sort(Comparator.comparing(MenuSnapshot.Quest::done).thenComparing(MenuSnapshot.Quest::name));
        if(quests.size()>180)quests=new ArrayList<>(quests.subList(0,180));
        long time=player.m_9236_().m_46468_();
        return new MenuSnapshot(data.hp(),data.maxHp(),data.sp(),data.maxSp(),data.pulseTonics(),data.activePersona(),personas,quests,links(player),Math.floorDiv(time,24000)+1,time,message,data.playerLevel(),data.playerExperience(),data.playerNextExperience(),data.yen(),com.gx.velveteclipse.forge.BestiaryService.entries(player),data.personaCapacity(),data.reserveIds());
    }
    public static MenuSnapshot.Persona describe(PersonaInstance instance){
            var def=PersonaRegistry.persona(instance.definitionId()).orElseThrow();var stats=def.baseStats();int n=Math.max(0,instance.level()-def.baseLevel());
            var skills=instance.learnedSkills().stream().sorted().map(k->PersonaRegistry.skill(k).orElse(null)).filter(Objects::nonNull).toList();
              var bonuses=com.gx.velveteclipse.equipment.EquipmentBonuses.NONE.withSkills(instance.learnedSkills());
              List<String> affinities=java.util.Arrays.stream(com.gx.velveteclipse.core.persona.Element.values()).map(e->java.util.Map.entry(e,bonuses.affinity(e,def.affinity(e)))).filter(e->e.getValue()!=Affinity.NORMAL).map(e->e.getKey()+"  "+e.getValue()).toList();
            return new MenuSnapshot.Persona(def.id(),def.id().equals("cinder_wisp")?"Thanatos":def.displayName(),def.arcana().name(),instance.level(),instance.experience(),PersonaInstance.experienceForNextLevel(instance.level()),
                new int[]{stats.strength()+n/2,stats.magic()+n/2,stats.endurance()+n/2,stats.agility()+n/3,stats.luck()+n/3},skills,affinities);
    }
    private static List<MenuSnapshot.Link> links(Object player){
        String json=RuntimeBridge.tagGetString(RuntimeBridge.persistentData(player),LINKS);
        try{var a=MenuNetwork.JSON.fromJson(json,MenuSnapshot.Link[].class);return a==null?new ArrayList<>():new ArrayList<>(Arrays.asList(a));}
        catch(RuntimeException e){return new ArrayList<>();}
    }
    @SubscribeEvent public static void encounter(PlayerInteractEvent.EntityInteract event){
        if(!(event.getEntity() instanceof ServerPlayer player)||!(event.getTarget() instanceof Villager villager)||BattleService.hasActiveBattle(player))return;
        List<MenuSnapshot.Link> links=links(player);String id=villager.m_20148_().toString();long day=Math.floorDiv(player.m_9236_().m_46468_(),24000)+1;
        var old=links.stream().filter(l->l.id().equals(id)).findFirst().orElse(null);
        if(old!=null&&old.lastDay()==day)return;
        if(old==null&&links.size()>=64)return;
        links.removeIf(l->l.id().equals(id));
        links.add(new MenuSnapshot.Link(id,villager.m_5446_().getString(),old==null?1:Math.min(10,old.rank()+1),day));
        RuntimeBridge.tagPutString(RuntimeBridge.persistentData(player),LINKS,MenuNetwork.JSON.toJson(links));
    }
    @SubscribeEvent public static void clonePlayer(PlayerEvent.Clone event){
        String json=RuntimeBridge.tagGetString(RuntimeBridge.persistentData(event.getOriginal()),LINKS);
        if(json!=null&&!json.isBlank())RuntimeBridge.tagPutString(RuntimeBridge.persistentData(event.getEntity()),LINKS,json);
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event){REQUESTS.remove(event.getEntity().m_20148_());}
}
