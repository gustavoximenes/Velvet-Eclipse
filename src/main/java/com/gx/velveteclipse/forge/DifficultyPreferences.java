package com.gx.velveteclipse.forge;

import com.gx.velveteclipse.core.battle.BattleDifficulty;
import com.gx.velveteclipse.menu.MenuSnapshot;
import com.gx.velveteclipse.party.PartyService;
import com.gx.velveteclipse.util.RuntimeBridge;
import net.minecraft.server.level.ServerPlayer;

/** Saved personal preference, authoritative party leader selection, frozen per encounter. */
public final class DifficultyPreferences {
    private static final String KEY="velvet_eclipse:difficulty";
    public static BattleDifficulty personal(Object p){return BattleDifficulty.parse(RuntimeBridge.tagGetString(RuntimeBridge.persistentData(p),KEY));}
    public static BattleDifficulty effective(Object p){return p instanceof ServerPlayer sp?PartyService.difficulty(sp):personal(p);}
    public static void savePersonal(Object p,BattleDifficulty value){RuntimeBridge.tagPutString(RuntimeBridge.persistentData(p),KEY,value.id());}
    public static void copy(Object from,Object to){savePersonal(to,personal(from));}
    private static boolean available(ServerPlayer p){return p.m_6084_()&&!com.gx.velveteclipse.awakening.AwakeningService.active(p)&&!com.gx.velveteclipse.exploration.ExplorationService.preparing(p);}
    private static boolean busy(ServerPlayer p){return PartyService.members(p).stream().anyMatch(m->BattleService.current(m)!=null||com.gx.velveteclipse.raid.RaidService.member(m));}
    public static MenuSnapshot.DifficultyChoice view(ServerPlayer p){
        boolean leader=PartyService.difficultyLeader(p),blocked=busy(p)||!available(p);
        var raid=com.gx.velveteclipse.raid.RaidService.selectedDifficulty(p);
        return new MenuSnapshot.DifficultyChoice((raid==null?effective(p):raid).id(),leader&&!blocked,!leader?"Somente o líder pode alterar.":blocked?"Disponível fora de combates e raids.":"Vale para o grupo; solo usa sua preferência.");
    }
    public static String set(ServerPlayer p,String id){
        boolean leader=PartyService.difficultyLeader(p),blocked=busy(p);
        if(!BattleDifficulty.mayChoose(leader,available(p),blocked,id))return !leader?"Somente o líder pode alterar.":blocked?"Disponível fora de combates e raids.":"Dificuldade indisponível.";
        var value=BattleDifficulty.parse(id);savePersonal(p,value);PartyService.difficulty(p,value);
        return "Dificuldade salva para os próximos combates.";
    }
    private DifficultyPreferences(){}
}
