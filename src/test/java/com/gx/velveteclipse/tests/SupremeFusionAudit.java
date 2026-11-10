package com.gx.velveteclipse.tests;

import com.gx.velveteclipse.core.fusion.*;
import com.gx.velveteclipse.core.persona.*;
import com.gx.velveteclipse.forge.PlayerPersonaData;
import com.gx.velveteclipse.roguelike.ArcanaUpCatalog;
import com.gx.velveteclipse.client.menu.*;
import com.gx.velveteclipse.localization.DisplayText;
import com.gx.velveteclipse.room.VelvetArchive;
import com.google.gson.*;
import java.util.*;
import java.nio.file.*;

public final class SupremeFusionAudit {
    static int checks;
    static void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
    static Map<String,Integer> allRanks(){var map=new LinkedHashMap<String,Integer>();for(var r:SupremeFusion.CATALOG)if(!r.allRanks())map.put(ArcanaUpCatalog.of(r.arcana()).id(),10);return map;}
    static PlayerPersonaData owner(List<String> inputs){var d=new PlayerPersonaData();while(d.personaCapacity()<8)d.expandPersonaCapacity();d.setPlayerLevel(99);inputs.forEach(d::givePersona);return d;}
    public static void main(String[] args)throws Exception {
        PersonaRegistry.bootstrap();UUID self=UUID.randomUUID();
        check(SupremeFusion.CATALOG.size()==23&&SupremeFusion.CATALOG.stream().filter(r->!r.allRanks()).map(SupremeFusion.Requirement::arcana).distinct().count()==22,"22 Arcana ultimates plus Orpheus Telos");
        check(SupremeFusion.CATALOG.stream().noneMatch(r->r.arcana()==Arcana.WORLD),"World has no fabricated catalogue Persona");
        var examples=new LinkedHashMap<String,List<String>>();
        for(var r:SupremeFusion.CATALOG)if(ReloadCatalogue.special().containsKey(r.id()))examples.put(r.id(),ReloadCatalogue.special().get(r.id()));
        var personas=ReloadCatalogue.ids().stream().map(id->new PersonaInstance(PersonaRegistry.persona(id).orElseThrow())).toList();
        for(int a=0;a<personas.size();a++)for(int b=a+1;b<personas.size();b++){
            var recipe=FusionService.preview(personas.get(a),personas.get(b)).orElse(null);
            if(recipe!=null&&SupremeFusion.isSupreme(recipe.personaId()))examples.putIfAbsent(recipe.personaId(),List.of(personas.get(a).definitionId(),personas.get(b).definitionId()));
        }
        check(examples.size()==23,"real normal or special recipe available for every supreme result");
        for(var r:SupremeFusion.CATALOG){
            var def=PersonaRegistry.persona(r.id()).orElseThrow();check(def.arcana()==r.arcana(),"supreme belongs to expected Reload Arcana: "+r.id());
            String rankId=ArcanaUpCatalog.of(r.arcana()).id();var d=owner(examples.get(r.id()));
            var sources=examples.get(r.id()).stream().map(id->new PartyFusion.Source(self,id)).toList();String special=ReloadCatalogue.special().containsKey(r.id())?r.id():"";
            d.fusionContext(true,Map.of(rankId,9));String before=FusionTransaction.fingerprint(d);
            var locked=PartyFusion.preview(Map.of(self,d),self,sources,special,List.of());
            check(locked!=null&&!locked.allowed()&&locked.result().definitionId().equals(r.id())&&locked.message().contains("rank 10"),"rank 9 denies normal and special supreme results: "+r.id());
            check(!PartyFusion.commit(Map.of(self,d),self,sources,special,List.of())&&before.equals(FusionTransaction.fingerprint(d)),"locked commit is atomic: "+r.id());
            check(!d.canExchange(examples.get(r.id()),new PersonaInstance(def))&&!d.exchangeFusion(examples.get(r.id()),new PersonaInstance(def)),"low-level mutation cannot bypass rank gate");
            d.fusionContext(false,Map.of(rankId,9));check(SupremeFusion.unlocked(d,r.id())&&PartyFusion.preview(Map.of(self,d),self,sources,special,List.of()).allowed(),"normal mode has no rank restriction");
            d.fusionContext(true,r.allRanks()?allRanks():Map.of(rankId,10));
            var unlocked=PartyFusion.preview(Map.of(self,d),self,sources,special,List.of());check(unlocked.allowed(),"rank 10 unlocks result: "+r.id());
            var search=SupremeFusion.available(Map.of(self,d),self,sources);check(search.stream().anyMatch(o->o.id().equals(r.id()))&&search.stream().allMatch(o->SupremeFusion.isSupreme(o.id())),"exclusive search includes only permitted supreme options");
            if(sources.size()==2){
                String a=sources.get(0).persona(),b=sources.get(1).persona();d.fusionContext(true,Map.of(rankId,9));
                check(!FusionTransaction.preview(d,a,b,null).allowed()&&!FusionTransaction.commit(d,a,b,null)&&!d.replaceFusion(a,b,new PersonaInstance(def)),"legacy normal route cannot bypass rank requirement");
                d.fusionContext(true,r.allRanks()?allRanks():Map.of(rankId,10));
            }
            check(PartyFusion.commit(Map.of(self,d),self,sources,special,List.of())&&d.instance(r.id())!=null,"unlocked supreme commits with correct result");
        }
        var ranks=allRanks();var telos=owner(examples.get("orpheus_telos"));
        telos.fusionContext(true,Map.of("fool",10));check(!SupremeFusion.unlocked(telos,"orpheus_telos"),"Telos does not unlock with Fool alone");
        for(String missing:new ArrayList<>(ranks.keySet())){var incomplete=new HashMap<>(ranks);incomplete.put(missing,9);telos.fusionContext(true,incomplete);check(!SupremeFusion.unlocked(telos,"orpheus_telos"),"all 22 ranks required, including "+missing);}
        telos.fusionContext(true,ranks);check(SupremeFusion.unlocked(telos,"orpheus_telos")&&SupremeFusion.maxed(telos)==22,"World is not required for Telos");
        var recipe=examples.get("odin");var receiver=owner(List.of("pixie"));var donor=owner(recipe);donor.givePersona("apsaras");UUID other=UUID.randomUUID();
        donor.fusionContext(true,Map.of("emperor",10));receiver.fusionContext(true,Map.of("emperor",9));var owners=Map.of(self,receiver,other,donor);var sources=recipe.stream().map(id->new PartyFusion.Source(other,id)).toList();
        check(!PartyFusion.preview(owners,self,sources,"",List.of()).allowed()&&!PartyFusion.commit(owners,self,sources,"",List.of()),"a donor's rank cannot unlock recipient's supreme");
        receiver.fusionContext(true,Map.of("emperor",10));check(PartyFusion.commit(owners,self,sources,"",List.of())&&receiver.instance("odin")!=null,"recipient rank authorizes cooperative supreme");

        var gated=owner(recipe);gated.fusionContext(true,Map.of("emperor",10));var ticket=new FusionTicket(gated,recipe.get(0),recipe.get(1),0);gated.fusionContext(true,Map.of("emperor",9));
        check(!ticket.confirm(gated,ticket.token,1),"rank loss invalidates already displayed fusion ticket");
        gated.fusionContext(false,Map.of());ticket=new FusionTicket(gated,recipe.get(0),recipe.get(1),0);gated.fusionContext(true,Map.of());
        check(!ticket.confirm(gated,ticket.token,1),"enabling roguelike invalidates unrestricted old preview");
        for(int i=0;i<VelvetArchiveDesign.HOME.length;i++){
            var rect=VelvetArchiveDesign.card(i);double angle=Math.toRadians(VelvetArchiveDesign.angle(i));
            double x=rect.x()+rect.w()/2*Math.cos(angle)-rect.h()/2*Math.sin(angle),y=rect.y()+rect.w()/2*Math.sin(angle)+rect.h()/2*Math.cos(angle);
            check(VelvetArchiveDesign.pickCard(x,y)==i,"all six home options clickable");
            for(double px:new double[]{0,rect.w()})for(double py:new double[]{0,rect.h()}){double cx=rect.x()+px*Math.cos(angle)-py*Math.sin(angle),cy=rect.y()+px*Math.sin(angle)+py*Math.cos(angle);check(cx>=0&&cx<=900&&cy>=80&&cy<=474,"home card remains in viewport");}
        }
        check(VelvetArchiveDesign.HOME.length==6&&VelvetArchiveDesign.HOME[5].equals("Fusões supremas"),"dedicated sixth menu entry");
        var state=new SupremeFusion.State(SupremeFusion.entries(telos),List.of());var json=new Gson();
        var view=new VelvetArchive.View(false,self.toString(),99,0,List.of(),List.of(),List.of(),List.of(),null,List.of(),List.of(),"","",false,"",8,0,List.of(),0,0,false,"",state);
        var decoded=json.fromJson(FusionPayload.decode(FusionPayload.encode(json.toJson(view))),VelvetArchive.View.class);
        check(decoded.supreme().equals(state),"bounded archive codec carries rank locks and all catalogue entries");
        var out=Path.of("build/reports/supreme-fusion");Files.createDirectories(out);export(out,examples);
        Files.writeString(out.resolve("validation.json"),new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("checks",checks,"passed",true,"arcanaUltimates",22,"extra","orpheus_telos","offlineOnly",true,"recipes",examples)));
        System.out.println("PASS supreme fusion: "+checks+" checks; 22 Arcana gates, Telos, normal-mode compatibility, normal/special/cooperative routes, direct mutation guards, tickets, search, codec and six-option UI. Offline only.");
    }
    static void export(Path out,Map<String,List<String>> recipes)throws Exception {
        for(String language:List.of("en_us","pt_br")){
            DisplayText.language(language);var home=new RoguePreview.Canvas(1800,1000,2);home.fill(0,0,900,500,0xff0b1435);VelvetArchiveDesign.home(home,5);home.body("OFFLINE UI PREVIEW",610,60,12,0xff9fbacf,0);home.save(out.resolve(language+"-home.png"));
            for(String mode:List.of("locked","unlocked","normal")){
                var d=owner(List.of());d.fusionContext(!mode.equals("normal"),Map.of("emperor",mode.equals("unlocked")?10:9));var entries=SupremeFusion.entries(d);int selected=4;var entry=entries.get(selected);
                var rows=entries.stream().map(e->new VelvetArchiveDesign.Row("Lv "+e.level()+"   "+e.name(),e.arcana()+" / "+VelvetArchiveDesign.supremeStatus(e))).toList();
                var options=entry.unlocked()?List.of(new FusionSearch.Option("odin","Odin",75,"",recipes.get("odin").stream().map(id->new PartyFusion.Source(new UUID(0,1),id)).toList())):List.<FusionSearch.Option>of();
                var c=new RoguePreview.Canvas(1800,1000,2);c.fill(0,0,900,500,0xff0b1435);VelvetArchiveDesign.listBackground(c,"FUSÕES SUPREMAS","Escolha as Personas e confira o resultado no inspect.","JOGADOR Lv 99");VelvetArchiveDesign.rows(c,rows,selected,0,entries.size(),0);c.text("ODIN",520,115,25,0xffffffff,0);
                c.fill(524,148,340,145,0xff111f4c);c.body("OFFLINE UI PREVIEW",590,203,16,0xff9fbacf,0);c.body("Persona model omitted",594,230,12,0xff9fbacf,0);VelvetArchiveDesign.supremeDetail(c,entry,options,0);VelvetArchiveDesign.footer(c,"ESC Voltar    ENTER Selecionar    R Atualizar","A / D Combinação");c.save(out.resolve(language+"-"+mode+".png"));
            }
        }
        DisplayText.language("en_us");
    }
}
