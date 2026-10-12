package com.gx.velveteclipse.city;

import com.google.gson.Gson;
import java.io.*;import java.util.*;import java.util.zip.GZIPInputStream;
import com.gx.velveteclipse.room.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Furniture operates on the exact local tiles; computers never participate. */
@Mod.EventBusSubscriber(modid="velvet_eclipse")
public final class CityFurniture {
    public record Seat(String id,double x,double y,double z,float yaw,double[] bounds,int[] anchor,int tile){}
    public record Door(String id,int[][] cells,double[] bounds){}
    public record Catalogue(List<Seat> seats,List<Door> doors){}
    private static final Catalogue CATALOGUE=load();
    private static Catalogue load(){try(var in=new GZIPInputStream(Objects.requireNonNull(CityFurniture.class.getResourceAsStream("/data/velvet_eclipse/city/furniture.json.gz")))){return new Gson().fromJson(new InputStreamReader(in,java.nio.charset.StandardCharsets.UTF_8),Catalogue.class);}catch(IOException e){throw new IllegalStateException(e);}}
    public static Catalogue catalogue(){return CATALOGUE;}
    private static BlockPos pos(int[] c){return new BlockPos(c[0],c[1],c[2]);}
    private static AABB bounds(double[] b){return new AABB(b[0],b[1],b[2],b[3],b[4],b[5]);}
    public static boolean validSeat(VelvetSeatEntity e,String id){
        if(!CityService.inside(e))return false;
        var s=CATALOGUE.seats().stream().filter(v->v.id().equals(id)).findFirst().orElse(null);
        return s!=null&&e.m_9236_().m_7702_(pos(s.anchor())) instanceof CityArchitectureEntity tile&&tile.id()==s.tile();
    }
    private static int state(ServerLevel level,Door d){
        int found=-1;
        for(var c:d.cells()){
            var at=pos(c);int id=level.m_7702_(at) instanceof CityArchitectureEntity tile?tile.id():level.m_8055_(at).m_60795_()?-1:-2;
            if(matches(id,c[3]))continue;
            int mode=matches(id,c[4])?0:matches(id,c[5])?1:-2;if(mode<0||found>=0&&found!=mode)return -2;found=mode;
        }
        return found;
    }
    private static boolean matches(int actual,int expected){
        if(actual==expected)return true;
        var tile=CityArchitecture.tile(expected);
        return actual==-1&&tile!=null&&tile.length()==0&&tile.boxes().length==0;
    }
    private static boolean change(ServerLevel level,Door d,boolean open){
        if(state(level,d)==-2)return false;
        for(var c:d.cells()){
            var shape=CityArchitecture.shape(c[open?4:5]);
            for(var entity:level.m_45976_(LivingEntity.class,new AABB(pos(c)).m_82400_(.01)))
                if(entity.m_6084_())for(var box:shape.m_83299_())if(box.m_82386_(c[0],c[1],c[2]).m_82381_(entity.m_20191_()))return false;
        }
        for(var c:d.cells()){
            var at=pos(c);var target=CityArchitecture.tile(c[open?4:5]);
            if(target.length()==0&&target.boxes().length==0){level.m_7731_(at,net.minecraft.world.level.block.Blocks.f_50016_.m_49966_(),3);continue;}
            if(!(level.m_7702_(at) instanceof CityArchitectureEntity))level.m_7731_(at,CityRegistry.ARCHITECTURE.get().m_49966_(),3);
            if(level.m_7702_(at) instanceof CityArchitectureEntity tile)tile.configure(c[open?4:5]);
        }
        return true;
    }
    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent e){
        if(e.phase!=TickEvent.Phase.END)return;var server=net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
        if(server==null||server.m_129783_().m_46467_()%20!=0||!CityState.get(server).ready)return;
        var level=server.m_129880_(CityService.DIMENSION);if(level==null)return;
        for(var d:CATALOGUE.doors())for(var p:level.m_6907_())if(bounds(d.bounds()).m_82400_(12).m_82390_(p.m_20182_())){
            if(state(level,d)==-1)change(level,d,true);break;
        }
    }
    @SubscribeEvent public static void interact(PlayerInteractEvent.RightClickBlock e){
        if(e.getHand()!=InteractionHand.MAIN_HAND||!(e.getEntity() instanceof ServerPlayer p)||!CityService.inside(p)||!CityState.get(p.m_20194_()).ready)return;
        if(com.gx.velveteclipse.forge.BattleService.current(p)!=null||p.m_20159_())return;
        var hit=e.getHitVec().m_82450_();var level=p.m_284548_();
        if(p.m_20275_(hit.f_82479_,hit.f_82480_,hit.f_82481_)>25)return;
        for(var d:CATALOGUE.doors())if(Arrays.stream(d.cells()).anyMatch(c->pos(c).equals(e.getPos()))){
            // Only click the leaf's actual cell, not a distant GUI or arbitrary packet coordinate.
            int current=state(level,d);if(current==-2)continue;e.setCanceled(true);
            long now=System.currentTimeMillis();var tag=p.getPersistentData();if(now-tag.m_128454_("city_furniture_click")<300)return;tag.m_128356_("city_furniture_click",now);
            if(!change(level,d,current==1))p.m_5661_(com.gx.velveteclipse.localization.ModText.component("Afaste-se da porta para abri-la ou fechá-la."),true);return;
        }
        for(var s:CATALOGUE.seats())if(bounds(s.bounds()).m_82400_(.08).m_82390_(hit)){
            if(!(level.m_7702_(pos(s.anchor())) instanceof CityArchitectureEntity tile)||tile.id()!=s.tile())continue;e.setCanceled(true);
            if(!level.m_45976_(VelvetSeatEntity.class,new AABB(s.x()-.25,s.y()-.25,s.z()-.25,s.x()+.25,s.y()+.25,s.z()+.25)).isEmpty())return;
            var seat=VelvetRoomRegistry.SEAT.get().m_20615_(level);if(seat==null)return;seat.cityFurniture(s.id(),p.m_20182_());seat.m_6034_(s.x(),s.y(),s.z());seat.m_146922_(s.yaw());
            if(!level.m_7967_(seat))return;p.m_146922_(s.yaw());p.m_5616_(s.yaw());p.m_5618_(s.yaw());if(!p.m_7998_(seat,true))seat.m_146870_();else p.m_5661_(com.gx.velveteclipse.localization.ModText.component("SHIFT para levantar."),true);return;
        }
    }
    private CityFurniture(){}
}
