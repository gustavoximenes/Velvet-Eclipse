package com.gx.velveteclipse.exploration;

import com.gx.velveteclipse.forge.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.*;
import static com.gx.velveteclipse.exploration.ExplorationRules.*;

/** Authoritative preparation. No BattleSession exists before ENTRY_READY. */
@Mod.EventBusSubscriber(modid="velvet_eclipse",bus=Mod.EventBusSubscriber.Bus.FORGE)
public final class ExplorationService {
    private static final Map<UUID,State> PLAYERS=new HashMap<>();
    private static final EncounterReservation RESERVATIONS=new EncounterReservation();
    private static final class State {
        final ServerPlayer player; final Level level;
        Mode mode=Mode.EXPLORATION; int tick,sequence; boolean commit;
        com.gx.velveteclipse.core.battle.EncounterOpening opening;
        Entity shadow; Vec3 start,end,shadowAnchor; boolean previousNoAi; float yaw;
        State(ServerPlayer p){player=p;level=p.m_9236_();}
    }
    public static boolean active(Object p){return p instanceof ServerPlayer player&&PLAYERS.containsKey(player.m_20148_());}
    public static boolean preparing(Object p){State s=p instanceof ServerPlayer player?PLAYERS.get(player.m_20148_()):null;return s!=null&&s.mode==Mode.BATTLE_ENTRY;}
    public static boolean blocksBattleStart(Object p){State s=p instanceof ServerPlayer player?PLAYERS.get(player.m_20148_()):null;return s!=null&&s.mode==Mode.BATTLE_ENTRY&&!s.commit;}
    public static boolean reserved(Object entity){return entity instanceof Entity e&&RESERVATIONS.reserved(e.m_20148_());}
    public static void request(ServerPlayer p,int action){
        if(!p.m_6084_()||p.m_5833_()||p.m_20159_())return;
        State s=PLAYERS.get(p.m_20148_());
        if(action==ExplorationNetwork.TOGGLE){
            if(BattleService.current(p)!=null)return;
            if(s==null){s=new State(p);PLAYERS.put(p.m_20148_(),s);send(s);}
            else if(canToggle(s.mode)){release(s);PLAYERS.remove(p.m_20148_());ExplorationNetwork.reply(p,Mode.NORMAL,0,s.sequence,-1,0);}
        }else if(action==ExplorationNetwork.ATTACK&&s!=null&&canAttack(s.mode)&&BattleService.current(p)==null){
            s.mode=Mode.EXPLORATION_ATTACK;s.tick=0;s.sequence++;s.yaw=p.m_146908_();send(s);
        }
    }
    public static void tickServer(){
        for(var it=PLAYERS.entrySet().iterator();it.hasNext();){
            State s=it.next().getValue();ServerPlayer p=s.player;
            if(!p.m_6084_()||p.m_9236_()!=s.level||p.m_213877_()||p.m_5833_()||p.m_20159_()){
                release(s);it.remove();ExplorationNetwork.reply(p,Mode.NORMAL,0,s.sequence,-1,0);continue;
            }
            if(BattleService.current(p)!=null){
                if(s.mode!=Mode.BATTLE){release(s);s.mode=Mode.BATTLE;s.tick=0;send(s);}continue;
            }
            if(s.mode==Mode.BATTLE){s.mode=Mode.EXPLORATION;s.tick=0;send(s);continue;}
            if(s.mode==Mode.EXPLORATION_ATTACK){
                ++s.tick;
                if(hitWindow(s.tick)){
                    Entity target=findTarget(s);
                    if(target!=null&&RESERVATIONS.reserve(p.m_20148_(),target.m_20148_())){prepare(s,target);continue;}
                }
                if(s.tick>=ATTACK_END){s.mode=Mode.EXPLORATION;s.tick=0;send(s);}
            }else if(s.mode==Mode.BATTLE_ENTRY){
                if(s.shadow==null||!s.shadow.m_6084_()||s.shadow.m_9236_()!=s.level){cancel(s);continue;}
                ++s.tick;freezeShadow(s);
                Vec3 desired=s.start.m_165921_(s.end,entryProgress(s.tick));
                p.m_20256_(Vec3.f_82478_);
                // Native collision solver; no teleport or packet per animation frame.
                p.m_6478_(MoverType.SELF,desired.m_82546_(p.m_20182_()));
                p.m_146922_(s.yaw);p.m_146926_(0);
                if(s.tick>=ENTRY_READY){
                    p.m_20256_(Vec3.f_82478_);s.commit=true;
                    p.f_8906_.m_9774_(p.m_20185_(),p.m_20186_(),p.m_20189_(),s.yaw,0);
                    try { ShadowWorldService.beginReservedEncounter(p,s.shadow,s.opening); }
                    finally {s.commit=false;release(s);}
                    s.mode=BattleService.current(p)!=null?Mode.BATTLE:Mode.EXPLORATION;s.tick=0;send(s);
                }
            }
        }
    }
    private static Entity findTarget(State s){
        ServerPlayer p=s.player;double yaw=Math.toRadians(s.yaw),fx=-Math.sin(yaw),fz=Math.cos(yaw);
        var family=com.gx.velveteclipse.equipment.EquipmentData.load(p).bonuses().family();
        double reach=switch(family){case BOW,GUN->10;case SPEAR->4;case GREATSWORD,AXE->3.3;case FISTS,KNIFE->2.2;default->REACH;};
        Entity best=null;double distance=Double.MAX_VALUE;
        for(Entity e:s.level.m_6443_(Entity.class,p.m_20191_().m_82400_(reach),e->ShadowWorldService.worldShadow(e)&&e.m_6084_()&&!reserved(e))){
            double dx=e.m_20185_()-p.m_20185_(),dy=e.m_20186_()-p.m_20186_(),dz=e.m_20189_()-p.m_20189_();
            if(!(Math.abs(dy)<=1.8&&dx*dx+dz*dz<=reach*reach&&dx*dx+dz*dz>.0001&&(dx*fx+dz*fz)/Math.sqrt(dx*dx+dz*dz)>=(family==com.gx.velveteclipse.equipment.EquipmentCatalog.Family.BOW||family==com.gx.velveteclipse.equipment.EquipmentCatalog.Family.GUN?.98:CONE_COS))||dx*dx+dz*dz>=distance)continue;
            Vec3 aim=e.m_20182_().m_82520_(0,Math.min(1,e.m_20206_()*.6),0);
            if(s.level.m_45547_(new ClipContext(p.m_146892_(),aim,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p)).m_6662_()!=HitResult.Type.MISS)continue;
            distance=dx*dx+dz*dz;best=e;
        }
        return best;
    }
    private static void prepare(State s,Entity target){
        s.opening=ShadowWorldService.opening(s.player,target);s.shadow=target;s.shadowAnchor=target.m_20182_();
        if(target instanceof Mob mob){s.previousNoAi=mob.m_21525_();mob.m_21557_(true);mob.m_21573_().m_26573_();}
        s.start=s.player.m_20182_();s.end=s.start;
        Vec3 delta=target.m_20182_().m_82546_(s.start);double horizontal=Math.sqrt(delta.f_82479_*delta.f_82479_+delta.f_82481_*delta.f_82481_);
        s.yaw=(float)Math.toDegrees(Math.atan2(-delta.f_82479_,delta.f_82481_));
        double distance=Math.max(0,Math.min(1.45,horizontal-1.0));
        // Check the full body and support below each step before reserving the path.
        if(horizontal>.001)for(int i=1;i<=12;i++){
            Vec3 candidate=s.start.m_82520_(delta.f_82479_/horizontal*distance*i/12,0,delta.f_82481_/horizontal*distance*i/12);
            Vec3 offset=candidate.m_82546_(s.start);
            if(!s.level.m_45756_(s.player,s.player.m_20191_().m_82383_(offset)))break;
            var floor=s.level.m_45547_(new ClipContext(candidate.m_82520_(0,.1,0),candidate.m_82520_(0,-.5,0),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,s.player));
            if(floor.m_6662_()==HitResult.Type.MISS)break;
            s.end=candidate;
        }
        s.mode=Mode.BATTLE_ENTRY;s.tick=0;s.player.m_20256_(Vec3.f_82478_);freezeShadow(s);send(s);
    }
    private static void freezeShadow(State s){s.shadow.m_20256_(Vec3.f_82478_);s.shadow.m_6034_(s.shadowAnchor.f_82479_,s.shadowAnchor.f_82480_,s.shadowAnchor.f_82481_);}
    private static void release(State s){
        RESERVATIONS.release(s.player.m_20148_());
        if(s.shadow instanceof Mob mob&&!mob.m_213877_()&&!com.gx.velveteclipse.forge.ShadowWorldService.held(mob))mob.m_21557_(s.previousNoAi);
        s.shadow=null;s.start=null;s.end=null;s.player.m_20256_(Vec3.f_82478_);
    }
    public static void expire(ServerPlayer p){State s=PLAYERS.get(p.m_20148_());if(s!=null)cancel(s);}
    private static void cancel(State s){release(s);s.mode=Mode.EXPLORATION;s.tick=0;send(s);}
    private static void send(State s){ExplorationNetwork.reply(s.player,s.mode,s.tick,s.sequence,s.shadow==null?-1:s.shadow.m_19879_(),s.yaw,s.start==null?s.player.m_20182_():s.start,s.end==null?s.player.m_20182_():s.end);}
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){State s=PLAYERS.remove(e.getEntity().m_20148_());if(s!=null)release(s);}
    @SubscribeEvent public static void stop(ServerStoppedEvent e){for(State s:PLAYERS.values())release(s);PLAYERS.clear();RESERVATIONS.clear();}
    @SubscribeEvent public static void attack(AttackEntityEvent e){if(active(e.getEntity()))e.setCanceled(true);else if(e.getTarget() instanceof com.gx.velveteclipse.shadow.FieldShadowEntity){e.setCanceled(true);if(e.getEntity() instanceof ServerPlayer p)ShadowWorldService.attackShadow(p,e.getTarget());}}
    @SubscribeEvent public static void mine(PlayerInteractEvent.LeftClickBlock e){if(active(e.getEntity()))e.setCanceled(true);}
    @SubscribeEvent public static void useBlock(PlayerInteractEvent.RightClickBlock e){if(preparing(e.getEntity()))e.setCanceled(true);}
    @SubscribeEvent public static void useItem(PlayerInteractEvent.RightClickItem e){if(preparing(e.getEntity()))e.setCanceled(true);}
    @SubscribeEvent public static void interact(PlayerInteractEvent.EntityInteract e){if(preparing(e.getEntity()))e.setCanceled(true);}
    @SubscribeEvent public static void damage(LivingAttackEvent e){if(reserved(e.getEntity())||preparing(e.getEntity()))e.setCanceled(true);}
}
