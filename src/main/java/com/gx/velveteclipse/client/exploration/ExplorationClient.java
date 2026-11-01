package com.gx.velveteclipse.client.exploration;

import com.gx.velveteclipse.client.*;
import com.gx.velveteclipse.exploration.*;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.*;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import static com.gx.velveteclipse.exploration.ExplorationRules.*;

@Mod.EventBusSubscriber(modid="velvet_eclipse",bus=Mod.EventBusSubscriber.Bus.FORGE,value=Dist.CLIENT)
public final class ExplorationClient {
    public static final KeyMapping TOGGLE=new KeyMapping("key.velvet_eclipse.exploration",KeyConflictContext.IN_GAME,InputConstants.Type.KEYSYM,72,"key.categories.velvet_eclipse");
    private static Mode mode=Mode.NORMAL;
    private static int tick,sequence=-1,requestTicks;
    private static float yaw;
    private static ClientLevel level;
    private static Vec3 start,end,attackStart;
    private static com.gx.velveteclipse.equipment.EquipmentCatalog.Family attackFamily=com.gx.velveteclipse.equipment.EquipmentCatalog.Family.SWORD;
    public static com.gx.velveteclipse.equipment.EquipmentCatalog.Family attackFamily(){return attackFamily;}
    private static boolean requestPending;
    public static Mode mode(){return cameraMode(PersonaCinematics.skillSequenceActive(),mode==Mode.BATTLE_ENTRY,ClientBattleState.snapshot().active(),mode==Mode.EXPLORATION_ATTACK,mode!=Mode.NORMAL);}
    public static boolean enabled(){return mode!=Mode.NORMAL;}
    public static boolean entry(){return mode==Mode.BATTLE_ENTRY;}
    public static boolean attacking(){return mode==Mode.EXPLORATION_ATTACK;}
    public static boolean swordVisible(){return !PersonaCinematics.skillSequenceActive()&&(ClientBattleState.snapshot().active()||mode==Mode.EXPLORATION||mode==Mode.EXPLORATION_ATTACK||mode==Mode.BATTLE_ENTRY);}
    public static double swordAttackTime(float partial){return PersonaCinematics.meleeActive()?com.gx.velveteclipse.core.battle.MeleeTimeline.slash(PersonaCinematics.meleeTicks()+partial):attacking()?tick+partial:-1;}
    public static boolean blocksMenu(){return attacking()||entry()||EntryTransition.visible();}
    public static int ticks(){return tick;}
    public static float yaw(){return yaw;}
    public static void accept(ExplorationNetwork.State state){
        if(state.mode()<0||state.mode()>=Mode.values().length)return;
        var mc=Minecraft.m_91087_();if(mc.f_91074_==null||mc.f_91073_==null)return;
        Mode next=Mode.values()[state.mode()];
        if(level==mc.f_91073_&&state.sequence()<sequence)return;
        if(next==Mode.BATTLE_ENTRY)EntryTransition.prepare(state.opening(),state.tick());
        else if(next!=Mode.BATTLE)EntryTransition.cancelPreparation();
        // A predicted swing keeps its elapsed time when the acknowledgement arrives.
        tick=next==Mode.EXPLORATION_ATTACK&&attacking()?Math.max(tick,state.tick()):state.tick();
        mode=next;sequence=state.sequence();yaw=state.yaw();level=mc.f_91073_;requestPending=false;
        if(entry()){
            start=new Vec3(state.sx(),state.sy(),state.sz());end=new Vec3(state.ex(),state.ey(),state.ez());
            mc.f_91074_.m_20256_(Vec3.f_82478_);
            if(mc.f_91080_!=null)mc.m_91152_(null);
        }else{start=null;end=null;}
    }
    /** Called once by the existing central camera tick, never by a second controller. */
    public static void tick(){
        EntryTransition.tick();
        com.gx.velveteclipse.client.ClientPersonaCutinOverlay.tick();
        var mc=Minecraft.m_91087_();
        if(mc.f_91074_==null||mc.f_91073_==null||!mc.f_91074_.m_6084_()||(level!=null&&level!=mc.f_91073_)){
            reset();while(TOGGLE.m_90859_()){}return;
        }
        while(TOGGLE.m_90859_())if(mc.f_91080_==null&&canToggle(mode())&&!requestPending){requestPending=true;requestTicks=0;ExplorationNetwork.request(ExplorationNetwork.TOGGLE);}
        if(requestPending&&++requestTicks>60){requestPending=false;if(attacking()){mode=Mode.EXPLORATION;tick=0;}}
        if(attacking()||entry())tick++;
        if(attacking())AttackAdvance.move(mc.f_91074_,attackStart,attackFamily,yaw,tick);
        if(attacking()&&tick>=com.gx.velveteclipse.core.battle.WeaponAttackTiming.end(attackFamily)&&!requestPending){mode=Mode.EXPLORATION;tick=0;}
        if(entry()&&start!=null&&end!=null){
            Vec3 desired=start.m_165921_(end,entryProgress(tick));
            mc.f_91074_.m_20256_(Vec3.f_82478_);
            mc.f_91074_.m_6478_(MoverType.SELF,desired.m_82546_(mc.f_91074_.m_20182_()));
            mc.f_91074_.m_146922_(yaw);mc.f_91074_.m_146926_(0);
        }
        ExplorationAnimation.update(mc.f_91074_);
    }
    public static void reset(){mode=Mode.NORMAL;tick=0;sequence=-1;level=null;start=end=null;requestPending=false;ExplorationAnimation.clear();EntryTransition.reset();com.gx.velveteclipse.client.EvokerPlayerAnimation.clear();com.gx.velveteclipse.client.ClientPersonaCutinOverlay.reset();PersonaCinematics.reset();BattleTargetHud.reset();PersonaSelectorPortrait.reset();}
    @SubscribeEvent public static void mouse(InputEvent.MouseButton.Pre e){
        var mc=Minecraft.m_91087_();if(mc.f_91080_!=null)return;
        if(entry()||EntryTransition.visible()){e.setCanceled(true);return;}
        if(!ClientBattleState.snapshot().active()&&swordVisible()&&e.getButton()==0){
            e.setCanceled(true);
            if(e.getAction()==1&&canAttack(mode())&&!requestPending){attackFamily=com.gx.velveteclipse.client.equipment.EquipmentScreen.family();attackStart=mc.f_91074_.m_20182_();mode=Mode.EXPLORATION_ATTACK;tick=0;yaw=mc.f_91074_.m_146908_();requestPending=true;requestTicks=0;ExplorationNetwork.request(ExplorationNetwork.ATTACK);}
        }
    }
    @SubscribeEvent public static void interaction(InputEvent.InteractionKeyMappingTriggered e){
        if(entry()||EntryTransition.visible()||(swordVisible()&&e.isAttack())){e.setCanceled(true);e.setSwingHand(false);}
    }
    @SubscribeEvent public static void movement(MovementInputUpdateEvent e){
        if(!entry()&&!attacking()&&!ClientBattleState.snapshot().active())return;var in=e.getInput();in.f_108566_=0;in.f_108567_=0;
        in.f_108568_=in.f_108569_=in.f_108570_=in.f_108571_=in.f_108572_=in.f_108573_=false;
    }
    @SubscribeEvent public static void open(ScreenEvent.Opening e){if((entry()||EntryTransition.visible())&&Minecraft.m_91087_().f_91074_!=null&&Minecraft.m_91087_().f_91074_.m_6084_())e.setCanceled(true);}
    @Mod.EventBusSubscriber(modid="velvet_eclipse",bus=Mod.EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static class Registration {@SubscribeEvent public static void keys(RegisterKeyMappingsEvent e){e.register(TOGGLE);}}
}
