package com.gx.velveteclipse.tests;

import com.gx.velveteclipse.core.battle.*;
import com.gx.velveteclipse.core.persona.*;
import com.gx.velveteclipse.forge.DifficultyPreferences;
import com.gx.velveteclipse.menu.MenuSnapshot;
import com.gx.velveteclipse.party.PartyService;
import com.gx.velveteclipse.party.PartyRoster;
import com.gx.velveteclipse.raid.PriestessRules;
import com.gx.velveteclipse.client.menu.ReloadMenuDesign;
import com.gx.velveteclipse.localization.DisplayText;
import com.google.gson.GsonBuilder;
import net.minecraft.nbt.CompoundTag;
import java.util.*;
import java.nio.file.*;

public final class DifficultyAudit {
    private static int checks;
    private static void check(boolean ok,String why){checks++;if(!ok)throw new AssertionError(why);}
    private static UUID id(int n){return new UUID(0,n);}
    private static BattleCombatant ally(int n){return new BattleCombatant(id(n),"Player",BattleSide.PLAYERS,new PersonaInstance("izanagi",20,0,Set.of("zio","dia")),1000,1000,100,100);}
    private static BattleCombatant foe(int n,int hp){return BattleCombatant.enemy(id(100+n),"Shadow",com.gx.velveteclipse.shadow.EnemyCatalog.get("magic_hand"),20,hp,100,Set.of("zio","mudo"));}
    private static BattleSession battle(int count,BattleDifficulty difficulty){
        var list=new ArrayList<BattleCombatant>();for(int n=1;n<=count;n++)list.add(ally(n));list.add(foe(1,1000));list.add(foe(2,1000));
        return new BattleSession(list,new Random(17),EncounterOpening.STANDARD,difficulty);
    }
    public static final class PlayerFixture {private final CompoundTag data=new CompoundTag();public CompoundTag getPersistentData(){return data;}}
    public static void main(String[] args)throws Exception {
        PersonaRegistry.bootstrap();
        check(BattleDifficulty.parse(null)==BattleDifficulty.NORMAL&&BattleDifficulty.parse("invalid")==BattleDifficulty.NORMAL,"safe migration/default");
        check(!BattleDifficulty.valid("NORMAL")&&!BattleDifficulty.valid(null),"unrecognized network values rejected");
        for(boolean leader:new boolean[]{false,true})for(boolean available:new boolean[]{false,true})for(boolean busy:new boolean[]{false,true})for(String value:new String[]{"normal","easy","invalid"})
            check(BattleDifficulty.mayChoose(leader,available,busy,value)==(leader&&available&&!busy&&BattleDifficulty.valid(value)),"leader and combat/raid lock enforced together");
        var personal=new PlayerFixture();check(DifficultyPreferences.personal(personal)==BattleDifficulty.NORMAL,"new world uses Normal");DifficultyPreferences.savePersonal(personal,BattleDifficulty.HARD);
        var replacement=new PlayerFixture();DifficultyPreferences.copy(personal,replacement);check(DifficultyPreferences.personal(replacement)==BattleDifficulty.HARD,"death/clone preference preserved");
        check(DifficultyPreferences.personal(new PlayerFixture())==BattleDifficulty.NORMAL,"unrelated player isolated");
        var state=new PartyService.State();var groups=PartyService.State.class.getDeclaredField("difficulties");groups.setAccessible(true);
        @SuppressWarnings("unchecked") var settings=(Map<String,String>)groups.get(state);settings.put("party-a","easy");settings.put("party-b","hard");
        var load=PartyService.State.class.getDeclaredMethod("load",CompoundTag.class);load.setAccessible(true);var loaded=(PartyService.State)load.invoke(null,state.m_7176_(new CompoundTag()));
        check(groups.get(loaded).equals(settings),"party choices survive world reload without crossing groups");
        var roster=new PartyRoster();roster.create("leader");roster.invite("leader","member",0);roster.accept("member",1);check(roster.group("member").members().get(0).equals("leader"),"member still uses actual leader");roster.leave("leader");check(roster.group("member").members().get(0).equals("member"),"leadership transfers on leaving");
        var matrix=new ArrayList<Object>();
        var strike=new SkillDefinition("__audit_damage","Strike",Element.ALMIGHTY,100,100,0,0,false,SkillTarget.ONE_ENEMY);
        for(var d:BattleDifficulty.values()){
            check(d.step(4)==d&&d.step(-4)==d,"selector wraps in both directions");
            double previous=0;
            for(int count=1;count<=64;count++){
                var s=battle(count,d);var enemy=s.find(id(101)).orElseThrow();var player=s.find(id(1)).orElseThrow();
                check(s.difficulty()==d&&enemy.maxHp()==Math.round(1000*d.enemyHealth(count)),"actual session HP scaling");
                check(enemy.maxHp()>previous&&enemy.hp()==enemy.maxHp(),"smooth monotonic group health");previous=enemy.maxHp();
                check(player.maxHp()==1000&&player.hp()==1000&&player.encounterDamage()==1,"player stats and resources untouched");
                var base=foe(7,1000);int standard=DamageCalculator.roll(base,player,strike,new Random(1)).damage();int scaled=DamageCalculator.roll(enemy,player,strike,new Random(1)).damage();
                check(Math.abs(scaled-standard*d.enemyDamage(count))<=1,"physical/magic damage uses encounter factor once");
                check(DamageCalculator.roll(player,base,strike,new Random(1)).damage()==DamageCalculator.roll(player,enemy,strike,new Random(1)).damage(),"player outgoing damage and enemy defense preserved");
                if(count<=5)matrix.add(Map.of("difficulty",d.id(),"participants",count,"enemyHp",enemy.maxHp(),"enemyDamage",scaled,"raidMillis",d.raidDuration(count)));
            }
        }
        check(BattleDifficulty.NORMAL.enemyHealth(4)==1&&BattleDifficulty.NORMAL.enemyDamage(4)==1,"four-player Normal retains original baseline");
        var solo=battle(1,BattleDifficulty.NORMAL);var enemy=solo.find(id(101)).orElseThrow();enemy.damage(100);var active=solo.currentActor().id();int old=enemy.hp();
        for(int i=0;i<500;i++){check(solo.join(ally(2)),"join");check(enemy.hp()==450&&enemy.maxHp()==600,"joining keeps 75 percent HP");check(solo.currentActor().id().equals(active),"joining does not steal turn");check(solo.leave(id(2)),"leave");check(enemy.hp()==old&&enemy.maxHp()==400,"no healing or rounding exploit from repeated joins");}
        enemy.damage(10000);solo.join(ally(2));check(enemy.hp()==0,"rescaling cannot resurrect defeated enemies");
        var ko=battle(3,BattleDifficulty.NORMAL);ko.find(id(2)).orElseThrow().damage(99999);ko.join(ally(4));check(ko.find(id(101)).orElseThrow().maxHp()==1000,"KO members count until they leave");
        var reserved=battle(1,BattleDifficulty.EASY);while(reserved.currentActor().side()!=BattleSide.PLAYERS)reserved.perform(reserved.currentActor().id(),BattleAction.guard());
        var before=reserved.prepare(id(1),BattleAction.skill("zio",id(101)));check(before!=null,"reserve a real skill");reserved.join(ally(2));check(reserved.prepared()==before,"membership does not reroll a committed hit");
        int sp=reserved.find(id(1)).orElseThrow().sp();reserved.perform(id(1),before.action());check(reserved.prepared()==null&&reserved.find(id(1)).orElseThrow().sp()<sp,"reservation settles and charges once");
        var calm=battle(1,BattleDifficulty.RELAXED);var caster=calm.find(id(101)).orElseThrow();var target=calm.find(id(1)).orElseThrow();var mudo=PersonaRegistry.skill("mudo").orElseThrow();
        var guaranteedHit=new Random(){@Override public double nextDouble(){return 0;}};
        check(DamageCalculator.roll(caster,target,mudo,guaranteedHit).damage()==0,"Relaxed preserves Izanagi's Dark immunity");
        target.setPersona(new PersonaInstance("orpheus",20,0,Set.of("agi")));
        var gentleHit=DamageCalculator.resolve(caster,target,mudo,guaranteedHit);
        check(!gentleHit.missed()&&!gentleHit.effect().startsWith("instant")&&target.hp()==1,"Relaxed hostile instant KO leaves vulnerable player at one HP");
        DamageCalculator.resolve(caster,target,mudo,guaranteedHit);check(target.hp()==1,"repeated instant KO cannot kill at one HP in Relaxed");
        var normal=battle(1,BattleDifficulty.NORMAL);var vulnerable=normal.find(id(1)).orElseThrow();vulnerable.setPersona(new PersonaInstance("orpheus",20,0,Set.of("agi")));
        DamageCalculator.resolve(normal.find(id(101)).orElseThrow(),vulnerable,mudo,guaranteedHit);check(!vulnerable.alive(),"Normal retains successful instant KO");
        for(var d:BattleDifficulty.values())for(int n:new int[]{1,2,3,4,5,12}){
            var timer=new PriestessRules(0,d,n);check(timer.remaining(0)==d.raidDuration(n),"raid difficulty and larger-party time budget");timer.update(74,100,new Random(1));long once=timer.remaining(1000);timer.update(74,100,new Random(2));check(timer.remaining(1000)==once,"phase penalty still applies once");
        }
        var gson=new GsonBuilder().create();var choice=new MenuSnapshot.DifficultyChoice("hard",false,"Somente o líder pode alterar.");check(gson.fromJson(gson.toJson(choice),MenuSnapshot.DifficultyChoice.class).equals(choice),"menu authority and choice round-trip");
        check(MenuSnapshot.empty().difficulty().id().equals("normal")&&!MenuSnapshot.empty().difficulty().editable(),"unsynchronized menu is read-only");
        for(int i=0;i<6;i++){var box=ReloadMenuDesign.row(8,i,false);check(box.y()+box.h()<ReloadMenuDesign.back().y(),"all System rows fit above footer");check(ReloadMenuDesign.pickRow(8,false,box.x()+1,box.y()+1)==i,"visible System row matches input");}
        var out=Path.of("build/reports/difficulty");Files.createDirectories(out);
        for(String loc:List.of("en_us","pt_br")){DisplayText.language(loc);check(!DisplayText.text("Dificuldade").isBlank(),"translated label");LocalizationAudit.render(out.resolve("system-"+loc+".png"),loc,4);}
        DisplayText.language("en_us");check(DisplayText.text("Dificuldade").equals("Difficulty"),"English selector");
        Files.writeString(out.resolve("validation.json"),new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("status","passed","checks",checks,"matrix",matrix,"testedInGame",false)));
        System.out.println("PASS difficulty: "+checks+" checks; 1..64 participants, 4 settings, leader policy, persistence, reserved hits, no HP reset, raid timers and System EN/PT.");
    }
}
