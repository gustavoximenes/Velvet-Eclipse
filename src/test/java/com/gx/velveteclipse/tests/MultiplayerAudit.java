package com.gx.velveteclipse.tests;

import com.gx.velveteclipse.core.battle.*;
import com.gx.velveteclipse.core.persona.*;
import com.gx.velveteclipse.network.*;
import com.gx.velveteclipse.client.*;
import com.gx.velveteclipse.forge.VelvetCommands;
import java.util.*;
import net.minecraft.network.FriendlyByteBuf;
import io.netty.buffer.Unpooled;

public final class MultiplayerAudit {
    private static int checks;
    private static void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
    private static UUID id(int n){return new UUID(0,n);}
    private static BattleCombatant ally(int n){return new BattleCombatant(id(n),"Player "+n,BattleSide.PLAYERS,new PersonaInstance("izanagi",n==1?20:1,0,Set.of("zio","mazio","dia","media","rakukaja")),250,100,90,90);}
    private static BattleSession battle(int count,int seed){
        var list=new ArrayList<BattleCombatant>();list.add(ally(1));
        for(int i=0;i<count;i++)list.add(new BattleCombatant(id(10+i),"Enemy "+i,BattleSide.ENEMIES,new PersonaInstance(PersonaRegistry.persona("umbra_slime").orElseThrow()),250,250,40,40));
        return new BattleSession(list,new Random(seed));
    }
    public static void main(String[] args)throws Exception{
        PersonaRegistry.bootstrap();
        var dispatcher=new com.mojang.brigadier.CommandDispatcher<net.minecraft.commands.CommandSourceStack>();VelvetCommands.register(dispatcher);
        var node=(com.mojang.brigadier.tree.ArgumentCommandNode<?,?>)dispatcher.findNode(List.of("velvet","battle","demo","count"));check(node!=null,"demo count command exists");
        for(int n=1;n<=5;n++){check(BattleFormation.validateCount(n)==n,"valid count");check(node.getType().parse(new com.mojang.brigadier.StringReader(""+n)).equals(n),"command accepts 1..5");
            var slots=new HashSet<Double>();for(int i=0;i<n;i++)check(slots.add(BattleFormation.enemyX(i,n)),"distinct enemy formation");
        }
        for(int n:new int[]{-1,0,6,100}){try{BattleFormation.validateCount(n);throw new AssertionError("invalid count accepted");}catch(IllegalArgumentException expected){checks++;}try{node.getType().parse(new com.mojang.brigadier.StringReader(""+n));throw new AssertionError("command limit missing");}catch(com.mojang.brigadier.exceptions.CommandSyntaxException expected){checks++;}}
        for(int count=1;count<=5;count++)for(int seed=0;seed<80;seed++){
            var direct=battle(count,seed);var delayed=battle(count,seed);check(delayed.currentActor().id().equals(id(1)),"player starts");
            var action=BattleAction.skill("mazio",id(9+count));var prepared=delayed.prepare(id(1),action);check(prepared!=null&&prepared.hits().size()==count,"all and only enemies reserved");
            var joining=ally(2);check(delayed.join(joining)&&direct.join(ally(2)),"join during reserved turn");
            check(delayed.prepared()==prepared&&delayed.currentActor().id().equals(id(1)),"joining preserves current reservation and turn");
            check(!delayed.join(joining),"no duplicate joins");
            var expected=direct.perform(id(1),action);var result=delayed.perform(id(1),action);check(result.equals(expected),"reserved group damage matches immediate simulation");
            check(joining.hp()==100&&delayed.find(id(1)).orElseThrow().sp()==80,"all allies unharmed, area cost paid once");
            for(int i=0;i<count;i++)check(delayed.find(id(10+i)).orElseThrow().hp()==direct.find(id(10+i)).orElseThrow().hp(),"each target resolved exactly once");
        }
        for(int removed:new int[]{1,2,3}){
            var s=battle(5,1);s.join(ally(2));s.join(ally(3));
            if(removed!=1)s.perform(id(1),BattleAction.guard());UUID current=s.currentActor().id();check(s.leave(id(removed)),"leave existing player");
            if(!current.equals(id(removed)))check(s.currentActor().id().equals(current),"other player leaving preserves turn");
            var visited=new HashSet<UUID>();int size=s.participants().size();for(int i=0;i<size;i++){var a=s.currentActor();check(visited.add(a.id()),"no duplicate turns after removal");s.perform(a.id(),BattleAction.guard());}check(visited.size()==size,"every remaining combatant acts once");
        }
        var interrupted=battle(2,2);interrupted.join(ally(2));var r=interrupted.prepare(id(1),BattleAction.skill("dia",id(2)));check(r!=null,"reserve ally heal");interrupted.leave(id(2));check(interrupted.prepared()==null&&interrupted.find(id(1)).orElseThrow().sp()==90,"departing target cancels without cost");
        var last=battle(1,0);last.leave(id(1));check(last.state()==BattleState.ENEMIES_WON&&last.currentActor()==null,"last ally leaving terminates session");check(!last.join(ally(2)),"finished battle refuses joining");
        for(String skill:List.of("dia","media","rakukaja")){
            var s=battle(3,8);var a=s.currentActor();var friend=ally(2);s.join(friend);var unrelated=ally(3);s.join(unrelated);int enemyHp=s.find(id(10)).orElseThrow().hp();
            var prepared=s.prepare(a.id(),BattleAction.skill(skill,friend.id()));check(prepared!=null&&prepared.hits().isEmpty()&&!prepared.cutin(),"support/heal has no damage or cut-in");
            s.perform(a.id(),prepared.action());check(s.find(id(10)).orElseThrow().hp()==enemyHp,"support leaves enemy HP untouched");
            if(skill.equals("dia"))check(friend.hp()==150&&a.hp()==100&&unrelated.hp()==100&&a.sp()==87,"Dia heals selected ally only and charges caster");
            if(skill.equals("media"))check(friend.hp()==140&&a.hp()==140&&unrelated.hp()==140&&a.sp()==83,"Media heals all living allies once");
            if(skill.equals("rakukaja"))check(friend.defenseMultiplier()==1.33&&a.defenseMultiplier()==1&&unrelated.defenseMultiplier()==1&&a.sp()==78,"buff goes to selected ally");
        }
        for(String skill:List.of("dia","rakukaja","zio")){
            var s=battle(2,1);s.join(ally(2));UUID wrong=skill.equals("zio")?id(2):id(10);check(s.prepare(id(1),BattleAction.skill(skill,wrong))==null,"reject wrong-side target");check(s.prepare(id(1),BattleAction.skill(skill,id(999)))==null,"reject target from another battle");check(s.currentActor().sp()==90,"invalid targets cost nothing");
        }
        packets();selection();
        System.out.println("PASS multiplayer: "+checks+" checks; 400 group reservations, 1..5 enemy commands, joining/leaving turns, chosen/all-ally healing, buffs, target selection and bounded network codecs.");
    }
    private static void packets(){
        var b=new FriendlyByteBuf(Unpooled.buffer());try{
            for(UUID target:new UUID[]{null,id(14)}){var action=new BattleActionC2S("skill","zio",target);BattleActionC2S.encode(action,b);check(action.equals(BattleActionC2S.decode(b)),"target action roundtrip");}
            var roster=new BattleRosterS2C(List.of(new BattleRosterS2C.Target(id(1),42,"Player",true,70,100,"izanagi","",true),new BattleRosterS2C.Target(id(10),43,"Enemy",false,85,85,"umbra_slime","ELECTRIC:WEAK",false)));BattleRosterS2C.encode(roster,b);check(roster.equals(BattleRosterS2C.decode(b)),"roster codec");
            var scene=new BattleSceneS2C(List.of(new BattleSceneS2C.Scene(id(50),"Leader",22,64,-30,5,true,List.of(43,44,45,46,47))));BattleSceneS2C.encode(scene,b);check(scene.equals(BattleSceneS2C.decode(b)),"observer codec");
            var bolt=new BattleLightningS2C(List.of(new BattleLightningS2C.Strike(42,63,-500,456)));BattleLightningS2C.encode(bolt,b);check(bolt.equals(BattleLightningS2C.decode(b)),"multiplayer effect codec");
            check(!b.isReadable(),"codecs consume exact payload");
            b.writeInt(6);try{BattleLightningS2C.decode(b);throw new AssertionError("unbounded effects");}catch(IllegalArgumentException expected){checks++;}
            b.clear();b.writeInt(-1);try{BattleRosterS2C.decode(b);throw new AssertionError("negative roster");}catch(IllegalArgumentException expected){checks++;}
        }finally{b.release();}
    }
    private static void selection(){
        var snapshot=new BattleSnapshotS2C(true,true,"izanagi","Izanagi",100,250,90,90,"Enemy",85,85,2,List.of("dia","rakukaja","zio"),List.of("izanagi"),"");ClientBattleState.accept(snapshot);
        BattleTargetHud.accept(new BattleRosterS2C(List.of(new BattleRosterS2C.Target(id(1),1,"Me",true,100,250,"izanagi","",true),new BattleRosterS2C.Target(id(2),2,"Friend",true,100,250,"izanagi","",false),new BattleRosterS2C.Target(id(10),10,"E1",false,85,85,"umbra_slime","",false),new BattleRosterS2C.Target(id(11),11,"E2",false,85,85,"umbra_slime","",false))));
        ClientBattleUiState.open(ClientBattleUiState.Page.SKILLS);check(ClientBattleUiState.selectIndex(0,snapshot),"select Dia");check(ClientBattleUiState.page()==ClientBattleUiState.Page.TARGET,"healing opens selector instead of immediately sending");check(BattleTargetHud.candidates().stream().allMatch(BattleRosterS2C.Target::ally),"healing lists only allies");BattleTargetHud.move(1);check(BattleTargetHud.selectedId().equals(id(2)),"cycle to friend");
        ClientBattleUiState.back();ClientBattleUiState.selectIndex(2,snapshot);check(BattleTargetHud.candidates().stream().noneMatch(BattleRosterS2C.Target::ally),"offensive skill lists enemies");BattleTargetHud.move(1);check(BattleTargetHud.selectedId().equals(id(11)),"choose second enemy");ClientBattleState.accept(BattleSnapshotS2C.empty());BattleTargetHud.reset();
    }
}
