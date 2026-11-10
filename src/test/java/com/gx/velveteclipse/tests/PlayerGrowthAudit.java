package com.gx.velveteclipse.tests;

import com.gx.velveteclipse.forge.*;
import com.gx.velveteclipse.progression.PlayerLevelGrowth;
import com.gx.velveteclipse.core.persona.*;
import com.gx.velveteclipse.core.battle.*;
import com.gx.velveteclipse.equipment.*;
import com.gx.velveteclipse.roguelike.*;
import com.gx.velveteclipse.shuffle.*;
import com.gx.velveteclipse.client.*;
import com.gx.velveteclipse.network.BattleResultsS2C;
import net.minecraft.nbt.CompoundTag;
import java.nio.file.*;
import java.util.*;

/** Resource growth, old-save compatibility, reward boundaries and shared battle costs. */
public final class PlayerGrowthAudit {
    private static int checks;
    private static void check(boolean value,String message) { checks++;if(!value)throw new AssertionError(message); }
    public static final class Player {
        final CompoundTag tag=new CompoundTag();
        public CompoundTag getPersistentData(){return tag;}
    }
    private static PlayerPersonaData data() {var d=new PlayerPersonaData();d.givePersona("cinder_wisp");return d;}
    private static Player legacy(int level,int hp,int sp,int baseHp,int baseSp) {
        var p=new Player();p.tag.m_128359_("velvet_eclipse:data_v1","V1|"+hp+"|"+baseHp+"|"+sp+"|"+baseSp+"|cinder_wisp|3|cinder_wisp,1,0,agi|"+level+"|0|200|2|||true");return p;
    }
    private static BattleCombatant actor(PlayerPersonaData d) {
        return new BattleCombatant(UUID.randomUUID(),"Player",BattleSide.PLAYERS,d.activeInstance(),d.maxHp(),d.hp(),d.maxSp(),d.sp());
    }
    public static void main(String[] args)throws Exception {
        PersonaRegistry.bootstrap();
        var d=data();check(d.maxHp()==120&&d.maxSp()==45&&d.hp()==120&&d.sp()==45,"level one keeps initial capacities");
        d.setResources(100,32);
        for(int level=1;level<99;level++) {
            int hp=d.maxHp(),sp=d.maxSp();d.addPlayerExperience(d.playerNextExperience()-1);
            check(d.playerLevel()==level&&d.maxHp()==hp&&d.maxSp()==sp,"partial XP does not grow resources at "+level);
            d.addPlayerExperience(1);
            check(d.playerLevel()==level+1&&d.playerExperience()==0,"exact level threshold "+level);
            check(d.maxHp()==hp+6&&d.maxSp()==sp+3,"capacity growth at "+level);
            check(d.hp()==d.maxHp()-20&&d.sp()==d.maxSp()-13,"level-up preserves missing HP/SP at "+level);
        }
        check(d.maxHp()==708&&d.maxSp()==339,"level 99 capacities");
        d.addPlayerExperience(Integer.MAX_VALUE);check(d.playerLevel()==99&&d.playerExperience()==0&&d.hp()==688,"cap rejects further growth and XP");

        var bulk=data();var steps=data();int total=17;
        for(int level=1;level<15;level++){int xp=PersonaInstance.experienceForNextLevel(level);total+=xp;steps.addPlayerExperience(xp);}
        steps.addPlayerExperience(17);bulk.addPlayerExperience(total);
        check(bulk.playerLevel()==15&&bulk.playerExperience()==17&&bulk.hp()==steps.hp()&&bulk.sp()==steps.sp(),"multi-level reward equals incremental awards");
        bulk.addPlayerExperience(0);bulk.addPlayerExperience(-100);check(bulk.playerExperience()==17,"nonpositive XP is ignored");
        bulk.addPlayerExperience(Integer.MAX_VALUE);check(bulk.playerLevel()==99&&bulk.maxHp()==708&&bulk.hp()==708,"large awards do not overflow");

        d=data();d.setResources(75,20);d.setPlayerLevel(25);
        check(d.maxHp()==264&&d.maxSp()==117&&d.hp()==75&&d.sp()==20,"admin raising changes maxima without healing");
        d.restore();d.setPlayerLevel(1);check(d.hp()==120&&d.sp()==45,"admin lowering clamps to lower capacities");
        for(int bad:new int[]{Integer.MIN_VALUE,0,100,Integer.MAX_VALUE}){
            try{d.setPlayerLevel(bad);throw new AssertionError("accepted level "+bad);}catch(IllegalArgumentException expected){}
            check(d.playerLevel()==1&&d.hp()==120&&d.sp()==45,"invalid admin changes are atomic");
        }
        d.setResources(0,0);d.addPlayerExperience(58);check(d.hp()==0&&d.maxHp()==126&&d.sp()==3,"growth never revives KO players");
        d=data();d.setPersonaLevel("cinder_wisp",30);check(d.playerLevel()==1&&d.maxHp()==120&&d.maxSp()==45,"Persona level is independent");

        var old=legacy(37,80,12,150,55);var migrated=PlayerPersonaData.load(old);
        check(migrated.maxHp()==366&&migrated.maxSp()==163&&migrated.hp()==80&&migrated.sp()==12,"legacy leveled save gains missing capacity, keeps custom bases and current resources");
        for(int i=0;i<12;i++) {
            migrated.save(old);migrated=PlayerPersonaData.load(old);
            check(migrated.maxHp()==366&&migrated.maxSp()==163&&migrated.hp()==80&&migrated.sp()==12,"reconnect neither duplicates growth nor heals");
        }
        var raw=old.tag.m_128461_("velvet_eclipse:data_v1").split("\\|",-1);
        check(raw[2].equals("150")&&raw[4].equals("55"),"persisted base excludes growth and gear");
        migrated.addPlayerExperience(migrated.playerNextExperience());check(migrated.maxHp()==372&&migrated.hp()==86&&migrated.sp()==15,"first post-migration level adds only one increment");
        migrated.save(old);var clone=new Player();PlayerPersonaData.copyPersisted(old,clone);
        check(PlayerPersonaData.load(clone).maxHp()==372&&PlayerPersonaData.load(clone).sp()==15,"respawn clone retains level and growth");
        var levelOne=new Player();levelOne.tag.m_128359_("velvet_eclipse:data_v1","V1|90|120|10|45|cinder_wisp|3|cinder_wisp,1,0,agi");
        check(PlayerPersonaData.load(levelOne).maxHp()==120&&PlayerPersonaData.load(levelOne).hp()==90,"pre-player-level save stays compatible");
        var maxed=legacy(99,0,0,120,45);maxed.tag.m_128359_("velvet_eclipse:data_v1",maxed.tag.m_128461_("velvet_eclipse:data_v1").replace("|99|0|","|99|100|"));
        check(PlayerPersonaData.load(maxed).playerExperience()==0&&PlayerPersonaData.load(maxed).hp()==0,"capped legacy XP resets without reviving");
        var huge=PlayerPersonaData.load(legacy(98,Integer.MAX_VALUE,Integer.MAX_VALUE,Integer.MAX_VALUE,Integer.MAX_VALUE));huge.addPlayerExperience(huge.playerNextExperience());
        check(huge.maxHp()==Integer.MAX_VALUE&&huge.hp()==Integer.MAX_VALUE&&huge.sp()==Integer.MAX_VALUE,"custom capacities saturate without overflow");

        var geared=legacy(10,100,20,120,45);var bag=EquipmentLoadout.starter();bag.owned.add("gladius");bag.equip("gladius");EquipmentData.save(geared,bag);
        d=PlayerPersonaData.load(geared);check(d.maxHp()==184&&d.maxSp()==72,"equipment stacks once after level growth");
        d.addPlayerExperience(d.playerNextExperience());d.restore();d.save(geared);bag.remove(EquipmentCatalog.Slot.WEAPON);EquipmentData.save(geared,bag);
        check(PlayerPersonaData.load(geared).maxHp()==180&&PlayerPersonaData.load(geared).hp()==180,"unequip clamps HP and preserves progression");

        d=data();d.setResources(50,10);var combat=actor(d);d.addPlayerExperience(58);d.setPersonaLevel("cinder_wisp",2);
        var active=PlayerBattleView.of(d,combat,false);var result=PlayerBattleView.of(d,combat,true);var outside=PlayerBattleView.of(d,null,false);
        check(active.maxHp()==120&&active.hp()==50&&active.maxSp()==45&&active.persona().level()==1,"ongoing combat uses live combatant");
        check(result.maxHp()==126&&result.hp()==56&&result.maxSp()==48&&result.sp()==13&&result.persona().level()==2,"results use post-reward resources and Persona");
        check(outside.hp()==result.hp()&&outside.maxSp()==result.maxSp(),"acknowledgement keeps same resources");
        var other=data();other.setPlayerLevel(30);other.setResources(41,17);
        check(PlayerBattleView.of(other,null,false).hp()==41&&PlayerBattleView.of(d,null,false).hp()==56,"player progression remains individual");

        d=data();d.setResources(50,10);var offer=new ShuffleData();offer.add(new ShuffleRules.Round("xp",1,List.of(new ShuffleRules.Card(ShuffleRules.Kind.WANDS,"",260,1))));
        ShuffleRewards.claim(offer,d,"xp",0,k->true);check(d.playerLevel()==3&&d.maxHp()==132&&d.hp()==62&&d.sp()==16,"Shuffle XP follows same growth curve");
        ShuffleRewards.claim(offer,d,"xp",0,k->true);check(d.hp()==62&&d.sp()==16,"replayed Shuffle offer cannot grant resources");
        var bash=PersonaRegistry.skill("bash").orElseThrow();var evolved=actor(d);
        var normal=SkillPayment.of(bash,d.maxHp(),EquipmentBonuses.NONE,RogueBonuses.NONE);
        evolved.rogue(new RogueBonuses(RogueCatalog.Focus.WEAKNESS,Set.of()));
        check(evolved.hpPayment(bash)==(normal.hp()+1)/2,"Weakness Hitter halves physical payment after resource growth");
        int before=evolved.hp();check(evolved.spendSkill(bash)&&evolved.hp()==before-(normal.hp()+1)/2,"combat spends exact displayed discounted amount");
        var profile=new RogueProfile();profile.focus="weakness";profile.collect("magician");profile.activateFromItem("magician","magician",true,true);var player=new Player();d.save(player);RogueService.save(player,profile);
        PlayerPersonaData.load(player).save(player);check(RogueService.load(player).active.contains("magician")&&RogueService.load(player).focus.equals("weakness"),"progression save preserves independent roguelike profile");

        var text=new ArrayList<String>();HudDesign.Canvas canvas=new HudDesign.Canvas(){
            public void fill(int x,int y,int w,int h,int color){}
            public void text(String s,int x,int y,int color){text.add(s);}
            public int width(String s){return s.length()*6;}
        };
        BattleResultsDesign.paint(canvas,new BattleResultsS2C("test",true,260,1,3,120,122,"Pixie",3,"",false),900,500,2);
        check(text.contains("MAX HP +12   MAX SP +6"),"results display total resource growth for multiple levels");
        com.gx.velveteclipse.localization.DisplayText.language("pt_br");
        check(com.gx.velveteclipse.localization.DisplayText.text("MAX HP +12   MAX SP +6").equals("HP MÁX. +12   SP MÁX. +6"),"resource gains localized in Portuguese");
        com.gx.velveteclipse.localization.DisplayText.language("en_us");
        check(com.gx.velveteclipse.localization.DisplayText.text("MAX HP +12   MAX SP +6").equals("MAX HP +12   MAX SP +6"),"resource gains localized in English");
        text.clear();BattleResultsDesign.paint(canvas,new BattleResultsS2C("test",true,1,3,3,121,122,"Pixie",3,"",false),900,500,2);
        check(text.stream().noneMatch(s->s.startsWith("MAX HP +")),"no growth label without a level-up");
        var service=Files.readString(Path.of("src/main/java/com/gx/velveteclipse/forge/BattleService.java"));
        check(service.contains("if(!g.results.started())persist(member,g.session)"),"abort path protects already-granted resources from stale combatant persistence");
        var report=Path.of("build/reports/player-growth");Files.createDirectories(report);
        Files.writeString(report.resolve("validation.json"),"{\"checks\":"+checks+",\"offline\":true,\"hpPerLevel\":6,\"spPerLevel\":3,\"level99\":{\"hp\":708,\"sp\":339}}");
        System.out.println("PASS player growth: "+checks+" checks; XP, 1..99, old saves, gear, clone, results, Shuffle and roguelike costs. Offline only.");
    }
}
