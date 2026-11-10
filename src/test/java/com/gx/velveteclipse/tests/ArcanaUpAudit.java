package com.gx.velveteclipse.tests;

import com.gx.velveteclipse.roguelike.*;
import com.gx.velveteclipse.core.persona.*;
import com.gx.velveteclipse.core.fusion.*;
import com.gx.velveteclipse.forge.PlayerPersonaData;
import com.gx.velveteclipse.client.menu.RogueDesign;
import com.gx.velveteclipse.localization.DisplayText;
import com.google.gson.*;
import java.util.*;
import java.nio.file.*;

public final class ArcanaUpAudit {
    static int checks;
    static void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
    public static final class Player {
        final net.minecraft.nbt.CompoundTag tag=new net.minecraft.nbt.CompoundTag();
        public net.minecraft.nbt.CompoundTag getPersistentData(){return tag;}
    }
    static PlayerPersonaData inputs(){var d=new PlayerPersonaData();d.givePersona("iron_oni");d.givePersona("lumen_hare");d.setPlayerLevel(99);return d;}
    static FusionTransaction.Preview preview(PlayerPersonaData d){return FusionTransaction.preview(d,"iron_oni","lumen_hare",null);}
    public static void main(String[] args)throws Exception {
        PersonaRegistry.bootstrap();var json=new Gson();
        check(ArcanaUpCatalog.CARDS.size()==Arcana.values().length&&ArcanaUpCatalog.CARDS.size()==23,"one variant for all supported Arcanas");
        var p=new RogueProfile();
        for(var card:ArcanaUpCatalog.CARDS){
            check(p.rank(card.id())==0&&ArcanaUpCatalog.offered(card.key()).arcana()==card.arcana(),"rank starts at zero with exact Arcana identity");
            p.collect(card.key());check(p.rank(card.id())==0,"collecting never activates a rank");
            for(int rank=1;rank<=10;rank++)check(p.activateFromItem(card.key(),card.key(),true,true)&&p.rank(card.id())==rank,"one card grants exactly one rank");
            check(!p.activateFromItem(card.key(),card.key(),true,true)&&p.rank(card.id())==10,"max rank refuses another consumption");
        }
        check(p.active.isEmpty()&&p.collected.isEmpty()&&p.upCollected.size()==23,"rank cards never activate or collect combat buffs");
        for(boolean enabled:new boolean[]{true,false})for(boolean editable:new boolean[]{true,false})for(String held:List.of("up:magician","magician","up:star","")){
            var d=new RogueProfile();boolean expected=enabled&&editable&&held.equals("up:magician");
            check(d.activateFromItem("up:magician",held,enabled,editable)==expected&&d.rank("magician")== (expected?1:0),"mode, combat and exact offered item are authoritative");
        }
        check(!new RogueProfile().activateFromItem("up:invalid","up:invalid",true,true),"unknown Arcana cannot mutate ranks");
        var owner=new Player();RogueService.save(owner,p);var clone=new Player();RogueService.save(clone,RogueService.load(owner));
        check(RogueService.load(clone).ranks.equals(p.ranks)&&RogueService.load(owner).upCollected.equals(p.upCollected),"rank collection survives persistence and clone");
        check(RogueService.load(new Player()).ranks.isEmpty(),"a different player starts independently");
        var old=json.fromJson("{\"focus\":\"dps_magic\",\"active\":[\"magician\"]}",RogueProfile.class).normalized();
        check(old.ranks.isEmpty()&&old.active.contains("magician")&&old.focus.equals("dps_magic"),"old saves retain buffs without invented ranks");
        var bad=json.fromJson("{\"ranks\":{\"magician\":99,\"star\":-1,\"devil\":null,\"forged\":10},\"upCollected\":[\"moon\",\"forged\"]}",RogueProfile.class).normalized();
        check(bad.ranks.equals(Map.of("magician",10))&&bad.upCollected.equals(Set.of("magician","moon")),"unknown, null and out-of-range saved ranks normalized");
        var offers=new ArcanaConfirmation();UUID id=UUID.randomUUID();var offer=offers.open(id,"up:moon",40,20);
        check(offers.take(UUID.randomUUID(),offer.token(),21)==null,"rank confirmation bound to owner");
        var once=offers.take(id,offer.token(),21);var moon=new RogueProfile();check(once!=null&&moon.activateFromItem(once.card(),"up:moon",true,true)&&offers.take(id,offer.token(),21)==null&&moon.rank("moon")==1,"replayed token cannot raise rank twice");
        offer=offers.open(id,"up:moon",0,20);check(offers.take(id,offer.token(),1221)==null,"expired rank offer rejected");

        var random=new Random(1105);int buff=0,up=0,empty=0;var drops=new HashSet<String>();
        for(int i=0;i<100000;i++){String card=RogueCatalog.rollTreasure(random);if(card.isEmpty())empty++;else if(ArcanaUpCatalog.offered(card)!=null){up++;drops.add(card);}else{if(RogueCatalog.card(card)==null)throw new AssertionError("unknown loot");buff++;drops.add(card);}}
        check(buff>24000&&buff<26000&&up>9300&&up<10700&&empty>63500&&empty<66500,"25 percent Buff, 10 percent Up, 65 percent empty");
        check(drops.size()==32,"all 9 buffs and 23 rank variants reachable");

        var data=inputs();var raw=preview(data);check(raw!=null&&raw.allowed(),"normal fusion fixture");
        var def=PersonaRegistry.persona(raw.result().definitionId()).orElseThrow();var arcana=ArcanaUpCatalog.of(def.arcana());
        data.setPlayerLevel(raw.requiredLevel());data.fusionRanks(Map.of(arcana.id(),10));String fingerprint=FusionTransaction.fingerprint(data);
        var boosted=preview(data);check(boosted.allowed()&&boosted.requiredLevel()==raw.requiredLevel()&&boosted.result().level()==raw.result().level()+1,"rank 10 grants next-level EXP without raising eligibility requirement");
        check(boosted.message().contains("Arcana Up:")&&boosted.message().contains("EXP"),"preview exposes rank and awarded EXP");
        preview(data);preview(data);check(FusionTransaction.fingerprint(data).equals(fingerprint),"repeated previews do not grant XP or consume ingredients");
        int playerHp=data.hp(),playerLevel=data.playerLevel();check(FusionTransaction.commit(data,"iron_oni","lumen_hare",null),"boosted result can exceed player level after valid fusion");
        var result=data.instance(def.id());check(result.level()==boosted.result().level()&&result.experience()==boosted.result().experience()&&result.learnedSkills().equals(boosted.result().learnedSkills()),"commit grants previewed bonus once");
        check(!FusionTransaction.commit(data,"iron_oni","lumen_hare",null)&&data.playerLevel()==playerLevel&&data.hp()==playerHp,"replay cannot award XP and fusion does not level the player");
        check(data.compendiumInstances().stream().anyMatch(i->i.definitionId().equals(def.id())&&i.level()==result.level()),"boosted result registered in Compendium");

        data=inputs();data.fusionRanks(Map.of(arcana.id(),1));var rankOne=preview(data);int expected=(PersonaInstance.experienceForNextLevel(raw.result().level())+9)/10;
        check(rankOne.result().experience()==expected,"rank 1 grants ten percent rounded up");
        data.fusionRanks(Map.of());check(preview(data).result().experience()==raw.result().experience()&&preview(data).result().level()==raw.result().level(),"disabled fusion context suspends bonus");
        var different=ArcanaUpCatalog.CARDS.stream().filter(c->c.arcana()!=def.arcana()).findFirst().orElseThrow();data.fusionRanks(Map.of(different.id(),10));
        check(preview(data).result().level()==raw.result().level()&&preview(data).result().experience()==raw.result().experience(),"only matching result Arcana contributes");
        var ticket=new FusionTicket(data,"iron_oni","lumen_hare",0);data.fusionRanks(Map.of(arcana.id(),10));check(!ticket.confirm(data,ticket.token,1),"changing rank invalidates old confirmation");
        ticket=new FusionTicket(data,"iron_oni","lumen_hare",0);data.fusionRanks(Map.of());check(!ticket.confirm(data,ticket.token,1),"disabling ranked bonus invalidates old confirmation");
        data.fusionRanks(Map.of(arcana.id(),10));var capped=new PersonaInstance(def.id(),98,PersonaInstance.experienceForNextLevel(98)-1,raw.result().learnedSkills());
        check(FusionArcanaBonus.apply(data,capped)==1&&capped.level()==99&&capped.experience()==0&&FusionArcanaBonus.apply(data,capped)==0,"bonus stops at Persona level 99");

        UUID receiver=UUID.randomUUID(),donor=UUID.randomUUID();var receiving=new PlayerPersonaData();receiving.givePersona("cinder_wisp");receiving.setPlayerLevel(99);receiving.fusionRanks(Map.of(arcana.id(),10));
        var giving=inputs();giving.expandPersonaCapacity();giving.givePersona("izanagi");
        var owners=new LinkedHashMap<UUID,PlayerPersonaData>();owners.put(receiver,receiving);owners.put(donor,giving);
        var sources=List.of(new PartyFusion.Source(donor,"iron_oni"),new PartyFusion.Source(donor,"lumen_hare"));
        var party=PartyFusion.preview(owners,receiver,sources,"",List.of());check(party!=null&&party.allowed()&&party.result().level()==boosted.result().level(),"party fusion uses recipient's rank");
        receiving.fusionRanks(Map.of());giving.fusionRanks(Map.of(arcana.id(),10));var noBorrow=PartyFusion.preview(owners,receiver,sources,"",List.of());
        check(noBorrow.result().level()==raw.result().level()&&noBorrow.result().experience()==raw.result().experience(),"donor rank does not leak to recipient");
        receiving.fusionRanks(Map.of(arcana.id(),10));check(PartyFusion.commit(owners,receiver,sources,"",List.of()),"party rank bonus commits");
        check(receiving.instance(def.id()).level()==boosted.result().level()&&giving.rosterIds().equals(Set.of("izanagi")),"only recipient gains EXP and donor keeps remaining Persona");
        var special=ReloadCatalogue.special().entrySet().stream().filter(e->e.getValue().size()<=6&&PersonaRegistry.persona(e.getKey()).orElseThrow().baseLevel()<99).findFirst().orElseThrow();
        var specialist=new PlayerPersonaData();while(specialist.personaCapacity()<8)specialist.expandPersonaCapacity();for(var ingredient:special.getValue())specialist.givePersona(ingredient);specialist.setPlayerLevel(99);
        var sd=PersonaRegistry.persona(special.getKey()).orElseThrow();specialist.fusionRanks(Map.of(ArcanaUpCatalog.of(sd.arcana()).id(),10));
        var specialSources=special.getValue().stream().map(s->new PartyFusion.Source(receiver,s)).toList();var sp=PartyFusion.preview(Map.of(receiver,specialist),receiver,specialSources,special.getKey(),List.of());
        check(sp!=null&&sp.allowed()&&sp.result().level()==sd.baseLevel()+1&&PartyFusion.commit(Map.of(receiver,specialist),receiver,specialSources,special.getKey(),List.of()),"special fusion grants same rank bonus");

        var state=new RogueNetwork.State(true,true,true,true,"none",List.of(),List.of(),"","up:magician","token",Map.of("magician",9));
        check(json.fromJson(json.toJson(state),RogueNetwork.State.class).equals(state),"network rank snapshot round trip");
        for(int page=0;page<4;page++){
            var view=new RogueDesign.View(new RogueNetwork.State(false,true,false,true,"none",List.of(),List.of(),"","","",p.ranks),page,22,0,false);
            var buttons=RogueDesign.buttons(view);for(int i=0;i<buttons.size();i++){var rect=buttons.get(i).rect();check(rect.x()>=0&&rect.y()>=0&&rect.x()+rect.w()<=900&&rect.y()+rect.h()<=500&&RogueDesign.hit(view,rect.x()+rect.w()/2,rect.y()+rect.h()/2)==i,"native drawn controls match clickable areas");}
        }
        var buttons=RogueDesign.buttons(new RogueDesign.View(state,3,1,0,false));check(buttons.get(0).action().equals("cancel")&&buttons.get(1).enabled(),"rank confirmation defaults to Cancel");
        var full=new RogueNetwork.State(true,true,true,true,"none",List.of(),List.of(),"","up:magician","token",Map.of("magician",10));
        check(!RogueDesign.buttons(new RogueDesign.View(full,3,1,0,false)).get(1).enabled(),"max rank confirmation is disabled");
        var resources=Path.of("src/main/resources/assets/velvet_eclipse");var images=new HashSet<String>();
        for(var card:ArcanaUpCatalog.CARDS){
            String item="arcana_up_"+card.id();check(com.gx.velveteclipse.room.CreativeCategory.of(item,false)==com.gx.velveteclipse.room.CreativeCategory.ARCANA_BUFFS,"all variants in creative tab");
            var png=resources.resolve("textures/item/"+item+".png");var model=resources.resolve("models/item/"+item+".json");
            var texture=javax.imageio.ImageIO.read(png.toFile());check(texture.getWidth()==64&&texture.getHeight()==64&&JsonParser.parseString(Files.readString(model)).getAsJsonObject().getAsJsonObject("textures").get("layer0").getAsString().equals("velvet_eclipse:item/"+item),"texture/model dimensions and binding");
            images.add(Base64.getEncoder().encodeToString(Files.readAllBytes(png)));
            for(String lang:List.of("en_us","pt_br"))check(JsonParser.parseString(Files.readString(resources.resolve("lang/"+lang+".json"))).getAsJsonObject().has("item.velvet_eclipse."+item),"localized item name");
        }
        check(images.size()==23,"all rank variants have distinct art");
        DisplayText.language("pt_br");check(DisplayText.text("Fusion bonus: +90% > +100% of next-level EXP.").equals("Bônus de fusão: +90% > +100% da EXP do próximo nível."),"dynamic bonus confirmation in Portuguese");DisplayText.language("en_us");
        var out=Path.of("build/reports/arcana-up");ArcanaUpPreview.export(out);
        Files.writeString(out.resolve("validation.json"),new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("checks",checks,"passed",true,"variants",23,"maxRank",10,"offlineOnly",true,"lootSample",Map.of("buff",buff,"up",up,"empty",empty))));
        System.out.println("PASS Arcana Up: "+checks+" checks; 23 variants, rank cap, persistence, confirmations, loot, solo/party/special fusion, preview integrity, creative assets and EN/PT UI. Offline only.");
    }
}
