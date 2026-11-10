package com.gx.velveteclipse.core.fusion;

import com.gx.velveteclipse.core.persona.*;
import com.gx.velveteclipse.forge.PlayerPersonaData;
import com.gx.velveteclipse.roguelike.ArcanaUpCatalog;
import java.util.*;

/** Reload's Arcana ultimates. World has no Persona in the installed catalogue. */
public final class SupremeFusion {
    public record Requirement(String id, Arcana arcana, boolean allRanks) {}
    public record Entry(String id,String name,String arcana,int level,int rank,boolean unlocked,boolean gated,boolean allRanks,int maxed,int total,List<String> ingredients) {}
    public record State(List<Entry> entries,List<FusionSearch.Option> options) {
        public static State empty(){return new State(List.of(),List.of());}
    }
    private static Requirement r(String id,Arcana a){return new Requirement(id,a,false);}
    public static final List<Requirement> CATALOG=List.of(
        r("susano_o",Arcana.FOOL),r("futsunushi",Arcana.MAGICIAN),r("scathach",Arcana.PRIESTESS),
        r("alilat",Arcana.EMPRESS),r("odin",Arcana.EMPEROR),r("kohryu",Arcana.HIEROPHANT),
        r("cybele",Arcana.LOVERS),r("thor",Arcana.CHARIOT),r("melchizedek",Arcana.JUSTICE),
        r("arahabaki",Arcana.HERMIT),r("lakshmi",Arcana.FORTUNE),r("atavaka",Arcana.STRENGTH),
        r("attis",Arcana.HANGED),r("thanatos",Arcana.DEATH),r("yurlungur",Arcana.TEMPERANCE),
        r("beelzebub",Arcana.DEVIL),r("chi_you",Arcana.TOWER),r("helel",Arcana.STAR),
        r("sandalphon",Arcana.MOON),r("asura",Arcana.SUN),r("messiah",Arcana.JUDGEMENT),
        r("metatron",Arcana.AEON),new Requirement("orpheus_telos",Arcana.FOOL,true));
    public static Requirement requirement(String id){return CATALOG.stream().filter(r->r.id().equals(id)).findFirst().orElse(null);}
    public static boolean isSupreme(String id){return requirement(id)!=null;}
    public static int maxed(PlayerPersonaData d){return (int)CATALOG.stream().filter(r->!r.allRanks()&&d.fusionRank(r.arcana())==10).count();}
    public static boolean unlocked(PlayerPersonaData d,String id){
        var r=requirement(id);return r==null||!d.roguelikeFusion()||(r.allRanks()?maxed(d)==22:d.fusionRank(r.arcana())==10);
    }
    public static String lockedMessage(String id){
        var r=requirement(id);if(r==null)return "";
        return r.allRanks()?"Supreme fusion locked: all 22 fusion Arcanas must reach rank 10.":"Supreme fusion locked: "+ArcanaUpCatalog.of(r.arcana()).name()+" requires rank 10.";
    }
    public static List<Entry> entries(PlayerPersonaData d){return CATALOG.stream().map(r->{
        var def=PersonaRegistry.persona(r.id()).orElseThrow();
        return new Entry(r.id(),def.displayName(),ArcanaUpCatalog.of(r.arcana()).name(),def.baseLevel(),d.fusionRank(r.arcana()),unlocked(d,r.id()),d.roguelikeFusion(),r.allRanks(),maxed(d),22,ReloadCatalogue.special().getOrDefault(r.id(),List.of()));
    }).toList();}
    /** Bound the wire payload while retaining multiple valid ingredient combinations per result. */
    public static List<FusionSearch.Option> available(Map<UUID,PlayerPersonaData> owners,UUID receiver,List<PartyFusion.Source> stock){
        var counts=new HashMap<String,Integer>();return FusionSearch.available(owners,receiver,stock).stream()
            .filter(o->isSupreme(o.id())).filter(o->counts.merge(o.id(),1,Integer::sum)<=8).toList();
    }
    private SupremeFusion() {}
}
