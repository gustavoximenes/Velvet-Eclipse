package com.gx.velveteclipse.awakening;

import com.gx.velveteclipse.darkhour.DarkHourService;
import com.gx.velveteclipse.forge.*;
import com.gx.velveteclipse.util.RuntimeBridge;
import java.util.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;

/** No world actors/blocks or persistent invulnerability: the stage exists only on its owner's client. */
@Mod.EventBusSubscriber(modid="velvet_eclipse")
public final class AwakeningService {
    private static final Map<UUID,Run> RUNS=new HashMap<>();
    private static final ResourceLocation EVOKER=new ResourceLocation("velvet_eclipse","model_evoker");
    private static long now(){return System.nanoTime()/1_000_000;}
    private static final class Run {
        final ServerPlayer player;
        final Object level;
        final Vec3 anchor;
        final float yaw,pitch;
        final String token=UUID.randomUUID().toString();
        final AwakeningTimeline.Session timeline=new AwakeningTimeline.Session(now());
        long heartbeat=now();
        Run(ServerPlayer p){player=p;level=p.m_9236_();anchor=p.m_20182_();yaw=p.m_146908_();pitch=p.m_146909_();}
    }
    public static boolean active(Object p){return p instanceof ServerPlayer s&&RUNS.containsKey(s.m_20148_());}
    public static boolean canBattle(Object p){return !active(p)&&PlayerPersonaData.load(p).hasPersona();}
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void use(PlayerInteractEvent.RightClickItem e){
        if(!EVOKER.equals(ForgeRegistries.ITEMS.getKey(e.getItemStack().m_41720_())))return;
        e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);
        if(e.getEntity() instanceof ServerPlayer p)start(p);
    }
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void block(PlayerInteractEvent.RightClickBlock e){
        if(active(e.getEntity())||EVOKER.equals(ForgeRegistries.ITEMS.getKey(e.getItemStack().m_41720_()))){e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);if(e.getEntity() instanceof ServerPlayer p&&!active(p))start(p);}
    }
    private static void start(ServerPlayer p){
        if(active(p))return;
        var data=PlayerPersonaData.load(p);
        if(data.hasPersona()||data.awakened()){RuntimeBridge.sendMessage(p,"Seu vínculo com uma Persona já foi despertado.");return;}
        if(!DarkHourService.active(p.m_20194_().m_129783_())){RuntimeBridge.sendMessage(p,"A Evoker só responde durante a Dark Hour.");return;}
        if(!p.m_6084_()||p.m_5833_()||p.m_20159_()||BattleService.current(p)!=null||com.gx.velveteclipse.raid.RaidService.inside(p)||com.gx.velveteclipse.room.VelvetRoomService.inside(p)||com.gx.velveteclipse.exploration.ExplorationService.blocksBattleStart(p))return;
        com.gx.velveteclipse.exploration.ExplorationService.expire(p);
        p.m_6915_();p.m_5810_();
        var run=new Run(p);RUNS.put(p.m_20148_(),run);
        p.m_20256_(Vec3.f_82478_);AwakeningNetwork.send(p,run.token,"start","");
    }
    public static void request(ServerPlayer p,AwakeningNetwork.Request request){
        var r=RUNS.get(p.m_20148_());if(r==null||!r.token.equals(request.token()))return;
        if(!valid(r)){stop(r,"cancel");return;}
        switch(request.action()){
            case "alive"->r.heartbeat=now();
            case "cancel"->stop(r,"cancel");
            case "choose"->{if(r.timeline.choose(request.persona(),now()))AwakeningNetwork.send(p,r.token,"continue",r.timeline.persona());}
            case "finish"->{
                if(!r.timeline.ready(now()))return;
                var data=PlayerPersonaData.load(p);
                if(data.hasPersona()||data.awakened()){stop(r,"cancel");return;}
                if(data.completeAwakening(r.timeline.persona())&&r.timeline.finish(now())){
                    data.save(p);
                    var achievement=p.m_20194_().m_129889_().m_136041_(new ResourceLocation("velvet_eclipse","awakening"));
                    if(achievement!=null)p.m_8960_().m_135988_(achievement,"awakened");
                    stop(r,"complete");BattleService.sync(p,"I am thou Thou art I");
                }
            }
        }
    }
    private static boolean valid(Run r){var p=r.player;return p.m_6084_()&&!p.m_213877_()&&!p.m_5833_()&&p.m_9236_()==r.level&&p.m_20275_(r.anchor.f_82479_,r.anchor.f_82480_,r.anchor.f_82481_)<16&&DarkHourService.active(p.m_20194_().m_129783_());}
    @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent e){
        if(e.phase!=TickEvent.Phase.END||!(e.player instanceof ServerPlayer p))return;
        var r=RUNS.get(p.m_20148_());if(r==null)return;
        if(!valid(r)||now()-r.heartbeat>30_000){stop(r,"cancel");return;}
        RuntimeBridge.hardFreezePlayer(p,new double[]{r.anchor.f_82479_,r.anchor.f_82480_,r.anchor.f_82481_},r.yaw,r.pitch);
        p.f_19789_=0;
    }
    private static void stop(Run r,String phase){if(!RUNS.remove(r.player.m_20148_(),r))return;r.player.m_20256_(Vec3.f_82478_);AwakeningNetwork.send(r.player,r.token,phase,r.timeline.persona());}
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){RUNS.remove(e.getEntity().m_20148_());}
    @SubscribeEvent public static void stopped(ServerStoppedEvent e){RUNS.clear();}
    @SubscribeEvent public static void damage(LivingAttackEvent e){if(active(e.getEntity()))e.setCanceled(true);}
    @SubscribeEvent public static void attack(AttackEntityEvent e){if(active(e.getEntity()))e.setCanceled(true);}
    @SubscribeEvent public static void mine(PlayerInteractEvent.LeftClickBlock e){if(active(e.getEntity()))e.setCanceled(true);}
    @SubscribeEvent public static void item(PlayerInteractEvent.RightClickItem e){if(active(e.getEntity()))e.setCanceled(true);}
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void entity(PlayerInteractEvent.EntityInteract e){interact(e);}
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void entityAt(PlayerInteractEvent.EntityInteractSpecific e){interact(e);}
    private static void interact(PlayerInteractEvent e){if(active(e.getEntity())||EVOKER.equals(ForgeRegistries.ITEMS.getKey(e.getItemStack().m_41720_()))){e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);if(e.getEntity() instanceof ServerPlayer p&&!active(p))start(p);}}
    @SubscribeEvent public static void container(PlayerContainerEvent.Open e){if(e.getEntity() instanceof ServerPlayer p&&active(p))p.m_6915_();}
}
